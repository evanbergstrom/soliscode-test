package com.soliscode.test.contract.sequencedmap;

import java.util.SequencedMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.MapContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the `putLast(K, V)` method of [SequencedMap].
///
/// ### Purpose
/// Verifies that the `putLast(K, V)` method correctly inserts a mapping at the end
/// of the map's sequencing order, or moves an existing mapping to the end if it's already
/// present, or handles unsupported operations appropriately.
///
/// ### Usage Examples
///
/// #### Implementation
/// ```java
/// class MySequencedMapTest implements PutLastContract<MySequencedMap<String, String>, String, String> {
///     @Override
///     public MapProvider<MySequencedMap<String, String>, String, String> objectProvider() {
///         return new MySequencedMapProvider();
///     }
/// }
/// ```
///
/// ### Thread Safety
/// The tests in this contract are not guaranteed to be thread-safe. If the map implementation
/// is intended for concurrent use, additional thread-safety tests should be performed.
///
/// @param <M> The map type being tested.
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @see SequencedMap#putLast(Object, Object)
/// @author evanbergstrom
/// @since 1.0.0
public interface PutLastContract<M extends SequencedMap<K, V>, K, V> extends MapContractSupport<M, K, V> {

    /// Verifies that `putLast(K, V)` adds a new entry to the end of the map or
    /// throws [UnsupportedOperationException] if not supported.
    ///
    /// @throws UnsupportedOperationException if the method is not supported and the contract
    ///         specifies it should not be.
    /// @since 1.0.0
    @Test
    @DisplayName("putLast: with new entry, adds to end and updates size")
    default void putLastWithNewEntryAddsToBackAndUpdatesSize() {
        SequencedMap<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        if (supportsMethod(SequencedMapMethods.PUT_LAST)) {
            assertNull(map.putLast(key, value));
            assertTrue(map.containsKey(key));
            assertEquals(value, map.get(key));
            assertEquals(key, map.lastEntry().getKey());
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.putLast(key, value));
        }
    }

    /// Verifies that `putLast(K, V)` updates and moves an existing entry to the end
    /// of the map or throws [UnsupportedOperationException] if not supported.
    ///
    /// @throws UnsupportedOperationException if the method is not supported and the contract
    ///         specifies it should not be.
    /// @since 1.0.0
    @Test
    @DisplayName("putLast: with existing entry, updates and moves to end")
    default void putLastWithExistingEntryUpdatesAndMovesToBack() {
        K key1 = keyProvider().createInstance(1);
        V value1 = valueProvider().createInstance(1);
        K key2 = keyProvider().createInstance(2);
        V value2 = valueProvider().createInstance(2);

        SequencedMap<K, V> map = provider().emptyInstance();
        if (supportsMethod(SequencedMapMethods.PUT_LAST) && supportsMethod(SequencedMapMethods.PUT_FIRST)) {
            map.putFirst(key1, value1);
            map.putFirst(key2, value2);

            V newValue1 = valueProvider().createInstance(3);
            assertEquals(value1, map.putLast(key1, newValue1));
            assertEquals(key1, map.lastEntry().getKey());
            assertEquals(newValue1, map.get(key1));
        } else if (!supportsMethod(SequencedMapMethods.PUT_LAST)) {
            assertThrows(UnsupportedOperationException.class, () -> map.putLast(key1, value1));
        }
    }
}
