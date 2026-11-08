package com.soliscode.test.contract.iterable;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.contract.support.WithIntegerElement;
import com.soliscode.test.interfaces.IterableOnly;
import com.soliscode.test.provider.CollectionProvider;
import com.soliscode.test.provider.FunctionalCollectionProvider;

import static com.soliscode.test.assertions.collection.CollectionAssertions.assertContainsAll;
import static com.soliscode.test.assertions.collection.CollectionAssertions.assertIsEmpty;

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
public class IterableOnlyContractTest extends AbstractTest
        implements IterableContract<Iterable<Integer>, Integer>, WithIntegerElement {

    /// Default constructor.
    public IterableOnlyContractTest() {
        super();
    }

    /// Returns the objectProvider for creating and populating `IterableOnly` instances.
    ///
    /// This implementation uses [FunctionalCollectionProvider] to wrap [IterableOnly]
    /// constructors and factory methods for testing.
    ///
    /// @return a [CollectionProvider] for `IterableOnly`
    @Override
    public CollectionProvider<Iterable<Integer>, Integer> provider() {
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
