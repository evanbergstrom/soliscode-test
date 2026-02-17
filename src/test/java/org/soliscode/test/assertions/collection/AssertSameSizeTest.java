package org.soliscode.test.assertions.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.interfaces.IterableOnly;
import org.soliscode.test.util.IterableTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertSameSize;

/**
 * Test Suite for assertSameSize Collection Assertion
 *
 * <p>This test class validates the behavior of the {@code assertSameSize} assertion,
 * ensuring it correctly identifies when two {@link Iterable} or {@link java.util.Collection}
 * instances have the same size, while handling various edge cases like empty collections,
 * null inputs, and size mismatches with custom messages.
 *
 * @author evanbergstrom
 * @since 1.0.0
 */
@DisplayName("Tests for AssertIsSame class")
public class AssertSameSizeTest {

    private static final String TEST_MESSAGE = "Test message";

    /**
     * Verifies that {@code assertSameSize} correctly handles empty collections.
     *
     * <p>It ensures that two empty collections are considered to have the same size,
     * and that a non-empty collection compared to an empty one results in an
     * {@link AssertionFailedError}.
     */
    @Test
    public void testAssertSameSizeOnEmptyCollection() {
        Iterable<Integer> empty1 = IterableTestUtils.empty();
        assertSameSize(empty1, empty1);
        assertSameSize(empty1, empty1, TEST_MESSAGE);
        assertSameSize(empty1, empty1, () -> TEST_MESSAGE);

        Iterable<Integer> empty2 = IterableTestUtils.empty();
        CollectionAssertions.assertSameSize(empty1, empty2);
        CollectionAssertions.assertSameSize(empty1, empty2, TEST_MESSAGE);
        CollectionAssertions.assertSameSize(empty1, empty2, () -> TEST_MESSAGE);

        Iterable<Integer> nonEmpty = IterableOnly.of(1);
        assertThrows(AssertionFailedError.class, () -> CollectionAssertions.assertSameSize(empty1, nonEmpty));

        assertThrows(AssertionFailedError.class, () -> CollectionAssertions.assertSameSize(nonEmpty, empty1));
    }

    /**
     * Verifies that {@code assertSameSize} correctly identifies collections with the same size.
     *
     * <p>It ensures that collections with the same number of elements pass the assertion,
     * while collections with different sizes cause an {@link AssertionFailedError}.
     */
    @Test
    public void testAssertSameSizeOnNonEmptyCollection() {
        Iterable<Integer> list1 = IterableOnly.of(1);
        Iterable<Integer> list2 = IterableOnly.of(2);
        Iterable<Integer> list3 = IterableOnly.of(4, 5);

        assertDoesNotThrow(() -> assertSameSize(list1, list2));

        assertThrows(AssertionFailedError.class, () -> assertSameSize(list1, list3));
    }

    /**
     * Verifies that {@code assertSameSize} throws {@link NullPointerException} when either
     * collection is null.
     */
    @Test
    public void testAssertSameSizeThrowsOnNullCollection() {
        Iterable<Integer> list1 = IterableOnly.of(1);

        assertThrows(NullPointerException.class, () -> assertSameSize(list1, null));

        assertThrows(NullPointerException.class, () -> assertSameSize(null, list1));

        assertThrows(NullPointerException.class, () -> assertSameSize(null, null));
    }

    /**
     * Verifies that {@code assertSameSize} includes the provided failure message
     * when the assertion fails due to size mismatch.
     */
    @Test
    public void testAssertSameSizeWithMessage() {
        assertThrows(AssertionFailedError.class,
                () -> assertSameSize(IterableOnly.of(1), IterableOnly.of(1, 2), TEST_MESSAGE), TEST_MESSAGE);
    }

    /**
     * Verifies that {@code assertSameSize} includes the message from the provided
     * supplier when the assertion fails due to size mismatch.
     */
    @Test
    public void testAssertSameSizeWithMessageSupplier() {
        assertThrows(AssertionFailedError.class,
                () -> assertSameSize(IterableOnly.of(1), IterableOnly.of(1, 2), () -> TEST_MESSAGE), TEST_MESSAGE);
    }
}
