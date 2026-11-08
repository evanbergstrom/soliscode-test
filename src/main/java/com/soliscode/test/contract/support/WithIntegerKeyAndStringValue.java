package com.soliscode.test.contract.support;

import java.util.Map;

import com.soliscode.test.provider.ObjectProvider;
import com.soliscode.test.provider.Providers;

/// Mixin interface that provides [Integer] keys and [String] values for map-related contract tests.
///
/// ### Purpose
/// This interface simplifies the implementation of map contracts by providing default
/// [ObjectProvider] implementations for common types. It is particularly useful when
/// testing [Map] implementations where the specific types of keys and values are less
/// important than the map's behavior.
///
/// ### Usage Examples
///
/// #### Implementing a Map Contract
/// ```java
/// class MyMapTest implements MapContract<Integer, String, MyMap<Integer, String>>,
///                            WithIntegerKeyAndStringValue<MyMap<Integer, String>> {
///     @Override
///     public MyMap<Integer, String> createMap() {
///         return new MyMap<>();
///     }
/// }
/// ```
///
/// ### Thread Safety
/// This interface is thread-safe as it only provides immutable providers and stateless
/// default methods.
///
/// @param <M> the type of map being tested
/// @author evanbergstrom
/// @since 1.0.0
public interface WithIntegerKeyAndStringValue<M extends Map<Integer, String>>
        extends MapContractSupport<M, Integer, String> {

    /// A objectProvider that generates [Integer] keys.
    ObjectProvider<Integer> KEY_PROVIDER = Providers.integerProvider();

    /// A objectProvider that generates [String] values.
    ObjectProvider<String> VALUE_PROVIDER = Providers.stringProvider();

    /// Returns a objectProvider for [Integer] keys.
    ///
    /// @return a non-null integer objectProvider
    @Override
    default ObjectProvider<Integer> keyProvider() {
        return Providers.integerProvider();
    }

    /// Returns a objectProvider for [String] values.
    ///
    /// @return a non-null string objectProvider
    @Override
    default ObjectProvider<String> valueProvider() {
        return Providers.stringProvider();
    }
}
