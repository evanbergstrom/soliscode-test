package com.soliscode.test.contract.support;

import java.util.Map;

import com.soliscode.test.provider.MapProvider;
import com.soliscode.test.provider.ObjectProvider;

/// Interface for contract classes that need a map objectProvider.
///
/// @param <K> the key type
/// @param <V> the value type
/// @param <M> the map type being provided
/// @author evanbergstrom
/// @since 1.0
public interface MapProviderSupport<M extends Map<K, V>, K, V> extends ProviderSupport<M> {

    /// Returns a map objectProvider that can be used to create instances of the map class being tested.
    /// @return a map objectProvider
    @Override
    MapProvider<K, V, M> provider();

    /// Returns the object objectProvider used to create keys for map instances.
    /// @return the ObjectProvider used for generating map keys
    default ObjectProvider<K> keyProvider() {
        return provider().keyProvider();
    }

    /// Returns the object objectProvider used to create values for map instances.
    /// @return the ObjectProvider used for generating map values
    default ObjectProvider<V> valueProvider() {
        return provider().valueProvider();
    }
}
