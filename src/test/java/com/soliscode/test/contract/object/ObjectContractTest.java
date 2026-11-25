/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.object;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.provider.FunctionalProvider;
import com.soliscode.test.provider.ObjectProvider;

/// **Tests for the `ObjectContract` test suite**
/// This class provides tests to ensure that the [ObjectContract] itself works as expected.
/// It uses various implementations to verify that the contract tests correctly identify
/// both compliant and non-compliant behaviors.
/// ## Test Scope
/// The tests in this class cover:
/// - Basic [Object] contract compliance for standard classes (e.g., [String]).
/// - Verification of [EqualsMethodContract], [HashCodeMethodContract], and [ToStringMethodContract].
///
/// @author evanbergstrom
/// @see ObjectContract
/// @since 1.0.0
@DisplayName("Tests for the ObjectContract test")
public class ObjectContractTest {

    /// Default constructor.
    public ObjectContractTest() {
        super();
    }

    /// **Test `ObjectContract` with the `String` class**
    /// This nested class verifies that [ObjectContract] correctly identifies [String]
    /// as a class that complies with the standard [Object] contract.
    @Nested
    @DisplayName("Test ObjectContract with String class")
    public class TestWithStringClass extends AbstractTest implements ObjectContract<String>, WithString {

        /// Default constructor.
        public TestWithStringClass() {
            super();
        }
    }

    /// A class with value equality, for [MyClassTest], which is [ObjectContract]'s example.
    public static final class MyClass {
        private final long value;

        /// Creates the default instance.
        public MyClass() {
            this(0);
        }

        /// Creates an instance from a seed.
        ///
        /// @param valueArg the value
        public MyClass(final long valueArg) {
            this.value = valueArg;
        }

        /// Copies an instance.
        ///
        /// @param other the instance to copy
        public MyClass(final MyClass other) {
            this(other.value);
        }

        @Override
        public boolean equals(final @Nullable Object o) {
            return o instanceof MyClass other && other.value == value;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(value);
        }

        @Override
        public String toString() {
            return "MyClass[" + value + "]";
        }
    }

    /// **[ObjectContract]'s example, as its Javadoc gives it**, so that the example compiles and passes (#70).
    @Nested
    @DisplayName("ObjectContract's Javadoc example")
    public class MyClassTest extends AbstractTest implements ObjectContract<MyClass> {

        /// Default constructor.
        public MyClassTest() {
            super();
        }

        @Override
        public ObjectProvider<MyClass> provider() {
            return new FunctionalProvider<>(MyClass::new, MyClass::new, MyClass::new);
        }
    }
}
