/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.coverage;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DynamicNode;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.breakable.Break;
import com.soliscode.test.breakable.BreakableComparator;
import com.soliscode.test.contract.comparator.ComparatorContract;
import com.soliscode.test.provider.ComparableProviders;
import com.soliscode.test.provider.ComparatorProvider;
import com.soliscode.test.provider.ComparatorProviders;

import static com.soliscode.test.breakable.BreakableComparator.COMPARE_ALWAYS_RETURNS_ZERO;
import static com.soliscode.test.breakable.BreakableComparator.COMPARE_BREAKS_TIES_BY_IDENTITY;
import static com.soliscode.test.breakable.BreakableComparator.COMPARE_IS_CYCLIC;
import static com.soliscode.test.breakable.BreakableComparator.COMPARE_IS_NOT_SIGN_SYMMETRIC;
import static com.soliscode.test.breakable.BreakableComparator.COMPARE_IS_REVERSED;
import static com.soliscode.test.breakable.BreakableComparator.COMPARE_NULL_DOES_NOT_THROW;
import static com.soliscode.test.breakable.BreakableComparator.COMPARE_NULL_THROWS_ILLEGAL_ARGUMENT;
import static com.soliscode.test.breakable.BreakableComparator.COMPARE_OF_AN_OBJECT_WITH_ITSELF_IS_NOT_ZERO;
import static com.soliscode.test.breakable.BreakableComparator.COMPARE_TREATS_NEAR_VALUES_AS_EQUAL;
import static com.soliscode.test.breakable.BreakableComparator.REVERSED_KEEPS_THE_ORDER;
import static com.soliscode.test.breakable.BreakableComparator.THEN_COMPARING_ACCEPTS_NULL;
import static com.soliscode.test.breakable.BreakableComparator.THEN_COMPARING_CONSULTS_THE_OTHER_FIRST;
import static com.soliscode.test.breakable.BreakableComparator.THEN_COMPARING_DOUBLE_IGNORES_THE_KEY;
import static com.soliscode.test.breakable.BreakableComparator.THEN_COMPARING_IGNORES_THE_KEY;
import static com.soliscode.test.breakable.BreakableComparator.THEN_COMPARING_IGNORES_THE_OTHER;
import static com.soliscode.test.breakable.BreakableComparator.THEN_COMPARING_INT_IGNORES_THE_KEY;
import static com.soliscode.test.breakable.BreakableComparator.THEN_COMPARING_LONG_IGNORES_THE_KEY;
import static com.soliscode.test.breakable.BreakableComparator.THEN_COMPARING_REVERSES_THE_KEY_COMPARATOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/// The subjects `ContractCoverageTest` runs `ComparatorContract` against: `Comparator.naturalOrder()`,
/// `Comparator.reverseOrder()`, `String.CASE_INSENSITIVE_ORDER` and `Comparator.nullsFirst(naturalOrder())`, and
/// `BreakableComparator` over `naturalOrder()` and the `Integer` samples, whose tier of two `100_000`s gives equal
/// pairs that are not identical (#42).
///
/// Beyond the coverage test's own checks, it shows by name, for each break, that each test it targets fails, and that
/// every test it neither targets nor is known to fail as well passes; and that the targets cover every test method.
final class ComparatorSubjects {

    private static final String ORDER = "compareFollowsTheSamplesOrder";
    private static final String SIGN_SYMMETRY = "compareIsSignSymmetric";
    private static final String TRANSITIVITY = "compareIsTransitive";
    private static final String ZERO_ALIKE = "compareZeroComparesAlikeWithOthers";
    private static final String NULL = "compareNullThrowsNullPointerException";
    private static final String CONSISTENCY = "compareIsConsistentWithEquals";
    private static final String REVERSED = "reversedImposesTheReverseOrder";
    private static final String THEN = "thenComparingBreaksTiesWithTheOtherComparator";
    private static final String KEY = "thenComparingByKeyBreaksTiesWithTheKey";
    private static final String KEY_COMPARATOR = "thenComparingByKeyAndComparatorBreaksTiesWithTheKeyComparator";
    private static final String INT_KEY = "thenComparingIntBreaksTiesWithTheKey";
    private static final String LONG_KEY = "thenComparingLongBreaksTiesWithTheKey";
    private static final String DOUBLE_KEY = "thenComparingDoubleBreaksTiesWithTheKey";
    private static final String THEN_NULL = "thenComparingNullArgumentThrowsNullPointerException";
    private static final String NULLS_FIRST = "nullsFirstPutsNullFirst";
    private static final String NULLS_LAST = "nullsLastPutsNullLast";

    /// The tests each break targets: each must fail with it.
    private static final Map<Break, List<String>> TARGETS = Map.ofEntries(
            Map.entry(COMPARE_IS_REVERSED, List.of(ORDER)),
            Map.entry(COMPARE_ALWAYS_RETURNS_ZERO, List.of(ORDER, CONSISTENCY)),
            Map.entry(COMPARE_OF_AN_OBJECT_WITH_ITSELF_IS_NOT_ZERO, List.of(SIGN_SYMMETRY)),
            Map.entry(COMPARE_IS_NOT_SIGN_SYMMETRIC, List.of(SIGN_SYMMETRY)),
            Map.entry(COMPARE_IS_CYCLIC, List.of(TRANSITIVITY)),
            Map.entry(COMPARE_TREATS_NEAR_VALUES_AS_EQUAL, List.of(ZERO_ALIKE)),
            Map.entry(COMPARE_NULL_DOES_NOT_THROW, List.of(NULL)),
            Map.entry(COMPARE_NULL_THROWS_ILLEGAL_ARGUMENT, List.of(NULL)),
            Map.entry(COMPARE_BREAKS_TIES_BY_IDENTITY, List.of(CONSISTENCY)),
            Map.entry(REVERSED_KEEPS_THE_ORDER, List.of(REVERSED, NULLS_FIRST, NULLS_LAST)),
            Map.entry(THEN_COMPARING_IGNORES_THE_OTHER, List.of(THEN, NULLS_FIRST, NULLS_LAST)),
            Map.entry(THEN_COMPARING_CONSULTS_THE_OTHER_FIRST, List.of(THEN)),
            Map.entry(THEN_COMPARING_ACCEPTS_NULL, List.of(THEN_NULL)),
            Map.entry(THEN_COMPARING_IGNORES_THE_KEY, List.of(KEY)),
            Map.entry(THEN_COMPARING_REVERSES_THE_KEY_COMPARATOR, List.of(KEY_COMPARATOR)),
            Map.entry(THEN_COMPARING_INT_IGNORES_THE_KEY, List.of(INT_KEY)),
            Map.entry(THEN_COMPARING_LONG_IGNORES_THE_KEY, List.of(LONG_KEY)),
            Map.entry(THEN_COMPARING_DOUBLE_IGNORES_THE_KEY, List.of(DOUBLE_KEY)));

    /// The tests a break also fails, beyond its targets, which are not checked. Every other test must pass with it.
    private static final Map<Break, List<String>> ALSO_FAILS = Map.of(
            COMPARE_OF_AN_OBJECT_WITH_ITSELF_IS_NOT_ZERO, List.of(ORDER, ZERO_ALIKE, CONSISTENCY),
            COMPARE_IS_NOT_SIGN_SYMMETRIC, List.of(ORDER, TRANSITIVITY),
            COMPARE_TREATS_NEAR_VALUES_AS_EQUAL, List.of(ORDER, CONSISTENCY),
            COMPARE_IS_CYCLIC, List.of(ORDER),
            COMPARE_BREAKS_TIES_BY_IDENTITY, List.of(ORDER),
            THEN_COMPARING_CONSULTS_THE_OTHER_FIRST, List.of(NULLS_FIRST, NULLS_LAST));

    private ComparatorSubjects() { }

    /// `ComparatorContract` on a provider, claiming consistency with `equals` or not, and permitting nulls or not.
    static class Subject<T> extends AbstractTest implements ComparatorContract<T> {
        private final ComparatorProvider<T> provider;
        private final boolean consistent;
        private final boolean permitsNulls;

        Subject(final ComparatorProvider<T> providerArg, final boolean consistentArg, final boolean permitsNullsArg) {
            this.provider = providerArg;
            this.consistent = consistentArg;
            this.permitsNulls = permitsNullsArg;
        }

        @Override
        public ComparatorProvider<T> comparatorProvider() {
            return provider;
        }

        @Override
        public boolean comparatorIsConsistentWithEquals() {
            return consistent;
        }

        @Override
        public boolean comparatorPermitsNulls() {
            return permitsNulls;
        }
    }

    /// `ComparatorContract` on `Comparator.naturalOrder()` over the `Integer` samples, claiming to permit nulls or not.
    static Subject<Integer> naturalOrder(final boolean permitsNulls) {
        return new Subject<>(ComparatorProviders.naturalOrder(ComparableProviders.integers()), true, permitsNulls);
    }

    /// `ComparatorContract` on `BreakableComparator` over `naturalOrder()` and the `Integer` samples, with a break or
    /// none. Each test takes a new comparator, so that the ties break numbers the samples afresh.
    static Subject<Integer> broken(final @Nullable Break b, final boolean permitsNulls) {
        Set<Break> breaks = b == null ? Set.of() : Set.of(b);
        List<List<Integer>> tiers = ComparableProviders.integers().ascendingSamples();
        ComparatorProvider<Integer> provider = new ComparatorProvider<>() {
            @Override
            public Comparator<Integer> comparator() {
                return new BreakableComparator<>(Comparator.<Integer>naturalOrder(), breaks);
            }

            @Override
            public List<List<Integer>> ascendingSamples() {
                return tiers;
            }
        };
        return new Subject<>(provider, true, permitsNulls);
    }

    /// `ComparatorContract` on `BreakableComparator` with a break, or none, refusing nulls.
    static Subject<Integer> broken(final @Nullable Break b) {
        return broken(b, false);
    }

    /// The JDK subjects beyond `naturalOrder()`, and the claims that make them fail; and the breakable that permits
    /// nulls, with the claim that it does.
    ///
    /// @return the checks
    static Stream<DynamicNode> moreSubjects() {
        List<Method> methods = ContractRuns.testMethods(ComparatorContract.class, false);
        Supplier<Subject<Integer>> nullsFirst = () -> new Subject<>(ComparatorProviders.of(
                Comparator.nullsFirst(Comparator.<Integer>naturalOrder()),
                ComparableProviders.integers().ascendingSamples()), true, true);
        return Stream.of(
                passes("reverseOrder()", () -> new Subject<>(
                        ComparatorProviders.reverseOrder(ComparableProviders.integers()), true, false), methods),
                passes("CASE_INSENSITIVE_ORDER, not consistent with equals",
                        () -> new Subject<>(ComparatorProviders.caseInsensitiveOrder(), false, false), methods),
                passes("nullsFirst(naturalOrder()), permitting nulls", nullsFirst, methods),
                passes("the breakable with COMPARE_NULL_DOES_NOT_THROW, permitting nulls",
                        () -> broken(COMPARE_NULL_DOES_NOT_THROW, true), methods),
                fails("CASE_INSENSITIVE_ORDER claimed consistent with equals",
                        () -> new Subject<>(ComparatorProviders.caseInsensitiveOrder(), true, false), methods),
                fails("naturalOrder() claimed to permit nulls", () -> naturalOrder(true), methods),
                fails("nullsFirst(naturalOrder()) claimed to refuse nulls", () -> new Subject<>(
                        nullsFirst.get().comparatorProvider(), true, false), methods));
    }

    private static DynamicNode passes(final String name, final Supplier<?> contract, final List<Method> methods) {
        return dynamicTest("passes against " + name, () -> {
            List<String> failures = ContractRuns.failures(contract, methods);
            assertTrue(failures.isEmpty(), () -> "failures:\n  " + String.join("\n  ", failures));
        });
    }

    private static DynamicNode fails(final String name, final Supplier<?> contract, final List<Method> methods) {
        return dynamicTest("fails against " + name, () ->
                assertFalse(ContractRuns.failures(contract, methods).isEmpty(),
                        () -> "no test method fails against " + name));
    }

    /// The checks by name: for each break, that each test it targets fails, and that every test it neither targets
    /// nor also fails passes. Every break of `BreakableComparator` must have targets, and every test method must be
    /// one.
    ///
    /// @param breaks the breakable's breaks, by key
    /// @return the checks
    static Stream<DynamicNode> namedChecks(final Map<String, Break> breaks) {
        List<Method> methods = ContractRuns.testMethods(ComparatorContract.class, false);
        Set<String> names = methods.stream().map(Method::getName).collect(Collectors.toSet());
        Stream<DynamicNode> complete = Stream.of(dynamicTest("every break has targets, and every test is one", () -> {
            assertEquals(Set.copyOf(breaks.values()), TARGETS.keySet(), "the breaks with targets");
            assertEquals(names, TARGETS.values().stream().flatMap(List::stream).collect(Collectors.toSet()),
                    "the tests some break targets");
        }));
        Stream<DynamicNode> targets = TARGETS.entrySet().stream().flatMap(e -> e.getValue().stream().map(test ->
                dynamicTest(test + " fails with " + e.getKey().description(), () ->
                        assertFalse(ContractRuns.failures(() -> broken(e.getKey()), named(methods, test)).isEmpty(),
                                () -> test + " passes with the break: it cannot see it"))));
        Stream<DynamicNode> kept = TARGETS.entrySet().stream().map(e -> {
            Set<String> mayFail = new HashSet<>(e.getValue());
            mayFail.addAll(ALSO_FAILS.getOrDefault(e.getKey(), List.of()));
            List<Method> rest = methods.stream().filter(m -> !mayFail.contains(m.getName())).toList();
            return dynamicTest("the other tests pass with " + e.getKey().description(), () -> {
                List<String> failures = ContractRuns.failures(() -> broken(e.getKey()), rest);
                assertTrue(failures.isEmpty(), () -> "the break should not reach them, but:\n  "
                        + String.join("\n  ", failures));
            });
        });
        return Stream.of(complete, targets, kept).flatMap(s -> s);
    }

    private static List<Method> named(final List<Method> methods, final String name) {
        List<Method> named = methods.stream().filter(m -> m.getName().equals(name)).toList();
        assertEquals(1, named.size(), () -> "no test method " + name);
        return named;
    }
}
