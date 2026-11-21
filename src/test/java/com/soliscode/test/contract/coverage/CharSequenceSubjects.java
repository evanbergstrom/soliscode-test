/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.coverage;

import java.lang.reflect.Method;
import java.nio.CharBuffer;
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
import com.soliscode.test.breakable.BreakableCharSequence;
import com.soliscode.test.contract.charsequence.CharSequenceContract;
import com.soliscode.test.provider.CharSequenceProvider;
import com.soliscode.test.provider.CharSequenceProviders;

import static com.soliscode.test.breakable.BreakableCharSequence.CHARS_SIGN_EXTENDS;
import static com.soliscode.test.breakable.BreakableCharSequence.CHAR_AT_IS_OFF_BY_ONE;
import static com.soliscode.test.breakable.BreakableCharSequence.CHAR_AT_NEGATIVE_INDEX_THROWS_ILLEGAL_ARGUMENT;
import static com.soliscode.test.breakable.BreakableCharSequence.CHAR_AT_PAST_THE_END_RETURNS_ZERO;
import static com.soliscode.test.breakable.BreakableCharSequence.CODE_POINTS_DOES_NOT_COMBINE_SURROGATE_PAIRS;
import static com.soliscode.test.breakable.BreakableCharSequence.CODE_POINTS_DROPS_UNPAIRED_SURROGATES;
import static com.soliscode.test.breakable.BreakableCharSequence.COMPARE_TO_COMPARES_CODE_POINTS;
import static com.soliscode.test.breakable.BreakableCharSequence.COMPARE_TO_COMPARES_LENGTHS_FIRST;
import static com.soliscode.test.breakable.BreakableCharSequence.GET_CHARS_ACCEPTS_A_NULL_DESTINATION;
import static com.soliscode.test.breakable.BreakableCharSequence.GET_CHARS_ALLOWS_A_NEGATIVE_DESTINATION_BEGIN;
import static com.soliscode.test.breakable.BreakableCharSequence.GET_CHARS_ALLOWS_A_NEGATIVE_SOURCE_BEGIN;
import static com.soliscode.test.breakable.BreakableCharSequence.GET_CHARS_ALLOWS_SOURCE_BEGIN_AFTER_END;
import static com.soliscode.test.breakable.BreakableCharSequence.GET_CHARS_ALLOWS_SOURCE_END_PAST_THE_LENGTH;
import static com.soliscode.test.breakable.BreakableCharSequence.GET_CHARS_COPIES_ONE_CHAR_TOO_MANY;
import static com.soliscode.test.breakable.BreakableCharSequence.GET_CHARS_DOES_NOT_CHECK_THE_DESTINATION_SIZE;
import static com.soliscode.test.breakable.BreakableCharSequence.GET_CHARS_IGNORES_THE_DESTINATION_OFFSET;
import static com.soliscode.test.breakable.BreakableCharSequence.IS_EMPTY_ALWAYS_RETURNS_FALSE;
import static com.soliscode.test.breakable.BreakableCharSequence.LENGTH_COUNTS_CODE_POINTS;
import static com.soliscode.test.breakable.BreakableCharSequence.SUB_SEQUENCE_ALLOWS_A_NEGATIVE_END;
import static com.soliscode.test.breakable.BreakableCharSequence.SUB_SEQUENCE_ALLOWS_A_NEGATIVE_START;
import static com.soliscode.test.breakable.BreakableCharSequence.SUB_SEQUENCE_ALLOWS_END_PAST_THE_LENGTH;
import static com.soliscode.test.breakable.BreakableCharSequence.SUB_SEQUENCE_ALLOWS_START_AFTER_END;
import static com.soliscode.test.breakable.BreakableCharSequence.SUB_SEQUENCE_TREATS_END_AS_A_LENGTH;
import static com.soliscode.test.breakable.BreakableCharSequence.TO_STRING_IS_OBJECTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/// The subjects `ContractCoverageTest` runs `CharSequenceContract` against: `String`, `StringBuilder`, `CharBuffer`
/// (on a new array, at a nonzero position, and read-only over a `String`), `StringBuffer`, a sequence that keeps the
/// interface's defaults, and `BreakableCharSequence` over a `String` (#43).
///
/// Beyond the coverage test's own checks, it shows by name that the test each break targets fails for it, so that
/// every test method is shown to fail, and that every test it neither targets nor also fails passes with it.
final class CharSequenceSubjects {

    private static final String LENGTH = "lengthIsTheNumberOfChars";
    private static final String CHAR_AT = "charAtReturnsEachChar";
    private static final String CHAR_AT_BOUNDS = "charAtOutOfBoundsThrowsIndexOutOfBoundsException";
    private static final String IS_EMPTY = "isEmptyIsTrueExactlyWhenTheLengthIsZero";
    private static final String SUB_SEQUENCE = "subSequenceHoldsTheCharsFromStartToEnd";
    private static final String SUB_SEQUENCE_BOUNDS = "subSequenceOutOfBoundsThrowsIndexOutOfBoundsException";
    private static final String TO_STRING = "toStringHoldsTheSameChars";
    private static final String CHARS = "charsZeroExtendsEachChar";
    private static final String CODE_POINTS = "codePointsCombinesSurrogatePairsAndZeroExtendsTheRest";
    private static final String COMPARE = "compareIsLexicographicByChar";
    private static final String GET_CHARS = "getCharsCopiesTheRangeIntoTheDestination";
    private static final String GET_CHARS_BOUNDS = "getCharsOutOfBoundsThrowsIndexOutOfBoundsException";
    private static final String GET_CHARS_NULL = "getCharsNullDestinationThrowsNullPointerException";

    /// The test each break targets.
    private static final Map<Break, String> TARGETS = Map.ofEntries(
            Map.entry(LENGTH_COUNTS_CODE_POINTS, LENGTH),
            Map.entry(CHAR_AT_IS_OFF_BY_ONE, CHAR_AT),
            Map.entry(CHAR_AT_PAST_THE_END_RETURNS_ZERO, CHAR_AT_BOUNDS),
            Map.entry(CHAR_AT_NEGATIVE_INDEX_THROWS_ILLEGAL_ARGUMENT, CHAR_AT_BOUNDS),
            Map.entry(IS_EMPTY_ALWAYS_RETURNS_FALSE, IS_EMPTY),
            Map.entry(SUB_SEQUENCE_TREATS_END_AS_A_LENGTH, SUB_SEQUENCE),
            Map.entry(SUB_SEQUENCE_ALLOWS_A_NEGATIVE_START, SUB_SEQUENCE_BOUNDS),
            Map.entry(SUB_SEQUENCE_ALLOWS_A_NEGATIVE_END, SUB_SEQUENCE_BOUNDS),
            Map.entry(SUB_SEQUENCE_ALLOWS_START_AFTER_END, SUB_SEQUENCE_BOUNDS),
            Map.entry(SUB_SEQUENCE_ALLOWS_END_PAST_THE_LENGTH, SUB_SEQUENCE_BOUNDS),
            Map.entry(TO_STRING_IS_OBJECTS, TO_STRING),
            Map.entry(CHARS_SIGN_EXTENDS, CHARS),
            Map.entry(CODE_POINTS_DOES_NOT_COMBINE_SURROGATE_PAIRS, CODE_POINTS),
            Map.entry(CODE_POINTS_DROPS_UNPAIRED_SURROGATES, CODE_POINTS),
            Map.entry(COMPARE_TO_COMPARES_CODE_POINTS, COMPARE),
            Map.entry(COMPARE_TO_COMPARES_LENGTHS_FIRST, COMPARE),
            Map.entry(GET_CHARS_IGNORES_THE_DESTINATION_OFFSET, GET_CHARS),
            Map.entry(GET_CHARS_COPIES_ONE_CHAR_TOO_MANY, GET_CHARS),
            Map.entry(GET_CHARS_ALLOWS_A_NEGATIVE_SOURCE_BEGIN, GET_CHARS_BOUNDS),
            Map.entry(GET_CHARS_ALLOWS_A_NEGATIVE_DESTINATION_BEGIN, GET_CHARS_BOUNDS),
            Map.entry(GET_CHARS_ALLOWS_SOURCE_BEGIN_AFTER_END, GET_CHARS_BOUNDS),
            Map.entry(GET_CHARS_ALLOWS_SOURCE_END_PAST_THE_LENGTH, GET_CHARS_BOUNDS),
            Map.entry(GET_CHARS_DOES_NOT_CHECK_THE_DESTINATION_SIZE, GET_CHARS_BOUNDS),
            Map.entry(GET_CHARS_ACCEPTS_A_NULL_DESTINATION, GET_CHARS_NULL));

    /// The tests a break also fails, beyond its target, which are not checked. Every other test must pass with it.
    /// `compare` of a breakable and a `String` loops over the breakable's `length` and `charAt`.
    private static final Map<Break, List<String>> ALSO_FAILS = Map.of(
            LENGTH_COUNTS_CODE_POINTS, List.of(COMPARE),
            CHAR_AT_IS_OFF_BY_ONE, List.of(COMPARE));

    private CharSequenceSubjects() { }

    /// `CharSequenceContract` on a provider, claiming a natural order by `char` or not.
    static class Subject<T extends CharSequence> extends AbstractTest implements CharSequenceContract<T> {
        private final CharSequenceProvider<T> provider;
        private final boolean byChar;

        Subject(final CharSequenceProvider<T> providerArg, final boolean byCharArg) {
            this.provider = providerArg;
            this.byChar = byCharArg;
        }

        @Override
        public CharSequenceProvider<T> charSequenceProvider() {
            return provider;
        }

        @Override
        public boolean naturalOrderIsByChar() {
            return byChar;
        }
    }

    /// A sequence over a `String` that implements only the four abstract methods of `CharSequence`, so that
    /// `isEmpty`, `chars`, `codePoints` and `getChars` are the interface's defaults. It is not `Comparable`.
    static final class DefaultsOnly implements CharSequence {
        private final String chars;

        DefaultsOnly(final String charsArg) {
            this.chars = charsArg;
        }

        @Override
        public int length() {
            return chars.length();
        }

        @Override
        public char charAt(final int index) {
            return chars.charAt(index);
        }

        @Override
        public CharSequence subSequence(final int start, final int end) {
            return new DefaultsOnly(chars.substring(start, end));
        }

        @Override
        public String toString() {
            return chars;
        }
    }

    /// `CharSequenceContract` on `String`.
    static Subject<String> strings() {
        return new Subject<>(CharSequenceProviders.strings(), true);
    }

    /// `CharSequenceContract` on `BreakableCharSequence` over a `String`, with a break or none, claiming a natural
    /// order by `char` or not.
    static Subject<BreakableCharSequence> broken(final @Nullable Break b, final boolean byChar) {
        Set<Break> breaks = b == null ? Set.of() : Set.of(b);
        return new Subject<>(CharSequenceProviders.of(s -> new BreakableCharSequence(s, breaks)), byChar);
    }

    /// `CharSequenceContract` on `BreakableCharSequence` over a `String`, with a break or none.
    static Subject<BreakableCharSequence> broken(final @Nullable Break b) {
        return broken(b, true);
    }

    /// The JDK subjects beyond `String`, the sequence that keeps the defaults, and the breakable with a natural order
    /// not by `char`, which opts out.
    ///
    /// @return the checks
    static Stream<DynamicNode> moreSubjects() {
        List<Method> methods = ContractRuns.testMethods(CharSequenceContract.class, false);
        return Stream.of(
                passes("StringBuilder", () -> new Subject<>(CharSequenceProviders.stringBuilders(), true), methods),
                passes("CharBuffer", () -> new Subject<>(CharSequenceProviders.charBuffers(), true), methods),
                passes("CharBuffer at a nonzero position", () -> new Subject<>(CharSequenceProviders.of(s ->
                        CharBuffer.wrap(("##" + s + "##").toCharArray(), 2, s.length())), true), methods),
                passes("CharBuffer.wrap(CharSequence), read-only",
                        () -> new Subject<>(CharSequenceProviders.of(s -> CharBuffer.wrap(s)), true), methods),
                passes("StringBuffer", () -> new Subject<>(CharSequenceProviders.of(StringBuffer::new), true),
                        methods),
                passes("a sequence that keeps the interface's defaults",
                        () -> new Subject<>(CharSequenceProviders.of(DefaultsOnly::new), true), methods),
                passes("the breakable with COMPARE_TO_COMPARES_LENGTHS_FIRST, its natural order not by char",
                        () -> broken(COMPARE_TO_COMPARES_LENGTHS_FIRST, false), methods));
    }

    private static DynamicNode passes(final String name, final Supplier<?> contract, final List<Method> methods) {
        return dynamicTest("passes against " + name, () -> {
            List<String> failures = ContractRuns.failures(contract, methods);
            assertTrue(failures.isEmpty(), () -> "failures:\n  " + String.join("\n  ", failures));
        });
    }

    /// The checks by name: for each break, that the test it targets fails, and that every test it neither targets
    /// nor also fails passes. Every break of `BreakableCharSequence` must have a target, and every test method must be
    /// one.
    ///
    /// @param breaks the breakable's breaks, by key
    /// @return the checks
    static Stream<DynamicNode> namedChecks(final Map<String, Break> breaks) {
        List<Method> methods = ContractRuns.testMethods(CharSequenceContract.class, false);
        Set<String> names = methods.stream().map(Method::getName).collect(Collectors.toSet());
        Stream<DynamicNode> complete = Stream.of(dynamicTest("every break has a target, and every test is one", () -> {
            assertEquals(Set.copyOf(breaks.values()), TARGETS.keySet(), "the breaks with a target");
            assertEquals(names, Set.copyOf(TARGETS.values()), "the tests some break targets");
        }));
        Stream<DynamicNode> targets = TARGETS.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(e -> dynamicTest(e.getValue() + " fails with " + e.getKey().description(), () ->
                        assertFalse(ContractRuns.failures(() -> broken(e.getKey()), named(methods, e.getValue()))
                                .isEmpty(), () -> e.getValue() + " passes with the break: it cannot see it")));
        Stream<DynamicNode> kept = TARGETS.entrySet().stream().map(e -> {
            Set<String> mayFail = new HashSet<>(ALSO_FAILS.getOrDefault(e.getKey(), List.of()));
            mayFail.add(e.getValue());
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
