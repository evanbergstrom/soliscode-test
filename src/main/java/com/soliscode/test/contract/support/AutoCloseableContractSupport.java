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

import java.util.List;

import org.junit.platform.commons.util.UnrecoverableExceptions;
import org.opentest4j.AssertionFailedError;

import com.soliscode.test.provider.ResourceUse;

import static org.junit.jupiter.api.Assertions.assertThrows;

/// The base interface of the auto-closeable contracts: it gives them the provider's resources and uses, checked, the
/// steps that open, use and close a resource, and whether the class under test claims that its `close` is idempotent.
///
/// Each resource is closed exactly once, except by the tests of a second `close`. A use that fails on an open resource
/// still closes it, and a failing close is then added to the use's failure as suppressed, so that it does not hide
/// it.
///
/// @param <C> the type of the resource under test
/// @since 0.1.3
public interface AutoCloseableContractSupport<C extends AutoCloseable> extends AutoCloseableProviderSupport<C> {

    /// Whether the class under test claims that its `close` is idempotent: that a second call has no effect.
    /// [AutoCloseable#close()] strongly encourages it, without requiring it; it is tested unless a class that does not
    /// claim it overrides this to return `false` (the user's decision on #41). [java.io.Closeable#close()] requires
    /// it, so `CloseableContract` tests it whatever this returns.
    ///
    /// @return `true` unless overridden
    default boolean closeIsIdempotent() {
        return true;
    }

    /// Returns a new, open resource from the provider, checked: a provider that gives `null` fails the test with a
    /// message that says the provider, not the class under test, is at fault.
    ///
    /// @return the resource
    default C openResource() {
        C resource = autoCloseableProvider().open();
        if (resource == null) {
            throw new AssertionFailedError("the auto-closeable provider is at fault: it opened a null resource");
        }
        return resource;
    }

    /// Returns the provider's uses, checked: a null list or a null use fails the test with a message that says the
    /// provider is at fault.
    ///
    /// @return the uses
    default List<ResourceUse<? super C>> resourceUses() {
        List<ResourceUse<? super C>> uses = autoCloseableProvider().uses();
        if (uses == null) {
            throw new AssertionFailedError("the auto-closeable provider is at fault: its uses are null");
        }
        for (int i = 0; i < uses.size(); i++) {
            if (uses.get(i) == null) {
                throw new AssertionFailedError("the auto-closeable provider is at fault: its use " + i + " is null");
            }
        }
        return uses;
    }

    /// Returns a new resource, closed once. A `close` that throws fails the test.
    ///
    /// @return the closed resource
    default C closedResource() {
        C resource = openResource();
        close(resource, "close() of an open resource");
        return resource;
    }

    /// Closes a resource. A `close` that throws fails the test, with the exception as its cause.
    ///
    /// @param resource the resource
    /// @param what     what the call is, for the failure message
    default void close(final C resource, final String what) {
        try {
            resource.close();
        } catch (final Exception e) {
            throw new AssertionFailedError(what + " threw " + e, e);
        }
    }

    /// Calls a use on a new, open resource, where it must complete, and then closes the resource.
    ///
    /// @param use the use
    default void useWhileOpen(final ResourceUse<? super C> use) {
        C resource = openResource();
        AssertionFailedError failure = null;
        try {
            use.action().accept(resource);
        } catch (final Throwable t) {
            UnrecoverableExceptions.rethrowIfUnrecoverable(t);
            failure = new AssertionFailedError(use.name() + " threw " + t + " on an open resource", t);
        }
        try {
            resource.close();
        } catch (final Exception e) {
            AssertionFailedError closeFailure = new AssertionFailedError("close() after " + use.name() + " threw "
                    + e, e);
            if (failure == null) {
                failure = closeFailure;
            } else {
                failure.addSuppressed(closeFailure);
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    /// Calls a use on a closed resource and checks that it does what its class says once closed: throws an exception
    /// of its type, or a subtype, or completes.
    ///
    /// @param use      the use
    /// @param resource the closed resource
    /// @param when     when it is called, for the failure message
    default void assertUseOnceClosed(final ResourceUse<? super C> use, final C resource, final String when) {
        Class<? extends Throwable> thrown = use.thrownOnceClosed();
        if (thrown != null) {
            assertThrows(thrown, () -> use.action().accept(resource), () -> use.name() + " " + when);
            return;
        }
        try {
            use.action().accept(resource);
        } catch (final Throwable t) {
            UnrecoverableExceptions.rethrowIfUnrecoverable(t);
            throw new AssertionFailedError(use.name() + " threw " + t + " " + when + ", where its class says it works"
                    + " once closed", t);
        }
    }

    /// Checks that a second `close` has no effect: it completes, and each use then does what it does once closed.
    /// The idempotence tests call it.
    default void assertCloseTwiceHasNoEffect() {
        close(closedResource(), "a second close()");
        for (final ResourceUse<? super C> use : resourceUses()) {
            C resource = closedResource();
            close(resource, "a second close()");
            assertUseOnceClosed(use, resource, "after a second close()");
        }
    }
}
