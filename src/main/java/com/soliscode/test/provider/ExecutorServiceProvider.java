/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.provider;

import java.util.concurrent.ExecutorService;

/// Makes the executors a [com.soliscode.test.contract.executorservice.ExecutorServiceContract] tests: each a new
/// executor, not yet shut down.
///
/// The contract shuts down every executor it makes, and waits, bounded, for it to terminate.
///
/// ## Usage Examples
/// ```java
/// class MyExecutorTest extends AbstractTest implements ExecutorServiceContract<MyExecutor> {
///     @Override
///     public ExecutorServiceProvider<MyExecutor> executorServiceProvider() {
///         return ExecutorServiceProviders.of(() -> new MyExecutor(4));
///     }
/// }
/// ```
///
/// @param <E> the type of the executor
/// @since 0.1.5
/// @see ExecutorServiceProviders
@FunctionalInterface
public interface ExecutorServiceProvider<E extends ExecutorService> {

    /// Returns a new executor, not yet shut down, that accepts every task the contract submits before shutdown: up to
    /// [com.soliscode.test.contract.support.ExecutorServiceContractSupport#HELD_TASKS] held at once, and
    /// `ThreadSafeSubmitContract.SUBMITTERS × TASKS_PER_SUBMITTER` under contention.
    ///
    /// @return the executor
    E newExecutor();
}
