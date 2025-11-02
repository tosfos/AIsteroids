/**
 * Exception thrown when game initialization fails.
 *
 * <p>This exception is thrown during game startup when critical
 * components fail to initialize properly.</p>
 *
 * @author AIsteroids Development Team
 * @version 1.0
 */
public class GameInitializationException extends GameException {
    /**
     * Creates a new game initialization exception with the specified message.
     *
     * @param message The error message
     */
    public GameInitializationException(String message) {
        super(message);
    }

    /**
     * Creates a new game initialization exception with the specified message and cause.
     *
     * @param message The error message
     * @param cause The cause of this exception
     */
    public GameInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}

