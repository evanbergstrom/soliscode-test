/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

/// The contract of the [java.util.concurrent.ExecutorService] interface: [ExecutorServiceContract], with a method
/// contract for each of its methods and `Executor`'s `execute`, and [ThreadSafeExecutorServiceContract], which adds
/// the tests of what its Javadoc says holds under concurrent use ([ThreadSafeSubmitContract],
/// [ThreadSafeShutdownContract]). It has no method enum: no method of `ExecutorService` is optional.
///
/// @since 0.1.5
@NullMarked
package com.soliscode.test.contract.executorservice;

import org.jspecify.annotations.NullMarked;
