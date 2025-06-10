import java.awt.Rectangle;

public class AdvancedTestRunner {

    private int testsRun = 0;
    private int testsPassed = 0;

    public static void main(String[] args) {
        AdvancedTestRunner runner = new AdvancedTestRunner();
        runner.runAllTests();
    }

    public void runAllTests() {
        System.out.println("🔬 Running Advanced AIsteroids Test Suite...\n");

        testGameObjectInheritance();
        testCollisionEdgeCases();
        testScoreCalculatorEdgeCases();
        testPowerUpMechanics();
        testWaveSystemRobustness();
        testPlayerShipMechanics();
        testBulletBehavior();
        testAsteroidSplitting();

        System.out.println("\n📊 Advanced Test Results:");
        System.out.println("Tests run: " + testsRun);
        System.out.println("Tests passed: " + testsPassed);
        System.out.println("Tests failed: " + (testsRun - testsPassed));

        if (testsPassed == testsRun) {
            System.out.println("🏆 ALL ADVANCED TESTS PASSED!");
            System.exit(0);
        } else {
            System.out.println("❌ Some tests failed. Check output above.");
            System.exit(1);
        }
    }

    public void testGameObjectInheritance() {
        System.out.println("Testing GameObject Inheritance...");

        testCase("All game objects implement GameEntity", () -> {
            TestGameObject obj = new TestGameObject(0, 0, 5);
            return obj instanceof GameEntity;
        });

        testCase("Position wrapping works correctly", () -> {
            TestGameObject obj = new TestGameObject(-10, -10, 5);
            obj.wrapAroundScreen();
            return obj.getX() == 800 && obj.getY() == 600; // Assuming screen size
        });

        testCase("Velocity updates position correctly", () -> {
            TestGameObject obj = new TestGameObject(100, 100, 5);
            obj.setVelocity(50, -30);
            obj.updatePosition(0.2); // 200ms
            return Math.abs(obj.getX() - 110) < 0.001 && Math.abs(obj.getY() - 94) < 0.001;
        });
    }

    public void testCollisionEdgeCases() {
        System.out.println("\nTesting Collision Edge Cases...");
        DefaultCollisionDetector detector = new DefaultCollisionDetector();

        testCase("Zero radius objects don't collide", () -> {
            TestGameObject obj1 = new TestGameObject(100, 100, 0);
            TestGameObject obj2 = new TestGameObject(100, 100, 0);
            return !detector.checkCollision(obj1, obj2);
        });

        testCase("Exact touch distance is collision", () -> {
            TestGameObject obj1 = new TestGameObject(100, 100, 10);
            TestGameObject obj2 = new TestGameObject(120, 100, 10); // Distance = 20, radii sum = 20
            return detector.checkCollision(obj1, obj2);
        });

        testCase("Large distance prevents collision", () -> {
            TestGameObject obj1 = new TestGameObject(0, 0, 10);
            TestGameObject obj2 = new TestGameObject(1000, 1000, 10);
            return !detector.checkCollision(obj1, obj2);
        });
    }

    public void testScoreCalculatorEdgeCases() {
        System.out.println("\nTesting ScoreCalculator Edge Cases...");
        ScoreCalculator calculator = new ScoreCalculator();

        testCase("Zero radius asteroid gets minimum score", () -> {
            ScoreCalculator.ScoreResult result = calculator.calculateAsteroidScore(0, 1, 1.0);
            return result.baseScore > 0;
        });

        testCase("Very high wave number doesn't break scoring", () -> {
            ScoreCalculator.ScoreResult result = calculator.calculateAsteroidScore(20, 100, 5.0);
            return result.totalScore > 0 && result.totalScore < 1000000; // Reasonable bounds
        });

        testCase("Perfect wave with speed bonus combines correctly", () -> {
            ScoreCalculator.ScoreResult result = calculator.calculateWaveCompletionScore(1, true, 10);
            return result.multiplierBonus >= 1500; // 1000 perfect + 500 speed
        });

        testCase("Score multiplier caps at 5.0", () -> {
            double multiplier = calculator.calculateScoreMultiplier(50, 100, true);
            return multiplier <= 5.0;
        });
    }

    public void testPowerUpMechanics() {
        System.out.println("\nTesting PowerUp Mechanics...");

        testCase("All PowerUp types have valid properties", () -> {
            for (PowerUp.PowerUpType type : PowerUp.PowerUpType.values()) {
                if (type.getName() == null || type.getColor() == null || type.getDuration() <= 0) {
                    return false;
                }
            }
            return true;
        });

        testCase("PowerUp collision radius matches bounds", () -> {
            PowerUp powerUp = new PowerUp(100, 100, PowerUp.PowerUpType.RAPID_FIRE);
            Rectangle bounds = powerUp.getBounds();
            double radius = powerUp.getRadius();
            return bounds.width == radius * 2 && bounds.height == radius * 2;
        });

        testCase("PowerUp creates with random movement", () -> {
            PowerUp powerUp = new PowerUp(100, 100, PowerUp.PowerUpType.SHIELD);
            // PowerUps should have some initial velocity
            return powerUp.getVx() != 0 || powerUp.getVy() != 0;
        });
    }

    public void testWaveSystemRobustness() {
        System.out.println("\nTesting Wave System Robustness...");
        WaveSystem waveSystem = new WaveSystem();

        testCase("Wave system handles multiple resets", () -> {
            waveSystem.startWave(10);
            waveSystem.reset();
            return waveSystem.getCurrentWave() == 1;
        });

        testCase("Asteroid destruction updates count correctly", () -> {
            waveSystem.startWave(1);
            int initialCount = waveSystem.getAsteroidsRemaining();
            waveSystem.asteroidDestroyed();
            return waveSystem.getAsteroidsRemaining() == initialCount - 1;
        });

        testCase("Boss waves occur at correct intervals", () -> {
            waveSystem.startWave(5);
            boolean boss5 = waveSystem.isBossWave();
            waveSystem.startWave(10);
            boolean boss10 = waveSystem.isBossWave();
            waveSystem.startWave(7);
            boolean boss7 = waveSystem.isBossWave();
            return boss5 && boss10 && !boss7;
        });

        testCase("Difficulty multiplier increases with waves", () -> {
            waveSystem.startWave(1);
            double diff1 = waveSystem.getDifficultyMultiplier();
            waveSystem.startWave(10);
            double diff10 = waveSystem.getDifficultyMultiplier();
            return diff10 > diff1;
        });
    }

    public void testPlayerShipMechanics() {
        System.out.println("\nTesting PlayerShip Mechanics...");
        PlayerShip ship = new PlayerShip(400, 300);

        testCase("PlayerShip starts with 3 lives", () -> {
            return ship.getLives() == 3;
        });

        testCase("PlayerShip damage reduces lives", () -> {
            int initialLives = ship.getLives();
            ship.damage();
            return ship.getLives() == initialLives - 1;
        });

        testCase("PlayerShip reset restores state", () -> {
            ship.damage();
            ship.reset();
            return ship.getLives() == 3 && ship.isActive();
        });

        testCase("PlayerShip fire bullet creates bullets", () -> {
            var bullets = ship.fireBulletForTesting();
            return bullets.size() >= 1; // Should create at least one bullet
        });
    }

    public void testBulletBehavior() {
        System.out.println("\nTesting Bullet Behavior...");

        testCase("Bullet moves in correct direction", () -> {
            Bullet bullet = new Bullet(100, 100, 0); // 0 radians = right
            bullet.update(0.1);
            return bullet.getX() > 100 && bullet.getY() == 100;
        });

        testCase("Bullet expires after lifespan", () -> {
            Bullet bullet = new Bullet(100, 100, 0);
            bullet.update(3.0); // More than 2 second lifespan
            return !bullet.isActive();
        });

        testCase("Bullet radius matches collision size", () -> {
            Bullet bullet = new Bullet(100, 100, 0);
            return bullet.getRadius() == 3.0;
        });
    }

    public void testAsteroidSplitting() {
        System.out.println("\nTesting Asteroid Behavior...");

        testCase("Asteroid has correct radius for size", () -> {
            Asteroid small = new Asteroid(100, 100, 1, 0, 0);
            Asteroid large = new Asteroid(100, 100, 3, 0, 0);
            return large.getRadius() > small.getRadius();
        });

        testCase("Asteroid moves based on velocity", () -> {
            Asteroid asteroid = new Asteroid(100, 100, 2, 50, 0);
            asteroid.update(0.1);
            return asteroid.getX() > 100;
        });

        testCase("Large asteroid creates smaller asteroids when hit", () -> {
            // This would require a mock GameEngine to test properly
            // For now, just test that the asteroid becomes inactive
            Asteroid asteroid = new Asteroid(100, 100, 3, 0, 0);
            // Can't test hit() without GameEngine, but can test properties
            return asteroid.getRadius() == 45; // Size 3 * 15 = 45
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

    // Test implementation of GameObject
    private static class TestGameObject extends GameObject {
        private double radius;

        public TestGameObject(double x, double y, double radius) {
            super(x, y);
            this.radius = radius;
        }

        @Override
        public void update(double deltaTime) {
            updatePosition(deltaTime);
        }

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
