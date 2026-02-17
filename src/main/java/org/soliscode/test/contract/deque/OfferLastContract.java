package org.soliscode.test.contract.deque;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `offerLast` method of a `Deque`**
/// This interface defines tests for the [offerLast()][Deque#offerLast] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Deque] implementations.
///
/// @param <E> The element type being tested.
/// @param <D> The deque type being tested.
/// @author evanbergstrom
/// @see Deque#offerLast
/// @since 1.0.0
public interface OfferLastContract<E, D extends Deque<E>> extends CollectionContractSupport<E, D> {

    /// Tests that the [offerLast][Deque#offerLast] method correctly adds a single element to the end of the deque.
    @DisplayName("offerLast(E) adds a single element to the end and updates size")
    @Test
    default void offerLast_singleElement_returnsTrueAndUpdatesSize() {
        if (supportsMethod(DequeMethods.OFFER_LAST)) {
            D deque = provider().emptyInstance();
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            for (int i = 0; i < values.size(); i++) {
                E element = values.get(i);
                assertTrue(deque.offerLast(element));
                assertTrue(deque.contains(element));
                assertEquals(i + 1, deque.size());
                assertEquals(element, deque.peekLast());
            }
        } else {
            D deque = provider().emptyInstance();
            E element = elementProvider().createInstance();
            assertThrows(UnsupportedOperationException.class, () -> deque.offerLast(element));
        }
    }

    /// Tests that the [offerLast][Deque#offerLast] method handles null values correctly.
    @DisplayName("offerLast(E) handles null values based on permission")
    @Test
    default void offerLast_withNullValue_handlesCorrectly() {
        if (supportsMethod(DequeMethods.OFFER_LAST)) {
            D deque = provider().emptyInstance();
            if (permitNulls()) {
                assertTrue(deque.offerLast(null));
                assertTrue(deque.contains(null));
                assertEquals(1, deque.size());
                assertNull(deque.peekLast());
            } else {
                assertThrows(NullPointerException.class, () -> deque.offerLast(null));
            }
        } else {
            D deque = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, () -> deque.offerLast(null));
        }
    }

    /// Tests that the [offerLast][Deque#offerLast] method handles duplicate values correctly.
    @DisplayName("offerLast(E) handles duplicate values based on permission")
    @Test
    default void offerLast_withDuplicateValue_handlesCorrectly() {
        if (supportsMethod(DequeMethods.OFFER_LAST)) {
            if (permitDuplicates()) {
                List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
                D deque = provider().createInstance(values);
                for (int i = 0; i < values.size(); i++) {
                    E element = values.get(i);
                    assertTrue(deque.offerLast(element));
                    assertTrue(deque.contains(element));
                    assertEquals(values.size() + i + 1, deque.size());
                    assertEquals(element, deque.peekLast());
                }
            } else {
                D deque = provider().emptyInstance();
                E value = elementProvider().createInstance();
                assertTrue(deque.offerLast(value));
                E otherValue = elementProvider().copyInstance(value);
                assertFalse(deque.offerLast(otherValue));
                assertEquals(1, deque.size());
            }
        } else {
            D deque = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class,
                    () -> deque.offerLast(elementProvider().createInstance()));
        }
    }
}
