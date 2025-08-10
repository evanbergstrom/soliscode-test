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

import java.util.*;

/// An iterable that is provided for tests that only implement the [Iterable] interface. If the code that is
/// being tested tries to detect the kind of iterable to optimize the algorithm, it will be forced to use only the
/// methods on iterable.
///
/// @param <E> the element type.
/// @author evanbergstrom
/// @since 1.0
public class IterableOnly<E> implements Iterable<E> {

    protected Iterable<E> iterable;

    /// Creates an empty iterable.
    public IterableOnly() {
        this.iterable = new ArrayList<>();
    }

    /// Creates a copy of an iterable.
    /// @param other the instance of `IterableOnly` to copy.
    public IterableOnly(final Iterable<E> other) {
        final List<E> store = new ArrayList<>();
        other.forEach(store::add);
        this.iterable = store;
    }

    /// Creates an instance of `IterableOnly` from the collection provided.
    /// @param c The collection of elements.
    public IterableOnly(final Collection<E> c) {
        this.iterable = c;
    }

    /// {@inheritDoc}
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof IterableOnly<?> other) {
            return iterable.equals(other.iterable);
        } else {
            return false;
        }
    }

    /// {@inheritDoc}
    @Override
    public int hashCode() {
        return iterable.hashCode();
    }

    /// {@inheritDoc}
    @Override
    public String toString() {
        return iterable.toString();
    }

    /// {@inheritDoc}
    @Override
    @NotNull
    public Iterator<E> iterator() {
        return iterable.iterator();
    }

    /// {@inheritDoc}
    @Override
    public Spliterator<E> spliterator() {
        return iterable.spliterator();
    }

    /// Creates an iterable from an empty collection of elements.
    /// @param <E> the element type.
    /// @return an iterable on an empty collection of elements.
    public static <E> Iterable<E> of() {
        return new IterableOnly<>(List.of());
    }

    /// Creates an iterable from a list of one element.
    /// ```java
    ///    Iterable<Integer> iterable = IterableOnly.of(1, 2, 3, 4);
    /// ```
    /// @param <E> the element type.
    /// @param values the array of elements.
    /// @return an iterable containing the element.
    /// @throws NullPointerException if the array is `null`
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <E> Iterable<E> of(final E... values) {
        return new IterableOnly<>(List.of(values));
    }

    /// Creates an iterable from a list of one element.
    /// ```java
    ///    Iterable<Integer> iterable = IterableOnly.of(1);
    /// ```
    /// @param <E> the element type.
    /// @param e1 the first element.
    /// @return an iterable containing the specified elements.
    /// @throws NullPointerException if the array is `null`
    public static <E> Iterable<E> of(final E e1) {
        return new IterableOnly<>(List.of(e1));
    }

    /// Creates an iterable from an list of two elements.
    /// ```java
    ///    Iterable<Integer> iterable = IterableOnly.of(1, 2);
    /// ```
    /// @param <E> the element type.
    /// @param e1 the first element.
    /// @param e2 the second element.
    /// @return an iterable containing the specified elements.
    /// @throws NullPointerException if the array is `null`
    public static <E> Iterable<E> of(final E e1, final E e2) {
        return new IterableOnly<>(List.of(e1, e2));
    }

    /// Creates an iterable from an list of three elements.
    /// ```java
    ///    Iterable<Integer> iterable = IterableOnly.of(1, 2, 3);
    /// ```
    /// @param <E> the element type.
    /// @param e1 the first element.
    /// @param e2 the second element.
    /// @param e3 the third element.
    /// @return an iterable containing the specified elements.
    /// @throws NullPointerException if the array is `null`
    public static <E> Iterable<E> of(final E e1, final E e2, final E e3) {
        return new IterableOnly<>(List.of(e1, e2, e3));
    }

    /// Creates an iterable from a list of four elements.
    /// ```java
    ///    Iterable<Integer> iterable = IterableOnly.of(1, 2, 3, 4);
    /// ```
    /// @param <E> the element type.
    /// @param e1 the first element.
    /// @param e2 the second element.
    /// @param e3 the third element.
    /// @param e4 the third element.
    /// @return an iterable containing the specified elements.
    /// @throws NullPointerException if the array is `null`
    public static <E> Iterable<E> of(final E e1, final E e2, final E e3, final E e4) {
        return new IterableOnly<>(List.of(e1, e2, e3, e4));
    }
}
