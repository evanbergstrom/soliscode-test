package org.soliscode.test.contract.support;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.provider.ObjectProvider;
import org.soliscode.test.provider.Providers;

import java.util.Map;

/// Mixin interface that adds key and value providers for [Integer] and [String] respectively.
/// @author evanbergstrom
/// @since 1.0
public interface WithIntegerKeyAndStringValue<M extends Map<Integer, String>> extends MapContractSupport<Integer, String, M> {

    ObjectProvider<Integer> KEY_PROVIDER = Providers.integerProvider();

    ObjectProvider<String> VALUE_PROVIDER = Providers.stringProvider();

    @Override
    default @NonNull ObjectProvider<Integer> keyProvider() {
        return Providers.integerProvider();
    }

    @Override
    default @NonNull ObjectProvider<String> valueProvider() {
        return Providers.stringProvider();
    }
}
