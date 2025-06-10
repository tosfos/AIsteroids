public class BulletTest {

    public static void main(String[] args) {
        BulletTest test = new BulletTest();
        test.runAllTests();
    }

    public void runAllTests() {
        System.out.println("Running Bullet Tests...");

        testBulletCreation();
        testBulletMovement();
        testBulletLifespan();
        testBulletCollision();

        System.out.println("All Bullet tests passed! ✅");
    }

    public void testBulletCreation() {
        Bullet bullet = new Bullet(100, 200, Math.PI / 4); // 45 degrees

        // Test initial position
        assert bullet.getX() == 100 : "Bullet X position should be correct";
        assert bullet.getY() == 200 : "Bullet Y position should be correct";

        // Test initial state
        assert bullet.isActive() : "Bullet should be initially active";

        // Test radius
        assert bullet.getRadius() == 3.0 : "Bullet radius should be 3.0";

        // Test bounds
        java.awt.Rectangle bounds = bullet.getBounds();
        assert bounds.width == 6 : "Bullet bounds width should be 6";
        assert bounds.height == 6 : "Bullet bounds height should be 6";

        System.out.println("✓ Bullet creation tests passed");
    }

    public void testBulletMovement() {
        Bullet bullet = new Bullet(100, 100, 0); // 0 radians = rightward

        double initialX = bullet.getX();
        double initialY = bullet.getY();

        // Update bullet position
        bullet.update(0.1); // 100ms

        // Bullet should move right (positive X direction)
        assert bullet.getX() > initialX : "Bullet should move in X direction";
        assert bullet.getY() == initialY : "Bullet Y should remain constant for 0 radians";

        System.out.println("✓ Bullet movement tests passed");
    }

    public void testBulletLifespan() {
        Bullet bullet = new Bullet(100, 100, 0);

        // Bullet should be active initially
        assert bullet.isActive() : "Bullet should be active initially";

        // Update bullet beyond its lifespan (2 seconds)
        bullet.update(3.0);

        // Bullet should be inactive after lifespan expires
        assert !bullet.isActive() : "Bullet should be inactive after lifespan expires";

        System.out.println("✓ Bullet lifespan tests passed");
    }

    public void testBulletCollision() {
        Bullet bullet = new Bullet(100, 100, 0);

        // Test collision radius matches expected value
        assert bullet.getRadius() == 3.0 : "Bullet collision radius should be 3.0";

        // Test bounds match radius
        java.awt.Rectangle bounds = bullet.getBounds();
        double expectedSize = bullet.getRadius() * 2;
        assert bounds.width == expectedSize : "Bounds width should match radius * 2";
        assert bounds.height == expectedSize : "Bounds height should match radius * 2";

        System.out.println("✓ Bullet collision tests passed");
    }
}
