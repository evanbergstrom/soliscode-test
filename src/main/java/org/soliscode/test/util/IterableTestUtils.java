package org.soliscode.test.util;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Assertions;
import org.soliscode.test.interfaces.IterableOnly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.soliscode.test.breakable.Breakables.ensureUnbroken;

/// This class consists exclusively of static methods that operate on or return iterables.
///
/// The methods of this class all throw a `NullPointerException` if the iterables or class objects provided to
/// them are `null`.
///
/// @author evanbergstrom
/// @since 1.0.0
public final class IterableTestUtils {

    private IterableTestUtils() { }

    /// Returns `true` if the iterable contains no elements.
    ///
    /// @param i The iterable.
    /// @return `true` if the iterable contains no elements, `false` otherwise.
    public static boolean isEmpty(final @NonNull Iterable<?> i) {
        if (i instanceof Collection<?> c) {
            return ensureUnbroken(c).isEmpty();
        } else {
            return !ensureUnbroken(i).iterator().hasNext();
        }
    }

    /// Returns the number of elements in this iterable.  If this iterable contains more than `Integer.MAX_VALUE`
    /// elements, returns`Integer.MAX_VALUE`.
    ///
    /// @param i The iterable.
    /// @return the number of elements in this iterable.
    public static int size(final @NonNull Iterable<?> i) {
        if (i instanceof Collection<?> c) {
            return ensureUnbroken(c).size();
        } else {
            int n = 0;
            for (Object o : ensureUnbroken(i)) {
                n++;
            }
            return n;
        }
    }

    /// Returns `true` if this iterable contains the specified element. Returns `true` if and only if this
    /// iterable contains at least one element `e` such that `Objects.equals(o, e)`.
    ///
    /// @param i The iterable.
    /// @param o element whose presence in this iterable is to be tested
    /// @return {@code true} if this iterable contains the specified element, `false` otherwise.
    /// @throws ClassCastException if the type of the specified element is incompatible with this collection
    /// @throws NullPointerException if the specified element is null and this iterable does not permit null elements
    public static boolean contains(final @NonNull Iterable<?> i, final Object o) {
        if (i instanceof Collection<?> c) {
            return ensureUnbroken(c).contains(o);
        } else {
            for (Object e : ensureUnbroken(i)) {
                if (e.equals(o)) {
                    return true;
                }
            }
            return false;
        }
    }

    /// Returns `true` if this iterable contains the specified element. Returns `true` if and only if this
    /// iterable contains this element by identity.
    ///
    /// @param i The iterable.
    /// @param o element whose presence in this iterable is to be tested
    /// @return `true` if this iterable contains the specified element, `false` otherwise.
    /// @throws ClassCastException if the type of the specified element is incompatible with this collection
    /// @throws NullPointerException if the specified element is null and this iterable does not permit null elements
    public static boolean containsByIdentity(final @NonNull Iterable<?> i, final Object o) {
        for (Object e : ensureUnbroken(i)) {
            if (e == o) {
                return true;
            }
        }
        return false;
    }

    /// Returns an empty iterable.
    /// @param <E> the element type.
    /// @return an empty iterable.
    public static <E> Iterable<E> empty() {
        return new EmptyIterable<>();
    }


    /// Compare the elements in two iterables for equality.
    /// @param iterable1 The first iterable to compare for equality.
    /// @param iterable2 The second iterable to compare for equality.
    /// @return `true` if the iterables contain elements that are equal at each position.
    public static boolean equals(final @NonNull Iterable<?> iterable1, final @NonNull Iterable<?> iterable2) {
        Iterator<?> i1 = ensureUnbroken(iterable1).iterator();
        Iterator<?> i2 = ensureUnbroken(iterable2).iterator();
        while (i1.hasNext() && i2.hasNext()) {
            if (!i1.next().equals(i2.next())) {
                return false;
            }
        }
        return !(i1.hasNext() || i2.hasNext());
    }

    private static final class EmptyIterable<E> implements Iterable<E> {
        @Override
        @NonNull
        public Iterator<E> iterator() {
            return Collections.emptyIterator();
        }
    }

    /// Converts an array of elements to an Iterable for use in assertions.
    /// This method wraps the array elements in an IterableOnly instance, which restricts
    /// access to only the Iterable interface methods. This is useful for testing code
    /// that should only depend on the Iterable interface.
    ///
    /// @param <E> the type of elements in the array
    /// @param elements the array of elements to convert to an iterable
    /// @return an Iterable containing the elements from the array
    /// @throws org.opentest4j.AssertionFailedError if elements is null
    /// @see IterableOnly
    /// @see Arrays#stream(Object[])
    public static <E> Iterable<E> asIterable(final E[] elements) {
        Assertions.assertNotNull(elements);
        return new IterableOnly<>(Arrays.stream(elements).collect(Collectors.toList()));
    }

    /// Converts the iterable to a collection.
    /// @param <E> the type of the element.
    /// @param i the iterable to convert.
    /// @return a collection with the same elements as the iterable.
    public static <E> @NonNull Collection<E> asCollection(final @NonNull Iterable<E> i) {
        Collection<E> s = new ArrayList<>();
        ensureUnbroken(i).forEach(s::add);
        return s;
    }

    /// Converts the iterable to a list. This method will create a new list as the return value, even if the argument is
    /// already a instance of [List].
    /// @param <E> the type of the element.
    /// @param i the iterable to convert.
    /// @return a list with the same elements as the iterable.
    public static <E> @NonNull List<E> asList(final @NonNull Iterable<E> i) {
        if (i instanceof Collection<E> c) {
            return new ArrayList<>(c);
        } else {
            List<E> l = new ArrayList<>();
            ensureUnbroken(i).forEach(l::add);
            return l;
        }
    }

    /// Converts the iterable to a set. This method will create a new set as the return value, even if the argument is
    /// already a instance of [Set].
    /// @param <E> the type of the element.
    /// @param i the iterable to convert.
    /// @return a set with the same elements as the iterable.
    public static <E> @NonNull Set<E> asSet(final @NonNull Iterable<E> i) {
        if (i instanceof Set<E> set) {
            return new HashSet<>(set);
        } else {
            Set<E> s = new HashSet<>();
            ensureUnbroken(i).forEach(s::add);
            return s;
        }
    }

    /// Creates a copy of an iterable. The returned copy will have the same elements as the original but may
    /// have a different implementation.
    /// @param <E> the type of the elements.
    /// @param iterable the iterable to copy.
    /// @return a copy of the original iterable.
    public static <E> @NonNull Iterable<E> copy(final @NonNull Iterable<E> iterable) {
        List<E> list = new ArrayList<>();
        for (E e : iterable) {
            list.add(e);
        }
        return list;
    }

    /// Creates a copy of an the first N elements of an iterable.
    /// @param <E> the type of the elements.
    /// @param iterable the iterable to copy.
    /// @param n the number of elements to copy.
    /// @return a copy of the original iterable.
    public static <E> Iterable<E> copyFirst(final @NonNull Iterable<E> iterable, final int n) {
        List<E> list = new ArrayList<>();
        int i = 0;
        Iterator<E> iter = ensureUnbroken(iterable).iterator();
        while (i < n && iter.hasNext()) {
            list.add(iter.next());
            i++;
        }
        return list;
    }

    /// Creates a copy of an iterable and returns it as a [List].
    /// @param <E> the type of the elements.
    /// @param iterable the iterable to copy.
    /// @return a list with the same elements as the original iterable.
    public static <E> List<E> newList(final @NonNull Iterable<E> iterable) {
        final List<E> list = new ArrayList<>();
        ensureUnbroken(iterable).forEach(list::add);
        return list;
    }

    /// Returns the first element of the iterable.
    /// @param <E> the type of the element.
    /// @param iterable the iterable to get the first element from.
    /// @return the first element of the iterable, or `null` if the iterable is empty.
    public static <E> E first(final Iterable<E> iterable) {
        Iterator<E> iter = iterable.iterator();
        if (iter.hasNext()) {
            return iter.next();
        }
        return null;
    }

    /// Returns the last element of the iterable.
    /// @param <E> the type of the element.
    /// @param iterable the iterable to get the last element from.
    /// @return the last element of the iterable, or `null` if the iterable is empty.
    public static <E> E last(final Iterable<E> iterable) {
        Iterator<E> iter = iterable.iterator();
        E last = null;
        while (iter.hasNext()) {
            last = iter.next();
        }
        return last;
    }


    /// Returns an iterator that will skips the first element.
    /// @param <E> the element type.
    /// @param iterable the iterable to get the iterator for.
    /// @return An iterator that skips the first element.
    public static <E> Iterator<E> skipFirstIterator(final @NonNull Iterable<E> iterable) {
        final Iterator<E> i = iterable.iterator();
        if (i.hasNext()) {
            i.next();
        }
        return i;
    }

    /// Returns an iterator that will skips the last element.
    /// @param <E> the element type.
    /// @param iterable the iterable to get the iterator for.
    /// @return An iterator that skips the last element.
    public static <E> Iterator<E> skipLastIterator(final @NonNull Iterable<E> iterable) {
        return new SkipLastIterator<>(iterable.iterator());
    }

    private static final class SkipLastIterator<T> implements Iterator<T> {

        private final Iterator<T> source;
        private T buffer;                  // Lookahead buffer
        private boolean hasNext;          // Whether there's a next element to return
        private boolean canRemove = false;      // Whether remove() is allowed
        private boolean removePending = false;  // Whether remove() should be executed

        SkipLastIterator(final @NonNull Iterator<T> source) {
            this.source = Objects.requireNonNull(source);
            prefetch();
        }

        private void prefetch() {
            if (source.hasNext()) {
                buffer = source.next();
                if (source.hasNext()) {
                    hasNext = true;
                } else {
                    // Only one element — skip it
                    hasNext = false;
                    buffer = null;
                }
            } else {
                hasNext = false;
            }
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public T next() {
            if (!hasNext) {
                throw new NoSuchElementException("No more elements");
            }

            // Perform deferred remove, if requested
            if (removePending) {
                source.remove();
                removePending = false;
            }

            T current = buffer;

            // Advance the buffer
            if (source.hasNext()) {
                buffer = source.next();
                hasNext = true;
            } else {
                buffer = null;
                hasNext = false;
            }

            canRemove = true;
            return current;
        }

        @Override
        public void remove() {
            if (!canRemove) {
                throw new IllegalStateException("remove() must follow next(), and only once per element");
            }
            // Defer the actual remove to the next call of next()
            removePending = true;
            canRemove = false;
        }
    }
}
