import java.util.List;
import java.util.ArrayList;

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

    private int score = 0;
    private boolean gameOver = false;

    public GameEngine() {
       gameObjects = new ArrayList<>();

       // Start ambient space sound
       SoundManager.startAmbientSpace();

       // Create player ship at center.
       player = new PlayerShip(WIDTH / 2, HEIGHT / 2);
       addGameObject(player);

       // Spawn a few initial asteroids.
       for (int i = 0; i < 5; i++) {
          addGameObject(Asteroid.createRandomAsteroid(WIDTH, HEIGHT, 3)); // size 3 represents a large asteroid.
       }

       // Start a separate thread to periodically spawn new asteroids.
       new Thread(() -> {
         while (true) {
           try {
             Thread.sleep(5000); // Every 5 seconds, add a new asteroid.
           } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
             break;
           }
           addGameObject(Asteroid.createRandomAsteroid(WIDTH, HEIGHT, 3));
         }
       }, "AsteroidSpawner").start();
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
             ((Bullet) a).setAlive(false);
             ((Asteroid) b).hit(this); // Asteroid may split or get destroyed.
             SoundManager.playAsteroidHit(); // Play impact sound
         } else if (b instanceof Bullet && a instanceof Asteroid) {
             ((Bullet) b).setAlive(false);
             ((Asteroid) a).hit(this);
             SoundManager.playAsteroidHit(); // Play impact sound
         }
         // Player ship colliding with an asteroid.
         else if (a instanceof PlayerShip && b instanceof Asteroid) {
            ((PlayerShip)a).damage();
         } else if (b instanceof PlayerShip && a instanceof Asteroid) {
            ((PlayerShip)b).damage();
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

            // Spawn initial asteroids
            for (int i = 0; i < 5; i++) {
                addGameObject(Asteroid.createRandomAsteroid(WIDTH, HEIGHT, 3));
            }

            // Reset game over flag
            gameOver = false;

            // Restart ambient sound
            SoundManager.startAmbientSpace();
        }
    }
}
