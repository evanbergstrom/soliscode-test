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
package com.soliscode.test.contract.spliterator;

import java.util.Spliterator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.SpliteratorContractSupport;

import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [estimateSize()][Spliterator#estimateSize()] method of a [Spliterator]: after a split, neither
/// the part nor what is left estimates more than the spliterator did before.
///
/// The exact size a `SIZED` or `SUBSIZED` spliterator must report is tested with those characteristics
/// ([ReportedCharacteristicsContract]). Where the size is not known, the estimate "may be arbitrarily inaccurate",
/// and is not tested.
///
/// @param <S> the type of the spliterator under test
/// @param <E> the element type
/// @since 0.1.3
/// @see Spliterator#estimateSize()
public interface EstimateSizeContract<S extends Spliterator<E>, E> extends SpliteratorContractSupport<S, E> {

    /// Tests that after a split that returns a part, neither the part's estimate nor the spliterator's is more than
    /// the spliterator's before the split.
    @Test
    @DisplayName("estimateSize() after trySplit() is no more than before, for both parts")
    default void estimateSizeAfterTrySplitIsNoMoreThanBefore() {
        Spliterator<E> spliterator = defaultSource().spliterator();
        long before = spliterator.estimateSize();
        Spliterator<E> part = spliterator.trySplit();
        if (part != null) {
            long left = spliterator.estimateSize();
            long split = part.estimateSize();
            assertTrue(left <= before, () -> "estimateSize() after trySplit() is " + left + ", more than the "
                    + before + " before it");
            assertTrue(split <= before, () -> "the part's estimateSize() is " + split + ", more than the " + before
                    + " before the split");
        }
    }
}
