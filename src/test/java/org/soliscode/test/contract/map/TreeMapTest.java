package org.soliscode.test.contract.map;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.navigablemap.NavigableMapContract;
import org.soliscode.test.provider.IntegerProvider;
import org.soliscode.test.provider.MapProvider;
import org.soliscode.test.provider.MapProviders;
import org.soliscode.test.provider.StringProvider;

import java.util.TreeMap;

public class TreeMapTest extends AbstractTest
        implements NavigableMapContract<Integer, String, TreeMap<Integer,String>> {

    @Override
    public @NonNull MapProvider<Integer, String, TreeMap<Integer, String>> provider() {
        return MapProviders.provideTreeMap(new IntegerProvider(), new StringProvider());
    }
}
