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

/// Utility class for creating instances of object providers for value classes.
/// @author evanbergstrom
/// @since 1.0
public final class Providers {

    private Providers() {}

    /// Creates a provider for the [Integer] class.
    /// @return a provider of [Integer] objects.
    public static @NotNull IntegerProvider integerProvider() {
        return new IntegerProvider();
    }

    /// Creates a provider for the [Long] class.
    /// @return a provider of [Long] objects.
    public static @NotNull LongProvider longProvider() {
        return new LongProvider();
    }

    /// Creates a provider for the [Short] class.
    /// @return a provider of [Short] objects.
    public static @NotNull ShortProvider shortProvider() {
        return new ShortProvider();
    }

    /// Creates a provider for the [Double] class.
    /// @return a provider of [Double] objects.
    public static DoubleProvider doubleProvider() {
        return new DoubleProvider();
    }

    /// Creates a provider for the [Float] class.
    /// @return a provider of [Float] objects.
    public static FloatProvider floatProvider() {
        return new FloatProvider();
    }

    /// Creates a provider for the [String] class.
    /// @return a provider of [String] objects.
    public static StringProvider stringProvider() {
        return new StringProvider();
    }
}
