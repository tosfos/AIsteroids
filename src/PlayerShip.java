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
    private int lives = 3;
    // Invulnerability timer in seconds after being hit.
    private double invulnerabilityTimer = 0;
        // Engine trail particles
    private List<TrailParticle> engineTrail;

    // Power-up system
    private Map<PowerUp.PowerUpType, Double> activePowerUps;
    private double fireRate = 10.0; // Base fire rate (shots per second) - much faster for better gameplay
    private double lastFireTime = 0;
    private boolean hasShield = false;
    private double shieldTimer = 0;

    // Constants for rotation and acceleration.
    private double rotationSpeed = Math.toRadians(180); // 180° per second.
    private double acceleration = 200; // pixels per second^2.
    private double maxSpeed = 300; // pixels per second.

    public PlayerShip(double x, double y) {
       super(x, y);
       engineTrail = new ArrayList<>();
       activePowerUps = new HashMap<>();
       // Initialize to allow immediate shooting
       lastFireTime = 1.0;
    }

    @Override
    public void update(double deltaTime) {
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
            // Adjust velocity based on current angle
            vx += Math.cos(angle) * acceleration * deltaTime;
            vy += Math.sin(angle) * acceleration * deltaTime;

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
            float alpha = (float)(0.3 + 0.7 * Math.abs(Math.sin(System.currentTimeMillis() * 0.02)));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
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
        g.setPaint(new Color(0, 255, 255, 200));
        g.setStroke(new BasicStroke(2.5f));
        g.draw(ship);

        // Inner glow
        g.setPaint(new Color(255, 255, 255, 100));
        g.setStroke(new BasicStroke(1f));
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
        if (accelerating) {
            // Add trail particles from engine position
            double engineX = x - Math.cos(angle) * 12;
            double engineY = y - Math.sin(angle) * 12;

            for (int i = 0; i < 3; i++) {
                double offsetX = (Math.random() - 0.5) * 6;
                double offsetY = (Math.random() - 0.5) * 6;
                Color trailColor = new Color(0, 150, 255, 150);
                engineTrail.add(new TrailParticle(
                    engineX + offsetX, engineY + offsetY, trailColor, 0.5));
            }
        }
    }

    private void updateEngineTrail(double deltaTime) {
        engineTrail.removeIf(particle -> !particle.isAlive());
        for (TrailParticle particle : engineTrail) {
            particle.update(deltaTime);
        }
    }

    private void drawEngineTrail(Graphics2D g) {
        // Create a copy to avoid concurrent modification
        List<TrailParticle> trailCopy = new ArrayList<>(engineTrail);
        for (TrailParticle particle : trailCopy) {
            particle.draw(g);
        }
    }

    private void drawEngineFlame(Graphics2D g) {
        // Create animated flame effect
        double flameLength = 15 + 5 * Math.sin(System.currentTimeMillis() * 0.05);

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
            // Set invulnerability period (2 seconds) to prevent immediate further damage
            invulnerabilityTimer = 2.0;
            // Play shield recharge sound when respawning
            SoundManager.playShieldRecharge();

            // Create warp effect when respawning (needs to be called from GameEngine)
            // This will be handled by GameEngine when damage occurs
        }
    }

    // Fire a bullet from the ship's tip.
    public List<Bullet> fireBullet() {
        // Check fire rate
        if (lastFireTime < (1.0 / getCurrentFireRate())) {
            return new ArrayList<>();
        }

        lastFireTime = 0;
        SoundManager.playLaser();

        // Track bullets fired for achievements
        LeaderboardSystem.bulletFired();

        List<Bullet> bullets = new ArrayList<>();
        double bulletX = x + Math.cos(angle) * 15;
        double bulletY = y + Math.sin(angle) * 15;

        if (activePowerUps.containsKey(PowerUp.PowerUpType.SPREAD_SHOT)) {
            // Fire 3 bullets in spread pattern
            for (int i = -1; i <= 1; i++) {
                double spreadAngle = angle + (i * Math.PI / 12); // 15 degree spread
                bullets.add(new Bullet(bulletX, bulletY, spreadAngle));
            }
        } else if (activePowerUps.containsKey(PowerUp.PowerUpType.MULTI_SHOT)) {
            // Fire 5 bullets in wider spread
            for (int i = -2; i <= 2; i++) {
                double spreadAngle = angle + (i * Math.PI / 8); // 22.5 degree spread
                bullets.add(new Bullet(bulletX, bulletY, spreadAngle));
            }
        } else {
            // Normal single bullet
            bullets.add(new Bullet(bulletX, bulletY, angle));
        }

        return bullets;
    }

    // Test-friendly bullet firing without rate limiting
    public List<Bullet> fireBulletForTesting() {
        List<Bullet> bullets = new ArrayList<>();
        double bulletX = x + Math.cos(angle) * 15;
        double bulletY = y + Math.sin(angle) * 15;

        if (activePowerUps.containsKey(PowerUp.PowerUpType.SPREAD_SHOT)) {
            // Fire 3 bullets in spread pattern
            for (int i = -1; i <= 1; i++) {
                double spreadAngle = angle + (i * Math.PI / 12); // 15 degree spread
                bullets.add(new Bullet(bulletX, bulletY, spreadAngle));
            }
        } else if (activePowerUps.containsKey(PowerUp.PowerUpType.MULTI_SHOT)) {
            // Fire 5 bullets in wider spread
            for (int i = -2; i <= 2; i++) {
                double spreadAngle = angle + (i * Math.PI / 8); // 22.5 degree spread
                bullets.add(new Bullet(bulletX, bulletY, spreadAngle));
            }
        } else {
            // Normal single bullet
            bullets.add(new Bullet(bulletX, bulletY, angle));
        }

        return bullets;
    }

    private double getCurrentFireRate() {
        double rate = fireRate;
        if (activePowerUps.containsKey(PowerUp.PowerUpType.RAPID_FIRE)) {
            rate *= 3.0; // Triple fire rate
        }
        return rate;
    }

    public void addPowerUp(PowerUp.PowerUpType type) {
        activePowerUps.put(type, type.getDuration());
        SoundManager.playPowerUp();

        // Track power-up collection
        LeaderboardSystem.powerUpCollected();

        // Track specific power-up usage
        if (type == PowerUp.PowerUpType.RAPID_FIRE) {
            LeaderboardSystem.rapidFireUsed();
        }

        // Apply immediate effects
        switch (type) {
            case SHIELD:
                hasShield = true;
                shieldTimer = type.getDuration();
                break;
            case SPEED_BOOST:
                maxSpeed = 500;
                rotationSpeed = Math.toRadians(270);
                acceleration = 400;
                break;
        }
    }

    private void updatePowerUps(double deltaTime) {
        List<PowerUp.PowerUpType> expiredPowerUps = new ArrayList<>();

        for (Map.Entry<PowerUp.PowerUpType, Double> entry : activePowerUps.entrySet()) {
            double timeLeft = entry.getValue() - deltaTime;
            if (timeLeft <= 0) {
                expiredPowerUps.add(entry.getKey());
            } else {
                entry.setValue(timeLeft);
            }
        }

        // Remove expired power-ups and reset effects
        for (PowerUp.PowerUpType type : expiredPowerUps) {
            activePowerUps.remove(type);
            switch (type) {
                case SHIELD:
                    hasShield = false;
                    shieldTimer = 0;
                    break;
                case SPEED_BOOST:
                    maxSpeed = 300;
                    rotationSpeed = Math.toRadians(180);
                    acceleration = 200;
                    break;
            }
        }
    }

    public boolean hasShield() {
        return hasShield;
    }

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
        lives = 3;
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
            int particleAlpha = Math.max(0, Math.min(255, (int)(color.getAlpha() * alpha)));
            Color fadeColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), particleAlpha);

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
