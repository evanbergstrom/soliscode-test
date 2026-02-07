package org.soliscode.test.safety;

import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.soliscode.test.assertions.AssertNoDeadlocks.enableDeadlockDetection;
import static org.soliscode.test.assertions.Assertions.assertNoDeadlocks;

/// **Coordinating Multi-threaded Contention Tests**
///
/// This class provides a mechanism to coordinate multiple threads executing operations on a shared subject
/// simultaneously. It is designed to expose race conditions and thread-safety issues by maximizing
/// contention between worker threads.
///
/// ## Purpose
/// The `ContentionCoordinator` allows testers to define various operations and the number of threads/iterations
/// for each. It then synchronizes these threads to start at the same time, ensuring high contention during
/// execution.
///
/// ## Effect
/// When executed, the coordinator:
/// 1. Creates a thread pool sized to accommodate all configured worker threads.
/// 2. Ensures all threads are initialized and waiting at a common barrier.
/// 3. Releases all threads simultaneously to maximize concurrent access to the subject.
/// 4. Periodically calls `Thread.yield()` within worker loops to increase the probability of race conditions.
/// 5. Collects any exceptions thrown by worker threads and reports them together.
///
/// ## Use Case
/// Use this class when testing classes that claim to be thread-safe under concurrent access,
/// especially when multiple different operations (e.g., `add_singleElement_returnsTrueAndUpdatesSize` and `remove`) are performed
/// simultaneously by many threads.
///
/// ## Usage Examples
/// ```java
///     ContentionCoordinator<MyThreadSafeClass> coordinator = new ContentionCoordinator<>();
///     coordinator.add_singleElement_returnsTrueAndUpdatesSize(MyThreadSafeClass::operationA);
///     coordinator.add_singleElement_returnsTrueAndUpdatesSize(4, 5000, MyThreadSafeClass::operationB);
///     coordinator.execute(new MyThreadSafeClass());
/// ```
///
/// ## Thread Safety
/// This class is thread-safe. It can be configured from multiple threads simultaneously by calling
/// the `add_singleElement_returnsTrueAndUpdatesSize` methods. The [ContentionCoordinator#execute] method can also be called concurrently,
/// although it is typically used for a single execution cycle.
///
/// @param <S> the type of the subject being tested
/// @author evanbergstrom
/// @since 1.0
public class ContentionCoordinator<S> {

    /// The default timeout for workers to become ready in seconds.
    ///
    /// This timeout governs how long the coordinator waits for all worker threads
    /// to initialize and reach the start barrier.
    public static final int DEFAULT_START_TIMEOUT_SECONDS = 5;

    /// The default timeout for workers to finish execution in seconds.
    ///
    /// This timeout governs how long the coordinator waits for all worker threads
    /// to complete their iterations after being signaled to start.
    public static final int DEFAULT_SHUTDOWN_TIMEOUT_SECONDS = 5;

    /// The default number of threads for an operation.
    ///
    /// When set to 0, the actual number of threads for each operation will be equal
    /// to available processors divided by the number of operations configured.
    /// Each operation will get at least one thread.
    public static final int DEFAULT_THREADS = 0;

    /// The default number of iterations for an operation.
    ///
    /// Each worker thread will execute its assigned operation this many times.
    public static final int DEFAULT_ITERATIONS = 30000;

    /// The interval at which threads will yield to increase contention.
    ///
    /// A value of 64 means `Thread.yield()` will be called every 64 iterations.
    public static final int DEFAULT_YIELD_INTERVAL = 64;

    /// The number of available processors on the current machine.
    public static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    private final int startTimeoutSeconds;
    private final int shutdownTimeoutSeconds;
    private final int yieldInterval;
    private final Collection<Operation> operations;

    private final class Operation {

        // Operations need to be immutable for coordinator to be thread safe.
        private final Consumer<S> consumer;
        private final int threads;
        private final int iterations;

        private Operation(final @NonNull Consumer<S> consumer, final int threads, final int iterations) {
            this.consumer = consumer;
            this.threads = threads;
            this.iterations = iterations;
        }

        public void execute(final @NonNull S subject) {
            for (int i = 0; i < iterations; i++) {
                if ((i & yieldInterval) == 0) {
                    Thread.yield();  // Yield to increase probability of contention
                }
                consumer.accept(subject);
            }
        }
    }

    /// Creates a `ContentionCoordinator` with default timeouts and yield interval.
    ///
    /// Uses [ContentionCoordinator#DEFAULT_START_TIMEOUT_SECONDS],
    /// [ContentionCoordinator#DEFAULT_SHUTDOWN_TIMEOUT_SECONDS],
    /// and [ContentionCoordinator#DEFAULT_YIELD_INTERVAL].
    ///
    /// @since 1.0
    public ContentionCoordinator() {
        this(DEFAULT_START_TIMEOUT_SECONDS, DEFAULT_SHUTDOWN_TIMEOUT_SECONDS, DEFAULT_YIELD_INTERVAL);
    }

    /// Creates a `ContentionCoordinator` with specified timeouts and yield interval.
    ///
    /// @param startTimeoutSeconds    the maximum time in seconds to wait for workers to be ready
    /// @param shutdownTimeoutSeconds the maximum time in seconds to wait for workers to finish
    /// @param yieldInterval          the interval in milliseconds at which worker threads should yield
    /// @since 1.0
    public ContentionCoordinator(final int startTimeoutSeconds, final int shutdownTimeoutSeconds,
                                 final int yieldInterval) {
        this.startTimeoutSeconds = startTimeoutSeconds;
        this.shutdownTimeoutSeconds = shutdownTimeoutSeconds;
        this.yieldInterval = yieldInterval;
        this.operations = new ConcurrentLinkedQueue<>();
    }

    /// Adds an operation to be executed using default thread count and iterations.
    ///
    /// This method is thread-safe and can be called from multiple threads.
    /// Uses [ContentionCoordinator#DEFAULT_THREADS] and [ContentionCoordinator#DEFAULT_ITERATIONS].
    ///
    /// @param consumer the operation to perform on the subject
    /// @throws NullPointerException if `consumer` is null
    /// @since 1.0
    public void add(final @NonNull Consumer<S> consumer) {
        add(DEFAULT_THREADS, DEFAULT_ITERATIONS, consumer);
    }

    /// Adds an operation to be executed with specific thread count and iterations.
    ///
    /// This method is thread-safe and can be called from multiple threads.
    ///
    /// @param threads    the number of threads to use for this operation
    /// @param iterations the number of times each thread should execute the operation
    /// @param consumer   the operation to perform on the subject
    /// @throws NullPointerException if `consumer` is null
    /// @since 1.0
    public void add(final int threads, final int iterations, final @NonNull Consumer<S> consumer) {
        operations.add(new Operation(consumer, threads, iterations));
    }

    /// Executes all configured operations on the provided subject.
    ///
    /// This method will:
    /// 1. Create a thread pool with the total number of configured threads.
    /// 2. Initialize all worker threads and wait for them to be ready.
    /// 3. Signal all threads to start simultaneously.
    /// 4. Wait for all threads to complete within the shutdown timeout.
    /// 5. Collect and report any failures that occurred in worker threads.
    ///
    /// @param subject the object to perform operations on
    /// @throws AssertionError if workers fail to start or finish in time, or if any worker thread throws an exception
    /// @throws NullPointerException if `subject` is null
    /// @since 1.0
    public void execute(final @NonNull S subject) {
        java.util.Objects.requireNonNull(subject);
        enableDeadlockDetection();

        // Collect *all* failures from worker threads
        ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();

        // Calculate the number of threads per operation
        final int defaultThreads = Math.max(1, NUMBER_OF_CORES / operations.size());
        final int totalThreads = operations.stream().mapToInt(o -> (o.threads == 0) ? defaultThreads : o.threads).sum();

        // Create latches that facilitate thread contention
        final CountDownLatch ready = new CountDownLatch(totalThreads);  // Make sure all threads are ready
        final CountDownLatch start = new CountDownLatch(1);             // Start all operations simultaneously
        final CountDownLatch done = new CountDownLatch(totalThreads);   // Wait for all threads to finish

        ExecutorService pool = Executors.newFixedThreadPool(totalThreads, createThreadFactory());
        try (pool) {
            operations.forEach((op) -> {
                int workerThreads = (op.threads == 0) ? defaultThreads : op.threads;
                for (int t = 0; t < workerThreads; t++) {
                    pool.submit(() -> {
                        ready.countDown();  // indicate that this thread is ready to start
                        try {
                            start.await();  // Wait for all threads to start
                            op.execute(subject);
                        } catch (Throwable e) {
                            errors.add(e);
                        } finally {
                            done.countDown();  // indicate that this thread is done
                        }
                    });
                }
            });

            // Wait for all the thready to be ready.
            assertTrue(ready.await(startTimeoutSeconds, TimeUnit.SECONDS),
                    "Timeout: " + ready.getCount() + " (of " + totalThreads  + ") workers were not ready  after "
                    + startTimeoutSeconds + " seconds");

            // Start operations as simultaneously as possible.
            start.countDown();

            // Wait for all the thready to be done.
            if (!done.await(shutdownTimeoutSeconds, TimeUnit.SECONDS)) {
                assertNoDeadlocks();
                throw new AssertionError("Timeout: " + done.getCount() + " (of " + totalThreads
                        + ") workers didn't finish after " + shutdownTimeoutSeconds + " seconds");
            }
        } catch (InterruptedException e) {
            throw new AssertionError("Interrupted while waiting for threads to finish", e);
        } finally {
            insureShutdown(pool);
        }

        if (!errors.isEmpty()) {
            AssertionError ae = new AssertionError("Operations failed: " + errors.size());
            errors.forEach(ae::addSuppressed);
            throw ae;
        }
    }

    private ThreadFactory createThreadFactory() {
        return new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(0);
            public Thread newThread(final @NonNull Runnable r) {
                return new Thread(r, "ContentionCoordinator-" + threadNumber.getAndIncrement());
            }
        };
    }

    private void insureShutdown(final @NonNull ExecutorService pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(shutdownTimeoutSeconds, TimeUnit.SECONDS)) {
                assertNoDeadlocks();
                pool.shutdownNow();
            }
        } catch (InterruptedException ie) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
