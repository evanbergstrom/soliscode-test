package org.soliscode.test.contract.sequencedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.SequencedMap;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the `putFirst(K, V)` method of [SequencedMap].
///
/// ### Purpose
/// Verifies that the `putFirst(K, V)` method correctly inserts a mapping at the beginning
/// of the map's sequencing order, or moves an existing mapping to the front if it's already
/// present, or handles unsupported operations appropriately.
///
/// ### Usage Examples
///
/// #### Implementation
/// ```java
/// public class MySequencedMapTest implements PutFirstContract<String, String, MySequencedMap<String, String>> {
///     @Override
///     public MapProvider<String, String, MySequencedMap<String, String>> provider() {
///         return new MySequencedMapProvider();
///     }
/// }
/// ```
///
/// ### Thread Safety
/// The tests in this contract are not guaranteed to be thread-safe. If the map implementation
/// is intended for concurrent use, additional thread-safety tests should be performed.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @see SequencedMap#putFirst(Object, Object)
/// @author evanbergstrom
/// @since 1.0.0
public interface PutFirstContract<K, V, M extends SequencedMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Verifies that `putFirst(K, V)` adds a new entry to the front of the map or
    /// throws [UnsupportedOperationException] if not supported.
    ///
    /// @throws UnsupportedOperationException if the method is not supported and the contract
    ///         specifies it should not be.
    /// @since 1.0.0
    @Test
    @DisplayName("putFirst: with new entry, adds to front and updates size")
    default void putFirst_withNewEntry_addsToFrontAndUpdatesSize() {
        SequencedMap<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        if (supportsMethod(SequencedMapMethods.PUT_FIRST)) {
            assertNull(map.putFirst(key, value));
            assertTrue(map.containsKey(key));
            assertEquals(value, map.get(key));
            assertEquals(key, map.firstEntry().getKey());
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.putFirst(key, value));
        }
    }

    /// Verifies that `putFirst(K, V)` updates and moves an existing entry to the front
    /// of the map or throws [UnsupportedOperationException] if not supported.
    ///
    /// @throws UnsupportedOperationException if the method is not supported and the contract
    ///         specifies it should not be.
    /// @since 1.0.0
    @Test
    @DisplayName("putFirst: with existing entry, updates and moves to front")
    default void putFirst_withExistingEntry_updatesAndMovesToFront() {
        K key1 = keyProvider().createInstance(1);
        V value1 = valueProvider().createInstance(1);
        K key2 = keyProvider().createInstance(2);
        V value2 = valueProvider().createInstance(2);

        SequencedMap<K, V> map = provider().emptyInstance();
        if (supportsMethod(SequencedMapMethods.PUT_FIRST) && supportsMethod(SequencedMapMethods.PUT_LAST)) {
            map.putLast(key1, value1);
            map.putLast(key2, value2);

            V newValue1 = valueProvider().createInstance(3);
            assertEquals(value1, map.putFirst(key1, newValue1));
            assertEquals(key1, map.firstEntry().getKey());
            assertEquals(newValue1, map.get(key1));
        } else if (!supportsMethod(SequencedMapMethods.PUT_FIRST)) {
            assertThrows(UnsupportedOperationException.class, () -> map.putFirst(key1, value1));
        }
    }
}
