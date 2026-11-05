package org.soliscode.test.contract;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.breakable.Breakable;
import org.soliscode.test.breakable.BreakableBuilder;
import org.soliscode.test.contract.support.ProviderSupport;
import org.soliscode.test.provider.ObjectProvider;

/// A base class for contracts that are created dynamically for testing purposes.
///
/// This class provides support for creating contract instances with specific breaks or
/// method support configurations. It is used to generate tests for implementations
/// of various interfaces (e.g., `Collection`, `Map`) where different behavioral
/// anomalies need to be systematically tested.
///
/// @author evanbergstrom
/// @since 1.0
public abstract class DynamicContract<C extends Breakable> extends AbstractTest
        implements ProviderSupport<C> {

    private final BreakableBuilder<C> builder;

    public DynamicContract(final @NonNull BreakableBuilder<C> builder) {
        this.builder = builder;
    }

    /// Returns the objectProvider for instances of the class being tested.
    ///
    /// @return the objectProvider instance.
    @Override
    public @NonNull ObjectProvider<C> provider() {
        return BreakableBuilder.objectProvider(builder);
    }
}
