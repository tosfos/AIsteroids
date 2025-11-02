import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Random;

/**
 * PowerUp represents collectible items that provide temporary enhancements to the player ship.
 * Power-ups spawn randomly during gameplay, drift across the screen, and disappear after a configurable
 * time period if not collected. Each power-up has unique visual characteristics and gameplay effects.
 *
 * @author AIsteroids Development Team
 * @version 1.0
 * @since 1.0
 */
public class PowerUp extends GameObject {

    /**
     * Enumeration of all available power-up types with their gameplay effects.
     * Each power-up has a name, visual color, and duration in seconds.
     */
    public enum PowerUpType {
        /**
         * Triples the player's firing rate for 10 seconds.
         * Effect: Base fire rate × 3.0
         * Visual: Orange color with "R" symbol
         */
        RAPID_FIRE("Rapid Fire", Color.ORANGE, 10.0),

        /**
         * Fires 3 bullets in a spread pattern (15° apart) for 8 seconds.
         * Effect: Single shot becomes 3 bullets with 15° spread
         * Visual: Cyan color with "S" symbol
         */
        SPREAD_SHOT("Spread Shot", Color.CYAN, 8.0),

        /**
         * Provides invulnerability to asteroid collisions for 12 seconds.
         * Effect: Blocks all asteroid damage, triggers achievement tracking
         * Visual: Green color with "♦" symbol
         */
        SHIELD("Shield", Color.GREEN, 12.0),

        /**
         * Increases ship speed and maneuverability for 6 seconds.
         * Effect: Max speed 300→500, rotation speed 180°→270°/s, acceleration 200→400
         * Visual: Yellow color with "»" symbol
         */
        SPEED_BOOST("Speed Boost", Color.YELLOW, 6.0),

        /**
         * Fires 5 bullets in a wide spread pattern (22.5° apart) for 15 seconds.
         * Effect: Single shot becomes 5 bullets with 22.5° spread
         * Visual: Magenta color with "M" symbol
         */
        MULTI_SHOT("Multi Shot", Color.MAGENTA, 15.0),

        /**
         * High-damage beam weapon for 20 seconds.
         * Effect: Currently reserved for future implementation
         * Visual: Red color with "L" symbol
         */
        LASER_BEAM("Laser Beam", Color.RED, 20.0);

        private final String name;
        private final Color color;
        private final double duration;

        /**
         * Constructs a PowerUpType with specified properties.
         * @param name Human-readable name of the power-up
         * @param color Visual color used for rendering
         * @param duration Effect duration in seconds
         */
        PowerUpType(String name, Color color, double duration) {
            this.name = name;
            this.color = color;
            this.duration = duration;
        }

        /** @return Human-readable name of this power-up type */
        public String getName() { return name; }

        /** @return Visual color used for rendering this power-up */
        public Color getColor() { return color; }

        /** @return Duration of the power-up effect in seconds */
        public double getDuration() { return duration; }
    }

    /** The type of this power-up instance */
    private PowerUpType type;

    /** Current rotation angle for visual spinning effect */
    private double rotation = 0;

    /** Speed of rotation in radians per second */
    private double rotationSpeed;

    /** Phase for pulsing glow animation */
    private double pulsePhase = 0;

    /** Total time the power-up remains in the world before disappearing */
    private double lifespan = GameConfig.PowerUp.LIFETIME; // seconds before disappearing

    /** Time this power-up has been alive */
    private double timeAlive = 0;

    /** Random number generator for various effects */
    private static final Random rand = new Random();

    /**
     * Creates a new PowerUp at the specified location.
     * The power-up will have random initial velocity and rotation speed.
     *
     * @param x Initial X coordinate
     * @param y Initial Y coordinate
     * @param type The type of power-up to create
     */
    public PowerUp(double x, double y, PowerUpType type) {
        super(x, y);
        this.type = type;
        this.rotationSpeed = 2.0 + rand.nextDouble() * 2.0;
        this.vx = (rand.nextDouble() - 0.5) * 20;
        this.vy = (rand.nextDouble() - 0.5) * 20;
    }

    /**
     * Updates the power-up's position, rotation, and lifetime.
     * Power-ups drift slowly across the screen and become inactive after their lifespan expires.
     *
     * @param deltaTime Time elapsed since last update in seconds
     */
    @Override
    public void update(double deltaTime) {
        x += vx * deltaTime;
        y += vy * deltaTime;
        rotation += rotationSpeed * deltaTime;
        pulsePhase += deltaTime * 4;
        timeAlive += deltaTime;

        // Fade out over last 5 seconds
        if (timeAlive >= lifespan) {
            active = false;
        }
    }

    /**
     * Renders the power-up with visual effects including glow, pulsing, and fade-out.
     * The power-up displays its type symbol and fades out during the last 5 seconds of life.
     *
     * @param g Graphics context for rendering
     */
    @Override
    public void draw(Graphics2D g) {
        AffineTransform old = g.getTransform();
        g.translate(x, y);
        g.rotate(rotation);

        // Pulsing glow effect
        float pulseScale = 1.0f + 0.3f * (float)Math.sin(pulsePhase);
        float alpha = 1.0f;

        // Fade out in last 5 seconds
        if (timeAlive > lifespan - 5) {
            alpha = (float)(lifespan - timeAlive) / 5.0f;
        }

        // Outer glow
        Color glowColor = new Color(type.getColor().getRed(), type.getColor().getGreen(),
                                   type.getColor().getBlue(), (int)(100 * alpha));
        g.setColor(glowColor);
        g.fillOval((int)(-15 * pulseScale), (int)(-15 * pulseScale),
                   (int)(30 * pulseScale), (int)(30 * pulseScale));

        // Main body
        Color mainColor = new Color(type.getColor().getRed(), type.getColor().getGreen(),
                                   type.getColor().getBlue(), (int)(255 * alpha));
        g.setColor(mainColor);
        g.fillOval(-10, -10, 20, 20);

        // Inner highlight
        g.setColor(GraphicsUtils.colorWithAlpha(255, 255, 255, alpha * 0.59f));
        g.fillOval(-6, -6, 12, 12);

        // Type indicator symbol
        g.setColor(GraphicsUtils.colorWithAlpha(0, 0, 0, alpha * 0.78f));
        g.setFont(new Font("Arial", Font.BOLD, 8));
        FontMetrics fm = g.getFontMetrics();
        String symbol = getTypeSymbol();
        int symbolWidth = fm.stringWidth(symbol);
        g.drawString(symbol, -symbolWidth/2, 3);

        g.setTransform(old);
    }

    /**
     * Returns the visual symbol character for this power-up type.
     * Used in both the power-up itself and the HUD status indicators.
     *
     * @return Single character symbol representing this power-up type
     */
    private String getTypeSymbol() {
        switch (type) {
            case RAPID_FIRE: return "R";
            case SPREAD_SHOT: return "S";
            case SHIELD: return "♦";
            case SPEED_BOOST: return "»";
            case MULTI_SHOT: return "M";
            case LASER_BEAM: return "L";
            default: return "?";
        }
    }

    /**
     * Returns the bounding rectangle for collision detection.
     * @return Rectangle representing the power-up's collision bounds
     */
    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)x - 12, (int)y - 12, 24, 24);
    }

    /**
     * Returns the collision radius for circular collision detection.
     * @return Radius in pixels for collision detection
     */
    @Override
    public double getRadius() {
        return 12.0; // PowerUp radius for collision detection
    }

    /**
     * Gets the type of this power-up.
     * @return The PowerUpType of this power-up instance
     */
    public PowerUpType getType() {
        return type;
    }

    /**
     * Creates a random power-up at a random position within screen bounds.
     * Used by the wave system to spawn power-ups during gameplay.
     *
     * @param screenWidth Width of the game screen
     * @param screenHeight Height of the game screen
     * @return A new PowerUp instance with random type and position
     */
    public static PowerUp createRandomPowerUp(int screenWidth, int screenHeight) {
        PowerUpType[] types = PowerUpType.values();
        PowerUpType randomType = types[rand.nextInt(types.length)];

        // Spawn at random position
        double x = rand.nextDouble() * screenWidth;
        double y = rand.nextDouble() * screenHeight;

        return new PowerUp(x, y, randomType);
    }
}
