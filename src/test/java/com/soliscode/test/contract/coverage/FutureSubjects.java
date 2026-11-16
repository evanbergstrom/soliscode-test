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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DynamicNode;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.breakable.Break;
import com.soliscode.test.breakable.BreakableFuture;
import com.soliscode.test.contract.future.FutureContract;
import com.soliscode.test.contract.future.ThreadSafeFutureContract;
import com.soliscode.test.provider.FutureProvider;
import com.soliscode.test.provider.FutureProviders;

import static com.soliscode.test.breakable.BreakableFuture.CANCEL_CANCELS_A_DONE_FUTURE;
import static com.soliscode.test.breakable.BreakableFuture.CANCEL_DOES_NOTHING;
import static com.soliscode.test.breakable.BreakableFuture.CANCEL_IS_NOT_THREAD_SAFE;
import static com.soliscode.test.breakable.BreakableFuture.CANCEL_RETURNS_FALSE;
import static com.soliscode.test.breakable.BreakableFuture.COMPLETE_OVERRIDES_CANCELLATION;
import static com.soliscode.test.breakable.BreakableFuture.EXCEPTION_NOW_RETURNS_CANCELLATION_EXCEPTION;
import static com.soliscode.test.breakable.BreakableFuture.EXCEPTION_NOW_RETURNS_EXECUTION_EXCEPTION;
import static com.soliscode.test.breakable.BreakableFuture.EXCEPTION_NOW_RETURNS_NULL_ONCE_SUCCEEDED;
import static com.soliscode.test.breakable.BreakableFuture.GET_DOES_NOT_WAIT;
import static com.soliscode.test.breakable.BreakableFuture.GET_IGNORES_INTERRUPT;
import static com.soliscode.test.breakable.BreakableFuture.GET_IS_NOT_THREAD_SAFE;
import static com.soliscode.test.breakable.BreakableFuture.GET_RETURNS_NULL_ONCE_SUCCEEDED;
import static com.soliscode.test.breakable.BreakableFuture.GET_THROWS_EXECUTION_EXCEPTION_ONCE_CANCELLED;
import static com.soliscode.test.breakable.BreakableFuture.GET_THROWS_EXECUTION_EXCEPTION_WITHOUT_CAUSE;
import static com.soliscode.test.breakable.BreakableFuture.IS_CANCELLED_IS_FALSE_ONCE_CANCELLED;
import static com.soliscode.test.breakable.BreakableFuture.IS_CANCELLED_IS_TRUE_ONCE_FAILED;
import static com.soliscode.test.breakable.BreakableFuture.IS_DONE_IS_FALSE_ONCE_CANCELLED;
import static com.soliscode.test.breakable.BreakableFuture.IS_DONE_IS_FALSE_ONCE_FAILED;
import static com.soliscode.test.breakable.BreakableFuture.IS_DONE_IS_TRUE_WHILE_PENDING;
import static com.soliscode.test.breakable.BreakableFuture.NULL_RESULT_READS_AS_PENDING;
import static com.soliscode.test.breakable.BreakableFuture.RESULT_NOW_RETURNS_NULL_ONCE_FAILED;
import static com.soliscode.test.breakable.BreakableFuture.RESULT_NOW_RETURNS_NULL_WHILE_PENDING;
import static com.soliscode.test.breakable.BreakableFuture.RESULT_NOW_THROWS_ONCE_SUCCEEDED;
import static com.soliscode.test.breakable.BreakableFuture.STATE_IS_FAILED_ONCE_CANCELLED;
import static com.soliscode.test.breakable.BreakableFuture.STATE_IS_RUNNING_ONCE_FAILED;
import static com.soliscode.test.breakable.BreakableFuture.STATE_IS_SUCCESS_WHILE_PENDING;
import static com.soliscode.test.breakable.BreakableFuture.TIMED_GET_DOES_NOT_TIME_OUT;
import static com.soliscode.test.breakable.BreakableFuture.TIMED_GET_DOES_NOT_WAIT;
import static com.soliscode.test.breakable.BreakableFuture.TIMED_GET_IGNORES_INTERRUPT;
import static com.soliscode.test.breakable.BreakableFuture.TIMED_GET_IS_NOT_THREAD_SAFE;
import static com.soliscode.test.breakable.BreakableFuture.TIMED_GET_TIMES_OUT_WITH_ZERO_TIMEOUT_ONCE_DONE;
import static com.soliscode.test.contract.coverage.ContractRuns.fails;
import static com.soliscode.test.contract.coverage.ContractRuns.passes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/// The subjects `ContractCoverageTest` runs `FutureContract` and `ThreadSafeFutureContract` against:
/// `CompletableFuture`, `FutureTask`, and `BreakableFuture` with a break or none (#44).
///
/// Beyond the coverage test's own checks, it shows by name that the test each single-thread break targets fails for
/// it, that every such break has a target, and that every test method of `FutureContract` is one. The thread-unsafe
/// breaks are left out of these, as `UncheckedReasons.uncheckedReason` leaves them out of `FutureContract`'s run;
/// [#threadSafeChecks] shows each failing its own concurrency test, named in [#THREAD_SAFETY_TARGETS].
final class FutureSubjects {

    /// The test each single-thread break targets.
    private static final Map<Break, String> TARGETS = Map.ofEntries(
            Map.entry(IS_DONE_IS_TRUE_WHILE_PENDING, "isDoneIsFalseWhilePending"),
            Map.entry(IS_DONE_IS_FALSE_ONCE_FAILED, "isDoneIsTrueOnceDone"),
            Map.entry(IS_DONE_IS_FALSE_ONCE_CANCELLED, "isDoneIsTrueOnceDone"),
            Map.entry(IS_CANCELLED_IS_TRUE_ONCE_FAILED, "isCancelledIsFalseUnlessCancelled"),
            Map.entry(IS_CANCELLED_IS_FALSE_ONCE_CANCELLED, "isCancelledIsTrueOnceCancelled"),
            Map.entry(CANCEL_RETURNS_FALSE, "cancelOfAPendingFutureCancelsIt"),
            Map.entry(CANCEL_DOES_NOTHING, "cancelOfAPendingFutureCancelsIt"),
            Map.entry(CANCEL_CANCELS_A_DONE_FUTURE, "cancelOfADoneFutureHasNoEffect"),
            Map.entry(COMPLETE_OVERRIDES_CANCELLATION, "finishingACancelledFutureLeavesItCancelled"),
            Map.entry(GET_RETURNS_NULL_ONCE_SUCCEEDED, "getReturnsTheResultOnceSucceeded"),
            Map.entry(GET_THROWS_EXECUTION_EXCEPTION_WITHOUT_CAUSE, "getThrowsExecutionExceptionOnceFailed"),
            Map.entry(GET_THROWS_EXECUTION_EXCEPTION_ONCE_CANCELLED, "getThrowsCancellationExceptionOnceCancelled"),
            Map.entry(GET_DOES_NOT_WAIT, "getWaitsForCompletion"),
            Map.entry(GET_IGNORES_INTERRUPT, "getThrowsInterruptedExceptionWhenInterrupted"),
            Map.entry(NULL_RESULT_READS_AS_PENDING, "aNullResultIsAResult"),
            Map.entry(TIMED_GET_TIMES_OUT_WITH_ZERO_TIMEOUT_ONCE_DONE,
                    "timedGetWithZeroTimeoutGivesTheOutcomeOnceDone"),
            Map.entry(TIMED_GET_DOES_NOT_TIME_OUT, "timedGetTimesOutWhilePending"),
            Map.entry(TIMED_GET_DOES_NOT_WAIT, "timedGetWaitsForCompletion"),
            Map.entry(TIMED_GET_IGNORES_INTERRUPT, "timedGetThrowsInterruptedExceptionWhenInterrupted"),
            Map.entry(RESULT_NOW_THROWS_ONCE_SUCCEEDED, "resultNowReturnsTheResultOnceSucceeded"),
            Map.entry(RESULT_NOW_RETURNS_NULL_WHILE_PENDING, "resultNowThrowsUnlessSucceeded"),
            Map.entry(RESULT_NOW_RETURNS_NULL_ONCE_FAILED, "resultNowThrowsUnlessSucceeded"),
            Map.entry(EXCEPTION_NOW_RETURNS_EXECUTION_EXCEPTION, "exceptionNowReturnsTheExceptionOnceFailed"),
            Map.entry(EXCEPTION_NOW_RETURNS_NULL_ONCE_SUCCEEDED, "exceptionNowThrowsUnlessFailed"),
            Map.entry(EXCEPTION_NOW_RETURNS_CANCELLATION_EXCEPTION, "exceptionNowThrowsUnlessFailed"),
            Map.entry(STATE_IS_SUCCESS_WHILE_PENDING, "stateIsRunningWhilePending"),
            Map.entry(STATE_IS_FAILED_ONCE_CANCELLED, "stateIsTheOutcomeOnceDone"),
            Map.entry(STATE_IS_RUNNING_ONCE_FAILED, "stateIsTheOutcomeOnceDone"));

    /// The concurrency test each thread-unsafe break targets.
    private static final Map<Break, String> THREAD_SAFETY_TARGETS = Map.of(
            GET_IS_NOT_THREAD_SAFE, "getWhenCompletedUnderContentionIsThreadSafe",
            TIMED_GET_IS_NOT_THREAD_SAFE, "timedGetWhenCompletedUnderContentionIsThreadSafe",
            CANCEL_IS_NOT_THREAD_SAFE, "cancelWhenCalledUnderContentionIsThreadSafe");

    private FutureSubjects() { }

    /// `FutureContract` on a provider.
    static class Subject<F extends java.util.concurrent.Future<Integer>> extends AbstractTest
            implements FutureContract<F, Integer> {
        private final FutureProvider<F, Integer> futures;

        Subject(final FutureProvider<F, Integer> futuresArg) {
            this.futures = futuresArg;
        }

        @Override
        public FutureProvider<F, Integer> futureProvider() {
            return futures;
        }
    }

    /// `ThreadSafeFutureContract` on a provider.
    static class ThreadSafeSubject<F extends java.util.concurrent.Future<Integer>> extends AbstractTest
            implements ThreadSafeFutureContract<F, Integer> {
        private final FutureProvider<F, Integer> futures;

        ThreadSafeSubject(final FutureProvider<F, Integer> futuresArg) {
            this.futures = futuresArg;
        }

        @Override
        public FutureProvider<F, Integer> futureProvider() {
            return futures;
        }
    }

    /// `FutureContract` on `CompletableFuture`.
    static Subject<CompletableFuture<Integer>> completableFutures() {
        return new Subject<>(FutureProviders.completableFutures());
    }

    /// `FutureContract` on `FutureTask`.
    static Subject<FutureTask<Integer>> futureTasks() {
        return new Subject<>(FutureProviders.futureTasks());
    }

    /// `BreakableFuture`s with a break, or none, finished by `complete` and `completeExceptionally`.
    static FutureProvider<BreakableFuture<Integer>, Integer> breakables(final @Nullable Break b) {
        Set<Break> breaks = b == null ? Set.of() : Set.of(b);
        return FutureProviders.completedBy(() -> new BreakableFuture<>(breaks), BreakableFuture::complete,
                BreakableFuture::completeExceptionally, new AtomicInteger()::incrementAndGet);
    }

    /// `FutureContract` on `BreakableFuture` with a break, or none.
    static Subject<BreakableFuture<Integer>> broken(final @Nullable Break b) {
        return new Subject<>(breakables(b));
    }

    /// `ThreadSafeFutureContract` on `BreakableFuture` with a break, or none.
    static ThreadSafeSubject<BreakableFuture<Integer>> brokenThreadSafe(final @Nullable Break b) {
        return new ThreadSafeSubject<>(breakables(b));
    }

    /// The checks of `FutureContract` beyond the coverage test's own: that it passes against `FutureTask`, and the
    /// checks by name ([#namedChecks]) of the breaks it checks.
    ///
    /// @param checked whether a break, by key, is checked against `FutureContract`
    /// @return the checks
    static Stream<DynamicNode> moreChecks(final Predicate<String> checked) {
        Map<String, Break> breaks = new LinkedHashMap<>(ContractRuns.breaks(
                ContractRuns.withSuperclasses(BreakableFuture.class)));
        breaks.keySet().removeIf(checked.negate());
        return Stream.concat(Stream.of(passes("FutureTask", FutureSubjects::futureTasks,
                ContractRuns.testMethods(FutureContract.class, false))), namedChecks(breaks));
    }

    /// The checks of `ThreadSafeFutureContract`: it passes against `CompletableFuture`, `FutureTask` and the unbroken
    /// breakable, and fails for each of the breakable's three thread-unsafe breaks. Each break is shown failing its
    /// own concurrency test, named in [#THREAD_SAFETY_TARGETS], and passing `FutureContract`'s single-thread tests,
    /// so that the failure is the concurrency test's.
    ///
    /// @param checked whether a break, by key, is checked against `ThreadSafeFutureContract`
    /// @return the checks
    static Stream<DynamicNode> threadSafeChecks(final Predicate<String> checked) {
        List<Method> all = ContractRuns.testMethods(ThreadSafeFutureContract.class, true);
        Map<String, Break> breaks = new LinkedHashMap<>(ContractRuns.breaks(
                ContractRuns.withSuperclasses(BreakableFuture.class)));
        breaks.keySet().removeIf(checked.negate());
        assertEquals(THREAD_SAFETY_TARGETS.keySet(), Set.copyOf(breaks.values()),
                () -> "the thread-safety breaks of BreakableFuture: " + breaks.keySet());
        return Stream.of(dynamicContainer("ThreadSafeFutureContract", Stream.concat(
                Stream.of(passes("CompletableFuture",
                                () -> new ThreadSafeSubject<>(FutureProviders.completableFutures()), all),
                        passes("FutureTask", () -> new ThreadSafeSubject<>(FutureProviders.futureTasks()), all),
                        passes("unbroken BreakableFuture", () -> brokenThreadSafe(null), all)),
                breaks.entrySet().stream().flatMap(e -> Stream.of(
                        fails(e.getKey() + " in " + THREAD_SAFETY_TARGETS.get(e.getValue()),
                                () -> brokenThreadSafe(e.getValue()), named(all,
                                        THREAD_SAFETY_TARGETS.get(e.getValue()))),
                        passes("FutureContract's single-thread tests with " + e.getKey(),
                                () -> broken(e.getValue()), ContractRuns.testMethods(FutureContract.class, false)))))));
    }

    /// The checks by name: for each single-thread break, that the test it targets fails, that every such break has a
    /// target, and that every test method is one.
    ///
    /// @param breaks the single-thread breaks of `BreakableFuture`, by key
    /// @return the checks
    static Stream<DynamicNode> namedChecks(final Map<String, Break> breaks) {
        List<Method> methods = ContractRuns.testMethods(FutureContract.class, false);
        Stream<DynamicNode> complete = Stream.of(dynamicTest("every break has a target, and every test is one",
                () -> {
                    assertEquals(Set.copyOf(breaks.values()), TARGETS.keySet(), "the breaks with a target");
                    assertEquals(methods.stream().map(Method::getName).collect(Collectors.toSet()),
                            Set.copyOf(TARGETS.values()), "the tests some break targets");
                }));
        Stream<DynamicNode> targets = TARGETS.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(e -> dynamicTest(e.getValue() + " fails with " + e.getKey().description(), () ->
                        assertFalse(ContractRuns.failures(() -> broken(e.getKey()), named(methods, e.getValue()))
                                .isEmpty(), () -> e.getValue() + " passes with the break: it cannot see it")));
        return Stream.concat(complete, targets);
    }

    private static List<Method> named(final List<Method> methods, final String name) {
        List<Method> named = methods.stream().filter(m -> m.getName().equals(name)).toList();
        assertEquals(1, named.size(), () -> "no test method " + name);
        return named;
    }
}
