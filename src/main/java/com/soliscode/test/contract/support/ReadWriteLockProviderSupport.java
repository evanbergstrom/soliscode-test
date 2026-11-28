/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.support;

import java.util.concurrent.locks.ReadWriteLock;

import com.soliscode.test.provider.ReadWriteLockProvider;

/// Interface for contract classes that test a [ReadWriteLock], whose locks a [ReadWriteLockProvider] makes.
///
/// @param <L> the type of the lock under test
/// @since 0.1.7
public interface ReadWriteLockProviderSupport<L extends ReadWriteLock> {

    /// Returns the provider of the locks under test.
    ///
    /// @return the provider
    ReadWriteLockProvider<L> readWriteLockProvider();
}
