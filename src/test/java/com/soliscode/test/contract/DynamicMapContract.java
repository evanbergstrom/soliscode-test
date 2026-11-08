package com.soliscode.test.contract;

import java.util.Map;

import com.soliscode.test.breakable.Breakable;
import com.soliscode.test.breakable.BreakableBuilder;
import com.soliscode.test.breakable.BreakableMapBuilder;
import com.soliscode.test.contract.support.MapProviderSupport;
import com.soliscode.test.provider.MapProvider;
import com.soliscode.test.provider.ObjectProvider;

/// A base class for map contracts that are created dynamically, for maps built by a builder.
///
/// @param <M> the type of breakable map
/// @param <K> the type of keys
/// @param <V> the type of values
/// @since 1.0
public class DynamicMapContract<M extends Breakable & Map<K, V>, K, V>
        extends DynamicContract<M> implements MapProviderSupport<M, K, V> {

    private final BreakableMapBuilder<M, K, V> builder;
    private final ObjectProvider<K> keyProvider;
    private final ObjectProvider<V> valueProvider;

    /// Creates a contract for maps built by a builder.
    ///
    /// @param builderArg the builder of the maps under test
    /// @param keyProviderArg the provider of keys
    /// @param valueProviderArg the provider of values
    public DynamicMapContract(final BreakableMapBuilder<M, K, V> builderArg,
                              final ObjectProvider<K> keyProviderArg,
                              final ObjectProvider<V> valueProviderArg) {
        super(builderArg);
        this.builder = builderArg;
        this.keyProvider = keyProviderArg;
        this.valueProvider = valueProviderArg;
    }

    /// Returns the provider of the maps under test.
    ///
    /// @return the map provider
    @Override
    public MapProvider<K, V, M> provider() {
        return BreakableBuilder.mapProvider(builder, keyProvider, valueProvider);
    }
}
