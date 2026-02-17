package org.soliscode.test.contract.iterable;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.interfaces.IterableOnly;
import org.soliscode.test.provider.CollectionProvider;
import org.soliscode.test.provider.FunctionalCollectionProvider;

import java.util.ArrayList;
import java.util.List;

import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContainsAll;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertIsEmpty;

/// **Contract-based tests for `IterableOnly`**
///
/// This class provides the `IterableOnly`-specific implementation of the [IterableContract],
/// ensuring that [IterableOnly] correctly adheres to the [Iterable] specification
/// within the SolisCode test framework.
///
/// ## Test Scope
/// This class tests all methods defined in the [Iterable] interface as implemented
/// by [IterableOnly], including:
/// - Iterator creation and traversal
/// - Correct element sequencing
/// - Static factory methods (`of`)
///
/// ## Configuration
/// The tests use [Integer] elements.
///
/// @author evanbergstrom
/// @see IterableOnly
/// @see IterableContract
/// @since 1.0.0
@DisplayName("Tests for the IterableOnly class")
public class TestableIterableOnlyTestContract extends AbstractTest
        implements IterableContract<Integer, Iterable<Integer>>, WithIntegerElement {

    /// Returns the provider for creating and populating `IterableOnly` instances.
    ///
    /// This implementation uses [FunctionalCollectionProvider] to wrap [IterableOnly]
    /// constructors and factory methods for testing.
    ///
    /// @return a [CollectionProvider] for `IterableOnly`
    @Override
    public @NonNull CollectionProvider<Integer, Iterable<Integer>> provider() {
        return new FunctionalCollectionProvider<>(IterableOnly::new, IterableOnly::new,
                (c) -> new IterableOnly<>(new ArrayList<>(c)), elementProvider());
    }


    /// Tests that the zero-argument `of()` factory method creates an empty iterable.
    @Test
    @DisplayName("Test that of with no parameters creates an empty iterable")
    public void testOf0() {
        Iterable<Integer> i = IterableOnly.of();
        assertIsEmpty(i);
    }

    /// Tests that the one-argument `of(E)` factory method creates an iterable with the expected element.
    @Test
    @DisplayName("Test that of with one parameters creates an iterable with one element")
    public void testOf1() {
        Iterable<Integer> i = IterableOnly.of(1);
        assertContainsAll(List.of(1), i);
    }

    /// Tests that the two-argument `of(E, E)` factory method creates an iterable with the expected elements.
    @Test
    @DisplayName("Test that of with two parameters creates an iterable with two elements")
    public void testOf2() {
        Iterable<Integer> i = IterableOnly.of(1, 2);
        assertContainsAll(List.of(1, 2), i);
    }

    /// Tests that the three-argument `of(E, E, E)` factory method creates an iterable with the expected elements.
    @Test
    @DisplayName("Test that of with three parameters creates an iterable with three elements")
    public void testOf3() {
        Iterable<Integer> i = IterableOnly.of(1, 2, 3);
        assertContainsAll(List.of(1, 2, 3), i);
    }

    /// Tests that the four-argument `of(E, E, E, E)` factory method creates an iterable with the expected elements.
    @Test
    @DisplayName("Test that of with four parameters creates an iterable with four elements")
    public void testOf4() {
        Iterable<Integer> i = IterableOnly.of(1, 2, 3, 4);
        assertContainsAll(List.of(1, 2, 3, 4), i);
    }

    /// Tests that the varargs `of(E...)` factory method works correctly with an array of elements.
    @Test
    @DisplayName("Test that of works with an array of integer")
    public void testOfWithArray() {
        Iterable<Integer> i = IterableOnly.of(1, 2, 3, 4, 5, 6);
        assertContainsAll(List.of(1, 2, 3, 4, 5, 6), i);
    }
}
