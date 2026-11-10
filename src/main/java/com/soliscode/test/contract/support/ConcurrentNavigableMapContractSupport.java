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
package com.soliscode.test.contract.support;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.concurrent.ConcurrentNavigableMap;

import org.junit.jupiter.api.function.Executable;

import com.soliscode.test.contract.map.MapMethods;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// The base interface of the [ConcurrentNavigableMap] contracts: the checks that a view is backed by the map, both
/// ways, that its method contracts share.
///
/// A test builds the map from [#sourceEntries()], takes the view once, and then changes the map and the view in turn
/// with a key `k` the map holds and the view's range includes. A view fetched again at each step would hide a copy.
///
/// @param <M> the concurrent navigable map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentNavigableMap
/// @since 0.1.2
public interface ConcurrentNavigableMapContractSupport<M extends ConcurrentNavigableMap<K, V>, K, V>
        extends ConcurrentMapContractSupport<M, K, V>, OrderedMapContractSupport<M, K, V> {

    /// Says whether the write-through tests can run: the map holds at least 2 entries, and supports `put` and
    /// `remove`.
    ///
    /// @return `true` if they can run
    default boolean canWriteThrough() {
        return defaultSize() >= 2 && supportsMethod(MapMethods.PUT) && supportsMethod(MapMethods.REMOVE);
    }

    /// Asserts that a map view is backed by the map, both ways: `map.remove(k)` leaves the view without `k`;
    /// `view.put(k, w)` maps `k` to `w` in the map; `view.remove(k)` removes it from the map; `map.put(k, v)` shows
    /// in the view. `w` is a value the map does not hold, and the map ends as it began.
    ///
    /// @param map the map
    /// @param view the view, taken once
    /// @param key a key the map holds and the view's range includes
    /// @param description what the view is, for the messages
    default void assertMapViewWritesThrough(final M map, final NavigableMap<K, V> view, final K key,
                                            final String description) {
        assertNotNull(view, description);
        V value = Objects.requireNonNull(map.get(key), "the map holds the key");
        V other = valueProvider().createInstanceNotIn(map.values());
        map.remove(key);
        assertFalse(view.containsKey(key), () -> description + " still holds " + key + ", removed from the map");
        view.put(key, other);
        assertEquals(other, map.get(key), () -> description + ".put(" + key + ", v) did not reach the map");
        view.remove(key);
        assertFalse(map.containsKey(key), () -> description + ".remove(" + key + ") did not reach the map");
        map.put(key, value);
        assertEquals(value, view.get(key), () -> description + " does not show " + key + ", put in the map");
    }

    /// Asserts that a key-set view is backed by the map, both ways, and refuses `add` and `addAll`: `set.remove(k)`
    /// removes `k`'s mapping; removing `k` through the set's iterator does too; `map.remove(k)` leaves the set
    /// without `k`, and `map.put(k, v)` shows in it; `add` and `addAll` of a key the map does not hold throw
    /// `UnsupportedOperationException` and leave the map unchanged. The map ends as it began.
    ///
    /// @param map the map
    /// @param keys the key set, taken once
    /// @param key a key the map holds
    /// @param description what the set is, for the messages
    default void assertKeySetWritesThrough(final M map, final NavigableSet<K> keys, final K key,
                                           final String description) {
        assertNotNull(keys, description);
        V value = Objects.requireNonNull(map.get(key), "the map holds the key");
        assertTrue(keys.remove(key), () -> description + ".remove(" + key + ") returned false");
        assertFalse(map.containsKey(key), () -> description + ".remove(" + key + ") did not reach the map");
        map.put(key, value);
        assertTrue(keys.contains(key), () -> description + " does not show " + key + ", put in the map");
        boolean removed = false;
        for (Iterator<K> i = keys.iterator(); i.hasNext();) {
            if (i.next().equals(key)) {
                i.remove();
                removed = true;
                break;
            }
        }
        assertTrue(removed, () -> description + "'s iterator did not return " + key);
        assertFalse(map.containsKey(key), () -> description + "'s iterator remove() did not reach the map");
        map.put(key, value);
        map.remove(key);
        assertFalse(keys.contains(key), () -> description + " still holds " + key + ", removed from the map");
        map.put(key, value);
        assertTrue(keys.contains(key), () -> description + " does not show " + key + ", put back in the map");
        K absent = keyProvider().createInstanceNotIn(map.keySet());
        assertUnsupportedAndUnchanged(map, () -> keys.add(absent), description + ".add(k)");
        assertUnsupportedAndUnchanged(map, () -> keys.addAll(List.of(absent)), description + ".addAll(List.of(k))");
    }

    /// Asserts that a call throws `UnsupportedOperationException` and leaves the map as it was.
    ///
    /// @param map the map
    /// @param call the call
    /// @param description what the call is, for the message
    default void assertUnsupportedAndUnchanged(final M map, final Executable call, final String description) {
        Map<K, V> before = new HashMap<>(map);
        assertThrows(UnsupportedOperationException.class, call, description);
        assertEquals(before, new HashMap<>(map), description + " changed the map");
    }
}
