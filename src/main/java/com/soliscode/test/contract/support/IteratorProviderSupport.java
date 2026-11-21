/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.support;

import java.util.Iterator;

import com.soliscode.test.provider.IteratorProvider;

/// Interface for contract classes that test an iterator, made by an [IteratorProvider].
///
/// @param <I> the type of the iterator under test
/// @param <E> the element type
/// @since 0.1.3
public interface IteratorProviderSupport<I extends Iterator<E>, E> {

    /// Returns the provider of the sources, and the iterators under test over them.
    ///
    /// @return the iterator provider
    IteratorProvider<I, E> iteratorProvider();
}
