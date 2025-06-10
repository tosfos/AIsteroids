import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Graphics2D;

class GameObjectTest {

    private TestGameObject gameObject;
    private MockCollisionDetector mockCollisionDetector;

    @BeforeEach
    void setUp() {
        mockCollisionDetector = new MockCollisionDetector();
        gameObject = new TestGameObject(100, 150, mockCollisionDetector);
    }

    @Test
    void testInitialPosition() {
        assertEquals(100, gameObject.getX(), 0.001);
        assertEquals(150, gameObject.getY(), 0.001);
        assertTrue(gameObject.isActive());
    }

    @Test
    void testPositionUpdate() {
        gameObject.setVelocity(50, -30);
        gameObject.updatePosition(0.1); // 100ms

        assertEquals(105, gameObject.getX(), 0.001);
        assertEquals(147, gameObject.getY(), 0.001);
    }

    @Test
    void testScreenWrapHorizontal() {
        gameObject.setPosition(-5, 100);
        gameObject.wrapAroundScreen();
        assertEquals(800, gameObject.getX(), 0.001); // Assumes GameEngine.WIDTH = 800

        gameObject.setPosition(805, 100);
        gameObject.wrapAroundScreen();
        assertEquals(0, gameObject.getX(), 0.001);
    }

    @Test
    void testScreenWrapVertical() {
        gameObject.setPosition(100, -5);
        gameObject.wrapAroundScreen();
        assertEquals(600, gameObject.getY(), 0.001); // Assumes GameEngine.HEIGHT = 600

        gameObject.setPosition(100, 605);
        gameObject.wrapAroundScreen();
        assertEquals(0, gameObject.getY(), 0.001);
    }

    @Test
    void testCollisionDetection() {
        TestGameObject other = new TestGameObject(120, 170, mockCollisionDetector);

        mockCollisionDetector.setShouldCollide(true);
        assertTrue(gameObject.checkCollision(other));

        mockCollisionDetector.setShouldCollide(false);
        assertFalse(gameObject.checkCollision(other));
    }

    @Test
    void testSettersAndGetters() {
        gameObject.setPosition(200, 250);
        assertEquals(200, gameObject.getX(), 0.001);
        assertEquals(250, gameObject.getY(), 0.001);

        gameObject.setVelocity(10, -20);
        assertEquals(10, gameObject.getVx(), 0.001);
        assertEquals(-20, gameObject.getVy(), 0.001);

        gameObject.setActive(false);
        assertFalse(gameObject.isActive());
    }

    // Test implementation of GameObject
    private static class TestGameObject extends GameObject {
        public TestGameObject(double x, double y, CollisionDetector collisionDetector) {
            super(x, y, collisionDetector);
        }

        @Override
        public void update(double deltaTime) {
            updatePosition(deltaTime);
        }

        @Override
        public void draw(Graphics2D g) {
            // Test implementation - no drawing needed
        }

        @Override
        public double getRadius() {
            return 10.0;
        }
    }

    // Mock collision detector for testing
    private static class MockCollisionDetector implements CollisionDetector {
        private boolean shouldCollide = false;

        public void setShouldCollide(boolean shouldCollide) {
            this.shouldCollide = shouldCollide;
        }

        @Override
        public boolean checkCollision(GameObject obj1, GameObject obj2) {
            return shouldCollide;
        }
    }
}
