package org.soliscode.test.contract.support;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.provider.MapProvider;
import org.soliscode.test.provider.ObjectProvider;

import java.util.Map;

/// Interface for contract classes that need a map provider.
///
/// @param <K> the key type
/// @param <V> the value type
/// @param <M> the map type being provided
/// @author evanbergstrom
/// @since 1.0
public interface MapProviderSupport<K, V, M extends Map<K, V>> extends ProviderSupport<M> {

    /// Returns a map provider that can be used to create instances of the map class being tested.
    /// @return a map provider
    @NonNull MapProvider<K, V, M> provider();

    /// Returns the object provider used to create keys for map instances.
    /// @return the ObjectProvider used for generating map keys
    default @NonNull ObjectProvider<K> keyProvider() {
        return provider().keyProvider();
    }

    /// Returns the object provider used to create values for map instances.
    /// @return the ObjectProvider used for generating map values
    default @NonNull ObjectProvider<V> valueProvider() {
        return provider().valueProvider();
    }
}
