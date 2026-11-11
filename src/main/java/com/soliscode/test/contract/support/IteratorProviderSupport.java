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

import java.util.Iterator;

import com.soliscode.test.provider.IteratorProvider;

/// Interface for contract classes that test an iterator, made by an [IteratorProvider].
///
/// @param <I> the type of the iterator under test
/// @param <E> the element type
/// @since 0.1.3
public interface IteratorProviderSupport<I extends Iterator<E>, E> {

    /// Returns the provider of the sources, and the iterators under test over them.
    ///
    /// @return the iterator provider
    IteratorProvider<I, E> iteratorProvider();
}
