package org.soliscode.test.assertions.collection;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.interfaces.IterableOnly;
import org.soliscode.test.interfaces.NumberOnly;
import org.soliscode.test.util.IterableTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContainsSameByIdentity;

/// Test suite for validating the behavior of [CollectionAssertions#assertContainsSameByIdentity] assertion methods.
///
/// This test class verifies that the `assertContainsSameByIdentity` methods correctly validate
/// that two iterables contain exactly the same set of elements using identity comparison (reference equality)
/// rather than [Object#equals(Object)] method comparison.
///
/// Identity comparison means that two objects are considered equal only if they are the exact same
/// object reference (`obj1 == obj2`), not if they are logically equal according to their
/// `equals()` method. The assertion validates that both iterables contain identical object
/// references in any order, with the same multiplicity.
/// ### Key Differences from Equality-Based Assertions
///
/// This assertion differs from `assertContainsSame` (equality-based) in critical ways:
///
///   - Uses `==` comparison instead of `equals()` method
///   - Two separate instances of the same logical value will fail the assertion
///   - Only identical object references are considered equivalent
///   - Useful for testing object caching, singleton patterns, or reference semantics
///
/// ### Key Testing Scenarios
///
///   - Empty collections - verifies behavior with empty iterables
///   - Null handling - ensures proper [NullPointerException] for null arguments
///   - Identity-based comparison - validates that only identical object references pass
///   - Different object instances - confirms that logically equal but distinct instances fail
///   - Size mismatches - tests behavior when collections have different sizes
///   - Message handling - tests custom error messages and message suppliers
///
/// ### Assertion Method Variants Tested
///
///   - [CollectionAssertions#assertContainsSameByIdentity(Iterable, Iterable)] - basic assertion
///   - [CollectionAssertions#assertContainsSameByIdentity(Iterable, Iterable, String)] - with custom message
///   - [CollectionAssertions#assertContainsSameByIdentity(Iterable, Iterable, java.util.function.Supplier)] - with message supplier
///
/// ### Common Use Cases
///
/// This assertion is particularly valuable for:
///
///   - Testing object pooling and caching mechanisms
///   - Validating singleton or flyweight pattern implementations
///   - Ensuring reference preservation in copy operations
///   - Testing collection operations that should maintain object identity
///   - Verifying that collections contain exact object references, not copies
///
///
/// Usage example demonstrating identity vs. equality:
/// <pre>
/// `String a = new String("test");String b = new String("test");String c = a; // Same referenceList<String> list1 = Arrays.asList(a, c);List<String> list2 = Arrays.asList(a, b); // Different instance// This would pass (equality comparison)assertContainsSame(list1, list2);// This would fail (identity comparison) - b is different instance than cassertContainsSameByIdentity(list1, list2);`</pre>
///
/// @author evanbergstrom
/// @see CollectionAssertions#assertContainsSameByIdentity(Iterable, Iterable)
/// @see AssertContainsSameByIdentity
/// @see CollectionAssertions#assertContainsSame(Iterable, Iterable)
/// @since 1.0
public class AssertContainsSameByIdentityTest {

    private static final String TEST_MESSAGE = "Test message";

    /// Tests the `assertContainsSameByIdentity` assertion with empty collections.
    ///
    /// Verifies that:
    ///
    ///   - Empty collections trivially satisfy the "contains same" requirement when tested against other empty collections
    ///   - Both self-comparison and cross-comparison of empty collections pass
    ///   - Non-empty collections fail when compared against empty collections
    ///   - All three method variants (no message, string message, message supplier) behave consistently
    ///
    ///
    /// This test validates edge case behavior where both collections have no elements,
    /// which should trivially satisfy the "contains same" condition since both are empty.
    @Test
    public void assertContainsSameByIdentity_onEmptyCollection_passes() {
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

    /// Tests the `assertContainsSameByIdentity` assertion's null parameter handling.
    ///
    /// Verifies that [NullPointerException] is thrown for all combinations of null parameters:
    ///
    ///   - When the expected collection (first parameter) is null
    ///   - When the actual collection (second parameter) is null
    ///   - When both parameters are null
    ///
    ///
    /// This test ensures robust null safety and prevents undefined behavior when null
    /// iterables are passed to the assertion methods.
    @Test
    public void assertContainsSameByIdentity_onNullCollection_throwsNullPointerException() {

        Iterable<Integer> nonEmpty = IterableOnly.of(1);
        assertThrows(NullPointerException.class, () -> CollectionAssertions.assertContainsSameByIdentity(nonEmpty, null));

        assertThrows(NullPointerException.class, () -> CollectionAssertions.assertContainsSameByIdentity(null, nonEmpty));

        assertThrows(NullPointerException.class, () -> CollectionAssertions.assertContainsSameByIdentity(null, null));
    }

    /// Tests the `assertContainsSameByIdentity` assertion with collections containing elements.
    ///
    /// This test validates the core identity-based comparison behavior:
    ///
    ///   - When collections contain the same object references, the assertion passes
    ///   - When collections contain different object instances (even if logically equal), the assertion fails
    ///   - When collections have different sizes, the assertion fails
    ///   - Message variants fail consistently when appropriate
    ///
    ///
    /// Uses [NumberOnly] objects to test identity comparison - `IterableTestUtils.copy()`
    /// preserves object references while `NumberOnly.listOf()` creates new instances.
    /// This demonstrates the critical difference between identity and equality comparison.
    @Test
    public void assertContainsSameByIdentity_onCollectionWithElements_passesIfSameInstances() {
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
     * Tests the `assertContainsSameByIdentity` assertion with collections of different lengths.
     */
    public void assertContainsSameByIdentity_onCollectionWithElements_failsIfDifferentLengths() {
        Iterable<NumberOnly> shorter = NumberOnly.listOf(1, 2, 3, 4);
        Iterable<NumberOnly> longer = NumberOnly.listOf(1, 2, 3, 4, 5);
        assertThrows(AssertionFailedError.class, () -> assertContainsSameByIdentity(shorter, longer));
        assertThrows(AssertionFailedError.class, () -> assertContainsSameByIdentity(longer, shorter));
    }

    /// Tests the `assertContainsSameByIdentity` assertion with a custom error message.
    ///
    /// Verifies that when the assertion fails due to different collections, the custom message
    /// is included in the thrown [AssertionFailedError]. This ensures that developers can
    /// provide contextual error messages to aid in debugging test failures.
    ///
    /// Uses collections with different sizes to guarantee failure and validate message handling.
    @Test
    public void assertContainsSameByIdentity_withCustomMessage_includesMessageOnFailure() {
        assertThrows(AssertionFailedError.class,
                () -> assertContainsSameByIdentity(IterableOnly.of(1, 2),
                        IterableOnly.of(1), TEST_MESSAGE), TEST_MESSAGE);
    }

    /// Tests the `assertContainsSameByIdentity` assertion with a message supplier.
    ///
    /// Verifies that when the assertion fails due to different collections, the message from
    /// the supplier function is included in the thrown [AssertionFailedError]. The supplier
    /// pattern allows for lazy evaluation of error messages, which can be more efficient when
    /// the message construction is expensive.
    ///
    /// Uses collections with different sizes to guarantee failure and validate message supplier handling.
    @Test
    public void assertContainsSameByIdentity_withMessageSupplier_includesMessageOnFailure() {
        assertThrows(AssertionFailedError.class,
                () -> assertContainsSameByIdentity(IterableOnly.of(1, 2),
                        IterableOnly.of(1), () -> TEST_MESSAGE), TEST_MESSAGE);
    }
}
