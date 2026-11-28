/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.readwritelock;

import java.util.concurrent.locks.ReadWriteLock;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.provider.ReadWriteLockProvider;
import com.soliscode.test.provider.ReadWriteLockProviders;

/// `ThreadSafeReadWriteLockContract` run by JUnit on `StampedLock.asReadWriteLock()`, as a user's test class runs it.
@DisplayName("ThreadSafeReadWriteLockContract on StampedLock.asReadWriteLock()")
public class StampedReadWriteLockReadWriteLockTest extends AbstractTest
        implements ThreadSafeReadWriteLockContract<ReadWriteLock> {

    /// Default constructor.
    public StampedReadWriteLockReadWriteLockTest() {
        super();
    }

    @Override
    public ReadWriteLockProvider<ReadWriteLock> readWriteLockProvider() {
        return ReadWriteLockProviders.stampedReadWriteLocks();
    }
}
