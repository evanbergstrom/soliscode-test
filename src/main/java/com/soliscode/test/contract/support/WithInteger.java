/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.support;

import com.soliscode.test.provider.IntegerNumberProvider;
import com.soliscode.test.provider.IntegerProvider;

/// Provides an implementation of the [#provider()] method that provides instances of [Integer].
///
/// @author evanbergstrom
/// @see com.soliscode.test.contract.object.ObjectContract
/// @see IntegerProvider
/// @since 1.0
public interface WithInteger extends IntegerSupport<Integer> {

    /// Returns an instance of `NumberProvider` that provides [Integer] objects.
    /// @return an instance of [IntegerProvider].
    @Override
    default IntegerNumberProvider<Integer> provider() {
        return new IntegerProvider();
    }
}
