package org.soliscode.test.assertions;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.assertions.collection.CollectionAssertions;
import org.soliscode.test.interfaces.IterableOnly;
import org.soliscode.test.util.IterableTestOps;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContainsNone;

public class AssertContainsNoneTest {

    private static final String TEST_MESSAGE = "Test message";

    @Test
    public void testAssertContainsNoneOnEmptyCollection() {
        Iterable<Integer> empty1 = IterableTestOps.empty();
        assertContainsNone(empty1, empty1);
        assertContainsNone(empty1, empty1, TEST_MESSAGE);
        assertContainsNone(empty1, empty1, () -> TEST_MESSAGE);

        Iterable<Integer> nonEmpty = IterableOnly.of(1);
        assertContainsNone(empty1, nonEmpty);
        assertContainsNone(empty1, nonEmpty, TEST_MESSAGE);
        assertContainsNone(empty1, nonEmpty, () -> TEST_MESSAGE);
    }

    @Test
    public void testAssertContainsNoneOnNullCollection() {

        Iterable<Integer> nonEmpty = IterableOnly.of(1);
        assertThrows(AssertionFailedError.class, () -> {
            CollectionAssertions.assertContainsNone(nonEmpty, null);
        });

        assertThrows(AssertionFailedError.class, () -> {
            CollectionAssertions.assertContainsNone(null, nonEmpty);
        });

        assertThrows(AssertionFailedError.class, () -> {
            CollectionAssertions.assertContainsNone(null, null);
        });
    }

    @Test
    public void testAssertContainsNoneOnCollectionWithElements() {
        Iterable<Integer> actual = IterableOnly.of(1, 2, 3, 4);
        Iterable<Integer> excluded = IterableOnly.of(0, 5);
        assertContainsNone(excluded, actual);

        Iterable<Integer> subset = IterableOnly.of(2, 3);
        assertThrows(AssertionFailedError.class, () -> {
            assertContainsNone(subset, actual);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsNone(subset, actual, TEST_MESSAGE);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsNone(subset, actual, () -> TEST_MESSAGE);
        });
    }

    @Test
    public void testAssertContainsNoneWithMessage() {
        assertThrows(AssertionFailedError.class, () -> {
            assertContainsNone(IterableOnly.of(1, 2), IterableOnly.of(1), TEST_MESSAGE);
        }, TEST_MESSAGE);
    }

    @Test
    public void testAssertContainsNoneWithMessageSupplier() {
        assertThrows(AssertionFailedError.class, () -> {
            assertContainsNone(IterableOnly.of(1, 2), IterableOnly.of(1), () -> TEST_MESSAGE);
        }, TEST_MESSAGE);
    }
}
