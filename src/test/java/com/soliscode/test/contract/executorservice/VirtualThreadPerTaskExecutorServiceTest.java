/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.executorservice;

import java.util.concurrent.ExecutorService;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.provider.ExecutorServiceProvider;
import com.soliscode.test.provider.ExecutorServiceProviders;

/// `ThreadSafeExecutorServiceContract` run by JUnit on `Executors.newVirtualThreadPerTaskExecutor()`, as a user's
/// test class runs it.
@DisplayName("ThreadSafeExecutorServiceContract on newVirtualThreadPerTaskExecutor()")
public class VirtualThreadPerTaskExecutorServiceTest extends AbstractTest
        implements ThreadSafeExecutorServiceContract<ExecutorService> {

    /// Default constructor.
    public VirtualThreadPerTaskExecutorServiceTest() {
        super();
    }

    @Override
    public ExecutorServiceProvider<ExecutorService> executorServiceProvider() {
        return ExecutorServiceProviders.virtualThreadPerTaskExecutors();
    }
}
