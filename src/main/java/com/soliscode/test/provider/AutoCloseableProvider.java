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
