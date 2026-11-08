/*
 * Copyright 2024 Evan Bergstrom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.soliscode.test.breakable;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.contract.map.MapMethods;

import static com.soliscode.test.breakable.BreakableMap.COMPUTE_DOES_NOT_COMPUTE_VALUE;
import static com.soliscode.test.breakable.BreakableMap.COMPUTE_IF_ABSENT_ALWAYS_COMPUTES_VALUE;
import static com.soliscode.test.breakable.BreakableMap.COMPUTE_IF_ABSENT_NEVER_COMPUTES_VALUE;
import static com.soliscode.test.breakable.BreakableMap.COMPUTE_IF_PRESENT_NEVER_COMPUTES_VALUE;
import static com.soliscode.test.breakable.BreakableMap.FOR_EACH_DOES_NOT_CALL_ACTION;
import static com.soliscode.test.breakable.BreakableMap.FOR_EACH_SKIPS_FIRST_PAIR;
import static com.soliscode.test.breakable.BreakableMap.GET_OR_DEFAULT_ALWAYS_RETURNS_NULL;
import static com.soliscode.test.breakable.BreakableMap.MERGE_DOES_NOT_MERGE_THE_VALUES;
import static com.soliscode.test.breakable.BreakableMap.PUT_IF_ABSENT_ALWAYS_REPLACES_VALUE;
import static com.soliscode.test.breakable.BreakableMap.PUT_IF_ABSENT_DOES_NOT_PUT_VALUE;
import static com.soliscode.test.breakable.BreakableMap.REMOVE_DOES_NOT_REMOVE_VALUE;
import static com.soliscode.test.breakable.BreakableMap.REPLACE_2_ARG_DOES_NOT_REPLACE_VALUE;
import static com.soliscode.test.breakable.BreakableMap.REPLACE_3_ARG_DOES_NOT_REPLACE_VALUE;
import static com.soliscode.test.breakable.BreakableMap.REPLACE_ALL_DOES_NOT_REPLACE_ANY_PAIRS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests of [BreakableMap]'s implementations of the [java.util.Map] interface's default methods, with and without
/// their breaks: `getOrDefault`, `forEach` and `replaceAll`; the conditional operations `putIfAbsent`,
/// `remove(Object, Object)` and the two `replace` methods; and `computeIfAbsent`, `computeIfPresent`, `compute` and
/// `merge`. `getOrDefault`, `forEach`, `replaceAll` and `remove(Object, Object)` are also tested where the map does
/// not support them.
///
/// The map's other methods, its constructors, builder and views are tested in [BreakableMapTest].
///
/// @see BreakableMap
public class BreakableMapDefaultMethodsTest extends AbstractTest {

    /// Default constructor
    public BreakableMapDefaultMethodsTest() {
        super();
    }

    // ========== Functional Method Break Tests ==========

    /// Tests the GET_OR_DEFAULT_ALWAYS_RETURNS_NULL break functionality.
    ///
    /// Validates that when the GET_OR_DEFAULT_ALWAYS_RETURNS_NULL break is active, the
    /// getOrDefault() method always returns null regardless of whether the key exists
    /// or what default value is provided.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the GET_OR_DEFAULT_ALWAYS_RETURNS_NULL break
    /// - Tests getOrDefault() with both existing and non-existing keys
    /// - Verifies that null is always returned
    ///
    /// **Break Behavior:**
    /// - `getOrDefault(key, defaultValue)` returns null for any key
    /// - Default value parameter is ignored
    /// - Actual map data remains accessible through other methods
    ///
    /// **Assertions:**
    /// - `assertNull(map.getOrDefault("key", 999))` for existing key
    /// - `assertNull(map.getOrDefault("nonexistent", 999))` for non-existing key
    @Test
    @DisplayName("Test GET_OR_DEFAULT_ALWAYS_RETURNS_NULL break")
    public void testGetOrDefaultAlwaysReturnsNull() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(GET_OR_DEFAULT_ALWAYS_RETURNS_NULL)
            .build();

        map.put("key", 42);
        assertNull(map.getOrDefault("key", 999)); // Returns null instead of value
        assertNull(map.getOrDefault("nonexistent", 999)); // Returns null instead of default
    }

    /// Tests that getOrDefault() throws UnsupportedOperationException when method is not supported.
    ///
    /// Validates that when the getOrDefault method is marked as unsupported using the Builder's
    /// doesNotSupport configuration, the method throws UnsupportedOperationException regardless
    /// of any breaks that might be configured.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with getOrDefault marked as unsupported
    /// - Configures a getOrDefault-related break
    /// - Attempts to call getOrDefault() and verifies exception is thrown
    ///
    /// **Unsupported Method Behavior:**
    /// - Method throws UnsupportedOperationException immediately
    /// - Breaks are not evaluated when method is unsupported
    /// - Other supported methods continue to work normally
    ///
    /// **Assertions:**
    /// - `assertThrows(UnsupportedOperationException.class, () -> map.getOrDefault("nonexistant", 1))`
    @Test
    @DisplayName("Test getOrDefault(Object ,Object) throws when not supported")
    public void testGetOrDefaultWhenNotSupported() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
                .addBreak(GET_OR_DEFAULT_ALWAYS_RETURNS_NULL)
                .doesNotSupport(MapMethods.GET_OR_DEFAULT)
                .build();

        assertThrows(UnsupportedOperationException.class, () -> map.getOrDefault("nonexistent", 1));
    }

    /// Tests the FOR_EACH_DOES_NOT_CALL_ACTION break functionality.
    ///
    /// Validates that when the FOR_EACH_DOES_NOT_CALL_ACTION break is active, the forEach()
    /// method appears to complete successfully but never calls the provided BiConsumer action.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the FOR_EACH_DOES_NOT_CALL_ACTION break
    /// - Adds multiple key-value pairs to the map
    /// - Calls forEach() with an action that collects visited keys
    /// - Verifies that the action was never called
    ///
    /// **Break Behavior:**
    /// - `forEach(action)` method completes without exception
    /// - BiConsumer action is never invoked
    /// - No key-value pairs are processed
    ///
    /// **Assertions:**
    /// - `assertTrue(visited.isEmpty())` action was never called
    @Test
    @DisplayName("Test FOR_EACH_DOES_NOT_CALL_ACTION break")
    public void testForEachDoesNotCallAction() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(FOR_EACH_DOES_NOT_CALL_ACTION)
            .build();

        map.put("key1", 1);
        map.put("key2", 2);

        List<String> visited = new ArrayList<>();
        map.forEach((k, v) -> visited.add(k));

        assertTrue(visited.isEmpty()); // Action was never called
    }

    /// Tests the FOR_EACH_SKIPS_FIRST_PAIR break functionality.
    ///
    /// Validates that when the FOR_EACH_SKIPS_FIRST_PAIR break is active, the forEach()
    /// method processes all key-value pairs except for the first one in iteration order.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the FOR_EACH_SKIPS_FIRST_PAIR break
    /// - Adds multiple key-value pairs to the map
    /// - Calls forEach() with an action that collects visited keys
    /// - Verifies that all keys except the first are processed
    ///
    /// **Break Behavior:**
    /// - `forEach(action)` skips the first key-value pair in iteration order
    /// - All other pairs are processed normally
    /// - BiConsumer action is called for n-1 pairs
    ///
    /// **Assertions:**
    /// - `assertEquals(2, visited.size())` one less than total pairs
    /// - `assertFalse(visited.contains(firstKey))` first key was skipped
    @Test
    @DisplayName("Test FOR_EACH_SKIPS_FIRST_PAIR break")
    public void testForEachSkipsFirstPair() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(FOR_EACH_SKIPS_FIRST_PAIR)
            .build();

        map.put("key1", 1);
        map.put("key2", 2);
        map.put("key3", 3);

        String firstKey = map.keySet().iterator().next();

        List<String> visited = new ArrayList<>();
        map.forEach((k, v) -> visited.add(k));

        assertEquals(2, visited.size()); // One less than total
        assertFalse(visited.contains(firstKey)); // First key skipped
    }

    /// Tests that forEach() throws UnsupportedOperationException when method is not supported.
    ///
    /// Validates that when the forEach method is marked as unsupported using the Builder's
    /// doesNotSupport configuration, the method throws UnsupportedOperationException regardless
    /// of any breaks that might be configured.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with forEach marked as unsupported
    /// - Configures multiple forEach-related breaks
    /// - Attempts to call forEach() and verifies exception is thrown
    ///
    /// **Unsupported Method Behavior:**
    /// - Method throws UnsupportedOperationException immediately
    /// - Breaks are not evaluated when method is unsupported
    /// - Other supported methods continue to work normally
    ///
    /// **Assertions:**
    /// - `assertThrows(UnsupportedOperationException.class, () -> map.forEach((s, i) -> {} ))`
    @Test
    @DisplayName("Test forEach(BiConsumer) throws when not supported")
    public void testForEachWhenNotSupported() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
                .addBreak(FOR_EACH_SKIPS_FIRST_PAIR)
                .addBreak(FOR_EACH_DOES_NOT_CALL_ACTION)
                .doesNotSupport(MapMethods.FOR_EACH)
                .build();

        assertThrows(UnsupportedOperationException.class, () -> map.forEach((s, i) -> {} ));
    }

    /// Tests the REPLACE_ALL_DOES_NOT_REPLACE_ANY_PAIRS break functionality.
    ///
    /// Validates that when the REPLACE_ALL_DOES_NOT_REPLACE_ANY_PAIRS break is active, the
    /// replaceAll() method appears to complete successfully but does not actually modify
    /// any values in the map.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the REPLACE_ALL_DOES_NOT_REPLACE_ANY_PAIRS break
    /// - Adds key-value pairs and calls replaceAll() with a transformation function
    /// - Verifies that values remain unchanged
    ///
    /// **Break Behavior:**
    /// - `replaceAll(function)` method completes without exception
    /// - BiFunction transformation is ignored
    /// - All values remain unchanged
    ///
    /// **Assertions:**
    /// - `assertEquals(Integer.valueOf(1), map.get("key1"))` original values preserved
    /// - `assertEquals(Integer.valueOf(2), map.get("key2"))` no transformation applied
    @Test
    @DisplayName("Test REPLACE_ALL_DOES_NOT_REPLACE_ANY_PAIRS break")
    public void testReplaceAllDoesNotReplaceAnyPairs() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(REPLACE_ALL_DOES_NOT_REPLACE_ANY_PAIRS)
            .build();

        map.put("key1", 1);
        map.put("key2", 2);

        map.replaceAll((k, v) -> v * 10); // Should multiply by 10

        // Values should be unchanged due to break
        assertEquals(Integer.valueOf(1), map.get("key1"));
        assertEquals(Integer.valueOf(2), map.get("key2"));
    }

    /// Tests that replaceAll() throws UnsupportedOperationException when method is not supported.
    ///
    /// Validates that when the replaceAll method is marked as unsupported using the Builder's
    /// doesNotSupport configuration, the method throws UnsupportedOperationException regardless
    /// of any breaks that might be configured.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with replaceAll marked as unsupported
    /// - Configures a replaceAll-related break
    /// - Attempts to call replaceAll() and verifies exception is thrown
    ///
    /// **Unsupported Method Behavior:**
    /// - Method throws UnsupportedOperationException immediately
    /// - Breaks are not evaluated when method is unsupported
    /// - Other supported methods continue to work normally
    ///
    /// **Assertions:**
    /// - `assertThrows(UnsupportedOperationException.class, () -> map.replaceAll((s, i) -> i ))`
    @Test
    @DisplayName("Test replaceAll(BiFunction) throws when not supported")
    public void testReplaceAllWhenNotSupported() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
                .addBreak(REPLACE_ALL_DOES_NOT_REPLACE_ANY_PAIRS)
                .doesNotSupport(MapMethods.REPLACE_ALL)
                .build();

        assertThrows(UnsupportedOperationException.class, () -> map.replaceAll((s, i) -> i ));
    }

    // ========== Conditional Operation Break Tests ==========

    /// Tests the PUT_IF_ABSENT_DOES_NOT_PUT_VALUE break functionality.
    ///
    /// Validates that when the PUT_IF_ABSENT_DOES_NOT_PUT_VALUE break is active, the
    /// putIfAbsent() method appears to succeed but does not actually add the key-value
    /// pair to the map.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the PUT_IF_ABSENT_DOES_NOT_PUT_VALUE break
    /// - Calls putIfAbsent() for a non-existing key
    /// - Verifies that method returns null but key is not added
    ///
    /// **Break Behavior:**
    /// - `putIfAbsent(key, value)` returns null as if key was absent
    /// - Key-value pair is not actually added to the map
    /// - Map remains unchanged
    ///
    /// **Assertions:**
    /// - `assertNull(map.putIfAbsent("key", 42))` returns null as if absent
    /// - `assertFalse(map.containsKey("key"))` key was not added
    @Test
    @DisplayName("Test PUT_IF_ABSENT_DOES_NOT_PUT_VALUE break")
    public void testPutIfAbsentDoesNotPutValue() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(PUT_IF_ABSENT_DOES_NOT_PUT_VALUE)
            .build();

        assertNull(map.putIfAbsent("key", 42)); // Returns null as if absent
        assertFalse(map.containsKey("key")); // But key was not added
    }

    /// Tests the PUT_IF_ABSENT_ALWAYS_REPLACES_VALUE break functionality.
    ///
    /// Validates that when the PUT_IF_ABSENT_ALWAYS_REPLACES_VALUE break is active, the
    /// putIfAbsent() method replaces existing values even when the key is already present,
    /// contrary to its normal behavior.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the PUT_IF_ABSENT_ALWAYS_REPLACES_VALUE break
    /// - Adds a key-value pair and calls putIfAbsent() with the same key
    /// - Verifies that value is replaced despite key being present
    ///
    /// **Break Behavior:**
    /// - `putIfAbsent(existingKey, newValue)` returns old value
    /// - Value is replaced even though key exists
    /// - Normal "if absent" logic is bypassed
    ///
    /// **Assertions:**
    /// - `assertEquals(Integer.valueOf(42), map.putIfAbsent("key", 84))` returns old value
    /// - `assertEquals(Integer.valueOf(84), map.get("key"))` value was replaced
    @Test
    @DisplayName("Test PUT_IF_ABSENT_ALWAYS_REPLACES_VALUE break")
    public void testPutIfAbsentAlwaysReplacesValue() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(PUT_IF_ABSENT_ALWAYS_REPLACES_VALUE)
            .build();

        map.put("key", 42);
        assertEquals(Integer.valueOf(42), map.putIfAbsent("key", 84)); // Should return 42
        assertEquals(Integer.valueOf(84), map.get("key")); // But value was replaced
    }

    /// Tests the REMOVE_DOES_NOT_REMOVE_VALUE break functionality for two-argument remove.
    ///
    /// Validates that when the REMOVE_DOES_NOT_REMOVE_VALUE break is active, the
    /// remove(key, value) method appears to succeed but does not actually remove the
    /// key-value pair from the map.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the REMOVE_DOES_NOT_REMOVE_VALUE break
    /// - Adds a key-value pair and calls remove(key, value)
    /// - Verifies that method returns true but key remains
    ///
    /// **Break Behavior:**
    /// - `remove(key, value)` returns true as if removal succeeded
    /// - Key-value pair remains in the map
    /// - Normal conditional removal logic is bypassed
    ///
    /// **Assertions:**
    /// - `assertTrue(map.remove("key", 42))` returns true as if removed
    /// - `assertTrue(map.containsKey("key"))` key is still there
    @Test
    @DisplayName("Test REMOVE_DOES_NOT_REMOVE_VALUE break")
    public void testRemoveTwoArgDoesNotRemoveValue() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(REMOVE_DOES_NOT_REMOVE_VALUE)
            .build();

        map.put("key", 42);
        assertTrue(map.remove("key", 42)); // Returns true as if removed
        assertTrue(map.containsKey("key")); // But key is still there
    }

    /// Tests that remove() throws UnsupportedOperationException when method is not supported.
    ///
    /// Validates that when the remove method is marked as unsupported using the Builder's
    /// doesNotSupport configuration, the method throws UnsupportedOperationException regardless
    /// of any breaks that might be configured.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with remove marked as unsupported
    /// - Configures a remove-related break
    /// - Attempts to call remove() and verifies exception is thrown
    ///
    /// **Unsupported Method Behavior:**
    /// - Method throws UnsupportedOperationException immediately
    /// - Breaks are not evaluated when method is unsupported
    /// - Other supported methods continue to work normally
    ///
    /// **Assertions:**
    /// - `assertThrows(UnsupportedOperationException.class, () -> map.remove("nonexistant"))`
    @Test
    @DisplayName("Test remove(Object)throws when not supported")
    public void testRemoveWhenNotSupported() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
                .doesNotSupport(MapMethods.REMOVE)
                .addBreak(REMOVE_DOES_NOT_REMOVE_VALUE)
                .build();

        assertThrows(UnsupportedOperationException.class, () -> map.remove("nonexistent"));
    }

    /// Tests the REPLACE_3_ARG_DOES_NOT_REPLACE_VALUE break functionality.
    ///
    /// Validates that when the REPLACE_3_ARG_DOES_NOT_REPLACE_VALUE break is active, the
    /// replace(key, oldValue, newValue) method appears to succeed but does not actually
    /// replace the value in the map.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the REPLACE_3_ARG_DOES_NOT_REPLACE_VALUE break
    /// - Adds a key-value pair and calls replace() with matching old value
    /// - Verifies that method returns true but value remains unchanged
    ///
    /// **Break Behavior:**
    /// - `replace(key, oldValue, newValue)` returns true as if replacement succeeded
    /// - Value remains unchanged in the map
    /// - Normal conditional replacement logic is bypassed
    ///
    /// **Assertions:**
    /// - `assertTrue(map.replace("key", 42, 84))` returns true as if replaced
    /// - `assertEquals(Integer.valueOf(42), map.get("key"))` value unchanged
    @Test
    @DisplayName("Test REPLACE_3_ARG_DOES_NOT_REPLACE_VALUE break")
    public void testReplace3ArgDoesNotReplaceValue() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(REPLACE_3_ARG_DOES_NOT_REPLACE_VALUE)
            .build();

        map.put("key", 42);
        assertTrue(map.replace("key", 42, 84)); // Returns true as if replaced
        assertEquals(Integer.valueOf(42), map.get("key")); // But value unchanged
    }

    /// Tests the REPLACE_2_ARG_DOES_NOT_REPLACE_VALUE break functionality.
    ///
    /// Validates that when the REPLACE_2_ARG_DOES_NOT_REPLACE_VALUE break is active, the
    /// replace(key, value) method returns the old value as if replacement succeeded but
    /// does not actually replace the value in the map.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the REPLACE_2_ARG_DOES_NOT_REPLACE_VALUE break
    /// - Adds a key-value pair and calls replace() with new value
    /// - Verifies that method returns old value but value remains unchanged
    ///
    /// **Break Behavior:**
    /// - `replace(key, value)` returns old value as if replacement succeeded
    /// - Value remains unchanged in the map
    /// - Normal replacement logic is bypassed
    ///
    /// **Assertions:**
    /// - `assertEquals(Integer.valueOf(42), map.replace("key", 84))` returns old value
    /// - `assertEquals(Integer.valueOf(42), map.get("key"))` value unchanged
    @Test
    @DisplayName("Test REPLACE_2_ARG_DOES_NOT_REPLACE_VALUE break")
    public void testReplace2ArgDoesNotReplaceValue() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(REPLACE_2_ARG_DOES_NOT_REPLACE_VALUE)
            .build();

        map.put("key", 42);
        assertEquals(Integer.valueOf(42), map.replace("key", 84)); // Returns old value
        assertEquals(Integer.valueOf(42), map.get("key")); // But value unchanged
    }

    // ========== Compute Method Break Tests ==========

    /// Tests the COMPUTE_IF_ABSENT_NEVER_COMPUTES_VALUE break functionality.
    ///
    /// Validates that when the COMPUTE_IF_ABSENT_NEVER_COMPUTES_VALUE break is active, the
    /// computeIfAbsent() method returns null without calling the mapping function and
    /// without adding any key-value pair to the map.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the COMPUTE_IF_ABSENT_NEVER_COMPUTES_VALUE break
    /// - Calls computeIfAbsent() for a non-existing key with a mapping function
    /// - Verifies that function is not called and key is not added
    ///
    /// **Break Behavior:**
    /// - `computeIfAbsent(key, function)` returns null immediately
    /// - Mapping function is never invoked
    /// - No key-value pair is added to the map
    ///
    /// **Assertions:**
    /// - `assertNull(map.computeIfAbsent("key", k -> 42))` returns null
    /// - `assertFalse(functionCalled[0])` function should not be called
    /// - `assertFalse(map.containsKey("key"))` key should not be added
    @Test
    @DisplayName("Test COMPUTE_IF_ABSENT_NEVER_COMPUTES_VALUE break")
    public void testComputeIfAbsentNeverComputesValue() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(COMPUTE_IF_ABSENT_NEVER_COMPUTES_VALUE)
            .build();

        boolean[] functionCalled = {false};
        assertNull(map.computeIfAbsent("key", k -> {
            functionCalled[0] = true;
            return 42;
        }));

        assertFalse(functionCalled[0]); // Function should not be called
        assertFalse(map.containsKey("key")); // Key should not be added
    }

    /// Tests the COMPUTE_IF_ABSENT_ALWAYS_COMPUTES_VALUE break functionality.
    ///
    /// Validates that when the COMPUTE_IF_ABSENT_ALWAYS_COMPUTES_VALUE break is active, the
    /// computeIfAbsent() method calls the mapping function and replaces the value even when
    /// the key is already present in the map.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the COMPUTE_IF_ABSENT_ALWAYS_COMPUTES_VALUE break
    /// - Adds a key-value pair and calls computeIfAbsent() for the existing key
    /// - Verifies that function is called and value is replaced
    ///
    /// **Break Behavior:**
    /// - `computeIfAbsent(existingKey, function)` calls function even for existing keys
    /// - Existing value is replaced with computed value
    /// - Normal "if absent" logic is bypassed
    ///
    /// **Assertions:**
    /// - `assertTrue(functionCalled[0])` function should be called even though key exists
    /// - `assertEquals(Integer.valueOf(84), result)` returns computed value
    /// - `assertEquals(Integer.valueOf(84), map.get("key"))` value should be replaced
    @Test
    @DisplayName("Test COMPUTE_IF_ABSENT_ALWAYS_COMPUTES_VALUE break")
    public void testComputeIfAbsentAlwaysComputesValue() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(COMPUTE_IF_ABSENT_ALWAYS_COMPUTES_VALUE)
            .build();

        map.put("key", 42);

        boolean[] functionCalled = {false};
        Integer result = map.computeIfAbsent("key", k -> {
            functionCalled[0] = true;
            return 84;
        });

        assertTrue(functionCalled[0]); // Function should be called even though key exists
        assertEquals(Integer.valueOf(84), result);
        assertEquals(Integer.valueOf(84), map.get("key")); // Value should be replaced
    }

    /// Tests the COMPUTE_IF_PRESENT_NEVER_COMPUTES_VALUE break functionality.
    ///
    /// Validates that when the COMPUTE_IF_PRESENT_NEVER_COMPUTES_VALUE break is active, the
    /// computeIfPresent() method returns null without calling the remapping function even
    /// when the key is present in the map.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the COMPUTE_IF_PRESENT_NEVER_COMPUTES_VALUE break
    /// - Adds a key-value pair and calls computeIfPresent() for the existing key
    /// - Verifies that function is not called and value remains unchanged
    ///
    /// **Break Behavior:**
    /// - `computeIfPresent(key, function)` returns null immediately
    /// - Remapping function is never invoked
    /// - Existing value remains unchanged
    ///
    /// **Assertions:**
    /// - `assertNull(map.computeIfPresent("key", (k, v) -> v * 2))` returns null
    /// - `assertFalse(functionCalled[0])` function should not be called
    /// - `assertEquals(Integer.valueOf(42), map.get("key"))` value should be unchanged
    @Test
    @DisplayName("Test COMPUTE_IF_PRESENT_NEVER_COMPUTES_VALUE break")
    public void testComputeIfPresentNeverComputesValue() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(COMPUTE_IF_PRESENT_NEVER_COMPUTES_VALUE)
            .build();

        map.put("key", 42);

        boolean[] functionCalled = {false};
        assertNull(map.computeIfPresent("key", (k, v) -> {
            functionCalled[0] = true;
            return v * 2;
        }));

        assertFalse(functionCalled[0]); // Function should not be called
        assertEquals(Integer.valueOf(42), map.get("key")); // Value should be unchanged
    }

    /// Tests the COMPUTE_DOES_NOT_COMPUTE_VALUE break functionality.
    ///
    /// Validates that when the COMPUTE_DOES_NOT_COMPUTE_VALUE break is active, the
    /// compute() method returns the current value without calling the remapping function,
    /// regardless of whether the key exists or not.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the COMPUTE_DOES_NOT_COMPUTE_VALUE break
    /// - Adds a key-value pair and calls compute() for the existing key
    /// - Verifies that function is not called and current value is returned
    ///
    /// **Break Behavior:**
    /// - `compute(key, function)` returns current value immediately
    /// - Remapping function is never invoked
    /// - Existing value remains unchanged
    ///
    /// **Assertions:**
    /// - `assertFalse(functionCalled[0])` function should not be called
    /// - `assertEquals(Integer.valueOf(42), result)` returns current value
    /// - `assertEquals(Integer.valueOf(42), map.get("key"))` value unchanged
    @Test
    @DisplayName("Test COMPUTE_DOES_NOT_COMPUTE_VALUE break")
    public void testComputeDoesNotComputeValue() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(COMPUTE_DOES_NOT_COMPUTE_VALUE)
            .build();

        map.put("key", 42);

        boolean[] functionCalled = {false};
        Integer result = map.compute("key", (k, v) -> {
            functionCalled[0] = true;
            return v != null ? v * 2 : 1;
        });

        assertFalse(functionCalled[0]); // Function should not be called
        assertEquals(Integer.valueOf(42), result); // Returns current value
        assertEquals(Integer.valueOf(42), map.get("key")); // Value unchanged
    }

    /// Tests the MERGE_DOES_NOT_MERGE_THE_VALUES break functionality.
    ///
    /// Validates that when the MERGE_DOES_NOT_MERGE_THE_VALUES break is active, the
    /// merge() method returns the current value without calling the remapping function
    /// to merge the old and new values.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the MERGE_DOES_NOT_MERGE_THE_VALUES break
    /// - Adds a key-value pair and calls merge() with a new value
    /// - Verifies that function is not called and current value is returned
    ///
    /// **Break Behavior:**
    /// - `merge(key, value, function)` returns current value immediately
    /// - Remapping function is never invoked
    /// - Existing value remains unchanged
    ///
    /// **Assertions:**
    /// - `assertFalse(functionCalled[0])` function should not be called
    /// - `assertEquals(Integer.valueOf(42), result)` returns current value
    /// - `assertEquals(Integer.valueOf(42), map.get("key"))` value unchanged
    @Test
    @DisplayName("Test MERGE_DOES_NOT_MERGE_THE_VALUES break")
    public void testMergeDoesNotMergeValues() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(MERGE_DOES_NOT_MERGE_THE_VALUES)
            .build();

        map.put("key", 42);

        boolean[] functionCalled = {false};
        Integer result = map.merge("key", 10, (oldVal, newVal) -> {
            functionCalled[0] = true;
            return oldVal + newVal;
        });

        assertFalse(functionCalled[0]); // Function should not be called
        assertEquals(Integer.valueOf(42), result); // Returns current value
        assertEquals(Integer.valueOf(42), map.get("key")); // Value unchanged
    }
}
