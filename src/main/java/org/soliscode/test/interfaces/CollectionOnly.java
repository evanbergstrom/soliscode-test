package org.soliscode.test.interfaces;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/// An collection that is provided for tests that only implement the [Collection] interface. If the code that is
/// being tested tries to detect the kind of collection to optimize the algorithm, it will be forced to use only the
/// methods on `Collection`.
///
/// @param <E> the element type.
/// @author evanbergstrom
/// @since 1.0
public class CollectionOnly<E> extends IterableOnly<E> implements Collection<E> {

    private final Collection<E> collection;

    /// Creates an empty collection.
    public CollectionOnly() {
        // super(collection = new ArrayList<>()); <-- This will work once Flexible Constructors are available
        super(new ArrayList<>());
        this.collection = (Collection<E>) iterable;
    }

    /// Creates a copy of a collection.
    /// @param other the instance of `Collection` to copy.
    public CollectionOnly(final CollectionOnly<E> other) {
        // super(collection = new ArrayList<>(other.collection)); <-- This will work once Flexible Constructors are available
        super(new ArrayList<>(other.collection));
        this.collection = (Collection<E>) iterable;
    }

    /// Creates an instance of `CollectionOnly` from the collection provided.
    /// @param c The collection of elements.
    public CollectionOnly(final Collection<E> c) {
        //super (collection = c); <-- This will work once Flexible Constructors are available
        super (c);
        this.collection = c;
    }

    @Override
    public int size() {
        return collection.size();
    }

    @Override
    public boolean isEmpty() {
        return collection.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return collection.contains(o);
    }

    @Override
    public @NotNull Object @NotNull [] toArray() {
        return collection.toArray();
    }

    @Override
    public @NotNull <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
        return collection.toArray(a);
    }

    @Override
    public boolean add(E e) {
        return collection.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return collection.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return collection.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        return collection.addAll(c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return collection.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return collection.retainAll(c);
    }

    @Override
    public void clear() {
        collection.clear();
    }

    /// {@inheritDoc}
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof CollectionOnly<?> other) {
            return collection.equals(other.collection);
        } else if (obj instanceof Collection<?> c) {
            return collection.equals(c);
        } else {
            return false;
        }
    }

    /// {@inheritDoc}
    @Override
    public int hashCode() {
        return collection.hashCode();
    }

    /// {@inheritDoc}
    @Override
    public String toString() {
        return collection.toString();
    }

    /// Creates a collection from an empty collection of elements.
    /// @param <E> the element type.
    /// @return an collection on an empty collection of elements.
    public static <E> Collection<E> of() {
        return new CollectionOnly<>(List.of());
    }

    /// Creates a collection from a list of one element.
    /// ```java
    ///    Collection<Integer> collection = CollectionOnly.of(1, 2, 3, 4);
    /// ```
    /// @param <E> the element type.
    /// @param values the array of elements.
    /// @return a collection containing the element.
    /// @throws NullPointerException if the array is `null`
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <E> Collection<E> of(final E... values) {
        return new CollectionOnly<>(List.of(values));
    }


    /// Creates a collection from a list of one element.
    /// ```java
    ///    Collection<Integer> iterable = CollectionOnly.of(1);
    /// ```
    /// @param <E> the element type.
    /// @param e1 the first element.
    /// @return a collection containing the specified elements.
    /// @throws NullPointerException if the array is `null`
    public static <E> Collection<E> of(final E e1) {
        return new CollectionOnly<>(List.of(e1));
    }

    /// Creates a collection from an list of two elements.
    /// ```java
    ///    Collection<Integer> iterable = CollectionOnly.of(1, 2);
    /// ```
    /// @param <E> the element type.
    /// @param e1 the first element.
    /// @param e2 the second element.
    /// @return a collection containing the specified elements.
    /// @throws NullPointerException if the array is `null`
    public static <E> Collection<E> of(final E e1, final E e2) {
        return new CollectionOnly<>(List.of(e1, e2));
    }

    /// Creates a collection from an list of three elements.
    /// ```java
    ///    Collection<Integer> iterable = CollectionOnly.of(1, 2, 3);
    /// ```
    /// @param <E> the element type.
    /// @param e1 the first element.
    /// @param e2 the second element.
    /// @param e3 the third element.
    /// @return a collection containing the specified elements.
    /// @throws NullPointerException if the array is `null`
    public static <E> Collection<E> of(final E e1, final E e2, final E e3) {
        return new CollectionOnly<>(List.of(e1, e2, e3));
    }

    /// Creates a collection from a list of four elements.
    /// ```java
    ///    Collection<Integer> iterable = CollectionOnly.of(1, 2, 3, 4);
    /// ```
    /// @param <E> the element type.
    /// @param e1 the first element.
    /// @param e2 the second element.
    /// @param e3 the third element.
    /// @param e4 the third element.
    /// @return a collection containing the specified elements.
    /// @throws NullPointerException if the array is `null`
    public static <E> Collection<E> of(final E e1, final E e2, final E e3, final E e4) {
        return new CollectionOnly<>(List.of(e1, e2, e3, e4));
    }
}
