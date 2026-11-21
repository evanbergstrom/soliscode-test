/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
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
