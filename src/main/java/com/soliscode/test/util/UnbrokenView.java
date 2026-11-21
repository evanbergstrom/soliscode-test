/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.util;

/// An iterable that can give its elements without its breaks: a breakable, which the helpers of [IterableTestUtils]
/// read through [#unbroken()], so that counting or comparing its elements does not trip a break it was made with.
///
/// The breakables implement it (`BreakableIterable`, and so `BreakableCollection`, whose `unbroken()` is the
/// collection it wraps). Added in #67, so that `util` depends on no breakable.
///
/// @param <E> the element type
/// @since 0.1.5
public interface UnbrokenView<E> extends Iterable<E> {

    /// Returns the elements without the breaks: the iterable or collection this one wraps.
    ///
    /// @return the unbroken elements
    Iterable<E> unbroken();
}
