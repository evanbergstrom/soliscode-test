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

/// An operation an [OpenLoopPacer] sends at its rate.
///
/// @since 0.1.6
@FunctionalInterface
public interface PacedOperation {

    /// Runs the operation once; the pacer times it from its scheduled send to its return.
    ///
    /// @param sequence the operation's number, counted from 0 with the warmup's, which a [StageTaps] can use as
    ///     its id
    /// @throws Exception if it fails, which ends the run
    void run(long sequence) throws Exception;
}
