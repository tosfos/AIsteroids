import java.io.File;

public class LeaderboardSystemTest {

    private static final String TEST_LEADERBOARD = "test_leaderboard_tmp.txt";
    private static final String TEST_ACHIEVEMENTS = "test_achievements_tmp.txt";

    public static void main(String[] args) {
        LeaderboardSystemTest test = new LeaderboardSystemTest();
        test.runAllTests();
    }

    public void runAllTests() {
        System.out.println("Running LeaderboardSystem Tests...");

        setupTestFiles();
        try {
            testAchievementTracking();
            testAchievementNameCheck();
            testScoreTracking();
            testStatisticsTracking();
            testIsolatedPersistence();
        } finally {
            teardownTestFiles();
        }

        System.out.println("All LeaderboardSystem tests passed! ✅");
    }

    private void setupTestFiles() {
        new File(TEST_LEADERBOARD).delete();
        new File(TEST_ACHIEVEMENTS).delete();
        LeaderboardSystem.setDataFilesForTesting(TEST_LEADERBOARD, TEST_ACHIEVEMENTS);
    }

    private void teardownTestFiles() {
        LeaderboardSystem.resetDataFilesForTesting();
        new File(TEST_LEADERBOARD).delete();
        new File(TEST_ACHIEVEMENTS).delete();
    }

    public void testAchievementTracking() {
        LeaderboardSystem.Achievement[] achievements = LeaderboardSystem.Achievement.values();
        assert achievements.length == 14 : "Should have 14 achievements defined";

        for (LeaderboardSystem.Achievement achievement : achievements) {
            assert achievement.getName() != null : "Achievement should have a name";
            assert achievement.getDescription() != null : "Achievement should have a description";
            assert achievement.getRequirement() > 0 : "Achievement should have positive requirement";
        }

        System.out.println("✓ Achievement tracking tests passed");
    }

    public void testAchievementNameCheck() {
        LeaderboardSystem.resetStats();

        // FIRST_KILL should unlock using achievement.name() not getName()
        LeaderboardSystem.asteroidDestroyed();
        assert LeaderboardSystem.isAchievementUnlocked(LeaderboardSystem.Achievement.FIRST_KILL)
            : "FIRST_KILL should unlock after destroying one asteroid";

        // Verify storage uses enum constant name
        assert new File(TEST_ACHIEVEMENTS).exists() : "Achievements file should be created";
        String contents = readFile(TEST_ACHIEVEMENTS);
        assert contents.contains("FIRST_KILL")
            : "Achievement storage should use enum name(), not display name";
        assert !contents.contains("First Blood")
            : "Achievement storage should not use display name";

        System.out.println("✓ Achievement name() check tests passed");
    }

    public void testScoreTracking() {
        LeaderboardSystem.addScore("TestPlayer", 1000, 5);

        var leaderboard = LeaderboardSystem.getLeaderboard();
        assert leaderboard != null : "Leaderboard should not be null";
        assert !leaderboard.isEmpty() : "Leaderboard should contain test entry";

        assert LeaderboardSystem.getGameStat("bullets_fired") >= 0 : "Bullets fired should be non-negative";
        assert LeaderboardSystem.getGameStat("asteroids_destroyed") >= 0 : "Asteroids destroyed should be non-negative";
        assert LeaderboardSystem.getGameStat("waves_completed") >= 0 : "Waves completed should be non-negative";

        System.out.println("✓ Score tracking tests passed");
    }

    public void testStatisticsTracking() {
        LeaderboardSystem.resetStats();

        int initialBullets = LeaderboardSystem.getGameStat("bullets_fired");
        int initialAsteroids = LeaderboardSystem.getGameStat("asteroids_destroyed");

        LeaderboardSystem.bulletFired();
        assert LeaderboardSystem.getGameStat("bullets_fired") == initialBullets + 1 : "Bullet count should increment";

        LeaderboardSystem.asteroidDestroyed();
        assert LeaderboardSystem.getGameStat("asteroids_destroyed") == initialAsteroids + 1 : "Asteroid count should increment";

        var unlockedAchievements = LeaderboardSystem.getUnlockedAchievements();
        assert unlockedAchievements != null : "Unlocked achievements should not be null";

        var lockedAchievements = LeaderboardSystem.getLockedAchievements();
        assert lockedAchievements != null : "Locked achievements should not be null";

        System.out.println("✓ Statistics tracking tests passed");
    }

    public void testIsolatedPersistence() {
        // Verify test files are used, not production files
        assert new File(TEST_LEADERBOARD).exists() || new File(TEST_ACHIEVEMENTS).exists()
            : "Test should write to isolated temp files";

        System.out.println("✓ Isolated persistence tests passed");
    }

    private String readFile(String path) {
        StringBuilder sb = new StringBuilder();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (java.io.IOException e) {
            return "";
        }
        return sb.toString();
    }
}
