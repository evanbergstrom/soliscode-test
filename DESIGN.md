# General Design Principles

This document outlines the core design principles and philosophy guiding this library's development.

## Core Philosophy

**Principle of Least Surprise**: The API should behave as users naturally expect. Method names, parameter ordering, and
return values should follow established Java conventions and patterns. 

**Fail Fast**: Detect and report errors as early as possible, preferably at compile time, otherwise at the point of
misconfiguration rather than during execution.

## API Design

### Immutability by Default
- All public data structures should be immutable unless mutability is essential
- Use builder patterns for complex object construction
- Prefer `final` fields and parameters and defensive copying

### Null Safety
- Never return `null` from public methods unless implementing an interface from a library.
- Use `Optional<T>` for potentially absent values
- Validate all public method parameters with clear error messages
- Document nullability expectations with `@Nullable` and `@NonNull` annotations

### Method Design
- Methods should do one thing well (Single Responsibility)
- Prefer pure functions where possible (no side effects)
- Use descriptive parameter names, especially for boolean parameters
- Limit parameter counts (consider parameter objects for >5 parameters)

### Exception Handling
- Use checked exceptions only for recoverable conditions users must handle
- Provide specific exception types with actionable error messages
- Include context in exception messages (what failed, why, how to fix)

## Threading and Concurrency

### Thread Safety Strategy
- **Document thread safety guarantees** for each public class
- Prefer immutable objects to avoid synchronization overhead
- When mutable state is necessary, use clear synchronization strategies
- Avoid exposing internal synchronization mechanisms

### Async Design
- Long-running operations should offer async alternatives
- Use standard Java concurrency patterns (`CompletableFuture`, etc.)
- Provide both blocking and non-blocking variants where appropriate

## Performance Considerations

### Memory Management
- Minimize object allocation in hot paths
- Provide ways to reuse expensive objects (pools, builders)
- Document memory implications of API choices

### Lazy Initialization
- Defer expensive computations until actually needed
- Cache computations only when benefit clearly outweighs memory cost
- Make caching behavior transparent to users

## Extensibility and Maintenance

### Extension Points
- Use interfaces and abstract classes to define extension points
- Prefer composition over inheritance for user customization
- Provide hooks for common customization needs

### Backwards Compatibility
- Follow semantic versioning strictly
- Deprecate before removal with clear migration paths
- Maintain compatibility within major versions

### Dependencies
- Minimize external dependencies, especially transitive ones
- Pin dependency versions to avoid version conflicts
- Provide ways to substitute key dependencies (logging, serialization)

## Version and Environment Requirements

### Java Version
- **Target Version**: Java 23 (minimum required version)
- **Version Compatibility**: Support graceful degradation on older Java versions where possible
- **Modern Features**: Utilize modern Java features (pattern matching, records, etc.) appropriately

### Dependencies
- **JUnit 5**: Primary testing framework dependency (version 5.x required)
- **JSpecify**: Use for null safety annotations (`@NonNull`, `@Nullable`)
- **Maven**: Build system with standard directory layout
- **Checkstyle**: Code quality enforcement with custom rules

## Testing and Validation

### Design for Testability
- Avoid static dependencies that complicate testing
- Provide test utilities for common setup scenarios
- Make internal state observable for testing when necessary

### Input Validation
- Validate inputs at API boundaries with clear error messages
- Fail fast on invalid configurations
- Provide builder validation to catch errors early

### Unit Testing
- Each class should have a corresponding unit test that provides high coverage of the class being tested.
- Unit tests should never rely on execution timing for any tests, including when testing asynchronous or multi-threaded
  implementations or algorithms.
- Unit testing should include testing for anticipated error conditions.
- All unit test methods should have a descriptive method name.
- Unit tests should be documented using both javadoc (to be consistent with the library code) and inline in the code
  where necessary to clarify the logic being tested.

### Testing Framework Integration
- **Contract Testing**: Use provider pattern to enable parameterized testing across different implementations
- **Break Testing**: Systematically test both normal behavior and controlled contract violations
- **Provider Pattern**: Implement providers for consistent test data generation across test classes
- **Framework Compliance**: All test classes should integrate with the SolisCode testing framework patterns
- **Isolation**: Tests should be independent and not rely on execution order or shared state
- **Error Scenarios**: Include comprehensive testing of error conditions and edge cases

## Documentation Standards

### Self-Documenting Code
- Use intention-revealing names for classes, methods, and variables
- Prefer composition and small methods over complex implementations
- Include usage examples in class-level Javadoc

### API Documentation
- Document not just what methods do, but when and why to use them
- Include performance characteristics for non-trivial operations
- Provide complete examples showing typical usage patterns.

### JavaDoc Standards
- **Format**: Use markdown-style JavaDoc comments with `///` syntax
- **No HTML**: Avoid HTML tags in favor of markdown formatting
- **Comprehensive Examples**: Include practical code examples in all public API documentation
- **Real-World Context**: Document not just functionality but practical usage scenarios
- **Cross-References**: Use `@see` tags to link related functionality
- **Parameter Documentation**: Document all parameters with `@param` including constraints
- **Exception Documentation**: Use `@throws` to document all possible exceptions and their conditions
- **Performance Notes**: Include performance characteristics for non-trivial operations
- **Thread Safety**: Explicitly document thread safety guarantees
- **Since Tags**: Use `@since` to indicate version when functionality was introduced

# Library Design Principles

## Library Design

- This library is meant to be used as an extension of the JUnit 5 library.
- Code style and Idioms should conform to those in the JUnit 5 library as much as possible.

## Package Designs

### Assertion Methods (org.soliscode.test.assertions)
- Assertion methods should extend the ones available in Junit 5.
- They can include methods that test for conditions that could be implemented with Junit 5 methods (*i.e.* assertEquals,
  assertNotEquals, assertTrue, assertFalse) that provide better error information for common use cases.
- They should be organized by the types of values that they test (*e.g.* collections, strings, etc.)
- They should use the Assertion implementation pattern from JUnit 5, with one class per assertion, possibly with closely
  related variations.
- They should be available through a utility class that aggregates all of the related assertions. 

### Breakable classes (org.soliscode.test.breakable)

- All of the collection classes used to test the Contract classes should implement the Breakable interface.
- These classes should all be in the org.soliscode.test.breakable package.
- Each Breakable class should have constants that define each break that is supported. They should appear in the
  breakable class in which they are first supported.
- All of the breakable classes should provide a builder class.

### Break Mechanism Design
- **Purpose**: Enable testing of code that must handle non-compliant or corrupted implementations
- **Implementation**: Breaks are implemented as feature flags that modify specific method behaviors
- **Naming Convention**: Break constants should use descriptive names that clearly indicate the violation
- **Scope**: Each break should target a specific contract violation, not general "brokenness"
- **Documentation**: Every break must document exactly what normal behavior it overrides
- **Testing**: Breaks should be testable both for their effect and for proper isolation
- **Builder Integration**: All breaks should be configurable through the builder pattern

### Contract classes (org.soliscode.test.contract)

- All contract classes will have "Contract" at the end of their name
- All contract classes will be in a subpackage of the org.soliscode.test.contract package corresponding to the
  interface that the contract is for.
- Contract classes will be provided for two different cases:
  1. **Methods**: A contract class will be provided for each method in an interface containing all of the tests needed
                  to insure that all of the specifications for that method are satisfied.
  2. **Interfaces**: A contract class will be provided for the interface that will extend all of the method interfaces
                     So that an implementing test class can simply extend the interface contract that is necessaary.

### Interface Narrowing Classes (org.soliscode.test.interfaces)
- Interface narrowing classes are used to narrow the type of an object so that any code that optimizes for different
  interfaces will be forced to use the code path for the specified interface.
- Interface narrowing classes will have the same name as the interface with the suffix "Only"
- These classes will all be in the org.soliscode.test.interfaces package.
- The Interfaces utility class will be the primary api for users, and should allow construction of any Interface
  narrowing class from an object implementing the corresponding interface.

### Provider Classes (org.soliscode.test.provider)
- Provider classes are meant to enable Contracts to be parameterized for the type of class being tested, and for the
  element type for collection classes being tested.
- Providers should be implemented for all the basic value types in the JDK (*e.g.* Integer, Double, String, etc.) as
  well as the temporal types (*e.g.* LocalDate, LocalDateTime).

### Utility Classes (org.soliscode.test.util)
- Utility classes provide common functionality needed across the testing framework
- **Collection Utilities**: `UsesCollections` provides mutable collection factory methods for testing
- **Reflection Utilities**: `ReflectionTestUtils` offers interface discovery and type analysis capabilities
- **Runtime Utilities**: `RuntimeTestUtils` handles cross-version Java compatibility detection
- **String Utilities**: `UncachedString` provides string implementations that avoid interning for controlled testing
- **Test Operations**: Various utility classes for common testing operations and data manipulation
- Utility classes should follow the standard Java utility class pattern (private constructor, static methods)
- All utility classes should be thread-safe unless explicitly documented otherwise 

# Principle Evolution

These principles should evolve with the library. When adding new principles or changing existing ones:

1. Document the rationale for the change
2. Update this document to reflect the changes
3. Ensure consistency across the existing codebase
4. Consider backwards compatibility implications

# For AI Assistants

When working with this codebase:

1. **Follow Existing Patterns**: Look for established patterns in the codebase before creating new approaches
2. **Maintain Principle Consistency**: New code should align with these documented principles
3. **Update Documentation**: If you change behavior, update relevant documentation and examples
4. **Test Coverage**: Ensure new functionality includes appropriate test coverage
5. **Error Messages**: Make error messages specific and actionable for library users