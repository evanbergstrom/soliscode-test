/*
 * Copyright 2024 Evan Bergstrom
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


import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;

/// Utility class for creating map providers.
/// @author evanbergstrom
/// @since 1.0
public final class MapProviders {

    private MapProviders() { }

    /// Create an instance of this map objectProvider that uses the methods and element providers specified in the
    /// arguments for its implementation.
    ///
    /// For example, to create a map objectProvider for a `HashMap` of String keys and Integer values:
    /// ```java
    ///     MapProvider<String, Integer, HashMap<String, Integer>> objectProvider = MapProviders.from(
    ///         HashMap::new, HashMap::new, HashMap::new,
    ///         Providers.stringProvider(), Providers.integerProvider());
    /// ```
    /// @param <K> the key type.
    /// @param <V> the value type.
    /// @param <M> the map type.
    /// @param defaultConstructor the supplier to use to create default instances of the map.
    /// @param copyConstructor the function to use to create a copy of the map.
    /// @param mapConstructor the function to use to create an instance from another map.
    /// @param keyProvider the key objectProvider.
    /// @param valueProvider the value objectProvider.
    /// @return the map objectProvider.
    /// @throws NullPointerException if any of the arguments are `null`
    public static <K, V, M extends Map<K, V>> MapProvider<K, V, M> from(
            final Supplier<M> defaultConstructor,
            final Function<M, M> copyConstructor,
            final Function<Map<K, V>, M> mapConstructor,
            final ObjectProvider<K> keyProvider,
            final ObjectProvider<V> valueProvider) {
        return new FunctionalMapProvider<>(defaultConstructor, copyConstructor, mapConstructor,
                keyProvider, valueProvider);
    }

    /// Creates a map objectProvider for instances of [HashMap] with keys and values created using the specified
    /// providers.
    /// @param <K> the type of keys.
    /// @param <V> the type of values.
    /// @param keyProvider the objectProvider to use to create the keys.
    /// @param valueProvider the objectProvider to use to create the values.
    /// @return the map objectProvider.
    // NonApiType: this provides HashMap itself, to test it.
    @SuppressWarnings("NonApiType")
    public static <K, V> MapProvider<K, V, HashMap<K, V>> provideHashMap(
            final ObjectProvider<K> keyProvider,
            final ObjectProvider<V> valueProvider) {
        return MapProviders.from(HashMap::new, HashMap::new, HashMap::new, keyProvider, valueProvider);
    }

    /// Creates a map objectProvider for instances of [TreeMap] with keys and values created using the specified
    /// providers.
    /// @param <K> the type of keys.
    /// @param <V> the type of values.
    /// @param keyProvider the objectProvider to use to create the keys.
    /// @param valueProvider the objectProvider to use to create the values.
    /// @return the map objectProvider.
    // NonApiType: this provides TreeMap itself, to test it.
    @SuppressWarnings("NonApiType")
    public static <K, V> MapProvider<K, V, TreeMap<K, V>> provideTreeMap(
            final ObjectProvider<K> keyProvider,
            final ObjectProvider<V> valueProvider) {
        return MapProviders.from(TreeMap::new, TreeMap::new, TreeMap::new, keyProvider, valueProvider);
    }

    /// Creates a map objectProvider for instances of [LinkedHashMap] with keys and values created using the specified
    /// providers.
    /// @param <K> the type of keys.
    /// @param <V> the type of values.
    /// @param keyProvider the objectProvider to use to create the keys.
    /// @param valueProvider the objectProvider to use to create the values.
    /// @return the map objectProvider.
    // NonApiType: this provides LinkedHashMap itself, to test it.
    @SuppressWarnings("NonApiType")
    public static <K, V> MapProvider<K, V, LinkedHashMap<K, V>> provideLinkedHashMap(
            final ObjectProvider<K> keyProvider,
            final ObjectProvider<V> valueProvider) {
        return MapProviders.from(LinkedHashMap::new, LinkedHashMap::new, LinkedHashMap::new, keyProvider,
                valueProvider);
    }

    /// Creates a objectProvider that wraps the provided map from an underlying objectProvider.
    /// @param <K> the key type.
    /// @param <V> the value type.
    /// @param <M> the map type provided by the underlying map.
    /// @param <W> the map type of the wrapped map.
    /// @param provider the underlying objectProvider.
    /// @param wrapper the function used to wrap the provided map.
    /// @return the wrapped map objectProvider.
    public static <K, V, M extends Map<K, V>, W extends Map<K, V>>
        MapProvider<K, V, W> wrap(final MapProvider<K, V, M> provider, final Function<M, W> wrapper)  {
        return new WrappedMapProvider<>(provider, wrapper);
    }

    private record WrappedMapProvider<K, V, M extends Map<K, V>, W extends Map<K, V>>(
            MapProvider<K, V, M> provider, Function<M, W> wrapper)
                implements MapProvider<K, V, W> {

        @Override
        public ObjectProvider<K> keyProvider() {
            return provider.keyProvider();
        }

        @Override
        public ObjectProvider<V> valueProvider() {
            return provider.valueProvider();
        }

        @Override
        public W defaultInstance() {
            return wrapper.apply(provider.defaultInstance());
        }

        @Override
        public W emptyInstance() {
            return wrapper.apply(provider.emptyInstance());
        }

        @Override
        public W copyInstance(final W ws) {
            // This is a bit tricky since we need to unwrap or use the objectProvider's copy.
            // But we don't have an unwrap. CollectionProviders just calls objectProvider.createInstance(ws)
            // which in CollectionProvider takes a Collection.
            // For MapProvider, createInstance(Map) exists.
            return wrapper.apply(provider.createInstance(ws));
        }

        @Override
        public W createInstance(final Map<K, V> m) {
            return wrapper.apply(provider.createInstance(m));
        }

        @Override
        public W createInstance(final long seed) {
            return wrapper.apply(provider.createInstance(seed));
        }

        @Override
        public W createSingleton() {
            return wrapper.apply(provider.createSingleton());
        }

        @Override
        public W createSingleton(final K key, final V value) {
            return wrapper.apply(provider.createSingleton(key, value));
        }

        @Override
        public W createInstance(final int size) {
            return wrapper.apply(provider.createInstance(size));
        }

        @Override
        public W createInstance(final int size, final long seed) {
            return wrapper.apply(provider.createInstance(size, seed));
        }
    }
}
