package org.soliscode.test.assertions.collection;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.interfaces.IterableOnly;
import org.soliscode.test.util.IterableTestUtils;
import org.soliscode.test.util.UncachedInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContainsAllByIdentity;

/**
 * Test suite for validating the behavior of {@link CollectionAssertions#assertContainsAllByIdentity} assertion methods.
 * 
 * <p>This test class verifies that the {@code assertContainsAllByIdentity} methods correctly validate
 * that an iterable contains all elements from another iterable using identity comparison (reference equality)
 * rather than {@link Object#equals(Object)} method comparison.
 * 
 * <p>Identity comparison means that two objects are considered equal only if they are the exact same
 * object reference ({@code obj1 == obj2}), not if they are logically equal according to their
 * {@code equals()} method.
 * 
 * <h3>Key Testing Scenarios</h3>
 * <ul>
 * <li>Empty collections - verifies behavior with empty iterables</li>
 * <li>Null handling - ensures proper {@link NullPointerException} for null arguments</li>
 * <li>Identity-based comparison - validates that only identical object references pass the assertion</li>
 * <li>Equal but distinct objects - confirms that logically equal but different instances fail the assertion</li>
 * <li>Message handling - tests custom error messages and message suppliers</li>
 * </ul>
 * 
 * <h3>Assertion Method Variants Tested</h3>
 * <ul>
 * <li>{@link CollectionAssertions#assertContainsAllByIdentity(Iterable, Iterable)} - basic assertion</li>
 * <li>{@link CollectionAssertions#assertContainsAllByIdentity(Iterable, Iterable, String)} - with custom message</li>
 * <li>{@link CollectionAssertions#assertContainsAllByIdentity(Iterable, Iterable, java.util.function.Supplier)} - with message supplier</li>
 * </ul>
 * 
 * <p>This differs from {@code assertContainsAll} which uses {@code equals()} comparison. The identity-based
 * assertions are particularly useful when testing object caching, singleton patterns, or scenarios where
 * object identity is semantically important.
 * 
 * <p>Usage example demonstrating the difference:
 * <pre>{@code
 * String a = new String("test");
 * String b = new String("test"); 
 * List<String> list1 = Arrays.asList(a);
 * List<String> list2 = Arrays.asList(b);
 * 
 * // This would pass (equals comparison)
 * assertContainsAll(list1, list2);
 * 
 * // This would fail (identity comparison)
 * assertContainsAllByIdentity(list1, list2);
 * }</pre>
 * 
 * @author evanbergstrom
 * @see CollectionAssertions#assertContainsAllByIdentity(Iterable, Iterable)
 * @see AssertContainsAllByIdentity
 * @since 1.0
 */
public class AssertContainsAllByIdentityTest {

    private static final String TEST_MESSAGE = "Test message";

    /**
     * Tests the {@code assertContainsAllByIdentity} assertion with empty collections.
     * 
     * <p>Verifies that:
     * <ul>
     * <li>Empty collections satisfy the "contains all" requirement when tested against other empty collections</li>
     * <li>All three method variants (no message, string message, message supplier) behave consistently</li>
     * <li>Non-empty collections fail when tested against empty collections</li>
     * </ul>
     * 
     * <p>This test validates the edge case behavior where both collections have no elements,
     * which should trivially satisfy the "contains all" condition.
     */
    @Test
    public void testAssertSameSizeOnEmptyCollection() {
        Iterable<Integer> empty1 = IterableTestUtils.empty();
        assertContainsAllByIdentity(empty1, empty1);
        assertContainsAllByIdentity(empty1, empty1, TEST_MESSAGE);
        assertContainsAllByIdentity(empty1, empty1, () -> TEST_MESSAGE);

        Iterable<Integer> empty2 = IterableTestUtils.empty();
        assertContainsAllByIdentity(empty1, empty2);
        assertContainsAllByIdentity(empty1, empty2, TEST_MESSAGE);
        assertContainsAllByIdentity(empty1, empty2, () -> TEST_MESSAGE);

        Iterable<Integer> nonEmpty = IterableOnly.of(1);
        assertThrows(AssertionFailedError.class, () -> assertContainsAllByIdentity(nonEmpty, empty1));

        assertThrows(AssertionFailedError.class, () -> assertContainsAllByIdentity(nonEmpty, empty1, TEST_MESSAGE));

        assertThrows(AssertionFailedError.class, () -> assertContainsAllByIdentity(nonEmpty, empty1,
                () -> TEST_MESSAGE));
    }

    /**
     * Tests the {@code assertContainsAllByIdentity} assertion's null parameter handling.
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
    public void testAssertSameSizeOnNullCollection() {

        Iterable<Integer> nonEmpty = IterableOnly.of(1);
        assertThrows(NullPointerException.class,
                () -> CollectionAssertions.assertContainsAllByIdentity(nonEmpty, null));

        assertThrows(NullPointerException.class,
                () -> CollectionAssertions.assertContainsAllByIdentity(null, nonEmpty));

        assertThrows(NullPointerException.class,
                () -> CollectionAssertions.assertContainsAllByIdentity(null, null));
    }

    /**
     * Tests the {@code assertContainsAllByIdentity} assertion with collections containing identical object references.
     * 
     * <p>This test verifies the core identity-based comparison behavior:
     * <ul>
     * <li>When a subset contains references that exist in a superset, the assertion passes</li>
     * <li>When a superset is tested against a subset, the assertion fails (missing elements)</li>
     * <li>All method variants (no message, string message, message supplier) fail consistently</li>
     * </ul>
     * 
     * <p>Uses the same Integer object references in both collections to ensure identity comparison
     * succeeds when appropriate. This demonstrates the difference from equals-based comparison.
     */
    @Test
    public void testAssertSameSizeOnCollectionWithElements() {
        Integer i1 = 1;
        Integer i2 = 2;
        Integer i3 = 3;
        Integer i4 = 4;

        Iterable<Integer> superset = IterableOnly.of(i1, i2, i3, i4);
        Iterable<Integer> subset = IterableOnly.of(i2, i3);
        assertContainsAllByIdentity(subset, superset);

        assertThrows(AssertionFailedError.class, () -> assertContainsAllByIdentity(superset, subset));

        assertThrows(AssertionFailedError.class,
                () -> assertContainsAllByIdentity(superset, subset, TEST_MESSAGE));

        assertThrows(AssertionFailedError.class,
                () -> assertContainsAllByIdentity(superset, subset, () -> TEST_MESSAGE));
    }

    /**
     * Tests the {@code assertContainsAllByIdentity} assertion with logically equal but distinct object instances.
     * 
     * <p>This is the critical test that demonstrates identity vs. equality semantics:
     * <ul>
     * <li>Uses {@link UncachedInteger} objects that are equal ({@code equals()} returns true) but not identical</li>
     * <li>Verifies that even when objects are logically equal, identity comparison fails</li>
     * <li>Tests both directions: subset vs superset and superset vs subset both fail</li>
     * </ul>
     * 
     * <p>{@code UncachedInteger.valueOf(0)} creates new instances each time, ensuring they are
     * equal but not identical ({@code ==} returns false). This validates that the assertion
     * uses {@code ==} comparison rather than {@code equals()} comparison.
     */
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

        assertThrows(AssertionFailedError.class, () -> assertContainsAllByIdentity(subset, superset));

        assertThrows(AssertionFailedError.class, () -> assertContainsAllByIdentity(superset, subset));
    }

    /**
     * Tests the {@code assertContainsAllByIdentity} assertion with a custom error message.
     * 
     * <p>Verifies that when the assertion fails, the custom message is included in the
     * thrown {@link AssertionFailedError}. This ensures that developers can provide
     * contextual error messages to aid in debugging test failures.
     * 
     * <p>Uses a scenario guaranteed to fail (superset tested against subset) to trigger
     * the assertion error and validate message handling.
     */
    @Test
    public void testAssertSameSizeWithMessage() {
        assertThrows(AssertionFailedError.class, () -> assertContainsAllByIdentity(IterableOnly.of(1, 2),
                IterableOnly.of(1), TEST_MESSAGE), TEST_MESSAGE);
    }

    /**
     * Tests the {@code assertContainsAllByIdentity} assertion with a message supplier.
     * 
     * <p>Verifies that when the assertion fails, the message from the supplier function
     * is included in the thrown {@link AssertionFailedError}. The supplier pattern allows
     * for lazy evaluation of error messages, which can be more efficient when the message
     * construction is expensive.
     * 
     * <p>Uses a scenario guaranteed to fail (superset tested against subset) to trigger
     * the assertion error and validate message supplier handling.
     */
    @Test
    public void testAssertSameSizeWithMessageSupplier() {
        assertThrows(AssertionFailedError.class, () -> assertContainsAllByIdentity(IterableOnly.of(1, 2),
                IterableOnly.of(1), () -> TEST_MESSAGE), TEST_MESSAGE);
    }
}
