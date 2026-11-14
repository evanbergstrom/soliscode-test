/*
 * Copyright 2026 Evan Bergstrom
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
package com.soliscode.test.provider;

import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.function.ThrowingConsumer;

/// A use of a resource that its class documents, and what it does once the resource is closed: throw an exception of a
/// given type, or work as before.
///
/// [com.soliscode.test.contract.autocloseable.AutoCloseableContract] calls each use on an open resource, where it must
/// complete, and on a closed one, where it must do what its class says.
///
/// ## Usage Examples
/// ```java
/// ResourceUse<BufferedReader> read = ResourceUse.throwsOnceClosed("read()", IOException.class, r -> r.read());
/// ResourceUse<ByteArrayOutputStream> size = ResourceUse.worksOnceClosed("size()", s -> s.size());
/// ```
///
/// @param name             the use's name, for failure messages
/// @param action           the use: it calls a method of the resource
/// @param thrownOnceClosed the type of exception the use throws once the resource is closed, or `null` if it works
///                         once closed
/// @param <C>              the type of the resource
/// @since 0.1.3
/// @see AutoCloseableProvider
public record ResourceUse<C>(String name, ThrowingConsumer<? super C> action,
                             @Nullable Class<? extends Throwable> thrownOnceClosed) {

    /// Creates a use.
    ///
    /// @param name             the use's name, for failure messages
    /// @param action           the use
    /// @param thrownOnceClosed the type of exception the use throws once the resource is closed, or `null` if it
    ///                         works once closed
    /// @throws NullPointerException if `name` or `action` is null
    public ResourceUse {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(action, "action");
    }

    /// A use that throws an exception of the given type, or a subtype, once the resource is closed.
    ///
    /// @param name   the use's name
    /// @param type   the type of exception it throws once closed
    /// @param action the use
    /// @param <C>    the type of the resource
    /// @return the use
    /// @throws NullPointerException if an argument is null
    public static <C> ResourceUse<C> throwsOnceClosed(final String name, final Class<? extends Throwable> type,
                                                      final ThrowingConsumer<? super C> action) {
        return new ResourceUse<>(name, action, Objects.requireNonNull(type, "type"));
    }

    /// A use that works once the resource is closed, as it does while it is open.
    ///
    /// @param name   the use's name
    /// @param action the use
    /// @param <C>    the type of the resource
    /// @return the use
    /// @throws NullPointerException if an argument is null
    public static <C> ResourceUse<C> worksOnceClosed(final String name, final ThrowingConsumer<? super C> action) {
        return new ResourceUse<>(name, action, null);
    }

    /// Returns the use's name, and what it does once closed.
    ///
    /// @return the description
    @Override
    public String toString() {
        Class<? extends Throwable> thrown = thrownOnceClosed;
        return name + (thrown == null ? " (works once closed)" : " (throws " + thrown.getSimpleName()
                + " once closed)");
    }
}
