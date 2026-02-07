package org.soliscode.test.breakable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.navigablemap.NavigableMapMethods;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.breakable.BreakableNavigableMap.*;

/// **Test Suite for BreakableNavigableMap Implementation**
///
/// This comprehensive test class validates the behavior of BreakableNavigableMap, focusing on both
/// standard NavigableMap contract compliance and the controlled violation of navigable map semantics through
/// programmatic breaks. The tests ensure that BreakableNavigableMap maintains proper navigable map behavior
/// under normal conditions while correctly implementing break mechanisms for testing purposes.
///
/// ## Test Coverage
///
/// ### NavigableMap Contract Compliance Testing
/// - **Map Interface**: Full validation of inherited Map interface methods
/// - **SortedMap Interface**: Full validation of inherited SortedMap interface methods
/// - **NavigableMap Interface**: Full validation of NavigableMap-specific methods
/// - **Navigation Operations**: Lower, floor, ceiling, higher key/entry operations
/// - **Entry Access**: First/last entry operations and polling
/// - **View Operations**: Descending maps, navigable key sets, descending key sets
/// - **Sub-Map Operations**: Enhanced headMap, tailMap, subMap functionality
///
/// ### Break Mechanism Testing
/// - **Navigation Breaks**: LOWER_*, FLOOR_*, CEILING_*, HIGHER_* breaks
/// - **Entry Access Breaks**: FIRST_ENTRY_*, LAST_ENTRY_*, POLL_*_ENTRY_* breaks
/// - **View Breaks**: DESCENDING_MAP_*, NAVIGABLE_KEY_SET_*, DESCENDING_KEY_SET_* breaks
/// - **Enhanced Sub-Map Breaks**: *_TWO_ARG_*, *_FOUR_ARG_* variants
/// - **Exception Breaks**: Various exception-throwing behaviors
/// - **Return Value Breaks**: Null returns and empty collection behaviors
///
/// ### Builder Pattern Testing
/// - **NavigableMap-Specific Configuration**: Builder setup with NavigableMap parameters
/// - **Copy Semantics**: Builder copying and independence for navigable maps
/// - **Fluent Interface**: Method chaining with navigable map configuration
/// - **Pre-populated Maps**: Building from existing TreeMaps and other NavigableMap implementations
///
/// ### Constructor Testing
/// - **Default Construction**: Empty navigable map creation with natural ordering
/// - **Copy Construction**: Creating navigable maps from existing BreakableNavigableMap instances
/// - **NavigableMap Construction**: Building from existing NavigableMap implementations
/// - **Comparator Inheritance**: Proper comparator handling in construction
///
/// ## Test Architecture
///
/// This test class follows the SolisCode testing framework patterns:
/// - **AbstractTest Extension**: Inherits common testing infrastructure
/// - **NavigableMap-Specific Testing**: Focused on NavigableMap interface compliance
/// - **Break Isolation**: Each break is tested independently for navigable map operations
/// - **State Validation**: Verifies navigable map state consistency with navigation guarantees
///
/// @author evanbergstrom
/// @since 1.0.0
/// @see BreakableNavigableMap
/// @see NavigableMap
/// @see AbstractTest
public class BreakableNavigableMapTest extends AbstractTest {

    // ========== Constructor Tests ==========

    /// Tests the default constructor functionality and initial state validation.
    @Test
    @DisplayName("Test default constructor creates empty navigable map")
    public void testDefaultConstructor() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap<>();

        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertNull(map.comparator()); // Natural ordering
        assertTrue(map.permitsNullKeys());
        assertTrue(map.permitsNullValues());
    }

    /// Tests the copy constructor behavior and configuration inheritance.
    @Test
    @DisplayName("Test copy constructor")
    public void testCopyConstructor() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(LOWER_ENTRY_THROWS_EXCEPTION);
        builder.doesNotPermitNullKeys();
        BreakableNavigableMap<String, Integer> original = builder.build();

        original.put("key1", 1);
        original.put("key2", 2);

        BreakableNavigableMap<String, Integer> copy = new BreakableNavigableMap<>(original);

        // Verify independent copies
        assertNotSame(original, copy);
        assertEquals(original.get("key1"), copy.get("key1"));
        assertEquals(original.get("key2"), copy.get("key2"));

        // Verify configuration is copied
        assertFalse(copy.permitsNullKeys());
        assertTrue(copy.permitsNullValues());

        // Verify breaks are copied
        assertThrows(NoSuchElementException.class, () -> copy.lowerEntry("key2"));
    }

    /// Tests constructor with TreeMap integration and natural ordering.
    @Test
    @DisplayName("Test constructor with TreeMap")
    public void testConstructorWithTreeMap() {
        TreeMap<String, Integer> treeMap = new TreeMap<>();
        treeMap.put("banana", 2);
        treeMap.put("apple", 1);
        treeMap.put("cherry", 3);

        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<>(treeMap).build();

        assertEquals("apple", map.firstKey());
        assertEquals("cherry", map.lastKey());
        assertEquals(3, map.size());
    }

    // ========== Builder Tests ==========

    /// Tests basic Builder pattern functionality and configuration transfer.
    @Test
    @DisplayName("Test builder pattern")
    public void testBuilder() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(HIGHER_KEY_ALWAYS_RETURNS_NULL);
        builder.addBreak(DESCENDING_MAP_RETURNS_EMPTY_MAP);
        builder.doesNotPermitNullKeys();
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("key", 42);

        // Verify breaks are active
        assertNull(map.higherKey("key"));
        assertTrue(map.descendingMap().isEmpty());

        // Verify null policy
        assertFalse(map.permitsNullKeys());
        assertTrue(map.permitsNullValues());
    }

    /// Tests Builder creation with pre-existing NavigableMap data.
    @Test
    @DisplayName("Test builder with existing NavigableMap")
    public void testBuilderWithExistingNavigableMap() {
        TreeMap<String, Integer> existingMap = new TreeMap<>();
        existingMap.put("apple", 1);
        existingMap.put("banana", 2);
        existingMap.put("cherry", 3);

        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>(existingMap);
        builder.addBreak(LOWER_KEY_ALWAYS_RETURNS_NULL);
        BreakableNavigableMap<String, Integer> map = builder.build();

        // Original data should be present
        assertEquals(Integer.valueOf(1), map.get("apple"));
        assertEquals(Integer.valueOf(2), map.get("banana"));
        assertEquals(Integer.valueOf(3), map.get("cherry"));

        // Break should be active
        assertNull(map.lowerKey("banana")); // Due to break
    }

    /// Tests Builder copy functionality and configuration inheritance.
    @Test
    @DisplayName("Test builder copy")
    public void testBuilderCopy() {
        BreakableNavigableMap.Builder<String, Integer> original = new BreakableNavigableMap.Builder<>();
        original.addBreak(FIRST_ENTRY_THROWS_EXCEPTION);
        original.doesNotPermitNullValues();

        BreakableNavigableMap.Builder<String, Integer> copy = original.copy();

        // Modify copy
        copy.addBreak(LAST_ENTRY_THROWS_EXCEPTION);

        BreakableNavigableMap<String, Integer> originalMap = original.build();
        BreakableNavigableMap<String, Integer> copyMap = copy.build();

        originalMap.put("apple", 1);
        originalMap.put("banana", 2);
        copyMap.put("cherry", 3);
        copyMap.put("date", 4);

        // Original should have first entry break but normal lastEntry
        assertThrows(NoSuchElementException.class, originalMap::firstEntry);
        assertNotNull(originalMap.lastEntry());

        // Copy should have both breaks
        assertThrows(NoSuchElementException.class, copyMap::firstEntry);
        assertThrows(NoSuchElementException.class, copyMap::lastEntry);
    }

    // ========== Navigation Entry Break Tests ==========

    /// Tests the LOWER_ENTRY_ALWAYS_RETURNS_NULL break functionality.
    @Test
    @DisplayName("Test LOWER_ENTRY_ALWAYS_RETURNS_NULL break")
    public void testLowerEntryAlwaysReturnsNull() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(LOWER_ENTRY_ALWAYS_RETURNS_NULL);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);
        map.put("cherry", 3);

        assertNull(map.lowerEntry("cherry")); // Should normally return banana entry
        assertNull(map.lowerEntry("banana")); // Should normally return apple entry
    }

    /// Tests the LOWER_ENTRY_THROWS_EXCEPTION break functionality.
    @Test
    @DisplayName("Test LOWER_ENTRY_THROWS_EXCEPTION break")
    public void testLowerEntryThrowsException() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(LOWER_ENTRY_THROWS_EXCEPTION);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);

        assertThrows(NoSuchElementException.class, () -> map.lowerEntry("banana"));
    }

    /// Tests the LOWER_ENTRY_RETURNS_RANDOM_ENTRY break functionality.
    @Test
    @DisplayName("Test LOWER_ENTRY_RETURNS_RANDOM_ENTRY break")
    public void testLowerEntryReturnsRandomEntry() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(LOWER_ENTRY_RETURNS_RANDOM_ENTRY);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);
        map.put("cherry", 3);

        Map.Entry<String, Integer> entry = map.lowerEntry("cherry");
        assertNotNull(entry);
        assertTrue(map.containsKey(entry.getKey()));
    }

    /// Tests that lowerEntry() throws UnsupportedOperationException when method is not supported.
    @Test
    @DisplayName("Test lowerEntry() throws when not supported")
    public void testLowerEntryWhenNotSupported() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(LOWER_ENTRY_ALWAYS_RETURNS_NULL);
        builder.doesNotSupport(NavigableMapMethods.LOWER_ENTRY);
        BreakableNavigableMap<String, Integer> map = builder.build();

        assertThrows(UnsupportedOperationException.class, () -> map.lowerEntry("banana"));
    }

    // ========== Navigation Key Break Tests ==========

    /// Tests the LOWER_KEY_ALWAYS_RETURNS_NULL break functionality.
    @Test
    @DisplayName("Test LOWER_KEY_ALWAYS_RETURNS_NULL break")
    public void testLowerKeyAlwaysReturnsNull() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(LOWER_KEY_ALWAYS_RETURNS_NULL);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);

        assertNull(map.lowerKey("banana")); // Should normally return "apple"
    }

    /// Tests the LOWER_KEY_THROWS_EXCEPTION break functionality.
    @Test
    @DisplayName("Test LOWER_KEY_THROWS_EXCEPTION break")
    public void testLowerKeyThrowsException() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(LOWER_KEY_THROWS_EXCEPTION);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);

        assertThrows(NoSuchElementException.class, () -> map.lowerKey("banana"));
    }

    /// Tests the LOWER_KEY_RETURNS_RANDOM_KEY break functionality.
    @Test
    @DisplayName("Test LOWER_KEY_RETURNS_RANDOM_KEY break")
    public void testLowerKeyReturnsRandomKey() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(LOWER_KEY_RETURNS_RANDOM_KEY);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);
        map.put("cherry", 3);

        String lowerKey = map.lowerKey("cherry");
        assertNotNull(lowerKey);
        assertTrue(map.containsKey(lowerKey));
    }

    // ========== Floor Navigation Break Tests ==========

    /// Tests the FLOOR_ENTRY_ALWAYS_RETURNS_NULL break functionality.
    @Test
    @DisplayName("Test FLOOR_ENTRY_ALWAYS_RETURNS_NULL break")
    public void testFloorEntryAlwaysReturnsNull() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(FLOOR_ENTRY_ALWAYS_RETURNS_NULL);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);

        assertNull(map.floorEntry("banana")); // Should normally return banana entry
    }

    /// Tests the FLOOR_KEY_THROWS_EXCEPTION break functionality.
    @Test
    @DisplayName("Test FLOOR_KEY_THROWS_EXCEPTION break")
    public void testFloorKeyThrowsException() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(FLOOR_KEY_THROWS_EXCEPTION);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);

        assertThrows(NoSuchElementException.class, () -> map.floorKey("apple"));
    }

    // ========== Ceiling Navigation Break Tests ==========

    /// Tests the CEILING_ENTRY_ALWAYS_RETURNS_NULL break functionality.
    @Test
    @DisplayName("Test CEILING_ENTRY_ALWAYS_RETURNS_NULL break")
    public void testCeilingEntryAlwaysReturnsNull() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(CEILING_ENTRY_ALWAYS_RETURNS_NULL);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);

        assertNull(map.ceilingEntry("apple")); // Should normally return apple entry
    }

    /// Tests the CEILING_KEY_THROWS_EXCEPTION break functionality.
    @Test
    @DisplayName("Test CEILING_KEY_THROWS_EXCEPTION break")
    public void testCeilingKeyThrowsException() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(CEILING_KEY_THROWS_EXCEPTION);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);

        assertThrows(NoSuchElementException.class, () -> map.ceilingKey("apple"));
    }

    // ========== Higher Navigation Break Tests ==========

    /// Tests the HIGHER_ENTRY_ALWAYS_RETURNS_NULL break functionality.
    @Test
    @DisplayName("Test HIGHER_ENTRY_ALWAYS_RETURNS_NULL break")
    public void testHigherEntryAlwaysReturnsNull() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(HIGHER_ENTRY_ALWAYS_RETURNS_NULL);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);

        assertNull(map.higherEntry("apple")); // Should normally return banana entry
    }

    /// Tests the HIGHER_KEY_THROWS_EXCEPTION break functionality.
    @Test
    @DisplayName("Test HIGHER_KEY_THROWS_EXCEPTION break")
    public void testHigherKeyThrowsException() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(HIGHER_KEY_THROWS_EXCEPTION);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);

        assertThrows(NoSuchElementException.class, () -> map.higherKey("apple"));
    }

    // ========== Entry Access Break Tests ==========

    /// Tests the FIRST_ENTRY_ALWAYS_RETURNS_NULL break functionality.
    @Test
    @DisplayName("Test FIRST_ENTRY_ALWAYS_RETURNS_NULL break")
    public void testFirstEntryAlwaysReturnsNull() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(FIRST_ENTRY_ALWAYS_RETURNS_NULL);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);

        assertNull(map.firstEntry()); // Should normally return apple entry
        assertFalse(map.isEmpty()); // Map has elements but firstEntry returns null
    }

    /// Tests the FIRST_ENTRY_THROWS_EXCEPTION break functionality.
    @Test
    @DisplayName("Test FIRST_ENTRY_THROWS_EXCEPTION break")
    public void testFirstEntryThrowsException() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(FIRST_ENTRY_THROWS_EXCEPTION);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);

        assertThrows(NoSuchElementException.class, map::firstEntry);
    }

    /// Tests the LAST_ENTRY_RETURNS_RANDOM_ENTRY break functionality.
    @Test
    @DisplayName("Test LAST_ENTRY_RETURNS_RANDOM_ENTRY break")
    public void testLastEntryReturnsRandomEntry() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(LAST_ENTRY_RETURNS_RANDOM_ENTRY);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);
        map.put("cherry", 3);

        Map.Entry<String, Integer> lastEntry = map.lastEntry();
        assertNotNull(lastEntry);
        assertTrue(map.containsKey(lastEntry.getKey()));
    }

    // ========== Poll Entry Break Tests ==========

    /// Tests the POLL_FIRST_ENTRY_ALWAYS_RETURNS_NULL break functionality.
    @Test
    @DisplayName("Test POLL_FIRST_ENTRY_ALWAYS_RETURNS_NULL break")
    public void testPollFirstEntryAlwaysReturnsNull() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(POLL_FIRST_ENTRY_ALWAYS_RETURNS_NULL);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);

        assertNull(map.pollFirstEntry());
        assertEquals(2, map.size()); // Elements should still be there
    }

    /// Tests the POLL_FIRST_ENTRY_THROWS_EXCEPTION break functionality.
    @Test
    @DisplayName("Test POLL_FIRST_ENTRY_THROWS_EXCEPTION break")
    public void testPollFirstEntryThrowsException() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(POLL_FIRST_ENTRY_THROWS_EXCEPTION);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);

        assertThrows(NoSuchElementException.class, map::pollFirstEntry);
    }

    /// Tests the POLL_FIRST_ENTRY_DOES_NOT_REMOVE break functionality.
    @Test
    @DisplayName("Test POLL_FIRST_ENTRY_DOES_NOT_REMOVE break")
    public void testPollFirstEntryDoesNotRemove() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(POLL_FIRST_ENTRY_DOES_NOT_REMOVE);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);

        Map.Entry<String, Integer> entry = map.pollFirstEntry();
        assertNotNull(entry);
        assertEquals("apple", entry.getKey());
        assertEquals(2, map.size()); // Should not have been removed
        assertTrue(map.containsKey("apple")); // Should still be there
    }

    /// Tests the POLL_LAST_ENTRY_ALWAYS_RETURNS_NULL break functionality.
    @Test
    @DisplayName("Test POLL_LAST_ENTRY_ALWAYS_RETURNS_NULL break")
    public void testPollLastEntryAlwaysReturnsNull() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(POLL_LAST_ENTRY_ALWAYS_RETURNS_NULL);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);

        assertNull(map.pollLastEntry());
        assertEquals(1, map.size()); // Element should still be there
    }

    // ========== Map View Break Tests ==========

    /// Tests the DESCENDING_MAP_RETURNS_EMPTY_MAP break functionality.
    @Test
    @DisplayName("Test DESCENDING_MAP_RETURNS_EMPTY_MAP break")
    public void testDescendingMapReturnsEmptyMap() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(DESCENDING_MAP_RETURNS_EMPTY_MAP);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);

        NavigableMap<String, Integer> descendingMap = map.descendingMap();
        assertTrue(descendingMap.isEmpty()); // Empty due to break
        assertEquals(2, map.size()); // Original map still has data
    }

    /// Tests the DESCENDING_MAP_RETURNS_NULL break functionality.
    @Test
    @DisplayName("Test DESCENDING_MAP_RETURNS_NULL break")
    public void testDescendingMapReturnsNull() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(DESCENDING_MAP_RETURNS_NULL);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);

        assertNull(map.descendingMap());
    }

    /// Tests the DESCENDING_MAP_THROWS_EXCEPTION break functionality.
    @Test
    @DisplayName("Test DESCENDING_MAP_THROWS_EXCEPTION break")
    public void testDescendingMapThrowsException() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(DESCENDING_MAP_THROWS_EXCEPTION);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);

        assertThrows(UnsupportedOperationException.class, map::descendingMap);
    }

    // ========== Key Set Break Tests ==========

    /// Tests the NAVIGABLE_KEY_SET_RETURNS_EMPTY_SET break functionality.
    @Test
    @DisplayName("Test NAVIGABLE_KEY_SET_RETURNS_EMPTY_SET break")
    public void testNavigableKeySetReturnsEmptySet() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(NAVIGABLE_KEY_SET_RETURNS_EMPTY_SET);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);

        NavigableSet<String> keySet = map.navigableKeySet();
        assertTrue(keySet.isEmpty()); // Empty due to break
        assertEquals(2, map.size()); // Original map still has data
    }

    /// Tests the NAVIGABLE_KEY_SET_RETURNS_NULL break functionality.
    @Test
    @DisplayName("Test NAVIGABLE_KEY_SET_RETURNS_NULL break")
    public void testNavigableKeySetReturnsNull() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(NAVIGABLE_KEY_SET_RETURNS_NULL);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);

        assertNull(map.navigableKeySet());
    }

    /// Tests the DESCENDING_KEY_SET_THROWS_EXCEPTION break functionality.
    @Test
    @DisplayName("Test DESCENDING_KEY_SET_THROWS_EXCEPTION break")
    public void testDescendingKeySetThrowsException() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(DESCENDING_KEY_SET_THROWS_EXCEPTION);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);

        assertThrows(UnsupportedOperationException.class, map::descendingKeySet);
    }

    // ========== Enhanced Sub-Map Break Tests ==========

    /// Tests the SUB_MAP_FOUR_ARG_RETURNS_EMPTY_MAP break functionality.
    @Test
    @DisplayName("Test SUB_MAP_FOUR_ARG_RETURNS_EMPTY_MAP break")
    public void testSubMapFourArgReturnsEmptyMap() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(SUB_MAP_FOUR_ARG_RETURNS_EMPTY_MAP);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);
        map.put("cherry", 3);

        NavigableMap<String, Integer> subMap = map.subMap("apple", true, "cherry", false);
        assertTrue(subMap.isEmpty()); // Empty due to break
        assertEquals(3, map.size()); // Original map still has data
    }

    /// Tests the SUB_MAP_FOUR_ARG_RETURNS_NULL break functionality.
    @Test
    @DisplayName("Test SUB_MAP_FOUR_ARG_RETURNS_NULL break")
    public void testSubMapFourArgReturnsNull() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(SUB_MAP_FOUR_ARG_RETURNS_NULL);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);

        assertNull(map.subMap("apple", true, "banana", true));
    }

    /// Tests the HEAD_MAP_TWO_ARG_THROWS_EXCEPTION break functionality.
    @Test
    @DisplayName("Test HEAD_MAP_TWO_ARG_THROWS_EXCEPTION break")
    public void testHeadMapTwoArgThrowsException() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(HEAD_MAP_TWO_ARG_THROWS_EXCEPTION);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);

        assertThrows(IllegalArgumentException.class, () -> map.headMap("banana", false));
    }

    /// Tests the TAIL_MAP_TWO_ARG_RETURNS_EMPTY_MAP break functionality.
    @Test
    @DisplayName("Test TAIL_MAP_TWO_ARG_RETURNS_EMPTY_MAP break")
    public void testTailMapTwoArgReturnsEmptyMap() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(TAIL_MAP_TWO_ARG_RETURNS_EMPTY_MAP);
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);

        NavigableMap<String, Integer> tailMap = map.tailMap("apple", true);
        assertTrue(tailMap.isEmpty()); // Empty due to break
        assertEquals(2, map.size()); // Original map still has data
    }

    // ========== Static Factory Method Tests ==========

    /// Tests the static wrap factory method functionality.
    @Test
    @DisplayName("Test wrap factory method")
    public void testWrapFactoryMethod() {
        NavigableMap<String, Integer> existingMap = new TreeMap<>();
        existingMap.put("apple", 1);
        existingMap.put("banana", 2);

        Set<Break> breaks = Set.of(HIGHER_KEY_ALWAYS_RETURNS_NULL, LOWER_ENTRY_THROWS_EXCEPTION);

        BreakableNavigableMap<String, Integer> wrappedMap = BreakableNavigableMap.wrap(existingMap, breaks);

        // Verify breaks are active
        assertNull(wrappedMap.higherKey("apple"));
        assertThrows(NoSuchElementException.class, () -> wrappedMap.lowerEntry("banana"));

        // Verify original data is preserved
        assertTrue(wrappedMap.containsKey("apple"));
        assertTrue(wrappedMap.containsKey("banana"));

        // Verify null policies default to permissive
        assertTrue(wrappedMap.permitsNullKeys());
        assertTrue(wrappedMap.permitsNullValues());
    }

    // ========== Navigation and NavigableMap Behavior Tests ==========

    /// Tests that BreakableNavigableMap maintains proper navigation.
    @Test
    @DisplayName("Test navigation is maintained with breaks")
    public void testNavigationMaintained() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(DESCENDING_MAP_RETURNS_EMPTY_MAP); // This shouldn't affect navigation
        BreakableNavigableMap<String, Integer> map = builder.build();

        // Add elements in non-alphabetical order
        map.put("zebra", 26);
        map.put("apple", 1);
        map.put("banana", 2);
        map.put("cherry", 3);

        // Verify navigation operations work correctly
        assertEquals("apple", map.firstKey());
        assertEquals("zebra", map.lastKey());
        assertEquals("banana", map.higherKey("apple"));
        assertEquals("apple", map.lowerKey("banana"));
        assertEquals("cherry", map.ceilingKey("cherry"));
        assertEquals("banana", map.floorKey("banana"));
    }

    /// Tests BreakableNavigableMap with custom comparator.
    @Test
    @DisplayName("Test custom comparator behavior")
    public void testCustomComparator() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        BreakableNavigableMap<String, Integer> map = builder
                .addBreak(NAVIGABLE_KEY_SET_RETURNS_EMPTY_SET)
                .setComparator(Comparator.reverseOrder())
                .add("apple", 1)
                .add("banana", 2)
                .add("cherry", 3)
                .build();

        // Verify custom comparator is preserved
        assertNotNull(map.comparator());
        assertEquals(Collections.reverseOrder(), map.comparator());

        // Verify reverse ordering in navigation
        assertEquals("cherry", map.firstKey()); // First in reverse order
        assertEquals("apple", map.lastKey());   // Last in reverse order
        assertEquals("banana", map.higherKey("cherry")); // Higher in reverse order
        assertEquals("banana", map.lowerKey("apple"));   // Lower in reverse order
    }

    // ========== Inheritance and Map Interface Tests ==========

    /// Tests that BreakableNavigableMap properly inherits Map and SortedMap functionality.
    @Test
    @DisplayName("Test inheritance from BreakableSortedMap")
    public void testSortedMapInheritance() {
        BreakableNavigableMap.Builder<String, Integer> builder = new BreakableNavigableMap.Builder<>();
        builder.addBreak(BreakableMap.GET_ALWAYS_RETURNS_NULL);     // Map-level break
        builder.addBreak(BreakableSortedMap.FIRST_KEY_THROWS_EXCEPTION); // SortedMap-level break
        builder.addBreak(HIGHER_KEY_ALWAYS_RETURNS_NULL);           // NavigableMap-level break
        BreakableNavigableMap<String, Integer> map = builder.build();

        map.put("apple", 1);
        map.put("banana", 2);

        // Map-level functionality with break
        assertNull(map.get("apple")); // Due to GET_ALWAYS_RETURNS_NULL break
        assertEquals(2, map.size());   // Size should work normally

        // SortedMap-level functionality with break
        assertThrows(NoSuchElementException.class, map::firstKey); // Due to FIRST_KEY_THROWS_EXCEPTION break
        assertEquals("banana", map.lastKey()); // Should work normally

        // NavigableMap-level functionality with break
        assertNull(map.higherKey("apple")); // Due to HIGHER_KEY_ALWAYS_RETURNS_NULL break
        assertEquals("apple", map.lowerKey("banana")); // Should work normally
    }
}