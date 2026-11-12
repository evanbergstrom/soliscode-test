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
package com.soliscode.test.contract.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;

import com.soliscode.test.provider.IteratorSource;

/// The checks of a weakly consistent iterator, shared by
/// [com.soliscode.test.contract.iterator.WeaklyConsistentIteratorContract] and the concurrent collections' contracts.
///
/// The `java.util.concurrent` package summary, "Weakly consistent": such iterators "may proceed concurrently with
/// other operations", "will never throw ConcurrentModificationException", and "are guaranteed to traverse elements as
/// they existed upon construction exactly once, and may (but are not guaranteed to) reflect any modifications
/// subsequent to construction". [#check] makes a source of unique elements, changes it during a traversal, other
/// than through the iterator ([Change]), and asserts, as the contracts read that text:
/// 1. no [ConcurrentModificationException] from `hasNext`, `next` or `forEachRemaining`;
/// 2. no element returned twice: the changes never add an element twice or re-add one removed, so a repeat is a
///    fault, an added element's included (stricter than "exactly once", which covers the elements at construction);
/// 3. every element present at construction and not removed since is returned (one removed or added during the
///    traversal may be returned or not);
/// 4. every element returned was present at construction or added since ("as they existed").
///
/// Order is not checked: weak consistency says nothing of it.
///
/// @since 0.1.3
public final class WeakConsistencyChecks {

    /// The most elements ({@value}) a traversal takes before it fails: an iterator that does not end must fail the
    /// test, not hang it.
    public static final int TRAVERSAL_LIMIT = IteratorContractSupport.TRAVERSAL_LIMIT;

    private WeakConsistencyChecks() { }

    /// The change made to the source during a traversal. Each is made on a new source.
    public enum Change {
        /// Two elements are taken; the first of them is removed from the source; the rest are taken with `hasNext`
        /// and `next`.
        RETURNED_ELEMENT_REMOVED("an element it had returned removed from the source"),

        /// Two elements are taken; one not returned yet is removed; the rest are taken with `hasNext` and `next`.
        UNRETURNED_ELEMENT_REMOVED("an element it had not returned removed from the source"),

        /// Two elements are taken; a new element is added; the rest are taken with `hasNext` and `next`. Where
        /// [#check] is asked to, one element is first removed, before the iterator is made, so that a bounded source
        /// built full has room.
        ELEMENT_ADDED("an element added to the source"),

        /// One element is taken; then `forEachRemaining`, whose action removes the first element taken when it is
        /// given the second.
        ///
        /// It removes the head on purpose: `ArrayBlockingQueue`'s `forEachRemaining` reads its array up to an index
        /// taken once, under its lock, so an interior removal by the action shifts the array under it and hands the
        /// action `null` or skips a slot. That is the class's own matter, not weak consistency's as these checks read
        /// it, and an interior removal is checked with `hasNext` and `next` only ([#UNRETURNED_ELEMENT_REMOVED]).
        CHANGED_DURING_FOR_EACH_REMAINING("the source changed by the action of forEachRemaining");

        private final String description;

        Change(final String descriptionArg) {
            this.description = descriptionArg;
        }

        /// Returns what the change is, for failure messages.
        ///
        /// @return the description
        public String description() {
            return description;
        }
    }

    /// A source that a check can change: the iterators under test over it, and its own removal and addition, other
    /// than through any iterator of it.
    ///
    /// @param <E> the element type
    public interface Source<E> {

        /// Returns a new iterator under test over the source.
        ///
        /// @return the iterator
        Iterator<E> iterator();

        /// Removes the element from the source, other than through an iterator of it.
        ///
        /// @param element an element the source holds
        /// @return whether it was removed
        boolean remove(E element);

        /// Adds the element to the source, other than through an iterator of it.
        ///
        /// @param element an element the source does not hold
        /// @return whether it was added
        boolean add(E element);
    }

    /// Returns the source an [IteratorSource] gives, changed through its [IteratorSource#removeFromSource] and
    /// [IteratorSource#addToSource].
    ///
    /// @param source the iterator source
    /// @param <E> the element type
    /// @return the source
    public static <E> Source<E> of(final IteratorSource<?, E> source) {
        Objects.requireNonNull(source);
        return of(source::iterator, source::removeFromSource, source::addToSource);
    }

    /// Returns a source of the given iterators, removal and addition.
    ///
    /// @param iterators makes a new iterator under test
    /// @param remove removes an element, and returns whether it did
    /// @param add adds an element, and returns whether it did
    /// @param <E> the element type
    /// @return the source
    public static <E> Source<E> of(final Supplier<? extends Iterator<E>> iterators, final Predicate<E> remove,
                                   final Predicate<E> add) {
        Objects.requireNonNull(iterators);
        Objects.requireNonNull(remove);
        Objects.requireNonNull(add);
        return new Source<>() {
            @Override
            public Iterator<E> iterator() {
                return iterators.get();
            }

            @Override
            public boolean remove(final E element) {
                return remove.test(element);
            }

            @Override
            public boolean add(final E element) {
                return add.test(element);
            }
        };
    }

    /// Makes the change during a traversal of a new iterator over the source, and asserts the four checks.
    ///
    /// @param source the source, which holds exactly `elements` and nothing else
    /// @param elements the elements it holds, unique, at least four
    /// @param extra an element it does not hold, for [Change#ELEMENT_ADDED]
    /// @param change the change
    /// @param removeBeforeAdd for [Change#ELEMENT_ADDED], whether to remove one element first, before the iterator is
    ///                        made, so that a bounded source built full has room
    /// @param what the iterator, for failure messages ("the queue's iterator()")
    /// @param <E> the element type
    /// @return `true` once the checks pass; `false`, checking nothing, if the source refused a removal or an addition
    /// @throws org.opentest4j.AssertionFailedError if a check fails
    public static <E> boolean check(final Source<E> source, final List<E> elements, final E extra,
                                    final Change change, final boolean removeBeforeAdd, final String what) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(change);
        if (elements.size() < 4) {
            throw new IllegalArgumentException("a check needs at least four elements, not " + elements.size());
        }
        List<E> held = new ArrayList<>(elements);
        if (change == Change.ELEMENT_ADDED && removeBeforeAdd && !source.remove(held.removeLast())) {
            return false;
        }
        String context = what + ", with " + change.description();
        List<E> returned = new ArrayList<>();
        Set<E> added = new HashSet<>();
        Iterator<E> iterator = source.iterator();
        try {
            if (change == Change.CHANGED_DURING_FOR_EACH_REMAINING) {
                if (!traverseWithForEachRemaining(source, iterator, returned, context)) {
                    return false;
                }
            } else {
                take(iterator, 2, returned, context);
                boolean changed = switch (change) {
                    case RETURNED_ELEMENT_REMOVED -> source.remove(returned.getFirst());
                    case UNRETURNED_ELEMENT_REMOVED -> source.remove(lastNotIn(held, returned));
                    case ELEMENT_ADDED -> source.add(extra) && added.add(extra);
                    case CHANGED_DURING_FOR_EACH_REMAINING -> throw new AssertionError("handled above");
                };
                if (!changed) {
                    return false;
                }
                while (iterator.hasNext()) {
                    Assertions.assertTrue(returned.size() < TRAVERSAL_LIMIT,
                            () -> context + ": the iterator did not end");
                    returned.add(iterator.next());
                }
            }
        } catch (final ConcurrentModificationException e) {
            Assertions.fail(context + ": it threw ConcurrentModificationException, which a weakly consistent iterator "
                    + "never throws", e);
        }
        assertWeaklyConsistent(held, change, added, returned, context);
        return true;
    }

    /// Runs every [Change] on the iterators of a collection, each on a new collection made from the elements, and
    /// asserts the checks of [#check]. A change runs only where the methods that make it are supported: the
    /// removals need `remove(Object)`, the addition `add`, and the change during `forEachRemaining` that method of the
    /// iterator too; the addition removes one element first where `remove(Object)` is supported, and is skipped where
    /// it is not and the collection is a full [BlockingQueue].
    ///
    /// @param make makes the collection from the elements it is to hold
    /// @param iterators the iterator under test of a collection (`iterator()`, `descendingIterator()`)
    /// @param canRemove whether `remove(Object)` is supported
    /// @param canAdd whether `add` is supported
    /// @param canForEachRemaining whether the iterator's `forEachRemaining` is supported
    /// @param elements the elements, unique, at least four
    /// @param extra an element not among them
    /// @param what the iterator, for failure messages
    /// @param <C> the collection type
    /// @param <E> the element type
    /// @throws org.opentest4j.AssertionFailedError if a check fails, or the collection refuses a supported change
    public static <C extends Collection<E>, E> void checkCollection(final Function<List<E>, C> make,
                                                                    final Function<C, Iterator<E>> iterators,
                                                                    final boolean canRemove, final boolean canAdd,
                                                                    final boolean canForEachRemaining,
                                                                    final List<E> elements, final E extra,
                                                                    final String what) {
        for (final Change change : Change.values()) {
            if ((change == Change.ELEMENT_ADDED ? !canAdd : !canRemove)
                    || (change == Change.CHANGED_DURING_FOR_EACH_REMAINING && !canForEachRemaining)) {
                continue;
            }
            C collection = make.apply(new ArrayList<>(elements));
            if (change == Change.ELEMENT_ADDED && !canRemove
                    && collection instanceof BlockingQueue<?> queue
                    && queue.remainingCapacity() == 0) {
                continue;
            }
            Source<E> source = of(() -> iterators.apply(collection), collection::remove, collection::add);
            Assertions.assertTrue(check(source, elements, extra, change, canRemove, what),
                    () -> what + ", with " + change.description() + ": the collection refused a remove(Object) or "
                            + "add it supports, of an element it held or did not hold");
        }
    }

    /// Runs every [Change] on the iterators of a map's view, each on a new map made from the entries, and asserts the
    /// checks of [#check]. The view's elements are made from the entries (a key, a value, an entry); the source is
    /// changed through the map, other than through the view: an element is removed with `remove(key)` of the key it
    /// was made from, and the extra one added with `put(extraKey, extraValue)`. A map is not bounded, so the addition
    /// removes nothing first. A change runs only where the methods that make it are supported: the removals need
    /// `remove(Object)`, the addition `put`.
    ///
    /// @param make makes the map from the entries it is to hold
    /// @param entries the entries: unique keys and unique values, at least four
    /// @param extraKey a key not among them
    /// @param extraValue a value not among them
    /// @param iterators the iterator under test of a map (`keySet().iterator()`, say)
    /// @param element makes the view's element of a key and its value
    /// @param canRemove whether `remove(Object)` is supported
    /// @param canPut whether `put` is supported
    /// @param what the iterator, for failure messages
    /// @param <M> the map type
    /// @param <K> the key type
    /// @param <V> the value type
    /// @param <T> the view's element type
    /// @throws org.opentest4j.AssertionFailedError if a check fails, or the map refuses a supported change
    public static <M extends Map<K, V>, K, V, T> void checkMapView(final Function<Map<K, V>, M> make,
                                                                   final Map<K, V> entries, final K extraKey,
                                                                   final V extraValue,
                                                                   final Function<M, Iterator<T>> iterators,
                                                                   final BiFunction<K, V, T> element,
                                                                   final boolean canRemove, final boolean canPut,
                                                                   final String what) {
        Map<T, K> keyOf = new HashMap<>();
        List<T> elements = new ArrayList<>();
        for (final Map.Entry<K, V> e : entries.entrySet()) {
            T t = element.apply(e.getKey(), e.getValue());
            elements.add(t);
            keyOf.put(t, e.getKey());
        }
        T extra = element.apply(extraKey, extraValue);
        for (final Change change : Change.values()) {
            if (change == Change.ELEMENT_ADDED ? !canPut : !canRemove) {
                continue;
            }
            M map = make.apply(new LinkedHashMap<>(entries));
            Source<T> source = of(() -> iterators.apply(map), t -> {
                K key = keyOf.get(t);
                return key != null && map.remove(key) != null;
            }, t -> t.equals(extra) && map.put(extraKey, extraValue) == null);
            Assertions.assertTrue(check(source, elements, extra, change, false, what),
                    () -> what + ", with " + change.description() + ": the map refused a remove(Object) or put it "
                            + "supports, of a key it held or did not hold");
        }
    }

    /// Takes one element, then the rest with `forEachRemaining`, whose action removes the first element taken when
    /// it is given the second.
    ///
    /// @return whether the source removed it
    private static <E> boolean traverseWithForEachRemaining(final Source<E> source, final Iterator<E> iterator,
                                                            final List<E> returned, final String context) {
        take(iterator, 1, returned, context);
        boolean[] refused = {false};
        iterator.forEachRemaining(e -> {
            Assertions.assertTrue(returned.size() < TRAVERSAL_LIMIT, () -> context + ": the iterator did not end");
            returned.add(e);
            if (returned.size() == 2 && !source.remove(returned.getFirst())) {
                refused[0] = true;
            }
        });
        return !refused[0];
    }

    private static <E> void take(final Iterator<E> iterator, final int n, final List<E> returned,
                                 final String context) {
        for (int i = 0; i < n; i++) {
            Assertions.assertTrue(iterator.hasNext(), () -> context + ": hasNext() was false before the change, with "
                    + returned.size() + " elements returned");
            returned.add(iterator.next());
        }
    }

    /// Returns the last of the elements that is not among those returned.
    private static <E> E lastNotIn(final List<E> elements, final List<E> returned) {
        for (int i = elements.size() - 1; i >= 0; i--) {
            if (!returned.contains(elements.get(i))) {
                return elements.get(i);
            }
        }
        throw new AssertionError("every element was returned before the change");
    }

    /// Asserts checks 2 to 4 on what the traversal returned.
    ///
    /// @param held the elements the source held when the iterator was made
    /// @param change the change, which says which element was removed
    /// @param added the elements added during the traversal
    /// @param returned what the iterator returned, in order
    /// @param context the iterator and the change, for failure messages
    private static <E> void assertWeaklyConsistent(final List<E> held, final Change change, final Set<E> added,
                                                   final List<E> returned, final String context) {
        Map<E, Integer> counts = new HashMap<>();
        for (final E e : returned) {
            counts.merge(e, 1, Integer::sum);
        }
        for (final Map.Entry<E, Integer> count : counts.entrySet()) {
            Assertions.assertEquals(1, count.getValue(), () -> context + ": it returned " + count.getKey() + " "
                    + count.getValue() + " times, where a weakly consistent iterator returns each element once; "
                    + "returned " + returned);
        }
        Set<E> removed = removedBy(change, held, returned);
        for (final E e : held) {
            if (!removed.contains(e)) {
                Assertions.assertTrue(counts.containsKey(e), () -> context + ": it did not return " + e + ", which "
                        + "the source held throughout; returned " + returned);
            }
        }
        for (final E e : returned) {
            Assertions.assertTrue(held.contains(e) || added.contains(e), () -> context + ": it returned " + e
                    + ", which the source never held; returned " + returned);
        }
    }

    /// Returns the element the change removed, if it removed one: the first returned for
    /// [Change#RETURNED_ELEMENT_REMOVED] and [Change#CHANGED_DURING_FOR_EACH_REMAINING]; for
    /// [Change#UNRETURNED_ELEMENT_REMOVED], the last of `held` not among the two returned before the change.
    private static <E> Set<E> removedBy(final Change change, final List<E> held, final List<E> returned) {
        return switch (change) {
            case RETURNED_ELEMENT_REMOVED, CHANGED_DURING_FOR_EACH_REMAINING ->
                    Collections.singleton(returned.getFirst());
            case UNRETURNED_ELEMENT_REMOVED -> Collections.singleton(lastNotIn(held, returned.subList(0, 2)));
            case ELEMENT_ADDED -> Collections.emptySet();
        };
    }
}
