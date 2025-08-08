package org.soliscode.test.util;

import java.util.*;
import java.util.function.Consumer;

/// Collects the arguments for each call to the [accept()][Consumer#accept] method.
///
/// @param <E> The type of the parameter for the `accept` method.
///
/// @author evanbergstrom
/// @since 1.0
public class CollectingConsumer<E> implements Consumer<E> {

    private final List<E> collected = new ArrayList<>();

    /// Collects the argument provided to this method.
    /// @param e The argument to collect.
    @Override
    public void accept(final E e) {
        collected.add(e);
    }

    /// Returns all the arguments provided to the `accept` method in order.
    /// @return A [List] of the arguments to the accept method.
    public List<E> toList() {
        return collected;
    }

    /// Returns all the arguments provided to the `accept` method.
    /// @return A [Set] of the arguments to the accept method.
    public Set<E> toSet() {
        return new HashSet<>(collected);
    }
}
