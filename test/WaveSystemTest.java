public class WaveSystemTest {

    public static void main(String[] args) {
        WaveSystemTest test = new WaveSystemTest();
        test.runAllTests();
    }

    public void runAllTests() {
        System.out.println("Running Wave System Tests...");

        testWaveProgression();
        testBossWaves();
        testWaveReset();
        testAsteroidCounting();

        System.out.println("All Wave System tests passed! ✅");
    }

    public void testWaveProgression() {
        WaveSystem waveSystem = new WaveSystem();

        // Test initial state
        assert waveSystem.getCurrentWave() == 1 : "Wave system should start at wave 1";

        // Test wave progression
        waveSystem.startWave(5);
        assert waveSystem.getCurrentWave() == 5 : "Should be able to set current wave";

        // Test difficulty scaling
        waveSystem.startWave(1);
        double diff1 = waveSystem.getDifficultyMultiplier();
        waveSystem.startWave(10);
        double diff10 = waveSystem.getDifficultyMultiplier();
        assert diff10 > diff1 : "Difficulty should increase with wave number";

        System.out.println("✓ Wave progression tests passed");
    }

    public void testBossWaves() {
        WaveSystem waveSystem = new WaveSystem();

        // Test boss wave detection
        waveSystem.startWave(5);
        assert waveSystem.isBossWave() : "Wave 5 should be a boss wave";

        waveSystem.startWave(10);
        assert waveSystem.isBossWave() : "Wave 10 should be a boss wave";

        waveSystem.startWave(15);
        assert waveSystem.isBossWave() : "Wave 15 should be a boss wave";

        waveSystem.startWave(3);
        assert !waveSystem.isBossWave() : "Wave 3 should not be a boss wave";

        waveSystem.startWave(7);
        assert !waveSystem.isBossWave() : "Wave 7 should not be a boss wave";

        System.out.println("✓ Boss wave tests passed");
    }

    public void testWaveReset() {
        WaveSystem waveSystem = new WaveSystem();

        // Advance to later wave
        waveSystem.startWave(10);
        assert waveSystem.getCurrentWave() == 10 : "Should be at wave 10";

        // Reset
        waveSystem.reset();
        assert waveSystem.getCurrentWave() == 1 : "Reset should return to wave 1";

        System.out.println("✓ Wave reset tests passed");
    }

    public void testAsteroidCounting() {
        WaveSystem waveSystem = new WaveSystem();

        // Start a wave
        waveSystem.startWave(1);
        int initialCount = waveSystem.getAsteroidsRemaining();
        assert initialCount > 0 : "Wave should have asteroids";

        // Destroy an asteroid
        waveSystem.asteroidDestroyed();
        assert waveSystem.getAsteroidsRemaining() == initialCount - 1 : "Asteroid count should decrease";

        // Test scaling with wave number
        waveSystem.startWave(1);
        int wave1Count = waveSystem.getAsteroidsRemaining();
        waveSystem.startWave(5);
        int wave5Count = waveSystem.getAsteroidsRemaining();
        assert wave5Count >= wave1Count : "Later waves should have at least as many asteroids";

        System.out.println("✓ Asteroid counting tests passed");
    }
}
