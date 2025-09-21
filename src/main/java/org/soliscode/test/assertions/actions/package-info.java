/*
 * Copyright 2024 Evan Bergstrom
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

/// Action classes that implement assertions on consumed elements in testing scenarios.
///
/// ## Overview
/// This package provides a comprehensive framework for testing methods that accept consumer actions
/// (such as [Consumer][java.util.function.Consumer] implementations). The classes in this package
/// allow developers to create sophisticated assertions about how consumer methods behave, including
/// what elements are consumed, how many times consumption occurs, and whether all expected elements
/// are processed.
///
/// ## Core Concepts
///
/// ### CheckableAction Interface
/// All assertion consumers in this package implement the [CheckableAction][org.soliscode.test.assertions.actions.CheckableAction]
/// interface, which provides the `assertCheck()` method for post-execution validation. These consumers
/// also extend [Consumer][java.util.function.Consumer], allowing them to be used anywhere a regular
/// {@code Consumer} is expected while providing additional assertion capabilities.
///
/// ### Assertion Actions
/// Assertion actions are special consumer implementations that validate specific behaviors:
///
/// - **Content Validation** - Verify what elements are consumed
/// - **Count Validation** - Verify how many elements are consumed
/// - **Completeness Validation** - Verify that all expected elements are consumed
/// - **Exclusion Validation** - Verify that no elements are consumed at all
///
/// ## Main Classes
///
/// ### Factory Class
/// - [AssertActions][org.soliscode.test.assertions.actions.AssertActions] - Factory class providing static methods to
///   create assertion consumers with various validation strategies.
///
/// ### Consumer Implementations
/// - [AssertConsumeOnly][org.soliscode.test.assertions.actions.AssertConsumeOnly] - Validates that only specific
///   values are consumed (allows partial consumption of the expected set)
/// - [AssertConsumeExactly][org.soliscode.test.assertions.actions.AssertConsumeExactly] - Validates that exactly the
///   specified set of values are consumed (requires complete consumption of all expected elements)
/// - [AssertConsumeCount][org.soliscode.test.assertions.actions.AssertConsumeCount] - Validates that a specific number
///   of elements are consumed (regardless of actual values)
/// - [AssertConsumeNone][org.soliscode.test.assertions.actions.AssertConsumeNone] - Validates that no elements are
///   consumed at all
///
/// ### Supporting Interface
/// - [CheckableAction][org.soliscode.test.assertions.actions.CheckableAction] - Base interface providing the
///   `assertCheck()` method for post-execution validation of assertion actions
///
/// ## Usage Patterns
///
/// ### Basic Usage
/// The typical usage pattern involves creating an assertion consumer, passing it to the method under test,
/// and then validating the results:
///
/// ```java
/// // Create assertion consumer
/// Consumer<Integer> consumer = AssertActions.consumeOnly(Set.of(1, 2, 3));
///
/// // Pass to method under test
/// collection.forEach(consumer);
///
/// // For some consumers, call assertCheck() if needed
/// if (consumer instanceof CheckableAction checkable) {
///     checkable.assertCheck(); // Only needed for certain consumers
/// }
/// ```
///
/// ### Testing Different Scenarios
///
/// #### Validating Consumed Content
/// ```java
/// // Test that only specific values are consumed
/// Consumer<String> consumer = AssertActions.consumeOnly(List.of("a", "b", "c"));
/// someMethod.forEach(consumer);
/// // Throws AssertionFailedError if unexpected values are consumed
/// ```
///
/// #### Validating Exact Consumption
/// ```java
/// // Test that exactly specified values are consumed (all of them, once each)
/// CheckableAction consumer = AssertActions.consumeExactly(Set.of(1, 2, 3));
/// collection.forEach((Consumer<Integer>) consumer);
/// consumer.assertCheck(); // Ensures all expected values were consumed
/// ```
///
/// #### Validating Consumption Count
/// ```java
/// // Test that exactly 5 elements are consumed (regardless of values)
/// CheckableAction consumer = AssertActions.consumeCount(5);
/// collection.forEach((Consumer<Object>) consumer);
/// consumer.assertCheck(); // Validates count matches expectation
/// ```
///
/// #### Validating No Consumption
/// ```java
/// // Test that no elements are consumed
/// Consumer<String> consumer = AssertActions.consumeNone();
/// emptyCollection.forEach(consumer);
/// // Throws AssertionFailedError immediately if any element is consumed
/// ```
///
/// ### Error Message Customization
/// Most assertion consumers support custom error messages:
///
/// ```java
/// // With custom message
/// CheckableAction consumer = AssertActions.consumeCount(3, "Expected exactly 3 items");
///
/// // With message supplier (lazy evaluation)
/// CheckableAction consumer = AssertActions.consumeCount(3,
///     () -> "Expected 3 items but got " + actualCount);
/// ```
///
/// ## Integration with Testing Frameworks
/// This package is designed to integrate seamlessly with JUnit 5 and other testing frameworks:
///
/// ```java
/// &#64;Test
/// void testForEachConsumesAllElements() {
///     List<Integer> data = List.of(1, 2, 3, 4, 5);
///     CheckableAction consumer = AssertActions.consumeExactly(data);
///
///     // Method under test
///     data.forEach((Consumer<Integer>) consumer);
///
///     // Validate all elements were consumed
///     assertDoesNotThrow(consumer::assertCheck);
/// }
///
/// &#64;Test
/// void testFilteredForEachConsumesOnlyValidElements() {
///     List<Integer> data = List.of(1, 2, 3, 4, 5);
///     Consumer<Integer> consumer = AssertActions.consumeOnly(List.of(2, 4));
///
///     // Method under test - should only consume even numbers
///     data.stream()
///         .filter(n -> n % 2 == 0)
///         .forEach(consumer);
/// }
/// ```
///
/// ## Advanced Usage
///
/// ### Testing Stream Operations
/// The assertion consumers work excellently with Java Streams:
///
/// ```java
/// // Test stream filtering
/// List<String> words = List.of("apple", "banana", "apricot", "cherry");
/// Consumer<String> consumer = AssertActions.consumeOnly(List.of("apple", "apricot"));
///
/// words.stream()
///     .filter(word -> word.startsWith("a"))
///     .forEach(consumer);
/// ```
///
/// ### Testing Parallel Operations
/// ```java
/// // Test parallel stream consumption
/// List<Integer> numbers = IntStream.range(1, 1000).boxed().collect(Collectors.toList());
/// CheckableAction consumer = AssertActions.consumeCount(999);
///
/// numbers.parallelStream().forEach((Consumer<Integer>) consumer);
/// consumer.assertCheck();
/// ```
///
/// ### Complex Assertion Scenarios
/// ```java
/// // Test that a method processes exactly the expected subset
/// Set<String> expectedProcessed = Set.of("valid1", "valid2", "valid3");
/// CheckableAction consumer = AssertActions.consumeExactly(expectedProcessed);
///
/// List<String> input = List.of("valid1", "invalid", "valid2", "valid3", "invalid2");
/// processor.processValidItems(input, (Consumer<String>) consumer);
///
/// consumer.assertCheck(); // Ensures exactly the valid items were processed
/// ```
///
/// ## Error Handling
/// All assertion consumers throw [AssertionFailedError][org.opentest4j.AssertionFailedError] when assertions fail,
/// which integrates properly with JUnit 5 and other testing frameworks. Error messages are
/// designed to be descriptive and include information about expected vs actual values where applicable.
///
/// ## Performance Considerations
/// The assertion consumers are optimized for testing scenarios and prioritize correctness and
/// clear error reporting over performance. They should not be used in production code where
/// performance is critical.
///
/// @author evanbergstrom
/// @since 1.0
/// @see org.soliscode.test.assertions.actions.AssertActions
/// @see org.soliscode.test.assertions.actions.CheckableAction
/// @see java.util.function.Consumer
/// @see org.junit.jupiter.api.Assertions
package org.soliscode.test.assertions.actions;
