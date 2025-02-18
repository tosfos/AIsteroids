import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.Random;

public class Asteroid extends GameObject {
    private int size; // 3 = large, 1 = small.
    private double rotation;    // current rotation angle.
    private double rotationSpeed;  // rotation speed in radians per second.
    private double radius;      // visual radius (based on size).
    private Shape shape;
    
    private static final Random rand = new Random();
    
    public Asteroid(double x, double y, int size, double vx, double vy) {
       super(x, y);
       this.size = size;
       this.vx = vx;
       this.vy = vy;
       this.radius = size * 15;
       this.rotation = 0;
       this.rotationSpeed = (rand.nextDouble() - 0.5) * 2;  // Random rotation speed between -1 and 1 rad/s.
       
       // Create an irregular polygon for the asteroid shape
       int numPoints = 8 + rand.nextInt(5); // between 8 and 12 vertices
       double angleStep = 2 * Math.PI / numPoints;
       GeneralPath path = new GeneralPath();
       for (int i = 0; i < numPoints; i++) {
           double thisAngle = i * angleStep;
           // Vary radius between 70% and 130% of the base radius
           double offset = radius * (0.7 + rand.nextDouble() * 0.6);
           double px = Math.cos(thisAngle) * offset;
           double py = Math.sin(thisAngle) * offset;
           if (i == 0) {
               path.moveTo(px, py);
           } else {
               path.lineTo(px, py);
           }
       }
       path.closePath();
       this.shape = path;
    }
    
    @Override
    public void update(double deltaTime) {
       x += vx * deltaTime;
       y += vy * deltaTime;
       rotation += rotationSpeed * deltaTime;
    }
    
    @Override
    public void draw(Graphics2D g) {
       AffineTransform old = g.getTransform();
       g.translate(x, y);
       g.rotate(rotation);
       
       // Use a gradient to fill the asteroid shape for extra depth
       GradientPaint asteroidPaint = new GradientPaint((float)-radius, (float)-radius, Color.lightGray, (float)radius, (float)radius, Color.darkGray, true);
       Paint oldPaint = g.getPaint();
       g.setPaint(asteroidPaint);
       g.fill(shape);
       
       // Draw the asteroid outline
       g.setPaint(Color.black);
       g.setStroke(new BasicStroke(2));
       g.draw(shape);
       
       g.setPaint(oldPaint);
       g.setTransform(old);
    }
    
    @Override
    public Rectangle getBounds() {
       return new Rectangle((int)(x - radius), (int)(y - radius), (int)(radius * 2), (int)(radius * 2));
    }
    
    // Called when the asteroid is hit by a bullet.
    public void hit(GameEngine engine) {
       SoundManager.playExplosion();
       if (size > 1) {
          double speed = Math.hypot(vx, vy);
          double baseAngle = Math.atan2(vy, vx);
          double angle1 = baseAngle + Math.toRadians(20);
          double angle2 = baseAngle - Math.toRadians(20);
          engine.addGameObject(new Asteroid(x, y, size - 1, speed * Math.cos(angle1), speed * Math.sin(angle1)));
          engine.addGameObject(new Asteroid(x, y, size - 1, speed * Math.cos(angle2), speed * Math.sin(angle2)));
          // Award points based on asteroid size
          engine.addScore(size * 100);
       } else {
          // Small asteroids worth 100 points
          engine.addScore(100);
       }
       // Mark this asteroid as destroyed.
       alive = false;
    }
    
    // Factory method to create a random asteroid.
    public static Asteroid createRandomAsteroid(int screenWidth, int screenHeight, int size) {
       double x = rand.nextDouble() * screenWidth;
       double y = rand.nextDouble() * screenHeight;
       double angle = rand.nextDouble() * 2 * Math.PI;
       double speed = 50 + rand.nextDouble() * 50;
       double vx = speed * Math.cos(angle);
       double vy = speed * Math.sin(angle);
       return new Asteroid(x, y, size, vx, vy);
    }
} 