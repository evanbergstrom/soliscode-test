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

package org.soliscode.test;

/// An interface used to indicate which of the optional methods of another interface that this class supports.
///
/// @author evanbergstrom
/// @since 1.0.0
public interface SupportedMethods {

    /// Used to determine if this object supports an optional method.
    /// @param method The identifier for the method.
    /// @return 'true' if the method is supported. `false` if it is not.
    boolean supportsMethod(OptionalMethod method);
}
