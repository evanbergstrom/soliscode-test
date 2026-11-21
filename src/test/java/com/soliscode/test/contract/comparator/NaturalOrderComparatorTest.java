/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.comparator;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.provider.ComparableProviders;
import com.soliscode.test.provider.ComparatorProvider;
import com.soliscode.test.provider.ComparatorProviders;

/// `ComparatorContract` run by JUnit on `Comparator.naturalOrder()` over `Integer`s, as a user's test class runs it.
@DisplayName("ComparatorContract on Comparator.naturalOrder()")
public class NaturalOrderComparatorTest extends AbstractTest implements ComparatorContract<Integer> {

    /// Default constructor.
    public NaturalOrderComparatorTest() {
        super();
    }

    @Override
    public ComparatorProvider<Integer> comparatorProvider() {
        return ComparatorProviders.naturalOrder(ComparableProviders.integers());
    }
}
