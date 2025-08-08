package org.soliscode.test.contract.support;

import org.jetbrains.annotations.NotNull;
import org.soliscode.test.provider.NumberProvider;

/// Common interface for contracts that test classes that implement the [Number] interface.
/// @param <T> the class of the objects being tested.
/// @author evanbergstrom
/// @since 1.0
public interface NumberSupport<T extends Number> extends ContractSupport<T> {

    /// Returns a number provider that can be used to create instances of the class being tested.
    /// @return a number provider.
    @NotNull NumberProvider<T> provider();
}
