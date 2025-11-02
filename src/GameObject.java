import java.awt.*;

/**
 * Abstract base class for all game entities in the AIsteroids game.
 * Provides common functionality for position, velocity, rotation, and collision detection.
 *
 * <p>All game objects (player ship, asteroids, bullets, power-ups, etc.) extend this class.
 * This class handles common behaviors like position updates, screen wrapping, and collision
 * detection through a pluggable collision detector interface.</p>
 *
 * <p>Key features:
 * <ul>
 *   <li>Position and velocity management</li>
 *   <li>Automatic screen wrapping (objects wrap around screen edges)</li>
 *   <li>Collision detection via pluggable collision detector</li>
 *   <li>Life cycle management (active/alive state)</li>
 *   <li>Rotation support for visual orientation</li>
 * </ul>
 * </p>
 *
 * <p>Thread safety: This class is not thread-safe. All game object operations
 * should be performed on the game update thread.</p>
 *
 * @author AIsteroids Development Team
 * @version 1.0
 */
public abstract class GameObject implements GameEntity {
    protected double x, y, vx, vy;
    protected double rotation;
    protected double rotationSpeed;
    protected boolean active = true;
    protected CollisionDetector collisionDetector;

    /**
     * Creates a new game object at the specified position.
     * Uses the default collision detector implementation.
     *
     * @param x Initial X coordinate (must be finite)
     * @param y Initial Y coordinate (must be finite)
     * @throws IllegalArgumentException if x or y is NaN or infinite
     */
    public GameObject(double x, double y) {
        // Allow any finite coordinates (objects can be off-screen)
        if (Double.isNaN(x) || Double.isInfinite(x)) {
            throw new IllegalArgumentException("x coordinate must be finite, got: " + x);
        }
        if (Double.isNaN(y) || Double.isInfinite(y)) {
            throw new IllegalArgumentException("y coordinate must be finite, got: " + y);
        }
        this.x = x;
        this.y = y;
        this.collisionDetector = new DefaultCollisionDetector();
    }

    /**
     * Creates a new game object with a custom collision detector.
     * This constructor is primarily for testing and dependency injection.
     *
     * @param x Initial X coordinate (must be finite)
     * @param y Initial Y coordinate (must be finite)
     * @param collisionDetector Custom collision detector implementation
     * @throws IllegalArgumentException if x or y is NaN/infinite, or collisionDetector is null
     */
    public GameObject(double x, double y, CollisionDetector collisionDetector) {
        // Allow any finite coordinates (objects can be off-screen)
        if (Double.isNaN(x) || Double.isInfinite(x)) {
            throw new IllegalArgumentException("x coordinate must be finite, got: " + x);
        }
        if (Double.isNaN(y) || Double.isInfinite(y)) {
            throw new IllegalArgumentException("y coordinate must be finite, got: " + y);
        }
        this.x = x;
        this.y = y;
        this.collisionDetector = InputValidator.validateNotNull(collisionDetector, "collisionDetector");
    }

    /**
     * Updates the game object's state based on elapsed time.
     * Subclasses must implement this to update position, animation, timers, etc.
     *
     * @param deltaTime Time elapsed since last update in seconds
     */
    public abstract void update(double deltaTime);

    /**
     * Renders the game object to the screen.
     * Subclasses must implement this to draw their visual representation.
     *
     * @param g Graphics context for drawing
     */
    public abstract void draw(Graphics2D g);

    /**
     * Returns the bounding rectangle for collision detection.
     * Used for broad-phase collision detection before precise checks.
     *
     * @return Bounding rectangle in screen coordinates
     */
    public abstract Rectangle getBounds();

    /**
     * Checks if this game object is currently alive/active.
     * Dead objects are typically removed from the game.
     *
     * @return true if the object is alive, false otherwise
     */
    public boolean isAlive() {
       return active;
    }

    /**
     * Sets whether this game object is alive/active.
     * Setting to false will mark the object for removal.
     *
     * @param active New alive state
     */
    public void setAlive(boolean active) {
       this.active = active;
    }

    /**
     * Gets the X coordinate of this game object.
     * @return Current X coordinate
     */
    public double getX() { return x; }

    /**
     * Gets the Y coordinate of this game object.
     * @return Current Y coordinate
     */
    public double getY() { return y; }

    /**
     * Sets the X coordinate of this game object.
     * @param x New X coordinate
     */
    public void setX(double x) { this.x = x; }

    /**
     * Sets the Y coordinate of this game object.
     * @param y New Y coordinate
     */
    public void setY(double y) { this.y = y; }

    /**
     * Gets the collision radius of this game object.
     * Used for circular collision detection.
     *
     * @return Collision radius in pixels
     */
    public abstract double getRadius();

    /**
     * Updates the object's position and rotation based on velocity and delta time.
     * Also handles screen wrapping. This method is separated for testability.
     *
     * @param deltaTime Time elapsed since last update in seconds
     * @throws IllegalArgumentException if deltaTime is invalid
     */
    public void updatePosition(double deltaTime) {
        InputValidator.validateDeltaTime(deltaTime);
        x += vx * deltaTime;
        y += vy * deltaTime;
        rotation += rotationSpeed * deltaTime;
        wrapAroundScreen();
    }

    /**
     * Wraps the object's position around screen edges.
     * Objects that move off one edge appear on the opposite edge.
     */
    public void wrapAroundScreen() {
        if (x < 0) x = GameEngine.WIDTH;
        if (x > GameEngine.WIDTH) x = 0;
        if (y < 0) y = GameEngine.HEIGHT;
        if (y > GameEngine.HEIGHT) y = 0;
    }

    /**
     * Checks if this object collides with another game object.
     * Uses the configured collision detector for the check.
     *
     * @param other The other game object to check collision with
     * @return true if the objects are colliding, false otherwise
     * @throws IllegalArgumentException if other is null
     */
    public boolean checkCollision(GameObject other) {
        InputValidator.validateNotNull(other, "other");
        return collisionDetector.checkCollision(this, other);
    }

    // Getters for testing
    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public double getRotation() { return rotation; }
    public boolean isActive() { return active; }

    // Setters for testing
    public void setPosition(double x, double y) { this.x = x; this.y = y; }
    public void setVelocity(double vx, double vy) { this.vx = vx; this.vy = vy; }
    public void setActive(boolean active) { this.active = active; }
}

/**
 * Interface for game entities that can be updated and rendered.
 * Used primarily for testing and type abstraction.
 */
interface GameEntity {
    void update(double deltaTime);
    void draw(Graphics2D g);
    double getRadius();
    double getX();
    double getY();
    boolean isActive();
}

/**
 * Interface for collision detection strategies.
 * Allows different collision detection algorithms to be plugged in.
 */
interface CollisionDetector {
    boolean checkCollision(GameObject obj1, GameObject obj2);
}

/**
 * Default circular collision detection implementation.
 * Uses distance-based collision checking between object centers.
 */
class DefaultCollisionDetector implements CollisionDetector {
    @Override
    public boolean checkCollision(GameObject obj1, GameObject obj2) {
        double radius1 = obj1.getRadius();
        double radius2 = obj2.getRadius();

        // If either object has zero radius, no collision
        if (radius1 <= 0 || radius2 <= 0) {
            return false;
        }

        double dx = obj1.getX() - obj2.getX();
        double dy = obj1.getY() - obj2.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance <= (radius1 + radius2);
    }
}
