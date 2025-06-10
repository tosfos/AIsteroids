public class IntegrationTestSuite {

    private int testsRun = 0;
    private int testsPassed = 0;

    public static void main(String[] args) {
        IntegrationTestSuite runner = new IntegrationTestSuite();
        runner.runAllTests();
    }

    public void runAllTests() {
        System.out.println("🧪 Running AIsteroids Test Suite...\n");

        testScoreCalculator();
        testCollisionDetection();
        testPowerUpSystem();
        testWaveProgression();

        System.out.println("\n📊 Test Results:");
        System.out.println("Tests run: " + testsRun);
        System.out.println("Tests passed: " + testsPassed);
        System.out.println("Tests failed: " + (testsRun - testsPassed));

        if (testsPassed == testsRun) {
            System.out.println("🎉 ALL TESTS PASSED!");
            System.exit(0);
        } else {
            System.out.println("❌ Some tests failed. Check output above.");
            System.exit(1);
        }
    }

    public void testScoreCalculator() {
        System.out.println("Testing ScoreCalculator...");
        ScoreCalculator calculator = new ScoreCalculator();

        // Test asteroid scoring
        testCase("Small asteroid base score", () -> {
            ScoreCalculator.ScoreResult result = calculator.calculateAsteroidScore(10, 1, 1.0);
            return result.baseScore == 20;
        });

        testCase("Medium asteroid base score", () -> {
            ScoreCalculator.ScoreResult result = calculator.calculateAsteroidScore(20, 1, 1.0);
            return result.baseScore == 50;
        });

        testCase("Large asteroid base score", () -> {
            ScoreCalculator.ScoreResult result = calculator.calculateAsteroidScore(30, 1, 1.0);
            return result.baseScore == 100;
        });

        // Test wave progression multiplier
        testCase("Wave multiplier increases score", () -> {
            ScoreCalculator.ScoreResult wave1 = calculator.calculateAsteroidScore(20, 1, 1.0);
            ScoreCalculator.ScoreResult wave4 = calculator.calculateAsteroidScore(20, 4, 1.0);
            return wave4.totalScore > wave1.totalScore;
        });

        // Test perfect wave bonus
        testCase("Perfect wave gives bonus", () -> {
            ScoreCalculator.ScoreResult normal = calculator.calculateWaveCompletionScore(1, false, 30);
            ScoreCalculator.ScoreResult perfect = calculator.calculateWaveCompletionScore(1, true, 30);
            return perfect.totalScore > normal.totalScore && perfect.multiplierBonus == 1000;
        });

        // Test speed bonus
        testCase("Speed bonus for fast completion", () -> {
            ScoreCalculator.ScoreResult slow = calculator.calculateWaveCompletionScore(1, false, 25);
            ScoreCalculator.ScoreResult fast = calculator.calculateWaveCompletionScore(1, false, 15);
            return fast.totalScore > slow.totalScore && fast.multiplierBonus == 500;
        });

        // Test score multiplier
        testCase("Score multiplier calculation", () -> {
            double base = calculator.calculateScoreMultiplier(1, 0, false);
            double enhanced = calculator.calculateScoreMultiplier(5, 3, true);
            return base == 1.0 && enhanced > base && enhanced <= 5.0;
        });
    }

    public void testCollisionDetection() {
        System.out.println("\nTesting Collision Detection...");
        DefaultCollisionDetector detector = new DefaultCollisionDetector();
        TestGameObject obj1 = new TestGameObject(100, 100, 10);
        TestGameObject obj2 = new TestGameObject(105, 105, 10);
        TestGameObject obj3 = new TestGameObject(150, 150, 10);

        testCase("Close objects collide", () -> {
            return detector.checkCollision(obj1, obj2);
        });

        testCase("Distant objects don't collide", () -> {
            return !detector.checkCollision(obj1, obj3);
        });

        testCase("Object collides with itself", () -> {
            return detector.checkCollision(obj1, obj1);
        });
    }

    public void testPowerUpSystem() {
        System.out.println("\nTesting PowerUp System...");

        testCase("PowerUp types are defined", () -> {
            return PowerUp.PowerUpType.values().length == 6;
        });

        testCase("PowerUp has valid position", () -> {
            PowerUp powerUp = new PowerUp(100, 150, PowerUp.PowerUpType.RAPID_FIRE);
            return powerUp.getX() == 100 && powerUp.getY() == 150;
        });

        testCase("PowerUp is initially active", () -> {
            PowerUp powerUp = new PowerUp(0, 0, PowerUp.PowerUpType.SHIELD);
            return powerUp.isActive();
        });
    }

    public void testWaveProgression() {
        System.out.println("\nTesting Wave System...");
        WaveSystem waveSystem = new WaveSystem(); // Default constructor

        testCase("Wave system starts at wave 1", () -> {
            return waveSystem.getCurrentWave() == 1;
        });

        testCase("Boss wave detection", () -> {
            // Test current wave boss status
            waveSystem.startWave(5);
            boolean wave5Boss = waveSystem.isBossWave();
            waveSystem.startWave(10);
            boolean wave10Boss = waveSystem.isBossWave();
            waveSystem.startWave(3);
            boolean wave3Boss = waveSystem.isBossWave();
            return wave5Boss && wave10Boss && !wave3Boss;
        });

        testCase("Asteroid count scales with wave", () -> {
            waveSystem.startWave(1);
            int wave1Count = waveSystem.getAsteroidsRemaining();
            waveSystem.startWave(5);
            int wave5Count = waveSystem.getAsteroidsRemaining();
            return wave5Count >= wave1Count; // Boss waves might have fewer but stronger asteroids
        });
    }

    private void testCase(String name, TestFunction test) {
        testsRun++;
        try {
            if (test.run()) {
                System.out.println("  ✓ " + name);
                testsPassed++;
            } else {
                System.out.println("  ❌ " + name + " - Assertion failed");
            }
        } catch (Exception e) {
            System.out.println("  ❌ " + name + " - Exception: " + e.getMessage());
        }
    }

    @FunctionalInterface
    interface TestFunction {
        boolean run() throws Exception;
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
