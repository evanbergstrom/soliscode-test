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

import com.soliscode.test.provider.ComparatorProvider;

/// Interface for contract classes that test a [java.util.Comparator], which a [ComparatorProvider] makes with its
/// samples.
///
/// @param <T> the type the comparator compares
/// @since 0.1.4
public interface ComparatorProviderSupport<T> {

    /// Returns the provider of the comparator under test and its ordered samples.
    ///
    /// @return the comparator provider
    ComparatorProvider<T> comparatorProvider();
}
