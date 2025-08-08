package org.soliscode.test.provider;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/// Provides instances of [Collection] for use in testing.
///
/// @param <E> the element type for the iterable
/// @param <I> the iterable type.
/// @author evanbergstrom
/// @since 1.0.0
public interface CollectionProvider<E, I extends Iterable<E>> extends ObjectProvider<I> {

    /// Returns the element provider used to create elements for instances of the collection.
    /// @return the element provider.
    @NotNull ObjectProvider<E> elementProvider();

    /// Creates an instance of iterable with no elements.
    /// @return and instance of the iterable
    @NotNull I emptyInstance();

    /// Creates an instance of iterable with the same elements as in the specified collection.
    /// @param c the elements to use for the iterable.
    /// @return an instance of the iterable.
    @NotNull I createInstance(final @NotNull Collection<E> c);

    /// Creates an instance of iterable being tested based upon an integer seed value. Two iterables created with the
    /// same seed value should have the same number of elements and the elements in the same iteration position should
    /// hav the same value. Two iterables with different seed values should have either different numbers of elements,
    /// elements with different values, or both.
    /// @param   seed an integer seed to use to create and instance of the iterable.
    /// @return  an instance of the iterable.
    @NotNull I createInstance(int seed);

    /// Creates an instance of iterable with a single element.
    /// @return and instance of the iterable
    @NotNull I createSingleton();

    /// Creates an instance of iterable with a single element.
    /// @param e the element for the iterable.
    /// @return and instance of iterable.
    @NotNull I createSingleton(E e);

    /// Creates an instance of iterable with the same elements provided.
    /// @param elements the elements for the iterable.
    /// @return and instance of iterable.
    /// @throws NullPointerException if the argument is `null`.
    @NotNull I createInstance(@NotNull E[] elements);

    /// Create an iterable with a set of elements that does not contain any duplicate values.
    /// @return an instance of the iterable.
    @NotNull I createInstanceWithUniqueElements();

    /// Create an iterable with a set of elements of a specified length that does not contain any duplicate values.
    /// @param size the number of elements in the iterable.
    /// @return an instance of the iterable.
    @NotNull I createInstanceWithUniqueElements(int size);

    /// Create an iterable with a set of elements of a specified length and using the specified seed value that does not
    /// contain any duplicate values.
    /// @param size the number of elements in the iterable.
    /// @param seed the seed value to use to create the elements.
    /// @return an instance of the iterable.
    @NotNull I createInstanceWithUniqueElements(int size, int seed);
}
