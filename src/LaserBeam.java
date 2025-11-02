import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

/**
 * Represents a high-powered laser beam weapon fired from the player ship.
 * The laser beam is a concentrated energy weapon that can quickly destroy asteroids.
 */
public class LaserBeam extends Projectile {
    private double angle;  // Direction of the beam in radians
    private double length; // Length of the beam in pixels
    private double thickness; // Thickness of the beam in pixels
    private double pulsePhase; // For pulsing animation effect

    /**
     * Creates a new laser beam at the specified starting position and angle.
     *
     * @param x Starting X coordinate
     * @param y Starting Y coordinate
     * @param angle Direction of the beam in radians
     */
    public LaserBeam(double x, double y, double angle) {
        super(x, y);
        this.angle = angle;
        this.length = GameEngine.WIDTH; // Beam extends across screen
        this.thickness = 4.0;
        this.pulsePhase = 0;
        this.vx = 0; // Laser beam doesn't move
        this.vy = 0;
        this.damageValue = 3; // Laser beam does 3 levels of damage
    }

    @Override
    public void update(double deltaTime) {
        // Animate the beam pulsing effect
        pulsePhase += deltaTime * 10.0;
        thickness = 3.0 + Math.sin(pulsePhase) * 1.5;
    }

    @Override
    public void draw(Graphics2D g) {
        AffineTransform old = g.getTransform();
        
        // Draw beam core
        drawBeamLayer(g, Color.RED, thickness);
        
        // Draw inner glow
        drawBeamLayer(g, new Color(255, 150, 150, 100), thickness * 2);
        
        // Draw outer glow
        drawBeamLayer(g, new Color(255, 100, 100, 50), thickness * 3);

        g.setTransform(old);
    }

    private void drawBeamLayer(Graphics2D g, Color color, double width) {
        g.setColor(color);
        g.setStroke(new BasicStroke((float)width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        double endX = x + Math.cos(angle) * length;
        double endY = y + Math.sin(angle) * length;
        g.draw(new Line2D.Double(x, y, endX, endY));
    }

    @Override
    public Rectangle getBounds() {
        // Create a thin rectangle along the beam's path
        double endX = x + Math.cos(angle) * length;
        double endY = y + Math.sin(angle) * length;
        return new Rectangle(
            (int)Math.min(x, endX),
            (int)Math.min(y, endY),
            (int)Math.abs(endX - x),
            (int)Math.abs(endY - y)
        );
    }

    @Override
    public double getRadius() {
        return thickness; // Use beam thickness as collision radius
    }

    /**
     * Gets the beam's angle for collision detection.
     * @return The beam's angle in radians
     */
    public double getAngle() {
        return angle;
    }

    /**
     * Gets the beam's length for collision detection.
     * @return The beam's length in pixels
     */
    public double getLength() {
        return length;
    }
}
