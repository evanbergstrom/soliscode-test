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

/// A source of elements, and the iterators under test over it: what an
/// [com.soliscode.test.contract.iterator.IteratorContract] tests.
///
/// A source is made by an [IteratorProvider] from a list of elements, and holds exactly those. A contract takes what
/// an iterator should return from that list, and uses [#elements()] only to see what the iterator's `remove`, and a
/// list iterator's `set` and `add`, did to the source.
///
/// An iterator with no collection behind it (one over a range, say) supplies a source of its own; one whose contents
/// are fixed can be tested with `remove` unsupported, if its element provider supplies exactly those contents, and an
/// empty source where none are asked for.
///
/// @param <I> the type of the iterator under test
/// @param <E> the element type
/// @since 0.1.3
public interface IteratorSource<I extends Iterator<E>, E> {

    /// Returns a new iterator over the source, at its start.
    ///
    /// @return the iterator under test
    I iterator();

    /// Returns a copy of what the source holds now, in the order its iterators return them where [#ordered()].
    ///
    /// It must not use the iterator under test, or a contract would compare that iterator with itself; and it must be
    /// a snapshot, which later changes to the source leave as it was.
    ///
    /// @return the elements the source holds
    List<E> elements();

    /// Returns whether the source's iterators return its elements in a defined order: that of the list it was made
    /// from, and of [#elements()].
    ///
    /// [Iterator] promises no order; where there is none, the contracts compare elements as a multiset. A
    /// [java.util.ListIterator]'s source is always ordered.
    ///
    /// @return `true` unless overridden
    default boolean ordered() {
        return true;
    }

    /// Removes the element from the source other than through any iterator of it, and returns `true`; or returns
    /// `false`, changing nothing, if the source cannot be changed so.
    ///
    /// [com.soliscode.test.contract.iterator.WeaklyConsistentIteratorContract] calls it during a traversal, to see
    /// what the iterator does after its source changes.
    ///
    /// @param element an element the source holds
    /// @return whether it was removed; `false` unless overridden
    /// @since 0.1.3
    default boolean removeFromSource(final E element) {
        return false;
    }

    /// Adds the element to the source other than through any iterator of it, and returns `true`; or returns `false`,
    /// changing nothing, if the source cannot be changed so.
    ///
    /// [com.soliscode.test.contract.iterator.WeaklyConsistentIteratorContract] calls it during a traversal, to see
    /// what the iterator does after its source changes. Where the source is [#ordered()], [#elements()] afterwards
    /// holds the element where the source put it.
    ///
    /// @param element an element the source does not hold
    /// @return whether it was added; `false` unless overridden
    /// @since 0.1.3
    default boolean addToSource(final E element) {
        return false;
    }
}
