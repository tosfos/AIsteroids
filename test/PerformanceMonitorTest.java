/**
 * Test suite for PerformanceMonitor functionality.
 * Tests FPS calculation, memory tracking, and outlier detection.
 */
public class PerformanceMonitorTest {
    private static final double DELTA = 0.001; // For floating point comparisons

    private static class TestOutlierCallback implements PerformanceMonitor.FrameTimeOutlierCallback {
        private double lastFrameTime;
        private double lastAverageTime;
        private int callCount = 0;

        @Override
        public void onFrameTimeOutlier(double frameTimeMs, double averageTimeMs) {
            lastFrameTime = frameTimeMs;
            lastAverageTime = averageTimeMs;
            callCount++;
        }

        public void reset() {
            lastFrameTime = 0;
            lastAverageTime = 0;
            callCount = 0;
        }
    }

    private static void sleepPrecise(long millis) {
        long start = System.nanoTime();
        long targetNanos = start + (millis * 1_000_000L);
        while (System.nanoTime() < targetNanos) {
            Thread.yield();
        }
    }

    public static void main(String[] args) {
        testFPSCalculation();
        testMemoryMonitoring();
        testOutlierDetection();
        testAllocationRateTracking();
        System.out.println("All PerformanceMonitor tests passed! ✅");
    }

    private static void testFPSCalculation() {
        System.out.println("Testing FPS calculation...");
        
        // Reset state by recording initial frame
        PerformanceMonitor.recordFrame();
        
        // Simulate 60 FPS - sleep for ~16.67ms between frames
        for (int i = 0; i < 60; i++) {
            sleepPrecise(17); // Slightly longer than 16.67ms to account for overhead
            PerformanceMonitor.recordFrame();
        }

        PerformanceMonitor.PerfStats stats = PerformanceMonitor.getStats();
        double fps = stats.getFPS();
        
        // Allow some variance due to system timing
        assert fps >= 55.0 && fps <= 65.0 : 
            String.format("FPS should be close to 60, got %.2f", fps);
        
        System.out.println("✓ FPS calculation test passed");
    }

    private static void testMemoryMonitoring() {
        System.out.println("Testing memory monitoring...");
        
        // Allocate some memory to test monitoring
        byte[] data = new byte[10 * 1024 * 1024]; // 10MB
        double memoryUsage = PerformanceMonitor.getCurrentMemoryMB();
        
        assert memoryUsage > 0 : "Memory usage should be positive";
        assert memoryUsage < Runtime.getRuntime().maxMemory() / (1024.0 * 1024.0) :
            "Memory usage should be less than max memory";
            
        System.out.println("✓ Memory monitoring test passed");
        
        // Clear reference to allow GC
        data = null;
        System.gc();
    }

    private static void testOutlierDetection() {
        System.out.println("Testing frame time outlier detection...");
        
        TestOutlierCallback callback = new TestOutlierCallback();
        PerformanceMonitor.setFrameTimeOutlierCallback(callback);
        
        // Record normal frames
        for (int i = 0; i < 10; i++) {
            sleepPrecise(17);
            PerformanceMonitor.recordFrame();
        }
        
        // Record an outlier frame (simulate spike)
        sleepPrecise(50); // Much longer frame
        PerformanceMonitor.recordFrame();
        
        assert callback.callCount > 0 : "Outlier callback should have been triggered";
        assert callback.lastFrameTime > 45 : 
            String.format("Outlier frame time should be >45ms, got %.2f", callback.lastFrameTime);
        assert callback.lastAverageTime < 20 : 
            String.format("Average frame time should be <20ms, got %.2f", callback.lastAverageTime);
            
        System.out.println("✓ Outlier detection test passed");
    }

    private static void testAllocationRateTracking() {
        System.out.println("Testing allocation rate tracking...");
        
        // Reset allocation tracking
        PerformanceMonitor.recordFrame();
        sleepPrecise(1000); // Wait 1s for initial rate calculation
        
        // Get initial stats
        PerformanceMonitor.PerfStats initialStats = PerformanceMonitor.getStats();
        
        // Allocate memory over time
        long startTime = System.nanoTime();
        byte[][] allocations = new byte[5][];
        for (int i = 0; i < 5; i++) {
            allocations[i] = new byte[10 * 1024 * 1024]; // 10MB each
            sleepPrecise(200); // Space allocations over 1 second
            PerformanceMonitor.recordFrame();
        }
        
        // Wait for rate calculation to update
        sleepPrecise(1000);
        PerformanceMonitor.recordFrame();
        
        PerformanceMonitor.PerfStats finalStats = PerformanceMonitor.getStats();
        double rate = finalStats.getAllocationRateMBs();
        
        assert rate > 0 : String.format("Allocation rate should be positive, got %.2f MB/s", rate);
        
        // Clear references to allow GC
        allocations = null;
        System.gc();
        
        System.out.println("✓ Allocation rate tracking test passed");
    }
}
