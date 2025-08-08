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

package org.soliscode.test.provider;

import org.jetbrains.annotations.NotNull;

/// Provides instances of an [Long] for the purposes of testing.
/// @author evanbergstrom
/// @since 1.0
/// @see Long
public class LongProvider implements IntegerNumberProvider<Long> {

    /// Creates an instance of the [Long] class with a numeric value equal to the primitive long value.
    /// @param value the primitive long value.
    /// @return an instance of the `Long` class.
    @Override
    public @NotNull Long createValue(final int value) {
        return (long)value;
    }
}
