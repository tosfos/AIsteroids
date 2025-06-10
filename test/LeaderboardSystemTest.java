public class LeaderboardSystemTest {

    public static void main(String[] args) {
        LeaderboardSystemTest test = new LeaderboardSystemTest();
        test.runAllTests();
    }

    public void runAllTests() {
        System.out.println("Running LeaderboardSystem Tests...");

        testAchievementTracking();
        testScoreTracking();
        testStatisticsTracking();

        System.out.println("All LeaderboardSystem tests passed! ✅");
    }

    public void testAchievementTracking() {
        // Test achievement definitions
        LeaderboardSystem.Achievement[] achievements = LeaderboardSystem.Achievement.values();
        assert achievements.length == 14 : "Should have 14 achievements defined";

        // Test each achievement has valid properties
        for (LeaderboardSystem.Achievement achievement : achievements) {
            assert achievement.getName() != null : "Achievement should have a name";
            assert achievement.getDescription() != null : "Achievement should have a description";
            assert achievement.getRequirement() > 0 : "Achievement should have positive requirement";
        }

        System.out.println("✓ Achievement tracking tests passed");
    }

        public void testScoreTracking() {
        // Test score addition
        LeaderboardSystem.addScore("TestPlayer", 1000, 5);

        // Test leaderboard retrieval
        var leaderboard = LeaderboardSystem.getLeaderboard();
        assert leaderboard != null : "Leaderboard should not be null";

        // Test statistics exist
        assert LeaderboardSystem.getGameStat("bullets_fired") >= 0 : "Bullets fired should be non-negative";
        assert LeaderboardSystem.getGameStat("asteroids_destroyed") >= 0 : "Asteroids destroyed should be non-negative";
        assert LeaderboardSystem.getGameStat("waves_completed") >= 0 : "Waves completed should be non-negative";

        System.out.println("✓ Score tracking tests passed");
    }

    public void testStatisticsTracking() {
        LeaderboardSystem.resetStats();

        int initialBullets = LeaderboardSystem.getGameStat("bullets_fired");
        int initialAsteroids = LeaderboardSystem.getGameStat("asteroids_destroyed");

        // Test statistic increment methods
        LeaderboardSystem.bulletFired();
        assert LeaderboardSystem.getGameStat("bullets_fired") == initialBullets + 1 : "Bullet count should increment";

        LeaderboardSystem.asteroidDestroyed();
        assert LeaderboardSystem.getGameStat("asteroids_destroyed") == initialAsteroids + 1 : "Asteroid count should increment";

        // Test achievement tracking
        var unlockedAchievements = LeaderboardSystem.getUnlockedAchievements();
        assert unlockedAchievements != null : "Unlocked achievements should not be null";

        var lockedAchievements = LeaderboardSystem.getLockedAchievements();
        assert lockedAchievements != null : "Locked achievements should not be null";

        System.out.println("✓ Statistics tracking tests passed");
    }
}
