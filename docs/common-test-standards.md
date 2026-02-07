# Testing and Validation

## Design for Testability
- Avoid static dependencies that complicate testing
- Provide test utilities for common setup scenarios
- Make internal state observable for testing when necessary

## Input Validation
- Validate inputs at API boundaries with clear error messages
- Fail fast on invalid configurations
- Provide builder validation to catch errors early

## Unit Testing
- Each class should have a corresponding unit test that provides high coverage of the class being tested.
- Each test should test for a single concern so that the cause of any failure is easy to identify.
- Unit tests should never rely on execution timing for any tests, including when testing asynchronous or multi-threaded
  implementations or algorithms.
- Unit testing should include testing for all expected error conditions.
- All unit test methods should have a descriptive method name.

## Naming
JUnit 5 test methods MUST follow the exact naming pattern:

    methodName_condition_expectedResult

Requirements:
- Exactly three name segments
- Exactly two underscores
- Each segment:
    - starts with a lowercase letter
    - contains only alphanumeric characters
- No leading or trailing underscores
- No extra underscores
- No `test` prefix

Regex (authoritative):
```texrt
    ^[a-z][a-zA-Z0-9]*_[a-z][a-zA-Z0-9]*_[a-z][a-zA-Z0-9]*$
```

If a method is annotated with a JUnit 5 test annotation and does not match this format,
the AI agent MUST suggest a rename.

## Documentation
- All unit test methods should be documented using both javadoc (to be consistent with the library code) and inline
  in the code where necessary to clarify the logic being tested.
- All test classes should include a Javadoc header with a description of the class and any relevant links.
    - The documentation should include a @see tag with the method being tested.
    - The documentation should include the exceptions that could be thrown
        - The cause of any AssertionFailedErrors can be "if any exceptions failed"  
- All tests should include the @DisplayName annotation with a descriptive name.

## Annotation Ordering
When declaring a JUnit 5 test method, annotations MUST be ordered top-to-bottom according to the following precedence groups:
1. Classification / Filtering 
    - Annotations that describe what kind of test this is or how it should be selected excluded. 
    - Includes:
        - @Tag 
        - Custom meta-annotations composed of @Tag (e.g. @ConcurrencyTest, @SlowTest)
        - @Disabled, @DisabledOnOs, @DisabledOnJre, etc.
        - These annotations control when and where the test runs.
2. Execution Control
    - Annotations that modify how the test is executed.
    - Includes:
        - @RepeatedTest
        - @ParameterizedTest
        - @Timeout
        - @Execution
        - Conditional execution annotations (@EnabledOnOs, @EnabledIfEnvironmentVariable, etc.)
        - These annotations control scheduling, repetition, and runtime behavior. 
3. Documentation / Identification
    - Annotations that exist only for human readability.
    - Includes:
        - @DisplayName
        - @DisplayNameGeneration
    - These annotations do not affect execution semantics.
4. Test Declaration
  - The annotation that declares the test itself.
  - Includes:
      - @Test
      - @RepeatedTest (if not already used above)
      - @ParameterizedTest (if not already used above)
      - Exactly one test declaration annotation MUST be present.


## Testing Framework Integration
- **Contract Testing**: Use provider pattern to enable parameterized testing across different implementations
- **Break Testing**: Systematically test both normal behavior and controlled contract violations
- **Provider Pattern**: Implement providers for consistent test data generation across test classes
- **Framework Compliance**: All test classes should integrate with the SolisCode testing framework patterns
- **Isolation**: Tests should be independent and not rely on execution order or shared state
- **Error Scenarios**: Include comprehensive testing of error conditions and edge cases