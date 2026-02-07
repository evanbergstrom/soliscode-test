package org.soliscode.test.annotations;

public final class TestTags {

    private TestTags() { }

    /// The number of times to repeat the calling of a method to check for the consistency of the results.
    public static final int THREAD_SAFETY_REPEATS = 10;

    public static final int THREAD_SAFETY_TIMEOUT = 10; //seconds



    public static final String SLOW = "slow";
    public static final String VERY_SLOW = "very-slow";
    public static final String EXTREMELY_SLOW = "extremely-slow";

    public static final String CONCURRENCY = "concurrency";
    public static final String NONDETERMINISTIC = "nondeterministic";
}
