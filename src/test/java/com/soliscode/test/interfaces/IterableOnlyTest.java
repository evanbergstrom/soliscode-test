package com.soliscode.test.interfaces;

import java.util.ArrayList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.assertions.Assertions;
import com.soliscode.test.contract.iterable.IterableContract;
import com.soliscode.test.contract.support.WithIntegerElement;
import com.soliscode.test.provider.CollectionProvider;
import com.soliscode.test.provider.FunctionalCollectionProvider;


/// Tests for the [IterableOnly] class.
///
/// @author evanbergstrom
/// @since 1.0
/// @see IterableOnly
@DisplayName("Tests for the IterableOnly class")
public class IterableOnlyTest extends AbstractTest implements IterableContract<IterableOnly<Integer>, Integer>,
        WithIntegerElement {

    /// Default constructor.
    public IterableOnlyTest() {
        super();
    }


    @Override
    public CollectionProvider<IterableOnly<Integer>, Integer> provider() {
        return new FunctionalCollectionProvider<>(IterableOnly::new, IterableOnly::new,
                (c) -> new IterableOnly<>(new ArrayList<>(c)), elementProvider());
    }

    /// Verifies that [IterableOnly] only implements the [Iterable] interface.
    ///
    /// @see IterableOnly
    @DisplayName("the constructor returns an object that implements only Iterable")
    @Test
    public void constructorWhenCalledReturnsIterableThatImplementsOnlyIterable() {
        Iterable<Integer> iterable = new IterableOnly<>();
        Assertions.assertImplementsOnly(Iterable.class, iterable);
    }
}
