package org.soliscode.test.contract.map;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.provider.IntegerProvider;
import org.soliscode.test.provider.MapProvider;
import org.soliscode.test.provider.MapProviders;
import org.soliscode.test.provider.StringProvider;

import java.util.HashMap;

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
public class HashMapTest extends AbstractTest implements MapContract<Integer, String, HashMap<Integer,String>>  {

    /// Returns the provider for creating and populating `HashMap` instances.
    ///
    /// This implementation uses [MapProviders#provideHashMap] to provide a
    /// [MapProvider] that creates [HashMap] instances with [Integer] keys
    /// and [String] values.
    ///
    /// @return a [MapProvider] for `HashMap`
    @Override
    public @NonNull MapProvider<Integer, String, HashMap<Integer, String>> provider() {
        return MapProviders.provideHashMap(new IntegerProvider(), new StringProvider());
    }
}
