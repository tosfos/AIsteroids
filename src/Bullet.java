import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a projectile bullet fired from the player ship.
 *
 * <p>Bullets have a limited lifetime and move at a constant speed in a
 * straight line. They include visual effects like glowing trails and
 * sparkle animations for enhanced visual appeal.</p>
 *
 * <p>Key features:
 * <ul>
 *   <li>Constant speed movement in specified direction</li>
 *   <li>Automatic expiration after configured lifetime</li>
 *   <li>Visual trail effect for better visibility</li>
 *   <li>Multi-layer glow rendering</li>
 *   <li>Sparkle animation effects</li>
 * </ul>
 * </p>
 *
 * @author AIsteroids Development Team
 * @version 1.0
 */
public class Bullet extends Projectile {
    private double angle;
    private double speed = GameConfig.Bullet.SPEED; // pixels per second.
    private double lifespan = GameConfig.Bullet.LIFETIME; // Bullet lifetime from config
    private double timeAlive = 0;
    private List<TrailPoint> trail;
    private int trailLength = 8;

    /**
     * Creates a new bullet at the specified position and angle.
     *
     * @param x Starting X coordinate
     * @param y Starting Y coordinate
     * @param angle Direction of travel in radians (0 = right, PI/2 = down)
     */
    public Bullet(double x, double y, double angle) {
       super(x, y);
       this.angle = angle;
       // Compute initial velocity.
       vx = speed * Math.cos(angle);
       vy = speed * Math.sin(angle);
       trail = new ArrayList<>();
    }

    @Override
    public void update(double deltaTime) {
       // Add current position to trail
       trail.add(new TrailPoint(x, y, timeAlive));

       // Keep trail length manageable
       while (trail.size() > trailLength) {
           trail.remove(0);
       }

       x += vx * deltaTime;
       y += vy * deltaTime;
       timeAlive += deltaTime;

       if(timeAlive > lifespan) {
          active = false;
       }
    }

    @Override
    public void draw(Graphics2D g) {
       // Draw glowing trail first
       drawTrail(g);

       // Create a more impressive bullet effect
       int bulletSize = 12;

       // Multi-layer glow effect
       Color[] glowColors = {
           new Color(255, 255, 255, 255), // Bright white core
           new Color(100, 200, 255, 200), // Blue middle
           new Color(50, 150, 255, 100),  // Outer blue glow
           new Color(0, 100, 255, 50)     // Very outer glow
       };

       float[] glowRadii = {2f, 4f, 6f, 8f};

       // Save transform and current settings
       AffineTransform old = g.getTransform();
       Paint oldPaint = g.getPaint();
       Composite oldComposite = g.getComposite();

       g.translate(x, y);

       // Draw multiple glow layers
       for (int i = glowColors.length - 1; i >= 0; i--) {
           float[] dist = {0.0f, 1.0f};
           Color[] colors = {glowColors[i], new Color(glowColors[i].getRed(), glowColors[i].getGreen(), glowColors[i].getBlue(), 0)};
           RadialGradientPaint paint = new RadialGradientPaint(0, 0, glowRadii[i], dist, colors);

           g.setPaint(paint);
           g.fillOval((int)(-glowRadii[i]), (int)(-glowRadii[i]),
                     (int)(glowRadii[i] * 2), (int)(glowRadii[i] * 2));
       }

       // Add sparkle effect
       drawSparkles(g);

       // Restore graphics settings
       g.setPaint(oldPaint);
       g.setComposite(oldComposite);
       g.setTransform(old);
    }

    private void drawTrail(Graphics2D g) {
        if (trail.size() < 2) return;

        Composite oldComposite = g.getComposite();

        for (int i = 0; i < trail.size() - 1; i++) {
            TrailPoint p1 = trail.get(i);
            TrailPoint p2 = trail.get(i + 1);

            // Calculate fade based on age
            float age = (float)(timeAlive - p1.time);
            float maxAge = 0.3f; // Trail lasts 0.3 seconds
            float alpha = Math.max(0, 1.0f - (age / maxAge));

            if (alpha > 0) {
                // Trail gets thinner and more transparent as it gets older
                float thickness = 3.0f * alpha;

                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.8f));
                g.setColor(new Color(100, 200, 255));
                g.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                // Don't draw trail if points are too far apart (screen wrap)
                double dx = p2.x - p1.x;
                double dy = p2.y - p1.y;
                double distanceSquared = dx * dx + dy * dy;
                if (distanceSquared < GameEngine.WIDTH * 0.25) { // Only draw if less than half screen width apart
                    g.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
                }
            }
        }

        g.setComposite(oldComposite);
    }

    private void drawSparkles(Graphics2D g) {
        // Add animated sparkle effects around the bullet
        long time = System.currentTimeMillis();
        for (int i = 0; i < 6; i++) {
            double sparkleAngle = (time * 0.01 + i * Math.PI / 3) % (2 * Math.PI);
            double sparkleDistance = 8 + 3 * Math.sin(time * 0.02 + i);
            double sparkleX = Math.cos(sparkleAngle) * sparkleDistance;
            double sparkleY = Math.sin(sparkleAngle) * sparkleDistance;

            float sparkleAlpha = (float)(0.5 + 0.5 * Math.sin(time * 0.03 + i));
            g.setColor(new Color(255, 255, 255, (int)(100 * sparkleAlpha)));
            g.fillOval((int)(sparkleX - 1), (int)(sparkleY - 1), 2, 2);
        }
    }

    @Override
    public Rectangle getBounds() {
       return new Rectangle((int)x - 3, (int)y - 3, 6, 6);
    }

    @Override
    public double getRadius() {
        return 3.0; // Bullet radius for collision detection
    }

    // Inner class for trail points
    private static class TrailPoint {
        double x, y, time;

        TrailPoint(double x, double y, double time) {
            this.x = x;
            this.y = y;
            this.time = time;
        }
    }
}
