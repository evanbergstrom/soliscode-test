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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `ComparatorProviders`: that each factory's tiers are in the order its JDK comparator gives, and that
/// `of` and `ascending` refuse what they should.
///
/// @see ComparatorProviders
@DisplayName("Tests for ComparatorProviders")
public class ComparatorProvidersTest {

    /// Default constructor.
    public ComparatorProvidersTest() {
        super();
    }

    /// Checks that the provider's comparator calls the samples of each tier equal, and each less than every sample of
    /// later tiers.
    private static <T> void assertAscending(final ComparatorProvider<T> provider) {
        Comparator<T> c = provider.comparator();
        List<List<T>> tiers = provider.ascendingSamples();
        for (int i = 0; i < tiers.size(); i++) {
            for (final T x : tiers.get(i)) {
                for (int j = 0; j < tiers.size(); j++) {
                    for (final T y : tiers.get(j)) {
                        assertEquals(Integer.compare(i, j), Integer.signum(c.compare(x, y)), x + " against " + y);
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("naturalOrder() is Comparator.naturalOrder() over the comparable provider's tiers")
    void naturalOrderAscends() {
        ComparatorProvider<Integer> provider = ComparatorProviders.naturalOrder(ComparableProviders.integers());
        assertSame(Comparator.<Integer>naturalOrder(), provider.comparator());
        assertEquals(ComparableProviders.integers().ascendingSamples(), provider.ascendingSamples());
        assertAscending(provider);
    }

    @Test
    @DisplayName("reverseOrder() is Comparator.reverseOrder() over the comparable provider's tiers, reversed")
    void reverseOrderAscends() {
        ComparatorProvider<Integer> provider = ComparatorProviders.reverseOrder(ComparableProviders.integers());
        assertSame(Comparator.<Integer>reverseOrder(), provider.comparator());
        assertEquals(Integer.MAX_VALUE, provider.ascendingSamples().getFirst().getFirst());
        assertAscending(provider);
    }

    @Test
    @DisplayName("caseInsensitiveOrder() ascends in CASE_INSENSITIVE_ORDER, with tiers that are not equals")
    void caseInsensitiveOrderAscends() {
        ComparatorProvider<String> provider = ComparatorProviders.caseInsensitiveOrder();
        assertSame(String.CASE_INSENSITIVE_ORDER, provider.comparator());
        assertAscending(provider);
        assertTrue(provider.ascendingSamples().stream().anyMatch(t -> t.size() > 1 && !t.get(0).equals(t.get(1))));
    }

    @Test
    @DisplayName("caseInsensitiveOrder() makes a comparison by upper case alone misorder some pair")
    void caseInsensitiveOrderCatchesUpperCaseAlone() {
        Comparator<String> upper = Comparator.comparing(s -> s.toUpperCase(Locale.ROOT));
        List<List<String>> tiers = ComparatorProviders.caseInsensitiveOrder().ascendingSamples();
        boolean misordered = false;
        for (int i = 0; i < tiers.size(); i++) {
            for (int j = 0; j < tiers.size(); j++) {
                for (final String x : tiers.get(i)) {
                    for (final String y : tiers.get(j)) {
                        misordered |= Integer.signum(upper.compare(x, y)) != Integer.compare(i, j);
                    }
                }
            }
        }
        assertTrue(misordered, "a comparison by upper case orders every pair of the samples as the tiers do");
    }

    @Test
    @DisplayName("of() keeps its comparator and copies its tiers")
    void ofCopies() {
        List<Integer> tier = new ArrayList<>(List.of(1));
        List<List<Integer>> tiers = new ArrayList<>(List.of(List.of(0), tier));
        Comparator<Integer> c = Comparator.naturalOrder();
        ComparatorProvider<Integer> provider = ComparatorProviders.of(c, tiers);
        tier.add(2);
        tiers.clear();
        assertEquals(List.of(List.of(0), List.of(1)), provider.ascendingSamples());
        assertSame(c, provider.comparator());
    }

    @Test
    @DisplayName("ascending() makes a tier of each sample")
    void ascendingMakesSingletonTiers() {
        assertEquals(List.of(List.of(1), List.of(2)),
                ComparatorProviders.ascending(Comparator.<Integer>naturalOrder(), 1, 2).ascendingSamples());
    }

    @Test
    @DisplayName("of() refuses a null comparator, no tiers, a null tier, an empty tier and a null sample")
    void ofRefusesBadArguments() {
        Comparator<Integer> c = Comparator.naturalOrder();
        assertThrows(NullPointerException.class, () -> ComparatorProviders.of(null, List.of(List.of(1))));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> ComparatorProviders.of(c, List.of()));
        assertTrue(e.getMessage().contains("comparator provider"), e.getMessage());
        assertThrows(NullPointerException.class, () -> ComparatorProviders.of(c, Arrays.asList(List.of(1), null)));
        assertThrows(IllegalArgumentException.class, () -> ComparatorProviders.of(c, List.of(List.of(1), List.of())));
        assertThrows(NullPointerException.class, () -> ComparatorProviders.of(c, List.of(Arrays.asList(1, null))));
    }

    @Test
    @DisplayName("ascending() refuses no samples, a null array and a null sample")
    void ascendingRefusesBadSamples() {
        Comparator<Integer> c = Comparator.naturalOrder();
        assertThrows(IllegalArgumentException.class, () -> ComparatorProviders.ascending(c));
        assertThrows(NullPointerException.class, () -> ComparatorProviders.ascending(c, (Integer[]) null));
        assertThrows(NullPointerException.class, () -> ComparatorProviders.ascending(c, 1, null));
    }
}
