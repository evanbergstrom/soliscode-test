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

package org.soliscode.test.contract.support;

import org.jetbrains.annotations.NotNull;
import org.soliscode.test.contract.object.ObjectContract;
import org.soliscode.test.provider.*;

/// Provides an implementation of the [#provider()] method that provides instances of [Integer].
///
/// @author evanbergstrom
/// @see ObjectContract
/// @see IntegerProvider
/// @since 1.0
public interface WithInteger extends IntegerSupport<Integer> {

    /// Returns an instance of `NumberProvider` that provides [Integer] objects.
    /// @return an instance of [IntegerProvider].
    default @NotNull IntegerNumberProvider<Integer> provider() {
        return new IntegerProvider();
    }
}
