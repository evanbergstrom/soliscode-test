/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

/// The contract of the [java.util.concurrent.Future] interface: [FutureContract], with a method contract for each of
/// its eight methods, and [ThreadSafeFutureContract], which adds the tests of what its Javadoc says holds under
/// concurrent use ([ThreadSafeGetContract], [ThreadSafeCancelContract]). It has no method enum: no method of
/// `Future` is optional.
///
/// @since 0.1.4
@NullMarked
package com.soliscode.test.contract.future;

import org.jspecify.annotations.NullMarked;
