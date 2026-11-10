package com.soliscode.test.breakable;

import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.contract.map.MapMethods;

import static com.soliscode.test.breakable.BreakableConcurrentMap.COMPUTE_IF_ABSENT_IS_NOT_THREAD_SAFE;
import static com.soliscode.test.breakable.BreakableConcurrentMap.COMPUTE_IF_PRESENT_IS_NOT_THREAD_SAFE;
import static com.soliscode.test.breakable.BreakableConcurrentMap.COMPUTE_IS_NOT_THREAD_SAFE;
import static com.soliscode.test.breakable.BreakableConcurrentMap.MERGE_IS_NOT_THREAD_SAFE;
import static com.soliscode.test.breakable.BreakableConcurrentMap.PUT_IF_ABSENT_ALWAYS_RETURNS_NULL;
import static com.soliscode.test.breakable.BreakableConcurrentMap.PUT_IF_ABSENT_DOES_NOT_ADD_PAIR;
import static com.soliscode.test.breakable.BreakableConcurrentMap.PUT_IF_ABSENT_IGNORES_EXISTING;
import static com.soliscode.test.breakable.BreakableConcurrentMap.REMOVE_TWO_ARG_ALWAYS_RETURNS_FALSE;
import static com.soliscode.test.breakable.BreakableConcurrentMap.REMOVE_TWO_ARG_DOES_NOT_REMOVE_PAIR;
import static com.soliscode.test.breakable.BreakableConcurrentMap.REMOVE_TWO_ARG_IGNORES_VALUE;
import static com.soliscode.test.breakable.BreakableConcurrentMap.REPLACE_THREE_ARG_ALWAYS_RETURNS_FALSE;
import static com.soliscode.test.breakable.BreakableConcurrentMap.REPLACE_THREE_ARG_DOES_NOT_REPLACE_VALUE;
import static com.soliscode.test.breakable.BreakableConcurrentMap.REPLACE_THREE_ARG_IGNORES_OLD_VALUE;
import static com.soliscode.test.breakable.BreakableConcurrentMap.REPLACE_TWO_ARG_ALWAYS_RETURNS_NULL;
import static com.soliscode.test.breakable.BreakableConcurrentMap.REPLACE_TWO_ARG_DOES_NOT_REPLACE_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    /// Default constructor
    public BreakableConcurrentMapTest() {
        super();
    }

    // ========== Break Testing: putIfAbsent Operations ==========

    /// Verifies the `PUT_IF_ABSENT_IGNORES_EXISTING` break constant.
    @Test
    @DisplayName("putIfAbsent(K, V) with PUT_IF_ABSENT_IGNORES_EXISTING replaces an existing value")
    public void putIfAbsentWhenIgnoresExistingBreakAddedIgnoresExistingAndReplacesValue() {
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
    @DisplayName("putIfAbsent(K, V) with PUT_IF_ABSENT_ALWAYS_RETURNS_NULL returns null")
    public void putIfAbsentWhenAlwaysReturnsNullBreakAddedReturnsNull() {
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
    @DisplayName("putIfAbsent(K, V) with PUT_IF_ABSENT_DOES_NOT_ADD_PAIR does not add the pair")
    public void putIfAbsentWhenDoesNotAddPairBreakAddedDoesNotAddPair() {
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
    @DisplayName("remove(K, V) with REMOVE_TWO_ARG_IGNORES_VALUE removes the key whatever its value")
    public void removeWhenTwoArgIgnoresValueBreakAddedRemovesKeyRegardlessOfValue() {
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
    @DisplayName("remove(K, V) with REMOVE_TWO_ARG_ALWAYS_RETURNS_FALSE returns false")
    public void removeWhenTwoArgAlwaysReturnsFalseBreakAddedReturnsFalse() {
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
    @DisplayName("remove(K, V) with REMOVE_TWO_ARG_DOES_NOT_REMOVE_PAIR leaves the pair in the map")
    public void removeWhenTwoArgDoesNotRemovePairBreakAddedDoesNotRemovePair() {
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
    @DisplayName("replace(K, V, V) with REPLACE_THREE_ARG_IGNORES_OLD_VALUE replaces whatever the old value")
    public void replaceWhenThreeArgIgnoresOldValueBreakAddedReplacesRegardlessOfOldValue() {
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
    @DisplayName("replace(K, V, V) with REPLACE_THREE_ARG_ALWAYS_RETURNS_FALSE returns false")
    public void replaceWhenThreeArgAlwaysReturnsFalseBreakAddedReturnsFalse() {
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
    @DisplayName("replace(K, V, V) with REPLACE_THREE_ARG_DOES_NOT_REPLACE_VALUE leaves the value unchanged")
    public void replaceWhenThreeArgDoesNotReplaceValueBreakAddedDoesNotReplaceValue() {
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
    @DisplayName("replace(K, V) with REPLACE_TWO_ARG_ALWAYS_RETURNS_NULL returns null")
    public void replaceWhenTwoArgAlwaysReturnsNullBreakAddedReturnsNull() {
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
    @DisplayName("replace(K, V) with REPLACE_TWO_ARG_DOES_NOT_REPLACE_VALUE leaves the value unchanged")
    public void replaceWhenTwoArgDoesNotReplaceValueBreakAddedDoesNotReplaceValue() {
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

    // ========== Break Testing: Thread-safety Breaks, on One Thread ==========

    /// Verifies that `COMPUTE_IF_ABSENT_IS_NOT_THREAD_SAFE` gives `computeIfAbsent`'s results on one thread.
    @Test
    @DisplayName("computeIfAbsent(K, Function) with COMPUTE_IF_ABSENT_IS_NOT_THREAD_SAFE is correct on one thread")
    public void computeIfAbsentWhenNotThreadSafeBreakAddedIsCorrectOnOneThread() {
        BreakableConcurrentMap<String, Integer> map = new BreakableConcurrentMap.Builder<String, Integer>()
                .addBreak(COMPUTE_IF_ABSENT_IS_NOT_THREAD_SAFE).build();

        assertEquals(Integer.valueOf(100), map.computeIfAbsent("key1", k -> 100));
        assertEquals(Integer.valueOf(100), map.computeIfAbsent("key1", k -> 200));
        assertNull(map.computeIfAbsent("key2", k -> null));
        assertFalse(map.containsKey("key2"));
    }

    /// Verifies that `COMPUTE_IF_PRESENT_IS_NOT_THREAD_SAFE` gives `computeIfPresent`'s results on one thread.
    @Test
    @DisplayName("computeIfPresent(K, BiFunction) with COMPUTE_IF_PRESENT_IS_NOT_THREAD_SAFE is correct on one "
            + "thread")
    public void computeIfPresentWhenNotThreadSafeBreakAddedIsCorrectOnOneThread() {
        BreakableConcurrentMap<String, Integer> map = new BreakableConcurrentMap.Builder<String, Integer>()
                .addBreak(COMPUTE_IF_PRESENT_IS_NOT_THREAD_SAFE).build();
        map.put("key1", 100);

        assertEquals(Integer.valueOf(200), map.computeIfPresent("key1", (k, v) -> v * 2));
        assertEquals(Integer.valueOf(200), map.get("key1"));
        assertNull(map.computeIfPresent("key2", (k, v) -> 1));
        assertNull(map.computeIfPresent("key1", (k, v) -> null));
        assertFalse(map.containsKey("key1"));
    }

    /// Verifies that `COMPUTE_IS_NOT_THREAD_SAFE` gives `compute`'s results on one thread: the function is applied
    /// once.
    @Test
    @DisplayName("compute(K, BiFunction) with COMPUTE_IS_NOT_THREAD_SAFE is correct on one thread")
    public void computeWhenNotThreadSafeBreakAddedIsCorrectOnOneThread() {
        BreakableConcurrentMap<String, Integer> map = new BreakableConcurrentMap.Builder<String, Integer>()
                .addBreak(COMPUTE_IS_NOT_THREAD_SAFE).build();
        map.put("key1", 100);

        assertEquals(Integer.valueOf(200), map.compute("key1", (k, v) -> v != null ? v * 2 : 50));
        assertEquals(Integer.valueOf(200), map.get("key1"));
        assertEquals(Integer.valueOf(50), map.compute("key2", (k, v) -> v != null ? v * 2 : 50));
        assertNull(map.compute("key1", (k, v) -> null));
        assertFalse(map.containsKey("key1"));
    }

    /// Verifies that `MERGE_IS_NOT_THREAD_SAFE` gives `merge`'s results on one thread.
    @Test
    @DisplayName("merge(K, V, BiFunction) with MERGE_IS_NOT_THREAD_SAFE is correct on one thread")
    public void mergeWhenNotThreadSafeBreakAddedIsCorrectOnOneThread() {
        BreakableConcurrentMap<String, Integer> map = new BreakableConcurrentMap.Builder<String, Integer>()
                .addBreak(MERGE_IS_NOT_THREAD_SAFE).build();

        assertEquals(Integer.valueOf(50), map.merge("key1", 50, Integer::sum));
        assertEquals(Integer.valueOf(150), map.merge("key1", 100, Integer::sum));
        assertEquals(Integer.valueOf(150), map.get("key1"));
        assertNull(map.merge("key1", 1, (a, b) -> null));
        assertFalse(map.containsKey("key1"));
    }

    /// Verifies that `REPLACE_ALL_IS_NOT_THREAD_SAFE` gives `replaceAll`'s results on one thread, on a skip-list store
    /// too, whose entries do not support `setValue`.
    @Test
    @DisplayName("replaceAll(BiFunction) with REPLACE_ALL_IS_NOT_THREAD_SAFE is correct on one thread, on both stores")
    public void replaceAllWhenNotThreadSafeBreakAddedIsCorrectOnOneThread() {
        java.util.List<BreakableConcurrentMap<String, Integer>> maps = java.util.List.of(
                new BreakableConcurrentMap.Builder<String, Integer>()
                        .addBreak(BreakableConcurrentMap.REPLACE_ALL_IS_NOT_THREAD_SAFE).build(),
                new BreakableConcurrentNavigableMap.Builder<String, Integer>()
                        .addBreak(BreakableConcurrentMap.REPLACE_ALL_IS_NOT_THREAD_SAFE).build());
        for (final BreakableConcurrentMap<String, Integer> map : maps) {
            map.put("key1", 1);
            map.put("key2", 2);
            map.replaceAll((k, v) -> v * 10);
            assertEquals(Integer.valueOf(10), map.get("key1"));
            assertEquals(Integer.valueOf(20), map.get("key2"));
        }
    }

    // ========== Fixes ==========

    /// Verifies that a map refuses null keys and values, as its store does, however it is made.
    @Test
    @DisplayName("a BreakableConcurrentMap refuses null keys and values however it is made")
    public void permitsWhenMadeAnyWayRefuseNulls() {
        java.util.List<BreakableConcurrentMap<String, Integer>> maps = java.util.List.of(
                new BreakableConcurrentMap<>(), new BreakableConcurrentMap.Builder<String, Integer>().build(),
                BreakableConcurrentMap.wrap(new ConcurrentHashMap<>(), java.util.Set.of()));
        for (final BreakableConcurrentMap<String, Integer> map : maps) {
            assertFalse(map.permitsNullKeys());
            assertFalse(map.permitsNullValues());
        }
    }

    /// Verifies that a `null` function result removes the mapping, or records none, on a map that refuses nulls.
    @Test
    @DisplayName("a null function result removes the mapping or records none")
    public void computeWhenFunctionReturnsNullRemovesOrRecordsNoMapping() {
        BreakableConcurrentMap<String, Integer> map = new BreakableConcurrentMap.Builder<String, Integer>().build();
        assertNull(map.computeIfAbsent("key1", k -> null));
        map.put("key1", 1);
        assertNull(map.computeIfPresent("key1", (k, v) -> null));
        map.put("key1", 1);
        assertNull(map.compute("key1", (k, v) -> null));
        map.put("key1", 1);
        assertNull(map.merge("key1", 2, (a, b) -> null));
        assertTrue(map.isEmpty());
    }

    /// Verifies that a `null` function is refused where the function would not be called.
    @Test
    @DisplayName("a null function throws NullPointerException where it would not be called")
    @SuppressWarnings("NullAway") // passes null on purpose
    public void computeWhenFunctionIsNullThrowsNullPointerException() {
        BreakableConcurrentMap<String, Integer> map = new BreakableConcurrentMap.Builder<String, Integer>().build();
        map.put("present", 1);
        assertThrows(NullPointerException.class, () -> map.computeIfAbsent("present", null));
        assertThrows(NullPointerException.class, () -> map.computeIfPresent("absent", null));
        assertThrows(NullPointerException.class, () -> map.merge("absent", 1, null));
    }

    /// Verifies that a clone has a store of its own.
    @Test
    @DisplayName("clone() makes a map with a store of its own")
    public void cloneWhenChangedDoesNotChangeOriginal() {
        BreakableConcurrentMap<String, Integer> map = new BreakableConcurrentMap.Builder<String, Integer>().build();
        map.put("key1", 1);
        BreakableMap<String, Integer> clone = map.clone();
        assertInstanceOf(BreakableConcurrentMap.class, clone);
        assertNull(clone.putIfAbsent("key2", 2));
        assertEquals(Integer.valueOf(2), clone.get("key2"));
        assertFalse(map.containsKey("key2"));
    }

    // ========== Normal Operations Testing ==========

    /// Verifies that basic `ConcurrentMap` operations work correctly when no breaks are present.
    @Test
    @DisplayName("the ConcurrentMap methods behave normally with no breaks")
    public void concurrentMapWhenNoBreaksExecutesNormalOperations() {
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
    @DisplayName("the compute methods behave normally with no breaks")
    public void computeOperationsWhenNoBreaksExecutesNormalOperations() {
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
    @DisplayName("the Map methods inherited from BreakableMap behave normally")
    public void inheritanceWhenInheritedFromBreakableMapExecutesNormalMapOperations() {
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
    @DisplayName("the builder configures the breaks")
    public void builderPatternWhenUsedConfiguresBreaks() {
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
    @DisplayName("the builder uses the map it is given")
    public void builderWhenExistingMapProvidedUsesExistingMap() {
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
    @DisplayName("the copy constructor copies the breaks and the entries")
    public void copyConstructorWhenUsedCopiesBreaksAndState() {
        BreakableConcurrentMap.Builder<String, Integer> originalBuilder = new BreakableConcurrentMap.Builder<>();
        originalBuilder.addBreak(PUT_IF_ABSENT_DOES_NOT_ADD_PAIR);
        BreakableConcurrentMap<String, Integer> original = originalBuilder.build();

        BreakableConcurrentMap<String, Integer> copy = new BreakableConcurrentMap<>(original);

        assertNull(copy.putIfAbsent("key1", 100));
        assertNull(copy.get("key1")); // Break should be inherited
    }

    /// Verifies that the builder copy method correctly copies the configuration.
    @Test
    @DisplayName("a copied builder keeps the configuration")
    public void builderCopyWhenUsedCopiesConfiguration() {
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
    @DisplayName("wrap() makes a map with the given breaks")
    public void wrapWhenUsedCreatesMapWithSpecifiedBreaks() {
        ConcurrentHashMap<String, Integer> hashMap = new ConcurrentHashMap<>();
        BreakableConcurrentMap<String, Integer> map = BreakableConcurrentMap.wrap(
            hashMap, java.util.Set.of(COMPUTE_IF_ABSENT_IS_NOT_THREAD_SAFE));

        // Test that the wrap method works and applies breaks
        Integer result = map.computeIfAbsent("key1", k -> 100);
        assertNotNull(result);
    }

    // ========== Error Handling Testing ==========

    /// Verifies that methods marked as unsupported correctly throw `UnsupportedOperationException`.
    @Test
    @DisplayName("an unsupported method throws UnsupportedOperationException")
    public void unsupportedMethodsWhenCalledThrowUnsupportedOperationException() {
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
    @DisplayName("a ConcurrentHashMap works as the backing map")
    public void concurrentHashMapWhenUsedAsBackingIsSuccessful() {
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
    @DisplayName("several breaks configured together each take effect")
    public void multipleBreaksWhenConfiguredInteractCorrectly() {
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
    @DisplayName("the constructor uses the ConcurrentHashMap it is given")
    public void constructorWhenConcurrentHashMapProvidedUsesProvidedMap() {
        BreakableConcurrentMap<String, Integer> map = new BreakableConcurrentMap<>();

        // Test that it works with default ConcurrentHashMap backing
        assertNull(map.putIfAbsent("key1", 100));
        assertEquals(Integer.valueOf(100), map.putIfAbsent("key1", 200));

        assertTrue(map.remove("key1", 100));
        assertFalse(map.containsKey("key1"));
    }

    /// Verifies that the default constructor creates an empty map.
    @Test
    @DisplayName("the default constructor makes an empty map")
    public void defaultConstructorWhenCalledCreatesEmptyMap() {
        BreakableConcurrentMap<String, Integer> map = new BreakableConcurrentMap<>();

        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertFalse(map.containsKey("any"));
    }

    /// Verifies the atomic operation guarantees of `ConcurrentMap`.
    @Test
    @DisplayName("putIfAbsent, remove(K, V) and replace(K, V, V) act only when the condition holds")
    public void atomicOperationsWhenCalledGuaranteeAtomicity() {
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
    @DisplayName("putIfAbsent(K, V) with PUT_IF_ABSENT_ALWAYS_RETURNS_NULL and PUT_IF_ABSENT_IGNORES_EXISTING "
            + "returns null")
    public void breakPriorityWhenMultipleBreaksConflictFollowsPriorityRules() {
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
    @DisplayName("BreakableMap's breaks still work on a BreakableConcurrentMap")
    public void inheritedBreaksWhenInheritedFromBreakableMapStillWork() {
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
