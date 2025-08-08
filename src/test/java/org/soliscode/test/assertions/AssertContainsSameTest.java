package org.soliscode.test.assertions;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.assertions.collection.CollectionAssertions;
import org.soliscode.test.interfaces.IterableOnly;
import org.soliscode.test.util.IterableTestOps;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContainsSame;

public class AssertContainsSameTest {

    private static final String TEST_MESSAGE = "Test message";

    @Test
    public void testAssertContainsSameOnEmptyCollection() {
        Iterable<Integer> empty1 = IterableTestOps.empty();
        assertContainsSame(empty1, empty1);
        assertContainsSame(empty1, empty1, TEST_MESSAGE);
        assertContainsSame(empty1, empty1, () -> TEST_MESSAGE);

        Iterable<Integer> empty2 = IterableTestOps.empty();
        assertContainsSame(empty1, empty2);
        assertContainsSame(empty1, empty2, TEST_MESSAGE);
        assertContainsSame(empty1, empty2, () -> TEST_MESSAGE);

        Iterable<Integer> nonEmpty = IterableOnly.of(1);
        assertThrows(AssertionFailedError.class, () -> {
            assertContainsSame(nonEmpty, empty1);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsSame(nonEmpty, empty1, TEST_MESSAGE);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsSame(nonEmpty, empty1, () -> TEST_MESSAGE);
        });
    }

    @Test
    public void testAssertContainsSameOnNullCollection() {

        Iterable<Integer> nonEmpty = IterableOnly.of(1);
        assertThrows(AssertionFailedError.class, () -> {
            CollectionAssertions.assertContainsSame(nonEmpty, null);
        });

        assertThrows(AssertionFailedError.class, () -> {
            CollectionAssertions.assertContainsSame(null, nonEmpty);
        });

        assertThrows(AssertionFailedError.class, () -> {
            CollectionAssertions.assertContainsSame(null, null);
        });
    }

    @Test
    public void testAssertContainsSameOnCollectionWithElements() {
        Iterable<Integer> expected = IterableOnly.of(1, 2, 3, 4);
        Iterable<Integer> same = IterableOnly.of(4, 2, 1, 3);
        Iterable<Integer> notSameMissing = IterableOnly.of(4, 2, 1);
        Iterable<Integer> notSameDifferent = IterableOnly.of(4, 2, 1, 5);
        Iterable<Integer> notSameDuplicates = IterableOnly.of(1, 2, 3, 4, 1, 2, 3, 4);
        assertContainsSame(expected, same);

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsSame(expected, notSameMissing);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsSame(expected, notSameDifferent);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsSame(expected, notSameMissing, TEST_MESSAGE);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsSame(expected, notSameDifferent, () -> TEST_MESSAGE);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsSame(expected, notSameDuplicates, () -> TEST_MESSAGE);
        });
    }

    @Test
    public void testAssertContainsAllWithMessage() {
        assertThrows(AssertionFailedError.class, () -> {
            assertContainsSame(IterableOnly.of(1, 2), IterableOnly.of(1), TEST_MESSAGE);
        }, TEST_MESSAGE);
    }

    @Test
    public void testAssertContainsAllWithMessageSupplier() {
        assertThrows(AssertionFailedError.class, () -> {
            assertContainsSame(IterableOnly.of(1, 2), IterableOnly.of(1), () -> TEST_MESSAGE);
        }, TEST_MESSAGE);
    }
}
