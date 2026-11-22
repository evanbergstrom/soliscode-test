# SolisCode Test user guide

This guide is for **Java developers who write JUnit tests** who want to test their own implementations of the JDK's interfaces (a `List`, a `Map`, a `Comparator`, an `ExecutorService`) against what the JDK documents, test that their classes are thread-safe, and measure their performance. It assumes you know Java and Maven, and JUnit's `@Test`, and that you have read the JDK's documentation of the interface your class implements.

> It describes `main`. The newest release is 0.1.6; [Upgrading](upgrading.md) says what changed.

SolisCode Test has three parts, with a fourth beside them:
- **Test methods JUnit lacks**: assertions such as `assertThrowsAnyOf`, `assertNoDeadlocks` and the collection assertions, in `com.soliscode.test.assertions`.
- **Test suites for the JDK's interfaces**: a [contract](concepts/contracts.md) for each collection interface and for the other interfaces the library covers, with a [breakable](concepts/breakables.md) implementation of each and the [providers](concepts/providers.md) that make the instances a contract tests.
- **Thread-safety testing**: contracts for the concurrent interfaces, and a [`ContentionCoordinator`](how-to/write-a-thread-safety-test.md) for your own classes.
- **Performance testing**: the parts a scenario of the SolisCode performance gate is measured with, in `com.soliscode.test.performance`.

## How this guide is organised

The guide follows the [Diátaxis](https://diataxis.fr/) framework. Each part answers a different kind of question:

| Part | Use it when you want to… | Style |
|---|---|---|
| [Getting started](getting-started/installation.md) | **learn** by doing | Guided lessons, start to finish |
| [How-to guides](how-to/README.md) | **do** a specific task | Recipes for one goal each |
| [Reference](reference/configuration.md) | **look up** exact details | Complete, short, structured |
| [Concepts](concepts/README.md) | **understand** how and why | Discussion and background |

## Contents

### Getting started
1. [Installation](getting-started/installation.md)
2. [First steps](getting-started/first-steps.md)
3. [Next steps](getting-started/next-steps.md)

### How-to guides
See the [how-to index](how-to/README.md).

### Reference
- [Configuration](reference/configuration.md)
- [API reference](reference/api.md) (the Javadoc)
- [Error messages](reference/errors.md)

### Concepts
See the [concepts index](concepts/README.md).

### Other
- [Troubleshooting and FAQ](troubleshooting.md)
- [Upgrading](upgrading.md)
- [Glossary](glossary.md)

## See also

- [The project's README](../../README.md)
- [The design documents](../design/system/constitution.md), for people who change the library
