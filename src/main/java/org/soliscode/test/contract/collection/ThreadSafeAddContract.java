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

/// **Contract for thread-safe `add` operations on collections**
///
/// This interface defines tests for the [add][Collection#add] method under concurrent access.
/// It is designed to be used as a mix-in interface by test classes that verify thread-safe
/// [Collection] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a collection's `add` implementation correctly
/// handles concurrent access from multiple threads, specifically:
/// - Concurrent `add` operations.
/// - `add` operations concurrent with `remove`.
/// - `add` operations concurrent with `removeAll`.
/// - `add` operations concurrent with `removeIf`.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyThreadSafeCollectionAddTest implements ThreadSafeAddContract<Integer, MyThreadSafeCollection<Integer>> {
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
/// @see Collection#add
/// @since 1.0.0
public interface ThreadSafeAddContract<E, C extends Collection<E>>
        extends ThreadSafeCollectionContractSupport<E, C> {

    /// Tests that the [add][Collection#add] method is thread-safe when called under contention with itself.
    ///
    /// This test verifies that if multiple threads call `add` simultaneously, all elements are
    /// correctly added to the collection.
    ///
    /// @since 1.0.0
    @DisplayName("add(E) is thread safe under contention with itself")
    @VerySlow
    @ConcurrencyTest
    default void add_whenCalledUnderContentionWithItself_isThreadSafe() {
        C collection = provider().emptyInstance();
        if (supportsMethod(CollectionMethods.ADD)) {
            final RecordingSupplier<E> supplier = elementProvider().uniqueInstanceSupplier();

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

    /// Tests that the [add][Collection#add] method is thread-safe when called under contention with `remove`.
    ///
    /// This test verifies that concurrent `add` and `remove` operations do not leave the collection
    /// in an inconsistent state.
    ///
    /// @since 1.0.0
    @DisplayName("add(E) is thread safe under contention with remove(Object)")
    @VerySlow
    @ConcurrencyTest
    default void add_whenCalledUnderContentionWithRemove_isThreadSafe() {
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

    /// Tests that the [add][Collection#add] method is thread-safe when called under contention with `removeAll`.
    ///
    /// This test verifies that concurrent `add` and `removeAll` operations do not leave the collection
    /// in an inconsistent state.
    ///
    /// @since 1.0.0
    @DisplayName("add(E) is thread safe under contention with removeAll(Collection)")
    @VerySlow
    @ConcurrencyTest
    default void add_whenCalledUnderContentionWithRemoveAll_isThreadSafe() {
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

    /// Tests that the [add][Collection#add] method is thread-safe when called under contention with `removeIf`.
    ///
    /// This test verifies that concurrent `add` and `removeIf` operations do not leave the collection
    /// in an inconsistent state.
    ///
    /// @since 1.0.0
    @DisplayName("add(E) is thread safe under contention with removeIf(Predicate)")
    @VerySlow
    @ConcurrencyTest
    default void add_whenCalledUnderContentionWithRemoveIf_isThreadSafe() {
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
