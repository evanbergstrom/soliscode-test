/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.future;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.provider.FutureProvider;
import com.soliscode.test.provider.FutureProviders;

/// `ThreadSafeFutureContract` run by JUnit on `CompletableFuture`, as a user's test class runs it.
@DisplayName("ThreadSafeFutureContract on CompletableFuture")
public class CompletableFutureFutureTest extends AbstractTest
        implements ThreadSafeFutureContract<CompletableFuture<Integer>, Integer> {

    /// Default constructor.
    public CompletableFutureFutureTest() {
        super();
    }

    @Override
    public FutureProvider<CompletableFuture<Integer>, Integer> futureProvider() {
        return FutureProviders.completableFutures();
    }
}
