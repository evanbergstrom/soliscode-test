# First steps

In this tutorial you run the `List` contract against a list class of your own, see it pass, and then see it catch a fault you put in the class. It takes about **15 minutes**.

**You learn how to:**
- write a test class that runs a [contract](../glossary.md)
- tell the contract how to make the instances it tests, through a [provider](../glossary.md)
- read the failure a contract reports

**Before you begin:** [install SolisCode Test](installation.md), and have a Maven project on Java 25 with the dependency added.

## Step 1: Add a list class

The contract needs a class to test. Use one of your own if you have one; this tutorial uses `ForwardingList`, a list that keeps its elements in an `ArrayList`. Put it in `src/main/java`, in a package of yours:

```java
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ForwardingList<E> extends AbstractList<E> {

    private final List<E> elements;

    public ForwardingList() {
        this.elements = new ArrayList<>();
    }

    public ForwardingList(final Collection<? extends E> initial) {
        this.elements = new ArrayList<>(initial);
    }

    @Override
    public E get(final int index) {
        return elements.get(index);
    }

    @Override
    public E set(final int index, final E element) {
        return elements.set(index, element);
    }

    @Override
    public void add(final int index, final E element) {
        elements.add(index, element);
        modCount++;
    }

    @Override
    public E remove(final int index) {
        E removed = elements.remove(index);
        modCount++;
        return removed;
    }

    @Override
    public void clear() {
        modCount++;
        elements.clear();
    }

    @Override
    public int size() {
        return elements.size();
    }
}
```

## Step 2: Run the `List` contract against it

Add this test class in `src/test/java`, in the same package:

```java
import com.soliscode.test.AbstractTest;
import com.soliscode.test.contract.list.ListContract;
import com.soliscode.test.contract.support.WithIntegerElement;
import com.soliscode.test.provider.CollectionProvider;
import com.soliscode.test.provider.CollectionProviders;

class ForwardingListTest extends AbstractTest
        implements ListContract<ForwardingList<Integer>, Integer>, WithIntegerElement {

    @Override
    public CollectionProvider<ForwardingList<Integer>, Integer> provider() {
        return CollectionProviders.from(ForwardingList::new, ForwardingList::new, ForwardingList::new,
                elementProvider());
    }
}
```

Run it:

```sh
mvn -B -Dtest=ForwardingListTest test
```

You should see:

```text
[INFO] Tests run: 139, Failures: 0, Errors: 0, Skipped: 0
```

> **What just happened?** The class has no test methods of its own: `ListContract` holds them, as default methods JUnit runs in any class that implements it. `provider()` tells the contract how to make lists (empty, as a copy, and from a collection: the three constructor references), and `WithIntegerElement` gives it `Integer` elements. `AbstractTest` keeps which of the list's optional methods it supports: all of them, here. For the full story, see [Contracts](../concepts/contracts.md).

## Step 3: See it catch a fault

Change `set` in `ForwardingList` so that it returns the new element, rather than the one it replaced, as `List.set` must:

```java
    @Override
    public E set(final int index, final E element) {
        elements.set(index, element);
        return element;
    }
```

Run the test again. The contract fails, and names what `List` requires:

```text
[ERROR] Failures:
[ERROR]   ForwardingListTest.setWhenIndexIsValidReplacesElement set(int, E) returns the element replaced ==> expected: <0> but was: <10>
[ERROR] Tests run: 139, Failures: 1, Errors: 0, Skipped: 0
```

Put `set` back as it was, and the test passes again.

## Summary

You have run the 139 tests of the `List` contract against a list of your own, and seen one of them catch a fault. On the way you learned to:
- write a test class that runs a contract, by implementing it
- tell the contract how to make the instances it tests, through a provider
- read the failure a contract reports: the test method's name, and what the interface requires

The complete code for this tutorial: [`ForwardingList.java`](../../../src/test/java/com/soliscode/test/guide/ForwardingList.java) and [`ForwardingListTest.java`](../../../src/test/java/com/soliscode/test/guide/ForwardingListTest.java), which this library's own build compiles and runs.

## See also

- [Next steps](next-steps.md)
- [API reference](../reference/api.md)
