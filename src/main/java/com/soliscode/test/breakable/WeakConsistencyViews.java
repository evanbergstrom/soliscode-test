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
package com.soliscode.test.breakable;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

/// Views of a map, wrapped so that their iterators apply [BreakableIterator]'s three weak-consistency breaks
/// ([BreakableIterator#ITERATOR_IS_FAIL_FAST], [BreakableIterator#ITERATOR_RETURNS_AN_ELEMENT_TWICE_AFTER_A_CHANGE],
/// [BreakableIterator#ITERATOR_SKIPS_AN_ELEMENT_AFTER_A_CHANGE]), with the view's size as the probe.
///
/// A breakable wraps a view only where one of those breaks is set. Every method but the iterators delegates to the
/// view, so that the wrapper changes nothing else.
final class WeakConsistencyViews {

    private WeakConsistencyViews() { }

    /// Returns the set, or, where one of the weak-consistency breaks is set, a wrapper whose iterator applies it.
    ///
    /// @param set the view
    /// @param breaks the breakable's breaks
    /// @param <E> the element type
    /// @return the view or its wrapper
    static <E> Set<E> set(final Set<E> set, final Set<Break> breaks) {
        return BreakableIterator.hasWeakConsistencyBreak(breaks) ? new WrappedSet<>(set, breaks) : set;
    }

    /// Returns the navigable set, or, where one of the weak-consistency breaks is set, a wrapper whose `iterator()` and
    /// `descendingIterator()` apply it.
    ///
    /// @param set the view
    /// @param breaks the breakable's breaks
    /// @param <E> the element type
    /// @return the view or its wrapper
    static <E> NavigableSet<E> navigableSet(final NavigableSet<E> set, final Set<Break> breaks) {
        return BreakableIterator.hasWeakConsistencyBreak(breaks) ? new WrappedNavigableSet<>(set, breaks) : set;
    }

    /// A set that delegates every method to another, and wraps its iterator.
    private static class WrappedSet<E> extends AbstractSet<E> {
        private final Set<E> set;
        private final Set<Break> breaks;

        WrappedSet(final Set<E> setArg, final Set<Break> breaksArg) {
            this.set = setArg;
            this.breaks = breaksArg;
        }

        Set<Break> breaks() {
            return breaks;
        }

        @Override
        public Iterator<E> iterator() {
            return BreakableIterator.withWeakConsistencyBreaks(set.iterator(), breaks, set::size);
        }

        @Override
        public int size() {
            return set.size();
        }

        @Override
        public boolean isEmpty() {
            return set.isEmpty();
        }

        @Override
        public boolean contains(final @Nullable Object o) {
            return set.contains(o);
        }

        @Override
        public boolean containsAll(final Collection<?> c) {
            return set.containsAll(c);
        }

        @Override
        public boolean add(final E e) {
            return set.add(e);
        }

        @Override
        public boolean addAll(final Collection<? extends E> c) {
            return set.addAll(c);
        }

        @Override
        public boolean remove(final @Nullable Object o) {
            return set.remove(o);
        }

        @Override
        public boolean removeAll(final Collection<?> c) {
            return set.removeAll(c);
        }

        @Override
        public boolean retainAll(final Collection<?> c) {
            return set.retainAll(c);
        }

        @Override
        public boolean removeIf(final Predicate<? super E> filter) {
            return set.removeIf(filter);
        }

        @Override
        public void clear() {
            set.clear();
        }

        @Override
        public Object[] toArray() {
            return set.toArray();
        }

        @Override
        public <T> T[] toArray(final T[] a) {
            return set.toArray(a);
        }

        @Override
        public <T> T[] toArray(final IntFunction<T[]> generator) {
            return set.toArray(generator);
        }

        @Override
        public void forEach(final Consumer<? super E> action) {
            set.forEach(action);
        }

        @Override
        public Spliterator<E> spliterator() {
            return set.spliterator();
        }

        @Override
        public boolean equals(final @Nullable Object o) {
            return o == this || set.equals(o);
        }

        @Override
        public int hashCode() {
            return set.hashCode();
        }

        @Override
        public String toString() {
            return set.toString();
        }
    }

    /// A navigable set that delegates every method to another, and wraps its two iterators and those of the sets it
    /// returns.
    private static final class WrappedNavigableSet<E> extends WrappedSet<E> implements NavigableSet<E> {
        private final NavigableSet<E> set;

        WrappedNavigableSet(final NavigableSet<E> setArg, final Set<Break> breaksArg) {
            super(setArg, breaksArg);
            this.set = setArg;
        }

        @Override
        public Iterator<E> descendingIterator() {
            return BreakableIterator.withWeakConsistencyBreaks(set.descendingIterator(), breaks(), set::size);
        }

        @Override
        public @Nullable E lower(final E e) {
            return set.lower(e);
        }

        @Override
        public @Nullable E floor(final E e) {
            return set.floor(e);
        }

        @Override
        public @Nullable E ceiling(final E e) {
            return set.ceiling(e);
        }

        @Override
        public @Nullable E higher(final E e) {
            return set.higher(e);
        }

        @Override
        public @Nullable E pollFirst() {
            return set.pollFirst();
        }

        @Override
        public @Nullable E pollLast() {
            return set.pollLast();
        }

        @Override
        public @Nullable Comparator<? super E> comparator() {
            return set.comparator();
        }

        @Override
        public E first() {
            return set.first();
        }

        @Override
        public E last() {
            return set.last();
        }

        @Override
        public NavigableSet<E> descendingSet() {
            return new WrappedNavigableSet<>(set.descendingSet(), breaks());
        }

        @Override
        public NavigableSet<E> subSet(final E fromElement, final boolean fromInclusive, final E toElement,
                                      final boolean toInclusive) {
            return new WrappedNavigableSet<>(set.subSet(fromElement, fromInclusive, toElement, toInclusive), breaks());
        }

        @Override
        public NavigableSet<E> headSet(final E toElement, final boolean inclusive) {
            return new WrappedNavigableSet<>(set.headSet(toElement, inclusive), breaks());
        }

        @Override
        public NavigableSet<E> tailSet(final E fromElement, final boolean inclusive) {
            return new WrappedNavigableSet<>(set.tailSet(fromElement, inclusive), breaks());
        }

        @Override
        public SortedSet<E> subSet(final E fromElement, final E toElement) {
            return subSet(fromElement, true, toElement, false);
        }

        @Override
        public SortedSet<E> headSet(final E toElement) {
            return headSet(toElement, false);
        }

        @Override
        public SortedSet<E> tailSet(final E fromElement) {
            return tailSet(fromElement, true);
        }

        @Override
        public NavigableSet<E> reversed() {
            return descendingSet();
        }
    }
}
