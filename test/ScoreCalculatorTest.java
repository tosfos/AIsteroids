public class ScoreCalculatorTest {

    public static void main(String[] args) {
        ScoreCalculatorTest test = new ScoreCalculatorTest();
        test.runAllTests();
    }

    public void runAllTests() {
        System.out.println("Running ScoreCalculator Tests...");

        testAsteroidScoring();
        testWaveCompletionScoring();
        testPowerUpScoring();
        testScoreMultiplier();
        testPerfectWaveBonus();
        testSpeedBonus();

        System.out.println("All ScoreCalculator tests passed! ✅");
    }

    public void testAsteroidScoring() {
        ScoreCalculator calculator = new ScoreCalculator();

        // Test small asteroid
        ScoreCalculator.ScoreResult result = calculator.calculateAsteroidScore(10, 1, 1.0);
        assert result.baseScore == 20 : "Small asteroid base score should be 20";
        assert result.totalScore == 20 : "Small asteroid total score should be 20 at wave 1";

        // Test medium asteroid with wave multiplier
        result = calculator.calculateAsteroidScore(20, 3, 1.5);
        assert result.baseScore == 50 : "Medium asteroid base score should be 50";
        assert result.totalScore > 50 : "Medium asteroid should have bonus at wave 3";

        // Test large asteroid
        result = calculator.calculateAsteroidScore(30, 2, 1.0);
        assert result.baseScore == 100 : "Large asteroid base score should be 100";

        System.out.println("✓ Asteroid scoring tests passed");
    }

    public void testWaveCompletionScoring() {
        ScoreCalculator calculator = new ScoreCalculator();

        // Test normal wave completion
        ScoreCalculator.ScoreResult result = calculator.calculateWaveCompletionScore(3, false, 30);
        assert result.baseScore == 1500 : "Wave 3 base score should be 1500";
        assert result.multiplierBonus == 0 : "No bonus for normal completion";

        // Test perfect wave
        result = calculator.calculateWaveCompletionScore(2, true, 25);
        assert result.multiplierBonus == 1000 : "Perfect wave should give 1000 bonus";

        System.out.println("✓ Wave completion scoring tests passed");
    }

    public void testPowerUpScoring() {
        ScoreCalculator calculator = new ScoreCalculator();

        // Test early wave power-up
        ScoreCalculator.ScoreResult result = calculator.calculatePowerUpScore(1);
        assert result.totalScore == 25 : "Wave 1 power-up should be 25 points";

        // Test later wave power-up
        result = calculator.calculatePowerUpScore(6);
        assert result.totalScore == 50 : "Wave 6 power-up should be 50 points (2x multiplier)";

        System.out.println("✓ Power-up scoring tests passed");
    }

    public void testScoreMultiplier() {
        ScoreCalculator calculator = new ScoreCalculator();

        // Test base multiplier
        double multiplier = calculator.calculateScoreMultiplier(1, 0, false);
        assert multiplier == 1.0 : "Base multiplier should be 1.0";

        // Test wave progression
        multiplier = calculator.calculateScoreMultiplier(3, 2, true);
        assert multiplier > 1.0 : "Multiplier should increase with wave, power-ups, and shield";
        assert multiplier <= 5.0 : "Multiplier should be capped at 5.0";

        System.out.println("✓ Score multiplier tests passed");
    }

    public void testPerfectWaveBonus() {
        ScoreCalculator calculator = new ScoreCalculator();

        ScoreCalculator.ScoreResult normal = calculator.calculateWaveCompletionScore(1, false, 30);
        ScoreCalculator.ScoreResult perfect = calculator.calculateWaveCompletionScore(1, true, 30);

        assert perfect.totalScore > normal.totalScore : "Perfect wave should score higher";
        assert perfect.multiplierBonus == 1000 : "Perfect wave bonus should be 1000";

        System.out.println("✓ Perfect wave bonus tests passed");
    }

    public void testSpeedBonus() {
        ScoreCalculator calculator = new ScoreCalculator();

        ScoreCalculator.ScoreResult slow = calculator.calculateWaveCompletionScore(1, false, 25);
        ScoreCalculator.ScoreResult fast = calculator.calculateWaveCompletionScore(1, false, 15);

        assert fast.totalScore > slow.totalScore : "Fast completion should score higher";
        assert fast.multiplierBonus == 500 : "Speed bonus should be 500";

        System.out.println("✓ Speed bonus tests passed");
    }
}
