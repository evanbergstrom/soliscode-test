package org.soliscode.test.assertions.string;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.opentest4j.AssertionFailedError;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// **Ordered String Containment Assertion Utilities**
///
/// This utility class provides specialized assertion methods for verifying that strings contain
/// expected substrings in a specific sequential order. It supports both case-sensitive and
/// case-insensitive ordered string matching with comprehensive error reporting and flexible
/// message handling.
///
/// ## Core Functionality
///
/// Unlike simple containment checking, these methods verify that substrings appear in the target
/// string in the exact order specified in the expected list. This is particularly useful for
/// validating workflows, process logs, state transitions, and any content where sequence matters.
///
/// ### Ordered Matching Algorithm
///
/// The algorithm maintains a position pointer that advances through the target string:
/// 1. Search for the first expected substring starting from position 0
/// 2. When found, advance the position to just after the found substring
/// 3. Search for the next expected substring starting from the new position
/// 4. Repeat until all substrings are found or one is missing
///
/// This ensures that substrings must appear in the specified order, but allows other content
/// to appear between them.
///
/// ### Case-Sensitive vs Case-Insensitive
///
/// - **Case-Sensitive**: Uses `String.indexOf()` for exact character matching
/// - **Case-Insensitive**: Converts both strings to lowercase before using `String.indexOf()`
///
/// ## Method Variants
///
/// Each assertion method comes in three variants for flexible error message handling:
///
/// 1. **Basic**: No custom error message (uses default assertion failure message)
/// 2. **With Message**: Accepts a custom String message for assertion failures
/// 3. **With Supplier**: Accepts a `Supplier<String>` for lazy error message evaluation
///
/// ## Usage Examples
/// Calls to this class should be done through the [Assertions][org.soliscode.test.assertions.Assertions]
///  utility class.
///
/// ### Workflow Validation
/// ```java
/// List<String> workflowSteps = List.of("initialize", "process", "validate", "complete");
/// String processLog = "System initialize successful\nData process started\nValidate results\nProcess complete";
///
/// // Case-sensitive ordered checking
/// Assertions.assertStringContainsInOrder(workflowSteps, processLog);  // Passes
///
/// // Case-insensitive ordered checking
/// List<String> mixedCaseSteps = List.of("INITIALIZE", "process", "Validate", "COMPLETE");
/// Assertions.assertStringContainsInOrderIgnoreCase(mixedCaseSteps, processLog);  // Passes
/// ```
///
/// ### API Call Sequence Validation
/// ```java
/// List<String> apiSequence = List.of("authenticate", "authorize", "execute", "log");
/// String apiTrace = "authenticate user -> authorize action -> execute request -> log result";
///
/// Assertions.assertStringContainsInOrder(
///     apiSequence,
///     apiTrace,
///     "API call sequence validation failed"
/// );  // Passes - all steps in correct order
/// ```
///
/// ### State Machine Transitions
/// ```java
/// List<String> stateTransitions = List.of("IDLE", "STARTING", "RUNNING", "STOPPING", "STOPPED");
/// String machineLog = "State: IDLE -> STARTING -> RUNNING -> STOPPING -> STOPPED";
///
/// Assertions.assertStringContainsInOrder(
///     stateTransitions,
///     machineLog,
///     () -> "State machine transition validation failed. Current state: " + getCurrentState()
/// );  // Passes - states appear in expected order
/// ```
///
/// ## Error Handling
///
/// All assertion methods throw `AssertionFailedError` when validation fails, providing:
/// - **Expected value**: The complete list of substrings that should appear in order
/// - **Actual value**: The complete target string that was searched
/// - **Descriptive reason**: Clear explanation that expected strings were not found in correct order
/// - **Custom message**: User-provided message (when specified)
///
/// ## Performance Characteristics
///
/// - **Time Complexity**: O(n*m) where n is the length of the target string and m is the total length of all expected substrings
/// - **Space Complexity**: O(1) for case-sensitive, O(n) for case-insensitive (due to lowercase conversion)
/// - **Early Termination**: Algorithm stops immediately when the first out-of-order substring is detected
/// - **Lazy Evaluation**: Supplier-based messages are only computed when assertions fail
///
/// ## Implementation Notes
///
/// - **Thread Safety**: All methods are static and stateless, making them thread-safe
/// - **Position Tracking**: Uses index-based searching to ensure proper ordering
/// - **Memory Efficiency**: Case-insensitive methods convert strings to lowercase once per call
/// - **Null Safety**: Uses `@NonNull` and `@Nullable` annotations for compile-time safety
///
/// @author evanbergstrom
/// @since 1.0.0
/// @see AssertStringContains
/// @see org.soliscode.test.assertions.Assertions
/// @see org.opentest4j.AssertionFailedError
public final class AssertStringContainsInOrder {

    /// Private constructor to prevent instantiation of this utility class.
    ///
    /// This class is designed to be used only through its static methods and should
    /// never be instantiated.
    private AssertStringContainsInOrder() { }

    /// Asserts that the actual string contains all expected substrings in the specified order with case-sensitive matching.
    ///
    /// This method verifies that every substring in the expected list appears in the actual string
    /// in the exact sequential order specified. The search uses case-sensitive matching and maintains
    /// a position pointer that advances through the target string to ensure proper ordering.
    ///
    /// ## Ordering Algorithm
    ///
    /// The method uses `String.indexOf(substring, startPosition)` to find each expected substring
    /// starting from the position immediately after the previous match. This ensures that:
    /// 1. Substrings must appear in the specified order
    /// 2. Later substrings cannot appear before earlier ones
    /// 3. Other content can appear between expected substrings
    ///
    /// ## Examples
    ///
    /// ```java
    /// // Process workflow validation
    /// List<String> workflowSteps = List.of("start", "initialize", "process", "complete");
    /// String processLog = "start operation -> initialize system -> process data -> complete successfully";
    /// assertStringContainsInOrder(workflowSteps, processLog);  // Passes
    ///
    /// // API call sequence checking
    /// List<String> apiCalls = List.of("authenticate", "authorize", "execute");
    /// String apiTrace = "authenticate user -> authorize action -> execute request";
    /// assertStringContainsInOrder(apiCalls, apiTrace);  // Passes
    ///
    /// // State transition validation
    /// List<String> states = List.of("IDLE", "STARTING", "RUNNING", "STOPPING");
    /// String stateLog = "State: IDLE -> STARTING -> RUNNING -> STOPPING";
    /// assertStringContainsInOrder(states, stateLog);  // Passes
    ///
    /// // Case sensitivity demonstration
    /// List<String> caseTest = List.of("Start", "Process", "End");
    /// String log = "start processing -> Process data -> end";
    /// assertStringContainsInOrder(caseTest, log);  // Fails - "Start" != "start"
    ///
    /// // Order violation example
    /// List<String> wrongOrder = List.of("complete", "start");
    /// String processLog2 = "start operation -> complete successfully";
    /// assertStringContainsInOrder(wrongOrder, processLog2);  // Fails - wrong order
    /// ```
    ///
    /// ## Performance Characteristics
    ///
    /// The algorithm has O(n*m) time complexity where n is the length of the actual string
    /// and m is the total length of all expected substrings. The search is optimized by
    /// advancing the start position after each successful match, avoiding redundant scanning
    /// of already-processed text.
    ///
    /// @param expected the list of substrings that must appear in order within the actual string
    /// @param actual the string to search within for the expected substrings in order
    /// @throws AssertionFailedError if any expected substring is not found in the correct order
    /// @see #assertStringContainsInOrderIgnoreCase(List, String)
    /// @see #assertStringContainsInOrder(List, String, String)
    /// @since 1.0.0
    public static void assertStringContainsInOrder(final @NonNull List<String> expected, final @NonNull String actual) {
        checkStringContainsInOrder(expected, actual, null);
    }

    /// Asserts that the actual string contains all expected substrings in the specified order with case-insensitive matching.
    ///
    /// This method verifies that every substring in the expected list appears in the actual string
    /// in the exact sequential order specified, ignoring case differences. Both the actual string
    /// and each expected substring are converted to lowercase before performing the ordered search.
    ///
    /// ## Case-Insensitive Ordering
    ///
    /// The method performs the same ordered containment checking as the case-sensitive version,
    /// but converts both the actual string and expected substrings to lowercase using
    /// `String.toLowerCase()` before applying the ordering algorithm. This allows for flexible
    /// matching while still enforcing the required sequence.
    ///
    /// ## Examples
    ///
    /// ```java
    /// // Workflow validation with mixed case
    /// List<String> workflowSteps = List.of("start", "process", "complete");
    /// String processLog = "START operation -> PROCESS data -> COMPLETE successfully";
    /// assertStringContainsInOrderIgnoreCase(workflowSteps, processLog);  // Passes
    ///
    /// // User command sequence with case variations
    /// List<String> commands = List.of("login", "backup", "logout");
    /// String userSession = "LOGIN user -> BACKUP database -> logout user";
    /// assertStringContainsInOrderIgnoreCase(commands, userSession);  // Passes
    ///
    /// // Protocol sequence regardless of case
    /// List<String> protocols = List.of("http", "redirect", "https");
    /// String connectionLog = "HTTP request -> REDIRECT to secure -> HTTPS established";
    /// assertStringContainsInOrderIgnoreCase(protocols, connectionLog);  // Passes
    ///
    /// // Build process with mixed case logging
    /// List<String> buildSteps = List.of("compile", "test", "package", "deploy");
    /// String buildLog = "COMPILE sources -> test execution -> PACKAGE artifacts -> Deploy to server";
    /// assertStringContainsInOrderIgnoreCase(buildSteps, buildLog);  // Passes
    ///
    /// // Order still matters even with case insensitivity
    /// List<String> wrongOrder = List.of("deploy", "compile");
    /// String log = "COMPILE first -> then DEPLOY";
    /// assertStringContainsInOrderIgnoreCase(wrongOrder, log);  // Fails - wrong order
    /// ```
    ///
    /// ## Performance Notes
    ///
    /// The method converts the actual string to lowercase once at the beginning, then
    /// converts each expected substring to lowercase during the search iteration. This
    /// approach minimizes string conversion overhead while maintaining the ordering algorithm's
    /// efficiency.
    ///
    /// @param expected the list of substrings that must appear in order within the actual string (case-insensitive)
    /// @param actual the string to search within for the expected substrings in order
    /// @throws AssertionFailedError if any expected substring is not found in the correct order (ignoring case)
    /// @see #assertStringContainsInOrder(List, String)
    /// @see #assertStringContainsInOrderIgnoreCase(List, String, String)
    /// @since 1.0.0
    public static void assertStringContainsInOrderIgnoreCase(final @NonNull List<String> expected,
                                                             final @NonNull String actual) {
        checkStringContainsInOrderIgnoreCase(expected, actual, null);
    }

    /// Asserts that the actual string contains all expected substrings in the specified order with case-sensitive matching and a custom error message.
    ///
    /// This method verifies that every substring in the expected list appears in the actual string
    /// in the exact sequential order specified using case-sensitive matching. If the assertion fails,
    /// the provided custom message is included in the assertion error to provide additional context.
    ///
    /// ## Custom Error Messages for Ordering
    ///
    /// The custom message is particularly valuable for ordered assertions because it can provide
    /// context about what process, workflow, or sequence was being validated. This helps identify
    /// not just that the assertion failed, but which specific ordered operation was problematic.
    ///
    /// ## Examples
    ///
    /// ```java
    /// // Database transaction sequence validation
    /// List<String> transactionSteps = List.of("BEGIN", "INSERT", "UPDATE", "COMMIT");
    /// String transactionLog = "BEGIN transaction -> INSERT data -> ROLLBACK transaction";
    /// assertStringContainsInOrder(
    ///     transactionSteps,
    ///     transactionLog,
    ///     "Database transaction sequence validation failed - expected proper commit flow"
    /// );  // Fails with custom message about missing UPDATE and COMMIT
    ///
    /// // API request pipeline validation
    /// List<String> pipelineSteps = List.of("validate", "transform", "store", "respond");
    /// String requestTrace = "validate input -> transform data -> respond to client";
    /// assertStringContainsInOrder(
    ///     pipelineSteps,
    ///     requestTrace,
    ///     "API request pipeline missing critical storage step"
    /// );  // Fails with custom message about missing "store"
    ///
    /// // Build deployment process validation
    /// List<String> deploymentSteps = List.of("compile", "test", "package", "deploy", "verify");
    /// String buildLog = "compile sources -> test execution -> package artifacts -> deploy to staging";
    /// assertStringContainsInOrder(
    ///     deploymentSteps,
    ///     buildLog,
    ///     "Deployment process incomplete - missing post-deployment verification"
    /// );  // Fails with custom message about missing "verify"
    ///
    /// // User workflow validation with business context
    /// List<String> userFlow = List.of("login", "select_product", "add_to_cart", "checkout", "payment");
    /// String sessionLog = "login successful -> select_product -> add_to_cart -> logout";
    /// assertStringContainsInOrder(
    ///     userFlow,
    ///     sessionLog,
    ///     "E-commerce user flow interrupted - customer abandoned cart before payment"
    /// );  // Fails with business-relevant message
    /// ```
    ///
    /// @param expected the list of substrings that must appear in order within the actual string
    /// @param actual the string to search within for the expected substrings in order
    /// @param message the custom message to include in the assertion error if validation fails
    /// @throws AssertionFailedError if any expected substring is not found in the correct order, including the custom message
    /// @see #assertStringContainsInOrder(List, String)
    /// @see #assertStringContainsInOrder(List, String, Supplier)
    /// @since 1.0.0
    public static void assertStringContainsInOrder(final @NonNull List<String> expected, final @NonNull String actual,
                                                   final String message) {
        checkStringContainsInOrder(expected, actual, message);
    }

    /// Asserts that the actual string contains all expected substrings in the specified order with case-insensitive matching and a custom error message.
    ///
    /// This method verifies that every substring in the expected list appears in the actual string
    /// in the exact sequential order specified, ignoring case differences. If the assertion fails,
    /// the provided custom message is included in the assertion error to provide additional context
    /// about the validation failure.
    ///
    /// ## Case-Insensitive Ordering with Custom Messages
    ///
    /// This variant combines the flexibility of case-insensitive matching with the clarity of
    /// custom error messages and the precision of ordered validation. It's ideal for scenarios
    /// where sequence matters but case variations are expected, and specific business context
    /// needs to be communicated on failure.
    ///
    /// ## Examples
    ///
    /// ```java
    /// // System startup sequence validation with mixed case logging
    /// List<String> startupSteps = List.of("initialize", "load_config", "start_services", "ready");
    /// String systemLog = "INITIALIZE system -> LOAD_CONFIG files -> start_services -> health_check";
    /// assertStringContainsInOrderIgnoreCase(
    ///     startupSteps,
    ///     systemLog,
    ///     "System startup sequence incomplete - missing final 'ready' status"
    /// );  // Fails with custom message despite case differences
    ///
    /// // User onboarding flow with case-insensitive commands
    /// List<String> onboardingSteps = List.of("register", "verify_email", "setup_profile", "welcome");
    /// String userFlow = "REGISTER account -> verify_email -> SETUP_PROFILE -> logout";
    /// assertStringContainsInOrderIgnoreCase(
    ///     onboardingSteps,
    ///     userFlow,
    ///     "User onboarding incomplete - missing welcome screen presentation"
    /// );  // Fails with business context message
    ///
    /// // Network protocol handshake regardless of case
    /// List<String> handshakeSteps = List.of("syn", "syn_ack", "ack", "established");
    /// String connectionLog = "SYN sent -> SYN_ACK received -> ACK sent -> timeout";
    /// assertStringContainsInOrderIgnoreCase(
    ///     handshakeSteps,
    ///     connectionLog,
    ///     "TCP handshake failed - connection not properly established"
    /// );  // Fails with networking context
    ///
    /// // CI/CD pipeline with mixed case step names
    /// List<String> cicdSteps = List.of("checkout", "build", "test", "security_scan", "deploy");
    /// String pipelineLog = "CHECKOUT code -> BUILD artifacts -> TEST execution -> deploy to prod";
    /// assertStringContainsInOrderIgnoreCase(
    ///     cicdSteps,
    ///     pipelineLog,
    ///     "CI/CD pipeline security violation - security scan was skipped before deployment"
    /// );  // Fails with security-focused message
    /// ```
    ///
    /// @param expected the list of substrings that must appear in order within the actual string (case-insensitive)
    /// @param actual the string to search within for the expected substrings in order
    /// @param message the custom message to include in the assertion error if validation fails
    /// @throws AssertionFailedError if any expected substring is not found in the correct order (ignoring case), including the custom message
    /// @see #assertStringContainsInOrderIgnoreCase(List, String)
    /// @see #assertStringContainsInOrderIgnoreCase(List, String, Supplier)
    /// @since 1.0.0
    public static void assertStringContainsInOrderIgnoreCase(final @NonNull List<String> expected,
                                                             final @NonNull String actual,
                                                             final String message) {
        checkStringContainsInOrderIgnoreCase(expected, actual, message);
    }

    /// Asserts that the actual string contains all expected substrings in the specified order with case-sensitive matching and a lazily-evaluated error message.
    ///
    /// This method verifies that every substring in the expected list appears in the actual string
    /// in the exact sequential order specified using case-sensitive matching. If the assertion fails,
    /// the error message is generated by invoking the provided supplier, which allows for expensive
    /// message construction to be deferred until actually needed.
    ///
    /// ## Lazy Message Evaluation for Ordered Assertions
    ///
    /// The supplier-based approach is particularly valuable for ordered assertions because sequence
    /// validation often requires complex debugging information. The supplier allows expensive
    /// operations (such as state queries, log analysis, or diagnostic data collection) to be
    /// deferred until the assertion actually fails.
    ///
    /// ## Examples
    ///
    /// ```java
    /// // Complex workflow analysis with expensive state gathering
    /// List<String> workflowSteps = List.of("validate", "authorize", "execute", "audit");
    /// String processTrace = getProcessTrace();  // Potentially expensive operation
    /// assertStringContainsInOrder(
    ///     workflowSteps,
    ///     processTrace,
    ///     () -> "Workflow validation failed. Current state: " +
    ///           getCurrentWorkflowState() + ", Full process history: " +
    ///           getCompleteProcessHistory()  // Very expensive calls
    /// );
    ///
    /// // Database transaction monitoring with detailed diagnostics
    /// List<String> transactionFlow = List.of("BEGIN", "LOCK", "UPDATE", "COMMIT");
    /// String transactionLog = getTransactionLog(transactionId);
    /// assertStringContainsInOrder(
    ///     transactionFlow,
    ///     transactionLog,
    ///     () -> "Transaction sequence validation failed for ID: " + transactionId +
    ///           ". Database state: " + queryDatabaseState() +
    ///           ", Lock status: " + getCurrentLockStatus()  // Database queries
    /// );
    ///
    /// // Distributed system event ordering with cluster state
    /// List<String> eventSequence = List.of("leader_elect", "sync_start", "data_transfer", "sync_complete");
    /// String clusterEvents = getClusterEventLog();
    /// assertStringContainsInOrder(
    ///     eventSequence,
    ///     clusterEvents,
    ///     () -> "Cluster synchronization failed. Cluster topology: " +
    ///           getClusterTopology() + ", Node health: " +
    ///           checkAllNodeHealth()  // Expensive cluster analysis
    /// );
    ///
    /// // Build pipeline validation with comprehensive build context
    /// List<String> buildStages = List.of("compile", "test", "security_scan", "package");
    /// String buildOutput = getBuildLog(buildId);
    /// assertStringContainsInOrder(
    ///     buildStages,
    ///     buildOutput,
    ///     () -> "Build pipeline validation failed for build: " + buildId +
    ///           ". Environment: " + getBuildEnvironment() +
    ///           ", Dependencies: " + analyzeDependencies()  // Complex analysis
    /// );
    /// ```
    ///
    /// @param expected the list of substrings that must appear in order within the actual string
    /// @param actual the string to search within for the expected substrings in order
    /// @param messageOrSupplier the supplier for the custom error message (only invoked on assertion failure)
    /// @throws AssertionFailedError if any expected substring is not found in the correct order, with the supplied custom message
    /// @see #assertStringContainsInOrder(List, String)
    /// @see #assertStringContainsInOrder(List, String, String)
    /// @since 1.0.0
    public static void assertStringContainsInOrder(final @NonNull List<String> expected, final @NonNull String actual,
                                             final Supplier<String> messageOrSupplier) {
        checkStringContainsInOrder(expected, actual, messageOrSupplier);
    }

    /// Asserts that the actual string contains all expected substrings in the specified order with case-insensitive matching and a lazily-evaluated error message.
    ///
    /// This method verifies that every substring in the expected list appears in the actual string
    /// in the exact sequential order specified, ignoring case differences. If the assertion fails,
    /// the error message is generated by invoking the provided supplier, which allows for expensive
    /// message construction to be deferred until actually needed.
    ///
    /// ## Case-Insensitive Ordering with Lazy Evaluation
    ///
    /// This variant combines the flexibility of case-insensitive matching with the performance
    /// benefits of lazy message evaluation and the precision of ordered validation. It's ideal
    /// for scenarios where sequence matters, case variations are expected, and detailed diagnostic
    /// information should only be computed when validation actually fails.
    ///
    /// ## Examples
    ///
    /// ```java
    /// // System service startup with mixed case logging and expensive diagnostics
    /// List<String> serviceSteps = List.of("init", "config", "start", "health_check");
    /// String serviceLog = "INIT service -> CONFIG loaded -> START successful -> monitor";
    /// assertStringContainsInOrderIgnoreCase(
    ///     serviceSteps,
    ///     serviceLog,
    ///     () -> "Service startup incomplete. System state: " +
    ///           getFullSystemDiagnostics() + ", Service health: " +
    ///           performComprehensiveHealthCheck()  // Expensive system calls
    /// );  // Fails but only evaluates expensive diagnostics on failure
    ///
    /// // User interaction flow with case-insensitive commands and session analysis
    /// List<String> userActions = List.of("login", "browse", "purchase", "logout");
    /// String sessionTrace = "LOGIN successful -> BROWSE products -> checkout -> LOGOUT";
    /// assertStringContainsInOrderIgnoreCase(
    ///     userActions,
    ///     sessionTrace,
    ///     () -> "User session incomplete for user: " + getCurrentUserId() +
    ///           ". Session history: " + getCompleteSessionHistory() +
    ///           ", Shopping cart: " + analyzeShoppingCartState()  // Database/analytics calls
    /// );  // Missing "purchase" step, expensive analysis only on failure
    ///
    /// // Network protocol sequence with case variations and network diagnostics
    /// List<String> protocolSteps = List.of("connect", "authenticate", "exchange", "disconnect");
    /// String networkLog = "CONNECT established -> authenticate user -> EXCHANGE data -> timeout";
    /// assertStringContainsInOrderIgnoreCase(
    ///     protocolSteps,
    ///     networkLog,
    ///     () -> "Network protocol sequence failed. Connection state: " +
    ///           analyzeNetworkConnection() + ", Latency metrics: " +
    ///           gatherLatencyMetrics()  // Network analysis calls
    /// );  // Missing proper "disconnect", diagnostics only computed on failure
    ///
    /// // CI/CD deployment pipeline with mixed case and infrastructure analysis
    /// List<String> deploySteps = List.of("build", "test", "security", "deploy", "verify");
    /// String pipelineLog = "BUILD complete -> TEST passed -> SECURITY approved -> DEPLOY staging";
    /// assertStringContainsInOrderIgnoreCase(
    ///     deploySteps,
    ///     pipelineLog,
    ///     () -> "Deployment pipeline incomplete. Infrastructure state: " +
    ///           getInfrastructureHealth() + ", Security scan results: " +
    ///           getDetailedSecurityReport()  // Infrastructure and security API calls
    /// );  // Missing "verify" step, expensive infrastructure analysis only on failure
    /// ```
    ///
    /// @param expected the list of substrings that must appear in order within the actual string (case-insensitive)
    /// @param actual the string to search within for the expected substrings in order
    /// @param messageOrSupplier the supplier for the custom error message (only invoked on assertion failure)
    /// @throws AssertionFailedError if any expected substring is not found in the correct order (ignoring case), with the supplied custom message
    /// @see #assertStringContainsInOrderIgnoreCase(List, String)
    /// @see #assertStringContainsInOrderIgnoreCase(List, String, String)
    /// @since 1.0.0
    public static void assertStringContainsInOrderIgnoreCase(final @NonNull List<String> expected,
                                                             final @NonNull String actual,
                                                             final Supplier<String> messageOrSupplier) {
        checkStringContainsInOrderIgnoreCase(expected, actual, messageOrSupplier);
    }

    private static void checkStringContainsInOrder(final @NonNull List<String> expected, final @NonNull String actual,
                                            final @Nullable Object messageOrSupplier) {

        int pos = 0;
        for (String e : expected) {
            int index = actual.indexOf(e, pos);
            if (index < 0) {
                throw buildException(expected, actual, messageOrSupplier);
            }
            pos = index + e.length();
        }
    }

    private static void checkStringContainsInOrderIgnoreCase(final @NonNull List<String> expected,
                                                             final @NonNull String actual,
                                                             final @Nullable Object messageOrSupplier) {
        String actualLowerCase = actual.toLowerCase();
        int pos = 0;
        for (String e : expected) {
            int index = actualLowerCase.indexOf(e.toLowerCase(), pos);
            if (index < 0) {
                throw buildException(expected, actual, messageOrSupplier);
            }
            pos = index + e.length();
        }
    }

    private static AssertionFailedError buildException(final @NonNull List<String> expected,
                                                       final @NonNull String actual,
                                                       final @Nullable Object messageOrSupplier) {
        return assertionFailure()
                .message(messageOrSupplier)
                .expected(expected)
                .actual(actual)
                .reason("expected strings [" + expected + "] in string \"" + actual
                        + "\" in the correct order")
                .build();
    }
}
