/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.support;

import java.util.Spliterator;

import com.soliscode.test.provider.SpliteratorProvider;

/// Interface for contract classes that test a spliterator, made by a [SpliteratorProvider].
///
/// @param <S> the type of the spliterator under test
/// @param <E> the element type
/// @since 0.1.3
public interface SpliteratorProviderSupport<S extends Spliterator<E>, E> {

    /// Returns the provider of the sources, and the spliterators under test over them.
    ///
    /// @return the spliterator provider
    SpliteratorProvider<S, E> spliteratorProvider();
}
