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
package com.soliscode.test.contract.listiterator;

import com.soliscode.test.InterfaceMethod;

/// The optional methods of [java.util.ListIterator] that are its own, for `supportsMethod(InterfaceMethod)`.
/// `remove()`'s is [com.soliscode.test.contract.iterable.IterableMethods#ITERATOR_REMOVE].
///
/// @since 0.1.3
public enum ListIteratorMethods implements InterfaceMethod {

    /// The optional method [java.util.ListIterator#set(Object)].
    LIST_ITERATOR_SET("set(E)"),

    /// The optional method [java.util.ListIterator#add(Object)].
    LIST_ITERATOR_ADD("add(E)");

    private final String name;

    ListIteratorMethods(final String nameArg) {
        this.name = nameArg;
    }

    /// {@inheritDoc}
    @Override
    public String methodName() {
        return name;
    }
}
