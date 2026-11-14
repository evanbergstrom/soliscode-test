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

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/// Factories of [FutureProvider]s: of futures finished by calls on them, of futures over a computation, and of
/// `CompletableFuture` and `FutureTask`.
///
/// @since 0.1.4
public final class FutureProviders {

    private FutureProviders() { }

    /// A provider of the futures `make` makes, finished by calls on them, as `CompletableFuture` is by `complete` and
    /// `completeExceptionally`.
    ///
    /// @param make     makes a new future, not yet done, each call
    /// @param complete completes a future with a result, which may be `null`
    /// @param fail     completes a future with an exception
    /// @param values   gives each source's value, not null
    /// @param <F>      the type of the future
    /// @param <V>      the type of its result
    /// @return the provider
    /// @throws NullPointerException if an argument is null
    public static <F extends Future<V>, V> FutureProvider<F, V> completedBy(
            final Supplier<? extends F> make, final BiConsumer<? super F, ? super @Nullable V> complete,
            final BiConsumer<? super F, ? super Exception> fail, final Supplier<? extends V> values) {
        Objects.requireNonNull(make, "make");
        Objects.requireNonNull(complete, "complete");
        Objects.requireNonNull(fail, "fail");
        Objects.requireNonNull(values, "values");
        return () -> {
            F future = make.get();
            V value = values.get();
            Exception exception = failure();
            return new FutureSource<>() {
                @Override
                public F future() {
                    return future;
                }

                @Override
                public V value() {
                    return value;
                }

                @Override
                public Exception exception() {
                    return exception;
                }

                @Override
                public void succeedWith(final @Nullable V result) {
                    complete.accept(future, result);
                }

                @Override
                public void fail() {
                    fail.accept(future, exception);
                }
            };
        };
    }

    /// A provider of the futures `make` makes over a computation, finished by running it, as `FutureTask` is by
    /// `run()`. The computation returns the result, or throws the exception, that the source was last asked for.
    ///
    /// @param make   makes a new future over the given computation, not yet run
    /// @param run    runs a future's computation
    /// @param values gives each source's value, not null
    /// @param <F>    the type of the future
    /// @param <V>    the type of its result
    /// @return the provider
    /// @throws NullPointerException if an argument is null
    public static <F extends Future<V>, V> FutureProvider<F, V> ofTask(
            final Function<? super Callable<@Nullable V>, ? extends F> make, final Consumer<? super F> run,
            final Supplier<? extends V> values) {
        Objects.requireNonNull(make, "make");
        Objects.requireNonNull(run, "run");
        Objects.requireNonNull(values, "values");
        return () -> {
            V value = values.get();
            Exception exception = failure();
            Outcome<V> outcome = new Outcome<>();
            Callable<@Nullable V> computation = () -> {
                if (outcome.failed) {
                    throw exception;
                }
                return outcome.result;
            };
            F future = make.apply(computation);
            return new FutureSource<>() {
                @Override
                public F future() {
                    return future;
                }

                @Override
                public V value() {
                    return value;
                }

                @Override
                public Exception exception() {
                    return exception;
                }

                @Override
                public void succeedWith(final @Nullable V result) {
                    outcome.set(false, result);
                    run.accept(future);
                }

                @Override
                public void fail() {
                    outcome.set(true, null);
                    run.accept(future);
                }
            };
        };
    }

    /// `CompletableFuture`s, finished by `complete` and `completeExceptionally`, over distinct `Integer` values.
    ///
    /// @return the provider
    public static FutureProvider<CompletableFuture<Integer>, Integer> completableFutures() {
        return completedBy(CompletableFuture::new, CompletableFuture::complete,
                CompletableFuture::completeExceptionally, new AtomicInteger()::incrementAndGet);
    }

    /// `FutureTask`s, finished by `run()`, over distinct `Integer` values.
    ///
    /// @return the provider
    public static FutureProvider<FutureTask<Integer>, Integer> futureTasks() {
        return ofTask(FutureTask::new, FutureTask::run, new AtomicInteger()::incrementAndGet);
    }

    private static Exception failure() {
        return new Exception("the future's failure, made by its source");
    }

    /// What a task's computation does when run: set by the source before it runs the task, read by the thread that
    /// runs it, which is the same thread.
    private static final class Outcome<V> {
        private boolean failed;
        private @Nullable V result;

        void set(final boolean failedArg, final @Nullable V resultArg) {
            this.failed = failedArg;
            this.result = resultArg;
        }
    }
}
