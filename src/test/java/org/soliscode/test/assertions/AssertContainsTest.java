package org.soliscode.test.assertions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.assertions.collection.CollectionAssertions;
import org.soliscode.test.interfaces.IterableOnly;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContains;

@DisplayName("Tests for assertContains")
public class AssertContainsTest {
    private static final String TEST_MESSAGE = "Test message";

    @DisplayName("Test assertContains with an iterable")
    @Test
    void testAssertContainsOnIterable() {
        Iterable<Integer> iterable = IterableOnly.of(1,2,3);
        assertContains(1, iterable);
        assertContains(1, iterable, TEST_MESSAGE);
        assertContains(1, iterable, () -> TEST_MESSAGE);

        assertContains(2, iterable);
        assertContains(3, iterable);

        assertThrows(AssertionFailedError.class, () -> {
            assertContains(4, iterable);
        });
    }

    @DisplayName("Test assertContains with a collection")
    @Test
    void testAssertContainsOnCollection() {
        Iterable<Integer> collection = List.of(1, 2, 3);
        assertContains(1, collection);
        assertContains(1, collection, TEST_MESSAGE);
        assertContains(1, collection, () -> TEST_MESSAGE);

        assertContains(2, collection);
        assertContains(3, collection);

        assertThrows(AssertionFailedError.class, () -> {
            assertContains(4, collection);
        });
    }

    @DisplayName("Test assertContains with an empty collection")
    @Test
    public void testAssertContainsOnEmptyCollection() {

        Iterable<Integer> iterable = Collections.emptyList();
        assertThrows(AssertionFailedError.class, () -> {
            CollectionAssertions.assertContains(1, iterable);
        });
    }

    @DisplayName("Test assertContains with a null collection")
    @Test
    public void testAssertContainsOnNullCollection() {

        assertThrows(AssertionFailedError.class, () -> {
            CollectionAssertions.assertContains(1, null);
        });
    }
}
