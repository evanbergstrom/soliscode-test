package org.soliscode.test.contract.collection;

import java.util.Collection;


public interface ThreadSafeCollectionContract<E, C extends Collection<E>> extends CollectionContract<E, C>,
        ThreadSafeClearContract<E, C>,
        ThreadSafeAddContract<E, C> {
}
