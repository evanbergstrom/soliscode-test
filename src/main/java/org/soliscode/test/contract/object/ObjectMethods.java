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

package org.soliscode.test.contract.object;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.InterfaceMethod;

/// Values used to identify object class methods for use with the [ObjectContract#supportsMethod(InterfaceMethod)]
/// method.
///
/// @author evanbergstrom
/// @since 1.0
public enum ObjectMethods implements InterfaceMethod {

    /// The option al method [Object#equals(Object)].
    EQUALS("equals(Object)"),

    /// The option al method [Object#hashCode()].
    HASH_CODE("hashCode()"),

    /// The option al method [Object#toString()].
    TO_STRING("toString()"),

    /// The option al method [java.io.Serializable].
    SERIALIZATION("serialization");

    private final @NonNull String name;

    ObjectMethods(final @NonNull String name) {
        this.name = name;
    }

    /// {@inheritDoc}
    @Override
    public @NonNull String methodName() {
        return name;
    }
}
