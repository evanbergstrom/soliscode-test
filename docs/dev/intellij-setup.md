
# IntelliJ IDEA Setup

## Coverage
When running with coverage,it impacts the execution of the tests:
- Changes method size
- Affects inlining
- Alters JIT behavior
- Changes lock timing
- Slows execution slightly

The thread safety tests can be negatively impacted by these changes. To avoid transient failures, configure the
coverage to skip the non-deterministic tests.

1. Go top Settings -> Build, Execution, Deployment -> Coverage -> Exclude
2. Add the org.soliscode.test.annotations.NonDeterministic annotation