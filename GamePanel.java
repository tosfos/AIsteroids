import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel implements KeyListener {
    private GameEngine engine;
    private PlayerShip player;
    private List<Star> stars;
    private List<Particle> particles;
    private ParticleSystem particleSystem;
    private Random rand = new Random();

    // UI enhancement variables
    private Font hudFont;
    private Font titleFont;
    private Font subtitleFont;

    public GamePanel(GameEngine engine) {
       this.engine = engine;
       this.player = engine.getPlayer();
       setPreferredSize(new Dimension(GameEngine.WIDTH, GameEngine.HEIGHT));
       setBackground(Color.black);

       // Initialize starfield
       initializeStarfield();

       // Initialize particle system
       particleSystem = new ParticleSystem();

       // Set reference in engine for particle effects
       engine.setGamePanel(this);

       // Initialize particle system
       particles = new ArrayList<>();

       // Initialize fonts
       try {
           hudFont = new Font("Orbitron", Font.BOLD, 16);
           titleFont = new Font("Orbitron", Font.BOLD, 48);
           subtitleFont = new Font("Orbitron", Font.PLAIN, 24);
       } catch (Exception e) {
           // Fallback fonts
           hudFont = new Font("Arial", Font.BOLD, 16);
           titleFont = new Font("Arial", Font.BOLD, 48);
           subtitleFont = new Font("Arial", Font.PLAIN, 24);
       }

       setFocusable(true);
       addKeyListener(this);

       // Use a Swing Timer (executing on the EDT) to repaint at ~60 FPS.
       Timer timer = new Timer(16, e -> repaint());
       timer.start();
    }

    private void initializeStarfield() {
        stars = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            stars.add(new Star(
                rand.nextDouble() * GameEngine.WIDTH,
                rand.nextDouble() * GameEngine.HEIGHT,
                rand.nextDouble() * 3 + 1, // Size between 1-4
                rand.nextFloat() * 0.7f + 0.3f // Brightness between 0.3-1.0
            ));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
       super.paintComponent(g);
       Graphics2D g2d = (Graphics2D) g;

       // Enable high-quality rendering
       g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
       g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
       g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
       g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

       // Draw animated starfield background
       drawStarfield(g2d);

       // Draw particle effects
       updateAndDrawParticles(g2d);

       // Update and draw enhanced particle system
       particleSystem.update(1.0/60.0);
       particleSystem.draw(g2d);

       // Draw all game objects with enhanced effects
       for (GameObject obj : engine.getGameObjects()) {
            obj.draw(g2d);
       }

       // Draw enhanced UI
       drawEnhancedHUD(g2d);

       // If game is over, draw the enhanced game over screen
       if (engine.isGameOver()) {
           drawEnhancedGameOver(g2d);
       }
    }

    private void drawStarfield(Graphics2D g) {
        for (Star star : stars) {
            star.update();
            star.draw(g);
        }
    }

    private void updateAndDrawParticles(Graphics2D g) {
        particles.removeIf(particle -> !particle.isAlive());
        for (Particle particle : particles) {
            particle.update();
            particle.draw(g);
        }
    }

    public void addExplosion(double x, double y, int numParticles) {
        for (int i = 0; i < numParticles; i++) {
            double angle = rand.nextDouble() * 2 * Math.PI;
            double speed = rand.nextDouble() * 200 + 50;
            Color color = new Color[] {
                Color.ORANGE, Color.RED, Color.YELLOW, Color.WHITE
            }[rand.nextInt(4)];

            particles.add(new Particle(x, y,
                Math.cos(angle) * speed,
                Math.sin(angle) * speed,
                color, 1.0 + rand.nextDouble() * 2.0));
        }
    }

    public ParticleSystem getParticleSystem() {
        return particleSystem;
    }

    private void drawEnhancedHUD(Graphics2D g) {
        // Create a subtle glow effect for the HUD
        g.setFont(hudFont);

        // Lives display with ship icons
        g.setColor(new Color(0, 255, 255, 200)); // Cyan with transparency
        g.drawString("LIVES:", 10, 25);

        for (int i = 0; i < player.getLives(); i++) {
            drawMiniShip(g, 80 + i * 25, 20);
        }

        // Score with glow effect
        String scoreText = "SCORE: " + String.format("%06d", engine.getScore());
        drawGlowText(g, scoreText, 10, 50, new Color(255, 255, 0), new Color(255, 255, 0, 100));

        // Add a subtle border effect
        g.setColor(new Color(0, 255, 255, 50));
        g.setStroke(new BasicStroke(2));
        g.drawRect(5, 5, 250, 50);
    }

    private void drawMiniShip(Graphics2D g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.scale(0.5, 0.5);

        g2.setColor(new Color(0, 255, 255));
        int[] xPoints = {0, -6, 0, 6};
        int[] yPoints = {-8, 6, 2, 6};
        g2.fillPolygon(xPoints, yPoints, 4);

        g2.dispose();
    }

    private void drawGlowText(Graphics2D g, String text, int x, int y, Color textColor, Color glowColor) {
        // Draw glow effect
        for (int i = 0; i < 5; i++) {
            g.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(),
                glowColor.getAlpha() / (i + 1)));
            g.drawString(text, x - i, y - i);
            g.drawString(text, x + i, y + i);
            g.drawString(text, x - i, y + i);
            g.drawString(text, x + i, y - i);
        }

        // Draw main text
        g.setColor(textColor);
        g.drawString(text, x, y);
    }

    private void drawEnhancedGameOver(Graphics2D g) {
        // Animated background overlay
        float alpha = 0.8f + 0.2f * (float) Math.sin(System.currentTimeMillis() * 0.003);
        int bgAlpha = Math.max(0, Math.min(255, (int)(255 * alpha)));
        g.setColor(new Color(0, 0, 0, bgAlpha));
        g.fillRect(0, 0, getWidth(), getHeight());

        // Animated border effect
        int borderAlpha = Math.max(50, Math.min(255, (int)(100 + 155 * Math.sin(System.currentTimeMillis() * 0.01))));
        g.setColor(new Color(255, 0, 0, borderAlpha));
        g.setStroke(new BasicStroke(5));
        g.drawRect(50, 50, getWidth() - 100, getHeight() - 100);

        // Game Over text with animated glow
        g.setFont(titleFont);
        String gameOver = "GAME OVER";
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(gameOver)) / 2;
        int y = getHeight() / 2 - 80;

        // Pulsing glow effect
        int glowAlpha = Math.max(50, Math.min(255, (int)(50 + 100 * Math.sin(System.currentTimeMillis() * 0.008))));
        Color glowColor = new Color(255, 0, 0, glowAlpha);
        drawGlowText(g, gameOver, x, y, Color.RED, glowColor);

        // Final Score with enhanced styling
        g.setFont(subtitleFont);
        String finalScore = "FINAL SCORE: " + String.format("%06d", engine.getScore());
        fm = g.getFontMetrics();
        x = (getWidth() - fm.stringWidth(finalScore)) / 2;
        y = getHeight() / 2 - 10;
        drawGlowText(g, finalScore, x, y, Color.YELLOW, new Color(255, 255, 0, 100));

        // Restart instruction with pulsing effect
        g.setFont(hudFont);
        String restart = "PRESS [N] FOR NEW GAME";
        fm = g.getFontMetrics();
        x = (getWidth() - fm.stringWidth(restart)) / 2;
        y = getHeight() / 2 + 60;

        float pulseAlpha = 0.5f + 0.5f * (float) Math.sin(System.currentTimeMillis() * 0.01);
        int restartAlpha = Math.max(0, Math.min(255, (int)(255 * pulseAlpha)));
        g.setColor(new Color(0, 255, 255, restartAlpha));
        g.drawString(restart, x, y);
    }

    // KeyListener events for controlling the player ship.
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // If game is over, only respond to N for new game
        if (engine.isGameOver()) {
            if (key == KeyEvent.VK_N) {
                engine.restart();
            }
            return;
        }

        // Normal gameplay controls
        switch (key) {
            case KeyEvent.VK_LEFT:
                player.setTurnLeft(true);
                break;
            case KeyEvent.VK_RIGHT:
                player.setTurnRight(true);
                break;
            case KeyEvent.VK_UP:
                player.setAccelerating(true);
                break;
            case KeyEvent.VK_SPACE:
                // Fire a bullet from the player's ship.
                List<Bullet> bullets = player.fireBullet();
                for (Bullet bullet : bullets) {
                    engine.addGameObject(bullet);
                }
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // If game is over, ignore all input except N
        if (engine.isGameOver()) {
            return;
        }

        int key = e.getKeyCode();
        switch (key) {
            case KeyEvent.VK_LEFT:
                player.setTurnLeft(false);
                break;
            case KeyEvent.VK_RIGHT:
                player.setTurnRight(false);
                break;
            case KeyEvent.VK_UP:
                player.setAccelerating(false);
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used.
    }

    // Inner class for starfield
    private static class Star {
        private double x, y;
        private double size;
        private float brightness;
        private double twinkleSpeed;

        public Star(double x, double y, double size, float brightness) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.brightness = brightness;
            this.twinkleSpeed = Math.random() * 0.02 + 0.005;
        }

        public void update() {
            brightness = (float)(0.3 + 0.7 * Math.abs(Math.sin(System.currentTimeMillis() * twinkleSpeed)));
        }

        public void draw(Graphics2D g) {
            Color starColor = new Color(1f, 1f, 1f, brightness);
            g.setColor(starColor);
            g.fillOval((int)(x - size/2), (int)(y - size/2), (int)size, (int)size);
        }
    }

    // Inner class for particle effects
    private static class Particle {
        private double x, y, vx, vy;
        private Color color;
        private double life, maxLife;
        private double size;

        public Particle(double x, double y, double vx, double vy, Color color, double life) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.life = life;
            this.maxLife = life;
            this.size = 3;
        }

        public void update() {
            x += vx * 0.016; // Assuming ~60 FPS
            y += vy * 0.016;
            vx *= 0.98; // Friction
            vy *= 0.98;
            life -= 0.016;
            size *= 0.99;
        }

        public boolean isAlive() {
            return life > 0;
        }

        public void draw(Graphics2D g) {
            float alpha = (float)(life / maxLife);
            Color fadeColor = new Color(color.getRed(), color.getGreen(), color.getBlue(),
                (int)(color.getAlpha() * alpha));
            g.setColor(fadeColor);
            g.fillOval((int)(x - size/2), (int)(y - size/2), (int)size, (int)size);
        }
    }
}
