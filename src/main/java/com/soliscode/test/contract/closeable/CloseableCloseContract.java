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
package com.soliscode.test.contract.closeable;

import java.io.Closeable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.autocloseable.CloseContract;

/// Contract for the [close()][Closeable#close()] method of a [Closeable].
///
/// It is [CloseContract], with idempotence required: "If the stream is already closed then invoking this method has
/// no effect", says [Closeable#close()]. The test of a second `close` runs whatever [#closeIsIdempotent()] returns.
///
/// @param <C> the type of the resource under test
/// @since 0.1.3
/// @see Closeable#close()
public interface CloseableCloseContract<C extends Closeable> extends CloseContract<C> {

    /// Returns `true`: [Closeable#close()] requires idempotence. Overriding this has no effect on a `Closeable`
    /// contract, whose test of a second `close` does not read it.
    ///
    /// @return `true`
    @Override
    default boolean closeIsIdempotent() {
        return true;
    }

    /// Tests that a second `close()` has no effect: it returns normally, and each use then does what it does once
    /// closed. `Closeable` requires it, so it runs whatever [#closeIsIdempotent()] returns.
    @Test
    @Override
    @DisplayName("close() twice has no effect, as Closeable requires")
    default void closeTwiceHasNoEffect() {
        assertCloseTwiceHasNoEffect();
    }
}
