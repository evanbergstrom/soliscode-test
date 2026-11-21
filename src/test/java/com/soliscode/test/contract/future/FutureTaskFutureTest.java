/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.future;

import java.util.concurrent.FutureTask;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.provider.FutureProvider;
import com.soliscode.test.provider.FutureProviders;

/// `ThreadSafeFutureContract` run by JUnit on `FutureTask`, as a user's test class runs it.
@DisplayName("ThreadSafeFutureContract on FutureTask")
public class FutureTaskFutureTest extends AbstractTest
        implements ThreadSafeFutureContract<FutureTask<Integer>, Integer> {

    /// Default constructor.
    public FutureTaskFutureTest() {
        super();
    }

    @Override
    public FutureProvider<FutureTask<Integer>, Integer> futureProvider() {
        return FutureProviders.futureTasks();
    }
}
