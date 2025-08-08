package org.soliscode.test.util;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.stream.Collectors;


/// This class consists exclusively of static methods that operate on or return collections.
///
/// The methods of this class all throw a `NullPointerException` if the iterables or class objects provided to
/// them are `null`.
///
/// @author evanbergstrom
/// @since 1.0.0
public final class CollectionTestOps {

    private CollectionTestOps() {
    }

    /// Removed all the elements in a collection that have the same identity as the elements in another collection.
    /// @param <E> the element type.
    /// @param c the collection to remove the elements from
    /// @param e the collection of elements to remove.
    /// @return the collection without the removed elements.
    public static <E> Collection<E> removeAllByIdentity(final Collection<E> c, final Collection<?> e) {
        final Set<Object> s = new IdentitySet<>(e);
        c.removeIf(s::contains);
        return c;
    }

    /// Creates a collection backed by the provided collection that does not accept null values as elements.
    ///
    /// @param <E> The element type of the collection
    /// @param c The collection.
    /// @return A collection that does not accept null values.
    public static <E> Collection<E> preventNulls(final Collection<E> c) {
        return new PreventNullsCollection<>(c);
    }

    private record PreventNullsCollection<E>(@NotNull Collection<E> collection) implements Collection<E> {

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
            @NotNull
            public Iterator<E> iterator() {
                return collection.iterator();
            }

            @Override
            @NotNull
            public Object @NotNull [] toArray() {
                return collection.toArray();
            }

            @Override
            @NotNull
            public <T> T @NotNull [] toArray(@NotNull final T @NotNull [] a) {
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
            public boolean containsAll(@NotNull final Collection<?> c) {
                if (collection.contains(null)) {
                    throw new NullPointerException();
                }
                return collection.containsAll(c);
            }

            @Override
            public boolean addAll(@NotNull final Collection<? extends E> c) {
                if (collection.contains(null)) {
                    throw new NullPointerException();
                }
                return collection.addAll(c);
            }

            @Override
            public boolean removeAll(@NotNull final Collection<?> c) {
                if (collection.contains(null)) {
                    throw new NullPointerException();
                }
                return collection.removeAll(c);
            }

            @Override
            public boolean retainAll(@NotNull final Collection<?> c) {
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
            public String toString() {
                return collection.toString();
            }

        @Override
            public boolean equals(Object obj) {
                if (obj instanceof PreventNullsCollection<?> prevent) {
                    return collection.equals(prevent.collection);
                } else {
                    return false;
                }
            }
        }

    /// Creates a collection backed by the provided collection that does not accept null values as elements.
    ///
    /// @param <E> The element type of the collection
    /// @param c The collection.
    /// @return A collection that does not accept null values.
    public static <E> List<E> preventNulls(final List<E> c) {
        return new PreventNullsList<>(c);
    }

    private record PreventNullsList<E>(@NotNull List<E> list) implements List<E> {

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
            @NotNull
            public Iterator<E> iterator() {
                return list.iterator();
            }

            @Override
            @NotNull
            public Object @NotNull [] toArray() {
                return list.toArray();
            }

            @Override
            @NotNull
            public <T> T @NotNull [] toArray(final T @NotNull [] a) {
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
            public boolean containsAll(@NotNull final Collection<?> c) {
                //noinspection SlowListContainsAll
                return list.containsAll(c);
            }

            @Override
            public boolean addAll(@NotNull final Collection<? extends E> c) {
                if (list.contains(null)) {
                    throw new NullPointerException();
                }
                return list.addAll(c);
            }

            @Override
            public boolean addAll(int index, Collection<? extends E> c) {
                if (c.contains(null)) {
                    throw new NullPointerException();
                }
                return list.addAll(index, c);
            }

            @Override
            public boolean removeAll(final @NotNull Collection<?> c) {
                return list.removeAll(c);
            }

            @Override
            public boolean retainAll(final @NotNull Collection<?> c) {
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
            public E get(int index) {
                return list.get(index);
            }

            @Override
            public E set(int index, E element) {
                if (element == null) {
                    throw new NullPointerException();
                }
                return list.set(index, element);
            }

            @Override
            public void add(int index, E element) {
                if (element == null) {
                    throw new NullPointerException();
                }
                list.add(index, element);
            }

            @Override
            public E remove(int index) {
                return list.remove(index);
            }

            @Override
            public int indexOf(Object o) {
                return list.indexOf(o);
            }

            @Override
            public int lastIndexOf(Object o) {
                return list.lastIndexOf(o);
            }

            @Override
            @NotNull
            public ListIterator<E> listIterator() {
                return list.listIterator();
            }

            @Override
            @NotNull
            public ListIterator<E> listIterator(int index) {
                return list.listIterator(index);
            }

            @Override
            @NotNull
            public List<E> subList(int fromIndex, int toIndex) {
                return list.subList(fromIndex, toIndex);
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                } else if (obj instanceof PreventNullsList<?> prevent) {
                    return list.equals(prevent.list);
                } else {
                    return false;
                }
            }
        }

    public static <E> List<E> listOf(E e1, E e2, E e3)   {
        List<E> l = new ArrayList<>();
        l.add(e1);
        l.add(e2);
        l.add(e3);
        return l;
    }

    /// Gets the class of the a generic parameter form an instance of the generic class.
    /// @param o an instance of a generic class.
    /// @param parameter the index of the generic parameter to get the class for.
    /// @return the class object fo the generic parameter.
    @SuppressWarnings("unchecked")
    public static Class<?> getGenericParameter(@NotNull Object o, int parameter) {
        TypeVariable<Class<?>> var = (TypeVariable<Class<?>>) Arrays.stream(o.getClass().getTypeParameters()).toArray()[0];
        return var.getGenericDeclaration();
    }


    public static String toCSVString(Collection<?> collection) {
        return "[" + collection.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", ")) + "]";
    }
}
