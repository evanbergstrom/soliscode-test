package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.soliscode.test.annotations.ConcurrencyTest;
import org.soliscode.test.annotations.Slow;
import org.soliscode.test.safety.ContentionCoordinator;

import java.util.Collection;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertIsEmpty;

/// **Contract for thread-safe `clear` operations on collections**
///
/// This interface defines tests for the [clear][Collection#clear] method under concurrent access.
/// It is designed to be used as a mix-in interface by test classes that verify thread-safe
/// [Collection] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a collection's `clear` implementation correctly
/// handles concurrent access from multiple threads, specifically:
/// - Concurrent `clear` operations.
/// - `clear` operations concurrent with `add`.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyThreadSafeCollectionClearTest implements ThreadSafeClearContract<Integer, MyThreadSafeCollection<Integer>> {
///     @Override
///     public CollectionProvider<Integer, MyThreadSafeCollection<Integer>> provider() {
///         return MyThreadSafeCollection::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface focuses on testing the thread safety of the [Collection] implementation.
/// The tests use [ContentionCoordinator] to simulate concurrent access.
///
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see Collection#clear
/// @since 1.0.0
public interface ThreadSafeClearContract<E, C extends Collection<E>>
        extends ThreadSafeCollectionContractSupport<E, C> {

    /// Tests that the [clear][Collection#clear] method is thread-safe when called under contention.
    ///
    /// This test verifies that if multiple threads call `clear` simultaneously, the collection
    /// is correctly emptied.
    ///
    /// @since 1.0.0
    @DisplayName("clear() is thread safe on collection with elements")
    @Slow
    @ConcurrencyTest  // repeat to shake out flaky races
    default void clear_whenCalledUnderContention_isThreadSafe() {
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

    /// Tests that the [clear][Collection#clear] method is thread-safe when called under contention with `add`.
    ///
    /// This test verifies that concurrent `clear` and `add` operations do not leave the collection
    /// in an inconsistent state.
    ///
    /// @since 1.0.0
    @DisplayName("clear() is thread safe under contention with add(Object)")
    @Slow
    @ConcurrencyTest  // repeat to shake out flaky races
    default void clear_whenCalledUnderContentionWithAdd_isThreadSafe() {
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
