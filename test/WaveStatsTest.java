/**
 * Test suite for WaveStats metrics tracking.
 */
public class WaveStatsTest {
    private static final double DELTA = 0.001;  // For floating point comparisons

    public static void main(String[] args) {
        testBasicMetrics();
        testMetricCalculations();
        testBossWaveScoring();
        testPerfectWaveTracking();
        testTimedMetrics();
        System.out.println("All WaveStats tests passed! ✅");
    }

    private static void testBasicMetrics() {
        System.out.println("Testing basic metrics recording...");
        
        WaveStats stats = new WaveStats(1, false);
        assert stats.getWaveNumber() == 1 : "Wave number should be 1";
        assert !stats.isBossWave() : "Should not be a boss wave";
        assert stats.isPerfectWave() : "Should start as perfect wave";
        
        stats.recordBulletShot();
        assert stats.getBulletsShot() == 1 : "Should record one bullet shot";
        
        stats.recordAsteroidDestroyed();
        assert stats.getAsteroidsDestroyed() == 1 : "Should record one asteroid destroyed";
        
        stats.recordPowerUpCollected();
        assert stats.getPowerUpsCollected() == 1 : "Should record one power-up collected";
        
        System.out.println("✓ Basic metrics test passed");
    }

    private static void testMetricCalculations() {
        System.out.println("Testing metric calculations...");
        
        WaveStats stats = new WaveStats(1, false);
        
        // Perfect accuracy case
        stats.recordBulletShot();
        stats.recordAsteroidDestroyed();
        assert Math.abs(stats.getAccuracy() - 1.0) < DELTA : 
            "Accuracy should be 1.0 for perfect hits";
            
        // Test accuracy with misses
        stats.recordBulletShot();
        stats.recordBulletShot();
        assert Math.abs(stats.getAccuracy() - 0.333) < 0.01 : 
            "Accuracy should be ~0.333 for 1/3 hits";
            
        System.out.println("✓ Metric calculations test passed");
    }

    private static void testBossWaveScoring() {
        System.out.println("Testing boss wave scoring...");
        
        WaveStats normalStats = new WaveStats(1, false);
        WaveStats bossStats = new WaveStats(5, true);
        
        // Record identical performance
        for (WaveStats stats : new WaveStats[]{normalStats, bossStats}) {
            stats.recordBulletShot();
            stats.recordAsteroidDestroyed();
            stats.recordPowerUpCollected();
        }
        
        assert bossStats.getWavePerformance() > normalStats.getWavePerformance() :
            "Boss wave performance should be higher for identical actions";
            
        System.out.println("✓ Boss wave scoring test passed");
    }

    private static void testPerfectWaveTracking() {
        System.out.println("Testing perfect wave tracking...");
        
        WaveStats stats = new WaveStats(1, false);
        assert stats.isPerfectWave() : "Should start as perfect wave";
        
        stats.recordBulletShot();
        stats.recordAsteroidDestroyed();
        assert stats.isPerfectWave() : "Should remain perfect with no hits";
        
        stats.recordHit();
        assert !stats.isPerfectWave() : "Should not be perfect after taking damage";
        
        System.out.println("✓ Perfect wave tracking test passed");
    }

    private static void testTimedMetrics() {
        System.out.println("Testing timed metrics...");
        
        WaveStats stats = new WaveStats(1, false);
        long startTime = stats.getStartTime();
        
        try {
            Thread.sleep(100); // Small delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long waveTime = stats.getWaveTime();
        assert waveTime >= 100 : 
            String.format("Wave time should be ≥100ms, got %d", waveTime);
            
        // Test power-up efficiency (collections per minute)
        stats.recordPowerUpCollected();
        stats.recordPowerUpCollected();
        
        double efficiency = stats.getPowerUpEfficiency();
        assert efficiency > 0 : "Power-up efficiency should be positive";
        
        System.out.println("✓ Timed metrics test passed");
    }
}
