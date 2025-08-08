package org.soliscode.test.contract.support;

import org.jetbrains.annotations.NotNull;
import org.soliscode.test.provider.CollectionProvider;


/// Interface for contract classes that need a collection provider.
/// @param <E> the type of the the collection
/// @param <C> the type of he collection being provided.
/// @author evanbergstrom
/// @since 1.0
public interface CollectionProviderSupport<E, C extends Iterable<E>>
        extends ProviderSupport<C>, ElementProviderSupport<E> {

    /// Returns a collection provider that can be used to create instances of the collection class being tested.
    /// @return a collection provider.
    @NotNull CollectionProvider<E, C> provider();
}
