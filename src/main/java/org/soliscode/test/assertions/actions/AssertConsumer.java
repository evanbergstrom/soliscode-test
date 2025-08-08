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

import java.util.function.Consumer;

/// Interface for classes that are used to test methods that accept consumer actions (*i.e.* objects that implement
/// the [Consumer] interface).
///
/// @param <T> the type of object ot be consumed.
/// @author evanbergstrom
/// @since 1.0
public interface AssertConsumer<T> extends Consumer<T>, AssertAction {
}
