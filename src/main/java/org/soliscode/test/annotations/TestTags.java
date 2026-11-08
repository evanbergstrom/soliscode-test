package org.soliscode.test.annotations;

/// **Test Tags**
///
/// This class contains constants used for tagging and configuring tests. These tags
/// allow for categorizing tests by performance (e.g., {@link #SLOW}, {@link #VERY_SLOW})
/// or by type (e.g., {@link #CONCURRENCY}, {@link #NONDETERMINISTIC}).
///
/// It also defines configuration constants for concurrency testing, such as repetition
/// counts and timeouts.
///
/// @see Slow
/// @see ConcurrencyTest
/// @see Nondeterministic
/// @since 1.0.0
public final class TestTags {

    private TestTags() { }

    /// The number of times to repeat the calling of a method to check for the consistency of the results.
    public static final int THREAD_SAFETY_REPEATS = 5;

    /// The timeout in seconds for thread-safety tests to prevent deadlocks from hanging the test suite.
    public static final int THREAD_SAFETY_TIMEOUT = 10; //seconds

    /// Tag for tests that take a significant amount of time to execute (typically 100ms – 1s).
    public static final String SLOW = "slow";

    /// Tag for tests that take a long time to execute (typically 1s – 10s).
    public static final String VERY_SLOW = "very-slow";

    /// Tag for tests that take an extremely long time to execute (typically > 10s).
    public static final String EXTREMELY_SLOW = "extremely-slow";

    /// Tag for tests that specifically target thread-safety and concurrency behavior.
    public static final String CONCURRENCY = "concurrency";

    /// Tag for tests whose results may vary between runs due to timing or randomness.
    public static final String NONDETERMINISTIC = "nondeterministic";
}
