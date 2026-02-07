package org.soliscode.test.annotations;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Timeout;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Tag(TestTags.CONCURRENCY)
@Nondeterministic
@Timeout(TestTags.THREAD_SAFETY_TIMEOUT)  // guard against deadlocks
@RepeatedTest(TestTags.THREAD_SAFETY_REPEATS)
public @interface ConcurrencyTest {
}
