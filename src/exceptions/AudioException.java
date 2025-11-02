/**
 * Exception thrown when audio-related errors occur.
 *
 * <p>This exception is thrown when sound or music system operations fail,
 * such as when MIDI or audio lines are unavailable.</p>
 *
 * @author AIsteroids Development Team
 * @version 1.0
 */
public class AudioException extends GameException {
    /**
     * Creates a new audio exception with the specified message.
     *
     * @param message The error message
     */
    public AudioException(String message) {
        super(message);
    }

    /**
     * Creates a new audio exception with the specified message and cause.
     *
     * @param message The error message
     * @param cause The cause of this exception
     */
    public AudioException(String message, Throwable cause) {
        super(message, cause);
    }
}

