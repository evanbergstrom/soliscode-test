/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.methods;

import com.soliscode.test.InterfaceMethod;

/// Values used to identify object class methods for use with the `supportsMethod(InterfaceMethod)` method.
///
/// @author evanbergstrom
/// @since 1.0.0
public enum ObjectMethods implements InterfaceMethod {

    /// The optional method [Object#equals(Object)].
    EQUALS("equals(Object)"),

    /// The optional method [Object#hashCode()].
    HASH_CODE("hashCode()"),

    /// The optional method [Object#toString()].
    TO_STRING("toString()"),

    /// The optional method [java.io.Serializable].
    SERIALIZATION("serialization");

    private final String name;

    ObjectMethods(final String nameArg) {
        this.name = nameArg;
    }

    /// {@inheritDoc}
    @Override
    public String methodName() {
        return name;
    }
}
