import java.awt.*;
import java.awt.geom.AffineTransform;

public class Bullet extends GameObject {
    private double angle;
    private double speed = 400; // pixels per second.
    private double lifespan = 2; // Bullet lasts for 2 seconds.
    private double timeAlive = 0;
    
    public Bullet(double x, double y, double angle) {
       super(x, y);
       this.angle = angle;
       // Compute initial velocity.
       vx = speed * Math.cos(angle);
       vy = speed * Math.sin(angle);
    }
    
    @Override
    public void update(double deltaTime) {
       x += vx * deltaTime;
       y += vy * deltaTime;
       timeAlive += deltaTime;
       if(timeAlive > lifespan) {
          alive = false;
       }
    }
    
    @Override
    public void draw(Graphics2D g) {
       // Create a glowing bullet effect using RadialGradientPaint
       int bulletSize = 8;
       float radius = bulletSize / 2f;
       float[] dist = {0.0f, 1.0f};
       Color[] colors = {Color.white, Color.yellow};
       RadialGradientPaint paint = new RadialGradientPaint(new java.awt.geom.Point2D.Float(radius, radius), radius, dist, colors);
       
       // Save transform and current paint
       AffineTransform old = g.getTransform();
       Paint oldPaint = g.getPaint();
       
       // Translate so that the bullet is drawn centered correctly
       g.translate((int)x - bulletSize/2, (int)y - bulletSize/2);
       g.setPaint(paint);
       g.fillOval(0, 0, bulletSize, bulletSize);
       
       // Restore graphics settings
       g.setPaint(oldPaint);
       g.setTransform(old);
    }
    
    @Override
    public Rectangle getBounds() {
       return new Rectangle((int)x - 2, (int)y - 2, 4, 4);
    }
} 