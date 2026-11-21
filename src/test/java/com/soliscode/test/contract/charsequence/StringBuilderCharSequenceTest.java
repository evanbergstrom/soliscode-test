/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.charsequence;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.provider.CharSequenceProvider;
import com.soliscode.test.provider.CharSequenceProviders;

/// `CharSequenceContract` run by JUnit on `StringBuilder`, as a user's test class runs it.
@DisplayName("CharSequenceContract on StringBuilder")
public class StringBuilderCharSequenceTest extends AbstractTest implements CharSequenceContract<StringBuilder> {

    /// Default constructor.
    public StringBuilderCharSequenceTest() {
        super();
    }

    @Override
    public CharSequenceProvider<StringBuilder> charSequenceProvider() {
        return CharSequenceProviders.stringBuilders();
    }
}
