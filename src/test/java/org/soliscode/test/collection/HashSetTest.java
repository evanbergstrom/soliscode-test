package org.soliscode.test.collection;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.DoesNotPermitDuplicates;
import org.soliscode.test.contract.collection.CollectionContract;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.provider.CollectionProvider;
import org.soliscode.test.provider.CollectionProviders;

import java.util.HashSet;

public class HashSetTest extends AbstractTest
        implements CollectionContract<Integer, HashSet<Integer>>, WithIntegerElement, DoesNotPermitDuplicates {

    @Override
    public @NonNull CollectionProvider<Integer, HashSet<Integer>> provider() {
        return CollectionProviders.from(HashSet::new, HashSet::new, HashSet::new, elementProvider());
    }
}
