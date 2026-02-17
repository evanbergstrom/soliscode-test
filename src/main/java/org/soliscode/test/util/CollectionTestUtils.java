package org.soliscode.test.util;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.stream.Collectors;


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
    public static <E> Collection<E> removeAllByIdentity(final @NonNull Collection<E> c,
                                                        final @NonNull Collection<?> e) {
        final Set<Object> s = new IdentitySet<>(e);
        c.removeIf(s::contains);
        return c;
    }

    /// Creates a collection backed by the provided collection that does not accept null values as elements.
    /// The returned collection will throw a NullPointerException when attempting to add_singleElement_returnsTrueAndUpdatesSize, check for,
    /// or remove null values. This is useful for testing collection implementations that should
    /// not permit null values.
    ///
    /// @param <E> the element type of the collection
    /// @param c the backing collection
    /// @return a collection wrapper that rejects null values
    /// @throws NullPointerException if the backing collection is null
    /// @see PreventNullsCollection
    public static <E> Collection<E> preventNulls(final Collection<E> c) {
        return new PreventNullsCollection<>(c);
    }

    private record PreventNullsCollection<E>(@NonNull Collection<E> collection) implements Collection<E> {

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
            @NonNull
            public Iterator<E> iterator() {
                return collection.iterator();
            }

            @Override
            @NonNull
            public Object @NonNull [] toArray() {
                return collection.toArray();
            }

            @Override
            @NonNull
            public <T> T @NonNull [] toArray(@NonNull final T @NonNull [] a) {
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
            public boolean containsAll(@NonNull final Collection<?> c) {
                if (collection.contains(null)) {
                    throw new NullPointerException();
                }
                return collection.containsAll(c);
            }

            @Override
            public boolean addAll(@NonNull final Collection<? extends E> c) {
                if (collection.contains(null)) {
                    throw new NullPointerException();
                }
                return collection.addAll(c);
            }

            @Override
            public boolean removeAll(@NonNull final Collection<?> c) {
                if (collection.contains(null)) {
                    throw new NullPointerException();
                }
                return collection.removeAll(c);
            }

            @Override
            public boolean retainAll(@NonNull final Collection<?> c) {
                if (collection.contains(null)) {
                    throw new NullPointerException();
                }
                return collection.retainAll(c);
            }

            @Override
            public void clear() {
                collection.clear();
            }

            @Override
            public @NonNull String toString() {
                return collection.toString();
            }

            @Override
            public boolean equals(final Object obj) {
                if (obj instanceof PreventNullsCollection<?>(Collection<?> collection1)) {
                    return collection.equals(collection1);
                } else {
                    return false;
                }
            }

        @Override
        public int hashCode() {
            return collection.hashCode();
        }
    }

    /// Creates a list backed by the provided list that does not accept null values as elements.
    /// The returned list will throw a NullPointerException when attempting to add_singleElement_returnsTrueAndUpdatesSize, set, or
    /// insert null values. This is useful for testing list implementations that should
    /// not permit null values.
    ///
    /// @param <E> the element type of the list
    /// @param c the backing list
    /// @return a list wrapper that rejects null values
    /// @throws NullPointerException if the backing list is null
    /// @see PreventNullsList
    public static <E> List<E> preventNulls(final List<E> c) {
        return new PreventNullsList<>(c);
    }

    private record PreventNullsList<E>(@NonNull List<E> list) implements List<E> {

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
                return list.contains(o);
            }

            @Override
            @NonNull
            public Iterator<E> iterator() {
                return list.iterator();
            }

            @Override
            @NonNull
            public Object @NonNull [] toArray() {
                return list.toArray();
            }

            @Override
            @NonNull
            public <T> T @NonNull [] toArray(final T @NonNull [] a) {
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
                return list.remove(o);
            }

            @Override
            public boolean containsAll(@NonNull final Collection<?> c) {
                if (c.contains(null)) {
                    throw new NullPointerException();
                }
                //noinspection SlowListContainsAll
                return list.containsAll(c);
            }

            @Override
            public boolean addAll(@NonNull final Collection<? extends E> c) {
                if (c.contains(null)) {
                    throw new NullPointerException();
                }
                return list.addAll(c);
            }

            @Override
            public boolean addAll(final int index, final @NonNull Collection<? extends E> c) {
                if (c.contains(null)) {
                    throw new NullPointerException();
                }
                return list.addAll(index, c);
            }

            @Override
            public boolean removeAll(final @NonNull Collection<?> c) {
                if (c.contains(null)) {
                    throw new NullPointerException();
                }
                return list.removeAll(c);
            }

            @Override
            public boolean retainAll(final @NonNull Collection<?> c) {
                if (c.contains(null)) {
                    throw new NullPointerException();
                }
                return list.retainAll(c);
            }

            @Override
            public void clear() {
                list.clear();
            }

            @Override
            public @NonNull String toString() {
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
                return list.indexOf(o);
            }

            @Override
            public int lastIndexOf(final Object o) {
                return list.lastIndexOf(o);
            }

            @Override
            public @NonNull ListIterator<E> listIterator() {
                return list.listIterator();
            }

            @Override
            public @NonNull ListIterator<E> listIterator(final int index) {
                return list.listIterator(index);
            }

            @Override
            public @NonNull List<E> subList(final int fromIndex, final int toIndex) {
                return list.subList(fromIndex, toIndex);
            }

            @Override
            public boolean equals(final Object obj) {
                if (obj == this) {
                    return true;
                } else if (obj instanceof PreventNullsList<?>(List<?> list1)) {
                    return list.equals(list1);
                } else {
                    return false;
                }
            }

        @Override
        public int hashCode() {
            return list.hashCode();
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
    public static <E> List<E> listOf(final E e1)   {
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
    public static <E> List<E> listOf(final E e1, final E e2)   {
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
    public static <E> List<E> listOf(final E e1, final E e2, final E e3)   {
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
    public static String toCSVString(final @NonNull Collection<?> collection) {
        return "[" + collection.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", ")) + "]";
    }
}
