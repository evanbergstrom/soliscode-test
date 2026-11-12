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
package com.soliscode.test.breakable;

import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import org.jspecify.annotations.Nullable;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.MethodStatus;
import com.soliscode.test.contract.iterable.IterableMethods;

/// An iterator that can be broken in well-defined ways in order to test collection utilities or testing classes.
///
/// # Breaks
/// The breaks that are supported for this class are listed in the description of the method that they impact.
///
/// Any breaks that are not listed in this class can be added to an instance of `BreakableIterator`, but will
/// not have any impact on how it functions.
///
/// @param <E> the element type.
/// @author evanbergstrom
/// @since 1.0
/// @see Iterator
public class BreakableIterator<E> extends AbstractBreakable implements Iterator<E> {

    /// The iterator will always have no elements: the constructor replaces the underlying iterator with an empty one.
    /// **Affected Methods:** [hasNext()][#hasNext()], [next()][#next()],
    /// [forEachRemaining(Consumer)][#forEachRemaining(Consumer)]
    public static final Break ITERATOR_IS_ALWAYS_EMPTY =
            new Break("iterator has no elements");

    /// The iterator will skip the first element: the constructor advances the underlying iterator past it.
    /// **Affected Methods:** [hasNext()][#hasNext()], [next()][#next()],
    /// [forEachRemaining(Consumer)][#forEachRemaining(Consumer)]
    public static final Break ITERATOR_SKIPS_FIRST_ELEMENT =
            new Break("iterator skips the first element.");

    /// The iterator `hasNext` method will always return a true.
    /// **Affected Methods:** [hasNext()][#hasNext()]
    /// @see BreakableIterator#hasNext
    public static final Break ITERATOR_HAS_NEXT_ALWAYS_RETURNS_TRUE =
            new Break("iterator hasNext always returns true.");

    /// The iterator `hasNext` method will always return a false.
    /// **Affected Methods:** [hasNext()][#hasNext()]
    /// @see BreakableIterator#hasNext
    public static final Break ITERATOR_HAS_NEXT_ALWAYS_RETURNS_FALSE =
            new Break("iterator hasNext always returns false.");

    /// The iterator `hasNext` method will always return the opposite value
    /// **Affected Methods:** [hasNext()][#hasNext()]
    /// @see BreakableIterator#hasNext
    public static final Break ITERATOR_HAS_NEXT_RETURNS_OPPOSITE_VALUE =
            new Break("iterator hasNext always returns opposite value");

    /// **Purpose:** `hasNext` advances the iterator, which [Iterator#hasNext] must not do.
    ///
    /// **Effect:** `hasNext` takes the next element and discards it, and returns whether there was one; so a `next`
    /// after it returns the element after that, or throws.
    ///
    /// **Affected Methods:** [hasNext()][#hasNext()]
    /// @since 0.1.3
    public static final Break ITERATOR_HAS_NEXT_ADVANCES =
            new Break("iterator hasNext advances the iterator");

    /// **Purpose:** `next` works only after `hasNext`, a fault of iterators that compute the next element in
    /// `hasNext`; [Iterator#next] needs no call to `hasNext` first.
    ///
    /// **Effect:** `next` throws [NoSuchElementException] unless `hasNext` has been called since the last `next`
    /// (or since the iterator was made).
    ///
    /// **Affected Methods:** [next()][#next()]
    /// @since 0.1.3
    public static final Break ITERATOR_NEXT_REQUIRES_HAS_NEXT =
            new Break("iterator next requires a call to hasNext first");

    /// The iterator `next` method will always return `null`
    /// **Affected Methods:** [next()][#next()]
    /// @see BreakableIterator#next
    public static final Break ITERATOR_NEXT_ALWAYS_RETURNS_NULL =
            new Break("iterator next method returns null");

    /// The iterator `next` method throws the wrong exception (`RuntimeException`, not `NoSuchElementException`) if
    /// there are no more elements.
    /// **Affected Methods:** [next()][#next()]
    /// @see BreakableIterator#next
    public static final Break ITERATOR_NEXT_THROWS_WRONG_EXCEPTION =
            new Break("iterator next method throws wrong exception");

    /// The iterator 'remove' method does not remove the element at the current iterator position.
    /// **Affected Methods:** [remove()][#remove()]
    /// @see BreakableIterator#remove
    public static final Break ITERATOR_REMOVE_DOES_NOT_REMOVE_ELEMENT =
            new Break("iterator remove does not remove elements");

    /// The iterator 'remove' method throws the wrong exception for an illegal state
    /// **Affected Methods:** [remove()][#remove()]
    /// @see BreakableIterator#remove
    public static final Break ITERATOR_REMOVE_THROWS_WRONG_EXCEPTION_FOR_ILLEGAL_STATE =
            new Break("iterator remove throws wrong exception for illegal state");

    /// The iterator 'remove' method throws the wrong exception if it is not supported.
    /// **Affected Methods:** [remove()][#remove()]
    /// @see BreakableIterator#remove
    public static final Break ITERATOR_REMOVE_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED =
            new Break("iterator remove method throws wrong exception if not supported.");

    /// **Purpose:** `remove` called in an illegal state does nothing, where [Iterator#remove] throws
    /// [IllegalStateException].
    ///
    /// **Effect:** `remove` before `next`, or a second `remove` after one `next`, returns without removing anything
    /// or throwing.
    ///
    /// **Affected Methods:** [remove()][#remove()]
    /// @since 0.1.3
    public static final Break ITERATOR_REMOVE_IGNORES_ILLEGAL_STATE =
            new Break("iterator remove ignores an illegal state");

    /// **Purpose:** `forEachRemaining` swallows an exception from its action, where [Iterator#forEachRemaining]
    /// relays it to the caller.
    ///
    /// **Effect:** a [RuntimeException] the action throws for an element is caught, and the iteration goes on with
    /// the next element.
    ///
    /// **Affected Methods:** [forEachRemaining(Consumer)][#forEachRemaining(Consumer)]
    /// @since 0.1.3
    public static final Break ITERATOR_FOR_EACH_REMAINING_SWALLOWS_ACTION_EXCEPTION =
            new Break("iterator forEachRemaining swallows an exception from its action");

    /// The iterator 'forEachRemaining' does not call the action
    /// **Affected Methods:** [forEachRemaining(Consumer)][#forEachRemaining(Consumer)]
    /// @see BreakableIterator#forEachRemaining(Consumer)
    public static final Break ITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION =
            new Break("iterator forEachRemaining does not call action");

    /// The iterator 'forEachRemaining' throws the wrong exception for a `null` argument.
    /// **Affected Methods:** [forEachRemaining(Consumer)][#forEachRemaining(Consumer)]
    /// @see BreakableIterator#forEachRemaining(Consumer)
    public static final Break ITERATOR_FOR_EACH_REMAINING_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT =
            new Break("iterator forEachRemaining throws wrong exception for null argument.");

    /// The iterator 'forEachRemaining' throws the wrong exception if it is not supported.
    /// **Affected Methods:** [forEachRemaining(Consumer)][#forEachRemaining(Consumer)]
    /// @see BreakableIterator#forEachRemaining(Consumer)
    public static final Break ITERATOR_FOR_EACH_REMAINING_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED =
            new Break("iterator forEachRemaining throws wrong exception if not supported.");

    /// **Purpose:** the iterator is fail-fast, as `ArrayList`'s is, where a weakly consistent one (the
    /// `java.util.concurrent` package summary) "will never throw ConcurrentModificationException".
    ///
    /// **Effect:** once the iterator sees a change to its source made other than through it, `next()`, `remove()` and
    /// `forEachRemaining` throw [ConcurrentModificationException]. `hasNext()` does not throw.
    ///
    /// A change is seen only by an iterator made with a probe of its source's size
    /// ([#BreakableIterator(Iterator, Set, Map, int, boolean, IntSupplier)]): `hasNext()` and `next()` read it on
    /// entry, and `remove()` too under this break, and a size other than the one the iterator expects (the size at
    /// its creation, less one for each removal through it) is a change. Without a probe, the break has no effect; a
    /// [BreakableListIterator] has none.
    ///
    /// **Affected Methods:** [next()][#next()], [remove()][#remove()],
    /// [forEachRemaining(Consumer)][#forEachRemaining(Consumer)]
    /// @since 0.1.3
    public static final Break ITERATOR_IS_FAIL_FAST =
            new Break("iterator is fail-fast: it throws ConcurrentModificationException after a change to its source");

    /// **Purpose:** the iterator returns an element twice after a change to its source, where a weakly consistent one
    /// traverses each element "exactly once".
    ///
    /// **Effect:** once, at the first change it sees (as [#ITERATOR_IS_FAIL_FAST] says), the next `next()` returns the
    /// element the last `next()` returned again, without advancing, and `hasNext()` returns `true` until it has. No
    /// effect if no element has been returned yet, or without a probe of the source's size.
    ///
    /// **Affected Methods:** [hasNext()][#hasNext()], [next()][#next()],
    /// [forEachRemaining(Consumer)][#forEachRemaining(Consumer)]
    /// @since 0.1.3
    public static final Break ITERATOR_RETURNS_AN_ELEMENT_TWICE_AFTER_A_CHANGE =
            new Break("iterator returns an element twice after a change to its source");

    /// **Purpose:** the iterator skips an element after a change to its source, where a weakly consistent one
    /// traverses every element that existed at its construction (and was not removed since).
    ///
    /// **Effect:** once, at the first change it sees (as [#ITERATOR_IS_FAIL_FAST] says), before it does anything else,
    /// the iterator discards the next element of the underlying iterator, if there is one; so `hasNext()` stays
    /// truthful. No effect without a probe of the source's size.
    ///
    /// **Affected Methods:** [hasNext()][#hasNext()], [next()][#next()],
    /// [forEachRemaining(Consumer)][#forEachRemaining(Consumer)]
    /// @since 0.1.3
    public static final Break ITERATOR_SKIPS_AN_ELEMENT_AFTER_A_CHANGE =
            new Break("iterator skips an element after a change to its source");

    /// The underlying iterator.
    private final Iterator<E> iterator;

    /// The probe of the source's size, or `null` where there is none or no weak-consistency break is set.
    private final @Nullable IntSupplier sourceSize;

    /// The size the source should have if nothing but this iterator changed it.
    private int expectedSize;

    /// Whether a change to the source, other than through this iterator, has been seen.
    private boolean changeSeen;

    /// Whether the next `next()` returns [#lastReturned] again, for
    /// [#ITERATOR_RETURNS_AN_ELEMENT_TWICE_AFTER_A_CHANGE] only.
    private boolean repeatPending;

    /// Whether `next()` has returned an element.
    private boolean hasReturned;

    /// The element `next()` returned last.
    private @Nullable E lastReturned;

    /// Whether `hasNext` has been called since the last `next`, for [#ITERATOR_NEXT_REQUIRES_HAS_NEXT] only.
    private boolean hasNextCalled;


    /// Creates a `BreakableIterator` from an existing iterator and specifying the breaks and method statuses.
    ///
    /// Under [#ITERATOR_IS_ALWAYS_EMPTY], a [ListIterator] is replaced by an empty list iterator, so that a
    /// [BreakableListIterator] still has one.
    ///
    /// # Breaks
    /// This constructor is affected by the following breaks:
    /// - [ITERATOR_IS_ALWAYS_EMPTY][BreakableIterator#ITERATOR_IS_ALWAYS_EMPTY]
    /// - [ITERATOR_SKIPS_FIRST_ELEMENT][BreakableIterator#ITERATOR_SKIPS_FIRST_ELEMENT]
    ///
    /// @param iteratorArg       the underlying iterator.
    /// @param breaks         the set of breaks to apply.
    /// @param methodStatuses the status of each method in the interface.
    /// @param characteristics the characteristics of the iterator.
    /// @param isSafe          whether the iterator is safe for concurrent access.
    public BreakableIterator(final Iterator<E> iteratorArg, final Set<Break> breaks,
                             final Map<InterfaceMethod, MethodStatus> methodStatuses,
                             final int characteristics, final boolean isSafe) {
        this(iteratorArg, breaks, methodStatuses, characteristics, isSafe, null);
    }

    /// Creates a `BreakableIterator` as [#BreakableIterator(Iterator, Set, Map, int, boolean)] does, with a probe of
    /// its source's size, through which [#ITERATOR_IS_FAIL_FAST], [#ITERATOR_RETURNS_AN_ELEMENT_TWICE_AFTER_A_CHANGE]
    /// and [#ITERATOR_SKIPS_AN_ELEMENT_AFTER_A_CHANGE] see a change to the source made other than through the
    /// iterator.
    ///
    /// The probe is read only where one of those breaks is set: once here, and on entry to `hasNext()` and `next()`,
    /// and to `remove()` under [#ITERATOR_IS_FAIL_FAST], so that `remove()` never applies the other two.
    ///
    /// @param iteratorArg     the underlying iterator.
    /// @param breaks          the set of breaks to apply.
    /// @param methodStatuses  the status of each method in the interface.
    /// @param characteristics the characteristics of the iterator.
    /// @param isSafe          whether the iterator is safe for concurrent access.
    /// @param sourceSizeArg   the probe of the source's size, or `null` for none.
    /// @since 0.1.3
    public BreakableIterator(final Iterator<E> iteratorArg, final Set<Break> breaks,
                             final Map<InterfaceMethod, MethodStatus> methodStatuses,
                             final int characteristics, final boolean isSafe,
                             final @Nullable IntSupplier sourceSizeArg) {
        super(breaks, methodStatuses, isSafe);
        this.sourceSize = sourceSizeArg != null && hasWeakConsistencyBreak(breaks) ? sourceSizeArg : null;
        this.expectedSize = this.sourceSize == null ? 0 : this.sourceSize.getAsInt();
        if (hasBreak(ITERATOR_IS_ALWAYS_EMPTY)) {
            this.iterator = iteratorArg instanceof ListIterator<E>
                    ? Collections.emptyListIterator() : Collections.emptyIterator();
        } else {
            this.iterator = iteratorArg;
            if (hasBreak(ITERATOR_SKIPS_FIRST_ELEMENT) && this.iterator.hasNext()) {
                this.iterator.next();
            }
        }
    }

    /// Implements the [hasNext][Iterator#hasNext] method from the [Iterator] interface.
    ///
    /// # Breaks
    /// This method can be broken using the following collection breaks:
    /// - [ITERATOR_IS_ALWAYS_EMPTY][BreakableIterator#ITERATOR_IS_ALWAYS_EMPTY]
    /// - [ITERATOR_HAS_NEXT_ALWAYS_RETURNS_TRUE][BreakableIterator#ITERATOR_HAS_NEXT_ALWAYS_RETURNS_TRUE]
    /// - [ITERATOR_HAS_NEXT_ALWAYS_RETURNS_FALSE][BreakableIterator#ITERATOR_HAS_NEXT_ALWAYS_RETURNS_FALSE]
    /// - [ITERATOR_HAS_NEXT_RETURNS_OPPOSITE_VALUE][BreakableIterator#ITERATOR_HAS_NEXT_RETURNS_OPPOSITE_VALUE]
    /// - [#ITERATOR_HAS_NEXT_ADVANCES]
    /// - [#ITERATOR_RETURNS_AN_ELEMENT_TWICE_AFTER_A_CHANGE], [#ITERATOR_SKIPS_AN_ELEMENT_AFTER_A_CHANGE]
    ///
    /// # Example
    /// An iterator that has any of these breaks can be obtained from a `BreakableIterable` built with them:
    /// ```java
    ///     Iterator<Integer> iterator = Breakables.buildIterable(1,2,3,4,5)
    ///         .addBreak(ITERATOR_HAS_NEXT_ALWAYS_RETURNS_TRUE)
    ///         .build().iterator();
    /// ```
    /// @return 'true' or 'false' depending on the state of the iterator and the breaks.
    /// @see Iterator#hasNext
    @Override
    public boolean hasNext() {
        checkForChange();
        if (repeatPending) {
            return true;
        }
        hasNextCalled = true;
        if (hasBreak(ITERATOR_HAS_NEXT_ALWAYS_RETURNS_TRUE)) {
            return true;
        } else if (hasBreak(ITERATOR_HAS_NEXT_ADVANCES)) {
            if (!iterator.hasNext()) {
                return false;
            }
            iterator.next();
            return true;
        } else if (hasBreak(ITERATOR_HAS_NEXT_ALWAYS_RETURNS_FALSE)) {
            return false;
        } else if (hasBreak(ITERATOR_HAS_NEXT_RETURNS_OPPOSITE_VALUE)) {
            return !iterator.hasNext();
        } else {
            return iterator.hasNext();
        }
    }

    /// Implements the [next][Iterator#next] method from the [Iterator] interface.
    ///
    /// # Breaks
    /// This method can be broken using the following collection breaks:
    /// - [ITERATOR_NEXT_ALWAYS_RETURNS_NULL][BreakableIterator#ITERATOR_NEXT_ALWAYS_RETURNS_NULL]
    /// - [ITERATOR_NEXT_THROWS_WRONG_EXCEPTION][BreakableIterator#ITERATOR_NEXT_THROWS_WRONG_EXCEPTION]
    /// - [#ITERATOR_NEXT_REQUIRES_HAS_NEXT]
    /// - [#ITERATOR_IS_FAIL_FAST], [#ITERATOR_RETURNS_AN_ELEMENT_TWICE_AFTER_A_CHANGE],
    ///   [#ITERATOR_SKIPS_AN_ELEMENT_AFTER_A_CHANGE]
    ///
    /// An iterator that has any of these breaks can be obtained from a `BreakableIterable` built with them:
    /// ```java
    ///     Iterator<Integer> iterator = Breakables.buildIterable(1,2,3,4,5)
    ///         .addBreak(ITERATOR_NEXT_ALWAYS_RETURNS_NULL)
    ///         .build().iterator();
    /// ```
    /// @return The next element or `null` if there are no more elements or if the iterator is broken.
    /// @throws NoSuchElementException if the iteration has no more elements
    /// @see Iterator#next
    @Override
    public @Nullable E next() {
        checkForChange();
        if (changeSeen && hasBreak(ITERATOR_IS_FAIL_FAST)) {
            throw new ConcurrentModificationException("the source was changed other than through the iterator");
        }
        boolean afterHasNext = hasNextCalled;
        hasNextCalled = false;
        if (repeatPending) {
            repeatPending = false;
            return lastReturned;
        }
        if (hasBreak(ITERATOR_NEXT_REQUIRES_HAS_NEXT) && !afterHasNext) {
            throw new NoSuchElementException("hasNext() was not called first");
        }
        try {
            E element = iterator.next();
            hasReturned = true;
            lastReturned = element;
            if (hasBreak(ITERATOR_NEXT_ALWAYS_RETURNS_NULL)) {
                return null;
            } else {
                return element;
            }
        } catch (final NoSuchElementException e) {
            if (hasBreak(ITERATOR_NEXT_THROWS_WRONG_EXCEPTION)) {
                throw new RuntimeException();
            } else {
                throw e;
            }
        }
    }

    /// Implements the optional [remove][Iterator#remove] method from the [Iterator] interface.
    ///
    /// # Breaks
    /// This method can be broken using the following collection breaks:
    /// - [ITERATOR_REMOVE_DOES_NOT_REMOVE_ELEMENT][BreakableIterator#ITERATOR_REMOVE_DOES_NOT_REMOVE_ELEMENT]
    /// - [#ITERATOR_REMOVE_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED]
    /// - [#ITERATOR_REMOVE_THROWS_WRONG_EXCEPTION_FOR_ILLEGAL_STATE]
    /// - [#ITERATOR_REMOVE_IGNORES_ILLEGAL_STATE]
    /// - [#ITERATOR_IS_FAIL_FAST]
    ///
    /// An iterator that has any of these breaks can be obtained from a `BreakableIterable` built with them:
    /// ```java
    ///     Iterator<Integer> iterator = Breakables.buildIterable(1,2,3,4,5)
    ///         .addBreak(ITERATOR_REMOVE_DOES_NOT_REMOVE_ELEMENT)
    ///         .build().iterator();
    /// ```
    /// @throws UnsupportedOperationException if this iterator does not support the 'remove' operation
    /// @throws IllegalStateException if the `next` method has not yet been called, or the `remove` method has already
    ///                               been called after the last call to the `next` method.
    /// @see Iterator#remove
    @Override
    public void remove() {
        checkOptionalMethodSupport(IterableMethods.ITERATOR_REMOVE,
                ITERATOR_REMOVE_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED);

        if (hasBreak(ITERATOR_IS_FAIL_FAST)) {
            checkForChange();
            if (changeSeen) {
                throw new ConcurrentModificationException("the source was changed other than through the iterator");
            }
        }
        if (hasBreak(ITERATOR_REMOVE_DOES_NOT_REMOVE_ELEMENT)) {
            return;
        }

        try {
            iterator.remove();
            expectedSize--;
        } catch (final IllegalStateException e) {
            if (hasBreak(ITERATOR_REMOVE_THROWS_WRONG_EXCEPTION_FOR_ILLEGAL_STATE)) {
                throw wrongException(e);
            } else if (!hasBreak(ITERATOR_REMOVE_IGNORES_ILLEGAL_STATE)) {
                throw e;
            }
        }
    }

    /// Implements the [forEachRemaining][Iterator#forEachRemaining] method from the [Iterator] interface.
    ///
    /// # Breaks
    /// This method can be broken using the following iterable breaks:
    /// - [#ITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION]
    /// - [#ITERATOR_FOR_EACH_REMAINING_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT]
    /// - [#ITERATOR_FOR_EACH_REMAINING_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED]
    /// - [#ITERATOR_FOR_EACH_REMAINING_SWALLOWS_ACTION_EXCEPTION]
    /// - [#ITERATOR_IS_FAIL_FAST], [#ITERATOR_RETURNS_AN_ELEMENT_TWICE_AFTER_A_CHANGE],
    ///   [#ITERATOR_SKIPS_AN_ELEMENT_AFTER_A_CHANGE]: with one of them and a probe, `forEachRemaining` runs as
    ///   `while (hasNext()) action.accept(next())`, so that a change its action makes is seen, and the other
    ///   `forEachRemaining` breaks do not apply.
    ///
    /// An iterator that has any of these breaks can be obtained from a `BreakableIterable` built with them:
    /// ```java
    ///     Iterator<Integer> iterator = Breakables.buildIterable(1,2,3,4,5)
    ///         .addBreak(ITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION)
    ///         .build().iterator();
    /// ```
    /// @param action The action to run on the remaining elements.
    /// @throws NullPointerException if the specified action is null
    @Override
    public void forEachRemaining(final Consumer<? super E> action) {
        checkOptionalMethodSupport(IterableMethods.ITERATOR_FOR_EACH_REMAINING,
                ITERATOR_FOR_EACH_REMAINING_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED);

        if (action == null) {
            if (hasBreak(ITERATOR_FOR_EACH_REMAINING_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT)) {
                throw new RuntimeException();
            }
            throw new NullPointerException();
        }
        if (sourceSize != null) {
            // A weak-consistency break: element by element, so that a change the action makes is seen.
            while (hasNext()) {
                action.accept(next());
            }
            return;
        }
        if (hasBreak(ITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION)) {
            return;
        }
        if (hasBreak(ITERATOR_FOR_EACH_REMAINING_SWALLOWS_ACTION_EXCEPTION)) {
            iterator.forEachRemaining(e -> {
                try {
                    action.accept(e);
                } catch (final RuntimeException ignored) {
                    // The break: the exception is not relayed.
                }
            });
            return;
        }
        iterator.forEachRemaining(action);
    }

    /// Returns whether the breaks hold one of the three that act on a change to the source:
    /// [#ITERATOR_IS_FAIL_FAST], [#ITERATOR_RETURNS_AN_ELEMENT_TWICE_AFTER_A_CHANGE] and
    /// [#ITERATOR_SKIPS_AN_ELEMENT_AFTER_A_CHANGE]. A breakable passes a probe of its source's size only where they do.
    ///
    /// @param breaks the breaks
    /// @return whether one of them is set
    static boolean hasWeakConsistencyBreak(final Set<Break> breaks) {
        return breaks.contains(ITERATOR_IS_FAIL_FAST)
                || breaks.contains(ITERATOR_RETURNS_AN_ELEMENT_TWICE_AFTER_A_CHANGE)
                || breaks.contains(ITERATOR_SKIPS_AN_ELEMENT_AFTER_A_CHANGE);
    }

    /// Returns a new iterator over `iteratorArg` that applies only the three weak-consistency breaks among `breaks`,
    /// with the probe, or `iteratorArg` itself where none of them is set. A breakable whose iterator does not come
    /// from [BreakableIterable#iterator()] (a deque's descending iterator, a map's views) wraps it so, and no other
    /// iterator break reaches it.
    ///
    /// @param iteratorArg the iterator to wrap
    /// @param breaks the breakable's breaks
    /// @param sourceSizeArg the probe of the source's size
    /// @param <T> the element type
    /// @return the wrapped iterator, or `iteratorArg`
    static <T> Iterator<T> withWeakConsistencyBreaks(final Iterator<T> iteratorArg, final Set<Break> breaks,
                                                     final IntSupplier sourceSizeArg) {
        if (!hasWeakConsistencyBreak(breaks)) {
            return iteratorArg;
        }
        Set<Break> weak = new HashSet<>();
        for (final Break b : List.of(ITERATOR_IS_FAIL_FAST, ITERATOR_RETURNS_AN_ELEMENT_TWICE_AFTER_A_CHANGE,
                ITERATOR_SKIPS_AN_ELEMENT_AFTER_A_CHANGE)) {
            if (breaks.contains(b)) {
                weak.add(b);
            }
        }
        return new BreakableIterator<>(iteratorArg, weak, Map.of(), 0, false, sourceSizeArg);
    }

    /// Reads the probe, if there is one, and acts on the first change it shows: marks it seen, and applies
    /// [#ITERATOR_RETURNS_AN_ELEMENT_TWICE_AFTER_A_CHANGE] or [#ITERATOR_SKIPS_AN_ELEMENT_AFTER_A_CHANGE].
    private void checkForChange() {
        if (sourceSize == null || changeSeen || sourceSize.getAsInt() == expectedSize) {
            return;
        }
        changeSeen = true;
        if (hasBreak(ITERATOR_RETURNS_AN_ELEMENT_TWICE_AFTER_A_CHANGE) && hasReturned) {
            repeatPending = true;
        }
        if (hasBreak(ITERATOR_SKIPS_AN_ELEMENT_AFTER_A_CHANGE) && iterator.hasNext()) {
            iterator.next();
        }
    }

    /// Returns the iterator this one delegates to: the one it was made with, or the empty one
    /// [#ITERATOR_IS_ALWAYS_EMPTY] puts in its place.
    ///
    /// @return the underlying iterator
    /// @since 0.1.3
    protected Iterator<E> delegate() {
        return iterator;
    }
}
