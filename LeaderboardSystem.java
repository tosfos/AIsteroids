import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class LeaderboardSystem {
    private static final String LEADERBOARD_FILE = "leaderboard.dat";
    private static final String ACHIEVEMENTS_FILE = "achievements.dat";
    private static final int MAX_LEADERBOARD_ENTRIES = 10;

    private static List<LeaderboardEntry> leaderboard = new ArrayList<>();
    private static Set<Achievement> unlockedAchievements = new HashSet<>();
    private static Map<String, Integer> gameStats = new HashMap<>();

    static {
        loadLeaderboard();
        loadAchievements();
        initializeStats();
    }

    public static class LeaderboardEntry implements Serializable {
        private static final long serialVersionUID = 1L;

        public String playerName;
        public int score;
        public int wave;
        public long timestamp;
        public String date;

        public LeaderboardEntry(String name, int score, int wave) {
            this.playerName = name;
            this.score = score;
            this.wave = wave;
            this.timestamp = System.currentTimeMillis();
            this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(timestamp));
        }
    }

    public enum Achievement {
        FIRST_KILL("First Blood", "Destroy your first asteroid", 1),
        WAVE_SURVIVOR("Wave Survivor", "Complete 5 waves", 5),
        BOSS_SLAYER("Boss Slayer", "Complete a boss wave", 1),
        POWER_COLLECTOR("Power Collector", "Collect 10 power-ups", 10),
        PERFECT_WARRIOR("Perfect Warrior", "Complete a wave without taking damage", 1),
        SPEED_DEMON("Speed Demon", "Complete a wave in under 20 seconds", 1),
        CENTURY_CLUB("Century Club", "Score 10,000 points", 10000),
        MILLENNIUM_MASTER("Millennium Master", "Score 100,000 points", 100000),
        ASTEROID_ANNIHILATOR("Asteroid Annihilator", "Destroy 100 asteroids", 100),
        WAVE_MASTER("Wave Master", "Reach wave 10", 10),
        LEGEND("Legend", "Reach wave 20", 20),
        RAPID_FIRE_EXPERT("Rapid Fire Expert", "Use rapid fire power-up 25 times", 25),
        SHIELD_MASTER("Shield Master", "Block 50 asteroid hits with shield", 50),
        MULTI_SHOT_MASTER("Multi-Shot Master", "Fire 1000 bullets in one game", 1000);

        private final String name;
        private final String description;
        private final int requirement;

        Achievement(String name, String description, int requirement) {
            this.name = name;
            this.description = description;
            this.requirement = requirement;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getRequirement() { return requirement; }
    }

    private static void initializeStats() {
        gameStats.putIfAbsent("asteroids_destroyed", 0);
        gameStats.putIfAbsent("waves_completed", 0);
        gameStats.putIfAbsent("boss_waves_completed", 0);
        gameStats.putIfAbsent("power_ups_collected", 0);
        gameStats.putIfAbsent("perfect_waves", 0);
        gameStats.putIfAbsent("total_score", 0);
        gameStats.putIfAbsent("bullets_fired", 0);
        gameStats.putIfAbsent("rapid_fire_uses", 0);
        gameStats.putIfAbsent("shield_blocks", 0);
        gameStats.putIfAbsent("highest_wave", 0);
        gameStats.putIfAbsent("games_played", 0);
    }

    public static void addScore(String playerName, int score, int wave) {
        LeaderboardEntry entry = new LeaderboardEntry(playerName, score, wave);
        leaderboard.add(entry);

        // Sort by score (descending)
        leaderboard.sort((a, b) -> Integer.compare(b.score, a.score));

        // Keep only top entries
        if (leaderboard.size() > MAX_LEADERBOARD_ENTRIES) {
            leaderboard = leaderboard.subList(0, MAX_LEADERBOARD_ENTRIES);
        }

        saveLeaderboard();
    }

    public static List<LeaderboardEntry> getLeaderboard() {
        return new ArrayList<>(leaderboard);
    }

    public static void updateGameStats(String statName, int value) {
        gameStats.put(statName, gameStats.getOrDefault(statName, 0) + value);
        checkAchievements();
    }

    public static void setGameStat(String statName, int value) {
        gameStats.put(statName, value);
        checkAchievements();
    }

    public static int getGameStat(String statName) {
        return gameStats.getOrDefault(statName, 0);
    }

    public static Map<String, Integer> getAllStats() {
        return new HashMap<>(gameStats);
    }

    private static void checkAchievements() {
        for (Achievement achievement : Achievement.values()) {
            if (!unlockedAchievements.contains(achievement)) {
                boolean unlocked = false;

                switch (achievement) {
                    case FIRST_KILL:
                        unlocked = getGameStat("asteroids_destroyed") >= 1;
                        break;
                    case WAVE_SURVIVOR:
                        unlocked = getGameStat("waves_completed") >= 5;
                        break;
                    case BOSS_SLAYER:
                        unlocked = getGameStat("boss_waves_completed") >= 1;
                        break;
                    case POWER_COLLECTOR:
                        unlocked = getGameStat("power_ups_collected") >= 10;
                        break;
                    case PERFECT_WARRIOR:
                        unlocked = getGameStat("perfect_waves") >= 1;
                        break;
                    case SPEED_DEMON:
                        // This needs to be checked externally when wave completed
                        break;
                    case CENTURY_CLUB:
                        unlocked = getGameStat("total_score") >= 10000;
                        break;
                    case MILLENNIUM_MASTER:
                        unlocked = getGameStat("total_score") >= 100000;
                        break;
                    case ASTEROID_ANNIHILATOR:
                        unlocked = getGameStat("asteroids_destroyed") >= 100;
                        break;
                    case WAVE_MASTER:
                        unlocked = getGameStat("highest_wave") >= 10;
                        break;
                    case LEGEND:
                        unlocked = getGameStat("highest_wave") >= 20;
                        break;
                    case RAPID_FIRE_EXPERT:
                        unlocked = getGameStat("rapid_fire_uses") >= 25;
                        break;
                    case SHIELD_MASTER:
                        unlocked = getGameStat("shield_blocks") >= 50;
                        break;
                    case MULTI_SHOT_MASTER:
                        unlocked = getGameStat("bullets_fired") >= 1000;
                        break;
                }

                if (unlocked) {
                    unlockAchievement(achievement);
                }
            }
        }
    }

    public static void unlockAchievement(Achievement achievement) {
        if (!unlockedAchievements.contains(achievement)) {
            unlockedAchievements.add(achievement);
            saveAchievements();
            // Achievement notification would be handled by UI
        }
    }

    public static boolean isAchievementUnlocked(Achievement achievement) {
        return unlockedAchievements.contains(achievement);
    }

    public static Set<Achievement> getUnlockedAchievements() {
        return new HashSet<>(unlockedAchievements);
    }

    public static List<Achievement> getLockedAchievements() {
        List<Achievement> locked = new ArrayList<>();
        for (Achievement achievement : Achievement.values()) {
            if (!unlockedAchievements.contains(achievement)) {
                locked.add(achievement);
            }
        }
        return locked;
    }

    private static void loadLeaderboard() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(LEADERBOARD_FILE))) {
            leaderboard = (List<LeaderboardEntry>) ois.readObject();
        } catch (Exception e) {
            leaderboard = new ArrayList<>();
        }
    }

    private static void saveLeaderboard() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(LEADERBOARD_FILE))) {
            oos.writeObject(leaderboard);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadAchievements() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ACHIEVEMENTS_FILE))) {
            unlockedAchievements = (Set<Achievement>) ois.readObject();
            gameStats = (Map<String, Integer>) ois.readObject();
        } catch (Exception e) {
            unlockedAchievements = new HashSet<>();
            gameStats = new HashMap<>();
        }
    }

    private static void saveAchievements() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ACHIEVEMENTS_FILE))) {
            oos.writeObject(unlockedAchievements);
            oos.writeObject(gameStats);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void gameStarted() {
        updateGameStats("games_played", 1);
    }

    public static void gameEnded(int finalScore, int finalWave) {
        setGameStat("total_score", getGameStat("total_score") + finalScore);
        setGameStat("highest_wave", Math.max(getGameStat("highest_wave"), finalWave));
    }

    public static void waveCompleted(int wave, boolean perfect, boolean boss, long completionTime) {
        updateGameStats("waves_completed", 1);
        if (perfect) {
            updateGameStats("perfect_waves", 1);
        }
        if (boss) {
            updateGameStats("boss_waves_completed", 1);
        }

        // Check speed demon achievement
        if (completionTime < 20000 && !isAchievementUnlocked(Achievement.SPEED_DEMON)) {
            unlockAchievement(Achievement.SPEED_DEMON);
        }
    }

    public static void asteroidDestroyed() {
        updateGameStats("asteroids_destroyed", 1);
    }

    public static void powerUpCollected() {
        updateGameStats("power_ups_collected", 1);
    }

    public static void bulletFired() {
        updateGameStats("bullets_fired", 1);
    }

    public static void rapidFireUsed() {
        updateGameStats("rapid_fire_uses", 1);
    }

    public static void shieldBlocked() {
        updateGameStats("shield_blocks", 1);
    }

    public static void resetStats() {
        gameStats.clear();
        initializeStats();
        saveAchievements();
    }

    public static String getPlayerName() {
        return System.getProperty("user.name", "Player");
    }
}
