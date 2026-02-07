package org.soliscode.test.breakable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.map.MapMethods;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.breakable.BreakableMap.*;

/// **Test Suite for BreakableMap Implementation**
///
/// This comprehensive test class validates the behavior of BreakableMap, focusing on both
/// standard Map contract compliance and the controlled violation of map semantics through
/// programmatic breaks. The tests ensure that BreakableMap maintains proper map behavior
/// under normal conditions while correctly implementing break mechanisms for testing purposes.
///
/// ## Test Coverage
///
/// ### Contract Compliance Testing
/// - **Map Interface**: Full validation of Map interface methods
/// - **Key-Value Semantics**: Proper association and retrieval
/// - **Standard Operations**: Put, get, remove, containsKey operations
/// - **View Collections**: KeySet, values, entrySet behavior
/// - **Functional Operations**: forEach, replaceAll, compute methods
///
/// ### Break Mechanism Testing
/// - **Size and State Breaks**: SIZE_ALWAYS_RETURNS_ZERO, IS_EMPTY_ALWAYS_RETURNS_TRUE
/// - **Containment Breaks**: CONTAINS_KEY_ALWAYS_RETURNS_FALSE, CONTAINS_VALUE_RETURNS_OPPOSITE_VALUE
/// - **Access Breaks**: GET_ALWAYS_RETURNS_NULL, GET_FAILS_FOR_FIRST_KEY
/// - **Modification Breaks**: PUT_DOES_NOT_ADD_PAIR, REMOVE_DOES_NOT_REMOVE_KEY
/// - **Functional Breaks**: FOR_EACH_SKIPS_FIRST_PAIR, COMPUTE_IF_ABSENT_NEVER_COMPUTES_VALUE
/// - **View Breaks**: KEY_SET_RETURNS_EMPTY_SET, VALUES_RETURNS_NULL_WHEN_EMPTY
///
/// ### Builder Pattern Testing
/// - **Configuration**: Builder setup with various parameters
/// - **Copy Semantics**: Builder copying and independence
/// - **Fluent Interface**: Method chaining and configuration transfer
/// - **Null Handling**: Null key and value configuration
///
/// ### Constructor Testing
/// - **Default Construction**: Empty map creation
/// - **Copy Construction**: Creating maps from existing instances
/// - **Map Construction**: Building maps from other map instances
/// - **Null Policy**: Testing null key and value handling
///
/// ## Test Architecture
///
/// This test class follows the SolisCode testing framework patterns:
/// - **AbstractTest Extension**: Inherits common testing infrastructure
/// - **Map-Specific Testing**: Focused on Map interface compliance
/// - **Break Isolation**: Each break is tested independently
/// - **State Validation**: Verifies map state consistency
///
/// ### Framework Integration
/// ```java
/// // The test class integrates framework components:
/// public class BreakableMapTest extends AbstractTest {  // Base testing infrastructure
/// ```
///
/// ## Test Methodology
///
/// ### Standard Behavior Validation
/// Tests verify that BreakableMap behaves like a proper Map implementation:
/// - Key-value associations
/// - Proper return values from operations
/// - Correct size calculations
/// - Standard iteration behavior
/// - View collection consistency
///
/// ### Break Behavior Validation
/// Tests verify that breaks work as intended without compromising overall functionality:
/// - Break activation changes specific behaviors
/// - Non-broken operations continue to work normally
/// - State consistency is maintained
/// - Break effects are isolated and predictable
///
/// ### Builder Testing Strategy
/// Tests validate the builder pattern implementation:
/// - Configuration transfer from builder to map
/// - Builder reusability and independence
/// - Copy constructor behavior and isolation
/// - Fluent interface method chaining
/// - Null handling configuration
///
/// ## Example Test Scenarios
///
/// ### Normal Map Behavior
/// ```java
/// @Test
/// void testStandardMapBehavior() {
///     BreakableMap<String, Integer> map = new BreakableMap<>();
///     assertNull(map.put("key", 42));      // First put returns null
///     assertEquals(42, map.put("key", 84)); // Update returns previous value
///     assertEquals(1, map.size());          // Size reflects content
/// }
/// ```
///
/// ### Break Behavior Testing
/// ```java
/// @Test
/// void testSizeBreak() {
///     BreakableMap<String, Integer> brokenMap = new BreakableMap.Builder<String, Integer>()
///         .addBreak(SIZE_ALWAYS_RETURNS_ZERO)
///         .build();
///     brokenMap.put("key", 42);
///     assertEquals(0, brokenMap.size()); // Size always zero due to break
/// }
/// ```
///
/// @author evanbergstrom
/// @since 1.0.0
/// @see BreakableMap
/// @see AbstractTest
public class BreakableMapTest extends AbstractTest {

    // ========== Constructor Tests ==========

    /// Tests the default constructor functionality and initial state validation.
    ///
    /// Validates that the default BreakableMap constructor creates an empty map with the correct
    /// initial configuration:
    /// - Map should be empty (size 0, isEmpty returns true)
    /// - Should permit null keys by default (permitsNullKeys returns true)
    /// - Should permit null values by default (permitsNullValues returns true)
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap using the default constructor
    /// - Verifies empty state and default null handling policies
    ///
    /// **Assertions:**
    /// - `assertTrue(map.isEmpty())` - Map should be empty
    /// - `assertEquals(0, map.size())` - Size should be zero
    /// - `assertTrue(map.permitsNullKeys())` - Should permit null keys
    /// - `assertTrue(map.permitsNullValues())` - Should permit null values
    @Test
    @DisplayName("Test default constructor creates empty map")
    public void testDefaultConstructor() {
        BreakableMap<String, Integer> map = new BreakableMap<>();

        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertTrue(map.permitsNullKeys());
        assertTrue(map.permitsNullValues());
    }

    /// Tests the copy constructor behavior and configuration inheritance.
    ///
    /// Validates that the copy constructor creates a new BreakableMap instance that properly
    /// inherits all configuration from the source map including breaks and null policies, while
    /// maintaining data independence.
    ///
    /// **Test Scenario:**
    /// - Creates an original map with specific breaks and null key restrictions
    /// - Uses copy constructor to create a new instance
    /// - Verifies configuration inheritance and data copying
    ///
    /// **Assertions:**
    /// - `assertNotSame(original, copy)` - Different object instances
    /// - `assertEquals(original.get("key1"), copy.get("key1"))` - Data copied correctly
    /// - `assertFalse(copy.permitsNullKeys())` - Null policy inherited
    /// - `assertEquals(0, copy.size())` - Breaks inherited and active
    @Test
    @DisplayName("Test copy constructor")
    public void testCopyConstructor() {
        BreakableMap<String, Integer> original = new BreakableMap.Builder<String, Integer>()
            .addBreak(SIZE_ALWAYS_RETURNS_ZERO)
            .doesNotPermitNullKeys()
            .build();

        original.put("key1", 1);
        original.put("key2", 2);

        BreakableMap<String, Integer> copy = new BreakableMap<>(original);

        // Verify independent copies
        assertNotSame(original, copy);
        assertEquals(original.get("key1"), copy.get("key1"));
        assertEquals(original.get("key2"), copy.get("key2"));

        // Verify configuration is copied
        assertFalse(copy.permitsNullKeys());
        assertTrue(copy.permitsNullValues());

        // Verify breaks are copied
        assertEquals(0, copy.size()); // Break should be active
    }

    // ========== Builder Tests ==========

    /// Tests basic Builder pattern functionality and configuration transfer.
    ///
    /// Validates that the Builder correctly constructs BreakableMap instances with specified
    /// breaks and null policies, ensuring all configuration is properly transferred from
    /// builder to final instance.
    ///
    /// **Test Scenario:**
    /// - Uses Builder to configure breaks and null policies
    /// - Builds the final BreakableMap instance
    /// - Verifies break activation and policy enforcement
    ///
    /// **Assertions:**
    /// - `assertEquals(0, map.size())` - SIZE_ALWAYS_RETURNS_ZERO break active
    /// - `assertNull(map.get("key"))` - GET_ALWAYS_RETURNS_NULL break active
    /// - `assertFalse(map.permitsNullKeys())` - Null key policy transferred
    /// - `assertTrue(map.permitsNullValues())` - Default null value policy maintained
    @Test
    @DisplayName("Test builder pattern")
    public void testBuilder() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(SIZE_ALWAYS_RETURNS_ZERO)
            .addBreak(GET_ALWAYS_RETURNS_NULL)
            .doesNotPermitNullKeys()
            .build();

        map.put("key", 42);

        // Verify breaks are active
        assertEquals(0, map.size());
        assertNull(map.get("key"));

        // Verify null policy
        assertFalse(map.permitsNullKeys());
        assertTrue(map.permitsNullValues());
    }

    /// Tests Builder creation with pre-existing map data.
    ///
    /// Validates that the Builder can be initialized with existing map data and that breaks
    /// are properly applied to the pre-existing data without affecting data integrity.
    ///
    /// **Test Scenario:**
    /// - Creates a HashMap with initial data
    /// - Uses Builder constructor with existing map
    /// - Adds breaks and verifies their effect on existing data
    ///
    /// **Assertions:**
    /// - `assertEquals(Integer.valueOf(1), map.get("key1"))` - Original data preserved
    /// - `assertFalse(map.containsKey("key1"))` - Break affects behavior on existing data
    @Test
    @DisplayName("Test builder with existing map")
    public void testBuilderWithExistingMap() {
        Map<String, Integer> existingMap = new HashMap<>();
        existingMap.put("key1", 1);
        existingMap.put("key2", 2);

        BreakableMap<String, Integer> map = new BreakableMap.Builder<>(existingMap)
            .addBreak(CONTAINS_KEY_ALWAYS_RETURNS_FALSE)
            .build();

        // Original data should be present
        assertEquals(Integer.valueOf(1), map.get("key1"));
        assertEquals(Integer.valueOf(2), map.get("key2"));

        // Break should be active
        assertFalse(map.containsKey("key1")); // Due to break
    }

    /// Tests Builder copy functionality and configuration inheritance.
    ///
    /// Validates that Builder.copy() creates an independent builder instance that inherits
    /// all configuration from the source builder but can be modified without affecting the original.
    ///
    /// **Test Scenario:**
    /// - Creates a base builder with specific configuration
    /// - Creates a copy and adds additional breaks
    /// - Builds maps from both builders and verifies independence
    ///
    /// **Assertions:**
    /// - `assertEquals(0, originalMap.size())` - Original has off-by-one break
    /// - `assertEquals(Integer.valueOf(42), originalMap.get("key"))` - Original has normal get
    /// - `assertNull(copyMap.get("key"))` - Copy has additional GET_ALWAYS_RETURNS_NULL break
    @Test
    @DisplayName("Test builder copy")
    public void testBuilderCopy() {
        BreakableMap.Builder<String, Integer> original = new BreakableMap.Builder<String, Integer>()
            .addBreak(SIZE_IS_OFF_BY_ONE)
            .doesNotPermitNullValues();

        BreakableMap.Builder<String, Integer> copy = original.copy();

        // Modify copy
        copy.addBreak(GET_ALWAYS_RETURNS_NULL);

        BreakableMap<String, Integer> originalMap = original.build();
        BreakableMap<String, Integer> copyMap = copy.build();

        originalMap.put("key", 42);
        copyMap.put("key", 84);

        // Original should have off-by-one size but normal get
        assertEquals(0, originalMap.size()); // 1 - 1 = 0
        assertEquals(Integer.valueOf(42), originalMap.get("key"));

        // Copy should have both breaks
        assertEquals(0, copyMap.size()); // 1 - 1 = 0
        assertNull(copyMap.get("key")); // Due to GET_ALWAYS_RETURNS_NULL
    }

    // ========== Size and State Break Tests ==========

    /// Tests the SIZE_ALWAYS_RETURNS_ZERO break functionality.
    ///
    /// Validates that when the SIZE_ALWAYS_RETURNS_ZERO break is active, the size() method
    /// consistently returns 0 regardless of the actual number of elements in the map.
    /// This break is useful for testing code that depends on size calculations.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the SIZE_ALWAYS_RETURNS_ZERO break
    /// - Adds multiple elements to the map
    /// - Verifies that size() always returns 0 despite containing elements
    ///
    /// **Break Behavior:**
    /// - `size()` method returns 0 regardless of actual element count
    /// - Other methods like `containsKey()` and `isEmpty()` continue to work normally
    /// - Map state remains consistent except for size reporting
    ///
    /// **Assertions:**
    /// - `assertEquals(0, map.size())` after adding multiple elements
    /// - `assertFalse(map.isEmpty())` to verify isEmpty works independently
    @Test
    @DisplayName("Test SIZE_ALWAYS_RETURNS_ZERO break")
    public void testSizeAlwaysReturnsZero() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(SIZE_ALWAYS_RETURNS_ZERO)
            .build();

        map.put("key1", 1);
        map.put("key2", 2);
        map.put("key3", 3);

        assertEquals(0, map.size());
        assertFalse(map.isEmpty()); // isEmpty should work normally
    }

    /// Tests the SIZE_IS_OFF_BY_ONE break functionality.
    ///
    /// Validates that when the SIZE_IS_OFF_BY_ONE break is active, the size() method
    /// returns the actual size minus one. This creates scenarios where size calculations
    /// are consistently incorrect by a fixed offset.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the SIZE_IS_OFF_BY_ONE break
    /// - Tests size reporting with empty map and after adding elements
    /// - Verifies the consistent off-by-one behavior
    ///
    /// **Break Behavior:**
    /// - `size()` method returns actual size - 1
    /// - Empty map (size 0) reports -1
    /// - Map with 2 elements reports size 1
    /// - Other map operations remain unaffected
    ///
    /// **Assertions:**
    /// - `assertEquals(-1, map.size())` for empty map
    /// - `assertEquals(0, map.size())` after adding one element
    /// - `assertEquals(1, map.size())` after adding two elements
    @Test
    @DisplayName("Test SIZE_IS_OFF_BY_ONE break")
    public void testSizeIsOffByOne() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(SIZE_IS_OFF_BY_ONE)
            .build();

        assertEquals(-1, map.size()); // 0 - 1 = -1

        map.put("key1", 1);
        assertEquals(0, map.size()); // 1 - 1 = 0

        map.put("key2", 2);
        assertEquals(1, map.size()); // 2 - 1 = 1
    }

    /// Tests the IS_EMPTY_ALWAYS_RETURNS_TRUE break functionality.
    ///
    /// Validates that when the IS_EMPTY_ALWAYS_RETURNS_TRUE break is active, the isEmpty()
    /// method consistently returns true regardless of whether the map actually contains elements.
    /// This break simulates scenarios where empty state detection is unreliable.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the IS_EMPTY_ALWAYS_RETURNS_TRUE break
    /// - Adds elements to the map to make it non-empty
    /// - Verifies that isEmpty() still returns true
    ///
    /// **Break Behavior:**
    /// - `isEmpty()` method returns true regardless of actual content
    /// - Other state-checking methods like `size()` continue to work normally
    /// - Actual map content remains accessible through other methods
    ///
    /// **Assertions:**
    /// - `assertTrue(map.isEmpty())` for both empty and non-empty states
    /// - `assertEquals(1, map.size())` to verify size works independently
    @Test
    @DisplayName("Test IS_EMPTY_ALWAYS_RETURNS_TRUE break")
    public void testIsEmptyAlwaysReturnsTrue() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(IS_EMPTY_ALWAYS_RETURNS_TRUE)
            .build();

        assertTrue(map.isEmpty());

        map.put("key", 1);
        assertTrue(map.isEmpty()); // Still true due to break
        assertEquals(1, map.size()); // Size should work normally
    }

    /// Tests the IS_EMPTY_ALWAYS_RETURNS_FALSE break functionality.
    ///
    /// Validates that when the IS_EMPTY_ALWAYS_RETURNS_FALSE break is active, the isEmpty()
    /// method consistently returns false even when the map is actually empty. This break
    /// simulates scenarios where empty state detection incorrectly reports non-empty status.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the IS_EMPTY_ALWAYS_RETURNS_FALSE break
    /// - Ensures the map is empty (no elements added)
    /// - Verifies that isEmpty() returns false despite being empty
    ///
    /// **Break Behavior:**
    /// - `isEmpty()` method returns false regardless of actual empty state
    /// - `size()` method continues to accurately report 0 for empty map
    /// - Other state-checking methods remain unaffected
    ///
    /// **Assertions:**
    /// - `assertFalse(map.isEmpty())` for an actually empty map
    /// - `assertEquals(0, map.size())` to verify actual empty state
    @Test
    @DisplayName("Test IS_EMPTY_ALWAYS_RETURNS_FALSE break")
    public void testIsEmptyAlwaysReturnsFalse() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(IS_EMPTY_ALWAYS_RETURNS_FALSE)
            .build();

        assertFalse(map.isEmpty()); // False even when empty
        assertEquals(0, map.size()); // Size should work normally

        map.put("key", 1);
        assertFalse(map.isEmpty());
    }

    /// Tests the IS_EMPTY_RETURNS_OPPOSITE_VALUE break functionality.
    ///
    /// Validates that when the IS_EMPTY_RETURNS_OPPOSITE_VALUE break is active, the isEmpty()
    /// method returns the opposite of what it should return based on the actual map state.
    /// This break simulates scenarios with inverted empty state logic.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the IS_EMPTY_RETURNS_OPPOSITE_VALUE break
    /// - Tests isEmpty() on both empty and non-empty states
    /// - Verifies that results are consistently inverted
    ///
    /// **Break Behavior:**
    /// - `isEmpty()` returns false when map is actually empty
    /// - `isEmpty()` returns true when map actually contains elements
    /// - Other state-checking methods remain unaffected
    ///
    /// **Assertions:**
    /// - `assertFalse(map.isEmpty())` when map is empty (should be true, returns false)
    /// - `assertTrue(map.isEmpty())` when map has elements (should be false, returns true)
    @Test
    @DisplayName("Test IS_EMPTY_RETURNS_OPPOSITE_VALUE break")
    public void testIsEmptyReturnsOppositeValue() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(IS_EMPTY_RETURNS_OPPOSITE_VALUE)
            .build();

        assertFalse(map.isEmpty()); // Should be true, returns false

        map.put("key", 1);
        assertTrue(map.isEmpty()); // Should be false, returns true
    }

    /// Tests that isEmpty() throws UnsupportedOperationException when method is not supported.
    ///
    /// Validates that when the isEmpty method is marked as unsupported using the Builder's
    /// doesNotSupport configuration, the method throws UnsupportedOperationException regardless
    /// of any breaks that might be configured.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with isEmpty marked as unsupported
    /// - Configures multiple isEmpty-related breaks
    /// - Attempts to call isEmpty() and verifies exception is thrown
    ///
    /// **Unsupported Method Behavior:**
    /// - Method throws UnsupportedOperationException immediately
    /// - Breaks are not evaluated when method is unsupported
    /// - Other supported methods continue to work normally
    ///
    /// **Assertions:**
    /// - `assertThrows(UnsupportedOperationException.class, map::isEmpty)`
    @Test
    @DisplayName("Test isEmpty() throws when not supported")
    public void testIsEmptyWhenNotSupported() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
                .doesNotSupport(MapMethods.IS_EMPTY)
                .addBreak(IS_EMPTY_ALWAYS_RETURNS_FALSE)
                .addBreak(IS_EMPTY_RETURNS_OPPOSITE_VALUE)
                .build();

        assertThrows(UnsupportedOperationException.class, map::isEmpty);
    }

    // ========== Containment Break Tests ==========

    /// Tests the CONTAINS_KEY_ALWAYS_RETURNS_TRUE break functionality.
    ///
    /// Validates that when the CONTAINS_KEY_ALWAYS_RETURNS_TRUE break is active, the
    /// containsKey() method returns true for any key, regardless of whether that key
    /// is actually present in the map. This break simulates unreliable key detection.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the CONTAINS_KEY_ALWAYS_RETURNS_TRUE break
    /// - Tests containsKey() with both existing and non-existing keys
    /// - Verifies that all containsKey() calls return true
    ///
    /// **Break Behavior:**
    /// - `containsKey(key)` returns true for any key parameter
    /// - Actual key presence remains unchanged in the underlying map
    /// - Other containment methods like `containsValue()` work normally
    ///
    /// **Assertions:**
    /// - `assertTrue(map.containsKey("nonexistent"))` for keys that don't exist
    /// - `assertTrue(map.containsKey("key"))` for keys that do exist
    /// - `assertTrue(map.containsKey("still_nonexistent"))` for additional non-existing keys
    @Test
    @DisplayName("Test CONTAINS_KEY_ALWAYS_RETURNS_TRUE break")
    public void testContainsKeyAlwaysReturnsTrue() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
                .addBreak(CONTAINS_KEY_ALWAYS_RETURNS_TRUE)
                .build();

        assertTrue(map.containsKey("nonexistent"));

        map.put("key", 1);
        assertTrue(map.containsKey("key"));
        assertTrue(map.containsKey("still_nonexistent"));
    }

    /// Tests the CONTAINS_KEY_ALWAYS_RETURNS_FALSE break functionality.
    ///
    /// Validates that when the CONTAINS_KEY_ALWAYS_RETURNS_FALSE break is active, the
    /// containsKey() method returns false for any key, even when that key is actually
    /// present in the map. This break simulates scenarios where key detection fails.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the CONTAINS_KEY_ALWAYS_RETURNS_FALSE break
    /// - Adds a key-value pair to the map
    /// - Verifies that containsKey() returns false even for existing keys
    ///
    /// **Break Behavior:**
    /// - `containsKey(key)` returns false for any key parameter
    /// - Actual key-value pairs remain accessible through other methods like `get()`
    /// - Map structure and data integrity remain intact
    ///
    /// **Assertions:**
    /// - `assertFalse(map.containsKey("key"))` even though key exists
    /// - `assertEquals(Integer.valueOf(1), map.get("key"))` to verify key is actually present
    @Test
    @DisplayName("Test CONTAINS_KEY_ALWAYS_RETURNS_FALSE break")
    public void testContainsKeyAlwaysReturnsFalse() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
                .addBreak(CONTAINS_KEY_ALWAYS_RETURNS_FALSE)
                .build();

        map.put("key", 1);
        assertFalse(map.containsKey("key")); // False even though key exists
        assertEquals(Integer.valueOf(1), map.get("key")); // Get should work normally
    }

    /// Tests the CONTAINS_KEY_RETURNS_OPPOSITE_VALUE break functionality.
    ///
    /// Validates that when the CONTAINS_KEY_RETURNS_OPPOSITE_VALUE break is active, the
    /// containsKey() method returns the opposite of what it should return based on actual
    /// key presence in the map. This break simulates inverted key detection logic.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the CONTAINS_KEY_RETURNS_OPPOSITE_VALUE break
    /// - Tests containsKey() with both existing and non-existing keys
    /// - Verifies that results are consistently inverted
    ///
    /// **Break Behavior:**
    /// - `containsKey(key)` returns false when key actually exists
    /// - `containsKey(key)` returns true when key doesn't exist
    /// - Actual map data remains accessible through other methods
    ///
    /// **Assertions:**
    /// - `assertTrue(map.containsKey("nonexistent"))` for non-existing key (should be false, returns true)
    /// - `assertFalse(map.containsKey("key"))` for existing key (should be true, returns false)
    @Test
    @DisplayName("Test CONTAINS_KEY_RETURNS_OPPOSITE_VALUE break")
    public void testContainsKeyReturnsOppositeValue() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
                .addBreak(CONTAINS_KEY_RETURNS_OPPOSITE_VALUE)
                .build();

        assertTrue(map.containsKey("nonexistent")); // Should be false, returns true

        map.put("key", 1);
        assertFalse(map.containsKey("key")); // Should be true, returns false
    }

    /// Tests that containsKey() throws UnsupportedOperationException when method is not supported.
    ///
    /// Validates that when the containsKey method is marked as unsupported using the Builder's
    /// doesNotSupport configuration, the method throws UnsupportedOperationException regardless
    /// of any breaks that might be configured.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with containsKey marked as unsupported
    /// - Attempts to call containsKey() and verifies exception is thrown
    ///
    /// **Unsupported Method Behavior:**
    /// - Method throws UnsupportedOperationException immediately
    /// - Breaks are not evaluated when method is unsupported
    /// - Other supported methods continue to work normally
    ///
    /// **Assertions:**
    /// - `assertThrows(UnsupportedOperationException.class, () -> map.containsKey("nonexistent"))`
    @Test
    @DisplayName("Test containsKey(Object) throws when not supported")
    public void testContainsKeyWhenNotSupported() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
                .doesNotSupport(MapMethods.CONTAINS_KEY)
                .build();

        assertThrows(UnsupportedOperationException.class, () -> map.containsKey("nonexistent"));
    }

    /// Tests the CONTAINS_VALUE_ALWAYS_RETURNS_TRUE break functionality.
    ///
    /// Validates that when the CONTAINS_VALUE_ALWAYS_RETURNS_TRUE break is active, the
    /// containsValue() method returns true for any value, regardless of whether that value
    /// is actually present in the map. This break simulates unreliable value detection.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with CONTAINS_VALUE_ALWAYS_RETURNS_TRUE break
    /// - Also adds CONTAINS_KEY_RETURNS_OPPOSITE_VALUE break for additional test complexity
    /// - Tests containsValue() with both existing and non-existing values
    ///
    /// **Break Behavior:**
    /// - `containsValue(value)` returns true for any value parameter
    /// - Actual values remain unchanged in the underlying map
    /// - Other breaks continue to function independently
    ///
    /// **Assertions:**
    /// - `assertTrue(map.containsValue(999))` for non-existing value
    /// - `assertTrue(map.containsValue(1))` for existing value
    @Test
    @DisplayName("Test CONTAINS_VALUE_ALWAYS_RETURNS_TRUE break")
    public void testContainsValueAlwaysReturnsTrue() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
                .addBreak(CONTAINS_VALUE_ALWAYS_RETURNS_TRUE)
                .addBreak(CONTAINS_KEY_RETURNS_OPPOSITE_VALUE)
                .build();

        assertTrue(map.containsValue(999));

        map.put("key", 1);
        assertTrue(map.containsValue(1));
        assertTrue(map.containsValue(999)); // Still true for nonexistent
    }

    /// Tests the CONTAINS_VALUE_ALWAYS_RETURNS_FALSE break functionality.
    ///
    /// Validates that when the CONTAINS_VALUE_ALWAYS_RETURNS_FALSE break is active, the
    /// containsValue() method returns false for any value, even when that value is actually
    /// present in the map. This break simulates scenarios where value detection fails.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the CONTAINS_VALUE_ALWAYS_RETURNS_FALSE break
    /// - Adds a key-value pair to the map
    /// - Verifies that containsValue() returns false even for existing values
    ///
    /// **Break Behavior:**
    /// - `containsValue(value)` returns false for any value parameter
    /// - Actual values remain accessible through other methods like `get()`
    /// - Map structure and data integrity remain intact
    ///
    /// **Assertions:**
    /// - `assertFalse(map.containsValue(1))` even though value exists
    /// - `assertEquals(Integer.valueOf(1), map.get("key"))` to verify value is actually present
    @Test
    @DisplayName("Test CONTAINS_VALUE_ALWAYS_RETURNS_FALSE break")
    public void testContainsValueAlwaysReturnsFalse() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
                .addBreak(CONTAINS_VALUE_ALWAYS_RETURNS_FALSE)
                .build();

        map.put("key", 1);
        assertFalse(map.containsValue(1)); // False even though value exists
        assertEquals(Integer.valueOf(1), map.get("key")); // Get should work normally
    }

    /// Tests the CONTAINS_VALUE_RETURNS_OPPOSITE_VALUE break functionality.
    ///
    /// Validates that when the CONTAINS_VALUE_RETURNS_OPPOSITE_VALUE break is active, the
    /// containsValue() method returns the opposite of what it should return based on actual
    /// value presence in the map. This break simulates inverted value detection logic.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the CONTAINS_VALUE_RETURNS_OPPOSITE_VALUE break
    /// - Tests containsValue() with both existing and non-existing values
    /// - Verifies that results are consistently inverted
    ///
    /// **Break Behavior:**
    /// - `containsValue(value)` returns false when value actually exists
    /// - `containsValue(value)` returns true when value doesn't exist
    /// - Actual map data remains accessible through other methods
    ///
    /// **Assertions:**
    /// - `assertTrue(map.containsValue(999))` for non-existing value (should be false, returns true)
    /// - `assertFalse(map.containsValue(1))` for existing value (should be true, returns false)
    @Test
    @DisplayName("Test CONTAINS_VALUE_RETURNS_OPPOSITE_VALUE break")
    public void testContainsValueReturnsOppositeValue() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(CONTAINS_VALUE_RETURNS_OPPOSITE_VALUE)
            .build();

        assertTrue(map.containsValue(999)); // Should be false, returns true

        map.put("key", 1);
        assertFalse(map.containsValue(1)); // Should be true, returns false
    }

    /// Tests that containsValue() throws UnsupportedOperationException when method is not supported.
    ///
    /// Validates that when the containsValue method is marked as unsupported using the Builder's
    /// doesNotSupport configuration, the method throws UnsupportedOperationException regardless
    /// of any breaks that might be configured.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with containsValue marked as unsupported
    /// - Configures a containsValue-related break
    /// - Attempts to call containsValue() and verifies exception is thrown
    ///
    /// **Unsupported Method Behavior:**
    /// - Method throws UnsupportedOperationException immediately
    /// - Breaks are not evaluated when method is unsupported
    /// - Other supported methods continue to work normally
    ///
    /// **Assertions:**
    /// - `assertThrows(UnsupportedOperationException.class, () -> map.containsValue(1))`
    @Test
    @DisplayName("Test containsValue(Object) throws when not supported")
    public void testContainsValueWhenNotSupported() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
                .addBreak(CONTAINS_VALUE_RETURNS_OPPOSITE_VALUE)
                .doesNotSupport(MapMethods.CONTAINS_VALUE)
                .build();

        assertThrows(UnsupportedOperationException.class, () -> map.containsValue(1));
    }


    // ========== Access Break Tests ==========

    /// Tests the GET_ALWAYS_RETURNS_NULL break functionality.
    ///
    /// Validates that when the GET_ALWAYS_RETURNS_NULL break is active, the get() method
    /// consistently returns null regardless of whether the requested key exists and has
    /// an associated value in the map.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the GET_ALWAYS_RETURNS_NULL break
    /// - Adds a key-value pair to the map
    /// - Verifies that get() returns null despite the key existing
    ///
    /// **Break Behavior:**
    /// - `get(key)` returns null for any key parameter
    /// - Actual key-value pairs remain accessible through other methods
    /// - containsKey() continues to work normally showing key presence
    ///
    /// **Assertions:**
    /// - `assertNull(map.get("key"))` even though key exists
    /// - `assertTrue(map.containsKey("key"))` to verify key is actually present
    @Test
    @DisplayName("Test GET_ALWAYS_RETURNS_NULL break")
    public void testGetAlwaysReturnsNull() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(GET_ALWAYS_RETURNS_NULL)
            .build();

        map.put("key", 42);
        assertNull(map.get("key")); // Returns null due to break
        assertTrue(map.containsKey("key")); // containsKey should work normally
    }

    /// Tests the GET_FAILS_FOR_FIRST_KEY break functionality.
    ///
    /// Validates that when the GET_FAILS_FOR_FIRST_KEY break is active, the get() method
    /// returns null for the first key in the map's iteration order, while other keys
    /// continue to return their actual values.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the GET_FAILS_FOR_FIRST_KEY break
    /// - Adds multiple key-value pairs to the map
    /// - Tests get() for the first key and other keys
    ///
    /// **Break Behavior:**
    /// - `get(firstKey)` returns null regardless of actual value
    /// - `get(otherKeys)` returns actual values normally
    /// - First key is determined by keySet iteration order
    ///
    /// **Assertions:**
    /// - `assertNull(map.get(firstKey))` for the first key in iteration order
    /// - `assertNotNull(map.get(key))` for all other keys
    @Test
    @DisplayName("Test GET_FAILS_FOR_FIRST_KEY break")
    public void testGetFailsForFirstKey() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(GET_FAILS_FOR_FIRST_KEY)
            .build();

        map.put("key1", 1);
        map.put("key2", 2);
        map.put("key3", 3);

        // First key in iteration order should return null
        String firstKey = map.keySet().iterator().next();
        assertNull(map.get(firstKey));

        // Other keys should work normally
        for (String key : map.keySet()) {
            if (!key.equals(firstKey)) {
                assertNotNull(map.get(key));
            }
        }
    }

    /// Tests the GET_FAILS_FOR_LAST_KEY break functionality.
    ///
    /// Validates that when the GET_FAILS_FOR_LAST_KEY break is active, the get() method
    /// returns null for the last key in the map's iteration order, while other keys
    /// continue to return their actual values.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the GET_FAILS_FOR_LAST_KEY break
    /// - Adds multiple key-value pairs to the map
    /// - Tests get() for the last key and other keys
    ///
    /// **Break Behavior:**
    /// - `get(lastKey)` returns null regardless of actual value
    /// - `get(otherKeys)` returns actual values normally
    /// - Last key is determined by keySet iteration order
    ///
    /// **Assertions:**
    /// - `assertNull(map.get(lastKey))` for the last key in iteration order
    /// - `assertNotNull(map.get(key))` for all other keys
    @Test
    @DisplayName("Test GET_FAILS_FOR_LAST_KEY break")
    public void testGetFailsForLastKey() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(GET_FAILS_FOR_LAST_KEY)
            .build();

        map.put("key1", 1);
        map.put("key2", 2);
        map.put("key3", 3);

        // Find the last key
        String lastKey = null;
        for (String key : map.keySet()) {
            lastKey = key;
        }

        // Last key should return null
        assertNull(map.get(lastKey));

        // Other keys should work normally
        for (String key : map.keySet()) {
            if (!key.equals(lastKey)) {
                assertNotNull(map.get(key));
            }
        }
    }

    /// Tests that get() throws UnsupportedOperationException when method is not supported.
    ///
    /// Validates that when the get method is marked as unsupported using the Builder's
    /// doesNotSupport configuration, the method throws UnsupportedOperationException regardless
    /// of any breaks that might be configured.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with get marked as unsupported
    /// - Configures multiple get-related breaks
    /// - Attempts to call get() and verifies exception is thrown
    ///
    /// **Unsupported Method Behavior:**
    /// - Method throws UnsupportedOperationException immediately
    /// - Breaks are not evaluated when method is unsupported
    /// - Other supported methods continue to work normally
    ///
    /// **Assertions:**
    /// - `assertThrows(UnsupportedOperationException.class, () -> map.get("nonexistant"))`
    @Test
    @DisplayName("Test get(Object) throws when not supported")
    public void testGetWhenNotSupported() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
                .addBreak(GET_ALWAYS_RETURNS_NULL)
                .addBreak(GET_FAILS_FOR_FIRST_KEY)
                .addBreak(GET_FAILS_FOR_LAST_KEY)
                .doesNotSupport(MapMethods.GET)
                .build();

        assertThrows(UnsupportedOperationException.class, () -> map.get("nonexistent"));
    }

    // ========== Modification Break Tests ==========

    /// Tests the PUT_DOES_NOT_ADD_PAIR break functionality.
    ///
    /// Validates that when the PUT_DOES_NOT_ADD_PAIR break is active, the put() method
    /// appears to succeed (returns appropriate values) but does not actually add_singleElement_returnsTrueAndUpdatesSize the
    /// key-value pair to the map.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the PUT_DOES_NOT_ADD_PAIR break
    /// - Attempts to put a key-value pair
    /// - Verifies that put appears successful but map remains empty
    ///
    /// **Break Behavior:**
    /// - `put(key, value)` returns null as if successful
    /// - Map size remains unchanged (0)
    /// - containsKey() returns false showing key was not added
    ///
    /// **Assertions:**
    /// - `assertNull(map.put("key", 42))` returns null as if successful
    /// - `assertEquals(0, map.size())` map remains empty
    /// - `assertFalse(map.containsKey("key"))` key was not added
    @Test
    @DisplayName("Test PUT_DOES_NOT_ADD_PAIR break")
    public void testPutDoesNotAddPair() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(PUT_DOES_NOT_ADD_PAIR)
            .build();

        assertNull(map.put("key", 42)); // Returns null as if successful
        assertEquals(0, map.size()); // But map is still empty
        assertFalse(map.containsKey("key"));
    }

    /// Tests the PUT_ALWAYS_RETURNS_NULL break functionality.
    ///
    /// Validates that when the PUT_ALWAYS_RETURNS_NULL break is active, the put() method
    /// always returns null regardless of whether it's adding a new key or updating an
    /// existing key, while still performing the actual put operation.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the PUT_ALWAYS_RETURNS_NULL break
    /// - Tests both new key addition and existing key update
    /// - Verifies that put operations succeed but always return null
    ///
    /// **Break Behavior:**
    /// - `put(key, value)` returns null for new keys (normally returns null)
    /// - `put(key, newValue)` returns null for updates (normally returns old value)
    /// - Actual key-value operations proceed normally
    ///
    /// **Assertions:**
    /// - `assertNull(map.put("key", 42))` for first put
    /// - `assertNull(map.put("key", 84))` for update (should return 42, returns null)
    /// - `assertEquals(Integer.valueOf(84), map.get("key"))` value is actually updated
    @Test
    @DisplayName("Test PUT_ALWAYS_RETURNS_NULL break")
    public void testPutAlwaysReturnsNull() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(PUT_ALWAYS_RETURNS_NULL)
            .build();

        assertNull(map.put("key", 42)); // First put returns null
        assertNull(map.put("key", 84)); // Update also returns null (should return 42)

        assertEquals(Integer.valueOf(84), map.get("key")); // Value is actually updated
    }

    /// Tests the REMOVE_DOES_NOT_REMOVE_KEY break functionality.
    ///
    /// Validates that when the REMOVE_DOES_NOT_REMOVE_KEY break is active, the remove() method
    /// returns the value as if the removal was successful, but the key-value pair remains
    /// in the map.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the REMOVE_DOES_NOT_REMOVE_KEY break
    /// - Adds a key-value pair and attempts to remove it
    /// - Verifies that remove appears successful but key remains
    ///
    /// **Break Behavior:**
    /// - `remove(key)` returns the associated value
    /// - Key-value pair remains in the map after remove call
    /// - containsKey() and get() continue to work with the key
    ///
    /// **Assertions:**
    /// - `assertEquals(Integer.valueOf(42), map.remove("key"))` returns value
    /// - `assertTrue(map.containsKey("key"))` key is still there
    /// - `assertEquals(Integer.valueOf(42), map.get("key"))` value is still accessible
    @Test
    @DisplayName("Test REMOVE_DOES_NOT_REMOVE_KEY break")
    public void testRemoveDoesNotRemoveKey() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(REMOVE_DOES_NOT_REMOVE_KEY)
            .build();

        map.put("key", 42);
        assertEquals(Integer.valueOf(42), map.remove("key")); // Returns value

        // But key is still there
        assertTrue(map.containsKey("key"));
        assertEquals(Integer.valueOf(42), map.get("key"));
    }

    /// Tests the REMOVE_ALWAYS_RETURNS_NULL break functionality.
    ///
    /// Validates that when the REMOVE_ALWAYS_RETURNS_NULL break is active, the remove() method
    /// always returns null regardless of whether a key was actually present and removed,
    /// while still performing the actual removal operation.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the REMOVE_ALWAYS_RETURNS_NULL break
    /// - Adds a key-value pair and removes it
    /// - Verifies that remove returns null but key is actually removed
    ///
    /// **Break Behavior:**
    /// - `remove(key)` returns null instead of the actual value
    /// - Removal operation proceeds normally
    /// - Key is actually removed from the map
    ///
    /// **Assertions:**
    /// - `assertNull(map.remove("key"))` returns null instead of value
    /// - `assertFalse(map.containsKey("key"))` key is actually removed
    @Test
    @DisplayName("Test REMOVE_ALWAYS_RETURNS_NULL break")
    public void testRemoveAlwaysReturnsNull() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(REMOVE_ALWAYS_RETURNS_NULL)
            .build();

        map.put("key", 42);
        assertNull(map.remove("key")); // Returns null instead of 42

        // But key is actually removed
        assertFalse(map.containsKey("key"));
    }

    // ========== Bulk Operation Break Tests ==========

    /// Tests the PUT_ALL_DOES_NOT_ADD_ANY_PAIRS break functionality.
    ///
    /// Validates that when the PUT_ALL_DOES_NOT_ADD_ANY_PAIRS break is active, the putAll()
    /// method appears to succeed but does not actually add_singleElement_returnsTrueAndUpdatesSize any of the provided key-value
    /// pairs to the map.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the PUT_ALL_DOES_NOT_ADD_ANY_PAIRS break
    /// - Attempts to add_singleElement_returnsTrueAndUpdatesSize multiple pairs using putAll()
    /// - Verifies that putAll appears successful but map remains empty
    ///
    /// **Break Behavior:**
    /// - `putAll(map)` method completes without exception
    /// - No key-value pairs are actually added to the map
    /// - Map size remains unchanged
    ///
    /// **Assertions:**
    /// - `assertEquals(0, map.size())` after putAll operation
    /// - `assertFalse(map.containsKey("key1"))` and `assertFalse(map.containsKey("key2"))`
    @Test
    @DisplayName("Test PUT_ALL_DOES_NOT_ADD_ANY_PAIRS break")
    public void testPutAllDoesNotAddAnyPairs() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(PUT_ALL_DOES_NOT_ADD_ANY_PAIRS)
            .build();

        Map<String, Integer> toAdd = Map.of("key1", 1, "key2", 2);
        map.putAll(toAdd); // Should appear to succeed

        assertEquals(0, map.size()); // But nothing was added
        assertFalse(map.containsKey("key1"));
        assertFalse(map.containsKey("key2"));
    }

    /// Tests that putAll() throws UnsupportedOperationException when method is not supported.
    ///
    /// Validates that when the putAll method is marked as unsupported using the Builder's
    /// doesNotSupport configuration, the method throws UnsupportedOperationException regardless
    /// of any breaks that might be configured.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with putAll marked as unsupported
    /// - Configures a putAll-related break
    /// - Attempts to call putAll() and verifies exception is thrown
    ///
    /// **Unsupported Method Behavior:**
    /// - Method throws UnsupportedOperationException immediately
    /// - Breaks are not evaluated when method is unsupported
    /// - Other supported methods continue to work normally
    ///
    /// **Assertions:**
    /// - `assertThrows(UnsupportedOperationException.class, () -> map.putAll(Collections.emptyMap()))`
    @Test
    @DisplayName("Test putAll(Map)throws when not supported")
    public void testPutAllWhenNotSupported() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
                .doesNotSupport(MapMethods.PUT_ALL)
                .addBreak(PUT_ALL_DOES_NOT_ADD_ANY_PAIRS)
                .build();

        assertThrows(UnsupportedOperationException.class, () -> map.putAll(Collections.emptyMap()));
    }

    /// Tests the CLEAR_ALL_DOES_NOT_REMOVE_ANY_PAIRS break functionality.
    ///
    /// Validates that when the CLEAR_ALL_DOES_NOT_REMOVE_ANY_PAIRS break is active, the clear()
    /// method appears to succeed but does not actually remove any key-value pairs from the map.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the CLEAR_ALL_DOES_NOT_REMOVE_ANY_PAIRS break
    /// - Adds multiple key-value pairs and calls clear()
    /// - Verifies that clear appears successful but all pairs remain
    ///
    /// **Break Behavior:**
    /// - `clear()` method completes without exception
    /// - All key-value pairs remain in the map
    /// - Map size and contents are unchanged
    ///
    /// **Assertions:**
    /// - `assertEquals(2, map.size())` after clear operation
    /// - `assertTrue(map.containsKey("key1"))` and `assertTrue(map.containsKey("key2"))`
    @Test
    @DisplayName("Test CLEAR_ALL_DOES_NOT_REMOVE_ANY_PAIRS break")
    public void testClearDoesNotRemoveAnyPairs() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(CLEAR_ALL_DOES_NOT_REMOVE_ANY_PAIRS)
            .build();

        map.put("key1", 1);
        map.put("key2", 2);
        map.clear(); // Should appear to succeed

        // But nothing was removed
        assertEquals(2, map.size());
        assertTrue(map.containsKey("key1"));
        assertTrue(map.containsKey("key2"));
    }

    /// Tests the CLEAR_DOES_NOT_REMOVE_FIRST_PAIR break functionality.
    ///
    /// Validates that when the CLEAR_DOES_NOT_REMOVE_FIRST_PAIR break is active, the clear()
    /// method removes all key-value pairs except for the first one in iteration order.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the CLEAR_DOES_NOT_REMOVE_FIRST_PAIR break
    /// - Adds multiple key-value pairs and calls clear()
    /// - Verifies that all pairs except the first are removed
    ///
    /// **Break Behavior:**
    /// - `clear()` removes all pairs except the first in keySet iteration order
    /// - First key-value pair remains accessible
    /// - Map size becomes 1 instead of 0
    ///
    /// **Assertions:**
    /// - `assertEquals(1, map.size())` after clear operation
    /// - `assertTrue(map.containsKey(firstKey))` first key remains
    @Test
    @DisplayName("Test CLEAR_DOES_NOT_REMOVE_FIRST_PAIR break")
    public void testClearDoesNotRemoveFirstPair() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(CLEAR_DOES_NOT_REMOVE_FIRST_PAIR)
            .build();

        map.put("key1", 1);
        map.put("key2", 2);
        map.put("key3", 3);

        String firstKey = map.keySet().iterator().next();
        map.clear();

        // First key should remain
        assertEquals(1, map.size());
        assertTrue(map.containsKey(firstKey));
    }

    /// Tests the CLEAR_DOES_NOT_REMOVE_LAST_PAIR break functionality.
    ///
    /// Validates that when the CLEAR_DOES_NOT_REMOVE_LAST_PAIR break is active, the clear()
    /// method removes all key-value pairs except for the last one in iteration order.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the CLEAR_DOES_NOT_REMOVE_LAST_PAIR break
    /// - Adds multiple key-value pairs and calls clear()
    /// - Verifies that all pairs except the last are removed
    ///
    /// **Break Behavior:**
    /// - `clear()` removes all pairs except the last in keySet iteration order
    /// - Last key-value pair remains accessible
    /// - Map size becomes 1 instead of 0
    ///
    /// **Assertions:**
    /// - `assertEquals(1, map.size())` after clear operation
    /// - `assertFalse(map.isEmpty())` map is not empty after clear
    @Test
    @DisplayName("Test CLEAR_DOES_NOT_REMOVE_LAST_PAIR break")
    public void testClearDoesNotRemoveLastPair() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(CLEAR_DOES_NOT_REMOVE_LAST_PAIR)
            .build();

        map.put("key1", 1);
        map.put("key2", 2);
        map.put("key3", 3);

        map.clear();

        // After clear with this break, one element should remain
        assertEquals(1, map.size(), "Size should be 1 after clearing with CLEAR_DOES_NOT_REMOVE_LAST_PAIR break");
        assertFalse(map.isEmpty(), "Map should not be empty after clear with this break");
    }

    /// Tests that clear() throws UnsupportedOperationException when method is not supported.
    ///
    /// Validates that when the clear method is marked as unsupported using the Builder's
    /// doesNotSupport configuration, the method throws UnsupportedOperationException regardless
    /// of any breaks that might be configured.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with clear marked as unsupported
    /// - Configures multiple clear-related breaks
    /// - Attempts to call clear() and verifies exception is thrown
    ///
    /// **Unsupported Method Behavior:**
    /// - Method throws UnsupportedOperationException immediately
    /// - Breaks are not evaluated when method is unsupported
    /// - Other supported methods continue to work normally
    ///
    /// **Assertions:**
    /// - `assertThrows(UnsupportedOperationException.class, map::clear)`
    @Test
    @DisplayName("Test clear() throws when not supported")
    public void testClearWhenNotSupported() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
                .addBreak(CLEAR_ALL_DOES_NOT_REMOVE_ANY_PAIRS)
                .addBreak(CLEAR_DOES_NOT_REMOVE_FIRST_PAIR)
                .addBreak(CLEAR_DOES_NOT_REMOVE_LAST_PAIR)
                .doesNotSupport(MapMethods.CLEAR)
                .build();

        assertThrows(UnsupportedOperationException.class, map::clear);
    }

    // ========== View Collection Break Tests ==========

    /// Tests the KEY_SET_RETURNS_EMPTY_SET break functionality.
    ///
    /// Validates that when the KEY_SET_RETURNS_EMPTY_SET break is active, the keySet()
    /// method returns an empty set regardless of the actual keys present in the map.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the KEY_SET_RETURNS_EMPTY_SET break
    /// - Adds multiple key-value pairs to the map
    /// - Verifies that keySet() returns empty set despite map containing data
    ///
    /// **Break Behavior:**
    /// - `keySet()` returns an empty set regardless of actual keys
    /// - Map data remains accessible through other methods
    /// - Map size reflects actual content
    ///
    /// **Assertions:**
    /// - `assertTrue(keySet.isEmpty())` despite map containing keys
    /// - `assertEquals(2, map.size())` map still has data
    @Test
    @DisplayName("Test KEY_SET_RETURNS_EMPTY_SET break")
    public void testKeySetReturnsEmptySet() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(KEY_SET_RETURNS_EMPTY_SET)
            .build();

        map.put("key1", 1);
        map.put("key2", 2);

        Set<String> keySet = map.keySet();
        assertTrue(keySet.isEmpty()); // Returns empty set due to break
        assertEquals(2, map.size()); // Map still has data
    }

    /// Tests the KEY_SET_RETURNS_NULL_WHEN_EMPTY break functionality.
    ///
    /// Validates that when the KEY_SET_RETURNS_NULL_WHEN_EMPTY break is active, the keySet()
    /// method returns null when the map is empty, but returns a normal set when not empty.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the KEY_SET_RETURNS_NULL_WHEN_EMPTY break
    /// - Tests keySet() on both empty and non-empty states
    /// - Verifies conditional null return behavior
    ///
    /// **Break Behavior:**
    /// - `keySet()` returns null when map is empty
    /// - `keySet()` returns normal set when map contains elements
    /// - Break only affects empty state
    ///
    /// **Assertions:**
    /// - `assertNull(map.keySet())` when map is empty
    /// - `assertNotNull(map.keySet())` when map has elements
    @Test
    @DisplayName("Test KEY_SET_RETURNS_NULL_WHEN_EMPTY break")
    public void testKeySetReturnsNullWhenEmpty() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(KEY_SET_RETURNS_NULL_WHEN_EMPTY)
            .build();

        assertNull(map.keySet()); // Returns null when empty

        map.put("key", 1);
        assertNotNull(map.keySet()); // Returns normal set when not empty
    }

    /// Tests that keySet() throws UnsupportedOperationException when method is not supported.
    ///
    /// Validates that when the keySet method is marked as unsupported using the Builder's
    /// doesNotSupport configuration, the method throws UnsupportedOperationException regardless
    /// of any breaks that might be configured.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with keySet marked as unsupported
    /// - Configures multiple keySet-related breaks
    /// - Attempts to call keySet() and verifies exception is thrown
    ///
    /// **Unsupported Method Behavior:**
    /// - Method throws UnsupportedOperationException immediately
    /// - Breaks are not evaluated when method is unsupported
    /// - Other supported methods continue to work normally
    ///
    /// **Assertions:**
    /// - `assertThrows(UnsupportedOperationException.class, map::keySet)`
    @Test
    @DisplayName("Test keySet() throws when not supported")
    public void testKeySetWhenNotSupported() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
                .addBreak(KEY_SET_RETURNS_NULL_WHEN_EMPTY)
                .addBreak(KEY_SET_RETURNS_EMPTY_SET)
                .doesNotSupport(MapMethods.KEY_SET)
                .build();

        assertThrows(UnsupportedOperationException.class, map::keySet);
    }

    /// Tests the VALUES_RETURNS_EMPTY_COLLECTION break functionality.
    ///
    /// Validates that when the VALUES_RETURNS_EMPTY_COLLECTION break is active, the values()
    /// method returns an empty collection regardless of the actual values present in the map.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the VALUES_RETURNS_EMPTY_COLLECTION break
    /// - Adds multiple key-value pairs to the map
    /// - Verifies that values() returns empty collection despite map containing data
    ///
    /// **Break Behavior:**
    /// - `values()` returns an empty collection regardless of actual values
    /// - Map data remains accessible through other methods
    /// - Map size reflects actual content
    ///
    /// **Assertions:**
    /// - `assertTrue(values.isEmpty())` despite map containing values
    /// - `assertEquals(2, map.size())` map still has data
    @Test
    @DisplayName("Test VALUES_RETURNS_EMPTY_COLLECTION break")
    public void testValuesReturnsEmptyCollection() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(VALUES_RETURNS_EMPTY_COLLECTION)
            .build();

        map.put("key1", 1);
        map.put("key2", 2);

        Collection<Integer> values = map.values();
        assertTrue(values.isEmpty()); // Returns empty collection due to break
        assertEquals(2, map.size()); // Map still has data
    }

    /// Tests the VALUES_RETURNS_NULL_WHEN_EMPTY break functionality.
    ///
    /// Validates that when the VALUES_RETURNS_NULL_WHEN_EMPTY break is active, the values()
    /// method returns null when the map is empty, but returns a normal collection when not empty.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the VALUES_RETURNS_NULL_WHEN_EMPTY break
    /// - Tests values() on both empty and non-empty states
    /// - Verifies conditional null return behavior
    ///
    /// **Break Behavior:**
    /// - `values()` returns null when map is empty
    /// - `values()` returns normal collection when map contains elements
    /// - Break only affects empty state
    ///
    /// **Assertions:**
    /// - `assertNull(map.values())` when map is empty
    /// - `assertNotNull(map.values())` when map has elements
    @Test
    @DisplayName("Test VALUES_RETURNS_NULL_WHEN_EMPTY break")
    public void testValuesReturnsNullWhenEmpty() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(VALUES_RETURNS_NULL_WHEN_EMPTY)
            .build();

        assertNull(map.values()); // Returns null when empty

        map.put("key", 1);
        assertNotNull(map.values()); // Returns normal collection when not empty
    }

    /// Tests that values() throws UnsupportedOperationException when method is not supported.
    ///
    /// Validates that when the values method is marked as unsupported using the Builder's
    /// doesNotSupport configuration, the method throws UnsupportedOperationException regardless
    /// of any breaks that might be configured.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with values marked as unsupported
    /// - Configures multiple values-related breaks
    /// - Attempts to call values() and verifies exception is thrown
    ///
    /// **Unsupported Method Behavior:**
    /// - Method throws UnsupportedOperationException immediately
    /// - Breaks are not evaluated when method is unsupported
    /// - Other supported methods continue to work normally
    ///
    /// **Assertions:**
    /// - `assertThrows(UnsupportedOperationException.class, map::values)`
    @Test
    @DisplayName("Test values() throws when not supported")
    public void testValuesWhenNotSupported() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
                .addBreak(VALUES_RETURNS_NULL_WHEN_EMPTY)
                .addBreak(VALUES_RETURNS_EMPTY_COLLECTION)
                .doesNotSupport(MapMethods.VALUES)
                .build();

        assertThrows(UnsupportedOperationException.class, map::values);
    }

    /// Tests the ENTRY_SET_RETURNS_EMPTY_SET break functionality.
    ///
    /// Validates that when the ENTRY_SET_RETURNS_EMPTY_SET break is active, the entrySet()
    /// method returns an empty set regardless of the actual entries present in the map.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the ENTRY_SET_RETURNS_EMPTY_SET break
    /// - Adds multiple key-value pairs to the map
    /// - Verifies that entrySet() returns empty set despite map containing data
    ///
    /// **Break Behavior:**
    /// - `entrySet()` returns an empty set regardless of actual entries
    /// - Map data remains accessible through other methods
    /// - Map size reflects actual content
    ///
    /// **Assertions:**
    /// - `assertTrue(entrySet.isEmpty())` despite map containing entries
    /// - `assertEquals(2, map.size())` map still has data
    @Test
    @DisplayName("Test ENTRY_SET_RETURNS_EMPTY_SET break")
    public void testEntrySetReturnsEmptySet() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(ENTRY_SET_RETURNS_EMPTY_SET)
            .build();

        map.put("key1", 1);
        map.put("key2", 2);

        Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
        assertTrue(entrySet.isEmpty()); // Returns empty set due to break
        assertEquals(2, map.size()); // Map still has data
    }

    /// Tests the ENTRY_SET_RETURNS_NULL_WHEN_EMPTY break functionality.
    ///
    /// Validates that when the ENTRY_SET_RETURNS_NULL_WHEN_EMPTY break is active, the entrySet()
    /// method returns null when the map is empty, but returns a normal set when not empty.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with the ENTRY_SET_RETURNS_NULL_WHEN_EMPTY break
    /// - Tests entrySet() on both empty and non-empty states
    /// - Verifies conditional null return behavior
    ///
    /// **Break Behavior:**
    /// - `entrySet()` returns null when map is empty
    /// - `entrySet()` returns normal set when map contains elements
    /// - Break only affects empty state
    ///
    /// **Assertions:**
    /// - `assertNull(map.entrySet())` when map is empty
    /// - `assertNotNull(map.entrySet())` when map has elements
    @Test
    @DisplayName("Test ENTRY_SET_RETURNS_NULL_WHEN_EMPTY break")
    public void testEntrySetReturnsNullWhenEmpty() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .addBreak(ENTRY_SET_RETURNS_NULL_WHEN_EMPTY)
            .build();

        assertNull(map.entrySet()); // Returns null when empty

        map.put("key", 1);
        assertNotNull(map.entrySet()); // Returns normal set when not empty
    }

    /// Tests that entrySet() throws UnsupportedOperationException when method is not supported.
    ///
    /// Validates that when the entrySet method is marked as unsupported using the Builder's
    /// doesNotSupport configuration, the method throws UnsupportedOperationException regardless
    /// of any breaks that might be configured.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with entrySet marked as unsupported
    /// - Configures multiple entrySet-related breaks
    /// - Attempts to call entrySet() and verifies exception is thrown
    ///
    /// **Unsupported Method Behavior:**
    /// - Method throws UnsupportedOperationException immediately
    /// - Breaks are not evaluated when method is unsupported
    /// - Other supported methods continue to work normally
    ///
    /// **Assertions:**
    /// - `assertThrows(UnsupportedOperationException.class, map::entrySet)`
    @Test
    @DisplayName("Test entrySet() throws when not supported")
    public void testEntrySetWhenNotSupported() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
                .addBreak(ENTRY_SET_RETURNS_EMPTY_SET)
                .addBreak(ENTRY_SET_RETURNS_NULL_WHEN_EMPTY)
                .doesNotSupport(MapMethods.ENTRY_SET)
                .build();

        assertThrows(UnsupportedOperationException.class, map::entrySet);
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
    /// putIfAbsent() method appears to succeed but does not actually add_singleElement_returnsTrueAndUpdatesSize the key-value
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

    // ========== Null Handling Tests ==========

    /// Tests null key handling with different null key policies.
    ///
    /// Validates that BreakableMap correctly enforces null key policies both when permitting
    /// and rejecting null keys. This test covers the behavior of put(), get(), and containsKey()
    /// methods with null key parameters.
    ///
    /// **Test Scenarios:**
    /// 1. **Permissive Policy**: Map permits null keys and accepts null key operations
    /// 2. **Restrictive Policy**: Map rejects null keys and throws NullPointerException
    ///
    /// **Permissive Behavior:**
    /// - `permitsNullKeys()` returns true
    /// - `put(null, value)` succeeds without exception
    /// - `get(null)` returns the associated value
    ///
    /// **Restrictive Behavior:**
    /// - `permitsNullKeys()` returns false
    /// - `put(null, value)` throws NullPointerException
    /// - `get(null)` throws NullPointerException
    /// - `containsKey(null)` throws NullPointerException
    ///
    /// **Assertions:**
    /// - Policy verification: `assertTrue(map.permitsNullKeys())` and `assertFalse(strictMap.permitsNullKeys())`
    /// - Operation success: `assertDoesNotThrow(() -> map.put(null, 42))`
    /// - Exception throwing: `assertThrows(NullPointerException.class, () -> strictMap.put(null, 42))`
    @Test
    @DisplayName("Test null key handling")
    public void testNullKeyHandling() {
        // Map that permits null keys
        BreakableMap<String, Integer> map = new BreakableMap<>();
        assertTrue(map.permitsNullKeys());

        assertDoesNotThrow(() -> map.put(null, 42));
        assertEquals(Integer.valueOf(42), map.get(null));

        // Map that doesn't permit null keys
        BreakableMap<String, Integer> strictMap = new BreakableMap.Builder<String, Integer>()
            .doesNotPermitNullKeys()
            .build();

        assertFalse(strictMap.permitsNullKeys());
        assertThrows(NullPointerException.class, () -> strictMap.put(null, 42));
        assertThrows(NullPointerException.class, () -> strictMap.get(null));
        assertThrows(NullPointerException.class, () -> strictMap.containsKey(null));
    }

    /// Tests null value handling with different null value policies.
    ///
    /// Validates that BreakableMap correctly enforces null value policies both when permitting
    /// and rejecting null values. This test covers the behavior of put() and containsValue()
    /// methods with null value parameters.
    ///
    /// **Test Scenarios:**
    /// 1. **Permissive Policy**: Map permits null values and accepts null value operations
    /// 2. **Restrictive Policy**: Map rejects null values and throws NullPointerException
    ///
    /// **Permissive Behavior:**
    /// - `permitsNullValues()` returns true
    /// - `put(key, null)` succeeds without exception
    /// - `get(key)` returns null for null-valued keys
    ///
    /// **Restrictive Behavior:**
    /// - `permitsNullValues()` returns false
    /// - `put(key, null)` throws NullPointerException
    /// - `containsValue(null)` throws NullPointerException
    ///
    /// **Assertions:**
    /// - Policy verification: `assertTrue(map.permitsNullValues())` and `assertFalse(strictMap.permitsNullValues())`
    /// - Operation success: `assertDoesNotThrow(() -> map.put("key", null))`
    /// - Exception throwing: `assertThrows(NullPointerException.class, () -> strictMap.put("key", null))`
    @Test
    @DisplayName("Test null value handling")
    public void testNullValueHandling() {
        // Map that permits null values
        BreakableMap<String, Integer> map = new BreakableMap<>();
        assertTrue(map.permitsNullValues());

        assertDoesNotThrow(() -> map.put("key", null));
        assertNull(map.get("key"));

        // Map that doesn't permit null values
        BreakableMap<String, Integer> strictMap = new BreakableMap.Builder<String, Integer>()
            .doesNotPermitNullValues()
            .build();

        assertFalse(strictMap.permitsNullValues());
        assertThrows(NullPointerException.class, () -> strictMap.put("key", null));
        assertThrows(NullPointerException.class, () -> strictMap.containsValue(null));
    }


    // ========== Static Factory Method Tests ==========

    /// Tests the static wrap factory method functionality.
    ///
    /// Validates that the BreakableMap.wrap() factory method correctly creates a BreakableMap
    /// instance that wraps an existing map with specified breaks while preserving the
    /// original data and applying the configured behavioral modifications.
    ///
    /// **Test Scenario:**
    /// - Creates a HashMap with initial data
    /// - Uses wrap() factory method with specific breaks
    /// - Verifies that breaks are active and data is preserved
    ///
    /// **Factory Method Behavior:**
    /// - `BreakableMap.wrap(existingMap, breaks)` creates wrapped instance
    /// - Original map data is preserved and accessible
    /// - Specified breaks are immediately active
    /// - Default null policies are permissive
    ///
    /// **Assertions:**
    /// - Break activation: `assertEquals(0, wrappedMap.size())` and `assertNull(wrappedMap.get("key1"))`
    /// - Data preservation: `assertTrue(wrappedMap.containsKey("key1"))` and `assertTrue(wrappedMap.containsKey("key2"))`
    /// - Default policies: `assertTrue(wrappedMap.permitsNullKeys())` and `assertTrue(wrappedMap.permitsNullValues())`
    @Test
    @DisplayName("Test wrap factory method")
    public void testWrapFactoryMethod() {
        Map<String, Integer> existingMap = new HashMap<>();
        existingMap.put("key1", 1);
        existingMap.put("key2", 2);

        Set<Break> breaks = Set.of(SIZE_ALWAYS_RETURNS_ZERO, GET_ALWAYS_RETURNS_NULL);

        BreakableMap<String, Integer> wrappedMap = BreakableMap.wrap(existingMap, breaks, DEFAULT_METHOD_STATUSES,
                DEFAULT_PERMITS, DEFAULT_SAFETY);

        // Verify breaks are active
        assertEquals(0, wrappedMap.size());
        assertNull(wrappedMap.get("key1"));

        // Verify original data is preserved (can be accessed through containsKey)
        assertTrue(wrappedMap.containsKey("key1"));
        assertTrue(wrappedMap.containsKey("key2"));

        // Verify null policies default to permissive
        assertTrue(wrappedMap.permitsNullKeys());
        assertTrue(wrappedMap.permitsNullValues());
    }

    // ========== Object Method Tests ==========

    /// Tests the equals method implementation for BreakableMap.
    ///
    /// Validates that BreakableMap correctly implements equals() by comparing the underlying
    /// map content rather than the break configuration. This ensures that maps with the same
    /// data but different breaks are considered equal, maintaining Map interface contract compliance.
    ///
    /// **Test Scenarios:**
    /// 1. **Empty Maps**: All empty maps should be equal regardless of configuration
    /// 2. **Same Content**: Maps with identical data should be equal
    /// 3. **Different Content**: Maps with different data should not be equal
    /// 4. **Cross-Type Equality**: BreakableMap should equal regular HashMap with same data
    /// 5. **Non-Map Objects**: Should not equal non-map objects
    ///
    /// **Equality Behavior:**
    /// - Comparison based on underlying map content only
    /// - Break configuration does not affect equality
    /// - Follows standard Map.equals() contract
    ///
    /// **Assertions:**
    /// - Empty equality: `assertEquals(map1, map2)` and `assertEquals(map1, regularMap)`
    /// - Content equality: `assertEquals(map1, map2)` after adding same data
    /// - Content inequality: `assertNotEquals(map1, map2)` after adding different data
    /// - Type safety: `assertNotEquals(map1, "Hello")` for non-map objects
    @Test
    @DisplayName("Test equals method")
    public void testEquals() {
        BreakableMap<String, Integer> map1 = new BreakableMap<>();
        BreakableMap<String, Integer> map2 = new BreakableMap<>();
        HashMap<String, Integer> regularMap = new HashMap<>();

        // Empty maps should be equal
        assertEquals(map1, map2);
        assertEquals(map1, regularMap);

        // Add same data
        map1.put("key", 42);
        map2.put("key", 42);
        regularMap.put("key", 42);

        assertEquals(map1, map2);
        assertEquals(map1, regularMap);

        // Different data should not be equal
        map2.put("key2", 84);
        assertNotEquals(map1, map2);
        assertEquals(map1, regularMap); // regularMap still equals map1 (both have only "key")

        // non-maps should not be equal
        //noinspection AssertBetweenInconvertibleTypes,MisorderedAssertEqualsArguments
        assertNotEquals(map1, "Hello");
    }

    /// Tests the hashCode method implementation for BreakableMap.
    ///
    /// Validates that BreakableMap correctly implements hashCode() by incorporating both the
    /// underlying map content and the break/policy configuration. This ensures that maps with
    /// the same data but different configurations have different hash codes when appropriate.
    ///
    /// **Test Scenarios:**
    /// 1. **Same Configuration**: Maps with identical data and configuration should have same hash code
    /// 2. **Different Configuration**: Maps with same data but different null policies should have different hash codes
    ///
    /// **HashCode Behavior:**
    /// - Incorporates underlying map content
    /// - Includes null policy configuration
    /// - Maintains consistency with equals() method
    ///
    /// **Assertions:**
    /// - Consistency: `assertEquals(map1.hashCode(), map2.hashCode())` for same configuration
    /// - Distinction: `assertNotEquals(map1.hashCode(), map3.hashCode())` for different configuration
    @Test
    @DisplayName("Test hashCode method")
    public void testHashCode() {
        BreakableMap<String, Integer> map1 = new BreakableMap.Builder<String, Integer>()
            .doesNotPermitNullKeys()
            .build();
        BreakableMap<String, Integer> map2 = new BreakableMap.Builder<String, Integer>()
            .doesNotPermitNullKeys()
            .build();
        BreakableMap<String, Integer> map3 = new BreakableMap<>(); // Different null policy

        map1.put("key", 42);
        map2.put("key", 42);
        map3.put("key", 43);

        // Maps with same data and configuration should have same hash code
        assertEquals(map1.hashCode(), map2.hashCode());

        // Maps with different configuration should likely have different hash codes
        assertNotEquals(map1.hashCode(), map3.hashCode());
    }

    /// Tests the toString method implementation for BreakableMap.
    ///
    /// Validates that BreakableMap provides a meaningful string representation that shows
    /// the underlying map content in a standard format, making it useful for debugging
    /// and logging purposes.
    ///
    /// **Test Scenarios:**
    /// 1. **Empty Map**: Should return standard empty map representation
    /// 2. **Non-Empty Map**: Should include key-value pairs in the output
    ///
    /// **ToString Behavior:**
    /// - Delegates to underlying map's toString() implementation
    /// - Provides standard Map string representation
    /// - Break configuration not included in output
    ///
    /// **Assertions:**
    /// - Empty representation: `assertEquals("{}", map.toString())`
    /// - Content inclusion: `assertTrue(map.toString().contains("key"))` and `assertTrue(map.toString().contains("42"))`
    @Test
    @DisplayName("Test toString method")
    public void testToString() {
        BreakableMap<String, Integer> map = new BreakableMap<>();
        assertEquals("{}", map.toString());

        map.put("key", 42);
        assertTrue(map.toString().contains("key"));
        assertTrue(map.toString().contains("42"));
    }

    /// Tests the clone method implementation for BreakableMap.
    ///
    /// Validates that BreakableMap correctly implements clone() by creating a deep copy of
    /// the underlying map data while preserving all break configuration and null policies.
    /// This ensures that the cloned map is functionally identical but completely independent.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with breaks, null policies, and data
    /// - Clones the map and verifies complete configuration transfer
    /// - Tests independence by modifying original after cloning
    ///
    /// **Clone Behavior:**
    /// - Creates new BreakableMap instance
    /// - Deep copies underlying map data
    /// - Preserves all break configurations
    /// - Maintains null key and value policies
    /// - Ensures complete independence from original
    ///
    /// **Assertions:**
    /// - Instance independence: `assertNotSame(original, cloned)`
    /// - Data preservation: `assertTrue(cloned.containsKey("key1"))` and `assertTrue(cloned.containsKey("key2"))`
    /// - Policy preservation: `assertFalse(cloned.permitsNullKeys())` and `assertFalse(cloned.permitsNullValues())`
    /// - Break preservation: `assertEquals(0, cloned.size())` and `assertNull(cloned.get("key1"))`
    /// - Independence: modifications to original don't affect clone
    /// - Policy enforcement: clone maintains null restrictions independently
    @Test
    @DisplayName("Test clone method")
    public void testClone() {
        BreakableMap<String, Integer> original = new BreakableMap.Builder<String, Integer>()
            .addBreak(SIZE_ALWAYS_RETURNS_ZERO)
            .addBreak(GET_ALWAYS_RETURNS_NULL)
            .doesNotPermitNullKeys()
            .doesNotPermitNullValues()
            .build();

        original.put("key1", 1);
        original.put("key2", 2);

        BreakableMap<String, Integer> cloned = original.clone();

        // Should be different objects
        assertNotSame(original, cloned);

        // Should have same data (even though get() returns null due to break)
        assertTrue(cloned.containsKey("key1"));
        assertTrue(cloned.containsKey("key2"));

        // Should have same null key policy
        assertFalse(cloned.permitsNullKeys());
        assertEquals(original.permitsNullKeys(), cloned.permitsNullKeys());

        // Should have same null value policy
        assertFalse(cloned.permitsNullValues());
        assertEquals(original.permitsNullValues(), cloned.permitsNullValues());

        // Should have same breaks
        assertEquals(0, cloned.size()); // SIZE_ALWAYS_RETURNS_ZERO is active
        assertNull(cloned.get("key1")); // GET_ALWAYS_RETURNS_NULL is active

        // Should be independent - modifications to original don't affect clone
        original.put("key3", 3);
        assertFalse(cloned.containsKey("key3"));

        // Clone should maintain null policies independently
        assertThrows(NullPointerException.class, () -> cloned.put(null, 42));
        assertThrows(NullPointerException.class, () -> cloned.put("key", null));
        assertThrows(NullPointerException.class, () -> cloned.containsKey(null));
        assertThrows(NullPointerException.class, () -> cloned.containsValue(null));
    }

    // ========== Unsupported Method Tests ==========

    /// Tests unsupported method exception handling.
    ///
    /// Validates that BreakableMap correctly throws UnsupportedOperationException for methods
    /// that have been marked as unsupported through the Builder configuration, while ensuring
    /// that other methods continue to work normally.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with specific methods marked as unsupported
    /// - Attempts to call unsupported methods and verifies exceptions
    /// - Calls supported methods and verifies normal operation
    ///
    /// **Unsupported Method Behavior:**
    /// - Configured methods throw UnsupportedOperationException immediately
    /// - Exception thrown before any break evaluation
    /// - Other methods remain fully functional
    ///
    /// **Assertions:**
    /// - Exception throwing: `assertThrows(UnsupportedOperationException.class, map::size)`
    /// - Exception throwing: `assertThrows(UnsupportedOperationException.class, () -> map.put("key", 42))`
    /// - Normal operation: `assertDoesNotThrow(map::isEmpty)` and `assertDoesNotThrow(() -> map.get("key"))`
    @Test
    @DisplayName("Test unsupported method exceptions")
    public void testUnsupportedMethodExceptions() {
        BreakableMap<String, Integer> map = new BreakableMap.Builder<String, Integer>()
            .doesNotSupport(MapMethods.SIZE)
            .doesNotSupport(MapMethods.PUT)
            .build();

        assertThrows(UnsupportedOperationException.class, map::size);
        assertThrows(UnsupportedOperationException.class, () -> map.put("key", 42));

        // Other methods should still work
        assertDoesNotThrow(map::isEmpty);
        assertDoesNotThrow(() -> map.get("key"));
    }
}