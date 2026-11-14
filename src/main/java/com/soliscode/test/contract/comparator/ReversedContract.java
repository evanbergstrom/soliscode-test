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
package com.soliscode.test.contract.comparator;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.ComparatorContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Contract for the [reversed()][Comparator#reversed()] method of a [Comparator].
///
/// It follows the Javadoc: `reversed()` "imposes the reverse ordering of this comparator". The reverse is checked
/// against the results the comparator under test is observed to give, so that a fault of `compare` is reported by the
/// [CompareContract] tests, not here.
///
/// @param <T> the type the comparator compares
/// @since 0.1.4
/// @see Comparator#reversed()
public interface ReversedContract<T> extends ComparatorContractSupport<T> {

    /// Tests that `signum(reversed().compare(x, y)) == signum(compare(y, x))`, for every pair of law samples.
    @Test
    @DisplayName("reversed() imposes the reverse order")
    default void reversedImposesTheReverseOrder() {
        Comparator<T> c = comparator();
        Comparator<T> reversed = c.reversed();
        List<LawSample<T>> samples = lawSamples();
        for (final LawSample<T> x : samples) {
            for (final LawSample<T> y : samples) {
                int expected = Integer.signum(c.compare(y.value(), x.value()));
                int actual = Integer.signum(reversed.compare(x.value(), y.value()));
                assertEquals(expected, actual, () -> "signum(reversed().compare(" + x + ", " + y
                        + ")) is not signum(compare(" + y + ", " + x + "))");
            }
        }
    }
}
