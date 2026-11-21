/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.provider;

import java.util.concurrent.Future;

/// Makes the futures a [com.soliscode.test.contract.future.FutureContract] tests: each a new future that is not yet
/// done, as a [FutureSource] that can finish it.
///
/// `Future` has no method that completes a future, so the provider says how: by calls on the future, as
/// `CompletableFuture` is finished, or by running the computation it is over, as `FutureTask` is.
///
/// ## Usage Examples
/// ```java
/// class MyFutureTest extends AbstractTest implements FutureContract<MyFuture<Integer>, Integer> {
///     @Override
///     public FutureProvider<MyFuture<Integer>, Integer> futureProvider() {
///         return FutureProviders.completedBy(MyFuture::new, MyFuture::complete, MyFuture::fail,
///                 new AtomicInteger()::incrementAndGet);
///     }
/// }
/// ```
///
/// @param <F> the type of the future
/// @param <V> the type of its result
/// @since 0.1.4
/// @see FutureProviders
public interface FutureProvider<F extends Future<V>, V> {

    /// Returns a new future that is not yet done, with the way to finish it. The contract finishes or cancels each
    /// one it uses.
    ///
    /// @return the source of the future
    FutureSource<F, V> pending();
}
