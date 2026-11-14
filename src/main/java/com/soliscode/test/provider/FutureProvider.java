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
