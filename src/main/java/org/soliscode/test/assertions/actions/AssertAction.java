/*
 * Copyright 2024 Evan Bergstrom
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

package org.soliscode.test.assertions.actions;

/// Interface for classes that are used to test methods that accept actions (*i.e.* suppliers, consumers, *etc.*).
///
/// @author evanbergstrom
/// @since 1.0
public interface AssertAction {

    /// An assertion method that is called after a method had used the action and returned. This is used to test
    /// assertions on the aggregate effect of calling an action multiple times during the execution of a method.
    /// @throws org.opentest4j.AssertionFailedError if the assertion fails.
    default void assertCheck() {
    }
}
