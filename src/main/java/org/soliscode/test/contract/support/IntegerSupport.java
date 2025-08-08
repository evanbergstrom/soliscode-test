package org.soliscode.test.contract.support;

import org.jetbrains.annotations.NotNull;
import org.soliscode.test.provider.IntegerNumberProvider;

/// Common interface for contracts that test classes that implement the [Number] interface and have integer values
/// (*i.e.* Short, Integer, Long, etc.).
/// @param <T> the class of the objects being tested.
/// @author evanbergstrom
/// @since 1.0
public interface IntegerSupport<T extends Number> extends NumberSupport<T> {

    /// Returns a number provider that can be used to create instances of the class being tested.
    /// @return a number provider.
    @NotNull IntegerNumberProvider<T> provider();
}
