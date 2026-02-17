package org.soliscode.test.contract.deque;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `removeLastOccurrence` method of a `Deque`**
/// This interface defines tests for the [removeLastOccurrence()][Deque#removeLastOccurrence] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Deque] implementations.
///
/// @param <E> The element type being tested.
/// @param <D> The deque type being tested.
/// @author evanbergstrom
/// @see Deque#removeLastOccurrence
/// @since 1.0.0
public interface RemoveLastOccurrenceContract<E, D extends Deque<E>> extends CollectionContractSupport<E, D> {

    /// Tests that the [removeLastOccurrence][Deque#removeLastOccurrence] method correctly removes the last occurrence.
    @DisplayName("removeLastOccurrence(Object) removes the last occurrence of the specified element")
    @Test
    default void removeLastOccurrence_whenPresent_removesLastOccurrence() {
        if (supportsMethod(DequeMethods.REMOVE_LAST_OCCURRENCES)) {
            E element = elementProvider().createInstance();
            E other = elementProvider().createInstance();
            // Create a deque with: element, other, element
            D deque = provider().createInstance(List.of(element, other, element));

            assertTrue(deque.removeLastOccurrence(element));
            assertEquals(2, deque.size());

            // The remaining elements should be: element, other
            // Since it was the LAST occurrence, 'element' should still be at the front
            assertEquals(element, deque.peekFirst());
            assertEquals(other, deque.peekLast());
        } else {
            D deque = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, () -> deque.removeLastOccurrence(null));
        }
    }

    /// Tests that the [removeLastOccurrence][Deque#removeLastOccurrence] method returns false when the element is not present.
    @DisplayName("removeLastOccurrence(Object) returns false when the element is not present")
    @Test
    default void removeLastOccurrence_whenNotPresent_returnsFalse() {
        if (supportsMethod(DequeMethods.REMOVE_LAST_OCCURRENCES)) {
            D deque = provider().createInstanceWithUniqueElements();
            E notPresent = elementProvider().createInstanceNotIn(deque);
            int sizeBefore = deque.size();
            assertFalse(deque.removeLastOccurrence(notPresent));
            assertEquals(sizeBefore, deque.size());
        } else {
            D deque = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, () -> deque.removeLastOccurrence(null));
        }
    }
}
