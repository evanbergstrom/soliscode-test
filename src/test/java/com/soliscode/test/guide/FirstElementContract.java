/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.guide;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.CollectionProviderSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// How to check that a contract can fail: a contract of the reader's own, one test method a list's test class
/// gets by implementing it.
///
/// @param <L> the type of the list
/// @param <E> the type of its elements
interface FirstElementContract<L extends List<E>, E> extends CollectionProviderSupport<L, E> {

    /// The first element of a list made from three unique elements is the first of them.
    @Test
    @DisplayName("get(0) returns the first element")
    default void getZeroReturnsTheFirstElement() {
        List<E> elements = provider().elementProvider().createUniqueInstances(3);
        L list = provider().createInstance(elements);
        assertEquals(elements.getFirst(), list.get(0));
    }
}
