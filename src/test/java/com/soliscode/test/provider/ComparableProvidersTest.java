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
package com.soliscode.test.provider;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `ComparableProviders`: that each factory's tiers are in the order the JDK's own `compareTo` gives, and
/// that `of` and `ascending` refuse what they should.
///
/// @see ComparableProviders
@DisplayName("Tests for ComparableProviders")
public class ComparableProvidersTest {

    /// Default constructor.
    public ComparableProvidersTest() {
        super();
    }

    /// Checks that the samples of each tier compare as equal, and each is less than every sample of later tiers.
    private static <T extends Comparable<? super T>> void assertAscending(final List<List<T>> tiers) {
        for (int i = 0; i < tiers.size(); i++) {
            for (final T x : tiers.get(i)) {
                for (int j = 0; j < tiers.size(); j++) {
                    for (final T y : tiers.get(j)) {
                        assertEquals(Integer.compare(i, j), Integer.signum(x.compareTo(y)), x + " against " + y);
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("integers() ascends, with the extremes and two equal objects in a tier")
    void integersAscend() {
        List<List<Integer>> tiers = ComparableProviders.integers().ascendingSamples();
        assertAscending(tiers);
        assertEquals(Integer.MIN_VALUE, tiers.getFirst().getFirst());
        assertEquals(Integer.MAX_VALUE, tiers.getLast().getFirst());
        assertTrue(tiers.stream().anyMatch(t -> t.size() == 2));
    }

    @Test
    @DisplayName("integers() makes a compareTo that subtracts misorder some pair")
    void integersCatchSubtraction() {
        Comparator<Integer> subtracting = (x, y) -> x - y;
        List<List<Integer>> tiers = ComparableProviders.integers().ascendingSamples();
        boolean misordered = false;
        for (int i = 0; i < tiers.size(); i++) {
            for (int j = 0; j < tiers.size(); j++) {
                for (final Integer x : tiers.get(i)) {
                    for (final Integer y : tiers.get(j)) {
                        misordered |= Integer.signum(subtracting.compare(x, y)) != Integer.compare(i, j);
                    }
                }
            }
        }
        assertTrue(misordered, "a subtracting comparator orders every pair of the samples as the tiers do");
    }

    @Test
    @DisplayName("strings() ascends in UTF-16 order, with an equal copy in a tier")
    void stringsAscend() {
        List<List<String>> tiers = ComparableProviders.strings().ascendingSamples();
        assertAscending(tiers);
        List<String> pair = tiers.stream().filter(t -> t.size() == 2).findFirst().orElseThrow();
        assertNotSame(pair.get(0), pair.get(1));
        String emoji = tiers.get(tiers.size() - 2).getFirst();
        String last = tiers.getLast().getFirst();
        assertTrue(emoji.codePointAt(0) > last.codePointAt(0), "the code points are in the opposite order");
    }

    @Test
    @DisplayName("bigDecimals() ascends, with tiers that compare as equal but are not equals")
    void bigDecimalsAscend() {
        List<List<BigDecimal>> tiers = ComparableProviders.bigDecimals().ascendingSamples();
        assertAscending(tiers);
        assertTrue(tiers.stream().anyMatch(t -> t.size() > 1 && !t.get(0).equals(t.get(1))));
    }

    @Test
    @DisplayName("of() copies its tiers")
    void ofCopies() {
        List<Integer> tier = new ArrayList<>(List.of(1));
        List<List<Integer>> tiers = new ArrayList<>(List.of(List.of(0), tier));
        ComparableProvider<Integer> provider = ComparableProviders.of(tiers);
        tier.add(2);
        tiers.clear();
        assertEquals(List.of(List.of(0), List.of(1)), provider.ascendingSamples());
    }

    @Test
    @DisplayName("ascending() makes a tier of each sample, and accepts one sample")
    void ascendingMakesSingletonTiers() {
        assertEquals(List.of(List.of(false), List.of(true)),
                ComparableProviders.ascending(false, true).ascendingSamples());
        assertEquals(List.of(List.of(1)), ComparableProviders.ascending(1).ascendingSamples());
    }

    @Test
    @DisplayName("of() refuses no tiers, a null list, a null tier, an empty tier and a null sample")
    void ofRefusesBadTiers() {
        assertThrows(IllegalArgumentException.class, () -> ComparableProviders.of(List.of()));
        assertThrows(NullPointerException.class, () -> ComparableProviders.of(null));
        assertThrows(NullPointerException.class,
                () -> ComparableProviders.of(Arrays.asList(List.of(1), null)));
        assertThrows(IllegalArgumentException.class, () -> ComparableProviders.of(List.of(List.of(1), List.of())));
        assertThrows(NullPointerException.class,
                () -> ComparableProviders.of(List.of(Arrays.asList(1, null))));
    }

    @Test
    @DisplayName("ascending() refuses no samples, a null array and a null sample")
    void ascendingRefusesBadSamples() {
        assertThrows(IllegalArgumentException.class, ComparableProviders::<Integer>ascending);
        assertThrows(NullPointerException.class, () -> ComparableProviders.ascending((Integer[]) null));
        assertThrows(NullPointerException.class, () -> ComparableProviders.ascending(1, null));
        assertFalse(ComparableProviders.ascending(1, 2).ascendingSamples().isEmpty());
    }
}
