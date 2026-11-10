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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// The base interface of the [ConcurrentMap] contracts: the null switches set as a concurrent map's are, and the
/// assertions the contracts share.
///
/// Every default of [ConcurrentMap] "assumes that the ConcurrentMap cannot contain null values", and the JDK's
/// concurrent maps refuse null keys and values. So [#permitNullKeys()] and [#permitNullValues()] are `false` here; a
/// test of a concurrent map that permits nulls overrides them, and the null tests then do not run.
///
/// @param <M> the concurrent map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentMap
/// @since 0.1.2
public interface ConcurrentMapContractSupport<M extends ConcurrentMap<K, V>, K, V> extends MapContractSupport<M, K, V> {

    /// {@inheritDoc}
    ///
    /// @return `false`, as a concurrent map refuses null keys
    @Override
    default boolean permitNullKeys() {
        return false;
    }

    /// {@inheritDoc}
    ///
    /// @return `false`, as a concurrent map refuses null values
    @Override
    default boolean permitNullValues() {
        return false;
    }

    /// Asserts that a call throws `NullPointerException` and leaves the map as it was.
    ///
    /// @param map the map the call acts on
    /// @param call the call
    /// @param description what the call is, for the message
    default void assertNullPointerAndUnchanged(final M map, final Executable call, final String description) {
        Map<K, V> before = new HashMap<>(map);
        assertThrows(NullPointerException.class, call, description);
        assertEquals(before, new HashMap<>(map), description + " changed the map");
    }

    /// Asserts that a call whose `NullPointerException` the JDK makes optional either throws it or gives the other
    /// result it allows, and leaves the map as it was.
    ///
    /// @param map the map the call acts on
    /// @param expected the result the call may give instead of throwing
    /// @param call the call
    /// @param description what the call is, for the message
    default void assertResultOrNullPointerAndUnchanged(final M map, final boolean expected,
                                                       final BooleanSupplier call, final String description) {
        Map<K, V> before = new HashMap<>(map);
        try {
            assertEquals(expected, call.getAsBoolean(), description);
        } catch (final NullPointerException e) {
            // The exception the JDK allows.
        }
        assertEquals(before, new HashMap<>(map), description + " changed the map");
    }

    /// Asserts that a call whose `NullPointerException` the JDK makes optional either throws it or returns the other
    /// result it allows.
    ///
    /// @param <T> the result type
    /// @param expected the result the call may give instead of throwing
    /// @param call the call
    /// @param description what the call is, for the message
    default <T> void assertResultOrNullPointer(final T expected, final Supplier<T> call, final String description) {
        try {
            assertEquals(expected, call.get(), description);
        } catch (final NullPointerException e) {
            // The exception the JDK allows.
        }
    }
}
