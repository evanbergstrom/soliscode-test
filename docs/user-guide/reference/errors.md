# Error reference

A contract's own failures are assertion failures that name the test method and say what the interface requires; their messages are the contract's, one for each test, and are not listed here. This page lists the errors the library's machinery reports, from the coordinator, the providers and the performance library.

## Error index

| Exception | Message (shortened) |
|---|---|
| [`AssertionError`](#contentioncoordinator-timeouts) | `Timeout: … workers didn't finish after … seconds` |
| [`AssertionError`](#contentioncoordinator-timeouts) | `Timeout: … workers were not ready … after … seconds` |
| [`AssertionError`](#operations-failed) | `Operations failed: …` |
| [`AssertionError`](#no-instance-found) | `no instance found that the collection does not contain after … attempts` |
| [`IllegalArgumentException`, `IllegalStateException`](#cannot-create-unique-instances) | `cannot create … unique instances, limit is …` |
| [`AssertionFailedError`](#the-operation-allocates) | `the operation allocates … bytes per run, more than the bound of …` |
| [`WrongException`](#wrongexception) | Thrown by a breakable on purpose |
| [`DeliberateException`](#deliberateexception) | Thrown by a task or future on purpose |

---

### ContentionCoordinator timeouts

**Message**

```text
Timeout: 17 (of 18) workers didn't finish after 5 seconds
Timeout: 2 (of 8) workers were not ready  after 5 seconds
```

With a phase interval, the first ends with the phase: `(phase 3: 4 workers registered, 2 arrived)`.

**Cause:** `ContentionCoordinator.execute` waits a bounded time for its threads to start and to finish. The threads did not finish in time: an operation deadlocked, waits for something that never comes, or is too slow for the number of iterations. When the threads do not finish, it checks for deadlocked threads before it reports the timeout, and reports them instead if there are any; when they are not ready in time, it reports that alone.

**Fix:**
1. Look for a deadlock, or for an operation that blocks waiting for another thread.
2. If the operations are only slow, give fewer iterations to `add`, or a longer shutdown timeout to the constructor.

**See also:** [Bounded thread-safety tests](../concepts/bounded-thread-safety-tests.md)

---

### Operations failed

**Message**

```text
Operations failed: 3
```

**Cause:** an operation threw in one or more of the coordinator's threads. Each exception is attached to the error as a suppressed exception.

**Fix:**
1. Read the suppressed exceptions in the report: each is one thread's failure.
2. A `ConcurrentModificationException` or an `ArrayIndexOutOfBoundsException` from your class under contention is usually the race the test is for.

**See also:** [How to write a thread-safety test](../how-to/write-a-thread-safety-test.md)

---

### No instance found

**Message**

```text
no instance found that the collection does not contain after 10000 attempts: its contains(Object) may be broken
```

**Cause:** `ObjectProvider.createInstanceNotIn` looked for an element that the collection does not hold, and every one it made was reported present. Either the collection's `contains` returns `true` for elements it does not hold, or the provider's seeds give equal instances.

**Fix:**
1. Check the class's `contains`.
2. Check that your provider's `createInstance(seed)` gives unequal instances for different seeds.

**See also:** [How to supply instances and elements](../how-to/supply-instances-and-elements.md)

---

### Cannot create unique instances

**Message**

```text
cannot create 100 unique instances, limit is 100
```

**Cause:** a test asked a provider for more unique instances than its `uniqueSizeLimit()`: `IllegalArgumentException` from `createUniqueInstances`, which checks the number first, or `IllegalStateException` from a unique-instance supplier, which counts as it goes.

**Fix:**
1. Raise `uniqueSizeLimit()` in your provider, if your type has that many distinct values.
2. Otherwise, make the test use fewer: a smaller `defaultSize()` for a map.

**See also:** [Providers](../concepts/providers.md)

---

### The operation allocates

**Message**

```text
the operation allocates 0.0044444444444444444 bytes per run, more than the bound of 0.0
```

**Cause:** `AllocationAssertion.assertAllocatesAtMost` measured more than the bound. A fraction of a byte a run is measurement noise; 16 bytes or more is an object.

**Fix:**
1. For "allocates nothing", use a bound of 1 byte a run, not 0.
2. Otherwise, find the allocation: a boxed value, a lambda that captures, an iterator.

**See also:** [How to write a performance test](../how-to/write-a-performance-test.md)

---

### WrongException

**Message:** the breakable's, where it gives one: a breakable throws it from a method whose break makes it throw the wrong exception.

**Cause:** a break such as `GET_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX` is set. `WrongException` is none of the exceptions the JDK's interfaces document, so a test that expects the right one fails.

**Fix:**
1. None, if you set the break: it is the failure you were looking for.
2. If you did not, check the breakable's builder.

**See also:** [Breakables and their breaks](../concepts/breakables.md)

---

### DeliberateException

**Message:** the one the test gives it.

**Cause:** a task or a future made by a provider failed on purpose, so that a test can tell that failure from any other. The `Future` and `ExecutorService` contracts use it.

**Fix:**
1. None: a contract expects it. If it escapes a test of yours, the test expected the task to succeed.

**See also:** [API reference](api.md#packages)

---

## See also

- [Troubleshooting and FAQ](../troubleshooting.md)
- [API reference](api.md)
