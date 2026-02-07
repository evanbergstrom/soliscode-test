package org.soliscode.test.annotations;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// **Extremely Slow Test Annotation**
///
/// This annotation is used to tag tests that take a extremely long amount of time to execute.
/// It is typically used for tests that perform complex calculations, large-scale data
/// processing, or intensive I/O operations.
///
/// The tag is one of four used to categorize tests in the test suite:
///
/// | Tag           | Example Times  |
/// | :------------ | :------------  |
/// | none          | < 100 ms – 1 s |
/// | Slow          | 100 ms – 1 s   |
/// | VerySlow      | 1 s – 10 s     |
/// | ExtremelySlow | \> 10 s        |
///
/// The actual times will vary based upon the system the tests are being run on, the times above are just examples
/// to establish the relative performance between the tags.
///
/// ## Usage
/// ```java
/// @Test
/// @ExtremelySlow
/// void testComplexAlgorithm() {
///     // ... intensive test logic ...
/// }
/// ```
///
/// Tests tagged with `@ExtremelySlow` can be excluded from fast-running CI/CD pipelines or
/// local development cycles using JUnit 5 tag filtering.
///
/// ```bash
/// mvn test -Dgroups=!extremely-slow
/// ```
///
/// @see Tag
/// @see TestTags#EXTREMELY_SLOW
/// @since 1.0.0
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Tag("extremely-slow")
public @interface ExtremelySlow {
}
