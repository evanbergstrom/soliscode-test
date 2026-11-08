package org.soliscode.test.annotations;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Timeout;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// **Concurrency Test Annotation**
///
/// This annotation is used to mark tests that specifically target thread-safety and
/// concurrency behavior. It is a composed annotation that combines several JUnit 5
/// features to provide a consistent environment for concurrency testing.
///
/// ## Features
/// - **Repeated Execution**: The test is repeated multiple times (defined by {@link TestTags#THREAD_SAFETY_REPEATS})
///   to increase the probability of uncovering race conditions or other timing-dependent bugs.
/// - **Timeout Guard**: A timeout is applied (defined by {@link TestTags#THREAD_SAFETY_TIMEOUT})
///   to prevent tests from hanging indefinitely in case of deadlocks.
/// - **Tagging**: Tests are tagged with {@link TestTags#CONCURRENCY} for easy filtering.
/// - **Nondeterministic**: Marked with {@link Nondeterministic} as results may vary between runs.
///
/// ## Usage
/// ```java
/// @ConcurrencyTest
/// void testConcurrentAccess() {
///     // ... code that tests thread safety ...
/// }
/// ```
///
/// @see org.junit.jupiter.api.RepeatedTest
/// @see org.junit.jupiter.api.Timeout
/// @see org.junit.jupiter.api.Tag
/// @see TestTags#CONCURRENCY
/// @since 1.0.0
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Tag(TestTags.CONCURRENCY)
@Nondeterministic
@Timeout(TestTags.THREAD_SAFETY_TIMEOUT)  // guard against deadlocks
@RepeatedTest(TestTags.THREAD_SAFETY_REPEATS)
public @interface ConcurrencyTest {
}
