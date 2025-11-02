/**
 * Calculates scores for various game events and actions.
 * 
 * <p>This class provides static methods for computing scores based on game
 * events like asteroid destruction, wave completion, and power-up collection.
 * It handles multipliers, bonuses, and scoring rules.</p>
 * 
 * <p>Scoring factors:
 * <ul>
 *   <li>Asteroid size (small/medium/large)</li>
 *   <li>Wave progression multipliers</li>
 *   <li>Perfect wave bonuses</li>
 *   <li>Speed completion bonuses</li>
 *   <li>Boss wave multipliers</li>
 * </ul>
 * </p>
 * 
 * @author AIsteroids Development Team
 * @version 1.0
 */
public class ScoreCalculator {

    public static class ScoreResult {
        public final int baseScore;
        public final int multiplierBonus;
        public final int totalScore;
        public final String reason;

        public ScoreResult(int baseScore, int multiplierBonus, int totalScore, String reason) {
            this.baseScore = baseScore;
            this.multiplierBonus = multiplierBonus;
            this.totalScore = totalScore;
            this.reason = reason;
        }
    }

    // Base scoring constants
    public static final int SMALL_ASTEROID_SCORE = 20;
    public static final int MEDIUM_ASTEROID_SCORE = 50;
    public static final int LARGE_ASTEROID_SCORE = 100;
    public static final int POWER_UP_SCORE = 25;
    public static final int WAVE_COMPLETION_BASE = 500;
    public static final int PERFECT_WAVE_BONUS = 1000;
    public static final int SPEED_BONUS_THRESHOLD = 20; // seconds
    public static final int SPEED_BONUS_AMOUNT = 500;

    public ScoreResult calculateAsteroidScore(double asteroidRadius, int currentWave, double waveMultiplier) {
        int baseScore = getAsteroidBaseScore(asteroidRadius);
        int multiplier = Math.max(1, currentWave / 2); // Wave-based multiplier
        int bonus = (int)(baseScore * (waveMultiplier - 1.0));
        int total = baseScore * multiplier + bonus;

        String reason = String.format("Asteroid destroyed (wave %d, %.1fx multiplier)", currentWave, waveMultiplier);
        return new ScoreResult(baseScore, bonus, total, reason);
    }

    public ScoreResult calculateWaveCompletionScore(int waveNumber, boolean isPerfect, double completionTime) {
        int baseScore = WAVE_COMPLETION_BASE * waveNumber;
        int bonus = 0;
        String bonusReason = "";

        if (isPerfect) {
            bonus += PERFECT_WAVE_BONUS;
            bonusReason += "Perfect wave! ";
        }

        if (completionTime < SPEED_BONUS_THRESHOLD) {
            bonus += SPEED_BONUS_AMOUNT;
            bonusReason += "Speed bonus! ";
        }

        int total = baseScore + bonus;
        String reason = String.format("Wave %d completed. %s", waveNumber, bonusReason);
        return new ScoreResult(baseScore, bonus, total, reason);
    }

    public ScoreResult calculatePowerUpScore(int currentWave) {
        int baseScore = POWER_UP_SCORE;
        int multiplier = Math.max(1, currentWave / 3);
        int total = baseScore * multiplier;

        String reason = String.format("Power-up collected (wave %d)", currentWave);
        return new ScoreResult(baseScore, 0, total, reason);
    }

    private int getAsteroidBaseScore(double radius) {
        if (radius <= 15) return SMALL_ASTEROID_SCORE;
        if (radius <= 25) return MEDIUM_ASTEROID_SCORE;
        return LARGE_ASTEROID_SCORE;
    }

    public double calculateScoreMultiplier(int currentWave, int powerUpCount, boolean hasShield) {
        double multiplier = 1.0;

        // Wave progression multiplier
        multiplier += (currentWave - 1) * 0.1;

        // Power-up bonus
        multiplier += powerUpCount * 0.05;

        // Shield bonus
        if (hasShield) {
            multiplier += 0.2;
        }

        return Math.min(multiplier, 5.0); // Cap at 5x
    }
}
