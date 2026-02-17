package org.soliscode.test.contract.deque;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `push` method of a `Deque`**
/// This interface defines tests for the [push()][Deque#push] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Deque] implementations.
///
/// @param <E> The element type being tested.
/// @param <D> The deque type being tested.
/// @author evanbergstrom
/// @see Deque#push
/// @since 1.0.0
public interface PushContract<E, D extends Deque<E>> extends CollectionContractSupport<E, D> {

    /// Tests that the [push][Deque#push] method correctly adds a single element to the front.
    @DisplayName("push(E) adds a single element to the front and updates size")
    @Test
    default void push_singleElement_addsToFrontAndUpdatesSize() {
        if (supportsMethod(DequeMethods.PUSH)) {
            D deque = provider().emptyInstance();
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            for (int i = 0; i < values.size(); i++) {
                E element = values.get(i);
                deque.push(element);
                assertTrue(deque.contains(element));
                assertEquals(i + 1, deque.size());
                assertEquals(element, deque.peekFirst());
            }
        } else {
            D deque = provider().emptyInstance();
            E element = elementProvider().createInstance();
            assertThrows(UnsupportedOperationException.class, () -> deque.push(element));
        }
    }

    /// Tests that the [push][Deque#push] method handles null values correctly.
    @DisplayName("push(E) handles null values based on permission")
    @Test
    default void push_withNullValue_handlesCorrectly() {
        if (supportsMethod(DequeMethods.PUSH)) {
            D deque = provider().emptyInstance();
            if (permitNulls()) {
                deque.push(null);
                assertTrue(deque.contains(null));
                assertEquals(1, deque.size());
                assertNull(deque.peekFirst());
            } else {
                assertThrows(NullPointerException.class, () -> deque.push(null));
            }
        } else {
            D deque = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, () -> deque.push(null));
        }
    }
}
