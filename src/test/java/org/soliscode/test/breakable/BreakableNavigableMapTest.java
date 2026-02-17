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

    /// Verifies the default constructor creates an empty navigable map with proper initial state.
    @Test
    @DisplayName("Test default constructor creates empty navigable map")
    public void defaultConstructor_whenCalled_createsEmptyNavigableMap() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap<>();

        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertNull(map.comparator()); // Natural ordering
        assertTrue(map.permitsNullKeys());
        assertTrue(map.permitsNullValues());
    }

    /// Verifies the copy constructor correctly copies elements and configuration.
    @Test
    @DisplayName("Test copy constructor")
    public void copyConstructor_whenCalled_copiesElementsAndConfiguration() {
        BreakableNavigableMap<String, Integer> original = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(LOWER_ENTRY_THROWS_EXCEPTION)
                .doesNotPermitNullKeys()
                .build();

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

    /// Verifies the constructor with TreeMap correctly initializes the map with data.
    @Test
    @DisplayName("Test constructor with TreeMap")
    public void constructorWithTreeMap_whenCalled_initializesWithData() {
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

    /// Verifies the builder correctly configures and builds a navigable map.
    @Test
    @DisplayName("Test builder pattern")
    public void builder_whenCalled_configuresAndBuildsMap() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(HIGHER_KEY_ALWAYS_RETURNS_NULL)
                .addBreak(DESCENDING_MAP_RETURNS_EMPTY_MAP)
                .doesNotPermitNullKeys()
                .build();

        map.put("key", 42);

        // Verify breaks are active
        assertNull(map.higherKey("key"));
        assertTrue(map.descendingMap().isEmpty());

        // Verify null policy
        assertFalse(map.permitsNullKeys());
        assertTrue(map.permitsNullValues());
    }

    /// Verifies the builder correctly initializes with data from an existing navigable map.
    @Test
    @DisplayName("Test builder with existing NavigableMap")
    public void builder_withExistingNavigableMap_initializesWithData() {
        TreeMap<String, Integer> existingMap = new TreeMap<>();
        existingMap.put("apple", 1);
        existingMap.put("banana", 2);
        existingMap.put("cherry", 3);

        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<>(existingMap)
                .addBreak(LOWER_KEY_ALWAYS_RETURNS_NULL)
                .build();

        // Original data should be present
        assertEquals(Integer.valueOf(1), map.get("apple"));
        assertEquals(Integer.valueOf(2), map.get("banana"));
        assertEquals(Integer.valueOf(3), map.get("cherry"));

        // Break should be active
        assertNull(map.lowerKey("banana")); // Due to break
    }

    /// Verifies the builder copy method creates an independent builder with the same configuration.
    @Test
    @DisplayName("Test builder copy")
    public void builderCopy_whenCalled_createsIndependentBuilderWithSameConfiguration() {
        BreakableNavigableMap.Builder<String, Integer> original = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(FIRST_ENTRY_THROWS_EXCEPTION)
                .doesNotPermitNullValues();

        BreakableNavigableMap.Builder<String, Integer> copy = original.copy()
                .addBreak(LAST_ENTRY_THROWS_EXCEPTION);

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

    /// Verifies that `lowerEntry()` returns `null` when the {@link BreakableNavigableMap#LOWER_ENTRY_ALWAYS_RETURNS_NULL} break is added.
    @Test
    @DisplayName("Test LOWER_ENTRY_ALWAYS_RETURNS_NULL break")
    public void lowerEntry_whenAlwaysReturnsNullBreakAdded_returnsNull() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(LOWER_ENTRY_ALWAYS_RETURNS_NULL)
                .build();

        map.put("apple", 1);
        map.put("banana", 2);
        map.put("cherry", 3);

        assertNull(map.lowerEntry("cherry")); // Should normally return banana entry
        assertNull(map.lowerEntry("banana")); // Should normally return apple entry
    }

    /// Verifies that `lowerEntry()` throws `NoSuchElementException` when the {@link BreakableNavigableMap#LOWER_ENTRY_THROWS_EXCEPTION} break is added.
    @Test
    @DisplayName("Test LOWER_ENTRY_THROWS_EXCEPTION break")
    public void lowerEntry_whenThrowsExceptionBreakAdded_throwsNoSuchElementException() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(LOWER_ENTRY_THROWS_EXCEPTION)
                .build();

        map.put("apple", 1);
        map.put("banana", 2);

        assertThrows(NoSuchElementException.class, () -> map.lowerEntry("banana"));
    }

    /// Verifies that `lowerEntry()` returns a random entry when the {@link BreakableNavigableMap#LOWER_ENTRY_RETURNS_RANDOM_ENTRY} break is added.
    @Test
    @DisplayName("Test LOWER_ENTRY_RETURNS_RANDOM_ENTRY break")
    public void lowerEntry_whenReturnsRandomEntryBreakAdded_returnsRandomEntry() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(LOWER_ENTRY_RETURNS_RANDOM_ENTRY)
                .build();

        map.put("apple", 1);
        map.put("banana", 2);
        map.put("cherry", 3);

        Map.Entry<String, Integer> entry = map.lowerEntry("cherry");
        assertNotNull(entry);
        assertTrue(map.containsKey(entry.getKey()));
    }

    /// Verifies that `lowerEntry()` throws `UnsupportedOperationException` when the method is not supported.
    @Test
    @DisplayName("Test lowerEntry() throws when not supported")
    public void lowerEntry_whenNotSupported_throwsUnsupportedOperationException() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(LOWER_ENTRY_ALWAYS_RETURNS_NULL)
                .doesNotSupport(NavigableMapMethods.LOWER_ENTRY)
                .build();

        assertThrows(UnsupportedOperationException.class, () -> map.lowerEntry("banana"));
    }

    // ========== Navigation Key Break Tests ==========

    /// Verifies that `lowerKey()` returns `null` when the {@link BreakableNavigableMap#LOWER_KEY_ALWAYS_RETURNS_NULL} break is added.
    @Test
    @DisplayName("Test LOWER_KEY_ALWAYS_RETURNS_NULL break")
    public void lowerKey_whenAlwaysReturnsNullBreakAdded_returnsNull() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(LOWER_KEY_ALWAYS_RETURNS_NULL)
                .build();

        map.put("apple", 1);
        map.put("banana", 2);

        assertNull(map.lowerKey("banana")); // Should normally return "apple"
    }

    /// Verifies that `lowerKey()` throws `NoSuchElementException` when the {@link BreakableNavigableMap#LOWER_KEY_THROWS_EXCEPTION} break is added.
    @Test
    @DisplayName("Test LOWER_KEY_THROWS_EXCEPTION break")
    public void lowerKey_whenThrowsExceptionBreakAdded_throwsNoSuchElementException() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(LOWER_KEY_THROWS_EXCEPTION)
                .build();

        map.put("apple", 1);
        map.put("banana", 2);

        assertThrows(NoSuchElementException.class, () -> map.lowerKey("banana"));
    }

    /// Verifies that `lowerKey()` returns a random key when the {@link BreakableNavigableMap#LOWER_KEY_RETURNS_RANDOM_KEY} break is added.
    @Test
    @DisplayName("Test LOWER_KEY_RETURNS_RANDOM_KEY break")
    public void lowerKey_whenReturnsRandomKeyBreakAdded_returnsRandomKey() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(LOWER_KEY_RETURNS_RANDOM_KEY)
                .build();

        map.put("apple", 1);
        map.put("banana", 2);
        map.put("cherry", 3);

        String lowerKey = map.lowerKey("cherry");
        assertNotNull(lowerKey);
        assertTrue(map.containsKey(lowerKey));
    }

    // ========== Floor Navigation Break Tests ==========

    /// Verifies that `floorEntry()` returns `null` when the {@link BreakableNavigableMap#FLOOR_ENTRY_ALWAYS_RETURNS_NULL} break is added.
    @Test
    @DisplayName("Test FLOOR_ENTRY_ALWAYS_RETURNS_NULL break")
    public void floorEntry_whenAlwaysReturnsNullBreakAdded_returnsNull() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(FLOOR_ENTRY_ALWAYS_RETURNS_NULL)
                .build();

        map.put("apple", 1);
        map.put("banana", 2);

        assertNull(map.floorEntry("banana")); // Should normally return banana entry
    }

    /// Verifies that `floorKey()` throws `NoSuchElementException` when the {@link BreakableNavigableMap#FLOOR_KEY_THROWS_EXCEPTION} break is added.
    @Test
    @DisplayName("Test FLOOR_KEY_THROWS_EXCEPTION break")
    public void floorKey_whenThrowsExceptionBreakAdded_throwsNoSuchElementException() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(FLOOR_KEY_THROWS_EXCEPTION)
                .build();

        map.put("apple", 1);

        assertThrows(NoSuchElementException.class, () -> map.floorKey("apple"));
    }

    // ========== Ceiling Navigation Break Tests ==========

    /// Verifies that `ceilingEntry()` returns `null` when the {@link BreakableNavigableMap#CEILING_ENTRY_ALWAYS_RETURNS_NULL} break is added.
    @Test
    @DisplayName("Test CEILING_ENTRY_ALWAYS_RETURNS_NULL break")
    public void ceilingEntry_whenAlwaysReturnsNullBreakAdded_returnsNull() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(CEILING_ENTRY_ALWAYS_RETURNS_NULL)
                .build();

        map.put("apple", 1);
        map.put("banana", 2);

        assertNull(map.ceilingEntry("apple")); // Should normally return apple entry
    }

    /// Verifies that `ceilingKey()` throws `NoSuchElementException` when the {@link BreakableNavigableMap#CEILING_KEY_THROWS_EXCEPTION} break is added.
    @Test
    @DisplayName("Test CEILING_KEY_THROWS_EXCEPTION break")
    public void ceilingKey_whenThrowsExceptionBreakAdded_throwsNoSuchElementException() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(CEILING_KEY_THROWS_EXCEPTION)
                .build();

        map.put("apple", 1);

        assertThrows(NoSuchElementException.class, () -> map.ceilingKey("apple"));
    }

    // ========== Higher Navigation Break Tests ==========

    /// Verifies that `higherEntry()` returns `null` when the {@link BreakableNavigableMap#HIGHER_ENTRY_ALWAYS_RETURNS_NULL} break is added.
    @Test
    @DisplayName("Test HIGHER_ENTRY_ALWAYS_RETURNS_NULL break")
    public void higherEntry_whenAlwaysReturnsNullBreakAdded_returnsNull() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(HIGHER_ENTRY_ALWAYS_RETURNS_NULL)
                .build();

        map.put("apple", 1);
        map.put("banana", 2);

        assertNull(map.higherEntry("apple")); // Should normally return banana entry
    }

    /// Verifies that `higherKey()` throws `NoSuchElementException` when the {@link BreakableNavigableMap#HIGHER_KEY_THROWS_EXCEPTION} break is added.
    @Test
    @DisplayName("Test HIGHER_KEY_THROWS_EXCEPTION break")
    public void higherKey_whenThrowsExceptionBreakAdded_throwsNoSuchElementException() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(HIGHER_KEY_THROWS_EXCEPTION)
                .build();

        map.put("apple", 1);

        assertThrows(NoSuchElementException.class, () -> map.higherKey("apple"));
    }

    // ========== Entry Access Break Tests ==========

    /// Verifies that `firstEntry()` returns `null` when the {@link BreakableNavigableMap#FIRST_ENTRY_ALWAYS_RETURNS_NULL} break is added.
    @Test
    @DisplayName("Test FIRST_ENTRY_ALWAYS_RETURNS_NULL break")
    public void firstEntry_whenAlwaysReturnsNullBreakAdded_returnsNull() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(FIRST_ENTRY_ALWAYS_RETURNS_NULL)
                .build();

        map.put("apple", 1);
        map.put("banana", 2);

        assertNull(map.firstEntry()); // Should normally return apple entry
        assertFalse(map.isEmpty()); // Map has elements but firstEntry returns null
    }

    /// Verifies that `firstEntry()` throws `NoSuchElementException` when the {@link BreakableNavigableMap#FIRST_ENTRY_THROWS_EXCEPTION} break is added.
    @Test
    @DisplayName("Test FIRST_ENTRY_THROWS_EXCEPTION break")
    public void firstEntry_whenThrowsExceptionBreakAdded_throwsNoSuchElementException() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(FIRST_ENTRY_THROWS_EXCEPTION)
                .build();

        map.put("apple", 1);

        assertThrows(NoSuchElementException.class, map::firstEntry);
    }

    /// Verifies that `lastEntry()` returns a random entry when the {@link BreakableNavigableMap#LAST_ENTRY_RETURNS_RANDOM_ENTRY} break is added.
    @Test
    @DisplayName("Test LAST_ENTRY_RETURNS_RANDOM_ENTRY break")
    public void lastEntry_whenReturnsRandomEntryBreakAdded_returnsRandomEntry() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(LAST_ENTRY_RETURNS_RANDOM_ENTRY)
                .build();

        map.put("apple", 1);
        map.put("banana", 2);
        map.put("cherry", 3);

        Map.Entry<String, Integer> lastEntry = map.lastEntry();
        assertNotNull(lastEntry);
        assertTrue(map.containsKey(lastEntry.getKey()));
    }

    // ========== Poll Entry Break Tests ==========

    /// Verifies that `pollFirstEntry()` returns `null` when the {@link BreakableNavigableMap#POLL_FIRST_ENTRY_ALWAYS_RETURNS_NULL} break is added.
    @Test
    @DisplayName("Test POLL_FIRST_ENTRY_ALWAYS_RETURNS_NULL break")
    public void pollFirstEntry_whenAlwaysReturnsNullBreakAdded_returnsNull() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(POLL_FIRST_ENTRY_ALWAYS_RETURNS_NULL)
                .build();

        map.put("apple", 1);
        map.put("banana", 2);

        assertNull(map.pollFirstEntry());
        assertEquals(2, map.size()); // Elements should still be there
    }

    /// Verifies that `pollFirstEntry()` throws `NoSuchElementException` when the {@link BreakableNavigableMap#POLL_FIRST_ENTRY_THROWS_EXCEPTION} break is added.
    @Test
    @DisplayName("Test POLL_FIRST_ENTRY_THROWS_EXCEPTION break")
    public void pollFirstEntry_whenThrowsExceptionBreakAdded_throwsNoSuchElementException() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(POLL_FIRST_ENTRY_THROWS_EXCEPTION)
                .build();

        map.put("apple", 1);

        assertThrows(NoSuchElementException.class, map::pollFirstEntry);
    }

    /// Verifies that `pollFirstEntry()` does not remove the entry when the {@link BreakableNavigableMap#POLL_FIRST_ENTRY_DOES_NOT_REMOVE} break is added.
    @Test
    @DisplayName("Test POLL_FIRST_ENTRY_DOES_NOT_REMOVE break")
    public void pollFirstEntry_whenDoesNotRemoveBreakAdded_doesNotRemoveEntry() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(POLL_FIRST_ENTRY_DOES_NOT_REMOVE)
                .build();

        map.put("apple", 1);
        map.put("banana", 2);

        Map.Entry<String, Integer> entry = map.pollFirstEntry();
        assertNotNull(entry);
        assertEquals("apple", entry.getKey());
        assertEquals(2, map.size()); // Should not have been removed
        assertTrue(map.containsKey("apple")); // Should still be there
    }

    /// Verifies that `pollLastEntry()` returns `null` when the {@link BreakableNavigableMap#POLL_LAST_ENTRY_ALWAYS_RETURNS_NULL} break is added.
    @Test
    @DisplayName("Test POLL_LAST_ENTRY_ALWAYS_RETURNS_NULL break")
    public void pollLastEntry_whenAlwaysReturnsNullBreakAdded_returnsNull() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(POLL_LAST_ENTRY_ALWAYS_RETURNS_NULL)
                .build();

        map.put("apple", 1);

        assertNull(map.pollLastEntry());
        assertEquals(1, map.size()); // Element should still be there
    }

    // ========== Map View Break Tests ==========

    /// Verifies that `descendingMap()` returns an empty map when the {@link BreakableNavigableMap#DESCENDING_MAP_RETURNS_EMPTY_MAP} break is added.
    @Test
    @DisplayName("Test DESCENDING_MAP_RETURNS_EMPTY_MAP break")
    public void descendingMap_whenReturnsEmptyMapBreakAdded_returnsEmptyMap() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(DESCENDING_MAP_RETURNS_EMPTY_MAP)
                .build();

        map.put("apple", 1);
        map.put("banana", 2);

        NavigableMap<String, Integer> descendingMap = map.descendingMap();
        assertTrue(descendingMap.isEmpty()); // Empty due to break
        assertEquals(2, map.size()); // Original map still has data
    }

    /// Verifies that `descendingMap()` returns `null` when the {@link BreakableNavigableMap#DESCENDING_MAP_RETURNS_NULL} break is added.
    @Test
    @DisplayName("Test DESCENDING_MAP_RETURNS_NULL break")
    public void descendingMap_whenReturnsNullBreakAdded_returnsNull() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(DESCENDING_MAP_RETURNS_NULL)
                .build();

        map.put("apple", 1);

        assertNull(map.descendingMap());
    }

    /// Verifies that `descendingMap()` throws `UnsupportedOperationException` when the {@link BreakableNavigableMap#DESCENDING_MAP_THROWS_EXCEPTION} break is added.
    @Test
    @DisplayName("Test DESCENDING_MAP_THROWS_EXCEPTION break")
    public void descendingMap_whenThrowsExceptionBreakAdded_throwsUnsupportedOperationException() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(DESCENDING_MAP_THROWS_EXCEPTION)
                .build();

        map.put("apple", 1);

        assertThrows(UnsupportedOperationException.class, map::descendingMap);
    }

    // ========== Key Set Break Tests ==========

    /// Verifies that `navigableKeySet()` returns an empty set when the {@link BreakableNavigableMap#NAVIGABLE_KEY_SET_RETURNS_EMPTY_SET} break is added.
    @Test
    @DisplayName("Test NAVIGABLE_KEY_SET_RETURNS_EMPTY_SET break")
    public void navigableKeySet_whenReturnsEmptySetBreakAdded_returnsEmptySet() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(NAVIGABLE_KEY_SET_RETURNS_EMPTY_SET)
                .build();

        map.put("apple", 1);
        map.put("banana", 2);

        NavigableSet<String> keySet = map.navigableKeySet();
        assertTrue(keySet.isEmpty()); // Empty due to break
        assertEquals(2, map.size()); // Original map still has data
    }

    /// Verifies that `navigableKeySet()` returns `null` when the {@link BreakableNavigableMap#NAVIGABLE_KEY_SET_RETURNS_NULL} break is added.
    @Test
    @DisplayName("Test NAVIGABLE_KEY_SET_RETURNS_NULL break")
    public void navigableKeySet_whenReturnsNullBreakAdded_returnsNull() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(NAVIGABLE_KEY_SET_RETURNS_NULL)
                .build();

        map.put("apple", 1);

        assertNull(map.navigableKeySet());
    }

    /// Verifies that `descendingKeySet()` throws `UnsupportedOperationException` when the {@link BreakableNavigableMap#DESCENDING_KEY_SET_THROWS_EXCEPTION} break is added.
    @Test
    @DisplayName("Test DESCENDING_KEY_SET_THROWS_EXCEPTION break")
    public void descendingKeySet_whenThrowsExceptionBreakAdded_throwsUnsupportedOperationException() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(DESCENDING_KEY_SET_THROWS_EXCEPTION)
                .build();

        map.put("apple", 1);

        assertThrows(UnsupportedOperationException.class, map::descendingKeySet);
    }

    // ========== Enhanced Sub-Map Break Tests ==========

    /// Verifies that `subMap()` returns an empty map when the {@link BreakableNavigableMap#SUB_MAP_FOUR_ARG_RETURNS_EMPTY_MAP} break is added.
    @Test
    @DisplayName("Test SUB_MAP_FOUR_ARG_RETURNS_EMPTY_MAP break")
    public void subMap_whenReturnsEmptyMapBreakAdded_returnsEmptyMap() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(SUB_MAP_FOUR_ARG_RETURNS_EMPTY_MAP)
                .build();

        map.put("apple", 1);
        map.put("banana", 2);
        map.put("cherry", 3);

        NavigableMap<String, Integer> subMap = map.subMap("apple", true, "cherry", false);
        assertTrue(subMap.isEmpty()); // Empty due to break
        assertEquals(3, map.size()); // Original map still has data
    }

    /// Verifies that `subMap()` returns `null` when the {@link BreakableNavigableMap#SUB_MAP_FOUR_ARG_RETURNS_NULL} break is added.
    @Test
    @DisplayName("Test SUB_MAP_FOUR_ARG_RETURNS_NULL break")
    public void subMap_whenReturnsNullBreakAdded_returnsNull() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(SUB_MAP_FOUR_ARG_RETURNS_NULL)
                .build();

        map.put("apple", 1);
        map.put("banana", 2);

        assertNull(map.subMap("apple", true, "banana", true));
    }

    /// Verifies that `headMap()` throws an exception when the {@link BreakableNavigableMap#HEAD_MAP_TWO_ARG_THROWS_EXCEPTION} break is added.
    @Test
    @DisplayName("Test HEAD_MAP_TWO_ARG_THROWS_EXCEPTION break")
    public void headMap_whenThrowsExceptionBreakAdded_throwsIllegalArgumentException() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(HEAD_MAP_TWO_ARG_THROWS_EXCEPTION)
                .build();

        map.put("apple", 1);
        map.put("banana", 2);

        assertThrows(IllegalArgumentException.class, () -> map.headMap("banana", false));
    }

    /// Verifies that `tailMap()` returns an empty map when the {@link BreakableNavigableMap#TAIL_MAP_TWO_ARG_RETURNS_EMPTY_MAP} break is added.
    @Test
    @DisplayName("Test TAIL_MAP_TWO_ARG_RETURNS_EMPTY_MAP break")
    public void tailMap_whenReturnsEmptyMapBreakAdded_returnsEmptyMap() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(TAIL_MAP_TWO_ARG_RETURNS_EMPTY_MAP)
                .build();

        map.put("apple", 1);
        map.put("banana", 2);

        NavigableMap<String, Integer> tailMap = map.tailMap("apple", true);
        assertTrue(tailMap.isEmpty()); // Empty due to break
        assertEquals(2, map.size()); // Original map still has data
    }

    // ========== Static Factory Method Tests ==========

    /// Verifies the static `wrap` factory method correctly wraps an existing `NavigableMap`.
    @Test
    @DisplayName("Test wrap factory method")
    public void wrap_whenCalled_createsWrappedInstance() {
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

    /// Verifies that `BreakableNavigableMap` maintains proper navigation order under normal conditions.
    @Test
    @DisplayName("Test navigation is maintained with breaks")
    public void navigation_whenCalled_isMaintained() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(DESCENDING_MAP_RETURNS_EMPTY_MAP) // This shouldn't affect navigation
                .build();

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

    /// Verifies that `BreakableNavigableMap` correctly uses a custom comparator for ordering.
    @Test
    @DisplayName("Test custom comparator behavior")
    public void customComparator_whenProvided_affectsOrdering() {
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

    /// Verifies that `BreakableNavigableMap` correctly inherits and respects breaks from parent classes.
    @Test
    @DisplayName("Test inheritance from BreakableSortedMap")
    public void inheritance_fromParentClasses_respectsBreaks() {
        BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap.Builder<String, Integer>()
                .addBreak(BreakableMap.GET_ALWAYS_RETURNS_NULL)     // Map-level break
                .addBreak(BreakableSortedMap.FIRST_KEY_THROWS_EXCEPTION) // SortedMap-level break
                .addBreak(HIGHER_KEY_ALWAYS_RETURNS_NULL)           // NavigableMap-level break
                .build();

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