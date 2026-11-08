package com.soliscode.test.contract.map;

import java.util.HashMap;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.provider.IntegerProvider;
import com.soliscode.test.provider.MapProvider;
import com.soliscode.test.provider.MapProviders;
import com.soliscode.test.provider.StringProvider;

/// **Contract-based tests for `HashMap`**
///
/// This class provides the `HashMap`-specific implementation of the [MapContract],
/// ensuring that [HashMap] correctly adheres to the [java.util.Map] specification
/// within the SolisCode test framework.
///
/// ## Test Scope
/// This class tests all methods defined in the [java.util.Map] interface as implemented
/// by [HashMap], including:
/// - Basic operations: `put`, `get`, `remove`, `size`, `isEmpty`
/// - Bulk operations: `putAll`, `clear`
/// - Collection views: `keySet`, `values`, `entrySet`
/// - Default methods: `getOrDefault`, `forEach`, `replaceAll`, `putIfAbsent`, etc.
/// - Functional operations: `computeIfAbsent`, `computeIfPresent`, `compute`, `merge`
///
/// ## Configuration
/// The tests use [IntegerProvider] for keys and [StringProvider] for values.
///
/// @author evanbergstrom
/// @see HashMap
/// @see MapContract
/// @since 1.0.0
public class HashMapTest extends AbstractTest implements MapContract<HashMap<Integer,String>, Integer, String>  {

    /// Default constructor.
    public HashMapTest() {
        super();
    }

    /// Returns the objectProvider for creating and populating `HashMap` instances.
    ///
    /// This implementation uses [MapProviders#provideHashMap] to provide a
    /// [MapProvider] that creates [HashMap] instances with [Integer] keys
    /// and [String] values.
    ///
    /// @return a [MapProvider] for `HashMap`
    @Override
    public MapProvider<Integer, String, HashMap<Integer, String>> provider() {
        return MapProviders.provideHashMap(new IntegerProvider(), new StringProvider());
    }
}
