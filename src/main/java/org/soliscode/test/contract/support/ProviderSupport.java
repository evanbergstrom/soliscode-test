package org.soliscode.test.contract.support;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.provider.ObjectProvider;

/// Interface for contract classes that need an object provider.
/// @param <T> the type of the object being provided.
/// @author evanbergstrom
/// @since 1.0
public interface ProviderSupport<T> {

    /// Returns an object provider that can be used to create instances of the class being tested.
    /// @return an object provider.
    @NonNull ObjectProvider<T> provider();
}
