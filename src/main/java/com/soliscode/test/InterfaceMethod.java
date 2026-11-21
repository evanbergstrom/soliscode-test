/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test;


/// Interface class for enumerations that represent optional methods classes used while testing the test contracts.
/// These enumerations indicate methods that are not implemented by the class being tested.
///
/// @author Evan Bergstrom
/// @since 1.0
public interface InterfaceMethod {

    /// The name of the method.
    ///
    /// @return the name of the method.
    String methodName();
}
