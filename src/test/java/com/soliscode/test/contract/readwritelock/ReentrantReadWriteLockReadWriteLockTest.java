/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.readwritelock;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.provider.ReadWriteLockProvider;
import com.soliscode.test.provider.ReadWriteLockProviders;

/// `ThreadSafeReadWriteLockContract` run by JUnit on `ReentrantReadWriteLock`, as a user's test class runs it.
@DisplayName("ThreadSafeReadWriteLockContract on ReentrantReadWriteLock")
public class ReentrantReadWriteLockReadWriteLockTest extends AbstractTest
        implements ThreadSafeReadWriteLockContract<ReentrantReadWriteLock> {

    /// Default constructor.
    public ReentrantReadWriteLockReadWriteLockTest() {
        super();
    }

    @Override
    public ReadWriteLockProvider<ReentrantReadWriteLock> readWriteLockProvider() {
        return ReadWriteLockProviders.reentrantReadWriteLocks();
    }
}
