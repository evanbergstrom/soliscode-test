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
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests that a [BreakableSortedSet] and a [BreakableNavigableSet] built thread-safe are thread-safe (#23): they
/// report [AbstractBreakable#isSafe()], and their methods run with the breakable's lock held.
public class SortedSetSafetyTest {

    @Test
    @DisplayName("a BreakableSortedSet built thread-safe reports isSafe")
    public void sortedSetBuiltSafeReportsIsSafe() {
        final BreakableSortedSet<String> safe = new BreakableSortedSet.Builder<String>().setSafe(true).build();
        assertTrue(safe.isSafe());
        assertTrue(new BreakableSortedSet<>(safe).isSafe());
        assertFalse(new BreakableSortedSet.Builder<String>().build().isSafe());
    }

    @Test
    @DisplayName("a BreakableNavigableSet built thread-safe reports isSafe")
    public void navigableSetBuiltSafeReportsIsSafe() {
        final BreakableNavigableSet<String> safe = new BreakableNavigableSet.Builder<String>().setSafe(true).build();
        assertTrue(safe.isSafe());
        assertTrue(new BreakableNavigableSet<>(safe).isSafe());
        assertFalse(new BreakableNavigableSet.Builder<String>().build().isSafe());
    }

    @Test
    @DisplayName("a thread-safe BreakableSortedSet holds its lock while it adds to the backing set")
    public void safeSortedSetTakesTheLock() {
        assertEquals(1, lockHeldDuringAdd(true, false));
        assertEquals(0, lockHeldDuringAdd(false, false));
    }

    @Test
    @DisplayName("a thread-safe BreakableNavigableSet holds its lock while it adds to the backing set")
    public void safeNavigableSetTakesTheLock() {
        assertEquals(1, lockHeldDuringAdd(true, true));
        assertEquals(0, lockHeldDuringAdd(false, true));
    }

    /// Builds a set through the protected constructor the builders call, adds one element to it, and returns how
    /// many of the backing set's `add` calls ran with the breakable's lock held by the calling thread.
    private static int lockHeldDuringAdd(final boolean isSafe, final boolean navigable) {
        final ProbingTreeSet backing = new ProbingTreeSet();
        final BreakableSortedSet<String> breakable = navigable
                ? new BreakableNavigableSet<>(backing, new HashSet<>(), new HashMap<>(),
                        BreakableSortedSet.DEFAULT_CHARACTERISTICS, BreakableSortedSet.DEFAULT_PERMITS, isSafe,
                        Object.class)
                : new BreakableSortedSet<>(backing, new HashSet<>(), new HashMap<>(),
                        BreakableSortedSet.DEFAULT_CHARACTERISTICS, BreakableSortedSet.DEFAULT_PERMITS, isSafe,
                        Object.class);
        backing.probe = () -> isLockHeldByCurrentThread(breakable);
        assertEquals(isSafe, breakable.isSafe());
        assertTrue(breakable.add("a"));
        return backing.heldCount;
    }

    /// Reads the breakable's private lock and reports whether the current thread holds it.
    private static boolean isLockHeldByCurrentThread(final AbstractBreakable breakable) {
        try {
            final Field field = AbstractBreakable.class.getDeclaredField("lock");
            field.setAccessible(true);
            final ReentrantLock lock = (ReentrantLock) field.get(breakable);
            return lock != null && lock.isHeldByCurrentThread();
        } catch (final ReflectiveOperationException e) {
            throw new AssertionError("Cannot read AbstractBreakable's lock", e);
        }
    }

    /// A backing set that counts the `add` calls made while its probe reports the lock held.
    private static final class ProbingTreeSet extends TreeSet<String> {
        private static final long serialVersionUID = 1L;

        private transient BooleanSupplier probe = () -> false;
        private int heldCount;

        @Override
        public boolean add(final String e) {
            if (probe.getAsBoolean()) {
                heldCount++;
            }
            return super.add(e);
        }
    }
}
