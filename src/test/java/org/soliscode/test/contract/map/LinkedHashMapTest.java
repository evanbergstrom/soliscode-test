package org.soliscode.test.contract.map;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.sequencedmap.SequencedMapContract;
import org.soliscode.test.provider.IntegerProvider;
import org.soliscode.test.provider.MapProvider;
import org.soliscode.test.provider.MapProviders;
import org.soliscode.test.provider.StringProvider;

import java.util.LinkedHashMap;

public class LinkedHashMapTest extends AbstractTest
        implements SequencedMapContract<Integer, String, LinkedHashMap<Integer,String>> {

    @Override
    public @NonNull  MapProvider<Integer, String, LinkedHashMap<Integer, String>> provider() {
        return MapProviders.provideLinkedHashMap(new IntegerProvider(), new StringProvider());
    }
}
