/*
 * Copyright 2024 Evan Bergstrom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/// Support for testing the implementation of the [java.lang.Object] interface methods.
///
/// This package provides contract interfaces that validate the proper implementation of Object methods:
/// - [EqualsMethodContract][org.soliscode.test.contract.object.EqualsMethodContract] - Tests equals() method according
///      to Object contract
/// - [HashCodeMethodContract][org.soliscode.test.contract.object.HashCodeMethodContract] - Tests hashCode() method
///      according to Object contract
/// - [ToStringMethodContract][org.soliscode.test.contract.object.ToStringMethodContract] - Tests toString() method for
///      basic functionality
/// - [ObjectContract][org.soliscode.test.contract.object.ObjectContract] - Combines all Object method contracts
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
package org.soliscode.test.contract.object;
