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
package com.soliscode.test.contract.autocloseable;

/// **Contract for the [AutoCloseable] interface.**
///
/// A test class implements it with a [com.soliscode.test.provider.AutoCloseableProvider] of new, open resources of the
/// class under test, and the uses its class documents, each with what it does once the resource is closed; JUnit runs
/// its test methods as the class's own. Where the class's `close` is not idempotent, the class overrides
/// [com.soliscode.test.contract.support.AutoCloseableContractSupport#closeIsIdempotent()] to return `false`. A
/// [java.io.Closeable] is tested with [com.soliscode.test.contract.closeable.CloseableContract], which extends this.
///
/// `AutoCloseable` has one method, so this contract is [CloseContract], which says how it reads the Javadoc.
///
/// ## Usage Examples
/// ```java
/// class ExecutorServiceCloseTest extends AbstractTest implements AutoCloseableContract<ExecutorService> {
///     @Override
///     public AutoCloseableProvider<ExecutorService> autoCloseableProvider() {
///         return AutoCloseableProviders.of(Executors::newSingleThreadExecutor,
///                 ResourceUse.throwsOnceClosed("submit(Runnable)", RejectedExecutionException.class,
///                         e -> e.submit(() -> { })),
///                 ResourceUse.worksOnceClosed("isShutdown()", ExecutorService::isShutdown));
///     }
/// }
/// ```
///
/// @param <C> the type of the resource under test
/// @since 0.1.3
/// @see AutoCloseable
public interface AutoCloseableContract<C extends AutoCloseable> extends CloseContract<C> {
}
