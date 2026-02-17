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

package org.soliscode.test.contract.support;

import org.soliscode.test.SupportedMethods;

/// Common interface for contract classes.
/// @param <T> the class of the objects being tested.
/// @author evanbergstrom
/// @since 1.0
public interface ContractSupport<T> extends ProviderSupport<T>, SupportedMethods {

    /// The number of times an operation should be repeated during consistency testing.
    ///
    /// This constant is used in test cases to ensure that operations perform consistently
    /// when executed multiple times. It defines the default number of repetitions for such
    /// tests, providing a balance between thorough validation and execution time. The value
    /// of {@value CONSISTENCY_REPEATS} is considered sufficient for detecting inconsistencies
    /// in most scenarios.
    ///
    /// Usage example:
    /// <pre>
    /// for (int i = 0; i < CONSISTENCY_REPEATS; i++) {
    ///     assertEquals(expected, runOperationUnderTest());
    /// }
    /// </pre>
    ///
    /// @see ContractSupport
    /// @since 1.0
    int CONSISTENCY_REPEATS = 10;
}
