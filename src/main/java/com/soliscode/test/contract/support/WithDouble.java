/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.support;

import com.soliscode.test.provider.DoubleProvider;
import com.soliscode.test.provider.ObjectProvider;

/// Provides an implementation of the [#provider()] method that provides instances of [Double]. This is a mix-in class
/// that is meant ot be used alongside a Contract class.
///
/// ```java
/// ```
///
/// @author evanbergstrom
/// @see com.soliscode.test.contract.object.ObjectContract
/// @see DoubleProvider
/// @since 1.0
public interface WithDouble extends ContractSupport<Double> {

    /// Returns an instance of `ObjectProvider` that provides [Double] objects.
    /// @return an instance of [DoubleProvider].
    @Override
    default ObjectProvider<Double> provider() {
        return new DoubleProvider();
    }
}
