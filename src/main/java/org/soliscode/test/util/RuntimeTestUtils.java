package org.soliscode.test.util;

/// **Runtime Environment Testing Utilities**
///
/// This utility class provides methods for inspecting and analyzing the Java runtime environment
/// during test execution. It focuses on cross-version compatibility and runtime feature detection,
/// enabling tests to adapt their behavior based on the underlying JVM characteristics.
///
/// ## Core Functionality
///
/// The class provides utilities for runtime introspection that are essential for writing
/// version-agnostic tests and ensuring compatibility across different Java versions. This
/// is particularly important for libraries that need to support multiple JVM versions
/// while maintaining consistent test behavior.
///
/// ### Version Detection
///
/// The primary functionality revolves around accurate Java version detection that works
/// consistently across different JVM implementations and Java version numbering schemes.
/// This handles the transition from legacy version numbering (1.8, 1.7) to modern
/// version numbering (9, 11, 17, 21, etc.).
///
/// ## Version Numbering Evolution
///
/// ### Legacy Numbering (Java 8 and Earlier)
/// - Format: "1.x.y_z" (e.g., "1.8.0_291", "1.7.0_80")
/// - Major version extracted from the second position (8 from "1.8")
/// - Used `java.specification.version` system property
///
/// ### Modern Numbering (Java 9+)
/// - Format: "x.y.z" (e.g., "11.0.12", "17.0.2", "21.0.1")
/// - Major version is the first number (11, 17, 21)
/// - Uses `Runtime.version().feature()` method introduced in Java 9
///
/// ## Usage Patterns
///
/// ### Version-Conditional Testing
/// ```java
/// @Test
/// void testFeatureAvailability() {
///     int javaVersion = RuntimeTestUtils.majorVersion();
///
///     if (javaVersion >= 11) {
///         // Test features available in Java 11+
///         testModuleSystemFeatures();
///         testHttpClientFeatures();
///     }
///
///     if (javaVersion >= 17) {
///         // Test features available in Java 17+
///         testSealedClassesFeatures();
///         testPatternMatchingFeatures();
///     }
/// }
/// ```
///
/// ### Cross-Version Compatibility Validation
/// ```java
/// @ParameterizedTest
/// @ValueSource(ints = {8, 11, 17, 21})
/// void testCrossVersionCompatibility(int targetVersion) {
///     int currentVersion = RuntimeTestUtils.majorVersion();
///
///     assumeTrue(currentVersion >= targetVersion,
///                "Test requires Java " + targetVersion + " or higher");
///
///     // Run version-specific compatibility tests
///     validateApiCompatibility(targetVersion);
/// }
/// ```
///
/// ### Runtime Environment Validation
/// ```java
/// @Test
/// void validateTestEnvironment() {
///     int javaVersion = RuntimeTestUtils.majorVersion();
///
///     // Ensure minimum version requirements
///     assertTrue(javaVersion >= 8,
///                "This library requires Java 8 or higher, found: " + javaVersion);
///
///     // Log environment information for debugging
///     System.out.println("Running tests on Java " + javaVersion);
/// }
/// ```
///
/// ### Feature Detection for Conditional Logic
/// ```java
/// public class VersionAwareTest {
///
///     @BeforeEach
///     void configureTestEnvironment() {
///         int version = RuntimeTestUtils.majorVersion();
///
///         if (version >= 9) {
///             enableModulePathTesting();
///         } else {
///             enableClassPathTesting();
///         }
///
///         if (version >= 14) {
///             enableRecordTesting();
///         }
///     }
/// }
/// ```
///
/// ## Implementation Strategy
///
/// The implementation uses a fallback strategy to handle version detection across
/// different Java versions:
///
/// 1. **Modern Approach**: Attempts to use `Runtime.version().feature()` (Java 9+)
/// 2. **Legacy Fallback**: Falls back to parsing `java.specification.version` (Java 8 and earlier)
/// 3. **Error Handling**: Uses broad exception catching to ensure compatibility
///
/// This approach ensures reliable version detection regardless of the underlying JVM
/// implementation or Java version, making it suitable for CI/CD environments with
/// varying Java configurations.
///
/// ## Use Cases
///
/// ### Continuous Integration Testing
/// Validate code behavior across multiple Java versions in CI pipelines:
/// - Version-specific feature testing
/// - Backward compatibility validation
/// - Performance regression detection across versions
///
/// ### Library Development
/// Ensure library compatibility across supported Java versions:
/// - API compatibility testing
/// - Feature availability detection
/// - Version-specific optimization paths
///
/// ### Framework Integration
/// Adapt test behavior based on runtime capabilities:
/// - Conditional feature enablement
/// - Version-specific mocking strategies
/// - Runtime optimization selection
///
/// ## Performance Characteristics
///
/// - **Time Complexity**: O(1) - constant time version lookup
/// - **Caching**: Results can be cached as version doesn't change during execution
/// - **Memory Usage**: Minimal - only string parsing operations
/// - **Exception Handling**: Graceful fallback with minimal performance impact
///
/// ## Thread Safety
///
/// All methods in this class are thread-safe as they:
/// - Access immutable system properties
/// - Use read-only JVM APIs
/// - Perform no state modifications
/// - Use only local variables and constants
///
/// @author evanbergstrom
/// @since 1.0.0
/// @see Runtime#version()
/// @see System#getProperty(String)
public final class RuntimeTestUtils {

    /// Private constructor to prevent instantiation of this utility class.
    ///
    /// This class is designed to be used only through its static methods and should
    /// never be instantiated.
    private RuntimeTestUtils() { }

    /// Gets the major version number of the currently running Java runtime.
    ///
    /// This method provides a unified way to determine the Java major version across
    /// different Java version numbering schemes. It automatically handles the transition
    /// from legacy version numbering (Java 8 and earlier) to modern version numbering
    /// (Java 9 and later) using a fallback strategy for maximum compatibility.
    ///
    /// ## Version Detection Strategy
    ///
    /// The method uses a two-tier approach for robust version detection:
    ///
    /// 1. **Primary Method (Java 9+)**: Uses `Runtime.version().feature()`
    ///    - Direct access to the feature version number
    ///    - Most accurate for modern Java versions
    ///    - Returns the major version directly (e.g., 11, 17, 21)
    ///
    /// 2. **Fallback Method (Java 8 and earlier)**: Parses `java.specification.version`
    ///    - Handles legacy version format "1.x.y_z"
    ///    - Extracts major version from the second position
    ///    - Supports both legacy (1.8 → 8) and modern (11 → 11) formats
    ///
    /// ## Version Number Examples
    ///
    /// ```java
    /// // Modern Java versions (Java 9+)
    /// // Java 11.0.12 → returns 11
    /// // Java 17.0.2 → returns 17
    /// // Java 21.0.1 → returns 21
    ///
    /// // Legacy Java versions (Java 8 and earlier)
    /// // Java 1.8.0_291 → returns 8
    /// // Java 1.7.0_80 → returns 7
    /// // Java 1.6.0_45 → returns 6
    ///
    /// int version = RuntimeTestUtils.majorVersion();
    /// System.out.println("Running on Java " + version);
    /// ```
    ///
    /// ## Usage Examples
    ///
    /// ### Version-Conditional Feature Testing
    /// ```java
    /// @Test
    /// void testModernJavaFeatures() {
    ///     int javaVersion = RuntimeTestUtils.majorVersion();
    ///
    ///     if (javaVersion >= 14) {
    ///         // Test switch expressions (Java 14+)
    ///         testSwitchExpressions();
    ///         // Test records (Java 14+ preview, Java 16+ standard)
    ///         testRecordFeatures();
    ///     }
    ///
    ///     if (javaVersion >= 17) {
    ///         // Test sealed classes (Java 17+)
    ///         testSealedClasses();
    ///         // Test pattern matching for instanceof (Java 16+)
    ///         testPatternMatching();
    ///     }
    /// }
    /// ```
    ///
    /// ### Environment Validation
    /// ```java
    /// @BeforeAll
    /// static void validateEnvironment() {
    ///     int requiredVersion = 11;
    ///     int currentVersion = RuntimeTestUtils.majorVersion();
    ///
    ///     assumeTrue(currentVersion >= requiredVersion,
    ///                "Tests require Java " + requiredVersion + " or higher. " +
    ///                "Current version: " + currentVersion);
    /// }
    /// ```
    ///
    /// ### CI/CD Pipeline Adaptation
    /// ```java
    /// @Test
    /// void testPerformanceOptimizations() {
    ///     int javaVersion = RuntimeTestUtils.majorVersion();
    ///
    ///     if (javaVersion >= 11) {
    ///         // Use G1GC optimizations available in Java 11+
    ///         configureG1GCOptimizations();
    ///     }
    ///
    ///     if (javaVersion >= 17) {
    ///         // Use ZGC features available in Java 17+
    ///         configureZGCFeatures();
    ///     }
    ///
    ///     runPerformanceTests();
    /// }
    /// ```
    ///
    /// ### Cross-Version API Compatibility
    /// ```java
    /// @ParameterizedTest
    /// @ValueSource(ints = {8, 11, 17, 21})
    /// void testApiCompatibility(int targetVersion) {
    ///     int currentVersion = RuntimeTestUtils.majorVersion();
    ///
    ///     if (currentVersion >= targetVersion) {
    ///         // Test features available in the target version
    ///         validateApiFeatures(targetVersion);
    ///     } else {
    ///         // Skip test if current version doesn't support target features
    ///         assumeTrue(false, "Skipping test for Java " + targetVersion +
    ///                          " on Java " + currentVersion);
    ///     }
    /// }
    /// ```
    ///
    /// ## Error Handling and Compatibility
    ///
    /// The method uses broad exception handling (`catch (Throwable ignore)`) to ensure
    /// maximum compatibility across different JVM implementations and versions. This
    /// approach gracefully handles:
    /// - Missing methods in older Java versions
    /// - Security manager restrictions
    /// - Custom JVM implementations
    /// - Unusual system property configurations
    ///
    /// ## Performance Considerations
    ///
    /// - **Caching Recommended**: The Java version doesn't change during execution,
    ///   so results can be cached for repeated calls
    /// - **Minimal Overhead**: String parsing operations are lightweight
    /// - **No Side Effects**: Method is pure and doesn't modify any state
    ///
    /// @return the major version number of the current Java runtime (e.g., 8, 11, 17, 21)
    /// @throws RuntimeException if version detection fails (extremely rare)
    /// @see Runtime#version()
    /// @see System#getProperty(String)
    /// @since 1.0.0
    public static int majorVersion() {
        try {
            return Runtime.version().feature(); // Java 9+
        } catch (Throwable ignore) {
            // Java 8 and older: "1.8", "1.7", ...
            String v = System.getProperty("java.specification.version");
            return v.startsWith("1.") ? Integer.parseInt(v.substring(2))
                    : Integer.parseInt(v);
        }
    }
}
