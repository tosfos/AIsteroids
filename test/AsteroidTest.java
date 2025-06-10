public class AsteroidTest {

    public static void main(String[] args) {
        AsteroidTest test = new AsteroidTest();
        test.runAllTests();
    }

    public void runAllTests() {
        System.out.println("Running Asteroid Tests...");

        testAsteroidCreation();
        testAsteroidSizes();
        testAsteroidMovement();
        testAsteroidProperties();

        System.out.println("All Asteroid tests passed! ✅");
    }

    public void testAsteroidCreation() {
        Asteroid asteroid = new Asteroid(100, 200, 2, 50, 30);

        // Test initial position
        assert asteroid.getX() == 100 : "Asteroid X position should be correct";
        assert asteroid.getY() == 200 : "Asteroid Y position should be correct";

        // Test initial state
        assert asteroid.isActive() : "Asteroid should be initially active";

        // Test velocity
        assert asteroid.getVx() == 50 : "Asteroid X velocity should be correct";
        assert asteroid.getVy() == 30 : "Asteroid Y velocity should be correct";

        System.out.println("✓ Asteroid creation tests passed");
    }

    public void testAsteroidSizes() {
        Asteroid smallAsteroid = new Asteroid(100, 100, 1, 0, 0);
        Asteroid mediumAsteroid = new Asteroid(100, 100, 2, 0, 0);
        Asteroid largeAsteroid = new Asteroid(100, 100, 3, 0, 0);

        // Test size-based radius scaling
        assert largeAsteroid.getRadius() > mediumAsteroid.getRadius() : "Large asteroid should have larger radius than medium";
        assert mediumAsteroid.getRadius() > smallAsteroid.getRadius() : "Medium asteroid should have larger radius than small";

        // Test specific radius values (size * 15)
        assert smallAsteroid.getRadius() == 15 : "Small asteroid radius should be 15";
        assert mediumAsteroid.getRadius() == 30 : "Medium asteroid radius should be 30";
        assert largeAsteroid.getRadius() == 45 : "Large asteroid radius should be 45";

        System.out.println("✓ Asteroid size tests passed");
    }

    public void testAsteroidMovement() {
        Asteroid asteroid = new Asteroid(100, 100, 2, 50, 0); // Moving right

        double initialX = asteroid.getX();
        double initialY = asteroid.getY();

        // Update asteroid position
        asteroid.update(0.1); // 100ms

        // Asteroid should move based on velocity
        assert asteroid.getX() > initialX : "Asteroid should move in X direction";
        assert asteroid.getY() == initialY : "Asteroid Y should remain constant for zero Y velocity";

        System.out.println("✓ Asteroid movement tests passed");
    }

    public void testAsteroidProperties() {
        Asteroid asteroid = new Asteroid(100, 100, 2, 25, 25);

        // Test bounds match radius
        java.awt.Rectangle bounds = asteroid.getBounds();
        double radius = asteroid.getRadius();
        assert bounds.width == radius * 2 : "Bounds width should match radius * 2";
        assert bounds.height == radius * 2 : "Bounds height should match radius * 2";

        // Test bounds position
        assert bounds.x == (int)(asteroid.getX() - radius) : "Bounds X should be centered on asteroid X";
        assert bounds.y == (int)(asteroid.getY() - radius) : "Bounds Y should be centered on asteroid Y";

        System.out.println("✓ Asteroid properties tests passed");
    }
}
