# How to test a collection or map with its contract

**Goal:** a test class that runs the contract of the interface your `Collection`, `List`, `Set` or `Map` implements, and that tells the contract what your class permits.

## Before you start

- SolisCode Test 0.1.5 or later
- You have done [First steps](../getting-started/first-steps.md)

## Steps

1. Pick the contract of the most specific interface your class implements. Each contract includes those of the interfaces it extends, so `ListContract` runs `CollectionContract`'s tests too:

   | Your class implements | Contract | Its package |
   |---|---|---|
   | `Collection` | `CollectionContract<C, E>` | `com.soliscode.test.contract.collection` |
   | `List` | `ListContract<L, E>` | `com.soliscode.test.contract.list` |
   | `Set` | `SetContract<S, E>` | `com.soliscode.test.contract.set` |
   | `Map` | `MapContract<M, K, V>` | `com.soliscode.test.contract.map` |

   The [API reference](../reference/api.md#contracts) lists the others (`SortedSet`, `Deque`, `NavigableMap`, the concurrent ones and the rest).

2. Write a test class that extends `AbstractTest` and implements the contract, with your class as its first type argument. For a collection, give it its elements through `elementProvider()`, here from `WithIntegerElement`; for a map, give its keys and values to the provider. Return a provider made by `CollectionProviders.from` or `MapProviders.from`, from your class's no-argument constructor, its copy constructor and its constructor from a collection or map. Here a `LinkedHashMap` stands in for your map:

   ```java
   import java.util.LinkedHashMap;

   import com.soliscode.test.AbstractTest;
   import com.soliscode.test.contract.map.MapContract;
   import com.soliscode.test.provider.MapProvider;
   import com.soliscode.test.provider.MapProviders;
   import com.soliscode.test.provider.Providers;

   class InventoryMapTest extends AbstractTest
           implements MapContract<LinkedHashMap<String, Integer>, String, Integer> {

       @Override
       public MapProvider<String, Integer, LinkedHashMap<String, Integer>> provider() {
           return MapProviders.from(LinkedHashMap::new, LinkedHashMap::new, LinkedHashMap::new,
                   Providers.stringProvider(), Providers.integerProvider());
       }
   }
   ```

3. Tell the contract what your class does not do, where the interface lets it refuse:
   - It refuses `null`: for a collection, implement `DoesNotPermitNulls`; for a map, override `permitNullKeys()` or `permitNullValues()` to return `false`.
   - It refuses an argument of the wrong type with `ClassCastException`: implement `DoesNotPermitIncompatibleTypes`.
   - It holds no duplicates: implement `DoesNotPermitDuplicates` (a `Set`'s test always implements it).
   - It cannot be changed: call `doesNotSupportModification()` in the constructor (a collection contract; for a map, call `doesNotSupportMethod` for each method that changes it).
   - One optional method throws `UnsupportedOperationException`: call `doesNotSupportMethod(...)` in the constructor with its constant, such as `ListMethods.SET` or `CollectionMethods.REMOVE_IF` (from `com.soliscode.test.methods`).

   Here an unmodifiable `LinkedHashSet` stands in for your set:

   ```java
   import java.util.Collections;
   import java.util.LinkedHashSet;
   import java.util.Set;

   import com.soliscode.test.AbstractTest;
   import com.soliscode.test.contract.DoesNotPermitDuplicates;
   import com.soliscode.test.contract.set.SetContract;
   import com.soliscode.test.contract.support.WithIntegerElement;
   import com.soliscode.test.provider.CollectionProvider;
   import com.soliscode.test.provider.CollectionProviders;

   class UniqueTagSetTest extends AbstractTest
           implements SetContract<Set<Integer>, Integer>, WithIntegerElement, DoesNotPermitDuplicates {

       UniqueTagSetTest() {
           doesNotSupportModification();
       }

       @Override
       public CollectionProvider<Set<Integer>, Integer> provider() {
           return CollectionProviders.wrap(
                   CollectionProviders.from(LinkedHashSet::new, LinkedHashSet::new, LinkedHashSet::new,
                           elementProvider()),
                   Collections::unmodifiableSet);
       }
   }
   ```

   > **Note:** `CollectionProviders.wrap` makes each instance with the inner provider and passes it through the function, which is how a wrapper (unmodifiable, synchronized, checked) is tested. `MapProviders.wrap` does the same for maps.

4. Run the class with `mvn -B -Dtest=<YourClass>Test test`.

## Check that it worked

Maven reports the contract's tests, and no failure:

```text
[INFO] Tests run: 58, Failures: 0, Errors: 0, Skipped: 0
```

That is `InventoryMapTest`; `UniqueTagSetTest` runs 99. A failure names the test method, and its message says what the interface requires; the method's `@DisplayName` says it as a sentence.

## Troubleshooting

| Symptom | Fix |
|---|---|
| Tests of `add`, `remove` or another optional method fail with `UnsupportedOperationException` | Your class does not support the method: call `doesNotSupportMethod` for it, or `doesNotSupportModification()` for all of them |
| Tests whose names mention `Null` fail with `NullPointerException` | Your class refuses `null`: implement `DoesNotPermitNulls` for a collection, or override `permitNullKeys()` and `permitNullValues()` for a map |
| `equals` or `hashCode` tests fail for a wrapper such as `Collections.synchronizedCollection` | It inherits them from `Object`, as its documentation says: call `doesNotSupportMethod(ObjectMethods.EQUALS)` and `doesNotSupportMethod(ObjectMethods.HASH_CODE)` |

## See also

- [Configuration reference](../reference/configuration.md)
- [Contracts](../concepts/contracts.md)
