package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.soliscode.test.annotations.ConcurrencyTest;
import org.soliscode.test.annotations.VerySlow;
import org.soliscode.test.safety.ContentionCoordinator;
import org.soliscode.test.util.RecordingSupplier;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContainsSame;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertIsEmpty;

/**
 * Contract for thread-safe add_singleElement_returnsTrueAndUpdatesSize operations on collections.
 *
 * @param <E> the type of elements in the collection
 * @param <C> the type of the collection
 */
public interface ThreadSafeAddContract<E, C extends Collection<E>>
        extends ThreadSafeCollectionContractSupport<E, C> {

    @DisplayName("add_singleElement_returnsTrueAndUpdatesSize(E) is thread safe under contention itself")
    @VerySlow
    @ConcurrencyTest
    default void add_whenCalledUnderContentionWithItself_IsThreadSafe() {
        C collection = provider().emptyInstance();
        if (supportsMethod(CollectionMethods.ADD)) {
            final RecordingSupplier<E> supplier = elementProvider().uniqueInstanceSupplier();
            int iterations = ContentionCoordinator.DEFAULT_ITERATIONS;

            ContentionCoordinator<C> coordinator = new ContentionCoordinator<>(2, 2, 100);
            coordinator.add((c) -> c.add(supplier.get()));
            coordinator.execute(collection);

            Collection<E> supplied = supplier.recorded();
            assertContainsSame(supplied, collection);
            assertCollectionStillWorks(collection);
        } else {
            assertThrows(UnsupportedOperationException.class, () -> collection.add(elementProvider().createInstance()));
        }
    }

    @DisplayName("add_singleElement_returnsTrueAndUpdatesSize(E) is thread safe under contention with remove(Object)")
    @VerySlow
    @ConcurrencyTest
    default void add_whenCalledUnderContentionWithRemove_IsThreadSafe() {
        C collection = provider().emptyInstance();
        if (supportsMethod(CollectionMethods.ADD) && supportsMethod(CollectionMethods.REMOVE)) {
            final RecordingSupplier<E> supplier = elementProvider().uniqueInstanceSupplier();

            ContentionCoordinator<C> coordinator = new ContentionCoordinator<>();
            coordinator.add((c) -> {
                E element = supplier.get();
                c.add(element);
                c.remove(element);
            });
            coordinator.execute(collection);

            assertIsEmpty(collection);
            assertCollectionStillWorks(collection);
        }
    }

    @DisplayName("add_singleElement_returnsTrueAndUpdatesSize(E) is thread safe under contention with removeAll(Collection)")
    @VerySlow
    @ConcurrencyTest
    default void add_whenCalledUnderContentionWithRemoveAll_IsThreadSafe() {
        C collection = provider().emptyInstance();
        if (supportsMethod(CollectionMethods.ADD) && supportsMethod(CollectionMethods.REMOVE)) {
            final RecordingSupplier<E> supplier = elementProvider().uniqueInstanceSupplier();

            ContentionCoordinator<C> coordinator = new ContentionCoordinator<>();
            coordinator.add((c) -> {
                E element = supplier.get();
                c.add(element);
                c.removeAll(Collections.singleton(element));
            });
            coordinator.execute(collection);

            assertIsEmpty(collection);
            assertCollectionStillWorks(collection);
        }
    }

    @DisplayName("add_singleElement_returnsTrueAndUpdatesSize(E) is thread safe under contention with removeIf(Predicate)")
    @VerySlow
    @ConcurrencyTest
    default void add_whenCalledUnderContentionWithRemoveIf_IsThreadSafe() {
        C collection = provider().emptyInstance();
        if (supportsMethod(CollectionMethods.ADD) && supportsMethod(CollectionMethods.REMOVE)) {
            final RecordingSupplier<E> supplier = elementProvider().uniqueInstanceSupplier();

            ContentionCoordinator<C> coordinator = new ContentionCoordinator<>();
            coordinator.add((c) -> {
                E element = supplier.get();
                c.add(element);
                c.removeIf((e) -> e.equals(element));
            });
            coordinator.execute(collection);

            assertIsEmpty(collection);
            assertCollectionStillWorks(collection);
        }
    }
}
