package com.soliscode.test.contract.support;

import com.soliscode.test.provider.ObjectProvider;

/// Interface for contract classes that test iterables or collections with elements.
/// @param <E> the element type
/// @author evanbergstrom
/// @since 1.0
public interface ElementProviderSupport<E> {

    /// Returns the elements objectProvider for the contract.
    /// @return the element objectProvider.
    ObjectProvider<E> elementProvider();
}
