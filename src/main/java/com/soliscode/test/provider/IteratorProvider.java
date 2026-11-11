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
package com.soliscode.test.provider;

import java.util.Iterator;
import java.util.List;

/// Makes the sources an iterator contract tests: each a new source that holds given elements, with the iterators
/// under test over it.
///
/// ## Usage Examples
/// ```java
/// class MyIteratorTest extends AbstractTest implements IteratorContract<Iterator<Integer>, Integer>,
///         WithIntegerElement {
///     @Override
///     public IteratorProvider<Iterator<Integer>, Integer> iteratorProvider() {
///         return IteratorProviders.fromList(MyList::new);
///     }
/// }
/// ```
///
/// @param <I> the type of the iterator under test
/// @param <E> the element type
/// @since 0.1.3
/// @see IteratorProviders
@FunctionalInterface
public interface IteratorProvider<I extends Iterator<E>, E> {

    /// Returns a new source that holds exactly the given elements, in their order where it is
    /// [ordered][IteratorSource#ordered()].
    ///
    /// @param elements the elements; the list is not changed, and not kept
    /// @return the source
    IteratorSource<I, E> source(List<E> elements);
}
