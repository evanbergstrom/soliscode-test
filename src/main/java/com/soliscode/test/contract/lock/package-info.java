/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

/// The contract of the [java.util.concurrent.locks.Lock] interface: [LockContract], with a method contract for each of
/// its six methods, and [ThreadSafeLockContract], which adds the test of what its Javadoc says holds under contention
/// ([ThreadSafeLockingContract]). It has no method enum: no method of `Lock` is optional, though `newCondition()` may
/// be unsupported, which a test class says with
/// [com.soliscode.test.contract.support.LockContractSupport#supportsConditions()].
///
/// @since 0.1.7
@NullMarked
package com.soliscode.test.contract.lock;

import org.jspecify.annotations.NullMarked;
