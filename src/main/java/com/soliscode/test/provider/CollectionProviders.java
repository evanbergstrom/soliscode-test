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

package com.soliscode.test.provider;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.function.Supplier;

/// Utility class for creating collection providers.
/// @author evanbergstrom
/// @since 1.0
public final class CollectionProviders {

    private CollectionProviders() { }

    /// Create an instance of this collection objectProvider that uses the methods and element objectProvider
    /// specified in the arguments for its implementation.
    ///
    /// For example, to create a collection objectProvider for an `ArrayList` of Integer objects:
    /// ```java
    ///     CollectionProvider<Integer> objectProvider = CollectionProviders.from(ArrayList::new, ArrayList::new,
    ///         ArrayList::new, Providers.integerProvider());
    /// ```
    /// @param <E> the element type.
    /// @param <C> the collection type.
    /// @param defaultConstructor the supplier to use to create default instances of the collection.
    /// @param copyConstructor the function to use to create a copy of the collection.
    /// @param collectionConstructor the function to use to create an instance from another collection.
    /// @param elementProvider the element objectProvider.
    /// @return the collection objectProvider.
    /// @throws NullPointerException if any of the arguments are `null`
    public static <C extends Iterable<E>, E> CollectionProvider<C, E> from(
            final Supplier<C> defaultConstructor,
            final Function<C, C> copyConstructor,
            final Function<Collection<E>, C> collectionConstructor,
            final ObjectProvider<E> elementProvider) {
        return new FunctionalCollectionProvider<>(defaultConstructor, copyConstructor, collectionConstructor,
                elementProvider);
    }

    /// Creates a collection objectProvider for instances of [ArrayList] with elements creates using the specified
    /// @param elementProvider the objectProvider to use to create the elements.
    /// @param <E> The type of the elements
    /// @return the collection objectProvider.
    // NonApiType: this provides ArrayList itself, to test it.
    @SuppressWarnings("NonApiType")
    public static <E> CollectionProvider<ArrayList<E>, E> provideArrayList(
            final ObjectProvider<E> elementProvider) {
        return CollectionProviders.from(ArrayList::new, ArrayList::new, ArrayList::new, elementProvider);
    }

    /// Creates a collection objectProvider for instances of [LinkedList] with elements creates using the specified
    /// @param elementProvider the objectProvider to use to create the elements.
    /// @param <E> The type of the elements
    /// @return the collection objectProvider.
    // NonApiType, JdkObsolete: this provides LinkedList itself, to test it.
    @SuppressWarnings({"NonApiType", "JdkObsolete"})
    public static <E> CollectionProvider<LinkedList<E>, E> provideLinkedList(
            final ObjectProvider<E> elementProvider) {
        return CollectionProviders.from(LinkedList::new, LinkedList::new, LinkedList::new, elementProvider);
    }

    /// Creates a collection objectProvider for instances of [HashSet] with elements creates using the specified
    /// @param elementProvider the objectProvider to use to create the elements.
    /// @param <E> The type of the elements
    /// @return the collection objectProvider.
    // NonApiType: this provides HashSet itself, to test it.
    @SuppressWarnings("NonApiType")
    public static <E> CollectionProvider<HashSet<E>, E> provideHashSet(
            final ObjectProvider<E> elementProvider) {
        return CollectionProviders.from(HashSet::new, HashSet::new, HashSet::new, elementProvider);
    }

    /// Provides a collection objectProvider that supplies synchronized collections using the provided element
    /// objectProvider.
    /// This method wraps an [ArrayList] collection objectProvider into a synchronized collection
    /// by using [Collections#synchronizedCollection(Collection)].
    /// It ensures thread-safety during access to the returned collection instances.
    ///
    /// @param <E> the type of elements in the collection.
    /// @param elementProvider the objectProvider responsible for supplying elements to the collection.
    ///                        Must not be `null`.
    /// @return a [CollectionProvider] that provides thread-safe synchronized collections.
    /// @throws NullPointerException if `elementProvider` is `null`.
    public static <E> CollectionProvider<Collection<E>, E> provideSynchronizedCollection(
            final ObjectProvider<E> elementProvider) {
        return CollectionProviders.wrap(provideArrayList(elementProvider), Collections::synchronizedCollection);
    }

    /// Creates a objectProvider that wraps the provided collection from an underlying objectProvider.
    /// @param <E> the element type.
    /// @param <C> the collection type provided by the underlying collection.
    /// @param <W> the collection type of the wrapped collection.
    /// @param provider the underlying objectProvider.
    /// @param wrapper the function used to wrap the provided collection.
    /// @return the wrapped collection.
    public static <E, C extends Collection<E>, W extends Collection<E>>
        CollectionProvider<W, E> wrap(final CollectionProvider<C, E> provider, final Function<C, W> wrapper)  {
        return new WrappedCollectionProvider<>(provider, wrapper);
    }

    private record WrappedCollectionProvider<E, C extends Collection<E>, W extends Collection<E>>(
            CollectionProvider<C, E> provider, Function<C, W> wrapper)
                implements CollectionProvider<W, E> {

        @Override
            public ObjectProvider<E> elementProvider() {
                return provider.elementProvider();
            }

        @Override
        public W defaultInstance() {
            return wrapper.apply(provider.defaultInstance());
        }

        @Override
            public W emptyInstance() {
                return wrapper.apply(provider.emptyInstance());
            }

            @Override
            public W copyInstance(final W ws) {
                return wrapper.apply(provider.createInstance(ws));
            }

            @Override
            public W createInstance(final Collection<E> c) {
                return wrapper.apply(provider.createInstance(c));
            }

            @Override
            public W createInstance(final long seed) {
                return wrapper.apply(provider.createInstance(seed));
            }

            @Override
            public W createSingleton() {
                return wrapper.apply(provider.createSingleton());
            }

            @Override
            public W createInstanceWithUniqueElements() {
                return wrapper.apply(provider.createInstanceWithUniqueElements());
            }

            @Override
            public W createInstanceWithUniqueElements(final int size) {
                return wrapper.apply(provider.createInstanceWithUniqueElements(size));
            }

            @Override
            public W createInstanceWithUniqueElements(final int size, final int seed) {
                return wrapper.apply(provider.createInstanceWithUniqueElements(size, seed));
            }

            @Override
            public W createInstance(final E[] elements) {
                return wrapper.apply(provider.createInstance(elements));
            }

            @Override
            public W createSingleton(final E e) {
                return wrapper.apply(provider.createSingleton(e));
            }
        }
}
