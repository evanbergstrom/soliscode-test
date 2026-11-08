package com.soliscode.test.breakable;


import java.util.Map;

/// A builder for breakable maps.
///
/// It adds to [BreakableBuilder] a way to build a map that holds given entries as well as those added to the
/// builder, which is what a map provider needs to create a map from another map.
///
/// @param <M> the type of breakable map built
/// @param <K> the type of keys
/// @param <V> the type of values
/// @since 1.0.0
public interface BreakableMapBuilder<M extends Breakable & Map<K, V>, K, V> extends BreakableBuilder<M> {

    /// Builds a breakable map that holds the builder's entries and then `entries`.
    ///
    /// The map built does not share state with the builder or with `entries`.
    ///
    /// @param entries the entries to add after the builder's own; must not be null
    /// @return a new breakable map
    M build(Map<K, V> entries);
}
