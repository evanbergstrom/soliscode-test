package org.soliscode.test.contract.sequencedcollection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Iterator;
import java.util.List;
import java.util.SequencedCollection;

/// Contract for testing the [reversed][SequencedCollection#reversed] method of a [SequencedCollection].
///
/// ### Purpose
/// Verifies that `reversed()` returns a view of the collection with the elements in reverse order.
///
/// ### Usage Example
/// This contract is typically used through [SequencedCollectionContract]:
/// ```java
/// class MySequencedCollectionTest extends SequencedCollectionContract<String, MySequencedCollection<String>> {
///     // Inherits all reversed tests
/// }
/// ```
/// To exclude this contract if the method is not supported:
/// ```java
/// class MySequencedCollectionTest extends SequencedCollectionContract<String, MySequencedCollection<String>> {
///     public MySequencedCollectionTest() {
///         doesNotSupportMethod(SequencedCollectionMethods.REVERSED);
///     }
/// }
/// ```
///
/// ### Thread Safety
/// The tests in this contract are not thread-safe and should be run in a single-threaded environment
/// unless the underlying collection implementation specifically guarantees thread safety for these operations.
///
/// @param <E> The element type.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see SequencedCollection#reversed
/// @since 1.0.0
public interface ReversedContract<E, C extends SequencedCollection<E>> extends CollectionContractSupport<E, C> {

    /// Verifies that [reversed][SequencedCollection#reversed] returns a reversed view of the collection.
    ///
    /// The test follows these steps:
    /// 1. Creates a collection with multiple elements.
    /// 2. Calls `reversed()` and verifies that the resulting collection contains the same elements
    ///    but in the opposite order.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see SequencedCollection#reversed
    /// @since 1.0.0
    @DisplayName("reversed() returns a reversed view of the collection")
    @Test
    default void reversed_whenCalled_returnsReversedView() {
        if (supportsMethod(SequencedCollectionMethods.REVERSED)) {
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            SequencedCollection<E> collection = provider().createInstance(values);

            SequencedCollection<E> reversed = collection.reversed();
            Iterator<E> iterator = reversed.iterator();
            for (int i = values.size() - 1; i >= 0; i--) {
                Assertions.assertEquals(values.get(i), iterator.next());
            }
        }
    }
}
