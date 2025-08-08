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
import org.soliscode.test.provider.*;
import org.soliscode.test.util.IterableTestOps;

import java.util.*;
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

    protected static final int DEFAULT_CAPACITY = 10;

    private final @NotNull Iterable<E> iterable;
    private final int characteristics;

    /// The method `forEach` does not call the action on any of the elements.
    /// @see BreakableIterable#forEach(Consumer)
    public static final Break FOR_EACH_DOES_NOT_CALL_ACTION = new Break("The method `forEach` does not call the action on any of the elements");

    /// The `forEach` method does not call the action on the first element.
    /// @see BreakableIterable#forEach(Consumer)
    public static final Break FOR_EACH_SKIPS_FIRST_ELEMENT = new Break("The `forEach` method does not call the action on the first element");

    /// The method `forEach` does not call the action on the last element.
    /// @see BreakableIterable#forEach(Consumer)
    public static final Break FOR_EACH_SKIPS_LAST_ELEMENT = new Break("The method `forEach` does not call the action on the last element.");

    /// The method `forEach` throws the wrong exception when passes a `null` action argument.
    /// @see BreakableIterable#forEach(Consumer)
    public static final Break FOR_EACH_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT = new Break("The method `forEach` throws the wrong exception when passes a `null` action argument.");

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
    public boolean equals(Object obj) {
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
    public Spliterator<E> spliterator() {
        return new BreakableSpliterator<>(iterable.spliterator(), breaks(), characteristics);
    }

    /// returns the elements as an unbroken instance of `Iterable'
    /// @return an unbroken iterable.
    public @NotNull Iterable<E> unbroken() {
        return iterable;
    }

    /// Utility class for use by subclasses of `BreakableIterator` to make implementing a builder class easier.
    /// @author evanbergstrom
    /// @since 1.0
    protected static abstract class AbstractBuilder<B extends AbstractBuilder<B, C, E>, C extends BreakableIterable<E>, E> {

        protected final @NotNull Collection<E> elements;
        protected final @NotNull Set<Break> breaks;
        protected final @NotNull Set<OptionalMethod> unsupportedMethods;
        protected int characteristics;

        public AbstractBuilder() {
            this(new ArrayList<>());
        }

        public AbstractBuilder(final @NotNull Collection<E> elements) {
            this.elements = elements;
            this.breaks = new HashSet<>();
            this.unsupportedMethods = new HashSet<>();
            this.characteristics = 0;
        }

        protected AbstractBuilder(final @NotNull AbstractBuilder<B, C, E> other) {
            this.elements = new ArrayList<>(other.elements);
            this.breaks = new HashSet<>(other.breaks);
            this.unsupportedMethods = new HashSet<>(other.unsupportedMethods);
            this.characteristics = other.characteristics;
        }

        abstract public B self();

        abstract public B copy();

        abstract public C build();

        public final B addBreak(final @NotNull Break aBreak) {
            breaks.add(aBreak);
            return self();
        }

        public final B setCharacteristics(final int characteristics) {
            this.characteristics = this.characteristics | characteristics;
            return self();
        }

        public final B addElements(final @NotNull Iterable<E> i) {
            for(E e : i) {
                elements.add(e);
            }
            return self();
        }

        @SafeVarargs
        public final B addElements(E... e) {
            Collections.addAll(elements, e);
            return self();
        }

        public final B doesNotSupport(final @NotNull OptionalMethod method) {
            unsupportedMethods.add(method);
            return self();
        }
    }

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

        @Override
        public BreakableIterable<E> build() {
            BreakableIterable<E> iterator = new BreakableIterable<>(elements, breaks, characteristics);
            unsupportedMethods.forEach(iterator::doesNotSupportMethod);
            return iterator;
        }
    }

    public static <E> @NotNull CollectionProvider<E, BreakableIterable<E>> iterableProvider(final @NotNull ObjectProvider<E> elementProvider) {
        return CollectionProviders.from(
                BreakableIterable::new,
                BreakableIterable::new,
                BreakableIterable::new,
                elementProvider
        );
    }

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

    public interface WithProvider<E> extends CollectionProviderSupport<E, BreakableIterable<E>>  {
        @Override
        public default @NotNull CollectionProvider<E, BreakableIterable<E>> provider() {
            return BreakableIterable.iterableProvider(elementProvider());
        }
    }
}
