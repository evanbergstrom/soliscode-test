package com.soliscode.test.contract.sequencedmap;

import java.util.Map;
import java.util.SequencedMap;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.MapContractSupport;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the `sequencedEntrySet()` method of [SequencedMap].
///
/// ### Purpose
/// Verifies that the `sequencedEntrySet()` method correctly returns a sequenced set view
/// of the map's entries, or handles unsupported operations appropriately.
///
/// ### Usage Examples
///
/// #### Implementation
/// ```java
/// class MySequencedMapTest implements SequencedEntrySetContract<MySequencedMap<String, String>, String, String> {
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
/// @see SequencedMap#sequencedEntrySet()
/// @author evanbergstrom
/// @since 1.0.0
public interface SequencedEntrySetContract<M extends SequencedMap<K, V>, K, V> extends MapContractSupport<M, K, V> {

    /// Verifies that `sequencedEntrySet()` returns a sequenced set of entries or
    /// throws [UnsupportedOperationException] if not supported.
    ///
    /// @throws UnsupportedOperationException if the method is not supported and the contract
    ///         specifies it should not be.
    /// @since 1.0.0
    @Test
    @DisplayName("sequencedEntrySet: when called, returns sequenced set or throws UnsupportedOperationException")
    default void sequencedEntrySetWhenCalledReturnsSequencedSet() {
        SequencedMap<K, V> map = provider().emptyInstance();
        if (supportsMethod(SequencedMapMethods.SEQUENCED_ENTRY_SET)) {
            Set<Map.Entry<K, V>> entrySet = map.sequencedEntrySet();
            assertNotNull(entrySet);
            assertTrue(entrySet.isEmpty());
        } else {
            assertThrows(UnsupportedOperationException.class, map::sequencedEntrySet);
        }
    }
}
