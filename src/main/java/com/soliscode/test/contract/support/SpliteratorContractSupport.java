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
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import com.soliscode.test.provider.SpliteratorSource;

/// The base interface of the spliterator contracts: it gives them the sources of the spliterators under test, the
/// elements, and the traversals and assertions they share.
///
/// Every bound counts calls, not elements: a traversal fails after [#TRAVERSAL_LIMIT] calls to `tryAdvance` that
/// return `true`, and a split after as many splits, so that a spliterator that never ends fails a test rather than
/// hanging it, whether or not it calls the action.
///
/// @param <S> the type of the spliterator under test
/// @param <E> the element type
/// @since 0.1.3
public interface SpliteratorContractSupport<S extends Spliterator<E>, E>
        extends SpliteratorProviderSupport<S, E>, ElementProviderSupport<E> {

    /// The number of elements ({@value}) a test's source holds, unless it says otherwise.
    int DEFAULT_SIZE = 10;

    /// The most calls ({@value}) a test makes to `tryAdvance`, or to `trySplit`, before it fails.
    int TRAVERSAL_LIMIT = 10_000;

    /// Returns `n` unique elements from the element provider, in the order it makes them.
    ///
    /// @param n the number of elements
    /// @return the elements
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
    default SpliteratorSource<S, E> sourceOf(final List<E> elements) {
        return spliteratorProvider().source(elements);
    }

    /// Returns a new source of [#DEFAULT_SIZE] unique elements.
    ///
    /// @return the source
    default SpliteratorSource<S, E> defaultSource() {
        return sourceOf(uniqueElements(DEFAULT_SIZE));
    }

    /// Returns whether a source documents [ORDERED][Spliterator#ORDERED]: whether its elements, and its
    /// spliterators, have an order the contracts check.
    ///
    /// @param source the source
    /// @return whether order counts
    default boolean documentsOrder(final SpliteratorSource<S, E> source) {
        return (source.characteristics() & Spliterator.ORDERED) != 0;
    }

    /// Returns every element left in a spliterator, by `tryAdvance`, and fails after [#TRAVERSAL_LIMIT] calls that
    /// return `true`.
    ///
    /// @param spliterator the spliterator
    /// @return the elements, in the order it passed them
    default List<E> traverse(final Spliterator<E> spliterator) {
        List<E> elements = new ArrayList<>();
        int calls = 0;
        while (spliterator.tryAdvance(elements::add)) {
            calls++;
            Assertions.assertTrue(calls < TRAVERSAL_LIMIT, "tryAdvance(Consumer) did not end");
        }
        return elements;
    }

    /// Splits a spliterator, and each part, until each returns `null`, and returns the parts that are left, each
    /// part split off before the one it came from: prefix first, where the spliterator is ordered. It fails after
    /// [#TRAVERSAL_LIMIT] splits.
    ///
    /// @param spliterator the spliterator to split
    /// @param split splits a part, as `trySplit` does
    /// @param <T> the type of the parts
    /// @return the parts, in order
    default <T> List<T> leaves(final T spliterator, final Function<T, @Nullable T> split) {
        List<T> leaves = new ArrayList<>();
        int[] splits = {0};
        addLeaves(spliterator, split, leaves, splits);
        return leaves;
    }

    /// Asserts that elements are the expected ones: in order where `ordered`, as a multiset where not.
    ///
    /// @param expected the expected elements
    /// @param actual the elements
    /// @param ordered whether order counts
    /// @param description what the elements are, for the failure message
    default void assertSameElements(final List<E> expected, final List<E> actual, final boolean ordered,
                                    final String description) {
        if (ordered) {
            Assertions.assertEquals(expected, actual, description);
        } else {
            Assertions.assertEquals(counts(expected), counts(actual),
                    () -> description + ": expected " + expected + " in any order, but was " + actual);
        }
    }

    private static <T> void addLeaves(final T spliterator, final Function<T, @Nullable T> split, final List<T> leaves,
                                      final int[] splits) {
        // Each part split off comes before what is left of the spliterator; the remainder is split in a loop, so
        // that a spliterator that never stops splitting fails at the limit rather than overflowing the stack.
        T part = split.apply(spliterator);
        while (part != null) {
            splits[0]++;
            Assertions.assertTrue(splits[0] < TRAVERSAL_LIMIT, "trySplit() did not stop splitting");
            addLeaves(part, split, leaves, splits);
            part = split.apply(spliterator);
        }
        leaves.add(spliterator);
    }

    private static <T> Map<@Nullable T, Integer> counts(final List<T> elements) {
        Map<@Nullable T, Integer> counts = new HashMap<>();
        for (final T e : elements) {
            counts.merge(e, 1, Integer::sum);
        }
        return counts;
    }
}
