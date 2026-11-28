/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.readwritelock;

import java.util.concurrent.locks.ReadWriteLock;

/// **Contract for the [ReadWriteLock] interface.**
///
/// A test class implements it with a [com.soliscode.test.provider.ReadWriteLockProvider] of new read-write locks of
/// the class under test; JUnit runs its test methods as the class's own. What `ReadWriteLock`'s Javadoc says holds
/// under contention is tested by [ThreadSafeReadWriteLockContract], which extends this.
///
/// It extends a method contract for each of `ReadWriteLock`'s two methods, [ReadLockContract] and
/// [WriteLockContract], which test the locks they return together: the read lock is shared, the write lock exclusive
/// of readers and writers. The write lock is a `Lock`, which [com.soliscode.test.contract.lock.LockContract] tests
/// on its own. Every call that may block runs with a time limit, and no test takes a lock twice from one thread, since
/// `ReadWriteLock` does not require reentrancy.
///
/// ## Usage Examples
/// ```java
/// class ReentrantReadWriteLockTest extends AbstractTest
///         implements ReadWriteLockContract<ReentrantReadWriteLock> {
///     @Override
///     public ReadWriteLockProvider<ReentrantReadWriteLock> readWriteLockProvider() {
///         return ReadWriteLockProviders.reentrantReadWriteLocks();
///     }
/// }
/// ```
///
/// @param <L> the type of the read-write lock under test
/// @since 0.1.7
/// @see ReadWriteLock
public interface ReadWriteLockContract<L extends ReadWriteLock> extends ReadLockContract<L>, WriteLockContract<L> {
}
