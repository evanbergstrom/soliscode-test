package org.soliscode.test.contract.support;

import org.soliscode.test.contract.CollectionContractConfig;

/// The base interface for all classes that test `Collection` methods. It allows the contract class to create
/// collections and elements using the associated providers.
///
/// @param <C> The collection type being tested.
/// @param <E> The element type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface CollectionContractSupport<C extends Iterable<E>, E>
        extends ContractSupport<C>, CollectionProviderSupport<C, E>, ElementProviderSupport<E>,
            CollectionContractConfig {

    /// The default number of elements ({@value}) to use for a test.
    int DEFAULT_SIZE = 10;
}
