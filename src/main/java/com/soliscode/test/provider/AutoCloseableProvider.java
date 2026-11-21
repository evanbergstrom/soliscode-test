/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.provider;

import java.util.List;

/// Makes the resources a [com.soliscode.test.contract.autocloseable.AutoCloseableContract] tests: each a new, open
/// resource, with the uses its class documents and what each does once the resource is closed.
///
/// ## Usage Examples
/// ```java
/// class MyChannelTest extends AbstractTest implements CloseableContract<MyChannel> {
///     @Override
///     public AutoCloseableProvider<MyChannel> autoCloseableProvider() {
///         return AutoCloseableProviders.of(MyChannel::open,
///                 ResourceUse.throwsOnceClosed("send(byte)", ClosedChannelException.class, c -> c.send((byte) 1)),
///                 ResourceUse.worksOnceClosed("name()", c -> c.name()));
///     }
/// }
/// ```
///
/// @param <C> the type of the resource
/// @since 0.1.3
/// @see AutoCloseableProviders
public interface AutoCloseableProvider<C extends AutoCloseable> {

    /// Returns a new, open resource. The contract closes each one it opens.
    ///
    /// @return the resource
    C open();

    /// Returns the uses of a resource that its class documents, each with what it does once the resource is closed.
    /// It may be empty, for a resource with no method but `close`; then the contract tests `close` alone.
    ///
    /// @return the uses
    List<ResourceUse<? super C>> uses();
}
