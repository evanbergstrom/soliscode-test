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
package com.soliscode.test.contract.future;

import java.util.concurrent.Future;

/// **Contract for the thread safety of a `Future`**
///
/// It extends [FutureContract] with the tests of what [Future]'s Javadoc says holds under concurrent use: that a
/// `get`, timed or not, sees a completion another thread makes while it waits, with what that thread did before it
/// ([ThreadSafeGetContract]), and that when several threads cancel a future at once, at least one of them returns
/// `true` ([ThreadSafeCancelContract]).
///
/// The tests are tagged [com.soliscode.test.annotations.ConcurrencyTest] and
/// [com.soliscode.test.annotations.VerySlow], and run on a [com.soliscode.test.safety.ContentionCoordinator], with a
/// deadline of their own that bounds them in time.
///
/// ## Usage Examples
/// ```java
/// class FutureTaskSafetyTest extends AbstractTest
///         implements ThreadSafeFutureContract<FutureTask<Integer>, Integer> {
///     @Override
///     public FutureProvider<FutureTask<Integer>, Integer> futureProvider() {
///         return FutureProviders.futureTasks();
///     }
/// }
/// ```
///
/// @param <F> the type of the future under test
/// @param <V> the type of its result
/// @see Future
/// @since 0.1.4
public interface ThreadSafeFutureContract<F extends Future<V>, V> extends FutureContract<F, V>,
        ThreadSafeGetContract<F, V>, ThreadSafeCancelContract<F, V> {
}
