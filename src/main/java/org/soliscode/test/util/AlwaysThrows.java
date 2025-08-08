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

package org.soliscode.test.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/// Class that implements the [Object] and [Comparable] interfaces, and that throws an exception when any method is
/// called.
///
/// @author evanbergstrom
/// @see 1.0
public class AlwaysThrows implements Comparable<AlwaysThrows> {

    private final Supplier<RuntimeException> exceptionSupplier;

    /// Creates an instance that always throws an [UnsupportedOperationException] for every method call.
    public AlwaysThrows() {
        exceptionSupplier = UnsupportedOperationException::new;
    }

    /// Creates an instance that always throws an exception that is provided by the supplier parameter.
    /// @param exceptionSupplier The supplied used to generate exceptions.
    public AlwaysThrows(final Supplier<RuntimeException> exceptionSupplier) {
        this.exceptionSupplier = exceptionSupplier;
    }

    /// Always throws the supplied exception
    /// @param obj The object to compare for equality (not used).
    /// @return Never returns a value
    @Override
    public boolean equals(final Object obj) {
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
    public int compareTo(@NotNull final AlwaysThrows obj) {
        throw exceptionSupplier.get();
    }

    /// Always throws the supplied exception
    /// @return Never returns a value
    @Override
    public String toString() {
        throw exceptionSupplier.get();
    }
}
