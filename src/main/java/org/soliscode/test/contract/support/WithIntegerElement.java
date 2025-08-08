package org.soliscode.test.contract.support;

import org.jetbrains.annotations.NotNull;
import org.soliscode.test.provider.IntegerProvider;
import org.soliscode.test.provider.ObjectProvider;

/// Mixing for a collection contract class that implements a element provider for instances of [Integer].
/// ```java
///    public class ArrayListTest extends AbstractTest implements ListContract<Integer, ArrayList<Integer>>
///         WithArrayList<Integer>, WithIntegerElement {
/// ```
/// @author evanbergstrom
/// @since 1.0
public interface WithIntegerElement extends ElementProviderSupport<Integer> {

    /// Returns an elements provider for instances of [Integer]..
    /// @return an `Integer` element provider.
    default @NotNull ObjectProvider<Integer> elementProvider() {
        return new IntegerProvider();
    }
}
