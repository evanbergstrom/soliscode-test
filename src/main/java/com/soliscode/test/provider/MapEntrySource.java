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
import java.util.Optional;

/// An entry under test, and the map behind it, if there is one: what a
/// [com.soliscode.test.contract.mapentry.MapEntryContract] tests.
///
/// A source is made by a [MapEntryProvider] from a key and a value, and its entry holds that mapping. Where the entry
/// is a map's (one got by iterating its `entrySet()`), [#backingValue()] reads the map, so that a contract can see
/// that `setValue` writes through to it, as [Map.Entry#setValue(Object)] says.
///
/// @param <T> the type of the entry under test
/// @param <K> the key type
/// @param <V> the value type
/// @since 0.1.3
public interface MapEntrySource<T extends Map.Entry<K, V>, K, V> {

    /// Returns the entry under test: the same one on each call.
    ///
    /// @return the entry
    T entry();

    /// Returns the value the map behind the entry holds now for the entry's key, read other than through the entry;
    /// or empty where there is no map behind it, or the map no longer holds the key.
    ///
    /// It must not use the entry under test, or a contract would compare the entry with itself.
    ///
    /// @return the value in the backing map; empty unless overridden
    default Optional<V> backingValue() {
        return Optional.empty();
    }
}
