package com.soliscode.test.contract.support;

import java.util.ArrayList;

import com.soliscode.test.provider.CollectionProvider;
import com.soliscode.test.provider.CollectionProviders;

/// Mixing for a collection contract class that implements a collection objectProvider for instances of ArrayList.
/// ```java
///    public class ArrayListTest extends AbstractTest implements ListContract<Integer, ArrayList<Integer>>
///         WithArrayList<Integer>, WithIntegerElement {
/// ```
/// @param <E> the element type for the collection being tested.
/// @author evanbergstrom
/// @since 1.0
/// @see CollectionProvider
/// @see ArrayList
public interface WithArrayList<E> extends CollectionProviderSupport<ArrayList<E>, E> {

    // Returns a collection objectProvider that can be used to create instances of [ArrayList].
    /// @return an `ArrayList` collection objectProvider.
    // NonApiType: this provides ArrayList itself, for the contract tests of ArrayList.
    @SuppressWarnings("NonApiType")
    @Override
    default CollectionProvider<ArrayList<E>, E> provider() {
        return CollectionProviders.provideArrayList(elementProvider());
    }
}
