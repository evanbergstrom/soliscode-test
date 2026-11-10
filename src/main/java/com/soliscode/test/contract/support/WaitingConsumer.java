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

import org.jspecify.annotations.Nullable;

/// A consumer of [TransferQueueContractSupport#withWaitingConsumers]: its thread, whether it is done, and what it
/// received. `finish` and `release` both hold its monitor, so that no interrupt outlives the consumer's action.
///
/// @param <E> the element type
final class WaitingConsumer<E> {
    private @Nullable Thread thread;
    private boolean done;
    private @Nullable E received;

    /// Creates a consumer that has not started.
    WaitingConsumer() {
    }

    /// Records the current thread as the consumer's.
    synchronized void start() {
        thread = Thread.currentThread();
    }

    /// Records what the consumer received, marks it done, and clears its interrupt status.
    synchronized void finish(final @Nullable E element) {
        received = element;
        done = true;
        boolean ignored = Thread.interrupted();
    }

    /// Interrupts the consumer if it has started and is not done.
    synchronized void release() {
        Thread t = thread;
        if (!done && t != null) {
            t.interrupt();
        }
    }

    synchronized boolean isDone() {
        return done;
    }

    synchronized @Nullable Thread thread() {
        return thread;
    }

    synchronized @Nullable E received() {
        return received;
    }
}
