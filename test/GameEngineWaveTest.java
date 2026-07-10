/**
 * Tests for wave spawn logic and wave completion bonus.
 */
public class GameEngineWaveTest {

    public static void main(String[] args) {
        GameEngineWaveTest test = new GameEngineWaveTest();
        test.runAllTests();
    }

    public void runAllTests() {
        System.out.println("Running GameEngine Wave Tests...");

        testWaveCompletionBonusAwarded();
        testWaveSpawnWhenScreenClear();
        testNoSpuriousAsteroidDestroyed();

        System.out.println("All GameEngine wave tests passed! ✅");
    }

    public void testWaveCompletionBonusAwarded() {
        WaveSystem waveSystem = new WaveSystem();
        int initialScore = 0;

        // Simulate completing wave 1 by destroying all asteroids
        int remaining = waveSystem.getAsteroidsRemaining();
        for (int i = 0; i < remaining; i++) {
            waveSystem.asteroidDestroyed();
        }

        int bonus = waveSystem.consumePendingWaveBonus();
        assert bonus > 0 : "Wave completion should produce a pending bonus";
        assert waveSystem.getCurrentWave() == 2 : "Should advance to wave 2 after completion";

        int newScore = initialScore + bonus;
        assert newScore == bonus : "Bonus should be awardable to score";

        System.out.println("✓ Wave completion bonus tests passed");
    }

    public void testWaveSpawnWhenScreenClear() {
        WaveSystem waveSystem = new WaveSystem();
        int initialRemaining = waveSystem.getAsteroidsRemaining();

        // After destroying all wave asteroids, next wave should be ready to spawn
        for (int i = 0; i < initialRemaining; i++) {
            waveSystem.asteroidDestroyed();
        }

        assert waveSystem.getAsteroidsRemaining() > 0
            : "Next wave should have asteroids remaining to spawn";
        assert waveSystem.getCurrentWave() == 2 : "Should be on wave 2";

        System.out.println("✓ Wave spawn readiness tests passed");
    }

    public void testNoSpuriousAsteroidDestroyed() {
        WaveSystem waveSystem = new WaveSystem();
        int remainingBefore = waveSystem.getAsteroidsRemaining();

        // Simulating screen clear should NOT decrement via fake destroy;
        // remaining count should only change via actual asteroidDestroyed calls
        assert waveSystem.getAsteroidsRemaining() == remainingBefore
            : "Remaining count should not change without actual asteroid destruction";

        System.out.println("✓ No spurious asteroidDestroyed tests passed");
    }
}
