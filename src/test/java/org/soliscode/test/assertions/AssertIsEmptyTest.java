package org.soliscode.test.assertions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.interfaces.IterableOnly;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertIsEmpty;


@DisplayName("Tests for AssertIsEmpty class")
public class AssertIsEmptyTest {

    private static final String TEST_MESSAGE = "Test message";

    @Test
    public void testAssertIsEmptyOnIterable() {

        Iterable<Integer> empty = IterableOnly.of();
        assertIsEmpty(empty);
        assertIsEmpty(empty, TEST_MESSAGE);
        assertIsEmpty(empty, () -> TEST_MESSAGE);

        Iterable<Integer> notEmpty = IterableOnly.of(1);
        assertThrows(AssertionFailedError.class, () -> {
            assertIsEmpty(notEmpty);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertIsEmpty(notEmpty, TEST_MESSAGE);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertIsEmpty(notEmpty, () -> TEST_MESSAGE);
        });
    }

    @Test
    public void testAssertIsEmptyOnCollection() {

        Iterable<Integer> empty = Collections.emptyList();
        assertIsEmpty(empty);
        assertIsEmpty(empty, TEST_MESSAGE);
        assertIsEmpty(empty, () -> TEST_MESSAGE);

        Iterable<Integer> notEmpty = List.of(1);
        assertThrows(AssertionFailedError.class, () -> {
            assertIsEmpty(notEmpty);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertIsEmpty(notEmpty, TEST_MESSAGE);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertIsEmpty(notEmpty, () -> TEST_MESSAGE);
        });
    }

    @Test
    public void testAssertSameSizeWithMessageSupplier() {
        assertThrows(AssertionFailedError.class, () -> {
            assertIsEmpty(IterableOnly.of(1), () -> TEST_MESSAGE);
        }, TEST_MESSAGE);
    }
}
