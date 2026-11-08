package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.soliscode.test.annotations.ConcurrencyTest;
import org.soliscode.test.annotations.VerySlow;
import org.soliscode.test.safety.ContentionCoordinator;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContainsSame;

/// Thread-safety contract for [addAll(Collection)][Collection#addAll(Collection)].
///
/// @param <C> the type of collection
/// @param <E> the type of elements
/// @since 1.0
public interface ThreadSafeAddAllContract<C extends Collection<E>, E> extends ThreadSafeCollectionContractSupport<C, E> {

    /// Tests that no element is lost when threads call [addAll(Collection)][Collection#addAll(Collection)] at once.
    @DisplayName("addAll(Collection) is thread safe under contention with itself")
    @VerySlow
    @ConcurrencyTest
    default void addAll_whenCalledUnderContentionWithItself_isThreadSafe() {
        C collection = provider().emptyInstance();
        if (supportsMethod(CollectionMethods.ADD_ALL)) {
            Supplier<E> supplier = elementProvider().uniqueInstanceSupplier();
            Set<E> added = ConcurrentHashMap.newKeySet();
            ContentionCoordinator<C> coordinator = new ContentionCoordinator<>();
            coordinator.add((c) -> {
                E first = supplier.get();
                E second = supplier.get();
                added.add(first);
                added.add(second);
                c.addAll(java.util.List.of(first, second));
            });
            coordinator.execute(collection);

            assertContainsSame(added, collection);
            assertCollectionStillWorks(collection);
        }
    }
}
