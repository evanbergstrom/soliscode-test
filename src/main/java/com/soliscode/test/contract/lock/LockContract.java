/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.lock;

import java.util.concurrent.locks.Lock;

/// **Contract for the [Lock] interface.**
///
/// A test class implements it with a [com.soliscode.test.provider.LockProvider] of new locks of the class under
/// test; JUnit runs its test methods as the class's own. A class whose `newCondition()` throws
/// [UnsupportedOperationException] overrides
/// [com.soliscode.test.contract.support.LockContractSupport#supportsConditions()] to return `false`, and one whose
/// acquisition cannot be interrupted while it waits overrides
/// [com.soliscode.test.contract.support.LockContractSupport#interruptsLockAcquisition()]. What `Lock`'s Javadoc says
/// of locking under contention is tested by [ThreadSafeLockContract], which extends this.
///
/// It extends a method contract for each of `Lock`'s six methods: [LockMethodContract] (`lock()`),
/// [LockInterruptiblyContract], [TryLockContract], [TimedTryLockContract], [UnlockContract] and
/// [NewConditionContract]; each says how it reads the Javadoc. Every call that may block runs with a time limit, so
/// that a faulty lock fails a test rather than hanging it.
///
/// It reads `Lock` strictly in one way: `Lock`'s Javadoc allows a lock that does not exclude ("some locks may allow
/// concurrent access to a shared resource, such as the read lock of a ReadWriteLock"); this contract tests an
/// exclusive lock, so a read lock is tested by [com.soliscode.test.contract.readwritelock.ReadWriteLockContract]
/// instead. It does not require reentrancy (`StampedLock`'s views are not reentrant): no test takes a lock twice from
/// one thread. The write lock of a read-write lock is a `Lock` it can test, with
/// [com.soliscode.test.provider.LockProviders#writeLocksOf].
///
/// ## Usage Examples
/// ```java
/// class ReentrantLockTest extends AbstractTest implements LockContract<ReentrantLock> {
///     @Override
///     public LockProvider<ReentrantLock> lockProvider() {
///         return LockProviders.reentrantLocks();
///     }
/// }
/// ```
///
/// @param <L> the type of the lock under test
/// @since 0.1.7
/// @see Lock
public interface LockContract<L extends Lock> extends LockMethodContract<L>, LockInterruptiblyContract<L>,
        TryLockContract<L>, TimedTryLockContract<L>, UnlockContract<L>, NewConditionContract<L> {
}
