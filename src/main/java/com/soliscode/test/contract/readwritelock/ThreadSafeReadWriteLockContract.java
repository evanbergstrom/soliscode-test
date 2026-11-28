/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.readwritelock;

import java.util.concurrent.locks.ReadWriteLock;

/// **Contract for the thread safety of a `ReadWriteLock`**
///
/// It extends [ReadWriteLockContract] with the test of what [ReadWriteLock]'s Javadoc says holds under contention:
/// writers exclude each other and readers ([ThreadSafeReadWriteContract]). The test shows exclusion only, not that a
/// reader sees what the last writer wrote, which the Javadoc also says: see [ThreadSafeReadWriteContract].
///
/// The test is tagged [com.soliscode.test.annotations.ConcurrencyTest] and [com.soliscode.test.annotations.VerySlow],
/// and runs on a [com.soliscode.test.safety.ContentionCoordinator], with a deadline of its own that bounds it in time.
///
/// ## Usage Examples
/// ```java
/// class ReentrantReadWriteLockSafetyTest extends AbstractTest
///         implements ThreadSafeReadWriteLockContract<ReentrantReadWriteLock> {
///     @Override
///     public ReadWriteLockProvider<ReentrantReadWriteLock> readWriteLockProvider() {
///         return ReadWriteLockProviders.reentrantReadWriteLocks();
///     }
/// }
/// ```
///
/// @param <L> the type of the read-write lock under test
/// @see ReadWriteLock
/// @since 0.1.7
public interface ThreadSafeReadWriteLockContract<L extends ReadWriteLock> extends ReadWriteLockContract<L>,
        ThreadSafeReadWriteContract<L> {
}
