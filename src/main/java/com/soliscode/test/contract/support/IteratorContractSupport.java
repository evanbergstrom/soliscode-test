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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;

import com.soliscode.test.SupportedMethods;
import com.soliscode.test.provider.IteratorSource;

/// The base interface of the iterator contracts: it gives them the sources of the iterators under test, the elements,
/// which optional methods are supported, and the assertions they share.
///
/// Where a source is not [ordered][IteratorSource#ordered()], the assertions compare elements as a multiset.
///
/// @param <I> the type of the iterator under test
/// @param <E> the element type
/// @since 0.1.3
public interface IteratorContractSupport<I extends Iterator<E>, E>
        extends IteratorProviderSupport<I, E>, ElementProviderSupport<E>, SupportedMethods {

    /// The number of elements ({@value}) a test's source holds, unless it says otherwise.
    int DEFAULT_SIZE = 10;

    /// The most elements ({@value}) a test traverses before it fails: an iterator that does not end must fail the
    /// test, not hang it.
    int TRAVERSAL_LIMIT = 10_000;

    /// Returns `n` unique elements from the element provider.
    ///
    /// @param n the number of elements
    /// @return the elements, in the order they were made
    default List<E> uniqueElements(final int n) {
        Supplier<E> supplier = elementProvider().uniqueInstanceSupplier();
        List<E> elements = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            elements.add(supplier.get());
        }
        return elements;
    }

    /// Returns a new source that holds the given elements.
    ///
    /// @param elements the elements
    /// @return the source
    default IteratorSource<I, E> sourceOf(final List<E> elements) {
        return iteratorProvider().source(elements);
    }

    /// Returns every element left in an iterator, calling `next()` until `hasNext()` is false, and fails if there
    /// are more than [#TRAVERSAL_LIMIT].
    ///
    /// @param iterator the iterator
    /// @return the elements, in the order it returned them
    default List<E> drain(final Iterator<E> iterator) {
        List<E> returned = new ArrayList<>();
        while (iterator.hasNext()) {
            Assertions.assertTrue(returned.size() < TRAVERSAL_LIMIT, "the iterator did not end");
            returned.add(iterator.next());
        }
        return returned;
    }

    /// Asserts that elements are the expected ones: in order where the source is ordered, as a multiset where not.
    ///
    /// @param expected the expected elements, in order where the source is ordered
    /// @param actual the elements
    /// @param source the source, which says whether order counts
    /// @param description what the elements are, for the failure message
    default void assertSameElements(final List<E> expected, final List<E> actual, final IteratorSource<I, E> source,
                                    final String description) {
        if (source.ordered()) {
            Assertions.assertEquals(expected, actual, description);
        } else {
            Assertions.assertEquals(counts(expected), counts(actual),
                    () -> description + ": expected " + expected + " in any order, but was " + actual);
        }
    }

    /// Asserts that the source holds the expected elements now: in order where it is ordered, as a multiset where not.
    ///
    /// @param expected the elements it should hold
    /// @param source the source
    default void assertSourceHolds(final List<E> expected, final IteratorSource<I, E> source) {
        assertSameElements(expected, source.elements(), source, "the source's elements");
    }

    private static <T> Map<T, Integer> counts(final List<T> elements) {
        Map<T, Integer> counts = new HashMap<>();
        for (final T e : elements) {
            counts.merge(e, 1, Integer::sum);
        }
        return counts;
    }
}
