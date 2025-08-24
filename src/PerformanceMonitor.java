import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Arrays;

/**
 * Performance monitoring utility for game optimization.
 * Tracks frame rates, memory usage, allocation rates, and performance metrics.
 * Thread-safe with minimal contention using ReadWriteLock.
 */
public class PerformanceMonitor {
    // Thread-safe locks and state
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static final long[] frameTimes = new long[GameConfig.Performance.SAMPLE_SIZE];
    private static int frameIndex = 0;
    private static final AtomicLong lastFrameTime = new AtomicLong(System.nanoTime());
    private static final AtomicLong lastMemoryCheckTime = new AtomicLong(System.nanoTime());
    private static double averageFPS = 0.0;
    private static double lastMemoryUsage = 0.0;
    private static double allocationRate = 0.0; // MB/s
    private static boolean isInitialized = false;

    // Runtime instance for memory monitoring
    private static final Runtime runtime = Runtime.getRuntime();

    // Callback for frame time outliers
    public interface FrameTimeOutlierCallback {
        void onFrameTimeOutlier(double frameTimeMs, double averageTimeMs);
    }
    private static FrameTimeOutlierCallback outlierCallback = null;

    /**
     * Records a frame completion and updates metrics.
     */
    public static void recordFrame() {
        long currentTime = System.nanoTime();
        long frameTime;

        lock.writeLock().lock();
        try {
            if (!isInitialized) {
                // Initialize array on first call
                Arrays.fill(frameTimes, GameConfig.FRAME_TIME_MS * 1_000_000L);
                isInitialized = true;
                lastFrameTime.set(currentTime);
                return;
            }

            frameTime = currentTime - lastFrameTime.get();
            if (frameTime > 0) { // Protect against negative frame times
                frameTimes[frameIndex] = frameTime;
                frameIndex = (frameIndex + 1) % GameConfig.Performance.SAMPLE_SIZE;
                lastFrameTime.set(currentTime);

                // Calculate average FPS and check for outliers
                long totalTime = 0;
                for (long time : frameTimes) {
                    totalTime += time;
                }
                if (totalTime > 0) {
                    double avgFrameTime = totalTime / (double) GameConfig.Performance.SAMPLE_SIZE;
                    averageFPS = 1e9 / avgFrameTime; // Convert ns to FPS

                    // Check for frame time outliers
                    if (outlierCallback != null && 
                        frameTime > avgFrameTime * GameConfig.Performance.FRAME_TIME_OUTLIER_THRESHOLD) {
                        outlierCallback.onFrameTimeOutlier(
                            frameTime / 1_000_000.0,  // Convert to ms
                            avgFrameTime / 1_000_000.0
                        );
                    }
                }
            }
        } finally {
            lock.writeLock().unlock();
        }

        // Update allocation rate periodically
        updateAllocationRate();
    }

    /**
     * Gets the current performance statistics snapshot.
     */
    public static PerfStats getStats() {
        lock.readLock().lock();
        try {
            return new PerfStats(
                averageFPS,
                getCurrentMemoryMB(),
                allocationRate
            );
        } finally {
            lock.readLock().unlock();
        }
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
     * Updates memory allocation rate (MB/s) based on usage delta.
     */
    private static void updateAllocationRate() {
        long currentTime = System.nanoTime();
        double currentMemory = getCurrentMemoryMB();

        lock.writeLock().lock();
        try {
            long timeDelta = currentTime - lastMemoryCheckTime.get();
            if (timeDelta > TimeUnit.SECONDS.toNanos(1)) { // Update once per second
                double memoryDelta = currentMemory - lastMemoryUsage;
                allocationRate = (memoryDelta / (timeDelta / 1e9)); // MB/s
                lastMemoryUsage = currentMemory;
                lastMemoryCheckTime.set(currentTime);
            }
        } finally {
            lock.writeLock().unlock();
        }
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

        if (memoryUsageRatio > GameConfig.Performance.HIGH_MEMORY_THRESHOLD) {
            System.gc();
            try {
                Thread.sleep(GameConfig.Performance.GC_PAUSE_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        // No need for runFinalization() - it's deprecated and less effective
    }

    /**
     * Sets a callback to be notified of frame time outliers.
     */
    public static void setFrameTimeOutlierCallback(FrameTimeOutlierCallback callback) {
        outlierCallback = callback;
    }

    /**
     * Immutable snapshot of performance statistics.
     */
    public static class PerfStats {
        private final double fps;
        private final double memoryUsageMB;
        private final double allocationRateMBs;

        private PerfStats(double fps, double memoryUsageMB, double allocationRateMBs) {
            this.fps = fps;
            this.memoryUsageMB = memoryUsageMB;
            this.allocationRateMBs = allocationRateMBs;
        }

        public double getFPS() { return fps; }
        public double getMemoryUsageMB() { return memoryUsageMB; }
        public double getAllocationRateMBs() { return allocationRateMBs; }

        @Override
        public String toString() {
            return String.format(
                "FPS: %.1f, Memory: %.1f MB, Allocation Rate: %.1f MB/s",
                fps, memoryUsageMB, allocationRateMBs
            );
        }
    }
}
