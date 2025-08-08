package org.soliscode.test.contract.sequenced;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.CollectionMethods;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.assertions.Assertions.assertThrowsAny;

/// Test for the `removeLast method` in the [SequencedCollection] interface. This contract class can be used individually
/// by a test class, but it is normally used through the [SequencedCollectionContract] class:
/// ```java
/// public class MyCollectionTest extends SequencedCollectionContract<Integer, MyCollection<Integer>> {
/// }
/// ```
/// If a test is using the SequencedCollectionContract class, but the class being tested does not implement the `removeLast`
/// method based upon the specification in the `SequencedCollection` class, then it can be omitted from the tests using the
/// `doesNotSupportMethod()` method:
/// ```java
/// public class MyCollectionTest extends SequencedCollectionContract<Integer, MyCollection<Integer>> {
///     public MyCollectionTest() {
///         doesNotSupportMethod(CollectionMethods.RemoveLast);
///     }
/// }
/// ```
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface RemoveLastContract<E, C extends SequencedCollection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the [removeLast][SequencedCollection#removeLast] method works with a collection with elements.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see SequencedCollection#removeLast
    @DisplayName("Test that the removeLast method works")
    @Test
    default void testRemoveLast() {
        List<E> elements = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        if (supportsMethod(CollectionMethods.RemoveLast)) {
            SequencedCollection<E> collection = provider().emptyInstance();
            collection.addAll(elements);

            for (int i=elements.size()-1; i >= 0; i--) {
                E removed = collection.removeLast();
                assertEquals(elements.get(i), removed);
                assertFalse(collection.contains(removed));
            }
        } else {
            SequencedCollection<E> collection = provider().createInstance(elements);
            assertThrows(UnsupportedOperationException.class, collection::removeLast);
        }
    }

    /// Tests that the [removeLast][SequencedCollection#removeLast] method works for an empty collection.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see SequencedCollection#removeLast
    @DisplayName("Test that the removeLast method throws for an empty collection")
    @Test
    default void testRemoveLastOnEmptyCollection() {
        if (supportsMethod(CollectionMethods.RemoveLast)) {
            SequencedCollection<E> collection = provider().emptyInstance();
            assertThrows(NoSuchElementException.class, collection::removeLast);
        } else {
            SequencedCollection<E> collection = provider().emptyInstance();
            assertThrowsAny(List.of(UnsupportedOperationException.class, NoSuchElementException.class),
                    collection::removeLast);
        }
    }
}