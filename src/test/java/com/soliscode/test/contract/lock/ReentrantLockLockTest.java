/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.lock;

import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.provider.LockProvider;
import com.soliscode.test.provider.LockProviders;

/// `ThreadSafeLockContract` run by JUnit on `ReentrantLock`, as a user's test class runs it.
@DisplayName("ThreadSafeLockContract on ReentrantLock")
public class ReentrantLockLockTest extends AbstractTest implements ThreadSafeLockContract<ReentrantLock> {

    /// Default constructor.
    public ReentrantLockLockTest() {
        super();
    }

    @Override
    public LockProvider<ReentrantLock> lockProvider() {
        return LockProviders.reentrantLocks();
    }
}
