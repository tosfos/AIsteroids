import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GameEngine implements Runnable {
    // List of active game objects (player, asteroids, bullets)
    private List<GameObject> gameObjects;
    private final AtomicBoolean running = new AtomicBoolean(false);

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

    private volatile int score = 0;
    private volatile boolean gameOver = false;

    // Controlled thread management
    private ExecutorService waveManagerExecutor;
    private final AtomicBoolean waveManagerRunning = new AtomicBoolean(false);

    public GameEngine() {
       gameObjects = new ArrayList<>();

       try {
           // Start ambient space sound
           SoundManager.startAmbientSpace();

           // Start dynamic music
           MusicSystem.startMusic();

           // Initialize wave system
           waveSystem = new WaveSystem();

           // Track game start
           LeaderboardSystem.gameStarted();

           // Create player ship at center.
           player = new PlayerShip(WIDTH / 2, HEIGHT / 2);
           addGameObject(player);

           // Spawn initial wave of asteroids based on wave system
           spawnWaveAsteroids();

           // Start controlled wave manager thread
           startWaveManager();
       } catch (Exception e) {
           System.err.println("Error initializing GameEngine: " + e.getMessage());
           cleanup();
           throw new RuntimeException("Failed to initialize game", e);
       }
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
                    if (Math.random() < powerUpInfo.spawnChance) {
                        addGameObject(PowerUp.createRandomPowerUp(WIDTH, HEIGHT));
                    }

                    // Check if we need to spawn more asteroids for current wave
                    checkAndSpawnAsteroids();
                } catch (Exception e) {
                    System.err.println("Error in wave manager: " + e.getMessage());
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
        } catch (Exception e) {
            System.err.println("Error stopping audio systems: " + e.getMessage());
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
                if (!player.isAlive() && !gameOver) {
                    gameOver = true;
                    try {
                        SoundManager.playGameOver(); // Play game over sound
                        SoundManager.stopAmbientSpace(); // Stop ambient sound
                        MusicSystem.stopMusic(); // Stop dynamic music

                        // Record final stats and add to leaderboard
                        LeaderboardSystem.gameEnded(score, waveSystem.getCurrentWave());
                        LeaderboardSystem.addScore(LeaderboardSystem.getPlayerName(), score, waveSystem.getCurrentWave());
                    } catch (Exception e) {
                        System.err.println("Error handling game over: " + e.getMessage());
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
            } catch (Exception e) {
                System.err.println("Error in game loop: " + e.getMessage());
                // Continue running unless it's a critical error
            }
        }
        cleanup();
    }

    public void update(double deltaTime) {
        // Don't update game state if game is over
        if (gameOver) {
            return;
        }

        // Update dynamic music intensity based on game state
        updateMusicIntensity();

        synchronized(lock) {
            updateGameObjects(deltaTime);
            processCollisions();
            removeDeadObjects();
        }

        // Screen wrapping for all game objects.
        synchronized(lock) {
            wrapObjectsAroundScreen();
        }
    }

    private void updateGameObjects(double deltaTime) {
        // Update every game object using a copy to avoid concurrent modification
        for (GameObject obj : new ArrayList<>(gameObjects)) {
            obj.update(deltaTime);
        }
    }

    private void processCollisions() {
        // Optimized collision detection with early exits
        int size = gameObjects.size();
        for (int i = 0; i < size; i++) {
           GameObject a = gameObjects.get(i);
           if (!a.isAlive()) continue; // Early exit for dead objects

           for (int j = i + 1; j < size; j++) {
               GameObject b = gameObjects.get(j);
               if (!b.isAlive()) continue; // Early exit for dead objects

               // Quick distance check before expensive bounds intersection
               double dx = a.getX() - b.getX();
               double dy = a.getY() - b.getY();
               double distance = dx * dx + dy * dy; // Avoid sqrt for performance
               double maxDistance = a.getRadius() + b.getRadius();

               if (distance <= maxDistance * maxDistance) {
                   handleCollision(a, b);
               }
           }
        }
    }

    private void removeDeadObjects() {
        // Remove objects that are no longer "alive"
        gameObjects.removeIf(obj -> !obj.isAlive());
    }

    private void wrapObjectsAroundScreen() {
        for (GameObject obj : gameObjects) {
            if (obj.getX() < 0) obj.setX(WIDTH);
            if (obj.getX() > WIDTH) obj.setX(0);
            if (obj.getY() < 0) obj.setY(HEIGHT);
            if (obj.getY() > HEIGHT) obj.setY(0);
        }
    }

    private void handleCollision(GameObject a, GameObject b) {
        // Determine collision type and handle appropriately
        if (isBulletAsteroidCollision(a, b)) {
            handleBulletAsteroidCollision(a, b);
        } else if (isPlayerAsteroidCollision(a, b)) {
            handlePlayerAsteroidCollision(a, b);
        } else if (isPlayerPowerUpCollision(a, b)) {
            handlePlayerPowerUpCollision(a, b);
        }
    }

    private boolean isBulletAsteroidCollision(GameObject a, GameObject b) {
        return (a instanceof Bullet && b instanceof Asteroid) ||
               (b instanceof Bullet && a instanceof Asteroid);
    }

    private boolean isPlayerAsteroidCollision(GameObject a, GameObject b) {
        return (a instanceof PlayerShip && b instanceof Asteroid) ||
               (b instanceof PlayerShip && a instanceof Asteroid);
    }

    private boolean isPlayerPowerUpCollision(GameObject a, GameObject b) {
        return (a instanceof PlayerShip && b instanceof PowerUp) ||
               (b instanceof PlayerShip && a instanceof PowerUp);
    }

    private void handleBulletAsteroidCollision(GameObject a, GameObject b) {
        Bullet bullet = (a instanceof Bullet) ? (Bullet) a : (Bullet) b;
        Asteroid asteroid = (a instanceof Asteroid) ? (Asteroid) a : (Asteroid) b;

        bullet.setAlive(false);

        // Create impact sparks
        double impactAngle = Math.atan2(asteroid.getY() - bullet.getY(),
                                      asteroid.getX() - bullet.getX());
        createImpactSparks(bullet.getX(), bullet.getY(), impactAngle);

        asteroid.hit(this); // Asteroid may split or get destroyed
        InputValidator.safeExecute(() -> SoundManager.playAsteroidHit(),
            "Failed to play asteroid hit sound");
    }

    private void handlePlayerAsteroidCollision(GameObject a, GameObject b) {
        PlayerShip ship = (a instanceof PlayerShip) ? (PlayerShip) a : (PlayerShip) b;

        if (!ship.hasShield()) {
            double oldX = ship.getX();
            double oldY = ship.getY();
            ship.damage();

            // Notify wave system of damage
            waveSystem.playerDamaged();

            if (ship.isAlive()) {
                // Create warp effect at old position
                createWarpEffect(oldX, oldY);
                // Create warp effect at new position
                createWarpEffect(ship.getX(), ship.getY());
            }
        } else {
            // Shield blocked the hit
            LeaderboardSystem.shieldBlocked();
        }
    }

    private void handlePlayerPowerUpCollision(GameObject a, GameObject b) {
        PlayerShip ship = (a instanceof PlayerShip) ? (PlayerShip) a : (PlayerShip) b;
        PowerUp powerUp = (a instanceof PowerUp) ? (PowerUp) a : (PowerUp) b;

        ship.addPowerUp(powerUp.getType());
        powerUp.setAlive(false);
    }

    public void addGameObject(GameObject obj) {
       synchronized(lock) {
          gameObjects.add(obj);
       }
    }

    // Provide a thread-safe snapshot of the game objects for rendering.
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
            player.reset();
            addGameObject(player);

            // Reset wave system
            waveSystem.reset();

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
        // Count asteroids
        int asteroidCount = 0;
        boolean hasPowerUp = false;

        synchronized(lock) {
            for (GameObject obj : gameObjects) {
                if (obj instanceof Asteroid) {
                    asteroidCount++;
                }
            }
            hasPowerUp = !player.getActivePowerUps().isEmpty();
        }

        MusicSystem.updateMusicIntensity(asteroidCount, player.getLives(), hasPowerUp);
    }

    private void spawnWaveAsteroids() {
        int asteroidsToSpawn = waveSystem.getAsteroidsRemaining();
        for (int i = 0; i < asteroidsToSpawn; i++) {
            WaveSystem.AsteroidSpawnInfo spawnInfo = waveSystem.getSpawnInfo();
            Asteroid asteroid = createAsteroidWithDifficulty(spawnInfo);
            addGameObject(asteroid);
        }
    }

    private void checkAndSpawnAsteroids() {
        // Count current asteroids
        int currentAsteroids = 0;
        synchronized(lock) {
            for (GameObject obj : gameObjects) {
                if (obj instanceof Asteroid) {
                    currentAsteroids++;
                }
            }
        }

        // If no asteroids remain and wave is in progress, complete the wave
        if (currentAsteroids == 0 && waveSystem.isWaveInProgress()) {
            waveSystem.asteroidDestroyed(); // This will trigger wave completion
            spawnWaveAsteroids(); // Spawn next wave
        }
    }

    private Asteroid createAsteroidWithDifficulty(WaveSystem.AsteroidSpawnInfo spawnInfo) {
        // Create asteroid at screen edge
        double x, y;
        Random rand = new Random();

        if (rand.nextBoolean()) {
            x = rand.nextBoolean() ? -50 : WIDTH + 50;
            y = rand.nextDouble() * HEIGHT;
        } else {
            x = rand.nextDouble() * WIDTH;
            y = rand.nextBoolean() ? -50 : HEIGHT + 50;
        }

        // Calculate velocity towards screen center with some randomness
        double angle = Math.atan2(HEIGHT/2 - y, WIDTH/2 - x) + (rand.nextDouble() - 0.5) * Math.PI / 2;
        double vx = spawnInfo.speed * Math.cos(angle);
        double vy = spawnInfo.speed * Math.sin(angle);

        return new Asteroid(x, y, spawnInfo.size, vx, vy);
    }

    public WaveSystem getWaveSystem() {
        return waveSystem;
    }
}
