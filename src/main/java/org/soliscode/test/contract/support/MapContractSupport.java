package org.soliscode.test.contract.support;

import java.util.Map;

/// The base interface for all classes that test `Map` methods. It allows the contract class to create
/// maps, keys, and values using the associated providers.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface MapContractSupport<K, V, M extends Map<K, V>>
        extends ContractSupport<M>, MapProviderSupport<K, V, M> {

    /// The default number of entries ({@value}) to use for a test.
    int DEFAULT_SIZE = 10;
}
