package com.soliscode.test.breakable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.MethodStatus;
import com.soliscode.test.provider.CollectionProvider;
import com.soliscode.test.provider.CollectionProviders;
import com.soliscode.test.provider.MapProvider;
import com.soliscode.test.provider.MapProviders;
import com.soliscode.test.provider.ObjectProvider;

/// A builder of breakable objects.
///
/// A builder holds the breaks and method statuses that the objects it builds will have. Each object it builds has its
/// own copy of them, so that changing the builder, or one object, does not change another.
///
/// @param <C> the type of breakable object built
/// @since 1.0.0
public interface BreakableBuilder<C extends Breakable> {
    /// Returns this builder instance for method chaining.
    ///
    /// This method is used by the fluent API to return the correctly typed builder instance from methods defined in
    /// this abstract class.
    ///
    /// @return this builder instance
    BreakableBuilder self();

    /// Creates a copy of this builder.
    ///
    /// @return a new builder instance that is a copy of this one
    BreakableBuilder copy();

    /// Creates a copy of a breakable object, with its own copy of the object's elements, breaks and statuses.
    ///
    /// @param other the object to copy
    /// @return a new breakable object
    C copy(C other);

    /// Builds the breakable object with the current configuration.
    ///
    /// @return a new breakable instance
    C build();

    /// Adds a break to this builder.
    ///
    /// @param aBreak the break to add; must not be null
    /// @return this builder for method chaining
    /// @throws NullPointerException if `aBreak` is null
    BreakableBuilder addBreak(Break aBreak);

    /// Adds a collection of breaks to this builder.
    ///
    /// @param moreBreaks the collection of breaks to add; must not be null
    /// @return this builder for method chaining
    /// @throws NullPointerException if `moreBreaks` is null
    default BreakableBuilder addBreaks(final Collection<Break> moreBreaks) {
        moreBreaks.forEach(this::addBreak);
        return self();
    }

    /// Configures the builder so that the resulting object does not support the specified method.
    ///
    /// @param method the optional method that should not be supported; must not be null
    /// @return this builder for method chaining
    /// @throws NullPointerException if `method` is null
    BreakableBuilder doesNotSupport(InterfaceMethod method);

    /// Adds a map of method status configurations to this builder.
    ///
    /// @param moreStatuses the map of method statuses to add; must not be null
    /// @return this builder for method chaining
    /// @throws NullPointerException if `moreStatuses` is null
    BreakableBuilder setMethodStatuses(Map<InterfaceMethod, MethodStatus> moreStatuses);

    /// Returns the breaks that have been added to the builder.
    ///
    /// @return the set of breaks for the breakable object
    /// @since 1.0.0
    Set<Break> breaks();

    /// Returns the method status configurations that have been added to the builder.
    ///
    /// @return the map of method configurations
    /// @since 1.0.0
    Map<InterfaceMethod, MethodStatus> methodStatuses();

    /// Sets whether the resulting object is safe for concurrent access.
    ///
    /// @param newSafe whether the resulting object is safe for concurrent access
    /// @return this builder for method chaining
    BreakableBuilder setSafe(boolean newSafe);

    /// Returns whether the resulting object is safe for concurrent access.
    ///
    /// @return whether the resulting object is safe for concurrent access
    boolean isSafe();

    /// Returns an object provider whose instances are built by a builder.
    ///
    /// @param <T> the type of breakable object
    /// @param builder the builder that builds each instance
    /// @return an object provider
    static <T extends Breakable> ObjectProvider<T> objectProvider(final BreakableBuilder<T> builder) {
        return new ObjectProvider<T>() {
            @Override
            public T defaultInstance() {
                return builder.build();
            }

            @Override
            public T createInstance(final long seed) {
                return builder.build();
            }

            @Override
            public T copyInstance(final T other) {
                return builder.copy(other);
            }
        };
    }

    /// Returns a collection provider whose instances are built by a builder.
    ///
    /// An instance created from another iterable reads that iterable's elements without its breaks.
    ///
    /// @param <I> the type of breakable iterable
    /// @param <E> the type of elements
    /// @param builder the builder that builds each instance
    /// @param elementProvider the provider of elements
    /// @return a collection provider
    static <I extends Iterable<E> & Breakable, E> CollectionProvider<I, E> collectionProvider(
            final BreakableCollectionBuilder<I, E> builder,
            final ObjectProvider<E> elementProvider) {
        return CollectionProviders.from(
                builder::build,
                builder::copy,
                (c) -> builder.build(Breakables.ensureUnbroken(c)),
                elementProvider);
    }

    /// Returns a map provider whose instances are built by a builder.
    ///
    /// @param <M> the type of breakable map
    /// @param <K> the type of keys
    /// @param <V> the type of values
    /// @param builder the builder that builds each instance
    /// @param keyProvider the provider of keys
    /// @param valueProvider the provider of values
    /// @return a map provider
    static <M extends Map<K, V> & Breakable, K, V> MapProvider<K, V, M> mapProvider(
            final BreakableMapBuilder<M, K, V> builder,
            final ObjectProvider<K> keyProvider,
            final ObjectProvider<V> valueProvider) {
        return MapProviders.from(builder::build, builder::copy, builder::build, keyProvider, valueProvider);
    }
}
