/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

/// Support for testing the implementation of the [java.lang.Object] interface methods.
///
/// This package provides contract interfaces that validate the proper implementation of Object methods:
/// - [EqualsMethodContract][com.soliscode.test.contract.object.EqualsMethodContract] - Tests equals() method according
///      to Object contract
/// - [HashCodeMethodContract][com.soliscode.test.contract.object.HashCodeMethodContract] - Tests hashCode() method
///      according to Object contract
/// - [ToStringMethodContract][com.soliscode.test.contract.object.ToStringMethodContract] - Tests toString() method for
///      basic functionality
/// - [ObjectContract][com.soliscode.test.contract.object.ObjectContract] - Combines all Object method contracts
///
/// The contracts test the fundamental Object methods that all Java classes inherit:
/// - `equals()` - Tests reflexivity, symmetry, transitivity, consistency, and null handling
/// - `hashCode()` - Tests consistency with equals() and general hash code properties
/// - `toString()` - Tests that the method returns a non-null String representation
///
/// These contracts do not test methods like `getClass()`, `clone()`, `notify()`, `notifyAll()`,
/// `wait()`, or `finalize()` as they are either final, native, or require special handling.
///
/// @author evanbergstrom
/// @since 1.0
@NullMarked
package com.soliscode.test.contract.object;

import org.jspecify.annotations.NullMarked;
