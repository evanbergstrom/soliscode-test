package org.soliscode.test.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

import static org.soliscode.test.breakable.Breakables.ensureUnbroken;

/// This class consists exclusively of static methods that operate on or return iterables.
///
/// The methods of this class all throw a `NullPointerException` if the iterables or class objects provided to
/// them are `null`.
///
/// @author evanbergstrom
/// @since 1.0.0
public final class IterableTestOps {

    private IterableTestOps() {}

    /// Returns `true` if the iterable contains no elements.
    ///
    /// @param i The iterable.
    /// @return `true` if the iterable contains no elements, `false` otherwise.
    public static boolean isEmpty(final Iterable<?> i) {
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
    public static int size(final Iterable<?> i) {
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
    public static boolean contains(final Iterable<?> i, final Object o) {
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
    public static boolean containsByIdentity(final Iterable<?> i, final Object o) {
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


    // Compare the elements in two iterables for equality.
    /// @param iterable1 The first iterable to compare for equality.
    /// @param iterable2 The second iterable to compare for equality.
    /// @return `true` if the iterables contain elements that are equal at each position.
    public static boolean equals(final Iterable<?> iterable1, final Iterable<?> iterable2) {
        Iterator<?> i1 = ensureUnbroken(iterable1).iterator();
        Iterator<?> i2 = ensureUnbroken(iterable2).iterator();
        while (i1.hasNext() && i2.hasNext()) {
            if (!i1.next().equals(i2.next())) {
                return false;
            }
        }
        return !(i1.hasNext() || i2.hasNext());
    }

    private static class EmptyIterable<E> implements Iterable<E> {
        @Override
        @NotNull
        public Iterator<E> iterator() {
            return Collections.emptyIterator();
        }
    }

    /// Converts the iterable to a collection.
    /// @param <E> the type of the element.
    /// @param i the iterable to convert.
    /// @return a collection with the same elements as the iterable.
    public static <E> @NotNull Collection<E> asCollection(final @NotNull Iterable<E> i) {
        Collection<E> s = new ArrayList<>();
        ensureUnbroken(i).forEach(s::add);
        return s;
    }

    /// Converts the iterable to a list. This method will create a new list as the return value, even if the argument is
    /// already a instance of [List].
    /// @param <E> the type of the element.
    /// @param i the iterable to convert.
    /// @return a list with the same elements as the iterable.
    public static <E> @NotNull List<E> asList(final @NotNull Iterable<E> i) {
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
    public static <E> @NotNull Set<E> asSet(final @NotNull Iterable<E> i) {
        if (i instanceof Set<E> set) {
            return new HashSet<>(set);
        } else {
            Set<E> s = new HashSet<>();
            ensureUnbroken(i).forEach(s::add);
            return s;
        }
    }

    /// Creates a copy of an iterable. The returned copy weill have the same elements as the original but may
    /// have a different implementation.
    /// @param <E> the type of the elements.
    /// @param iterable the iterable to copy.
    /// @return a copy of the original iterable.
    public static <E> @NotNull Iterable<E> copy(final @NotNull Iterable<E> iterable) {
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
    public static <E> Iterable<E> copyFirst(final Iterable<E> iterable, int n) {
        List<E> list = new ArrayList<>();
        int i = 0;
        Iterator<E> iter = ensureUnbroken(iterable).iterator();
        while(i < n && iter.hasNext()) {
            list.add(iter.next());
            i++;
        }
        return list;
    }

    /// Creates a copy of an iterable and returns it as a [List].
    /// @param <E> the type of the elements.
    /// @param iterable the iterable to copy.
    /// @return a list with the same elements as the original iterable.
    public static <E> List<E> newList(final @NotNull Iterable<E> iterable) {
        final List<E> list = new ArrayList<>();
        ensureUnbroken(iterable).forEach(list::add);
        return list;
    }

    /// Returns an iterator that will skips the first element.
    /// @param <E> the element type.
    /// @param iterable the iterable to get the iterator for.
    /// @return An iterator that skips the first element.
    public static <E> Iterator<E> skipFirstIterator(final @NotNull Iterable<E> iterable) {
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
    public static <E> Iterator<E> skipLastIterator(final @NotNull Iterable<E> iterable) {
        return new SkipLastIterator<>(iterable.iterator());
    }

    private static final class SkipLastIterator<T> implements Iterator<T> {

        private final Iterator<T> source;
        private T buffer;                  // Lookahead buffer
        private boolean hasNext;          // Whether there's a next element to return
        private boolean canRemove = false;      // Whether remove() is allowed
        private boolean removePending = false;  // Whether remove() should be executed

        public SkipLastIterator(Iterator<T> source) {
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
            if (!canRemove)
                throw new IllegalStateException("remove() must follow next(), and only once per element");

            // Defer the actual remove to the next call of next()
            removePending = true;
            canRemove = false;
        }
    }
}
