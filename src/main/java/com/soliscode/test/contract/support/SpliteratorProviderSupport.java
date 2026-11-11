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

import java.util.Spliterator;

import com.soliscode.test.provider.SpliteratorProvider;

/// Interface for contract classes that test a spliterator, made by a [SpliteratorProvider].
///
/// @param <S> the type of the spliterator under test
/// @param <E> the element type
/// @since 0.1.3
public interface SpliteratorProviderSupport<S extends Spliterator<E>, E> {

    /// Returns the provider of the sources, and the spliterators under test over them.
    ///
    /// @return the spliterator provider
    SpliteratorProvider<S, E> spliteratorProvider();
}
