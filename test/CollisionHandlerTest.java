/**
 * Tests for CollisionHandler: laser segment-circle intersection and shield debounce.
 */
public class CollisionHandlerTest {

    public static void main(String[] args) {
        CollisionHandlerTest test = new CollisionHandlerTest();
        test.runAllTests();
    }

    public void runAllTests() {
        System.out.println("Running CollisionHandler Tests...");

        testLaserSegmentCircleIntersection();
        testLaserMissesDistantAsteroid();
        testShieldBlockDebounce();

        System.out.println("All CollisionHandler tests passed! ✅");
    }

    public void testLaserSegmentCircleIntersection() {
        assert LaserBeam.segmentIntersectsCircle(0, 300, 800, 300, 400, 300, 30)
            : "Beam should intersect asteroid on its path";

        assert LaserBeam.segmentIntersectsCircle(0, 300, 800, 300, 400, 320, 25)
            : "Beam should intersect asteroid near its path";

        System.out.println("✓ Laser segment-circle intersection tests passed");
    }

    public void testLaserMissesDistantAsteroid() {
        assert !LaserBeam.segmentIntersectsCircle(0, 300, 800, 300, 400, 500, 30)
            : "Beam should miss asteroid far from path";

        LaserBeam beam = new LaserBeam(0, 300, 0);
        Asteroid farAsteroid = new Asteroid(700, 300, 2, 0, 0);
        assert beam.intersectsCircle(farAsteroid.getX(), farAsteroid.getY(), farAsteroid.getRadius())
            : "Beam should hit asteroid at far end of segment, not just at origin";

        System.out.println("✓ Laser miss detection tests passed");
    }

    public void testShieldBlockDebounce() {
        GameEngine engine = GameEngine.createForTesting();
        CollisionHandler handler = new CollisionHandler(engine);
        PlayerShip ship = engine.getPlayer();
        ship.addPowerUp(PowerUp.PowerUpType.SHIELD);
        Asteroid asteroid = new Asteroid(410, 300, 2, 0, 0);

        LeaderboardSystem.resetStats();
        int initialBlocks = LeaderboardSystem.getGameStat("shield_blocks");

        handler.beginCollisionFrame();
        handler.handleCollision(ship, asteroid);
        handler.handleCollision(ship, asteroid);
        handler.endCollisionFrame();

        assert LeaderboardSystem.getGameStat("shield_blocks") == initialBlocks + 1
            : "Shield block should only count once per asteroid contact";

        handler.beginCollisionFrame();
        handler.endCollisionFrame();
        handler.beginCollisionFrame();
        handler.handleCollision(ship, asteroid);
        handler.endCollisionFrame();

        assert LeaderboardSystem.getGameStat("shield_blocks") == initialBlocks + 2
            : "Shield block should count again after contact ends";

        System.out.println("✓ Shield debounce tests passed");
    }
}
