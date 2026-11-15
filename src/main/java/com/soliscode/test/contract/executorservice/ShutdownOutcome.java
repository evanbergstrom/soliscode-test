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
package com.soliscode.test.contract.executorservice;

/// What became of a task's submission in a round of [ThreadSafeShutdownContract], as recorded in the round's
/// outcomes.
final class ShutdownOutcome {
    /// Not submitted.
    static final int NONE = 0;
    /// Accepted after the shutdowner set its flag, or while it was setting it.
    static final int ACCEPTED = 1;
    /// Accepted, with `execute` returned before the shutdowner set its flag.
    static final int ACCEPTED_BEFORE_SHUTDOWN = 2;
    /// Rejected.
    static final int REJECTED = 3;

    private ShutdownOutcome() { }
}
