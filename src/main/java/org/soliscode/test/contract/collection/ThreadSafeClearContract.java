package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.soliscode.test.annotations.ConcurrencyTest;
import org.soliscode.test.annotations.Slow;
import org.soliscode.test.safety.ContentionCoordinator;

import java.util.Collection;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertIsEmpty;

public interface ThreadSafeClearContract<E, C extends Collection<E>>
        extends ThreadSafeCollectionContractSupport<E, C> {

    @DisplayName("clear() is thread safe on collection with elements")
    @Slow
    @ConcurrencyTest  // repeat to shake out flaky races
    default void clearIsSafeUnderContention() {
        C collection = provider().createSingleton(elementProvider().createInstance());
        if (supportsMethod(CollectionMethods.CLEAR)) {
            ContentionCoordinator<C> coordinator = new ContentionCoordinator<>();
            coordinator.add(Collection::clear);
            coordinator.execute(collection);

            assertIsEmpty(collection);
            assertCollectionStillWorks(collection);
        } else {
            assertThrows(UnsupportedOperationException.class, collection::clear);
        }
    }

    @DisplayName("clear() is thread safe under contention with add_singleElement_returnsTrueAndUpdatesSize(Object)")
    @Slow
    @ConcurrencyTest  // repeat to shake out flaky races
    default void clearIsSafeUnderContentionWithAdd() {
        C collection = provider().createSingleton(elementProvider().createInstance());
        if (supportsMethod(CollectionMethods.CLEAR)) {
            final Supplier<E> uniqueSupplier = elementProvider().uniqueInstanceSupplier();

            ContentionCoordinator<C> coordinator = new ContentionCoordinator<>();
            coordinator.add(Collection::clear);
            coordinator.add((c) -> c.add(uniqueSupplier.get()));
            coordinator.execute(collection);
            collection.clear();

            assertIsEmpty(collection);
            assertCollectionStillWorks(collection);
        } else {
            assertThrows(UnsupportedOperationException.class, collection::clear);
        }
    }
}
