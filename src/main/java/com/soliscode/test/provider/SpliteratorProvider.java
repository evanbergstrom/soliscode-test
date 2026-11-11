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

import java.util.List;
import java.util.Spliterator;

/// Makes the sources a spliterator contract tests: each a new source that holds given elements, with the
/// spliterators under test over it.
///
/// ## Usage Examples
/// ```java
/// class MySpliteratorTest extends AbstractTest
///         implements SpliteratorContract<Spliterator<Integer>, Integer>, WithIntegerElement {
///     @Override
///     public SpliteratorProvider<Spliterator<Integer>, Integer> spliteratorProvider() {
///         return SpliteratorProviders.fromCollection(MyList::new, Spliterator.ORDERED | Spliterator.SIZED, true);
///     }
/// }
/// ```
///
/// @param <S> the type of the spliterator under test
/// @param <E> the element type
/// @since 0.1.3
/// @see SpliteratorProviders
@FunctionalInterface
public interface SpliteratorProvider<S extends Spliterator<E>, E> {

    /// Returns a new source that holds the given elements, as its class keeps them (a sorted set in its order).
    ///
    /// The contracts ask only for unique elements, in the order the element provider makes them, or for none.
    ///
    /// @param elements the elements; the list is not changed, and not kept
    /// @return the source
    /// @throws IllegalArgumentException if the source cannot hold these elements (a range holds only consecutive
    ///         ascending ones)
    SpliteratorSource<S, E> source(List<E> elements);
}
