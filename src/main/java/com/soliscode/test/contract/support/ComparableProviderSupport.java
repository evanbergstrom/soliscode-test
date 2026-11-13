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

import com.soliscode.test.provider.ComparableProvider;

/// Interface for contract classes that test a [Comparable], whose samples a [ComparableProvider] makes.
///
/// @param <T> the type under test
/// @since 0.1.3
public interface ComparableProviderSupport<T extends Comparable<? super T>> {

    /// Returns the provider of the ordered samples of the type under test.
    ///
    /// @return the comparable provider
    ComparableProvider<T> comparableProvider();
}
