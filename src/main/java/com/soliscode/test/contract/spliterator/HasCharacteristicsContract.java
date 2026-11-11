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

import java.util.List;
import java.util.Spliterator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.SpliteratorContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Contract for the [hasCharacteristics(int)][Spliterator#hasCharacteristics(int)] method of a [Spliterator]: it
/// returns `true` where `characteristics()` contains every given characteristic.
///
/// @param <S> the type of the spliterator under test
/// @param <E> the element type
/// @since 0.1.3
/// @see Spliterator#hasCharacteristics(int)
public interface HasCharacteristicsContract<S extends Spliterator<E>, E> extends SpliteratorContractSupport<S, E> {

    /// Every characteristic `Spliterator` defines, as an unmodifiable list.
    List<Integer> ALL_CHARACTERISTICS = List.of(Spliterator.ORDERED, Spliterator.DISTINCT, Spliterator.SORTED,
            Spliterator.SIZED, Spliterator.NONNULL, Spliterator.IMMUTABLE, Spliterator.CONCURRENT,
            Spliterator.SUBSIZED);

    /// Tests that `hasCharacteristics(c)` is `(characteristics() & c) == c`, for each characteristic, for none, and
    /// for all of them together.
    @Test
    @DisplayName("hasCharacteristics(c) is true exactly where characteristics() contains c")
    default void hasCharacteristicsAgreesWithCharacteristics() {
        Spliterator<E> spliterator = defaultSource().spliterator();
        int all = 0;
        for (final int c : ALL_CHARACTERISTICS) {
            assertHasCharacteristics(spliterator, c);
            all |= c;
        }
        assertHasCharacteristics(spliterator, 0);
        assertHasCharacteristics(spliterator, all);
    }

    private static void assertHasCharacteristics(final Spliterator<?> spliterator, final int mask) {
        int reported = spliterator.characteristics();
        assertEquals((reported & mask) == mask, spliterator.hasCharacteristics(mask),
                () -> "hasCharacteristics(0x" + Integer.toHexString(mask) + "), where characteristics() is 0x"
                        + Integer.toHexString(reported));
    }
}
