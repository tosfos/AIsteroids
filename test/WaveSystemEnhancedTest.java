/**
 * Test suite for enhanced WaveSystem functionality.
 * Tests adaptive difficulty, statistics tracking, and smooth progression.
 */
import java.util.List;

public class WaveSystemEnhancedTest {
    private static final double DELTA = 0.001;  // For floating point comparisons

    public static void main(String[] args) {
        testDifficultyProgression();
        testAdaptiveDifficulty();
        testStatsTracking();
        testSmoothedDifficultyCurve();
        testPerformanceHistory();
        System.out.println("All WaveSystem enhanced tests passed! ✅");
    }

    private static void testDifficultyProgression() {
        System.out.println("Testing difficulty progression...");
        
        WaveSystem system = new WaveSystem();
        double initialDifficulty = system.getDifficultyMultiplier();
        
        // Progress through several waves
        for (int i = 0; i < 5; i++) {
            system.startWave(i + 2);
            double newDifficulty = system.getDifficultyMultiplier();
            assert newDifficulty > initialDifficulty : 
                String.format("Difficulty should increase (wave %d)", i + 2);
            initialDifficulty = newDifficulty;
        }
        
        System.out.println("✓ Difficulty progression test passed");
    }

    private static void testAdaptiveDifficulty() {
        System.out.println("Testing adaptive difficulty...");
        
        WaveSystem system = new WaveSystem();
        system.startWave(1);
        
        // Simulate poor performance
        for (int i = 0; i < 10; i++) {
            system.playerDamaged();
        }
        system.recordBulletShot();
        system.asteroidDestroyed(); // Complete wave with poor performance
        
        double difficulty1 = system.getDifficultyMultiplier();
        
        // Reset and simulate good performance
        system = new WaveSystem();
        system.startWave(1);
        
        // Record perfect accuracy and no damage
        for (int i = 0; i < 10; i++) {
            system.recordBulletShot();
            system.asteroidDestroyed();
        }
        system.recordPowerUpCollected();
        system.asteroidDestroyed(); // Complete wave with good performance
        
        double difficulty2 = system.getDifficultyMultiplier();
        
        assert difficulty2 > difficulty1 : 
            "Difficulty should be higher for better performance";
            
        System.out.println("✓ Adaptive difficulty test passed");
    }

    private static void testStatsTracking() {
        System.out.println("Testing statistics tracking...");
        
        WaveSystem system = new WaveSystem();
        system.startWave(1);
        
        // Record various events
        system.recordBulletShot();
        system.asteroidDestroyed();
        system.recordPowerUpCollected();
        system.playerDamaged();
        
        WaveStats stats = system.getCurrentWaveStats();
        assert stats != null : "Current wave stats should exist";
        assert stats.getBulletsShot() == 1 : "Should track bullets shot";
        assert stats.getAsteroidsDestroyed() == 1 : "Should track asteroids destroyed";
        assert stats.getPowerUpsCollected() == 1 : "Should track power-ups collected";
        assert stats.getHitsTaken() == 1 : "Should track hits taken";
        assert !stats.isPerfectWave() : "Should not be perfect after damage";
        
        System.out.println("✓ Statistics tracking test passed");
    }

    private static void testSmoothedDifficultyCurve() {
        System.out.println("Testing smoothed difficulty curve...");
        
        WaveSystem system = new WaveSystem();
        double lastDiff = system.getDifficultyMultiplier();
        double lastDelta = 0;
        
        // Check that difficulty increases follow a smoothed curve
        for (int i = 2; i <= 10; i++) {
            system.startWave(i);
            double diff = system.getDifficultyMultiplier();
            double delta = diff - lastDiff;
            
            if (i > 2) {
                // Delta should decrease as we progress (smoothed curve)
                assert delta <= lastDelta + DELTA : 
                    "Difficulty curve should smooth out over time";
            }
            
            lastDiff = diff;
            lastDelta = delta;
        }
        
        assert system.getDifficultyMultiplier() <= GameConfig.Wave.MAX_DIFFICULTY :
            "Difficulty should not exceed maximum";
            
        System.out.println("✓ Smoothed difficulty curve test passed");
    }

    private static void testPerformanceHistory() {
        System.out.println("Testing performance history...");
        
        WaveSystem system = new WaveSystem();
        
        // Complete several waves with varying performance
        for (int wave = 1; wave <= 5; wave++) {
            system.startWave(wave);
            
            // Alternate between good and poor performance
            if (wave % 2 == 0) {
                // Good performance
                for (int i = 0; i < 5; i++) {
                    system.recordBulletShot();
                    system.asteroidDestroyed();
                }
            } else {
                // Poor performance
                for (int i = 0; i < 5; i++) {
                    system.recordBulletShot();
                    system.playerDamaged();
                }
            }
            
            // Complete wave
            while (system.getAsteroidsRemaining() > 0) {
                system.asteroidDestroyed();
            }
        }
        
        List<WaveStats> recent = system.getRecentWaveStats();
        assert recent.size() <= GameConfig.Wave.PERFORMANCE_HISTORY_SIZE :
            "Recent stats should be limited to history size";
            
        List<WaveStats> historical = system.getHistoricalWaveStats();
        assert historical.size() == 5 : "Should have stats for all completed waves";
        
        // Verify that stats are immutable
        try {
            historical.add(new WaveStats(6, false));
            assert false : "Historical stats should be unmodifiable";
        } catch (UnsupportedOperationException e) {
            // Expected
        }
        
        System.out.println("✓ Performance history test passed");
    }
}
