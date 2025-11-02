import pool.ObjectPool;
import pool.Reusable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test suite for ObjectPool implementation.
 * Tests basic pooling, metrics, and thread safety.
 */
public class ObjectPoolTest {
    private static final double DELTA = 0.001;  // For floating point comparisons

    public static void main(String[] args) {
        testBasicPooling();
        testPoolMetrics();
        testMaxSize();
        testThreadSafety();
        testObjectReuse();
        testLargeScale();
        System.out.println("All ObjectPool tests passed! ✅");
    }

    private static void testBasicPooling() {
        System.out.println("Testing basic pooling...");
        
        TestObject.resetCreationCount();
        // Use a single-slot pool to guarantee immediate reuse of the same instance
        ObjectPool<TestObject> pool = new ObjectPool<>(TestObject::new, 0, 1);
        
        // Acquire (creates new since pool is empty)
        TestObject obj1 = pool.acquire();
        assert obj1 != null : "Should acquire a non-null object";
        
        // Release and ensure next acquire returns the same instance
        pool.release(obj1);
        TestObject obj2 = pool.acquire();
        
        assert obj2 == obj1 : "Should reuse the same object when pool size is 1";
        
        System.out.println("✓ Basic pooling test passed");
    }

    private static void testPoolMetrics() {
        System.out.println("Testing pool metrics...");
        
        ObjectPool<TestObject> pool = new ObjectPool<>(TestObject::new, 2, 5);
        ObjectPool.PoolMetrics initialMetrics = pool.getMetrics();
        
        assert initialMetrics.getCurrentSize() == 2 : "Initial size should be 2";
        assert initialMetrics.getTotalCreations() == 2 : "Should track initial creations";
        
        // Exercise pool
        TestObject obj = pool.acquire();
        ObjectPool.PoolMetrics metrics = pool.getMetrics();
        
        assert metrics.getTotalAcquires() == 1 : "Should track acquires";
        assert metrics.getTotalReuses() == 0 : "No reuses yet";
        
        pool.release(obj);
        obj = pool.acquire();
        metrics = pool.getMetrics();
        
        assert metrics.getTotalReuses() == 1 : "Should track reuses";
        assert metrics.getTotalReleases() == 1 : "Should track releases";
        
        System.out.println("✓ Pool metrics test passed");
    }

    private static void testMaxSize() {
        System.out.println("Testing max size constraint...");
        
        ObjectPool<TestObject> pool = new ObjectPool<>(TestObject::new, 1, 2);
        List<TestObject> objects = new ArrayList<>();
        
        // Acquire up to max size
        for (int i = 0; i < 2; i++) {
            objects.add(pool.acquire());
        }
        
        assert pool.getMetrics().getCurrentSize() == 2 : 
            "Pool should reach max size";
        
        // Try to acquire one more - would block, so do in separate thread
        Thread acquireThread = new Thread(() -> {
            TestObject obj = pool.acquire();
            objects.add(obj);
        });
        acquireThread.start();
        
        try {
            Thread.sleep(100); // Give acquire thread time to block
            assert acquireThread.isAlive() : "Thread should block on acquire";
            
            // Release one object
            pool.release(objects.remove(0));
            acquireThread.join(1000);
            
            assert !acquireThread.isAlive() : "Thread should complete after release";
            assert objects.size() == 2 : "Should acquire released object";
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        
        System.out.println("✓ Max size test passed");
    }

    private static void testThreadSafety() {
        System.out.println("Testing thread safety...");
        
        final int THREAD_COUNT = 10;
        final int ITERATIONS = 1000;
        final ObjectPool<TestObject> pool = new ObjectPool<>(TestObject::new, 5, 20);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);
        final AtomicInteger errors = new AtomicInteger(0);
        
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        
        // Each thread repeatedly acquires and releases objects
        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    
                    for (int j = 0; j < ITERATIONS; j++) {
                        TestObject obj = null;
                        try {
                            obj = pool.acquire();
                            assert obj != null : "Acquired object should not be null";
                            Thread.yield(); // Increase chance of thread interleaving
                        } catch (Exception e) {
                            errors.incrementAndGet();
                        } finally {
                            if (obj != null) {
                                pool.release(obj);
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    errors.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        
        // Start all threads simultaneously
        startLatch.countDown();
        
        try {
            // Wait for all threads to complete
            boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
            executor.shutdown();
            
            assert completed : "All threads should complete";
            assert errors.get() == 0 : "No errors should occur";
            
            ObjectPool.PoolMetrics metrics = pool.getMetrics();
            assert metrics.getTotalAcquires() == THREAD_COUNT * ITERATIONS :
                "Should track all acquires";
            assert metrics.getTotalReleases() == THREAD_COUNT * ITERATIONS :
                "Should track all releases";
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        
        System.out.println("✓ Thread safety test passed");
    }

    private static void testObjectReuse() {
        System.out.println("Testing object reuse...");
        
        TestObject.resetCreationCount();
        ObjectPool<TestObject> pool = new ObjectPool<>(TestObject::new, 1, 1);
        
        // Single object should be reused
        for (int i = 0; i < 100; i++) {
            TestObject obj = pool.acquire();
            assert obj.getValue() == 0 : "Object should be reset";
            obj.setValue(i);
            pool.release(obj);
        }
        
        assert TestObject.getCreationCount() == 1 : 
            "Should only create one object";
        assert pool.getMetrics().getReuseRatio() > 0.98 : 
            "Should have high reuse ratio";
            
        System.out.println("✓ Object reuse test passed");
    }

    private static void testLargeScale() {
        System.out.println("Testing large scale usage...");
        
        final int POOL_SIZE = 1000;
        TestObject.resetCreationCount();
        ObjectPool<TestObject> pool = new ObjectPool<>(TestObject::new, POOL_SIZE, POOL_SIZE);
        
        // Acquire all objects
        List<TestObject> objects = new ArrayList<>();
        for (int i = 0; i < POOL_SIZE; i++) {
            objects.add(pool.acquire());
        }
        
        assert pool.getMetrics().getCurrentSize() == POOL_SIZE :
            "Pool should maintain correct size";
        
        // Release all objects
        for (TestObject obj : objects) {
            pool.release(obj);
        }
        
        ObjectPool.PoolMetrics metrics = pool.getMetrics();
        assert metrics.getTotalAcquires() == POOL_SIZE :
            "Should track large number of acquires";
        assert metrics.getTotalReleases() == POOL_SIZE :
            "Should track large number of releases";
            
        System.out.println("✓ Large scale test passed");
    }

    /**
     * Test object implementation.
     */
    private static class TestObject implements Reusable {
        private static final AtomicInteger creationCount = new AtomicInteger(0);
        private int value;
        
        public TestObject() {
            creationCount.incrementAndGet();
        }
        
        public static int getCreationCount() {
            return creationCount.get();
        }
        
        public static void resetCreationCount() {
            creationCount.set(0);
        }
        
        public int getValue() {
            return value;
        }
        
        public void setValue(int value) {
            this.value = value;
        }
        
        @Override
        public void reset() {
            value = 0;
        }
    }
}
