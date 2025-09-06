# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SolisCode Test is a Java library for testing classes that implement standard JDK interfaces. It provides:
- **Contracts**: Test suites for standard interface methods (Object, Iterator, Iterable, Collection, List, etc.)
- **Interface Restrictions**: Wrapper classes that restrict objects to only interface methods
- **Assertions**: Specialized assertions for testing collection behavior
- **Providers**: Classes that generate test elements and collections
- **Breakables**: Collection implementations that can be programmatically broken for testing

## Build System

This project uses Maven with Java 23. Key commands:

### Building and Testing
```bash
mvn compile                    # Compile source code
mvn test                       # Run all JUnit 5 tests
mvn verify                     # Run tests + checkstyle + coverage + dependency-check
mvn clean install             # Full build with all checks
mvn site                       # Generate project documentation site
```

### Test Execution
```bash
mvn test -Dtest=ArrayListTest                    # Run specific test class
mvn test -Dtest="*ContractTest"                  # Run tests matching pattern
mvn surefire:test                                # Alternative test runner
```

### Code Quality
```bash
mvn checkstyle:check           # Run checkstyle validation (config in checkstyle.xml)
mvn jacoco:report              # Generate test coverage reports
mvn dependency-check:check     # OWASP security vulnerability scan
```

### Release and Publishing
```bash
npm run commit                 # Interactive commit with conventional commits
npm run release               # Semantic release (CI only)
mvn deploy                    # Deploy to Maven Central (CI only)
```

## Architecture

### Core Testing Framework Structure
- `AbstractTest`: Base class providing optional method support configuration
- `OptionalMethodSupport`: Configuration for methods that may not be supported by implementations
- Contract interfaces define test suites for specific interface behaviors

### Package Organization
- `org.soliscode.test.contract.*`: Test contracts for standard interfaces
  - `collection/`: Collection interface contracts (add, remove, contains, etc.)
  - `list/`: List-specific contracts (get, set, indexOf, etc.)  
  - `iterable/`: Iterable and Iterator contracts
  - `object/`: Object method contracts (equals, hashCode, toString)
  - `sequenced/`: SequencedCollection contracts (addFirst, addLast, etc.)
  - `support/`: Base interfaces and utilities for contract implementation
- `org.soliscode.test.assertions.*`: Specialized assertion utilities
- `org.soliscode.test.breakable.*`: Broken collection implementations for testing
- `org.soliscode.test.interfaces.*`: Interface restriction utilities

### Contract Implementation Pattern
Test classes extend `AbstractTest` and implement contract interfaces:
```java
public class ArrayListTest extends AbstractTest 
        implements ListContract<Integer, ArrayList<Integer>>, WithIntegerElement {
    public ArrayListTest() {
        // Configure collection behavior
        permitNulls(true);
        permitDuplicates(true);
    }
}
```

### Provider System
- `CollectionProviderSupport`: Creates collection instances for testing
- `ElementProviderSupport`: Generates test elements
- `WithXxx` interfaces: Provide common element types (WithInteger, WithString, etc.)

## Development Notes

- Uses JUnit 5 exclusively with custom extensions and dynamic test generation
- Checkstyle enforced with custom rules in `checkstyle.xml`
- Test naming convention: `*Test.java` (not `Test*.java`)
- GitLab CI/CD configured for automated testing, security scanning, and Maven Central publishing
- Semantic versioning with conventional commits required
- Test coverage tracked with JaCoCo, reports in `target/site/jacoco/`