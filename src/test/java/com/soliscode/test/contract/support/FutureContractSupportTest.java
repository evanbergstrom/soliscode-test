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
package com.soliscode.test.contract.support;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import com.soliscode.test.provider.FutureProvider;
import com.soliscode.test.provider.FutureProviders;
import com.soliscode.test.provider.FutureSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `FutureContractSupport`: its checks of the provider, its bounded calls, and its test that a call waits
/// for another thread.
///
/// @see FutureContractSupport
@DisplayName("Tests for FutureContractSupport")
public class FutureContractSupportTest {

    /// Default constructor.
    public FutureContractSupportTest() {
        super();
    }

    /// The support on a given provider.
    private record Support(FutureProvider<CompletableFuture<Integer>, Integer> futureProvider)
            implements FutureContractSupport<CompletableFuture<Integer>, Integer> {
    }

    private static Support support() {
        return new Support(FutureProviders.completableFutures());
    }

    @Test
    @DisplayName("pending() blames the provider for a null source or future, and names isDone for a done one")
    void pendingBlamesTheProvider() {
        Support nullSource = new Support(() -> null);
        assertTrue(assertThrows(AssertionFailedError.class, nullSource::pending).getMessage()
                .contains("provider is at fault"));
        FutureSource<CompletableFuture<Integer>, Integer> real = FutureProviders.completableFutures().pending();
        Support nullFuture = new Support(() -> new FutureSource<>() {
            @Override
            public CompletableFuture<Integer> future() {
                return null;
            }

            @Override
            public Integer value() {
                return 1;
            }

            @Override
            public Exception exception() {
                return real.exception();
            }

            @Override
            public void succeedWith(final Integer result) {
            }

            @Override
            public void fail() {
            }
        });
        assertTrue(assertThrows(AssertionFailedError.class, nullFuture::pending).getMessage()
                .contains("null future"));
        real.succeed();
        Support done = new Support(() -> real);
        assertTrue(assertThrows(AssertionFailedError.class, done::pending).getMessage().contains("isDone()"));
    }

    @Test
    @DisplayName("The finished sources are done as their names say, and a null result is permitted by default")
    void finishedSources() throws Exception {
        Support support = support();
        assertTrue(support.permitsNullResult());
        FutureSource<CompletableFuture<Integer>, Integer> succeeded = support.succeeded();
        support.assertSucceeded(succeeded.future(), succeeded.value(), "once succeeded");
        FutureSource<CompletableFuture<Integer>, Integer> failed = support.failed();
        support.assertFailed(failed.future(), failed.exception(), "once failed");
        support.assertCancelled(support.cancelled().future(), "once cancelled");
        assertEquals(succeeded.value(), support.resultNow(succeeded.future()));
        assertEquals(failed.exception(), support.exceptionNow(failed.future()));
        assertEquals(succeeded.value(), support.get(succeeded.future(), 0, TimeUnit.SECONDS));
        assertThrows(AssertionFailedError.class,
                () -> support.assertSucceeded(failed.future(), 1, "of a failed future"));
    }

    @Test
    @DisplayName("bounded() rethrows what the call throws, and boundedInterrupted() sets and clears the status")
    void boundedCalls() throws Exception {
        Support support = support();
        assertEquals(3, support.bounded(() -> 3));
        assertThrows(IllegalStateException.class, () -> support.bounded(() -> {
            throw new IllegalStateException("thrown");
        }));
        assertThrows(StackOverflowError.class, () -> support.bounded(() -> {
            throw new StackOverflowError("thrown");
        }));
        support.boundedInterrupted(() -> assertTrue(Thread.currentThread().isInterrupted()));
        assertThrows(InterruptedException.class,
                () -> support.boundedInterrupted(new CompletableFuture<Integer>()::get));
        assertFalse(Thread.currentThread().isInterrupted());
    }

    @Test
    @DisplayName("assertWaitsFor() passes a call that waits, and fails one that returns before the release")
    void assertWaitsFor() {
        Support support = support();
        FutureSource<CompletableFuture<Integer>, Integer> source = support.pending();
        support.assertWaitsFor(source.future(), source.future()::get, source::succeed);
        FutureSource<CompletableFuture<Integer>, Integer> early = support.pending();
        assertThrows(AssertionError.class, () -> support.assertWaitsFor(early.future(), () -> { }, early::succeed));
        assertThrows(AssertionError.class, () -> support.assertWaitsFor(early.future(), () -> {
            throw new java.io.IOException("checked");
        }, () -> { }));
        assertFalse(support.isWaiting(null));
        assertFalse(support.isWaiting(Thread.currentThread()));
    }
}
