/**
 * Centralized configuration for all game constants and settings.
 * This replaces magic numbers scattered throughout the codebase.
 */
public final class GameConfig {

    // Private constructor to prevent instantiation
    private GameConfig() {}

    // === DISPLAY SETTINGS ===
    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_HEIGHT = 600;
    public static final int TARGET_FPS = 60;
    public static final int FRAME_TIME_MS = 1000 / TARGET_FPS; // ~16ms

    // === PLAYER SHIP SETTINGS ===
    public static final class PlayerShip {
        public static final int INITIAL_LIVES = 3;
        public static final double ROTATION_SPEED_DEGREES = 180.0; // degrees per second
        public static final double ROTATION_SPEED_RADIANS = Math.toRadians(ROTATION_SPEED_DEGREES);
        public static final double ACCELERATION = 200.0; // pixels per second^2
        public static final double MAX_SPEED = 300.0; // pixels per second
        public static final double FIRE_RATE = 10.0; // shots per second
        public static final double INVULNERABILITY_TIME = 2.0; // seconds
        public static final double RADIUS = 15.0; // collision radius
        public static final double ENGINE_TRAIL_PARTICLES = 3.0;
        public static final double ENGINE_TRAIL_SPREAD = 6.0;
        public static final double ENGINE_TRAIL_LIFETIME = 0.5; // seconds
    }

    // === BULLET SETTINGS ===
    public static final class Bullet {
        public static final double SPEED = 500.0; // pixels per second
        public static final double LIFETIME = 2.0; // seconds
        public static final double RADIUS = 3.0; // collision radius
        public static final int MULTI_SHOT_COUNT = 3; // bullets per multi-shot
        public static final double MULTI_SHOT_SPREAD = Math.toRadians(15.0); // angle spread
    }

    // === ASTEROID SETTINGS ===
    public static final class Asteroid {
        public static final double LARGE_RADIUS = 45.0; // size 3
        public static final double MEDIUM_RADIUS = 30.0; // size 2
        public static final double SMALL_RADIUS = 15.0; // size 1
        public static final double RADIUS_PER_SIZE = 15.0;
        public static final double MIN_SPEED = 20.0;
        public static final double MAX_SPEED = 80.0;
        public static final double MAX_ROTATION_SPEED = 0.25; // radians per second
        public static final int MIN_VERTICES = 8;
        public static final int MAX_ADDITIONAL_VERTICES = 5; // 8-12 total
        public static final double SHAPE_VARIATION = 0.6; // 70%-130% of base radius
        public static final double SPAWN_MARGIN = 50.0; // pixels from screen edge
        public static final int SPLIT_ANGLE_MIN = 30; // degrees
        public static final int SPLIT_ANGLE_MAX = 60; // degrees
        public static final double SPLIT_SPEED_MIN = 0.8; // multiplier
        public static final double SPLIT_SPEED_MAX = 1.2; // multiplier
    }

    // === POWER-UP SETTINGS ===
    public static final class PowerUp {
        public static final double RADIUS = 12.0;
        public static final double SPEED = 30.0;
        public static final double LIFETIME = 15.0; // seconds before despawning
        public static final double RAPID_FIRE_DURATION = 10.0; // seconds
        public static final double RAPID_FIRE_MULTIPLIER = 3.0; // fire rate multiplier
        public static final double SHIELD_DURATION = 8.0; // seconds
        public static final double MULTI_SHOT_DURATION = 12.0; // seconds
        public static final double EXTRA_LIFE_RARITY = 0.1; // 10% chance
    }

    // === WAVE SYSTEM SETTINGS ===
    public static final class Wave {
        public static final int BOSS_WAVE_INTERVAL = 5; // every 5th wave
        public static final double POWER_UP_BASE_CHANCE = 0.2; // 20% base chance
        public static final double POWER_UP_CHANCE_INCREMENT = 0.05; // +5% per wave
        public static final double POWER_UP_MAX_CHANCE = 0.8; // 80% max chance
        public static final int SCORE_MULTIPLIER_MAX = 5; // maximum score multiplier
        public static final double PERFECT_WAVE_BONUS = 1.5; // 50% bonus
        public static final double SPEED_BONUS_THRESHOLD = 30.0; // seconds
        public static final double SPEED_BONUS_MULTIPLIER = 1.2; // 20% bonus
        public static final int ASTEROIDS_PER_WAVE_BASE = 4;
        public static final int ASTEROIDS_PER_WAVE_INCREMENT = 2;
        public static final int ASTEROIDS_MAX_PER_WAVE = 15;
    }

    // === SCORING SETTINGS ===
    public static final class Scoring {
        public static final int SMALL_ASTEROID_POINTS = 100;
        public static final int MEDIUM_ASTEROID_POINTS = 200;
        public static final int LARGE_ASTEROID_POINTS = 300;
        public static final int POWER_UP_POINTS = 50;
        public static final int WAVE_COMPLETION_BONUS = 1000;
    }

    // === VISUAL EFFECTS SETTINGS ===
    public static final class Effects {
        public static final int EXPLOSION_PARTICLES_PER_SIZE = 8;
        public static final int DEBRIS_PARTICLES_PER_SIZE = 3;
        public static final double PARTICLE_SPEED_MIN = 50.0;
        public static final double PARTICLE_SPEED_MAX = 200.0;
        public static final double PARTICLE_LIFETIME_MIN = 1.0;
        public static final double PARTICLE_LIFETIME_MAX = 3.0;
        public static final int STARFIELD_STAR_COUNT = 200;
        public static final double STAR_SIZE_MIN = 1.0;
        public static final double STAR_SIZE_MAX = 4.0;
        public static final float STAR_BRIGHTNESS_MIN = 0.3f;
        public static final float STAR_BRIGHTNESS_MAX = 1.0f;
        public static final double INVULNERABILITY_BLINK_SPEED = 0.02;
        public static final float INVULNERABILITY_MIN_ALPHA = 0.3f;
        public static final float INVULNERABILITY_MAX_ALPHA = 1.0f;
    }

    // === THREAD SETTINGS ===
    public static final class Threading {
        public static final int WAVE_MANAGER_CHECK_INTERVAL_MS = 3000;
        public static final int THREAD_SHUTDOWN_TIMEOUT_SECONDS = 2;
    }

    // === AUDIO SETTINGS ===
    public static final class Audio {
        public static final int MUSIC_INTENSITY_LEVELS = 5;
        public static final double MUSIC_INTENSITY_THRESHOLD = 0.2; // per level
    }

    // === UI SETTINGS ===
    public static final class UI {
        public static final String FONT_NAME = "Orbitron";
        public static final String FALLBACK_FONT_NAME = "Arial";
        public static final int HUD_FONT_SIZE = 16;
        public static final int TITLE_FONT_SIZE = 48;
        public static final int SUBTITLE_FONT_SIZE = 24;
        public static final int LIVES_DISPLAY_SPACING = 25;
        public static final int HUD_MARGIN = 10;
        public static final int HUD_LINE_HEIGHT = 25;
    }

    // === LEADERBOARD SETTINGS ===
    public static final class Leaderboard {
        public static final int MAX_ENTRIES = 10;
        public static final String DEFAULT_PLAYER_NAME = "Anonymous";
        public static final String SAVE_FILE = "leaderboard.dat";
    }
}
