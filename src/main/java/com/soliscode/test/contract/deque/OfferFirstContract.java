package com.soliscode.test.contract.deque;

import java.util.Deque;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.CollectionContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// This interface tests if a deque class has implemented the [offerFirst][Deque#offerFirst] method
/// correctly.
///
/// @param <E> The element type being tested.
/// @param <D> The deque type being tested.
/// @author evanbergstrom
/// @see Deque#offerFirst
/// @since 1.0
public interface OfferFirstContract<D extends Deque<E>, E> extends CollectionContractSupport<D, E> {

    /// Tests that the [offerFirst][Deque#offerFirst] method works.
    @DisplayName("offerFirst(E) adds a single element and updates size")
    @Test
    default void offerFirstSingleElementReturnsTrueAndUpdatesSize() {
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
    // NullAway: passes null to offerFirst on purpose; a deque that permits nulls accepts it, others must throw.
    @SuppressWarnings("NullAway")
    default void offerFirstWithNullValueHandlesCorrectly() {
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
    default void offerFirstWithDuplicateValueHandlesCorrectly() {
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
