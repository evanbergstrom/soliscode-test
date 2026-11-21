/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.support;

import com.soliscode.test.provider.ComparatorProvider;

/// Interface for contract classes that test a [java.util.Comparator], which a [ComparatorProvider] makes with its
/// samples.
///
/// @param <T> the type the comparator compares
/// @since 0.1.4
public interface ComparatorProviderSupport<T> {

    /// Returns the provider of the comparator under test and its ordered samples.
    ///
    /// @return the comparator provider
    ComparatorProvider<T> comparatorProvider();
}
