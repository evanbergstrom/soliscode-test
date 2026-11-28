/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.lock;

import java.util.concurrent.locks.Lock;

/// **Contract for the thread safety of a `Lock`**
///
/// It extends [LockContract] with the test of what [Lock]'s Javadoc says holds under contention: threads that acquire
/// the lock at once hold it one at a time ([ThreadSafeLockingContract]). The test shows exclusion only, not the memory
/// effects of a monitor that the Javadoc also requires: see [ThreadSafeLockingContract].
///
/// The test is tagged [com.soliscode.test.annotations.ConcurrencyTest] and [com.soliscode.test.annotations.VerySlow],
/// and runs on a [com.soliscode.test.safety.ContentionCoordinator], with a deadline of its own that bounds it in time.
///
/// ## Usage Examples
/// ```java
/// class ReentrantLockSafetyTest extends AbstractTest implements ThreadSafeLockContract<ReentrantLock> {
///     @Override
///     public LockProvider<ReentrantLock> lockProvider() {
///         return LockProviders.reentrantLocks();
///     }
/// }
/// ```
///
/// @param <L> the type of the lock under test
/// @see Lock
/// @since 0.1.7
public interface ThreadSafeLockContract<L extends Lock> extends LockContract<L>, ThreadSafeLockingContract<L> {
}
