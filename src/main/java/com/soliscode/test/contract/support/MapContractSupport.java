package com.soliscode.test.contract.support;

import java.util.Map;

/// The base interface for all classes that test `Map` methods. It allows the contract class to create
/// maps, keys, and values using the associated providers.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface MapContractSupport<M extends Map<K, V>, K, V>
        extends ContractSupport<M>, MapProviderSupport<M, K, V> {

    /// Returns the number of entries a contract puts in a map it creates for a test.
    ///
    /// A test class may override it to test larger or smaller maps.
    ///
    /// @return the number of entries; 10 unless overridden
    /// @since 0.1.1
    default int defaultSize() {
        return 10;
    }

    /// Says whether the map under test permits a `null` key. Where it does not, the contracts that test nulls expect
    /// a `null` key to be refused with `NullPointerException`; where it does, they do not run those tests.
    ///
    /// @return `true` unless overridden; `ConcurrentMapContractSupport` returns `false`
    /// @since 0.1.2
    default boolean permitNullKeys() {
        return true;
    }

    /// Says whether the map under test permits a `null` value. Where it does not, the contracts that test nulls
    /// expect a `null` value to be refused with `NullPointerException`; where it does, they do not run those tests.
    ///
    /// @return `true` unless overridden; `ConcurrentMapContractSupport` returns `false`
    /// @since 0.1.2
    default boolean permitNullValues() {
        return true;
    }
}
