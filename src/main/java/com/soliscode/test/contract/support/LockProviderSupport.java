/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.support;

import java.util.concurrent.locks.Lock;

import com.soliscode.test.provider.LockProvider;

/// Interface for contract classes that test a [Lock], whose locks a [LockProvider] makes.
///
/// @param <L> the type of the lock under test
/// @since 0.1.7
public interface LockProviderSupport<L extends Lock> {

    /// Returns the provider of the locks under test.
    ///
    /// @return the provider
    LockProvider<L> lockProvider();
}
