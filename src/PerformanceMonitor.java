/**
 * Performance monitoring utility for game optimization.
 * Tracks frame rates, memory usage, and performance metrics.
 */
public class PerformanceMonitor {
    private static final int SAMPLE_SIZE = 60; // Track last 60 frames

    // Frame rate tracking
    private static final long[] frameTimes = new long[SAMPLE_SIZE];
    private static int frameIndex = 0;
    private static volatile long lastFrameTime = System.nanoTime();
    private static volatile double averageFPS = 0.0;
    private static boolean isInitialized = false;

    // Memory tracking
    private static final Runtime runtime = Runtime.getRuntime();

    /**
     * Records a frame completion for FPS calculation.
     */
    public static synchronized void recordFrame() {
        long currentTime = System.nanoTime();
        if (!isInitialized) {
            // Initialize array on first call
            for (int i = 0; i < SAMPLE_SIZE; i++) {
                frameTimes[i] = GameConfig.FRAME_TIME_MS * 1_000_000L; // Convert ms to ns
            }
            isInitialized = true;
            lastFrameTime = currentTime;
            return;
        }

        long frameTime = currentTime - lastFrameTime;
        if (frameTime > 0) { // Protect against negative frame times
            frameTimes[frameIndex] = frameTime;
            frameIndex = (frameIndex + 1) % SAMPLE_SIZE;
            lastFrameTime = currentTime;

            // Calculate average FPS
            long totalTime = 0;
            for (long time : frameTimes) {
                totalTime += time;
            }
            if (totalTime > 0) {
                averageFPS = (double) SAMPLE_SIZE / (totalTime / 1e9);
            }
        }
    }

    /**
     * Gets the current average FPS.
     */
    public static double getAverageFPS() {
        return averageFPS;
    }

    /**
     * Gets current memory usage in MB.
     */
    public static double getCurrentMemoryMB() {
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        return usedMemory / (1024.0 * 1024.0);
    }

    /**
     * Triggers garbage collection if memory usage is high.
     */
    public static void checkMemoryPressure() {
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        double memoryUsageRatio = (double) usedMemory / maxMemory;

        // More aggressive memory management
        if (memoryUsageRatio > 0.8) { // If using more than 80% of available memory
            System.gc(); // Suggest garbage collection
            try {
                Thread.sleep(10); // Give GC a chance to run
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else if (memoryUsageRatio > 0.7) { // At 70%, do a lighter GC suggestion
            runtime.runFinalization();
        }
    }
}
