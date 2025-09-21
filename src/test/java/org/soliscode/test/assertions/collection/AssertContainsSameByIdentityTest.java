package org.soliscode.test.assertions.collection;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.interfaces.IterableOnly;
import org.soliscode.test.interfaces.NumberOnly;
import org.soliscode.test.util.IterableTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContainsSameByIdentity;

public class AssertContainsSameByIdentityTest {

    private static final String TEST_MESSAGE = "Test message";

    @Test
    public void testAssertContainsSameOnEmptyCollection() {
        Iterable<Integer> empty1 = IterableTestUtils.empty();
        assertContainsSameByIdentity(empty1, empty1);
        assertContainsSameByIdentity(empty1, empty1, TEST_MESSAGE);
        assertContainsSameByIdentity(empty1, empty1, () -> TEST_MESSAGE);

        Iterable<Integer> empty2 = IterableTestUtils.empty();
        assertContainsSameByIdentity(empty1, empty2);
        assertContainsSameByIdentity(empty1, empty2, TEST_MESSAGE);
        assertContainsSameByIdentity(empty1, empty2, () -> TEST_MESSAGE);

        Iterable<Integer> nonEmpty = IterableOnly.of(1);
        assertThrows(AssertionFailedError.class, () -> {
            assertContainsSameByIdentity(nonEmpty, empty1);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsSameByIdentity(nonEmpty, empty1, TEST_MESSAGE);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsSameByIdentity(nonEmpty, empty1, () -> TEST_MESSAGE);
        });
    }

    @Test
    public void testAssertContainsSameOnNullCollection() {

        Iterable<Integer> nonEmpty = IterableOnly.of(1);
        assertThrows(NullPointerException.class, () -> {
            CollectionAssertions.assertContainsSame(nonEmpty, null);
        });

        assertThrows(NullPointerException.class, () -> {
            CollectionAssertions.assertContainsSame(null, nonEmpty);
        });

        assertThrows(NullPointerException.class, () -> {
            CollectionAssertions.assertContainsSame(null, null);
        });
    }

    @Test
    public void testAssertContainsAllOnCollectionWithElements() {
        Iterable<NumberOnly> expected = NumberOnly.listOf(1, 2, 3, 4);
        Iterable<NumberOnly> same = IterableTestUtils.copy(expected);
        assertContainsSameByIdentity(expected, same);

        Iterable<NumberOnly> notSame = NumberOnly.listOf(1, 2, 3, 4);
        assertThrows(AssertionFailedError.class, () -> {
            assertContainsSameByIdentity(expected, notSame);
        });

        Iterable<NumberOnly> notSameMissing = IterableTestUtils.copyFirst(expected, 3);
        assertThrows(AssertionFailedError.class, () -> {
            assertContainsSameByIdentity(expected, notSameMissing);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsSameByIdentity(expected, notSame, TEST_MESSAGE);
        });

    }

    @Test
    public void testAssertContainsAllWithMessage() {
        assertThrows(AssertionFailedError.class, () -> {
            assertContainsSameByIdentity(IterableOnly.of(1, 2), IterableOnly.of(1), TEST_MESSAGE);
        }, TEST_MESSAGE);
    }

    @Test
    public void testAssertContainsAllWithMessageSupplier() {
        assertThrows(AssertionFailedError.class, () -> {
            assertContainsSameByIdentity(IterableOnly.of(1, 2), IterableOnly.of(1), () -> TEST_MESSAGE);
        }, TEST_MESSAGE);
    }
}
