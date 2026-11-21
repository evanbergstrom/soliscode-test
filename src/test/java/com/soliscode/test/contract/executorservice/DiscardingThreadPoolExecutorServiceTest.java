/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.executorservice;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.provider.ExecutorServiceProvider;
import com.soliscode.test.provider.ExecutorServiceProviders;

/// `ThreadSafeExecutorServiceContract` run by JUnit on a `ThreadPoolExecutor` of 2 threads with a `DiscardPolicy`,
/// which discards the tasks submitted after shutdown rather than reject them, and so opts out of
/// `rejectsTasksAfterShutdown()`. Each executor's core threads are started first: one whose threads are still to
/// start can fail `ThreadSafeShutdownContract` through `ThreadPoolExecutor`'s own race, which that contract
/// describes (#54).
@DisplayName("ThreadSafeExecutorServiceContract on a ThreadPoolExecutor with a DiscardPolicy, core threads prestarted")
public class DiscardingThreadPoolExecutorServiceTest extends AbstractTest
        implements ThreadSafeExecutorServiceContract<ThreadPoolExecutor> {

    /// Default constructor.
    public DiscardingThreadPoolExecutorServiceTest() {
        super();
    }

    /// A new `ThreadPoolExecutor` of 2 threads, over an unbounded queue, with a `DiscardPolicy`.
    ///
    /// @return the executor
    static ThreadPoolExecutor discarding() {
        return new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                new ThreadPoolExecutor.DiscardPolicy());
    }

    @Override
    public ExecutorServiceProvider<ThreadPoolExecutor> executorServiceProvider() {
        return ExecutorServiceProviders.of(() -> {
            ThreadPoolExecutor executor = discarding();
            executor.prestartAllCoreThreads();
            return executor;
        });
    }

    @Override
    public boolean rejectsTasksAfterShutdown() {
        return false;
    }
}
