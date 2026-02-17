package org.soliscode.test.annotations;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark tests that are inherently nondeterministic.
 * <p>
 * A test is considered nondeterministic if it may produce different results across different
 * executions despite no changes in the code or environment. This typically happens in tests
 * involving:
 * <ul>
 *     <li>Random data generation</li>
 *     <li>Timing-sensitive operations</li>
 *     <li>Concurrency and race conditions</li>
 *     <li>External resource dependency</li>
 * </ul>
 * <p>
 * This annotation applies the {@link TestTags#NONDETERMINISTIC} JUnit 5 tag, allowing these
 * tests to be filtered during test execution (e.g., to run them more frequently or to isolate
 * flaky tests).
 * <p>
 * This annotation is also automatically applied by {@link ConcurrencyTest}.
 *
 * @author evanbergstrom
 * @see TestTags#NONDETERMINISTIC
 * @see ConcurrencyTest
 * @since 1.0.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Tag(TestTags.NONDETERMINISTIC)
public @interface Nondeterministic {
}
