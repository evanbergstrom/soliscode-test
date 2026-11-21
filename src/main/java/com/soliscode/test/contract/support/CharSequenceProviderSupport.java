/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.support;

import com.soliscode.test.provider.CharSequenceProvider;

/// Interface for contract classes that test a [CharSequence], which a [CharSequenceProvider] makes of given contents.
///
/// @param <T> the class under test
/// @since 0.1.4
public interface CharSequenceProviderSupport<T extends CharSequence> {

    /// Returns the provider of the sequences under test and their contents.
    ///
    /// @return the char sequence provider
    CharSequenceProvider<T> charSequenceProvider();
}
