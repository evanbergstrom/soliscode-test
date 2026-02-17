package org.soliscode.test.contract.deque;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `removeFirstOccurrence` method of a `Deque`**
/// This interface defines tests for the [removeFirstOccurrence()][Deque#removeFirstOccurrence] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Deque] implementations.
///
/// @param <E> The element type being tested.
/// @param <D> The deque type being tested.
/// @author evanbergstrom
/// @see Deque#removeFirstOccurrence
/// @since 1.0.0
public interface RemoveFirstOccurrenceContract<E, D extends Deque<E>> extends CollectionContractSupport<E, D> {

    /// Tests that the [removeFirstOccurrence][Deque#removeFirstOccurrence] method correctly removes the first occurrence.
    @DisplayName("removeFirstOccurrence(Object) removes the first occurrence of the specified element")
    @Test
    default void removeFirstOccurrence_whenPresent_removesFirstOccurrence() {
        if (supportsMethod(DequeMethods.REMOVE_FIRST_OCCURRENCES)) {
            E element = elementProvider().createInstance();
            E other = elementProvider().createInstance();
            // Create a deque with: element, other, element
            D deque = provider().createInstance(List.of(element, other, element));

            assertTrue(deque.removeFirstOccurrence(element));
            assertEquals(2, deque.size());

            // The remaining elements should be: other, element
            // Since it was the FIRST occurrence, 'other' should now be at the front
            assertEquals(other, deque.peekFirst());
            assertEquals(element, deque.peekLast());
        } else {
            D deque = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, () -> deque.removeFirstOccurrence(null));
        }
    }

    /// Tests that the [removeFirstOccurrence][Deque#removeFirstOccurrence] method returns false when the element is not present.
    @DisplayName("removeFirstOccurrence(Object) returns false when the element is not present")
    @Test
    default void removeFirstOccurrence_whenNotPresent_returnsFalse() {
        if (supportsMethod(DequeMethods.REMOVE_FIRST_OCCURRENCES)) {
            D deque = provider().createInstanceWithUniqueElements();
            E notPresent = elementProvider().createInstanceNotIn(deque);
            int sizeBefore = deque.size();
            assertFalse(deque.removeFirstOccurrence(notPresent));
            assertEquals(sizeBefore, deque.size());
        } else {
            D deque = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, () -> deque.removeFirstOccurrence(null));
        }
    }
}
