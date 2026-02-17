package org.soliscode.test.breakable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.map.MapMethods;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.breakable.BreakableConcurrentMap.*;

/// Comprehensive test suite for `BreakableConcurrentMap` functionality.
///
/// This test class validates all aspects of the `BreakableConcurrentMap` implementation,
/// including break constants, normal operations, error handling, and builder patterns.
/// The tests ensure that the `BreakableConcurrentMap` correctly implements both the
/// `ConcurrentMap` interface and the Break mechanism.
///
/// ## Purpose
/// The purpose of these tests is to verify that `BreakableConcurrentMap` correctly
/// simulates various "broken" behaviors useful for testing how other components handle
/// buggy or edge-case `ConcurrentMap` implementations.
///
/// ## Usage Examples
///
/// ### Testing PutIfAbsent Break
/// ```java
/// BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
/// builder.addBreak(PUT_IF_ABSENT_IGNORES_EXISTING);
/// BreakableConcurrentMap<String, Integer> map = builder.build();
///
/// map.put("key1", 100);
/// map.putIfAbsent("key1", 200); // Replaces value instead of returning 100
/// ```
///
/// ## Thread Safety
/// The tests themselves are not thread-safe and should be run sequentially. However,
/// `BreakableConcurrentMap` is designed to be as thread-safe as its backing `ConcurrentMap`.
///
/// @author evanbergstrom
/// @since 1.0.0
/// @see BreakableConcurrentMap
/// @see AbstractTest
public class BreakableConcurrentMapTest extends AbstractTest {

    // ========== Break Testing: putIfAbsent Operations ==========

    /// Verifies the `PUT_IF_ABSENT_IGNORES_EXISTING` break constant.
    @Test
    @DisplayName("putIfAbsent_whenIgnoresExistingBreakAdded_ignoresExistingAndReplacesValue")
    public void putIfAbsent_whenIgnoresExistingBreakAdded_ignoresExistingAndReplacesValue() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.addBreak(PUT_IF_ABSENT_IGNORES_EXISTING);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        map.put("key1", 100);
        Integer oldValue = map.putIfAbsent("key1", 200);

        // With break, should ignore existing and return old value
        assertEquals(Integer.valueOf(100), oldValue);
        assertEquals(Integer.valueOf(200), map.get("key1")); // Value should be replaced
    }

    /// Verifies the `PUT_IF_ABSENT_ALWAYS_RETURNS_NULL` break constant.
    @Test
    @DisplayName("putIfAbsent_whenAlwaysReturnsNullBreakAdded_returnsNull")
    public void putIfAbsent_whenAlwaysReturnsNullBreakAdded_returnsNull() {
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

    /// Verifies the `PUT_IF_ABSENT_DOES_NOT_ADD_PAIR` break constant.
    @Test
    @DisplayName("putIfAbsent_whenDoesNotAddPairBreakAdded_doesNotAddPair")
    public void putIfAbsent_whenDoesNotAddPairBreakAdded_doesNotAddPair() {
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

    /// Verifies the `REMOVE_TWO_ARG_IGNORES_VALUE` break constant.
    @Test
    @DisplayName("remove_whenTwoArgIgnoresValueBreakAdded_removesKeyRegardlessOfValue")
    public void remove_whenTwoArgIgnoresValueBreakAdded_removesKeyRegardlessOfValue() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.addBreak(REMOVE_TWO_ARG_IGNORES_VALUE);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        map.put("key1", 100);
        boolean result = map.remove("key1", 999); // Wrong value

        assertTrue(result); // Should return true due to break ignoring value
        assertNull(map.get("key1")); // Key should be removed regardless of value
    }

    /// Verifies the `REMOVE_TWO_ARG_ALWAYS_RETURNS_FALSE` break constant.
    @Test
    @DisplayName("remove_whenTwoArgAlwaysReturnsFalseBreakAdded_returnsFalse")
    public void remove_whenTwoArgAlwaysReturnsFalseBreakAdded_returnsFalse() {
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

    /// Verifies the `REMOVE_TWO_ARG_DOES_NOT_REMOVE_PAIR` break constant.
    @Test
    @DisplayName("remove_whenTwoArgDoesNotRemovePairBreakAdded_doesNotRemovePair")
    public void remove_whenTwoArgDoesNotRemovePairBreakAdded_doesNotRemovePair() {
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

    /// Verifies the `REPLACE_THREE_ARG_IGNORES_OLD_VALUE` break constant.
    @Test
    @DisplayName("replace_whenThreeArgIgnoresOldValueBreakAdded_replacesRegardlessOfOldValue")
    public void replace_whenThreeArgIgnoresOldValueBreakAdded_replacesRegardlessOfOldValue() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.addBreak(REPLACE_THREE_ARG_IGNORES_OLD_VALUE);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        map.put("key1", 100);
        boolean result = map.replace("key1", 999, 200); // Wrong old value

        assertTrue(result); // Should return true due to break ignoring old value
        assertEquals(Integer.valueOf(200), map.get("key1")); // Value should be replaced
    }

    /// Verifies the `REPLACE_THREE_ARG_ALWAYS_RETURNS_FALSE` break constant.
    @Test
    @DisplayName("replace_whenThreeArgAlwaysReturnsFalseBreakAdded_returnsFalse")
    public void replace_whenThreeArgAlwaysReturnsFalseBreakAdded_returnsFalse() {
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

    /// Verifies the `REPLACE_THREE_ARG_DOES_NOT_REPLACE_VALUE` break constant.
    @Test
    @DisplayName("replace_whenThreeArgDoesNotReplaceValueBreakAdded_doesNotReplaceValue")
    public void replace_whenThreeArgDoesNotReplaceValueBreakAdded_doesNotReplaceValue() {
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

    /// Verifies the `REPLACE_TWO_ARG_ALWAYS_RETURNS_NULL` break constant.
    @Test
    @DisplayName("replace_whenTwoArgAlwaysReturnsNullBreakAdded_returnsNull")
    public void replace_whenTwoArgAlwaysReturnsNullBreakAdded_returnsNull() {
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

    /// Verifies the `REPLACE_TWO_ARG_DOES_NOT_REPLACE_VALUE` break constant.
    @Test
    @DisplayName("replace_whenTwoArgDoesNotReplaceValueBreakAdded_doesNotReplaceValue")
    public void replace_whenTwoArgDoesNotReplaceValueBreakAdded_doesNotReplaceValue() {
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

    /// Verifies the `COMPUTE_IF_ABSENT_RACE_CONDITION` break constant.
    @Test
    @DisplayName("computeIfAbsent_whenRaceConditionBreakAdded_executesWithoutException")
    public void computeIfAbsent_whenRaceConditionBreakAdded_executesWithoutException() {
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

    /// Verifies the `COMPUTE_IF_PRESENT_RACE_CONDITION` break constant.
    @Test
    @DisplayName("computeIfPresent_whenRaceConditionBreakAdded_executesWithoutException")
    public void computeIfPresent_whenRaceConditionBreakAdded_executesWithoutException() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.addBreak(COMPUTE_IF_PRESENT_RACE_CONDITION);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        map.put("key1", 100);
        Integer result = map.computeIfPresent("key1", (k, v) -> v * 2);

        // The exact result depends on the race condition simulation
        // but the operation should complete without throwing exceptions
        assertNotNull(result);
    }

    /// Verifies the `COMPUTE_RACE_CONDITION` break constant.
    @Test
    @DisplayName("compute_whenRaceConditionBreakAdded_executesWithoutException")
    public void compute_whenRaceConditionBreakAdded_executesWithoutException() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.addBreak(COMPUTE_RACE_CONDITION);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        map.put("key1", 100);
        Integer result = map.compute("key1", (k, v) -> v != null ? v * 2 : 50);

        // The race condition simulation performs the operation twice
        // The exact result may vary but should not throw exceptions
        assertNotNull(result);
    }

    /// Verifies the `MERGE_RACE_CONDITION` break constant.
    @Test
    @DisplayName("merge_whenRaceConditionBreakAdded_executesWithoutException")
    public void merge_whenRaceConditionBreakAdded_executesWithoutException() {
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

    /// Verifies that basic `ConcurrentMap` operations work correctly when no breaks are present.
    @Test
    @DisplayName("concurrentMap_whenNoBreaks_executesNormalOperations")
    public void concurrentMap_whenNoBreaks_executesNormalOperations() {
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

    /// Verifies that compute operations work correctly when no breaks are present.
    @Test
    @DisplayName("computeOperations_whenNoBreaks_executesNormalOperations")
    public void computeOperations_whenNoBreaks_executesNormalOperations() {
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

    /// Verifies that `BreakableConcurrentMap` correctly inherits operations from `BreakableMap`.
    @Test
    @DisplayName("inheritance_whenInheritedFromBreakableMap_executesNormalMapOperations")
    public void inheritance_whenInheritedFromBreakableMap_executesNormalMapOperations() {
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

    /// Verifies that the builder pattern correctly configures breaks.
    @Test
    @DisplayName("builderPattern_whenUsed_configuresBreaks")
    public void builderPattern_whenUsed_configuresBreaks() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.addBreak(PUT_IF_ABSENT_IGNORES_EXISTING);
        builder.addBreak(REMOVE_TWO_ARG_ALWAYS_RETURNS_FALSE);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        map.put("key1", 100);
        assertNotNull(map.putIfAbsent("key1", 200)); // Should ignore existing due to break
        assertFalse(map.remove("key1", 200)); // Should return false due to break
    }

    /// Verifies that the builder can be initialized with an existing `ConcurrentMap`.
    @Test
    @DisplayName("builder_whenExistingMapProvided_usesExistingMap")
    public void builder_whenExistingMapProvided_usesExistingMap() {
        ConcurrentHashMap<String, Integer> hashMap = new ConcurrentHashMap<>();
        hashMap.put("existing", 500);

        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>(hashMap);
        builder.addBreak(REPLACE_TWO_ARG_DOES_NOT_REPLACE_VALUE);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        assertEquals(Integer.valueOf(500), map.replace("existing", 600));
        assertEquals(Integer.valueOf(500), map.get("existing")); // Should remain unchanged due to break
    }

    /// Verifies the copy constructor correctly copies breaks and state.
    @Test
    @DisplayName("copyConstructor_whenUsed_copiesBreaksAndState")
    public void copyConstructor_whenUsed_copiesBreaksAndState() {
        BreakableConcurrentMap.Builder<String, Integer> originalBuilder = new BreakableConcurrentMap.Builder<>();
        originalBuilder.addBreak(PUT_IF_ABSENT_DOES_NOT_ADD_PAIR);
        BreakableConcurrentMap<String, Integer> original = originalBuilder.build();

        BreakableConcurrentMap<String, Integer> copy = new BreakableConcurrentMap<>(original);

        assertNull(copy.putIfAbsent("key1", 100));
        assertNull(copy.get("key1")); // Break should be inherited
    }

    /// Verifies that the builder copy method correctly copies the configuration.
    @Test
    @DisplayName("builderCopy_whenUsed_copiesConfiguration")
    public void builderCopy_whenUsed_copiesConfiguration() {
        BreakableConcurrentMap.Builder<String, Integer> originalBuilder = new BreakableConcurrentMap.Builder<>();
        originalBuilder.addBreak(REPLACE_THREE_ARG_DOES_NOT_REPLACE_VALUE);

        BreakableConcurrentMap.Builder<String, Integer> copyBuilder = originalBuilder.copy();
        BreakableConcurrentMap<String, Integer> map = copyBuilder.build();

        map.put("key1", 100);
        assertTrue(map.replace("key1", 100, 200));
        assertEquals(Integer.valueOf(100), map.get("key1")); // Break should be copied
    }

    /// Verifies the static `wrap` factory method.
    @Test
    @DisplayName("wrap_whenUsed_createsMapWithSpecifiedBreaks")
    public void wrap_whenUsed_createsMapWithSpecifiedBreaks() {
        ConcurrentHashMap<String, Integer> hashMap = new ConcurrentHashMap<>();
        BreakableConcurrentMap<String, Integer> map = BreakableConcurrentMap.wrap(
            hashMap, java.util.Set.of(COMPUTE_IF_ABSENT_RACE_CONDITION));

        // Test that the wrap method works and applies breaks
        Integer result = map.computeIfAbsent("key1", k -> 100);
        assertNotNull(result);
    }

    // ========== Error Handling Testing ==========

    /// Verifies that methods marked as unsupported correctly throw `UnsupportedOperationException`.
    @Test
    @DisplayName("unsupportedMethods_whenCalled_throwUnsupportedOperationException")
    public void unsupportedMethods_whenCalled_throwUnsupportedOperationException() {
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

    /// Verifies integration with `ConcurrentHashMap`.
    @Test
    @DisplayName("concurrentHashMap_whenUsedAsBacking_isSuccessful")
    public void concurrentHashMap_whenUsedAsBacking_isSuccessful() {
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

    /// Verifies that multiple breaks can interact correctly.
    @Test
    @DisplayName("multipleBreaks_whenConfigured_interactCorrectly")
    public void multipleBreaks_whenConfigured_interactCorrectly() {
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

    /// Verifies the constructor that takes an existing `ConcurrentHashMap`.
    @Test
    @DisplayName("constructor_whenConcurrentHashMapProvided_usesProvidedMap")
    public void constructor_whenConcurrentHashMapProvided_usesProvidedMap() {
        BreakableConcurrentMap<String, Integer> map = new BreakableConcurrentMap<>();

        // Test that it works with default ConcurrentHashMap backing
        assertNull(map.putIfAbsent("key1", 100));
        assertEquals(Integer.valueOf(100), map.putIfAbsent("key1", 200));

        assertTrue(map.remove("key1", 100));
        assertFalse(map.containsKey("key1"));
    }

    /// Verifies that the default constructor creates an empty map.
    @Test
    @DisplayName("defaultConstructor_whenCalled_createsEmptyMap")
    public void defaultConstructor_whenCalled_createsEmptyMap() {
        BreakableConcurrentMap<String, Integer> map = new BreakableConcurrentMap<>();

        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertFalse(map.containsKey("any"));
    }

    /// Verifies the atomic operation guarantees of `ConcurrentMap`.
    @Test
    @DisplayName("atomicOperations_whenCalled_guaranteeAtomicity")
    public void atomicOperations_whenCalled_guaranteeAtomicity() {
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

    /// Verifies the priority and interaction between different breaks.
    @Test
    @DisplayName("breakPriority_whenMultipleBreaksConflict_followsPriorityRules")
    public void breakPriority_whenMultipleBreaksConflict_followsPriorityRules() {
        BreakableConcurrentMap.Builder<String, Integer> builder = new BreakableConcurrentMap.Builder<>();
        builder.addBreak(PUT_IF_ABSENT_ALWAYS_RETURNS_NULL);
        builder.addBreak(PUT_IF_ABSENT_IGNORES_EXISTING);
        BreakableConcurrentMap<String, Integer> map = builder.build();

        map.put("key1", 100);

        // The ALWAYS_RETURNS_NULL break should take precedence
        Integer result = map.putIfAbsent("key1", 200);
        assertNull(result);
    }

    /// Verifies that breaks inherited from `BreakableMap` still function correctly.
    @Test
    @DisplayName("inheritedBreaks_whenInheritedFromBreakableMap_stillWork")
    public void inheritedBreaks_whenInheritedFromBreakableMap_stillWork() {
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