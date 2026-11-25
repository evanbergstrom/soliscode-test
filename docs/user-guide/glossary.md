# Glossary

**Break**
: A named fault of a breakable, a `Break` constant on its class, such as `BreakableList.GET_ALWAYS_RETURNS_THE_LAST_ELEMENT`. Its description says the one thing it changes. See [Breakables and their breaks](concepts/breakables.md).

**Breakable**
: An implementation of a JDK interface that keeps the interface's contract until a break is set. The library tests its contracts against them. See [Breakables and their breaks](concepts/breakables.md).

**Contention**
: Many threads calling one object's methods at the same time. `ContentionCoordinator` makes it, to expose races. See [Bounded thread-safety tests](concepts/bounded-thread-safety-tests.md).

**Contract**
: An interface of default `@Test` methods that tests any class implementing one JDK interface, as the JDK documents it. A test class runs it by implementing it. See [Contracts](concepts/contracts.md).

**Element provider**
: The object provider of a collection's elements, or a map's keys and values, which a test class returns from `elementProvider()`. See [Providers](concepts/providers.md).

**Method contract**
: The contract of one method of an interface, such as `contract.list.GetContract`. An interface's contract combines its methods' contracts. See [Contracts](concepts/contracts.md).

**Object provider**
: An `ObjectProvider<T>`: makes values of one type from a seed, and equal copies of them. See [Providers](concepts/providers.md).

**Optional method**
: A method an interface lets an implementation refuse with `UnsupportedOperationException`, such as `Collection.add`. A test class says which its class refuses with `doesNotSupportMethod`. See [Configuration](reference/configuration.md#optional-methods).

**Phase interval**
: The number of calls after which a `ContentionCoordinator`'s threads wait for each other, so that they keep contending on a busy machine. 0, the default, means no phases. See [Configuration](reference/configuration.md#contentioncoordinator).

**Provider**
: The factory a contract gets the instances it tests from, and their elements: a collection provider, a map provider, or the provider of another interface. See [Providers](concepts/providers.md).

**Scenario**
: A `PerformanceScenario` of the SolisCode performance gate: a measurement, run at each of its points, that returns metrics. See [How to write a performance test](how-to/write-a-performance-test.md).

**Seed**
: The `long` an object provider makes an instance from. The same seed gives an equal instance, and different seeds unequal ones, so a test's data is the same on every run.

**Thread-safety contract**
: A contract that extends an interface's contract with tests of its methods under contention, such as `ThreadSafeCollectionContract`. See [How to write a thread-safety test](how-to/write-a-thread-safety-test.md).

**Weakly consistent**
: Of an iterator: it never throws `ConcurrentModificationException`, and reflects some, all or none of the changes made to its source after it was made, as the `java.util.concurrent` package summary defines it. `WeaklyConsistentIteratorContract` tests one.

## See also

- [The SolisCode parent's glossary](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.17/docs/guide/09-glossary.md)
- [Concepts](concepts/README.md)
