package org.soliscode.test.contract.dynamic;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Disabled;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.breakable.Break;
import org.soliscode.test.breakable.BreakableQueue;
import org.soliscode.test.contract.DynamicContract;
import org.soliscode.test.contract.queue.QueueContract;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.provider.CollectionProvider;

/// Dynamically created instance of `QueueContract` that will run on instances of `BreakableQueue` with a
/// specified break.
@Disabled("Used only for dynamic test generation")
public final class DynamicBrokenQueueContract // private so it is not discovered by JUnit
        extends DynamicContract<BreakableQueue<Integer>, CollectionProvider<Integer, BreakableQueue<Integer>>>
        implements QueueContract<Integer, BreakableQueue<Integer>>, WithIntegerElement {

    /// Constructs a dynamically created `DynamicBrokenQueueContract` instance to test the behavior of
    /// `BreakableQueue` collections under a specified `Break` scenario. This class provides a test contract
    /// for verifying that `BreakableQueue` instances adhere to or intentionally violate the expected
    /// behaviors, depending on the defined breaks. It uses the `InterfaceMethod` to identify specific
    /// methods affected.
    ///
    /// @param b The specific [Break] instance that defines the intentional deviation in behavior.
    ///          Must not be `null`. Example: A break that makes the iterator skip the first element.
    /// @param m The [InterfaceMethod] representing the method being tested or marked as unsupported.
    ///          Must not be `null`. For example, this could be a method name like "add" or "remove".
    public DynamicBrokenQueueContract(final @NonNull Break b, final @NonNull InterfaceMethod m) {
        super(b, m, (breaks, statuses, test) ->
                BreakableQueue.queueProvider(WithIntegerElement.PROVIDER, breaks, statuses));
    }

    @Override
    public boolean supportsMethod(final @NonNull InterfaceMethod method) {
        return super.supportsMethod(method);
    }

    @Override
    public void doesNotSupportMethod(final @NonNull InterfaceMethod method) {
        super.doesNotSupportMethod(method);
    }
}
