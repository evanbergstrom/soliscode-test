package com.soliscode.test.contract.map;

import java.util.TreeMap;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.contract.navigablemap.NavigableMapContract;
import com.soliscode.test.contract.sequencedmap.SequencedMapMethods;
import com.soliscode.test.provider.IntegerProvider;
import com.soliscode.test.provider.MapProvider;
import com.soliscode.test.provider.MapProviders;
import com.soliscode.test.provider.StringProvider;

/// **Contract-based tests for `TreeMap`**
///
/// This class provides the `TreeMap`-specific implementation of the [NavigableMapContract],
/// ensuring that [TreeMap] correctly adheres to the [java.util.NavigableMap] specification
/// within the SolisCode test framework.
///
/// ## Test Scope
/// This class tests all methods defined in the [java.util.NavigableMap] interface as implemented
/// by [TreeMap], including:
/// - Navigation operations: `lowerEntry`, `floorEntry`, `ceilingEntry`, `higherEntry`, etc.
/// - Sub-map operations: `subMap`, `headMap`, `tailMap`
/// - Descending views: `descendingMap`, `descendingKeySet`
/// - All standard [java.util.Map] and [java.util.SortedMap] operations.
///
/// ## Configuration
/// - The tests use [IntegerProvider] for keys and [StringProvider] for values.
/// - [SequencedMapMethods#PUT_FIRST] and [SequencedMapMethods#PUT_LAST] are explicitly
///   marked as unsupported, as [TreeMap] does not support these operations.
///
/// @author evanbergstrom
/// @see TreeMap
/// @see NavigableMapContract
/// @since 1.0.0
public class TreeMapTest extends AbstractTest
        implements NavigableMapContract<TreeMap<Integer,String>, Integer, String> {

    /// Default constructor.
    public TreeMapTest() {
        super();
        doesNotSupportMethod(SequencedMapMethods.PUT_FIRST);
        doesNotSupportMethod(SequencedMapMethods.PUT_LAST);
    }

    /// Returns the objectProvider for creating and populating `TreeMap` instances.
    ///
    /// This implementation uses [MapProviders#provideTreeMap] to provide a
    /// [MapProvider] that creates [TreeMap] instances with [Integer] keys
    /// and [String] values.
    ///
    /// @return a [MapProvider] for `TreeMap`
    @Override
    public MapProvider<Integer, String, TreeMap<Integer, String>> provider() {
        return MapProviders.provideTreeMap(new IntegerProvider(), new StringProvider());
    }
}
