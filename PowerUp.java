import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.Random;

public class PowerUp extends GameObject {
    public enum PowerUpType {
        RAPID_FIRE("Rapid Fire", Color.ORANGE, 10.0),
        SPREAD_SHOT("Spread Shot", Color.CYAN, 8.0),
        SHIELD("Shield", Color.GREEN, 12.0),
        SPEED_BOOST("Speed Boost", Color.YELLOW, 6.0),
        MULTI_SHOT("Multi Shot", Color.MAGENTA, 15.0),
        LASER_BEAM("Laser Beam", Color.RED, 20.0);

        private final String name;
        private final Color color;
        private final double duration;

        PowerUpType(String name, Color color, double duration) {
            this.name = name;
            this.color = color;
            this.duration = duration;
        }

        public String getName() { return name; }
        public Color getColor() { return color; }
        public double getDuration() { return duration; }
    }

    private PowerUpType type;
    private double rotation = 0;
    private double rotationSpeed;
    private double pulsePhase = 0;
    private double lifespan = 30.0; // 30 seconds before disappearing
    private double timeAlive = 0;
    private static final Random rand = new Random();

    public PowerUp(double x, double y, PowerUpType type) {
        super(x, y);
        this.type = type;
        this.rotationSpeed = 2.0 + rand.nextDouble() * 2.0;
        this.vx = (rand.nextDouble() - 0.5) * 20;
        this.vy = (rand.nextDouble() - 0.5) * 20;
    }

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
        g.setColor(new Color(255, 255, 255, (int)(150 * alpha)));
        g.fillOval(-6, -6, 12, 12);

        // Type indicator symbol
        g.setColor(new Color(0, 0, 0, (int)(200 * alpha)));
        g.setFont(new Font("Arial", Font.BOLD, 8));
        FontMetrics fm = g.getFontMetrics();
        String symbol = getTypeSymbol();
        int symbolWidth = fm.stringWidth(symbol);
        g.drawString(symbol, -symbolWidth/2, 3);

        g.setTransform(old);
    }

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

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)x - 12, (int)y - 12, 24, 24);
    }

    @Override
    public double getRadius() {
        return 12.0; // PowerUp radius for collision detection
    }

    public PowerUpType getType() {
        return type;
    }

    public static PowerUp createRandomPowerUp(int screenWidth, int screenHeight) {
        PowerUpType[] types = PowerUpType.values();
        PowerUpType randomType = types[rand.nextInt(types.length)];

        // Spawn at random position
        double x = rand.nextDouble() * screenWidth;
        double y = rand.nextDouble() * screenHeight;

        return new PowerUp(x, y, randomType);
    }
}
