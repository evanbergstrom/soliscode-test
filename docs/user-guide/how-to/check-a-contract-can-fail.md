# How to check that a contract can fail, with a breakable

**Goal:** a test that shows a test method you wrote fails against an implementation with a known fault, so that you know it is not a test that passes whatever the class does.

## Before you start

- SolisCode Test 0.1.1 or later
- A test method of your own that tests an interface the library has a breakable for: here a small contract of your own for `List`

## Steps

1. Write your test method as a contract: a default `@Test` method in an interface that gets its instances from `provider()`. This one checks that `get(0)` returns the first element:

   ```java
   import java.util.List;

   import org.junit.jupiter.api.DisplayName;
   import org.junit.jupiter.api.Test;

   import com.soliscode.test.contract.support.CollectionProviderSupport;

   import static org.junit.jupiter.api.Assertions.assertEquals;

   interface FirstElementContract<L extends List<E>, E> extends CollectionProviderSupport<L, E> {

       @Test
       @DisplayName("get(0) returns the first element")
       default void getZeroReturnsTheFirstElement() {
           List<E> elements = provider().elementProvider().createUniqueInstances(3);
           L list = provider().createInstance(elements);
           assertEquals(elements.getFirst(), list.get(0));
       }
   }
   ```

2. Find the break that your test method should catch. Each breakable class in `com.soliscode.test.breakable` names its breaks as `Break` constants, each with a description of the one fault it makes; `BreakableList.GET_ALWAYS_RETURNS_THE_LAST_ELEMENT` is the fault this method is for.

3. Write a test class that runs your contract against a correct class, and one that runs it against the breakable with that break, and assert that the second fails. Build the breakable with its `Builder`, add the break, and turn the builder into a provider with `BreakableBuilder.collectionProvider`:

   ```java
   import java.util.ArrayList;

   import org.junit.jupiter.api.DisplayName;
   import org.junit.jupiter.api.Test;
   import org.opentest4j.AssertionFailedError;

   import com.soliscode.test.AbstractTest;
   import com.soliscode.test.breakable.BreakableBuilder;
   import com.soliscode.test.breakable.BreakableList;
   import com.soliscode.test.contract.support.WithArrayList;
   import com.soliscode.test.contract.support.WithIntegerElement;
   import com.soliscode.test.provider.CollectionProvider;

   import static org.junit.jupiter.api.Assertions.assertThrows;

   class FirstElementContractTest extends AbstractTest
           implements FirstElementContract<ArrayList<Integer>, Integer>, WithArrayList<Integer>, WithIntegerElement {

       private static final class BrokenGet extends AbstractTest
               implements FirstElementContract<BreakableList<Integer>, Integer>, WithIntegerElement {

           @Override
           public CollectionProvider<BreakableList<Integer>, Integer> provider() {
               BreakableList.Builder<Integer> builder = new BreakableList.Builder<Integer>()
                       .addBreak(BreakableList.GET_ALWAYS_RETURNS_THE_LAST_ELEMENT);
               return BreakableBuilder.collectionProvider(builder, elementProvider());
           }
       }

       @Test
       @DisplayName("The contract fails when get returns the last element")
       void contractFailsWhenGetReturnsTheLastElement() {
           assertThrows(AssertionFailedError.class, new BrokenGet()::getZeroReturnsTheFirstElement);
       }
   }
   ```

   > **Note:** `BrokenGet` is `private` so that JUnit does not run it as a test class of its own, where its test would fail: JUnit runs any static nested class with `@Test` methods that is not private, whatever launcher finds it (an IDE, a package scan), even though Maven's Surefire leaves out nested classes by their name. The outer class runs the contract against `ArrayList` (from `WithArrayList`), which must pass, and calls the contract's method on `BrokenGet` directly, which must fail. A breakable with no break is a correct implementation, so `new BreakableList.Builder<Integer>()` alone passes too.

4. Run the test class.

## Check that it worked

Both tests pass: the contract's method against `ArrayList`, and the check that it fails against the broken list.

```text
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
```

Remove the `addBreak` line and the check fails, because the method passes against a correct list: that is the failure this test exists to report.

## Troubleshooting

| Symptom | Fix |
|---|---|
| The check fails: `Expected org.opentest4j.AssertionFailedError to be thrown, but nothing was thrown` | Your test method does not catch the break: it does not test what the break changes, or the instances it makes do not show the fault (with one element, the first is the last) |
| The check fails with an exception other than `AssertionFailedError` | The break makes the method throw, or your test method failed with an error rather than an assertion: assert the exception your method reports, or `AssertionError` |

## See also

- [API reference](../reference/api.md#breakables), which lists the breakables
- [Breakables and their breaks](../concepts/breakables.md)
