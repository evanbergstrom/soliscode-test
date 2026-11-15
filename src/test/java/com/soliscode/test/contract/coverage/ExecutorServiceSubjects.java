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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DynamicNode;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.breakable.Break;
import com.soliscode.test.breakable.BreakableExecutorService;
import com.soliscode.test.contract.executorservice.ExecutorServiceContract;
import com.soliscode.test.contract.executorservice.ThreadSafeExecutorServiceContract;
import com.soliscode.test.provider.ExecutorServiceProvider;
import com.soliscode.test.provider.ExecutorServiceProviders;

import static com.soliscode.test.breakable.BreakableExecutorService.AWAIT_TERMINATION_DOES_NOT_WAIT;
import static com.soliscode.test.breakable.BreakableExecutorService.AWAIT_TERMINATION_IGNORES_INTERRUPT;
import static com.soliscode.test.breakable.BreakableExecutorService.AWAIT_TERMINATION_RETURNS_TRUE_ON_TIMEOUT;
import static com.soliscode.test.breakable.BreakableExecutorService.AWAIT_TERMINATION_TIMES_OUT_ONCE_TERMINATED;
import static com.soliscode.test.breakable.BreakableExecutorService.CLOSE_CLEARS_THE_INTERRUPT;
import static com.soliscode.test.breakable.BreakableExecutorService.CLOSE_DOES_NOT_WAIT;
import static com.soliscode.test.breakable.BreakableExecutorService.EXECUTE_ACCEPTS_NULL;
import static com.soliscode.test.breakable.BreakableExecutorService.EXECUTE_DOES_NOT_RUN_THE_TASK;
import static com.soliscode.test.breakable.BreakableExecutorService.EXECUTE_RUNS_THE_TASK_TWICE;
import static com.soliscode.test.breakable.BreakableExecutorService.INVOKE_ALL_ACCEPTS_NULL;
import static com.soliscode.test.breakable.BreakableExecutorService.INVOKE_ALL_DOES_NOT_WAIT;
import static com.soliscode.test.breakable.BreakableExecutorService.INVOKE_ALL_IGNORES_INTERRUPT;
import static com.soliscode.test.breakable.BreakableExecutorService.INVOKE_ALL_REJECTS_NO_TASKS;
import static com.soliscode.test.breakable.BreakableExecutorService.INVOKE_ALL_REVERSES_THE_ORDER;
import static com.soliscode.test.breakable.BreakableExecutorService.INVOKE_ANY_ACCEPTS_EMPTY;
import static com.soliscode.test.breakable.BreakableExecutorService.INVOKE_ANY_ACCEPTS_NULL;
import static com.soliscode.test.breakable.BreakableExecutorService.INVOKE_ANY_FAILS_IF_ANY_TASK_FAILS;
import static com.soliscode.test.breakable.BreakableExecutorService.INVOKE_ANY_IGNORES_INTERRUPT;
import static com.soliscode.test.breakable.BreakableExecutorService.INVOKE_ANY_RETURNS_NULL_WHEN_ALL_FAIL;
import static com.soliscode.test.breakable.BreakableExecutorService.IS_SHUTDOWN_IS_FALSE_AFTER_SHUTDOWN;
import static com.soliscode.test.breakable.BreakableExecutorService.IS_SHUTDOWN_IS_FALSE_AFTER_SHUTDOWN_NOW;
import static com.soliscode.test.breakable.BreakableExecutorService.IS_SHUTDOWN_IS_TRUE_WHILE_TASKS_RUN;
import static com.soliscode.test.breakable.BreakableExecutorService.IS_TERMINATED_IS_FALSE_ONCE_TERMINATED;
import static com.soliscode.test.breakable.BreakableExecutorService.IS_TERMINATED_IS_TRUE_BEFORE_SHUTDOWN;
import static com.soliscode.test.breakable.BreakableExecutorService.IS_TERMINATED_IS_TRUE_WHILE_TASKS_RUN;
import static com.soliscode.test.breakable.BreakableExecutorService.SECOND_CLOSE_THROWS;
import static com.soliscode.test.breakable.BreakableExecutorService.SECOND_SHUTDOWN_RESTARTS;
import static com.soliscode.test.breakable.BreakableExecutorService.SHUTDOWN_DOES_NOT_REJECT;
import static com.soliscode.test.breakable.BreakableExecutorService.SHUTDOWN_DROPS_QUEUED_TASKS;
import static com.soliscode.test.breakable.BreakableExecutorService.SHUTDOWN_IS_NOT_THREAD_SAFE;
import static com.soliscode.test.breakable.BreakableExecutorService.SHUTDOWN_NOW_DISCARDS_WAITING_TASKS;
import static com.soliscode.test.breakable.BreakableExecutorService.SHUTDOWN_NOW_DOES_NOT_REJECT;
import static com.soliscode.test.breakable.BreakableExecutorService.SHUTDOWN_NOW_RUNS_THE_TASKS_IT_RETURNS;
import static com.soliscode.test.breakable.BreakableExecutorService.SHUTDOWN_WAITS_FOR_TASKS;
import static com.soliscode.test.breakable.BreakableExecutorService.SUBMIT_CALLABLE_ACCEPTS_NULL;
import static com.soliscode.test.breakable.BreakableExecutorService.SUBMIT_CALLABLE_RETURNS_NULL;
import static com.soliscode.test.breakable.BreakableExecutorService.SUBMIT_CALLABLE_SWALLOWS_THE_EXCEPTION;
import static com.soliscode.test.breakable.BreakableExecutorService.SUBMIT_IS_NOT_THREAD_SAFE;
import static com.soliscode.test.breakable.BreakableExecutorService.SUBMIT_RUNNABLE_ACCEPTS_NULL;
import static com.soliscode.test.breakable.BreakableExecutorService.SUBMIT_RUNNABLE_DOES_NOT_RUN_THE_TASK;
import static com.soliscode.test.breakable.BreakableExecutorService.SUBMIT_RUNNABLE_WITH_RESULT_ACCEPTS_NULL;
import static com.soliscode.test.breakable.BreakableExecutorService.SUBMIT_RUNNABLE_WITH_RESULT_RETURNS_NULL;
import static com.soliscode.test.breakable.BreakableExecutorService.TIMED_INVOKE_ALL_DOES_NOT_CANCEL;
import static com.soliscode.test.breakable.BreakableExecutorService.TIMED_INVOKE_ALL_DOES_NOT_WAIT;
import static com.soliscode.test.breakable.BreakableExecutorService.TIMED_INVOKE_ANY_DOES_NOT_TIME_OUT;
import static com.soliscode.test.breakable.BreakableExecutorService.TIMED_INVOKE_ANY_DOES_NOT_WAIT;
import static com.soliscode.test.contract.coverage.ContractRuns.fails;
import static com.soliscode.test.contract.coverage.ContractRuns.passes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/// The subjects `ContractCoverageTest` runs `ExecutorServiceContract` and `ThreadSafeExecutorServiceContract`
/// against: `Executors.newFixedThreadPool(2)`, `Executors.newVirtualThreadPerTaskExecutor()`, a new `ForkJoinPool`,
/// a `ThreadPoolExecutor` with a `DiscardPolicy` that opts out of `rejectsTasksAfterShutdown()`, and
/// `BreakableExecutorService` with a break or none (#45).
///
/// Beyond the coverage test's own checks, it shows by name that the test each single-thread break targets fails for
/// it, that every such break has a target, and that every test method of `ExecutorServiceContract` is one. The
/// thread-unsafe breaks are left out of these, as `ContractCoverageTest.uncheckedReason` leaves them out of
/// `ExecutorServiceContract`'s run; [#threadSafeChecks] shows each failing its own concurrency test, named in
/// [#THREAD_SAFETY_TARGETS].
final class ExecutorServiceSubjects {

    /// The number of worker threads of each breakable.
    private static final int THREADS = 2;

    /// The test each single-thread break targets.
    private static final Map<Break, String> TARGETS = Map.ofEntries(
            Map.entry(EXECUTE_DOES_NOT_RUN_THE_TASK, "executeRunsTheCommandOnce"),
            Map.entry(EXECUTE_RUNS_THE_TASK_TWICE, "executeRunsTheCommandOnce"),
            Map.entry(EXECUTE_ACCEPTS_NULL, "executeOfNullThrowsNullPointerException"),
            Map.entry(SUBMIT_CALLABLE_RETURNS_NULL, "submitCallableGivesItsResult"),
            Map.entry(SUBMIT_CALLABLE_SWALLOWS_THE_EXCEPTION, "submitCallableGivesItsException"),
            Map.entry(SUBMIT_CALLABLE_ACCEPTS_NULL, "submitCallableOfNullThrowsNullPointerException"),
            Map.entry(SUBMIT_RUNNABLE_DOES_NOT_RUN_THE_TASK, "submitRunnableRunsItAndGivesNull"),
            Map.entry(SUBMIT_RUNNABLE_ACCEPTS_NULL, "submitRunnableOfNullThrowsNullPointerException"),
            Map.entry(SUBMIT_RUNNABLE_WITH_RESULT_RETURNS_NULL, "submitRunnableWithResultRunsItAndGivesTheResult"),
            Map.entry(SUBMIT_RUNNABLE_WITH_RESULT_ACCEPTS_NULL,
                    "submitRunnableWithResultOfNullThrowsNullPointerException"),
            Map.entry(SHUTDOWN_DROPS_QUEUED_TASKS, "shutdownRunsTheTasksAlreadySubmitted"),
            Map.entry(SHUTDOWN_DOES_NOT_REJECT, "shutdownRejectsNewTasks"),
            Map.entry(SECOND_SHUTDOWN_RESTARTS, "secondShutdownHasNoEffect"),
            Map.entry(SHUTDOWN_WAITS_FOR_TASKS, "shutdownDoesNotWait"),
            Map.entry(SHUTDOWN_NOW_DOES_NOT_REJECT, "shutdownNowRejectsNewTasksAndTerminates"),
            Map.entry(SHUTDOWN_NOW_RUNS_THE_TASKS_IT_RETURNS, "shutdownNowReturnsOrCancelsTheTasksNotStarted"),
            Map.entry(SHUTDOWN_NOW_DISCARDS_WAITING_TASKS, "shutdownNowReturnsOrCancelsTheTasksNotStarted"),
            Map.entry(IS_SHUTDOWN_IS_TRUE_WHILE_TASKS_RUN, "isShutdownIsFalseBeforeShutdown"),
            Map.entry(IS_SHUTDOWN_IS_FALSE_AFTER_SHUTDOWN, "isShutdownIsTrueAfterShutdown"),
            Map.entry(IS_SHUTDOWN_IS_FALSE_AFTER_SHUTDOWN_NOW, "isShutdownIsTrueAfterShutdownNow"),
            Map.entry(IS_TERMINATED_IS_TRUE_BEFORE_SHUTDOWN, "isTerminatedIsFalseBeforeShutdown"),
            Map.entry(IS_TERMINATED_IS_TRUE_WHILE_TASKS_RUN, "isTerminatedIsFalseWhileTasksRun"),
            Map.entry(IS_TERMINATED_IS_FALSE_ONCE_TERMINATED, "isTerminatedIsTrueOnceTerminated"),
            Map.entry(AWAIT_TERMINATION_RETURNS_TRUE_ON_TIMEOUT, "awaitTerminationTimesOutWhileTasksRun"),
            Map.entry(AWAIT_TERMINATION_TIMES_OUT_ONCE_TERMINATED,
                    "awaitTerminationWithZeroTimeoutIsTrueOnceTerminated"),
            Map.entry(AWAIT_TERMINATION_DOES_NOT_WAIT, "awaitTerminationWaitsForTheTasks"),
            Map.entry(AWAIT_TERMINATION_IGNORES_INTERRUPT, "awaitTerminationThrowsInterruptedExceptionWhenInterrupted"),
            Map.entry(CLOSE_DOES_NOT_WAIT, "closeWaitsForTheTasks"),
            Map.entry(SECOND_CLOSE_THROWS, "secondCloseHasNoEffect"),
            Map.entry(CLOSE_CLEARS_THE_INTERRUPT, "closeWhenInterruptedSetsTheInterruptStatusAgain"),
            Map.entry(INVOKE_ALL_REVERSES_THE_ORDER, "invokeAllGivesTheOutcomesInOrder"),
            Map.entry(INVOKE_ALL_REJECTS_NO_TASKS, "invokeAllGivesTheOutcomesInOrder"),
            Map.entry(INVOKE_ALL_DOES_NOT_WAIT, "invokeAllWaitsForTheTasks"),
            Map.entry(INVOKE_ALL_ACCEPTS_NULL, "invokeAllWithANullTaskThrowsNullPointerException"),
            Map.entry(INVOKE_ALL_IGNORES_INTERRUPT, "invokeAllThrowsInterruptedExceptionWhenInterrupted"),
            Map.entry(TIMED_INVOKE_ALL_DOES_NOT_WAIT, "timedInvokeAllWaitsForTheTasks"),
            Map.entry(TIMED_INVOKE_ALL_DOES_NOT_CANCEL, "timedInvokeAllCancelsTheTasksNotDone"),
            Map.entry(INVOKE_ANY_FAILS_IF_ANY_TASK_FAILS, "invokeAnyGivesTheResultOfATaskThatSucceeded"),
            Map.entry(INVOKE_ANY_RETURNS_NULL_WHEN_ALL_FAIL, "invokeAnyThrowsExecutionExceptionWhenEveryTaskFails"),
            Map.entry(INVOKE_ANY_ACCEPTS_EMPTY, "invokeAnyOfNoTasksThrowsIllegalArgumentException"),
            Map.entry(INVOKE_ANY_ACCEPTS_NULL, "invokeAnyWithANullTaskThrowsNullPointerException"),
            Map.entry(INVOKE_ANY_IGNORES_INTERRUPT, "invokeAnyThrowsInterruptedExceptionWhenInterrupted"),
            Map.entry(TIMED_INVOKE_ANY_DOES_NOT_WAIT, "timedInvokeAnyWaitsForATask"),
            Map.entry(TIMED_INVOKE_ANY_DOES_NOT_TIME_OUT, "timedInvokeAnyTimesOut"));

    /// The concurrency test each thread-unsafe break targets.
    private static final Map<Break, String> THREAD_SAFETY_TARGETS = Map.of(
            SUBMIT_IS_NOT_THREAD_SAFE, "submitWhenCalledUnderContentionRunsEachTaskOnce",
            SHUTDOWN_IS_NOT_THREAD_SAFE, "shutdownWhenCalledUnderContentionRunsEachAcceptedTask");

    private ExecutorServiceSubjects() { }

    /// `ExecutorServiceContract` on a provider.
    static class Subject<E extends ExecutorService> extends AbstractTest implements ExecutorServiceContract<E> {
        private final ExecutorServiceProvider<E> executors;
        private final boolean rejects;

        Subject(final ExecutorServiceProvider<E> executorsArg, final boolean rejectsArg) {
            this.executors = executorsArg;
            this.rejects = rejectsArg;
        }

        @Override
        public ExecutorServiceProvider<E> executorServiceProvider() {
            return executors;
        }

        @Override
        public boolean rejectsTasksAfterShutdown() {
            return rejects;
        }
    }

    /// `ThreadSafeExecutorServiceContract` on a provider.
    static class ThreadSafeSubject<E extends ExecutorService> extends Subject<E>
            implements ThreadSafeExecutorServiceContract<E> {
        ThreadSafeSubject(final ExecutorServiceProvider<E> executorsArg, final boolean rejectsArg) {
            super(executorsArg, rejectsArg);
        }
    }

    /// A `ThreadPoolExecutor` of 2 threads with a `DiscardPolicy`.
    static ThreadPoolExecutor discarding() {
        return new ThreadPoolExecutor(THREADS, THREADS, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                new ThreadPoolExecutor.DiscardPolicy());
    }

    /// `BreakableExecutorService`s of 2 threads with a break, or none.
    static ExecutorServiceProvider<BreakableExecutorService> breakables(final @Nullable Break b) {
        Set<Break> breaks = b == null ? Set.of() : Set.of(b);
        return () -> new BreakableExecutorService(THREADS, breaks);
    }

    /// `ExecutorServiceContract` on `BreakableExecutorService` with a break, or none.
    static Subject<BreakableExecutorService> broken(final @Nullable Break b) {
        return new Subject<>(breakables(b), true);
    }

    /// The checks of `ExecutorServiceContract`, as `ContractCoverageTest`'s harness makes them for the other
    /// contracts: it passes against `newFixedThreadPool(2)`, `newVirtualThreadPerTaskExecutor()`, a new
    /// `ForkJoinPool`, the discarding `ThreadPoolExecutor`, opted out, and the unbroken breakable, and fails for each
    /// single-thread break; with the checks by name ([#namedChecks]) of those breaks.
    ///
    /// @param checked whether a break, by key, is checked against `ExecutorServiceContract`
    /// @return the checks
    static Stream<DynamicNode> checks(final Predicate<String> checked) {
        Map<String, Break> breaks = new LinkedHashMap<>(ContractRuns.breaks(
                ContractRuns.withSuperclasses(BreakableExecutorService.class)));
        breaks.keySet().removeIf(checked.negate());
        List<Method> methods = ContractRuns.testMethods(ExecutorServiceContract.class, false);
        Stream<DynamicNode> passes = Stream.of(
                passes("newFixedThreadPool(2)", () -> new Subject<>(
                        ExecutorServiceProviders.fixedThreadPools(THREADS), true), methods),
                passes("newVirtualThreadPerTaskExecutor()", () -> new Subject<>(
                        ExecutorServiceProviders.virtualThreadPerTaskExecutors(), true), methods),
                passes("a new ForkJoinPool", () -> new Subject<>(ExecutorServiceProviders.forkJoinPools(), true),
                        methods),
                passes("a ThreadPoolExecutor with a DiscardPolicy, opted out", () -> new Subject<>(
                        ExecutorServiceProviders.of(ExecutorServiceSubjects::discarding), false), methods),
                passes("unbroken breakable", () -> broken(null), methods));
        Stream<DynamicNode> fails = breaks.entrySet().stream()
                .map(e -> fails(e.getKey(), () -> broken(e.getValue()), methods));
        return Stream.of(dynamicContainer("ExecutorServiceContract",
                Stream.of(passes, fails, namedChecks(breaks)).flatMap(n -> n)));
    }

    /// The checks of `ThreadSafeExecutorServiceContract`: it passes against the three JDK executors, the discarding
    /// `ThreadPoolExecutor`, opted out, and the unbroken breakable, and fails for each of the breakable's two
    /// thread-unsafe breaks. Each break is shown failing its own concurrency test, named in
    /// [#THREAD_SAFETY_TARGETS], and passing `ExecutorServiceContract`'s single-thread tests, so that the failure is
    /// the concurrency test's.
    ///
    /// @param checked whether a break, by key, is checked against `ThreadSafeExecutorServiceContract`
    /// @return the checks
    static Stream<DynamicNode> threadSafeChecks(final Predicate<String> checked) {
        List<Method> all = ContractRuns.testMethods(ThreadSafeExecutorServiceContract.class, true);
        Map<String, Break> breaks = new LinkedHashMap<>(ContractRuns.breaks(
                ContractRuns.withSuperclasses(BreakableExecutorService.class)));
        breaks.keySet().removeIf(checked.negate());
        assertEquals(THREAD_SAFETY_TARGETS.keySet(), Set.copyOf(breaks.values()),
                () -> "the thread-safety breaks of BreakableExecutorService: " + breaks.keySet());
        return Stream.of(dynamicContainer("ThreadSafeExecutorServiceContract", Stream.concat(
                Stream.of(passes("newFixedThreadPool(2)", () -> new ThreadSafeSubject<>(
                                ExecutorServiceProviders.fixedThreadPools(THREADS), true), all),
                        passes("newVirtualThreadPerTaskExecutor()", () -> new ThreadSafeSubject<>(
                                ExecutorServiceProviders.virtualThreadPerTaskExecutors(), true), all),
                        passes("a new ForkJoinPool", () -> new ThreadSafeSubject<>(
                                ExecutorServiceProviders.forkJoinPools(), true), all),
                        passes("a ThreadPoolExecutor with a DiscardPolicy, opted out", () -> new ThreadSafeSubject<>(
                                ExecutorServiceProviders.of(ExecutorServiceSubjects::discarding), false), all),
                        passes("unbroken BreakableExecutorService", () -> new ThreadSafeSubject<>(breakables(null),
                                true), all)),
                breaks.entrySet().stream().flatMap(e -> Stream.of(
                        fails(e.getKey() + " in " + THREAD_SAFETY_TARGETS.get(e.getValue()),
                                () -> new ThreadSafeSubject<>(breakables(e.getValue()), true), named(all,
                                        THREAD_SAFETY_TARGETS.get(e.getValue()))),
                        passes("ExecutorServiceContract's single-thread tests with " + e.getKey(),
                                () -> broken(e.getValue()),
                                ContractRuns.testMethods(ExecutorServiceContract.class, false)))))));
    }

    /// The checks by name: for each single-thread break, that the test it targets fails, that every such break has a
    /// target, and that every test method is one.
    ///
    /// @param breaks the single-thread breaks of `BreakableExecutorService`, by key
    /// @return the checks
    static Stream<DynamicNode> namedChecks(final Map<String, Break> breaks) {
        List<Method> methods = ContractRuns.testMethods(ExecutorServiceContract.class, false);
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
