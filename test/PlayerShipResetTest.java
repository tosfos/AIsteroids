/**
 * Tests that PlayerShip.reset() clears power-ups and restores defaults.
 */
public class PlayerShipResetTest {

    public static void main(String[] args) {
        PlayerShipResetTest test = new PlayerShipResetTest();
        test.runAllTests();
    }

    public void runAllTests() {
        System.out.println("Running PlayerShip Reset Tests...");

        testResetClearsPowerUps();
        testResetRestoresDefaults();

        System.out.println("All PlayerShip reset tests passed! ✅");
    }

    public void testResetClearsPowerUps() {
        PlayerShip ship = new PlayerShip(400, 300);
        ship.addPowerUp(PowerUp.PowerUpType.SHIELD);
        ship.addPowerUp(PowerUp.PowerUpType.RAPID_FIRE);
        ship.addPowerUp(PowerUp.PowerUpType.SPEED_BOOST);

        assert ship.hasShield() : "Shield should be active before reset";
        assert !ship.getActivePowerUps().isEmpty() : "Power-ups should be active before reset";

        ship.reset();

        assert !ship.hasShield() : "Shield should be cleared after reset";
        assert ship.getActivePowerUps().isEmpty() : "Active power-ups should be cleared after reset";

        System.out.println("✓ Reset clears power-ups tests passed");
    }

    public void testResetRestoresDefaults() {
        PlayerShip ship = new PlayerShip(400, 300);
        ship.addPowerUp(PowerUp.PowerUpType.SPEED_BOOST);

        // Speed boost modifies ship stats
        ship.update(0.01);

        ship.reset();

        // After reset, ship should be at center with default lives
        assert ship.getX() == GameEngine.WIDTH / 2 : "X should reset to center";
        assert ship.getY() == GameEngine.HEIGHT / 2 : "Y should reset to center";
        assert ship.getLives() == GameConfig.PlayerShip.INITIAL_LIVES : "Lives should reset";
        assert ship.isActive() : "Ship should be active after reset";

        System.out.println("✓ Reset restores defaults tests passed");
    }
}
