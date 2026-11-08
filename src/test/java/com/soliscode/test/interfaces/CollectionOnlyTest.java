package com.soliscode.test.interfaces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.contract.collection.CollectionContract;
import com.soliscode.test.contract.support.WithIntegerElement;
import com.soliscode.test.provider.CollectionProvider;
import com.soliscode.test.provider.FunctionalCollectionProvider;

import static com.soliscode.test.assertions.Assertions.assertImplementsOnly;
import static com.soliscode.test.assertions.collection.CollectionAssertions.assertEquals;
import static com.soliscode.test.interfaces.Interfaces.narrowToCollection;

/// Tests for the [CollectionOnly] class.
///
/// @author evanbergstrom
/// @since 1.0
/// @see CollectionOnly
@DisplayName("Tests for the CollectionOnly class")
public class CollectionOnlyTest extends AbstractTest implements CollectionContract<CollectionOnly<Integer>, Integer>,
        WithIntegerElement {

    /// Default constructor.
    public CollectionOnlyTest() {
        super();
    }

    @Override
    public CollectionProvider<CollectionOnly<Integer>, Integer> provider() {
        return new FunctionalCollectionProvider<>(CollectionOnly::new, CollectionOnly::new,
                (c) -> narrowToCollection(new ArrayList<>(c)), elementProvider());
    }

    /// Verifies that [CollectionOnly] only implements the [Collection] interface.
    ///
    /// @see CollectionOnly
    @DisplayName("the constructor returns an object that implements only Collection")
    @Test
    public void constructorWhenCalledReturnsCollectionThatImplementsOnlyCollection() {
        Collection<Integer> iterable = new CollectionOnly<>();
        assertImplementsOnly(Collection.class, iterable);
    }

    /// Verifies that [CollectionOnly#of] creates a collection that only implements the [Collection] interface.
    ///
    /// @see CollectionOnly#of
    @DisplayName("of() returns an object that implements only Collection")
    @Test
    public void ofWhenCalledReturnsCollectionThatImplementsOnlyCollection() {
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
