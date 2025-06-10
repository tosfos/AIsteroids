/**
 * Performance monitoring utility for game optimization.
 * Tracks frame rates, memory usage, and performance metrics.
 */
public class PerformanceMonitor {
    private static final int SAMPLE_SIZE = 60; // Track last 60 frames

    // Frame rate tracking
    private static final long[] frameTimes = new long[SAMPLE_SIZE];
    private static int frameIndex = 0;
    private static long lastFrameTime = System.nanoTime();
    private static double averageFPS = 0.0;

    // Memory tracking
    private static final Runtime runtime = Runtime.getRuntime();

    /**
     * Records a frame completion for FPS calculation.
     */
    public static void recordFrame() {
        long currentTime = System.nanoTime();
        frameTimes[frameIndex] = currentTime - lastFrameTime;
        frameIndex = (frameIndex + 1) % SAMPLE_SIZE;
        lastFrameTime = currentTime;

        // Calculate average FPS
        long totalTime = 0;
        for (long frameTime : frameTimes) {
            totalTime += frameTime;
        }
        if (totalTime > 0) {
            averageFPS = (double) SAMPLE_SIZE / (totalTime / 1e9);
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

        if (memoryUsageRatio > 0.8) { // If using more than 80% of available memory
            System.gc(); // Suggest garbage collection
        }
    }
}
