/*
 * Copyright 2026 Evan Bergstrom
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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.SequencedSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.methods.CollectionMethods;
import com.soliscode.test.methods.MapMethods;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests that a breakable's views carry its thread safety (#52): a view of a thread-safe breakable is a breakable that
/// shares the parent's lock, and every call it makes on the store, as every call the parent makes, runs with that lock
/// held; a view of an unsafe one holds no lock. It also tests what else a view carries: the parent's method statuses,
/// mapped onto a map's collection views, and none of its breaks.
///
/// Each breakable is built over a recording store: a [Proxy] over a JDK collection that notes, for each call, whether
/// the parent's lock is held, and that records the views the store returns too. A traversal is not made under the lock
/// (as in the JDK's synchronized wrappers), so the calls that make an iterator or spliterator are not checked.
public class BreakableViewSafetyTest {

    /// The store's methods that start a traversal, which the lock does not cover.
    private static final Set<String> TRAVERSALS = Set.of("iterator", "listIterator", "descendingIterator",
            "spliterator", "stream", "parallelStream");

    // ========== The views share the parent's lock ==========

    @Test
    @DisplayName("a thread-safe BreakableList's methods and views run every store call under its lock")
    public void listViewsHoldTheLock() {
        final Recorder recorder = new Recorder();
        final List<Integer> store = recorder.record(new ArrayList<>(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)));
        final BreakableList<Integer> list = recorder.watch(new BreakableList<>(store, Set.of(), Map.of(),
                BreakableIterable.DEFAULT_CHARACTERISTICS, BreakableCollection.DEFAULT_PERMITS, true, Object.class));
        exerciseList(list);
        exerciseList(viewOf(list, list.subList(1, 8)));
        exerciseList(viewOf(list, list.subList(1, 8).subList(1, 4)));
        exerciseList(viewOf(list, list.reversed()));
        recorder.assertEveryCallLocked();
    }

    @Test
    @DisplayName("a thread-safe BreakableSortedSet's methods and views run every store call under its lock")
    public void sortedSetViewsHoldTheLock() {
        final Recorder recorder = new Recorder();
        final NavigableSet<Integer> store = recorder.record(new TreeSet<>(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)));
        final BreakableSortedSet<Integer> set = recorder.watch(new BreakableSortedSet<>(store, Set.of(), Map.of(),
                BreakableSortedSet.DEFAULT_CHARACTERISTICS, BreakableSortedSet.DEFAULT_PERMITS, true, Object.class));
        exerciseSortedSet(set);
        exerciseSortedSet(viewOf(set, set.subSet(2, 8)));
        exerciseSortedSet(viewOf(set, set.headSet(5)));
        exerciseSortedSet(viewOf(set, set.tailSet(3)));
        exerciseSortedSet(viewOf(set, set.reversed()));
        exerciseSortedSet(viewOf(set, set.subSet(1, 9).headSet(5)));
        recorder.assertEveryCallLocked();
    }

    @Test
    @DisplayName("a thread-safe BreakableNavigableSet's methods and views run every store call under its lock")
    public void navigableSetViewsHoldTheLock() {
        final Recorder recorder = new Recorder();
        final NavigableSet<Integer> store = recorder.record(new TreeSet<>(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)));
        final BreakableNavigableSet<Integer> set = recorder.watch(new BreakableNavigableSet<>(store, Set.of(),
                Map.of(), BreakableSortedSet.DEFAULT_CHARACTERISTICS, BreakableSortedSet.DEFAULT_PERMITS, true,
                Object.class));
        exerciseNavigableSet(set);
        exerciseNavigableSet(navigableViewOf(set, set.subSet(2, true, 8, true)));
        exerciseNavigableSet(navigableViewOf(set, set.headSet(5, true)));
        exerciseNavigableSet(navigableViewOf(set, set.tailSet(3, false)));
        exerciseNavigableSet(navigableViewOf(set, set.descendingSet()));
        exerciseNavigableSet(navigableViewOf(set, set.reversed()));
        exerciseNavigableSet((NavigableSet<Integer>) navigableViewOf(set, set.subSet(2, 8)));
        exerciseNavigableSet((NavigableSet<Integer>) navigableViewOf(set, set.headSet(5)));
        exerciseNavigableSet((NavigableSet<Integer>) navigableViewOf(set, set.tailSet(3)));
        exerciseNavigableSet(navigableViewOf(set, set.descendingSet().subSet(8, true, 2, true)));
        recorder.assertEveryCallLocked();
    }

    @Test
    @DisplayName("a thread-safe BreakableSequencedSet's and BreakableSequencedCollection's views hold their lock")
    public void sequencedViewsHoldTheLock() {
        final Recorder recorder = new Recorder();
        final SequencedSet<Integer> setStore = recorder.record(new LinkedHashSet<>(List.of(0, 1, 2, 3, 4)));
        final BreakableSequencedSet<Integer> set = recorder.watch(new BreakableSequencedSet<>(setStore, Set.of(),
                Map.of(), BreakableIterable.DEFAULT_CHARACTERISTICS, BreakableCollection.DEFAULT_PERMITS, true,
                Object.class));
        exerciseSequenced(set);
        exerciseSequenced(viewOf(set, set.reversed()));
        exerciseSequenced(viewOf(set, set.reversed().reversed()));

        final Recorder listRecorder = new Recorder();
        final List<Integer> listStore = listRecorder.record(new ArrayList<>(List.of(0, 1, 2, 3, 4)));
        final BreakableSequencedCollection<Integer> collection = listRecorder.watch(
                new BreakableSequencedCollection<>(listStore, Set.of(), Map.of(),
                        BreakableIterable.DEFAULT_CHARACTERISTICS, BreakableCollection.DEFAULT_PERMITS, true,
                        Object.class));
        exerciseSequenced(collection);
        exerciseSequenced(viewOf(collection, collection.reversed()));
        recorder.assertEveryCallLocked();
        listRecorder.assertEveryCallLocked();
    }

    @Test
    @DisplayName("a thread-safe BreakableDeque's methods and its reversed() view run every store call under its lock")
    public void dequeViewHoldsTheLock() {
        final Recorder recorder = new Recorder();
        final Deque<Integer> store = recorder.record(new ArrayDeque<>(List.of(0, 1, 2, 3, 4)));
        final BreakableDeque<Integer> deque = recorder.watch(new BreakableDeque<>(store, Set.of(), Map.of(),
                BreakableIterable.DEFAULT_CHARACTERISTICS, BreakableQueue.DEFAULT_PERMITS, true, Object.class));
        exerciseDeque(deque);
        // Deque's reversed() is the JDK's view over the deque's own methods, which take the lock.
        exerciseDeque(deque.reversed());
        recorder.assertEveryCallLocked();
    }

    @Test
    @DisplayName("a thread-safe BreakableMap's methods and collection views run every store call under its lock")
    public void mapViewsHoldTheLock() {
        final Recorder recorder = new Recorder();
        final Map<Integer, String> store = recorder.record(new HashMap<>(entries(10)));
        final BreakableMap<Integer, String> map = recorder.watch(new BreakableMap<>(store, Set.of(), Map.of(),
                BreakableMap.DEFAULT_PERMITS, true));
        exerciseMap(map);
        exerciseCollection(viewOf(map, map.keySet()), 3);
        exerciseCollection(viewOf(map, map.values()), "v3");
        exerciseCollection(viewOf(map, map.entrySet()), Map.entry(3, "v3"));
        recorder.assertEveryCallLocked();
    }

    @Test
    @DisplayName("a thread-safe BreakableNavigableMap's methods and views run every store call under its lock")
    public void navigableMapViewsHoldTheLock() {
        final Recorder recorder = new Recorder();
        final NavigableMap<Integer, String> store = recorder.record(new TreeMap<>(entries(10)));
        final BreakableNavigableMap<Integer, String> map = recorder.watch(new BreakableNavigableMap<>(store,
                Set.of(), Map.of(), BreakableMap.DEFAULT_PERMITS, true));
        exerciseNavigableMap(map);
        exerciseNavigableMap(navigableViewOf(map, map.subMap(2, true, 8, true)));
        exerciseNavigableMap(navigableViewOf(map, map.headMap(6, false)));
        exerciseNavigableMap(navigableViewOf(map, map.tailMap(3, true)));
        exerciseNavigableMap(navigableViewOf(map, map.descendingMap()));
        exerciseNavigableMap((NavigableMap<Integer, String>) navigableViewOf(map, map.subMap(2, 8)));
        exerciseNavigableMap((NavigableMap<Integer, String>) navigableViewOf(map, map.headMap(6)));
        exerciseNavigableMap((NavigableMap<Integer, String>) navigableViewOf(map, map.tailMap(3)));
        exerciseNavigableMap(navigableViewOf(map, map.descendingMap().headMap(4, true)));
        exerciseNavigableSet(navigableViewOf(map, map.navigableKeySet()));
        exerciseNavigableSet(navigableViewOf(map, map.descendingKeySet()));
        exerciseCollection(viewOf(map, map.subMap(1, 9).keySet()), 3);
        exerciseCollection(viewOf(map, map.subMap(1, 9).entrySet()), Map.entry(3, "v3"));
        recorder.assertEveryCallLocked();
    }

    @Test
    @DisplayName("a thread-safe BreakableSortedMap's views are sorted maps that hold its lock")
    public void sortedMapViewsHoldTheLock() {
        final Recorder recorder = new Recorder();
        final SortedMap<Integer, String> store = recorder.recordAs(SortedMap.class, new TreeMap<>(entries(10)));
        final BreakableSortedMap<Integer, String> map = recorder.watch(new BreakableSortedMap<>(store, Set.of(),
                Map.of(), BreakableMap.DEFAULT_PERMITS, true));
        exerciseSortedMap(map);
        exerciseSortedMap(viewOf(map, map.subMap(2, 8)));
        exerciseSortedMap(viewOf(map, map.headMap(6)));
        exerciseSortedMap(viewOf(map, map.tailMap(3).headMap(7)));
        recorder.assertEveryCallLocked();
    }

    @Test
    @DisplayName("the views of an unsafe breakable hold no lock")
    public void unsafeViewsHoldNoLock() {
        final BreakableNavigableSet<Integer> set = new BreakableNavigableSet<>(new TreeSet<>(List.of(1, 2, 3)));
        final BreakableList<Integer> list = new BreakableList<>(new ArrayList<>(List.of(1, 2, 3)));
        final BreakableNavigableMap<Integer, String> map = new BreakableNavigableMap.Builder<Integer, String>()
                .build();
        map.putAll(entries(3));
        for (final Object view : List.of(set.subSet(1, 3), set.headSet(2), set.descendingSet(), set.reversed(),
                list.subList(0, 2), list.reversed(), map.keySet(), map.values(), map.entrySet(),
                map.navigableKeySet(), map.subMap(0, 2), map.descendingMap(), map.headMap(2).keySet())) {
            final AbstractBreakable breakable = assertInstanceOf(AbstractBreakable.class, view);
            assertFalse(lockOf(breakable) != null, () -> "a view of an unsafe breakable holds a lock: " + view);
            assertFalse(breakable.isSafe());
        }
    }

    @Test
    @DisplayName("a copy of a view of a thread-safe breakable has a lock of its own")
    public void copyOfAViewHasItsOwnLock() {
        final BreakableNavigableSet<Integer> set = new BreakableNavigableSet.Builder<Integer>().setSafe(true).build();
        set.addAll(List.of(1, 2, 3));
        final BreakableNavigableSet<Integer> view = (BreakableNavigableSet<Integer>) set.subSet(1, true, 3, true);
        assertSame(lockOf(set), lockOf(view));
        final BreakableNavigableSet<Integer> copy = new BreakableNavigableSet<>(view);
        assertNotNull(lockOf(copy));
        assertFalse(lockOf(copy) == lockOf(set), "the copy shares the parent's lock");
    }

    @Test
    @DisplayName("with REVERSED_IS_NOT_THREAD_SAFE, a thread-safe sequenced breakable's reversed() view holds no lock")
    public void reversedIsNotThreadSafeGivesAViewWithNoLock() {
        final BreakableSequencedCollection<Integer> collection = new BreakableSequencedCollection.Builder<Integer>()
                .setSafe(true).build(List.of(1, 2, 3));
        final BreakableSequencedCollection<Integer> broken = new BreakableSequencedCollection.Builder<Integer>()
                .setSafe(true).addBreak(BreakableSequencedCollection.REVERSED_IS_NOT_THREAD_SAFE)
                .build(List.of(1, 2, 3));
        final BreakableList<Integer> list = new BreakableList.Builder<Integer>().setSafe(true).build(List.of(1, 2, 3));
        final BreakableList<Integer> brokenList = new BreakableList.Builder<Integer>().setSafe(true)
                .addBreak(BreakableSequencedCollection.REVERSED_IS_NOT_THREAD_SAFE).build(List.of(1, 2, 3));
        viewOf(collection, collection.reversed());
        viewOf(list, list.reversed());
        for (final AbstractBreakable parent : List.<AbstractBreakable>of(broken, brokenList)) {
            assertNotNull(lockOf(parent));
            final Object view = ((SequencedCollection<?>) parent).reversed();
            final AbstractBreakable breakable = assertInstanceOf(AbstractBreakable.class, view);
            assertNull(lockOf(breakable), () -> "the reversed view holds a lock: " + view);
            assertFalse(breakable.isSafe());
            assertEquals(List.of(3, 2, 1), new ArrayList<>((Collection<?>) view));
        }
    }

    @Test
    @DisplayName("the concurrent maps and blocking queues report isSafe and hold no lock, however they are made")
    public void storeSafeBreakablesHoldNoLock() {
        final BreakableConcurrentMap<Integer, String> concurrent =
                new BreakableConcurrentMap.Builder<Integer, String>().setSafe(true).build();
        final BreakableConcurrentNavigableMap<Integer, String> navigable =
                new BreakableConcurrentNavigableMap.Builder<Integer, String>().setSafe(true).build();
        navigable.putAll(entries(5));
        final List<AbstractBreakable> breakables = List.of(concurrent, new BreakableConcurrentMap<>(concurrent),
                navigable, new BreakableConcurrentNavigableMap<>(navigable),
                (AbstractBreakable) Objects.requireNonNull(navigable.subMap(1, 3)),
                (AbstractBreakable) Objects.requireNonNull(navigable.navigableKeySet()),
                (AbstractBreakable) Objects.requireNonNull(concurrent.keySet()),
                new BreakableBlockingQueue.Builder<Integer>().setSafe(true).build(),
                new BreakableBlockingQueue<>(new BreakableBlockingQueue.Builder<Integer>().setSafe(true).build()),
                new BreakableBlockingDeque.Builder<Integer>().setSafe(true).build(),
                new BreakableTransferQueue.Builder<Integer>().setSafe(true).build());
        for (final AbstractBreakable breakable : breakables) {
            assertFalse(lockOf(breakable) != null, () -> breakable.getClass().getSimpleName() + " holds a lock");
        }
        for (final AbstractBreakable breakable : breakables.subList(0, 4)) {
            assertTrue(breakable.isSafe());
        }
        for (final AbstractBreakable breakable : breakables.subList(7, breakables.size())) {
            assertTrue(breakable.isSafe());
        }
    }

    // ========== What else a view carries ==========

    @Test
    @DisplayName("a view carries its parent's method statuses and none of its breaks")
    public void viewCarriesStatusesNotBreaks() {
        final BreakableList<Integer> list = new BreakableList.Builder<Integer>()
                .addElements(1, 2, 3, 4)
                .doesNotSupport(CollectionMethods.ADD)
                .addBreak(BreakableCollection.SIZE_ALWAYS_RETURNS_ZERO)
                .build();
        assertEquals(0, list.size());
        final List<Integer> view = list.subList(0, 3);
        assertThrows(UnsupportedOperationException.class, () -> view.add(9));
        assertEquals(3, view.size());

        final BreakableNavigableSet<Integer> set = new BreakableNavigableSet.Builder<Integer>()
                .addElements(1, 2, 3, 4)
                .addBreak(BreakableCollection.SIZE_ALWAYS_RETURNS_ZERO)
                .build();
        assertEquals(4, set.reversed().size());
        assertEquals(2, set.headSet(3).size());
    }

    @Test
    @DisplayName("a map's collection views refuse add and follow the map's remove and clear statuses")
    public void mapCollectionViewsFollowTheMapsStatuses() {
        final BreakableNavigableMap<Integer, String> map = new BreakableNavigableMap.Builder<Integer, String>()
                .doesNotSupport(MapMethods.REMOVE)
                .doesNotSupport(MapMethods.CLEAR)
                .build();
        map.putAll(entries(5));
        assertThrows(UnsupportedOperationException.class, () -> map.keySet().add(9));
        assertThrows(UnsupportedOperationException.class, () -> map.keySet().add(1));
        assertThrows(UnsupportedOperationException.class, () -> map.entrySet().addAll(Set.of(Map.entry(7, "v7"))));
        assertThrows(UnsupportedOperationException.class, () -> map.keySet().remove(1));
        assertThrows(UnsupportedOperationException.class, () -> map.values().removeIf(v -> true));
        assertThrows(UnsupportedOperationException.class, () -> map.entrySet().retainAll(Set.of()));
        assertThrows(UnsupportedOperationException.class, () -> map.values().clear());
        assertThrows(UnsupportedOperationException.class, () -> map.navigableKeySet().pollFirst());
        assertThrows(UnsupportedOperationException.class, () -> map.descendingKeySet().removeLast());
        assertThrows(UnsupportedOperationException.class, () -> map.navigableKeySet().headSet(3).remove(1));
        assertThrows(UnsupportedOperationException.class, () -> {
            final var iterator = map.keySet().iterator();
            iterator.next();
            iterator.remove();
        });
        assertEquals(5, map.size());
    }

    @Test
    @DisplayName("a map whose put is unsupported hands out entries whose setValue is unsupported, on every path")
    public void entriesOfAMapWithoutPutAreFixed() {
        final BreakableMap<Integer, String> map = new BreakableMap.Builder<Integer, String>()
                .addAll(entries(3))
                .doesNotSupport(MapMethods.PUT)
                .build();
        final Set<Map.Entry<Integer, String>> entrySet = Objects.requireNonNull(map.entrySet());
        @SuppressWarnings("unchecked")
        final Map.Entry<Integer, String> fromArray = (Map.Entry<Integer, String>) entrySet.toArray()[0];
        assertThrows(UnsupportedOperationException.class, () -> fromArray.setValue("x"));
        assertThrows(UnsupportedOperationException.class, () -> entrySet.forEach(e -> e.setValue("x")));
        assertThrows(UnsupportedOperationException.class, () -> entrySet.iterator().next().setValue("x"));
        assertThrows(UnsupportedOperationException.class,
                () -> entrySet.stream().findFirst().orElseThrow().setValue("x"));
        assertEquals(entries(3), map);
        assertTrue(entrySet.contains(Map.entry(1, "v1")));

        final BreakableMap<Integer, String> writable = new BreakableMap.Builder<Integer, String>()
                .addAll(entries(3))
                .build();
        Objects.requireNonNull(writable.entrySet()).forEach(e -> e.setValue("x"));
        assertEquals("x", writable.get(1));
    }

    // ========== The exercises ==========

    private static void exerciseCollection(final Collection<?> c, final Object member) {
        c.size();
        c.isEmpty();
        c.contains(member);
        c.containsAll(List.of(member));
        c.toArray();
        c.toArray(new Object[0]);
        c.hashCode();
        c.toString();
        c.equals(List.of());
        c.forEach(e -> { });
        attempt(() -> c.remove(member));
        attempt(() -> c.removeIf(e -> false));
        attempt(() -> c.retainAll(List.copyOf(c)));
    }

    private static void exerciseList(final List<Integer> list) {
        exerciseCollection(list, 3);
        list.add(5);
        list.remove(Integer.valueOf(5));
        list.get(0);
        list.set(0, list.get(0));
        list.add(1, 7);
        list.remove(1);
        list.indexOf(3);
        list.lastIndexOf(3);
        list.addAll(0, List.of(7));
        list.remove(0);
        list.replaceAll(x -> x);
        attempt(() -> list.sort(null));
        list.getFirst();
        list.getLast();
    }

    private static void exerciseSequenced(final SequencedCollection<Integer> c) {
        exerciseCollection(c, 3);
        c.getFirst();
        c.getLast();
        attempt(() -> c.addFirst(-1));
        attempt(() -> c.addLast(-2));
        attempt(c::removeFirst);
        attempt(c::removeLast);
    }

    private static void exerciseSortedSet(final SortedSet<Integer> set) {
        exerciseCollection(set, 4);
        attempt(() -> set.add(4));
        attempt(set::first);
        attempt(set::last);
        set.comparator();
    }

    private static void exerciseNavigableSet(final NavigableSet<Integer> set) {
        exerciseSortedSet(set);
        set.lower(4);
        set.floor(4);
        set.ceiling(4);
        set.higher(4);
        attempt(set::pollFirst);
        attempt(set::pollLast);
        attempt(set::removeFirst);
    }

    private static void exerciseDeque(final Deque<Integer> deque) {
        exerciseCollection(deque, 3);
        deque.offer(9);
        deque.offerFirst(8);
        deque.offerLast(7);
        deque.addFirst(6);
        deque.addLast(5);
        deque.push(4);
        deque.peek();
        deque.peekFirst();
        deque.peekLast();
        deque.element();
        deque.getFirst();
        deque.getLast();
        deque.poll();
        deque.pollFirst();
        deque.pollLast();
        deque.pop();
        deque.remove();
        deque.removeFirst();
        deque.removeLast();
        deque.removeFirstOccurrence(2);
        deque.removeLastOccurrence(1);
    }

    private static void exerciseMap(final Map<Integer, String> map) {
        map.size();
        map.isEmpty();
        map.containsKey(3);
        map.containsValue("v3");
        map.get(3);
        map.getOrDefault(3, "d");
        attempt(() -> map.put(20, "v20"));
        attempt(() -> map.remove(20));
        attempt(() -> map.putAll(Map.of(21, "v21")));
        attempt(() -> map.putIfAbsent(22, "v22"));
        attempt(() -> map.remove(22, "v22"));
        attempt(() -> map.replace(21, "v21", "w21"));
        attempt(() -> map.replace(21, "v21"));
        attempt(() -> map.computeIfAbsent(23, k -> "v23"));
        attempt(() -> map.computeIfPresent(23, (k, v) -> null));
        attempt(() -> map.compute(21, (k, v) -> null));
        attempt(() -> map.merge(3, "v3", (a, b) -> a));
        map.forEach((k, v) -> { });
        attempt(() -> map.replaceAll((k, v) -> v));
        map.hashCode();
        map.toString();
        map.equals(Map.of());
    }

    private static void exerciseSortedMap(final SortedMap<Integer, String> map) {
        exerciseMap(map);
        attempt(map::firstKey);
        attempt(map::lastKey);
        exerciseCollection(map.keySet(), 4);
    }

    private static void exerciseNavigableMap(final NavigableMap<Integer, String> map) {
        exerciseSortedMap(map);
        map.lowerEntry(4);
        map.lowerKey(4);
        map.floorEntry(4);
        map.floorKey(4);
        map.ceilingEntry(4);
        map.ceilingKey(4);
        map.higherEntry(4);
        map.higherKey(4);
        map.firstEntry();
        map.lastEntry();
        attempt(map::pollFirstEntry);
        attempt(map::pollLastEntry);
    }

    /// Runs an operation that a view may refuse: one out of its range, or one its interface does not support.
    private static void attempt(final Runnable operation) {
        try {
            operation.run();
        } catch (final UnsupportedOperationException | IllegalArgumentException | NoSuchElementException e) {
            // Refused; what the test checks is the lock the calls ran under.
        }
    }

    /// Returns a supplier as a runnable, for [#attempt(Runnable)].
    private static void attempt(final Supplier<?> operation) {
        attempt(() -> {
            operation.get();
        });
    }

    // ========== Views and locks ==========

    /// Checks that a view is a breakable that shares the parent's lock, and returns it.
    private static <T> T viewOf(final AbstractBreakable parent, final @Nullable T view) {
        final AbstractBreakable breakable = assertInstanceOf(AbstractBreakable.class, view);
        assertSame(lockOf(parent), lockOf(breakable), "the view does not share its parent's lock");
        assertTrue(breakable.isSafe());
        return Objects.requireNonNull(view);
    }

    /// Checks that a view of a navigable set or map is a navigable breakable that shares the parent's lock.
    private static <T> T navigableViewOf(final AbstractBreakable parent, final @Nullable T view) {
        assertTrue(view instanceof BreakableNavigableSet<?> || view instanceof BreakableNavigableMap<?, ?>,
                () -> "not a navigable breakable: " + view);
        return viewOf(parent, view);
    }

    /// Reads a breakable's private lock.
    private static @Nullable ReentrantLock lockOf(final AbstractBreakable breakable) {
        try {
            final Field field = AbstractBreakable.class.getDeclaredField("lock");
            field.setAccessible(true);
            return (ReentrantLock) field.get(breakable);
        } catch (final ReflectiveOperationException e) {
            throw new AssertionError("Cannot read AbstractBreakable's lock", e);
        }
    }

    private static Map<Integer, String> entries(final int count) {
        final Map<Integer, String> entries = new HashMap<>();
        for (int i = 0; i < count; i++) {
            entries.put(i, "v" + i);
        }
        return entries;
    }

    /// Records the calls made on a store, and on the views the store returns, with whether the watched breakable's
    /// lock was held.
    private static final class Recorder implements InvocationHandler {
        private final Map<Object, Object> targets = new HashMap<>();
        private final List<String> unlocked = new ArrayList<>();
        private int calls;
        private @Nullable AbstractBreakable watched;

        /// Returns a proxy over the store that records the calls made on it, of every interface the store implements.
        @SuppressWarnings("unchecked")
        <T> T record(final T store) {
            return (T) proxy(store, interfacesOf(store.getClass()));
        }

        /// Returns a proxy over the store of one interface alone, so that the breakable sees no other.
        @SuppressWarnings("unchecked")
        <T> T recordAs(final Class<?> iface, final Object store) {
            return (T) proxy(store, new Class<?>[] {iface});
        }

        /// Sets the breakable whose lock the calls must hold, and returns it.
        <T extends AbstractBreakable> T watch(final T breakable) {
            assertNotNull(lockOf(breakable));
            this.watched = breakable;
            return breakable;
        }

        void assertEveryCallLocked() {
            assertTrue(calls > 0, "no call reached the store");
            assertEquals(List.of(), unlocked, "store calls made without the breakable's lock");
        }

        @Override
        public @Nullable Object invoke(final Object proxy, final Method method, final Object @Nullable [] args)
                throws Throwable {
            final Object target = targets.get(new Identity(proxy));
            if (!TRAVERSALS.contains(method.getName())) {
                calls++;
                final ReentrantLock lock = watched == null ? null : lockOf(watched);
                if (lock == null || !lock.isHeldByCurrentThread()) {
                    unlocked.add(method.getDeclaringClass().getSimpleName() + "." + method.getName());
                }
            }
            final Object result;
            try {
                result = method.invoke(target, args);
            } catch (final InvocationTargetException e) {
                throw e.getCause();
            }
            final boolean view = result instanceof Collection<?> || result instanceof Map<?, ?>;
            return view && result != target ? proxy(result, interfacesOf(result.getClass())) : result;
        }

        private Object proxy(final Object target, final Class<?>[] interfaces) {
            final Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), interfaces, this);
            targets.put(new Identity(proxy), target);
            return proxy;
        }

        private static Class<?>[] interfacesOf(final Class<?> type) {
            final Set<Class<?>> interfaces = new LinkedHashSet<>();
            for (Class<?> c = type; c != null; c = c.getSuperclass()) {
                collect(c, interfaces);
            }
            return interfaces.toArray(new Class<?>[0]);
        }

        private static void collect(final Class<?> type, final Set<Class<?>> interfaces) {
            for (final Class<?> i : type.getInterfaces()) {
                if (Modifier.isPublic(i.getModifiers())) {
                    interfaces.add(i);
                }
                collect(i, interfaces);
            }
        }
    }

    /// A key that compares a proxy by identity, since a proxy's own `equals` and `hashCode` go to its handler.
    private record Identity(Object object) {
        @Override
        public boolean equals(final @Nullable Object o) {
            return o instanceof Identity that && that.object == object;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(object);
        }
    }
}
