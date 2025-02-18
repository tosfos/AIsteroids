import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

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
    
    // Constants for rotation and acceleration.
    private final double rotationSpeed = Math.toRadians(180); // 180° per second.
    private final double acceleration = 200; // pixels per second^2.
    private final double maxSpeed = 300; // pixels per second.
    
    public PlayerShip(double x, double y) {
       super(x, y);
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
    }
    
    @Override
    public void draw(Graphics2D g) {
        AffineTransform old = g.getTransform();
        g.translate(x, y);
        g.rotate(angle + Math.PI/2);
        
        // If invulnerable, make the ship blink by reducing opacity
        Composite oldComposite = g.getComposite();
        if (invulnerabilityTimer > 0) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        }
        
        // Create detailed spaceship shape using GeneralPath
        GeneralPath ship = new GeneralPath();
        ship.moveTo(0, -15);     // Ship tip (now pointing right)
        ship.lineTo(-10, 10);    // Left wing tip
        ship.lineTo(0, 5);       // Rear center
        ship.lineTo(10, 10);     // Right wing tip
        ship.closePath();
        
        // Fill the ship with a gradient for a 3D effect
        GradientPaint shipPaint = new GradientPaint(-10, -15, Color.darkGray, 10, 10, Color.lightGray, true);
        Paint oldPaint = g.getPaint();
        g.setPaint(shipPaint);
        g.fill(ship);
        
        // Draw ship outline
        g.setPaint(Color.black);
        g.setStroke(new BasicStroke(2));
        g.draw(ship);
        
        // If accelerating, draw a flame effect behind the ship
        if (accelerating) {
            GeneralPath flame = new GeneralPath();
            flame.moveTo(-4, 10);
            flame.lineTo(0, 20);
            flame.lineTo(4, 10);
            flame.closePath();
            GradientPaint flamePaint = new GradientPaint(0, 10, Color.orange, 0, 20, Color.red, true);
            g.setPaint(flamePaint);
            g.fill(flame);
        }
        
        // Restore original paint, composite and transform
        g.setPaint(oldPaint);
        g.setComposite(oldComposite);
        g.setTransform(old);
    }
    
    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)x - 10, (int)y - 10, 20, 20);
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
            alive = false;  // Ship destroyed.
        } else {
            // Reset ship to the center and clear velocity.
            x = GameEngine.WIDTH / 2;
            y = GameEngine.HEIGHT / 2;
            vx = 0;
            vy = 0;
            // Set invulnerability period (2 seconds) to prevent immediate further damage
            invulnerabilityTimer = 2.0;
        }
    }
    
    // Fire a bullet from the ship's tip.
    public Bullet fireBullet() {
        SoundManager.playLaser();
        double bulletX = x + Math.cos(angle) * 15;
        double bulletY = y + Math.sin(angle) * 15;
        return new Bullet(bulletX, bulletY, angle);
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
        // Ensure alive
        alive = true;
    }
} 