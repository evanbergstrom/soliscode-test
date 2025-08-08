package org.soliscode.test.contract.sequenced;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.CollectionMethods;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.assertions.Assertions.assertThrowsAny;

/// Test for the removeFirst method in the [SequencedCollection] interface. This contract class can be used individually
/// by a test class, but it is normally used through the [SequencedCollectionContract] class:
/// ```java
/// public class MyCollectionTest extends SequencedCollectionContract<Integer, MyCollection<Integer>> {
/// }
/// ```
/// If a test is using the SequencedCollectionContract class, but the class being tested does not implement the `removeFirst`
/// method based upon the specification in the `SequencedCollection` class, then it can be omitted from the tests using the
/// `doesNotSupportMethod()` method:
/// ```java
/// public class MyCollectionTest extends SequencedCollectionContract<Integer, MyCollection<Integer>> {
///     public MyCollectionTest() {
///         doesNotSupportMethod(CollectionMethods.RemoveFirst);
///     }
/// }
/// ```
/// @param <E> The element type.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface RemoveFirstContract<E, C extends SequencedCollection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the [removeFirst][SequencedCollection#removeFirst] method works.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see SequencedCollection#removeFirst
    @DisplayName("Test that the removeFirst method works")
    @Test
    default void testRemoveFirst() {
        List<E> elements = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        if (supportsMethod(CollectionMethods.RemoveFirst)) {
            SequencedCollection<E> collection =provider().emptyInstance();
            collection.addAll(elements);
            for (E element : elements) {
                E removed = collection.removeFirst();
                assertEquals(element, removed);
                assertFalse(collection.contains(removed));
            }
        } else {
            SequencedCollection<E> collection = provider().createInstance(elements);
            assertThrows(UnsupportedOperationException.class, collection::removeFirst);
        }
    }

    /// Tests that the [removeFirst][SequencedCollection#removeFirst] method works for an empty collection.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see SequencedCollection#removeFirst
    @DisplayName("Test that the removeFirst method throws for an empty collection")
    @Test
    default void testRemoveFirstOnEmptyCollection() {
        if (supportsMethod(CollectionMethods.RemoveFirst)) {
            SequencedCollection<E> collection = provider().emptyInstance();
            assertThrows(NoSuchElementException.class, collection::removeFirst);
        } else {
            SequencedCollection<E> collection = provider().emptyInstance();
            assertThrowsAny(List.of(UnsupportedOperationException.class, NoSuchElementException.class),
                    collection::removeFirst);
        }
    }
}