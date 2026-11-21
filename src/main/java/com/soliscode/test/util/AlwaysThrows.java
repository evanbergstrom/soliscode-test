/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.util;

import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/// Class that implements the [Object] and [Comparable] interfaces, and that throws an exception when any method is
/// called.
///
/// @author evanbergstrom
public class AlwaysThrows implements Comparable<AlwaysThrows> {

    private final Supplier<RuntimeException> exceptionSupplier;

    /// Creates an instance that always throws an [UnsupportedOperationException] for every method call.
    public AlwaysThrows() {
        exceptionSupplier = UnsupportedOperationException::new;
    }

    /// Creates an instance that always throws an exception that is provided by the supplier parameter.
    /// @param exceptionSupplierArg The supplied used to generate exceptions.
    public AlwaysThrows(final Supplier<RuntimeException> exceptionSupplierArg) {
        this.exceptionSupplier = exceptionSupplierArg;
    }

    /// Always throws the supplied exception
    /// @param obj The object to compare for equality (not used).
    /// @return Never returns a value
    @Override
    public boolean equals(final @Nullable Object obj) {
        throw exceptionSupplier.get();
    }

    /// Always throws the supplied exception
    /// @return Never returns a value
    @Override
    public int hashCode() {
        throw exceptionSupplier.get();
    }

    /// Always throws the supplied exception
    /// @param obj The object to compare (not used).
    /// @return Never returns a value
    @Override
    public int compareTo(final AlwaysThrows obj) {
        throw exceptionSupplier.get();
    }

    /// Always throws the supplied exception
    /// @return Never returns a value
    @Override
    public String toString() {
        throw exceptionSupplier.get();
    }
}
