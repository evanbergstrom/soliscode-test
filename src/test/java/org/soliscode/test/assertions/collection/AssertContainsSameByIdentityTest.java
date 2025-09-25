package org.soliscode.test.assertions.collection;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.interfaces.IterableOnly;
import org.soliscode.test.interfaces.NumberOnly;
import org.soliscode.test.util.IterableTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContainsSameByIdentity;

/**
 * Test suite for validating the behavior of {@link CollectionAssertions#assertContainsSameByIdentity} assertion methods.
 * 
 * <p>This test class verifies that the {@code assertContainsSameByIdentity} methods correctly validate
 * that two iterables contain exactly the same set of elements using identity comparison (reference equality)
 * rather than {@link Object#equals(Object)} method comparison.
 * 
 * <p>Identity comparison means that two objects are considered equal only if they are the exact same
 * object reference ({@code obj1 == obj2}), not if they are logically equal according to their
 * {@code equals()} method. The assertion validates that both iterables contain identical object
 * references in any order, with the same multiplicity.
 * 
 * <h3>Key Differences from Equality-Based Assertions</h3>
 * <p>This assertion differs from {@code assertContainsSame} (equality-based) in critical ways:
 * <ul>
 * <li>Uses {@code ==} comparison instead of {@code equals()} method</li>
 * <li>Two separate instances of the same logical value will fail the assertion</li>
 * <li>Only identical object references are considered equivalent</li>
 * <li>Useful for testing object caching, singleton patterns, or reference semantics</li>
 * </ul>
 * 
 * <h3>Key Testing Scenarios</h3>
 * <ul>
 * <li>Empty collections - verifies behavior with empty iterables</li>
 * <li>Null handling - ensures proper {@link NullPointerException} for null arguments</li>
 * <li>Identity-based comparison - validates that only identical object references pass</li>
 * <li>Different object instances - confirms that logically equal but distinct instances fail</li>
 * <li>Size mismatches - tests behavior when collections have different sizes</li>
 * <li>Message handling - tests custom error messages and message suppliers</li>
 * </ul>
 * 
 * <h3>Assertion Method Variants Tested</h3>
 * <ul>
 * <li>{@link CollectionAssertions#assertContainsSameByIdentity(Iterable, Iterable)} - basic assertion</li>
 * <li>{@link CollectionAssertions#assertContainsSameByIdentity(Iterable, Iterable, String)} - with custom message</li>
 * <li>{@link CollectionAssertions#assertContainsSameByIdentity(Iterable, Iterable, java.util.function.Supplier)} - with message supplier</li>
 * </ul>
 * 
 * <h3>Common Use Cases</h3>
 * <p>This assertion is particularly valuable for:
 * <ul>
 * <li>Testing object pooling and caching mechanisms</li>
 * <li>Validating singleton or flyweight pattern implementations</li>
 * <li>Ensuring reference preservation in copy operations</li>
 * <li>Testing collection operations that should maintain object identity</li>
 * <li>Verifying that collections contain exact object references, not copies</li>
 * </ul>
 * 
 * <p>Usage example demonstrating identity vs. equality:
 * <pre>{@code
 * String a = new String("test");
 * String b = new String("test");
 * String c = a; // Same reference
 * 
 * List<String> list1 = Arrays.asList(a, c);
 * List<String> list2 = Arrays.asList(a, b); // Different instance
 * 
 * // This would pass (equality comparison)
 * assertContainsSame(list1, list2);
 * 
 * // This would fail (identity comparison) - b is different instance than c
 * assertContainsSameByIdentity(list1, list2);
 * }</pre>
 * 
 * @author evanbergstrom
 * @see CollectionAssertions#assertContainsSameByIdentity(Iterable, Iterable)
 * @see AssertContainsSameByIdentity
 * @see CollectionAssertions#assertContainsSame(Iterable, Iterable)
 * @since 1.0
 */
public class AssertContainsSameByIdentityTest {

    private static final String TEST_MESSAGE = "Test message";

    /**
     * Tests the {@code assertContainsSameByIdentity} assertion with empty collections.
     * 
     * <p>Verifies that:
     * <ul>
     * <li>Empty collections trivially satisfy the "contains same" requirement when tested against other empty collections</li>
     * <li>Both self-comparison and cross-comparison of empty collections pass</li>
     * <li>Non-empty collections fail when compared against empty collections</li>
     * <li>All three method variants (no message, string message, message supplier) behave consistently</li>
     * </ul>
     * 
     * <p>This test validates edge case behavior where both collections have no elements,
     * which should trivially satisfy the "contains same" condition since both are empty.
     */
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
        assertThrows(AssertionFailedError.class,
                () -> assertContainsSameByIdentity(nonEmpty, empty1));

        assertThrows(AssertionFailedError.class,
                () -> assertContainsSameByIdentity(nonEmpty, empty1, TEST_MESSAGE));

        assertThrows(AssertionFailedError.class,
                () -> assertContainsSameByIdentity(nonEmpty, empty1, () -> TEST_MESSAGE));
    }

    /**
     * Tests the {@code assertContainsSameByIdentity} assertion's null parameter handling.
     * 
     * <p>Verifies that {@link NullPointerException} is thrown for all combinations of null parameters:
     * <ul>
     * <li>When the expected collection (first parameter) is null</li>
     * <li>When the actual collection (second parameter) is null</li>
     * <li>When both parameters are null</li>
     * </ul>
     * 
     * <p>This test ensures robust null safety and prevents undefined behavior when null
     * iterables are passed to the assertion methods.
     */
    @Test
    public void testAssertContainsSameOnNullCollection() {

        Iterable<Integer> nonEmpty = IterableOnly.of(1);
        assertThrows(NullPointerException.class, () -> CollectionAssertions.assertContainsSameByIdentity(nonEmpty, null));

        assertThrows(NullPointerException.class, () -> CollectionAssertions.assertContainsSameByIdentity(null, nonEmpty));

        assertThrows(NullPointerException.class, () -> CollectionAssertions.assertContainsSameByIdentity(null, null));
    }

    /**
     * Tests the {@code assertContainsSameByIdentity} assertion with collections containing elements.
     * 
     * <p>This test validates the core identity-based comparison behavior:
     * <ul>
     * <li>When collections contain the same object references, the assertion passes</li>
     * <li>When collections contain different object instances (even if logically equal), the assertion fails</li>
     * <li>When collections have different sizes, the assertion fails</li>
     * <li>Message variants fail consistently when appropriate</li>
     * </ul>
     * 
     * <p>Uses {@link NumberOnly} objects to test identity comparison - {@code IterableTestUtils.copy()}
     * preserves object references while {@code NumberOnly.listOf()} creates new instances.
     * This demonstrates the critical difference between identity and equality comparison.
     */
    @Test
    public void testAssertContainsAllOnCollectionWithElements() {
        Iterable<NumberOnly> expected = NumberOnly.listOf(1, 2, 3, 4);
        Iterable<NumberOnly> same = IterableTestUtils.copy(expected);
        assertContainsSameByIdentity(expected, same);

        Iterable<NumberOnly> notSame = NumberOnly.listOf(1, 2, 3, 4);
        assertThrows(AssertionFailedError.class, () -> assertContainsSameByIdentity(expected, notSame));

        Iterable<NumberOnly> notSameMissing = IterableTestUtils.copyFirst(expected, 3);
        assertThrows(AssertionFailedError.class, () -> assertContainsSameByIdentity(expected, notSameMissing));

        assertThrows(AssertionFailedError.class, () -> assertContainsSameByIdentity(expected, notSame, TEST_MESSAGE));

    }

    /**
     * Tests the {@code assertContainsSameByIdentity} assertion with a custom error message.
     * 
     * <p>Verifies that when the assertion fails due to different collections, the custom message
     * is included in the thrown {@link AssertionFailedError}. This ensures that developers can
     * provide contextual error messages to aid in debugging test failures.
     * 
     * <p>Uses collections with different sizes to guarantee failure and validate message handling.
     */
    @Test
    public void testAssertContainsAllWithMessage() {
        assertThrows(AssertionFailedError.class,
                () -> assertContainsSameByIdentity(IterableOnly.of(1, 2),
                        IterableOnly.of(1), TEST_MESSAGE), TEST_MESSAGE);
    }

    /**
     * Tests the {@code assertContainsSameByIdentity} assertion with a message supplier.
     * 
     * <p>Verifies that when the assertion fails due to different collections, the message from
     * the supplier function is included in the thrown {@link AssertionFailedError}. The supplier
     * pattern allows for lazy evaluation of error messages, which can be more efficient when
     * the message construction is expensive.
     * 
     * <p>Uses collections with different sizes to guarantee failure and validate message supplier handling.
     */
    @Test
    public void testAssertContainsAllWithMessageSupplier() {
        assertThrows(AssertionFailedError.class,
                () -> assertContainsSameByIdentity(IterableOnly.of(1, 2),
                        IterableOnly.of(1), () -> TEST_MESSAGE), TEST_MESSAGE);
    }
}
