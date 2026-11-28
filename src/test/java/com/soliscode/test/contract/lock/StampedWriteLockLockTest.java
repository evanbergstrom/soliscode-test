/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.lock;

import java.util.concurrent.locks.Lock;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.provider.LockProvider;
import com.soliscode.test.provider.LockProviders;

/// `ThreadSafeLockContract` run by JUnit on `StampedLock.asWriteLock()`, as a user's test class runs it, saying that
/// it does not support conditions, as its `newCondition()` documents.
@DisplayName("ThreadSafeLockContract on StampedLock.asWriteLock(), without conditions")
public class StampedWriteLockLockTest extends AbstractTest implements ThreadSafeLockContract<Lock> {

    /// Default constructor.
    public StampedWriteLockLockTest() {
        super();
    }

    @Override
    public LockProvider<Lock> lockProvider() {
        return LockProviders.stampedWriteLocks();
    }

    @Override
    public boolean supportsConditions() {
        return false;
    }
}
