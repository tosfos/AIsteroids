public class CollisionTest {

    public static void main(String[] args) {
        CollisionTest test = new CollisionTest();
        test.runAllTests();
    }

    public void runAllTests() {
        System.out.println("Running Collision Detection Tests...");

        testBasicCollision();
        testEdgeCases();
        testZeroRadius();

        System.out.println("All collision tests passed! ✅");
    }

    public void testBasicCollision() {
        DefaultCollisionDetector detector = new DefaultCollisionDetector();
        TestGameObject obj1 = new TestGameObject(100, 100, 10);
        TestGameObject obj2 = new TestGameObject(105, 105, 10);
        TestGameObject obj3 = new TestGameObject(150, 150, 10);

        // Close objects should collide
        assert detector.checkCollision(obj1, obj2) : "Close objects should collide";

        // Distant objects should not collide
        assert !detector.checkCollision(obj1, obj3) : "Distant objects should not collide";

        // Object should collide with itself
        assert detector.checkCollision(obj1, obj1) : "Object should collide with itself";

        System.out.println("✓ Basic collision tests passed");
    }

    public void testEdgeCases() {
        DefaultCollisionDetector detector = new DefaultCollisionDetector();

        // Exact touch distance
        TestGameObject obj1 = new TestGameObject(100, 100, 10);
        TestGameObject obj2 = new TestGameObject(120, 100, 10); // Distance = 20, radii sum = 20
        assert detector.checkCollision(obj1, obj2) : "Exact touch distance should be collision";

        // Large distance
        TestGameObject obj3 = new TestGameObject(0, 0, 10);
        TestGameObject obj4 = new TestGameObject(1000, 1000, 10);
        assert !detector.checkCollision(obj3, obj4) : "Large distance should prevent collision";

        System.out.println("✓ Edge case collision tests passed");
    }

    public void testZeroRadius() {
        DefaultCollisionDetector detector = new DefaultCollisionDetector();

        // Zero radius objects
        TestGameObject obj1 = new TestGameObject(100, 100, 0);
        TestGameObject obj2 = new TestGameObject(100, 100, 0);
        assert !detector.checkCollision(obj1, obj2) : "Zero radius objects should not collide";

        // One zero radius
        TestGameObject obj3 = new TestGameObject(100, 100, 0);
        TestGameObject obj4 = new TestGameObject(100, 100, 10);
        assert !detector.checkCollision(obj3, obj4) : "Zero radius object should not collide";

        System.out.println("✓ Zero radius collision tests passed");
    }

    // Simple test game object for collision testing
    private static class TestGameObject extends GameObject {
        private double radius;

        public TestGameObject(double x, double y, double radius) {
            super(x, y);
            this.radius = radius;
        }

        @Override
        public void update(double deltaTime) {}

        @Override
        public void draw(java.awt.Graphics2D g) {}

        @Override
        public double getRadius() {
            return radius;
        }

        @Override
        public java.awt.Rectangle getBounds() {
            return new java.awt.Rectangle((int)(x - radius), (int)(y - radius),
                                        (int)(radius * 2), (int)(radius * 2));
        }
    }
}
