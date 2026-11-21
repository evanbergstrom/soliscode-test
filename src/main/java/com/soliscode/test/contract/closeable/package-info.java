/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

/// The contract of the [java.io.Closeable] interface: [CloseableContract], which extends `AutoCloseableContract`, and
/// [CloseableCloseContract], the contract of its one method, which requires idempotence. It has no method enum:
/// `close` is not optional.
///
/// @since 0.1.3
@NullMarked
package com.soliscode.test.contract.closeable;

import org.jspecify.annotations.NullMarked;
