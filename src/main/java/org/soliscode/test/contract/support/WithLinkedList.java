package org.soliscode.test.contract.support;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.provider.CollectionProvider;
import org.soliscode.test.provider.CollectionProviders;

import java.util.LinkedList;

/// Mixing for a collection contract class that implements a collection provider for instances of LinkedList.
/// ```java
///    public class LinkedListTest extends AbstractTest implements ListContract<Integer, LinkedList<Integer>>
///         WithLinkedList<Integer>, WithIntegerElement {
/// ```
/// @param <E> the element type for the collection being tested.
/// @author evanbergstrom
/// @since 1.0
/// @see CollectionProvider
/// @see LinkedList
public interface WithLinkedList<E> extends CollectionProviderSupport<E, LinkedList<E>> {

    // Returns a collection provider that can be used to create instances of [LinkedList].
    /// @return a `LinkedList` collection provider.
    @Override
    default @NonNull CollectionProvider<E, LinkedList<E>> provider() {
        return CollectionProviders.provideLinkedList(elementProvider());
    }
}
