package org.soliscode.test.contract.support;

import org.jspecify.annotations.NonNull;
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

    /// A constant instance of [ObjectProvider] that supplies elements of type [Integer].
    ///
    /// This variable serves as a reusable provider for generating or supplying `Integer` instances
    /// in testing scenarios or wherever a customizable [ObjectProvider] implementation is required.
    /// It is specifically defined for use cases involving [Integer] data types and complies
    /// with the `ElementProviderSupport<Integer>` contract.
    ///
    /// **Note:** This implementation relies on [IntegerProvider], which acts
    /// as the concrete provider of `Integer` elements.
    ///
    /// @see IntegerProvider
    /// @see WithIntegerElement
    /// @since 1.0
    ObjectProvider<Integer> PROVIDER = new IntegerProvider();

    /// Returns an elements provider for instances of [Integer].
    /// @return an `Integer` element provider.
    default @NonNull ObjectProvider<Integer> elementProvider() {
        return new IntegerProvider();
    }
}

