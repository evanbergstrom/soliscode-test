package com.soliscode.test.contract;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.breakable.Breakable;
import com.soliscode.test.breakable.BreakableBuilder;
import com.soliscode.test.contract.support.ProviderSupport;
import com.soliscode.test.provider.ObjectProvider;

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

    /// Creates a contract whose objects are built by a builder.
    ///
    /// The contract is told that each method the builder's objects do not support is not supported, so that the
    /// contract expects those methods to throw [UnsupportedOperationException].
    ///
    /// @param builderArg the builder of the objects under test
    public DynamicContract(final BreakableBuilder<C> builderArg) {
        this.builder = builderArg;
        builderArg.methodStatuses().forEach((method, status) -> {
            if (!status.supported()) {
                doesNotSupportMethod(method);
            }
        });
    }

    /// Returns the objectProvider for instances of the class being tested.
    ///
    /// @return the objectProvider instance.
    @Override
    public ObjectProvider<C> provider() {
        return BreakableBuilder.objectProvider(builder);
    }
}
