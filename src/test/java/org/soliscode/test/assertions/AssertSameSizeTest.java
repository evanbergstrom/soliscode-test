package org.soliscode.test.assertions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.assertions.collection.CollectionAssertions;
import org.soliscode.test.interfaces.IterableOnly;
import org.soliscode.test.util.IterableTestOps;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertSameSize;

@DisplayName("Tests for AssertIsSame class")
public class AssertSameSizeTest {

    private static final String TEST_MESSAGE = "Test message";

    @Test
    public void testAssertSameSizeOnEmptyCollection() {
        Iterable<Integer> empty1 = IterableTestOps.empty();
        assertSameSize(empty1, empty1);
        assertSameSize(empty1, empty1, TEST_MESSAGE);
        assertSameSize(empty1, empty1, () -> TEST_MESSAGE);

        Iterable<Integer> empty2 = IterableTestOps.empty();
        CollectionAssertions.assertSameSize(empty1, empty2);
        CollectionAssertions.assertSameSize(empty1, empty2, TEST_MESSAGE);
        CollectionAssertions.assertSameSize(empty1, empty2, () -> TEST_MESSAGE);

        Iterable<Integer> nonEmpty = IterableOnly.of(1);
        assertThrows(AssertionFailedError.class, () -> {
            CollectionAssertions.assertSameSize(empty1, nonEmpty);
        });

        assertThrows(AssertionFailedError.class, () -> {
            CollectionAssertions.assertSameSize(nonEmpty, empty1);
        });
    }

    @Test
    public void testAssertSameSizeOnNonemptCollection() {
        Iterable<Integer> list1 = IterableOnly.of(1);
        Iterable<Integer> list2 = IterableOnly.of(2);
        Iterable<Integer> list3 = IterableOnly.of(4, 5);

        assertDoesNotThrow(() -> {
            assertSameSize(list1, list2);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertSameSize(list1, list3);
        });
    }

    @Test
    public void testAssertSameSizeThrowsOnNullCollection() {
        Iterable<Integer> list1 = IterableOnly.of(1);

        assertThrows(AssertionFailedError.class, () -> {
            assertSameSize(list1, null);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertSameSize(null, list1);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertSameSize(null, null);
        });
    }

    @Test
    public void testAssertSameSizeWithMessage() {
        assertThrows(AssertionFailedError.class, () -> {
            assertSameSize(IterableOnly.of(1), IterableOnly.of(1, 2), TEST_MESSAGE);
        }, TEST_MESSAGE);
    }

    @Test
    public void testAssertSameSizeWithMessageSupplier() {
        assertThrows(AssertionFailedError.class, () -> {
            assertSameSize(IterableOnly.of(1), IterableOnly.of(1, 2), () -> TEST_MESSAGE);
        }, TEST_MESSAGE);
    }
}
