import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Collections;

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

    // Power-up message system
    private String powerUpMessage = "";
    private double powerUpMessageTimer = 0;
    private static final double POWER_UP_MESSAGE_DURATION = GameConfig.UI.POWER_UP_MESSAGE_DURATION;
    private Font powerUpMessageFont;

    // Help system
    private boolean showingHelp = false;

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
           hudFont = new Font(GameConfig.UI.FONT_NAME, Font.BOLD, GameConfig.UI.HUD_FONT_SIZE);
           titleFont = new Font(GameConfig.UI.FONT_NAME, Font.BOLD, GameConfig.UI.TITLE_FONT_SIZE);
           subtitleFont = new Font(GameConfig.UI.FONT_NAME, Font.PLAIN, GameConfig.UI.SUBTITLE_FONT_SIZE);
           powerUpMessageFont = new Font(GameConfig.UI.FONT_NAME, Font.BOLD, GameConfig.UI.POWER_UP_MESSAGE_FONT_SIZE);
       } catch (Exception e) {
           // Fallback fonts
           hudFont = new Font(GameConfig.UI.FALLBACK_FONT_NAME, Font.BOLD, GameConfig.UI.HUD_FONT_SIZE);
           titleFont = new Font(GameConfig.UI.FALLBACK_FONT_NAME, Font.BOLD, GameConfig.UI.TITLE_FONT_SIZE);
           subtitleFont = new Font(GameConfig.UI.FALLBACK_FONT_NAME, Font.PLAIN, GameConfig.UI.SUBTITLE_FONT_SIZE);
           powerUpMessageFont = new Font(GameConfig.UI.FALLBACK_FONT_NAME, Font.BOLD, GameConfig.UI.POWER_UP_MESSAGE_FONT_SIZE);
       }

       setFocusable(true);
       addKeyListener(this);

       // Use a Swing Timer (executing on the EDT) to repaint at ~60 FPS.
       Timer timer = new Timer(GameConfig.FRAME_TIME_MS, e -> repaint());
       timer.start();
    }

    private void initializeStarfield() {
        stars = new ArrayList<>();
        for (int i = 0; i < GameConfig.Effects.STARFIELD_STAR_COUNT; i++) {
            double size = GameConfig.Effects.STAR_SIZE_MIN +
                          rand.nextDouble() * (GameConfig.Effects.STAR_SIZE_MAX - GameConfig.Effects.STAR_SIZE_MIN);
            float brightness = GameConfig.Effects.STAR_BRIGHTNESS_MIN +
                               rand.nextFloat() * (GameConfig.Effects.STAR_BRIGHTNESS_MAX - GameConfig.Effects.STAR_BRIGHTNESS_MIN);
            stars.add(new Star(
                rand.nextDouble() * GameEngine.WIDTH,
                rand.nextDouble() * GameEngine.HEIGHT,
                size,
                brightness
            ));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
       super.paintComponent(g);
       Graphics2D g2d = (Graphics2D) g;

       setupRenderingQuality(g2d);
       drawGameWorld(g2d);
       drawUI(g2d);
    }

    private void setupRenderingQuality(Graphics2D g2d) {
        // Enable high-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }

    private void drawGameWorld(Graphics2D g2d) {
        // Draw animated starfield background
        drawStarfield(g2d);

        // Draw particle effects
        updateAndDrawParticles(g2d);

        // Update and draw enhanced particle system
        particleSystem.update(1.0/60.0);
        particleSystem.draw(g2d);

        // Update power-up message timer
        if (powerUpMessageTimer > 0) {
            powerUpMessageTimer -= 1.0/60.0;
            if (powerUpMessageTimer <= 0) {
                powerUpMessage = "";
            }
        }

        // Draw all game objects with enhanced effects
        drawGameObjects(g2d);
    }

    private void drawGameObjects(Graphics2D g2d) {
        for (GameObject obj : engine.getGameObjects()) {
            obj.draw(g2d);
        }
    }

    private void drawUI(Graphics2D g2d) {
        // Draw enhanced UI
        drawEnhancedHUD(g2d);

        // Draw power-up message if active
        drawPowerUpMessage(g2d);

        // Draw help overlay if active
        if (showingHelp) {
            drawHelpOverlay(g2d);
        }

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
        // More efficient particle cleanup to reduce GC pressure
        particles.removeIf(particle -> {
            if (!particle.isAlive()) {
                return true; // Remove dead particle
            }
            particle.update();
            particle.draw(g);
            return false; // Keep alive particle
        });
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

    private void showLeaderboard() {
        // Create and show leaderboard dialog
        javax.swing.SwingUtilities.invokeLater(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append("=== LEADERBOARD ===\n\n");

            List<LeaderboardSystem.LeaderboardEntry> entries = LeaderboardSystem.getLeaderboard();
            if (entries.isEmpty()) {
                sb.append("No scores yet!\n");
            } else {
                for (int i = 0; i < entries.size(); i++) {
                    LeaderboardSystem.LeaderboardEntry entry = entries.get(i);
                    sb.append(String.format("%d. %s - %,d pts (Wave %d) - %s\n",
                        i + 1, entry.playerName, entry.score, entry.wave, entry.date));
                }
            }

            sb.append("\n=== ACHIEVEMENTS ===\n\n");
            Set<LeaderboardSystem.Achievement> unlocked = LeaderboardSystem.getUnlockedAchievements();
            sb.append("Unlocked: " + unlocked.size() + "/" + LeaderboardSystem.Achievement.values().length + "\n\n");

            for (LeaderboardSystem.Achievement achievement : LeaderboardSystem.Achievement.values()) {
                if (LeaderboardSystem.isAchievementUnlocked(achievement)) {
                    sb.append("★ " + achievement.getName() + " - " + achievement.getDescription() + "\n");
                } else {
                    sb.append("☐ " + achievement.getName() + " - " + achievement.getDescription() + "\n");
                }
            }

            javax.swing.JOptionPane.showMessageDialog(this, sb.toString(), "Leaderboard & Achievements",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void drawEnhancedHUD(Graphics2D g) {
        // Create a subtle glow effect for the HUD
        g.setFont(hudFont);

        // Lives display with ship icons
        g.setColor(new Color(0, 255, 255, 200)); // Cyan with transparency
        g.drawString("LIVES:", 10, 25);

        for (int i = 0; i < player.getLives(); i++) {
            drawMiniShip(g, GameConfig.UI.LIVES_ICON_X + i * GameConfig.UI.LIVES_DISPLAY_SPACING, GameConfig.UI.LIVES_ICON_Y);
        }

        // Score with glow effect
        String scoreText = "SCORE: " + String.format("%06d", engine.getScore());
        drawGlowText(g, scoreText, GameConfig.UI.HUD_MARGIN, GameConfig.UI.SCORE_TEXT_Y, GameConfig.UI.SCORE_TEXT_COLOR, GameConfig.UI.SCORE_GLOW_COLOR);

        // Wave information
        WaveSystem waveSystem = engine.getWaveSystem();
        String waveText = "WAVE: " + waveSystem.getCurrentWave();
        if (waveSystem.isBossWave()) {
            waveText += " [BOSS]";
            g.setColor(GameConfig.UI.BOSS_TEXT_COLOR);
        } else {
            g.setColor(GameConfig.UI.NORMAL_WAVE_COLOR);
        }
        g.drawString(waveText, GameConfig.UI.HUD_MARGIN, GameConfig.UI.WAVE_TEXT_Y);

        // Asteroids remaining
        String asteroidsText = "ASTEROIDS: " + waveSystem.getAsteroidsRemaining();
        g.setColor(GameConfig.UI.HUD_TEXT_COLOR);
        g.drawString(asteroidsText, GameConfig.UI.HUD_MARGIN, GameConfig.UI.ASTEROIDS_TEXT_Y);

        // Score multiplier indicator
        if (waveSystem.getScoreMultiplier() > 1) {
            String multiplierText = "x" + waveSystem.getScoreMultiplier();
            g.setColor(new Color(255, 255, 0, 255));
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString(multiplierText, engine.getScore() > 0 ? 200 : 150, 50);
            g.setFont(hudFont); // Reset font
        }

        // Power-up status indicators
        drawPowerUpStatusIcons(g);

        // Add a subtle border effect
        g.setColor(GameConfig.UI.HUD_BORDER_COLOR);
        g.setStroke(new BasicStroke(2));
        g.drawRect(GameConfig.UI.HUD_MARGIN - 5, GameConfig.UI.HUD_MARGIN - 5, GameConfig.UI.HUD_WIDTH, GameConfig.UI.HUD_HEIGHT);
    }

    private void drawPowerUpMessage(Graphics2D g) {
        if (powerUpMessage.isEmpty() || powerUpMessageTimer <= 0) {
            return;
        }

        g.setFont(powerUpMessageFont);
        FontMetrics fm = g.getFontMetrics();

        // Position message in center-top area
        int x = (getWidth() - fm.stringWidth(powerUpMessage)) / 2;
        int y = GameConfig.UI.POWER_UP_MESSAGE_Y;

        // Calculate fade effect for last second
        float alpha = 1.0f;
        if (powerUpMessageTimer < 1.0) {
            alpha = (float) powerUpMessageTimer;
        }

        // Draw glow effect
        Color glowColor = new Color(255, 255, 0, (int)(100 * alpha));
        drawGlowText(g, powerUpMessage, x, y, new Color(255, 255, 0, (int)(255 * alpha)), glowColor);
    }

    public void showPowerUpMessage(PowerUp.PowerUpType powerUpType) {
        switch (powerUpType) {
            case RAPID_FIRE:
                powerUpMessage = "RAPID FIRE ACTIVATED!";
                break;
            case SPREAD_SHOT:
                powerUpMessage = "SPREAD SHOT ACTIVATED!";
                break;
            case SHIELD:
                powerUpMessage = "SHIELD ACTIVATED!";
                break;
            case SPEED_BOOST:
                powerUpMessage = "SPEED BOOST ACTIVATED!";
                break;
            case MULTI_SHOT:
                powerUpMessage = "MULTI SHOT ACTIVATED!";
                break;
            case LASER_BEAM:
                powerUpMessage = "LASER BEAM ACTIVATED!";
                break;
            default:
                powerUpMessage = "POWER-UP ACTIVATED!";
                break;
        }
        powerUpMessageTimer = POWER_UP_MESSAGE_DURATION;
    }

    private void drawPowerUpStatusIcons(Graphics2D g) {
        Map<PowerUp.PowerUpType, Double> activePowerUps = player.getActivePowerUps();
        if (activePowerUps.isEmpty()) {
            return;
        }

        // Power-up status label
        g.setFont(hudFont);
        g.setColor(new Color(255, 255, 255, 200));
        g.drawString("POWER-UPS:", 10, 125);

        int iconX = 10;
        int iconY = 135;
        int iconSize = 20;
        int iconSpacing = 25;

        for (Map.Entry<PowerUp.PowerUpType, Double> entry : activePowerUps.entrySet()) {
            PowerUp.PowerUpType type = entry.getKey();
            double timeRemaining = entry.getValue();
            double totalDuration = type.getDuration();

            // Calculate fade based on remaining time
            float progress = (float) (timeRemaining / totalDuration);
            float alpha = Math.max(GameConfig.UI.POWER_UP_MIN_ALPHA, progress); // Minimum alpha so it's still visible

            // Draw icon background circle
            Color iconColor = type.getColor();
            Color fadedColor = new Color(iconColor.getRed(), iconColor.getGreen(),
                                       iconColor.getBlue(), (int)(255 * alpha));
            g.setColor(fadedColor);
            g.fillOval(iconX, iconY, iconSize, iconSize);

            // Draw glow effect
            Color glowColor = new Color(iconColor.getRed(), iconColor.getGreen(),
                                      iconColor.getBlue(), (int)(100 * alpha));
            g.setColor(glowColor);
            g.fillOval(iconX - 2, iconY - 2, iconSize + 4, iconSize + 4);
            g.setColor(fadedColor);
            g.fillOval(iconX, iconY, iconSize, iconSize);

            // Draw icon symbol
            g.setColor(new Color(0, 0, 0, (int)(255 * alpha)));
            g.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g.getFontMetrics();
            String symbol = getPowerUpSymbol(type);
            int symbolWidth = fm.stringWidth(symbol);
            int symbolHeight = fm.getAscent();
            g.drawString(symbol,
                iconX + (iconSize - symbolWidth) / 2,
                iconY + (iconSize + symbolHeight) / 2 - 2);

            // Draw progress ring
            drawProgressRing(g, iconX + iconSize/2, iconY + iconSize/2, iconSize/2 + 3, progress, fadedColor);

            iconX += iconSpacing;
        }
    }

    private void drawProgressRing(Graphics2D g, int centerX, int centerY, int radius, float progress, Color color) {
        g.setStroke(new BasicStroke(2));
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));

        // Full circle background
        g.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        // Progress arc
        g.setColor(color);
        int startAngle = 90; // Start at top
        int arcAngle = (int) (360 * progress);
        g.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, startAngle, arcAngle);
    }

    private String getPowerUpSymbol(PowerUp.PowerUpType type) {
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

    private void drawHelpOverlay(Graphics2D g) {
        // Semi-transparent background
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());

        // Help title
        g.setFont(titleFont);
        g.setColor(Color.WHITE);
        String title = "POWER-UP GUIDE";
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(title)) / 2;
        g.drawString(title, x, 80);

        // Power-up information
        g.setFont(hudFont);
        String[] helpLines = {
            "🟠 RAPID FIRE (R) - Triples firing rate for 10s",
            "🔵 SPREAD SHOT (S) - 3 bullets in spread for 8s",
            "🟢 SHIELD (♦) - Invulnerability for 12s",
            "🟡 SPEED BOOST (») - Enhanced speed for 6s",
            "🟣 MULTI SHOT (M) - 5 bullets in spread for 15s",
            "🔴 LASER BEAM (L) - Future weapon for 20s",
            "",
            "💡 TIPS:",
            "• Power-ups stack together for combos",
            "• HUD shows active power-ups with timers",
            "• Collect power-ups before they fade away",
            "• Use Shield + Rapid Fire for safe aggression",
            "",
            "CONTROLS:",
            "←/→ Rotate   ↑ Thrust   SPACE Fire   H Help"
        };

        int startY = 130;
        for (int i = 0; i < helpLines.length; i++) {
            if (helpLines[i].startsWith("🟠") || helpLines[i].startsWith("🔵") ||
                helpLines[i].startsWith("🟢") || helpLines[i].startsWith("🟡") ||
                helpLines[i].startsWith("🟣") || helpLines[i].startsWith("🔴")) {
                // Color-code power-up lines
                g.setColor(Color.YELLOW);
            } else if (helpLines[i].startsWith("💡") || helpLines[i].startsWith("CONTROLS:")) {
                g.setColor(Color.CYAN);
            } else if (helpLines[i].startsWith("•")) {
                g.setColor(new Color(200, 200, 200));
            } else {
                g.setColor(Color.WHITE);
            }

            fm = g.getFontMetrics();
            int lineX = (getWidth() - fm.stringWidth(helpLines[i])) / 2;
            g.drawString(helpLines[i], lineX, startY + i * 20);
        }

        // Close instruction
        g.setFont(powerUpMessageFont);
        g.setColor(Color.YELLOW);
        String closeMsg = "Press H to close help";
        fm = g.getFontMetrics();
        x = (getWidth() - fm.stringWidth(closeMsg)) / 2;
        g.drawString(closeMsg, x, getHeight() - 50);
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

        // Wave reached
        String waveText = "WAVE REACHED: " + engine.getWaveSystem().getCurrentWave();
        fm = g.getFontMetrics();
        x = (getWidth() - fm.stringWidth(waveText)) / 2;
        y = getHeight() / 2 + 20;
        g.setColor(new Color(100, 255, 100, 200));
        g.drawString(waveText, x, y);

        // Recent achievements
        drawRecentAchievements(g);

        // Restart instruction with pulsing effect
        g.setFont(hudFont);
        String restart = "PRESS [N] FOR NEW GAME  [L] FOR LEADERBOARD";
        fm = g.getFontMetrics();
        x = (getWidth() - fm.stringWidth(restart)) / 2;
        y = getHeight() / 2 + 100;

        float pulseAlpha = 0.5f + 0.5f * (float) Math.sin(System.currentTimeMillis() * 0.01);
        int restartAlpha = Math.max(0, Math.min(255, (int)(255 * pulseAlpha)));
        g.setColor(new Color(0, 255, 255, restartAlpha));
        g.drawString(restart, x, y);
    }

    private void drawRecentAchievements(Graphics2D g) {
        // Show last 3 unlocked achievements
        Set<LeaderboardSystem.Achievement> unlocked = LeaderboardSystem.getUnlockedAchievements();
        if (unlocked.isEmpty()) return;

        List<LeaderboardSystem.Achievement> recentAchievements = new ArrayList<>(unlocked);
        Collections.reverse(recentAchievements); // Show most recent first

        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.setColor(new Color(255, 215, 0, 180)); // Gold color

        int startY = getHeight() / 2 + 50;
        int count = 0;
        for (LeaderboardSystem.Achievement achievement : recentAchievements) {
            if (count >= 3) break; // Show only last 3
            String text = "★ " + achievement.getName();
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            g.drawString(text, x, startY + count * 15);
            count++;
        }
    }

    // KeyListener events for controlling the player ship.
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // If game is over, only respond to N for new game and L for leaderboard
        if (engine.isGameOver()) {
            if (key == KeyEvent.VK_N) {
                engine.restart();
            } else if (key == KeyEvent.VK_L) {
                showLeaderboard();
            }
            return;
        }

        // Normal gameplay controls
        switch (key) {
            case KeyEvent.VK_LEFT:
                if (!showingHelp) player.setTurnLeft(true);
                break;
            case KeyEvent.VK_RIGHT:
                if (!showingHelp) player.setTurnRight(true);
                break;
            case KeyEvent.VK_UP:
                if (!showingHelp) player.setAccelerating(true);
                break;
            case KeyEvent.VK_SPACE:
                if (!showingHelp) {
                    // Fire a bullet from the player's ship.
                    List<Bullet> bullets = player.fireBullet();
                    for (Bullet bullet : bullets) {
                        engine.addGameObject(bullet);
                    }
                }
                break;
            case KeyEvent.VK_H:
                // Toggle help overlay
                showingHelp = !showingHelp;
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
            x += vx * GameConfig.FRAME_TIME_MS / 1000.0; // Convert to seconds
            y += vy * GameConfig.FRAME_TIME_MS / 1000.0;
            vx *= 0.98; // Friction
            vy *= 0.98;
            life -= GameConfig.FRAME_TIME_MS / 1000.0;
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
