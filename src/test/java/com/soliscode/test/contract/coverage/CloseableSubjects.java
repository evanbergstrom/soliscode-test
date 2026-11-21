/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.coverage;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DynamicNode;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.breakable.Break;
import com.soliscode.test.breakable.BreakableCloseable;
import com.soliscode.test.contract.autocloseable.AutoCloseableContract;
import com.soliscode.test.contract.closeable.CloseableContract;
import com.soliscode.test.provider.AutoCloseableProvider;
import com.soliscode.test.provider.AutoCloseableProviders;
import com.soliscode.test.provider.ResourceUse;

import static com.soliscode.test.breakable.BreakableCloseable.CLOSEABLE_READ_COUNT_THROWS_ONCE_CLOSED;
import static com.soliscode.test.breakable.BreakableCloseable.CLOSEABLE_READ_THROWS_WHILE_OPEN;
import static com.soliscode.test.breakable.BreakableCloseable.CLOSEABLE_READ_THROWS_WRONG_EXCEPTION_ONCE_CLOSED;
import static com.soliscode.test.breakable.BreakableCloseable.CLOSEABLE_READ_WORKS_ONCE_CLOSED;
import static com.soliscode.test.breakable.BreakableCloseable.CLOSE_DOES_NOTHING;
import static com.soliscode.test.breakable.BreakableCloseable.CLOSE_THROWS;
import static com.soliscode.test.breakable.BreakableCloseable.CLOSE_TWICE_REOPENS;
import static com.soliscode.test.breakable.BreakableCloseable.CLOSE_TWICE_THROWS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/// The subjects `ContractCoverageTest` runs `AutoCloseableContract` and `CloseableContract` against: a
/// `BufferedReader` over a `StringReader`, a `ByteArrayOutputStream`, an `ExecutorService` (`AutoCloseableContract`
/// only: it is not a `Closeable`), and `BreakableCloseable` with its `read()`, `readCount()` and `isClosed()` (#41).
///
/// Beyond the coverage test's own checks, it shows by name, for each contract, that the test each break targets fails
/// for it, that every test method is a target, and that the tests a break should leave alone pass with it.
final class CloseableSubjects {

    private static final String CLOSE = "closeOnAnOpenResourceCompletes";
    private static final String WHILE_OPEN = "usesWorkWhileOpen";
    private static final String THROW_ONCE_CLOSED = "usesThatThrowOnceClosedThrow";
    private static final String WORK_ONCE_CLOSED = "usesThatWorkOnceClosedWork";
    private static final String TWICE = "closeTwiceHasNoEffect";

    /// The test each break targets.
    private static final Map<Break, String> TARGETS = Map.of(
            CLOSE_THROWS, CLOSE,
            CLOSE_DOES_NOTHING, THROW_ONCE_CLOSED,
            CLOSE_TWICE_THROWS, TWICE,
            CLOSE_TWICE_REOPENS, TWICE,
            CLOSEABLE_READ_THROWS_WHILE_OPEN, WHILE_OPEN,
            CLOSEABLE_READ_WORKS_ONCE_CLOSED, THROW_ONCE_CLOSED,
            CLOSEABLE_READ_THROWS_WRONG_EXCEPTION_ONCE_CLOSED, THROW_ONCE_CLOSED,
            CLOSEABLE_READ_COUNT_THROWS_ONCE_CLOSED, WORK_ONCE_CLOSED);

    /// The tests a break should leave alone. `CLOSE_THROWS` keeps none: every test closes.
    private static final Map<Break, List<String>> KEPT = Map.of(
            CLOSE_DOES_NOTHING, List.of(CLOSE, WHILE_OPEN, WORK_ONCE_CLOSED),
            CLOSE_TWICE_THROWS, List.of(CLOSE, WHILE_OPEN, THROW_ONCE_CLOSED, WORK_ONCE_CLOSED),
            CLOSE_TWICE_REOPENS, List.of(CLOSE, WHILE_OPEN, THROW_ONCE_CLOSED, WORK_ONCE_CLOSED),
            CLOSEABLE_READ_THROWS_WHILE_OPEN, List.of(CLOSE, THROW_ONCE_CLOSED, WORK_ONCE_CLOSED, TWICE),
            CLOSEABLE_READ_WORKS_ONCE_CLOSED, List.of(CLOSE, WHILE_OPEN, WORK_ONCE_CLOSED),
            CLOSEABLE_READ_THROWS_WRONG_EXCEPTION_ONCE_CLOSED, List.of(CLOSE, WHILE_OPEN, WORK_ONCE_CLOSED),
            CLOSEABLE_READ_COUNT_THROWS_ONCE_CLOSED, List.of(CLOSE, WHILE_OPEN, THROW_ONCE_CLOSED));

    private CloseableSubjects() { }

    /// `AutoCloseableContract` on a provider, with the default claim of idempotence or opting out.
    static class Subject<C extends AutoCloseable> extends AbstractTest implements AutoCloseableContract<C> {
        private final AutoCloseableProvider<C> resources;
        private final boolean idempotent;

        Subject(final AutoCloseableProvider<C> resourcesArg, final boolean idempotentArg) {
            this.resources = resourcesArg;
            this.idempotent = idempotentArg;
        }

        @Override
        public AutoCloseableProvider<C> autoCloseableProvider() {
            return resources;
        }

        @Override
        public boolean closeIsIdempotent() {
            return idempotent;
        }
    }

    /// `CloseableContract` on a provider, with `closeIsIdempotent()` as given, which it should not read.
    static class CloseableSubject<C extends Closeable> extends AbstractTest implements CloseableContract<C> {
        private final AutoCloseableProvider<C> resources;
        private final boolean idempotent;

        CloseableSubject(final AutoCloseableProvider<C> resourcesArg, final boolean idempotentArg) {
            this.resources = resourcesArg;
            this.idempotent = idempotentArg;
        }

        @Override
        public AutoCloseableProvider<C> autoCloseableProvider() {
            return resources;
        }

        @Override
        public boolean closeIsIdempotent() {
            return idempotent;
        }
    }

    /// `ExecutorService`s of one thread. `submit` is refused once closed, and `isShutdown()` and `isTerminated()`
    /// work.
    static AutoCloseableProvider<ExecutorService> executorServices() {
        return AutoCloseableProviders.of(Executors::newSingleThreadExecutor,
                ResourceUse.throwsOnceClosed("submit(Runnable)", RejectedExecutionException.class,
                        (ExecutorService e) -> e.submit(() -> { })),
                ResourceUse.worksOnceClosed("isShutdown()", ExecutorService::isShutdown),
                ResourceUse.worksOnceClosed("isTerminated()", ExecutorService::isTerminated));
    }

    /// `BreakableCloseable`s with a break, or none: `read()` throws `IOException` once closed, and `readCount()` and
    /// `isClosed()` work.
    static AutoCloseableProvider<BreakableCloseable> breakables(final @Nullable Break b) {
        Set<Break> breaks = b == null ? Set.of() : Set.of(b);
        return AutoCloseableProviders.of(() -> new BreakableCloseable(breaks),
                ResourceUse.throwsOnceClosed("read()", IOException.class, BreakableCloseable::read),
                ResourceUse.worksOnceClosed("readCount()", BreakableCloseable::readCount),
                ResourceUse.worksOnceClosed("isClosed()", BreakableCloseable::isClosed));
    }

    /// `AutoCloseableContract` on `BreakableCloseable` with a break, or none.
    static Subject<BreakableCloseable> broken(final @Nullable Break b, final boolean idempotent) {
        return new Subject<>(breakables(b), idempotent);
    }

    /// `CloseableContract` on `BreakableCloseable` with a break, or none.
    static CloseableSubject<BreakableCloseable> brokenCloseable(final @Nullable Break b, final boolean idempotent) {
        return new CloseableSubject<>(breakables(b), idempotent);
    }

    /// The checks by name for a contract: for each break, that the test it targets fails, and that the tests it
    /// should leave alone pass. Every break of `BreakableCloseable` must have a target, and every test method must be
    /// one.
    ///
    /// @param contract the contract, `AutoCloseableContract` or `CloseableContract`
    /// @param broken   makes the contract on the breakable with a break
    /// @param breaks   the breakable's breaks, by key
    /// @return the checks
    static Stream<DynamicNode> namedChecks(final Class<?> contract, final Function<Break, Supplier<?>> broken,
                                           final Map<String, Break> breaks) {
        List<Method> methods = ContractRuns.testMethods(contract, false);
        Stream<DynamicNode> complete = Stream.of(dynamicTest(contract.getSimpleName()
                + ": every break has a target, and every test is one", () -> {
                    assertEquals(Set.copyOf(breaks.values()), TARGETS.keySet(), "the breaks with a target");
                    assertEquals(methods.stream().map(Method::getName).collect(Collectors.toSet()),
                            Set.copyOf(TARGETS.values()), "the tests some break targets");
                }));
        Stream<DynamicNode> targets = TARGETS.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(e -> dynamicTest(contract.getSimpleName() + ": " + e.getValue() + " fails with "
                        + e.getKey().description(), () ->
                        assertFalse(ContractRuns.failures(broken.apply(e.getKey()), named(methods, e.getValue()))
                                .isEmpty(), () -> e.getValue() + " passes with the break: it cannot see it")));
        Stream<DynamicNode> kept = KEPT.entrySet().stream().flatMap(e -> e.getValue().stream().map(test ->
                dynamicTest(contract.getSimpleName() + ": " + test + " passes with " + e.getKey().description(),
                        () -> {
                            List<String> failures = ContractRuns.failures(broken.apply(e.getKey()),
                                    named(methods, test));
                            assertTrue(failures.isEmpty(), () -> "the break should leave it alone, but:\n  "
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
