package com.soliscode.test.contract.support;

import com.soliscode.test.provider.CollectionProvider;


/// Interface for contract classes that need a collection objectProvider.
/// @param <C> the type of the collection being provided.
/// @param <E> the type of the element in the collection.
/// @author evanbergstrom
/// @since 1.0
public interface CollectionProviderSupport<C extends Iterable<E>, E>
        extends ProviderSupport<C>, ElementProviderSupport<E> {

    /// Returns a collection objectProvider that can be used to create instances of the collection class being tested.
    /// @return a collection objectProvider.
    @Override
    CollectionProvider<C, E> provider();
}
