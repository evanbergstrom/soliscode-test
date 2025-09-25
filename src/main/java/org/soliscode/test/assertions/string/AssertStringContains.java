package org.soliscode.test.assertions.string;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.opentest4j.AssertionFailedError;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// **String Containment Assertion Utilities**
///
/// This utility class provides specialized assertion methods for verifying that strings contain
/// expected substrings. It supports both case-sensitive and case-insensitive string matching
/// with comprehensive error reporting and flexible message handling.
///
/// ## Core Functionality
///
/// The class provides assertion methods that verify whether a target string contains all
/// specified substrings from a list. Each substring is checked independently, and the
/// assertion fails immediately when the first missing substring is encountered.
///
/// ### Case-Sensitive Matching
/// Standard string containment checking using Java's `String.contains()` method, which
/// performs exact character matching including case sensitivity.
///
/// ### Case-Insensitive Matching
/// Case-insensitive containment checking by converting both the target string and expected
/// substrings to lowercase before comparison using `String.toLowerCase()`.
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
/// utility class.
///
/// ### Basic String Containment
/// ```java
/// List<String> requiredTerms = List.of("error", "database", "connection");
/// String logMessage = "Error: Database connection failed during startup";
///
/// // Case-sensitive checking
/// Assertions.assertStringContains(requiredTerms, logMessage);  // Passes
///
/// // Case-insensitive checking
/// List<String> mixedCase = List.of("ERROR", "Database", "CONNECTION");
/// Assertions.assertStringContainsIgnoreCase(mixedCase, logMessage);  // Passes
/// ```
///
/// ### Error Message Customization
/// ```java
/// List<String> expectedKeywords = List.of("user", "authenticated", "session");
/// String response = "User authentication successful, session created";
///
/// // With custom message
/// Assertions.assertStringContains(
///     expectedKeywords,
///     response,
///     "Authentication response missing required keywords"
/// );
///
/// // With lazy message supplier (for expensive message construction)
/// Assertions.assertStringContains(
///     expectedKeywords,
///     response,
///     () -> "Authentication failed for user: " + getCurrentUser() +
///           ", response: " + getDetailedResponse()
/// );
/// ```
///
/// ## Error Handling
///
/// All assertion methods throw `AssertionFailedError` when validation fails, providing:
/// - **Expected value**: The missing substring that caused the failure
/// - **Actual value**: The complete target string that was searched
/// - **Descriptive reason**: Clear explanation of what substring was missing
/// - **Custom message**: User-provided message (when specified)
///
/// ## Implementation Notes
///
/// - **Thread Safety**: All methods are static and stateless, making them thread-safe
/// - **Performance**: Case-insensitive methods convert strings to lowercase once per call
/// - **Memory Efficiency**: Lazy suppliers are only evaluated when assertions fail
/// - **Null Safety**: Uses `@NonNull` and `@Nullable` annotations for compile-time safety
///
/// @author evanbergstrom
/// @since 1.0.0
/// @see org.soliscode.test.assertions.Assertions
/// @see org.opentest4j.AssertionFailedError
public final class AssertStringContains {

    /// Private constructor to prevent instantiation of this utility class.
    ///
    /// This class is designed to be used only through its static methods and should
    /// never be instantiated.
    private AssertStringContains() { }

    /// Asserts that the actual string contains all expected substrings with case-sensitive matching.
    ///
    /// This method verifies that every substring in the expected list appears somewhere within
    /// the actual string. The search is performed using Java's standard `String.contains()`
    /// method, which is case-sensitive and uses exact character matching.
    ///
    /// ## Validation Process
    ///
    /// The method iterates through each expected substring and verifies its presence in the
    /// actual string. If any substring is missing, the assertion fails immediately with a
    /// descriptive error message indicating which substring was not found.
    ///
    /// ## Examples
    ///
    /// ```java
    /// // Log validation - checking for error indicators
    /// List<String> errorTerms = List.of("ERROR", "failed", "exception");
    /// String logEntry = "ERROR: Operation failed due to network exception";
    /// assertStringContains(errorTerms, logEntry);  // Passes
    ///
    /// // API response validation
    /// List<String> requiredFields = List.of("id", "name", "email");
    /// String jsonResponse = "{\"id\":123,\"name\":\"John\",\"email\":\"john@example.com\"}";
    /// assertStringContains(requiredFields, jsonResponse);  // Passes
    ///
    /// // Configuration validation
    /// List<String> configKeys = List.of("database.url", "database.port", "cache.size");
    /// String configFile = "database.url=localhost\n database.port=5432\n cache.size=1000";
    /// assertStringContains(configKeys, configFile);  // Passes
    ///
    /// // Case sensitivity demonstration
    /// List<String> caseTest = List.of("Error", "Failed");
    /// String message = "error: operation failed";
    /// assertStringContains(caseTest, message);  // Fails - case mismatch
    /// ```
    ///
    /// ## Performance Considerations
    ///
    /// The method uses `String.contains()` for each substring check, which has O(n*m)
    /// complexity where n is the length of the actual string and m is the length of
    /// the substring. For multiple substrings, the total complexity is O(k*n*m) where
    /// k is the number of expected substrings.
    ///
    /// @param expected the list of substrings that must all be present in the actual string
    /// @param actual the string to search within for the expected substrings
    /// @throws AssertionFailedError if any expected substring is not found in the actual string
    /// @see #assertStringContainsIgnoreCase(List, String)
    /// @see #assertStringContains(List, String, String)
    /// @since 1.0.0
    public static void assertStringContains(final @NonNull List<String> expected, final @NonNull String actual) {
        checkStringContains(expected, actual, null);
    }

    /// Asserts that the actual string contains all expected substrings with case-insensitive matching.
    ///
    /// This method verifies that every substring in the expected list appears somewhere within
    /// the actual string, ignoring case differences. Both the actual string and each expected
    /// substring are converted to lowercase using `String.toLowerCase()` before comparison.
    ///
    /// ## Case-Insensitive Matching
    ///
    /// The method performs case-insensitive containment checking by converting both strings
    /// to lowercase before using `String.contains()`. This allows matching regardless of
    /// the original case of characters in either the expected substrings or the actual string.
    ///
    /// ## Examples
    ///
    /// ```java
    /// // Log validation with mixed case
    /// List<String> errorTerms = List.of("error", "database", "failed");
    /// String logEntry = "ERROR: Database connection FAILED during startup";
    /// assertStringContainsIgnoreCase(errorTerms, logEntry);  // Passes
    ///
    /// // User input validation (case variations common)
    /// List<String> commands = List.of("start", "stop", "restart");
    /// String userInput = "Please START the service, then STOP it, and RESTART";
    /// assertStringContainsIgnoreCase(commands, userInput);  // Passes
    ///
    /// // Email validation with case insensitive domains
    /// List<String> domains = List.of("gmail.com", "yahoo.com", "hotmail.com");
    /// String emailList = "user@GMAIL.COM, admin@Yahoo.com, test@HOTMAIL.COM";
    /// assertStringContainsIgnoreCase(domains, emailList);  // Passes
    ///
    /// // Protocol checking regardless of case
    /// List<String> protocols = List.of("http", "https", "ftp");
    /// String urlList = "HTTP://example.com, HTTPS://secure.com, FTP://files.com";
    /// assertStringContainsIgnoreCase(protocols, urlList);  // Passes
    /// ```
    ///
    /// ## Performance Notes
    ///
    /// The method converts the actual string to lowercase once at the beginning, then
    /// converts each expected substring to lowercase during iteration. This is more
    /// efficient than converting the entire actual string for each substring check.
    ///
    /// @param expected the list of substrings that must all be present in the actual string (case-insensitive)
    /// @param actual the string to search within for the expected substrings
    /// @throws AssertionFailedError if any expected substring is not found in the actual string (ignoring case)
    /// @see #assertStringContains(List, String)
    /// @see #assertStringContainsIgnoreCase(List, String, String)
    /// @since 1.0.0
    public static void assertStringContainsIgnoreCase(final @NonNull List<String> expected,
                                                      final @NonNull String actual) {
        checkStringContainsIgnoreCase(expected, actual, null);
    }

    /// Asserts that the actual string contains all expected substrings with case-sensitive matching and a custom error message.
    ///
    /// This method verifies that every substring in the expected list appears somewhere within
    /// the actual string using case-sensitive matching. If the assertion fails, the provided
    /// custom message is included in the assertion error to provide additional context.
    ///
    /// ## Custom Error Messages
    ///
    /// The custom message is particularly useful for providing business context or debugging
    /// information when the assertion fails. It helps identify which specific validation
    /// failed in complex test scenarios.
    ///
    /// ## Examples
    ///
    /// ```java
    /// // API response validation with context
    /// List<String> requiredFields = List.of("user_id", "session_token", "expiry");
    /// String apiResponse = "{\"user_id\":123,\"session_token\":\"abc123\"}";
    /// assertStringContains(
    ///     requiredFields,
    ///     apiResponse,
    ///     "Authentication API response missing required fields"
    /// );  // Fails with custom message about missing "expiry"
    ///
    /// // Log file validation with specific context
    /// List<String> auditTerms = List.of("user_login", "timestamp", "ip_address");
    /// String auditLog = "user_login: john_doe, timestamp: 2024-01-01T10:00:00Z";
    /// assertStringContains(
    ///     auditTerms,
    ///     auditLog,
    ///     "Security audit log missing required tracking information"
    /// );  // Fails with custom message about missing "ip_address"
    ///
    /// // Configuration validation with descriptive error
    /// List<String> configParams = List.of("database.host", "database.port", "database.name");
    /// String config = "database.host=localhost\ndatabase.port=5432";
    /// assertStringContains(
    ///     configParams,
    ///     config,
    ///     "Database configuration incomplete - missing required parameters"
    /// );  // Fails with custom message about missing "database.name"
    /// ```
    ///
    /// @param expected the list of substrings that must all be present in the actual string
    /// @param actual the string to search within for the expected substrings
    /// @param message the custom message to include in the assertion error if validation fails
    /// @throws AssertionFailedError if any expected substring is not found, including the custom message
    /// @see #assertStringContains(List, String)
    /// @see #assertStringContains(List, String, Supplier)
    /// @since 1.0.0
    public static void assertStringContains(final @NonNull List<String> expected,
                                            final @NonNull String actual,
                                            final String message) {
        checkStringContains(expected, actual, message);
    }

    /// Asserts that the actual string contains all expected substrings with case-insensitive matching and a custom error message.
    ///
    /// This method verifies that every substring in the expected list appears somewhere within
    /// the actual string, ignoring case differences. If the assertion fails, the provided
    /// custom message is included in the assertion error to provide additional context about
    /// the validation failure.
    ///
    /// ## Case-Insensitive Custom Messages
    ///
    /// This variant combines the flexibility of case-insensitive matching with the clarity
    /// of custom error messages. It's particularly useful when validating user input,
    /// configuration files, or any content where case variations are expected but specific
    /// business context needs to be communicated on failure.
    ///
    /// ## Examples
    ///
    /// ```java
    /// // User command validation with helpful error messages
    /// List<String> validCommands = List.of("start", "stop", "restart", "status");
    /// String userInput = "START the server and check STATUS";
    /// assertStringContainsIgnoreCase(
    ///     validCommands,
    ///     userInput,
    ///     "User input contains invalid commands - only start/stop/restart/status allowed"
    /// );  // Passes despite case differences
    ///
    /// // Email domain validation with business context
    /// List<String> allowedDomains = List.of("company.com", "partner.org", "vendor.net");
    /// String emailAddresses = "user@COMPANY.COM, admin@partner.org";
    /// assertStringContainsIgnoreCase(
    ///     allowedDomains,
    ///     emailAddresses,
    ///     "Email validation failed - only corporate domains allowed"
    /// );  // Fails with custom message about missing "vendor.net"
    ///
    /// // Protocol validation in URLs regardless of case
    /// List<String> secureProtocols = List.of("https", "ftps", "sftp");
    /// String urlList = "HTTPS://secure.example.com, HTTP://insecure.example.com";
    /// assertStringContainsIgnoreCase(
    ///     secureProtocols,
    ///     urlList,
    ///     "Security validation failed - all URLs must use secure protocols"
    /// );  // Fails with custom message about missing secure protocols
    /// ```
    ///
    /// @param expected the list of substrings that must all be present in the actual string (case-insensitive)
    /// @param actual the string to search within for the expected substrings
    /// @param message the custom message to include in the assertion error if validation fails
    /// @throws AssertionFailedError if any expected substring is not found (ignoring case), including the custom message
    /// @see #assertStringContainsIgnoreCase(List, String)
    /// @see #assertStringContainsIgnoreCase(List, String, Supplier)
    /// @since 1.0.0
    public static void assertStringContainsIgnoreCase(final @NonNull List<String> expected,
                                                      final @NonNull String actual,
                                                      final String message) {
        checkStringContainsIgnoreCase(expected, actual, message);
    }

    /// Asserts that the actual string contains all expected substrings with case-sensitive matching and a lazily-evaluated error message.
    ///
    /// This method verifies that every substring in the expected list appears somewhere within
    /// the actual string using case-sensitive matching. If the assertion fails, the error message
    /// is generated by invoking the provided supplier, which allows for expensive message
    /// construction to be deferred until actually needed.
    ///
    /// ## Lazy Message Evaluation
    ///
    /// The supplier-based approach is particularly valuable when constructing error messages
    /// requires expensive operations (such as database queries, file I/O, or complex object
    /// serialization). The supplier is only invoked when the assertion fails, ensuring
    /// optimal performance for successful tests.
    ///
    /// ## Examples
    ///
    /// ```java
    /// // Expensive diagnostic information only computed on failure
    /// List<String> criticalTerms = List.of("SUCCESS", "COMPLETE", "VERIFIED");
    /// String processLog = getProcessLog();  // Potentially expensive operation
    /// assertStringContains(
    ///     criticalTerms,
    ///     processLog,
    ///     () -> "Process validation failed. Full system state: " +
    ///           getDiagnosticInfo() + ", Log: " + processLog  // Expensive call
    /// );
    ///
    /// // Database query context only when needed
    /// List<String> requiredFields = List.of("transaction_id", "amount", "status");
    /// String transactionData = getTransactionData(id);
    /// assertStringContains(
    ///     requiredFields,
    ///     transactionData,
    ///     () -> "Transaction validation failed for ID: " + id +
    ///           ". Database state: " + queryTransactionHistory(id)  // Database call
    /// );
    ///
    /// // Complex object serialization for debugging
    /// List<String> configKeys = List.of("api.key", "db.connection", "cache.config");
    /// String configContent = loadConfiguration();
    /// assertStringContains(
    ///     configKeys,
    ///     configContent,
    ///     () -> "Configuration validation failed. Current config: " +
    ///           serializeCompleteConfiguration()  // Expensive serialization
    /// );
    /// ```
    ///
    /// @param expected the list of substrings that must all be present in the actual string
    /// @param actual the string to search within for the expected substrings
    /// @param messageOrSupplier the supplier for the custom error message (only invoked on assertion failure)
    /// @throws AssertionFailedError if any expected substring is not found, with the supplied custom message
    /// @see #assertStringContains(List, String)
    /// @see #assertStringContains(List, String, String)
    /// @since 1.0.0
    public static void assertStringContains(final @NonNull List<String> expected,
                                            final @NonNull String actual,
                                            final Supplier<String> messageOrSupplier) {
        checkStringContains(expected, actual, messageOrSupplier);
    }

    /// Asserts that the actual string contains all expected substrings with case-insensitive matching and a lazily-evaluated error message.
    ///
    /// This method verifies that every substring in the expected list appears somewhere within
    /// the actual string, ignoring case differences. If the assertion fails, the error message
    /// is generated by invoking the provided supplier, which allows for expensive message
    /// construction to be deferred until actually needed.
    ///
    /// ## Case-Insensitive Lazy Evaluation
    ///
    /// This variant combines the flexibility of case-insensitive matching with the performance
    /// benefits of lazy message evaluation. It's ideal for scenarios where case variations are
    /// expected in the content being validated, but detailed diagnostic information should only
    /// be computed when validation actually fails.
    ///
    /// ## Examples
    ///
    /// ```java
    /// // User input validation with expensive context gathering
    /// List<String> expectedCommands = List.of("backup", "restore", "verify");
    /// String userScript = "BACKUP database, then RESTORE from file, and VERIFY integrity";
    /// assertStringContainsIgnoreCase(
    ///     expectedCommands,
    ///     userScript,
    ///     () -> "Script validation failed. User: " + getCurrentUser() +
    ///           ", Full script history: " + getUserScriptHistory()  // Expensive query
    /// );  // Passes despite case differences
    ///
    /// // Log analysis with complex diagnostic information
    /// List<String> errorIndicators = List.of("error", "exception", "failed", "timeout");
    /// String systemLog = "ERROR: Connection timeout occurred, Exception thrown";
    /// assertStringContainsIgnoreCase(
    ///     errorIndicators,
    ///     systemLog,
    ///     () -> "Error analysis failed. System state: " +
    ///           generateFullSystemDiagnostic()  // Very expensive operation
    /// );  // Only evaluates diagnostic on failure
    ///
    /// // Email domain validation with regulatory context
    /// List<String> approvedDomains = List.of("company.com", "partner.org", "approved.net");
    /// String emailBatch = "user@COMPANY.COM, admin@PARTNER.ORG, guest@external.com";
    /// assertStringContainsIgnoreCase(
    ///     approvedDomains,
    ///     emailBatch,
    ///     () -> "Email compliance check failed. Regulatory status: " +
    ///           checkComplianceDatabase() + ", Audit trail: " + generateAuditLog()
    /// );  // Expensive compliance checks only on failure
    /// ```
    ///
    /// @param expected the list of substrings that must all be present in the actual string (case-insensitive)
    /// @param actual the string to search within for the expected substrings
    /// @param messageOrSupplier the supplier for the custom error message (only invoked on assertion failure)
    /// @throws AssertionFailedError if any expected substring is not found (ignoring case), with the supplied custom message
    /// @see #assertStringContainsIgnoreCase(List, String)
    /// @see #assertStringContainsIgnoreCase(List, String, String)
    /// @since 1.0.0
    public static void assertStringContainsIgnoreCase(final @NonNull List<String> expected,
                                                      final @NonNull String actual,
                                                      final Supplier<String> messageOrSupplier) {
        checkStringContainsIgnoreCase(expected, actual, messageOrSupplier);
    }

    private static void checkStringContains(final @NonNull List<String> expected, final @NonNull String actual,
                                            final @Nullable Object messageOrSupplier) {
        for (String e : expected) {
            if (!actual.contains(e)) {
                throw buildException(e, actual, messageOrSupplier);
            }
        }
    }

    private static void checkStringContainsIgnoreCase(final @NonNull List<String> expected,
                                                      final @NonNull String actual,
                                                      final @Nullable Object messageOrSupplier) {
        String actualLowerCase = actual.toLowerCase();
        for (String e : expected) {
            if (!actualLowerCase.contains(e.toLowerCase())) {
                throw buildException(e, actual, messageOrSupplier);
            }
        }
    }

    private static AssertionFailedError buildException(final @NonNull String expected,
                                                       final @NonNull String actual,
                                                       final @Nullable Object messageOrSupplier) {
        return assertionFailure()
                .message(messageOrSupplier)
                .expected(expected)
                .actual(actual)
                .reason("missing string \"" + expected + "\" in string \"" + actual + "\"")
                .build();
    }
}
