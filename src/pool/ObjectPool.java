package pool;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Thread-safe object pool implementation.
 * Uses ConcurrentLinkedQueue for lock-free pooling and atomic counters for metrics.
 *
 * @param <T> Type of objects to pool, must implement Reusable
 */
public class ObjectPool<T extends Reusable> {
    private final Queue<T> pool;
    private final Supplier<T> factory;
    private final int maxSize;
    
    // Metrics
    private final AtomicInteger size = new AtomicInteger(0);
    private final AtomicLong acquireCount = new AtomicLong(0);
    private final AtomicLong releaseCount = new AtomicLong(0);
    private final AtomicLong creationCount = new AtomicLong(0);
    private final AtomicLong reuseCount = new AtomicLong(0);

    /**
     * Creates a new object pool.
     *
     * @param factory Factory method to create new instances
     * @param initialSize Initial pool size
     * @param maxSize Maximum pool size (-1 for unlimited)
     */
    public ObjectPool(Supplier<T> factory, int initialSize, int maxSize) {
        this.factory = factory;
        this.maxSize = maxSize;
        this.pool = new ConcurrentLinkedQueue<>();
        
        // Pre-populate pool
        for (int i = 0; i < initialSize; i++) {
            T obj = factory.get();
            pool.offer(obj);
            size.incrementAndGet();
            creationCount.incrementAndGet();
        }
    }

    /**
     * Acquires an object from the pool or creates a new one if necessary.
     */
    public T acquire() {
        acquireCount.incrementAndGet();
        T obj = pool.poll();
        
        if (obj != null) {
            reuseCount.incrementAndGet();
            return obj;
        }
        
        // Create new object if under max size or if unlimited
        if (maxSize < 0 || size.get() < maxSize) {
            obj = factory.get();
            size.incrementAndGet();
            creationCount.incrementAndGet();
            return obj;
        }
        
        // Wait for an object to become available
        while ((obj = pool.poll()) == null) {
            Thread.onSpinWait();
        }
        
        reuseCount.incrementAndGet();
        return obj;
    }

    /**
     * Returns an object to the pool.
     */
    public void release(T obj) {
        if (obj == null) {
            return;
        }
        
        releaseCount.incrementAndGet();
        obj.reset();
        pool.offer(obj);
    }

    /**
     * Gets current pool metrics.
     */
    public PoolMetrics getMetrics() {
        return new PoolMetrics(
            size.get(),
            acquireCount.get(),
            releaseCount.get(),
            creationCount.get(),
            reuseCount.get()
        );
    }

    /**
     * Immutable snapshot of pool metrics.
     */
    public static class PoolMetrics {
        private final int currentSize;
        private final long totalAcquires;
        private final long totalReleases;
        private final long totalCreations;
        private final long totalReuses;

        public PoolMetrics(int size, long acquires, long releases,
                         long creations, long reuses) {
            this.currentSize = size;
            this.totalAcquires = acquires;
            this.totalReleases = releases;
            this.totalCreations = creations;
            this.totalReuses = reuses;
        }

        public int getCurrentSize() { return currentSize; }
        public long getTotalAcquires() { return totalAcquires; }
        public long getTotalReleases() { return totalReleases; }
        public long getTotalCreations() { return totalCreations; }
        public long getTotalReuses() { return totalReuses; }
        
        public double getReuseRatio() {
            return totalAcquires > 0 ? 
                (double) totalReuses / totalAcquires : 0.0;
        }

        @Override
        public String toString() {
            return String.format(
                "Pool[size=%d, acquires=%d, releases=%d, creates=%d, reuses=%d, reuseRatio=%.2f]",
                currentSize, totalAcquires, totalReleases, totalCreations,
                totalReuses, getReuseRatio()
            );
        }
    }
}
