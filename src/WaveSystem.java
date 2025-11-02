import java.util.Random;
import java.util.Queue;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * Manages wave progression, adaptive difficulty, and wave-based statistics.
 *
 * <p>The wave system controls:
 * <ul>
 *   <li>Wave progression and numbering</li>
 *   <li>Boss wave spawning (every 5th wave)</li>
 *   <li>Adaptive difficulty based on player performance</li>
 *   <li>Asteroid spawning patterns and counts</li>
 *   <li>Power-up spawn chance calculations</li>
 *   <li>Score multipliers based on wave number</li>
 *   <li>Wave completion bonuses and rewards</li>
 * </ul>
 * </p>
 *
 * <p>The system uses a logistic curve for difficulty scaling and tracks
 * recent wave performance to adjust difficulty dynamically.</p>
 *
 * @author AIsteroids Development Team
 * @version 1.0
 */
public class WaveSystem {
    private int currentWave = 1;
    private int asteroidsRemaining = 0;
    private boolean waveInProgress = false;
    private boolean bossWave = false;
    private double difficultyMultiplier = 1.0;
    private int scoreMultiplier = 1;
    private Random rand = new Random();

    // Current wave stats
    private WaveStats currentStats;
    private Queue<WaveStats> recentStats;
    private List<WaveStats> historicalStats;

    public WaveSystem() {
        recentStats = new LinkedList<>();
        historicalStats = new ArrayList<>();
        startWave(1);
    }

    public void startWave(int waveNumber) {
        currentWave = waveNumber;
        waveInProgress = true;
        bossWave = (waveNumber % GameConfig.Wave.BOSS_WAVE_INTERVAL == 0);

        // Create new wave stats
        currentStats = new WaveStats(waveNumber, bossWave);

        // Calculate base difficulty using a smoothed logistic curve
        double x = (waveNumber - 1) * GameConfig.Wave.DIFFICULTY_SMOOTHING_FACTOR;
        double baseDifficulty = GameConfig.Wave.BASE_DIFFICULTY +
            (GameConfig.Wave.MAX_DIFFICULTY - GameConfig.Wave.BASE_DIFFICULTY) *
            (1.0 / (1.0 + Math.exp(-x)));

        // Apply performance-based adjustment and clamp final difficulty within allowed range
        double performanceModifier = calculatePerformanceModifier();
        double adjusted = baseDifficulty * performanceModifier;
        difficultyMultiplier = Math.max(GameConfig.Wave.BASE_DIFFICULTY,
            Math.min(GameConfig.Wave.MAX_DIFFICULTY, adjusted));

        // Calculate score multiplier (capped but not affected by performance)
        scoreMultiplier = Math.min(GameConfig.Wave.SCORE_MULTIPLIER_MAX,
            1 + (waveNumber - 1) / 3);

        // Calculate asteroids for this wave
        if (bossWave) {
            asteroidsRemaining = calculateBossWaveAsteroids();
        } else {
            asteroidsRemaining = calculateNormalWaveAsteroids();
        }
    }

    private int calculateNormalWaveAsteroids() {
        // Base asteroids increase each wave
        return Math.min(GameConfig.Wave.MAX_NORMAL_ASTEROIDS,
                        GameConfig.Wave.BASE_ASTEROIDS + (currentWave - 1) * GameConfig.Wave.ASTEROID_INCREMENT);
    }

    private int calculateBossWaveAsteroids() {
        // Boss waves have fewer but larger asteroids
        return Math.min(GameConfig.Wave.MAX_BOSS_ASTEROIDS,
                        GameConfig.Wave.BASE_BOSS_ASTEROIDS + currentWave / 2);
    }

    public void asteroidDestroyed() {
        asteroidsRemaining--;
        if (asteroidsRemaining <= 0 && waveInProgress) {
            completeWave();
        }
    }

    private void completeWave() {
        waveInProgress = false;

        // Record final stats
        long waveTime = currentStats.getWaveTime();
        LeaderboardSystem.waveCompleted(currentWave, currentStats.isPerfectWave(),
            bossWave, waveTime);

        // Wave completion bonus calculation is available via getWaveCompletionBonus()
        // if needed by the caller. Scoring is handled by ScoreCalculator and applied
        // in GameEngine when processing wave completion.

        // Update stats history
        recentStats.offer(currentStats);
        historicalStats.add(currentStats);
        while (recentStats.size() > GameConfig.Wave.PERFORMANCE_HISTORY_SIZE) {
            recentStats.poll();
        }

        // Start next wave
        startWave(currentWave + 1);
    }

    public void playerDamaged() {
        if (currentStats != null) {
            currentStats.recordHit();
        }
    }

    public void recordBulletShot() {
        if (currentStats != null) {
            currentStats.recordBulletShot();
        }
    }

    public void recordPowerUpCollected() {
        if (currentStats != null) {
            currentStats.recordPowerUpCollected();
        }
    }

    public AsteroidSpawnInfo getSpawnInfo() {
        AsteroidSpawnInfo info = new AsteroidSpawnInfo();

        if (bossWave) {
            // Boss waves spawn larger, more dangerous asteroids
            info.size = 3; // Always large
            info.speed = GameConfig.Wave.BOSS_BASE_SPEED + (difficultyMultiplier * GameConfig.Wave.BOSS_SPEED_MULTIPLIER);
            info.splitCount = 3; // Split into 3 instead of 2
        } else {
            // Normal waves have mixed asteroid sizes
            if (currentWave <= 3) {
                info.size = rand.nextBoolean() ? 3 : 2; // Large or medium
            } else {
                // Later waves can spawn any size
                info.size = 1 + rand.nextInt(3);
            }

            info.speed = (GameConfig.Wave.NORMAL_BASE_SPEED + currentWave * GameConfig.Wave.NORMAL_SPEED_INCREMENT)
                         + (rand.nextDouble() * GameConfig.Wave.NORMAL_SPEED_VARIATION);
            info.splitCount = 2;
        }

        // Apply difficulty multiplier
        info.speed *= difficultyMultiplier;

        return info;
    }

    public PowerUpSpawnInfo getPowerUpSpawnInfo() {
        PowerUpSpawnInfo info = new PowerUpSpawnInfo();

        // Higher waves spawn power-ups more frequently
        info.spawnChance = Math.min(GameConfig.Wave.POWER_UP_MAX_CHANCE,
                                    GameConfig.Wave.POWER_UP_BASE_CHANCE + (currentWave * GameConfig.Wave.POWER_UP_CHANCE_INCREMENT));

        // Boss waves guarantee power-up spawns
        if (bossWave) {
            info.spawnChance = GameConfig.Wave.POWER_UP_BOSS_CHANCE;
        }

        // Later waves favor more powerful power-ups
        if (currentWave >= GameConfig.Wave.POWER_UP_RARE_WAVE) {
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
        return currentStats != null && currentStats.isPerfectWave();
    }

    public long getWaveTime() {
        return currentStats != null ? currentStats.getWaveTime() : 0;
    }

    public int getWaveCompletionBonus() {
        int bonus = GameConfig.Scoring.WAVE_COMPLETION_BONUS * scoreMultiplier;
        if (isPerfectWave()) bonus *= 2;
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

        // Clear stats
        currentStats = null;
        recentStats.clear();
        historicalStats.clear();

        startWave(1);
    }

    /**
     * Calculates a performance-based difficulty modifier based on recent wave stats.
     */
    private double calculatePerformanceModifier() {
        if (recentStats.isEmpty()) {
            return 1.0; // No adjustment for first wave
        }

        // Calculate average performance over recent waves
        double totalPerformance = 0.0;
        int count = 0;
        for (WaveStats stats : recentStats) {
            totalPerformance += stats.getWavePerformance();
            count++;
        }
        double avgPerformance = totalPerformance / count;

        // Convert to difficulty modifier
        // Performance > 0.5 increases difficulty, < 0.5 decreases it
        double performanceDelta = (avgPerformance - 0.5) * GameConfig.Wave.PERFORMANCE_SCALE_FACTOR;
        return 1.0 + performanceDelta;
    }

    /**
     * Gets the stats for the current wave.
     */
    public WaveStats getCurrentWaveStats() {
        return currentStats;
    }

    /**
     * Gets a list of recent wave stats.
     */
    public List<WaveStats> getRecentWaveStats() {
        return new ArrayList<>(recentStats);
    }

    /**
     * Gets all historical wave stats.
     */
    public List<WaveStats> getHistoricalWaveStats() {
        return Collections.unmodifiableList(historicalStats);
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
