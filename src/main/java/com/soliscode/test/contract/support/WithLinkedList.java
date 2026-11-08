package com.soliscode.test.contract.support;

import java.util.LinkedList;

import com.soliscode.test.provider.CollectionProvider;
import com.soliscode.test.provider.CollectionProviders;

/// Mixing for a collection contract class that implements a collection objectProvider for instances of LinkedList.
/// ```java
///    public class LinkedListTest extends AbstractTest implements ListContract<Integer, LinkedList<Integer>>
///         WithLinkedList<Integer>, WithIntegerElement {
/// ```
/// @param <E> the element type for the collection being tested.
/// @author evanbergstrom
/// @since 1.0
/// @see CollectionProvider
/// @see LinkedList
public interface WithLinkedList<E> extends CollectionProviderSupport<LinkedList<E>, E> {

    // Returns a collection objectProvider that can be used to create instances of [LinkedList].
    /// @return a `LinkedList` collection objectProvider.
    // NonApiType: this provides LinkedList itself, for the contract tests of LinkedList.
    @SuppressWarnings("NonApiType")
    @Override
    default CollectionProvider<LinkedList<E>, E> provider() {
        return CollectionProviders.provideLinkedList(elementProvider());
    }
}
