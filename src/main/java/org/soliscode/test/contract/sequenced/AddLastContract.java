package org.soliscode.test.contract.sequenced;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.CollectionMethods;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.List;
import java.util.SequencedCollection;

import static org.junit.jupiter.api.Assertions.*;

/// This interface tests if a class has implemented the `addLast()` method correctly based upon the specification in the
/// [SequencedCollection] class. This contract class can be used individually by a test class, but it is normally used
/// through the [SequencedCollectionContract] class:
/// ```java
/// public class MyCollectionTest extends SequencedCollectionContract<Integer, MyCollection<Integer>> {
/// }
/// ```
/// If a test is using the SequencedCollectionContract class, but the class being tested does not implement the `addLast`
/// method based upon the specification in the `SequencedCollection` class, then it can be omitted from the tests using the
/// `doesNotSupportMethod()` method:
/// ```java
/// public class MyCollectionTest extends SequencedCollectionContract<Integer, MyCollection<Integer>> {
///     public MyCollectionTest() {
///         doesNotSupportMethod(CollectionMethods.AddLast);
///     }
/// }
/// ```
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see SequencedCollection#addLast
/// @since 1.0
public interface AddLastContract<E, C extends SequencedCollection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the [addLast][SequencedCollection#addLast] method adds elements to the end of the collection.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see SequencedCollection#addLast
    @DisplayName("Test that the addLast method adds elements to the end of the collection")
    @Test
    default void testAddLast() {
        SequencedCollection<E> collection = provider().emptyInstance();
        if (supportsMethod(CollectionMethods.AddLast)) {
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            for (int i = 0; i < values.size(); i++) {
                E element = values.get(i);
                collection.addLast(element);
                assertTrue(collection.contains(element));
                assertEquals(i + 1, collection.size());
                assertEquals(collection.getLast(), element);
            }
        } else {
            assertThrows(UnsupportedOperationException.class, () -> collection.addLast(elementProvider().createInstance()));
        }
    }

    /// Tests that the [addFirst][SequencedCollection#addFirst] method handles null values correctly.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see SequencedCollection#addLast
    @DisplayName("Test that the addFirst method works with null element values")
    @Test
    default void testAddLastWithNullValue() {
        SequencedCollection<E> collection = provider().emptyInstance();
        if (supportsMethod(CollectionMethods.AddLast)) {
            if (permitNulls()) {
                collection.addLast(null);
                assertTrue(collection.contains(null));
                assertEquals(1, collection.size());
            } else {
                assertThrows(NullPointerException.class, () -> collection.addLast(null));
            }
        }
    }

    /// Tests that the [addFirst][SequencedCollection#addFirst] method handles duplicate values correctly.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see SequencedCollection#addLast
    @DisplayName("Test that the addFirst method works with duplicate element values")
    @Test
    default void addLastWithDuplicateValue() {
        if (supportsMethod(CollectionMethods.AddLast)) {
            if (permitDuplicates()) {
                List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
                SequencedCollection<E> collection = provider().createInstance(values);
                for (int i = 0; i < values.size(); i++) {
                    collection.addLast(values.get(i));
                    assertTrue(collection.contains(values.get(i)));
                    assertEquals(values.size() + i + 1, collection.size());
                }
            } else {
                SequencedCollection<E> collection = provider().emptyInstance();
                E value = elementProvider().createInstance();
                collection.addLast(value);
                E otherValue = elementProvider().copyInstance(value);
                collection.addLast(otherValue);
                assertEquals(1, collection.size());
            }
        }
    }
}
