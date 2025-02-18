import java.awt.*;

public abstract class GameObject {
    protected double x, y;
    protected double vx, vy;
    protected boolean alive = true;
    
    public GameObject(double x, double y) {
       this.x = x;
       this.y = y;
    }
    
    // Update state using deltaTime in seconds.
    public abstract void update(double deltaTime);
    // Render the object.
    public abstract void draw(Graphics2D g);
    // Return a bounding rectangle for collision detection.
    public abstract Rectangle getBounds();
    
    public boolean isAlive() {
       return alive;
    }
    
    public void setAlive(boolean alive) {
       this.alive = alive;
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
} 