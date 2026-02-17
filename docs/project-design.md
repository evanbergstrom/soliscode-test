# Library Design Principles
The following are the design principles for SolisCode Test. they include the design principles as described in
the [Design Principles](common-design-principles.md) document.

## Project Overview

SolisCode Test is a Java library for testing classes that implement standard JDK interfaces. It provides:
- **Contracts**: Test suites for standard interface methods (Object, Iterator, Iterable, Collection, List, etc.)
- **Interface Restrictions**: Wrapper classes that restrict objects to only interface methods
- **Assertions**: Specialized assertions for testing collection behavior
- **Providers**: Classes that generate test elements and collections
- **Breakables**: Collection implementations that can be programmatically broken for testing

## Version and Environment Requirements

### Java Version
- **Target Version**: Java 25 (minimum required version)
- **Version Compatibility**: Support graceful degradation on older Java versions where possible
- **Modern Features**: Utilize modern Java features (pattern matching, records, etc.) appropriately

### Dependencies
- **JUnit 5**: Primary testing framework dependency (version 5.x required)
- **JSpecify**: Use for null safety annotations (`@NonNull`, `@Nullable`)
- **Maven**: Build system with standard directory layout
- **Checkstyle**: Code quality enforcement with custom rules

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

- The collection classes used to test the Contract classes should implement the Breakable interface.
- These classes should all be in the org.soliscode.test.breakable package.
- Each Breakable class should have constants that define each break that is supported.
    - They should appear in the breakable class in which they are first supported.
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
  interface that the contract is for. These packages will all be at a single level under the contract package
- Contract classes will be provided for two different cases:
    1. **Methods**: A contract class will be provided for each method in an interface containing the tests needed
       to insure that the specifications for that method are satisfied.
    2. **Interfaces**: A contract class will be provided for the interface that will extend the method interfaces
       So that an implementing test class can simply extend the interface contract that is necessary.

## Method Contract Classes
- Method contracts will be provided for each method in an interface containing the tests needed to insure that the
  specifications for that method are satisfied.
- These contracts will be named after the method they test, with the suffix "test"
- These contracts will be in the same package as the contract class for the interface they test
- These contracts will extend the appropriate contract support class. These classes 
    - For classes that extend the Collection interface, the support class will be CollectionContractSupport
    - For classes that extend the Map interface, the support class will be MapContractSupport
- These contracts will be thread-safe unless explicitly documented otherwise.
- These contracts should be annotated with `@Test` to allow JUnit to discover them.
- These contracts should test for compliance with all behavior described in the interface specification.
    - They should test that all exceptions are thrown when appropriate and have the correct type.
- All method tests should work with classes that do not support that method
    - Support for the method should be checked using the checkMethodSupport() method.
For collection classes, these contracts should support classes that do not accept null elements.
    - Support for null elements should be checked using the permitsNulls method.
    - If the method takes an element as a parameter and nulls aren't supported, it should test that a NullPointerException is thrown.

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