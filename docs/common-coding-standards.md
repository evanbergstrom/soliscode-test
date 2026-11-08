# Java Coding Standards & AI Enforcement Rules (Java 25+)
> **Audience:** Humans and AI coding assistants  
> **Scope:** This document defines binding coding standards and enforcement rules for all Java code in this repository.
>
> **AI Enforcement Clause:**  
> AI-generated or AI-modified code **must comply** with the enforcement rules below.  
> If a request conflicts with these rules, the AI must **refuse** or **propose a compliant alternative**.

## Part I — AI Enforcement Rules (Authoritative)

### Language & Platform
- Target **Java 25+**.
- Do not use preview features unless explicitly instructed.
- Prefer standardized Java language features over third-party abstractions.

### Core Design Rules
- Immutability is the default.
- Prefer `record` for immutable data carriers.
- Prefer sealed types for closed hierarchies.
- Public APIs must not expose third-party types.
- **No Lombok.**

### Nullability
- Do not return `null` from public APIs.
- Use `Optional<T>` only for return values.
- Do not use `Optional` for fields or parameters.
- Validate inputs eagerly using `Objects.requireNonNull`.

### API Stability
- Do not change public APIs unless explicitly instructed.
- Constructors must validate arguments.
- Prefer static factory methods when validation or multiple creation paths exist.

### Concurrency
- Public APIs must clearly document thread safety.
- Prefer immutability and `java.util.concurrent` primitives.
- Avoid `synchronized` on public methods.
- Avoid blocking calls in shared execution paths.

### Performance
- Avoid unnecessary object allocation in hot paths.
- Prefer primitives over boxed types.
- Avoid reflection in core logic.
- Streams must not reduce clarity or performance.

### Collections & Streams
- Prefer interfaces over concrete collection types.
- Avoid exposing mutable collections.
- Stream pipelines must be short, readable, and side-effect free.

### Method Structure
- A method must have one primary responsibility.
- Methods should not exceed 10 decision points (if, switch case, loop, catch, ternary).
- Methods should not exceed 40 lines, excluding comments and blank lines.

### Control Flow
- Prefer early returns over nested conditionals.
- Nesting depth must not exceed 3 levels.
- Avoid else blocks when the if branch returns or throws.
- switch expressions are preferred over if / else if chains.

### Boolean Logic
- A single conditional expression must not exceed 2 logical operators (&&, ||).
- Complex boolean logic must be extracted into:
    - a well-named boolean method, or
    - a domain object predicate

### Error Handling
- Guard clauses must be used for validation failures.
- Exception handling blocks must be minimal and non-branching.
- A method must not both:
    - perform business logic, and
    - decide error-handling strategy

#### Exceptions
- Use unchecked exceptions for programming errors.
- Use checked exceptions only for recoverable conditions.
- Preserve original causes when wrapping exceptions.
- Never swallow exceptions.

## Loops & Iteration
- Avoid nested loops beyond 2 levels.
- Extract loop bodies with conditionals into named methods.
- Prefer collection operations (map, filter, anyMatch) when they reduce branching.

### Switch Usage
- switch expressions must:
    - return a value, or
    - delegate to named methods
- No fall-through logic.
Each case must be ≤ 5 lines or delegate.

### Decomposition
- Repeated conditionals must be extracted into a single method.
- Conditionals over type must be replaced with:
- polymorphism, or
- sealed type pattern matching

### Logging
- Do not log sensitive data.
- Do not log stack traces at INFO or lower.
- Logging must not alter program behavior.

### Testing
- Use **JUnit 5**.
- Tests must be deterministic and isolated.
- Avoid sleeps; use proper synchronization primitives.
- Prefer real objects over mocks.

### Formatting & Style
- Follow IDE auto-formatting.
- Line length ≤120 characters.
- One top-level class per file.
- Minimize nesting depth.

### Forbidden Practices
- No global mutable state.
- No reflection-based serialization in core paths.
- No silent exception handling.
- No dependency additions without justification.

## Part II — Human-Readable Java Coding Standards

### 1. Philosophy
These standards prioritize:
- Correctness over cleverness
- Immutability over mutation
- Clarity over brevity
- Long-term maintainability over short-term convenience

Java 25+ is treated as a **modern, expressive language**, not a legacy runtime.

### 2. Language Features
Use modern Java features where they improve readability and safety:
- Records for data modeling
- Sealed classes/interfaces for domain boundaries
- Pattern matching for `instanceof` and `switch`
- Switch expressions
- Text blocks for multiline literals
- `var` for obvious local inference

Avoid legacy APIs and constructs that obscure intent.

### 3. API Design
Public APIs must be:
- Explicit
- Predictable
- Safe by default

#### Immutability
- All public types should be immutable unless explicitly documented.
- Defensive copies must be used when accepting or returning collections.

#### Constructors vs Factories
Use static factories when:
- Validation is required
- Naming improves clarity
- Multiple creation strategies exist

### 4. Null Handling
- `null` is an implementation detail, not an API contract.
- Public methods must communicate absence using `Optional`.
- Fields and parameters must never be `Optional`.

### 5. Error Handling
- Checked exceptions represent recoverable, expected conditions.
- Unchecked exceptions represent bugs or contract violations.
- Exception messages must be precise and actionable.
- Wrapping exceptions must preserve the original cause.

### 6. Concurrency
Thread safety must be:
- Designed deliberately
- Documented explicitly
- Verified through tests

Prefer immutability and concurrency primitives over locks.
Blocking calls must be isolated and intentional.

### 7. Performance
Performance decisions must be:
- Measured
- Documented
- Context-aware

Avoid unnecessary allocation, boxing, and reflection.
Streams are encouraged when they improve clarity, not by default.

### 8. Collections
- Always code to interfaces.
- Do not leak mutable state.
- Choose collection implementations intentionally.
- Avoid deeply nested or complex stream pipelines.

### 9. Naming & Readability
Names must:
- Reflect intent
- Avoid abbreviations
- Read naturally at call sites

Boolean methods should read like predicates.

### 10. Documentation
Public APIs require Javadoc that explains:
- Behavior
- Thread safety
- Nullability
- Performance characteristics

Use `@implNote` for implementation details.


### 11. Testing
Tests are part of the API contract.
They must be:
- Fast
- Deterministic
- Isolated

Concurrency tests must use coordination primitives instead of timing assumptions.

### 12. Dependencies
Dependencies must:
- Be minimal
- Be justified
- Never leak into public APIs

Prefer JDK functionality whenever possible.

### 13. Logging
Logging is observational, not behavioral.
Logs must never:
- Leak sensitive data
- Change execution flow
- Replace proper error handling

### 14. Prohibited Practices
The following are explicitly disallowed:
- Lombok
- Global mutable state
- Reflection-based serialization
- Silent exception handling
- Undocumented public mutability

### 15. AI Usage Guidance
AI-generated code must:
- Follow this document without exception
- Prefer clear, idiomatic Java
- Refuse unsafe or non-compliant requests
- Propose compliant alternatives when necessary
