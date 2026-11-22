# How-to guides

These guides assume you have finished [First steps](../getting-started/first-steps.md) and want to do a specific task.

## Test an implementation of a JDK interface

- [How to test a collection or map with its contract](test-a-collection.md): run the `Collection`, `List`, `Set` or `Map` contract, and tell it what your class permits
- [How to supply instances and elements with a provider](supply-instances-and-elements.md): test with elements of your own type, from a provider you write
- [How to check that a contract can fail, with a breakable](check-a-contract-can-fail.md): show that a test method of yours fails against a known fault

## Test thread safety and performance

- [How to write a thread-safety test](write-a-thread-safety-test.md): run operations from many threads at once with a `ContentionCoordinator`, or a thread-safety contract
- [How to write a performance test](write-a-performance-test.md): bound an operation's allocation, and measure its latency in a scenario of the performance gate

## Can't find your task?

Check [Troubleshooting and FAQ](../troubleshooting.md), or ask in an [issue](https://gitlab.com/soliscode/soliscode-test/-/issues).

## See also

- [Concepts](../concepts/README.md)
- [API reference](../reference/api.md)
