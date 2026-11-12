/*
 * Copyright 2026 Evan Bergstrom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.soliscode.test.provider;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/// Factories of [MapEntryProvider]s: the JDK's entries of their own, and the entries of a map.
///
/// @since 0.1.3
public final class MapEntryProviders {

    private MapEntryProviders() { }

    /// Entries made by [AbstractMap.SimpleEntry]'s constructor: `setValue` is supported, and there is no map behind
    /// them.
    ///
    /// @param <K> the key type
    /// @param <V> the value type
    /// @return the provider
    public static <K, V> MapEntryProvider<Map.Entry<K, V>, K, V> simpleEntry() {
        return (key, value) -> new EntrySource<>(new AbstractMap.SimpleEntry<>(key, value));
    }

    /// Entries made by [AbstractMap.SimpleImmutableEntry]'s constructor: `setValue` is not supported.
    ///
    /// @param <K> the key type
    /// @param <V> the value type
    /// @return the provider
    public static <K, V> MapEntryProvider<Map.Entry<K, V>, K, V> simpleImmutableEntry() {
        return (key, value) -> new EntrySource<>(new AbstractMap.SimpleImmutableEntry<>(key, value));
    }

    /// Entries made by [Map#entry(Object, Object)]: `setValue` is not supported.
    ///
    /// @param <K> the key type
    /// @param <V> the value type
    /// @return the provider
    public static <K, V> MapEntryProvider<Map.Entry<K, V>, K, V> mapEntry() {
        return (key, value) -> new EntrySource<>(Map.entry(key, value));
    }

    /// The entries of a map: each source puts its one mapping into a new map from the supplier, and takes the entry
    /// under test by `entrySet().iterator().next()`. [Map.Entry]'s class Javadoc says such an entry is connected to
    /// the map, "if supported by the backing map", "*only* during iteration of the entry-set view", and that it is
    /// undefined once the map is changed other than through `setValue`. So the source keeps the iterator and never
    /// finishes the iteration (with one mapping, `hasNext()` is not called again), and nothing but the entry changes
    /// the map; [MapEntrySource#backingValue()] reads it with `get`, which changes nothing.
    ///
    /// @param maps makes a new, empty map; one that is not empty is refused with [IllegalArgumentException], and a
    ///             refusal of the `put` reaches the caller
    /// @param <K> the key type
    /// @param <V> the value type
    /// @return the provider
    public static <K, V> MapEntryProvider<Map.Entry<K, V>, K, V> fromMap(final Supplier<? extends Map<K, V>> maps) {
        Objects.requireNonNull(maps);
        return (key, value) -> {
            Map<K, V> map = maps.get();
            if (!map.isEmpty()) {
                throw new IllegalArgumentException("the supplied map is not empty: it holds " + map.size()
                        + " mappings");
            }
            map.put(key, value);
            Iterator<Map.Entry<K, V>> iteration = map.entrySet().iterator();
            return new MapSource<>(map, key, iteration, iteration.next());
        };
    }

    /// An entry with no map behind it.
    private record EntrySource<K, V>(Map.Entry<K, V> entry) implements MapEntrySource<Map.Entry<K, V>, K, V> {
    }

    /// A map's entry, the map, the key it was made with (so that the map is read without the entry), and the
    /// iteration of the entry set the entry came from, kept unfinished while the entry is in use.
    private record MapSource<K, V>(Map<K, V> map, K key, Iterator<Map.Entry<K, V>> iteration, Map.Entry<K, V> entry)
            implements MapEntrySource<Map.Entry<K, V>, K, V> {

        @Override
        public Optional<V> backingValue() {
            return Optional.ofNullable(map.get(key));
        }
    }
}
