package org.soliscode.test.contract.sequencedcollection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.List;
import java.util.SequencedCollection;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `addLast` method of a `SequencedCollection`**
///
/// This interface defines tests for the [addLast][SequencedCollection#addLast] method. It is designed
/// to be used as a mix-in interface by test classes that verify [SequencedCollection] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a sequenced collection's `addLast` implementation correctly:
/// - Adds a single element to the back of the collection.
/// - Ensures the collection contains the added element.
/// - Ensures the added element is the last element in the collection.
/// - Updates the collection size appropriately.
/// - Handles null values according to the collection's configuration (permitting or rejecting nulls).
/// - Handles duplicate values according to the collection's configuration (permitting or ignoring duplicates).
/// - Throws [UnsupportedOperationException] if the method is not supported by the implementation.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MySequencedCollectionTest implements AddLastContract<Integer, MySequencedCollection<Integer>> {
///     @Override
///     public CollectionProvider<Integer, MySequencedCollection<Integer>> provider() {
///         return MySequencedCollection::new;
///     }
///
///     @Override
///     public ObjectProvider<Integer> elementProvider() {
///         return new IntegerProvider();
///     }
/// }
/// ```
///
/// If a test is using the [SequencedCollectionContract] class, but the class being tested does not implement the
/// `addLast` method, then it can be omitted from the tests using the `doesNotSupportMethod()` method:
///
/// ```java
/// public class MyCollectionTest extends SequencedCollectionContract<Integer, MyCollection<Integer>> {
///     public MyCollectionTest() {
///         doesNotSupportMethod(SequencedCollectionMethods.ADD_LAST);
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [SequencedCollection] and [org.soliscode.test.provider.CollectionProvider] implementations being tested.
///
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see SequencedCollection#addLast
/// @since 1.0.0
public interface AddLastContract<E, C extends SequencedCollection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the [addLast][SequencedCollection#addLast] method successfully adds an element to the back.
    ///
    /// This test verifies that:
    /// 1. A single element is added to the collection.
    /// 2. The collection contains the added element after the call.
    /// 3. The added element is the last element in the collection.
    /// 4. The size of the collection increases by 1.
    /// 5. If `addLast` is not supported, it verifies that [UnsupportedOperationException] is thrown.
    ///
    /// @see SequencedCollection#addLast
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("addLast(E) adds a single element to the back and updates size")
    default void addLast_singleElement_addsToBackAndUpdatesSize() {
        if (supportsMethod(SequencedCollectionMethods.ADD_LAST)) {
            SequencedCollection<E> collection = provider().emptyInstance();
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            for (int i = 0; i < values.size(); i++) {
                E element = values.get(i);
                collection.addLast(element);
                assertTrue(collection.contains(element));
                assertEquals(i + 1, collection.size());
                assertEquals(collection.getLast(), element);
            }
        } else {
            SequencedCollection<E> collection = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, () ->
                    collection.addLast(elementProvider().createInstance()));
        }
    }

    /// Tests that the [addLast][SequencedCollection#addLast] method handles `null` values correctly.
    ///
    /// This test verifies the behavior based on [CollectionContractSupport#permitNulls()]:
    /// - If `null` is permitted: Adding `null` to the back should succeed, and it should be the last element.
    /// - If `null` is not permitted: Adding `null` should throw [NullPointerException].
    ///
    /// @see SequencedCollection#addLast
    /// @throws NullPointerException if null is not permitted and the argument is null
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("addLast(E) handles null values based on permission")
    default void addLast_withNullValue_handlesCorrectly() {
        if (supportsMethod(SequencedCollectionMethods.ADD_LAST)) {
            SequencedCollection<E> collection = provider().emptyInstance();
            if (permitNulls()) {
                collection.addLast(null);
                assertTrue(collection.contains(null));
                assertEquals(1, collection.size());
                assertNull(collection.getLast());
            } else {
                assertThrows(NullPointerException.class, () -> collection.addLast(null));
            }
        }
    }

    /// Tests that the [addLast][SequencedCollection#addLast] method handles duplicate values correctly.
    ///
    /// This test verifies the behavior based on [CollectionContractSupport#permitDuplicates()]:
    /// - If duplicates are permitted: Adding an existing element to the back should increase the collection size
    ///   and it should become the last element.
    /// - If duplicates are not permitted: The behavior for [SequencedCollection#addLast] with duplicates
    ///   is implementation-dependent (some might move the element), but it should at least ensure size is correct
    ///   and the element is at the back.
    ///
    /// @see SequencedCollection#addLast
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("addLast(E) handles duplicate values based on permission")
    default void addLast_withDuplicateValue_handlesCorrectly() {
        if (supportsMethod(SequencedCollectionMethods.ADD_LAST)) {
            if (permitDuplicates()) {
                List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
                SequencedCollection<E> collection = provider().createInstance(values);
                for (int i = 0; i < values.size(); i++) {
                    E element = values.get(i);
                    collection.addLast(element);
                    assertTrue(collection.contains(element));
                    assertEquals(values.size() + i + 1, collection.size());
                    assertEquals(collection.getLast(), element);
                }
            } else {
                SequencedCollection<E> collection = provider().emptyInstance();
                E value = elementProvider().createInstance();
                collection.addLast(value);
                E otherValue = elementProvider().copyInstance(value);
                collection.addLast(otherValue);
                // For Set-based SequencedCollections, addLast might reorder or do nothing if already present.
                // The size should still be 1 if it's a Set.
                assertEquals(1, collection.size());
                assertEquals(collection.getLast(), otherValue);
            }
        }
    }
}
