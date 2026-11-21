/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.provider;

import java.util.Map;

/// Makes the sources a map-entry contract tests: each a new source whose entry under test holds a given mapping.
///
/// ## Usage Examples
/// ```java
/// class MyEntryTest extends AbstractTest implements MapEntryContract<Map.Entry<Integer, String>, Integer, String> {
///     @Override
///     public MapEntryProvider<Map.Entry<Integer, String>, Integer, String> mapEntryProvider() {
///         return MapEntryProviders.fromMap(MyMap::new);
///     }
///     // keyProvider() and valueProvider() as well
/// }
/// ```
///
/// @param <T> the type of the entry under test
/// @param <K> the key type
/// @param <V> the value type
/// @since 0.1.3
/// @see MapEntryProviders
@FunctionalInterface
public interface MapEntryProvider<T extends Map.Entry<K, V>, K, V> {

    /// Returns a new source whose entry holds the given mapping.
    ///
    /// @param key the entry's key
    /// @param value the entry's value
    /// @return the source
    MapEntrySource<T, K, V> source(K key, V value);
}
