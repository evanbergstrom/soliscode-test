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

/// **Specialized Assertion Framework for Advanced Testing Scenarios**
///
/// This package provides a comprehensive collection of assertion methods that extend and complement
/// the standard JUnit Jupiter assertion framework. The assertions are designed to fill specific
/// gaps in common testing scenarios and provide more specialized validation capabilities.
///
/// ## Core Design Principles
///
/// ### Extension, Not Replacement
/// These assertions complement rather than replace JUnit's standard assertions. They work
/// seamlessly alongside `org.junit.jupiter.api.Assertions` and follow the same design patterns
/// and naming conventions.
///
/// ### Consistent API Design
/// All assertions follow JUnit's established patterns:
/// - Multiple overloads for different parameter combinations
/// - Support for custom error messages (String)
/// - Support for lazy error messages (Supplier<String>)
/// - Consistent parameter ordering (expected, actual, message)
///
/// ### Type Safety and Performance
/// - Leverage Java's type system for compile-time safety
/// - Minimal runtime overhead with lazy evaluation
/// - Zero-cost abstractions for successful test cases
///
/// ## Assertion Categories
///
/// ### Exception Handling Assertions
/// **Package**: `org.soliscode.test.assertions`
///
/// - **`assertThrowsAny`**: Verifies that an executable throws one of several acceptable exception types
/// - **`assertThrowsDifferent`**: Verifies that an executable throws an exception other than prohibited types
///
/// ```java
/// // Test method accepts multiple valid exception types
/// assertThrowsAny(
///     List.of(IllegalArgumentException.class, NullPointerException.class),
///     () -> parseInput(invalidData)
/// );
///
/// // Test method doesn't throw a specific unwanted exception
/// assertThrowsDifferent(
///     UnsupportedOperationException.class,
///     () -> mutableOperation()
/// );
/// ```
///
/// ### Type and Interface Validation
/// **Package**: `org.soliscode.test.assertions`
///
/// - **`assertNotInstanceOf`**: Verifies an object is NOT an instance of a specific type
/// - **`assertImplementsOnly`**: Verifies an object implements only permitted interfaces
///
/// ```java
/// // Verify object avoids deprecated types
/// assertNotInstanceOf(DeprecatedInterface.class, modernImplementation);
///
/// // Verify minimal interface implementation
/// assertImplementsOnly(
///     List.of(Serializable.class, Comparable.class),
///     cleanDesignObject
/// );
/// ```
///
/// ### Comparative Assertions
/// **Package**: `org.soliscode.test.assertions`
///
/// - **`assertLessThan`**: Verifies first value < second value
/// - **`assertGreaterThan`**: Verifies first value > second value
/// - **`assertLessThanOrEqual`**: Verifies first value <= second value
/// - **`assertGreaterThanOrEqual`**: Verifies first value >= second value
///
/// ```java
/// // Performance and ordering validations
/// assertLessThan(startTime, endTime);
/// assertGreaterThan(performanceScore, minimumThreshold);
/// assertLessThanOrEqual(memoryUsage, memoryLimit);
/// assertGreaterThanOrEqual(availableSpace, requiredSpace);
/// ```
///
/// ### String Content Assertions
/// **Package**: `org.soliscode.test.assertions.string`
///
/// - **`assertStringContains`**: Verifies string contains specified substrings
/// - **`assertStringContainsInOrder`**: Verifies string contains substrings in specified order
///
/// ```java
/// // Log and output validation
/// assertStringContains("SUCCESS", operationLog);
/// assertStringContains(List.of("user", "authenticated"), securityLog);
///
/// // Process flow validation
/// assertStringContainsInOrder(
///     List.of("initialize", "process", "complete"),
///     workflowLog
/// );
/// ```
///
/// ### Collection-Specific Assertions
/// **Package**: `org.soliscode.test.assertions.collection`
///
/// Specialized assertions for testing collection behavior, equality semantics, and container operations.
/// These assertions understand the nuances of collection contracts and provide detailed validation
/// for collection implementations.
///
/// ### Action and Behavior Assertions
/// **Package**: `org.soliscode.test.assertions.actions`
///
/// Assertions for testing behavioral contracts, method invocations, and side effects. These are
/// particularly useful for testing that methods consume expected numbers of elements or perform
/// expected operations.
///
/// ## Integration Examples
///
/// ### Basic Usage with Static Imports
/// ```java
/// import static org.junit.jupiter.api.Assertions.*;
/// import static org.soliscode.test.assertions.Assertions.*;
///
/// @Test
/// void comprehensiveValidation() {
///     // Standard JUnit assertions
///     assertNotNull(result);
///     assertEquals(expectedValue, result.getValue());
///
///     // SolisCode specialized assertions
///     assertGreaterThan(result.getScore(), baseline);
///     assertImplementsOnly(List.of(MyInterface.class), result);
///
///     // Exception behavior validation
///     assertThrowsAny(
///         List.of(ValidationException.class, ProcessingException.class),
///         () -> result.processWithRiskyOperation()
///     );
/// }
/// ```
///
/// ### Custom Error Messages
/// ```java
/// @Test
/// void performanceValidation() {
///     long startTime = System.currentTimeMillis();
///     performOperation();
///     long endTime = System.currentTimeMillis();
///
///     assertLessThan(
///         endTime - startTime,
///         MAX_OPERATION_TIME,
///         "Operation exceeded maximum allowed time"
///     );
/// }
/// ```
///
/// ### Lazy Error Messages for Expensive Operations
/// ```java
/// @Test
/// void complexValidation() {
///     assertGreaterThan(
///         actualResult,
///         expectedResult,
///         () -> "Performance test failed: " +
///               "actual=" + actualResult +
///               ", expected=" + expectedResult +
///               ", system_state=" + getComplexSystemState()
///     );
/// }
/// ```
///
/// ## Package Structure
///
/// - **Root Package** (`org.soliscode.test.assertions`): Core utility class and fundamental assertions
/// - **Collection Package** (`org.soliscode.test.assertions.collection`): Collection-specific validations
/// - **Action Package** (`org.soliscode.test.assertions.actions`): Behavioral and side-effect assertions
/// - **String Package** (`org.soliscode.test.assertions.string`): String content and format assertions
///
/// ## Error Handling Philosophy
///
/// All assertions in this package follow these error handling principles:
///
/// - **Clear Error Messages**: Default error messages are descriptive and actionable
/// - **Context Preservation**: Error messages include relevant values and expected conditions
/// - **Lazy Evaluation**: Expensive error message construction is deferred until failure occurs
/// - **JUnit Integration**: All assertions throw `AssertionFailedError` for seamless test framework integration
///
/// ## Performance Characteristics
///
/// - **Zero overhead on success**: No object allocation or expensive operations for passing tests
/// - **Efficient failure paths**: Error construction optimized for debugging effectiveness
/// - **Minimal reflection**: Type checking uses efficient runtime operations
/// - **Lazy suppliers**: Message suppliers only evaluated on assertion failure
///
/// @author evanbergstrom
/// @since 1.0
/// @see org.junit.jupiter.api.Assertions
/// @see org.opentest4j.AssertionFailedError
package org.soliscode.test.assertions;
