package org.soliscode.test.contract.dynamic;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Disabled;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.breakable.Break;
import org.soliscode.test.breakable.BreakableIterable;
import org.soliscode.test.contract.DynamicContract;
import org.soliscode.test.contract.iterable.IterableContract;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.provider.CollectionProvider;

/// Dynamically created instance of `IterableContract` that will run on instances of `BreakableIterator` with a
/// specified break. This contract will be expected to fail on certain tests depending on the specific break that
/// is being used.
@Disabled("Used only for dynamic test generation")
public final class DynamicBrokenIterableContract // private so it is not discovered by JUnit
        extends DynamicContract<BreakableIterable<Integer>, CollectionProvider<Integer, BreakableIterable<Integer>>>
        implements IterableContract<Integer, BreakableIterable<Integer>>, WithIntegerElement {

   /// Constructs a new `DynamicBrokenIterableContract` instance for dynamically testing iterable contracts on
   /// `BreakableIterator` instances. This constructor is used to define a contract expected to fail under certain
   /// conditions depending on the specified break and method.
   ///
   /// @param b The [Break] representing specific intentional deviations from expected iterable behavior for testing
   ///          purposes. Must not be null.
   /// @param m The [InterfaceMethod] representing a method that is either unsupported or behaves incorrectly under
   ///          certain conditions. Must not be null.
   public DynamicBrokenIterableContract(final @NonNull Break b, final @NonNull InterfaceMethod m) {
        super(b, m, (breaks, statuses, test) ->
                BreakableIterable.iterableProvider(WithIntegerElement.PROVIDER, breaks, statuses));
    }
}
