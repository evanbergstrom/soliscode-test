/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.support;

import java.util.concurrent.Future;

import com.soliscode.test.provider.FutureProvider;

/// Interface for contract classes that test a [Future], whose futures a [FutureProvider] makes.
///
/// @param <F> the type of the future under test
/// @param <V> the type of its result
/// @since 0.1.4
public interface FutureProviderSupport<F extends Future<V>, V> {

    /// Returns the provider of the futures under test.
    ///
    /// @return the provider
    FutureProvider<F, V> futureProvider();
}
