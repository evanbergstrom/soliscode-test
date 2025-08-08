package org.soliscode.test.collection;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.iterable.IterableContract;
import org.soliscode.test.interfaces.IterableOnly;
import org.soliscode.test.provider.CollectionProvider;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.provider.FunctionalCollectionProvider;

import java.util.ArrayList;
import java.util.List;

import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContainsAll;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertIsEmpty;

@DisplayName("Tests for the IterableOnly class")
public class TestableIterableOnlyTestContract extends AbstractTest
        implements IterableContract<Integer, Iterable<Integer>>, WithIntegerElement {

    @Override
    public @NotNull CollectionProvider<Integer, Iterable<Integer>> provider() {
        return new FunctionalCollectionProvider<>(IterableOnly::new, IterableOnly::new,
                (c) -> new IterableOnly<>(new ArrayList<>(c)), elementProvider());
    }


    @Test
    @DisplayName("Test that of with no parameters creates an empty iterable")
    public void testOf0() {
        Iterable<Integer> i = IterableOnly.of();
        assertIsEmpty(i);
    }

    @Test
    @DisplayName("Test that of with one parameters creates an iterable with one element")
    public void testOf1() {
        Iterable<Integer> i = IterableOnly.of(1);
        assertContainsAll(List.of(1), i);
    }

    @Test
    @DisplayName("Test that of with two parameters creates an iterable with two elements")
    public void testOf2() {
        Iterable<Integer> i = IterableOnly.of(1, 2);
        assertContainsAll(List.of(1, 2), i);
    }

    @Test
    @DisplayName("Test that of with three parameters creates an iterable with three elements")
    public void testOf3() {
        Iterable<Integer> i = IterableOnly.of(1, 2, 3);
        assertContainsAll(List.of(1, 2, 3), i);
    }

    @Test
    @DisplayName("Test that of with four parameters creates an iterable with four elements")
    public void testOf4() {
        Iterable<Integer> i = IterableOnly.of(1, 2, 3, 4);
        assertContainsAll(List.of(1, 2, 3, 4), i);
    }

    @Test
    @DisplayName("Test that of works with an array of integer")
    public void testOfWithArray() {
        Iterable<Integer> i = IterableOnly.of(1, 2, 3, 4, 5, 6);
        assertContainsAll(List.of(1, 2, 3, 4, 5, 6), i);
    }
}
