/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.coverage;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DynamicNode;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.breakable.Break;
import com.soliscode.test.breakable.BreakableObject;
import com.soliscode.test.contract.object.ObjectContract;
import com.soliscode.test.provider.CollectionProviders;
import com.soliscode.test.provider.FunctionalProvider;
import com.soliscode.test.provider.IntegerProvider;
import com.soliscode.test.provider.ObjectProvider;
import com.soliscode.test.provider.StringProvider;

import static com.soliscode.test.breakable.BreakableObject.EQUALS_ALWAYS_RETURNS_FALSE;
import static com.soliscode.test.breakable.BreakableObject.EQUALS_ALWAYS_RETURNS_TRUE;
import static com.soliscode.test.breakable.BreakableObject.EQUALS_IS_NOT_CONSISTENT;
import static com.soliscode.test.breakable.BreakableObject.EQUALS_IS_NOT_SYMMETRIC;
import static com.soliscode.test.breakable.BreakableObject.EQUALS_IS_NOT_TRANSITIVE;
import static com.soliscode.test.breakable.BreakableObject.EQUALS_THROWS_FOR_OTHER_TYPES;
import static com.soliscode.test.breakable.BreakableObject.HASH_CODE_ALWAYS_RETURNS_ZERO;
import static com.soliscode.test.breakable.BreakableObject.HASH_CODE_DIFFERS_FOR_EQUAL_VALUES;
import static com.soliscode.test.breakable.BreakableObject.HASH_CODE_IS_NOT_STABLE;
import static com.soliscode.test.breakable.BreakableObject.TO_STRING_IS_NOT_OVERRIDDEN;
import static com.soliscode.test.breakable.BreakableObject.TO_STRING_IS_NOT_STABLE;
import static com.soliscode.test.breakable.BreakableObject.TO_STRING_RETURNS_EMPTY_STRING;
import static com.soliscode.test.breakable.BreakableObject.TO_STRING_RETURNS_NULL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/// The subjects `ContractCoverageTest` runs `ObjectContract` against: `String`, `Integer`, a record of its own, a
/// `List` over a bounded element provider, and `BreakableObject` over the `Integer` provider (#70).
///
/// Beyond the coverage test's own checks, it shows by name that each test method fails with a break, so that no test
/// method is one that cannot fail, that the transitivity test fails at its conclusion, and that each opt-out lets
/// through its own break and only that one.
final class ObjectSubjects {

    /// How many values a bounded element provider has: a collection over it serves seeds below 1,991.
    private static final long BOUNDED_ELEMENTS = 2_000;

    /// A break that shows each test method failing.
    private static final Map<String, Break> SHOWN_BY = Map.ofEntries(
            Map.entry("equalsWhenSameInstanceReturnsTrue", EQUALS_ALWAYS_RETURNS_FALSE),
            Map.entry("equalsWhenCalledWithEqualAndUnequalValuesIsSymmetric", EQUALS_IS_NOT_SYMMETRIC),
            Map.entry("equalsWhenChainedIsTransitive", EQUALS_IS_NOT_TRANSITIVE),
            Map.entry("equalsWhenRepeatedReturnsConsistentResult", EQUALS_IS_NOT_CONSISTENT),
            Map.entry("equalsWithNullValueReturnsFalse", EQUALS_ALWAYS_RETURNS_TRUE),
            Map.entry("equalsWithOtherTypeReturnsFalse", EQUALS_THROWS_FOR_OTHER_TYPES),
            Map.entry("hashCodeWhenRepeatedIsConsistent", HASH_CODE_IS_NOT_STABLE),
            Map.entry("hashCodeWithEqualValuesReturnsSameInteger", HASH_CODE_DIFFERS_FOR_EQUAL_VALUES),
            Map.entry("hashCodeWithUniqueValuesReturnsUniqueIntegers", HASH_CODE_ALWAYS_RETURNS_ZERO),
            Map.entry("hashCodeWhenCalledManyTimesHasUniformDistribution", HASH_CODE_ALWAYS_RETURNS_ZERO),
            Map.entry("toStringWhenCalledReturnsNonNull", TO_STRING_RETURNS_NULL),
            Map.entry("toStringWhenRepeatedIsConsistent", TO_STRING_IS_NOT_STABLE),
            Map.entry("toStringWithEqualValuesReturnsSameString", TO_STRING_IS_NOT_STABLE),
            Map.entry("toStringWithUniqueValuesReturnsUniqueStrings", TO_STRING_RETURNS_EMPTY_STRING),
            Map.entry("toStringWhenCalledIsOverridden", TO_STRING_IS_NOT_OVERRIDDEN));

    private ObjectSubjects() { }

    /// A record of the coverage test's own. [Record]'s `hashCode` is not specified, so this subject relies on the
    /// JDK's current one spreading `Point((int) s, (int) (s >>> 32))`; `Point(s, s)` would not, with a hash of `32s`.
    ///
    /// @param x the first component
    /// @param y the second component
    record Point(int x, int y) {
    }

    /// The opt-outs a subject turns off.
    enum OptOut { NONE, SPREAD, DISTINGUISH, DESCRIBE }

    /// `ObjectContract` on a provider, with one of its opt-outs turned off, or none.
    static final class Subject<T> extends AbstractTest implements ObjectContract<T> {
        private final ObjectProvider<T> objects;
        private final OptOut optOut;

        Subject(final ObjectProvider<T> objectsArg, final OptOut optOutArg) {
            this.objects = objectsArg;
            this.optOut = optOutArg;
        }

        @Override
        public ObjectProvider<T> provider() {
            return objects;
        }

        @Override
        public boolean hashCodeSpreadsUnequalValues() {
            return optOut != OptOut.SPREAD;
        }

        @Override
        public boolean toStringDistinguishesUnequalValues() {
            return optOut != OptOut.DISTINGUISH;
        }

        @Override
        public boolean toStringDescribesValue() {
            return optOut != OptOut.DESCRIBE;
        }
    }

    /// `ObjectContract` on `String`.
    static Subject<String> strings() {
        return new Subject<>(new StringProvider(), OptOut.NONE);
    }

    /// `ObjectContract` on `Integer`.
    static Subject<Integer> integers() {
        return new Subject<>(new IntegerProvider(), OptOut.NONE);
    }

    /// `ObjectContract` on [Point].
    static Subject<Point> points() {
        return new Subject<>(new FunctionalProvider<>(() -> new Point(0, 0), p -> new Point(p.x(), p.y()),
                s -> new Point((int) (long) s, (int) (s >>> Integer.SIZE))), OptOut.NONE);
    }

    /// `ObjectContract` on an `ArrayList` over an `Integer` provider of [#BOUNDED_ELEMENTS] values, whose seeds the
    /// collection provider's limit bounds.
    static Subject<List<Integer>> boundedList() {
        ObjectProvider<Integer> bounded = new IntegerProvider() {
            @Override
            public long uniqueSizeLimit() {
                return BOUNDED_ELEMENTS;
            }
        };
        return new Subject<>(CollectionProviders.<List<Integer>, Integer>from(ArrayList::new, ArrayList::new,
                ArrayList::new, bounded), OptOut.NONE);
    }

    /// `ObjectContract` on `BreakableObject` over the `Integer` provider, with a break or none.
    static Subject<BreakableObject<Integer>> broken(final @Nullable Break b) {
        return broken(b, OptOut.NONE);
    }

    /// `ObjectContract` on `BreakableObject` over the `Integer` provider, with a break or none, and an opt-out.
    static Subject<BreakableObject<Integer>> broken(final @Nullable Break b, final OptOut optOut) {
        IntegerProvider integers = new IntegerProvider();
        return new Subject<>(new FunctionalProvider<>(() -> breakable(integers.createInstance(0), b),
                BreakableObject::new, s -> breakable(integers.createInstance(s), b)), optOut);
    }

    private static BreakableObject<Integer> breakable(final Integer value, final @Nullable Break b) {
        BreakableObject<Integer> o = new BreakableObject<>(value);
        if (b != null) {
            o.addBreak(b);
        }
        return o;
    }

    /// The checks by name: each test method fails alone with the break that shows it, every test method has one, the
    /// transitivity test fails at its conclusion, and each opt-out lets through its own break and only that one.
    ///
    /// @return the checks
    static Stream<DynamicNode> namedChecks() {
        List<Method> methods = ContractRuns.testMethods(ObjectContract.class, false);
        Stream<DynamicNode> complete = Stream.of(dynamicTest("every test method has a break that shows it", () ->
                assertEquals(methods.stream().map(Method::getName).collect(Collectors.toSet()), SHOWN_BY.keySet(),
                        "the test methods with a break")));
        Stream<DynamicNode> shown = methods.stream().filter(m -> SHOWN_BY.containsKey(m.getName()))
                .map(m -> {
                    Break b = SHOWN_BY.get(m.getName());
                    return ContractRuns.eachFails(m, b, () -> broken(b));
                });
        return Stream.of(complete, shown, Stream.of(transitivityConclusion(methods)), optOuts(methods)).flatMap(s -> s);
    }

    /// The transitivity break's premises hold and its conclusion fails, for a value and two copies made one after
    /// another, and `equalsWhenChainedIsTransitive` run against it fails at its conclusion, not a premise.
    private static DynamicNode transitivityConclusion(final List<Method> methods) {
        return dynamicTest("equalsWhenChainedIsTransitive fails at its conclusion with "
                + EQUALS_IS_NOT_TRANSITIVE.description(), () -> {
                    BreakableObject<Integer> x = breakable(1, EQUALS_IS_NOT_TRANSITIVE);
                    BreakableObject<Integer> y = new BreakableObject<>(x);
                    BreakableObject<Integer> z = new BreakableObject<>(y);
                    assertTrue(x.equals(y), "x equals y");
                    assertTrue(y.equals(z), "y equals z");
                    assertFalse(x.equals(z), "x does not equal z");
                    List<Method> transitive = methods.stream()
                            .filter(m -> m.getName().equals("equalsWhenChainedIsTransitive")).toList();
                    List<String> failures = ContractRuns.failures(() -> broken(EQUALS_IS_NOT_TRANSITIVE), transitive);
                    assertEquals(1, failures.size(), () -> "the test fails once: " + failures);
                    assertTrue(failures.getFirst().contains("equals is not transitive"),
                            () -> "the test fails at its conclusion: " + failures.getFirst());
                });
    }

    /// Each opt-out lets the contract pass with the breaks it covers, and still fail with one it does not.
    private static Stream<DynamicNode> optOuts(final List<Method> methods) {
        Map<OptOut, List<Break>> passes = Map.of(
                OptOut.SPREAD, List.of(HASH_CODE_ALWAYS_RETURNS_ZERO),
                OptOut.DISTINGUISH, List.of(TO_STRING_RETURNS_EMPTY_STRING),
                OptOut.DESCRIBE, List.of(TO_STRING_IS_NOT_STABLE, TO_STRING_IS_NOT_OVERRIDDEN));
        Map<OptOut, List<Break>> fails = Map.of(
                OptOut.SPREAD, List.of(TO_STRING_RETURNS_EMPTY_STRING),
                OptOut.DISTINGUISH, List.of(HASH_CODE_ALWAYS_RETURNS_ZERO),
                OptOut.DESCRIBE, List.of(TO_STRING_RETURNS_NULL, TO_STRING_RETURNS_EMPTY_STRING));
        Stream<DynamicNode> passing = passes.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .flatMap(e -> e.getValue().stream().map(b -> ContractRuns.passes(
                        b.description() + " with opt-out " + e.getKey(), () -> broken(b, e.getKey()), methods)));
        Stream<DynamicNode> failing = fails.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .flatMap(e -> e.getValue().stream().map(b -> ContractRuns.fails(
                        b.description() + " with opt-out " + e.getKey(), () -> broken(b, e.getKey()), methods)));
        return Stream.concat(passing, failing);
    }
}
