package org.soliscode.test.interfaces;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.collection.CollectionContract;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.provider.CollectionProvider;
import org.soliscode.test.provider.FunctionalCollectionProvider;

import java.util.*;

import static org.soliscode.test.assertions.Assertions.assertImplementsOnly;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertEquals;
import static org.soliscode.test.interfaces.Interfaces.narrowToCollection;

/// Tests for the [CollectionOnly] class.
///
/// @author evanbergstrom
/// @since 1.0
/// @see CollectionOnly
@DisplayName("Tests for the CollectionOnly class")
public class CollectionOnlyTest extends AbstractTest implements CollectionContract<Integer, CollectionOnly<Integer>>,
        WithIntegerElement {

    @Override
    public @NonNull CollectionProvider<Integer, CollectionOnly<Integer>> provider() {
        return new FunctionalCollectionProvider<>(CollectionOnly::new, CollectionOnly::new,
                (c) -> narrowToCollection(new ArrayList<>(c)), elementProvider());
    }

    /// Test that `CollectionOnly` only implements the `Collection` interface.
    @Test
    @DisplayName("CollectionOnly only implements the Collection interface.")
    public void testCollectionIsOnlyInterface() {
        Collection<Integer> iterable = new CollectionOnly<>();
        assertImplementsOnly(Collection.class, iterable);
    }

    /// Test that of() creates a collection that only implements the Collection interface.
    @Test
    @DisplayName("of() creates a collection that only implements the Collection interface.")
    public void testOfCreatesCollectionOnlyInterface() {
        Collection<Integer> empty = CollectionOnly.of();
        assertImplementsOnly(Collection.class, empty);
        assertEquals(List.of(), empty);

        Collection<Integer> one = CollectionOnly.of(1);
        assertImplementsOnly(Collection.class, one);
        assertEquals(List.of(1), one);

        Collection<Integer> two = CollectionOnly.of(1, 2);
        assertImplementsOnly(Collection.class, two);
        assertEquals(List.of(1,2), two);

        Collection<Integer> three = CollectionOnly.of(1, 2, 3);
        assertImplementsOnly(Collection.class, three);
        assertEquals(List.of(1,2,3), three);

        Collection<Integer> four = CollectionOnly.of(1, 2, 3, 4);
        assertImplementsOnly(Collection.class, four);
        assertEquals(List.of(1,2,3,4), four);

        Integer[] fiveElements = new Integer[] {1, 2, 3, 4, 5} ;
        Collection<Integer> five = CollectionOnly.of(fiveElements);
        assertImplementsOnly(Collection.class, five);
        assertEquals(List.of(1,2,3,4,5), five);
    }
}
