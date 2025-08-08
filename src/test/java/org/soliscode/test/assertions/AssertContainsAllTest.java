package org.soliscode.test.assertions;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.assertions.collection.CollectionAssertions;
import org.soliscode.test.interfaces.IterableOnly;
import org.soliscode.test.util.IterableTestOps;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContainsAll;

public class AssertContainsAllTest {

    private static final String TEST_MESSAGE = "Test message";

    @Test
    public void testAssertContainsAllOnEmptyCollection() {
        Iterable<Integer> empty1 = IterableTestOps.empty();
        assertContainsAll(empty1, empty1);
        assertContainsAll(empty1, empty1, TEST_MESSAGE);
        assertContainsAll(empty1, empty1, () -> TEST_MESSAGE);

        Iterable<Integer> empty2 = IterableTestOps.empty();
        assertContainsAll(empty1, empty2);
        assertContainsAll(empty1, empty2, TEST_MESSAGE);
        assertContainsAll(empty1, empty2, () -> TEST_MESSAGE);

        Iterable<Integer> nonEmpty = IterableOnly.of(1);
        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(nonEmpty, empty1);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(nonEmpty, empty1, TEST_MESSAGE);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(nonEmpty, empty1, () -> TEST_MESSAGE);
        });
    }

    @Test
    public void testAssertContainsAllOnNullCollection() {

        Iterable<Integer> nonEmpty = IterableOnly.of(1);
        assertThrows(AssertionFailedError.class, () -> {
            CollectionAssertions.assertContainsAll(nonEmpty, null);
        });

        assertThrows(AssertionFailedError.class, () -> {
            CollectionAssertions.assertContainsAll(null, nonEmpty);
        });

        assertThrows(AssertionFailedError.class, () -> {
            CollectionAssertions.assertContainsAll(null, null);
        });
    }

    @Test
    public void testAssertContainsAllOnCollectionWithElements() {
        Iterable<Integer> superset = IterableOnly.of(1, 2, 3, 4);
        Iterable<Integer> subset = IterableOnly.of(2, 3);
        assertContainsAll(subset, superset);

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(superset, subset);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(superset, subset, TEST_MESSAGE);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(superset, subset, () -> TEST_MESSAGE);
        });
    }

    @Test
    public void testAssertContainsAllWithMessage() {
        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(IterableOnly.of(1, 2), IterableOnly.of(1), TEST_MESSAGE);
        }, TEST_MESSAGE);
    }

    @Test
    public void testAssertContainsAllWithMessageSupplier() {
        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(IterableOnly.of(1, 2), IterableOnly.of(1), () -> TEST_MESSAGE);
        }, TEST_MESSAGE);
    }
}
