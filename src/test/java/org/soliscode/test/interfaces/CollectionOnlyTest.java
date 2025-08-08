package org.soliscode.test.interfaces;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.assertions.Assertions;
import org.soliscode.test.contract.collection.CollectionContract;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.provider.CollectionProvider;
import org.soliscode.test.provider.FunctionalCollectionProvider;

import java.util.ArrayList;
import java.util.Collection;

/// Tests for the [CollectionOnly] class.
///
/// @author evanbergstrom
/// @since 1.0
/// @see CollectionOnly
@DisplayName("Tests for the CollectionOnly class")
public class CollectionOnlyTest extends AbstractTest implements CollectionContract<Integer, CollectionOnly<Integer>>,
        WithIntegerElement {

    @Override
    public @NotNull CollectionProvider<Integer, CollectionOnly<Integer>> provider() {
        return new FunctionalCollectionProvider<>(CollectionOnly::new, CollectionOnly::new,
                (c) -> new CollectionOnly<>(new ArrayList<>(c)), elementProvider());
    }

    /// Test that `CollectionOnly` only implements the `Collection` interface.
    @Test
    @DisplayName("CollectionOnly only implements the Collection` interface.")
    public void testCollectionIsOnlyInterface() {
        Collection<Integer> iterable = new CollectionOnly<>();
        Assertions.assertImplementsOnly(Collection.class, iterable);
    }
}
