package org.soliscode.test.contract.sortedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.sequencedmap.PutFirstContract;

import java.util.SequencedMap;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the `putFirst` method of a `SortedMap`**
///
/// This interface defines tests for the [putFirst(K, V)][SequencedMap#putFirst] method
/// for [SortedMap] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a sorted map's `putFirst` implementation
/// correctly throws [UnsupportedOperationException] because the order of elements in
/// a sorted map is determined by their keys (or a comparator) and cannot be explicitly
/// set by adding elements to the front.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MySortedMapPutFirstTest implements SortedPutFirstContract<String, String, MySortedMap<String, String>> {
///     @Override
///     public MapProvider<String, String, MySortedMap<String, String>> provider() {
///         return MySortedMap::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [SortedMap] and [org.soliscode.test.provider.MapProvider] implementations being tested.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @see SequencedMap#putFirst
/// @since 1.0.0
public interface SortedPutFirstContract<K, V, M extends SortedMap<K, V>> extends PutFirstContract<K, V, M> {

    /// Tests that the [putFirst][SequencedMap#putFirst] method throws
    /// [UnsupportedOperationException] when adding a new entry.
    ///
    /// @see SequencedMap#putFirst
    /// @throws UnsupportedOperationException always
    /// @since 1.0.0
    @Test
    @DisplayName("putFirst(K, V) throws UnsupportedOperationException when adding a new entry")
    default void putFirst_withNewEntry_throwsUnsupportedOperationException() {
        SequencedMap<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        assertThrows(UnsupportedOperationException.class, () -> map.putFirst(key, value));
    }

    /// Tests that the [putFirst][SequencedMap#putFirst] method throws
    /// [UnsupportedOperationException] when updating an existing entry.
    ///
    /// @see SequencedMap#putFirst
    /// @throws UnsupportedOperationException always
    /// @since 1.0.0
    @Test
    @DisplayName("putFirst(K, V) throws UnsupportedOperationException when updating an existing entry")
    default void putFirst_withExistingEntry_throwsUnsupportedOperationException() {
        K key1 = keyProvider().createInstance(1);
        V value1 = valueProvider().createInstance(1);
        SequencedMap<K, V> map = provider().emptyInstance();
        assertThrows(UnsupportedOperationException.class, () -> map.putFirst(key1, value1));
    }
}
