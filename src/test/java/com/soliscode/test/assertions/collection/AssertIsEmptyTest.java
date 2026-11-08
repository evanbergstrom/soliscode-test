package com.soliscode.test.assertions.collection;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import com.soliscode.test.interfaces.IterableOnly;

import static com.soliscode.test.assertions.collection.CollectionAssertions.assertIsEmpty;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Test Suite for assertIsEmpty Collection Assertion
 *
 * <p>This test class validates the behavior of the {@code assertIsEmpty} assertion,
 * ensuring it correctly identifies when an {@link Iterable} or {@link java.util.Collection}
 * is empty, while handling various failure scenarios with custom messages.
 *
 * @author evanbergstrom
 * @since 1.0.0
 */
@DisplayName("Tests for AssertIsEmpty class")
public class AssertIsEmptyTest {

    private static final String TEST_MESSAGE = "Test message";

    /// Default constructor.
    public AssertIsEmptyTest() {}

    /**
     * Verifies that {@code assertIsEmpty} correctly identifies empty iterables.
     *
     * <p>It ensures that empty {@link Iterable} instances are correctly identified
     * as empty, and that non-empty iterables cause an {@link AssertionFailedError}.
     * Tests cover basic assertion, assertion with custom message, and assertion
     * with message supplier.
     */
    @Test
    @DisplayName("assertIsEmpty passes for an empty iterable and fails for a non-empty one, with each message variant")
    public void testAssertIsEmptyOnIterable() {

        Iterable<Integer> empty = IterableOnly.of();
        assertIsEmpty(empty);
        assertIsEmpty(empty, TEST_MESSAGE);
        assertIsEmpty(empty, () -> TEST_MESSAGE);

        Iterable<Integer> notEmpty = IterableOnly.of(1);
        assertThrows(AssertionFailedError.class, () -> assertIsEmpty(notEmpty));

        assertThrows(AssertionFailedError.class, () -> assertIsEmpty(notEmpty, TEST_MESSAGE));

        assertThrows(AssertionFailedError.class, () -> assertIsEmpty(notEmpty, () -> TEST_MESSAGE));
    }

    /**
     * Verifies that {@code assertIsEmpty} correctly identifies empty collections.
     *
     * <p>It ensures that empty {@link java.util.Collection} instances are correctly identified
     * as empty, and that non-empty collections cause an {@link AssertionFailedError}.
     * Tests cover basic assertion, assertion with custom message, and assertion
     * with message supplier.
     */
    @Test
    @DisplayName("assertIsEmpty passes for an empty collection, fails for a non-empty one, with each message variant")
    public void testAssertIsEmptyOnCollection() {

        Iterable<Integer> empty = Collections.emptyList();
        assertIsEmpty(empty);
        assertIsEmpty(empty, TEST_MESSAGE);
        assertIsEmpty(empty, () -> TEST_MESSAGE);

        Iterable<Integer> notEmpty = List.of(1);
        assertThrows(AssertionFailedError.class, () -> assertIsEmpty(notEmpty));

        assertThrows(AssertionFailedError.class, () -> assertIsEmpty(notEmpty, TEST_MESSAGE));

        assertThrows(AssertionFailedError.class, () -> assertIsEmpty(notEmpty, () -> TEST_MESSAGE));
    }

    /**
     * Verifies that {@code assertIsEmpty} uses the message provided by a supplier
     * when the assertion fails.
     */
    @Test
    @DisplayName("assertIsEmpty with a message supplier fails with the supplied message")
    public void testAssertIsEmptyWithMessageSupplier() {
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> assertIsEmpty(IterableOnly.of(1), () -> TEST_MESSAGE));
        assertTrue(String.valueOf(error.getMessage()).contains(TEST_MESSAGE),
                () -> "the failure's message should contain the supplied message: " + error.getMessage());
    }
}
