package org.soliscode.test.collection;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.DoesNotPermitNulls;
import org.soliscode.test.contract.list.ListContract;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.provider.*;

import java.util.ArrayList;

@Disabled
public class ArrayListNoNullsContract extends AbstractTest
        implements ListContract<Integer, ArrayList<Integer>>, WithIntegerElement, DoesNotPermitNulls {

    @Override
    public @NotNull CollectionProvider<Integer, ArrayList<Integer>> provider() {
        return CollectionProviders.provideArrayList(elementProvider());
    }


}
