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
package com.soliscode.test.contract.listiterator;

import java.util.ListIterator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.iterator.IteratorContract;

import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for the [ListIterator] interface.**
///
/// It has [IteratorContract]'s tests and a contract for each method `ListIterator` adds, with the cursor rules its
/// Javadoc states. A test class implements it with an [com.soliscode.test.provider.IteratorProvider] of the list
/// iterator under test (`IteratorProviders.listIteratorsOf`, for a list's) and an element provider, and says through
/// `supportsMethod` which of `remove` ([com.soliscode.test.contract.iterable.IterableMethods#ITERATOR_REMOVE]),
/// `set` and `add` ([ListIteratorMethods]) it does not support. A test runs the branch for a supported method only if
/// every optional method it calls is supported, and an unsupported one is checked only for
/// [UnsupportedOperationException].
///
/// A list iterator's order is its list's, so the source must be
/// [ordered][com.soliscode.test.provider.IteratorSource#ordered()], and every test, [IteratorContract]'s included,
/// checks order.
///
/// ## Usage Examples
/// ```java
/// class ArrayListListIteratorTest extends AbstractTest
///         implements ListIteratorContract<ListIterator<Integer>, Integer>, WithIntegerElement {
///     @Override
///     public IteratorProvider<ListIterator<Integer>, Integer> iteratorProvider() {
///         return IteratorProviders.arrayListListIterator();
///     }
/// }
/// ```
///
/// @param <I> the type of the list iterator under test
/// @param <E> the element type
/// @since 0.1.3
/// @see ListIterator
public interface ListIteratorContract<I extends ListIterator<E>, E>
        extends IteratorContract<I, E>, HasPreviousContract<I, E>, PreviousContract<I, E>, NextIndexContract<I, E>,
            PreviousIndexContract<I, E>, ListIteratorSetContract<I, E>, ListIteratorAddContract<I, E>,
            ListIteratorRemoveContract<I, E> {

    /// Tests that the list iterator's source is ordered, as a list's is.
    @Test
    @DisplayName("a list iterator's source is ordered")
    default void listIteratorSourceIsOrdered() {
        assertTrue(sourceOf(uniqueElements(DEFAULT_SIZE)).ordered(),
                "the source of a ListIterator must be ordered: its order is its list's");
    }
}
