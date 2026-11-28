/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.coverage;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DynamicNode;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.breakable.Break;
import com.soliscode.test.breakable.BreakableLock;
import com.soliscode.test.breakable.BreakableReadWriteLock;
import com.soliscode.test.contract.lock.LockContract;
import com.soliscode.test.contract.lock.ThreadSafeLockContract;
import com.soliscode.test.contract.readwritelock.ReadWriteLockContract;
import com.soliscode.test.contract.readwritelock.ThreadSafeReadWriteLockContract;
import com.soliscode.test.provider.LockProvider;
import com.soliscode.test.provider.LockProviders;
import com.soliscode.test.provider.ReadWriteLockProvider;
import com.soliscode.test.provider.ReadWriteLockProviders;

import static com.soliscode.test.breakable.BreakableLock.AWAIT_DOES_NOT_REACQUIRE_THE_LOCK;
import static com.soliscode.test.breakable.BreakableLock.AWAIT_DOES_NOT_RELEASE_THE_LOCK;
import static com.soliscode.test.breakable.BreakableLock.LOCK_DOES_NOT_ACQUIRE;
import static com.soliscode.test.breakable.BreakableLock.LOCK_DOES_NOT_WAIT;
import static com.soliscode.test.breakable.BreakableLock.LOCK_INTERRUPTIBLY_DEFERS_INTERRUPT_UNTIL_RELEASE;
import static com.soliscode.test.breakable.BreakableLock.LOCK_INTERRUPTIBLY_DOES_NOT_ACQUIRE;
import static com.soliscode.test.breakable.BreakableLock.LOCK_INTERRUPTIBLY_DOES_NOT_WAIT;
import static com.soliscode.test.breakable.BreakableLock.LOCK_INTERRUPTIBLY_IGNORES_INTERRUPT;
import static com.soliscode.test.breakable.BreakableLock.LOCK_INTERRUPTIBLY_IGNORES_INTERRUPT_WHILE_WAITING;
import static com.soliscode.test.breakable.BreakableLock.LOCK_INTERRUPTIBLY_KEEPS_THE_INTERRUPT_STATUS;
import static com.soliscode.test.breakable.BreakableLock.LOCK_INTERRUPTIBLY_RETURNS_WITHOUT_THE_LOCK_ON_INTERRUPT;
import static com.soliscode.test.breakable.BreakableLock.LOCK_INTERRUPTIBLY_TAKES_THE_LOCK_ON_INTERRUPT;
import static com.soliscode.test.breakable.BreakableLock.LOCK_INTERRUPTIBLY_THROWS_WHILE_HELD;
import static com.soliscode.test.breakable.BreakableLock.LOCK_IS_NOT_THREAD_SAFE;
import static com.soliscode.test.breakable.BreakableLock.NEW_CONDITION_IS_UNSUPPORTED;
import static com.soliscode.test.breakable.BreakableLock.NEW_CONDITION_RETURNS_THE_SAME_CONDITION;
import static com.soliscode.test.breakable.BreakableLock.SIGNAL_ALL_WAKES_ONE;
import static com.soliscode.test.breakable.BreakableLock.SIGNAL_DOES_NOTHING;
import static com.soliscode.test.breakable.BreakableLock.TIMED_TRY_LOCK_DEFERS_INTERRUPT_UNTIL_RELEASE;
import static com.soliscode.test.breakable.BreakableLock.TIMED_TRY_LOCK_DOES_NOT_WAIT;
import static com.soliscode.test.breakable.BreakableLock.TIMED_TRY_LOCK_FAILS_WITH_ZERO_TIMEOUT;
import static com.soliscode.test.breakable.BreakableLock.TIMED_TRY_LOCK_IGNORES_INTERRUPT;
import static com.soliscode.test.breakable.BreakableLock.TIMED_TRY_LOCK_IGNORES_INTERRUPT_WHILE_WAITING;
import static com.soliscode.test.breakable.BreakableLock.TIMED_TRY_LOCK_KEEPS_THE_INTERRUPT_STATUS;
import static com.soliscode.test.breakable.BreakableLock.TIMED_TRY_LOCK_MISSES_A_RELEASE;
import static com.soliscode.test.breakable.BreakableLock.TIMED_TRY_LOCK_RETURNS_FALSE_ON_INTERRUPT;
import static com.soliscode.test.breakable.BreakableLock.TIMED_TRY_LOCK_TAKES_THE_LOCK_ON_INTERRUPT;
import static com.soliscode.test.breakable.BreakableLock.TIMED_TRY_LOCK_THROWS_WHILE_HELD;
import static com.soliscode.test.breakable.BreakableLock.TRY_LOCK_ACQUIRES_A_HELD_LOCK;
import static com.soliscode.test.breakable.BreakableLock.TRY_LOCK_RETURNS_FALSE_WHEN_FREE;
import static com.soliscode.test.breakable.BreakableLock.TRY_LOCK_WAITS_FOR_THE_HOLDER;
import static com.soliscode.test.breakable.BreakableLock.UNLOCK_DOES_NOT_RELEASE;
import static com.soliscode.test.breakable.BreakableLock.UNLOCK_DOES_NOT_WAKE_A_WAITER;
import static com.soliscode.test.breakable.BreakableReadWriteLock.READ_LOCK_IGNORES_THE_WRITE_LOCK;
import static com.soliscode.test.breakable.BreakableReadWriteLock.READ_LOCK_IS_EXCLUSIVE;
import static com.soliscode.test.breakable.BreakableReadWriteLock.READ_LOCK_IS_NOT_THREAD_SAFE;
import static com.soliscode.test.breakable.BreakableReadWriteLock.WRITE_LOCK_IGNORES_READERS;
import static com.soliscode.test.breakable.BreakableReadWriteLock.WRITE_LOCK_IS_NOT_THREAD_SAFE;
import static com.soliscode.test.breakable.BreakableReadWriteLock.WRITE_LOCK_IS_SHARED;
import static com.soliscode.test.contract.coverage.ContractRuns.fails;
import static com.soliscode.test.contract.coverage.ContractRuns.passes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/// The subjects `ContractCoverageTest` runs `LockContract`, `ReadWriteLockContract` and their thread-safety contracts
/// against: `ReentrantLock`, `ReentrantReadWriteLock`, the views of `StampedLock`, and `BreakableLock` and
/// `BreakableReadWriteLock` with a break or none (#46).
///
/// Beyond passing and failing, it shows by name that the test each single-thread break targets fails for it, that
/// every such break has a target, and that every test method is one; and that the opt-outs' paths can fail
/// ([#optOutChecks]). The thread-unsafe breaks are left out of the single-thread checks, as
/// `UncheckedReasons.uncheckedReason` leaves them out; [#threadSafeLockChecks] and [#threadSafeReadWriteLockChecks]
/// show each failing its own concurrency test.
final class LockSubjects {

    /// The test of `LockContract` each single-thread break of `BreakableLock` targets.
    private static final Map<Break, String> LOCK_TARGETS = Map.ofEntries(
            Map.entry(LOCK_DOES_NOT_ACQUIRE, "lockAcquiresTheLock"),
            Map.entry(LOCK_DOES_NOT_WAIT, "lockWaitsForTheHolder"),
            Map.entry(LOCK_INTERRUPTIBLY_DOES_NOT_ACQUIRE, "lockInterruptiblyAcquiresTheLock"),
            Map.entry(LOCK_INTERRUPTIBLY_DOES_NOT_WAIT, "lockInterruptiblyWaitsForTheHolder"),
            Map.entry(LOCK_INTERRUPTIBLY_IGNORES_INTERRUPT,
                    "lockInterruptiblyThrowsInterruptedExceptionWhenInterruptedOnEntry"),
            Map.entry(LOCK_INTERRUPTIBLY_KEEPS_THE_INTERRUPT_STATUS,
                    "lockInterruptiblyThrowsInterruptedExceptionWhenInterruptedOnEntry"),
            Map.entry(LOCK_INTERRUPTIBLY_IGNORES_INTERRUPT_WHILE_WAITING,
                    "lockInterruptiblyThrowsInterruptedExceptionWhenInterruptedWhileWaiting"),
            Map.entry(LOCK_INTERRUPTIBLY_RETURNS_WITHOUT_THE_LOCK_ON_INTERRUPT,
                    "lockInterruptiblyThrowsInterruptedExceptionWhenInterruptedWhileWaiting"),
            Map.entry(LOCK_INTERRUPTIBLY_DEFERS_INTERRUPT_UNTIL_RELEASE,
                    "lockInterruptiblyThrowsInterruptedExceptionWhenInterruptedWhileWaiting"),
            Map.entry(LOCK_INTERRUPTIBLY_THROWS_WHILE_HELD,
                    "lockInterruptiblyThrowsInterruptedExceptionWhenInterruptedWhileWaiting"),
            Map.entry(LOCK_INTERRUPTIBLY_TAKES_THE_LOCK_ON_INTERRUPT,
                    "lockInterruptiblyThrowsInterruptedExceptionWhenInterruptedWhileWaiting"),
            Map.entry(TRY_LOCK_RETURNS_FALSE_WHEN_FREE, "tryLockOfAFreeLockAcquiresIt"),
            Map.entry(TRY_LOCK_ACQUIRES_A_HELD_LOCK, "tryLockOfAHeldLockReturnsFalseAtOnce"),
            Map.entry(TRY_LOCK_WAITS_FOR_THE_HOLDER, "tryLockOfAHeldLockReturnsFalseAtOnce"),
            Map.entry(TIMED_TRY_LOCK_FAILS_WITH_ZERO_TIMEOUT, "timedTryLockOfAFreeLockAcquiresItAtOnce"),
            Map.entry(TIMED_TRY_LOCK_DOES_NOT_WAIT, "timedTryLockTimesOutWhileHeld"),
            Map.entry(TIMED_TRY_LOCK_MISSES_A_RELEASE, "timedTryLockWaitsForTheHolder"),
            Map.entry(TIMED_TRY_LOCK_IGNORES_INTERRUPT, "timedTryLockThrowsInterruptedExceptionWhenInterruptedOnEntry"),
            Map.entry(TIMED_TRY_LOCK_KEEPS_THE_INTERRUPT_STATUS,
                    "timedTryLockThrowsInterruptedExceptionWhenInterruptedOnEntry"),
            Map.entry(TIMED_TRY_LOCK_IGNORES_INTERRUPT_WHILE_WAITING,
                    "timedTryLockThrowsInterruptedExceptionWhenInterruptedWhileWaiting"),
            Map.entry(TIMED_TRY_LOCK_RETURNS_FALSE_ON_INTERRUPT,
                    "timedTryLockThrowsInterruptedExceptionWhenInterruptedWhileWaiting"),
            Map.entry(TIMED_TRY_LOCK_DEFERS_INTERRUPT_UNTIL_RELEASE,
                    "timedTryLockThrowsInterruptedExceptionWhenInterruptedWhileWaiting"),
            Map.entry(TIMED_TRY_LOCK_THROWS_WHILE_HELD,
                    "timedTryLockThrowsInterruptedExceptionWhenInterruptedWhileWaiting"),
            Map.entry(TIMED_TRY_LOCK_TAKES_THE_LOCK_ON_INTERRUPT,
                    "timedTryLockThrowsInterruptedExceptionWhenInterruptedWhileWaiting"),
            Map.entry(UNLOCK_DOES_NOT_RELEASE, "unlockReleasesTheLock"),
            Map.entry(UNLOCK_DOES_NOT_WAKE_A_WAITER, "unlockWakesAWaitingThread"),
            Map.entry(NEW_CONDITION_RETURNS_THE_SAME_CONDITION, "newConditionReturnsANewCondition"),
            Map.entry(NEW_CONDITION_IS_UNSUPPORTED, "newConditionReturnsANewCondition"),
            Map.entry(AWAIT_DOES_NOT_RELEASE_THE_LOCK, "awaitReleasesTheLock"),
            Map.entry(AWAIT_DOES_NOT_REACQUIRE_THE_LOCK, "awaitReacquiresTheLockBeforeReturning"),
            Map.entry(SIGNAL_DOES_NOTHING, "signalWakesAWaitingThread"),
            Map.entry(SIGNAL_ALL_WAKES_ONE, "signalAllWakesEveryWaitingThread"));

    /// The test of `ReadWriteLockContract` each single-thread break of `BreakableReadWriteLock` targets.
    private static final Map<Break, String> READ_WRITE_TARGETS = Map.of(
            READ_LOCK_IS_EXCLUSIVE, "readLockIsShared",
            READ_LOCK_IGNORES_THE_WRITE_LOCK, "readLockWaitsForTheWriter",
            WRITE_LOCK_IS_SHARED, "writeLockIsExclusive",
            WRITE_LOCK_IGNORES_READERS, "writeLockWaitsForReaders");

    /// The concurrency test each thread-unsafe break targets.
    private static final Map<Break, String> THREAD_SAFETY_TARGETS = Map.of(
            LOCK_IS_NOT_THREAD_SAFE, "lockUnderContentionIsMutuallyExclusive",
            WRITE_LOCK_IS_NOT_THREAD_SAFE, "readWriteLockUnderContentionIsSafe",
            READ_LOCK_IS_NOT_THREAD_SAFE, "readWriteLockUnderContentionIsSafe");

    /// The two while-waiting interrupt tests, which the `interruptsLockAcquisition()` opt-out changes.
    private static final List<String> WHILE_WAITING_TESTS = List.of(
            "lockInterruptiblyThrowsInterruptedExceptionWhenInterruptedWhileWaiting",
            "timedTryLockThrowsInterruptedExceptionWhenInterruptedWhileWaiting");

    private LockSubjects() { }

    /// `LockContract` on a provider, with what the class under test supports.
    static class Subject<L extends Lock> extends AbstractTest implements LockContract<L> {
        private final LockProvider<L> locks;
        private final boolean conditions;
        private final boolean interruptible;

        Subject(final LockProvider<L> locksArg, final boolean conditionsArg, final boolean interruptibleArg) {
            this.locks = locksArg;
            this.conditions = conditionsArg;
            this.interruptible = interruptibleArg;
        }

        @Override
        public LockProvider<L> lockProvider() {
            return locks;
        }

        @Override
        public boolean supportsConditions() {
            return conditions;
        }

        @Override
        public boolean interruptsLockAcquisition() {
            return interruptible;
        }
    }

    /// `ThreadSafeLockContract` on a provider, with what the class under test supports.
    static class ThreadSafeSubject<L extends Lock> extends AbstractTest implements ThreadSafeLockContract<L> {
        private final LockProvider<L> locks;
        private final boolean conditions;

        ThreadSafeSubject(final LockProvider<L> locksArg, final boolean conditionsArg) {
            this.locks = locksArg;
            this.conditions = conditionsArg;
        }

        @Override
        public LockProvider<L> lockProvider() {
            return locks;
        }

        @Override
        public boolean supportsConditions() {
            return conditions;
        }
    }

    /// `ReadWriteLockContract` on a provider.
    static class ReadWriteSubject<L extends ReadWriteLock> extends AbstractTest implements ReadWriteLockContract<L> {
        private final ReadWriteLockProvider<L> locks;

        ReadWriteSubject(final ReadWriteLockProvider<L> locksArg) {
            this.locks = locksArg;
        }

        @Override
        public ReadWriteLockProvider<L> readWriteLockProvider() {
            return locks;
        }
    }

    /// `ThreadSafeReadWriteLockContract` on a provider.
    static class ThreadSafeReadWriteSubject<L extends ReadWriteLock> extends AbstractTest
            implements ThreadSafeReadWriteLockContract<L> {
        private final ReadWriteLockProvider<L> locks;

        ThreadSafeReadWriteSubject(final ReadWriteLockProvider<L> locksArg) {
            this.locks = locksArg;
        }

        @Override
        public ReadWriteLockProvider<L> readWriteLockProvider() {
            return locks;
        }
    }

    /// The `Lock`s `LockContract` passes against, by name, with whether each supports conditions: `ReentrantLock`
    /// (fair and not), the write locks of `ReentrantReadWriteLock`, of `StampedLock.asReadWriteLock()` and of an
    /// unbroken `BreakableReadWriteLock`, `StampedLock.asWriteLock()`, and an unbroken `BreakableLock`.
    private static Map<String, Map.Entry<LockProvider<? extends Lock>, Boolean>> passingLocks() {
        Map<String, Map.Entry<LockProvider<? extends Lock>, Boolean>> locks = new LinkedHashMap<>();
        locks.put("ReentrantLock", Map.entry(LockProviders.reentrantLocks(), true));
        locks.put("a fair ReentrantLock", Map.entry(LockProviders.fairReentrantLocks(), true));
        locks.put("ReentrantReadWriteLock.writeLock()",
                Map.entry(LockProviders.writeLocksOf(ReentrantReadWriteLock::new), true));
        locks.put("StampedLock.asReadWriteLock().writeLock(), without conditions",
                Map.entry(LockProviders.writeLocksOf(() -> new StampedLock().asReadWriteLock()), false));
        locks.put("StampedLock.asWriteLock(), without conditions", Map.entry(LockProviders.stampedWriteLocks(), false));
        locks.put("BreakableReadWriteLock.writeLock(), unbroken, without conditions",
                Map.entry(LockProviders.writeLocksOf(() -> new BreakableReadWriteLock(Set.of())), false));
        locks.put("unbroken BreakableLock", Map.entry(breakables(null), true));
        return locks;
    }

    /// The `ReadWriteLock`s `ReadWriteLockContract` passes against, by name.
    private static Map<String, ReadWriteLockProvider<? extends ReadWriteLock>> passingReadWriteLocks() {
        Map<String, ReadWriteLockProvider<? extends ReadWriteLock>> locks = new LinkedHashMap<>();
        locks.put("ReentrantReadWriteLock", ReadWriteLockProviders.reentrantReadWriteLocks());
        locks.put("a fair ReentrantReadWriteLock", ReadWriteLockProviders.fairReentrantReadWriteLocks());
        locks.put("StampedLock.asReadWriteLock()", ReadWriteLockProviders.stampedReadWriteLocks());
        locks.put("unbroken BreakableReadWriteLock", readWriteBreakables(null));
        return locks;
    }

    /// `BreakableLock`s with a break, or none.
    static LockProvider<BreakableLock> breakables(final @Nullable Break b) {
        Set<Break> breaks = b == null ? Set.of() : Set.of(b);
        return () -> new BreakableLock(breaks);
    }

    /// `BreakableReadWriteLock`s with a break, or none.
    static ReadWriteLockProvider<BreakableReadWriteLock> readWriteBreakables(final @Nullable Break b) {
        Set<Break> breaks = b == null ? Set.of() : Set.of(b);
        return () -> new BreakableReadWriteLock(breaks);
    }

    /// `LockContract` on `BreakableLock` with a break, or none.
    static Subject<BreakableLock> broken(final @Nullable Break b) {
        return new Subject<>(breakables(b), true, true);
    }

    /// `ReadWriteLockContract` on `BreakableReadWriteLock` with a break, or none.
    static ReadWriteSubject<BreakableReadWriteLock> brokenReadWrite(final @Nullable Break b) {
        return new ReadWriteSubject<>(readWriteBreakables(b));
    }

    /// The checks of `LockContract`: it passes against each lock of [#passingLocks()], fails for each break it
    /// checks, each pinned to its target ([#namedChecks]), and its opt-outs' paths can fail ([#optOutChecks]).
    ///
    /// @param checked whether a break, by key, is checked against `LockContract`
    /// @return the checks
    static Stream<DynamicNode> lockChecks(final Predicate<String> checked) {
        Map<String, Break> breaks = checkedBreaks(BreakableLock.class, checked);
        List<Method> methods = ContractRuns.testMethods(LockContract.class, false);
        Stream<DynamicNode> passes = passingLocks().entrySet().stream().map(e -> passes(e.getKey(),
                () -> new Subject<>(e.getValue().getKey(), e.getValue().getValue(), true), methods));
        Stream<DynamicNode> fails = breaks.entrySet().stream()
                .map(e -> fails(e.getKey(), () -> broken(e.getValue()), methods));
        return Stream.of(dynamicContainer("LockContract", Stream.of(passes, fails,
                namedChecks(LockContract.class, LOCK_TARGETS, breaks, LockSubjects::broken), optOutChecks())
                .flatMap(n -> n)));
    }

    /// The checks of `ReadWriteLockContract`: it passes against each lock of [#passingReadWriteLocks()], fails for
    /// each break it checks, each pinned to its target.
    ///
    /// @param checked whether a break, by key, is checked against `ReadWriteLockContract`
    /// @return the checks
    static Stream<DynamicNode> readWriteLockChecks(final Predicate<String> checked) {
        Map<String, Break> breaks = checkedBreaks(BreakableReadWriteLock.class, checked);
        List<Method> methods = ContractRuns.testMethods(ReadWriteLockContract.class, false);
        Stream<DynamicNode> passes = passingReadWriteLocks().entrySet().stream()
                .map(e -> passes(e.getKey(), () -> new ReadWriteSubject<>(e.getValue()), methods));
        Stream<DynamicNode> fails = breaks.entrySet().stream()
                .map(e -> fails(e.getKey(), () -> brokenReadWrite(e.getValue()), methods));
        return Stream.of(dynamicContainer("ReadWriteLockContract", Stream.of(passes, fails,
                namedChecks(ReadWriteLockContract.class, READ_WRITE_TARGETS, breaks, LockSubjects::brokenReadWrite))
                .flatMap(n -> n)));
    }

    /// The checks that the opt-outs' paths can fail: with `supportsConditions()` `false`, the unbroken
    /// `BreakableLock`, whose `newCondition()` works, fails `newConditionReturnsANewCondition`; with
    /// `interruptsLockAcquisition()` `false`, the while-waiting tests fail for the breaks that return without the lock
    /// on an interrupt, and pass against the unbroken breakable and the breaks that ignore the interrupt, which the
    /// looser rule allows.
    ///
    /// @return the checks
    static Stream<DynamicNode> optOutChecks() {
        List<Method> whileWaiting = ContractRuns.named(LockContract.class, WHILE_WAITING_TESTS.toArray(String[]::new));
        Supplier<Subject<BreakableLock>> withoutConditions = () -> new Subject<>(breakables(null), false, true);
        return Stream.of(dynamicContainer("opt-outs", Stream.of(
                fails("supportsConditions() false, against a lock whose conditions work", withoutConditions,
                        ContractRuns.named(LockContract.class, "newConditionReturnsANewCondition")),
                passes("interruptsLockAcquisition() false, unbroken BreakableLock",
                        () -> new Subject<>(breakables(null), true, false), whileWaiting),
                passes("interruptsLockAcquisition() false, with "
                                + LOCK_INTERRUPTIBLY_IGNORES_INTERRUPT_WHILE_WAITING.description(),
                        () -> new Subject<>(breakables(LOCK_INTERRUPTIBLY_IGNORES_INTERRUPT_WHILE_WAITING), true,
                                false), whileWaiting),
                passes("interruptsLockAcquisition() false, with "
                                + TIMED_TRY_LOCK_IGNORES_INTERRUPT_WHILE_WAITING.description(),
                        () -> new Subject<>(breakables(TIMED_TRY_LOCK_IGNORES_INTERRUPT_WHILE_WAITING), true,
                                false), whileWaiting),
                fails("interruptsLockAcquisition() false, with "
                                + LOCK_INTERRUPTIBLY_RETURNS_WITHOUT_THE_LOCK_ON_INTERRUPT.description(),
                        () -> new Subject<>(breakables(LOCK_INTERRUPTIBLY_RETURNS_WITHOUT_THE_LOCK_ON_INTERRUPT), true,
                                false), ContractRuns.named(LockContract.class, WHILE_WAITING_TESTS.get(0))),
                fails("interruptsLockAcquisition() false, with "
                                + TIMED_TRY_LOCK_RETURNS_FALSE_ON_INTERRUPT.description(),
                        () -> new Subject<>(breakables(TIMED_TRY_LOCK_RETURNS_FALSE_ON_INTERRUPT), true, false),
                        ContractRuns.named(LockContract.class, WHILE_WAITING_TESTS.get(1))),
                fails("interruptsLockAcquisition() false, with "
                                + LOCK_INTERRUPTIBLY_TAKES_THE_LOCK_ON_INTERRUPT.description(),
                        () -> new Subject<>(breakables(LOCK_INTERRUPTIBLY_TAKES_THE_LOCK_ON_INTERRUPT), true, false),
                        ContractRuns.named(LockContract.class, WHILE_WAITING_TESTS.get(0))),
                fails("interruptsLockAcquisition() false, with "
                                + TIMED_TRY_LOCK_TAKES_THE_LOCK_ON_INTERRUPT.description(),
                        () -> new Subject<>(breakables(TIMED_TRY_LOCK_TAKES_THE_LOCK_ON_INTERRUPT), true, false),
                        ContractRuns.named(LockContract.class, WHILE_WAITING_TESTS.get(1))))));
    }

    /// The checks of `ThreadSafeLockContract`: it passes against each lock of [#passingLocks()], and
    /// `LOCK_IS_NOT_THREAD_SAFE` fails its concurrency test and passes `LockContract`'s single-thread tests.
    ///
    /// @param checked whether a break, by key, is checked against `ThreadSafeLockContract`
    /// @return the checks
    static Stream<DynamicNode> threadSafeLockChecks(final Predicate<String> checked) {
        List<Method> all = ContractRuns.testMethods(ThreadSafeLockContract.class, true);
        Map<String, Break> breaks = checkedBreaks(BreakableLock.class, checked);
        assertEquals(Set.of(LOCK_IS_NOT_THREAD_SAFE), Set.copyOf(breaks.values()),
                () -> "the thread-safety breaks of BreakableLock: " + breaks.keySet());
        Stream<DynamicNode> passes = passingLocks().entrySet().stream().map(e -> passes(e.getKey(),
                () -> new ThreadSafeSubject<>(e.getValue().getKey(), e.getValue().getValue()), all));
        Stream<DynamicNode> fails = breaks.entrySet().stream().flatMap(e -> Stream.of(
                fails(e.getKey() + " in " + THREAD_SAFETY_TARGETS.get(e.getValue()),
                        () -> new ThreadSafeSubject<>(breakables(e.getValue()), true),
                        named(all, THREAD_SAFETY_TARGETS.get(e.getValue()))),
                passes("LockContract's single-thread tests with " + e.getKey(), () -> broken(e.getValue()),
                        ContractRuns.testMethods(LockContract.class, false))));
        return Stream.of(dynamicContainer("ThreadSafeLockContract", Stream.concat(passes, fails)));
    }

    /// The checks of `ThreadSafeReadWriteLockContract`: it passes against each lock of [#passingReadWriteLocks()],
    /// and each thread-unsafe break of `BreakableReadWriteLock` fails its concurrency test and passes
    /// `ReadWriteLockContract`'s single-thread tests.
    ///
    /// @param checked whether a break, by key, is checked against `ThreadSafeReadWriteLockContract`
    /// @return the checks
    static Stream<DynamicNode> threadSafeReadWriteLockChecks(final Predicate<String> checked) {
        List<Method> all = ContractRuns.testMethods(ThreadSafeReadWriteLockContract.class, true);
        Map<String, Break> breaks = checkedBreaks(BreakableReadWriteLock.class, checked);
        assertEquals(Set.of(WRITE_LOCK_IS_NOT_THREAD_SAFE, READ_LOCK_IS_NOT_THREAD_SAFE),
                Set.copyOf(breaks.values()), () -> "the thread-safety breaks of BreakableReadWriteLock: "
                        + breaks.keySet());
        Stream<DynamicNode> passes = passingReadWriteLocks().entrySet().stream()
                .map(e -> passes(e.getKey(), () -> new ThreadSafeReadWriteSubject<>(e.getValue()), all));
        Stream<DynamicNode> fails = breaks.entrySet().stream().flatMap(e -> Stream.of(
                fails(e.getKey() + " in " + THREAD_SAFETY_TARGETS.get(e.getValue()),
                        () -> new ThreadSafeReadWriteSubject<>(readWriteBreakables(e.getValue())),
                        named(all, THREAD_SAFETY_TARGETS.get(e.getValue()))),
                passes("ReadWriteLockContract's single-thread tests with " + e.getKey(),
                        () -> brokenReadWrite(e.getValue()),
                        ContractRuns.testMethods(ReadWriteLockContract.class, false))));
        return Stream.of(dynamicContainer("ThreadSafeReadWriteLockContract", Stream.concat(passes, fails)));
    }

    /// The checks by name: for each single-thread break, that the test it targets fails, that every such break has a
    /// target, and that every test method is one.
    private static Stream<DynamicNode> namedChecks(final Class<?> contract, final Map<Break, String> targets,
                                                   final Map<String, Break> breaks,
                                                   final java.util.function.Function<Break, ?> broken) {
        List<Method> methods = ContractRuns.testMethods(contract, false);
        Stream<DynamicNode> complete = Stream.of(dynamicTest("every break has a target, and every test is one",
                () -> {
                    assertEquals(Set.copyOf(breaks.values()), targets.keySet(), "the breaks with a target");
                    assertEquals(methods.stream().map(Method::getName).collect(Collectors.toSet()),
                            Set.copyOf(targets.values()), "the tests some break targets");
                }));
        Stream<DynamicNode> named = targets.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(e -> dynamicTest(e.getValue() + " fails with " + e.getKey().description(), () ->
                        assertFalse(ContractRuns.failures(() -> broken.apply(e.getKey()), named(methods,
                                e.getValue())).isEmpty(), () -> e.getValue() + " passes with the break: it cannot "
                                + "see it")));
        return Stream.concat(complete, named);
    }

    /// The breaks of a breakable that a contract checks, by key.
    private static Map<String, Break> checkedBreaks(final Class<?> breakable, final Predicate<String> checked) {
        Map<String, Break> breaks = new LinkedHashMap<>(ContractRuns.breaks(List.of(breakable)));
        breaks.keySet().removeIf(checked.negate());
        return breaks;
    }

    private static List<Method> named(final List<Method> methods, final String name) {
        List<Method> named = methods.stream().filter(m -> m.getName().equals(name)).toList();
        assertEquals(1, named.size(), () -> "no test method " + name);
        return named;
    }
}
