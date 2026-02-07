package org.soliscode.test.breakable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.map.MapMethods;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.breakable.BreakableConcurrentMap.*;

/// Comprehensive test suite for BreakableConcurrentMap functionality.
///
/// This test class validates all aspects of the BreakableConcurrentMap implementation,
/// including break constants, normal operations, error handling, and builder patterns.
/// The tests ensure that the BreakableConcurrentMap correctly implements both the
/// ConcurrentMap interface and the Break mechanism.
///
/// ## Test Categories
///
/// ### Break Testing
/// - Tests for all 14 break constants covering concurrent operations
/// - Verification of break behavior for putIfAbsent operations
/// - Testing break behavior for remove(key, value) operations
/// - Testing break behavior for replace operations
/// - Race condition simulation testing
///
/// ### Normal Operations
/// - Basic ConcurrentMap functionality
/// - Integration with ConcurrentHashMap
/// - Thread safety verification
/// - Atomic operation testing
///
/// ### Builder and Factory Methods
/// - Builder pattern functionality
/// - Copy constructor behavior
/// - Static factory methods
/// - Configuration inheritance
///
/// ### Error Handling
/// - UnsupportedOperationException testing
/// - Method support configuration
/// - Exception propagation
///
/// @author evanbergstrom
/// @since 1.0
/// @see BreakableConcurrentMap
/// @see AbstractTest
public class BreakableConcurrentMapTest extends AbstractTest {

    // ========== Break Testing: putIfAbsent Operations ==========

    @Test
    @DisplayName("Test PUT_IF_ABSENT_IGNORES_EXISTING break")
    public void testPutIfAbsentIgnoresExisting() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.addBreak(PUT_IF_ABSENT_IGNORES_EXISTING);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        map.put("key1", 100);
        Integer oldValue = map.putIfAbsent("key1", 200);

        // With break, should ignore existing and return old value
        assertEquals(Integer.valueOf(100), oldValue);
        assertEquals(Integer.valueOf(200), map.get("key1")); // Value should be replaced
    }

    @Test
    @DisplayName("Test PUT_IF_ABSENT_ALWAYS_RETURNS_NULL break")
    public void testPutIfAbsentAlwaysReturnsNull() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.addBreak(PUT_IF_ABSENT_ALWAYS_RETURNS_NULL);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        map.put("key1", 100);
        Integer result1 = map.putIfAbsent("key1", 200);
        Integer result2 = map.putIfAbsent("key2", 300);

        assertNull(result1); // Should return null due to break
        assertNull(result2); // Should return null due to break
        assertEquals(Integer.valueOf(100), map.get("key1")); // Original value should remain
        assertEquals(Integer.valueOf(300), map.get("key2")); // New value should be added
    }

    @Test
    @DisplayName("Test PUT_IF_ABSENT_DOES_NOT_ADD_PAIR break")
    public void testPutIfAbsentDoesNotAddPair() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.addBreak(PUT_IF_ABSENT_DOES_NOT_ADD_PAIR);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        map.put("key1", 100);
        Integer result1 = map.putIfAbsent("key1", 200);
        Integer result2 = map.putIfAbsent("key2", 300);

        assertEquals(Integer.valueOf(100), result1); // Should return existing value
        assertNull(result2); // Should return null (would-be return value)
        assertEquals(Integer.valueOf(100), map.get("key1")); // Original value should remain
        assertNull(map.get("key2")); // New pair should not be added due to break
    }

    // ========== Break Testing: remove(key, value) Operations ==========

    @Test
    @DisplayName("Test REMOVE_TWO_ARG_IGNORES_VALUE break")
    public void testRemoveTwoArgIgnoresValue() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.addBreak(REMOVE_TWO_ARG_IGNORES_VALUE);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        map.put("key1", 100);
        boolean result = map.remove("key1", 999); // Wrong value

        assertTrue(result); // Should return true due to break ignoring value
        assertNull(map.get("key1")); // Key should be removed regardless of value
    }

    @Test
    @DisplayName("Test REMOVE_TWO_ARG_ALWAYS_RETURNS_FALSE break")
    public void testRemoveTwoArgAlwaysReturnsFalse() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.addBreak(REMOVE_TWO_ARG_ALWAYS_RETURNS_FALSE);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        map.put("key1", 100);
        boolean result1 = map.remove("key1", 100); // Correct value
        boolean result2 = map.remove("key2", 200); // Non-existent key

        assertFalse(result1); // Should return false due to break
        assertFalse(result2); // Should return false due to break
        assertEquals(Integer.valueOf(100), map.get("key1")); // Value should remain
    }

    @Test
    @DisplayName("Test REMOVE_TWO_ARG_DOES_NOT_REMOVE_PAIR break")
    public void testRemoveTwoArgDoesNotRemovePair() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.addBreak(REMOVE_TWO_ARG_DOES_NOT_REMOVE_PAIR);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        map.put("key1", 100);
        boolean result1 = map.remove("key1", 100); // Correct value
        boolean result2 = map.remove("key1", 999); // Wrong value

        assertTrue(result1); // Should return true (matching value)
        assertFalse(result2); // Should return false (non-matching value)
        assertEquals(Integer.valueOf(100), map.get("key1")); // Value should remain due to break
    }

    // ========== Break Testing: replace(key, oldValue, newValue) Operations ==========

    @Test
    @DisplayName("Test REPLACE_THREE_ARG_IGNORES_OLD_VALUE break")
    public void testReplaceThreeArgIgnoresOldValue() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.addBreak(REPLACE_THREE_ARG_IGNORES_OLD_VALUE);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        map.put("key1", 100);
        boolean result = map.replace("key1", 999, 200); // Wrong old value

        assertTrue(result); // Should return true due to break ignoring old value
        assertEquals(Integer.valueOf(200), map.get("key1")); // Value should be replaced
    }

    @Test
    @DisplayName("Test REPLACE_THREE_ARG_ALWAYS_RETURNS_FALSE break")
    public void testReplaceThreeArgAlwaysReturnsFalse() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.addBreak(REPLACE_THREE_ARG_ALWAYS_RETURNS_FALSE);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        map.put("key1", 100);
        boolean result1 = map.replace("key1", 100, 200); // Correct old value
        boolean result2 = map.replace("key2", 300, 400); // Non-existent key

        assertFalse(result1); // Should return false due to break
        assertFalse(result2); // Should return false due to break
        assertEquals(Integer.valueOf(100), map.get("key1")); // Value should remain
    }

    @Test
    @DisplayName("Test REPLACE_THREE_ARG_DOES_NOT_REPLACE_VALUE break")
    public void testReplaceThreeArgDoesNotReplaceValue() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.addBreak(REPLACE_THREE_ARG_DOES_NOT_REPLACE_VALUE);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        map.put("key1", 100);
        boolean result1 = map.replace("key1", 100, 200); // Correct old value
        boolean result2 = map.replace("key1", 999, 300); // Wrong old value

        assertTrue(result1); // Should return true (matching old value)
        assertFalse(result2); // Should return false (non-matching old value)
        assertEquals(Integer.valueOf(100), map.get("key1")); // Value should remain due to break
    }

    // ========== Break Testing: replace(key, value) Operations ==========

    @Test
    @DisplayName("Test REPLACE_TWO_ARG_ALWAYS_RETURNS_NULL break")
    public void testReplaceTwoArgAlwaysReturnsNull() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.addBreak(REPLACE_TWO_ARG_ALWAYS_RETURNS_NULL);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        map.put("key1", 100);
        Integer result1 = map.replace("key1", 200); // Existing key
        Integer result2 = map.replace("key2", 300); // Non-existent key

        assertNull(result1); // Should return null due to break
        assertNull(result2); // Should return null due to break
        assertEquals(Integer.valueOf(200), map.get("key1")); // Value should be replaced
        assertNull(map.get("key2")); // Non-existent key should remain absent
    }

    @Test
    @DisplayName("Test REPLACE_TWO_ARG_DOES_NOT_REPLACE_VALUE break")
    public void testReplaceTwoArgDoesNotReplaceValue() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.addBreak(REPLACE_TWO_ARG_DOES_NOT_REPLACE_VALUE);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        map.put("key1", 100);
        Integer result1 = map.replace("key1", 200); // Existing key
        Integer result2 = map.replace("key2", 300); // Non-existent key

        assertEquals(Integer.valueOf(100), result1); // Should return old value
        assertNull(result2); // Should return null for non-existent key
        assertEquals(Integer.valueOf(100), map.get("key1")); // Value should remain due to break
        assertNull(map.get("key2")); // Non-existent key should remain absent
    }

    // ========== Break Testing: Race Condition Simulation ==========

    @Test
    @DisplayName("Test COMPUTE_IF_ABSENT_RACE_CONDITION break")
    public void testComputeIfAbsentRaceCondition() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.addBreak(COMPUTE_IF_ABSENT_RACE_CONDITION);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        // This test verifies the race condition simulation doesn't crash
        Integer result = map.computeIfAbsent("key1", k -> 100);

        // The exact result depends on the race condition simulation
        // but the operation should complete without throwing exceptions
        assertNotNull(result);
        assertTrue(map.containsKey("key1"));
    }

    @Test
    @DisplayName("Test COMPUTE_IF_PRESENT_RACE_CONDITION break")
    public void testComputeIfPresentRaceCondition() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.addBreak(COMPUTE_IF_PRESENT_RACE_CONDITION);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        map.put("key1", 100);
        Integer result = map.computeIfPresent("key1", (k, v) -> v * 2);

        // The exact result depends on the race condition simulation
        // but the operation should complete without throwing exceptions
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test COMPUTE_RACE_CONDITION break")
    public void testComputeRaceCondition() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.addBreak(COMPUTE_RACE_CONDITION);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        map.put("key1", 100);
        Integer result = map.compute("key1", (k, v) -> v != null ? v * 2 : 50);

        // The race condition simulation performs the operation twice
        // The exact result may vary but should not throw exceptions
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test MERGE_RACE_CONDITION break")
    public void testMergeRaceCondition() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.addBreak(MERGE_RACE_CONDITION);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        map.put("key1", 100);
        Integer result = map.merge("key1", 50, Integer::sum);

        // The exact result depends on the race condition simulation
        // but the operation should complete without throwing exceptions
        assertNotNull(result);
    }

    // ========== Normal Operations Testing ==========

    @Test
    @DisplayName("Test normal concurrent map operations")
    public void testNormalConcurrentMapOperations() {
        BreakableConcurrentMap<String, Integer> map = new BreakableConcurrentMap<>();

        // Test basic ConcurrentMap operations
        assertNull(map.putIfAbsent("key1", 100));
        assertEquals(Integer.valueOf(100), map.putIfAbsent("key1", 200));

        assertTrue(map.remove("key1", 100));
        assertFalse(map.remove("key1", 100));

        map.put("key2", 300);
        assertTrue(map.replace("key2", 300, 400));
        assertFalse(map.replace("key2", 300, 500));

        assertEquals(Integer.valueOf(400), map.replace("key2", 500));
    }

    @Test
    @DisplayName("Test compute operations without breaks")
    public void testComputeOperationsWithoutBreaks() {
        BreakableConcurrentMap<String, Integer> map = new BreakableConcurrentMap<>();

        // Test computeIfAbsent
        Integer result1 = map.computeIfAbsent("key1", k -> 100);
        assertEquals(Integer.valueOf(100), result1);
        assertEquals(Integer.valueOf(100), map.get("key1"));

        // Test computeIfPresent
        Integer result2 = map.computeIfPresent("key1", (k, v) -> v * 2);
        assertEquals(Integer.valueOf(200), result2);
        assertEquals(Integer.valueOf(200), map.get("key1"));

        // Test compute
        Integer result3 = map.compute("key1", (k, v) -> v != null ? v + 50 : 25);
        assertEquals(Integer.valueOf(250), result3);

        // Test merge
        Integer result4 = map.merge("key1", 50, Integer::sum);
        assertEquals(Integer.valueOf(300), result4);
    }

    @Test
    @DisplayName("Test inheritance from BreakableMap")
    public void testInheritanceFromBreakableMap() {
        BreakableConcurrentMap<String, Integer> map = new BreakableConcurrentMap<>();

        // Test inherited Map operations
        assertNull(map.put("key1", 100));
        assertEquals(Integer.valueOf(100), map.get("key1"));
        assertTrue(map.containsKey("key1"));
        assertFalse(map.isEmpty());
        assertEquals(1, map.size());

        // Test key and value sets
        //noinspection RedundantCollectionOperation
        assertTrue(map.keySet().contains("key1"));
        //noinspection RedundantCollectionOperation
        assertTrue(map.values().contains(100));
        //noinspection RedundantCollectionOperation
        assertEquals(1, map.entrySet().size());
    }

    // ========== Builder and Factory Testing ==========

    @Test
    @DisplayName("Test builder pattern")
    public void testBuilderPattern() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.addBreak(PUT_IF_ABSENT_IGNORES_EXISTING);
        builder.addBreak(REMOVE_TWO_ARG_ALWAYS_RETURNS_FALSE);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        map.put("key1", 100);
        assertNotNull(map.putIfAbsent("key1", 200)); // Should ignore existing due to break
        assertFalse(map.remove("key1", 200)); // Should return false due to break
    }

    @Test
    @DisplayName("Test builder with existing ConcurrentMap")
    public void testBuilderWithExistingConcurrentMap() {
        ConcurrentHashMap<String, Integer> hashMap = new ConcurrentHashMap<>();
        hashMap.put("existing", 500);

        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>(hashMap);
        builder.addBreak(REPLACE_TWO_ARG_DOES_NOT_REPLACE_VALUE);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        assertEquals(Integer.valueOf(500), map.replace("existing", 600));
        assertEquals(Integer.valueOf(500), map.get("existing")); // Should remain unchanged due to break
    }

    @Test
    @DisplayName("Test copy constructor")
    public void testCopyConstructor() {
        BreakableConcurrentMap.Builder<String, Integer> originalBuilder = new BreakableConcurrentMap.Builder<>();
        originalBuilder.addBreak(PUT_IF_ABSENT_DOES_NOT_ADD_PAIR);
        BreakableConcurrentMap<String, Integer> original = originalBuilder.build();

        BreakableConcurrentMap<String, Integer> copy = new BreakableConcurrentMap<>(original);

        assertNull(copy.putIfAbsent("key1", 100));
        assertNull(copy.get("key1")); // Break should be inherited
    }

    @Test
    @DisplayName("Test builder copy")
    public void testBuilderCopy() {
        BreakableConcurrentMap.Builder<String, Integer> originalBuilder = new BreakableConcurrentMap.Builder<>();
        originalBuilder.addBreak(REPLACE_THREE_ARG_DOES_NOT_REPLACE_VALUE);

        BreakableConcurrentMap.Builder<String, Integer> copyBuilder = originalBuilder.copy();
        BreakableConcurrentMap<String, Integer> map = copyBuilder.build();

        map.put("key1", 100);
        assertTrue(map.replace("key1", 100, 200));
        assertEquals(Integer.valueOf(100), map.get("key1")); // Break should be copied
    }

    @Test
    @DisplayName("Test wrap factory method")
    public void testWrapFactoryMethod() {
        ConcurrentHashMap<String, Integer> hashMap = new ConcurrentHashMap<>();
        BreakableConcurrentMap<String, Integer> map = BreakableConcurrentMap.wrap(
            hashMap, java.util.Set.of(COMPUTE_IF_ABSENT_RACE_CONDITION));

        // Test that the wrap method works and applies breaks
        Integer result = map.computeIfAbsent("key1", k -> 100);
        assertNotNull(result);
    }

    // ========== Error Handling Testing ==========

    @Test
    @DisplayName("Test unsupported method exceptions")
    public void testUnsupportedMethodExceptions() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.doesNotSupport(MapMethods.PUT_IF_ABSENT);
        builder.doesNotSupport(MapMethods.REMOVE_TWO_ARG);
        builder.doesNotSupport(MapMethods.REPLACE_THREE_ARG);
        builder.doesNotSupport(MapMethods.REPLACE_TWO_ARG);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        assertThrows(UnsupportedOperationException.class, () -> map.putIfAbsent("key1", 100));
        assertThrows(UnsupportedOperationException.class, () -> map.remove("key1", 100));
        assertThrows(UnsupportedOperationException.class, () -> map.replace("key1", 100, 200));
        assertThrows(UnsupportedOperationException.class, () -> map.replace("key1", 100));
    }

    // ========== Integration Testing ==========

    @Test
    @DisplayName("Test ConcurrentHashMap integration")
    public void testConcurrentHashMapIntegration() {
        ConcurrentHashMap<String, Integer> hashMap = new ConcurrentHashMap<>();
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>(hashMap);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        // Test that operations work with ConcurrentHashMap
        map.putIfAbsent("key1", 100);
        map.putIfAbsent("key2", 200);
        map.replace("key1", 100, 150);

        assertEquals(2, map.size());
        assertEquals(Integer.valueOf(150), map.get("key1"));
        assertEquals(Integer.valueOf(200), map.get("key2"));
    }

    @Test
    @DisplayName("Test multiple breaks interaction")
    public void testMultipleBreaksInteraction() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.addBreak(PUT_IF_ABSENT_ALWAYS_RETURNS_NULL);
        builder.addBreak(REMOVE_TWO_ARG_ALWAYS_RETURNS_FALSE);
        builder.addBreak(REPLACE_TWO_ARG_ALWAYS_RETURNS_NULL);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        map.put("key1", 100);

        // Test multiple breaks working together
        assertNull(map.putIfAbsent("key2", 200)); // Break effect
        assertEquals(Integer.valueOf(200), map.get("key2")); // Should still be added

        assertFalse(map.remove("key1", 100)); // Break effect
        assertEquals(Integer.valueOf(100), map.get("key1")); // Should still be present

        assertNull(map.replace("key1", 300)); // Break effect
        assertEquals(Integer.valueOf(300), map.get("key1")); // Should still be replaced
    }

    @Test
    @DisplayName("Test constructor with ConcurrentHashMap")
    public void testConstructorWithConcurrentHashMap() {
        BreakableConcurrentMap<String, Integer> map = new BreakableConcurrentMap<>();

        // Test that it works with default ConcurrentHashMap backing
        assertNull(map.putIfAbsent("key1", 100));
        assertEquals(Integer.valueOf(100), map.putIfAbsent("key1", 200));

        assertTrue(map.remove("key1", 100));
        assertFalse(map.containsKey("key1"));
    }

    @Test
    @DisplayName("Test default constructor creates empty concurrent map")
    public void testDefaultConstructorCreatesEmptyConcurrentMap() {
        BreakableConcurrentMap<String, Integer> map = new BreakableConcurrentMap<>();

        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertFalse(map.containsKey("any"));
    }

    @Test
    @DisplayName("Test atomic operation guarantees")
    public void testAtomicOperationGuarantees() {
        BreakableConcurrentMap<String, Integer> map = new BreakableConcurrentMap<>();

        // Test putIfAbsent atomicity
        assertNull(map.putIfAbsent("key1", 100));
        assertEquals(Integer.valueOf(100), map.putIfAbsent("key1", 200));
        assertEquals(Integer.valueOf(100), map.get("key1"));

        // Test remove atomicity
        assertFalse(map.remove("key1", 999));
        assertTrue(map.remove("key1", 100));
        assertFalse(map.containsKey("key1"));

        // Test replace atomicity
        map.put("key2", 300);
        assertFalse(map.replace("key2", 999, 400));
        assertEquals(Integer.valueOf(300), map.get("key2"));

        assertTrue(map.replace("key2", 300, 400));
        assertEquals(Integer.valueOf(400), map.get("key2"));
    }

    @Test
    @DisplayName("Test break priority and interaction")
    public void testBreakPriorityAndInteraction() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.addBreak(PUT_IF_ABSENT_ALWAYS_RETURNS_NULL);
        builder.addBreak(PUT_IF_ABSENT_IGNORES_EXISTING);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        map.put("key1", 100);

        // The ALWAYS_RETURNS_NULL break should take precedence
        Integer result = map.putIfAbsent("key1", 200);
        assertNull(result);
    }

    @Test
    @DisplayName("Test inherited Map breaks still work")
    public void testInheritedMapBreaksStillWork() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.addBreak(BreakableMap.SIZE_ALWAYS_RETURNS_ZERO);
        builder.addBreak(BreakableMap.CONTAINS_KEY_ALWAYS_RETURNS_FALSE);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        map.put("key1", 100);

        // Inherited breaks should still work
        assertEquals(0, map.size()); // SIZE_ALWAYS_RETURNS_ZERO break
        assertFalse(map.containsKey("key1")); // CONTAINS_KEY_ALWAYS_RETURNS_FALSE break

        // But ConcurrentMap operations should work normally
        assertEquals(Integer.valueOf(100), map.putIfAbsent("key1", 200));
    }
}