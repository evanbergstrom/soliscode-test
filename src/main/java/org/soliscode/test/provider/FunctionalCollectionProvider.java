package org.soliscode.test.provider;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/// A collection provider that uses a set of functions to construct instances of the collection. There are four
/// elements required to create a collection provider using this class:
/// - Default constructor function
/// - Copy constructor function
/// - Collection constructor function
/// - Element provider.
/// For example, to create a collection provider for an `ArrayList` of Integer objects:
/// ```java
///     CollectionProvider<Integer> provider = FunctionalCollectionProvider<Integer, ArrayList>(
///         ArrayList;:new, ArrayList::new, ArrayList::new, Providers.integerProvider());
/// ```
///
/// @param <E> the element type for the iterable
/// @param <I> the iterable type.
/// @author evanbergstrom
/// @since 1.0.0
public class FunctionalCollectionProvider<E, I extends Iterable<E>> extends FunctionalProvider<I>
        implements CollectionProvider<E, I> {

    /// Default number of elements to use for a test.
    private static final int DEFAULT_SIZE = 10;

    private final @NotNull Function<Collection<E>, I> collectionConstructor;
    private final @NotNull ObjectProvider<E> elementProvider;

    /// Create an instance of this collection provider that uses the methods and element provider specified in the
    /// arguments for its implementation.
    /// @param defaultConstructor the supplier to use to create default instances of the collection.
    /// @param copyConstructor the function to use to create a copy of the collection.
    /// @param collectionConstructor the function to use to create an instance from another collection.
    /// @param elementProvider the element provider.
    /// @throws NullPointerException if any of the arguments are `null`
    public FunctionalCollectionProvider(final @NotNull Supplier<I> defaultConstructor,
                                        final @NotNull Function<I, I> copyConstructor,
                                        final @NotNull Function<Collection<E>, I> collectionConstructor,
                                        final @NotNull ObjectProvider<E> elementProvider) {
        super(defaultConstructor, copyConstructor);
        this.collectionConstructor = Objects.requireNonNull(collectionConstructor);
        this.elementProvider = Objects.requireNonNull(elementProvider);
    }

    @Override
    public @NotNull ObjectProvider<E> elementProvider() {
        return elementProvider;
    }

    /// Creates an instance of iterable with no elements.
    /// @return and instance of iterable
    @Override
    public @NotNull I emptyInstance() {
        return defaultInstance();
    }

    @Override
    public @NotNull I createInstance(final @NotNull Collection<E> c) {
        return collectionConstructor.apply(c);
    }

    @Override
    public @NotNull I createInstance(int seed) {
        return createInstance(elementProvider().createUniqueInstances(DEFAULT_SIZE, seed));
    }

    @Override
    public @NotNull I createSingleton() {
        return createInstance(Collections.singleton(elementProvider.createInstance()));
    }

    /// Creates an instance of iterable with a single element.
    /// @param e the element for the iterable.
    /// @return and instance of iterable.
    @Override
    public @NotNull I createSingleton(E e) {
        return createInstance(Collections.singleton(e));
    }

    /// Creates an instance of iterable with the same elements provided.
    /// @param elements the elements for the iterable.
    /// @return and instance of iterable.
    /// @throws NullPointerException if the argument is `null`.
    @Override
    public  @NotNull I createInstance(@NotNull E[] elements) {
        return createInstance(Arrays.asList(elements));
    }

    @Override
    public @NotNull I createInstanceWithUniqueElements() {
        return createInstance(elementProvider.createUniqueInstances(DEFAULT_SIZE));
    }

    @Override
    public @NotNull I createInstanceWithUniqueElements(int size) {
        return createInstance(elementProvider.createUniqueInstances(size));
    }

    @Override
    public @NotNull I createInstanceWithUniqueElements(int size, int seed) {
        return createInstance(elementProvider.createUniqueInstances(size, seed));
    }

    /// Create an instance of this collection provider that uses the methods and element provider specified in the
    /// arguments for its implementation.
    /// @param <E> the type of the elements.
    /// @param <I> the type of the iterable.
    /// @param defaultConstructor the supplier to use to create default instances of the collection.
    /// @param copyConstructor the function to use to create a copy of the collection.
    /// @param collectionConstructor the function to use to create an instance from another collection.
    /// @param elementProvider the element provider.
    /// @return the collection provider.
    /// @throws NullPointerException if any of the arguments are `null`
    public static <E, I extends Iterable<E>> FunctionalCollectionProvider<E, I> from(
            final @NotNull Supplier<I> defaultConstructor,
            final @NotNull Function<I, I> copyConstructor,
            final @NotNull Function<Collection<E>, I> collectionConstructor,
            final @NotNull ObjectProvider<E> elementProvider) {
        return new FunctionalCollectionProvider<>(defaultConstructor, copyConstructor, collectionConstructor, elementProvider);
    }
}
