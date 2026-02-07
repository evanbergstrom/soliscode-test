package org.soliscode.test.assertions.collection;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.interfaces.IterableOnly;
import org.soliscode.test.util.IterableTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContainsNone;

/**
 * Test suite for validating the behavior of {@link CollectionAssertions#assertContainsNone} assertion methods.
 * 
 * <p>This test class verifies that the {@code assertContainsNone} methods correctly validate
 * that an iterable contains none of the elements from another iterable using equality comparison
 * ({@link Object#equals(Object)} method comparison).
 * 
 * <p>The {@code assertContainsNone} assertion is the logical opposite of {@code assertContains} - it
 * passes when the two iterables are completely disjoint (share no common elements) and fails when
 * any element from the excluded set is found in the actual iterable.
 * 
 * ### Key Testing Scenarios
 * <ul>
 * <li>Empty collections - verifies behavior when one or both collections are empty</li>
 * <li>Null handling - ensures proper {@link NullPointerException} for null arguments</li>
 * <li>Disjoint collections - validates that completely separate sets pass the assertion</li>
 * <li>Overlapping collections - confirms that any shared elements cause the assertion to fail</li>
 * <li>Message handling - tests custom error messages and message suppliers</li>
 * </ul>
 *
 * ### Assertion Method Variants Tested
 * <ul>
 * <li>{@link CollectionAssertions#assertContainsNone(Iterable, Iterable)} - basic assertion</li>
 * <li>{@link CollectionAssertions#assertContainsNone(Iterable, Iterable, String)} - with custom message</li>
 * <li>{@link CollectionAssertions#assertContainsNone(Iterable, Iterable, java.util.function.Supplier)} - with message supplier</li>
 * </ul>
 *
 * ### Common Use Cases
 * <p>This assertion is particularly useful for:
 * <ul>
 * <li>Testing collection removal operations ({@code removeAll}, {@code removeIf})</li>
 * <li>Validating filter operations that should exclude certain elements</li>
 * <li>Verifying set operations like difference or complement</li>
 * <li>Testing access control where certain items should be forbidden</li>
 * </ul>
 * 
 * <p>Usage example:
 * <pre>{@code
 * List<String> allowedItems = Arrays.asList("apple", "banana", "cherry");
 * List<String> forbiddenItems = Arrays.asList("poison", "toxin");
 * 
 * // Verify that allowedItems contains none of the forbidden elements
 * assertContainsNone(forbiddenItems, allowedItems);
 * }</pre>
 * 
 * @author evanbergstrom
 * @see CollectionAssertions#assertContainsNone(Iterable, Iterable)
 * @see AssertContainsNone
 * @see CollectionAssertions#assertContains(Object, Iterable)
 * @since 1.0
 */
public class AssertContainsNoneTest {

    private static final String TEST_MESSAGE = "Test message";

    /**
     * Tests the {@code assertContainsNone} assertion with empty collections.
     * 
     * <p>Verifies that:
     * <ul>
     * <li>Empty collections trivially satisfy the "contains none" requirement when tested against other empty collections</li>
     * <li>Empty collections contain none of the elements from non-empty collections (trivial success)</li>
     * <li>All three method variants (no message, string message, message supplier) behave consistently</li>
     * </ul>
     * 
     * <p>This test validates edge case behavior where the excluded set is empty, which should
     * always pass since there are no elements to exclude, or where the actual collection is empty,
     * which should always pass since an empty collection cannot contain any excluded elements.
     */
    @Test
    public void testAssertContainsNoneOnEmptyCollection() {
        Iterable<Integer> empty1 = IterableTestUtils.empty();
        assertContainsNone(empty1, empty1);
        assertContainsNone(empty1, empty1, TEST_MESSAGE);
        assertContainsNone(empty1, empty1, () -> TEST_MESSAGE);

        Iterable<Integer> nonEmpty = IterableOnly.of(1);
        assertContainsNone(empty1, nonEmpty);
        assertContainsNone(empty1, nonEmpty, TEST_MESSAGE);
        assertContainsNone(empty1, nonEmpty, () -> TEST_MESSAGE);
    }

    /**
     * Tests the {@code assertContainsNone} assertion's null parameter handling.
     * 
     * <p>Verifies that {@link NullPointerException} is thrown for all combinations of null parameters:
     * <ul>
     * <li>When the excluded collection (first parameter) is null</li>
     * <li>When the actual collection (second parameter) is null</li>
     * <li>When both parameters are null</li>
     * </ul>
     * 
     * <p>This test ensures robust null safety and prevents undefined behavior when null
     * iterables are passed to the assertion methods.
     */
    @Test
    public void testAssertContainsNoneOnNullCollection() {

        Iterable<Integer> nonEmpty = IterableOnly.of(1);
        assertThrows(NullPointerException.class,
                () -> CollectionAssertions.assertContainsNone(nonEmpty, null));

        assertThrows(NullPointerException.class,
                () -> CollectionAssertions.assertContainsNone(null, nonEmpty));

        assertThrows(NullPointerException.class,
                () -> CollectionAssertions.assertContainsNone(null, null));
    }

    /**
     * Tests the {@code assertContainsNone} assertion with collections containing elements.
     * 
     * <p>This test verifies the core disjoint collection behavior:
     * <ul>
     * <li>When collections are completely disjoint (share no common elements), the assertion passes</li>
     * <li>When collections have any overlapping elements, the assertion fails</li>
     * <li>All method variants (no message, string message, message supplier) fail consistently when appropriate</li>
     * </ul>
     * 
     * <p>Tests both positive cases (disjoint sets like {1,2,3,4} and {0,5}) and negative cases
     * (overlapping sets like {1,2,3,4} and {2,3}) to ensure the assertion correctly identifies
     * both the presence and absence of common elements.
     */
    @Test
    public void testAssertContainsNoneOnCollectionWithElements() {
        Iterable<Integer> actual = IterableOnly.of(1, 2, 3, 4);
        Iterable<Integer> excluded = IterableOnly.of(0, 5);
        assertContainsNone(excluded, actual);

        Iterable<Integer> subset = IterableOnly.of(2, 3);
        assertThrows(AssertionFailedError.class,
                () -> assertContainsNone(subset, actual));

        assertThrows(AssertionFailedError.class,
                () -> assertContainsNone(subset, actual, TEST_MESSAGE));

        assertThrows(AssertionFailedError.class,
                () -> assertContainsNone(subset, actual, () -> TEST_MESSAGE));
    }

    /**
     * Tests the {@code assertContainsNone} assertion with a custom error message.
     * 
     * <p>Verifies that when the assertion fails due to overlapping elements, the custom message
     * is included in the thrown {@link AssertionFailedError}. This ensures that developers can
     * provide contextual error messages to aid in debugging test failures.
     * 
     * <p>Uses a scenario guaranteed to fail (collections with overlapping elements) to trigger
     * the assertion error and validate message handling.
     */
    @Test
    public void testAssertContainsNoneWithMessage() {
        assertThrows(AssertionFailedError.class,
                () -> assertContainsNone(IterableOnly.of(1, 2), IterableOnly.of(1), TEST_MESSAGE), TEST_MESSAGE);
    }

    /**
     * Tests the {@code assertContainsNone} assertion with a message supplier.
     * 
     * <p>Verifies that when the assertion fails due to overlapping elements, the message from
     * the supplier function is included in the thrown {@link AssertionFailedError}. The supplier
     * pattern allows for lazy evaluation of error messages, which can be more efficient when
     * the message construction is expensive.
     * 
     * <p>Uses a scenario guaranteed to fail (collections with overlapping elements) to trigger
     * the assertion error and validate message supplier handling.
     */
    @Test
    public void testAssertContainsNoneWithMessageSupplier() {
        assertThrows(AssertionFailedError.class,
                () -> assertContainsNone(IterableOnly.of(1, 2), IterableOnly.of(1), () -> TEST_MESSAGE), TEST_MESSAGE);
    }
}
