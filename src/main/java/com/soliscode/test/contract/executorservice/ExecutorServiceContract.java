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

/// **Contract for the [ExecutorService] interface.**
///
/// A test class implements it with a [com.soliscode.test.provider.ExecutorServiceProvider] of new executors of the
/// class under test; JUnit runs its test methods as the class's own. Each test makes its own executor, and shuts it
/// down and waits for it, bounded, afterwards. A class that discards tasks after shutdown rather than reject them
/// overrides [com.soliscode.test.contract.support.ExecutorServiceContractSupport#rejectsTasksAfterShutdown()] to
/// return `false`. The tests read `execute`'s and `submit`'s `RejectedExecutionException` more strictly than the
/// Javadoc, which allows it for any task that "cannot be accepted": before shutdown, the executor must accept every
/// task they submit, [com.soliscode.test.contract.support.ExecutorServiceContractSupport#HELD_TASKS] held at once
/// here, and `SUBMITTERS × TASKS_PER_SUBMITTER` at once in [ThreadSafeSubmitContract]; a bounded executor needs a
/// queue at least that large. What `ExecutorService`'s Javadoc says holds under concurrent use is tested by
/// [ThreadSafeExecutorServiceContract], which extends this.
///
/// It extends a method contract for each of `ExecutorService`'s methods as of Java 25, and `Executor`'s `execute`:
/// [ExecuteContract], [SubmitCallableContract], [SubmitRunnableContract], [SubmitRunnableWithResultContract],
/// [ShutdownContract], [ShutdownNowContract], [IsShutdownContract], [IsTerminatedContract],
/// [AwaitTerminationContract], [CloseContract], [InvokeAllContract], [TimedInvokeAllContract], [InvokeAnyContract]
/// and [TimedInvokeAnyContract]; each says how it reads the Javadoc. Every call that may block runs with a time
/// limit, so that a faulty executor fails a test rather than hanging it.
///
/// ## Usage Examples
/// ```java
/// class FixedThreadPoolTest extends AbstractTest implements ExecutorServiceContract<ExecutorService> {
///     @Override
///     public ExecutorServiceProvider<ExecutorService> executorServiceProvider() {
///         return ExecutorServiceProviders.fixedThreadPools(2);
///     }
/// }
/// ```
///
/// @param <E> the type of the executor under test
/// @since 0.1.5
/// @see ExecutorService
public interface ExecutorServiceContract<E extends ExecutorService> extends ExecuteContract<E>,
        SubmitCallableContract<E>, SubmitRunnableContract<E>, SubmitRunnableWithResultContract<E>,
        ShutdownContract<E>, ShutdownNowContract<E>, IsShutdownContract<E>, IsTerminatedContract<E>,
        AwaitTerminationContract<E>, CloseContract<E>, InvokeAllContract<E>, TimedInvokeAllContract<E>,
        InvokeAnyContract<E>, TimedInvokeAnyContract<E> {
}
