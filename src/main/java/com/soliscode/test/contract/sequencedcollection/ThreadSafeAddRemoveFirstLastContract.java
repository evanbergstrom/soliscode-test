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

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.annotations.ConcurrencyTest;
import com.soliscode.test.annotations.VerySlow;
import com.soliscode.test.methods.SequencedCollectionMethods;

/// **Contract for `addFirst`, `addLast`, `removeFirst` and `removeLast` of a thread-safe `SequencedCollection` under
/// contention**
///
/// Producers add unique elements at both ends while consumers remove them from one end; nothing may be lost or
/// removed twice ([ThreadSafeSequencedCollectionContractSupport#assertAddsAndRemovesAtBothEndsAreThreadSafe]).
///
/// @param <C> the type of collection
/// @param <E> the type of elements
/// @see SequencedCollection#addFirst
/// @see SequencedCollection#removeFirst
/// @since 0.1.6
public interface ThreadSafeAddRemoveFirstLastContract<C extends SequencedCollection<E>, E>
        extends ThreadSafeSequencedCollectionContractSupport<C, E> {

    /// Tests that elements added at both ends by producers and removed at one end by consumers, all at once, are each
    /// removed at most once, and that none is lost; first with the consumers at the first end, then at the last.
    @DisplayName("addFirst, addLast, removeFirst and removeLast are thread safe under contention")
    @VerySlow
    @ConcurrencyTest
    default void addAndRemoveAtBothEndsWhenCalledUnderContentionAreThreadSafe() {
        if (supportsMethods(SequencedCollectionMethods.ADD_FIRST, SequencedCollectionMethods.ADD_LAST,
                SequencedCollectionMethods.REMOVE_FIRST, SequencedCollectionMethods.REMOVE_LAST)) {
            assertAddsAndRemovesAtBothEndsAreThreadSafe(c -> c, "the collection");
        }
    }
}
