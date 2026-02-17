package org.soliscode.test.assertions.collection;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.interfaces.IterableOnly;
import org.soliscode.test.util.IterableTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContainsSame;

/**
 * Test Suite for assertContainsSame Collection Assertion
 *
 * <p>This test class validates the behavior of the {@code assertContainsSame} assertion,
 * ensuring it correctly identifies when two collections contain the same elements,
 * regardless of order, while handling various edge cases like empty collections,
 * null inputs, and duplicate elements.
 *
 * @author evanbergstrom
 * @since 1.0.0
 */
public class AssertContainsSameTest {

    private static final String TEST_MESSAGE = "Test message";

    /**
     * Verifies that {@code assertContainsSame} correctly handles empty collections.
     *
     * <p>It ensures that two empty collections are considered to contain the same elements
     * and that a non-empty collection is not considered to contain the same elements
     * as an empty one.
     */
    @Test
    public void testAssertContainsSameOnEmptyCollection() {
        Iterable<Integer> empty1 = IterableTestUtils.empty();
        assertContainsSame(empty1, empty1);
        assertContainsSame(empty1, empty1, TEST_MESSAGE);
        assertContainsSame(empty1, empty1, () -> TEST_MESSAGE);

        Iterable<Integer> empty2 = IterableTestUtils.empty();
        assertContainsSame(empty1, empty2);
        assertContainsSame(empty1, empty2, TEST_MESSAGE);
        assertContainsSame(empty1, empty2, () -> TEST_MESSAGE);

        Iterable<Integer> nonEmpty = IterableOnly.of(1);
        assertThrows(AssertionFailedError.class, () -> assertContainsSame(nonEmpty, empty1));

        assertThrows(AssertionFailedError.class, () -> assertContainsSame(nonEmpty, empty1, TEST_MESSAGE));

        assertThrows(AssertionFailedError.class, () -> assertContainsSame(nonEmpty, empty1, () -> TEST_MESSAGE));
    }

    /**
     * Verifies that {@code assertContainsSame} throws {@link NullPointerException} when either
     * the expected or the actual collection is null.
     */
    @Test
    public void testAssertContainsSameOnNullCollection() {

        Iterable<Integer> nonEmpty = IterableOnly.of(1);
        assertThrows(NullPointerException.class, () -> CollectionAssertions.assertContainsSame(nonEmpty, null));

        assertThrows(NullPointerException.class, () -> CollectionAssertions.assertContainsSame(null, nonEmpty));

        assertThrows(NullPointerException.class, () -> CollectionAssertions.assertContainsSame(null, null));
    }

    /**
     * Verifies that {@code assertContainsSame} correctly compares collections with elements.
     *
     * <p>It ensures that collections with the same elements in different orders are
     * considered the same, and that collections with missing elements, different
     * elements, or different counts of duplicate elements are considered different.
     */
    @Test
    public void testAssertContainsSameOnCollectionWithElements() {
        Iterable<Integer> expected = IterableOnly.of(1, 2, 3, 4);
        Iterable<Integer> same = IterableOnly.of(4, 2, 1, 3);
        Iterable<Integer> notSameMissing = IterableOnly.of(4, 2, 1);
        Iterable<Integer> notSameDifferent = IterableOnly.of(4, 2, 1, 5);
        Iterable<Integer> notSameDuplicates = IterableOnly.of(1, 2, 3, 4, 1, 2, 3, 4);
        assertContainsSame(expected, same);

        assertThrows(AssertionFailedError.class, () -> assertContainsSame(expected, notSameMissing));

        assertThrows(AssertionFailedError.class, () -> assertContainsSame(expected, notSameDifferent));

        assertThrows(AssertionFailedError.class, () -> assertContainsSame(expected, notSameMissing, TEST_MESSAGE));

        assertThrows(AssertionFailedError.class, () -> assertContainsSame(expected, notSameDifferent,
                () -> TEST_MESSAGE));

        assertThrows(AssertionFailedError.class, () -> assertContainsSame(expected, notSameDuplicates,
                () -> TEST_MESSAGE));
    }

    /**
     * Verifies that {@code assertContainsSame} includes the provided failure message
     * when the assertion fails.
     */
    @Test
    public void testAssertContainsAllWithMessage() {
        assertThrows(AssertionFailedError.class,
                () -> assertContainsSame(IterableOnly.of(1, 2), IterableOnly.of(1), TEST_MESSAGE), TEST_MESSAGE);
    }

    /**
     * Verifies that {@code assertContainsSame} includes the message from the provided
     * supplier when the assertion fails.
     */
    @Test
    public void testAssertContainsAllWithMessageSupplier() {
        assertThrows(AssertionFailedError.class,
                () -> assertContainsSame(IterableOnly.of(1, 2), IterableOnly.of(1), () -> TEST_MESSAGE), TEST_MESSAGE);
    }
}
