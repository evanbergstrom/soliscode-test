package org.soliscode.test.contract.list;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.collection.AddContract;
import org.soliscode.test.contract.collection.CollectionMethods;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the `add` method of a `List`**
///
/// This interface defines tests for the [add(E)][List#add] method. It is designed
/// to be used as a mix-in interface by test classes that verify [List] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a list's `add` implementation correctly:
/// - Appends the specified element to the end of the list.
/// - Returns `true` if the list changed as a result of the call.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyListAddTest implements ListAddContract<String, MyList<String>> {
///     @Override
///     public CollectionProvider<String, MyList<String>> provider() {
///         return MyList::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [List] and [org.soliscode.test.provider.CollectionProvider] implementations being tested.
///
/// @param <E> The element type being tested.
/// @param <L> The list type being tested.
/// @author evanbergstrom
/// @see List#add(Object)
/// @since 1.0.0
public interface ListAddContract<E, L extends List<E>> extends AddContract<E, L> {

    /// Tests that the [add][List#add] method successfully appends elements to the end of the list.
    ///
    /// This test verifies that each call to `add(E)` places the new element at the end of the list.
    ///
    /// @see List#add(Object)
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Override
    @DisplayName("add(E) appends to the end of the list")
    @Test
    default void add_singleElement_returnsTrueAndUpdatesSize() {
        if (supportsMethod(CollectionMethods.ADD)) {
            List<E> list = provider().emptyInstance();
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            for (int i = 0; i < values.size(); i++) {
                E element = values.get(i);
                list.add(element);
                assertEquals(element, list.get(i));
            }
        } else {
            List<E> list = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, () -> list.add(elementProvider().createInstance()));
        }
    }
}
