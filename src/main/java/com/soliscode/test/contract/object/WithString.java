/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.object;

import com.soliscode.test.provider.ObjectProvider;
import com.soliscode.test.provider.StringProvider;

/// Provides an implementation of the [#provider()] method that provides instances of [String].
///
/// @author evanbergstrom
/// @see ObjectContract
/// @see StringProvider
/// @since 1.0
public interface WithString extends ObjectContract<String> {

    /// Returns an instance of `ObjectProvider` that provides [String] objects.
    /// @return an instance of [StringProvider].
    @Override
    default ObjectProvider<String> provider() {
        return new StringProvider();
    }
}

