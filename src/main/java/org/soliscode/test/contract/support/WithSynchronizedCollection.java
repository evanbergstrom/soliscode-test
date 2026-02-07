package org.soliscode.test.contract.support;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.provider.CollectionProvider;
import org.soliscode.test.provider.CollectionProviders;

import java.util.Collection;

public interface WithSynchronizedCollection<E> extends CollectionProviderSupport<E, Collection<E>> {

    // Returns a collection provider that can be used to create instances of [ArrayList].
    /// @return an `ArrayList` collection provider.
    @Override
    default @NonNull CollectionProvider<E, Collection<E>> provider() {
        return CollectionProviders.provideSynchronizedCollection(elementProvider());
    }
}
