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

public class BreakableConcurrentNavigableMapTest extends AbstractTest {

    @Test
    @DisplayName("Test default constructor creates empty concurrent navigable map")
    public void testDefaultConstructor() {
        final BreakableConcurrentNavigableMap<String, Integer> map = new BreakableConcurrentNavigableMap<>();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    @Test
    @DisplayName("Test constructor with ConcurrentSkipListMap")
    public void testConstructorWithConcurrentSkipListMap() {
        final ConcurrentSkipListMap<String, Integer> skipListMap = new ConcurrentSkipListMap<>();
        skipListMap.put("key1", 1);
        final BreakableConcurrentNavigableMap<String, Integer> map = new BreakableConcurrentNavigableMap<>(skipListMap);
        assertEquals(1, map.size());
        assertEquals(Integer.valueOf(1), map.get("key1"));
    }

    @Test
    @DisplayName("Test copy constructor")
    public void testCopyConstructor() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(COMPARATOR_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> original = builder.build();

        final BreakableConcurrentNavigableMap<String, Integer> copy = new BreakableConcurrentNavigableMap<>(original);
        assertTrue(copy.hasBreak(COMPARATOR_RETURNS_NULL));
    }

    @Test
    @DisplayName("Test wrap factory method")
    public void testWrapFactoryMethod() {
        final ConcurrentSkipListMap<String, Integer> skipListMap = new ConcurrentSkipListMap<>();
        skipListMap.put("key1", 1);
        final BreakableConcurrentNavigableMap<String, Integer> map = BreakableConcurrentNavigableMap.wrap(skipListMap);
        assertEquals(1, map.size());
        assertEquals(Integer.valueOf(1), map.get("key1"));
    }

    @Test
    @DisplayName("Test builder pattern")
    public void testBuilderPattern() {
        final BreakableConcurrentNavigableMap<String, Integer> map = (BreakableConcurrentNavigableMap<String, Integer>) BreakableConcurrentNavigableMap.<String, Integer>builder()
                .addBreak(FIRST_KEY_THROWS_EXCEPTION)
                .build();
        assertTrue(map.hasBreak(FIRST_KEY_THROWS_EXCEPTION));
    }

    @Test
    @DisplayName("Test builder with existing ConcurrentNavigableMap")
    public void testBuilderWithExistingConcurrentNavigableMap() {
        final ConcurrentSkipListMap<String, Integer> skipListMap = new ConcurrentSkipListMap<>();
        skipListMap.put("key1", 1);
        final BreakableConcurrentNavigableMap<String, Integer> map = new BreakableConcurrentNavigableMap.Builder<String, Integer>()
                .withConcurrentNavigableMap(skipListMap)
                .build();
        assertEquals(1, map.size());
        assertEquals(Integer.valueOf(1), map.get("key1"));
    }

    @Test
    @DisplayName("Test builder copy")
    public void testBuilderCopy() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> original = new BreakableConcurrentNavigableMap.Builder<>();
        original.addBreak(LAST_KEY_THROWS_EXCEPTION);

        final BreakableConcurrentNavigableMap.Builder<String, Integer> copy = original.copy();
        final BreakableConcurrentNavigableMap<String, Integer> map = copy.build();
        assertTrue(map.hasBreak(LAST_KEY_THROWS_EXCEPTION));
    }

    @Test
    @DisplayName("Test COMPARATOR_RETURNS_NULL break")
    public void testComparatorReturnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(COMPARATOR_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();

        assertNull(map.comparator());
    }

    @Test
    @DisplayName("Test FIRST_KEY_THROWS_EXCEPTION break")
    public void testFirstKeyThrowsException() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(FIRST_KEY_THROWS_EXCEPTION);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("key1", 1);

        assertThrows(NoSuchElementException.class, map::firstKey);
    }

    @Test
    @DisplayName("Test LAST_KEY_THROWS_EXCEPTION break")
    public void testLastKeyThrowsException() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(LAST_KEY_THROWS_EXCEPTION);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("key1", 1);

        assertThrows(NoSuchElementException.class, map::lastKey);
    }

    @Test
    @DisplayName("Test LOWER_ENTRY_RETURNS_NULL break")
    public void testLowerEntryReturnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(LOWER_ENTRY_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);
        map.put("c", 3);

        assertNull(map.lowerEntry("b"));
    }

    @Test
    @DisplayName("Test LOWER_KEY_RETURNS_NULL break")
    public void testLowerKeyReturnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(LOWER_KEY_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);
        map.put("c", 3);

        assertNull(map.lowerKey("b"));
    }

    @Test
    @DisplayName("Test FLOOR_ENTRY_RETURNS_NULL break")
    public void testFloorEntryReturnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(FLOOR_ENTRY_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);

        assertNull(map.floorEntry("a"));
    }

    @Test
    @DisplayName("Test FLOOR_KEY_RETURNS_NULL break")
    public void testFloorKeyReturnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(FLOOR_KEY_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);

        assertNull(map.floorKey("a"));
    }

    @Test
    @DisplayName("Test CEILING_ENTRY_RETURNS_NULL break")
    public void testCeilingEntryReturnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(CEILING_ENTRY_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);

        assertNull(map.ceilingEntry("a"));
    }

    @Test
    @DisplayName("Test CEILING_KEY_RETURNS_NULL break")
    public void testCeilingKeyReturnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(CEILING_KEY_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);

        assertNull(map.ceilingKey("a"));
    }

    @Test
    @DisplayName("Test HIGHER_ENTRY_RETURNS_NULL break")
    public void testHigherEntryReturnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(HIGHER_ENTRY_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);
        map.put("c", 3);

        assertNull(map.higherEntry("b"));
    }

    @Test
    @DisplayName("Test HIGHER_KEY_RETURNS_NULL break")
    public void testHigherKeyReturnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(HIGHER_KEY_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);
        map.put("c", 3);

        assertNull(map.higherKey("b"));
    }

    @Test
    @DisplayName("Test FIRST_ENTRY_RETURNS_NULL break")
    public void testFirstEntryReturnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(FIRST_ENTRY_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);

        assertNull(map.firstEntry());
    }

    @Test
    @DisplayName("Test LAST_ENTRY_RETURNS_NULL break")
    public void testLastEntryReturnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(LAST_ENTRY_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);

        assertNull(map.lastEntry());
    }

    @Test
    @DisplayName("Test POLL_FIRST_ENTRY_RETURNS_NULL break")
    public void testPollFirstEntryReturnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(POLL_FIRST_ENTRY_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);

        assertNull(map.pollFirstEntry());
    }

    @Test
    @DisplayName("Test POLL_LAST_ENTRY_RETURNS_NULL break")
    public void testPollLastEntryReturnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(POLL_LAST_ENTRY_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);

        assertNull(map.pollLastEntry());
    }

    @Test
    @DisplayName("Test DESCENDING_MAP_RETURNS_NULL break")
    public void testDescendingMapReturnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(DESCENDING_MAP_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();

        assertNull(map.descendingMap());
    }

    @Test
    @DisplayName("Test NAVIGABLE_KEY_SET_RETURNS_NULL break")
    public void testNavigableKeySetReturnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(NAVIGABLE_KEY_SET_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();

        assertNull(map.navigableKeySet());
    }

    @Test
    @DisplayName("Test DESCENDING_KEY_SET_RETURNS_NULL break")
    public void testDescendingKeySetReturnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(DESCENDING_KEY_SET_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();

        assertNull(map.descendingKeySet());
    }

    @Test
    @DisplayName("Test SUB_MAP_RETURNS_NULL break")
    public void testSubMapReturnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(SUB_MAP_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();

        assertNull(map.subMap("a", true, "z", true));
        assertNull(map.subMap("a", "z"));
    }

    @Test
    @DisplayName("Test HEAD_MAP_RETURNS_NULL break")
    public void testHeadMapReturnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(HEAD_MAP_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();

        assertNull(map.headMap("z", true));
        assertNull(map.headMap("z"));
    }

    @Test
    @DisplayName("Test TAIL_MAP_RETURNS_NULL break")
    public void testTailMapReturnsNull() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(TAIL_MAP_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();

        assertNull(map.tailMap("a", true));
        assertNull(map.tailMap("a"));
    }

    @Test
    @DisplayName("Test SUB_MAP_IGNORES_BOUNDS break")
    public void testSubMapIgnoresBounds() {
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

    @Test
    @DisplayName("Test HEAD_MAP_IGNORES_INCLUSIVE break")
    public void testHeadMapIgnoresInclusive() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(HEAD_MAP_IGNORES_INCLUSIVE);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);
        map.put("b", 2);

        final ConcurrentNavigableMap<String, Integer> headMap = map.headMap("b", false);
        assertTrue(headMap.containsKey("b"));
    }

    @Test
    @DisplayName("Test TAIL_MAP_IGNORES_INCLUSIVE break")
    public void testTailMapIgnoresInclusive() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(TAIL_MAP_IGNORES_INCLUSIVE);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);
        map.put("b", 2);

        final ConcurrentNavigableMap<String, Integer> tailMap = map.tailMap("a", false);
        assertTrue(tailMap.containsKey("a"));
    }

    @Test
    @DisplayName("Test NAVIGATION_RACE_CONDITION break")
    public void testNavigationRaceCondition() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(NAVIGATION_RACE_CONDITION);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();
        map.put("a", 1);
        map.put("c", 3);

        final Map.Entry<String, Integer> result = map.lowerEntry("b");
        assertNotNull(result);
        assertEquals("a", result.getKey());
    }

    @Test
    @DisplayName("Test normal concurrent navigable map operations")
    public void testNormalConcurrentNavigableMapOperations() {
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

    @Test
    @DisplayName("Test ConcurrentSkipListMap integration")
    public void testConcurrentSkipListMapIntegration() {
        final ConcurrentSkipListMap<String, Integer> skipListMap = new ConcurrentSkipListMap<>();
        skipListMap.put("key1", 1);
        skipListMap.put("key2", 2);

        final BreakableConcurrentNavigableMap<String, Integer> map = BreakableConcurrentNavigableMap.wrap(skipListMap);
        assertEquals(2, map.size());
        assertEquals("key1", map.firstKey());
        assertEquals("key2", map.lastKey());
    }

    @Test
    @DisplayName("Test inheritance from BreakableConcurrentMap")
    public void testInheritanceFromBreakableConcurrentMap() {
        final BreakableConcurrentNavigableMap<String, Integer> map = new BreakableConcurrentNavigableMap<>();
        assertInstanceOf(BreakableConcurrentMap.class, map);
        assertInstanceOf(ConcurrentNavigableMap.class, map);

        map.put("key1", 1);
        assertEquals(Integer.valueOf(1), map.get("key1"));
        assertEquals(Integer.valueOf(1), map.putIfAbsent("key1", 2));
    }

    @Test
    @DisplayName("Test inherited Map breaks still work")
    public void testInheritedMapBreaksStillWork() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(BreakableConcurrentMap.PUT_IF_ABSENT_ALWAYS_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();

        final Integer result = map.putIfAbsent("key1", 1);
        assertNull(result);
        assertTrue(map.containsKey("key1"));
    }

    @Test
    @DisplayName("Test unsupported method exceptions")
    public void testUnsupportedMethodExceptions() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.doesNotSupport(SortedMapMethods.FIRST_KEY);
        builder.doesNotSupport(SortedMapMethods.LAST_KEY);
        builder.doesNotSupport(NavigableMapMethods.NAVIGABLE_KEY_SET);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();

        assertThrows(UnsupportedOperationException.class, map::firstKey);
        assertThrows(UnsupportedOperationException.class, map::lastKey);
        assertThrows(UnsupportedOperationException.class, map::navigableKeySet);
    }

    @Test
    @DisplayName("Test multiple breaks interaction")
    public void testMultipleBreaksInteraction() {
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

    @Test
    @DisplayName("Test navigation operations without breaks")
    public void testNavigationOperationsWithoutBreaks() {
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

    @Test
    @DisplayName("Test break priority and interaction")
    public void testBreakPriorityAndInteraction() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(SUB_MAP_RETURNS_NULL);
        builder.addBreak(SUB_MAP_IGNORES_BOUNDS);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();

        assertNull(map.subMap("a", true, "z", true));
    }

    @Test
    @DisplayName("Test keySet delegates to navigableKeySet")
    public void testKeySetDelegatesToNavigableKeySet() {
        final BreakableConcurrentNavigableMap.Builder<String, Integer> builder = new BreakableConcurrentNavigableMap.Builder<>();
        builder.addBreak(NAVIGABLE_KEY_SET_RETURNS_NULL);
        final BreakableConcurrentNavigableMap<String, Integer> map = builder.build();

        assertNull(map.keySet());
    }
}