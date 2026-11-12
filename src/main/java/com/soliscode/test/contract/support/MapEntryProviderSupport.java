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

import java.util.Map;

import com.soliscode.test.provider.MapEntryProvider;

/// Interface for contract classes that test a map entry, made by a [MapEntryProvider].
///
/// @param <T> the type of the entry under test
/// @param <K> the key type
/// @param <V> the value type
/// @since 0.1.3
public interface MapEntryProviderSupport<T extends Map.Entry<K, V>, K, V> {

    /// Returns the provider of the sources, and the entries under test in them.
    ///
    /// @return the map-entry provider
    MapEntryProvider<T, K, V> mapEntryProvider();
}
