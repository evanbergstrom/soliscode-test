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

import java.util.Comparator;
import java.util.List;
import java.util.Spliterator;

import org.jspecify.annotations.Nullable;

/// A source of elements, and the spliterators under test over it: what a
/// [com.soliscode.test.contract.spliterator.SpliteratorContract] tests.
///
/// A source is made by a [SpliteratorProvider] from a list of elements. A contract takes what a spliterator should
/// encounter from [#elements()], checks the characteristics the source's class documents ([#characteristics()]),
/// and, where the source can be changed ([#removeOne()]), what a removal during traversal does.
///
/// @param <S> the type of the spliterator under test
/// @param <E> the element type
/// @since 0.1.3
public interface SpliteratorSource<S extends Spliterator<E>, E> {

    /// Returns a new top-level spliterator over the source, at its start.
    ///
    /// @return the spliterator under test
    S spliterator();

    /// Returns a copy of what the source holds now: in the order its spliterators encounter them where the source
    /// documents [ORDERED][Spliterator#ORDERED] ([#characteristics()]), in any order where not.
    ///
    /// It must not use a spliterator over the source, or a contract would compare the spliterator with itself; and
    /// it must be a snapshot, which later changes to the source leave as it was.
    ///
    /// @return the elements the source holds
    List<E> elements();

    /// Returns the characteristics the source's class documents its spliterators report (`ArrayList`'s report
    /// `SIZED`, `SUBSIZED` and `ORDERED`), which a contract checks are reported. A spliterator may report more.
    ///
    /// @return the documented characteristics; none unless overridden
    default int characteristics() {
        return 0;
    }

    /// Returns the comparator the source is sorted by, where it documents [SORTED][Spliterator#SORTED]: `null` where
    /// it is sorted in natural order, as [Spliterator#getComparator()] must then answer. A contract checks
    /// `getComparator()` against it.
    ///
    /// It returns `null` for natural order because `getComparator()` does: `null` is the answer it checks for, not
    /// a missing value. A source that documents `SORTED` by a comparator overrides this method too, or the contract
    /// expects natural order. The contract compares a non-null one with the spliterator's by how they order the
    /// source's elements, since `SortedSet#spliterator` allows a comparator that imposes the same total ordering.
    ///
    /// @return the source's comparator, or `null` for natural order; `null` unless overridden
    default @Nullable Comparator<? super E> comparator() {
        return null;
    }

    /// Removes one element from the source, a structural change, and returns `true`; or returns `false`, changing
    /// nothing, if the source cannot be changed (a range, an unmodifiable list, an array).
    ///
    /// A contract calls it during a traversal, to test what an [IMMUTABLE][Spliterator#IMMUTABLE] or
    /// [CONCURRENT][Spliterator#CONCURRENT] spliterator does then. It removes the last element of [#elements()], with
    /// `remove(Object)` where the source is a collection; in an ordered source, that is not one a traversal that has
    /// just begun has passed.
    ///
    /// @return whether an element was removed; `false` unless overridden
    default boolean removeOne() {
        return false;
    }
}
