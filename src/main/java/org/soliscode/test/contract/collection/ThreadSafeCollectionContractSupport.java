package org.soliscode.test.contract.collection;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Collection;

import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContains;

public interface ThreadSafeCollectionContractSupport<E, C extends Collection<E>>
        extends CollectionContractSupport<E, C> {

    default void assertCollectionStillWorks(final @NonNull C collection) {
        E e = elementProvider().createInstance();
        collection.add(e);
        assertContains(e, collection);
    }
}
