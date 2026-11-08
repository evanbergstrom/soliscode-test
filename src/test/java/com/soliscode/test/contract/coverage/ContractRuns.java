package com.soliscode.test.contract.coverage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.support.AnnotationSupport;

import com.soliscode.test.annotations.ConcurrencyTest;
import com.soliscode.test.breakable.Break;

/// Runs every test method of a contract against one contract instance, outside JUnit, and reports which fail.
///
/// The contract coverage tests use it to show that a contract passes against a correct implementation and fails
/// against a breakable implementation with each of its breaks.
final class ContractRuns {

    /// How many times a concurrency test method is run, at most, before it is taken to pass: a race need not show
    /// on every run.
    static final int CONCURRENCY_ATTEMPTS = 5;

    /// How long a test method may run before it is reported as hanging.
    static final int TIMEOUT_SECONDS = 10;

    /// How long a concurrency test method may run: its contention runs have their own timeouts, several seconds
    /// each, before they report a failure.
    static final int CONCURRENCY_TIMEOUT_SECONDS = 60;

    private ContractRuns() { }

    /// Returns the test methods of a contract interface, including those it inherits, in a stable order.
    ///
    /// @param contract the contract interface
    /// @param includeConcurrency whether to include the methods annotated [ConcurrencyTest]
    /// @return the test methods
    static List<Method> testMethods(final Class<?> contract, final boolean includeConcurrency) {
        return Arrays.stream(contract.getMethods())
                .filter(m -> !Modifier.isStatic(m.getModifiers()) && m.getParameterCount() == 0)
                .filter(m -> AnnotationSupport.isAnnotated(m, Test.class)
                        || AnnotationSupport.isAnnotated(m, RepeatedTest.class))
                .filter(m -> includeConcurrency || !isConcurrencyTest(m))
                .sorted(Comparator.comparing(Method::getName))
                .toList();
    }

    /// Runs each test method against a new contract instance and returns a description of each failure.
    ///
    /// @param contract creates the contract instance for each test method
    /// @param methods the test methods to run
    /// @return one line per failing test method, empty if all pass
    static List<String> failures(final Supplier<?> contract, final List<Method> methods) {
        return failures(contract, methods, false);
    }

    /// Runs the test methods against new contract instances, as [#failures(Supplier, List)] does, and stops at the
    /// first failure when `firstOnly`: a check that the contract fails needs only one.
    ///
    /// @param contract creates the contract instance for each test method
    /// @param methods the test methods to run
    /// @param firstOnly whether to stop at the first failure
    /// @return one line per failing test method, empty if all pass
    static List<String> failures(final Supplier<?> contract, final List<Method> methods,
                                          final boolean firstOnly) {
        List<String> failures = new ArrayList<>();
        for (final Method method : methods) {
            if (firstOnly && !failures.isEmpty()) {
                break;
            }
            int attempts = isConcurrencyTest(method) ? CONCURRENCY_ATTEMPTS : 1;
            Throwable failure = null;
            for (int i = 0; i < attempts && failure == null; i++) {
                failure = run(contract.get(), method);
            }
            if (failure != null) {
                failures.add(method.getName() + ": " + failure);
            }
        }
        return failures;
    }

    /// Returns the breaks declared as public static final fields of the given classes, keyed by
    /// `SimpleClassName.FIELD`, in declaration order.
    ///
    /// @param classes the classes that declare the breaks
    /// @return the breaks
    static Map<String, Break> breaks(final List<Class<?>> classes) {
        Map<String, Break> breaks = new LinkedHashMap<>();
        for (final Class<?> c : classes) {
            for (final var field : c.getDeclaredFields()) {
                int mod = field.getModifiers();
                if (field.getType() == Break.class && Modifier.isStatic(mod) && Modifier.isPublic(mod)) {
                    try {
                        breaks.put(c.getSimpleName() + "." + field.getName(), (Break) field.get(null));
                    } catch (final IllegalAccessException e) {
                        throw new AssertionError(e);
                    }
                }
            }
        }
        return breaks;
    }

    /// Returns `breakable` and its superclasses that declare breaks, then `extra`.
    ///
    /// @param breakable the breakable class
    /// @param extra other classes whose breaks the breakable honours
    /// @return the classes, without repeats
    static List<Class<?>> withSuperclasses(final Class<?> breakable,
                                                    final Class<?>... extra) {
        List<Class<?>> classes = new ArrayList<>();
        for (Class<?> c = breakable; c != null && c != Object.class; c = c.getSuperclass()) {
            classes.addFirst(c);
        }
        for (final Class<?> c : extra) {
            if (!classes.contains(c)) {
                classes.add(c);
            }
        }
        return classes;
    }

    private static boolean isConcurrencyTest(final Method method) {
        return AnnotationSupport.isAnnotated(method, ConcurrencyTest.class);
    }

    /// Runs one test method, on a daemon thread so that one that does not end is reported, not waited for.
    private static Throwable run(final Object contract, final Method method) {
        FutureTask<Throwable> task = new FutureTask<>(() -> {
            try {
                method.invoke(contract);
                return null;
            } catch (final InvocationTargetException e) {
                return e.getCause();
            }
        });
        Thread thread = new Thread(task, "contract-run-" + method.getName());
        thread.setDaemon(true);
        thread.start();
        try {
            return task.get(isConcurrencyTest(method) ? CONCURRENCY_TIMEOUT_SECONDS : TIMEOUT_SECONDS,
                    TimeUnit.SECONDS);
        } catch (final TimeoutException e) {
            thread.interrupt();
            return new Hang(method);
        } catch (final ExecutionException e) {
            throw new AssertionError("cannot call " + method, e.getCause());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("interrupted while running " + method, e);
        }
    }

    /// Whether any of the failures is a test method that did not end.
    ///
    /// @param failures the failures, as [#failures] returns them
    /// @return whether one of them is a hang
    static boolean anyHang(final List<String> failures) {
        return failures.stream().anyMatch(f -> f.contains(Hang.class.getSimpleName()));
    }

    /// A test method that did not end within [#TIMEOUT_SECONDS]: the contract hangs, which is a fault in itself.
    static final class Hang extends AssertionError {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        Hang(final Method method) {
            super(method.getName() + " did not end in time");
        }
    }
}
