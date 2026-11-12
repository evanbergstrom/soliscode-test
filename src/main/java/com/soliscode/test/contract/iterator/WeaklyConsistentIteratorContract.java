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
package com.soliscode.test.contract.iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.WeakConsistencyChecks;
import com.soliscode.test.contract.support.WeakConsistencyChecks.Change;
import com.soliscode.test.provider.IteratorSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for weakly consistent iterators**: [IteratorContract], and what the `java.util.concurrent` package
/// summary says of such iterators, "Weakly consistent": they "may proceed concurrently with other operations", "will
/// never throw ConcurrentModificationException", and "are guaranteed to traverse elements as they existed upon
/// construction exactly once, and may (but are not guaranteed to) reflect any modifications subsequent to
/// construction".
///
/// Each test makes a source of unique elements and changes it during a traversal, other than through the iterator:
/// an element it returned removed, one it had not returned removed, one added, and a change made by the action of
/// `forEachRemaining`. It checks that the iterator throws no [java.util.ConcurrentModificationException], returns no
/// element twice, returns every element the source held throughout, and returns only elements the source held.
/// [WeakConsistencyChecks] says how it reads the text: "exactly once" is read more strictly, for added elements too,
/// and order is not checked.
///
/// The source must be changeable: its [IteratorSource#removeFromSource] and [IteratorSource#addToSource] must do
/// what they say, or a test fails. The add test first removes one element, before the iterator is made, so that a
/// bounded source built full from the elements has room.
///
/// The concurrent collections' contracts (`BlockingQueueContract`, `BlockingDequeContract`, `TransferQueueContract`,
/// `ConcurrentMapContract`, `ConcurrentNavigableMapContract`) run the same checks on their iterators and views; this
/// contract is for an iterator tested by itself.
///
/// ## Usage Examples
/// ```java
/// class ConcurrentLinkedQueueIteratorTest extends AbstractTest
///         implements WeaklyConsistentIteratorContract<Iterator<Integer>, Integer>, WithIntegerElement {
///     @Override
///     public IteratorProvider<Iterator<Integer>, Integer> iteratorProvider() {
///         return IteratorProviders.fromCollection(ConcurrentLinkedQueue::new);
///     }
/// }
/// ```
///
/// @param <I> the type of the iterator under test
/// @param <E> the element type
/// @since 0.1.3
public interface WeaklyConsistentIteratorContract<I extends Iterator<E>, E> extends IteratorContract<I, E> {

    /// Tests the iterator with an element it has returned removed from the source.
    @Test
    @DisplayName("a weakly consistent iterator, with an element it returned removed from the source")
    default void iteratorWhenReturnedElementIsRemovedIsWeaklyConsistent() {
        checkWeakConsistency(Change.RETURNED_ELEMENT_REMOVED);
    }

    /// Tests the iterator with an element it has not returned removed from the source.
    @Test
    @DisplayName("a weakly consistent iterator, with an element it had not returned removed from the source")
    default void iteratorWhenUnreturnedElementIsRemovedIsWeaklyConsistent() {
        checkWeakConsistency(Change.UNRETURNED_ELEMENT_REMOVED);
    }

    /// Tests the iterator with an element added to the source.
    @Test
    @DisplayName("a weakly consistent iterator, with an element added to the source")
    default void iteratorWhenElementIsAddedIsWeaklyConsistent() {
        checkWeakConsistency(Change.ELEMENT_ADDED);
    }

    /// Tests `forEachRemaining` with the source changed by its action.
    @Test
    @DisplayName("forEachRemaining of a weakly consistent iterator, with the source changed by its action")
    default void forEachRemainingWhenSourceChangesIsWeaklyConsistent() {
        checkWeakConsistency(Change.CHANGED_DURING_FOR_EACH_REMAINING);
    }

    private void checkWeakConsistency(final Change change) {
        List<E> all = uniqueElements(DEFAULT_SIZE + 1);
        List<E> elements = new ArrayList<>(all.subList(0, DEFAULT_SIZE));
        IteratorSource<I, E> source = sourceOf(elements);
        assertTrue(WeakConsistencyChecks.check(WeakConsistencyChecks.of(source), elements, all.getLast(), change,
                        true, "the iterator"),
                "the source refused a change: a weakly consistent iterator is tested on a source that "
                        + "IteratorSource.removeFromSource and addToSource can change");
    }
}
