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
    private double lifetime; // Seconds remaining before beam expires

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
        // Beam is a brief flash tied to one fire action
        this.lifetime = 1.0 / GameConfig.TARGET_FPS;
    }

    /**
     * Creates a laser beam with a custom lifetime (used for power-up duration tie-in).
     */
    public LaserBeam(double x, double y, double angle, double lifetimeSeconds) {
        this(x, y, angle);
        this.lifetime = lifetimeSeconds;
    }

    @Override
    public void update(double deltaTime) {
        lifetime -= deltaTime;
        if (lifetime <= 0) {
            active = false;
            return;
        }
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

    /**
     * Checks if the beam segment intersects a circle (asteroid collision).
     */
    public boolean intersectsCircle(double cx, double cy, double radius) {
        double endX = x + Math.cos(angle) * length;
        double endY = y + Math.sin(angle) * length;
        return segmentIntersectsCircle(x, y, endX, endY, cx, cy, radius);
    }

    /**
     * Tests segment-circle intersection along the full beam path.
     */
    public static boolean segmentIntersectsCircle(double x1, double y1, double x2, double y2,
            double cx, double cy, double radius) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double fx = x1 - cx;
        double fy = y1 - cy;

        double a = dx * dx + dy * dy;
        if (a == 0) {
            double distSq = fx * fx + fy * fy;
            return distSq <= radius * radius;
        }

        double b = 2 * (fx * dx + fy * dy);
        double c = fx * fx + fy * fy - radius * radius;
        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) {
            return false;
        }

        discriminant = Math.sqrt(discriminant);
        double inv2a = 1.0 / (2 * a);
        double t1 = (-b - discriminant) * inv2a;
        double t2 = (-b + discriminant) * inv2a;

        if ((t1 >= 0 && t1 <= 1) || (t2 >= 0 && t2 <= 1)) {
            return true;
        }
        // Segment fully inside circle
        return t1 < 0 && t2 > 1;
    }
}
