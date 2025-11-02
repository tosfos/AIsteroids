import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class PlayerShip extends GameObject {
    // Ship orientation in radians (initially pointing up: -PI/2)
    private double angle = -Math.PI/2;
    // Flags for input control.
    private boolean turnLeft = false;
    private boolean turnRight = false;
    private boolean accelerating = false;
    // Player lives.
    private int lives = GameConfig.PlayerShip.INITIAL_LIVES;
    // Invulnerability timer in seconds after being hit.
    private double invulnerabilityTimer = 0;
        // Engine trail particles
    private List<TrailParticle> engineTrail;

    // Power-up system
    private Map<PowerUp.PowerUpType, Double> activePowerUps;
    private double fireRate = GameConfig.PlayerShip.FIRE_RATE; // Base fire rate (shots per second)
    private double lastFireTime = 0;
    private boolean hasShield = false;
    private double shieldTimer = 0;

    // Constants for rotation and acceleration.
    private double rotationSpeed = GameConfig.PlayerShip.ROTATION_SPEED_RADIANS;
    private double acceleration = GameConfig.PlayerShip.ACCELERATION; // pixels per second^2.
    private double maxSpeed = GameConfig.PlayerShip.MAX_SPEED; // pixels per second.

    public PlayerShip(double x, double y) {
       super(x, y);
       engineTrail = new ArrayList<>();
       activePowerUps = new HashMap<>();
       // Initialize to allow immediate shooting
       lastFireTime = 1.0;
    }

    @Override
    public void update(double deltaTime) {
        InputValidator.validateDeltaTime(deltaTime);

        // Update invulnerability timer
        if (invulnerabilityTimer > 0) {
            invulnerabilityTimer -= deltaTime;
        }

        // Handle rotation.
        if (turnLeft) {
            angle -= rotationSpeed * deltaTime;
        }
        if (turnRight) {
            angle += rotationSpeed * deltaTime;
        }

        // Accelerate in the facing direction.
        if (accelerating) {
            // Cache cos/sin to avoid repeated calculations
            double cosAngle = Math.cos(angle);
            double sinAngle = Math.sin(angle);
            // Adjust velocity based on current angle
            vx += cosAngle * acceleration * deltaTime;
            vy += sinAngle * acceleration * deltaTime;

            // Add engine trail particles
            addEngineTrail();
        }

        // Limit the maximum speed.
        double speed = Math.hypot(vx, vy);
        if (speed > maxSpeed) {
            vx = (vx / speed) * maxSpeed;
            vy = (vy / speed) * maxSpeed;
        }

        // Update position.
        x += vx * deltaTime;
        y += vy * deltaTime;

        // Update engine trail
        updateEngineTrail(deltaTime);

        // Update power-ups
        updatePowerUps(deltaTime);

        // Update last fire time
        lastFireTime += deltaTime;
    }

    @Override
    public void draw(Graphics2D g) {
        // Draw engine trail first (behind ship)
        drawEngineTrail(g);

        AffineTransform old = g.getTransform();
        g.translate(x, y);
        g.rotate(angle + Math.PI/2);

        // If invulnerable, make the ship blink by reducing opacity
        Composite oldComposite = g.getComposite();
        if (invulnerabilityTimer > 0) {
            float alpha = (float)(GameConfig.Effects.INVULNERABILITY_MIN_ALPHA +
                (GameConfig.Effects.INVULNERABILITY_MAX_ALPHA - GameConfig.Effects.INVULNERABILITY_MIN_ALPHA) *
                Math.abs(Math.sin(System.currentTimeMillis() * GameConfig.Effects.INVULNERABILITY_BLINK_SPEED)));
            g.setComposite(GraphicsUtils.createAlphaComposite(alpha));
        }

        // Create detailed spaceship shape using GeneralPath
        GeneralPath ship = new GeneralPath();
        ship.moveTo(0, -15);     // Ship tip (now pointing right)
        ship.lineTo(-8, 5);      // Left wing tip
        ship.lineTo(-4, 8);      // Left engine mount
        ship.lineTo(0, 5);       // Rear center
        ship.lineTo(4, 8);       // Right engine mount
        ship.lineTo(8, 5);       // Right wing tip
        ship.closePath();

        // Create a more advanced gradient for the ship
        Color[] shipColors = {
            new Color(100, 150, 255), // Light blue
            new Color(50, 100, 200),  // Medium blue
            new Color(20, 50, 150)    // Dark blue
        };

        float[] dist = {0.0f, 0.5f, 1.0f};
        RadialGradientPaint shipPaint = new RadialGradientPaint(
            0, -5, 12, dist, shipColors);

        Paint oldPaint = g.getPaint();
        g.setPaint(shipPaint);
        g.fill(ship);

        // Add metallic highlights
        g.setPaint(new Color(200, 220, 255, 150));
        GeneralPath highlight = new GeneralPath();
        highlight.moveTo(-2, -12);
        highlight.lineTo(2, -12);
        highlight.lineTo(1, -8);
        highlight.lineTo(-1, -8);
        highlight.closePath();
        g.fill(highlight);

        // Draw ship outline with glowing effect
        g.setPaint(GameConfig.Effects.Ship.SHIP_GLOW_COLOR);
        g.setStroke(new BasicStroke(GameConfig.Effects.Ship.SHIP_GLOW_THICKNESS));
        g.draw(ship);

        // Inner glow
        g.setPaint(GameConfig.Effects.Ship.SHIP_INNER_GLOW_COLOR);
        g.setStroke(new BasicStroke(GameConfig.Effects.Ship.SHIP_INNER_GLOW_THICKNESS));
        g.draw(ship);

        // If accelerating, draw enhanced flame effect behind the ship
        if (accelerating) {
            drawEngineFlame(g);
        }

        // Restore original paint, composite and transform
        g.setPaint(oldPaint);
        g.setComposite(oldComposite);
        g.setTransform(old);
    }

    private void addEngineTrail() {
        if (accelerating && engineTrail.size() < 20) { // Limit max particles to prevent memory bloat
            // Add trail particles from engine position
            double engineX = x - Math.cos(angle) * 12;
            double engineY = y - Math.sin(angle) * 12;

            // Reduce particle count to improve performance
            int particleCount = (int) GameConfig.PlayerShip.ENGINE_TRAIL_PARTICLES;
            for (int i = 0; i < particleCount; i++) {
                double offsetX = (Math.random() - 0.5) * GameConfig.PlayerShip.ENGINE_TRAIL_SPREAD;
                double offsetY = (Math.random() - 0.5) * GameConfig.PlayerShip.ENGINE_TRAIL_SPREAD;
                // Reuse color object to reduce allocations
                engineTrail.add(new TrailParticle(
                    engineX + offsetX, engineY + offsetY, TRAIL_COLOR,
                    GameConfig.PlayerShip.ENGINE_TRAIL_LIFETIME));
            }
        }
    }

    // Reusable color to reduce object allocation
    private static final Color TRAIL_COLOR = new Color(0, 150, 255, 150);

    // Cache commonly used values
    private static final double PI_OVER_12 = Math.PI / 12.0; // 15 degrees for spread shot
    private static final double PI_OVER_8 = Math.PI / 8.0;   // 22.5 degrees for multi shot
    private static final double BULLET_OFFSET_DISTANCE = 15.0;

    private void updateEngineTrail(double deltaTime) {
        // Optimize by combining update and cleanup in one pass
        engineTrail.removeIf(particle -> {
            if (!particle.isAlive()) {
                return true; // Remove dead particle
            }
            particle.update(deltaTime);
            return false; // Keep alive particle
        });
    }

    private void drawEngineTrail(Graphics2D g) {
        // Iterate directly - engineTrail is only accessed from update thread
        for (TrailParticle particle : engineTrail) {
            if (particle.isAlive()) {
                particle.draw(g);
            }
        }
    }

    private void drawEngineFlame(Graphics2D g) {
        // Create animated flame effect
        double flameLength = GameConfig.Effects.Ship.FLAME_BASE_LENGTH + GameConfig.Effects.Ship.FLAME_VARIATION * Math.sin(System.currentTimeMillis() * GameConfig.Effects.Ship.FLAME_ANIMATION_SPEED);

        // Main flame
        GeneralPath flame = new GeneralPath();
        flame.moveTo(-3, 8);
        flame.lineTo(0, 8 + flameLength);
        flame.lineTo(3, 8);
        flame.closePath();

        // Gradient flame colors
        Color[] flameColors = {Color.YELLOW, Color.ORANGE, Color.RED};
        float[] flameDist = {0.0f, 0.5f, 1.0f};
        LinearGradientPaint flamePaint = new LinearGradientPaint(
            0, 8, 0, (float)(8 + flameLength), flameDist, flameColors);

        g.setPaint(flamePaint);
        g.fill(flame);

        // Inner hot core
        GeneralPath innerFlame = new GeneralPath();
        innerFlame.moveTo(-1.5f, 8);
        innerFlame.lineTo(0, 8 + flameLength * 0.7);
        innerFlame.lineTo(1.5f, 8);
        innerFlame.closePath();

        g.setPaint(new Color(255, 255, 200, 200));
        g.fill(innerFlame);

        // Side flames for more realistic effect
        for (int i = -1; i <= 1; i += 2) {
            GeneralPath sideFlame = new GeneralPath();
            sideFlame.moveTo(i * 2, 10);
            sideFlame.lineTo(i * 4, 12 + flameLength * 0.4);
            sideFlame.lineTo(i * 1, 12);
            sideFlame.closePath();

            g.setPaint(new Color(255, 100, 0, 150));
            g.fill(sideFlame);
        }
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)x - 10, (int)y - 10, 20, 20);
    }

    @Override
    public double getRadius() {
        return 10.0; // Ship radius for collision detection
    }

    // Methods to update input state.
    public void setTurnLeft(boolean turn) {
        turnLeft = turn;
    }
    public void setTurnRight(boolean turn) {
        turnRight = turn;
    }
    public void setAccelerating(boolean accel) {
        accelerating = accel;
        if (accel) {
            SoundManager.playThruster();
        }
    }

    public int getLives() {
        return lives;
    }

    // Called when the ship collides with an asteroid.
    public void damage() {
        // If the ship is currently invulnerable, ignore the damage
        if (invulnerabilityTimer > 0) {
            return;
        }
        lives--;
        if (lives <= 0) {
            active = false;  // Ship destroyed.
        } else {
            // Reset ship to the center and clear velocity.
            x = GameEngine.WIDTH / 2;
            y = GameEngine.HEIGHT / 2;
            vx = 0;
            vy = 0;
            // Set invulnerability period to prevent immediate further damage
            invulnerabilityTimer = GameConfig.PlayerShip.INVULNERABILITY_TIME;
            lastFireTime = 1.0;
            // Play shield recharge sound when respawning
            SoundManager.playShieldRecharge();

            // Create warp effect when respawning (needs to be called from GameEngine)
            // This will be handled by GameEngine when damage occurs
        }
    }

    // Fire a bullet from the ship's tip.
    public List<Projectile> fireBullet() {
        // Check fire rate
        if (lastFireTime < (1.0 / getCurrentFireRate())) {
            return new ArrayList<>();
        }

        lastFireTime = 0;
        SoundManager.playLaser();

        // Pre-allocate list with estimated capacity to reduce resizing
        List<Projectile> bullets = new ArrayList<>(5);
        double cosAngle = Math.cos(angle);
        double sinAngle = Math.sin(angle);
        double bulletX = x + cosAngle * BULLET_OFFSET_DISTANCE;
        double bulletY = y + sinAngle * BULLET_OFFSET_DISTANCE;

        if (activePowerUps.containsKey(PowerUp.PowerUpType.LASER_BEAM)) {
            // Fire a high-powered laser beam
            bullets.add(new LaserBeam(bulletX, bulletY, angle));
            // Play laser beam sound (higher pitch than normal laser)
            SoundManager.playLaser();
        } else if (activePowerUps.containsKey(PowerUp.PowerUpType.SPREAD_SHOT)) {
            // Fire 3 bullets in spread pattern
            for (int i = -1; i <= 1; i++) {
                double spreadAngle = angle + (i * PI_OVER_12); // 15 degree spread
                bullets.add(new Bullet(bulletX, bulletY, spreadAngle));
            }
        } else if (activePowerUps.containsKey(PowerUp.PowerUpType.MULTI_SHOT)) {
            // Fire 5 bullets in wider spread
            for (int i = -2; i <= 2; i++) {
                double spreadAngle = angle + (i * PI_OVER_8); // 22.5 degree spread
                bullets.add(new Bullet(bulletX, bulletY, spreadAngle));
            }
        } else {
            // Normal single bullet
            bullets.add(new Bullet(bulletX, bulletY, angle));
        }

        LeaderboardSystem.updateGameStats("bullets_fired", bullets.size());
        return bullets;
    }

    // Test-friendly bullet firing without rate limiting
    public List<Bullet> fireBulletForTesting() {
        List<Bullet> bullets = new ArrayList<>(5);
        double cosAngle = Math.cos(angle);
        double sinAngle = Math.sin(angle);
        double bulletX = x + cosAngle * BULLET_OFFSET_DISTANCE;
        double bulletY = y + sinAngle * BULLET_OFFSET_DISTANCE;

        if (activePowerUps.containsKey(PowerUp.PowerUpType.SPREAD_SHOT)) {
            // Fire 3 bullets in spread pattern
            for (int i = -1; i <= 1; i++) {
                double spreadAngle = angle + (i * PI_OVER_12); // 15 degree spread
                bullets.add(new Bullet(bulletX, bulletY, spreadAngle));
            }
        } else if (activePowerUps.containsKey(PowerUp.PowerUpType.MULTI_SHOT)) {
            // Fire 5 bullets in wider spread
            for (int i = -2; i <= 2; i++) {
                double spreadAngle = angle + (i * PI_OVER_8); // 22.5 degree spread
                bullets.add(new Bullet(bulletX, bulletY, spreadAngle));
            }
        } else {
            // Normal single bullet
            bullets.add(new Bullet(bulletX, bulletY, angle));
        }

        return bullets;
    }

    /**
     * Calculates the current fire rate including power-up modifications.
     * The base fire rate is multiplied by power-up effects.
     *
     * @return Current fire rate in shots per second
     */
    private double getCurrentFireRate() {
        double rate = fireRate;
        if (activePowerUps.containsKey(PowerUp.PowerUpType.RAPID_FIRE)) {
            rate *= 3.0; // Triple fire rate with Rapid Fire power-up
        }
        return rate;
    }

    /**
     * Activates a power-up on this player ship.
     * Power-ups modify ship behavior for their duration and stack with existing power-ups.
     * Some power-ups have immediate effects (Shield, Speed Boost), while others modify
     * behavior during other actions (Rapid Fire, Spread Shot, Multi Shot).
     *
     * @param type The type of power-up to activate
     * @throws IllegalArgumentException if type is null
     */
    public void addPowerUp(PowerUp.PowerUpType type) {
        InputValidator.validateNotNull(type, "type");
        activePowerUps.put(type, type.getDuration());
        InputValidator.safeExecute(() -> SoundManager.playPowerUp(),
            "Failed to play power-up sound");

        // Track power-up collection for achievements and statistics
        LeaderboardSystem.powerUpCollected();

        // Track specific power-up usage for achievements
        if (type == PowerUp.PowerUpType.RAPID_FIRE) {
            LeaderboardSystem.rapidFireUsed();
        }

        // Apply immediate effects for certain power-ups
        switch (type) {
            case SHIELD:
                // Activate invulnerability shield
                hasShield = true;
                shieldTimer = type.getDuration();
                break;
            case SPEED_BOOST:
                // Increase ship maneuverability and speed
                maxSpeed = GameConfig.PowerUp.SPEED_BOOST_MAX_SPEED;
                rotationSpeed = Math.toRadians(GameConfig.PowerUp.SPEED_BOOST_ROTATION_SPEED_DEGREES);
                acceleration = GameConfig.PowerUp.SPEED_BOOST_ACCELERATION;
                break;
            case RAPID_FIRE:
            case SPREAD_SHOT:
            case MULTI_SHOT:
            case LASER_BEAM:
                // These power-ups are applied dynamically during fireBullet() calls
                // No immediate effects needed here
                break;
        }
    }

    /**
     * Updates all active power-ups, decreasing their remaining time and removing expired ones.
     * When power-ups expire, their effects are reverted to normal values.
     * Called every frame during ship update.
     *
     * @param deltaTime Time elapsed since last update in seconds
     */
    private void updatePowerUps(double deltaTime) {
        List<PowerUp.PowerUpType> expiredPowerUps = new ArrayList<>();

        // Decrease time remaining for all active power-ups
        for (Map.Entry<PowerUp.PowerUpType, Double> entry : activePowerUps.entrySet()) {
            double timeLeft = entry.getValue() - deltaTime;
            if (timeLeft <= 0) {
                expiredPowerUps.add(entry.getKey());
            } else {
                entry.setValue(timeLeft);
            }
        }

        // Remove expired power-ups and revert their effects
        for (PowerUp.PowerUpType type : expiredPowerUps) {
            activePowerUps.remove(type);
            switch (type) {
                case SHIELD:
                    // Deactivate shield protection
                    hasShield = false;
                    shieldTimer = 0;
                    break;
                case SPEED_BOOST:
                    // Restore normal ship maneuverability
                    maxSpeed = GameConfig.PlayerShip.MAX_SPEED;
                    rotationSpeed = GameConfig.PlayerShip.ROTATION_SPEED_RADIANS;
                    acceleration = GameConfig.PlayerShip.ACCELERATION;
                    break;
                case RAPID_FIRE:
                case SPREAD_SHOT:
                case MULTI_SHOT:
                case LASER_BEAM:
                    // These power-ups don't need explicit cleanup as they're
                    // checked dynamically during fireBullet() calls
                    break;
            }
        }
    }

    /**
     * Checks if the ship currently has shield protection active.
     * Shield power-up provides invulnerability to asteroid collisions.
     *
     * @return true if shield is active, false otherwise
     */
    public boolean hasShield() {
        return hasShield;
    }

    /**
     * Gets a snapshot of all currently active power-ups and their remaining durations.
     * Used by the HUD to display power-up status indicators.
     *
     * @return Map of active power-up types to their remaining time in seconds
     */
    public Map<PowerUp.PowerUpType, Double> getActivePowerUps() {
        return new HashMap<>(activePowerUps);
    }

    public void reset() {
        // Reset position
        x = GameEngine.WIDTH / 2;
        y = GameEngine.HEIGHT / 2;
        // Reset velocity
        vx = 0;
        vy = 0;
        // Reset angle
        angle = -Math.PI/2;
        // Reset control flags
        turnLeft = false;
        turnRight = false;
        accelerating = false;
        // Reset lives and invulnerability
        lives = GameConfig.PlayerShip.INITIAL_LIVES;
        invulnerabilityTimer = 0;
        // Allow immediate shooting after reset
        lastFireTime = 1.0;
        // Clear engine trail
        engineTrail.clear();
        // Ensure alive
        active = true;
    }

    // Inner class for engine trail particles
    private static class TrailParticle {
        private double x, y;
        private Color color;
        private double life, maxLife;
        private double size;

        public TrailParticle(double x, double y, Color color, double life) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.life = life;
            this.maxLife = life;
            this.size = 2 + Math.random() * 3;
        }

        public void update(double deltaTime) {
            life -= deltaTime;
            size *= 0.98;
        }

        public boolean isAlive() {
            return life > 0 && size > 0.5;
        }

                public void draw(Graphics2D g) {
            float alpha = (float)(life / maxLife);
            Color fadeColor = GraphicsUtils.colorWithAlpha(color, alpha);

            // Create glowing effect
            float[] dist = {0.0f, 1.0f};
            Color[] colors = {fadeColor, new Color(fadeColor.getRed(), fadeColor.getGreen(), fadeColor.getBlue(), 0)};
            RadialGradientPaint paint = new RadialGradientPaint(
                (float)x, (float)y, (float)size, dist, colors);

            g.setPaint(paint);
            g.fillOval((int)(x - size), (int)(y - size), (int)(size * 2), (int)(size * 2));
        }
    }
}
