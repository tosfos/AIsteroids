import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class GameEngine implements Runnable {
    // List of active game objects (player, asteroids, bullets)
    private List<GameObject> gameObjects;
    private boolean running = false;

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

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

    private int score = 0;
    private boolean gameOver = false;

    public GameEngine() {
       gameObjects = new ArrayList<>();

       // Start ambient space sound
       SoundManager.startAmbientSpace();

              // Start dynamic music
       MusicSystem.startMusic();

       // Initialize wave system
       waveSystem = new WaveSystem();

       // Create player ship at center.
       player = new PlayerShip(WIDTH / 2, HEIGHT / 2);
       addGameObject(player);

       // Spawn initial wave of asteroids based on wave system
       spawnWaveAsteroids();

              // Start a separate thread to periodically spawn power-ups and check wave completion
       new Thread(() -> {
         while (true) {
           try {
             Thread.sleep(3000); // Check every 3 seconds
           } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
             break;
           }

           // Spawn power-ups based on wave system
           WaveSystem.PowerUpSpawnInfo powerUpInfo = waveSystem.getPowerUpSpawnInfo();
           if (Math.random() < powerUpInfo.spawnChance) {
             addGameObject(PowerUp.createRandomPowerUp(WIDTH, HEIGHT));
           }

           // Check if we need to spawn more asteroids for current wave
           checkAndSpawnAsteroids();
         }
       }, "WaveManager").start();
    }

    public void start() {
       running = true;
       gameThread = new Thread(this, "GameEngineThread");
       gameThread.start();
    }

    public void stop() {
       running = false;
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            double deltaTime = (now - lastTime) / 1e9;  // Convert nanoseconds to seconds.
            lastTime = now;

            update(deltaTime);
            if (!player.isAlive() && !gameOver) {
                gameOver = true;
                SoundManager.playGameOver(); // Play game over sound
                SoundManager.stopAmbientSpace(); // Stop ambient sound
                MusicSystem.stopMusic(); // Stop dynamic music
            }

            // Sleep briefly (approximate 60 FPS update rate)
            try {
               Thread.sleep(16);
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
            }
        }
    }

    public void update(double deltaTime) {
        // Don't update game state if game is over
        if (gameOver) {
            return;
        }

        // Update dynamic music intensity based on game state
        updateMusicIntensity();

        synchronized(lock) {
            // Update every game object.
            for (GameObject obj : new ArrayList<>(gameObjects)) {
                obj.update(deltaTime);
            }

            // Check collisions: e.g., bullets hitting asteroids, or asteroids hitting the player.
            for (int i = 0; i < gameObjects.size(); i++) {
               GameObject a = gameObjects.get(i);
               for (int j = i + 1; j < gameObjects.size(); j++) {
                   GameObject b = gameObjects.get(j);
                   if (a.isAlive() && b.isAlive() && a.getBounds().intersects(b.getBounds())) {
                       handleCollision(a, b);
                   }
               }
            }

            // Remove objects that are no longer "alive".
            gameObjects.removeIf(obj -> !obj.isAlive());
        }

        // Screen wrapping for all game objects.
        synchronized(lock) {
          for (GameObject obj : gameObjects) {
              if (obj.getX() < 0) obj.setX(WIDTH);
              if (obj.getX() > WIDTH) obj.setX(0);
              if (obj.getY() < 0) obj.setY(HEIGHT);
              if (obj.getY() > HEIGHT) obj.setY(0);
          }
        }
    }

        private void handleCollision(GameObject a, GameObject b) {
         // Bullet hits asteroid.
         if (a instanceof Bullet && b instanceof Asteroid) {
             Bullet bullet = (Bullet) a;
             Asteroid asteroid = (Asteroid) b;
             bullet.setAlive(false);

             // Create impact sparks
             double impactAngle = Math.atan2(asteroid.getY() - bullet.getY(),
                                           asteroid.getX() - bullet.getX());
             createImpactSparks(bullet.getX(), bullet.getY(), impactAngle);

             asteroid.hit(this); // Asteroid may split or get destroyed.
             SoundManager.playAsteroidHit(); // Play impact sound
         } else if (b instanceof Bullet && a instanceof Asteroid) {
             Bullet bullet = (Bullet) b;
             Asteroid asteroid = (Asteroid) a;
             bullet.setAlive(false);

             // Create impact sparks
             double impactAngle = Math.atan2(asteroid.getY() - bullet.getY(),
                                           asteroid.getX() - bullet.getX());
             createImpactSparks(bullet.getX(), bullet.getY(), impactAngle);

             asteroid.hit(this);
             SoundManager.playAsteroidHit(); // Play impact sound
         }
                  // Player ship colliding with an asteroid.
         else if (a instanceof PlayerShip && b instanceof Asteroid) {
            PlayerShip ship = (PlayerShip)a;
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
            }
         } else if (b instanceof PlayerShip && a instanceof Asteroid) {
            PlayerShip ship = (PlayerShip)b;
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
            }
          }
         // Player ship colliding with power-up.
         else if (a instanceof PlayerShip && b instanceof PowerUp) {
            ((PlayerShip)a).addPowerUp(((PowerUp)b).getType());
            ((PowerUp)b).setAlive(false);
         } else if (b instanceof PlayerShip && a instanceof PowerUp) {
            ((PlayerShip)b).addPowerUp(((PowerUp)a).getType());
            ((PowerUp)a).setAlive(false);
         }
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
