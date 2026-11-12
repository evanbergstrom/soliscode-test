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
package com.soliscode.test.contract.mapentry;

import com.soliscode.test.InterfaceMethod;

/// The optional methods of [java.util.Map.Entry], for `supportsMethod(InterfaceMethod)`.
///
/// @since 0.1.3
public enum MapEntryMethods implements InterfaceMethod {

    /// The optional method [java.util.Map.Entry#setValue(Object)].
    MAP_ENTRY_SET_VALUE("setValue(V)");

    private final String name;

    MapEntryMethods(final String nameArg) {
        this.name = nameArg;
    }

    /// {@inheritDoc}
    @Override
    public String methodName() {
        return name;
    }
}
