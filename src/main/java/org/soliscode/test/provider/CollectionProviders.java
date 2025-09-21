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

package org.soliscode.test.provider;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.function.Supplier;

/// Utility class for creating collection providers.
/// @author evanbergstrom
/// @since 1.0
public final class CollectionProviders {

    private CollectionProviders() { }

    /// Create an instance of this collection provider that uses the methods and element provider specified in the
    /// arguments for its implementation.
    ///
    /// For example, to create a collection provider for an `ArrayList` of Integer objects:
    /// ```java
    ///     CollectionProvider<Integer> provider = CollectionProviders.from(ArrayList::new, ArrayList::new,
    ///         ArrayList::new, Providers.integerProvider());
    /// ```
    /// @param <E> the element type.
    /// @param <C> the collection type.
    /// @param defaultConstructor the supplier to use to create default instances of the collection.
    /// @param copyConstructor the function to use to create a copy of the collection.
    /// @param collectionConstructor the function to use to create an instance from another collection.
    /// @param elementProvider the element provider.
    /// @return the collection provider.
    /// @throws NullPointerException if any of the arguments are `null`
    public static <E, C extends Iterable<E>> CollectionProvider<E, C> from(
            final @NonNull Supplier<C> defaultConstructor,
            final @NonNull Function<C, C> copyConstructor,
            final @NonNull Function<Collection<E>, C> collectionConstructor,
            final @NonNull ObjectProvider<E> elementProvider) {
        return new FunctionalCollectionProvider<>(defaultConstructor, copyConstructor, collectionConstructor,
                elementProvider);
    }

    /// Creates a collection provider for instances of [ArrayList] with elements creates using the specified
    /// @param elementProvider the provider to use to create the elements.
    /// @param <E> The type of the elements
    /// @return the collection provider.
    public static <E> @NonNull CollectionProvider<E, ArrayList<E>> provideArrayList(
            final @NonNull ObjectProvider<E> elementProvider) {
        return CollectionProviders.from(ArrayList::new, ArrayList::new, ArrayList::new, elementProvider);
    }

    /// Creates a collection provider for instances of [LinkedList] with elements creates using the specified
    /// @param elementProvider the provider to use to create the elements.
    /// @param <E> The type of the elements
    /// @return the collection provider.
    public static <E> CollectionProvider<E, LinkedList<E>> provideLinkedList(
            final @NonNull ObjectProvider<E> elementProvider) {
        return CollectionProviders.from(LinkedList::new, LinkedList::new, LinkedList::new, elementProvider);
    }

    /// Creates a collection provider for instances of [HashSet] with elements creates using the specified
    /// @param elementProvider the provider to use to create the elements.
    /// @param <E> The type of the elements
    /// @return the collection provider.
    public static <E> CollectionProvider<E, HashSet<E>> provideHashSet(
            final @NonNull ObjectProvider<E> elementProvider) {
        return CollectionProviders.from(HashSet::new, HashSet::new, HashSet::new, elementProvider);
    }

    /// Creates a provider that wraps the provided collection from an underlying provider.
    /// @param <E> the element type.
    /// @param <C> the collection type provided by the underlying collection.
    /// @param <W> the collection type of the wrapped collection.
    /// @param provider the underlying provider.
    /// @param wrapper the function used to wrap the provided collection.
    /// @return the wrapped collection.
    public static <E, C extends Collection<E>, W extends Collection<E>>
        CollectionProvider<E, W> wrap(final CollectionProvider<E, C> provider, final Function<C, W> wrapper)  {
        return new WrappedCollectionProvider<>(provider, wrapper);
    }

    private record WrappedCollectionProvider<E, C extends Collection<E>, W extends Collection<E>>(
            @NonNull CollectionProvider<E, C> provider, @NonNull Function<C, W> wrapper)
                implements CollectionProvider<E, W> {

        @Override
            public @NonNull ObjectProvider<E> elementProvider() {
                return provider.elementProvider();
            }

        @Override
        public @NonNull W defaultInstance() {
            return wrapper.apply(provider.defaultInstance());
        }

        @Override
            public @NonNull W emptyInstance() {
                return wrapper.apply(provider.emptyInstance());
            }

            @Override
            public @NonNull W copyInstance(final @NonNull W ws) {
                return wrapper.apply(provider.createInstance(ws));
            }

            public @NonNull W createInstance(final @NonNull Collection<E> c) {
                return wrapper.apply(provider.createInstance(c));
            }

            @Override
            public @NonNull W createInstance(final long seed) {
                return wrapper.apply(provider.createInstance(seed));
            }

            @Override
            public @NonNull W createSingleton() {
                return wrapper.apply(provider.createSingleton());
            }

            @Override
            public @NonNull W createInstanceWithUniqueElements() {
                return wrapper.apply(provider.createInstanceWithUniqueElements());
            }

            @Override
            public @NonNull W createInstanceWithUniqueElements(final int size) {
                return wrapper.apply(provider.createInstanceWithUniqueElements(size));
            }

            @Override
            public @NonNull W createInstanceWithUniqueElements(final int size, final int seed) {
                return wrapper.apply(provider.createInstanceWithUniqueElements(size, seed));
            }

            @Override
            public @NonNull W createInstance(final @NonNull E[] elements) {
                return wrapper.apply(provider.createInstance(elements));
            }

            @Override
            public @NonNull W createSingleton(final E e) {
                return wrapper.apply(provider.createSingleton(e));
            }
        }
}
