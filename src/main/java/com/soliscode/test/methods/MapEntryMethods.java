/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.methods;

import com.soliscode.test.InterfaceMethod;

/// The optional methods of [java.util.Map.Entry], for `supportsMethod(InterfaceMethod)`.
///
/// @since 0.1.3
public enum MapEntryMethods implements InterfaceMethod {

    /// The optional method [java.util.Map.Entry#setValue(Object)].
    MAP_ENTRY_SET_VALUE("setValue(V)");

    private final String name;

    MapEntryMethods(final String nameArg) {
        this.name = nameArg;
    }

    /// {@inheritDoc}
    @Override
    public String methodName() {
        return name;
    }
}
