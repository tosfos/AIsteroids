import javax.swing.*;

/**
 * Main entry point for the AIsteroids game application.
 * Initializes the game window and starts the game engine.
 *
 * <p>The game runs on the Swing Event Dispatch Thread (EDT) to ensure
 * thread-safe UI operations. The game engine runs on a separate thread
 * to maintain consistent frame timing.</p>
 *
 * @author AIsteroids Development Team
 * @version 1.0
 */
public class Main {
    /**
     * Main entry point for the application.
     * Creates the game window, initializes the game engine and panel,
     * and starts the game loop.
     *
     * @param args Command-line arguments (not currently used)
     */
    public static void main(String[] args) {
        // Schedule UI creation on the Swing EDT.
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("AIsteroids");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            GameEngine engine = new GameEngine();
            GamePanel panel = new GamePanel(engine);
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // Start the game engine (which spawns its own update thread)
            engine.start();
        });
    }
}
