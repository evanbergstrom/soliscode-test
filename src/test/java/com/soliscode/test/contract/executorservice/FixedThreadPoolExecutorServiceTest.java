/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.executorservice;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.provider.ExecutorServiceProvider;
import com.soliscode.test.provider.ExecutorServiceProviders;

/// `ThreadSafeExecutorServiceContract` run by JUnit on `Executors.newFixedThreadPool(2)`, as a user's test class
/// runs it, with each pool's core threads started first: a pool whose threads are still to start can fail
/// `ThreadSafeShutdownContract` through `ThreadPoolExecutor`'s own race, which that contract describes (#54).
@DisplayName("ThreadSafeExecutorServiceContract on newFixedThreadPool(2), core threads prestarted")
public class FixedThreadPoolExecutorServiceTest extends AbstractTest
        implements ThreadSafeExecutorServiceContract<ThreadPoolExecutor> {

    /// Default constructor.
    public FixedThreadPoolExecutorServiceTest() {
        super();
    }

    @Override
    public ExecutorServiceProvider<ThreadPoolExecutor> executorServiceProvider() {
        return ExecutorServiceProviders.of(() -> {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
            executor.prestartAllCoreThreads();
            return executor;
        });
    }
}
