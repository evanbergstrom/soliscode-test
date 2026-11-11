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
package com.soliscode.test.contract.coverage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.jspecify.annotations.Nullable;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.breakable.Break;
import com.soliscode.test.breakable.BreakableSpliterator;
import com.soliscode.test.breakable.BreakableSpliteratorOfDouble;
import com.soliscode.test.breakable.BreakableSpliteratorOfInt;
import com.soliscode.test.breakable.BreakableSpliteratorOfLong;
import com.soliscode.test.contract.spliterator.SpliteratorContract;
import com.soliscode.test.contract.spliterator.SpliteratorOfDoubleContract;
import com.soliscode.test.contract.spliterator.SpliteratorOfIntContract;
import com.soliscode.test.contract.spliterator.SpliteratorOfLongContract;
import com.soliscode.test.provider.DoubleProvider;
import com.soliscode.test.provider.IntegerProvider;
import com.soliscode.test.provider.LongProvider;
import com.soliscode.test.provider.ObjectProvider;
import com.soliscode.test.provider.SpliteratorProvider;
import com.soliscode.test.provider.SpliteratorProviders;
import com.soliscode.test.provider.SpliteratorSource;

/// The subjects `ContractCoverageTest` runs the spliterator contracts against: JDK spliterators, and breakable ones
/// over a JDK spliterator, with a break or none (#38).
final class SpliteratorSubjects {

    /// What `ArrayList#spliterator` documents.
    static final int ARRAY_LIST_DOCUMENTED = Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED;

    /// What `TreeSet#spliterator` documents.
    static final int TREE_SET = Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED;

    /// What `Arrays.spliterator` documents.
    static final int ARRAY = Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED | Spliterator.IMMUTABLE;

    private SpliteratorSubjects() { }

    /// The JDK spliterators a breakable subject is made over, each with what its class documents.
    enum Base {
        /// An `ArrayList`'s spliterator; the source can remove an element.
        ARRAY_LIST(ArrayList::new, ARRAY_LIST_DOCUMENTED, Collection::spliterator),
        /// A `TreeSet`'s in natural order; the source can remove an element.
        TREE_SET_NATURAL(TreeSet::new, TREE_SET, Collection::spliterator),
        /// A `TreeSet`'s in reverse order; the source can remove an element.
        TREE_SET_REVERSED(l -> {
            TreeSet<Integer> set = new TreeSet<>(Comparator.reverseOrder());
            set.addAll(l);
            return set;
        }, TREE_SET, Collection::spliterator),
        /// An `IntStream.range`'s, boxed: `NONNULL` among its characteristics. Its source cannot be changed.
        INT_RANGE(List::copyOf, Spliterator.ORDERED,
                c -> c.isEmpty() ? IntStream.range(0, 0).spliterator()
                        : IntStream.range(c.iterator().next(), c.iterator().next() + c.size()).spliterator()),
        /// `Spliterators.spliteratorUnknownSize` over an `ArrayList`'s iterator: `ORDERED`, not `SIZED`; the source
        /// can remove an element, and the iterator then throws `ConcurrentModificationException`.
        UNKNOWN_SIZE(ArrayList::new, Spliterator.ORDERED,
                c -> Spliterators.spliteratorUnknownSize(c.iterator(), Spliterator.ORDERED));

        private final Function<List<Integer>, Collection<Integer>> factory;
        private final int documented;
        private final Function<Collection<Integer>, Spliterator<Integer>> spliterators;

        Base(final Function<List<Integer>, Collection<Integer>> factoryArg, final int documentedArg,
             final Function<Collection<Integer>, Spliterator<Integer>> spliteratorsArg) {
            this.factory = factoryArg;
            this.documented = documentedArg;
            this.spliterators = spliteratorsArg;
        }

        /// Whether its source can remove an element.
        boolean modifiable() {
            return this != INT_RANGE;
        }
    }

    /// A contract instance on a spliterator provider and an element provider.
    abstract static class Subject<S extends Spliterator<E>, E> extends AbstractTest {
        private final SpliteratorProvider<S, E> spliterators;
        private final ObjectProvider<E> elements;

        Subject(final SpliteratorProvider<S, E> spliteratorsArg, final ObjectProvider<E> elementsArg) {
            this.spliterators = spliteratorsArg;
            this.elements = elementsArg;
        }

        public SpliteratorProvider<S, E> spliteratorProvider() {
            return spliterators;
        }

        public ObjectProvider<E> elementProvider() {
            return elements;
        }
    }

    /// `SpliteratorContract` on a spliterator provider of `Integer`.
    static final class Plain<S extends Spliterator<Integer>> extends Subject<S, Integer>
            implements SpliteratorContract<S, Integer> {
        Plain(final SpliteratorProvider<S, Integer> spliterators) {
            super(spliterators, new IntegerProvider());
        }
    }

    /// `SpliteratorOfIntContract` on a provider of `int` spliterators.
    static final class OfInt<S extends Spliterator.OfInt> extends Subject<S, Integer>
            implements SpliteratorOfIntContract<S> {
        OfInt(final SpliteratorProvider<S, Integer> spliterators) {
            super(spliterators, new IntegerProvider());
        }
    }

    /// `SpliteratorOfLongContract` on a provider of `long` spliterators.
    static final class OfLong<S extends Spliterator.OfLong> extends Subject<S, Long>
            implements SpliteratorOfLongContract<S> {
        OfLong(final SpliteratorProvider<S, Long> spliterators) {
            super(spliterators, new LongProvider());
        }
    }

    /// `SpliteratorOfDoubleContract` on a provider of `double` spliterators.
    static final class OfDouble<S extends Spliterator.OfDouble> extends Subject<S, Double>
            implements SpliteratorOfDoubleContract<S> {
        OfDouble(final SpliteratorProvider<S, Double> spliterators) {
            super(spliterators, new DoubleProvider());
        }
    }

    /// `SpliteratorContract` on a breakable spliterator over a base, with a break or none.
    static Plain<BreakableSpliterator<Integer>> broken(final Base base, final @Nullable Break b) {
        return new Plain<>(elements -> new BrokenSource(base, base.factory.apply(new ArrayList<>(elements)),
                breaks(b)));
    }

    /// `SpliteratorOfIntContract` on a breakable `int` spliterator over an `int[]`'s, with a break or none.
    static OfInt<BreakableSpliteratorOfInt> brokenInt(final @Nullable Break b) {
        return new OfInt<>(elements -> new ArraySource<>(() -> {
            Spliterator.OfInt delegate = Arrays.spliterator(elements.stream().mapToInt(Integer::intValue).toArray());
            return new BreakableSpliteratorOfInt(delegate, breaks(b), Map.of(), delegate.characteristics(), false);
        }, elements));
    }

    /// `SpliteratorOfLongContract` on a breakable `long` spliterator over a `long[]`'s, with a break or none.
    static OfLong<BreakableSpliteratorOfLong> brokenLong(final @Nullable Break b) {
        return new OfLong<>(elements -> new ArraySource<>(() -> {
            Spliterator.OfLong delegate = Arrays.spliterator(elements.stream().mapToLong(Long::longValue).toArray());
            return new BreakableSpliteratorOfLong(delegate, breaks(b), Map.of(), delegate.characteristics(), false);
        }, elements));
    }

    /// `SpliteratorOfDoubleContract` on a breakable `double` spliterator over a `double[]`'s, with a break or none.
    static OfDouble<BreakableSpliteratorOfDouble> brokenDouble(final @Nullable Break b) {
        return new OfDouble<>(elements -> new ArraySource<>(() -> {
            Spliterator.OfDouble delegate = Arrays.spliterator(
                    elements.stream().mapToDouble(Double::doubleValue).toArray());
            return new BreakableSpliteratorOfDouble(delegate, breaks(b), Map.of(), delegate.characteristics(), false);
        }, elements));
    }

    /// The JDK spliterators of a filtered `DoubleStream`, which are not `SIZED`, and document only `ORDERED`.
    static SpliteratorProvider<Spliterator.OfDouble, Double> filteredDoubles() {
        return elements -> new ArraySource<>(() -> elements.stream().mapToDouble(Double::doubleValue)
                .filter(d -> true).spliterator(), elements, Spliterator.ORDERED);
    }

    private static Set<Break> breaks(final @Nullable Break b) {
        return b == null ? Set.of() : Set.of(b);
    }

    /// A collection, and breakable spliterators over a base's spliterators of it.
    private record BrokenSource(Base base, Collection<Integer> collection, Set<Break> breaks)
            implements SpliteratorSource<BreakableSpliterator<Integer>, Integer> {

        @Override
        public BreakableSpliterator<Integer> spliterator() {
            Spliterator<Integer> delegate = base.spliterators.apply(collection);
            return new BreakableSpliterator<>(delegate, breaks, Map.of(), delegate.characteristics(), false);
        }

        @Override
        public List<Integer> elements() {
            List<Integer> copy = new ArrayList<>();
            for (final Integer e : collection) {
                copy.add(e);
            }
            return copy;
        }

        @Override
        public int characteristics() {
            return base.documented;
        }

        @Override
        public @Nullable Comparator<? super Integer> comparator() {
            return collection instanceof SortedSet<Integer> sorted ? sorted.comparator() : null;
        }

        @Override
        public boolean removeOne() {
            return base.modifiable() && !collection.isEmpty() && collection.remove(elements().getLast());
        }
    }

    /// A fixed source: an array's or a stream's spliterators over given elements.
    private record ArraySource<S extends Spliterator<E>, E>(Supplier<S> spliterators,
                                                             List<E> given, int characteristics)
            implements SpliteratorSource<S, E> {

        ArraySource(final Supplier<S> spliteratorsArg, final List<E> givenArg) {
            this(spliteratorsArg, givenArg, ARRAY);
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

    /// The JDK providers `SpliteratorContract` passes against, by name.
    static Map<String, SpliteratorProvider<? extends Spliterator<Integer>, Integer>> jdkProviders() {
        Map<String, SpliteratorProvider<? extends Spliterator<Integer>, Integer>> providers =
                new LinkedHashMap<>();
        providers.put("TreeSet's spliterator", SpliteratorProviders.treeSet());
        providers.put("a reversed TreeSet's spliterator", SpliteratorProviders.treeSet(Comparator.reverseOrder()));
        providers.put("IntStream.range's spliterator", SpliteratorProviders.intRange());
        providers.put("LinkedList's spliterator", SpliteratorProviders.fromCollection(
                LinkedList::new, Spliterator.SIZED | Spliterator.ORDERED, true));
        providers.put("HashSet's spliterator, unordered", SpliteratorProviders.fromCollection(
                HashSet::new, Spliterator.SIZED | Spliterator.DISTINCT, true));
        providers.put("List.of's spliterator", SpliteratorProviders.fromCollection(List::copyOf, 0, false));
        providers.put("CopyOnWriteArrayList's spliterator, IMMUTABLE", SpliteratorProviders.fromCollection(
                CopyOnWriteArrayList::new,
                Spliterator.IMMUTABLE | Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED, true));
        providers.put("ConcurrentLinkedQueue's spliterator, CONCURRENT", SpliteratorProviders.fromCollection(
                ConcurrentLinkedQueue::new,
                Spliterator.CONCURRENT | Spliterator.ORDERED | Spliterator.NONNULL, true));
        providers.put("ConcurrentSkipListSet's spliterator, CONCURRENT and SORTED", SpliteratorProviders.fromCollection(
                ConcurrentSkipListSet::new, Spliterator.CONCURRENT | Spliterator.NONNULL
                        | Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED, true));
        return providers;
    }
}
