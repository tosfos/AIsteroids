import javax.swing.*;

public class Main {
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