package org.soliscode.test.util;

import java.util.function.Consumer;

/// Counts how many times the [accept()][Consumer#accept] method is called.
///
/// @param <T> The type of the parameter for the `accept` method.
/// @author evanbergstrom
/// @since 1.0
public class CountingConsumer<T> implements Consumer<T> {

    private int count = 0;

    /// Counts each time this method is called.
    /// @param t This parameter is ignored.
    @Override
    public void accept(final T t) {
        count++;
    }

    /// Returns the number of times the `accept` method has been called.
    /// @return the number of times the `accept` method has been called.
    public int count() {
        return count;
    }
}
