/// **Custom JUnit 5 Annotations for Specialized Testing**
///
/// This package provides a suite of custom JUnit 5 annotations designed to categorize, control, and enhance
/// the execution of tests within the SolisCode Test framework. These annotations provide a standardized
/// way to handle long-running tests, nondeterministic behavior, and concurrency testing.
///
/// ## Purpose
///
/// The annotations in this package serve several key purposes:
/// - **Categorization**: Grouping tests based on their performance characteristics or complexity.
/// - **CI/CD Control**: Enabling selective execution of tests in different environments (e.g., skipping slow tests in fast PR builds).
/// - **Behavioral Tagging**: Marking tests that are inherently nondeterministic or require special handling.
/// - **Concurrency Support**: Providing specialized support for testing thread-safe implementations under contention.
///
/// ## Annotation Categories
///
/// ### Performance Annotations
///
/// These annotations are used to tag tests based on their expected execution time. They map to standard JUnit 5
/// tags defined in {@link org.soliscode.test.annotations.TestTags}.
///
/// | Annotation | Description | Expected Execution Time |
/// | :--- | :--- | :--- |
/// | {@link org.soliscode.test.annotations.Slow} | Significant execution time | 100 ms – 1 s |
/// | {@link org.soliscode.test.annotations.VerySlow} | Very long execution time | 1 s – 10 s |
/// | {@link org.soliscode.test.annotations.ExtremelySlow} | Exceptional execution time | > 10 s |
///
/// ### Behavioral Annotations
///
/// - {@link org.soliscode.test.annotations.Nondeterministic}: Used to tag tests that may produce different results
///   across different executions despite no changes in the code or environment (e.g., tests involving random
///   data or timing-sensitive operations).
///
/// ### Concurrency Testing
///
/// - {@link org.soliscode.test.annotations.ConcurrencyTest}: A specialized composed annotation for testing
///   thread-safety and race conditions. It automatically applies {@link org.soliscode.test.annotations.Nondeterministic},
///   sets a timeout to guard against deadlocks, and repeats the test multiple times to increase the probability
///   of uncovering concurrency bugs.
///
/// ## Usage Examples
///
/// ### Using Performance Tags
///
/// ```java
/// @Test
/// @Slow
/// void testComplexCalculation() {
///     // This test will be tagged as "slow"
///     performExpensiveOperation();
/// }
/// ```
///
/// ### Concurrency Testing
///
/// ```java
/// @ConcurrencyTest
/// @DisplayName("Concurrent add operations should be thread-safe")
/// void testConcurrentAdd() {
///     // This test will be repeated multiple times with a timeout
///     runConcurrentAdders(collection);
/// }
/// ```
///
/// ## Thread Safety
///
/// The annotations themselves are stateless and thread-safe for use in concurrent test execution environments.
///
/// @see org.junit.jupiter.api.Tag
/// @see org.soliscode.test.annotations.TestTags
/// @since 1.0.0
package org.soliscode.test.annotations;
