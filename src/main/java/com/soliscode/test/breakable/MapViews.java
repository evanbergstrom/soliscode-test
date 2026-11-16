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
package com.soliscode.test.breakable;

import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.MethodStatus;
import com.soliscode.test.methods.CollectionMethods;
import com.soliscode.test.methods.IterableMethods;
import com.soliscode.test.methods.MapEntryMethods;
import com.soliscode.test.methods.MapMethods;
import com.soliscode.test.methods.SequencedCollectionMethods;

/// The configuration of a map's collection views (`keySet()`, `values()`, `entrySet()` and a navigable map's key
/// sets): the map's method statuses mapped onto the views' methods, the views' permits, and the entries a map whose
/// `put` is unsupported hands out (#52).
///
/// As `Collections.unmodifiableMap` makes every view of an unmodifiable map unmodifiable, a view follows the map's
/// statuses: `remove`'s for the view's removals, `clear`'s for its `clear`, and `put`'s for its entries' `setValue`.
/// `add` and `addAll` are unsupported, as [Map#keySet()], [Map#values()] and [Map#entrySet()] say.
final class MapViews {

    /// The permits of the map's entry set: nulls and incompatible types, so that the store decides (a `null` or
    /// another type is in no entry set).
    static final int ENTRY_PERMITS = BreakableCollection.PERMITS_NULLS | BreakableCollection.PERMITS_INCOMPATIBLE_TYPES;

    /// The view methods that remove an element, and so follow [MapMethods#REMOVE]'s status. A navigable key set's
    /// `pollFirst` and `pollLast` follow [CollectionMethods#REMOVE]'s.
    private static final InterfaceMethod[] REMOVALS = {
        CollectionMethods.REMOVE, CollectionMethods.REMOVE_ALL, CollectionMethods.RETAIN_ALL,
        CollectionMethods.REMOVE_IF, IterableMethods.ITERATOR_REMOVE,
        SequencedCollectionMethods.REMOVE_FIRST, SequencedCollectionMethods.REMOVE_LAST,
    };

    private MapViews() { }

    /// Returns the method statuses of a collection view of the map: the map's own, with `add` and `addAll`
    /// unsupported, the removals unsupported where the map's `remove` is, and `clear` where the map's `clear` is.
    ///
    /// @param map the map
    /// @param mapStatuses the map's method statuses
    /// @return the view's statuses, a new map
    static Map<InterfaceMethod, MethodStatus> statuses(final BreakableMap<?, ?> map,
                                                       final Map<InterfaceMethod, MethodStatus> mapStatuses) {
        final Map<InterfaceMethod, MethodStatus> statuses = new HashMap<>(mapStatuses);
        statuses.put(CollectionMethods.ADD, MethodStatus.UNSUPPORTED);
        statuses.put(CollectionMethods.ADD_ALL, MethodStatus.UNSUPPORTED);
        if (!map.supportsMethod(MapMethods.REMOVE)) {
            for (final InterfaceMethod method : REMOVALS) {
                statuses.put(method, MethodStatus.UNSUPPORTED);
            }
        }
        if (!map.supportsMethod(MapMethods.CLEAR)) {
            statuses.put(CollectionMethods.CLEAR, MethodStatus.UNSUPPORTED);
        }
        return statuses;
    }

    /// Returns the permits of the map's key set: nulls and incompatible types as the map permits them for keys.
    ///
    /// @param map the map
    /// @return the key set's permits ([BreakableCollection]'s `PERMITS_*` flags)
    static int keyPermits(final BreakableMap<?, ?> map) {
        return permits(map.permitsNullKeys(), (map.permits() & BreakableMap.PERMITS_INCOMPATIBLE_KEYS) != 0)
                & ~BreakableCollection.PERMITS_DUPLICATES;
    }

    /// Returns the permits of the map's values: nulls and incompatible types as the map permits them for values, and
    /// duplicates.
    ///
    /// @param map the map
    /// @return the values' permits ([BreakableCollection]'s `PERMITS_*` flags)
    static int valuePermits(final BreakableMap<?, ?> map) {
        return permits(map.permitsNullValues(), (map.permits() & BreakableMap.PERMITS_INCOMPATIBLE_VALUES) != 0);
    }

    private static int permits(final boolean nulls, final boolean incompatible) {
        return BreakableCollection.PERMITS_DUPLICATES
                | (nulls ? BreakableCollection.PERMITS_NULLS : 0)
                | (incompatible ? BreakableCollection.PERMITS_INCOMPATIBLE_TYPES : 0);
    }

    /// Returns the store's entry set, or, where the map's `put` is unsupported, a set over it whose entries'
    /// `setValue` is unsupported. Every path that hands out an entry (the iterator, and through it `forEach`, the
    /// spliterator, `stream` and `toArray`) hands out a wrapped one.
    ///
    /// @param <K> the key type
    /// @param <V> the value type
    /// @param map the map
    /// @param entries the store's entry set
    /// @return the entry set to make the view over
    static <K, V> Set<Entry<K, V>> entries(final BreakableMap<K, V> map, final Set<Entry<K, V>> entries) {
        return map.supportsMethod(MapMethods.PUT) ? entries : new FixedValueEntrySet<>(entries);
    }

    /// An entry set whose entries' `setValue` is unsupported; everything else is the store's.
    private static final class FixedValueEntrySet<K, V> extends AbstractSet<Entry<K, V>> {
        private static final Map<InterfaceMethod, MethodStatus> SET_VALUE_UNSUPPORTED =
                Map.of(MapEntryMethods.MAP_ENTRY_SET_VALUE, MethodStatus.UNSUPPORTED);

        private final Set<Entry<K, V>> entries;

        FixedValueEntrySet(final Set<Entry<K, V>> entriesArg) {
            this.entries = entriesArg;
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            final Iterator<Entry<K, V>> iterator = entries.iterator();
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public Entry<K, V> next() {
                    return new BreakableMapEntry<>(iterator.next(), Set.of(), SET_VALUE_UNSUPPORTED);
                }

                @Override
                public void remove() {
                    iterator.remove();
                }
            };
        }

        @Override
        public int size() {
            return entries.size();
        }

        @Override
        public boolean contains(final @Nullable Object o) {
            return entries.contains(o);
        }

        @Override
        public boolean remove(final @Nullable Object o) {
            return entries.remove(o);
        }

        @Override
        public void clear() {
            entries.clear();
        }
    }
}
