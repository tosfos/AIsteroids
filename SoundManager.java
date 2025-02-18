import javax.sound.sampled.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundManager {
    private static final int SAMPLE_RATE = 44100;  // CD quality audio
    
    // Executor for playing sounds asynchronously.
    private static final ExecutorService soundExecutor = Executors.newCachedThreadPool();
    
    // Generate and cache our sound effects
    private static final byte[] LASER_SOUND = generateLaserSound();
    private static final byte[] EXPLOSION_SOUND = generateExplosionSound();
    private static final byte[] THRUSTER_SOUND = generateThrusterSound();
    
    public static void playLaser() {
        playSound(LASER_SOUND);
    }
    
    public static void playExplosion() {
        playSound(EXPLOSION_SOUND);
    }
    
    public static void playThruster() {
        playSound(THRUSTER_SOUND);
    }
    
    private static void playSound(byte[] soundData) {
        soundExecutor.submit(() -> {
            try {
                AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();
                
                line.write(soundData, 0, soundData.length);
                line.drain();
                line.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    private static byte[] generateLaserSound() {
        // Duration: 0.1 seconds
        int numSamples = (int)(SAMPLE_RATE * 0.1);
        byte[] data = new byte[numSamples];
        
        // Generate a high-pitched sine wave that drops in frequency
        for (int i = 0; i < numSamples; i++) {
            double time = i / (double)SAMPLE_RATE;
            double frequency = 2000 - (i * 10);  // Dropping from 2000Hz to 1000Hz
            double value = Math.sin(2 * Math.PI * frequency * time);
            data[i] = (byte)(value * 127);
        }
        return data;
    }
    
    private static byte[] generateExplosionSound() {
        // Duration: 0.5 seconds
        int numSamples = (int)(SAMPLE_RATE * 0.5);
        byte[] data = new byte[numSamples];
        
        // Generate white noise that fades out
        for (int i = 0; i < numSamples; i++) {
            double fadeOut = 1.0 - (i / (double)numSamples);
            double noise = Math.random() * 2 - 1;
            data[i] = (byte)(noise * fadeOut * 127);
        }
        return data;
    }
    
    private static byte[] generateThrusterSound() {
        // Duration: 0.2 seconds
        int numSamples = (int)(SAMPLE_RATE * 0.2);
        byte[] data = new byte[numSamples];
        
        // Generate filtered noise for a whoosh effect
        for (int i = 0; i < numSamples; i++) {
            double time = i / (double)SAMPLE_RATE;
            double noise = Math.random();
            double frequency = 100 + (noise * 200);
            double value = Math.sin(2 * Math.PI * frequency * time);
            data[i] = (byte)(value * 127);
        }
        return data;
    }
} 