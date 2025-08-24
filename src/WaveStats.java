/**
 * Tracks performance metrics for each wave.
 * Used for adaptive difficulty adjustment and telemetry.
 */
public class WaveStats {
    private int waveNumber;
    private long startTime;
    private int hitsTaken;
    private int bulletsShot;
    private int asteroidsDestroyed;
    private int powerUpsCollected;
    private boolean isBossWave;
    private boolean isPerfectWave;
    
    // Cache derived metrics
    private double accuracy;
    private double survivalScore;
    private double powerUpEfficiency;
    private double wavePerformance;
    
    public WaveStats(int waveNumber, boolean isBossWave) {
        this.waveNumber = waveNumber;
        this.isBossWave = isBossWave;
        this.startTime = System.currentTimeMillis();
        this.isPerfectWave = true; // Start perfect until hit
    }
    
    public void recordHit() {
        hitsTaken++;
        isPerfectWave = false;
        recalculateMetrics();
    }
    
    public void recordBulletShot() {
        bulletsShot++;
        recalculateMetrics();
    }
    
    public void recordAsteroidDestroyed() {
        asteroidsDestroyed++;
        recalculateMetrics();
    }
    
    public void recordPowerUpCollected() {
        powerUpsCollected++;
        recalculateMetrics();
    }
    
    private void recalculateMetrics() {
        // Calculate accuracy (avoid divide by zero)
        accuracy = bulletsShot > 0 ? (double) asteroidsDestroyed / bulletsShot : 0.0;
        
        // Calculate survival score (inverse of hits taken, normalized)
        double timeSurvived = (System.currentTimeMillis() - startTime) / 1000.0;
        survivalScore = Math.max(0.0, 1.0 - (hitsTaken / Math.max(timeSurvived, 1.0)));
        
        // Calculate power-up efficiency (collection rate per minute)
        powerUpEfficiency = timeSurvived > 0 ? 
            (powerUpsCollected * 60.0) / timeSurvived : 0.0;
        
        // Overall wave performance (weighted average)
        double accuracyWeight = 0.4;
        double survivalWeight = 0.4;
        double powerUpWeight = 0.2;
        
        wavePerformance = (accuracy * accuracyWeight) +
                         (survivalScore * survivalWeight) +
                         (Math.min(1.0, powerUpEfficiency / 10.0) * powerUpWeight);
                         
        // Boss waves are weighted more heavily
        if (isBossWave) {
            wavePerformance *= 1.5;
        }
    }
    
    // Getters
    public double getAccuracy() { return accuracy; }
    public double getSurvivalScore() { return survivalScore; }
    public double getPowerUpEfficiency() { return powerUpEfficiency; }
    public double getWavePerformance() { return wavePerformance; }
    public int getWaveNumber() { return waveNumber; }
    public int getHitsTaken() { return hitsTaken; }
    public int getBulletsShot() { return bulletsShot; }
    public int getAsteroidsDestroyed() { return asteroidsDestroyed; }
    public int getPowerUpsCollected() { return powerUpsCollected; }
    public boolean isPerfectWave() { return isPerfectWave; }
    public boolean isBossWave() { return isBossWave; }
    public long getStartTime() { return startTime; }
    public long getWaveTime() { return System.currentTimeMillis() - startTime; }
    
    @Override
    public String toString() {
        return String.format(
            "Wave %d Stats [%s] - Time: %.1fs, Accuracy: %.1f%%, Survival: %.1f%%, Performance: %.1f%%",
            waveNumber,
            isBossWave ? "BOSS" : "Normal",
            getWaveTime() / 1000.0,
            accuracy * 100,
            survivalScore * 100,
            wavePerformance * 100
        );
    }
}
