package pool;

/**
 * Interface for pooled objects that can be reused.
 * Implementing classes must provide reset logic to restore object to initial state.
 */
public interface Reusable {
    /**
     * Reset the object's state to prepare for reuse.
     * This method should restore all fields to their initial values.
     */
    void reset();
}
