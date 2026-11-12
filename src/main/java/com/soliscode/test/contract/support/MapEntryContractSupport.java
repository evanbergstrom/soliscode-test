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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.soliscode.test.SupportedMethods;
import com.soliscode.test.provider.MapEntrySource;
import com.soliscode.test.provider.ObjectProvider;

/// The base interface of the map-entry contracts: it gives them the sources of the entries under test, the keys and
/// values, which optional methods are supported, and a reference entry to compare with.
///
/// No mix-in fixes the key and value types: a test class writes [#keyProvider()] and [#valueProvider()].
///
/// @param <T> the type of the entry under test
/// @param <K> the key type
/// @param <V> the value type
/// @since 0.1.3
public interface MapEntryContractSupport<T extends Map.Entry<K, V>, K, V>
        extends MapEntryProviderSupport<T, K, V>, SupportedMethods {

    /// Returns the provider of the keys.
    ///
    /// @return the key provider
    ObjectProvider<K> keyProvider();

    /// Returns the provider of the values.
    ///
    /// @return the value provider
    ObjectProvider<V> valueProvider();

    /// Returns `n` unique keys from the key provider.
    ///
    /// @param n the number of keys
    /// @return the keys, in the order they were made
    default List<K> uniqueKeys(final int n) {
        return unique(keyProvider().uniqueInstanceSupplier(), n);
    }

    /// Returns `n` unique values from the value provider.
    ///
    /// @param n the number of values
    /// @return the values, in the order they were made
    default List<V> uniqueValues(final int n) {
        return unique(valueProvider().uniqueInstanceSupplier(), n);
    }

    /// Returns a new source whose entry holds the given mapping.
    ///
    /// @param key the key
    /// @param value the value
    /// @return the source
    default MapEntrySource<T, K, V> sourceOf(final K key, final V value) {
        return mapEntryProvider().source(key, value);
    }

    /// Returns an entry of another class than most under test, an [AbstractMap.SimpleImmutableEntry], holding the
    /// given mapping: what the entry under test is compared with.
    ///
    /// @param key the key
    /// @param value the value
    /// @return the reference entry
    default Map.Entry<K, V> referenceEntry(final K key, final V value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    private static <X> List<X> unique(final Supplier<X> supplier, final int n) {
        List<X> made = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            made.add(supplier.get());
        }
        return made;
    }
}
