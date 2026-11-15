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
package com.soliscode.test.provider;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;

/// Factories of [ExecutorServiceProvider]s: of the executors a supplier makes, and of the JDK's fixed thread pools,
/// virtual-thread-per-task executors and fork/join pools.
///
/// @since 0.1.5
public final class ExecutorServiceProviders {

    private ExecutorServiceProviders() { }

    /// A provider of the executors `make` makes.
    ///
    /// @param make makes a new executor, not yet shut down, each call
    /// @param <E>  the type of the executor
    /// @return the provider
    /// @throws NullPointerException if `make` is null
    public static <E extends ExecutorService> ExecutorServiceProvider<E> of(final Supplier<? extends E> make) {
        Objects.requireNonNull(make, "make");
        return make::get;
    }

    /// A provider of new fixed thread pools, made by [Executors#newFixedThreadPool(int)].
    ///
    /// @param threads the number of threads of each pool
    /// @return the provider
    /// @throws IllegalArgumentException if `threads` is not positive
    public static ExecutorServiceProvider<ExecutorService> fixedThreadPools(final int threads) {
        if (threads <= 0) {
            throw new IllegalArgumentException("threads must be positive: " + threads);
        }
        return () -> Executors.newFixedThreadPool(threads);
    }

    /// A provider of new executors that start a virtual thread for each task, made by
    /// [Executors#newVirtualThreadPerTaskExecutor()].
    ///
    /// @return the provider
    public static ExecutorServiceProvider<ExecutorService> virtualThreadPerTaskExecutors() {
        return Executors::newVirtualThreadPerTaskExecutor;
    }

    /// A provider of new fork/join pools, made by [ForkJoinPool#ForkJoinPool()], with a parallelism of the number of
    /// processors.
    ///
    /// @return the provider
    public static ExecutorServiceProvider<ForkJoinPool> forkJoinPools() {
        return ForkJoinPool::new;
    }
}
