package org.soliscode.test.contract.sequencedcollection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.List;
import java.util.SequencedCollection;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `addFirst` method of a `SequencedCollection`**
///
/// This interface defines tests for the [addFirst][SequencedCollection#addFirst] method. It is designed
/// to be used as a mix-in interface by test classes that verify [SequencedCollection] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a sequenced collection's `addFirst` implementation correctly:
/// - Adds a single element to the front of the collection.
/// - Ensures the collection contains the added element.
/// - Ensures the added element is the first element in the collection.
/// - Updates the collection size appropriately.
/// - Handles null values according to the collection's configuration (permitting or rejecting nulls).
/// - Handles duplicate values according to the collection's configuration (permitting or ignoring duplicates).
/// - Throws [UnsupportedOperationException] if the method is not supported by the implementation.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MySequencedCollectionTest implements AddFirstContract<Integer, MySequencedCollection<Integer>> {
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
/// `addFirst` method, then it can be omitted from the tests using the `doesNotSupportMethod()` method:
///
/// ```java
/// public class MyCollectionTest extends SequencedCollectionContract<Integer, MyCollection<Integer>> {
///     public MyCollectionTest() {
///         doesNotSupportMethod(SequencedCollectionMethods.ADD_FIRST);
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
/// @see SequencedCollection#addFirst
/// @since 1.0.0
public interface AddFirstContract<E, C extends SequencedCollection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the [addFirst][SequencedCollection#addFirst] method successfully adds an element to the front.
    ///
    /// This test verifies that:
    /// 1. A single element is added to the collection.
    /// 2. The collection contains the added element after the call.
    /// 3. The added element is the first element in the collection.
    /// 4. The size of the collection increases by 1.
    /// 5. If `addFirst` is not supported, it verifies that [UnsupportedOperationException] is thrown.
    ///
    /// @see SequencedCollection#addFirst
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("addFirst(E) adds a single element to the front and updates size")
    default void addFirst_singleElement_addsToFrontAndUpdatesSize() {
        if (supportsMethod(SequencedCollectionMethods.ADD_FIRST)) {
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
            assertThrows(UnsupportedOperationException.class, () ->
                    collection.addFirst(elementProvider().createInstance()));
        }
    }

    /// Tests that the [addFirst][SequencedCollection#addFirst] method handles `null` values correctly.
    ///
    /// This test verifies the behavior based on [CollectionContractSupport#permitNulls()]:
    /// - If `null` is permitted: Adding `null` to the front should succeed, and it should be the first element.
    /// - If `null` is not permitted: Adding `null` should throw [NullPointerException].
    ///
    /// @see SequencedCollection#addFirst
    /// @throws NullPointerException if null is not permitted and the argument is null
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("addFirst(E) handles null values based on permission")
    default void addFirst_withNullValue_handlesCorrectly() {
        if (supportsMethod(SequencedCollectionMethods.ADD_FIRST)) {
            SequencedCollection<E> collection = provider().emptyInstance();
            if (permitNulls()) {
                collection.addFirst(null);
                assertTrue(collection.contains(null));
                assertEquals(1, collection.size());
                assertNull(collection.getFirst());
            } else {
                assertThrows(NullPointerException.class, () -> collection.addFirst(null));
            }
        }
    }

    /// Tests that the [addFirst][SequencedCollection#addFirst] method handles duplicate values correctly.
    ///
    /// This test verifies the behavior based on [CollectionContractSupport#permitDuplicates()]:
    /// - If duplicates are permitted: Adding an existing element to the front should increase the collection size
    ///   and it should become the first element.
    /// - If duplicates are not permitted: The behavior for [SequencedCollection#addFirst] with duplicates
    ///   is implementation-dependent (some might move the element), but it should at least ensure size is correct
    ///   and the element is at the front.
    ///
    /// @see SequencedCollection#addFirst
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("addFirst(E) handles duplicate values based on permission")
    default void addFirst_withDuplicateValue_handlesCorrectly() {
        if (supportsMethod(SequencedCollectionMethods.ADD_FIRST)) {
            if (permitDuplicates()) {
                List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
                SequencedCollection<E> collection = provider().createInstance(values);
                for (int i = 0; i < values.size(); i++) {
                    E element = values.get(i);
                    collection.addFirst(element);
                    assertTrue(collection.contains(element));
                    assertEquals(values.size() + i + 1, collection.size());
                    assertEquals(collection.getFirst(), element);
                }
            } else {
                SequencedCollection<E> collection = provider().emptyInstance();
                E value = elementProvider().createInstance();
                collection.addFirst(value);
                E otherValue = elementProvider().copyInstance(value);
                collection.addFirst(otherValue);
                // For Set-based SequencedCollections, addFirst might reorder or do nothing if already present.
                // The size should still be 1 if it's a Set.
                assertEquals(1, collection.size());
                assertEquals(collection.getFirst(), otherValue);
            }
        }
    }
}
