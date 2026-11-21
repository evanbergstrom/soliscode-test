/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.executorservice;

import java.util.concurrent.ForkJoinPool;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.provider.ExecutorServiceProvider;
import com.soliscode.test.provider.ExecutorServiceProviders;

/// `ThreadSafeExecutorServiceContract` run by JUnit on a new `ForkJoinPool`, as a user's test class runs it.
@DisplayName("ThreadSafeExecutorServiceContract on a new ForkJoinPool")
public class ForkJoinPoolExecutorServiceTest extends AbstractTest
        implements ThreadSafeExecutorServiceContract<ForkJoinPool> {

    /// Default constructor.
    public ForkJoinPoolExecutorServiceTest() {
        super();
    }

    @Override
    public ExecutorServiceProvider<ForkJoinPool> executorServiceProvider() {
        return ExecutorServiceProviders.forkJoinPools();
    }
}
