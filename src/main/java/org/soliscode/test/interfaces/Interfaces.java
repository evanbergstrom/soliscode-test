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

import java.util.Collection;

/// Utility class that helps create interface-narrowing views on objects.
///
/// @author evanbergstrom
/// @since 1.0
public final class Interfaces {

    private Interfaces() {}

    /// Wraps the object so that only the methods in the [Object] interface are available.
    /// @param obj the object to wrap.
    /// @return the wrapped object.
    public static ObjectOnly narrowToObject(final @NotNull Object obj) {
        return new ObjectOnly(obj);
    }

    /// Wraps the object so that only the methods in the [Number] interface are available.
    /// @param n the number to wrap.
    /// @return the wrapped number.
    public static NumberOnly narrowToNumber(final Number n) {
        return new NumberOnly(n);
    }

    /// Wraps the object so that only the methods in the [Iterable] interface are available.
    /// @param <E> the element type for the iterable.
    /// @param i the iterable to wrap
    /// @return the wrapped iterable.
    public static <E> IterableOnly<E> narrowToIterable(final Iterable<E> i) {
        return new IterableOnly<>(i);
    }

    /// Wraps the object so that only the methods in the [Collection] interface are available.
    /// @param <E> the element type for the iterable.
    /// @param i the iterable to wrap
    /// @return the wrapped iterable.
    public static <E> CollectionOnly<E> narrowToCollection(final Collection<E> i) {
        return new CollectionOnly<>(i);
    }
}
