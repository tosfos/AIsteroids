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
        public static final double LIFETIME = 0.7; // seconds
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
        // Wave progression
        public static final int BOSS_WAVE_INTERVAL = 5; // every 5th wave
        public static final int BASE_ASTEROIDS = 5; // Starting number of asteroids
        public static final int ASTEROID_INCREMENT = 2; // Additional asteroids per wave
        public static final int MAX_NORMAL_ASTEROIDS = 25; // Maximum asteroids in normal waves
        public static final int BASE_BOSS_ASTEROIDS = 3; // Starting number of boss wave asteroids
        public static final int MAX_BOSS_ASTEROIDS = 15; // Maximum asteroids in boss waves

        // Scoring and bonuses
        public static final int SCORE_MULTIPLIER_MAX = 5; // maximum score multiplier
        public static final double PERFECT_WAVE_BONUS = 1.5; // 50% bonus
        public static final int SPEED_BONUS_TIME = 30000; // milliseconds
        public static final int SPEED_BONUS_POINTS = 500; // Points for speed bonus

        // Asteroid spawning
        public static final double BOSS_BASE_SPEED = 30.0; // Base speed for boss wave asteroids
        public static final double BOSS_SPEED_MULTIPLIER = 20.0; // Speed multiplier for boss waves
        public static final double NORMAL_BASE_SPEED = 20.0; // Base speed for normal asteroids
        public static final double NORMAL_SPEED_INCREMENT = 5.0; // Speed increase per wave
        public static final double NORMAL_SPEED_VARIATION = 30.0; // Random speed variation

        // Power-up spawning
        public static final double POWER_UP_BASE_CHANCE = 0.1; // Base spawn chance
        public static final double POWER_UP_CHANCE_INCREMENT = 0.03; // Increment per wave
        public static final double POWER_UP_MAX_CHANCE = 0.4; // Maximum spawn chance
        public static final double POWER_UP_BOSS_CHANCE = 0.8; // Boss wave spawn chance
        public static final int POWER_UP_RARE_WAVE = 10; // Wave number when rare power-ups start appearing
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
        // General particle settings
        public static final int EXPLOSION_PARTICLES_PER_SIZE = 8;
        public static final int DEBRIS_PARTICLES_PER_SIZE = 3;
        public static final double PARTICLE_SPEED_MIN = 50.0;
        public static final double PARTICLE_SPEED_MAX = 200.0;
        public static final double PARTICLE_LIFETIME_MIN = 1.0;
        public static final double PARTICLE_LIFETIME_MAX = 3.0;

        // Specific particle type settings
        public static final class Explosion {
            public static final double SPEED_MIN = 50.0;
            public static final double SPEED_MAX = 200.0;
            public static final double LIFETIME_MIN = 0.8;
            public static final double LIFETIME_MAX = 1.2;
        }

        public static final class Spark {
            public static final double SPEED_MIN = 80.0;
            public static final double SPEED_MAX = 200.0;
            public static final double LIFETIME_MIN = 0.2;
            public static final double LIFETIME_MAX = 0.3;
        }

        public static final class Debris {
            public static final double SPEED_MIN = 20.0;
            public static final double SPEED_MAX = 100.0;
            public static final double LIFETIME_MIN = 2.0;
            public static final double LIFETIME_MAX = 5.0;
        }

        public static final class Warp {
            public static final double SPEED_MIN = 30.0;
            public static final double SPEED_MAX = 100.0;
            public static final double LIFETIME = 1.0;
        }

        // Starfield settings
        public static final int STARFIELD_STAR_COUNT = 200;
        public static final double STAR_SIZE_MIN = 1.0;
        public static final double STAR_SIZE_MAX = 4.0;
        public static final float STAR_BRIGHTNESS_MIN = 0.3f;
        public static final float STAR_BRIGHTNESS_MAX = 1.0f;

        // Visual effect settings
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
        // Music settings
        public static final int MUSIC_INTENSITY_LEVELS = 5;
        public static final double MUSIC_INTENSITY_THRESHOLD = 0.2; // per level
        public static final float MUSIC_MASTER_VOLUME_DEFAULT = 0.4f;
        public static final int MUSIC_BPM_BASE = 60;       // Base BPM
        public static final int MUSIC_BPM_PER_LEVEL = 8;   // BPM increase per intensity level
        public static final double MUSIC_VOLUME_BASE = 0.6; // Base volume factor
        public static final double MUSIC_VOLUME_PER_LEVEL = 0.08; // Volume increase per level

        // Sound effects (SFX) settings
        public static final int SFX_SAMPLE_RATE = 44100;
        public static final int SFX_BITS_PER_SAMPLE = 16;
        public static final int SFX_CHANNELS = 2; // Stereo
        public static final float SFX_MASTER_VOLUME_DEFAULT = 0.7f;
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
