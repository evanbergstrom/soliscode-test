package org.soliscode.test.contract;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.breakable.Breakable;
import org.soliscode.test.breakable.BreakableBuilder;
import org.soliscode.test.breakable.BreakableMapBuilder;
import org.soliscode.test.contract.support.MapProviderSupport;
import org.soliscode.test.provider.MapProvider;
import org.soliscode.test.provider.ObjectProvider;

import java.util.Map;

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
    /// @param builder the builder of the maps under test
    /// @param keyProvider the provider of keys
    /// @param valueProvider the provider of values
    public DynamicMapContract(final @NonNull BreakableMapBuilder<M, K, V> builder,
                              final @NonNull ObjectProvider<K> keyProvider,
                              final @NonNull ObjectProvider<V> valueProvider) {
        super(builder);
        this.builder = builder;
        this.keyProvider = keyProvider;
        this.valueProvider = valueProvider;
    }

    /// Returns the provider of the maps under test.
    ///
    /// @return the map provider
    @Override
    public @NonNull MapProvider<K, V, M> provider() {
        return BreakableBuilder.mapProvider(builder, keyProvider, valueProvider);
    }
}
