package com.soliscode.test.breakable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.collection.CollectionMethods;
import com.soliscode.test.contract.iterable.IterableMethods;
import com.soliscode.test.contract.sequencedcollection.SequencedCollectionMethods;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void navigableSetBuilderBuildIterableKeepsBuilderElements() {
        BreakableNavigableSet<Integer> set = new BreakableNavigableSet.Builder<Integer>()
                .addElements(3, 1)
                .build(List.of(2));
        assertEquals(List.of(1, 2, 3), new ArrayList<>(set));
    }

    @Test
    @DisplayName("BlockingDeque builder: build(Iterable) keeps the argument's elements")
    void blockingDequeBuilderBuildIterableKeepsArgumentElements() {
        BreakableBlockingDeque<Integer> deque = new BreakableBlockingDeque.Builder<Integer>()
                .addElements(1)
                .build(List.of(2, 3));
        assertEquals(List.of(1, 2, 3), new ArrayList<>(deque));
    }

    @Test
    @DisplayName("BlockingQueue builder: build(Iterable) keeps the builder's and the argument's elements")
    void blockingQueueBuilderBuildIterableKeepsAllElements() {
        BreakableBlockingQueue<Integer> queue = new BreakableBlockingQueue.Builder<Integer>()
                .addElements(1)
                .build(List.of(2));
        assertEquals(List.of(1, 2), new ArrayList<>(queue));
    }

    @Test
    @DisplayName("SequencedSet builder: elements given to the constructor are built")
    void sequencedSetBuilderWithConstructorElementsBuildsThem() {
        BreakableSequencedSet<Integer> set = new BreakableSequencedSet.Builder<>(List.of(3, 1, 2)).build();
        assertEquals(List.of(3, 1, 2), new ArrayList<>(set));
    }

    @Test
    @DisplayName("Map builder: build(Map) keeps the builder's entries and the argument's")
    void mapBuilderBuildMapKeepsAllEntries() {
        BreakableMap<Integer, String> map = new BreakableMap.Builder<Integer, String>()
                .add(1, "a")
                .build(Map.of(2, "b"));
        assertEquals(Map.of(1, "a", 2, "b"), new HashMap<>(map));
    }

    // ========== Queue storage permits nulls and compares by elements ==========

    @Test
    @DisplayName("Queue builder: the queue built permits null elements")
    void queueBuilderBuildPermitsNulls() {
        BreakableQueue<Integer> queue = new BreakableQueue.Builder<Integer>().build(Arrays.asList(1, null));
        assertEquals(2, queue.size());
        assertTrue(queue.contains(null));
    }

    @Test
    @DisplayName("Queue builder: two queues built with the same elements are equal")
    void queueBuilderBuildEqualByElements() {
        BreakableQueue.Builder<Integer> builder = new BreakableQueue.Builder<Integer>().addElements(1, 2);
        assertEquals(builder.build(), builder.build());
    }

    // ========== Built objects do not share breaks or elements with the builder ==========

    @Test
    @DisplayName("Queue builder: a break added after build does not reach the queue built")
    void queueBuilderBreakAddedAfterBuildNotInQueue() {
        BreakableQueue.Builder<Integer> builder = new BreakableQueue.Builder<>();
        BreakableQueue<Integer> queue = builder.build();
        builder.addBreak(BreakableQueue.OFFER_DOES_NOT_ADD_ELEMENT);
        assertFalse(queue.hasBreak(BreakableQueue.OFFER_DOES_NOT_ADD_ELEMENT));
    }

    @Test
    @DisplayName("TransferQueue builder: a break added after build does not reach the queue built")
    void transferQueueBuilderBreakAddedAfterBuildNotInQueue() {
        BreakableTransferQueue.Builder<Integer> builder = new BreakableTransferQueue.Builder<>();
        BreakableTransferQueue<Integer> queue = builder.build();
        builder.addBreak(BreakableTransferQueue.TRANSFER_THROWS_INTERRUPTED_EXCEPTION);
        assertFalse(queue.hasBreak(BreakableTransferQueue.TRANSFER_THROWS_INTERRUPTED_EXCEPTION));
    }

    @Test
    @DisplayName("Map builder: maps built by one builder do not share entries or breaks")
    void mapBuilderTwoMapsDoNotShareState() {
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
    void mapBuilderAddDoesNotChangeCallersMap() {
        Map<Integer, String> given = new HashMap<>();
        new BreakableMap.Builder<>(given).add(1, "a");
        assertTrue(given.isEmpty());
    }

    @Test
    @DisplayName("Deque builder: adding to the builder does not change the collection it was given")
    void dequeBuilderAddElementsDoesNotChangeCallersCollection() {
        List<Integer> given = new ArrayList<>();
        new BreakableDeque.Builder<>(given).addElements(1);
        assertTrue(given.isEmpty());
    }

    // ========== Unsupported methods reach the object built ==========

    @Test
    @DisplayName("Collection builder: methods marked unsupported throw in the collection built")
    void collectionBuilderDoesNotSupportMethodsThrow() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .doesNotSupportMethod(CollectionMethods.ADD)
                .doesNotSupport(CollectionMethods.REMOVE)
                .build();
        assertThrows(UnsupportedOperationException.class, () -> collection.add(4));
        assertThrows(UnsupportedOperationException.class, () -> collection.remove(1));
        assertEquals(List.of(1, 2, 3), List.copyOf(collection));
        collection.clear();
        assertTrue(collection.isEmpty());
    }

    @Test
    @DisplayName("SequencedCollection builder: methods marked unsupported throw in the collection built")
    void sequencedCollectionBuilderDoesNotSupportMethodsThrow() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2, 3)
                .doesNotSupport(SequencedCollectionMethods.ADD_FIRST)
                .doesNotSupport(SequencedCollectionMethods.REMOVE_LAST)
                .build();
        assertThrows(UnsupportedOperationException.class, () -> collection.addFirst(0));
        assertThrows(UnsupportedOperationException.class, collection::removeLast);
        collection.addLast(4);
        assertEquals(List.of(1, 2, 3, 4), List.copyOf(collection));
    }

    @Test
    @DisplayName("Iterable builder: an unsupported iterator remove throws in the iterable's iterators")
    void iterableBuilderDoesNotSupportIteratorRemoveIteratorThrows() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .doesNotSupport(IterableMethods.ITERATOR_REMOVE)
                .build();
        Iterator<Integer> iterator = iterable.iterator();
        iterator.next();
        assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    @Test
    @DisplayName("Collection builder: a copy keeps the methods marked unsupported")
    void collectionBuilderCopyKeepsUnsupportedMethods() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .doesNotSupportMethod(CollectionMethods.ADD)
                .copy()
                .build();
        assertThrows(UnsupportedOperationException.class, () -> collection.add(4));
    }

    // ========== Comparators ==========

    @Test
    @DisplayName("SortedSet builder: the comparator given to the constructor orders the set")
    void sortedSetBuilderWithComparatorOrdersSet() {
        BreakableSortedSet<Integer> set = new BreakableSortedSet.Builder<Integer>(Comparator.reverseOrder())
                .build(List.of(1, 2, 3));
        assertEquals(3, set.first());
    }

    @Test
    @DisplayName("SortedMap builder: a key the comparator accepts can be added before build")
    void sortedMapBuilderWithNullsFirstComparatorAcceptsNullKey() {
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
    void sortedMapCopyWhenChangedIsIndependent() {
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
    void navigableMapCopyWhenChangedIsIndependent() {
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
    void concurrentNavigableMapCopyWhenChangedIsIndependent() {
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
    void concurrentMapCopyKeepsPermits() {
        BreakableConcurrentMap<Integer, String> original = new BreakableConcurrentMap.Builder<Integer, String>()
                .doesNotPermitNullKeys()
                .build();
        assertEquals(original.permits(), new BreakableConcurrentMap<>(original).permits());
    }

    @Test
    @DisplayName("NavigableSet copy: keeps the original's permits")
    void navigableSetCopyKeepsPermits() {
        BreakableNavigableSet<Integer> original = new BreakableNavigableSet.Builder<Integer>()
                .doesNotPermitNulls()
                .build();
        assertEquals(original.permits(), new BreakableNavigableSet<>(original).permits());
    }
}
