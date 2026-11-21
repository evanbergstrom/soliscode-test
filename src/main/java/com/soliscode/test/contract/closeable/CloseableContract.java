/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
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
