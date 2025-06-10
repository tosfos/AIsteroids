import java.util.Random;

public class WaveSystem {
    private int currentWave = 1;
    private int asteroidsRemaining = 0;
    private boolean waveInProgress = false;
    private boolean bossWave = false;
    private double difficultyMultiplier = 1.0;
    private int scoreMultiplier = 1;
    private long waveStartTime = 0;
    private Random rand = new Random();

    // Wave completion bonuses
    private int waveCompletionBonus = 1000;
    private boolean perfectWave = true; // No damage taken this wave

    public WaveSystem() {
        startWave(1);
    }

    public void startWave(int waveNumber) {
        currentWave = waveNumber;
        waveInProgress = true;
        waveStartTime = System.currentTimeMillis();
        perfectWave = true;

        // Determine if this is a boss wave (every 5th wave)
        bossWave = (waveNumber % 5 == 0);

        // Calculate difficulty scaling
        difficultyMultiplier = 1.0 + (waveNumber - 1) * 0.3;
        scoreMultiplier = Math.min(10, 1 + (waveNumber - 1) / 3);

        // Calculate asteroids for this wave
        if (bossWave) {
            asteroidsRemaining = calculateBossWaveAsteroids();
        } else {
            asteroidsRemaining = calculateNormalWaveAsteroids();
        }
    }

    private int calculateNormalWaveAsteroids() {
        // Base: 5 asteroids, increases by 2 every wave, max 25
        return Math.min(25, 5 + (currentWave - 1) * 2);
    }

    private int calculateBossWaveAsteroids() {
        // Boss waves have fewer but larger asteroids
        return Math.min(15, 3 + currentWave / 2);
    }

    public void asteroidDestroyed() {
        asteroidsRemaining--;
        if (asteroidsRemaining <= 0 && waveInProgress) {
            completeWave();
        }
    }

    private void completeWave() {
        waveInProgress = false;
        long waveTime = System.currentTimeMillis() - waveStartTime;

        // Calculate wave completion bonus
        int bonus = waveCompletionBonus * scoreMultiplier;

        // Perfect wave bonus (no damage taken)
        if (perfectWave) {
            bonus *= 2;
        }

        // Speed bonus (completed quickly)
        if (waveTime < 30000) { // Under 30 seconds
            bonus += 500 * scoreMultiplier;
        }

        // Boss wave bonus
        if (bossWave) {
            bonus *= 3;
        }

        // Start next wave after brief pause
        startWave(currentWave + 1);
    }

    public void playerDamaged() {
        perfectWave = false;
    }

    public AsteroidSpawnInfo getSpawnInfo() {
        AsteroidSpawnInfo info = new AsteroidSpawnInfo();

        if (bossWave) {
            // Boss waves spawn larger, more dangerous asteroids
            info.size = 3; // Always large
            info.speed = 30 + (difficultyMultiplier * 20);
            info.splitCount = 3; // Split into 3 instead of 2
        } else {
            // Normal waves have mixed asteroid sizes
            if (currentWave <= 3) {
                info.size = rand.nextBoolean() ? 3 : 2; // Large or medium
            } else {
                // Later waves can spawn any size
                info.size = 1 + rand.nextInt(3);
            }

            info.speed = (20 + currentWave * 5) + (rand.nextDouble() * 30);
            info.splitCount = 2;
        }

        // Apply difficulty multiplier
        info.speed *= difficultyMultiplier;

        return info;
    }

    public PowerUpSpawnInfo getPowerUpSpawnInfo() {
        PowerUpSpawnInfo info = new PowerUpSpawnInfo();

        // Higher waves spawn power-ups more frequently
        info.spawnChance = Math.min(0.4, 0.1 + (currentWave * 0.03));

        // Boss waves guarantee power-up spawns
        if (bossWave) {
            info.spawnChance = 0.8;
        }

        // Later waves favor more powerful power-ups
        if (currentWave >= 10) {
            info.favorRare = true;
        }

        return info;
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public int getAsteroidsRemaining() {
        return asteroidsRemaining;
    }

    public boolean isWaveInProgress() {
        return waveInProgress;
    }

    public boolean isBossWave() {
        return bossWave;
    }

    public int getScoreMultiplier() {
        return scoreMultiplier;
    }

    public double getDifficultyMultiplier() {
        return difficultyMultiplier;
    }

    public boolean isPerfectWave() {
        return perfectWave;
    }

    public long getWaveTime() {
        if (waveInProgress) {
            return System.currentTimeMillis() - waveStartTime;
        }
        return 0;
    }

    public int getWaveCompletionBonus() {
        int bonus = waveCompletionBonus * scoreMultiplier;
        if (perfectWave) bonus *= 2;
        if (bossWave) bonus *= 3;
        return bonus;
    }

    public void reset() {
        currentWave = 1;
        asteroidsRemaining = 0;
        waveInProgress = false;
        bossWave = false;
        difficultyMultiplier = 1.0;
        scoreMultiplier = 1;
        perfectWave = true;
        startWave(1);
    }

    // Helper classes for spawn information
    public static class AsteroidSpawnInfo {
        public int size = 3;
        public double speed = 50;
        public int splitCount = 2;
    }

    public static class PowerUpSpawnInfo {
        public double spawnChance = 0.2;
        public boolean favorRare = false;
    }
}
