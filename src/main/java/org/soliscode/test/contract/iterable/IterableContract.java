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

package org.soliscode.test.contract.iterable;

import org.soliscode.test.contract.object.ObjectContract;

/// This interface tests if a iterable class has implemented the `Iterable` methods correctly based upon the
/// specification provided in [Iterable].
///
/// @param <E> The element type being tested.
/// @param <I> The iterator type being tested.
///
/// @author evanbergstrom
/// @see IteratorContract
/// @see ForEachContract
/// @see SpliteratorContract
/// @since 1.0
public interface IterableContract<E, I extends Iterable<E>>
    extends ObjectContract<I>,
        IteratorContract<E, I>,
        ForEachContract<E, I>,
        SpliteratorContract<E, I> {
}
