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
