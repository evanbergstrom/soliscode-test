# How to supply instances and elements with a provider

**Goal:** a contract that tests your class with elements of your own type, from a provider you write.

## Before you start

- SolisCode Test 0.1.1 or later
- A test class that runs a contract, as in [How to test a collection or map](test-a-collection.md)

## Steps

1. Use a ready-made element provider where one fits. `Providers` has `integerProvider()`, `longProvider()`, `shortProvider()`, `doubleProvider()`, `floatProvider()` and `stringProvider()`; `WithIntegerElement`, in `com.soliscode.test.contract.support`, gives a collection contract `Integer`s through `elementProvider()`.

2. For a type of your own, implement `ObjectProvider<T>`. It makes a default instance, an instance from a seed, and a copy that is equal to the original (for a mutable type, a separate object). Different seeds must give unequal instances, up to `uniqueSizeLimit()`:

   ```java
   import com.soliscode.test.provider.ObjectProvider;

   record Point(int x, int y) { }

   final class PointProvider implements ObjectProvider<Point> {

       @Override
       public Point defaultInstance() {
           return new Point(0, 0);
       }

       @Override
       public Point createInstance(final long seed) {
           return new Point((int) (seed % 1_000), (int) (seed / 1_000));
       }

       @Override
       public Point copyInstance(final Point other) {
           return new Point(other.x(), other.y());
       }

       @Override
       public long uniqueSizeLimit() {
           return 1_000_000;
       }
   }
   ```

3. Return it from the test class's `elementProvider()`, and pass `elementProvider()` to the collection provider. The collection provider is made from your class's three constructors, as `CollectionProviders.from` takes them: no argument, a copy, and from a collection:

   ```java
   import com.soliscode.test.AbstractTest;
   import com.soliscode.test.contract.list.ListContract;
   import com.soliscode.test.provider.CollectionProvider;
   import com.soliscode.test.provider.CollectionProviders;
   import com.soliscode.test.provider.ObjectProvider;

   class PointListTest extends AbstractTest implements ListContract<ForwardingList<Point>, Point> {

       @Override
       public ObjectProvider<Point> elementProvider() {
           return new PointProvider();
       }

       @Override
       public CollectionProvider<ForwardingList<Point>, Point> provider() {
           return CollectionProviders.from(ForwardingList::new, ForwardingList::new, ForwardingList::new,
                   elementProvider());
       }
   }
   ```

   > **Note:** if your class has no such constructors, write the `CollectionProvider` (or `MapProvider`) yourself, or pass lambdas to `from`. `CollectionProviders.wrap` puts each instance through a function, such as `Collections::unmodifiableList`. The other interfaces have providers of their own, made by `IteratorProviders`, `ComparatorProviders`, `FutureProviders`, `ExecutorServiceProviders` and the rest of `com.soliscode.test.provider`.

4. Run the test class.

## Check that it worked

The contract runs with your elements: `PointListTest` runs the same 139 tests as the `Integer` list of [First steps](../getting-started/first-steps.md).

```text
[INFO] Tests run: 139, Failures: 0, Errors: 0, Skipped: 0
```

## Troubleshooting

| Symptom | Fix |
|---|---|
| `IllegalArgumentException` or `IllegalStateException`: `cannot create … unique instances, limit is …` | A test needs more unique elements than `uniqueSizeLimit()` allows: raise it, if your type has that many distinct values |
| `AssertionError: no instance found that the collection does not contain after … attempts` | Your provider gives equal instances for different seeds, or the collection's `contains` is broken |

## See also

- [Error reference](../reference/errors.md)
- [Providers](../concepts/providers.md)
