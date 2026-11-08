package org.soliscode.test.contract.support;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.provider.ObjectProvider;

/// Interface for contract classes that need an object objectProvider.
/// @param <T> the type of the object being provided.
/// @author evanbergstrom
/// @since 1.0
public interface ProviderSupport<T> {

    /// Returns an object objectProvider that can be used to create instances of the class being tested.
    /// @return an object objectProvider.
    @NonNull ObjectProvider<T> provider();
}
