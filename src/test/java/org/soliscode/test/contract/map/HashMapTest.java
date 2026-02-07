package org.soliscode.test.contract.map;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.provider.IntegerProvider;
import org.soliscode.test.provider.MapProvider;
import org.soliscode.test.provider.MapProviders;
import org.soliscode.test.provider.StringProvider;

import java.util.HashMap;

public class HashMapTest extends AbstractTest implements MapContract<Integer, String, HashMap<Integer,String>>  {

    @Override
    public @NonNull MapProvider<Integer, String, HashMap<Integer, String>> provider() {
        return MapProviders.provideHashMap(new IntegerProvider(), new StringProvider());
    }
}
