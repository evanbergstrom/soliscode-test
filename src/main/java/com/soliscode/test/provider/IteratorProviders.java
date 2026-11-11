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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Function;

/// Factories of [IteratorProvider]s, for the iterators of a collection or list.
///
/// A list's source is ordered, and reads its elements with `get(int)`; a collection's is not, and reads them with
/// `contains` and `size`. Neither uses an iterator, unless the class's own `get` or `contains` does
/// ([java.util.AbstractSequentialList], [java.util.AbstractCollection]).
///
/// @since 0.1.3
public final class IteratorProviders {

    private IteratorProviders() { }

    /// The iterators of an [ArrayList].
    ///
    /// @param <E> the element type
    /// @return the provider
    public static <E> IteratorProvider<Iterator<E>, E> arrayList() {
        return fromList(ArrayList::new);
    }

    /// The iterators of a [LinkedList].
    ///
    /// @param <E> the element type
    /// @return the provider
    // JdkObsolete: this provides LinkedList's own iterators, to test them.
    @SuppressWarnings("JdkObsolete")
    public static <E> IteratorProvider<Iterator<E>, E> linkedList() {
        return fromList(LinkedList::new);
    }

    /// The list iterators of an [ArrayList].
    ///
    /// @param <E> the element type
    /// @return the provider
    public static <E> IteratorProvider<ListIterator<E>, E> arrayListListIterator() {
        return listIteratorsOf(ArrayList::new);
    }

    /// The list iterators of a [LinkedList].
    ///
    /// @param <E> the element type
    /// @return the provider
    // JdkObsolete: this provides LinkedList's own iterators, to test them.
    @SuppressWarnings("JdkObsolete")
    public static <E> IteratorProvider<ListIterator<E>, E> linkedListListIterator() {
        return listIteratorsOf(LinkedList::new);
    }

    /// The iterators of a list: ordered, its elements read with `get(int)`.
    ///
    /// @param factory makes the list from the elements it is to hold, in their order
    /// @param <E> the element type
    /// @return the provider
    public static <E> IteratorProvider<Iterator<E>, E> fromList(final Function<List<E>, ? extends List<E>> factory) {
        Objects.requireNonNull(factory);
        return elements -> new ListSource<>(factory.apply(new ArrayList<>(elements)), List::iterator);
    }

    /// The list iterators of a list: ordered, its elements read with `get(int)`.
    ///
    /// @param factory makes the list from the elements it is to hold, in their order
    /// @param <E> the element type
    /// @return the provider
    public static <E> IteratorProvider<ListIterator<E>, E> listIteratorsOf(
            final Function<List<E>, ? extends List<E>> factory) {
        Objects.requireNonNull(factory);
        return elements -> new ListSource<>(factory.apply(new ArrayList<>(elements)), List::listIterator);
    }

    /// The iterators of a collection: unordered, its elements read with `contains` and `size`.
    ///
    /// [IteratorSource#elements()] gives the given elements the collection still contains, in their given order, and
    /// throws [AssertionError] if its size is not their number. It is meant for unique elements, which the contracts
    /// use.
    ///
    /// @param factory makes the collection from the elements it is to hold
    /// @param <E> the element type
    /// @return the provider
    public static <E> IteratorProvider<Iterator<E>, E> fromCollection(
            final Function<List<E>, ? extends Collection<E>> factory) {
        Objects.requireNonNull(factory);
        return elements -> new CollectionSource<>(factory.apply(new ArrayList<>(elements)), new ArrayList<>(elements));
    }

    /// A list and the iterators of one kind over it.
    private record ListSource<I extends Iterator<E>, E>(List<E> list, Function<List<E>, I> iterators)
            implements IteratorSource<I, E> {

        @Override
        public I iterator() {
            return iterators.apply(list);
        }

        @Override
        public List<E> elements() {
            List<E> copy = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i++) {
                copy.add(list.get(i));
            }
            return copy;
        }
    }

    /// A collection, and the elements it was made from.
    private record CollectionSource<E>(Collection<E> collection, List<E> given)
            implements IteratorSource<Iterator<E>, E> {

        @Override
        public Iterator<E> iterator() {
            return collection.iterator();
        }

        @Override
        public List<E> elements() {
            List<E> present = new ArrayList<>();
            for (final E e : given) {
                if (collection.contains(e)) {
                    present.add(e);
                }
            }
            if (present.size() != collection.size()) {
                throw new AssertionError("the collection holds " + collection.size() + " elements, but only "
                        + present.size() + " of those it was made from: it holds others");
            }
            return present;
        }

        @Override
        public boolean ordered() {
            return false;
        }
    }
}
