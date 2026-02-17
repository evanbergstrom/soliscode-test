package org.soliscode.test.breakable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.navigablemap.NavigableMapMethods;
import org.soliscode.test.contract.sortedmap.SortedMapMethods;

import java.util.AbstractMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.breakable.BreakableConcurrentNavigableMap.*;

///
/// Tests for the `BreakableConcurrentNavigableMap` class, ensuring that it correctly implements the
/// `ConcurrentNavigableMap` interface and properly simulates various "broken" behaviors through
/// its break system.
///
/// ## Purpose
/// The primary purpose of these tests is to verify that:
/// 1. The `BreakableConcurrentNavigableMap` correctly delegates normal operations to an underlying
///    `ConcurrentSkipListMap` when no breaks are present.
/// 2. Specific navigation and view breaks (e.g., `LOWER_ENTRY_RETURNS_NULL`, `DESCENDING_MAP_RETURNS_NULL`)
///    correctly intercept and modify the behavior of the corresponding methods.
/// 3. The builder pattern and factory methods correctly configure the breakable map's state.
/// 4. Inherited map and concurrent map breaks function correctly within the navigable map context.
///
/// ## Usage Examples
/// These tests demonstrate how to use `BreakableConcurrentNavigableMap` to simulate edge cases
/// and faulty implementations of `ConcurrentNavigableMap`:
///
/// ```java
/// // Simulate a map where firstKey() always throws NoSuchElementException
/// BreakableConcurrentNavigableMap<String, Integer> map = BreakableConcurrentNavigableMap.<String, Integer>builder()
///     .addBreak(FIRST_KEY_THROWS_EXCEPTION)
///     .build();
/// ```
///
/// ## Thread Safety
/// While the underlying implementation (`ConcurrentSkipListMap`) is thread-safe, these tests are
/// generally executed in a single-threaded environment to verify the deterministic effects of the
/// injected breaks. The `NAVIGATION_RACE_CONDITION` break specifically simulates a "lost update"
/// or race condition during navigation.
///
/// @author Evan Bergstrom
/// @since 1.0.0
/// @see BreakableConcurrentNavigableMap
/// @see ConcurrentNavigableMap
public class BreakableConcurrentNavigableMapTest extends AbstractTest {

    /// Verifies that the default constructor creates an empty map.
    @Test
    @DisplayName("defaultConstructor: creates empty concurrent navigable map")
    public void defaultConstructor_whenCalled_createsEmptyMap() {
        final BreakableConcurrentNavigableMap<String, Integer> map = new BreakableConcurrentNavigableMap<>();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    /// Verifies that the constructor correctly wraps an existing ConcurrentSkipListMap.
    @Test
    @DisplayName("constructorWithConcurrentSkipListMap: initializes with existing entries")
    public void constructorWithConcurrentSkipListMap_whenCalled_initializesWithEntries() {
        final ConcurrentSkipListMap<String, Integer> skipListMap = new ConcurrentSkipListMap<>();
        skipListMap.put("key1", 1);
        final BreakableConcurrentNavigableMap<String, Integer> map = new BreakableConcurrentNavigableMap<>(skipListMap);
        assertEquals(1, map.size());
        assertEquals(Integer.valueOf(1), map.get("key1"));
    }

    /// Verifies that the copy constructor duplicates breaks from the original map.
    @Test
    @DisplayName("copyConstructor: duplicates breaks from original map")
    public void copyConstructor_whenCalled_duplicatesBreaks() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(COMPARATOR_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> original = builder.build();

        final BreakableConcurrentNavigableMap<String, Integer> copy = new BreakableConcurrentNavigableMap<>(original);
        assertTrue(copy.hasBreak(COMPARATOR_RETURNS_NULL));
    }

    /// Verifies that the wrap factory method correctly wraps an existing map.
    @Test
    @DisplayName("wrap: creates breakable map from existing ConcurrentNavigableMap")
    public void wrap_whenCalled_wrapsExistingMap() {
        final ConcurrentSkipListMap<String, Integer> skipListMap = new ConcurrentSkipListMap<>();
        skipListMap.put("key1", 1);
        final BreakableConcurrentNavigableMap<String, Integer> map = BreakableConcurrentNavigableMap.wrap(skipListMap);
        assertEquals(1, map.size());
        assertEquals(Integer.valueOf(1), map.get("key1"));
    }

    /// Verifies that the builder pattern correctly configures breaks.
    @Test
    @DisplayName("builderPattern: configures breaks using fluent API")
    public void builderPattern_whenUsed_configuresBreaks() {
        final BreakableConcurrentNavigableMap<String, Integer> map = (BreakableConcurrentNavigableMap<String, Integer>) BreakableConcurrentNavigableMap.<String, Integer>builder()
                .addBreak(FIRST_KEY_THROWS_EXCEPTION)
                .build();
        assertTrue(map.hasBreak(FIRST_KEY_THROWS_EXCEPTION));
    }

    /// Verifies that the builder can be initialized with an existing ConcurrentNavigableMap.
    @Test
    @DisplayName("builderWithExistingConcurrentNavigableMap: initializes builder with map")
    public void builderWithConcurrentNavigableMap_whenUsed_initializesWithEntries() {
        final ConcurrentSkipListMap<String, Integer> skipListMap = new ConcurrentSkipListMap<>();
        skipListMap.put("key1", 1);
        final BreakableConcurrentNavigableMap<String, Integer> map = new BreakableConcurrentNavigableMap.Builder<String, Integer>()
                .withConcurrentNavigableMap(skipListMap)
                .build();
        assertEquals(1, map.size());
        assertEquals(Integer.valueOf(1), map.get("key1"));
    }

    /// Verifies that the builder's copy method creates a new builder with the same breaks.
    @Test
    @DisplayName("builderCopy: creates a new builder with duplicated configuration")
    public void builderCopy_whenCalled_duplicatesConfiguration() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> original = new BreakableConcurrentNavigableMap.Builder<>();
        original.addBreak(LAST_KEY_THROWS_EXCEPTION);

        final BreakableConcurrentNavigableMap.Builder<String, Integer> copy = original.copy();
        final BreakableConcurrentNavigableMap<String, Integer> map = copy.build();
        assertTrue(map.hasBreak(LAST_KEY_THROWS_EXCEPTION));
    }

    /// Verifies that the COMPARATOR_RETURNS_NULL break causes the comparator method to return null.
    @Test
    @DisplayName("comparator: returns null when COMPARATOR_RETURNS_NULL break is added")
    public void comparator_whenComparatorReturnsNullBreakAdded_returnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(COMPARATOR_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();

        assertNull(map.comparator());
    }

    /// Verifies that the FIRST_KEY_THROWS_EXCEPTION break causes firstKey() to throw NoSuchElementException.
    @Test
    @DisplayName("firstKey: throws NoSuchElementException when FIRST_KEY_THROWS_EXCEPTION break is added")
    public void firstKey_whenFirstKeyThrowsExceptionBreakAdded_throwsNoSuchElementException() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(FIRST_KEY_THROWS_EXCEPTION);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("key1", 1);

        assertThrows(NoSuchElementException.class, map::firstKey);
    }

    /// Verifies that the LAST_KEY_THROWS_EXCEPTION break causes lastKey() to throw NoSuchElementException.
    @Test
    @DisplayName("lastKey: throws NoSuchElementException when LAST_KEY_THROWS_EXCEPTION break is added")
    public void lastKey_whenLastKeyThrowsExceptionBreakAdded_throwsNoSuchElementException() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(LAST_KEY_THROWS_EXCEPTION);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("key1", 1);

        assertThrows(NoSuchElementException.class, map::lastKey);
    }

    /// Verifies that the LOWER_ENTRY_RETURNS_NULL break causes lowerEntry() to return null.
    @Test
    @DisplayName("lowerEntry: returns null when LOWER_ENTRY_RETURNS_NULL break is added")
    public void lowerEntry_whenLowerEntryReturnsNullBreakAdded_returnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(LOWER_ENTRY_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);
        map.put("c", 3);

        assertNull(map.lowerEntry("b"));
    }

    /// Verifies that the LOWER_KEY_RETURNS_NULL break causes lowerKey() to return null.
    @Test
    @DisplayName("lowerKey: returns null when LOWER_KEY_RETURNS_NULL break is added")
    public void lowerKey_whenLowerKeyReturnsNullBreakAdded_returnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(LOWER_KEY_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);
        map.put("c", 3);

        assertNull(map.lowerKey("b"));
    }

    /// Verifies that the FLOOR_ENTRY_RETURNS_NULL break causes floorEntry() to return null.
    @Test
    @DisplayName("floorEntry: returns null when FLOOR_ENTRY_RETURNS_NULL break is added")
    public void floorEntry_whenFloorEntryReturnsNullBreakAdded_returnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(FLOOR_ENTRY_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);

        assertNull(map.floorEntry("a"));
    }

    /// Verifies that the FLOOR_KEY_RETURNS_NULL break causes floorKey() to return null.
    @Test
    @DisplayName("floorKey: returns null when FLOOR_KEY_RETURNS_NULL break is added")
    public void floorKey_whenFloorKeyReturnsNullBreakAdded_returnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(FLOOR_KEY_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);

        assertNull(map.floorKey("a"));
    }

    /// Verifies that the CEILING_ENTRY_RETURNS_NULL break causes ceilingEntry() to return null.
    @Test
    @DisplayName("ceilingEntry: returns null when CEILING_ENTRY_RETURNS_NULL break is added")
    public void ceilingEntry_whenCeilingEntryReturnsNullBreakAdded_returnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(CEILING_ENTRY_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);

        assertNull(map.ceilingEntry("a"));
    }

    /// Verifies that the CEILING_KEY_RETURNS_NULL break causes ceilingKey() to return null.
    @Test
    @DisplayName("ceilingKey: returns null when CEILING_KEY_RETURNS_NULL break is added")
    public void ceilingKey_whenCeilingKeyReturnsNullBreakAdded_returnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(CEILING_KEY_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);

        assertNull(map.ceilingKey("a"));
    }

    /// Verifies that the HIGHER_ENTRY_RETURNS_NULL break causes higherEntry() to return null.
    @Test
    @DisplayName("higherEntry: returns null when HIGHER_ENTRY_RETURNS_NULL break is added")
    public void higherEntry_whenHigherEntryReturnsNullBreakAdded_returnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(HIGHER_ENTRY_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);
        map.put("c", 3);

        assertNull(map.higherEntry("b"));
    }

    /// Verifies that the HIGHER_KEY_RETURNS_NULL break causes higherKey() to return null.
    @Test
    @DisplayName("higherKey: returns null when HIGHER_KEY_RETURNS_NULL break is added")
    public void higherKey_whenHigherKeyReturnsNullBreakAdded_returnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(HIGHER_KEY_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);
        map.put("c", 3);

        assertNull(map.higherKey("b"));
    }

    /// Verifies that the FIRST_ENTRY_RETURNS_NULL break causes firstEntry() to return null.
    @Test
    @DisplayName("firstEntry: returns null when FIRST_ENTRY_RETURNS_NULL break is added")
    public void firstEntry_whenFirstEntryReturnsNullBreakAdded_returnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(FIRST_ENTRY_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);

        assertNull(map.firstEntry());
    }

    /// Verifies that the LAST_ENTRY_RETURNS_NULL break causes lastEntry() to return null.
    @Test
    @DisplayName("lastEntry: returns null when LAST_ENTRY_RETURNS_NULL break is added")
    public void lastEntry_whenLastEntryReturnsNullBreakAdded_returnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(LAST_ENTRY_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);

        assertNull(map.lastEntry());
    }

    /// Verifies that the POLL_FIRST_ENTRY_RETURNS_NULL break causes pollFirstEntry() to return null.
    @Test
    @DisplayName("pollFirstEntry: returns null when POLL_FIRST_ENTRY_RETURNS_NULL break is added")
    public void pollFirstEntry_whenPollFirstEntryReturnsNullBreakAdded_returnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(POLL_FIRST_ENTRY_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);

        assertNull(map.pollFirstEntry());
    }

    /// Verifies that the POLL_LAST_ENTRY_RETURNS_NULL break causes pollLastEntry() to return null.
    @Test
    @DisplayName("pollLastEntry: returns null when POLL_LAST_ENTRY_RETURNS_NULL break is added")
    public void pollLastEntry_whenPollLastEntryReturnsNullBreakAdded_returnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(POLL_LAST_ENTRY_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);

        assertNull(map.pollLastEntry());
    }

    /// Verifies that the DESCENDING_MAP_RETURNS_NULL break causes descendingMap() to return null.
    @Test
    @DisplayName("descendingMap: returns null when DESCENDING_MAP_RETURNS_NULL break is added")
    public void descendingMap_whenDescendingMapReturnsNullBreakAdded_returnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(DESCENDING_MAP_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();

        assertNull(map.descendingMap());
    }

    /// Verifies that the NAVIGABLE_KEY_SET_RETURNS_NULL break causes navigableKeySet() to return null.
    @Test
    @DisplayName("navigableKeySet: returns null when NAVIGABLE_KEY_SET_RETURNS_NULL break is added")
    public void navigableKeySet_whenNavigableKeySetReturnsNullBreakAdded_returnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(NAVIGABLE_KEY_SET_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();

        assertNull(map.navigableKeySet());
    }

    /// Verifies that the DESCENDING_KEY_SET_RETURNS_NULL break causes descendingKeySet() to return null.
    @Test
    @DisplayName("descendingKeySet: returns null when DESCENDING_KEY_SET_RETURNS_NULL break is added")
    public void descendingKeySet_whenDescendingKeySetReturnsNullBreakAdded_returnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(DESCENDING_KEY_SET_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();

        assertNull(map.descendingKeySet());
    }

    /// Verifies that the SUB_MAP_RETURNS_NULL break causes subMap() to return null.
    @Test
    @DisplayName("subMap: returns null when SUB_MAP_RETURNS_NULL break is added")
    public void subMap_whenSubMapReturnsNullBreakAdded_returnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(SUB_MAP_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();

        assertNull(map.subMap("a", true, "z", true));
        assertNull(map.subMap("a", "z"));
    }

    /// Verifies that the HEAD_MAP_RETURNS_NULL break causes headMap() to return null.
    @Test
    @DisplayName("headMap: returns null when HEAD_MAP_RETURNS_NULL break is added")
    public void headMap_whenHeadMapReturnsNullBreakAdded_returnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(HEAD_MAP_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();

        assertNull(map.headMap("z", true));
        assertNull(map.headMap("z"));
    }

    /// Verifies that the TAIL_MAP_RETURNS_NULL break causes tailMap() to return null.
    @Test
    @DisplayName("tailMap: returns null when TAIL_MAP_RETURNS_NULL break is added")
    public void tailMap_whenTailMapReturnsNullBreakAdded_returnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(TAIL_MAP_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();

        assertNull(map.tailMap("a", true));
        assertNull(map.tailMap("a"));
    }

    /// Verifies that the SUB_MAP_IGNORES_BOUNDS break causes subMap() to include all entries regardless of bounds.
    @Test
    @DisplayName("subMap: ignores bounds when SUB_MAP_IGNORES_BOUNDS break is added")
    public void subMap_whenSubMapIgnoresBoundsBreakAdded_ignoresBounds() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(SUB_MAP_IGNORES_BOUNDS);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);

        // With the break, subMap should ignore bounds and include all elements
        // Normal behavior: subMap("b", false, "c", false) would return empty (excludes both "b" and "c")
        // Break behavior: should return all elements as it uses inclusive bounds (true, true)
        final ConcurrentNavigableMap<String, Integer> subMap = map.subMap("b", false, "c", false);
        assertEquals(3, subMap.size());
        assertTrue(subMap.containsKey("a"));
        assertTrue(subMap.containsKey("b"));
        assertTrue(subMap.containsKey("c"));
    }

    /// Verifies that the HEAD_MAP_IGNORES_INCLUSIVE break causes headMap() to include the bound even if specified as false.
    @Test
    @DisplayName("headMap: ignores inclusive flag when HEAD_MAP_IGNORES_INCLUSIVE break is added")
    public void headMap_whenHeadMapIgnoresInclusiveBreakAdded_ignoresInclusiveFlag() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(HEAD_MAP_IGNORES_INCLUSIVE);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);
        map.put("b", 2);

        final ConcurrentNavigableMap<String, Integer> headMap = map.headMap("b", false);
        assertTrue(headMap.containsKey("b"));
    }

    /// Verifies that the TAIL_MAP_IGNORES_INCLUSIVE break causes tailMap() to include the bound even if specified as false.
    @Test
    @DisplayName("tailMap: ignores inclusive flag when TAIL_MAP_IGNORES_INCLUSIVE break is added")
    public void tailMap_whenTailMapIgnoresInclusiveBreakAdded_ignoresInclusiveFlag() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(TAIL_MAP_IGNORES_INCLUSIVE);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);
        map.put("b", 2);

        final ConcurrentNavigableMap<String, Integer> tailMap = map.tailMap("a", false);
        assertTrue(tailMap.containsKey("a"));
    }

    /// Verifies that the NAVIGATION_RACE_CONDITION break simulates a race condition during navigation operations.
    @Test
    @DisplayName("lowerEntry: simulates race condition when NAVIGATION_RACE_CONDITION break is added")
    public void lowerEntry_whenNavigationRaceConditionBreakAdded_simulatesRaceCondition() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(NAVIGATION_RACE_CONDITION);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);
        map.put("c", 3);

        final Map.Entry<String, Integer> result = map.lowerEntry("b");
        assertNotNull(result);
        assertEquals("a", result.getKey());
    }

    /// Verifies that normal concurrent navigable map operations function correctly when no breaks are present.
    @Test
    @DisplayName("concurrentNavigableMap: executes normal operations when no breaks are present")
    public void concurrentNavigableMap_whenNoBreaks_executesNormalOperations() {
        final BreakableConcurrentNavigableMap<String, Integer> map = new BreakableConcurrentNavigableMap<>();
        map.put("b", 2);
        map.put("a", 1);
        map.put("c", 3);

        assertEquals("a", map.firstKey());
        assertEquals("c", map.lastKey());
        assertEquals(new AbstractMap.SimpleEntry<>("a", 1), map.firstEntry());
        assertEquals(new AbstractMap.SimpleEntry<>("c", 3), map.lastEntry());

        assertNull(map.lowerEntry("a"));
        assertEquals(new AbstractMap.SimpleEntry<>("a", 1), map.floorEntry("a"));
        assertEquals(new AbstractMap.SimpleEntry<>("b", 2), map.ceilingEntry("b"));
        assertNull(map.higherEntry("c"));

        final ConcurrentNavigableMap<String, Integer> subMap = map.subMap("a", true, "c", false);
        assertEquals(2, subMap.size());
        assertTrue(subMap.containsKey("a"));
        assertTrue(subMap.containsKey("b"));
        assertFalse(subMap.containsKey("c"));
    }

    /// Verifies correctly wrapping and using an underlying ConcurrentSkipListMap.
    @Test
    @DisplayName("concurrentSkipListMap: integrates correctly with underlying skip list map")
    public void concurrentSkipListMap_whenWrapped_integratesCorrectly() {
        final ConcurrentSkipListMap<String, Integer> skipListMap = new ConcurrentSkipListMap<>();
        skipListMap.put("key1", 1);
        skipListMap.put("key2", 2);

        final BreakableConcurrentNavigableMap<String, Integer> map = BreakableConcurrentNavigableMap.wrap(skipListMap);
        assertEquals(2, map.size());
        assertEquals("key1", map.firstKey());
        assertEquals("key2", map.lastKey());
    }

    /// Verifies that BreakableConcurrentNavigableMap correctly inherits from BreakableConcurrentMap.
    @Test
    @DisplayName("inheritance: correctly inherits from BreakableConcurrentMap and ConcurrentNavigableMap")
    public void inheritance_whenChecked_isCorrect() {
        final BreakableConcurrentNavigableMap<String, Integer> map = new BreakableConcurrentNavigableMap<>();
        assertInstanceOf(BreakableConcurrentMap.class, map);
        assertInstanceOf(ConcurrentNavigableMap.class, map);

        map.put("key1", 1);
        assertEquals(Integer.valueOf(1), map.get("key1"));
        assertEquals(Integer.valueOf(1), map.putIfAbsent("key1", 2));
    }

    /// Verifies that breaks inherited from BreakableConcurrentMap still function correctly.
    @Test
    @DisplayName("inheritedMapBreaks: inherited concurrent map breaks still function correctly")
    public void inheritedMapBreaks_whenAdded_stillWork() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(BreakableConcurrentMap.PUT_IF_ABSENT_ALWAYS_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();

        final Integer result = map.putIfAbsent("key1", 1);
        assertNull(result);
        assertTrue(map.containsKey("key1"));
    }

    /// Verifies that methods marked as unsupported in the builder throw UnsupportedOperationException.
    @Test
    @DisplayName("unsupportedMethods: throws UnsupportedOperationException for disabled methods")
    public void unsupportedMethods_whenCalled_throwUnsupportedOperationException() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.doesNotSupport(SortedMapMethods.FIRST_KEY);
        builder.doesNotSupport(SortedMapMethods.LAST_KEY);
        builder.doesNotSupport(NavigableMapMethods.NAVIGABLE_KEY_SET);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();

        assertThrows(UnsupportedOperationException.class, map::firstKey);
        assertThrows(UnsupportedOperationException.class, map::lastKey);
        assertThrows(UnsupportedOperationException.class, map::navigableKeySet);
    }

    /// Verifies that multiple breaks can be added and function independently.
    @Test
    @DisplayName("multipleBreaks: multiple breaks function independently when added together")
    public void multipleBreaks_whenAddedTogether_functionIndependently() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(FIRST_ENTRY_RETURNS_NULL);
        builder.addBreak(LAST_ENTRY_RETURNS_NULL);
        builder.addBreak(NAVIGABLE_KEY_SET_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("key1", 1);

        assertNull(map.firstEntry());
        assertNull(map.lastEntry());
        assertNull(map.navigableKeySet());
    }

    /// Verifies that navigation operations work as expected when no breaks are applied.
    @Test
    @DisplayName("navigationOperations: navigation operations work correctly without breaks")
    public void navigationOperations_whenNoBreaks_workCorrectly() {
        final BreakableConcurrentNavigableMap<String, Integer> map = new BreakableConcurrentNavigableMap<>();
        map.put("b", 2);
        map.put("d", 4);
        map.put("f", 6);

        assertEquals("b", map.lowerKey("c"));
        assertEquals("b", map.floorKey("b"));
        assertEquals("d", map.ceilingKey("d"));
        assertEquals("f", map.higherKey("e"));

        final NavigableSet<String> keySet = map.navigableKeySet();
        assertNotNull(keySet);
        assertEquals(3, keySet.size());
        assertTrue(keySet.contains("b"));
        assertTrue(keySet.contains("d"));
        assertTrue(keySet.contains("f"));
    }

    /// Verifies how multiple breaks targeting the same operation interact (priority).
    @Test
    @DisplayName("breakPriority: verifies interaction between multiple breaks on same operation")
    public void breakPriority_whenMultipleBreaksOnSameOperation_followsInteractionRules() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(SUB_MAP_RETURNS_NULL);
        builder.addBreak(SUB_MAP_IGNORES_BOUNDS);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();

        assertNull(map.subMap("a", true, "z", true));
    }

    /// Verifies that keySet() correctly delegates to navigableKeySet(), inheriting its breaks.
    @Test
    @DisplayName("keySet: correctly delegates to navigableKeySet and inherits its breaks")
    public void keySet_whenNavigableKeySetBroken_alsoReturnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(NAVIGABLE_KEY_SET_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();

        assertNull(map.keySet());
    }
}