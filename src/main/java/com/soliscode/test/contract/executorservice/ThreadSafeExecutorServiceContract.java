/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.executorservice;

import java.util.concurrent.ExecutorService;

/// **Contract for the thread safety of an `ExecutorService`**
///
/// It extends [ExecutorServiceContract] with the tests of what [ExecutorService]'s Javadoc says holds under
/// concurrent use: that tasks submitted from several threads at once each run once, and see what their submitter did
/// before submitting them, and that what a task does is seen after `get` ([ThreadSafeSubmitContract]); and that when
/// threads submit while another shuts the executor down, each accepted task runs once ([ThreadSafeShutdownContract]).
///
/// The tests are tagged [com.soliscode.test.annotations.ConcurrencyTest] and
/// [com.soliscode.test.annotations.VerySlow], and run on a [com.soliscode.test.safety.ContentionCoordinator], with a
/// deadline of their own that bounds them in time.
///
/// ## Usage Examples
/// ```java
/// class ForkJoinPoolSafetyTest extends AbstractTest implements ThreadSafeExecutorServiceContract<ForkJoinPool> {
///     @Override
///     public ExecutorServiceProvider<ForkJoinPool> executorServiceProvider() {
///         return ExecutorServiceProviders.forkJoinPools();
///     }
/// }
/// ```
///
/// @param <E> the type of the executor under test
/// @see ExecutorService
/// @since 0.1.5
public interface ThreadSafeExecutorServiceContract<E extends ExecutorService> extends ExecutorServiceContract<E>,
        ThreadSafeSubmitContract<E>, ThreadSafeShutdownContract<E> {
}
