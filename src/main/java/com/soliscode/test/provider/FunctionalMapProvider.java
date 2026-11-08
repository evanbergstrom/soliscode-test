package com.soliscode.test.provider;


import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/// A map objectProvider that uses a set of functions to construct instances of the map.
///
/// @param <K> the type of keys maintained by the map
/// @param <V> the type of mapped values
/// @param <M> the map type
/// @author evanbergstrom
/// @since 1.0.0
public class FunctionalMapProvider<K, V, M extends Map<K, V>> extends FunctionalProvider<M>
        implements MapProvider<K, V, M> {

    /// Default number of entries to use for a test.
    private static final int DEFAULT_SIZE = 10;

    private final Function<Map<K, V>, M> mapConstructor;
    private final ObjectProvider<K> keyProvider;
    private final ObjectProvider<V> valueProvider;

    /// Create an instance of this map objectProvider.
    /// @param defaultConstructor the supplier to use to create default instances of the map.
    /// @param copyConstructor the function to use to create a copy of the map.
    /// @param mapConstructorArg the function to use to create an instance from another map.
    /// @param keyProviderArg the key objectProvider.
    /// @param valueProviderArg the value objectProvider.
    /// @throws NullPointerException if any of the arguments are `null`
    public FunctionalMapProvider(final Supplier<M> defaultConstructor,
                                 final Function<M, M> copyConstructor,
                                 final Function<Map<K, V>, M> mapConstructorArg,
                                 final ObjectProvider<K> keyProviderArg,
                                 final ObjectProvider<V> valueProviderArg) {
        super(defaultConstructor, copyConstructor);
        this.mapConstructor = Objects.requireNonNull(mapConstructorArg);
        this.keyProvider = Objects.requireNonNull(keyProviderArg);
        this.valueProvider = Objects.requireNonNull(valueProviderArg);
    }

    @Override
    public ObjectProvider<K> keyProvider() {
        return keyProvider;
    }

    @Override
    public ObjectProvider<V> valueProvider() {
        return valueProvider;
    }

    @Override
    public M emptyInstance() {
        return defaultInstance();
    }

    @Override
    public M createInstance(final Map<K, V> m) {
        return mapConstructor.apply(m);
    }

    @Override
    public M createInstance(final long seed) {
        return createInstance(DEFAULT_SIZE, seed);
    }

    @Override
    public M createSingleton() {
        return createSingleton(keyProvider.createInstance(), valueProvider.createInstance());
    }

    @Override
    public M createSingleton(final K key, final V value) {
        M map = emptyInstance();
        map.put(key, value);
        return map;
    }

    @Override
    public M createInstance(final int size) {
        return createInstance(size, 0);
    }

    @Override
    public M createInstance(final int size, final long seed) {
        M map = emptyInstance();
        var keys = keyProvider.createUniqueInstances(size, seed);
        var values = valueProvider.createUniqueInstances(size, seed);
        for (int i = 0; i < size; i++) {
            map.put(keys.get(i), values.get(i));
        }
        return map;
    }
}
