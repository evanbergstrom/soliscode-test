# How to write a thread-safety test

**Goal:** a test, bounded in time, that runs operations on your thread-safe class from many threads at once and fails if it loses an update, throws, deadlocks or hangs.

## Before you start

- SolisCode Test 0.1.6 or later
- A class that says it is thread-safe. For a collection or map, see step 4: a thread-safety contract may already do this for you

## Steps

1. Here the class is `HitCounter`, a counter of your own that says it is thread-safe:

   ```java
   import java.util.concurrent.atomic.AtomicLong;

   final class HitCounter {

       private final AtomicLong hits = new AtomicLong();

       void hit() {
           hits.incrementAndGet();
       }

       long hits() {
           return hits.get();
       }
   }
   ```

   Make a `ContentionCoordinator` for your class, and add each operation the threads run, with the number of threads and how many times each thread runs it:

   ```java
   import org.junit.jupiter.api.DisplayName;

   import com.soliscode.test.annotations.ConcurrencyTest;
   import com.soliscode.test.safety.ContentionCoordinator;

   import static org.junit.jupiter.api.Assertions.assertEquals;

   class HitCounterThreadSafetyTest {

       private static final int THREADS = 4;
       private static final int ITERATIONS = 10_000;

       @ConcurrencyTest
       @DisplayName("Hits counted on several threads at once are all counted")
       void hitsCountedConcurrentlyAreAllCounted() {
           ContentionCoordinator<HitCounter> coordinator = new ContentionCoordinator<>();
           coordinator.add(THREADS, ITERATIONS, HitCounter::hit);
           coordinator.add(THREADS, ITERATIONS, HitCounter::hits);

           HitCounter counter = new HitCounter();
           coordinator.execute(counter);

           assertEquals((long) THREADS * ITERATIONS, counter.hits());
       }
   }
   ```

   `execute` starts every thread, holds them at a barrier until all are ready, releases them together, and waits for them to finish. Each thread yields now and then, to make the threads interleave more.

2. After `execute`, assert what must hold whatever the interleaving was: here, that no hit was lost.

3. Mark the method `@ConcurrencyTest` (from `com.soliscode.test.annotations`) rather than `@Test`. It runs the test 5 times, since a race need not show on every run, gives each run a timeout of 10 seconds, and tags it `concurrency` and `nondeterministic`, so that a build can choose to run it or not.

   > **Note:** if the machine is busy, a thread can make all its calls before another starts, and the threads never contend. The four-argument constructor, `new ContentionCoordinator<>(startTimeoutSeconds, shutdownTimeoutSeconds, yieldInterval, phaseInterval)`, with a phase interval above 0, holds the threads together in phases of that many calls. In phase mode no call may wait for another thread's progress past the current phase.

4. For a collection or map, implement the thread-safety contract of its interface instead, or as well: `ThreadSafeCollectionContract`, `ThreadSafeSequencedCollectionContract`, `ThreadSafeBlockingQueueContract`, `ThreadSafeBlockingDequeContract`, `ThreadSafeTransferQueueContract` or `ThreadSafeConcurrentMapContract`. Each runs its interface's contract and adds tests of its main methods under contention: for a collection, `add`, `addAll`, `clear`, `retainAll` and the reads; for a queue, `put`, `take`, `offer`, `poll` and `drainTo`; for a concurrent map, its atomic methods. Here a synchronized `ForwardingList` is tested:

   ```java
   import java.util.Collection;
   import java.util.Collections;

   import com.soliscode.test.AbstractTest;
   import com.soliscode.test.contract.collection.ThreadSafeCollectionContract;
   import com.soliscode.test.contract.support.WithIntegerElement;
   import com.soliscode.test.methods.ObjectMethods;
   import com.soliscode.test.provider.CollectionProvider;
   import com.soliscode.test.provider.CollectionProviders;

   class SynchronizedListThreadSafetyTest extends AbstractTest
           implements ThreadSafeCollectionContract<Collection<Integer>, Integer>, WithIntegerElement {

       SynchronizedListThreadSafetyTest() {
           doesNotSupportMethod(ObjectMethods.EQUALS);
           doesNotSupportMethod(ObjectMethods.HASH_CODE);
       }

       @Override
       public CollectionProvider<Collection<Integer>, Integer> provider() {
           return CollectionProviders.wrap(
                   CollectionProviders.from(ForwardingList::new, ForwardingList::new, ForwardingList::new,
                           elementProvider()),
                   Collections::synchronizedCollection);
       }
   }
   ```

   The `ExecutorService`, `Future`, `Lock` and `ReadWriteLock` contracts have thread-safety forms too: `ThreadSafeExecutorServiceContract`, `ThreadSafeFutureContract`, `ThreadSafeLockContract` and `ThreadSafeReadWriteLockContract`.

## Check that it worked

The test passes in each of its five runs:

```text
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
```

To see that it can fail, make `HitCounter` unsafe (a plain `long` field, and `hits++` in `hit`): a run in which the threads interleave loses hits, and the assertion reports fewer than were made. A test that never fails for a known race is a test to make stronger: more iterations, more threads, or phases.

## Troubleshooting

| Symptom | Fix |
|---|---|
| `Timeout: 17 (of 18) workers didn't finish after 5 seconds` | The operations take longer than the shutdown timeout, or deadlock: check for a deadlock first, then make fewer iterations, or give the four-argument constructor a longer timeout. A contract's method that is slow on your class can be slow for the same reason: `AbstractList.clear()` removes elements one at a time, and under contention with `add` it does not finish |
| `Timeout: … workers were not ready … after 5 seconds` | The machine could not start the threads in time: fewer threads, or a longer start timeout |
| `… timed out after 10 seconds` | A run of the `@ConcurrencyTest` method took longer than its timeout: fewer iterations |

## See also

- [Error reference](../reference/errors.md#contentioncoordinator-timeouts)
- [Bounded thread-safety tests](../concepts/bounded-thread-safety-tests.md)
