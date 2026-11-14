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

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.provider.FutureProvider;
import com.soliscode.test.provider.FutureProviders;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `ThreadSafeGetContract` itself: that it fails a future whose `get` misbehaves in ways the breakable does
/// not simulate.
///
/// @see ThreadSafeGetContract
@DisplayName("Tests for ThreadSafeGetContract")
public class ThreadSafeGetContractTest {

    /// Default constructor.
    public ThreadSafeGetContractTest() {
        super();
    }

    /// The contract on a given provider.
    private record Contract(FutureProvider<CompletableFuture<Integer>, Integer> futureProvider)
            implements ThreadSafeGetContract<CompletableFuture<Integer>, Integer> {
    }

    /// A `CompletableFuture` whose untimed `get` throws `InterruptedException` though its thread was not interrupted.
    private static final class InterruptingFuture extends CompletableFuture<Integer> {
        @Override
        public Integer get() throws InterruptedException {
            throw new InterruptedException("thrown by the future");
        }
    }

    @Test
    @DisplayName("A get that throws InterruptedException with nothing interrupting it fails the run")
    void getThatThrowsInterruptedExceptionOfItsOwnFails() {
        Contract contract = new Contract(FutureProviders.completedBy(InterruptingFuture::new,
                CompletableFuture::complete, CompletableFuture::completeExceptionally,
                new java.util.concurrent.atomic.AtomicInteger()::incrementAndGet));
        AssertionError failure = assertThrows(AssertionError.class,
                contract::getWhenCompletedUnderContentionIsThreadSafe);
        assertTrue(failure.getMessage().contains("nothing interrupted its thread"), failure::getMessage);
    }
}
