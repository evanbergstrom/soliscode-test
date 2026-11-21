/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.support;

import java.util.concurrent.ExecutorService;

import com.soliscode.test.provider.ExecutorServiceProvider;

/// Interface for contract classes that test an [ExecutorService], whose executors an [ExecutorServiceProvider]
/// makes.
///
/// @param <E> the type of the executor under test
/// @since 0.1.5
public interface ExecutorServiceProviderSupport<E extends ExecutorService> {

    /// Returns the provider of the executors under test.
    ///
    /// @return the provider
    ExecutorServiceProvider<E> executorServiceProvider();
}
