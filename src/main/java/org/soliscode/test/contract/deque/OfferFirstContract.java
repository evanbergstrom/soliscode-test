package org.soliscode.test.contract.deque;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// This interface tests if a deque class has implemented the [offerFirst][Deque#offerFirst] method
/// correctly.
///
/// @param <E> The element type being tested.
/// @param <D> The deque type being tested.
/// @author evanbergstrom
/// @see Deque#offerFirst
/// @since 1.0
public interface OfferFirstContract<E, D extends Deque<E>> extends CollectionContractSupport<E, D> {

    /// Tests that the [offerFirst][Deque#offerFirst] method works.
    @DisplayName("offerFirst(E) adds a single element and updates size")
    @Test
    default void offerFirst_singleElement_returnsTrueAndUpdatesSize() {
        if (supportsMethod(DequeMethods.OFFER_FIRST)) {
            D deque = provider().emptyInstance();
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            for (int i = 0; i < values.size(); i++) {
                assertTrue(deque.offerFirst(values.get(i)));
                assertTrue(deque.contains(values.get(i)));
                assertEquals(i + 1, deque.size());
                assertEquals(values.get(i), deque.peekFirst());
            }
        } else {
            D deque = provider().emptyInstance();
            E element = elementProvider().createInstance();
            assertThrows(UnsupportedOperationException.class, () -> deque.offerFirst(element));
        }
    }

    /// Tests that the [offerFirst][Deque#offerFirst] method handles null values correctly.
    @DisplayName("offerFirst(E) handles null values based on permission")
    @Test
    default void offerFirst_withNullValue_handlesCorrectly() {
        if (supportsMethod(DequeMethods.OFFER_FIRST)) {
            D deque = provider().emptyInstance();
            if (permitNulls()) {
                assertTrue(deque.offerFirst(null));
                assertTrue(deque.contains(null));
                assertEquals(1, deque.size());
                assertNull(deque.peekFirst());
            } else {
                assertThrows(NullPointerException.class, () -> deque.offerFirst(null));
            }
        } else {
            D deque = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, () -> deque.offerFirst(null));
        }
    }

    /// Tests that the [offerFirst][Deque#offerFirst] method handles duplicate values correctly.
    @DisplayName("offerFirst(E) handles duplicate values based on permission")
    @Test
    default void offerFirst_withDuplicateValue_handlesCorrectly() {
        if (supportsMethod(DequeMethods.OFFER_FIRST)) {
            if (permitDuplicates()) {
                List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
                D deque = provider().createInstance(values);
                for (int i = 0; i < values.size(); i++) {
                    assertTrue(deque.offerFirst(values.get(i)));
                    assertTrue(deque.contains(values.get(i)));
                    assertEquals(values.size() + i + 1, deque.size());
                    assertEquals(values.get(i), deque.peekFirst());
                }
            } else {
                D deque = provider().emptyInstance();
                E value = elementProvider().createInstance();
                assertTrue(deque.offerFirst(value));
                E otherValue = elementProvider().copyInstance(value);
                assertFalse(deque.offerFirst(otherValue));
                assertEquals(1, deque.size());
            }
        } else {
            D deque = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class,
                    () -> deque.offerFirst(elementProvider().createInstance()));
        }
    }
}
