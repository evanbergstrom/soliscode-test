/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.provider;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `FutureProviders`: that its sources give pending futures and finish them as they say.
///
/// @see FutureProviders
@DisplayName("Tests for FutureProviders")
public class FutureProvidersTest {

    /// Default constructor.
    public FutureProvidersTest() {
        super();
    }

    private static <F extends Future<Integer>> void assertSourcesFinish(final FutureProvider<F, Integer> provider)
            throws Exception {
        FutureSource<F, Integer> first = provider.pending();
        FutureSource<F, Integer> second = provider.pending();
        assertFalse(first.future().isDone(), "a new source's future");
        assertNotEquals(first.value(), second.value(), "two sources' values");
        assertSame(first.exception(), first.exception(), "a source's exception, called twice");
        first.succeed();
        assertEquals(first.value(), first.future().get());
        second.fail();
        assertSame(second.exception(), assertThrows(ExecutionException.class, second.future()::get).getCause());
        FutureSource<F, Integer> nulled = provider.pending();
        nulled.succeedWith(null);
        assertTrue(nulled.future().isDone());
        assertNull(nulled.future().get());
    }

    @Test
    @DisplayName("completableFutures() gives pending CompletableFutures that its sources finish")
    void completableFutures() throws Exception {
        assertSourcesFinish(FutureProviders.completableFutures());
    }

    @Test
    @DisplayName("futureTasks() gives pending FutureTasks that its sources finish by running them")
    void futureTasks() throws Exception {
        assertSourcesFinish(FutureProviders.futureTasks());
    }

    @Test
    @DisplayName("A source's finishing call after cancel reaches the future, which stays cancelled")
    void finishingAfterCancel() {
        FutureSource<FutureTask<Integer>, Integer> task = FutureProviders.futureTasks().pending();
        assertTrue(task.future().cancel(false));
        task.succeed();
        assertTrue(task.future().isCancelled());
        FutureSource<CompletableFuture<Integer>, Integer> completable = FutureProviders.completableFutures().pending();
        assertTrue(completable.future().cancel(false));
        completable.fail();
        assertTrue(completable.future().isCancelled());
    }

    @Test
    @DisplayName("The factories refuse null arguments")
    void factoriesRefuseNull() {
        assertThrows(NullPointerException.class, () -> FutureProviders.completedBy(null,
                CompletableFuture<Integer>::complete, CompletableFuture::completeExceptionally, () -> 1));
        assertThrows(NullPointerException.class, () -> FutureProviders.<CompletableFuture<Integer>, Integer>completedBy(
                CompletableFuture::new, null, CompletableFuture::completeExceptionally, () -> 1));
        assertThrows(NullPointerException.class, () -> FutureProviders.<CompletableFuture<Integer>, Integer>completedBy(
                CompletableFuture::new, CompletableFuture::complete, null, () -> 1));
        assertThrows(NullPointerException.class, () -> FutureProviders.<CompletableFuture<Integer>, Integer>completedBy(
                CompletableFuture::new, CompletableFuture::complete, CompletableFuture::completeExceptionally, null));
        assertThrows(NullPointerException.class,
                () -> FutureProviders.<FutureTask<Integer>, Integer>ofTask(null, FutureTask::run, () -> 1));
        assertThrows(NullPointerException.class,
                () -> FutureProviders.<FutureTask<Integer>, Integer>ofTask(FutureTask::new, null, () -> 1));
        assertThrows(NullPointerException.class,
                () -> FutureProviders.<FutureTask<Integer>, Integer>ofTask(FutureTask::new, FutureTask::run, null));
    }
}
