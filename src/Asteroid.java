import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.Random;

/**
 * Represents an asteroid obstacle in the game.
 * 
 * <p>Asteroids come in three sizes (large, medium, small) and split into
 * smaller asteroids when destroyed. Each asteroid has a randomly generated
 * irregular shape and unique coloring for visual variety.</p>
 * 
 * <p>Key features:
 * <ul>
 *   <li>Three size levels (3 = large, 2 = medium, 1 = small)</li>
 *   <li>Splits into two smaller asteroids when hit (except smallest)</li>
 *   <li>Random irregular polygon shape generation</li>
 *   <li>Rotating animation</li>
 *   <li>Varied color schemes for visual diversity</li>
 *   <li>Radial gradient lighting effects</li>
 * </ul>
 * </p>
 * 
 * @author AIsteroids Development Team
 * @version 1.0
 */
public class Asteroid extends GameObject {
    private int size; // 3 = large, 1 = small.
    private double rotation;    // current rotation angle.
    private double rotationSpeed;  // rotation speed in radians per second.
    private double radius;      // visual radius (based on size).
    private Shape shape;
    private Color baseColor;
    private Color[] textureColors;

    private static final Random rand = new Random();

    /**
     * Creates a new asteroid at the specified position.
     * 
     * @param x Initial X coordinate
     * @param y Initial Y coordinate
     * @param size Asteroid size (1 = small, 2 = medium, 3 = large)
     * @param vx Initial X velocity in pixels per second
     * @param vy Initial Y velocity in pixels per second
     * @throws IllegalArgumentException if size is not 1, 2, or 3
     */
    public Asteroid(double x, double y, int size, double vx, double vy) {
       super(x, y);
       this.size = InputValidator.validateAsteroidSize(size);
       this.vx = vx;
       this.vy = vy;
       this.radius = size * GameConfig.Asteroid.RADIUS_PER_SIZE;
       this.rotation = 0;
       this.rotationSpeed = (rand.nextDouble() - 0.5) * GameConfig.Asteroid.MAX_ROTATION_SPEED;

       // Generate random asteroid colors for variety
       generateAsteroidColors();

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

    private void generateAsteroidColors() {
        // Generate varied asteroid colors (browns, grays, with some color variation)
        int baseGray = 60 + rand.nextInt(80); // 60-140
        int redTint = baseGray + rand.nextInt(30);
        int greenTint = baseGray + rand.nextInt(20);
        int blueTint = baseGray - rand.nextInt(15);

        baseColor = new Color(
            Math.min(255, redTint),
            Math.min(255, greenTint),
            Math.max(0, blueTint)
        );

        // Create texture color variations
        textureColors = new Color[4];
        for (int i = 0; i < 4; i++) {
            int variation = rand.nextInt(40) - 20;
            textureColors[i] = new Color(
                Math.max(0, Math.min(255, baseColor.getRed() + variation)),
                Math.max(0, Math.min(255, baseColor.getGreen() + variation)),
                Math.max(0, Math.min(255, baseColor.getBlue() + variation))
            );
        }
    }

    @Override
    public void update(double deltaTime) {
       InputValidator.validateDeltaTime(deltaTime);
       x += vx * deltaTime;
       y += vy * deltaTime;
       rotation += rotationSpeed * deltaTime;
    }

    @Override
    public void draw(Graphics2D g) {
       AffineTransform old = g.getTransform();
       g.translate(x, y);
       g.rotate(rotation);

       // Create advanced lighting effect
       float lightX = (float)(-radius * 0.3);
       float lightY = (float)(-radius * 0.3);
       float darkX = (float)(radius * 0.5);
       float darkY = (float)(radius * 0.5);

       // Main asteroid body with radial gradient
       Color lightColor = new Color(
           Math.min(255, baseColor.getRed() + 60),
           Math.min(255, baseColor.getGreen() + 60),
           Math.min(255, baseColor.getBlue() + 60)
       );
       Color darkColor = new Color(
           Math.max(0, baseColor.getRed() - 40),
           Math.max(0, baseColor.getGreen() - 40),
           Math.max(0, baseColor.getBlue() - 40)
       );

       float[] dist = {0.0f, 0.7f, 1.0f};
       Color[] colors = {lightColor, baseColor, darkColor};
       RadialGradientPaint asteroidPaint = new RadialGradientPaint(
           lightX, lightY, (float)radius * 1.2f, dist, colors);

       Paint oldPaint = g.getPaint();
       g.setPaint(asteroidPaint);
       g.fill(shape);

               // Draw the asteroid outline with subtle glow
        g.setPaint(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 100));
        g.setStroke(new BasicStroke(3f));
        g.draw(shape);

        g.setPaint(new Color(lightColor.getRed(), lightColor.getGreen(), lightColor.getBlue(), 150));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(shape);

                g.setPaint(oldPaint);
        g.setTransform(old);
    }

    @Override
    public Rectangle getBounds() {
       return new Rectangle((int)(x - radius), (int)(y - radius), (int)(radius * 2), (int)(radius * 2));
    }

    @Override
    public double getRadius() {
        return radius;
    }

    // Called when the asteroid is hit by a bullet.
    public void hit(GameEngine engine) {
       InputValidator.validateNotNull(engine, "engine");

       InputValidator.safeExecute(() -> SoundManager.playExplosion(),
           "Failed to play explosion sound");

       // Trigger particle explosion effect
       triggerExplosionEffect(engine);

       if (size > 1) {
          double speed = Math.hypot(vx, vy);
          double baseAngle = Math.atan2(vy, vx);
          double angle1 = baseAngle + Math.toRadians(30 + rand.nextInt(30));
          double angle2 = baseAngle - Math.toRadians(30 + rand.nextInt(30));

          double newSpeed1 = speed * (0.8 + rand.nextDouble() * 0.4);
          double newSpeed2 = speed * (0.8 + rand.nextDouble() * 0.4);

          engine.addGameObject(new Asteroid(x, y, size - 1,
              newSpeed1 * Math.cos(angle1), newSpeed1 * Math.sin(angle1)));
          engine.addGameObject(new Asteroid(x, y, size - 1,
              newSpeed2 * Math.cos(angle2), newSpeed2 * Math.sin(angle2)));

                 // Award points based on asteroid size with wave multiplier
       int basePoints = size * 100;
       int multipliedPoints = basePoints * engine.getWaveSystem().getScoreMultiplier();
       engine.addScore(multipliedPoints);

       // Notify wave system of asteroid destruction
       engine.getWaveSystem().asteroidDestroyed();

       // Track achievement progress
       LeaderboardSystem.asteroidDestroyed();
       } else {
          // Small asteroids worth 100 points with wave multiplier
          int multipliedPoints = 100 * engine.getWaveSystem().getScoreMultiplier();
          engine.addScore(multipliedPoints);

          // Notify wave system of asteroid destruction
          engine.getWaveSystem().asteroidDestroyed();

          // Track achievement progress
          LeaderboardSystem.asteroidDestroyed();
       }
       // Mark this asteroid as destroyed.
       active = false;
    }

    private void triggerExplosionEffect(GameEngine engine) {
        // Create explosion particles through the engine
        engine.createExplosionEffect(x, y, size);

        // Create debris when asteroid breaks apart
        engine.createDebrisEffect(x, y, size * 3);
    }

    // Factory method to create a random asteroid.
    public static Asteroid createRandomAsteroid(int screenWidth, int screenHeight, int size) {
       double x, y;

       // Spawn asteroids from screen edges
       if (rand.nextBoolean()) {
           x = rand.nextBoolean() ? -50 : screenWidth + 50;
           y = rand.nextDouble() * screenHeight;
       } else {
           x = rand.nextDouble() * screenWidth;
           y = rand.nextBoolean() ? -50 : screenHeight + 50;
       }

       double angle = rand.nextDouble() * 2 * Math.PI;
       double speed = GameConfig.Asteroid.MIN_SPEED + rand.nextDouble() * (GameConfig.Asteroid.MAX_SPEED - GameConfig.Asteroid.MIN_SPEED); // Speed from config
       double vx = speed * Math.cos(angle);
       double vy = speed * Math.sin(angle);
       return new Asteroid(x, y, size, vx, vy);
    }
}
