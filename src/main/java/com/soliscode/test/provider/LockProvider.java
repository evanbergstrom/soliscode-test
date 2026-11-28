/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.provider;

import java.util.concurrent.locks.Lock;

/// Makes the locks a [com.soliscode.test.contract.lock.LockContract] tests: each a new lock, held by no thread.
///
/// ## Usage Examples
/// ```java
/// class MyLockTest extends AbstractTest implements LockContract<MyLock> {
///     @Override
///     public LockProvider<MyLock> lockProvider() {
///         return LockProviders.of(MyLock::new);
///     }
/// }
/// ```
///
/// @param <L> the type of the lock
/// @since 0.1.7
/// @see LockProviders
@FunctionalInterface
public interface LockProvider<L extends Lock> {

    /// Returns a new lock, held by no thread.
    ///
    /// @return the lock
    L newLock();
}
