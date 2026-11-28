/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

/// The contract of the [java.util.concurrent.locks.ReadWriteLock] interface: [ReadWriteLockContract], with a method
/// contract for each of its two methods, and [ThreadSafeReadWriteLockContract], which adds the test of what its
/// Javadoc says holds under contention ([ThreadSafeReadWriteContract]). It has no method enum: neither method is
/// optional.
///
/// @since 0.1.7
@NullMarked
package com.soliscode.test.contract.readwritelock;

import org.jspecify.annotations.NullMarked;
