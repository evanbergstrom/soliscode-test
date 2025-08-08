package org.soliscode.test.interfaces;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.assertions.Assertions;
import org.soliscode.test.contract.iterable.IterableContract;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.provider.CollectionProvider;
import org.soliscode.test.provider.FunctionalCollectionProvider;

import java.util.ArrayList;
import java.util.Collection;


/// Tests for the [IterableOnly] class.
///
/// @author evanbergstrom
/// @since 1.0
/// @see IterableOnly
@DisplayName("Tests for the IterableOnly class")
public class IterableOnlyTest extends AbstractTest implements IterableContract<Integer, IterableOnly<Integer>>,
        WithIntegerElement {

    @Override
    public @NotNull CollectionProvider<Integer, IterableOnly<Integer>> provider() {
        return new FunctionalCollectionProvider<>(IterableOnly::new, IterableOnly::new,
                (c) -> new IterableOnly<>(new ArrayList<>(c)), elementProvider());
    }

    /// Test that `IterableOnly` only implements the `Iterable` interface.
    @Test
    @DisplayName("IterableOnly only implements the Iterable interface.")
    public void testIterableIsOnlyInterface() {
        Iterable<Integer> iterable = new IterableOnly<>();
        Assertions.assertImplementsOnly(Iterable.class, iterable);
    }
}
