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
package com.soliscode.test.contract.coverage;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DynamicNode;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.breakable.Break;
import com.soliscode.test.breakable.BreakableComparable;
import com.soliscode.test.contract.comparable.ComparableContract;
import com.soliscode.test.provider.ComparableProvider;
import com.soliscode.test.provider.ComparableProviders;

import static com.soliscode.test.breakable.BreakableComparable.COMPARABLE_EQUALS_IS_IDENTITY;
import static com.soliscode.test.breakable.BreakableComparable.COMPARE_TO_ALWAYS_RETURNS_ZERO;
import static com.soliscode.test.breakable.BreakableComparable.COMPARE_TO_BREAKS_TIES_BY_IDENTITY;
import static com.soliscode.test.breakable.BreakableComparable.COMPARE_TO_IS_CYCLIC;
import static com.soliscode.test.breakable.BreakableComparable.COMPARE_TO_IS_NOT_SIGN_SYMMETRIC;
import static com.soliscode.test.breakable.BreakableComparable.COMPARE_TO_IS_REVERSED;
import static com.soliscode.test.breakable.BreakableComparable.COMPARE_TO_NULL_DOES_NOT_THROW;
import static com.soliscode.test.breakable.BreakableComparable.COMPARE_TO_NULL_THROWS_ILLEGAL_ARGUMENT;
import static com.soliscode.test.breakable.BreakableComparable.COMPARE_TO_SELF_IS_NOT_ZERO;
import static com.soliscode.test.breakable.BreakableComparable.COMPARE_TO_TREATS_NEAR_VALUES_AS_EQUAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/// The subjects `ContractCoverageTest` runs `ComparableContract` against: `Integer`, `String`, `BigDecimal` and
/// `Boolean`, and `BreakableComparable` over the `Integer` samples, each value wrapped twice, so that the consistency
/// test has pairs that are equal but not identical (#40).
///
/// Beyond the coverage test's own checks, it shows by name that the test each break targets fails for it, so that
/// every test method is shown to fail, and that the tests of the laws a break claims to keep pass with it.
final class ComparableSubjects {

    private static final String ORDER = "compareToFollowsTheSamplesOrder";
    private static final String SIGN_SYMMETRY = "compareToIsSignSymmetric";
    private static final String TRANSITIVITY = "compareToIsTransitive";
    private static final String ZERO_ALIKE = "compareToZeroComparesAlikeWithOthers";
    private static final String NULL = "compareToNullThrowsNullPointerException";
    private static final String CONSISTENCY = "compareToIsConsistentWithEquals";

    /// The test each break targets.
    private static final Map<Break, String> TARGETS = Map.of(
            COMPARE_TO_IS_REVERSED, ORDER,
            COMPARE_TO_ALWAYS_RETURNS_ZERO, ORDER,
            COMPARE_TO_SELF_IS_NOT_ZERO, SIGN_SYMMETRY,
            COMPARE_TO_IS_NOT_SIGN_SYMMETRIC, SIGN_SYMMETRY,
            COMPARE_TO_IS_CYCLIC, TRANSITIVITY,
            COMPARE_TO_TREATS_NEAR_VALUES_AS_EQUAL, ZERO_ALIKE,
            COMPARE_TO_NULL_DOES_NOT_THROW, NULL,
            COMPARE_TO_NULL_THROWS_ILLEGAL_ARGUMENT, NULL,
            COMPARE_TO_BREAKS_TIES_BY_IDENTITY, CONSISTENCY,
            COMPARABLE_EQUALS_IS_IDENTITY, CONSISTENCY);

    /// The tests of the laws a break claims to keep.
    private static final Map<Break, List<String>> KEPT = Map.of(
            COMPARE_TO_IS_REVERSED, List.of(SIGN_SYMMETRY, TRANSITIVITY, ZERO_ALIKE, NULL),
            COMPARE_TO_ALWAYS_RETURNS_ZERO, List.of(SIGN_SYMMETRY, TRANSITIVITY, ZERO_ALIKE, NULL),
            COMPARE_TO_IS_CYCLIC, List.of(SIGN_SYMMETRY, ZERO_ALIKE, NULL),
            COMPARE_TO_TREATS_NEAR_VALUES_AS_EQUAL, List.of(SIGN_SYMMETRY, NULL),
            COMPARE_TO_BREAKS_TIES_BY_IDENTITY, List.of(SIGN_SYMMETRY, TRANSITIVITY, ZERO_ALIKE, NULL),
            COMPARABLE_EQUALS_IS_IDENTITY, List.of(ORDER, SIGN_SYMMETRY, TRANSITIVITY, ZERO_ALIKE, NULL));

    private ComparableSubjects() { }

    /// `ComparableContract` on a provider, claiming consistency with `equals` or not.
    static class Subject<T extends Comparable<? super T>> extends AbstractTest implements ComparableContract<T> {
        private final ComparableProvider<T> samples;
        private final boolean consistent;

        Subject(final ComparableProvider<T> samplesArg, final boolean consistentArg) {
            this.samples = samplesArg;
            this.consistent = consistentArg;
        }

        @Override
        public ComparableProvider<T> comparableProvider() {
            return samples;
        }

        @Override
        public boolean naturalOrderingIsConsistentWithEquals() {
            return consistent;
        }
    }

    /// `ComparableContract` on `Integer`.
    static Subject<Integer> integers() {
        return new Subject<>(ComparableProviders.integers(), true);
    }

    /// `ComparableContract` on `String`.
    static Subject<String> strings() {
        return new Subject<>(ComparableProviders.strings(), true);
    }

    /// `ComparableContract` on `BigDecimal`, which does not claim consistency with `equals`, or, wrongly, does.
    static Subject<BigDecimal> bigDecimals(final boolean claimsConsistency) {
        return new Subject<>(ComparableProviders.bigDecimals(), claimsConsistency);
    }

    /// `ComparableContract` on `Boolean`, which has two values: two tiers.
    static Subject<Boolean> booleans() {
        return new Subject<>(ComparableProviders.ascending(false, true), true);
    }

    /// `ComparableContract` on `BreakableComparable` with a break, or none, over the `Integer` samples, each value
    /// wrapped twice in its tier.
    static Subject<BreakableComparable<Integer>> broken(final @Nullable Break b) {
        Set<Break> breaks = b == null ? Set.of() : Set.of(b);
        ComparableProvider<BreakableComparable<Integer>> wrapped = () -> {
            List<List<BreakableComparable<Integer>>> tiers = new ArrayList<>();
            for (final List<Integer> tier : ComparableProviders.integers().ascendingSamples()) {
                List<BreakableComparable<Integer>> twice = new ArrayList<>();
                for (final Integer v : tier) {
                    twice.add(new BreakableComparable<>(v, breaks));
                    twice.add(new BreakableComparable<>(v, breaks));
                }
                tiers.add(twice);
            }
            return tiers;
        };
        return new Subject<>(wrapped, true);
    }

    /// The checks by name: for each break, that the test it targets fails, and that the tests of the laws it keeps
    /// pass. Every break of `BreakableComparable` must have a target, and every test method must be one.
    ///
    /// @param breaks the breakable's breaks, by key
    /// @return the checks
    static Stream<DynamicNode> namedChecks(final Map<String, Break> breaks) {
        List<Method> methods = ContractRuns.testMethods(ComparableContract.class, false);
        Stream<DynamicNode> complete = Stream.of(dynamicTest("every break has a target, and every test is one", () -> {
            assertEquals(Set.copyOf(breaks.values()), TARGETS.keySet(), "the breaks with a target");
            assertEquals(methods.stream().map(Method::getName).collect(Collectors.toSet()),
                    Set.copyOf(TARGETS.values()), "the tests some break targets");
        }));
        Stream<DynamicNode> targets = TARGETS.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(e -> dynamicTest(e.getValue() + " fails with " + e.getKey().description(), () ->
                        assertFalse(ContractRuns.failures(() -> broken(e.getKey()), named(methods, e.getValue()))
                                .isEmpty(), () -> e.getValue() + " passes with the break: it cannot see it")));
        Stream<DynamicNode> kept = KEPT.entrySet().stream().flatMap(e -> e.getValue().stream().map(test ->
                dynamicTest(test + " passes with " + e.getKey().description(), () -> {
                    List<String> failures = ContractRuns.failures(() -> broken(e.getKey()), named(methods, test));
                    assertTrue(failures.isEmpty(), () -> "the break claims to keep it, but:\n  "
                            + String.join("\n  ", failures));
                })));
        return Stream.of(complete, targets, kept).flatMap(s -> s);
    }

    private static List<Method> named(final List<Method> methods, final String name) {
        List<Method> named = methods.stream().filter(m -> m.getName().equals(name)).toList();
        assertEquals(1, named.size(), () -> "no test method " + name);
        return named;
    }
}
