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

package org.soliscode.test;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/// A utility class that provides an implementation for the [SupportedMethods] interface.
/// This class manages the optional method support status for testing classes, allowing
/// specific methods to be marked as unsupported to test error handling and interface compliance.
///
/// By default, all methods are considered supported unless explicitly marked otherwise using
/// {@link #doesNotSupportMethod(OptionalMethod)}.
///
/// @author evanbergstrom
/// @since 1.0
/// @see SupportedMethods
/// @see OptionalMethod
public abstract class OptionalMethodSupport implements SupportedMethods {

    /// The default status for methods when not explicitly configured - indicates support is enabled.
    private static final MethodStatus DEFAULT_STATUS = new MethodStatus(true);

    /// Map storing the support status for each optional method that has been explicitly configured.
    private final @NotNull HashMap<OptionalMethod, MethodStatus> methodStatuses;

    /// Default constructor that initializes an empty method status map.
    /// All methods will be considered supported by default.
    protected OptionalMethodSupport() {
        this.methodStatuses = new HashMap<>();
    }

    /// Copy constructor that creates a new instance with the same method support configuration.
    /// @param other the OptionalMethodSupport instance to copy configuration from
    /// @throws NullPointerException if other is null
    protected OptionalMethodSupport(final @NotNull OptionalMethodSupport other) {
        this.methodStatuses = new HashMap<>(other.methodStatuses);
    }

    /// Checks whether the specified optional method is supported by this implementation.
    /// Returns true if the method has not been explicitly marked as unsupported.
    ///
    /// @param method the optional method to check for support
    /// @return true if the method is supported, false if it has been marked as unsupported
    /// @throws NullPointerException if method is null
    /// @see #doesNotSupportMethod(OptionalMethod)
    @Override
    public boolean supportsMethod(final @NotNull OptionalMethod method) {
        return methodStatuses.getOrDefault(method, DEFAULT_STATUS).supported();
    }

    /// Marks the specified optional method as unsupported by this implementation.
    /// Once a method is marked as unsupported, {@link #supportsMethod(OptionalMethod)}
    /// will return false for that method, and test classes can use this information
    /// to verify proper error handling when unsupported methods are called.
    ///
    /// @param method the optional method to mark as unsupported
    /// @throws NullPointerException if method is null
    /// @see #supportsMethod(OptionalMethod)
    /// @see #unsupportedMethods()
    public void doesNotSupportMethod(final @NotNull OptionalMethod method) {
        methodStatuses.put(method, new MethodStatus(false));
    }

    /// Returns a collection of the methods that are not supported by the class being tested.
    /// This method filters the configured method statuses to return only those methods
    /// that have been explicitly marked as unsupported.
    ///
    /// @return an immutable collection of unsupported optional methods; empty if no methods are unsupported
    /// @see #doesNotSupportMethod(OptionalMethod)
    /// @see #supportsMethod(OptionalMethod)
    protected @NotNull Collection<OptionalMethod> unsupportedMethods() {
        return methodStatuses.entrySet().stream()
                .filter((e) -> !e.getValue().supported())
                .map(Map.Entry::getKey)
                .toList();
    }

    /// Internal record that represents the support status of an optional method.
    /// @param supported true if the method is supported, false if it is not supported
    private record MethodStatus(boolean supported) { }
}
