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
package com.soliscode.test.performance;

import java.time.Duration;

/// A health check that says whether a process of a [ProcessTopology] is ready.
///
/// The topology polls it until it answers `true` or the process's ready timeout passes. Each call is given the
/// time left, and must return within it (a connect or a read with that time as its limit): an answer after the
/// timeout counts as not ready in time, whatever it says. A call that never returns holds the caller, since the
/// topology starts no thread to bound it.
///
/// @since 0.1.6
@FunctionalInterface
public interface ReadinessCheck {

    /// Returns whether the process is ready.
    ///
    /// @param remaining the time left before the timeout, above 0
    /// @return `true` if it is ready
    /// @throws InterruptedException if it is interrupted, which ends the start with its interruption
    /// @throws Exception if the check fails otherwise, which counts as not ready yet
    boolean isReady(Duration remaining) throws Exception;
}
