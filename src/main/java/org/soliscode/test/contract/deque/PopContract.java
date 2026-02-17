package org.soliscode.test.contract.deque;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `pop` method of a `Deque`**
/// This interface defines tests for the [pop()][Deque#pop] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Deque] implementations.
///
/// @param <E> The element type being tested.
/// @param <D> The deque type being tested.
/// @author evanbergstrom
/// @see Deque#pop
/// @since 1.0.0
public interface PopContract<E, D extends Deque<E>> extends CollectionContractSupport<E, D> {

    /// Tests that the [pop][Deque#pop] method correctly retrieves and removes the first element.
    @DisplayName("pop() retrieves and removes the first element")
    @Test
    default void pop_whenNotEmpty_returnsAndRemovesFirst() {
        if (supportsMethod(DequeMethods.POP)) {
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            D deque = provider().createInstance(values);
            for (int i = 0; i < values.size(); i++) {
                E expected = values.get(i);
                assertEquals(expected, deque.pop());
                assertFalse(deque.contains(expected));
                assertEquals(values.size() - i - 1, deque.size());
            }
        } else {
            D deque = provider().createInstanceWithUniqueElements();
            assertThrows(UnsupportedOperationException.class, deque::pop);
        }
    }

    /// Tests that the [pop][Deque#pop] method throws NoSuchElementException when the deque is empty.
    @DisplayName("pop() throws NoSuchElementException when the deque is empty")
    @Test
    default void pop_whenEmpty_throwsNoSuchElementException() {
        if (supportsMethod(DequeMethods.POP)) {
            D deque = provider().emptyInstance();
            assertThrows(java.util.NoSuchElementException.class, deque::pop);
        } else {
            D deque = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, deque::pop);
        }
    }
}
