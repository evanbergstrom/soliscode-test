/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.autocloseable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.AutoCloseableContractSupport;
import com.soliscode.test.provider.ResourceUse;

/// Contract for the [close()][AutoCloseable#close()] method of an [AutoCloseable].
///
/// It follows the Javadoc of [AutoCloseable#close()], and checks what the class under test documents about its other
/// methods once closed, through the provider's [ResourceUse]s. It reads the Javadoc more strictly in two places and
/// more loosely in two:
/// - `close()` on an open resource must return normally. The Javadoc lets it throw, but a resource whose `close`
///   fails cannot be tested further.
/// - Idempotence, which the Javadoc strongly encourages without requiring, is tested by default; a class whose `close`
///   is not idempotent opts out with [#closeIsIdempotent()].
/// - That a `close` that fails marks the resource closed first is not tested: it needs a resource whose `close`
///   fails, which a provider cannot make in general.
/// - That `close` does not throw [InterruptedException] is tested only as far as any throw fails
///   [#closeOnAnOpenResourceCompletes()]; a `close` while the thread is interrupted is not tested.
///
/// With no uses, as [com.soliscode.test.provider.AutoCloseableProviders#withoutUses(java.util.function.Supplier)]
/// makes, the three tests of uses check nothing and pass.
///
/// @param <C> the type of the resource under test
/// @since 0.1.3
/// @see AutoCloseable#close()
public interface CloseContract<C extends AutoCloseable> extends AutoCloseableContractSupport<C> {

    /// Tests that `close()` on a new, open resource returns normally.
    @Test
    @DisplayName("close() on an open resource returns normally")
    default void closeOnAnOpenResourceCompletes() {
        closedResource();
    }

    /// Tests that each use completes on an open resource, each on a new one.
    @Test
    @DisplayName("each use the class documents works while the resource is open")
    default void usesWorkWhileOpen() {
        for (final ResourceUse<? super C> use : resourceUses()) {
            useWhileOpen(use);
        }
    }

    /// Tests that each use the class says throws once closed throws its exception, or a subtype, after `close()`.
    @Test
    @DisplayName("each use the class says throws once closed throws its exception after close()")
    default void usesThatThrowOnceClosedThrow() {
        for (final ResourceUse<? super C> use : resourceUses()) {
            if (use.thrownOnceClosed() != null) {
                assertUseOnceClosed(use, closedResource(), "after close()");
            }
        }
    }

    /// Tests that each use the class says works once closed completes after `close()`.
    @Test
    @DisplayName("each use the class says works once closed works after close()")
    default void usesThatWorkOnceClosedWork() {
        for (final ResourceUse<? super C> use : resourceUses()) {
            if (use.thrownOnceClosed() == null) {
                assertUseOnceClosed(use, closedResource(), "after close()");
            }
        }
    }

    /// Tests that a second `close()` has no effect: it returns normally, and each use then does what it does once
    /// closed. It passes at once where the class does not claim it ([#closeIsIdempotent()]).
    @Test
    @DisplayName("close() twice has no effect, where the class claims it")
    default void closeTwiceHasNoEffect() {
        if (!closeIsIdempotent()) {
            return;
        }
        assertCloseTwiceHasNoEffect();
    }
}
