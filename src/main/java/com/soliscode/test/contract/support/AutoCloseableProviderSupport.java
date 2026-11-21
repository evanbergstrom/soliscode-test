/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.support;

import com.soliscode.test.provider.AutoCloseableProvider;

/// Interface for contract classes that test an [AutoCloseable], whose resources an [AutoCloseableProvider] makes.
///
/// @param <C> the type of the resource under test
/// @since 0.1.3
public interface AutoCloseableProviderSupport<C extends AutoCloseable> {

    /// Returns the provider of the resources under test.
    ///
    /// @return the provider
    AutoCloseableProvider<C> autoCloseableProvider();
}
