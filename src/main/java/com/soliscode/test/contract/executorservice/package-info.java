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
/// The contract of the [java.util.concurrent.ExecutorService] interface: [ExecutorServiceContract], with a method
/// contract for each of its methods and `Executor`'s `execute`, and [ThreadSafeExecutorServiceContract], which adds
/// the tests of what its Javadoc says holds under concurrent use ([ThreadSafeSubmitContract],
/// [ThreadSafeShutdownContract]). It has no method enum: no method of `ExecutorService` is optional.
///
/// @since 0.1.5
@NullMarked
package com.soliscode.test.contract.executorservice;

import org.jspecify.annotations.NullMarked;
