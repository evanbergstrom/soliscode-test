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
package com.soliscode.test.contract.sortedset;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.Spliterator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.contract.sequencedset.SequencedSetContract;
import com.soliscode.test.contract.set.SetContract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for the `SortedSet` interface**
///
/// This contract extends [SetContract] and [SequencedSetContract] and adds a method contract for each method that
/// [SortedSet] declares: `comparator`, `first`, `last`, `headSet`, `tailSet` and `subSet`, and its overrides of
/// `addFirst`, `addLast` (which throw [UnsupportedOperationException]) and `reversed`. Its own tests check that
/// iteration and the spliterator follow the set's order.
///
/// The expected results come from a [java.util.TreeSet] holding the same elements, with the set's `comparator()`
/// ([com.soliscode.test.contract.support.SortedSetContractSupport]). `SortedSet`'s own methods are not optional
/// operations, so the contract tests them always; it has no method enum. The inherited tests of
/// `SequencedCollection` expect a sorted set's elements in its comparator's order
/// ([com.soliscode.test.contract.support.CollectionContractSupport#encounterOrder]).
///
/// ## Usage Examples
///
/// ```java
/// class TreeSetTest extends AbstractTest
///         implements SortedSetContract<TreeSet<Integer>, Integer>, WithIntegerElement {
///
///     @Override
///     public CollectionProvider<TreeSet<Integer>, Integer> provider() {
///         return CollectionProviders.from(TreeSet::new, TreeSet::new, TreeSet::new, PROVIDER);
///     }
///
///     @Override
///     public boolean permitNulls() {
///         return false;
///     }
///
///     @Override
///     public boolean permitDuplicates() {
///         return false;
///     }
///
///     @Override
///     public boolean permitIncompatibleTypes() {
///         return false;
///     }
/// }
/// ```
///
/// ## Thread Safety
///
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the tests depends on
/// the [SortedSet] and [com.soliscode.test.provider.CollectionProvider] implementations being tested.
///
/// @param <S> the sorted set type being tested
/// @param <E> the element type being tested
/// @see SortedSet
/// @since 0.1.2
public interface SortedSetContract<S extends SortedSet<E>, E> extends SetContract<S, E>, SequencedSetContract<S, E>,
        ComparatorContract<S, E>,
        FirstContract<S, E>,
        LastContract<S, E>,
        HeadSetContract<S, E>,
        TailSetContract<S, E>,
        SubSetContract<S, E>,
        SortedAddFirstContract<S, E>,
        SortedAddLastContract<S, E>,
        SortedReversedContract<S, E> {

    @Override
    boolean supportsMethod(InterfaceMethod method);

    @Override
    void doesNotSupportMethod(InterfaceMethod method);

    /// Tests that [iterator()][SortedSet#iterator()] returns the elements in ascending order, as [SortedSet] requires:
    /// in the order of a [java.util.TreeSet] with the same elements and comparator.
    @Test
    @DisplayName("iterator() returns the elements in the set's order")
    default void iteratorWhenCalledReturnsElementsInOrder() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        S set = provider().createInstance(values);
        List<E> iterated = new ArrayList<>();
        Iterator<E> iterator = set.iterator();
        while (iterator.hasNext() && iterated.size() <= TRAVERSAL_LIMIT) {
            iterated.add(iterator.next());
        }
        assertIterableEquals(sortedReference(set, values), iterated, "iterator() is not in the set's order");
    }

    /// Tests that [spliterator()][SortedSet#spliterator()] reports [Spliterator#SORTED] and [Spliterator#ORDERED]
    /// (`DISTINCT` is [SetContract]'s), and that its comparator is `null` where the set's is, and otherwise orders
    /// the elements as the set's does ("the same as or impose the same total ordering").
    @Test
    @DisplayName("spliterator() reports SORTED and ORDERED, with the set's ordering")
    default void spliteratorWhenCalledReportsSortedAndOrdered() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        S set = provider().createInstance(values);
        Spliterator<E> spliterator = set.spliterator();
        assertTrue(spliterator.hasCharacteristics(Spliterator.SORTED), "the spliterator does not report SORTED");
        assertTrue(spliterator.hasCharacteristics(Spliterator.ORDERED), "the spliterator does not report ORDERED");
        Comparator<? super E> setOrder = set.comparator();
        Comparator<? super E> spliteratorOrder = spliterator.getComparator();
        if (setOrder == null) {
            assertNull(spliteratorOrder, "the set's comparator is null, but its spliterator's is not");
        } else {
            assertNotNull(spliteratorOrder, "the set has a comparator, but its spliterator's is null");
            List<E> sorted = new ArrayList<>(sortedReference(set, values));
            for (int i = 1; i < sorted.size(); i++) {
                E before = sorted.get(i - 1);
                E after = sorted.get(i);
                assertEquals(Integer.signum(setOrder.compare(before, after)),
                        Integer.signum(spliteratorOrder.compare(before, after)),
                        () -> "the spliterator's comparator orders " + before + " and " + after + " otherwise");
            }
        }
    }
}
