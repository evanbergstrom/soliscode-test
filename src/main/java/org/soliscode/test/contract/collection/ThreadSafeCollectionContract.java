package org.soliscode.test.contract.collection;

import java.util.Collection;


/// **Contract for thread-safe `Collection` implementations**
/// This interface defines a comprehensive contract for testing the thread safety of
/// [Collection] implementations. It combines standard [CollectionContract] tests
/// with specialized tests for concurrent operations.
/// ## Purpose
/// The purpose of this contract is to ensure that a collection implementation correctly
/// handles concurrent access from multiple threads across various operations, including:
/// - Basic collection operations inherited from [CollectionContract].
/// - Thread-safe clear operations ([ThreadSafeClearContract]).
/// - Thread-safe add operations ([ThreadSafeAddContract]).
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
/// ```java
/// class MyThreadSafeCollectionTest implements ThreadSafeCollectionContract<Integer, MyThreadSafeCollection<Integer>> {
///     @Override
///     public CollectionProvider<Integer, MyThreadSafeCollection<Integer>> provider() {
///         return MyThreadSafeCollection::new;
///     }
/// }
/// ```
/// ## Thread Safety
/// This contract interface focuses on testing the thread safety of the [Collection] implementation.
/// The tests use [org.soliscode.test.safety.ContentionCoordinator] to simulate concurrent access.
///
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see Collection
/// @see CollectionContract
/// @see ThreadSafeClearContract
/// @see ThreadSafeAddContract
/// @since 1.0.0
public interface ThreadSafeCollectionContract<E, C extends Collection<E>> extends CollectionContract<E, C>,
        ThreadSafeClearContract<E, C>,
        ThreadSafeAddContract<E, C> {
}
