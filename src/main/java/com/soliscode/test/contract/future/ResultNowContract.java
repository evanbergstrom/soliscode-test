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

import java.util.concurrent.Future;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.FutureContractSupport;
import com.soliscode.test.provider.FutureSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [resultNow()][Future#resultNow()] method of a [Future].
///
/// It follows the Javadoc: it "returns the computed result, without waiting", and throws [IllegalStateException]
/// "if the task has not completed or the task did not complete with a result".
///
/// @param <F> the type of the future under test
/// @param <V> the type of its result
/// @since 0.1.4
/// @see Future#resultNow()
public interface ResultNowContract<F extends Future<V>, V> extends FutureContractSupport<F, V> {

    /// Tests that `resultNow()` returns the value once succeeded.
    ///
    /// @throws Exception if a call on the future throws
    @Test
    @DisplayName("resultNow() returns the result once succeeded")
    default void resultNowReturnsTheResultOnceSucceeded() throws Exception {
        FutureSource<F, V> source = succeeded();
        assertEquals(source.value(), resultNow(source.future()), "resultNow() once succeeded");
    }

    /// Tests that `resultNow()` throws [IllegalStateException] while pending, once failed and once cancelled.
    @Test
    @DisplayName("resultNow() throws IllegalStateException unless succeeded")
    default void resultNowThrowsUnlessSucceeded() {
        F pending = pending().future();
        assertThrows(IllegalStateException.class, () -> resultNow(pending), "resultNow() while pending");
        F failed = failed().future();
        assertThrows(IllegalStateException.class, () -> resultNow(failed), "resultNow() once failed");
        F cancelled = cancelled().future();
        assertThrows(IllegalStateException.class, () -> resultNow(cancelled), "resultNow() once cancelled");
    }
}
