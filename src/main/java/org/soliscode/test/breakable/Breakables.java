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
package org.soliscode.test.breakable;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/// Utility class for creating breakable objects for testing contract classes.
/// @author evanbergstrom
/// @since 1.0
public final class Breakables {

    private Breakables() {}

    /// Creates a builder for a `BreakableIterable`.
    /// @param <E> the type of the elements.
    /// @param c the class of the element for the iterable.
    /// @return a builder.
    public static <E> BreakableIterable.Builder<E> buildIterable(final Class<E> c) {
        return new BreakableIterable.Builder<>();
    }

    /// Creates a builder for a `BreakableIterable` initialized with a set of elements.
    /// @param <E> the type of the elements.
    /// @param e the elements to add to the iterable.
    /// @return a builder.
    @SafeVarargs
    public static <E> BreakableIterable.Builder<E> buildIterable(final E...e) {
        return new BreakableIterable.Builder<E>()
                .addElements(e);
    }

    /// Creates a builder for a `BreakableCollection`.
    /// @param <E> the type of the elements.
    /// @param c the class of the element for the collection.
    /// @return a builder.
    public static <E> BreakableCollection.Builder<E> buildCollection(final Class<E> c) {
        return new BreakableCollection.Builder<>();
    }

    /// Creates a builder for a `BreakableCollection` initialized with a set of elements.
    /// @param <E> the type of the elements.
    /// @param e the elements to add to the collection.
    /// @return a builder.
    @SafeVarargs
    public static <E> BreakableCollection.Builder<E> buildCollection(final E... e) {
        return new BreakableCollection.Builder<E>()
                .addElements(e);
    }


    /// Creates a builder for a `BreakableSequencedCollection`.
    /// @param <E> the type of the elements.
    /// @param c the class of the element for the collection.
    /// @return a builder.
    public static <E> BreakableSequencedCollection.Builder<E> buildSequencedCollection(final Class<E> c) {
        return new BreakableSequencedCollection.Builder<>();
    }

    /// Creates a builder for a `BreakableSequencedCollection` initialized with a set of elements.
    /// @param <E> the type of the elements.
    /// @param e the elements to add to the collection.
    /// @return a builder.
    @SafeVarargs
    public static <E> BreakableSequencedCollection.Builder<E> buildSequencedCollection(final E... e) {
        return new BreakableSequencedCollection.Builder<E>()
                .addElements(e);
    }

    /// Makes sure that the iterator is not a broken iterator. If the argument is an instance of `BreakableIterator`
    /// then the underlying iterator used as the element store is returned.
    /// @param i an instance of iterator.
    /// @return an unbroken iterator.
    public static <E> Iterable<E> ensureUnbroken(final @NotNull Iterable<E> i) {
        if (i instanceof BreakableIterable<E> b) {
            return b.unbroken();
        } else {
            return i;
        }
    }

    /// Makes sure that the collection is not a broken collection. If the argument is an instance of `BreakableCollection`
    /// then the underlying collection used as the element store is returned.
    /// @param i an instance of collection.
    /// @return an unbroken collection.
    public static <E> Collection<E> ensureUnbroken(final @NotNull Collection<E> i) {
        if (i instanceof BreakableCollection<E> b) {
            return b.unbroken();
        } else {
            return i;
        }
    }
}
