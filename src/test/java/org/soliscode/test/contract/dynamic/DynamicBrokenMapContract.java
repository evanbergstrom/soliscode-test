package org.soliscode.test.contract.dynamic;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Disabled;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.breakable.Break;
import org.soliscode.test.breakable.BreakableMap;
import org.soliscode.test.contract.DynamicContract;
import org.soliscode.test.contract.map.MapContract;
import org.soliscode.test.contract.support.WithIntegerKeyAndStringValue;
import org.soliscode.test.provider.MapProvider;

/// A dynamic contract for testing broken map implementations.
///
/// This class extends [DynamicContract] to create test instances with specific
/// [Break] configurations. It is used to verify that the contract tests fail
/// as expected when map semantics are violated.
@Disabled("Used only for dynamic test generation")
public final class DynamicBrokenMapContract // private so it is not discovered by JUnit
        extends DynamicContract<BreakableMap<Integer, String>, MapProvider<Integer, String, BreakableMap<Integer, String>>>
        implements MapContract<Integer, String, BreakableMap<Integer, String>>, WithIntegerKeyAndStringValue<BreakableMap<Integer, String>> {

    /// Creates a new instance of `DynamicBrokenMapContract` with the specified break and method.
    ///
    /// @param b the break to apply to the map
    /// @param m the method being tested
    public DynamicBrokenMapContract(final @NonNull Break b, final @NonNull InterfaceMethod m) {
        super(b, m, (breaks, statuses, test) -> BreakableMap.mapProvider(
                WithIntegerKeyAndStringValue.KEY_PROVIDER,
                WithIntegerKeyAndStringValue.VALUE_PROVIDER,
                breaks, statuses));
    }
}
