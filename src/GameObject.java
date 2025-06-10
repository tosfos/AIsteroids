import java.awt.*;

public abstract class GameObject implements GameEntity {
    protected double x, y, vx, vy;
    protected double rotation;
    protected double rotationSpeed;
    protected boolean active = true;
    protected CollisionDetector collisionDetector;

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

    // Constructor for dependency injection (testable)
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

    // Update state using deltaTime in seconds.
    public abstract void update(double deltaTime);
    // Render the object.
    public abstract void draw(Graphics2D g);
    // Return a bounding rectangle for collision detection.
    public abstract Rectangle getBounds();

    public boolean isAlive() {
       return active;
    }

    public void setAlive(boolean active) {
       this.active = active;
    }

    public double getX() { return x; }
    public double getY() { return y; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }

    public abstract double getRadius();

    // Testable methods with clear responsibilities
    public void updatePosition(double deltaTime) {
        InputValidator.validateDeltaTime(deltaTime);
        x += vx * deltaTime;
        y += vy * deltaTime;
        rotation += rotationSpeed * deltaTime;
        wrapAroundScreen();
    }

    public void wrapAroundScreen() {
        if (x < 0) x = GameEngine.WIDTH;
        if (x > GameEngine.WIDTH) x = 0;
        if (y < 0) y = GameEngine.HEIGHT;
        if (y > GameEngine.HEIGHT) y = 0;
    }

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

// Interface for testability
interface GameEntity {
    void update(double deltaTime);
    void draw(Graphics2D g);
    double getRadius();
    double getX();
    double getY();
    boolean isActive();
}

// Interface for collision detection
interface CollisionDetector {
    boolean checkCollision(GameObject obj1, GameObject obj2);
}

// Default implementation
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
