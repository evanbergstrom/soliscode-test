/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.support;

import com.soliscode.test.provider.ComparableProvider;

/// Interface for contract classes that test a [Comparable], whose samples a [ComparableProvider] makes.
///
/// @param <T> the type under test
/// @since 0.1.3
public interface ComparableProviderSupport<T extends Comparable<? super T>> {

    /// Returns the provider of the ordered samples of the type under test.
    ///
    /// @return the comparable provider
    ComparableProvider<T> comparableProvider();
}
