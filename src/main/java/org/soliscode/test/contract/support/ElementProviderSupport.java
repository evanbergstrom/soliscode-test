package org.soliscode.test.contract.support;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.provider.ObjectProvider;

/// Interface for contract classes that test iterables or collections with elements.
/// @param <E> the element type
/// @author evanbergstrom
/// @since 1.0
public interface ElementProviderSupport<E> {

    /// Returns the elements provider for the contract.
    /// @return the element provider.
    @NonNull ObjectProvider<E> elementProvider();
}
