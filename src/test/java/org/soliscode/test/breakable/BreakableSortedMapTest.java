package org.soliscode.test.breakable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.sortedmap.SortedMapMethods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.breakable.BreakableSortedMap.*;

/// **Test Suite for BreakableSortedMap Implementation**
///
/// This comprehensive test class validates the behavior of BreakableSortedMap, focusing on both
/// standard SortedMap contract compliance and the controlled violation of sorted map semantics through
/// programmatic breaks. The tests ensure that BreakableSortedMap maintains proper sorted map behavior
/// under normal conditions while correctly implementing break mechanisms for testing purposes.
///
/// ## Test Coverage
///
/// ### SortedMap Contract Compliance Testing
/// - **Map Interface**: Full validation of inherited Map interface methods
/// - **SortedMap Interface**: Full validation of SortedMap-specific methods
/// - **Key Ordering**: Proper sorting and ordering behavior
/// - **Comparator Usage**: Natural and custom comparator handling
/// - **Sub-Map Operations**: headMap, tailMap, subMap functionality
/// - **Boundary Operations**: firstKey, lastKey behavior
///
/// ### Break Mechanism Testing
/// - **Comparator Breaks**: COMPARATOR_ALWAYS_RETURNS_NULL, COMPARATOR_THROWS_EXCEPTION
/// - **Key Boundary Breaks**: FIRST_KEY_THROWS_EXCEPTION, LAST_KEY_THROWS_EXCEPTION
/// - **Key Randomization Breaks**: FIRST_KEY_RETURNS_RANDOM_KEY, LAST_KEY_RETURNS_RANDOM_KEY
/// - **Sub-Map Breaks**: HEAD_MAP_*, TAIL_MAP_*, SUB_MAP_* variants
/// - **Exception Breaks**: Various exception-throwing behaviors
/// - **Return Value Breaks**: Empty map and null return behaviors
///
/// ### Builder Pattern Testing
/// - **SortedMap-Specific Configuration**: Builder setup with SortedMap parameters
/// - **Copy Semantics**: Builder copying and independence for sorted maps
/// - **Fluent Interface**: Method chaining with sorted map configuration
/// - **Pre-populated Maps**: Building from existing TreeMaps and other SortedMaps
///
/// ### Constructor Testing
/// - **Default Construction**: Empty sorted map creation with natural ordering
/// - **Copy Construction**: Creating sorted maps from existing BreakableSortedMap instances
/// - **SortedMap Construction**: Building from existing SortedMap implementations
/// - **Comparator Inheritance**: Proper comparator handling in construction
///
/// ## Test Architecture
///
/// This test class follows the SolisCode testing framework patterns:
/// - **AbstractTest Extension**: Inherits common testing infrastructure
/// - **SortedMap-Specific Testing**: Focused on SortedMap interface compliance
/// - **Break Isolation**: Each break is tested independently for sorted map operations
/// - **State Validation**: Verifies sorted map state consistency with ordering guarantees
///
/// ### Framework Integration
/// ```java
/// // The test class integrates framework components:
/// public class BreakableSortedMapTest extends AbstractTest {  // Base testing infrastructure
/// ```
///
/// ## Test Methodology
///
/// ### Standard Behavior Validation
/// Tests verify that BreakableSortedMap behaves like a proper SortedMap implementation:
/// - Key-value associations with proper ordering
/// - Correct comparator usage and natural ordering
/// - Proper return values from SortedMap operations
/// - Standard iteration behavior with sorted order
/// - Sub-map creation and boundary operations
///
/// ### Break Behavior Validation
/// Tests verify that breaks work as intended without compromising overall functionality:
/// - Break activation changes specific SortedMap behaviors
/// - Non-broken operations continue to work normally
/// - Ordering consistency is maintained where applicable
/// - Break effects are isolated and predictable
/// - Exception breaks properly throw expected exceptions
///
/// ### Builder Testing Strategy
/// Tests validate the builder pattern implementation for sorted maps:
/// - Configuration transfer from builder to sorted map
/// - Builder reusability and independence
/// - Copy constructor behavior with comparator preservation
/// - Fluent interface method chaining for sorted maps
/// - Pre-existing SortedMap integration
///
/// ## Example Test Scenarios
///
/// ### Normal SortedMap Behavior
/// ```java
/// @Test
/// void testStandardSortedMapBehavior() {
///     BreakableSortedMap<String, Integer> map = new BreakableSortedMap<>();
///     map.put("banana", 2);
///     map.put("apple", 1);
///     assertEquals("apple", map.firstKey());    // Proper ordering
///     assertEquals("banana", map.lastKey());    // Proper ordering
///     assertEquals(2, map.size());              // Size reflects content
/// }
/// ```
///
/// ### Break Behavior Testing
/// ```java
/// @Test
/// void testFirstKeyBreak() {
///     BreakableSortedMap<String, Integer> brokenMap = new BreakableSortedMap.Builder<String, Integer>()
///         .addBreak(FIRST_KEY_THROWS_EXCEPTION)
///         .build();
///     brokenMap.put("apple", 1);
///     assertThrows(NoSuchElementException.class, brokenMap::firstKey); // Break causes exception
/// }
/// ```
///
/// ### Sub-Map Behavior Testing
/// ```java
/// @Test
/// void testSubMapBreak() {
///     BreakableSortedMap<Integer, String> brokenMap = new BreakableSortedMap.Builder<Integer, String>()
///         .addBreak(HEAD_MAP_RETURNS_EMPTY_MAP)
///         .build();
///     brokenMap.put(1, "one");
///     brokenMap.put(2, "two");
///     SortedMap<Integer, String> headMap = brokenMap.headMap(2);
///     assertTrue(headMap.isEmpty()); // Empty due to break
/// }
/// ```
///
/// ## Quality Assurance
///
/// ### Test Isolation
/// Each test method is independent and doesn't rely on state from other tests.
/// Fresh BreakableSortedMap instances are created for each test scenario.
///
/// ### Error Condition Testing
/// Tests verify proper error handling for:
/// - Invalid comparator usage
/// - Boundary condition violations
/// - Sub-map creation failures
/// - Unsupported operations
/// - Break interaction edge cases
///
/// ### Ordering Validation
/// Tests specifically validate that sorted map ordering requirements are maintained
/// even when breaks are applied, ensuring that the fundamental sorted map contract
/// is preserved where possible.
///
/// @author evanbergstrom
/// @since 1.0.0
/// @see BreakableSortedMap
/// @see SortedMap
/// @see AbstractTest
public class BreakableSortedMapTest extends AbstractTest {

    // ========== Constructor Tests ==========

    /// Tests the default constructor functionality and initial state validation.
    ///
    /// Validates that the default BreakableSortedMap constructor creates an empty sorted map with the correct
    /// initial configuration:
    /// - Map should be empty (size 0, isEmpty returns true)
    /// - Should use natural ordering (comparator returns null)
    /// - Should permit null keys by default (permitsNullKeys returns true)
    /// - Should permit null values by default (permitsNullValues returns true)
    /// - Should maintain sorted order when elements are added
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableSortedMap using the default constructor
    /// - Verifies empty state and default configuration
    /// - Tests natural ordering behavior
    ///
    /// **Assertions:**
    /// - `assertTrue(map.isEmpty())` - Map should be empty
    /// - `assertEquals(0, map.size())` - Size should be zero
    /// - `assertNull(map.comparator())` - Should use natural ordering
    /// - `assertTrue(map.permitsNullKeys())` - Should permit null keys
    /// - `assertTrue(map.permitsNullValues())` - Should permit null values
    @Test
    @DisplayName("Test default constructor creates empty sorted map")
    public void testDefaultConstructor() {
        BreakableSortedMap<String, Integer> map = new BreakableSortedMap<>();

        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertNull(map.comparator()); // Natural ordering
        assertTrue(map.permitsNullKeys());
        assertTrue(map.permitsNullValues());
    }

    /// Tests the copy constructor behavior and configuration inheritance.
    ///
    /// Validates that the copy constructor creates a new BreakableSortedMap instance that properly
    /// inherits all configuration from the source map including breaks, null policies, and
    /// comparator while maintaining data independence.
    ///
    /// **Test Scenario:**
    /// - Creates an original sorted map with specific breaks and configuration
    /// - Uses copy constructor to create a new instance
    /// - Verifies configuration inheritance and data copying
    ///
    /// **Assertions:**
    /// - `assertNotSame(original, copy)` - Different object instances
    /// - `assertEquals(original.get("key1"), copy.get("key1"))` - Data copied correctly
    /// - `assertFalse(copy.permitsNullKeys())` - Null policy inherited
    /// - `assertThrows(NoSuchElementException.class, copy::firstKey)` - Breaks inherited and active
    @Test
    @DisplayName("Test copy constructor")
    public void testCopyConstructor() {
        BreakableSortedMap<String, Integer> original = new BreakableSortedMap.Builder<String, Integer>()
            .addBreak(FIRST_KEY_THROWS_EXCEPTION)
            .doesNotPermitNullKeys()
            .build();

        original.put("key1", 1);
        original.put("key2", 2);

        BreakableSortedMap<String, Integer> copy = new BreakableSortedMap<>(original);

        // Verify independent copies
        assertNotSame(original, copy);
        assertEquals(original.get("key1"), copy.get("key1"));
        assertEquals(original.get("key2"), copy.get("key2"));

        // Verify configuration is copied
        assertFalse(copy.permitsNullKeys());
        assertTrue(copy.permitsNullValues());

        // Verify breaks are copied
        assertThrows(NoSuchElementException.class, copy::firstKey);
    }

    /// Tests constructor with TreeMap integration and natural ordering.
    ///
    /// Validates that BreakableSortedMap properly integrates with TreeMap and maintains
    /// natural ordering behavior, ensuring compatibility with standard SortedMap implementations.
    ///
    /// **Test Scenario:**
    /// - Creates a TreeMap with initial data
    /// - Uses BreakableSortedMap constructor with the TreeMap
    /// - Verifies ordering and TreeMap integration
    ///
    /// **Assertions:**
    /// - `assertEquals("apple", map.firstKey())` - Proper natural ordering
    /// - `assertEquals("cherry", map.lastKey())` - Proper natural ordering
    /// - Ordering maintained after construction
    @Test
    @DisplayName("Test constructor with TreeMap")
    public void testConstructorWithTreeMap() {
        TreeMap<String, Integer> treeMap = new TreeMap<>();
        treeMap.put("banana", 2);
        treeMap.put("apple", 1);
        treeMap.put("cherry", 3);

        BreakableSortedMap<String, Integer> map = new BreakableSortedMap.Builder<>(treeMap).build();

        assertEquals("apple", map.firstKey());
        assertEquals("cherry", map.lastKey());
        assertEquals(3, map.size());
    }

    // ========== Builder Tests ==========

    /// Tests basic Builder pattern functionality and configuration transfer.
    ///
    /// Validates that the Builder correctly constructs BreakableSortedMap instances with specified
    /// breaks and null policies, ensuring all configuration is properly transferred from
    /// builder to final instance.
    ///
    /// **Test Scenario:**
    /// - Uses Builder to configure breaks and null policies
    /// - Builds the final BreakableSortedMap instance
    /// - Verifies break activation and policy enforcement
    ///
    /// **Assertions:**
    /// - `assertThrows(NoSuchElementException.class, map::firstKey)` - FIRST_KEY_THROWS_EXCEPTION break active
    /// - `assertNull(map.comparator())` - COMPARATOR_ALWAYS_RETURNS_NULL break active
    /// - `assertFalse(map.permitsNullKeys())` - Null key policy transferred
    /// - `assertTrue(map.permitsNullValues())` - Default null value policy maintained
    @Test
    @DisplayName("Test builder pattern")
    public void testBuilder() {
        BreakableSortedMap.Builder<String, Integer> builder = new BreakableSortedMap.Builder<>();
        builder.addBreak(FIRST_KEY_THROWS_EXCEPTION);
        builder.addBreak(COMPARATOR_ALWAYS_RETURNS_NULL);
        builder.doesNotPermitNullKeys();
        BreakableSortedMap<String, Integer> map = builder.build();

        map.put("key", 42);

        // Verify breaks are active
        assertThrows(NoSuchElementException.class, map::firstKey);
        assertNull(map.comparator());

        // Verify null policy
        assertFalse(map.permitsNullKeys());
        assertTrue(map.permitsNullValues());
    }

    /// Tests Builder creation with pre-existing SortedMap data.
    ///
    /// Validates that the Builder can be initialized with existing SortedMap data and that breaks
    /// are properly applied to the pre-existing data without affecting data integrity or ordering.
    ///
    /// **Test Scenario:**
    /// - Creates a TreeMap with initial data
    /// - Uses Builder constructor with existing map
    /// - Adds breaks and verifies their effect on existing data
    ///
    /// **Assertions:**
    /// - `assertEquals(Integer.valueOf(1), map.get("apple"))` - Original data preserved
    /// - `assertEquals(Integer.valueOf(2), map.get("banana"))` - Original data preserved
    /// - `assertTrue(map.headMap("banana").isEmpty())` - Break affects behavior on existing data
    @Test
    @DisplayName("Test builder with existing SortedMap")
    public void testBuilderWithExistingSortedMap() {
        TreeMap<String, Integer> existingMap = new TreeMap<>();
        existingMap.put("apple", 1);
        existingMap.put("banana", 2);
        existingMap.put("cherry", 3);

        BreakableSortedMap.Builder<String, Integer> builder = new BreakableSortedMap.Builder<>(existingMap);
        builder.addBreak(HEAD_MAP_RETURNS_EMPTY_MAP);
        BreakableSortedMap<String, Integer> map = builder.build();

        // Original data should be present
        assertEquals(Integer.valueOf(1), map.get("apple"));
        assertEquals(Integer.valueOf(2), map.get("banana"));
        assertEquals(Integer.valueOf(3), map.get("cherry"));

        // Break should be active
        assertTrue(map.headMap("banana").isEmpty()); // Due to break
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
    /// - `assertThrows(NoSuchElementException.class, originalMap::lastKey)` - Original has last key break
    /// - `assertEquals("apple", originalMap.firstKey())` - Original has normal firstKey
    /// - `assertThrows(NoSuchElementException.class, copyMap::firstKey)` - Copy has additional first key break
    @Test
    @DisplayName("Test builder copy")
    public void testBuilderCopy() {
        BreakableSortedMap.Builder<String, Integer> original = new BreakableSortedMap.Builder<>();
        original.addBreak(LAST_KEY_THROWS_EXCEPTION);
        original.doesNotPermitNullValues();

        BreakableSortedMap.Builder<String, Integer> copy = original.copy();

        // Modify copy
        copy.addBreak(FIRST_KEY_THROWS_EXCEPTION);

        BreakableSortedMap<String, Integer> originalMap = original.build();
        BreakableSortedMap<String, Integer> copyMap = copy.build();

        originalMap.put("apple", 1);
        originalMap.put("banana", 2);
        copyMap.put("cherry", 3);
        copyMap.put("date", 4);

        // Original should have last key break but normal firstKey
        assertThrows(NoSuchElementException.class, originalMap::lastKey);
        assertEquals("apple", originalMap.firstKey());

        // Copy should have both breaks
        assertThrows(NoSuchElementException.class, copyMap::lastKey);
        assertThrows(NoSuchElementException.class, copyMap::firstKey);
    }

    // ========== Comparator Break Tests ==========

    /// Tests the COMPARATOR_ALWAYS_RETURNS_NULL break functionality.
    ///
    /// Validates that when the COMPARATOR_ALWAYS_RETURNS_NULL break is active, the comparator()
    /// method consistently returns null regardless of whether the underlying SortedMap has a
    /// custom comparator configured.
    ///
    /// **Test Scenario:**
    /// - Creates a TreeMap with a custom comparator (reverse order)
    /// - Wraps it in a BreakableSortedMap with the COMPARATOR_ALWAYS_RETURNS_NULL break
    /// - Verifies that comparator() returns null despite having a custom comparator
    ///
    /// **Assertions:**
    /// - `assertNull(map.comparator())` despite underlying map having custom comparator
    /// - Map functionality remains intact with proper ordering
    @Test
    @DisplayName("Test COMPARATOR_ALWAYS_RETURNS_NULL break")
    public void testComparatorAlwaysReturnsNull() {
        BreakableSortedMap.Builder<String, Integer> builder = new BreakableSortedMap.Builder<>();
        BreakableSortedMap<String, Integer> map =  builder
                .addBreak(COMPARATOR_ALWAYS_RETURNS_NULL)
                .setComparator(Collections.reverseOrder())
                .add("apple", 1)
                .add("banana", 2)
                .build();

        assertNull(map.comparator()); // Returns null due to break
        // Underlying functionality should still work
        assertEquals("banana", map.firstKey()); // Still reverse ordered
    }

    /// Tests the COMPARATOR_THROWS_EXCEPTION break functionality.
    ///
    /// Validates that when the COMPARATOR_THROWS_EXCEPTION break is active, the comparator()
    /// method throws ClassCastException instead of returning the actual comparator.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableSortedMap with the COMPARATOR_THROWS_EXCEPTION break
    /// - Attempts to access the comparator and verifies exception is thrown
    ///
    /// **Assertions:**
    /// - `assertThrows(ClassCastException.class, map::comparator)` exception thrown due to break
    @Test
    @DisplayName("Test COMPARATOR_THROWS_EXCEPTION break")
    public void testComparatorThrowsException() {
        BreakableSortedMap.Builder<String, Integer> builder = new BreakableSortedMap.Builder<>();
        builder.addBreak(COMPARATOR_THROWS_EXCEPTION);
        BreakableSortedMap<String, Integer> map = builder.build();

        assertThrows(ClassCastException.class, map::comparator);
    }

    /// Tests that comparator() throws UnsupportedOperationException when method is not supported.
    ///
    /// Validates that when the comparator method is marked as unsupported using the Builder's
    /// doesNotSupport configuration, the method throws UnsupportedOperationException regardless
    /// of any breaks that might be configured.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableSortedMap with comparator marked as unsupported
    /// - Configures a comparator-related break
    /// - Attempts to call comparator() and verifies exception is thrown
    ///
    /// **Assertions:**
    /// - `assertThrows(UnsupportedOperationException.class, map::comparator)`
    @Test
    @DisplayName("Test comparator() throws when not supported")
    public void testComparatorWhenNotSupported() {
        BreakableSortedMap.Builder<String, Integer> builder = new BreakableSortedMap.Builder<>();
        builder.addBreak(COMPARATOR_ALWAYS_RETURNS_NULL);
        builder.doesNotSupport(SortedMapMethods.COMPARATOR);
        BreakableSortedMap<String, Integer> map = builder.build();

        assertThrows(UnsupportedOperationException.class, map::comparator);
    }

    // ========== First Key Break Tests ==========

    /// Tests the FIRST_KEY_THROWS_EXCEPTION break functionality.
    ///
    /// Validates that when the FIRST_KEY_THROWS_EXCEPTION break is active, the firstKey()
    /// method throws NoSuchElementException even when the map contains elements.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableSortedMap with the FIRST_KEY_THROWS_EXCEPTION break
    /// - Adds elements to the map
    /// - Verifies that firstKey() throws exception despite containing elements
    ///
    /// **Assertions:**
    /// - `assertThrows(NoSuchElementException.class, map::firstKey)` exception thrown due to break
    /// - `assertFalse(map.isEmpty())` map actually contains elements
    @Test
    @DisplayName("Test FIRST_KEY_THROWS_EXCEPTION break")
    public void testFirstKeyThrowsException() {
        BreakableSortedMap.Builder<String, Integer> builder = new BreakableSortedMap.Builder<>();
        builder.addBreak(FIRST_KEY_THROWS_EXCEPTION);
        BreakableSortedMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);

        assertThrows(NoSuchElementException.class, map::firstKey);
        assertFalse(map.isEmpty()); // Map has elements but firstKey throws
    }

    /// Tests the FIRST_KEY_RETURNS_RANDOM_KEY break functionality.
    ///
    /// Validates that when the FIRST_KEY_RETURNS_RANDOM_KEY break is active, the firstKey()
    /// method returns an arbitrary key from the map instead of the actual first key in order.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableSortedMap with the FIRST_KEY_RETURNS_RANDOM_KEY break
    /// - Adds elements with known ordering
    /// - Verifies that firstKey() returns a key that exists but may not be the actual first
    ///
    /// **Assertions:**
    /// - `assertTrue(map.containsKey(firstKey))` returned key exists in map
    /// - May or may not be the actual first key (random behavior)
    @Test
    @DisplayName("Test FIRST_KEY_RETURNS_RANDOM_KEY break")
    public void testFirstKeyReturnsRandomKey() {
        BreakableSortedMap.Builder<String, Integer> builder = new BreakableSortedMap.Builder<>();
        builder.addBreak(FIRST_KEY_RETURNS_RANDOM_KEY);
        BreakableSortedMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);
        map.put("cherry", 3);

        String firstKey = map.firstKey();
        // Should return one of the keys (but not necessarily the first)
        assertTrue(map.containsKey(firstKey));
    }

    /// Tests that firstKey() throws UnsupportedOperationException when method is not supported.
    ///
    /// Validates that when the firstKey method is marked as unsupported using the Builder's
    /// doesNotSupport configuration, the method throws UnsupportedOperationException regardless
    /// of any breaks that might be configured.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableSortedMap with firstKey marked as unsupported
    /// - Configures firstKey-related breaks
    /// - Attempts to call firstKey() and verifies exception is thrown
    ///
    /// **Assertions:**
    /// - `assertThrows(UnsupportedOperationException.class, map::firstKey)`
    @Test
    @DisplayName("Test firstKey() throws when not supported")
    public void testFirstKeyWhenNotSupported() {
        BreakableSortedMap.Builder<String, Integer> builder = new BreakableSortedMap.Builder<>();
        builder.addBreak(FIRST_KEY_THROWS_EXCEPTION);
        builder.addBreak(FIRST_KEY_RETURNS_RANDOM_KEY);
        builder.doesNotSupport(SortedMapMethods.FIRST_KEY);
        BreakableSortedMap<String, Integer> map = builder.build();

        assertThrows(UnsupportedOperationException.class, map::firstKey);
    }

    // ========== Last Key Break Tests ==========

    /// Tests the LAST_KEY_THROWS_EXCEPTION break functionality.
    ///
    /// Validates that when the LAST_KEY_THROWS_EXCEPTION break is active, the lastKey()
    /// method throws NoSuchElementException even when the map contains elements.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableSortedMap with the LAST_KEY_THROWS_EXCEPTION break
    /// - Adds elements to the map
    /// - Verifies that lastKey() throws exception despite containing elements
    ///
    /// **Assertions:**
    /// - `assertThrows(NoSuchElementException.class, map::lastKey)` exception thrown due to break
    /// - `assertFalse(map.isEmpty())` map actually contains elements
    @Test
    @DisplayName("Test LAST_KEY_THROWS_EXCEPTION break")
    public void testLastKeyThrowsException() {
        BreakableSortedMap.Builder<String, Integer> builder = new BreakableSortedMap.Builder<>();
        builder.addBreak(LAST_KEY_THROWS_EXCEPTION);
        BreakableSortedMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);

        assertThrows(NoSuchElementException.class, map::lastKey);
        assertFalse(map.isEmpty()); // Map has elements but lastKey throws
    }

    /// Tests the LAST_KEY_RETURNS_RANDOM_KEY break functionality.
    ///
    /// Validates that when the LAST_KEY_RETURNS_RANDOM_KEY break is active, the lastKey()
    /// method returns an arbitrary key from the map instead of the actual last key in order.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableSortedMap with the LAST_KEY_RETURNS_RANDOM_KEY break
    /// - Adds elements with known ordering
    /// - Verifies that lastKey() returns a key that exists but may not be the actual last
    ///
    /// **Assertions:**
    /// - `assertTrue(map.containsKey(lastKey))` returned key exists in map
    /// - May or may not be the actual last key (random behavior)
    @Test
    @DisplayName("Test LAST_KEY_RETURNS_RANDOM_KEY break")
    public void testLastKeyReturnsRandomKey() {
        BreakableSortedMap.Builder<String, Integer> builder = new BreakableSortedMap.Builder<>();
        builder.addBreak(LAST_KEY_RETURNS_RANDOM_KEY);
        BreakableSortedMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);
        map.put("cherry", 3);

        String lastKey = map.lastKey();
        // Should return one of the keys (but not necessarily the last)
        assertTrue(map.containsKey(lastKey));
    }

    /// Tests that lastKey() throws UnsupportedOperationException when method is not supported.
    ///
    /// Validates that when the lastKey method is marked as unsupported using the Builder's
    /// doesNotSupport configuration, the method throws UnsupportedOperationException regardless
    /// of any breaks that might be configured.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableSortedMap with lastKey marked as unsupported
    /// - Configures lastKey-related breaks
    /// - Attempts to call lastKey() and verifies exception is thrown
    ///
    /// **Assertions:**
    /// - `assertThrows(UnsupportedOperationException.class, map::lastKey)`
    @Test
    @DisplayName("Test lastKey() throws when not supported")
    public void testLastKeyWhenNotSupported() {
        BreakableSortedMap.Builder<String, Integer> builder = new BreakableSortedMap.Builder<>();
        builder.addBreak(LAST_KEY_THROWS_EXCEPTION);
        builder.addBreak(LAST_KEY_RETURNS_RANDOM_KEY);
        builder.doesNotSupport(SortedMapMethods.LAST_KEY);
        BreakableSortedMap<String, Integer> map = builder.build();

        assertThrows(UnsupportedOperationException.class, map::lastKey);
    }

    // ========== Head Map Break Tests ==========

    /// Tests the HEAD_MAP_RETURNS_EMPTY_MAP break functionality.
    ///
    /// Validates that when the HEAD_MAP_RETURNS_EMPTY_MAP break is active, the headMap()
    /// method returns an empty map regardless of the actual content that should be included.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableSortedMap with the HEAD_MAP_RETURNS_EMPTY_MAP break
    /// - Adds multiple elements that would normally be included in headMap
    /// - Verifies that headMap() returns empty map despite containing qualifying elements
    ///
    /// **Assertions:**
    /// - `assertTrue(headMap.isEmpty())` despite map containing elements before toKey
    /// - `assertEquals(3, map.size())` original map still has data
    @Test
    @DisplayName("Test HEAD_MAP_RETURNS_EMPTY_MAP break")
    public void testHeadMapReturnsEmptyMap() {
        BreakableSortedMap.Builder<String, Integer> builder = new BreakableSortedMap.Builder<>();
        builder.addBreak(HEAD_MAP_RETURNS_EMPTY_MAP);
        BreakableSortedMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);
        map.put("cherry", 3);

        SortedMap<String, Integer> headMap = map.headMap("cherry");
        assertTrue(headMap.isEmpty()); // Empty due to break
        assertEquals(3, map.size()); // Original map still has data
    }

    /// Tests the HEAD_MAP_RETURNS_NULL break functionality.
    ///
    /// Validates that when the HEAD_MAP_RETURNS_NULL break is active, the headMap()
    /// method returns null instead of a valid SortedMap.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableSortedMap with the HEAD_MAP_RETURNS_NULL break
    /// - Calls headMap() and verifies null return
    ///
    /// **Assertions:**
    /// - `assertNull(map.headMap("banana"))` returns null due to break
    @Test
    @DisplayName("Test HEAD_MAP_RETURNS_NULL break")
    public void testHeadMapReturnsNull() {
        BreakableSortedMap.Builder<String, Integer> builder = new BreakableSortedMap.Builder<>();
        builder.addBreak(HEAD_MAP_RETURNS_NULL);
        BreakableSortedMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);

        assertNull(map.headMap("banana"));
    }

    /// Tests the HEAD_MAP_THROWS_EXCEPTION break functionality.
    ///
    /// Validates that when the HEAD_MAP_THROWS_EXCEPTION break is active, the headMap()
    /// method throws IllegalArgumentException regardless of parameter validity.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableSortedMap with the HEAD_MAP_THROWS_EXCEPTION break
    /// - Calls headMap() with valid parameter and verifies exception is thrown
    ///
    /// **Assertions:**
    /// - `assertThrows(IllegalArgumentException.class, () -> map.headMap("banana"))` exception due to break
    @Test
    @DisplayName("Test HEAD_MAP_THROWS_EXCEPTION break")
    public void testHeadMapThrowsException() {
        BreakableSortedMap.Builder<String, Integer> builder = new BreakableSortedMap.Builder<>();
        builder.addBreak(HEAD_MAP_THROWS_EXCEPTION);
        BreakableSortedMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);

        assertThrows(IllegalArgumentException.class, () -> map.headMap("banana"));
    }

    /// Tests that headMap() throws UnsupportedOperationException when method is not supported.
    ///
    /// Validates that when the headMap method is marked as unsupported using the Builder's
    /// doesNotSupport configuration, the method throws UnsupportedOperationException regardless
    /// of any breaks that might be configured.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableSortedMap with headMap marked as unsupported
    /// - Configures headMap-related breaks
    /// - Attempts to call headMap() and verifies exception is thrown
    ///
    /// **Assertions:**
    /// - `assertThrows(UnsupportedOperationException.class, () -> map.headMap("banana"))`
    @Test
    @DisplayName("Test headMap() throws when not supported")
    public void testHeadMapWhenNotSupported() {
        BreakableSortedMap.Builder<String, Integer> builder = new BreakableSortedMap.Builder<>();
        builder.addBreak(HEAD_MAP_RETURNS_EMPTY_MAP);
        builder.addBreak(HEAD_MAP_RETURNS_NULL);
        builder.addBreak(HEAD_MAP_THROWS_EXCEPTION);
        builder.doesNotSupport(SortedMapMethods.HEAD_MAP);
        BreakableSortedMap<String, Integer> map = builder.build();

        assertThrows(UnsupportedOperationException.class, () -> map.headMap("banana"));
    }

    // ========== Tail Map Break Tests ==========

    /// Tests the TAIL_MAP_RETURNS_EMPTY_MAP break functionality.
    ///
    /// Validates that when the TAIL_MAP_RETURNS_EMPTY_MAP break is active, the tailMap()
    /// method returns an empty map regardless of the actual content that should be included.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableSortedMap with the TAIL_MAP_RETURNS_EMPTY_MAP break
    /// - Adds multiple elements that would normally be included in tailMap
    /// - Verifies that tailMap() returns empty map despite containing qualifying elements
    ///
    /// **Assertions:**
    /// - `assertTrue(tailMap.isEmpty())` despite map containing elements from fromKey
    /// - `assertEquals(3, map.size())` original map still has data
    @Test
    @DisplayName("Test TAIL_MAP_RETURNS_EMPTY_MAP break")
    public void testTailMapReturnsEmptyMap() {
        BreakableSortedMap.Builder<String, Integer> builder = new BreakableSortedMap.Builder<>();
        builder.addBreak(TAIL_MAP_RETURNS_EMPTY_MAP);
        BreakableSortedMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);
        map.put("cherry", 3);

        SortedMap<String, Integer> tailMap = map.tailMap("banana");
        assertTrue(tailMap.isEmpty()); // Empty due to break
        assertEquals(3, map.size()); // Original map still has data
    }

    /// Tests the TAIL_MAP_RETURNS_NULL break functionality.
    ///
    /// Validates that when the TAIL_MAP_RETURNS_NULL break is active, the tailMap()
    /// method returns null instead of a valid SortedMap.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableSortedMap with the TAIL_MAP_RETURNS_NULL break
    /// - Calls tailMap() and verifies null return
    ///
    /// **Assertions:**
    /// - `assertNull(map.tailMap("banana"))` returns null due to break
    @Test
    @DisplayName("Test TAIL_MAP_RETURNS_NULL break")
    public void testTailMapReturnsNull() {
        BreakableSortedMap.Builder<String, Integer> builder = new BreakableSortedMap.Builder<>();
        builder.addBreak(TAIL_MAP_RETURNS_NULL);
        BreakableSortedMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);

        assertNull(map.tailMap("banana"));
    }

    /// Tests the TAIL_MAP_THROWS_EXCEPTION break functionality.
    ///
    /// Validates that when the TAIL_MAP_THROWS_EXCEPTION break is active, the tailMap()
    /// method throws IllegalArgumentException regardless of parameter validity.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableSortedMap with the TAIL_MAP_THROWS_EXCEPTION break
    /// - Calls tailMap() with valid parameter and verifies exception is thrown
    ///
    /// **Assertions:**
    /// - `assertThrows(IllegalArgumentException.class, () -> map.tailMap("banana"))` exception due to break
    @Test
    @DisplayName("Test TAIL_MAP_THROWS_EXCEPTION break")
    public void testTailMapThrowsException() {
        BreakableSortedMap.Builder<String, Integer> builder = new BreakableSortedMap.Builder<>();
        builder.addBreak(TAIL_MAP_THROWS_EXCEPTION);
        BreakableSortedMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);

        assertThrows(IllegalArgumentException.class, () -> map.tailMap("banana"));
    }

    /// Tests that tailMap() throws UnsupportedOperationException when method is not supported.
    ///
    /// Validates that when the tailMap method is marked as unsupported using the Builder's
    /// doesNotSupport configuration, the method throws UnsupportedOperationException regardless
    /// of any breaks that might be configured.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableSortedMap with tailMap marked as unsupported
    /// - Configures tailMap-related breaks
    /// - Attempts to call tailMap() and verifies exception is thrown
    ///
    /// **Assertions:**
    /// - `assertThrows(UnsupportedOperationException.class, () -> map.tailMap("banana"))`
    @Test
    @DisplayName("Test tailMap() throws when not supported")
    public void testTailMapWhenNotSupported() {
        BreakableSortedMap.Builder<String, Integer> builder = new BreakableSortedMap.Builder<>();
        builder.addBreak(TAIL_MAP_RETURNS_EMPTY_MAP);
        builder.addBreak(TAIL_MAP_RETURNS_NULL);
        builder.addBreak(TAIL_MAP_THROWS_EXCEPTION);
        builder.doesNotSupport(SortedMapMethods.TAIL_MAP);
        BreakableSortedMap<String, Integer> map = builder.build();

        assertThrows(UnsupportedOperationException.class, () -> map.tailMap("banana"));
    }

    // ========== Sub Map Break Tests ==========

    /// Tests the SUB_MAP_RETURNS_EMPTY_MAP break functionality.
    ///
    /// Validates that when the SUB_MAP_RETURNS_EMPTY_MAP break is active, the subMap()
    /// method returns an empty map regardless of the actual content that should be included.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableSortedMap with the SUB_MAP_RETURNS_EMPTY_MAP break
    /// - Adds multiple elements that would normally be included in subMap
    /// - Verifies that subMap() returns empty map despite containing qualifying elements
    ///
    /// **Assertions:**
    /// - `assertTrue(subMap.isEmpty())` despite map containing elements in range
    /// - `assertEquals(4, map.size())` original map still has data
    @Test
    @DisplayName("Test SUB_MAP_RETURNS_EMPTY_MAP break")
    public void testSubMapReturnsEmptyMap() {
        BreakableSortedMap.Builder<String, Integer> builder = new BreakableSortedMap.Builder<>();
        builder.addBreak(SUB_MAP_RETURNS_EMPTY_MAP);
        BreakableSortedMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);
        map.put("cherry", 3);
        map.put("date", 4);

        SortedMap<String, Integer> subMap = map.subMap("banana", "date");
        assertTrue(subMap.isEmpty()); // Empty due to break
        assertEquals(4, map.size()); // Original map still has data
    }

    /// Tests the SUB_MAP_RETURNS_NULL break functionality.
    ///
    /// Validates that when the SUB_MAP_RETURNS_NULL break is active, the subMap()
    /// method returns null instead of a valid SortedMap.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableSortedMap with the SUB_MAP_RETURNS_NULL break
    /// - Calls subMap() and verifies null return
    ///
    /// **Assertions:**
    /// - `assertNull(map.subMap("apple", "cherry"))` returns null due to break
    @Test
    @DisplayName("Test SUB_MAP_RETURNS_NULL break")
    public void testSubMapReturnsNull() {
        BreakableSortedMap.Builder<String, Integer> builder = new BreakableSortedMap.Builder<>();
        builder.addBreak(SUB_MAP_RETURNS_NULL);
        BreakableSortedMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);
        map.put("cherry", 3);

        assertNull(map.subMap("apple", "cherry"));
    }

    /// Tests the SUB_MAP_THROWS_EXCEPTION break functionality.
    ///
    /// Validates that when the SUB_MAP_THROWS_EXCEPTION break is active, the subMap()
    /// method throws IllegalArgumentException regardless of parameter validity.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableSortedMap with the SUB_MAP_THROWS_EXCEPTION break
    /// - Calls subMap() with valid parameters and verifies exception is thrown
    ///
    /// **Assertions:**
    /// - `assertThrows(IllegalArgumentException.class, () -> map.subMap("apple", "cherry"))` exception due to break
    @Test
    @DisplayName("Test SUB_MAP_THROWS_EXCEPTION break")
    public void testSubMapThrowsException() {
        BreakableSortedMap.Builder<String, Integer> builder = new BreakableSortedMap.Builder<>();
        builder.addBreak(SUB_MAP_THROWS_EXCEPTION);
        BreakableSortedMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);
        map.put("cherry", 3);

        assertThrows(IllegalArgumentException.class, () -> map.subMap("apple", "cherry"));
    }

    /// Tests that subMap() throws UnsupportedOperationException when method is not supported.
    ///
    /// Validates that when the subMap method is marked as unsupported using the Builder's
    /// doesNotSupport configuration, the method throws UnsupportedOperationException regardless
    /// of any breaks that might be configured.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableSortedMap with subMap marked as unsupported
    /// - Configures subMap-related breaks
    /// - Attempts to call subMap() and verifies exception is thrown
    ///
    /// **Assertions:**
    /// - `assertThrows(UnsupportedOperationException.class, () -> map.subMap("apple", "cherry"))`
    @Test
    @DisplayName("Test subMap() throws when not supported")
    public void testSubMapWhenNotSupported() {
        BreakableSortedMap.Builder<String, Integer> builder = new BreakableSortedMap.Builder<>();
        builder.addBreak(SUB_MAP_RETURNS_EMPTY_MAP);
        builder.addBreak(SUB_MAP_RETURNS_NULL);
        builder.addBreak(SUB_MAP_THROWS_EXCEPTION);
        builder.doesNotSupport(SortedMapMethods.SUB_MAP);
        BreakableSortedMap<String, Integer> map = builder.build();

        assertThrows(UnsupportedOperationException.class, () -> map.subMap("apple", "cherry"));
    }

    // ========== Static Factory Method Tests ==========

    /// Tests the static wrap factory method functionality.
    ///
    /// Validates that the BreakableSortedMap.wrap() factory method correctly creates a BreakableSortedMap
    /// instance that wraps an existing SortedMap with specified breaks while preserving the
    /// original data, ordering, and applying the configured behavioral modifications.
    ///
    /// **Test Scenario:**
    /// - Creates a TreeMap with initial data
    /// - Uses wrap() factory method with specific breaks
    /// - Verifies that breaks are active and data/ordering is preserved
    ///
    /// **Assertions:**
    /// - `assertTrue(wrappedMap.headMap("banana").isEmpty())` - HEAD_MAP_RETURNS_EMPTY_MAP break active
    /// - `assertThrows(NoSuchElementException.class, wrappedMap::firstKey)` - FIRST_KEY_THROWS_EXCEPTION break active
    /// - `assertEquals("banana", wrappedMap.lastKey())` - Data and ordering preserved
    /// - `assertTrue(wrappedMap.permitsNullKeys())` and `assertTrue(wrappedMap.permitsNullValues())` - Default policies
    @Test
    @DisplayName("Test wrap factory method")
    public void testWrapFactoryMethod() {
        SortedMap<String, Integer> existingMap = new TreeMap<>();
        existingMap.put("apple", 1);
        existingMap.put("banana", 2);

        Set<Break> breaks = Set.of(HEAD_MAP_RETURNS_EMPTY_MAP, FIRST_KEY_THROWS_EXCEPTION);

        BreakableSortedMap<String, Integer> wrappedMap = BreakableSortedMap.wrap(existingMap, breaks);

        // Verify breaks are active
        assertTrue(wrappedMap.headMap("banana").isEmpty());
        assertThrows(NoSuchElementException.class, wrappedMap::firstKey);

        // Verify original data is preserved (can be accessed through other methods)
        assertEquals("banana", wrappedMap.lastKey());
        assertTrue(wrappedMap.containsKey("apple"));
        assertTrue(wrappedMap.containsKey("banana"));

        // Verify null policies default to permissive
        assertTrue(wrappedMap.permitsNullKeys());
        assertTrue(wrappedMap.permitsNullValues());
    }

    // ========== Ordering and SortedMap Behavior Tests ==========

    /// Tests that BreakableSortedMap maintains proper ordering.
    ///
    /// Validates that BreakableSortedMap preserves the ordering characteristics of the underlying
    /// SortedMap even when breaks are applied to other methods. This ensures that the fundamental
    /// sorted map contract is maintained.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableSortedMap with various breaks
    /// - Adds elements in random order
    /// - Verifies that ordering is maintained in iteration and key access
    ///
    /// **Assertions:**
    /// - Keys appear in sorted order during iteration
    /// - Natural ordering is preserved despite breaks
    @Test
    @DisplayName("Test ordering is maintained with breaks")
    public void testOrderingMaintained() {
        BreakableSortedMap.Builder<String, Integer> builder = new BreakableSortedMap.Builder<>();
        builder.addBreak(HEAD_MAP_RETURNS_EMPTY_MAP); // This shouldn't affect ordering
        BreakableSortedMap<String, Integer> map = builder.build();

        // Add elements in non-alphabetical order
        map.put("zebra", 26);
        map.put("apple", 1);
        map.put("banana", 2);
        map.put("cherry", 3);

        // Verify ordering in keySet iteration
        List<String> keys = new ArrayList<>(map.keySet());
        assertEquals(Arrays.asList("apple", "banana", "cherry", "zebra"), keys);

        // Verify firstKey and lastKey when not broken
        assertEquals("apple", map.firstKey());
        assertEquals("zebra", map.lastKey());
    }

    /// Tests BreakableSortedMap with custom comparator.
    ///
    /// Validates that BreakableSortedMap properly works with custom comparators and maintains
    /// the custom ordering even when breaks are applied to methods other than the comparator.
    ///
    /// **Test Scenario:**
    /// - Creates a TreeMap with reverse order comparator
    /// - Wraps it in BreakableSortedMap with non-comparator breaks
    /// - Verifies that custom ordering is preserved
    ///
    /// **Assertions:**
    /// - Reverse ordering is maintained
    /// - Comparator is accessible when not broken
    /// - Custom ordering affects firstKey/lastKey behavior
    @Test
    @DisplayName("Test custom comparator behavior")
    public void testCustomComparator() {
        BreakableSortedMap.Builder<String, Integer> builder = new BreakableSortedMap.Builder<>();
        BreakableSortedMap<String, Integer> map = builder
                .addBreak(SUB_MAP_RETURNS_EMPTY_MAP) // Non-comparator break
                .setComparator(Comparator.reverseOrder())
                .add("apple", 1)
                .add("banana", 2)
                .add("cherry", 3)
                .build();

        // Verify custom comparator is preserved
        assertNotNull(map.comparator());
        assertEquals(Collections.reverseOrder(), map.comparator());

        // Verify reverse ordering
        assertEquals("cherry", map.firstKey()); // First in reverse order
        assertEquals("apple", map.lastKey());   // Last in reverse order

        // Verify ordering in iteration
        List<String> keys = new ArrayList<>(map.keySet());
        assertEquals(Arrays.asList("cherry", "banana", "apple"), keys);
    }

    // ========== Inheritance and Map Interface Tests ==========

    /// Tests that BreakableSortedMap properly inherits Map functionality.
    ///
    /// Validates that BreakableSortedMap correctly extends BreakableMap and inherits all
    /// Map interface functionality while adding SortedMap-specific behavior.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableSortedMap and tests basic Map operations
    /// - Applies both Map-level and SortedMap-level breaks
    /// - Verifies that both types of functionality work correctly
    ///
    /// **Assertions:**
    /// - Map operations work normally
    /// - SortedMap operations work with breaks
    /// - Inheritance hierarchy is properly maintained
    @Test
    @DisplayName("Test inheritance from BreakableMap")
    public void testMapInheritance() {
        BreakableSortedMap.Builder<String, Integer> builder = new BreakableSortedMap.Builder<>();
        builder.addBreak(BreakableMap.GET_ALWAYS_RETURNS_NULL);     // Map-level break
        builder.addBreak(FIRST_KEY_THROWS_EXCEPTION);               // SortedMap-level break
        BreakableSortedMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);

        // Map-level functionality with break
        assertNull(map.get("apple")); // Due to GET_ALWAYS_RETURNS_NULL break
        assertEquals(2, map.size());   // Size should work normally

        // SortedMap-level functionality with break
        assertThrows(NoSuchElementException.class, map::firstKey); // Due to FIRST_KEY_THROWS_EXCEPTION break
        assertEquals("banana", map.lastKey()); // Should work normally
    }
}