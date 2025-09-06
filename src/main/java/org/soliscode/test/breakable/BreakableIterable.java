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
package org.soliscode.test.breakable;

import org.jetbrains.annotations.NotNull;
import org.soliscode.test.OptionalMethod;
import org.soliscode.test.contract.support.CollectionProviderSupport;
import org.soliscode.test.provider.CollectionProvider;
import org.soliscode.test.provider.CollectionProviders;
import org.soliscode.test.provider.ObjectProvider;
import org.soliscode.test.util.IterableTestOps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

/// An iterable that can be broken in well-defined ways in order to test collection utilities or testing classes.
///
/// # Breaks
/// The breaks that are supported for this class are listed in the description of the method that they impact.
/// In addition it supports the breaks in the [BreakableIterator] and [BreakableSpliterator] classes. Any iterator or
/// spliterator breaks that are added to this iterable will be passes along to iterator or spliterator instances that
/// are created.
///
/// Any breaks that are not listed in the supported classes can be added to an instance of `BreakableIterable`, but will
/// not have any impact on how it functions.
///
/// # Builder
/// Builder methods are provided to make declaring a broken collection easier, for example:
/// ```java
///     BreakableIterator<Integer> broken = Breakable.iterableOf(1, 2)
///         .addBreak(FOR_EACH_DOES_NOT_CALL_ACTION)
///         .setCharacteristics(ORDERED | SIZED | SUBSIZED)
///         .build();
/// ```
///
/// @author evanbergstrom
/// @param <E> The elements type for the `Iterable`.
/// @since 1.0
public class BreakableIterable<E> extends AbstractBreakable implements Iterable<E> {

    /// The default capacity used for operations that need a constant size value.
    protected static final int DEFAULT_CAPACITY = 10;

    /// The underlying iterable that stores the actual elements.
    private final @NotNull Iterable<E> iterable;

    /// The characteristics flags for the spliterator created by this iterable.
    private final int characteristics;

    /// The method `forEach` does not call the action on any of the elements.
    /// @see BreakableIterable#forEach(Consumer)
    public static final Break FOR_EACH_DOES_NOT_CALL_ACTION =
            new Break("The method `forEach` does not call the action on any of the elements");

    /// The `forEach` method does not call the action on the first element.
    /// @see BreakableIterable#forEach(Consumer)
    public static final Break FOR_EACH_SKIPS_FIRST_ELEMENT =
            new Break("The `forEach` method does not call the action on the first element");

    /// The method `forEach` does not call the action on the last element.
    /// @see BreakableIterable#forEach(Consumer)
    public static final Break FOR_EACH_SKIPS_LAST_ELEMENT =
            new Break("The method `forEach` does not call the action on the last element.");

    /// The method `forEach` throws the wrong exception when passes a `null` action argument.
    /// @see BreakableIterable#forEach(Consumer)
    public static final Break FOR_EACH_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT =
            new Break("The method `forEach` throws the wrong exception when passes a `null` action argument.");

    /// Creates and empty breakable iterable with no breaks. The spliterator for this iterable will have no
    /// characteristics set.
    public BreakableIterable() {
        super();
        this.iterable = new ArrayList<>();
        this.characteristics = 0;
    }

    /// Creates a breakable iterable from an existing instance.
    /// @param other the breakable iterator to copy.
    public BreakableIterable(final @NotNull BreakableIterable<E> other) {
        super(other);
        this.iterable = IterableTestOps.newList(other.iterable);
        this.characteristics = other.characteristics;
    }

    /// Creates a breakable iterable from an iterable.
    /// @param collection the iterator to use for the elements.
    public BreakableIterable(final @NotNull Collection<E> collection) {
        this(new ArrayList<>(collection), new HashSet<>(), 0);
    }

    /// Creates a breakable iterable with a set of breaks
    /// @param breaks the set of breaks to include.
    public BreakableIterable(final @NotNull Set<Break> breaks) {
        this(new ArrayList<>(), breaks, 0);
    }

    /// Creates a breakable iterable from another iterable with a set of breaks and characteristics specified by the
    /// other arguments.
    /// @param i the iterable to use for the elements.
    /// @param breaks the set of breaks to include.
    /// @param characteristics the characteristics of the iterable.
    public BreakableIterable(final @NotNull Iterable<E> i, final @NotNull Collection<Break> breaks,
                             final int characteristics) {
        super(breaks);
        this.iterable = i;
        this.characteristics = characteristics;
    }

    /// {@inheritDoc}
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof BreakableIterable<?> other) {
            return IterableTestOps.equals(iterable, other.iterable) && characteristics == other.characteristics;
        } else {
            return false;
        }
    }

    /// {@inheritDoc}
    @Override
    public int hashCode() {
        return iterable.hashCode();
    }

    /// {@inheritDoc}
    @Override
    public String toString() {
        return iterable.toString();
    }

    /// Implements the [iterator][Iterable#iterator] method from the [Iterable] interface.
    ///
    /// #Breaks
    /// This method can be broken using any of the breaks found in the [BreakableIterator] class.
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Iterable<Integer> iterable = Breakable.iterableOf(1,2,3,4,5)
    ///         .withBreak(BreakableIterator.ITERATOR_IS_ALWAYS_EMPTY)
    ///         .build();
    /// ```
    /// @return An `Iterator` over the elements in this collection.
    @Override
    public @NotNull Iterator<E> iterator() {
        final BreakableIterator<E> iterator = new BreakableIterator<>(iterable.iterator(), breaks(), characteristics);
        unsupportedMethods().forEach(iterator::doesNotSupportMethod);
        return iterator;
    }

    /// Implements the [forEach][Iterable#forEach] method from the [Iterable] interface. This method can be broken
    /// using the following collection breaks:
    /// - [FOR_EACH_DOES_NOT_CALL_ACTION][BreakableIterable#FOR_EACH_DOES_NOT_CALL_ACTION]
    /// - [FOR_EACH_SKIPS_FIRST_ELEMENT][BreakableIterable#FOR_EACH_SKIPS_FIRST_ELEMENT]
    /// - [FOR_EACH_SKIPS_LAST_ELEMENT][BreakableIterable#FOR_EACH_SKIPS_LAST_ELEMENT]
    /// - [FOR_EACH_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT][BreakableIterable#FOR_EACH_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT]
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakable.iterableOf(1,2,3,4,5)
    ///         .withBreak(FOR_EACH_DOES_NOT_CALL_ACTION)
    ///         .build();
    /// ```
    /// @param action The action to be performed for each element
    /// @throws NullPointerException if the specified action is null
    @Override
    public void forEach(final Consumer<? super E> action) {
        if (action == null) {
            if (hasBreak(FOR_EACH_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT)) {
                throw new RuntimeException();
            }
        }
        if (!hasBreak(FOR_EACH_DOES_NOT_CALL_ACTION)) {
            if (hasBreak(FOR_EACH_SKIPS_FIRST_ELEMENT)) {
                Iterator<E> iterator = this.iterable.iterator();
                if (iterator.hasNext()) {
                    iterator.next();
                }
                while (iterator.hasNext()) {
                    action.accept(iterator.next());
                }
            } else if (hasBreak(FOR_EACH_SKIPS_LAST_ELEMENT)) {
                Iterator<E> iterator = this.iterable.iterator();
                while (iterator.hasNext()) {
                    E element = iterator.next();
                    if (iterator.hasNext()) {
                        action.accept(element);
                    }
                }
            } else {
                iterable.forEach(action);
            }
        }
    }

    /// Implements the [iterator][Iterable#spliterator] method from the [Iterable] interface. This method can be broken
    ///  using the following collection breaks:
    ///
    /// #Breaks
    /// This method can be broken using any of the breaks found in the [BreakableSpliterator] class.
    ///
    /// A iterable that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Iterable<Integer> iterable = Breakable.iterableOf()
    ///         .addElements(1,2,3,4,5)
    ///         .withBreak(SPLITERATOR_IS_ALWAYS_EMPTY)
    ///         .build();
    /// ```
    /// @return a `Spliterator` over the elements in this collection.Ø
    @Override
    public @NotNull Spliterator<E> spliterator() {
        return new BreakableSpliterator<>(iterable.spliterator(), breaks(), characteristics);
    }

    /// returns the elements as an unbroken instance of `Iterable'
    /// @return an unbroken iterable.
    public @NotNull Iterable<E> unbroken() {
        return iterable;
    }

    /// Utility class for use by subclasses of `BreakableIterator` to make implementing a builder class easier.
    /// @param <B> builder type
    /// @param <C> the subclass of BreakableIterable
    /// @param <E> element type
    /// @author evanbergstrom
    // CHECKSTYLE:OFF: VisibilityModifier
    protected abstract static class AbstractBuilder<B extends AbstractBuilder<B, C, E>,
            C extends BreakableIterable<E>, E> {

        /// The collection of elements that will be used to create the breakable iterable.
        protected final @NotNull Collection<E> elements;

        /// The set of breaks that will be applied to the breakable iterable.
        protected final @NotNull Set<Break> breaks;

        /// The set of optional methods that will not be supported by the breakable iterable.
        protected final @NotNull Set<OptionalMethod> unsupportedMethods;

        /// The characteristics flags that will be applied to the spliterator.
        protected int characteristics;

        /// Default constructor that creates an empty builder.
        /// @see AbstractBuilder(Collection)
        public AbstractBuilder() {
            this(new ArrayList<>());
        }

        /// Creates a builder with the specified initial elements.
        /// @param elements the initial elements for the builder
        /// @throws NullPointerException if elements is null
        public AbstractBuilder(final @NotNull Collection<E> elements) {
            this.elements = elements;
            this.breaks = new HashSet<>();
            this.unsupportedMethods = new HashSet<>();
            this.characteristics = 0;
        }

        /// Copy constructor that creates a builder from another builder.
        /// @param other the builder to copy from
        /// @throws NullPointerException if other is null
        protected AbstractBuilder(final @NotNull AbstractBuilder<B, C, E> other) {
            this.elements = new ArrayList<>(other.elements);
            this.breaks = new HashSet<>(other.breaks);
            this.unsupportedMethods = new HashSet<>(other.unsupportedMethods);
            this.characteristics = other.characteristics;
        }

        /// Returns this builder instance for method chaining.
        /// @return this builder instance
        public abstract B self();

        /// Creates a copy of this builder.
        /// @return a new builder instance that is a copy of this one
        public abstract B copy();

        /// Builds the breakable iterable with the current configuration.
        /// @return a new breakable iterable instance
        public abstract C build();

        /// Adds a break to this builder.
        /// @param aBreak the break to add
        /// @return this builder for method chaining
        /// @throws NullPointerException if aBreak is null
        public final B addBreak(final @NotNull Break aBreak) {
            breaks.add(aBreak);
            return self();
        }

        /// Sets the characteristics for the spliterator using bitwise OR with existing characteristics.
        /// @param newCharacteristics the characteristics to add
        /// @return this builder for method chaining
        /// @see Spliterator
        public final B setCharacteristics(final int newCharacteristics) {
            this.characteristics = this.characteristics | newCharacteristics;
            return self();
        }

        /// Adds all elements from the specified iterable to this builder.
        /// @param i the iterable containing elements to add
        /// @return this builder for method chaining
        /// @throws NullPointerException if i is null
        public final B addElements(final @NotNull Iterable<E> i) {
            for (E e : i) {
                elements.add(e);
            }
            return self();
        }

        /// Adds the specified elements to this builder.
        /// @param e the elements to add
        /// @return this builder for method chaining
        /// @throws NullPointerException if e is null
        @SafeVarargs
        public final @NotNull B addElements(final @NotNull E... e) {
            Collections.addAll(elements, e);
            return self();
        }

        /// Configures the builder so that the resulting iterable does not support the specified method.
        /// @param method the optional method that should not be supported
        /// @return this builder for method chaining
        /// @throws NullPointerException if method is null
        public final @NotNull B doesNotSupport(final @NotNull OptionalMethod method) {
            unsupportedMethods.add(method);
            return self();
        }
    }
    // CHECKSTYLE:ON: VisibilityModifier


    /// Builder class for `BreakableIterator`.
    /// @param <E> The type of the elements.
    /// @author evanbergstrom
    /// @since 1.0
    public static class Builder<E> extends AbstractBuilder<Builder<E>, BreakableIterable<E>, E> {

        /// Creates a builder for `BreakableIterator`.
        public Builder() {
            super();
        }

        /// Copies a builder for `BreakableIterator`.
        /// @param other the builder to copy.
        public Builder(final @NotNull Builder<E> other) {
            super(other);
        }

        /// {@inheritDoc}
        @Override
        public Builder<E> self() {
            return this;
        }

        /// {@inheritDoc}
        @Override
        public Builder<E> copy() {
            return new Builder<>(this);
        }

        /// Builds a new BreakableIterable instance with the current configuration.
        /// @return a new BreakableIterable instance
        @Override
        public BreakableIterable<E> build() {
            BreakableIterable<E> iterator = new BreakableIterable<>(elements, breaks, characteristics);
            unsupportedMethods.forEach(iterator::doesNotSupportMethod);
            return iterator;
        }
    }

    /// Creates a collection provider for instances of BreakableIterable, given an element provider.
    /// @param <E> the element type
    /// @param elementProvider the element provider to use
    /// @return a collection provider for breakable iterables
    /// @throws NullPointerException if elementProvider is null
    public static <E> @NotNull CollectionProvider<E, BreakableIterable<E>> iterableProvider(
            final @NotNull ObjectProvider<E> elementProvider) {
        return CollectionProviders.from(
                BreakableIterable::new,
                BreakableIterable::new,
                BreakableIterable::new,
                elementProvider
        );
    }

    /// Creates a collection provider for instances of BreakableIterable with specific breaks applied.
    /// @param <E> the element type
    /// @param elementProvider the element provider to use
    /// @param breaks the breaks to apply to each instance of BreakableIterable
    /// @return a collection provider for breakable iterables
    /// @throws NullPointerException if elementProvider or breaks is null
    public static <E> @NotNull CollectionProvider<E, BreakableIterable<E>> iterableProvider(
            final @NotNull ObjectProvider<E> elementProvider,
            final @NotNull Set<Break> breaks) {
        return CollectionProviders.from(
                () -> new BreakableIterable<E>(new ArrayList<>(), breaks, 0),
                (o) -> new BreakableIterable<E>(o.iterable, breaks, o.characteristics),
                (c) -> new BreakableIterable<E>(c, breaks, 0),
                elementProvider
        );
    }

    /// Mixin interface that adds an implementation of the `provider()` method that provides instances of
    /// `BreakableIterable` that do not have any breaks applied.
    /// @param <E> element type
    public interface WithProvider<E> extends CollectionProviderSupport<E, BreakableIterable<E>>  {
        /// Provides a collection provider for BreakableIterable instances.
        /// @return a collection provider for breakable iterables
        @Override
        default @NotNull CollectionProvider<E, BreakableIterable<E>> provider() {
            return BreakableIterable.iterableProvider(elementProvider());
        }
    }
}
