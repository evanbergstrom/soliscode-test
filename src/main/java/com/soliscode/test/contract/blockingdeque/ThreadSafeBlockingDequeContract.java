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
package com.soliscode.test.contract.blockingdeque;

import java.util.concurrent.BlockingDeque;

import com.soliscode.test.contract.blockingqueue.ThreadSafeBlockingQueueContract;

/// **Contract for the thread safety of a `BlockingDeque`**
///
/// [BlockingDeque]: "Like BlockingQueue, a BlockingDeque is thread safe". This contract extends
/// [BlockingDequeContract] and [ThreadSafeBlockingQueueContract], whose tests of the queuing methods run on the deque
/// too (they insert at its last end and remove from its first), with a test of [BlockingDeque]'s own methods under
/// contention at both ends: `putFirst`, `putLast`, `takeFirst` and `takeLast` ([ThreadSafePutTakeFirstLastContract]),
/// and the timed `offerFirst`, `offerLast`, `pollFirst` and `pollLast` ([ThreadSafeOfferPollFirstLastContract]).
/// Each checks that no element is lost or delivered twice.
///
/// The tests are tagged [com.soliscode.test.annotations.ConcurrencyTest] and
/// [com.soliscode.test.annotations.VerySlow], and run on a [com.soliscode.test.safety.ContentionCoordinator], which
/// bounds them in time.
///
/// ## Usage Examples
///
/// ```java
/// class LinkedBlockingDequeSafetyTest extends AbstractTest
///         implements ThreadSafeBlockingDequeContract<LinkedBlockingDeque<Integer>, Integer>, WithIntegerElement {
///
///     @Override
///     public CollectionProvider<LinkedBlockingDeque<Integer>, Integer> provider() {
///         return CollectionProviders.from(LinkedBlockingDeque::new, LinkedBlockingDeque::new,
///                 LinkedBlockingDeque::new, PROVIDER);
///     }
/// }
/// ```
///
/// @param <D> the blocking deque type being tested
/// @param <E> the element type being tested
/// @see BlockingDeque
/// @since 0.1.2
public interface ThreadSafeBlockingDequeContract<D extends BlockingDeque<E>, E> extends BlockingDequeContract<D, E>,
        ThreadSafeBlockingQueueContract<D, E>,
        ThreadSafePutTakeFirstLastContract<D, E>,
        ThreadSafeOfferPollFirstLastContract<D, E> {
}
