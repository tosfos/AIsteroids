/**
 * Base exception class for all game-related exceptions.
 *
 * <p>This is the root of the game exception hierarchy and provides
 * a common base for all game-specific exceptions.</p>
 *
 * @author AIsteroids Development Team
 * @version 1.0
 */
public class GameException extends RuntimeException {
    /**
     * Creates a new game exception with the specified message.
     *
     * @param message The error message
     */
    public GameException(String message) {
        super(message);
    }

    /**
     * Creates a new game exception with the specified message and cause.
     *
     * @param message The error message
     * @param cause The cause of this exception
     */
    public GameException(String message, Throwable cause) {
        super(message, cause);
    }
}

