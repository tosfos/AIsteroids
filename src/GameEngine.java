import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameEngine implements Runnable {
    /** Input actions queued from the EDT and consumed on the game thread. */
    public enum InputAction {
        TURN_LEFT_ON, TURN_LEFT_OFF,
        TURN_RIGHT_ON, TURN_RIGHT_OFF,
        ACCELERATE_ON, ACCELERATE_OFF,
        FIRE
    }

    // List of active game objects (player, asteroids, bullets)
    private List<GameObject> gameObjects;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ConcurrentLinkedQueue<InputAction> inputQueue = new ConcurrentLinkedQueue<>();
    private volatile double lastDeltaTime = 1.0 / GameConfig.TARGET_FPS;

    public static final int WIDTH = GameConfig.SCREEN_WIDTH;
    public static final int HEIGHT = GameConfig.SCREEN_HEIGHT;

    // Thread for running the game loop
    private Thread gameThread;
    // A lock object to synchronize access to the gameObjects list.
    private final Object lock = new Object();

    // Keep a reference to the player ship.
    private PlayerShip player;

    // Reference to game panel for particle effects
    private GamePanel gamePanel;

    // Wave system for progressive difficulty
    private WaveSystem waveSystem;

    // Collision handler for managing object collisions
    private CollisionHandler collisionHandler;

    private volatile int score = 0;
    private volatile boolean gameOver = false;

    // Controlled thread management
    private ExecutorService waveManagerExecutor;
    private final AtomicBoolean waveManagerRunning = new AtomicBoolean(false);

    public GameEngine() {
        this(false);
    }

    /** Lightweight constructor for unit tests (skips audio and wave manager). */
    GameEngine(boolean forTesting) {
        gameObjects = new ArrayList<>();

        try {
            if (!forTesting) {
                SoundManager.startAmbientSpace();
                MusicSystem.startMusic();
            }

            waveSystem = new WaveSystem();
            collisionHandler = new CollisionHandler(this);

            if (!forTesting) {
                LeaderboardSystem.gameStarted();
            }

            player = new PlayerShip(WIDTH / 2, HEIGHT / 2);
            addGameObject(player);

            if (!forTesting) {
                spawnWaveAsteroids();
                startWaveManager();
            }
        } catch (GameInitializationException e) {
            cleanup();
            throw e;
        } catch (RuntimeException e) {
            cleanup();
            throw new GameInitializationException("Failed to initialize game: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Error initializing GameEngine: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            cleanup();
            throw new GameInitializationException("Failed to initialize game", e);
        }
    }

    /** Creates a minimal engine instance for collision/input unit tests. */
    public static GameEngine createForTesting() {
        return new GameEngine(true);
    }

    private void startWaveManager() {
        waveManagerExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "WaveManager");
            t.setDaemon(true);
            return t;
        });

        waveManagerRunning.set(true);
        waveManagerExecutor.submit(() -> {
            while (waveManagerRunning.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(GameConfig.Threading.WAVE_MANAGER_CHECK_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                if (!waveManagerRunning.get()) break;

                try {
                    // Spawn power-ups based on wave system
                    WaveSystem.PowerUpSpawnInfo powerUpInfo = waveSystem.getPowerUpSpawnInfo();
                    if (RANDOM.nextDouble() < powerUpInfo.spawnChance) {
                        addGameObject(PowerUp.createRandomPowerUp(WIDTH, HEIGHT));
                    }

                    // Check if we need to spawn more asteroids for current wave
                    checkAndSpawnAsteroids();
                } catch (RuntimeException e) {
                    System.err.println("Error in wave manager (runtime): " + e.getClass().getSimpleName() + " - " + e.getMessage());
                    e.printStackTrace();
                    // Don't break loop for runtime exceptions, but log them
                } catch (Exception e) {
                    System.err.println("Error in wave manager (unexpected): " + e.getClass().getSimpleName() + " - " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    public void start() {
        if (running.compareAndSet(false, true)) {
            gameThread = new Thread(this, "GameEngineThread");
            gameThread.start();
        }
    }

    public void stop() {
        running.set(false);
        cleanup();
    }

    private void cleanup() {
        // Stop wave manager
        if (waveManagerRunning.get()) {
            waveManagerRunning.set(false);
            if (waveManagerExecutor != null) {
                waveManagerExecutor.shutdown();
                try {
                    if (!waveManagerExecutor.awaitTermination(GameConfig.Threading.THREAD_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                        waveManagerExecutor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    waveManagerExecutor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }

        // Stop audio systems
        try {
            SoundManager.stopAmbientSpace();
            MusicSystem.stopMusic();
        } catch (RuntimeException e) {
            System.err.println("Error stopping audio systems: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            // Non-critical, continue cleanup
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                long now = System.nanoTime();
                double deltaTime = (now - lastTime) / 1e9;  // Convert nanoseconds to seconds.
                lastTime = now;

                update(deltaTime);
                if (player != null && !player.isAlive() && !gameOver) {
                    gameOver = true;
                    try {
                        SoundManager.playGameOver(); // Play game over sound
                        SoundManager.stopAmbientSpace(); // Stop ambient sound
                        MusicSystem.stopMusic(); // Stop dynamic music

                        // Record final stats and add to leaderboard
                        LeaderboardSystem.gameEnded(score, waveSystem.getCurrentWave());
                        LeaderboardSystem.addScore(LeaderboardSystem.getPlayerName(), score, waveSystem.getCurrentWave());
                    } catch (RuntimeException e) {
                        System.err.println("Error handling game over: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                        // Non-critical, game over state is still set
                    }
                }

                // Record frame for performance monitoring
                PerformanceMonitor.recordFrame();

                // Check memory pressure periodically
                PerformanceMonitor.checkMemoryPressure();

                // Sleep briefly (approximate 60 FPS update rate)
                Thread.sleep(GameConfig.FRAME_TIME_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (RuntimeException e) {
                System.err.println("Error in game loop: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                e.printStackTrace();
                // Continue running unless it's a critical error
            }
        }
        cleanup();
    }

    public void update(double deltaTime) {
        lastDeltaTime = deltaTime;
        // Don't update game state if game is over
        if (gameOver) {
            return;
        }

        // Update dynamic music intensity based on game state
        updateMusicIntensity();

        // Combine synchronized blocks to reduce lock contention
        synchronized(lock) {
            processInput();
            // No need for copy since we're already synchronized
            updateGameObjects(deltaTime);
            processCollisions();
            removeDeadObjects();
            wrapObjectsAroundScreen();
            awardPendingWaveBonus();
        }
    }

    private void processInput() {
        InputAction action;
        while ((action = inputQueue.poll()) != null) {
            switch (action) {
                case TURN_LEFT_ON: player.setTurnLeft(true); break;
                case TURN_LEFT_OFF: player.setTurnLeft(false); break;
                case TURN_RIGHT_ON: player.setTurnRight(true); break;
                case TURN_RIGHT_OFF: player.setTurnRight(false); break;
                case ACCELERATE_ON: player.setAccelerating(true); break;
                case ACCELERATE_OFF: player.setAccelerating(false); break;
                case FIRE:
                    List<Projectile> bullets = player.fireBullet();
                    if (!bullets.isEmpty()) {
                        waveSystem.recordBulletShot();
                    }
                    for (Projectile bullet : bullets) {
                        addGameObject(bullet);
                    }
                    break;
            }
        }
    }

    /**
     * Queues a player input action to be processed on the game thread.
     */
    public void queueInput(InputAction action) {
        inputQueue.offer(action);
    }

    public double getLastDeltaTime() {
        return lastDeltaTime;
    }

    private void awardPendingWaveBonus() {
        int bonus = waveSystem.consumePendingWaveBonus();
        if (bonus > 0) {
            addScore(bonus);
        }
    }

    private void updateGameObjects(double deltaTime) {
        // Direct iteration is safe since caller holds the lock
        for (GameObject obj : gameObjects) {
            obj.update(deltaTime);
        }
    }

    private void processCollisions() {
        collisionHandler.beginCollisionFrame();
        // Optimized collision detection with early exits
        int size = gameObjects.size();
        for (int i = 0; i < size; i++) {
            GameObject a = gameObjects.get(i);
            if (!a.isAlive()) continue; // Early exit for dead objects

            for (int j = i + 1; j < size; j++) {
                GameObject b = gameObjects.get(j);
                if (!b.isAlive()) continue; // Early exit for dead objects

                if (checkObjectsCollide(a, b)) {
                    collisionHandler.handleCollision(a, b);
                }
            }
        }
        collisionHandler.endCollisionFrame();
    }

    private boolean checkObjectsCollide(GameObject a, GameObject b) {
        // Laser beam uses segment-circle intersection along full beam path
        if (a instanceof LaserBeam && b instanceof Asteroid) {
            return collisionHandler.checkLaserAsteroidCollision((LaserBeam) a, (Asteroid) b);
        }
        if (b instanceof LaserBeam && a instanceof Asteroid) {
            return collisionHandler.checkLaserAsteroidCollision((LaserBeam) b, (Asteroid) a);
        }

        // Standard circular collision for all other pairs
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double distance = dx * dx + dy * dy; // Avoid sqrt for performance
        double maxDistance = a.getRadius() + b.getRadius();
        return distance <= maxDistance * maxDistance;
    }

    private void removeDeadObjects() {
        // Remove objects that are no longer "alive"
        gameObjects.removeIf(obj -> !obj.isAlive());
    }

    private void wrapObjectsAroundScreen() {
        // Use GameObject's built-in wrapping method to avoid duplication
        for (GameObject obj : gameObjects) {
            obj.wrapAroundScreen();
        }
    }

    /**
     * Notifies the wave system that the player has taken damage.
     * Used by CollisionHandler when player-asteroid collision occurs.
     */
    void notifyPlayerDamaged() {
        if (waveSystem != null) {
            waveSystem.playerDamaged();
        }
    }

    /**
     * Notifies the wave system that a power-up was collected.
     */
    void notifyPowerUpCollected() {
        if (waveSystem != null) {
            waveSystem.recordPowerUpCollected();
        }
    }

    /**
     * Shows a power-up message on the game panel.
     * Used by CollisionHandler when player collects a power-up.
     *
     * @param type The type of power-up collected
     */
    void showPowerUpMessage(PowerUp.PowerUpType type) {
        if (gamePanel != null) {
            gamePanel.showPowerUpMessage(type);
        }
    }

    public void addGameObject(GameObject obj) {
        synchronized(lock) {
            gameObjects.add(obj);
        }
    }

    /**
     * Provides a thread-safe snapshot of the game objects for rendering.
     *
     * @return A defensive copy of the current game objects list
     */
    public List<GameObject> getGameObjects() {
        synchronized(lock) {
            return new ArrayList<>(gameObjects);
        }
    }

    public PlayerShip getPlayer() {
        return player;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        score += points;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void restart() {
        synchronized(lock) {
            // Clear all existing objects
            gameObjects.clear();

            // Reset score
            score = 0;

            // Reset existing player ship
            if (player != null) {
                player.reset();
                addGameObject(player);
            }

            // Reset wave system
            if (waveSystem != null) {
                waveSystem.reset();
            }

            // Spawn initial wave asteroids
            spawnWaveAsteroids();

            // Reset game over flag
            gameOver = false;

            // Restart ambient sound
            SoundManager.startAmbientSpace();

            // Restart dynamic music
            MusicSystem.startMusic();

            // Clear particle effects
            if (gamePanel != null) {
                gamePanel.getParticleSystem().clear();
            }
        }
    }

    public void setGamePanel(GamePanel panel) {
        this.gamePanel = panel;
    }

    public void createExplosionEffect(double x, double y, int intensity) {
        if (gamePanel != null) {
            gamePanel.getParticleSystem().createExplosion(x, y, intensity);
        }
    }

    public void createDebrisEffect(double x, double y, int count) {
        if (gamePanel != null) {
            gamePanel.getParticleSystem().createDebris(x, y, count);
        }
    }

    public void createWarpEffect(double x, double y) {
        if (gamePanel != null) {
            gamePanel.getParticleSystem().createWarpEffect(x, y);
        }
    }

    public void createImpactSparks(double x, double y, double angle) {
        if (gamePanel != null) {
            gamePanel.getParticleSystem().createImpactSparks(x, y, angle);
        }
    }

    private void updateMusicIntensity() {
        // Use a snapshot of game objects to minimize lock contention
        List<GameObject> snapshot;
        boolean hasPowerUp;

        synchronized(lock) {
            snapshot = new ArrayList<>(gameObjects);
            hasPowerUp = (player != null && !player.getActivePowerUps().isEmpty());
        }

        // Count asteroids outside the synchronized block
        int asteroidCount = 0;
        for (GameObject obj : snapshot) {
            if (obj instanceof Asteroid) {
                asteroidCount++;
            }
        }

        int playerLives = (player != null) ? player.getLives() : 0;
        MusicSystem.updateMusicIntensity(asteroidCount, playerLives, hasPowerUp);
    }

    private void spawnWaveAsteroids() {
        int asteroidsToSpawn = waveSystem.getAsteroidsRemaining();
        synchronized(lock) {
            try {
                for (int i = 0; i < asteroidsToSpawn; i++) {
                    WaveSystem.AsteroidSpawnInfo spawnInfo = waveSystem.getSpawnInfo();
                    Asteroid asteroid = createAsteroidWithDifficulty(spawnInfo);
                    addGameObject(asteroid);
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.err.println("Error spawning asteroids: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                throw e; // Propagate to allow caller to handle wave system sync
            } catch (Exception e) {
                System.err.println("Unexpected error spawning asteroids: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to spawn asteroids", e);
            }
        }
    }

    private void checkAndSpawnAsteroids() {
        // Spawn asteroids when screen is clear but wave expects more
        synchronized(lock) {
            int currentAsteroids = 0;
            for (GameObject obj : gameObjects) {
                if (obj instanceof Asteroid) {
                    currentAsteroids++;
                }
            }

            if (currentAsteroids == 0 && waveSystem.getAsteroidsRemaining() > 0) {
                try {
                    spawnWaveAsteroids();
                } catch (IllegalArgumentException | IllegalStateException e) {
                    System.err.println("Error spawning wave asteroids: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                    waveSystem.reset();
                } catch (Exception e) {
                    System.err.println("Unexpected error spawning wave asteroids: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                    e.printStackTrace();
                    waveSystem.reset();
                }
            }
        }
    }

    // Reusable Random instance to avoid allocations
    private static final Random RANDOM = new Random();

    private Asteroid createAsteroidWithDifficulty(WaveSystem.AsteroidSpawnInfo spawnInfo) {
        // Create asteroid at screen edge
        double x, y;

        if (RANDOM.nextBoolean()) {
            x = RANDOM.nextBoolean() ? -GameConfig.Asteroid.SPAWN_MARGIN : WIDTH + GameConfig.Asteroid.SPAWN_MARGIN;
            y = RANDOM.nextDouble() * HEIGHT;
        } else {
            x = RANDOM.nextDouble() * WIDTH;
            y = RANDOM.nextBoolean() ? -GameConfig.Asteroid.SPAWN_MARGIN : HEIGHT + GameConfig.Asteroid.SPAWN_MARGIN;
        }

        // Calculate velocity towards screen center with some randomness
        double angle = java.lang.Math.atan2(HEIGHT/2 - y, WIDTH/2 - x) + (RANDOM.nextDouble() - 0.5) * GameConfig.Angles.PI_OVER_2;
        double vx = spawnInfo.speed * Math.cos(angle);
        double vy = spawnInfo.speed * Math.sin(angle);

        return new Asteroid(x, y, spawnInfo.size, vx, vy, spawnInfo.splitCount);
    }

    public WaveSystem getWaveSystem() {
        return waveSystem;
    }
}
