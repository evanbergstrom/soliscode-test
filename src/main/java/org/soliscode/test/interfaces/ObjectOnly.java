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

package org.soliscode.test.interfaces;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/// The `ObjectOnly` class wraps any object and only exposes the methods on the [Object] interface. This is used for
/// testing a class or method implementation that operates on an `Object` without any optimizations for the
/// instantiated class or any other implemented interfaces.
///
/// Objects of this class should be created using the [Interfaces] utility class:
/// ```java
///     String s = "a string value";
///     Object obj = Interfaces.objectOnly(s);
/// ```
///
/// @author evanbergstrom
/// @see Interfaces
/// @since 1.0
public class ObjectOnly {

    private final Object obj;

    /// Creates an instance of ObjectOnly. The instance created will wrap the object Integer(0).
    public ObjectOnly() {
        this.obj = 0;
    }

    // Copies and instance of ObjectOnly.
    public ObjectOnly(final @NotNull ObjectOnly other) {
        this.obj = Objects.requireNonNull(other).obj;
    }

    /// Creates an `ObjectOnly` object from any other object.
    /// @param obj the object whose interface is being restricted.
    public ObjectOnly(final @NotNull Object obj) {
        this.obj = Objects.requireNonNull(obj);
    }

    /// {@inheritDoc}
    @Override
    public int hashCode() {
        return obj.hashCode();
    }

    /// {@inheritDoc}
    @Override
    public boolean equals(Object o) {
        if (o instanceof ObjectOnly only) {
            return obj.equals(only.obj);
        } else {
            return false;
        }
    }

    /// {@inheritDoc}
    @Override
    public String toString() {
        return obj.toString();
    }
}
