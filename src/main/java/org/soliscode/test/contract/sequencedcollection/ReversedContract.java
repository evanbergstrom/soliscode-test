package org.soliscode.test.contract.sequencedcollection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Iterator;
import java.util.List;
import java.util.SequencedCollection;

/// Test for the reversed method in the [SequencedCollection] interface. This contract class can be used individually
/// by a test class, but it is normally used through the [SequencedCollectionContract] class:
/// ```java
/// public class MyCollectionTest extends SequencedCollectionContract<Integer, MyCollection<Integer>> {
/// }
/// ```
/// If a test is using the SequencedCollectionContract class, but the class being tested does not implement the `reversed`
/// method based upon the specification in the `SequencedCollection` class, then it can be omitted from the tests using the
/// `doesNotSupportMethod()` method:
/// ```java
/// public class MyCollectionTest extends SequencedCollectionContract<Integer, MyCollection<Integer>> {
///     public MyCollectionTest() {
///         doesNotSupportMethod(SequencedCollectionMethods.Reversed);
///     }
/// }
/// ```
/// @param <E> The element type being tested.
/// @param <C> The element type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface ReversedContract<E, C extends SequencedCollection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the [reversed][SequencedCollection#reversed] method returns a view =of the collection with the
    /// elements in the reverse ordering.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see SequencedCollection#reversed
    @Test
    @DisplayName("the reverse methods returns a reversed view of the collection.")
    default void testReversed() {
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
