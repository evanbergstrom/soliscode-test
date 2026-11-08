package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.soliscode.test.annotations.ConcurrencyTest;
import org.soliscode.test.annotations.VerySlow;
import org.soliscode.test.safety.ContentionCoordinator;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContainsSame;

/// Thread-safety contract for [retainAll(Collection)][Collection#retainAll(Collection)].
///
/// @param <C> the type of collection
/// @param <E> the type of elements
/// @since 1.0
public interface ThreadSafeRetainAllContract<C extends Collection<E>, E>
        extends ThreadSafeCollectionContractSupport<C, E> {

    /// How many times ({@value}) each thread of the retainAll test runs its operation.
    int RETAIN_ALL_ITERATIONS = 150;

    /// How many elements ({@value}) there are to keep, and as many to drop: more than the threads add.
    int RETAIN_ALL_POOL = 600;

    /// Tests that [retainAll(Collection)][Collection#retainAll(Collection)], under contention with [add(E)][Collection#add],
    /// removes only the elements it should: every element added to keep is still there at the end.
    @DisplayName("retainAll(Collection) is thread safe under contention with add(E)")
    @VerySlow
    @ConcurrencyTest
    default void retainAll_whenCalledUnderContentionWithAdd_isThreadSafe() {
        C collection = provider().emptyInstance();
        if (supportsMethod(CollectionMethods.RETAIN_ALL) && supportsMethod(CollectionMethods.ADD)) {
            // The elements to keep are fixed before the threads start: retainAll may read its argument once, so it
            // must not change while retainAll runs.
            List<E> pool = elementProvider().createUniqueInstances(2 * RETAIN_ALL_POOL);
            Set<E> keep = Set.copyOf(pool.subList(0, RETAIN_ALL_POOL));
            Queue<E> toKeep = new ConcurrentLinkedQueue<>(pool.subList(0, RETAIN_ALL_POOL));
            Queue<E> toDrop = new ConcurrentLinkedQueue<>(pool.subList(RETAIN_ALL_POOL, 2 * RETAIN_ALL_POOL));
            Set<E> kept = ConcurrentHashMap.newKeySet();

            // Fewer iterations than the default: each retainAll scans the whole collection.
            ContentionCoordinator<C> coordinator = new ContentionCoordinator<>();
            coordinator.add(0, RETAIN_ALL_ITERATIONS, (c) -> {
                E e = toKeep.poll();
                if (e != null) {
                    kept.add(e);
                    c.add(e);
                }
            });
            coordinator.add(0, RETAIN_ALL_ITERATIONS, (c) -> {
                E e = toDrop.poll();
                if (e != null) {
                    c.add(e);
                }
            });
            coordinator.add(0, RETAIN_ALL_ITERATIONS, (c) -> c.retainAll(keep));
            coordinator.execute(collection);

            collection.retainAll(keep);
            assertContainsSame(kept, collection);
            assertCollectionStillWorks(collection);
        }
    }
}
