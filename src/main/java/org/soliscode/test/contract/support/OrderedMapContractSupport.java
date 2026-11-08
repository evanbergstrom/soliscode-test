package org.soliscode.test.contract.support;

import org.jspecify.annotations.NonNull;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.SortedMap;
import java.util.TreeMap;

/// Support for the contracts of sorted, navigable and sequenced maps.
///
/// A test builds the map from known entries, then compares what the map returns with what a [TreeMap] holding the
/// same entries and the map's comparator returns (for a sorted map), or with the entries' encounter order (for a
/// sequenced map). The expected results so come from the JDK's implementation, not the one under test.
///
/// @param <M> the type of map
/// @param <K> the type of keys
/// @param <V> the type of values
/// @since 1.0
public interface OrderedMapContractSupport<M extends Map<K, V>, K, V> extends MapContractSupport<M, K, V> {

    /// Returns [#DEFAULT_SIZE] entries with unique keys and values, in the order they were made.
    ///
    /// @return the entries
    default @NonNull Map<K, V> sourceEntries() {
        List<K> keys = keyProvider().createUniqueInstances(DEFAULT_SIZE);
        List<V> values = valueProvider().createUniqueInstances(DEFAULT_SIZE);
        Map<K, V> entries = new LinkedHashMap<>();
        for (int i = 0; i < DEFAULT_SIZE; i++) {
            entries.put(keys.get(i), values.get(i));
        }
        return entries;
    }

    /// Returns the keys to look up: every key of `entries`, then two keys that are not in it.
    ///
    /// @param entries the entries the map holds
    /// @return the keys
    default @NonNull List<K> probeKeys(final @NonNull Map<K, V> entries) {
        List<K> keys = new ArrayList<>(entries.keySet());
        List<K> absent = new ArrayList<>(keys);
        for (int i = 0; i < 2; i++) {
            K key = keyProvider().createInstanceNotIn(absent);
            absent.add(key);
            keys.add(key);
        }
        return keys;
    }

    /// Returns a [TreeMap] with the entries and the map's comparator: the expected results for a sorted map.
    ///
    /// @param map the sorted map under test
    /// @param entries the entries it holds
    /// @return the reference map
    default @NonNull TreeMap<K, V> sortedReference(final @NonNull SortedMap<K, V> map,
                                                   final @NonNull Map<K, V> entries) {
        TreeMap<K, V> reference = new TreeMap<>(map.comparator());
        reference.putAll(entries);
        return reference;
    }

    /// Returns the entries in the order a sequenced map holding them should encounter them: sorted, for a sorted
    /// map, otherwise the order they were put in.
    ///
    /// [SequencedMap] allows any defined encounter order. A test of a sequenced map whose order is neither (a
    /// sorted view that is not a [SortedMap], say) overrides this method to give that order.
    ///
    /// @param map the sequenced map under test
    /// @param entries the entries it holds, in the order they were put in
    /// @return the entries in encounter order
    default @NonNull List<Map.Entry<K, V>> encounterOrder(final @NonNull SequencedMap<K, V> map,
                                                         final @NonNull Map<K, V> entries) {
        Map<K, V> ordered = (map instanceof SortedMap<K, V> sorted) ? sortedReference(sorted, entries) : entries;
        List<Map.Entry<K, V>> result = new ArrayList<>();
        ordered.forEach((k, v) -> result.add(new AbstractMap.SimpleImmutableEntry<>(k, v)));
        return result;
    }
}
