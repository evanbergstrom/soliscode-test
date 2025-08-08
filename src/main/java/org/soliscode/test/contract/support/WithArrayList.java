package org.soliscode.test.contract.support;

import org.jetbrains.annotations.NotNull;
import org.soliscode.test.provider.CollectionProvider;
import org.soliscode.test.provider.CollectionProviders;

import java.util.ArrayList;

/// Mixing for a collection contract class that implements a collection provider for instances of ArrayList.
/// ```java
///    public class ArrayListTest extends AbstractTest implements ListContract<Integer, ArrayList<Integer>>
///         WithArrayList<Integer>, WithIntegerElement {
/// ```
/// @param <E> the element type for the collection being tested.
/// @author evanbergstrom
/// @since 1.0
/// @see CollectionProvider
/// @see ArrayList
public interface WithArrayList<E> extends CollectionProviderSupport<E, ArrayList<E>> {

    // Returns a collection provider that can be used to create instances of [ArrayList].
    /// @return an `ArrayList` collection provider.
    @Override
    default @NotNull CollectionProvider<E, ArrayList<E>> provider() {
        return CollectionProviders.provideArrayList(elementProvider());
    }
}
