package org.soliscode.test.contract.list;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Disabled;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.DoesNotPermitNulls;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.provider.CollectionProvider;
import org.soliscode.test.provider.CollectionProviders;

import java.util.ArrayList;

@Disabled
public class ArrayListNoNullsContractTest extends AbstractTest
        implements ListContract<Integer, ArrayList<Integer>>, WithIntegerElement, DoesNotPermitNulls {

    @Override
    public @NonNull CollectionProvider<Integer, ArrayList<Integer>> provider() {
        return CollectionProviders.provideArrayList(elementProvider());
    }
}
