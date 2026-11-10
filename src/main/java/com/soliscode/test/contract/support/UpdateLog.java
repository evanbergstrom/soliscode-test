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
package com.soliscode.test.contract.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/// A log of updates to a key's value, for checking that no update was lost without counting.
///
/// Each update records the unique value it makes and the value it replaced (`record(new, old)`), whether or not the
/// map ends up storing it. A map that retries may call a function more than once. The values of the calls that lost
/// are logged too, but never stored, so no later update replaces them. After the run, the chain from a key's final
/// value back through the log reaches its first value. Every value a call reported as stored must be on that chain:
/// a lost update leaves one off it.
///
/// The values must be unique: each made once, none equal to another.
///
/// The log is thread safe.
///
/// @param <V> the value type
/// @since 0.1.2
public final class UpdateLog<V> {

    private final Map<V, V> previous = new ConcurrentHashMap<>();

    /// Creates an empty log.
    public UpdateLog() {
        super();
    }

    /// Records an update: the value made, and the value it replaced.
    ///
    /// @param newValue the value made
    /// @param oldValue the value it replaced
    public void record(final V newValue, final V oldValue) {
        V earlier = previous.putIfAbsent(newValue, oldValue);
        if (earlier != null && !earlier.equals(oldValue)) {
            fail("the value " + newValue + " was made twice: the values must be unique");
        }
    }

    /// Returns the chain from a key's final value back to its first, final value first.
    ///
    /// @param first the key's first value
    /// @param last the key's final value
    /// @return the chain
    public List<V> chain(final V first, final V last) {
        List<V> chain = new ArrayList<>();
        V value = last;
        chain.add(value);
        while (!value.equals(first)) {
            @Nullable V before = previous.get(value);
            if (before == null) {
                fail("the final value's chain of updates does not reach the first value: " + value
                        + " was not made by an update");
            }
            value = before;
            chain.add(value);
            if (chain.size() > previous.size() + 1) {
                fail("the chain of updates has a cycle");
            }
        }
        return chain;
    }

    /// Asserts that each value a call reported as stored is on the chain from the final value back to the first: no
    /// update was lost.
    ///
    /// @param first the key's first value
    /// @param last the key's final value
    /// @param stored the values the calls reported as stored
    /// @param description what the calls were, for the message
    public void assertNoUpdateLost(final V first, final V last, final Collection<V> stored, final String description) {
        Set<V> chain = new HashSet<>(chain(first, last));
        for (final V value : stored) {
            assertTrue(chain.contains(value), () -> description + " lost an update: " + value
                    + " was reported stored, but a later update replaced the value before it");
        }
    }

    /// Asserts that no update was lost, and that the chain holds exactly the updates reported, where every success is
    /// known.
    ///
    /// @param first the key's first value
    /// @param last the key's final value
    /// @param stored the values the calls reported as stored: every success
    /// @param description what the calls were, for the message
    public void assertExactly(final V first, final V last, final Collection<V> stored, final String description) {
        assertNoUpdateLost(first, last, stored, description);
        assertEquals(stored.size() + 1, chain(first, last).size(),
                () -> description + ": the chain of updates is not one per success");
    }
}
