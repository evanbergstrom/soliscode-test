package org.soliscode.test.provider;

import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/// A map provider that uses a set of functions to construct instances of the map.
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

    private final @NonNull Function<Map<K, V>, M> mapConstructor;
    private final @NonNull ObjectProvider<K> keyProvider;
    private final @NonNull ObjectProvider<V> valueProvider;

    /// Create an instance of this map provider.
    /// @param defaultConstructor the supplier to use to create default instances of the map.
    /// @param copyConstructor the function to use to create a copy of the map.
    /// @param mapConstructor the function to use to create an instance from another map.
    /// @param keyProvider the key provider.
    /// @param valueProvider the value provider.
    /// @throws NullPointerException if any of the arguments are `null`
    public FunctionalMapProvider(final @NonNull Supplier<M> defaultConstructor,
                                 final @NonNull Function<M, M> copyConstructor,
                                 final @NonNull Function<Map<K, V>, M> mapConstructor,
                                 final @NonNull ObjectProvider<K> keyProvider,
                                 final @NonNull ObjectProvider<V> valueProvider) {
        super(defaultConstructor, copyConstructor);
        this.mapConstructor = Objects.requireNonNull(mapConstructor);
        this.keyProvider = Objects.requireNonNull(keyProvider);
        this.valueProvider = Objects.requireNonNull(valueProvider);
    }

    @Override
    public @NonNull ObjectProvider<K> keyProvider() {
        return keyProvider;
    }

    @Override
    public @NonNull ObjectProvider<V> valueProvider() {
        return valueProvider;
    }

    @Override
    public @NonNull M emptyInstance() {
        return defaultInstance();
    }

    @Override
    public @NonNull M createInstance(final @NonNull Map<K, V> m) {
        return mapConstructor.apply(m);
    }

    @Override
    public @NonNull M createInstance(final long seed) {
        return createInstance(DEFAULT_SIZE, seed);
    }

    @Override
    public @NonNull M createSingleton() {
        return createSingleton(keyProvider.createInstance(), valueProvider.createInstance());
    }

    @Override
    public @NonNull M createSingleton(final K key, final V value) {
        M map = emptyInstance();
        map.put(key, value);
        return map;
    }

    @Override
    public @NonNull M createInstance(final int size) {
        return createInstance(size, 0);
    }

    @Override
    public @NonNull M createInstance(final int size, final long seed) {
        M map = emptyInstance();
        var keys = keyProvider.createUniqueInstances(size, seed);
        var values = valueProvider.createUniqueInstances(size, seed);
        for (int i = 0; i < size; i++) {
            map.put(keys.get(i), values.get(i));
        }
        return map;
    }
}
