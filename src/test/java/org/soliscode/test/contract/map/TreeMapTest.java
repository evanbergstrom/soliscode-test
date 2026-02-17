package org.soliscode.test.contract.map;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.navigablemap.NavigableMapContract;
import org.soliscode.test.contract.sequencedmap.SequencedMapMethods;
import org.soliscode.test.provider.IntegerProvider;
import org.soliscode.test.provider.MapProvider;
import org.soliscode.test.provider.MapProviders;
import org.soliscode.test.provider.StringProvider;

import java.util.TreeMap;

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
        implements NavigableMapContract<Integer, String, TreeMap<Integer,String>> {

    /// Constructs a new `TreeMapTest` instance and configures unsupported methods.
    ///
    /// Specifically, this constructor disables:
    /// - [SequencedMapMethods#PUT_FIRST]
    /// - [SequencedMapMethods#PUT_LAST]
    public TreeMapTest() {
        doesNotSupportMethod(SequencedMapMethods.PUT_FIRST);
        doesNotSupportMethod(SequencedMapMethods.PUT_LAST);
    }

    /// Returns the provider for creating and populating `TreeMap` instances.
    ///
    /// This implementation uses [MapProviders#provideTreeMap] to provide a
    /// [MapProvider] that creates [TreeMap] instances with [Integer] keys
    /// and [String] values.
    ///
    /// @return a [MapProvider] for `TreeMap`
    @Override
    public @NonNull MapProvider<Integer, String, TreeMap<Integer, String>> provider() {
        return MapProviders.provideTreeMap(new IntegerProvider(), new StringProvider());
    }
}
