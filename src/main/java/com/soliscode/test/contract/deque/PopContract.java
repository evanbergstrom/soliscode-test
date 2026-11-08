package com.soliscode.test.contract.deque;

import java.util.Deque;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.CollectionContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the `pop` method of a `Deque`**
/// This interface defines tests for the [pop()][Deque#pop] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Deque] implementations.
///
/// @param <E> The element type being tested.
/// @param <D> The deque type being tested.
/// @author evanbergstrom
/// @see Deque#pop
/// @since 1.0.0
public interface PopContract<D extends Deque<E>, E> extends CollectionContractSupport<D, E> {

    /// Tests that the [pop][Deque#pop] method correctly retrieves and removes the first element.
    @DisplayName("pop() retrieves and removes the first element")
    @Test
    default void popWhenNotEmptyReturnsAndRemovesFirst() {
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
    default void popWhenEmptyThrowsNoSuchElementException() {
        if (supportsMethod(DequeMethods.POP)) {
            D deque = provider().emptyInstance();
            assertThrows(java.util.NoSuchElementException.class, deque::pop);
        } else {
            D deque = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, deque::pop);
        }
    }
}
