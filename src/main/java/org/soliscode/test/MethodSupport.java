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

import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/// A utility class that provides an implementation for the [SupportedMethods] interface.
/// This class manages the optional method support status for testing classes, allowing
/// specific methods to be marked as unsupported to test error handling and interface compliance.
///
/// By default, all methods are considered supported unless explicitly marked otherwise using
/// {@link #doesNotSupportMethod(InterfaceMethod)}.
///
/// @author evanbergstrom
/// @since 1.0
/// @see SupportedMethods
/// @see InterfaceMethod
public abstract class MethodSupport implements SupportedMethods {

    /// The default status for methods when not explicitly configured - indicates support is enabled.
    private static final MethodStatus DEFAULT_STATUS = new MethodStatus(true);

    /// Map storing the support status for each optional method that has been explicitly configured.
    private final @NonNull Map<InterfaceMethod, MethodStatus> methodStatuses;

    /// Default constructor that initializes an empty method status map.
    /// All methods will be considered supported by default.
    protected MethodSupport() {
        this.methodStatuses = new HashMap<>();
    }

    /// Copy constructor that creates a new instance with the same method support configuration.
    /// @param other the MethodSupport instance to copy configuration from
    /// @throws NullPointerException if other is null
    protected MethodSupport(final @NonNull MethodSupport other) {
        this.methodStatuses = new HashMap<>(other.methodStatuses);
    }

    /// Field constructor that creates a new instance with the provided method support configuration.
    /// @param methodStatuses the method status configuration.
    protected MethodSupport(final @NonNull Map<InterfaceMethod, MethodStatus> methodStatuses) {
        this.methodStatuses = new HashMap<>(Objects.requireNonNull(methodStatuses));
    }

    /// Checks whether the specified optional method is supported by this implementation.
    /// Returns true if the method has not been explicitly marked as unsupported.
    ///
    /// @param method the optional method to check for support
    /// @return true if the method is supported, false if it has been marked as unsupported
    /// @throws NullPointerException if method is null
    /// @see #doesNotSupportMethod(InterfaceMethod)
    @Override
    public boolean supportsMethod(final @NonNull InterfaceMethod method) {
        return methodStatuses.getOrDefault(method, DEFAULT_STATUS).supported();
    }

    /// Marks the specified optional method as unsupported by this implementation.
    /// Once a method is marked as unsupported, {@link #supportsMethod(InterfaceMethod)}
    /// will return false for that method, and test classes can use this information
    /// to verify proper error handling when unsupported methods are called.
    ///
    /// @param method the optional method to mark as unsupported
    /// @throws NullPointerException if method is null
    /// @see #supportsMethod(InterfaceMethod)
    /// @see #unsupportedMethods()
    public void doesNotSupportMethod(final @NonNull InterfaceMethod method) {
        methodStatuses.put(method, new MethodStatus(false));
    }

    /// Checks whether all optional methods are supported by this implementation.
    ///
    /// This method verifies that there are no methods explicitly marked as unsupported.
    /// If no methods have been configured, it returns true, as all methods are
    /// supported by default.
    ///
    /// @return true if all optional methods are supported, false otherwise
    public boolean supportsAllMethods() {
        if (methodStatuses.isEmpty()) {
            return true;
        }
        return methodStatuses.values().stream().allMatch(MethodStatus::supported);
    }

    /// Returns a collection of the methods that are not supported by the class being tested.
    /// This method filters the configured method statuses to return only those methods
    /// that have been explicitly marked as unsupported.
    ///
    /// @return an immutable collection of unsupported optional methods; empty if no methods are unsupported
    /// @see #doesNotSupportMethod(InterfaceMethod)
    /// @see #supportsMethod(InterfaceMethod)
    protected @NonNull Collection<InterfaceMethod> unsupportedMethods() {
        return methodStatuses.entrySet().stream()
                .filter((e) -> !e.getValue().supported())
                .map(Map.Entry::getKey)
                .toList();
    }

    /// Returns an unmodifiable map of the explicitly configured method statuses.
    ///
    /// This map contains only those methods that have been explicitly marked as either
    /// supported or unsupported. Methods not present in this map are considered
    /// supported by default.
    ///
    /// @return an unmodifiable map of method status configurations
    protected @NonNull Map<InterfaceMethod, MethodStatus> methodStatuses() {
        return Collections.unmodifiableMap(methodStatuses);
    }

}
