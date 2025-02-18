import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class GamePanel extends JPanel implements KeyListener {
    private GameEngine engine;
    private PlayerShip player;
    
    public GamePanel(GameEngine engine) {
       this.engine = engine;
       this.player = engine.getPlayer();
       setPreferredSize(new Dimension(GameEngine.WIDTH, GameEngine.HEIGHT));
       setBackground(Color.black);
       
       setFocusable(true);
       addKeyListener(this);
       
       // Use a Swing Timer (executing on the EDT) to repaint at ~60 FPS.
       Timer timer = new Timer(16, e -> repaint());
       timer.start();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
       super.paintComponent(g);
       Graphics2D g2d = (Graphics2D) g;
       
       // Enable antialiasing for smoother visuals.
       g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
       
       // Draw all game objects.
       for (GameObject obj : engine.getGameObjects()) {
            obj.draw(g2d);
       }
       
       // Display UI info such as remaining lives and score
       g2d.setColor(Color.white);
       g2d.setFont(new Font("Arial", Font.BOLD, 16));
       g2d.drawString("Lives: " + player.getLives(), 10, 25);
       g2d.drawString("Score: " + engine.getScore(), 10, 50);
       
       // If game is over, draw the game over screen
       if (engine.isGameOver()) {
           drawGameOver(g2d);
       }
    }
    
    private void drawGameOver(Graphics2D g) {
        // Semi-transparent black overlay
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // Game Over text
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        String gameOver = "GAME OVER";
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(gameOver)) / 2;
        int y = getHeight() / 2 - 50;
        g.drawString(gameOver, x, y);
        
        // Final Score
        g.setFont(new Font("Arial", Font.BOLD, 24));
        String finalScore = "Final Score: " + engine.getScore();
        fm = g.getFontMetrics();
        x = (getWidth() - fm.stringWidth(finalScore)) / 2;
        y = getHeight() / 2 + 20;
        g.drawString(finalScore, x, y);
        
        // Press any key to restart
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        String restart = "Press N for New Game";
        fm = g.getFontMetrics();
        x = (getWidth() - fm.stringWidth(restart)) / 2;
        y = getHeight() / 2 + 60;
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
                engine.addGameObject(player.fireBullet());
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
} 