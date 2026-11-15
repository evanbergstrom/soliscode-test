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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.provider.ExecutorServiceProvider;
import com.soliscode.test.provider.ExecutorServiceProviders;

/// `ThreadSafeExecutorServiceContract` run by JUnit on a `ThreadPoolExecutor` of 2 threads with a `DiscardPolicy`,
/// which discards the tasks submitted after shutdown rather than reject them, and so opts out of
/// `rejectsTasksAfterShutdown()`.
@DisplayName("ThreadSafeExecutorServiceContract on a ThreadPoolExecutor with a DiscardPolicy")
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
        return ExecutorServiceProviders.of(DiscardingThreadPoolExecutorServiceTest::discarding);
    }

    @Override
    public boolean rejectsTasksAfterShutdown() {
        return false;
    }
}
