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
package com.soliscode.test.contract.support;

/// Interface for the contracts of collections whose iterators are weakly consistent: the `java.util.concurrent`
/// collections, whose tests of their iterators after a change to the source ([WeakConsistencyChecks]) run unless a
/// test class turns them off.
///
/// Of the interfaces those contracts test, only [java.util.concurrent.ConcurrentNavigableMap] promises weak
/// consistency in its own Javadoc (for its key sets). For `BlockingQueue`, `BlockingDeque`, `TransferQueue` and
/// `ConcurrentMap` it is the `java.util.concurrent` package summary's ("most concurrent Collection implementations"),
/// which every JDK class of theirs keeps; the contracts read it more strictly than those interfaces, by default.
///
/// @since 0.1.3
public interface WeaklyConsistentIteratorsSupport {

    /// Returns whether the iterators of the class under test, and of its views, are weakly consistent, so that the
    /// contract tests them after a change to the source. A test of a class whose iterators are fail-fast (they throw
    /// [java.util.ConcurrentModificationException]) overrides it to return `false`.
    ///
    /// @return `true` unless overridden
    default boolean iteratorsAreWeaklyConsistent() {
        return true;
    }
}
