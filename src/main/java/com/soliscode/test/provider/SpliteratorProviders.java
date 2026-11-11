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
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.jspecify.annotations.Nullable;

/// Factories of [SpliteratorProvider]s: for the spliterators of a collection, of a range and of an array.
///
/// Each source documents the characteristics the JDK documents for its spliterators
/// ([SpliteratorSource#characteristics()]).
///
/// @since 0.1.3
public final class SpliteratorProviders {

    /// What `Arrays.spliterator` documents its spliterators report.
    private static final int ARRAY = Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED
            | Spliterator.IMMUTABLE;

    /// What `TreeSet#spliterator` documents.
    private static final int TREE_SET = Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.SORTED
            | Spliterator.ORDERED;

    private SpliteratorProviders() { }

    /// The spliterators of an [ArrayList], which document `SIZED`, `SUBSIZED` and `ORDERED` (`ArrayList#spliterator`).
    /// Its sources can remove an element.
    ///
    /// @param <E> the element type
    /// @return the provider
    public static <E> SpliteratorProvider<Spliterator<E>, E> arrayList() {
        return fromCollection(ArrayList::new, Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED, true);
    }

    /// The spliterators of a [TreeSet] in natural order, which document `SIZED`, `DISTINCT`, `SORTED` and `ORDERED`
    /// (`TreeSet#spliterator`). Its sources can remove an element.
    ///
    /// @param <E> the element type, which must be [Comparable]
    /// @return the provider
    public static <E> SpliteratorProvider<Spliterator<E>, E> treeSet() {
        return fromCollection(TreeSet::new, TREE_SET, true);
    }

    /// The spliterators of a [TreeSet] ordered by a comparator, which document `SIZED`, `DISTINCT`, `SORTED` and
    /// `ORDERED` (`TreeSet#spliterator`). Its sources can remove an element.
    ///
    /// @param comparator the set's order
    /// @param <E> the element type
    /// @return the provider
    public static <E> SpliteratorProvider<Spliterator<E>, E> treeSet(final Comparator<? super E> comparator) {
        Objects.requireNonNull(comparator);
        return fromCollection(elements -> {
            TreeSet<E> set = new TreeSet<>(comparator);
            set.addAll(elements);
            return set;
        }, TREE_SET, true);
    }

    /// The spliterators of a collection.
    ///
    /// [SpliteratorSource#elements()] reads the collection with its iterator, not its spliterator;
    /// [SpliteratorSource#removeOne()], where the collection is modifiable, removes the last of them with
    /// `remove(Object)`; and [SpliteratorSource#comparator()] is a [SortedSet]'s comparator, or `null` for any other
    /// collection.
    ///
    /// @param factory makes the collection from the elements it is to hold
    /// @param characteristics those the collection's class documents its spliterators report
    /// @param modifiable whether an element can be removed from the collection
    /// @param <E> the element type
    /// @return the provider
    public static <E> SpliteratorProvider<Spliterator<E>, E> fromCollection(
            final Function<List<E>, ? extends Collection<E>> factory, final int characteristics,
            final boolean modifiable) {
        Objects.requireNonNull(factory);
        return elements -> new CollectionSource<>(factory.apply(new ArrayList<>(elements)), characteristics,
                modifiable);
    }

    /// The spliterators of `IntStream.range`, which document `ORDERED` ("a sequential ordered `IntStream`"). Its
    /// sources hold only consecutive ascending elements, and cannot be changed.
    ///
    /// @return the provider
    public static SpliteratorProvider<Spliterator.OfInt, Integer> intRange() {
        return elements -> {
            long first = firstOfRange(elements, Integer::longValue);
            // firstOfRange checks each element is first + i, so the end is at most Integer.MAX_VALUE + 1. Only there,
            // where IntStream.range cannot name its end, is the range closed.
            long end = first + elements.size();
            return new FixedSource<>(() -> end <= Integer.MAX_VALUE
                    ? IntStream.range((int) first, (int) end).spliterator()
                    : IntStream.rangeClosed((int) first, Integer.MAX_VALUE).spliterator(),
                    elements, Spliterator.ORDERED);
        };
    }

    /// The spliterators of `LongStream.range`, which document `ORDERED` ("a sequential ordered `LongStream`"). Its
    /// sources hold only consecutive ascending elements, and cannot be changed.
    ///
    /// @return the provider
    public static SpliteratorProvider<Spliterator.OfLong, Long> longRange() {
        return elements -> {
            long first = firstOfRange(elements, Long::longValue);
            return new FixedSource<>(() -> LongStream.range(first, first + elements.size()).spliterator(), elements,
                    Spliterator.ORDERED);
        };
    }

    /// The spliterators of an `int` array, `Arrays.spliterator(int[])`, which document `SIZED`, `SUBSIZED`,
    /// `ORDERED` and `IMMUTABLE`. Its sources cannot be changed.
    ///
    /// @return the provider
    public static SpliteratorProvider<Spliterator.OfInt, Integer> intArray() {
        return elements -> {
            int[] array = elements.stream().mapToInt(Integer::intValue).toArray();
            return new FixedSource<>(() -> Arrays.spliterator(array), elements, ARRAY);
        };
    }

    /// The spliterators of a `long` array, `Arrays.spliterator(long[])`, which document `SIZED`, `SUBSIZED`,
    /// `ORDERED` and `IMMUTABLE`. Its sources cannot be changed.
    ///
    /// @return the provider
    public static SpliteratorProvider<Spliterator.OfLong, Long> longArray() {
        return elements -> {
            long[] array = elements.stream().mapToLong(Long::longValue).toArray();
            return new FixedSource<>(() -> Arrays.spliterator(array), elements, ARRAY);
        };
    }

    /// The spliterators of a `double` array, `Arrays.spliterator(double[])`, which document `SIZED`, `SUBSIZED`,
    /// `ORDERED` and `IMMUTABLE`. Its sources cannot be changed.
    ///
    /// @return the provider
    public static SpliteratorProvider<Spliterator.OfDouble, Double> doubleArray() {
        return elements -> {
            double[] array = elements.stream().mapToDouble(Double::doubleValue).toArray();
            return new FixedSource<>(() -> Arrays.spliterator(array), elements, ARRAY);
        };
    }

    /// Returns the first of a run of consecutive ascending values, or 0 for none.
    ///
    /// @throws IllegalArgumentException if the values are not consecutive and ascending
    private static <E> long firstOfRange(final List<E> elements, final ToLongFunction<E> value) {
        if (elements.isEmpty()) {
            return 0;
        }
        long first = value.applyAsLong(elements.getFirst());
        for (int i = 0; i < elements.size(); i++) {
            if (value.applyAsLong(elements.get(i)) != first + i) {
                throw new IllegalArgumentException("a range holds only consecutive ascending values, but was given "
                        + elements);
            }
        }
        return first;
    }

    /// A collection, the characteristics its class documents, and whether it can be changed.
    private record CollectionSource<E>(Collection<E> collection, int characteristics, boolean modifiable)
            implements SpliteratorSource<Spliterator<E>, E> {

        @Override
        public Spliterator<E> spliterator() {
            return collection.spliterator();
        }

        @Override
        public List<E> elements() {
            List<E> copy = new ArrayList<>();
            for (final E e : collection) {
                copy.add(e);
            }
            return copy;
        }

        @Override
        public @Nullable Comparator<? super E> comparator() {
            return collection instanceof SortedSet<E> sorted ? sorted.comparator() : null;
        }

        @Override
        public boolean removeOne() {
            if (!modifiable || collection.isEmpty()) {
                return false;
            }
            return collection.remove(elements().getLast());
        }
    }

    /// A source whose contents are fixed: a range or an array, made from its elements.
    private record FixedSource<S extends Spliterator<E>, E>(Supplier<S> spliterators,
                                                             List<E> given, int characteristics)
            implements SpliteratorSource<S, E> {

        FixedSource {
            given = List.copyOf(given);
        }

        @Override
        public S spliterator() {
            return spliterators.get();
        }

        @Override
        public List<E> elements() {
            return new ArrayList<>(given);
        }
    }
}
