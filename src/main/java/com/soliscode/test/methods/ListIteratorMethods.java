/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.methods;

import com.soliscode.test.InterfaceMethod;

/// The optional methods of [java.util.ListIterator] that are its own, for `supportsMethod(InterfaceMethod)`.
/// `remove()`'s is [com.soliscode.test.methods.IterableMethods#ITERATOR_REMOVE].
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
