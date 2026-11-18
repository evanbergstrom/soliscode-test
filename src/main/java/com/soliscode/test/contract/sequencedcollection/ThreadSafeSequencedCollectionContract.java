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

/// **Contract for the thread safety of a `SequencedCollection`**
///
/// [SequencedCollection] says nothing about threads; this contract is for a class that documents itself as thread
/// safe, such as [java.util.concurrent.ConcurrentLinkedDeque], [java.util.concurrent.LinkedBlockingDeque] or a list
/// from [java.util.Collections#synchronizedList]. It extends [SequencedCollectionContract] with tests of the sequenced
/// methods under contention: `addFirst`, `addLast`, `removeFirst` and `removeLast`
/// ([ThreadSafeAddRemoveFirstLastContract]), `getFirst` and `getLast` ([ThreadSafeGetFirstLastContract]), and
/// `reversed()` and its view ([ThreadSafeReversedContract]). Each checks that no element is lost, removed twice, or
/// read as a `null`.
///
/// It does not extend [com.soliscode.test.contract.collection.ThreadSafeCollectionContract], which also checks that
/// bulk operations such as `addAll` and `retainAll` are atomic: `ConcurrentLinkedDeque` and others do not promise
/// that. A class whose bulk operations are atomic too implements both.
///
/// The tests are tagged [com.soliscode.test.annotations.ConcurrencyTest] and
/// [com.soliscode.test.annotations.VerySlow], and run on a [com.soliscode.test.safety.ContentionCoordinator], which
/// bounds them in time.
///
/// ## Usage Examples
///
/// ```java
/// class ConcurrentLinkedDequeSafetyTest extends AbstractTest
///         implements ThreadSafeSequencedCollectionContract<ConcurrentLinkedDeque<Integer>, Integer>,
///         WithIntegerElement, DoesNotPermitNulls {
///
///     // A deque keeps Object's identity equality.
///     @Override
///     public boolean equalsComparesValues() {
///         return false;
///     }
///
///     @Override
///     public CollectionProvider<ConcurrentLinkedDeque<Integer>, Integer> provider() {
///         return CollectionProviders.from(ConcurrentLinkedDeque::new, ConcurrentLinkedDeque::new,
///                 ConcurrentLinkedDeque::new, PROVIDER);
///     }
/// }
/// ```
///
/// @param <C> the type of collection
/// @param <E> the type of elements
/// @see SequencedCollection
/// @since 0.1.6
public interface ThreadSafeSequencedCollectionContract<C extends SequencedCollection<E>, E>
        extends SequencedCollectionContract<C, E>,
        ThreadSafeAddRemoveFirstLastContract<C, E>,
        ThreadSafeGetFirstLastContract<C, E>,
        ThreadSafeReversedContract<C, E> {
}
