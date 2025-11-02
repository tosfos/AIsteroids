import javax.sound.sampled.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class SoundManager {
    private static final int SAMPLE_RATE = GameConfig.Audio.SFX_SAMPLE_RATE;  // CD quality audio
    private static final int BITS_PER_SAMPLE = GameConfig.Audio.SFX_BITS_PER_SAMPLE;
    private static final int CHANNELS = GameConfig.Audio.SFX_CHANNELS; // Stereo

    // Executor for playing sounds asynchronously.
    private static final ExecutorService soundExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "SoundThread");
        t.setDaemon(true);  // Make sound threads daemon threads
        return t;
    });
    private static final Random random = new Random();

    // Master volume control
    private static float masterVolume = GameConfig.Audio.SFX_MASTER_VOLUME_DEFAULT;

    // Background ambient sound
    private static final AtomicBoolean ambientPlaying = new AtomicBoolean(false);
    private static SourceDataLine ambientLine;

    // Pre-generated sound variations for variety
    private static final byte[][] LASER_SOUNDS = generateLaserVariations();
    private static final byte[][] EXPLOSION_SOUNDS = generateExplosionVariations();
    private static final byte[][] THRUSTER_SOUNDS = generateThrusterVariations();
    private static final byte[] AMBIENT_SPACE = generateAmbientSpace();
    private static final byte[] ASTEROID_HIT = generateAsteroidHit();
    private static final byte[] SHIELD_RECHARGE = generateShieldRecharge();
    private static final byte[] POWER_UP = generatePowerUp();
    private static final byte[] GAME_OVER = generateGameOver();
    private static final byte[] LEVEL_UP = generateLevelUp();

    // Audio format for high-quality stereo sound
    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        SAMPLE_RATE,
        BITS_PER_SAMPLE,
        CHANNELS,
        (BITS_PER_SAMPLE / 8) * CHANNELS,
        SAMPLE_RATE,
        false
    );

    public static void playLaser() {
        byte[] sound = LASER_SOUNDS[random.nextInt(LASER_SOUNDS.length)];
        playSound(sound, 0.8f);
    }

    public static void playExplosion() {
        byte[] sound = EXPLOSION_SOUNDS[random.nextInt(EXPLOSION_SOUNDS.length)];
        playSound(sound, 1.0f);
    }

    public static void playThruster() {
        byte[] sound = THRUSTER_SOUNDS[random.nextInt(THRUSTER_SOUNDS.length)];
        playSound(sound, 0.6f);
    }

    public static void playAsteroidHit() {
        playSound(ASTEROID_HIT, 0.9f);
    }

    public static void playShieldRecharge() {
        playSound(SHIELD_RECHARGE, 0.7f);
    }

    public static void playPowerUp() {
        playSound(POWER_UP, 0.8f);
    }

    public static void playGameOver() {
        playSound(GAME_OVER, 1.0f);
    }

    public static void playLevelUp() {
        playSound(LEVEL_UP, 0.9f);
    }

    public static void startAmbientSpace() {
        if (!ambientPlaying.get()) {
            ambientPlaying.set(true);
            soundExecutor.submit(() -> {
                try {
                    DataLine.Info info = new DataLine.Info(SourceDataLine.class, AUDIO_FORMAT);
                    ambientLine = (SourceDataLine) AudioSystem.getLine(info);
                    ambientLine.open(AUDIO_FORMAT);
                    ambientLine.start();

                    // Loop ambient sound
                    while (ambientPlaying.get()) {
                        ambientLine.write(AMBIENT_SPACE, 0, AMBIENT_SPACE.length);
                    }

                    if (ambientLine != null) {
                        ambientLine.drain();
                        ambientLine.close();
                    }
                } catch (LineUnavailableException e) {
                    System.err.println("Audio line unavailable for ambient sound: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid audio format for ambient sound: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Unexpected error in ambient sound: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }

    public static void stopAmbientSpace() {
        ambientPlaying.set(false);
        if (ambientLine != null) {
            ambientLine.stop();
            ambientLine.close();
            ambientLine = null;
        }
    }

    private static volatile boolean isSoundSystemActive = true;

    public static void shutdown() {
        isSoundSystemActive = false;
        stopAmbientSpace();
        soundExecutor.shutdown();
        try {
            if (!soundExecutor.awaitTermination(500, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                soundExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            soundExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static void playSound(byte[] soundData, float volume) {
        if (!isSoundSystemActive) return;  // Don't play sounds if system is shutting down
        soundExecutor.submit(() -> {
            try {
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, AUDIO_FORMAT);
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(AUDIO_FORMAT);
                line.start();

                // Apply volume control
                byte[] adjustedData = applyVolume(soundData, volume * masterVolume);

                line.write(adjustedData, 0, adjustedData.length);
                line.drain();
                line.close();
            } catch (LineUnavailableException e) {
                System.err.println("Audio line unavailable for sound effect: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid audio format for sound effect: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Unexpected error playing sound: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static byte[] applyVolume(byte[] data, float volume) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i += 2) {
            // Convert to 16-bit signed
            short sample = (short) ((data[i + 1] << 8) | (data[i] & 0xFF));
            sample = (short) (sample * volume);
            // Convert back to bytes
            result[i] = (byte) (sample & 0xFF);
            result[i + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        return result;
    }

    private static byte[][] generateLaserVariations() {
        byte[][] variations = new byte[5][];

        for (int v = 0; v < 5; v++) {
            int numSamples = (int)(SAMPLE_RATE * 0.15) * CHANNELS;
            byte[] data = new byte[numSamples * 2]; // 16-bit samples

            for (int i = 0; i < numSamples / CHANNELS; i++) {
                double time = i / (double)SAMPLE_RATE;

                // Multiple oscillators for rich sound
                double baseFreq = 1800 + (v * 200) + (random.nextDouble() * 100);
                double freq1 = baseFreq - (i * 8);
                double freq2 = baseFreq * 1.5 - (i * 12);
                double freq3 = baseFreq * 0.5;

                // Complex waveform combining sine, square, and sawtooth
                double sine = Math.sin(2 * Math.PI * freq1 * time);
                double square = Math.signum(Math.sin(2 * Math.PI * freq2 * time)) * 0.3;
                double sawtooth = (2 * ((freq3 * time) % 1) - 1) * 0.2;

                // Envelope (attack, decay, sustain, release)
                double envelope = Math.exp(-time * 8) * (1 - Math.exp(-time * 50));

                double value = (sine + square + sawtooth) * envelope;

                // Add harmonic distortion
                value = Math.tanh(value * 2) * 0.5;

                short sample = (short) (value * 32767 * 0.8);

                // Stereo: slightly pan left/right for spatial effect
                short leftSample = (short) (sample * (0.6 + 0.4 * Math.sin(time * 10)));
                short rightSample = (short) (sample * (0.6 + 0.4 * Math.cos(time * 10)));

                int idx = i * 4;
                // Left channel
                data[idx] = (byte) (leftSample & 0xFF);
                data[idx + 1] = (byte) ((leftSample >> 8) & 0xFF);
                // Right channel
                data[idx + 2] = (byte) (rightSample & 0xFF);
                data[idx + 3] = (byte) ((rightSample >> 8) & 0xFF);
            }

            variations[v] = addReverb(data);
        }

        return variations;
    }

    private static byte[][] generateExplosionVariations() {
        byte[][] variations = new byte[4][];

        for (int v = 0; v < 4; v++) {
            double duration = 0.8 + (v * 0.2);
            int numSamples = (int)(SAMPLE_RATE * duration) * CHANNELS;
            byte[] data = new byte[numSamples * 2];

            for (int i = 0; i < numSamples / CHANNELS; i++) {
                double time = i / (double)SAMPLE_RATE;
                double progress = time / duration;

                // Multiple noise sources
                double noise1 = (random.nextDouble() - 0.5) * 2;
                double noise2 = (random.nextDouble() - 0.5) * 2;
                double noise3 = (random.nextDouble() - 0.5) * 2;

                // Filtered noise for different frequency bands
                double lowFreq = applyLowPass(noise1, 200, time);
                double midFreq = applyBandPass(noise2, 800, 2000, time);
                double highFreq = applyHighPass(noise3, 4000, time);

                // Complex envelope with multiple phases
                double envelope1 = Math.exp(-progress * 3) * (1 - Math.exp(-progress * 20));
                double envelope2 = Math.exp(-progress * 1.5) * Math.sin(progress * Math.PI);

                double value = (lowFreq * envelope1 + midFreq * envelope2 + highFreq * envelope1) * 0.7;

                // Add crackling effect
                if (random.nextDouble() < 0.1) {
                    value += (random.nextDouble() - 0.5) * envelope1;
                }

                short sample = (short) (value * 32767);

                // Stereo explosion effect
                double pan = Math.sin(time * 3) * 0.3;
                short leftSample = (short) (sample * (0.8 - pan));
                short rightSample = (short) (sample * (0.8 + pan));

                int idx = i * 4;
                data[idx] = (byte) (leftSample & 0xFF);
                data[idx + 1] = (byte) ((leftSample >> 8) & 0xFF);
                data[idx + 2] = (byte) (rightSample & 0xFF);
                data[idx + 3] = (byte) ((rightSample >> 8) & 0xFF);
            }

            variations[v] = addReverb(data);
        }

        return variations;
    }

    private static byte[][] generateThrusterVariations() {
        byte[][] variations = new byte[3][];

        for (int v = 0; v < 3; v++) {
            int numSamples = (int)(SAMPLE_RATE * 0.3) * CHANNELS;
            byte[] data = new byte[numSamples * 2];

            for (int i = 0; i < numSamples / CHANNELS; i++) {
                double time = i / (double)SAMPLE_RATE;

                // Jet engine simulation
                double baseNoise = (random.nextDouble() - 0.5) * 2;
                double modulation = Math.sin(time * 60 + v) * 0.3;

                // Multiple frequency bands for realistic jet sound
                double lowRumble = applyLowPass(baseNoise, 150, time) * 0.8;
                double midHiss = applyBandPass(baseNoise, 800, 3000, time) * 0.6;
                double highWhine = Math.sin(2 * Math.PI * (400 + modulation * 100) * time) * 0.3;

                double envelope = Math.sin(time * Math.PI / 0.3);
                double value = (lowRumble + midHiss + highWhine) * envelope;

                short sample = (short) (value * 32767 * 0.6);

                // Stereo panning for movement effect
                double pan = Math.sin(time * 8) * 0.2;
                short leftSample = (short) (sample * (0.7 - pan));
                short rightSample = (short) (sample * (0.7 + pan));

                int idx = i * 4;
                data[idx] = (byte) (leftSample & 0xFF);
                data[idx + 1] = (byte) ((leftSample >> 8) & 0xFF);
                data[idx + 2] = (byte) (rightSample & 0xFF);
                data[idx + 3] = (byte) ((rightSample >> 8) & 0xFF);
            }

            variations[v] = data;
        }

        return variations;
    }

    private static byte[] generateAmbientSpace() {
        int numSamples = (int)(SAMPLE_RATE * 10) * CHANNELS; // 10 second loop
        byte[] data = new byte[numSamples * 2];

        for (int i = 0; i < numSamples / CHANNELS; i++) {
            double time = i / (double)SAMPLE_RATE;

            // Deep space ambience
            double deepTone1 = Math.sin(2 * Math.PI * 40 * time) * 0.1;
            double deepTone2 = Math.sin(2 * Math.PI * 60 * time) * 0.08;
            double deepTone3 = Math.sin(2 * Math.PI * 80 * time) * 0.06;

            // Subtle cosmic wind
            double wind = (random.nextDouble() - 0.5) * 0.05;
            wind = applyLowPass(wind, 100, time);

            // Occasional cosmic sparkles
            double sparkle = 0;
            if (random.nextDouble() < 0.001) {
                sparkle = Math.sin(2 * Math.PI * (2000 + random.nextDouble() * 1000) * time) *
                         Math.exp(-(time % 1) * 10) * 0.1;
            }

            double value = deepTone1 + deepTone2 + deepTone3 + wind + sparkle;
            short sample = (short) (value * 32767);

            int idx = i * 4;
            data[idx] = (byte) (sample & 0xFF);
            data[idx + 1] = (byte) ((sample >> 8) & 0xFF);
            data[idx + 2] = (byte) (sample & 0xFF);
            data[idx + 3] = (byte) ((sample >> 8) & 0xFF);
        }

        return data;
    }

    private static byte[] generateAsteroidHit() {
        int numSamples = (int)(SAMPLE_RATE * 0.2) * CHANNELS;
        byte[] data = new byte[numSamples * 2];

        for (int i = 0; i < numSamples / CHANNELS; i++) {
            double time = i / (double)SAMPLE_RATE;
            double progress = time / 0.2;

            // Rock impact sound
            double impact = (random.nextDouble() - 0.5) * 2;
            impact = applyBandPass(impact, 200, 1000, time);

            // Metallic ring
            double ring = Math.sin(2 * Math.PI * 800 * time) * Math.exp(-time * 15);

            double envelope = Math.exp(-progress * 8);
            double value = (impact + ring) * envelope;

            short sample = (short) (value * 32767 * 0.7);

            int idx = i * 4;
            data[idx] = (byte) (sample & 0xFF);
            data[idx + 1] = (byte) ((sample >> 8) & 0xFF);
            data[idx + 2] = (byte) (sample & 0xFF);
            data[idx + 3] = (byte) ((sample >> 8) & 0xFF);
        }

        return addReverb(data);
    }

    private static byte[] generateShieldRecharge() {
        int numSamples = (int)(SAMPLE_RATE * 1.0) * CHANNELS;
        byte[] data = new byte[numSamples * 2];

        for (int i = 0; i < numSamples / CHANNELS; i++) {
            double time = i / (double)SAMPLE_RATE;

            // Rising energy sound
            double freq = 200 + (time * 600);
            double energy = Math.sin(2 * Math.PI * freq * time);

            // Harmonic overtones
            double harmonic1 = Math.sin(2 * Math.PI * freq * 2 * time) * 0.3;
            double harmonic2 = Math.sin(2 * Math.PI * freq * 3 * time) * 0.2;

            double envelope = Math.sin(time * Math.PI) * (1 - Math.exp(-time * 3));
            double value = (energy + harmonic1 + harmonic2) * envelope;

            short sample = (short) (value * 32767 * 0.5);

            int idx = i * 4;
            data[idx] = (byte) (sample & 0xFF);
            data[idx + 1] = (byte) ((sample >> 8) & 0xFF);
            data[idx + 2] = (byte) (sample & 0xFF);
            data[idx + 3] = (byte) ((sample >> 8) & 0xFF);
        }

        return addReverb(data);
    }

    private static byte[] generatePowerUp() {
        int numSamples = (int)(SAMPLE_RATE * 0.5) * CHANNELS;
        byte[] data = new byte[numSamples * 2];

        for (int i = 0; i < numSamples / CHANNELS; i++) {
            double time = i / (double)SAMPLE_RATE;

            // Ascending arpeggio
            double[] frequencies = {261.63, 329.63, 392.00, 523.25}; // C, E, G, C
            int noteIndex = (int)(time * 8) % frequencies.length;
            double freq = frequencies[noteIndex];

            double tone = Math.sin(2 * Math.PI * freq * time);
            double envelope = Math.exp(-time * 2) * Math.sin(time * Math.PI / 0.5);

            double value = tone * envelope;
            short sample = (short) (value * 32767 * 0.6);

            int idx = i * 4;
            data[idx] = (byte) (sample & 0xFF);
            data[idx + 1] = (byte) ((sample >> 8) & 0xFF);
            data[idx + 2] = (byte) (sample & 0xFF);
            data[idx + 3] = (byte) ((sample >> 8) & 0xFF);
        }

        return addReverb(data);
    }

    private static byte[] generateGameOver() {
        int numSamples = (int)(SAMPLE_RATE * 2.0) * CHANNELS;
        byte[] data = new byte[numSamples * 2];

        for (int i = 0; i < numSamples / CHANNELS; i++) {
            double time = i / (double)SAMPLE_RATE;

            // Descending dramatic chord
            double freq1 = 440 * Math.exp(-time * 0.5); // A falling
            double freq2 = 330 * Math.exp(-time * 0.3); // E falling
            double freq3 = 220 * Math.exp(-time * 0.2); // A falling

            double tone1 = Math.sin(2 * Math.PI * freq1 * time) * 0.4;
            double tone2 = Math.sin(2 * Math.PI * freq2 * time) * 0.3;
            double tone3 = Math.sin(2 * Math.PI * freq3 * time) * 0.5;

            double envelope = Math.exp(-time * 0.8);
            double value = (tone1 + tone2 + tone3) * envelope;

            short sample = (short) (value * 32767 * 0.7);

            int idx = i * 4;
            data[idx] = (byte) (sample & 0xFF);
            data[idx + 1] = (byte) ((sample >> 8) & 0xFF);
            data[idx + 2] = (byte) (sample & 0xFF);
            data[idx + 3] = (byte) ((sample >> 8) & 0xFF);
        }

        return addReverb(data);
    }

    private static byte[] generateLevelUp() {
        int numSamples = (int)(SAMPLE_RATE * 1.0) * CHANNELS;
        byte[] data = new byte[numSamples * 2];

        for (int i = 0; i < numSamples / CHANNELS; i++) {
            double time = i / (double)SAMPLE_RATE;

            // Triumphant ascending melody
            double[] frequencies = {523.25, 659.25, 783.99, 1046.50}; // C, E, G, C (high)
            int noteIndex = (int)(time * 4) % frequencies.length;
            double freq = frequencies[noteIndex];

            double tone = Math.sin(2 * Math.PI * freq * time);
            double harmonic = Math.sin(2 * Math.PI * freq * 2 * time) * 0.3;

            double envelope = Math.sin(time * Math.PI) * (1 - Math.exp(-time * 5));
            double value = (tone + harmonic) * envelope;

            short sample = (short) (value * 32767 * 0.8);

            int idx = i * 4;
            data[idx] = (byte) (sample & 0xFF);
            data[idx + 1] = (byte) ((sample >> 8) & 0xFF);
            data[idx + 2] = (byte) (sample & 0xFF);
            data[idx + 3] = (byte) ((sample >> 8) & 0xFF);
        }

        return addReverb(data);
    }

    // Audio processing utilities
    private static double applyLowPass(double input, double cutoff, double time) {
        // Simple RC low-pass filter simulation
        double rc = 1.0 / (2.0 * Math.PI * cutoff);
        double alpha = 1.0 / (1.0 + rc * SAMPLE_RATE);
        return input * alpha;
    }

    private static double applyHighPass(double input, double cutoff, double time) {
        // Simple RC high-pass filter simulation
        double rc = 1.0 / (2.0 * Math.PI * cutoff);
        double alpha = rc * SAMPLE_RATE / (1.0 + rc * SAMPLE_RATE);
        return input * alpha;
    }

    private static double applyBandPass(double input, double lowCutoff, double highCutoff, double time) {
        return applyHighPass(applyLowPass(input, highCutoff, time), lowCutoff, time);
    }

    private static byte[] addReverb(byte[] input) {
        byte[] output = new byte[input.length];
        double[] delayBuffer = new double[4410]; // 100ms delay buffer
        int delayIndex = 0;

        for (int i = 0; i < input.length; i += 4) {
            // Convert to sample values
            short leftSample = (short) ((input[i + 1] << 8) | (input[i] & 0xFF));
            short rightSample = (short) ((input[i + 3] << 8) | (input[i + 2] & 0xFF));

            // Add reverb
            double drySignal = leftSample / 32767.0;
            double wetSignal = delayBuffer[delayIndex] * 0.3;
            double outputSignal = drySignal + wetSignal;

            delayBuffer[delayIndex] = drySignal + wetSignal * 0.5;
            delayIndex = (delayIndex + 1) % delayBuffer.length;

            short finalSample = (short) (outputSignal * 32767 * 0.8);

            output[i] = (byte) (finalSample & 0xFF);
            output[i + 1] = (byte) ((finalSample >> 8) & 0xFF);
            output[i + 2] = (byte) (finalSample & 0xFF);
            output[i + 3] = (byte) ((finalSample >> 8) & 0xFF);
        }

        return output;
    }

    public static void setMasterVolume(float volume) {
        masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    public static float getMasterVolume() {
        return masterVolume;
    }
}
