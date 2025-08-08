package org.soliscode.test.assertions;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.assertions.collection.CollectionAssertions;
import org.soliscode.test.interfaces.IterableOnly;
import org.soliscode.test.util.IterableTestOps;
import org.soliscode.test.util.UncachedInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContainsAllByIdentity;

public class AssertContainsAllByIdentityTest {

    private static final String TEST_MESSAGE = "Test message";

    @Test
    public void testAssertSameSizeOnEmptyCollection() {
        Iterable<Integer> empty1 = IterableTestOps.empty();
        assertContainsAllByIdentity(empty1, empty1);
        assertContainsAllByIdentity(empty1, empty1, TEST_MESSAGE);
        assertContainsAllByIdentity(empty1, empty1, () -> TEST_MESSAGE);

        Iterable<Integer> empty2 = IterableTestOps.empty();
        assertContainsAllByIdentity(empty1, empty2);
        assertContainsAllByIdentity(empty1, empty2, TEST_MESSAGE);
        assertContainsAllByIdentity(empty1, empty2, () -> TEST_MESSAGE);

        Iterable<Integer> nonEmpty = IterableOnly.of(1);
        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAllByIdentity(nonEmpty, empty1);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAllByIdentity(nonEmpty, empty1, TEST_MESSAGE);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAllByIdentity(nonEmpty, empty1, () -> TEST_MESSAGE);
        });
    }

    @Test
    public void testAssertSameSizeOnNullCollection() {

        Iterable<Integer> nonEmpty = IterableOnly.of(1);
        assertThrows(AssertionFailedError.class, () -> {
            CollectionAssertions.assertContainsAllByIdentity(nonEmpty, null);
        });

        assertThrows(AssertionFailedError.class, () -> {
            CollectionAssertions.assertContainsAllByIdentity(null, nonEmpty);
        });

        assertThrows(AssertionFailedError.class, () -> {
            CollectionAssertions.assertContainsAllByIdentity(null, null);
        });
    }

    @Test
    public void testAssertSameSizeOnCollectionWithElements() {
        Integer i1 = 1;
        Integer i2 = 2;
        Integer i3 = 3;
        Integer i4 = 4;

        Iterable<Integer> superset = IterableOnly.of(i1, i2, i3, i4);
        Iterable<Integer> subset = IterableOnly.of(i2, i3);
        assertContainsAllByIdentity(subset, superset);

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAllByIdentity(superset, subset);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAllByIdentity(superset, subset, TEST_MESSAGE);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAllByIdentity(superset, subset, () -> TEST_MESSAGE);
        });
    }

    @Test
    public void testAssertSameSizeOnCollectionWithEqualElements() {

        UncachedInteger i1a = UncachedInteger.valueOf(0);
        UncachedInteger i2a = UncachedInteger.valueOf(0);
        UncachedInteger i3a = UncachedInteger.valueOf(0);
        UncachedInteger i4a = UncachedInteger.valueOf(0);

        UncachedInteger i2b = UncachedInteger.valueOf(0);
        UncachedInteger i3b = UncachedInteger.valueOf(0);

        Iterable<UncachedInteger> superset = IterableOnly.of(i1a, i2a, i3a, i4a);
        Iterable<UncachedInteger> subset = IterableOnly.of(i2b, i3b);

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAllByIdentity(subset, superset);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAllByIdentity(superset, subset);
        });
    }

    @Test
    public void testAssertSameSizeWithMessage() {
        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAllByIdentity(IterableOnly.of(1, 2), IterableOnly.of(1), TEST_MESSAGE);
        }, TEST_MESSAGE);
    }

    @Test
    public void testAssertSameSizeWithMessageSupplier() {
        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAllByIdentity(IterableOnly.of(1, 2), IterableOnly.of(1), () -> TEST_MESSAGE);
        }, TEST_MESSAGE);
    }
}
