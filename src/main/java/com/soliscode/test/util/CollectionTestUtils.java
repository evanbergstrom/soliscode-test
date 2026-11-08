package com.soliscode.test.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;


/// This class consists exclusively of static methods that operate on or return collections.
///
/// The methods of this class all throw a `NullPointerException` if the iterables or class objects provided to
/// them are `null`.
///
/// @author evanbergstrom
/// @since 1.0.0
public final class CollectionTestUtils {

    /// Private constructor to prevent instantiation.
    /// This utility class contains only static methods and should not be instantiated.
    private CollectionTestUtils() {
    }

    /// Removes all the elements in a collection that have the same identity as the elements in another collection.
    /// This method uses identity comparison (==) rather than equality comparison (equals()) to determine
    /// which elements to remove. This is useful for testing scenarios where object identity matters.
    ///
    /// @param <E> the element type
    /// @param c the collection to remove the elements from
    /// @param e the collection of elements to remove by identity
    /// @return the collection with matching identity elements removed
    /// @throws NullPointerException if either collection is null
    /// @see IdentitySet
    public static <E> Collection<E> removeAllByIdentity(final Collection<E> c,
                                                        final Collection<?> e) {
        final Set<Object> s = new IdentitySet<>(e);
        c.removeIf(s::contains);
        return c;
    }

    /// Creates a collection backed by the provided collection that does not accept null values as elements.
    /// The returned collection will throw a NullPointerException when attempting to add, check for,
    /// or remove null values. This is useful for testing collection implementations that should
    /// not permit null values. Its `containsAll`, `addAll`, `removeAll` and `retainAll` throw a NullPointerException
    /// when their argument has a null element, and otherwise act as the backing collection does.
    ///
    /// @param <E> the element type of the collection
    /// @param c the backing collection
    /// @return a collection wrapper that rejects null values
    /// @throws NullPointerException if the backing collection is null
    /// @see PreventNullsCollection
    public static <E> Collection<E> preventNulls(final Collection<E> c) {
        return new PreventNullsCollection<>(c);
    }

    /// Returns whether `c` has a null element. It iterates `c` rather than calling `c.contains(null)`, which throws
    /// on a collection that refuses null, such as `List.of(1)` (T-0008).
    private static boolean containsNull(final Collection<?> c) {
        for (final Object e : c) {
            if (e == null) {
                return true;
            }
        }
        return false;
    }

    private record PreventNullsCollection<E>(Collection<E> collection) implements Collection<E> {

        @Override
            public int size() {
                return collection.size();
            }

            @Override
            public boolean isEmpty() {
                return collection.isEmpty();
            }

            @Override
            public boolean contains(final Object o) {
                if (o == null) {
                    throw new NullPointerException();
                }
                return collection.contains(o);
            }

            @Override
            public Iterator<E> iterator() {
                return collection.iterator();
            }

            @Override
            public Object[] toArray() {
                return collection.toArray();
            }

            @Override
            public <T> T[] toArray(final T[] a) {
                return collection.toArray(a);
            }

            @Override
            public boolean add(final E e) {
                if (e == null) {
                    throw new NullPointerException();
                }
                return collection.add(e);
            }

            @Override
            public boolean remove(final Object o) {
                if (o == null) {
                    throw new NullPointerException();
                }
                return collection.remove(o);
            }

            @Override
            public boolean containsAll(final Collection<?> c) {
                if (containsNull(c)) {
                    throw new NullPointerException();
                }
                return collection.containsAll(c);
            }

            @Override
            public boolean addAll(final Collection<? extends E> c) {
                if (containsNull(c)) {
                    throw new NullPointerException();
                }
                return collection.addAll(c);
            }

            @Override
            public boolean removeAll(final Collection<?> c) {
                if (containsNull(c)) {
                    throw new NullPointerException();
                }
                return collection.removeAll(c);
            }

            @Override
            public boolean retainAll(final Collection<?> c) {
                if (containsNull(c)) {
                    throw new NullPointerException();
                }
                return collection.retainAll(c);
            }

            @Override
            public void clear() {
                collection.clear();
            }

            @Override
            public String toString() {
                return collection.toString();
            }

            /// Returns whether `obj` is this wrapper. A [Collection] has no defined equality, so the wrapper is equal
            /// only to itself, as [java.util.Collections#unmodifiableCollection] is.
            @Override
            public boolean equals(final @Nullable Object obj) {
                return this == obj;
            }

            /// Returns the identity hash code, consistent with [#equals(Object)].
            @Override
            public int hashCode() {
                return System.identityHashCode(this);
            }
    }

    /// Creates a list backed by the provided list that does not accept null values as elements.
    /// This is useful for testing list implementations that should not permit null values.
    ///
    /// The returned list throws a NullPointerException:
    /// - from `add`, `add(int, Object)`, `set`, `contains`, `remove(Object)`, `indexOf` and `lastIndexOf` given null;
    /// - from `containsAll`, both `addAll`s, `removeAll` and `retainAll` given an argument with a null element;
    /// - from `replaceAll` when the operator returns null;
    /// - from the `add` and `set` of its list iterators given null.
    ///
    /// Its `subList` returns the backing list's view, wrapped in the same way. Otherwise the list, its list iterators
    /// and its sub-lists act as the backing list and its own do; `sort` sorts the backing list as it sorts itself,
    /// even when it already holds a null. Its `reversed()` is [List]'s default view of this list: its writes go
    /// through this list and refuse null, but its `remove(Object)`, `removeAll` and `retainAll` do not check for null.
    ///
    /// @param <E> the element type of the list
    /// @param c the backing list
    /// @return a list wrapper that rejects null values
    /// @throws NullPointerException if the backing list is null
    /// @see PreventNullsList
    public static <E> List<E> preventNulls(final List<E> c) {
        return new PreventNullsList<>(c);
    }

    private record PreventNullsList<E>(List<E> list) implements List<E> {

        @Override
            public int size() {
                return list.size();
            }

            @Override
            public boolean isEmpty() {
                return list.isEmpty();
            }

            @Override
            public boolean contains(final Object o) {
                if (o == null) {
                    throw new NullPointerException();
                }
                return list.contains(o);
            }

            @Override
            public Iterator<E> iterator() {
                return list.iterator();
            }

            @Override
            public Object[] toArray() {
                return list.toArray();
            }

            @Override
            public <T> T[] toArray(final T[] a) {
                return list.toArray(a);
            }

            @Override
            public boolean add(final E e) {
                if (e == null) {
                    throw new NullPointerException();
                }
                return list.add(e);
            }

            @Override
            public boolean remove(final Object o) {
                if (o == null) {
                    throw new NullPointerException();
                }
                return list.remove(o);
            }

            @Override
            public boolean containsAll(final Collection<?> c) {
                if (containsNull(c)) {
                    throw new NullPointerException();
                }
                //noinspection SlowListContainsAll
                return list.containsAll(c);
            }

            @Override
            public boolean addAll(final Collection<? extends E> c) {
                if (containsNull(c)) {
                    throw new NullPointerException();
                }
                return list.addAll(c);
            }

            @Override
            public boolean addAll(final int index, final Collection<? extends E> c) {
                if (containsNull(c)) {
                    throw new NullPointerException();
                }
                return list.addAll(index, c);
            }

            @Override
            public boolean removeAll(final Collection<?> c) {
                if (containsNull(c)) {
                    throw new NullPointerException();
                }
                return list.removeAll(c);
            }

            @Override
            public boolean retainAll(final Collection<?> c) {
                if (containsNull(c)) {
                    throw new NullPointerException();
                }
                return list.retainAll(c);
            }

            @Override
            public void clear() {
                list.clear();
            }

            @Override
            public String toString() {
                return list.toString();
            }

        @Override
            public E get(final int index) {
                return list.get(index);
            }

            @Override
            public E set(final int index, final E element) {
                if (element == null) {
                    throw new NullPointerException();
                }
                return list.set(index, element);
            }

            @Override
            public void add(final int index, final E element) {
                if (element == null) {
                    throw new NullPointerException();
                }
                list.add(index, element);
            }

            @Override
            public E remove(final int index) {
                return list.remove(index);
            }

            @Override
            public int indexOf(final Object o) {
                if (o == null) {
                    throw new NullPointerException();
                }
                return list.indexOf(o);
            }

            @Override
            public int lastIndexOf(final Object o) {
                if (o == null) {
                    throw new NullPointerException();
                }
                return list.lastIndexOf(o);
            }

            @Override
            public ListIterator<E> listIterator() {
                return new PreventNullsListIterator<>(list.listIterator());
            }

            @Override
            public ListIterator<E> listIterator(final int index) {
                return new PreventNullsListIterator<>(list.listIterator(index));
            }

            /// Returns the backing list's view, wrapped so that it refuses `null` as this list does.
            @Override
            public List<E> subList(final int fromIndex, final int toIndex) {
                return new PreventNullsList<>(list.subList(fromIndex, toIndex));
            }

            /// Replaces each element with the operator's result, throwing a NullPointerException when that is null.
            /// What a null result leaves replaced is up to the backing list's `replaceAll`.
            @Override
            public void replaceAll(final UnaryOperator<E> operator) {
                Objects.requireNonNull(operator);
                list.replaceAll(e -> {
                    final E r = operator.apply(e);
                    if (r == null) {
                        throw new NullPointerException();
                    }
                    return r;
                });
            }

            /// Sorts the backing list, as it sorts itself. `List`'s default would write the elements back through
            /// this list's iterator, which would refuse a null that the backing list already holds.
            @Override
            public void sort(final @Nullable Comparator<? super E> c) {
                list.sort(c);
            }

            @Override
            public boolean equals(final @Nullable Object obj) {
                if (obj == this) {
                    return true;
                } else if (obj instanceof PreventNullsList<?>(List<?> list1)) {
                    return list.equals(list1);
                } else {
                    // A list equals any list with the same elements in the same order (List#equals).
                    return obj instanceof List<?> && list.equals(obj);
                }
            }

        @Override
        public int hashCode() {
            return list.hashCode();
        }
    }

    /// A list iterator of a `preventNulls` list: its `add` and `set` throw a NullPointerException for null, and
    /// otherwise it acts as the backing list's iterator does.
    private record PreventNullsListIterator<E>(ListIterator<E> iterator) implements ListIterator<E> {

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public E next() {
            return iterator.next();
        }

        @Override
        public boolean hasPrevious() {
            return iterator.hasPrevious();
        }

        @Override
        public E previous() {
            return iterator.previous();
        }

        @Override
        public int nextIndex() {
            return iterator.nextIndex();
        }

        @Override
        public int previousIndex() {
            return iterator.previousIndex();
        }

        @Override
        public void remove() {
            iterator.remove();
        }

        @Override
        public void set(final E e) {
            if (e == null) {
                throw new NullPointerException();
            }
            iterator.set(e);
        }

        @Override
        public void add(final E e) {
            if (e == null) {
                throw new NullPointerException();
            }
            iterator.add(e);
        }

        @Override
        public void forEachRemaining(final Consumer<? super E> action) {
            iterator.forEachRemaining(action);
        }
    }

    /// Creates a mutable empty list. This is a convenience method for creating small test
    /// lists no elements. Unlike List.of(), this method returns a mutable ArrayList.
    ///
    /// @param <E> the element type
    /// @return a mutable ArrayList containing no elements
    public static <E> List<E> listOf()   {
        return new ArrayList<>();
    }

    /// Creates a mutable list containing exactly one element. This is a convenience method for creating small test
    /// lists with known elements. Unlike List.of(), this method returns a mutable ArrayList.
    ///
    /// @param <E> the element type
    /// @param e1 the first element
    /// @return a mutable ArrayList containing the one element.
    public static <E> List<E> listOf(final @Nullable E e1)   {
        List<E> l = new ArrayList<>();
        l.add(e1);
        return l;
    }

    /// Creates a mutable list containing exactly two elements. This is a convenience method for creating small test
    /// lists with known elements. Unlike List.of(), this method returns a mutable ArrayList.
    ///
    /// @param <E> the element type
    /// @param e1 the first element
    /// @param e2 the second element
    /// @return a mutable ArrayList containing the two elements.
    public static <E> List<E> listOf(final @Nullable E e1, final @Nullable E e2)   {
        List<E> l = new ArrayList<>();
        l.add(e1);
        l.add(e2);
        return l;
    }

    /// Creates a mutable list containing exactly three elements.
    /// This is a convenience method for creating small test lists with known elements.
    /// Unlike List.of(), this method returns a mutable ArrayList.
    ///
    /// @param <E> the element type
    /// @param e1 the first element
    /// @param e2 the second element
    /// @param e3 the third element
    /// @return a mutable ArrayList containing the three elements
    public static <E> List<E> listOf(final @Nullable E e1, final @Nullable E e2, final @Nullable E e3)   {
        List<E> l = new ArrayList<>();
        l.add(e1);
        l.add(e2);
        l.add(e3);
        return l;
    }

    /// Converts a collection to a comma-separated string representation.
    /// The elements are converted to strings using their toString() method and
    /// joined with ", " (comma and space). The result is enclosed in square brackets.
    ///
    /// @param collection the collection to convert to a CSV string
    /// @return a string representation with comma-separated elements enclosed in square brackets
    /// @throws NullPointerException if collection is null
    /// @see Collectors#joining(CharSequence)
    public static String toCSVString(final Collection<?> collection) {
        return "[" + collection.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", ")) + "]";
    }
}
