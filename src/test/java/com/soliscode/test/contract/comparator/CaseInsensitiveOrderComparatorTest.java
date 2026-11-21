/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.comparator;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.provider.ComparatorProvider;
import com.soliscode.test.provider.ComparatorProviders;

/// `ComparatorContract` run by JUnit on `String.CASE_INSENSITIVE_ORDER`, which is not consistent with `equals`, as a
/// user's test class runs it.
@DisplayName("ComparatorContract on String.CASE_INSENSITIVE_ORDER")
public class CaseInsensitiveOrderComparatorTest extends AbstractTest implements ComparatorContract<String> {

    /// Default constructor.
    public CaseInsensitiveOrderComparatorTest() {
        super();
    }

    @Override
    public ComparatorProvider<String> comparatorProvider() {
        return ComparatorProviders.caseInsensitiveOrder();
    }

    @Override
    public boolean comparatorIsConsistentWithEquals() {
        return false;
    }
}
