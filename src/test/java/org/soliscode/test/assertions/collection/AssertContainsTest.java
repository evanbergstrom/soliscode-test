package org.soliscode.test.assertions.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.interfaces.IterableOnly;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContains;

/**
 * Test Suite for assertContains Collection Assertion
 *
 * <p>This test class validates the behavior of the {@code assertContains} assertion,
 * ensuring it correctly identifies when a collection contains a specific element,
 * while handling various edge cases like empty collections and null inputs.
 *
 * @author evanbergstrom
 * @since 1.0.0
 */
@DisplayName("Tests for assertContains")
public class AssertContainsTest {
    private static final String TEST_MESSAGE = "Test message";

    /**
     * Verifies that {@code assertContains} correctly identifies elements within an {@link Iterable}.
     *
     * <p>It ensures that elements present in the iterable are correctly identified,
     * including tests with custom failure messages and message suppliers, and that
     * elements not present in the iterable cause an {@link AssertionFailedError}.
     */
    @DisplayName("Test assertContains with an iterable")
    @Test
    void testAssertContainsOnIterable() {
        Iterable<Integer> iterable = IterableOnly.of(1,2,3);
        assertContains(1, iterable);
        assertContains(1, iterable, TEST_MESSAGE);
        assertContains(1, iterable, () -> TEST_MESSAGE);

        assertContains(2, iterable);
        assertContains(3, iterable);

        assertThrows(AssertionFailedError.class, () -> assertContains(4, iterable));
    }

    /**
     * Verifies that {@code assertContains} correctly identifies elements within a {@link java.util.Collection}.
     *
     * <p>It ensures that elements present in the collection are correctly identified,
     * including tests with custom failure messages and message suppliers, and that
     * elements not present in the collection cause an {@link AssertionFailedError}.
     */
    @DisplayName("Test assertContains with a collection")
    @Test
    void testAssertContainsOnCollection() {
        Iterable<Integer> collection = List.of(1, 2, 3);
        assertContains(1, collection);
        assertContains(1, collection, TEST_MESSAGE);
        assertContains(1, collection, () -> TEST_MESSAGE);

        assertContains(2, collection);
        assertContains(3, collection);

        assertThrows(AssertionFailedError.class, () -> assertContains(4, collection));
    }

    /**
     * Verifies that {@code assertContains} throws {@link AssertionFailedError} when
     * searching for any element within an empty collection.
     */
    @DisplayName("Test assertContains with an empty collection")
    @Test
    public void testAssertContainsOnEmptyCollection() {

        Iterable<Integer> iterable = Collections.emptyList();
        assertThrows(AssertionFailedError.class, () -> CollectionAssertions.assertContains(1, iterable));
    }

    /**
     * Verifies that {@code assertContains} throws {@link NullPointerException} when
     * the collection argument is null.
     */
    @DisplayName("Test assertContains with a null collection")
    @Test
    public void testAssertContainsOnNullCollection() {

        assertThrows(NullPointerException.class, () -> CollectionAssertions.assertContains(1, null));
    }
}
