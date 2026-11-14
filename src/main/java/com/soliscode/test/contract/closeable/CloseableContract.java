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

import com.soliscode.test.contract.autocloseable.AutoCloseableContract;

/// **Contract for the [Closeable] interface.**
///
/// It is [AutoCloseableContract], with the idempotence `Closeable` requires: [CloseableCloseContract] tests a second
/// `close` always, and overriding
/// [com.soliscode.test.contract.support.AutoCloseableContractSupport#closeIsIdempotent()] has no effect here. A test
/// class implements it with a [com.soliscode.test.provider.AutoCloseableProvider], as for `AutoCloseableContract`.
///
/// ## Usage Examples
/// ```java
/// class BufferedReaderCloseTest extends AbstractTest implements CloseableContract<BufferedReader> {
///     @Override
///     public AutoCloseableProvider<BufferedReader> autoCloseableProvider() {
///         return AutoCloseableProviders.bufferedReader();
///     }
/// }
/// ```
///
/// @param <C> the type of the resource under test
/// @since 0.1.3
/// @see Closeable
public interface CloseableContract<C extends Closeable> extends AutoCloseableContract<C>, CloseableCloseContract<C> {
}
