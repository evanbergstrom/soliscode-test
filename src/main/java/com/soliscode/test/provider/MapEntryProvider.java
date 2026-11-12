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
