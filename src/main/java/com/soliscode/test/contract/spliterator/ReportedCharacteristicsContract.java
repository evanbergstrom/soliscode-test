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
package com.soliscode.test.contract.spliterator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Spliterator;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.SpliteratorContractSupport;
import com.soliscode.test.provider.SpliteratorSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for what each characteristic a [Spliterator] reports promises: a test for each of `ORDERED`, `DISTINCT`,
/// `SORTED`, `SIZED`, `SUBSIZED`, `NONNULL`, `IMMUTABLE` and `CONCURRENT`. A test passes where the spliterator does
/// not report its characteristic.
///
/// Its readings of [Spliterator]:
/// - `CONCURRENT` with `SIZED`, or with `IMMUTABLE`, on a top-level spliterator (each one a source makes is) fails:
///   the Javadoc says it "should not" report them together and calls one that does "inconsistent"; the contract
///   reads that as a requirement, more strictly than the text.
/// - `IMMUTABLE` and `CONCURRENT` are tested with a removal from the source during a traversal, where the source can
///   make one ([SpliteratorSource#removeOne()]). An `IMMUTABLE` spliterator must finish without an exception and
///   encounter every element it had: "such changes cannot occur during traversal", so the removal does not reach
///   it (a `CopyOnWriteArrayList`'s works on a snapshot). A `CONCURRENT` one must finish without an exception, each
///   element it encounters one the source held before the removal: it "may be safely concurrently modified". What
///   else it encounters is its class's policy, not `Spliterator`'s.
/// - The policy of a spliterator that reports neither (fail-fast, or late-binding) is its class's, and not tested.
///
/// @param <S> the type of the spliterator under test
/// @param <E> the element type
/// @since 0.1.3
/// @see Spliterator#characteristics()
public interface ReportedCharacteristicsContract<S extends Spliterator<E>, E>
        extends SpliteratorContractSupport<S, E> {

    /// Tests that an `ORDERED` spliterator's parts, split until no part splits and traversed prefix first, encounter
    /// the elements in the order an unsplit spliterator does.
    @Test
    @DisplayName("ORDERED: the parts of a split, prefix first, traverse as the unsplit spliterator does")
    default void orderedCharacteristicHolds() {
        SpliteratorSource<S, E> source = defaultSource();
        if (!reports(source.spliterator(), Spliterator.ORDERED)) {
            return;
        }
        List<E> unsplit = traverse(source.spliterator());
        List<E> split = new ArrayList<>();
        for (final Spliterator<E> part : this.<Spliterator<E>>leaves(source.spliterator(), Spliterator::trySplit)) {
            split.addAll(traverse(part));
        }
        assertEquals(unsplit, split, "the elements of the parts, prefix first, against those of the unsplit "
                + "spliterator");
    }

    /// Tests that a `DISTINCT` spliterator encounters no two equal elements.
    @Test
    @DisplayName("DISTINCT: no two elements encountered are equal")
    default void distinctCharacteristicHolds() {
        Spliterator<E> spliterator = defaultSource().spliterator();
        if (!reports(spliterator, Spliterator.DISTINCT)) {
            return;
        }
        List<E> encountered = traverse(spliterator);
        assertEquals(encountered.size(), new HashSet<>(encountered).size(),
                () -> "a DISTINCT spliterator encountered equal elements: " + encountered);
    }

    /// Tests that a `SORTED` spliterator also reports `ORDERED`, that its `getComparator()` does not throw, and that
    /// it encounters the elements in that comparator's order (natural order where it is `null`). Where the source
    /// documents `SORTED`, `getComparator()` must be `null` where the source is sorted in natural order ("If the
    /// source is SORTED in natural order, returns null"), and otherwise order the source's elements as the source's
    /// comparator does: `SortedSet#spliterator` allows "the same as or impose the same total ordering as" the
    /// set's comparator, so the two are compared by how they order every pair of elements, not by identity.
    @Test
    @DisplayName("SORTED: ORDERED is reported too, getComparator() is the source's, and the elements are in its order")
    default void sortedCharacteristicHolds() {
        SpliteratorSource<S, E> source = defaultSource();
        Spliterator<E> spliterator = source.spliterator();
        if (!reports(spliterator, Spliterator.SORTED)) {
            return;
        }
        assertTrue(reports(spliterator, Spliterator.ORDERED), "a SORTED spliterator must also report ORDERED");
        Comparator<? super E> comparator = assertDoesNotThrow(spliterator::getComparator,
                "getComparator() of a SORTED spliterator");
        if ((source.characteristics() & Spliterator.SORTED) != 0) {
            assertSameOrdering(source.comparator(), comparator, source.elements());
        }
        List<E> encountered = traverse(spliterator);
        List<E> sorted = new ArrayList<>(encountered);
        // A null comparator sorts in natural order, as a SORTED spliterator's null comparator means.
        sorted.sort(comparator);
        assertEquals(sorted, encountered, () -> "the elements of a SORTED spliterator, by its comparator "
                + (comparator == null ? "(natural order)" : comparator));
    }

    /// Tests that a `SIZED` spliterator, before it is traversed or split, estimates the exact size, and that
    /// `getExactSizeIfKnown()` returns it: on an empty source, and on one of `DEFAULT_SIZE` elements. A part
    /// `trySplit` returns that reports `SIZED` has itself been neither traversed nor split, so it too must estimate
    /// its own count exactly. What is left after a split has been split, and need not.
    @Test
    @DisplayName("SIZED: before traversal or split, estimateSize() and getExactSizeIfKnown() are the exact size")
    default void sizedCharacteristicHolds() {
        for (final SpliteratorSource<S, E> source : List.of(sourceOf(List.of()), defaultSource())) {
            int size = source.elements().size();
            Spliterator<E> spliterator = source.spliterator();
            if (reports(spliterator, Spliterator.SIZED)) {
                assertEquals(size, spliterator.estimateSize(), "estimateSize() of a SIZED spliterator");
                assertEquals(size, spliterator.getExactSizeIfKnown(), "getExactSizeIfKnown() of a SIZED spliterator");
            }
        }
        Spliterator<E> part = defaultSource().spliterator().trySplit();
        if (part != null && reports(part, Spliterator.SIZED)) {
            long estimate = part.estimateSize();
            long exact = part.getExactSizeIfKnown();
            List<E> elements = traverse(part);
            assertEquals(elements.size(), estimate, "estimateSize() of a SIZED part, against its elements");
            assertEquals(elements.size(), exact, "getExactSizeIfKnown() of a SIZED part, against its elements");
        }
    }

    /// Tests that a `SUBSIZED` spliterator also reports `SIZED`, and that, split recursively, every part a split
    /// returns, direct or indirect, reports `SIZED` and `SUBSIZED` ("all child Spliterators, whether direct or
    /// indirect, will be SIZED"), that the two estimates of each split sum to the estimate before it, and that each
    /// part, and each remainder, estimates its own count at the end.
    ///
    /// The JDK promises `SIZED` and `SUBSIZED` of the parts a split returns, not of what is left ("the
    /// characteristics ... before splitting may differ from the characteristics after splitting"): a remainder is
    /// split again only while it still reports `SUBSIZED`. Its count is checked all the same, since the sum rule
    /// makes its estimate exact.
    @Test
    @DisplayName("SUBSIZED: SIZED is reported too, and every part of every split is SIZED, with sizes that sum")
    default void subsizedCharacteristicHolds() {
        Spliterator<E> spliterator = defaultSource().spliterator();
        if (!reports(spliterator, Spliterator.SUBSIZED)) {
            return;
        }
        assertTrue(reports(spliterator, Spliterator.SIZED), "a SUBSIZED spliterator must also report SIZED");
        int[] splits = {0};
        assertSubsizedSplits(spliterator, splits);
    }

    /// Splits a `SUBSIZED` spliterator until it returns `null`, checking each split and each part recursively, then
    /// checks that its estimate is its count.
    ///
    /// @param spliterator a spliterator that must be `SIZED` and `SUBSIZED`
    /// @param splits the number of splits so far, bounded by `TRAVERSAL_LIMIT`
    private void assertSubsizedSplits(final Spliterator<E> spliterator, final int[] splits) {
        int both = Spliterator.SIZED | Spliterator.SUBSIZED;
        long before = spliterator.estimateSize();
        Spliterator<E> part = spliterator.trySplit();
        while (part != null) {
            splits[0]++;
            assertTrue(splits[0] < TRAVERSAL_LIMIT, "trySplit() did not stop splitting");
            assertTrue(reports(part, both), "a part split off a SUBSIZED spliterator must be SIZED and SUBSIZED");
            long partSize = part.estimateSize();
            long leftSize = spliterator.estimateSize();
            assertEquals(before, partSize + leftSize, "the estimates of the two parts, against the estimate before");
            assertSubsizedSplits(part, splits);
            before = leftSize;
            part = reports(spliterator, Spliterator.SUBSIZED) ? spliterator.trySplit() : null;
        }
        assertEquals(before, traverse(spliterator).size(), "estimateSize() of a part, against its elements");
    }

    /// Tests that a `NONNULL` spliterator encounters no `null` element.
    @Test
    @DisplayName("NONNULL: no element encountered is null")
    default void nonNullCharacteristicHolds() {
        Spliterator<E> spliterator = defaultSource().spliterator();
        if (!reports(spliterator, Spliterator.NONNULL)) {
            return;
        }
        List<E> encountered = traverse(spliterator);
        for (int i = 0; i < encountered.size(); i++) {
            assertNotNull(encountered.get(i), "element " + i + " encountered by a NONNULL spliterator");
        }
    }

    /// Tests that an `IMMUTABLE` spliterator does not report `CONCURRENT`, and, where the source can remove an
    /// element, that a removal during its traversal does not reach it.
    @Test
    @DisplayName("IMMUTABLE: CONCURRENT is not reported, and a removal from the source does not reach it")
    default void immutableCharacteristicHolds() {
        SpliteratorSource<S, E> source = defaultSource();
        List<E> before = source.elements();
        Spliterator<E> spliterator = source.spliterator();
        if (!reports(spliterator, Spliterator.IMMUTABLE)) {
            return;
        }
        assertFalse(reports(spliterator, Spliterator.CONCURRENT),
                "a top-level IMMUTABLE spliterator must not report CONCURRENT");
        List<E> encountered = new ArrayList<>();
        spliterator.tryAdvance(encountered::add);
        if (source.removeOne()) {
            assertDoesNotThrow(() -> spliterator.forEachRemaining(encountered::add),
                    "an IMMUTABLE spliterator's traversal after a removal from the source");
            assertSameElements(before, encountered, documentsOrder(source),
                    "the elements an IMMUTABLE spliterator encountered, with a removal from the source between");
        }
    }

    /// Tests that a `CONCURRENT` spliterator reports neither `SIZED` nor `IMMUTABLE`, and, where the source can
    /// remove an element, that a removal during its traversal raises no exception and that it encounters only
    /// elements the source held before.
    @Test
    @DisplayName("CONCURRENT: neither SIZED nor IMMUTABLE is reported, and a removal from the source is safe")
    default void concurrentCharacteristicHolds() {
        SpliteratorSource<S, E> source = defaultSource();
        List<E> before = source.elements();
        Spliterator<E> spliterator = source.spliterator();
        if (!reports(spliterator, Spliterator.CONCURRENT)) {
            return;
        }
        assertFalse(reports(spliterator, Spliterator.SIZED), "a top-level CONCURRENT spliterator must not be SIZED");
        assertFalse(reports(spliterator, Spliterator.IMMUTABLE),
                "a top-level CONCURRENT spliterator must not report IMMUTABLE");
        List<E> encountered = new ArrayList<>();
        spliterator.tryAdvance(encountered::add);
        if (source.removeOne()) {
            assertDoesNotThrow(() -> spliterator.forEachRemaining(encountered::add),
                    "a CONCURRENT spliterator's traversal after a removal from the source");
            for (final E e : encountered) {
                assertTrue(before.contains(e), () -> "a CONCURRENT spliterator encountered " + e
                        + ", which the source did not hold");
            }
        }
    }

    /// Asserts that a spliterator's comparator is `null` where its source's is (natural order), and otherwise orders
    /// every pair of the elements as its source's does.
    ///
    /// @param expected the source's comparator, `null` for natural order
    /// @param actual the spliterator's
    /// @param elements the elements to compare
    private static <T> void assertSameOrdering(final @Nullable Comparator<? super T> expected,
                                               final @Nullable Comparator<? super T> actual, final List<T> elements) {
        if (expected == null) {
            assertNull(actual, "getComparator() of a spliterator over a source sorted in natural order");
            return;
        }
        assertNotNull(actual, () -> "getComparator() of a spliterator over a source sorted by " + expected);
        for (final T a : elements) {
            for (final T b : elements) {
                assertEquals(Integer.signum(expected.compare(a, b)), Integer.signum(actual.compare(a, b)),
                        () -> "getComparator() orders " + a + " and " + b + " otherwise than the source's comparator");
            }
        }
    }

    /// Returns whether a spliterator's `characteristics()` contains every given characteristic.
    ///
    /// @param spliterator the spliterator
    /// @param characteristics the characteristics
    /// @return whether it reports them all
    private static boolean reports(final Spliterator<?> spliterator, final int characteristics) {
        return (spliterator.characteristics() & characteristics) == characteristics;
    }
}
