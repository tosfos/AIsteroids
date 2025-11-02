/**
 * Exception thrown when collision-related errors occur.
 *
 * <p>This exception is thrown when invalid collision states or
 * collision processing errors are encountered.</p>
 *
 * @author AIsteroids Development Team
 * @version 1.0
 */
public class CollisionException extends GameException {
    /**
     * Creates a new collision exception with the specified message.
     *
     * @param message The error message
     */
    public CollisionException(String message) {
        super(message);
    }

    /**
     * Creates a new collision exception with the specified message and cause.
     *
     * @param message The error message
     * @param cause The cause of this exception
     */
    public CollisionException(String message, Throwable cause) {
        super(message, cause);
    }
}

