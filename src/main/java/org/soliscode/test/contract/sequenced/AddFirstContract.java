package org.soliscode.test.contract.sequenced;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.CollectionMethods;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.List;
import java.util.SequencedCollection;

import static org.junit.jupiter.api.Assertions.*;

/// This interface tests if a class has implemented the `addFirst()` method correctly based upon the specification in the
/// [SequencedCollection] class. This contract class can be used individually by a test class, but it is normally used
/// through the [SequencedCollectionContract] class:
/// ```java
/// public class MyCollectionTest extends SequencedCollectionContract<Integer, MyCollection<Integer>> {
/// }
/// ```
/// If a test is using the SequencedCollectionContract class, but the class being tested does not implement the `addFirst`
/// method based upon the specification in the `SequencedCollection` class, then it can be omitted from the tests using the
/// `doesNotSupportMethod()` method:
/// ```java
/// public class MyCollectionTest extends SequencedCollectionContract<Integer, MyCollection<Integer>> {
///     public MyCollectionTest() {
///         doesNotSupportMethod(CollectionMethods.AddFirst);
///     }
/// }
/// ```
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see SequencedCollection#addLast
/// @since 1.0
public interface AddFirstContract<E, C extends SequencedCollection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the [addFirst][SequencedCollection#addFirst] method works.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see SequencedCollection#addFirst
    @Test
    @DisplayName("The addFirst method works")
    default void testAddFirst() {
        if (supportsMethod(CollectionMethods.AddFirst)) {
            SequencedCollection<E> collection = provider().emptyInstance();
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            for (int i = 0; i < values.size(); i++) {
                E element = values.get(i);
                collection.addFirst(element);
                assertTrue(collection.contains(element));
                assertEquals(i + 1, collection.size());
                assertEquals(collection.getFirst(), element);
            }
        } else {
            SequencedCollection<E> collection = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, () -> collection.addFirst(elementProvider().createInstance()));
        }
    }

    /// Tests that the [addFirst][SequencedCollection#addFirst] method handles null values correctly.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see SequencedCollection#addFirst
    @Test
    @DisplayName("The addFirst method works with null element values")
    default void testAddFirstWithNullValue() {
        if (supportsMethod(CollectionMethods.Add)) {
            SequencedCollection<E> collection = provider().emptyInstance();
            if (permitNulls()) {
                collection.addFirst(null);
                assertTrue(collection.contains(null));
                assertEquals(1, collection.size());
            } else {
                assertThrows(NullPointerException.class, () -> collection.addFirst(null));
            }
        }
    }

    /// Tests that the [addFirst][SequencedCollection#addFirst] method handles duplicate values correctly.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see SequencedCollection#addFirst
    @Test
    @DisplayName("The addFirst method works with duplicate element values")
    default void addFirstWithDuplicateValue() {
        if (supportsMethod(CollectionMethods.Add)) {
            if (permitDuplicates()) {
                List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
                SequencedCollection<E> collection = provider().createInstance(values);
                for (int i = 0; i < values.size(); i++) {
                    collection.addFirst(values.get(i));
                    assertTrue(collection.contains(values.get(i)));
                    assertEquals(values.size() + i + 1, collection.size());
                }
            } else {
                SequencedCollection<E> collection = provider().emptyInstance();
                E value = elementProvider().createInstance();
                collection.addFirst(value);
                E otherValue = elementProvider().copyInstance(value);
                collection.addFirst(otherValue);
                assertEquals(1, collection.size());
            }
        }
    }
}
