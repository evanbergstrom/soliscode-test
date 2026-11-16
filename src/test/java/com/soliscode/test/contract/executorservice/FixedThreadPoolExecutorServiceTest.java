/*
 * Copyright 2026 Evan Bergstrom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
