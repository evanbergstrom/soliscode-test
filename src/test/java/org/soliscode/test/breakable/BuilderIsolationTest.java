package org.soliscode.test.breakable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests that builders build objects holding their own copy of the builder's state, that `build(Iterable)` and
/// `build(Map)` hold both the builder's elements and the argument's, and that copies are independent of the original.
///
/// Each test fails on a builder or copy-constructor fault fixed by soliscode/soliscode-test#4.
@DisplayName("Builders and copies do not share state")
public class BuilderIsolationTest {

    /// Default constructor.
    public BuilderIsolationTest() {
        super();
    }

    // ========== build(Iterable) holds the builder's elements and the argument's ==========

    @Test
    @DisplayName("NavigableSet builder: build(Iterable) keeps the builder's elements")
    void navigableSetBuilder_buildIterable_keepsBuilderElements() {
        BreakableNavigableSet<Integer> set = new BreakableNavigableSet.Builder<Integer>()
                .addElements(3, 1)
                .build(List.of(2));
        assertEquals(List.of(1, 2, 3), new ArrayList<>(set));
    }

    @Test
    @DisplayName("BlockingDeque builder: build(Iterable) keeps the argument's elements")
    void blockingDequeBuilder_buildIterable_keepsArgumentElements() {
        BreakableBlockingDeque<Integer> deque = new BreakableBlockingDeque.Builder<Integer>()
                .addElements(1)
                .build(List.of(2, 3));
        assertEquals(List.of(1, 2, 3), new ArrayList<>(deque));
    }

    @Test
    @DisplayName("BlockingQueue builder: build(Iterable) keeps the builder's and the argument's elements")
    void blockingQueueBuilder_buildIterable_keepsAllElements() {
        BreakableBlockingQueue<Integer> queue = new BreakableBlockingQueue.Builder<Integer>()
                .addElements(1)
                .build(List.of(2));
        assertEquals(List.of(1, 2), new ArrayList<>(queue));
    }

    @Test
    @DisplayName("SequencedSet builder: elements given to the constructor are built")
    void sequencedSetBuilder_withConstructorElements_buildsThem() {
        BreakableSequencedSet<Integer> set = new BreakableSequencedSet.Builder<>(List.of(3, 1, 2)).build();
        assertEquals(List.of(3, 1, 2), new ArrayList<>(set));
    }

    @Test
    @DisplayName("Map builder: build(Map) keeps the builder's entries and the argument's")
    void mapBuilder_buildMap_keepsAllEntries() {
        BreakableMap<Integer, String> map = new BreakableMap.Builder<Integer, String>()
                .add(1, "a")
                .build(Map.of(2, "b"));
        assertEquals(Map.of(1, "a", 2, "b"), new HashMap<>(map));
    }

    // ========== Queue storage permits nulls and compares by elements ==========

    @Test
    @DisplayName("Queue builder: the queue built permits null elements")
    void queueBuilder_build_permitsNulls() {
        BreakableQueue<Integer> queue = new BreakableQueue.Builder<Integer>().build(Arrays.asList(1, null));
        assertEquals(2, queue.size());
        assertTrue(queue.contains(null));
    }

    @Test
    @DisplayName("Queue builder: two queues built with the same elements are equal")
    void queueBuilder_build_equalByElements() {
        BreakableQueue.Builder<Integer> builder = new BreakableQueue.Builder<Integer>().addElements(1, 2);
        assertEquals(builder.build(), builder.build());
    }

    // ========== Built objects do not share breaks or elements with the builder ==========

    @Test
    @DisplayName("Queue builder: a break added after build does not reach the queue built")
    void queueBuilder_breakAddedAfterBuild_notInQueue() {
        BreakableQueue.Builder<Integer> builder = new BreakableQueue.Builder<>();
        BreakableQueue<Integer> queue = builder.build();
        builder.addBreak(BreakableQueue.OFFER_DOES_NOT_ADD_ELEMENT);
        assertFalse(queue.hasBreak(BreakableQueue.OFFER_DOES_NOT_ADD_ELEMENT));
    }

    @Test
    @DisplayName("TransferQueue builder: a break added after build does not reach the queue built")
    void transferQueueBuilder_breakAddedAfterBuild_notInQueue() {
        BreakableTransferQueue.Builder<Integer> builder = new BreakableTransferQueue.Builder<>();
        BreakableTransferQueue<Integer> queue = builder.build();
        builder.addBreak(BreakableTransferQueue.TRANSFER_THROWS_INTERRUPTED_EXCEPTION);
        assertFalse(queue.hasBreak(BreakableTransferQueue.TRANSFER_THROWS_INTERRUPTED_EXCEPTION));
    }

    @Test
    @DisplayName("Map builder: maps built by one builder do not share entries or breaks")
    void mapBuilder_twoMaps_doNotShareState() {
        BreakableMap.Builder<Integer, String> builder = new BreakableMap.Builder<>();
        BreakableMap<Integer, String> first = builder.build();
        first.put(1, "a");
        builder.addBreak(BreakableMap.SIZE_IS_OFF_BY_ONE);
        BreakableMap<Integer, String> second = builder.build();

        assertFalse(second.containsKey(1));
        assertFalse(first.hasBreak(BreakableMap.SIZE_IS_OFF_BY_ONE));
    }

    @Test
    @DisplayName("Map builder: adding to the builder does not change the map it was given")
    void mapBuilder_add_doesNotChangeCallersMap() {
        Map<Integer, String> given = new HashMap<>();
        new BreakableMap.Builder<>(given).add(1, "a");
        assertTrue(given.isEmpty());
    }

    @Test
    @DisplayName("Deque builder: adding to the builder does not change the collection it was given")
    void dequeBuilder_addElements_doesNotChangeCallersCollection() {
        List<Integer> given = new ArrayList<>();
        new BreakableDeque.Builder<>(given).addElements(1);
        assertTrue(given.isEmpty());
    }

    // ========== Comparators ==========

    @Test
    @DisplayName("SortedSet builder: the comparator given to the constructor orders the set")
    void sortedSetBuilder_withComparator_ordersSet() {
        BreakableSortedSet<Integer> set = new BreakableSortedSet.Builder<Integer>(Comparator.reverseOrder())
                .build(List.of(1, 2, 3));
        assertEquals(3, set.first());
    }

    @Test
    @DisplayName("SortedMap builder: a key the comparator accepts can be added before build")
    void sortedMapBuilder_withNullsFirstComparator_acceptsNullKey() {
        BreakableSortedMap<Integer, String> map = new BreakableSortedMap.Builder<Integer, String>()
                .setComparator(Comparator.nullsFirst(Comparator.<Integer>naturalOrder()))
                .add(1, "a")
                .add(null, "b")
                .build();
        assertNull(map.firstKey());
        assertEquals(2, map.size());
    }

    // ========== Copies are independent of the original and keep its configuration ==========

    @Test
    @DisplayName("SortedMap copy: changing the copy does not change the original, and its views agree")
    void sortedMapCopy_whenChanged_isIndependent() {
        BreakableSortedMap<Integer, String> original = new BreakableSortedMap.Builder<Integer, String>()
                .add(2, "b")
                .build();
        BreakableSortedMap<Integer, String> copy = new BreakableSortedMap<>(original);
        copy.put(1, "a");

        assertEquals(1, original.size());
        assertEquals(2, original.firstKey());
        assertEquals(2, copy.size());
        assertEquals(1, copy.firstKey());
    }

    @Test
    @DisplayName("NavigableMap copy: changing the copy does not change the original, and its views agree")
    void navigableMapCopy_whenChanged_isIndependent() {
        BreakableNavigableMap<Integer, String> original = new BreakableNavigableMap.Builder<Integer, String>()
                .add(2, "b")
                .build();
        BreakableNavigableMap<Integer, String> copy = new BreakableNavigableMap<>(original);
        copy.put(1, "a");

        assertEquals(1, original.size());
        assertEquals(2, original.firstKey());
        assertEquals(2, copy.size());
        assertEquals(1, copy.firstKey());
        assertEquals(1, copy.firstEntry().getKey());
    }

    @Test
    @DisplayName("ConcurrentNavigableMap copy: changing the copy does not change the original")
    void concurrentNavigableMapCopy_whenChanged_isIndependent() {
        BreakableConcurrentNavigableMap<Integer, String> original =
                new BreakableConcurrentNavigableMap.Builder<Integer, String>().add(2, "b").build();
        BreakableConcurrentNavigableMap<Integer, String> copy = new BreakableConcurrentNavigableMap<>(original);
        copy.put(1, "a");

        assertEquals(1, original.size());
        assertEquals(2, copy.size());
        assertEquals(1, copy.firstKey());
    }

    @Test
    @DisplayName("ConcurrentMap copy: keeps the original's permits")
    void concurrentMapCopy_keepsPermits() {
        BreakableConcurrentMap<Integer, String> original = new BreakableConcurrentMap.Builder<Integer, String>()
                .doesNotPermitNullKeys()
                .build();
        assertEquals(original.permits(), new BreakableConcurrentMap<>(original).permits());
    }

    @Test
    @DisplayName("NavigableSet copy: keeps the original's permits")
    void navigableSetCopy_keepsPermits() {
        BreakableNavigableSet<Integer> original = new BreakableNavigableSet.Builder<Integer>()
                .doesNotPermitNulls()
                .build();
        assertEquals(original.permits(), new BreakableNavigableSet<>(original).permits());
    }
}
