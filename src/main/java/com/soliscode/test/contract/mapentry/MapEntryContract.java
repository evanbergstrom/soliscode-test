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
package com.soliscode.test.contract.mapentry;

import java.util.Map;

/// **Contract for the [Map.Entry] interface.**
///
/// A test class implements it with a [com.soliscode.test.provider.MapEntryProvider] of the entry under test, and
/// providers of keys and values; JUnit runs its test methods as the class's own. Where the entry does not support
/// `setValue`, the class says so through `supportsMethod` ([MapEntryMethods#MAP_ENTRY_SET_VALUE]).
///
/// It follows the Javadoc of [Map.Entry]: `getKey` and `getValue` return the mapping ([GetKeyContract],
/// [GetValueContract]), `setValue` replaces the value, returns the old one and writes through to the map behind the
/// entry ([SetValueContract]), and `equals` and `hashCode` follow the formulas there ([EqualsContract],
/// [HashCodeContract]). Where it reads the Javadoc more loosely, the method contract says so. Keys and values
/// are not null, until #13; what an entry does after its mapping is removed from the map, which the Javadoc leaves
/// undefined, is not tested; nor is `toString`, which [Map.Entry] does not specify.
///
/// ## Usage Examples
/// ```java
/// class TreeMapEntryTest extends AbstractTest
///         implements MapEntryContract<Map.Entry<Integer, String>, Integer, String> {
///     @Override
///     public MapEntryProvider<Map.Entry<Integer, String>, Integer, String> mapEntryProvider() {
///         return MapEntryProviders.fromMap(TreeMap::new);
///     }
///
///     @Override
///     public ObjectProvider<Integer> keyProvider() {
///         return Providers.integerProvider();
///     }
///
///     @Override
///     public ObjectProvider<String> valueProvider() {
///         return Providers.stringProvider();
///     }
/// }
/// ```
///
/// @param <T> the type of the entry under test
/// @param <K> the key type
/// @param <V> the value type
/// @since 0.1.3
/// @see Map.Entry
public interface MapEntryContract<T extends Map.Entry<K, V>, K, V>
        extends GetKeyContract<T, K, V>, GetValueContract<T, K, V>, SetValueContract<T, K, V>,
        EqualsContract<T, K, V>, HashCodeContract<T, K, V> {
}
