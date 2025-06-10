import javax.sound.sampled.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Random;

public class MusicSystem {
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNELS = 2;
    private static final int BITS_PER_SAMPLE = 16;

    private static final ExecutorService musicExecutor = Executors.newCachedThreadPool();
    private static final AtomicBoolean musicPlaying = new AtomicBoolean(false);
    private static final AtomicInteger intensityLevel = new AtomicInteger(1); // 1-5 scale
    private static final Random rand = new Random();

    // Music layers
    private static SourceDataLine bassLine;
    private static SourceDataLine melodyLine;
    private static SourceDataLine percussionLine;
    private static SourceDataLine ambientLine;

    private static float masterMusicVolume = 0.4f;
    private static long musicStartTime = 0;

    // Pre-generated musical patterns
    private static final double[] BASS_NOTES = {55, 73.42, 82.41, 110}; // A1, D2, E2, A2
    private static final double[] MELODY_NOTES = {220, 246.94, 293.66, 329.63, 369.99, 440}; // A3, B3, D4, E4, F#4, A4
    private static final double[] HARMONY_NOTES = {130.81, 146.83, 164.81, 174.61, 196}; // C3, D3, E3, F3, G3

    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        SAMPLE_RATE,
        BITS_PER_SAMPLE,
        CHANNELS,
        (BITS_PER_SAMPLE / 8) * CHANNELS,
        SAMPLE_RATE,
        false
    );

    public static void startMusic() {
        if (!musicPlaying.get()) {
            musicPlaying.set(true);
            musicStartTime = System.currentTimeMillis();
            intensityLevel.set(1);

            // Start all music layers
            startBassLayer();
            startMelodyLayer();
            startPercussionLayer();
            startAmbientLayer();
        }
    }

    public static void stopMusic() {
        musicPlaying.set(false);
        if (bassLine != null) bassLine.stop();
        if (melodyLine != null) melodyLine.stop();
        if (percussionLine != null) percussionLine.stop();
        if (ambientLine != null) ambientLine.stop();
    }

    public static void setIntensity(int level) {
        intensityLevel.set(Math.max(1, Math.min(5, level)));
    }

    public static void setMasterVolume(float volume) {
        masterMusicVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    private static void startBassLayer() {
        musicExecutor.submit(() -> {
            try {
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, AUDIO_FORMAT);
                bassLine = (SourceDataLine) AudioSystem.getLine(info);
                bassLine.open(AUDIO_FORMAT);
                bassLine.start();

                while (musicPlaying.get()) {
                    byte[] bassPattern = generateBassPattern();
                    bassLine.write(bassPattern, 0, bassPattern.length);
                }

                bassLine.drain();
                bassLine.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void startMelodyLayer() {
        musicExecutor.submit(() -> {
            try {
                // Small delay to sync with bass
                Thread.sleep(100);

                DataLine.Info info = new DataLine.Info(SourceDataLine.class, AUDIO_FORMAT);
                melodyLine = (SourceDataLine) AudioSystem.getLine(info);
                melodyLine.open(AUDIO_FORMAT);
                melodyLine.start();

                while (musicPlaying.get()) {
                    byte[] melodyPattern = generateMelodyPattern();
                    melodyLine.write(melodyPattern, 0, melodyPattern.length);
                }

                melodyLine.drain();
                melodyLine.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void startPercussionLayer() {
        musicExecutor.submit(() -> {
            try {
                // Small delay to sync
                Thread.sleep(50);

                DataLine.Info info = new DataLine.Info(SourceDataLine.class, AUDIO_FORMAT);
                percussionLine = (SourceDataLine) AudioSystem.getLine(info);
                percussionLine.open(AUDIO_FORMAT);
                percussionLine.start();

                while (musicPlaying.get()) {
                    byte[] percussionPattern = generatePercussionPattern();
                    percussionLine.write(percussionPattern, 0, percussionPattern.length);
                }

                percussionLine.drain();
                percussionLine.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void startAmbientLayer() {
        musicExecutor.submit(() -> {
            try {
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, AUDIO_FORMAT);
                ambientLine = (SourceDataLine) AudioSystem.getLine(info);
                ambientLine.open(AUDIO_FORMAT);
                ambientLine.start();

                while (musicPlaying.get()) {
                    byte[] ambientPattern = generateAmbientPattern();
                    ambientLine.write(ambientPattern, 0, ambientPattern.length);
                }

                ambientLine.drain();
                ambientLine.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static byte[] generateBassPattern() {
        double beatDuration = getBeatDuration();
        int numSamples = (int)(SAMPLE_RATE * beatDuration * 4) * CHANNELS; // 4 beats
        byte[] data = new byte[numSamples * 2];

        int intensity = intensityLevel.get();
        double volume = 0.3 + (intensity * 0.1); // Bass gets stronger with intensity

        // Generate bass line pattern
        int[] bassPattern = {0, 0, 2, 1}; // Note indices for 4 beats

        for (int beat = 0; beat < 4; beat++) {
            int startSample = beat * (numSamples / 4 / CHANNELS);
            int endSample = (beat + 1) * (numSamples / 4 / CHANNELS);

            double frequency = BASS_NOTES[bassPattern[beat]];

            for (int i = startSample; i < endSample; i++) {
                double time = i / (double)SAMPLE_RATE;

                // Main bass note
                double bassNote = Math.sin(2 * Math.PI * frequency * time);
                // Sub-harmonic for depth
                double subBass = Math.sin(2 * Math.PI * frequency * 0.5 * time) * 0.3;

                // Envelope for note attack/decay
                double noteTime = (time % beatDuration) / beatDuration;
                double envelope = Math.exp(-noteTime * 3) * (1 - Math.exp(-noteTime * 20));

                double value = (bassNote + subBass) * envelope * volume;
                short sample = (short) (value * 32767);

                int idx = i * 4;
                // Stereo bass (slightly left)
                data[idx] = (byte) (sample & 0xFF);
                data[idx + 1] = (byte) ((sample >> 8) & 0xFF);
                short leftSample = (short) (sample * 0.8);
                data[idx + 2] = (byte) (leftSample & 0xFF);
                data[idx + 3] = (byte) ((leftSample >> 8) & 0xFF);
            }
        }

        return applyVolume(data, masterMusicVolume);
    }

    private static byte[] generateMelodyPattern() {
        double beatDuration = getBeatDuration();
        int numSamples = (int)(SAMPLE_RATE * beatDuration * 8) * CHANNELS; // 8 beats for melody
        byte[] data = new byte[numSamples * 2];

        int intensity = intensityLevel.get();
        double volume = 0.2 + (intensity * 0.08);

        // Melody patterns based on intensity
        int[][] melodyPatterns = {
            {0, -1, 1, -1, 0, -1, 2, -1}, // Calm (intensity 1-2)
            {0, 2, 1, 3, 0, 2, 4, 2},     // Building (intensity 3)
            {0, 3, 2, 5, 1, 4, 3, 5},     // Intense (intensity 4-5)
        };

        int patternIndex = Math.min(intensity - 1, 2);
        if (patternIndex < 0) patternIndex = 0;
        if (patternIndex > 2) patternIndex = 2;

        int[] pattern = melodyPatterns[patternIndex];

        for (int beat = 0; beat < 8; beat++) {
            int startSample = beat * (numSamples / 8 / CHANNELS);
            int endSample = (beat + 1) * (numSamples / 8 / CHANNELS);

            if (pattern[beat] >= 0) { // -1 means rest
                double frequency = MELODY_NOTES[pattern[beat]];

                for (int i = startSample; i < endSample; i++) {
                    double time = i / (double)SAMPLE_RATE;

                    // Main melody
                    double melody = Math.sin(2 * Math.PI * frequency * time);
                    // Harmonic for richness
                    double harmonic = Math.sin(2 * Math.PI * frequency * 2 * time) * 0.2;

                    // Note envelope
                    double noteTime = ((time % beatDuration) / beatDuration);
                    double envelope = Math.sin(noteTime * Math.PI) * Math.exp(-noteTime * 2);

                    double value = (melody + harmonic) * envelope * volume;
                    short sample = (short) (value * 32767);

                    int idx = i * 4;
                    // Stereo melody (slightly right)
                    short rightSample = (short) (sample * 0.8);
                    data[idx] = (byte) (rightSample & 0xFF);
                    data[idx + 1] = (byte) ((rightSample >> 8) & 0xFF);
                    data[idx + 2] = (byte) (sample & 0xFF);
                    data[idx + 3] = (byte) ((sample >> 8) & 0xFF);
                }
            }
        }

        return applyVolume(data, masterMusicVolume);
    }

    private static byte[] generatePercussionPattern() {
        double beatDuration = getBeatDuration();
        int numSamples = (int)(SAMPLE_RATE * beatDuration * 4) * CHANNELS; // 4 beats
        byte[] data = new byte[numSamples * 2];

        int intensity = intensityLevel.get();
        double volume = 0.1 + (intensity * 0.05);

        // Percussion patterns
        boolean[] kickPattern = {true, false, false, false}; // Every 4 beats
        boolean[] hihatPattern = {false, true, false, true}; // Off-beats

        for (int beat = 0; beat < 4; beat++) {
            int startSample = beat * (numSamples / 4 / CHANNELS);
            int endSample = (beat + 1) * (numSamples / 4 / CHANNELS);

            for (int i = startSample; i < endSample; i++) {
                double time = i / (double)SAMPLE_RATE;
                double noteTime = ((time % beatDuration) / beatDuration);
                double value = 0;

                // Kick drum
                if (kickPattern[beat] && noteTime < 0.1) {
                    double kickFreq = 60 - (noteTime * 300); // Frequency drop
                    value += Math.sin(2 * Math.PI * kickFreq * time) *
                            Math.exp(-noteTime * 15) * 0.8;
                }

                // Hi-hat (noise-based)
                if (hihatPattern[beat] && noteTime < 0.05) {
                    value += (rand.nextDouble() - 0.5) *
                            Math.exp(-noteTime * 25) * 0.3;
                }

                value *= volume;
                short sample = (short) (value * 32767);

                int idx = i * 4;
                data[idx] = (byte) (sample & 0xFF);
                data[idx + 1] = (byte) ((sample >> 8) & 0xFF);
                data[idx + 2] = (byte) (sample & 0xFF);
                data[idx + 3] = (byte) ((sample >> 8) & 0xFF);
            }
        }

        return applyVolume(data, masterMusicVolume);
    }

    private static byte[] generateAmbientPattern() {
        double beatDuration = getBeatDuration();
        int numSamples = (int)(SAMPLE_RATE * beatDuration * 16) * CHANNELS; // Long ambient pad
        byte[] data = new byte[numSamples * 2];

        int intensity = intensityLevel.get();
        double volume = 0.15 + (intensity * 0.03);

        for (int i = 0; i < numSamples / CHANNELS; i++) {
            double time = i / (double)SAMPLE_RATE;

            // Multiple harmony layers
            double harm1 = Math.sin(2 * Math.PI * HARMONY_NOTES[0] * time) * 0.3;
            double harm2 = Math.sin(2 * Math.PI * HARMONY_NOTES[2] * time) * 0.25;
            double harm3 = Math.sin(2 * Math.PI * HARMONY_NOTES[4] * time) * 0.2;

            // Slow modulation for movement
            double modulation = Math.sin(2 * Math.PI * time * 0.1) * 0.1;

            double value = (harm1 + harm2 + harm3) * (1 + modulation) * volume;
            short sample = (short) (value * 32767);

            int idx = i * 4;
            data[idx] = (byte) (sample & 0xFF);
            data[idx + 1] = (byte) ((sample >> 8) & 0xFF);
            data[idx + 2] = (byte) (sample & 0xFF);
            data[idx + 3] = (byte) ((sample >> 8) & 0xFF);
        }

        return applyVolume(data, masterMusicVolume);
    }

    private static double getBeatDuration() {
        int intensity = intensityLevel.get();
        // Tempo increases with intensity: 80 BPM -> 140 BPM
        double bpm = 80 + (intensity * 12);
        return 60.0 / bpm; // Beat duration in seconds
    }

    private static byte[] applyVolume(byte[] data, float volume) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i += 2) {
            short sample = (short) ((data[i + 1] << 8) | (data[i] & 0xFF));
            sample = (short) (sample * volume);
            result[i] = (byte) (sample & 0xFF);
            result[i + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        return result;
    }

    // Called by game to update music based on game state
    public static void updateMusicIntensity(int asteroidCount, int playerLives, boolean powerUpActive) {
        int newIntensity = 1;

        // Base intensity on asteroid count
        if (asteroidCount > 15) newIntensity = 5;
        else if (asteroidCount > 10) newIntensity = 4;
        else if (asteroidCount > 6) newIntensity = 3;
        else if (asteroidCount > 3) newIntensity = 2;

        // Increase intensity if low on lives
        if (playerLives == 1) newIntensity = Math.min(5, newIntensity + 1);

        // Boost during power-up
        if (powerUpActive) newIntensity = Math.min(5, newIntensity + 1);

        setIntensity(newIntensity);
    }

    public static void playVictoryTheme() {
        musicExecutor.submit(() -> {
            try {
                byte[] victoryMusic = generateVictoryTheme();
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, AUDIO_FORMAT);
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(AUDIO_FORMAT);
                line.start();
                line.write(victoryMusic, 0, victoryMusic.length);
                line.drain();
                line.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static byte[] generateVictoryTheme() {
        int numSamples = (int)(SAMPLE_RATE * 3.0) * CHANNELS; // 3 seconds
        byte[] data = new byte[numSamples * 2];

        // Victory melody: ascending major scale
        double[] victoryNotes = {261.63, 293.66, 329.63, 349.23, 392.00, 440.00, 493.88, 523.25};

        for (int i = 0; i < numSamples / CHANNELS; i++) {
            double time = i / (double)SAMPLE_RATE;
            int noteIndex = (int)(time * 2.5) % victoryNotes.length;
            double frequency = victoryNotes[noteIndex];

            double note = Math.sin(2 * Math.PI * frequency * time);
            double harmonic = Math.sin(2 * Math.PI * frequency * 2 * time) * 0.3;

            double value = (note + harmonic) * 0.5;
            short sample = (short) (value * 32767);

            int idx = i * 4;
            data[idx] = (byte) (sample & 0xFF);
            data[idx + 1] = (byte) ((sample >> 8) & 0xFF);
            data[idx + 2] = (byte) (sample & 0xFF);
            data[idx + 3] = (byte) ((sample >> 8) & 0xFF);
        }

        return applyVolume(data, masterMusicVolume);
    }
}
