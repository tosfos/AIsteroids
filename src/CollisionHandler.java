import java.util.List;

/**
 * Handles collision detection and resolution between game objects.
 *
 * <p>This class is responsible for:
 * <ul>
 *   <li>Detecting collisions between different types of game objects</li>
 *   <li>Determining collision types (bullet-asteroid, player-asteroid, player-powerup)</li>
 *   <li>Delegating collision resolution to appropriate handlers</li>
 * </ul>
 * </p>
 *
 * <p>Thread safety: This class is stateless and all methods are thread-safe.
 * It should be called from the game update thread while holding the game objects lock.</p>
 *
 * @author AIsteroids Development Team
 * @version 1.0
 */
public class CollisionHandler {

    private final GameEngine gameEngine;

    /**
     * Creates a new collision handler for the specified game engine.
     *
     * @param gameEngine The game engine that owns the game objects and handles effects
     */
    public CollisionHandler(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    /**
     * Checks if two game objects represent a bullet-asteroid collision.
     *
     * @param a First game object
     * @param b Second game object
     * @return true if this is a bullet (or laser beam) vs asteroid collision
     */
    public boolean isBulletAsteroidCollision(GameObject a, GameObject b) {
        boolean aIsProjectile = (a instanceof Bullet || a instanceof LaserBeam);
        boolean bIsProjectile = (b instanceof Bullet || b instanceof LaserBeam);
        return (aIsProjectile && b instanceof Asteroid) ||
               (bIsProjectile && a instanceof Asteroid);
    }

    /**
     * Checks if two game objects represent a player-asteroid collision.
     *
     * @param a First game object
     * @param b Second game object
     * @return true if this is a player ship vs asteroid collision
     */
    public boolean isPlayerAsteroidCollision(GameObject a, GameObject b) {
        return (a instanceof PlayerShip && b instanceof Asteroid) ||
               (b instanceof PlayerShip && a instanceof Asteroid);
    }

    /**
     * Checks if two game objects represent a player-powerup collision.
     *
     * @param a First game object
     * @param b Second game object
     * @return true if this is a player ship vs powerup collision
     */
    public boolean isPlayerPowerUpCollision(GameObject a, GameObject b) {
        return (a instanceof PlayerShip && b instanceof PowerUp) ||
               (b instanceof PlayerShip && a instanceof PowerUp);
    }

    /**
     * Handles a collision between two game objects.
     * Determines the collision type and delegates to appropriate handler.
     *
     * @param a First game object
     * @param b Second game object
     */
    public void handleCollision(GameObject a, GameObject b) {
        // Determine collision type and handle appropriately
        if (isBulletAsteroidCollision(a, b)) {
            handleBulletAsteroidCollision(a, b);
        } else if (isPlayerAsteroidCollision(a, b)) {
            handlePlayerAsteroidCollision(a, b);
        } else if (isPlayerPowerUpCollision(a, b)) {
            handlePlayerPowerUpCollision(a, b);
        }
    }

    /**
     * Handles collision between a bullet/laser and an asteroid.
     *
     * @param a First game object (bullet, laser, or asteroid)
     * @param b Second game object (bullet, laser, or asteroid)
     */
    private void handleBulletAsteroidCollision(GameObject a, GameObject b) {
        // Check if it's a laser beam or regular bullet
        boolean isLaserBeam = (a instanceof LaserBeam || b instanceof LaserBeam);
        Asteroid asteroid = (a instanceof Asteroid) ? (Asteroid) a : (Asteroid) b;

        if (isLaserBeam) {
            handleLaserBeamAsteroidCollision(a, b, asteroid);
        } else {
            handleBulletAsteroidCollisionInternal(a, b, asteroid);
        }
    }

    /**
     * Handles collision between a laser beam and an asteroid.
     *
     * @param a First game object (laser beam or asteroid)
     * @param b Second game object (laser beam or asteroid)
     * @param asteroid The asteroid involved in the collision
     */
    private void handleLaserBeamAsteroidCollision(GameObject a, GameObject b, Asteroid asteroid) {
        LaserBeam beam = (a instanceof LaserBeam) ? (LaserBeam) a : (LaserBeam) b;
        // High-powered beam reduces asteroid size by multiple levels
        for (int i = 0; i < beam.getDamage(); i++) {
            if (asteroid.isAlive()) { // Keep damaging until destroyed
                asteroid.hit(gameEngine);
            }
        }
        // Create larger impact effect with multiple spark directions
        double impactAngle = java.lang.Math.atan2(asteroid.getY() - beam.getY(),
                                      asteroid.getX() - beam.getX());
        createMultiDirectionImpactSparks(asteroid.getX(), asteroid.getY(), impactAngle);
        // Play higher pitched hit sound
        InputValidator.safeExecute(() -> SoundManager.playAsteroidHit(),
            "Failed to play asteroid hit sound");
    }

    /**
     * Handles collision between a regular bullet and an asteroid.
     *
     * @param a First game object (bullet or asteroid)
     * @param b Second game object (bullet or asteroid)
     * @param asteroid The asteroid involved in the collision
     */
    private void handleBulletAsteroidCollisionInternal(GameObject a, GameObject b, Asteroid asteroid) {
        Bullet bullet = (a instanceof Bullet) ? (Bullet) a : (Bullet) b;
        bullet.setAlive(false);

        // Create normal impact sparks
        double impactAngle = java.lang.Math.atan2(asteroid.getY() - bullet.getY(),
                                      asteroid.getX() - bullet.getX());
        gameEngine.createImpactSparks(bullet.getX(), bullet.getY(), impactAngle);

        asteroid.hit(gameEngine); // Asteroid may split or get destroyed
        InputValidator.safeExecute(() -> SoundManager.playAsteroidHit(),
            "Failed to play asteroid hit sound");
    }

    /**
     * Creates impact sparks in multiple directions around the impact point.
     *
     * @param x X coordinate of impact
     * @param y Y coordinate of impact
     * @param baseAngle Base angle for spark direction
     */
    private void createMultiDirectionImpactSparks(double x, double y, double baseAngle) {
        gameEngine.createImpactSparks(x, y, baseAngle);
        gameEngine.createImpactSparks(x, y, baseAngle + GameConfig.Angles.PI_OVER_2);
        gameEngine.createImpactSparks(x, y, baseAngle - GameConfig.Angles.PI_OVER_2);
    }

    /**
     * Handles collision between player ship and an asteroid.
     *
     * @param a First game object (player ship or asteroid)
     * @param b Second game object (player ship or asteroid)
     */
    private void handlePlayerAsteroidCollision(GameObject a, GameObject b) {
        PlayerShip ship = (a instanceof PlayerShip) ? (PlayerShip) a : (PlayerShip) b;

        if (!ship.hasShield()) {
            double oldX = ship.getX();
            double oldY = ship.getY();
            ship.damage();

            // Notify wave system of damage via GameEngine
            // (waveSystem is accessed through GameEngine's public method)
            gameEngine.notifyPlayerDamaged();

            if (ship.isAlive()) {
                // Create warp effect at old position
                gameEngine.createWarpEffect(oldX, oldY);
                // Create warp effect at new position
                gameEngine.createWarpEffect(ship.getX(), ship.getY());
            }
        } else {
            // Shield blocked the hit
            LeaderboardSystem.shieldBlocked();
        }
    }

    /**
     * Handles collision between player ship and a powerup.
     *
     * @param a First game object (player ship or powerup)
     * @param b Second game object (player ship or powerup)
     */
    private void handlePlayerPowerUpCollision(GameObject a, GameObject b) {
        PlayerShip ship = (a instanceof PlayerShip) ? (PlayerShip) a : (PlayerShip) b;
        PowerUp powerUp = (a instanceof PowerUp) ? (PowerUp) a : (PowerUp) b;

        ship.addPowerUp(powerUp.getType());
        powerUp.setAlive(false);

        // Show power-up message
        gameEngine.showPowerUpMessage(powerUp.getType());
    }
}
