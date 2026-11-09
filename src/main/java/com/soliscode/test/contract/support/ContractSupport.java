/*
 * Copyright 2024 Evan Bergstrom
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

import com.soliscode.test.SupportedMethods;

/// Common interface for contract classes.
/// @param <T> the class of the objects being tested.
/// @author evanbergstrom
/// @since 1.0
public interface ContractSupport<T> extends ProviderSupport<T>, SupportedMethods {

    /// Returns the number of times an operation is repeated when a contract tests that it gives the same
    /// result each time, such as `equals` called twice on the same arguments.
    ///
    /// A test class may override it to repeat more or fewer times.
    ///
    /// @return the number of repetitions; 10 unless overridden
    /// @since 0.1.1
    default int consistencyRepeats() {
        return 10;
    }

    /// Returns whether the class under test compares its values for equality, so that a value and a copy of it are
    /// equal and have the same hash code.
    ///
    /// The tests of `equals` and `hashCode` that compare a value with its copy run only where this returns `true`.
    /// A class that keeps [Object]'s identity equality, as most queues do ([java.util.Queue]: implementations
    /// "generally do not define element-based versions of methods equals and hashCode"), returns `false`; the tests
    /// that hold for identity equality (reflexive, `null`, another type, a stable hash code) still run.
    ///
    /// @return `true` unless overridden
    /// @since 0.1.2
    default boolean equalsComparesValues() {
        return true;
    }
}
