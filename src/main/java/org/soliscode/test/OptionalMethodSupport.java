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

import java.util.*;

/// A utility class that provides an implementation for the [SupportedMethods] interface.
/// @author evanbergstrom
/// @since 1.0
public abstract class OptionalMethodSupport implements SupportedMethods {

    private static final MethodStatus DEFAULT_STATUS = new MethodStatus(true);
    private final @NotNull HashMap<OptionalMethod, MethodStatus> methodStatuses;

    protected OptionalMethodSupport() {
        this.methodStatuses = new HashMap<>();
    }

    protected OptionalMethodSupport(final @NotNull OptionalMethodSupport other) {
        this.methodStatuses = new HashMap<>(other.methodStatuses);
    }

    @Override
    public boolean supportsMethod(final @NotNull OptionalMethod method) {
        return methodStatuses.getOrDefault(method, DEFAULT_STATUS).supported();
    }

    /// Specifies that the class does not support the method passed as an argument.
    /// @param method the method that is not supported.
    public void doesNotSupportMethod(final @NotNull OptionalMethod method) {
        methodStatuses.put(method, new MethodStatus(false));
    }

    /// Returns a collection if the methods that are not supported by the  class being tested.
    /// @return a collection of unsupported methods.
    protected @NotNull Collection<OptionalMethod> unsupportedMethods() {
        return methodStatuses.entrySet().stream()
                .filter((e) -> !e.getValue().supported())
                .map(Map.Entry::getKey)
                .toList();
    }

    private record MethodStatus(boolean supported) {}
}
