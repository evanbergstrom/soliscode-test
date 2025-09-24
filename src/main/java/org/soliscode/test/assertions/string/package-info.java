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

/// **String-Specific Assertion Utilities**
///
/// This package provides specialized assertion methods for string content validation that extend
/// beyond the basic string assertions available in standard testing frameworks. These utilities
/// focus on advanced string matching scenarios including containment checking, ordered substring
/// validation, and case-insensitive string analysis.
///
/// ## Package Overview
///
/// The string assertion package addresses common testing scenarios where standard string equality
/// or simple containment checks are insufficient. It provides precise control over how strings
/// are validated, with support for complex matching patterns that are essential for testing
/// real-world applications.
///
/// ### Core Capabilities
///
/// - **Substring containment validation** with flexible matching options
/// - **Ordered substring sequence checking** for workflow and process validation
/// - **Case-sensitive and case-insensitive matching** for different validation needs
/// - **Comprehensive error reporting** with detailed failure diagnostics
/// - **Performance-optimized algorithms** for efficient string processing
///
/// ## Assertion Classes
///
/// ### AssertStringContains
/// **Purpose**: Validates that a string contains all expected substrings from a list
///
/// **Key Features**:
/// - Case-sensitive and case-insensitive matching modes
/// - Multiple substring validation in a single assertion
/// - Flexible error message handling (direct, custom, and lazy-evaluated)
/// - Optimized for scenarios where substring order doesn't matter
///
/// **Common Use Cases**:
/// - Log file validation for required error indicators
/// - API response verification for mandatory fields
/// - Configuration file validation for required parameters
/// - User input validation for expected content
///
/// ### AssertStringContainsInOrder
/// **Purpose**: Validates that a string contains expected substrings in a specific sequential order
///
/// **Key Features**:
/// - Maintains position tracking to ensure proper ordering
/// - Case-sensitive and case-insensitive matching modes
/// - Advanced error reporting for sequence validation failures
/// - Optimized algorithm that advances through the string efficiently
///
/// **Common Use Cases**:
/// - Workflow and process validation (startup sequences, state transitions)
/// - API call sequence verification
/// - Build and deployment pipeline validation
/// - User interaction flow testing
/// - Database transaction sequence checking
///
/// ## Usage Patterns
///
/// ### Integration with Main Assertions Class
/// While these classes can be used directly, the recommended approach is to access them through
/// the main `Assertions` utility class, which provides a unified interface for all assertion types:
///
/// ```java
/// import static org.soliscode.test.assertions.Assertions.*;
///
/// // String containment checking
/// List<String> requiredTerms = List.of("error", "database", "connection");
/// String logEntry = "ERROR: Database connection failed";
/// assertStringContains(requiredTerms, logEntry);
///
/// // Ordered sequence validation
/// List<String> workflowSteps = List.of("start", "process", "complete");
/// String processLog = "start operation -> process data -> complete successfully";
/// assertStringContainsInOrder(workflowSteps, processLog);
/// ```
///
/// ### Error Message Customization
/// All assertion methods support three levels of error message customization:
///
/// ```java
/// // Basic assertion (default error message)
/// assertStringContains(expectedTerms, actualContent);
///
/// // Custom error message
/// assertStringContains(expectedTerms, actualContent, "Log validation failed");
///
/// // Lazy error message (for expensive message construction)
/// assertStringContains(expectedTerms, actualContent,
///     () -> "Validation failed: " + getExpensiveDiagnosticInfo());
/// ```
///
/// ### Case Sensitivity Handling
/// Each assertion type provides both case-sensitive and case-insensitive variants:
///
/// ```java
/// List<String> terms = List.of("error", "warning", "info");
/// String mixedCaseLog = "ERROR occurred, WARNING issued, INFO logged";
///
/// // Case-sensitive (would fail due to case mismatch)
/// assertStringContains(terms, mixedCaseLog);
///
/// // Case-insensitive (passes despite case differences)
/// assertStringContainsIgnoreCase(terms, mixedCaseLog);
/// ```
///
/// ## Performance Characteristics
///
/// ### AssertStringContains Performance
/// - **Time Complexity**: O(k*n*m) where k = number of expected substrings, n = actual string length, m = average substring length
/// - **Space Complexity**: O(1) for case-sensitive, O(n) for case-insensitive (due to lowercase conversion)
/// - **Optimization**: Case-insensitive methods convert strings to lowercase once per call
///
/// ### AssertStringContainsInOrder Performance
/// - **Time Complexity**: O(n*m) where n = actual string length, m = total length of expected substrings
/// - **Space Complexity**: O(1) for case-sensitive, O(n) for case-insensitive
/// - **Optimization**: Position tracking eliminates redundant scanning of processed text
/// - **Early Termination**: Algorithm stops immediately when sequence order is violated
///
/// ## Design Principles
///
/// ### Utility Class Pattern
/// All classes in this package follow the utility class pattern:
/// - Private constructors prevent instantiation
/// - All methods are static for direct access
/// - Thread-safe implementation with no shared state
/// - Immutable parameter handling
///
/// ### Error Handling Philosophy
/// - **Fail Fast**: Assertions fail immediately when validation criteria are not met
/// - **Clear Diagnostics**: Error messages provide specific information about what failed
/// - **Context Preservation**: Original values are included in error reports for debugging
/// - **Lazy Evaluation**: Expensive error message construction is deferred until needed
///
/// ### Integration Standards
/// - **JUnit Compatibility**: All assertions throw `AssertionFailedError` for seamless integration
/// - **Null Safety**: Uses `@NonNull` and `@Nullable` annotations for compile-time safety
/// - **Parameter Validation**: Consistent parameter ordering and validation across all methods
///
/// ## Real-World Applications
///
/// ### Log Analysis and Monitoring
/// ```java
/// // Validate that critical system events appear in logs
/// List<String> criticalEvents = List.of("startup", "config_loaded", "services_ready");
/// String systemLog = getSystemStartupLog();
/// assertStringContainsInOrder(criticalEvents, systemLog,
///     "System startup sequence validation failed");
/// ```
///
/// ### API Testing and Validation
/// ```java
/// // Ensure API responses contain required fields
/// List<String> mandatoryFields = List.of("user_id", "session_token", "expires_at");
/// String apiResponse = callAuthenticationAPI();
/// assertStringContains(mandatoryFields, apiResponse,
///     "Authentication response missing mandatory fields");
/// ```
///
/// ### Workflow and Process Testing
/// ```java
/// // Validate business process execution order
/// List<String> orderSteps = List.of("validate_payment", "reserve_inventory",
///                                   "process_order", "send_confirmation");
/// String orderProcessLog = getOrderProcessingTrace();
/// assertStringContainsInOrder(orderSteps, orderProcessLog,
///     "Order processing workflow validation failed");
/// ```
///
/// ### Configuration and Deployment Validation
/// ```java
/// // Verify deployment pipeline execution
/// List<String> deploymentSteps = List.of("build", "test", "security_scan", "deploy");
/// String cicdLog = getCICDPipelineLog();
/// assertStringContainsInOrder(deploymentSteps, cicdLog,
///     () -> "Deployment validation failed: " + getDeploymentDiagnostics());
/// ```
///
/// ## Best Practices
///
/// ### Choosing the Right Assertion
/// - Use `AssertStringContains` when substring presence matters but order doesn't
/// - Use `AssertStringContainsInOrder` when sequence and ordering are critical
/// - Consider case-insensitive variants for user input and mixed-case content
///
/// ### Error Message Design
/// - Provide business context in custom error messages
/// - Use lazy suppliers for expensive diagnostic information
/// - Include relevant identifiers (user IDs, transaction IDs, etc.) in failure messages
///
/// ### Performance Optimization
/// - Prefer case-sensitive methods when case doesn't vary
/// - Use specific substring lists rather than broad pattern matching
/// - Consider the cost of diagnostic information in supplier-based messages
///
/// @author evanbergstrom
/// @since 1.0.0
/// @see org.soliscode.test.assertions.Assertions
/// @see org.soliscode.test.assertions.string.AssertStringContains
/// @see org.soliscode.test.assertions.string.AssertStringContainsInOrder
package org.soliscode.test.assertions.string;
