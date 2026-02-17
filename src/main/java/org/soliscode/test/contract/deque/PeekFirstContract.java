package org.soliscode.test.contract.deque;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `peekFirst` method of a `Deque`**
/// This interface defines tests for the [peekFirst()][Deque#peekFirst] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Deque] implementations.
///
/// @param <E> The element type being tested.
/// @param <D> The deque type being tested.
/// @author evanbergstrom
/// @see Deque#peekFirst
/// @since 1.0.0
public interface PeekFirstContract<E, D extends Deque<E>> extends CollectionContractSupport<E, D> {

    /// Tests that the [peekFirst][Deque#peekFirst] method correctly retrieves the first element without removing it.
    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    @DisplayName("peekFirst() retrieves the first element without removing it")
    @Test
    default void peekFirst_whenNotEmpty_returnsFirst() {
        if (supportsMethod(DequeMethods.PEEK_FIRST)) {
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            D deque = provider().createInstance(values);
            assertEquals(values.get(0), deque.peekFirst());
            assertEquals(values.size(), deque.size());
        } else {
            D deque = provider().createInstanceWithUniqueElements();
            assertThrows(UnsupportedOperationException.class, deque::peekFirst);
        }
    }

    /// Tests that the [peekFirst][Deque#peekFirst] method returns null when the deque is empty.
    @DisplayName("peekFirst() returns null when the deque is empty")
    @Test
    default void peekFirst_whenEmpty_returnsNull() {
        if (supportsMethod(DequeMethods.PEEK_FIRST)) {
            D deque = provider().emptyInstance();
            assertNull(deque.peekFirst());
        } else {
            D deque = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, deque::peekFirst);
        }
    }
}
