/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.executorservice;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.ExecutorServiceContractSupport;
import com.soliscode.test.provider.DeliberateException;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [submit(Callable)][ExecutorService#submit(Callable)] method of an [ExecutorService].
///
/// It follows the Javadoc: it returns "a Future representing the pending results of the task", whose `get` "will
/// return the task's result upon successful completion", and throws [NullPointerException] "if the task is null".
/// A task that throws gives an [ExecutionException] from `get`, as [Future#get()] says; its cause is read loosely,
/// as the exception the task threw or, as `ForkJoinTask` documents, a copy of the same class whose cause is it
/// ([#assertCauseIs]).
///
/// @param <E> the type of the executor under test
/// @since 0.1.5
/// @see ExecutorService#submit(Callable)
public interface SubmitCallableContract<E extends ExecutorService> extends ExecutorServiceContractSupport<E> {

    /// Tests that the future `submit(Callable)` returns gives the callable's result.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("submit(Callable) gives the callable's result")
    default void submitCallableGivesItsResult() throws Exception {
        using(executor -> {
            Object value = new Object();
            Future<Object> future = executor.submit(() -> value);
            assertSame(value, bounded(future::get), "get() of the future submit(Callable) returned");
        });
    }

    /// Tests that the future `submit(Callable)` returns throws [ExecutionException] from `get` when the callable
    /// throws, with the callable's exception as its cause.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("submit(Callable) gives the callable's exception as an ExecutionException")
    default void submitCallableGivesItsException() throws Exception {
        using(executor -> {
            Exception failure = new DeliberateException("submitCallableGivesItsException");
            Future<Object> future = executor.submit(() -> {
                throw failure;
            });
            ExecutionException thrown = assertThrows(ExecutionException.class, () -> bounded(future::get),
                    "get() of the future of a callable that threw");
            assertCauseIs(failure, thrown, "get()");
        });
    }

    /// Tests that `submit((Callable) null)` throws [NullPointerException].
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("submit((Callable) null) throws NullPointerException")
    // NullAway: passes null to submit on purpose, to test that it throws.
    @SuppressWarnings("NullAway")
    default void submitCallableOfNullThrowsNullPointerException() throws Exception {
        using(executor -> assertThrows(NullPointerException.class,
                () -> executor.submit((Callable<Object>) null), "submit((Callable) null)"));
    }
}
