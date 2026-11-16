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
