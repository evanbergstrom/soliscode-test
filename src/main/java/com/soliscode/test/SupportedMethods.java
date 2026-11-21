/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test;

import java.util.Arrays;

/// An interface used to indicate which of the optional methods of another interface that this class supports.
///
/// @author evanbergstrom
/// @since 1.0.0
public interface SupportedMethods {

    /// Used to determine if this object supports an optional method.
    /// @param method The identifier for the method.
    /// @return 'true' if the method is supported. `false` if it is not.
    boolean supportsMethod(InterfaceMethod method);

    /// Used to determine if this object supports every one of some optional methods.
    /// @param methods The identifiers for the methods.
    /// @return `true` if each method is supported, `false` if any is not.
    /// @since 0.1.5
    default boolean supportsMethods(final InterfaceMethod... methods) {
        return Arrays.stream(methods).allMatch(this::supportsMethod);
    }
}
