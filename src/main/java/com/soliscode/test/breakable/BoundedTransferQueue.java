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

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.jspecify.annotations.Nullable;

/// A bounded [TransferQueue] on one [ReentrantLock], the store of a [BreakableTransferQueue] built with a capacity
/// or with fairness. The JDK has no bounded `TransferQueue`.
///
/// - **Hand-off first.** Every insert (`offer`, `put`, `transfer`, both `tryTransfer` forms) gives its element
///   straight to the first waiting consumer, if there is one, using no capacity. A consumer waits only on an empty
///   queue, in `take` or the timed `poll`.
/// - **Capacity.** Without a waiting consumer, `offer` and `put` enqueue when there is room; `transfer` and the timed
///   `tryTransfer` wait for room, then enqueue a node and wait until a consumer takes it. Interrupted or timed out
///   before then, they remove the node: the element "is not left enqueued" ([TransferQueue#transfer]).
/// - **Wake-ups.** Each waiting consumer waits on a condition of its own. `notFull` and `received` are signalled with
///   `signalAll` whenever a node leaves the queue, and `notFull` also when a consumer registers, so no wake-up is
///   lost; every waiter re-checks under the lock.
/// - **Removal by other means** (`remove(Object)`, the iterator, `drainTo`, `clear`) counts as reception for a
///   transfer waiting on the node, as in [java.util.concurrent.LinkedTransferQueue].
///
/// It refuses `null`, keeps identity equality, and iterates a snapshot. Fairness is the lock's.
///
/// @param <E> the type of elements held in this queue
/// @since 0.1.2
final class BoundedTransferQueue<E> extends AbstractQueue<E> implements TransferQueue<E>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int capacity;
    private final boolean fair;
    private final transient ReentrantLock lock;
    private final transient Condition notFull;
    private final transient Condition received;
    private final transient ArrayDeque<Node<E>> items = new ArrayDeque<>();
    private final transient ArrayDeque<Slot<E>> consumers = new ArrayDeque<>();

    /// Creates a queue holding the given elements, in their iteration order.
    ///
    /// @param capacityArg the capacity, at least 1
    /// @param fairArg whether the lock is fair
    /// @param elements the elements to start with
    /// @throws IllegalArgumentException if the capacity is below 1, or there are more elements than the capacity
    /// @throws NullPointerException if an element is null
    BoundedTransferQueue(final int capacityArg, final boolean fairArg, final Collection<? extends E> elements) {
        if (capacityArg < 1) {
            throw new IllegalArgumentException("capacity " + capacityArg + " is below 1");
        }
        if (elements.size() > capacityArg) {
            throw new IllegalArgumentException(elements.size() + " elements exceed the capacity of " + capacityArg);
        }
        this.capacity = capacityArg;
        this.fair = fairArg;
        this.lock = new ReentrantLock(fairArg);
        this.notFull = lock.newCondition();
        this.received = lock.newCondition();
        for (final E e : elements) {
            items.addLast(new Node<>(Objects.requireNonNull(e)));
        }
    }

    /// Returns a new queue with this one's capacity, fairness and elements.
    ///
    /// @return the copy
    BoundedTransferQueue<E> copy() {
        return new BoundedTransferQueue<>(capacity, fair, elements());
    }

    // ========== Inserts ==========

    @Override
    public boolean offer(final E e) {
        Objects.requireNonNull(e);
        lock.lock();
        try {
            if (handOff(e)) {
                return true;
            }
            if (items.size() == capacity) {
                return false;
            }
            items.addLast(new Node<>(e));
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void put(final E e) throws InterruptedException {
        Objects.requireNonNull(e);
        lock.lockInterruptibly();
        try {
            while (!handOff(e)) {
                if (items.size() < capacity) {
                    items.addLast(new Node<>(e));
                    return;
                }
                notFull.await();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean offer(final E e, final long timeout, final TimeUnit unit) throws InterruptedException {
        Objects.requireNonNull(e);
        long nanos = unit.toNanos(timeout);
        lock.lockInterruptibly();
        try {
            while (!handOff(e)) {
                if (items.size() < capacity) {
                    items.addLast(new Node<>(e));
                    return true;
                }
                if (nanos <= 0L) {
                    return false;
                }
                nanos = notFull.awaitNanos(nanos);
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean tryTransfer(final E e) {
        Objects.requireNonNull(e);
        lock.lock();
        try {
            return handOff(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void transfer(final E e) throws InterruptedException {
        Objects.requireNonNull(e);
        lock.lockInterruptibly();
        try {
            while (true) {
                if (handOff(e)) {
                    return;
                }
                if (items.size() < capacity) {
                    break;
                }
                notFull.await();
            }
            Node<E> node = new Node<>(e);
            items.addLast(node);
            try {
                while (!node.taken) {
                    received.await();
                }
            } catch (final InterruptedException ie) {
                if (node.taken) {
                    Thread.currentThread().interrupt();
                    return;
                }
                withdraw(node);
                throw ie;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean tryTransfer(final E e, final long timeout, final TimeUnit unit) throws InterruptedException {
        Objects.requireNonNull(e);
        long nanos = unit.toNanos(timeout);
        lock.lockInterruptibly();
        try {
            while (true) {
                if (handOff(e)) {
                    return true;
                }
                if (items.size() < capacity) {
                    break;
                }
                if (nanos <= 0L) {
                    return false;
                }
                nanos = notFull.awaitNanos(nanos);
            }
            Node<E> node = new Node<>(e);
            items.addLast(node);
            try {
                while (!node.taken) {
                    if (nanos <= 0L) {
                        withdraw(node);
                        return false;
                    }
                    nanos = received.awaitNanos(nanos);
                }
                return true;
            } catch (final InterruptedException ie) {
                if (node.taken) {
                    Thread.currentThread().interrupt();
                    return true;
                }
                withdraw(node);
                throw ie;
            }
        } finally {
            lock.unlock();
        }
    }

    // ========== Removals ==========

    @Override
    public @Nullable E poll() {
        lock.lock();
        try {
            return takeHead();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public E take() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            E head = takeHead();
            if (head != null) {
                return head;
            }
            Slot<E> slot = register();
            try {
                while (true) {
                    E item = slot.item;
                    if (item != null) {
                        return item;
                    }
                    slot.filled.await();
                }
            } catch (final InterruptedException ie) {
                E item = slot.item;
                if (item != null) {
                    Thread.currentThread().interrupt();
                    return item;
                }
                consumers.remove(slot);
                throw ie;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public @Nullable E poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        lock.lockInterruptibly();
        try {
            E head = takeHead();
            if (head != null || nanos <= 0L) {
                return head;
            }
            Slot<E> slot = register();
            try {
                while (true) {
                    E item = slot.item;
                    if (item != null) {
                        return item;
                    }
                    if (nanos <= 0L) {
                        consumers.remove(slot);
                        return null;
                    }
                    nanos = slot.filled.awaitNanos(nanos);
                }
            } catch (final InterruptedException ie) {
                E item = slot.item;
                if (item != null) {
                    Thread.currentThread().interrupt();
                    return item;
                }
                consumers.remove(slot);
                throw ie;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean remove(final @Nullable Object o) {
        if (o == null) {
            return false;
        }
        lock.lock();
        try {
            for (Iterator<Node<E>> it = items.iterator(); it.hasNext();) {
                Node<E> node = it.next();
                if (o.equals(node.item)) {
                    it.remove();
                    removed(node);
                    return true;
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            if (items.isEmpty()) {
                return;
            }
            for (final Node<E> node : items) {
                node.taken = true;
            }
            items.clear();
            notFull.signalAll();
            received.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int drainTo(final Collection<? super E> c) {
        return drainTo(c, Integer.MAX_VALUE);
    }

    @Override
    public int drainTo(final Collection<? super E> c, final int maxElements) {
        checkNotThis(Objects.requireNonNull(c));
        if (maxElements <= 0) {
            return 0;
        }
        lock.lock();
        int moved = 0;
        try {
            while (moved < maxElements && !items.isEmpty()) {
                Node<E> node = items.peekFirst();
                c.add(node.item);
                items.pollFirst();
                node.taken = true;
                moved++;
            }
            return moved;
        } finally {
            // Also when the collection's add throws partway: the nodes already moved have left the queue.
            if (moved > 0) {
                notFull.signalAll();
                received.signalAll();
            }
            lock.unlock();
        }
    }

    // ========== Queries ==========

    @Override
    public @Nullable E peek() {
        lock.lock();
        try {
            Node<E> head = items.peekFirst();
            return head == null ? null : head.item;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        lock.lock();
        try {
            return items.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean contains(final @Nullable Object o) {
        if (o == null) {
            return false;
        }
        lock.lock();
        try {
            for (final Node<E> node : items) {
                if (o.equals(node.item)) {
                    return true;
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int remainingCapacity() {
        lock.lock();
        try {
            return capacity - items.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean hasWaitingConsumer() {
        lock.lock();
        try {
            return !consumers.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getWaitingConsumerCount() {
        lock.lock();
        try {
            return consumers.size();
        } finally {
            lock.unlock();
        }
    }

    /// Returns an iterator over a snapshot of the queue. Its `remove` removes the node it last returned, if it is
    /// still in the queue.
    ///
    /// @return the iterator
    @Override
    public Iterator<E> iterator() {
        List<Node<E>> snapshot;
        lock.lock();
        try {
            snapshot = new ArrayList<>(items);
        } finally {
            lock.unlock();
        }
        return new SnapshotIterator(snapshot);
    }

    /// Returns a spliterator over a snapshot of the queue, reporting `ORDERED`, `NONNULL` and `CONCURRENT`, as
    /// `LinkedTransferQueue`'s does.
    ///
    /// @return the spliterator
    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator(),
                Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.CONCURRENT);
    }

    // ========== Internals (the lock held) ==========

    /// Gives the element to the first waiting consumer, if there is one.
    private boolean handOff(final E e) {
        Slot<E> slot = consumers.pollFirst();
        if (slot == null) {
            return false;
        }
        slot.item = e;
        slot.filled.signal();
        return true;
    }

    /// Takes the head node, if there is one, as a consumer receives it.
    private @Nullable E takeHead() {
        Node<E> head = items.pollFirst();
        if (head == null) {
            return null;
        }
        removed(head);
        return head.item;
    }

    /// Registers a waiting consumer; a producer waiting for room can then hand off to it.
    private Slot<E> register() {
        Slot<E> slot = new Slot<>(lock.newCondition());
        consumers.addLast(slot);
        notFull.signalAll();
        return slot;
    }

    /// Marks a node that has left the queue as taken, and wakes the waiters for room and for reception.
    private void removed(final Node<E> node) {
        node.taken = true;
        notFull.signalAll();
        received.signalAll();
    }

    /// Removes the node of a transfer that gave up: it is not left enqueued.
    private void withdraw(final Node<E> node) {
        items.removeFirstOccurrence(node);
        notFull.signalAll();
    }

    // ReferenceEquality: the check is for this very queue, by identity, as the JDK's queues make it.
    @SuppressWarnings("ReferenceEquality")
    private void checkNotThis(final Collection<? super E> c) {
        if (c == this) {
            throw new IllegalArgumentException("a queue cannot be drained into itself");
        }
    }

    private List<E> elements() {
        lock.lock();
        try {
            List<E> list = new ArrayList<>(items.size());
            for (final Node<E> node : items) {
                list.add(node.item);
            }
            return list;
        } finally {
            lock.unlock();
        }
    }

    // ========== Serialization ==========

    @Serial
    private Object writeReplace() {
        return new SerializationProxy<>(capacity, fair, new ArrayList<>(elements()));
    }

    @Serial
    private void readObject(final ObjectInputStream in) throws InvalidObjectException {
        throw new InvalidObjectException("a BoundedTransferQueue is read through its serialization proxy");
    }

    /// The serialized form: the capacity, the fairness and the elements.
    ///
    /// @param capacity the capacity
    /// @param fair whether the lock is fair
    /// @param elements the elements, in order
    /// @param <E> the element type
    private record SerializationProxy<E>(int capacity, boolean fair, ArrayList<E> elements) implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Serial
        private Object readResolve() {
            return new BoundedTransferQueue<>(capacity, fair, elements);
        }
    }

    /// An element in the queue; `taken` once a consumer has received it, or it was removed.
    private static final class Node<E> {
        private final E item;
        private boolean taken;

        Node(final E itemArg) {
            this.item = itemArg;
        }
    }

    /// A waiting consumer: the element handed to it, and the condition it waits on.
    private static final class Slot<E> {
        private final Condition filled;
        private @Nullable E item;

        Slot(final Condition filledArg) {
            this.filled = filledArg;
        }
    }

    /// An iterator over a snapshot of the nodes.
    private final class SnapshotIterator implements Iterator<E> {
        private final List<Node<E>> nodes;
        private int next;
        private @Nullable Node<E> last;

        SnapshotIterator(final List<Node<E>> nodesArg) {
            this.nodes = nodesArg;
        }

        @Override
        public boolean hasNext() {
            return next < nodes.size();
        }

        @Override
        public E next() {
            if (next >= nodes.size()) {
                throw new NoSuchElementException();
            }
            Node<E> node = nodes.get(next++);
            last = node;
            return node.item;
        }

        @Override
        public void remove() {
            Node<E> node = last;
            if (node == null) {
                throw new IllegalStateException("next() has not been called, or remove() was already called");
            }
            last = null;
            lock.lock();
            try {
                if (items.removeFirstOccurrence(node)) {
                    removed(node);
                }
            } finally {
                lock.unlock();
            }
        }
    }
}
