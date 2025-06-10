public class PowerUpTest {

    public static void main(String[] args) {
        PowerUpTest test = new PowerUpTest();
        test.runAllTests();
    }

    public void runAllTests() {
        System.out.println("Running PowerUp System Tests...");

        testPowerUpTypes();
        testPowerUpProperties();
        testPowerUpMechanics();

        System.out.println("All PowerUp tests passed! ✅");
    }

    public void testPowerUpTypes() {
        // Test that all power-up types are defined
        assert PowerUp.PowerUpType.values().length == 6 : "Should have 6 power-up types";

        // Test each type has valid properties
        for (PowerUp.PowerUpType type : PowerUp.PowerUpType.values()) {
            assert type.getName() != null : "PowerUp type should have a name";
            assert type.getColor() != null : "PowerUp type should have a color";
            assert type.getDuration() > 0 : "PowerUp type should have positive duration";
        }

        System.out.println("✓ PowerUp type tests passed");
    }

    public void testPowerUpProperties() {
        PowerUp powerUp = new PowerUp(100, 150, PowerUp.PowerUpType.RAPID_FIRE);

        // Test position
        assert powerUp.getX() == 100 : "PowerUp X position should be correct";
        assert powerUp.getY() == 150 : "PowerUp Y position should be correct";

        // Test initial state
        assert powerUp.isActive() : "PowerUp should be initially active";

        // Test radius
        assert powerUp.getRadius() == 12.0 : "PowerUp radius should be 12.0";

        // Test bounds
        java.awt.Rectangle bounds = powerUp.getBounds();
        assert bounds.width == 24 : "PowerUp bounds width should be 24";
        assert bounds.height == 24 : "PowerUp bounds height should be 24";

        System.out.println("✓ PowerUp property tests passed");
    }

    public void testPowerUpMechanics() {
        PowerUp powerUp = new PowerUp(100, 100, PowerUp.PowerUpType.SHIELD);

        // Test that PowerUp has initial movement
        assert powerUp.getVx() != 0 || powerUp.getVy() != 0 : "PowerUp should have initial velocity";

        // Test collision radius matches bounds
        java.awt.Rectangle bounds = powerUp.getBounds();
        double radius = powerUp.getRadius();
        assert bounds.width == radius * 2 : "PowerUp bounds width should match radius";
        assert bounds.height == radius * 2 : "PowerUp bounds height should match radius";

        // Test type access
        assert powerUp.getType() == PowerUp.PowerUpType.SHIELD : "PowerUp type should be accessible";

        System.out.println("✓ PowerUp mechanics tests passed");
    }
}
