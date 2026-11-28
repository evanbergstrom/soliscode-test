/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.provider;

import java.util.concurrent.locks.ReadWriteLock;

/// Makes the read-write locks a [com.soliscode.test.contract.readwritelock.ReadWriteLockContract] tests: each a new
/// lock, whose read and write locks no thread holds.
///
/// ## Usage Examples
/// ```java
/// class MyReadWriteLockTest extends AbstractTest implements ReadWriteLockContract<MyReadWriteLock> {
///     @Override
///     public ReadWriteLockProvider<MyReadWriteLock> readWriteLockProvider() {
///         return ReadWriteLockProviders.of(MyReadWriteLock::new);
///     }
/// }
/// ```
///
/// @param <L> the type of the read-write lock
/// @since 0.1.7
/// @see ReadWriteLockProviders
@FunctionalInterface
public interface ReadWriteLockProvider<L extends ReadWriteLock> {

    /// Returns a new read-write lock, whose read and write locks no thread holds.
    ///
    /// @return the lock
    L newLock();
}
