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

package org.soliscode.test.provider;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/// Provides instances of an [String] for the purposes of testing.
///
/// @author evanbergstrom
/// @since 1.0
/// @see String
public class StringProvider implements ObjectProvider<String> {

    @Override
    public @NotNull String defaultInstance() {
        return "";
    }

    /// Creates an instance of the string class with a value equal to the string representation of a primitive integer
    /// value.
    ///
    /// @param seed a primitive integer value.
    /// @return an instance of the `String` class
    @Override
    public @NotNull String createInstance(int seed) {
        return String.valueOf(seed);
    }

    /// Creates a copy of the string value. Since instances of [String] are immutable, the copy is not performed and
    /// the value provided as an argument is returned.
    /// @param other the string value to copy.
    /// @return the string value.
    /// @throws NullPointerException if the argument is `null`.
    @Override
    public @NotNull String copyInstance(final @NotNull String other) {
        return Objects.requireNonNull(other);
    }
}
