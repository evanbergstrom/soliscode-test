/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.future;

import java.util.concurrent.Future;

/// **Contract for the [Future] interface.**
///
/// A test class implements it with a [com.soliscode.test.provider.FutureProvider] of new, pending futures of the
/// class under test, each with the way to finish it; JUnit runs its test methods as the class's own. A class whose
/// results cannot be `null` overrides [com.soliscode.test.contract.support.FutureContractSupport#permitsNullResult()]
/// to return `false`. What `Future`'s Javadoc says holds under concurrent use is tested by [ThreadSafeFutureContract],
/// which extends this.
///
/// It extends a method contract for each of `Future`'s eight methods as of Java 25: [CancelContract],
/// [IsCancelledContract], [IsDoneContract], [GetContract], [TimedGetContract], [ResultNowContract],
/// [ExceptionNowContract] and [StateContract]; each says how it reads the Javadoc. Every call that may block runs
/// with a time limit, so that a faulty future fails a test rather than hanging it.
///
/// ## Usage Examples
/// ```java
/// class CompletableFutureTest extends AbstractTest
///         implements FutureContract<CompletableFuture<Integer>, Integer> {
///     @Override
///     public FutureProvider<CompletableFuture<Integer>, Integer> futureProvider() {
///         return FutureProviders.completableFutures();
///     }
/// }
/// ```
///
/// @param <F> the type of the future under test
/// @param <V> the type of its result
/// @since 0.1.4
/// @see Future
public interface FutureContract<F extends Future<V>, V> extends CancelContract<F, V>, IsCancelledContract<F, V>,
        IsDoneContract<F, V>, GetContract<F, V>, TimedGetContract<F, V>, ResultNowContract<F, V>,
        ExceptionNowContract<F, V>, StateContract<F, V> {
}
