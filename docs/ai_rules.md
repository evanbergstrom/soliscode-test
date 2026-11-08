# AI Rules

## Project Rules

### Coding Rules
Code generation should follow the rules defined in [Coding Standards](./common-coding-standards.md)

### Design Rules
Design should follow the rules defined in [Common Design Principles](./common-design-principles.md)
Design for this project should follow the rules defined in [Project Design Rules](./project-design.md)

### Documentation Rules
Documentation should follow the general rules defined in [Common Documentation Standards](./common-doc-standards.md)
Documentation for this project should follow the rules defined in [Project Documentation Rules](./project-doc-standards.md)

### Testing Rules
Tests should follow the rules defined in [Testing Standards](./common-test-standards.md)
Tests for this project should follow the rules defined in [Project Testing Rules](./project-test-standards.md)

## Coding Instructions

1. **Follow Existing Patterns**: Look for established patterns in the codebase before creating new approaches
2. **Maintain Principle Consistency**: New code should align with these documented principles
3. **Update Documentation**: If you change behavior, update relevant documentation and examples
4. **Test Coverage**: Ensure new functionality includes appropriate test coverage
5. **Error Messages**: Make error messages specific and actionable for library users
6. **Code Readability**: Write clean, readable code with meaningful variable and method names
7. **Performance Optimization**: Optimize for performance where necessary, but avoid premature optimization
8. **Code Reviews**: Participate in code reviews to ensure quality and consistency
9. **Documentation**: Keep documentation up-to-date with code changes
10. **Version Control**: Use version control effectively, with clear commit messages and branches

## Development Notes
- Uses JUnit 5 exclusively with custom extensions and dynamic test generation
- Checkstyle enforced with custom rules in `checkstyle.xml`
- Test naming convention: `*Test.java` (not `Test*.java`)
- JavaDoc validation is strict: `-failOnWarnings` and `-Werror` enabled
- GitLab CI/CD configured for automated testing, security scanning, and Maven Central publishing
- Semantic versioning with conventional commits required (`npm run commit`)
- Test coverage tracked with JaCoCo, reports in `target/site/jacoco/`

## Build System

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
