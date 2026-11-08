package org.soliscode.test.breakable;

import org.jspecify.annotations.NonNull;

/// A builder for breakable iterables and collections.
///
/// It adds to [BreakableBuilder] a way to build an object that holds given elements as well as those added to the
/// builder, which is what a collection provider needs to create a collection from another iterable.
///
/// @param <C> the type of breakable object built
/// @param <E> the type of elements
/// @since 1.0.0
public interface BreakableCollectionBuilder<C extends Breakable, E>
        extends BreakableBuilder<C> {

    /// Builds a breakable object that holds the builder's elements and then those of `i`.
    ///
    /// The object built does not share state with the builder or with `i`.
    ///
    /// @param i the elements to add after the builder's own; must not be null
    /// @return a new breakable object
    @NonNull C build(@NonNull Iterable<E> i);
}
