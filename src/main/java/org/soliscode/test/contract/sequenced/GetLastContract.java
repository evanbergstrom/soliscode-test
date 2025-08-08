package org.soliscode.test.contract.sequenced;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.CollectionMethods;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Test for the getFirst method in the [SequencedCollection] interface. This contract class can be used individually
/// by a test class, but it is normally used through the [SequencedCollectionContract] class:
/// ```java
/// public class MyCollectionTest extends SequencedCollectionContract<Integer, MyCollection<Integer>> {
/// }
/// ```
/// If a test is using the SequencedCollectionContract class, but the class being tested does not implement the `getLast`
/// method based upon the specification in the `SequencedCollection` class, then it can be omitted from the tests using the
/// `doesNotSupportMethod()` method:
/// ```java
/// public class MyCollectionTest extends SequencedCollectionContract<Integer, MyCollection<Integer>> {
///     public MyCollectionTest() {
///         doesNotSupportMethod(CollectionMethods.GetLast);
///     }
/// }
/// ```
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface GetLastContract<E, C extends SequencedCollection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the [getLast][SequencedCollection#getLast] method works.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see SequencedCollection#getLast
    @DisplayName("Test that the getLast method works")
    @Test
    default void testGetLast() {
        if (supportsMethod(CollectionMethods.GetLast)) {
            List<E> elements = elementProvider().createUniqueInstances(2);
            SequencedCollection<E> collection = provider().createInstance(elements);
            assertEquals(elements.get(1), collection.getLast());
        }
    }

    /// Tests that the [getLast][SequencedCollection#getLast] method works on an empty collection.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see SequencedCollection#getLast
    @DisplayName("Test that the getLast method throws for an empty collection")
    @Test
    default void testGetLastOnEmptyCollection() {
        if (supportsMethod(CollectionMethods.GetLast)) {
            SequencedCollection<E> collection = provider().emptyInstance();
            assertThrows(NoSuchElementException.class, collection::getLast);
        }
    }
}
