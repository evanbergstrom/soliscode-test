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
package com.soliscode.test.contract.sequencedcollection;

import java.util.SequencedCollection;
import java.util.function.Supplier;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.annotations.ConcurrencyTest;
import com.soliscode.test.annotations.VerySlow;
import com.soliscode.test.methods.SequencedCollectionMethods;

/// **Contract for `reversed()` of a thread-safe `SequencedCollection` under contention**
///
/// The run of [ThreadSafeAddRemoveFirstLastContract], with every call made through a view the collection's
/// `reversed()` returns, fresh for each call: `reversed()`, and the view, must be thread safe with the collection.
///
/// [SequencedCollection#reversed]: "If the collection implementation permits modifications to this view, the
/// modifications 'write through' to the underlying collection." A view may refuse them, so the test first tries
/// `addFirst`, `addLast`, `removeFirst` and `removeLast` once each through the view of another instance, and skips
/// itself if any throws [UnsupportedOperationException].
///
/// @param <C> the type of collection
/// @param <E> the type of elements
/// @see SequencedCollection#reversed
/// @since 0.1.6
public interface ThreadSafeReversedContract<C extends SequencedCollection<E>, E>
        extends ThreadSafeSequencedCollectionContractSupport<C, E> {

    /// Tests that elements added at both ends of reversed views by producers and removed at one end of reversed views
    /// by consumers, all at once, are each removed at most once, and that none is lost.
    @DisplayName("reversed() and its view are thread safe under contention at both ends")
    @VerySlow
    @ConcurrencyTest
    default void reversedViewWhenUsedUnderContentionIsThreadSafe() {
        if (supportsMethods(SequencedCollectionMethods.REVERSED, SequencedCollectionMethods.ADD_FIRST,
                SequencedCollectionMethods.ADD_LAST, SequencedCollectionMethods.REMOVE_FIRST,
                SequencedCollectionMethods.REMOVE_LAST) && viewPermitsModification()) {
            assertAddsAndRemovesAtBothEndsAreThreadSafe(SequencedCollection::reversed, "reversed()");
        }
    }

    private boolean viewPermitsModification() {
        Supplier<E> supplier = elementProvider().uniqueInstanceSupplier();
        SequencedCollection<E> view = provider().emptyInstance().reversed();
        try {
            view.addFirst(supplier.get());
            view.addLast(supplier.get());
            view.removeFirst();
            view.removeLast();
            return true;
        } catch (final UnsupportedOperationException e) {
            return false;
        }
    }
}
