package org.soliscode.test.util;

import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Supplier;

/**
 * A specialized {@link Supplier} that records all objects it provides.
 * <p>
 * This interface is useful in testing scenarios where you need to verify which
 * instances were created or supplied during a test execution.
 *
 * @param <T> the type of objects supplied
 * @author evanbergstrom
 * @since 1.0.0
 */
public interface RecordingSupplier<T> extends Supplier<T> {

    /**
     * Returns a list of all objects that have been supplied by this supplier.
     * <p>
     * The returned list contains the elements in the order they were supplied.
     * Depending on the implementation, this list may be a live view or a copy,
     * and it may or may not be thread-safe.
     *
     * @return a list of recorded objects
     */
    @NonNull List<T> recorded();
}
