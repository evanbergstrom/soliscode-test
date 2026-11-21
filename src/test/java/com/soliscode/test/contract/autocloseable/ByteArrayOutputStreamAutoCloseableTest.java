/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.autocloseable;

import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.provider.AutoCloseableProvider;
import com.soliscode.test.provider.AutoCloseableProviders;

/// `AutoCloseableContract` run by JUnit on a `ByteArrayOutputStream`, as a user's test class runs it.
@DisplayName("AutoCloseableContract on ByteArrayOutputStream")
public class ByteArrayOutputStreamAutoCloseableTest extends AbstractTest
        implements AutoCloseableContract<ByteArrayOutputStream> {

    /// Default constructor.
    public ByteArrayOutputStreamAutoCloseableTest() {
        super();
    }

    @Override
    public AutoCloseableProvider<ByteArrayOutputStream> autoCloseableProvider() {
        return AutoCloseableProviders.byteArrayOutputStream();
    }
}
