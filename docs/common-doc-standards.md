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
- **Test Documentation**: Tests should also be documented
  - Test documentation should not include examples, performance notes, or thread safety

### Standard Class-Level Javadoc Structure
Public classes and interfaces should follow this structural pattern in their Javadoc:
1.  **Summary Line**: A single, concise sentence describing the primary purpose.
2.  **Detailed Description**: A more thorough explanation of behavior and responsibilities.
3.  **Standard Sections** (using `##` headers):
    - `## Purpose` (optional if covered in description): Why the class exists.
    - `## Usage Examples`: At least one runnable or representative code block showing typical use.
    - `## Thread Safety`: Explicit guarantee or warning about concurrent access.
    - `## Implementation Notes` (optional): Technical details for extenders or maintainers.

### Code Block Standards
- Use triple backticks (```java) for all multi-line code blocks.
- Ensure examples use standard project types (e.g., `String`, `Integer`) and follow established coding patterns.
- Prefer complete, self-contained examples where feasible.