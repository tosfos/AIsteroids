import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages particle effects for visual enhancements in the game.
 *
 * <p>This class handles creation, update, and rendering of various particle types including:
 * <ul>
 *   <li>Explosion particles (when asteroids are destroyed)</li>
 *   <li>Spark particles (impact effects)</li>
 *   <li>Debris particles (asteroid fragments)</li>
 *   <li>Warp particles (ship teleportation effects)</li>
 *   <li>Shockwave particles (area effects)</li>
 * </ul>
 * </p>
 *
 * <p>Key features:
 * <ul>
 *   <li>Thread-safe particle management using CopyOnWriteArrayList</li>
 *   <li>Automatic cleanup of dead particles</li>
 *   <li>Memory leak prevention via maximum particle limits</li>
 *   <li>Synchronized update and rendering for thread safety</li>
 * </ul>
 * </p>
 *
 * <p>Thread safety: All public methods are synchronized to ensure thread-safe
 * access from multiple threads (game update thread and rendering thread).</p>
 *
 * @author AIsteroids Development Team
 * @version 1.0
 */
public class ParticleSystem {
    private List<Particle> particles;
    private static final Random rand = new Random();

    public ParticleSystem() {
        particles = new CopyOnWriteArrayList<>();
    }

    public synchronized void update(double deltaTime) {
        // Remove dead particles first
        particles.removeIf(particle -> !particle.isAlive());

        // Enforce maximum particle count to prevent memory leaks
        // Remove oldest particles if we exceed the limit
        while (particles.size() > GameConfig.Effects.MAX_TOTAL_PARTICLES) {
            particles.remove(0);
        }

        for (Particle particle : particles) {
            particle.update(deltaTime);
        }
    }

    public synchronized void draw(Graphics2D g) {
        // Iterate directly since we're synchronized - no copy needed
        for (Particle particle : particles) {
            if (particle.isAlive()) {
                particle.draw(g);
            }
        }
    }

    public synchronized void createExplosion(double x, double y, int intensity) {
        // Main explosion particles
        for (int i = 0; i < intensity * 8; i++) {
            double angle = rand.nextDouble() * 2 * Math.PI;
            double speed = GameConfig.Effects.Explosion.SPEED_MIN + rand.nextDouble() * (GameConfig.Effects.Explosion.SPEED_MAX - GameConfig.Effects.Explosion.SPEED_MIN);
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;

            Color color;
            if (rand.nextDouble() < 0.3) {
                color = Color.YELLOW;
            } else if (rand.nextDouble() < 0.6) {
                color = Color.ORANGE;
            } else {
                color = Color.RED;
            }

            particles.add(new ExplosionParticle(x, y, vx, vy, color, GameConfig.Effects.Explosion.LIFETIME_MIN + rand.nextDouble() * (GameConfig.Effects.Explosion.LIFETIME_MAX - GameConfig.Effects.Explosion.LIFETIME_MIN)));
        }

        // Shockwave
        particles.add(new ShockwaveParticle(x, y, intensity * 15));

        // Sparks
        for (int i = 0; i < intensity * 3; i++) {
            double angle = rand.nextDouble() * 2 * Math.PI;
            double speed = GameConfig.Effects.Spark.SPEED_MIN + rand.nextDouble() * (GameConfig.Effects.Spark.SPEED_MAX - GameConfig.Effects.Spark.SPEED_MIN);
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            particles.add(new SparkParticle(x, y, vx, vy, GameConfig.Effects.Spark.LIFETIME_MIN + rand.nextDouble() * (GameConfig.Effects.Spark.LIFETIME_MAX - GameConfig.Effects.Spark.LIFETIME_MIN)));
        }
    }

    public synchronized void createDebris(double x, double y, int count) {
        for (int i = 0; i < count; i++) {
            double angle = rand.nextDouble() * 2 * Math.PI;
            double speed = GameConfig.Effects.Debris.SPEED_MIN + rand.nextDouble() * (GameConfig.Effects.Debris.SPEED_MAX - GameConfig.Effects.Debris.SPEED_MIN);
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;

            Color debrisColor = new Color(
                100 + rand.nextInt(100),
                80 + rand.nextInt(80),
                60 + rand.nextInt(60)
            );

            particles.add(new DebrisParticle(x, y, vx, vy, debrisColor, GameConfig.Effects.Debris.LIFETIME_MIN + rand.nextDouble() * (GameConfig.Effects.Debris.LIFETIME_MAX - GameConfig.Effects.Debris.LIFETIME_MIN)));
        }
    }

    public synchronized void createImpactSparks(double x, double y, double impactAngle) {
        for (int i = 0; i < 5; i++) {
            double spreadAngle = impactAngle + (rand.nextDouble() - 0.5) * GameConfig.Angles.PI_OVER_2;
            double speed = GameConfig.Effects.Spark.SPEED_MIN + rand.nextDouble() * (GameConfig.Effects.Spark.SPEED_MAX - GameConfig.Effects.Spark.SPEED_MIN);
            double vx = Math.cos(spreadAngle) * speed;
            double vy = Math.sin(spreadAngle) * speed;
            particles.add(new SparkParticle(x, y, vx, vy, GameConfig.Effects.Spark.LIFETIME_MIN + rand.nextDouble() * (GameConfig.Effects.Spark.LIFETIME_MAX - GameConfig.Effects.Spark.LIFETIME_MIN)));
        }
    }

    public synchronized void createWarpEffect(double x, double y) {
        for (int i = 0; i < 20; i++) {
            double angle = rand.nextDouble() * 2 * Math.PI;
            double speed = GameConfig.Effects.Warp.SPEED_MIN + rand.nextDouble() * (GameConfig.Effects.Warp.SPEED_MAX - GameConfig.Effects.Warp.SPEED_MIN);
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            particles.add(new WarpParticle(x, y, vx, vy, GameConfig.Effects.Warp.LIFETIME));
        }
    }

    public synchronized void clear() {
        particles.clear();
    }

    // Base particle class
    private static abstract class Particle {
        protected double x, y, vx, vy;
        protected double life, maxLife;
        protected Color color;

        public Particle(double x, double y, double vx, double vy, Color color, double life) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.life = life;
            this.maxLife = life;
        }

        public void update(double deltaTime) {
            x += vx * deltaTime;
            y += vy * deltaTime;
            life -= deltaTime;
            updateSpecific(deltaTime);
        }

        protected abstract void updateSpecific(double deltaTime);
        public abstract void draw(Graphics2D g);

        public boolean isAlive() {
            return life > 0;
        }

        protected float getAlpha() {
            return Math.max(0, Math.min(1, (float)(life / maxLife)));
        }
    }

    // Explosion particle
    private static class ExplosionParticle extends Particle {
        private double size;
        private double initialSize;

        public ExplosionParticle(double x, double y, double vx, double vy, Color color, double life) {
            super(x, y, vx, vy, color, life);
            this.size = 2 + rand.nextDouble() * 4;
            this.initialSize = size;
        }

        @Override
        protected void updateSpecific(double deltaTime) {
            vx *= 0.98; // Air resistance
            vy *= 0.98;
            size = initialSize * getAlpha();
        }

        @Override
        public void draw(Graphics2D g) {
            float alpha = getAlpha();
            Color drawColor = GraphicsUtils.colorWithAlpha(color, alpha);
            g.setColor(drawColor);
            g.fillOval((int)(x - size), (int)(y - size), (int)(size * 2), (int)(size * 2));

            // Inner bright core
            g.setColor(GraphicsUtils.colorWithAlpha(255, 255, 200, alpha * 0.59f));
            g.fillOval((int)(x - size/2), (int)(y - size/2), (int)(size), (int)(size));
        }
    }

    // Shockwave particle
    private static class ShockwaveParticle extends Particle {
        private double radius;
        private double maxRadius;

        public ShockwaveParticle(double x, double y, double maxRadius) {
            super(x, y, 0, 0, Color.WHITE, 0.5);
            this.radius = 0;
            this.maxRadius = maxRadius;
        }

        @Override
        protected void updateSpecific(double deltaTime) {
            radius += maxRadius * deltaTime * 4; // Expand quickly
        }

        @Override
        public void draw(Graphics2D g) {
            if (radius < maxRadius) {
                float alpha = getAlpha() * 0.7f;
                g.setColor(GraphicsUtils.colorWithAlpha(255, 255, 255, alpha));
                g.setStroke(new BasicStroke(3f));
                g.drawOval((int)(x - radius), (int)(y - radius),
                          (int)(radius * 2), (int)(radius * 2));

                g.setColor(GraphicsUtils.colorWithAlpha(255, 200, 100, alpha * 0.59f));
                g.setStroke(new BasicStroke(1f));
                g.drawOval((int)(x - radius), (int)(y - radius),
                          (int)(radius * 2), (int)(radius * 2));
            }
        }
    }

    // Spark particle
    private static class SparkParticle extends Particle {
        public SparkParticle(double x, double y, double vx, double vy, double life) {
            super(x, y, vx, vy, Color.YELLOW, life);
            // Length is determined by visual trail in draw() method
        }

        @Override
        protected void updateSpecific(double deltaTime) {
            vy += 50 * deltaTime; // Gravity
            vx *= 0.99;
            vy *= 0.99;
        }

        @Override
        public void draw(Graphics2D g) {
            float alpha = getAlpha();
            g.setColor(GraphicsUtils.colorWithAlpha(255, 255, 100, alpha));
            g.setStroke(new BasicStroke(2f));

            double prevX = x - vx * 0.01;
            double prevY = y - vy * 0.01;
            g.drawLine((int)prevX, (int)prevY, (int)x, (int)y);
        }
    }

    // Debris particle
    private static class DebrisParticle extends Particle {
        private double size;
        private double rotation;
        private double rotationSpeed;

        public DebrisParticle(double x, double y, double vx, double vy, Color color, double life) {
            super(x, y, vx, vy, color, life);
            this.size = 1 + rand.nextDouble() * 3;
            this.rotation = rand.nextDouble() * 2 * Math.PI;
            this.rotationSpeed = (rand.nextDouble() - 0.5) * 8;
        }

        @Override
        protected void updateSpecific(double deltaTime) {
            vy += 30 * deltaTime; // Gravity
            vx *= 0.98;
            vy *= 0.98;
            rotation += rotationSpeed * deltaTime;
        }

        @Override
        public void draw(Graphics2D g) {
            AffineTransform old = g.getTransform();
            g.translate(x, y);
            g.rotate(rotation);

            float alpha = getAlpha();
            Color drawColor = GraphicsUtils.colorWithAlpha(color, alpha);
            g.setColor(drawColor);
            g.fillRect((int)(-size), (int)(-size), (int)(size * 2), (int)(size * 2));

            g.setTransform(old);
        }
    }

    // Warp effect particle
    private static class WarpParticle extends Particle {
        private double size;

        public WarpParticle(double x, double y, double vx, double vy, double life) {
            super(x, y, vx, vy, Color.CYAN, life);
            this.size = 1 + rand.nextDouble() * 2;
        }

        @Override
        protected void updateSpecific(double deltaTime) {
            // Particles slow down and converge toward center
            vx *= 0.95;
            vy *= 0.95;
        }

        @Override
        public void draw(Graphics2D g) {
            float alpha = getAlpha();
            Color drawColor = GraphicsUtils.colorWithAlpha(0, 255, 255, alpha);
            g.setColor(drawColor);

            // Draw as a small glowing dot
            RadialGradientPaint paint = new RadialGradientPaint(
                (float)x, (float)y, (float)size * 2,
                new float[]{0.0f, 1.0f},
                new Color[]{drawColor, new Color(0, 255, 255, 0)}
            );
            g.setPaint(paint);
            g.fillOval((int)(x - size), (int)(y - size), (int)(size * 2), (int)(size * 2));
        }
    }
}
