package org.soliscode.test.contract.support;

import org.soliscode.test.contract.CollectionContractConfig;

/// The base interface for all classes that test `Collection` methods. It allows the contract class to create
/// collections and elements using the associated providers.
///
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface CollectionContractSupport<E, C extends Iterable<E>>
        extends ContractSupport<C>, CollectionProviderSupport<E, C>, ElementProviderSupport<E>, CollectionContractConfig {

    /// The default number of elements ({@value}) to use for a test.
    int DEFAULT_SIZE = 10;
}
