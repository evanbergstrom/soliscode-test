/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.closeable;

import java.io.BufferedReader;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.provider.AutoCloseableProvider;
import com.soliscode.test.provider.AutoCloseableProviders;

/// `CloseableContract` run by JUnit on a `BufferedReader`, as a user's test class runs it: its five test methods,
/// the idempotence test once, as `CloseableCloseContract`'s override.
@DisplayName("CloseableContract on BufferedReader")
public class BufferedReaderCloseableTest extends AbstractTest implements CloseableContract<BufferedReader> {

    /// Default constructor.
    public BufferedReaderCloseableTest() {
        super();
    }

    @Override
    public AutoCloseableProvider<BufferedReader> autoCloseableProvider() {
        return AutoCloseableProviders.bufferedReader();
    }
}
