/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.support;

import java.util.concurrent.locks.ReadWriteLock;

import org.opentest4j.AssertionFailedError;

/// The base interface of the [ReadWriteLock] contracts: the provider's locks, checked, and the helpers of
/// [LockingSupport].
///
/// @param <L> the type of the read-write lock under test
/// @see com.soliscode.test.contract.readwritelock.ReadWriteLockContract
/// @since 0.1.7
public interface ReadWriteLockContractSupport<L extends ReadWriteLock>
        extends LockingSupport, ReadWriteLockProviderSupport<L> {

    /// Returns a new read-write lock from the provider, checked: a null one, or one whose read or write lock is null,
    /// fails the test with a message that says which is at fault.
    ///
    /// @return the lock
    default L newReadWriteLock() {
        L lock = readWriteLockProvider().newLock();
        if (lock == null) {
            throw new AssertionFailedError("the read-write lock provider is at fault: it gave a null lock");
        }
        if (lock.readLock() == null || lock.writeLock() == null) {
            throw new AssertionFailedError("readLock() or writeLock() returned null");
        }
        return lock;
    }
}
