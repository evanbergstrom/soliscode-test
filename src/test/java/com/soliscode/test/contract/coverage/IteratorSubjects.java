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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.MethodStatus;
import com.soliscode.test.breakable.Break;
import com.soliscode.test.breakable.BreakableIterator;
import com.soliscode.test.breakable.BreakableListIterator;
import com.soliscode.test.contract.iterator.IteratorContract;
import com.soliscode.test.contract.iterator.WeaklyConsistentIteratorContract;
import com.soliscode.test.contract.listiterator.ListIteratorContract;
import com.soliscode.test.contract.support.WithIntegerElement;
import com.soliscode.test.provider.IteratorProvider;
import com.soliscode.test.provider.IteratorSource;

/// The subjects `ContractCoverageTest` runs `IteratorContract` and `ListIteratorContract` against: JDK iterators,
/// and the breakable ones, each with the methods it does not support (#37).
final class IteratorSubjects {

    private IteratorSubjects() { }

    /// A contract instance on an iterator provider, which does not support the given methods.
    abstract static class Subject<I extends Iterator<Integer>> extends AbstractTest implements WithIntegerElement {
        private final IteratorProvider<I, Integer> iterators;

        Subject(final IteratorProvider<I, Integer> iteratorsArg, final List<InterfaceMethod> unsupported) {
            this.iterators = iteratorsArg;
            unsupported.forEach(this::doesNotSupportMethod);
        }

        public IteratorProvider<I, Integer> iteratorProvider() {
            return iterators;
        }
    }

    /// `IteratorContract` on a JDK iterator.
    static final class JdkIterator extends Subject<Iterator<Integer>>
            implements IteratorContract<Iterator<Integer>, Integer> {
        JdkIterator(final IteratorProvider<Iterator<Integer>, Integer> iterators,
                    final InterfaceMethod... unsupported) {
            super(iterators, List.of(unsupported));
        }
    }

    /// `WeaklyConsistentIteratorContract` on a JDK iterator.
    static final class JdkWeakIterator extends Subject<Iterator<Integer>>
            implements WeaklyConsistentIteratorContract<Iterator<Integer>, Integer> {
        JdkWeakIterator(final IteratorProvider<Iterator<Integer>, Integer> iterators,
                        final InterfaceMethod... unsupported) {
            super(iterators, List.of(unsupported));
        }
    }

    /// `WeaklyConsistentIteratorContract` on a `BreakableIterator` over a `ConcurrentLinkedQueue`'s iterator, with a
    /// break or none, and the queue's size as its probe: an ordered source of its own, so that the breaks
    /// `IteratorContract` sees on an `ArrayList` show here too.
    static final class BrokenWeakIterator extends Subject<BreakableIterator<Integer>>
            implements WeaklyConsistentIteratorContract<BreakableIterator<Integer>, Integer> {
        BrokenWeakIterator(final @Nullable Break b, final Optional<InterfaceMethod> unsupported) {
            super(elements -> new QueueSource(new ConcurrentLinkedQueue<>(elements), b, unsupported),
                    unsupported.stream().toList());
        }
    }

    /// A `ConcurrentLinkedQueue`, in its FIFO order, with breakable iterators over its own.
    private record QueueSource(ConcurrentLinkedQueue<Integer> queue, @Nullable Break b,
                               Optional<InterfaceMethod> unsupported)
            implements IteratorSource<BreakableIterator<Integer>, Integer> {
        @Override
        public BreakableIterator<Integer> iterator() {
            return new BreakableIterator<>(queue.iterator(), breaks(b), statuses(unsupported), 0, false, queue::size);
        }

        @Override
        public List<Integer> elements() {
            return new ArrayList<>(queue);
        }

        @Override
        public boolean removeFromSource(final Integer element) {
            return queue.remove(element);
        }

        @Override
        public boolean addToSource(final Integer element) {
            return queue.add(element);
        }
    }

    /// `ListIteratorContract` on a JDK list iterator.
    static final class JdkListIterator extends Subject<ListIterator<Integer>>
            implements ListIteratorContract<ListIterator<Integer>, Integer> {
        JdkListIterator(final IteratorProvider<ListIterator<Integer>, Integer> iterators,
                        final InterfaceMethod... unsupported) {
            super(iterators, List.of(unsupported));
        }
    }

    /// `IteratorContract` on a `BreakableIterator` over an `ArrayList`'s iterator, with a break or none.
    static final class BrokenIterator extends Subject<BreakableIterator<Integer>>
            implements IteratorContract<BreakableIterator<Integer>, Integer> {
        BrokenIterator(final @Nullable Break b, final Optional<InterfaceMethod> unsupported) {
            super(breakable(list -> new BreakableIterator<>(list.iterator(), breaks(b), statuses(unsupported), 0,
                    false)), unsupported.stream().toList());
        }
    }

    /// `ListIteratorContract` on a `BreakableListIterator` over an `ArrayList`'s list iterator, with a break or none.
    static final class BrokenListIterator extends Subject<BreakableListIterator<Integer>>
            implements ListIteratorContract<BreakableListIterator<Integer>, Integer> {
        BrokenListIterator(final @Nullable Break b, final Optional<InterfaceMethod> unsupported) {
            super(breakable(list -> new BreakableListIterator<>(list.listIterator(), breaks(b),
                    statuses(unsupported))), unsupported.stream().toList());
        }
    }

    private static Set<Break> breaks(final @Nullable Break b) {
        return b == null ? Set.of() : Set.of(b);
    }

    private static Map<InterfaceMethod, MethodStatus> statuses(final Optional<InterfaceMethod> unsupported) {
        Map<InterfaceMethod, MethodStatus> statuses = new HashMap<>();
        unsupported.ifPresent(m -> statuses.put(m, new MethodStatus(false)));
        return statuses;
    }

    /// Sources on an `ArrayList`, whose iterators are breakable ones over its own.
    private static <I extends Iterator<Integer>> IteratorProvider<I, Integer> breakable(
            final Function<List<Integer>, I> iterators) {
        return elements -> new ListSource<>(new ArrayList<>(elements), iterators);
    }

    private record ListSource<I extends Iterator<Integer>>(List<Integer> list, Function<List<Integer>, I> iterators)
            implements IteratorSource<I, Integer> {
        @Override
        public I iterator() {
            return iterators.apply(list);
        }

        @Override
        public List<Integer> elements() {
            return new ArrayList<>(list);
        }
    }
}
