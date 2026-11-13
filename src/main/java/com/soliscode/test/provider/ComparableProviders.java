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
import java.util.List;
import java.util.Objects;

/// Factories of [ComparableProvider]s: providers of given tiers, and samples of `Integer`, `String` and `BigDecimal`.
///
/// @since 0.1.3
public final class ComparableProviders {

    private ComparableProviders() { }

    /// A provider of the given tiers, lowest first, copied.
    ///
    /// @param tiers the tiers: at least one, none empty, each holding samples that compare as equal, every sample
    ///              less than every sample of the next tier
    /// @param <T> the type of the samples
    /// @return the provider
    /// @throws NullPointerException if `tiers`, a tier or a sample is null
    /// @throws IllegalArgumentException if there are no tiers, or a tier is empty
    public static <T extends Comparable<? super T>> ComparableProvider<T> of(
            final List<? extends List<? extends T>> tiers) {
        List<List<T>> copy = checkedCopy(tiers);
        return () -> copy;
    }

    /// A provider of the given samples, lowest first, each in a tier of its own.
    ///
    /// @param samples the samples, each less than the next
    /// @param <T> the type of the samples
    /// @return the provider
    /// @throws NullPointerException if `samples` or a sample is null
    /// @throws IllegalArgumentException if there are no samples
    @SafeVarargs
    public static <T extends Comparable<? super T>> ComparableProvider<T> ascending(final T... samples) {
        Objects.requireNonNull(samples, "samples");
        for (int i = 0; i < samples.length; i++) {
            Objects.requireNonNull(samples[i], "sample " + i);
        }
        return of(Arrays.stream(samples).map(List::of).toList());
    }

    /// Samples of `Integer`: `MIN_VALUE`, -1, 0, 1, two `Integer.valueOf(100_000)` in one tier (distinct objects with
    /// the default `Integer` cache), and `MAX_VALUE`. The extremes catch a `compareTo` that subtracts, which
    /// overflows.
    ///
    /// @return the provider
    public static ComparableProvider<Integer> integers() {
        return of(List.of(List.of(Integer.MIN_VALUE), List.of(-1), List.of(0), List.of(1),
                List.of(Integer.valueOf(100_000), Integer.valueOf(100_000)), List.of(Integer.MAX_VALUE)));
    }

    /// Samples of `String`, in its UTF-16 order: `""`, `"A"`, `"a"`, a tier of `"ab"` and an equal copy made at run
    /// time, `"b"`, U+1F600 (a surrogate pair, whose first unit is 0xD83D) and U+FFFF. The last two are in order of
    /// their UTF-16 units, which is the opposite of their code points' order.
    ///
    /// @return the provider
    public static ComparableProvider<String> strings() {
        String copy = String.valueOf(new char[] {'a', 'b'});
        return of(List.of(List.of(""), List.of("A"), List.of("a"), List.of("ab", copy), List.of("b"),
                List.of(Character.toString(0x1F600)), List.of(String.valueOf(Character.MAX_VALUE))));
    }

    /// Samples of `BigDecimal`: -1, a tier of `0`, `0.0` and `0.00`, `0.5`, a tier of `1`, `1.0` and `1.00`, and a
    /// tier of `10` and `1E+1`. Samples of one tier compare as equal but are not `equals`: `BigDecimal`'s natural
    /// ordering is not consistent with `equals`, so a test class of it overrides
    /// [com.soliscode.test.contract.support.ComparableContractSupport#naturalOrderingIsConsistentWithEquals()].
    ///
    /// @return the provider
    public static ComparableProvider<BigDecimal> bigDecimals() {
        return of(List.of(List.of(new BigDecimal("-1")),
                List.of(new BigDecimal("0"), new BigDecimal("0.0"), new BigDecimal("0.00")),
                List.of(new BigDecimal("0.5")),
                List.of(new BigDecimal("1"), new BigDecimal("1.0"), new BigDecimal("1.00")),
                List.of(new BigDecimal("10"), new BigDecimal("1E+1"))));
    }

    /// Checks the tiers and copies them.
    ///
    /// @param tiers the tiers
    /// @param <T> the type of the samples
    /// @return an unmodifiable copy
    private static <T> List<List<T>> checkedCopy(final List<? extends List<? extends T>> tiers) {
        Objects.requireNonNull(tiers, "tiers");
        if (tiers.isEmpty()) {
            throw new IllegalArgumentException("no tiers: a comparable provider needs at least one sample");
        }
        List<List<T>> copy = new ArrayList<>(tiers.size());
        for (int i = 0; i < tiers.size(); i++) {
            List<? extends T> tier = Objects.requireNonNull(tiers.get(i), "tier " + i);
            if (tier.isEmpty()) {
                throw new IllegalArgumentException("tier " + i + " is empty");
            }
            for (int j = 0; j < tier.size(); j++) {
                Objects.requireNonNull(tier.get(j), "sample " + j + " of tier " + i);
            }
            copy.add(List.copyOf(tier));
        }
        return List.copyOf(copy);
    }
}
