/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
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
