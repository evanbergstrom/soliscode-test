package com.soliscode.test.contract.map;

import java.util.LinkedHashMap;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.contract.sequencedmap.SequencedMapContract;
import com.soliscode.test.provider.IntegerProvider;
import com.soliscode.test.provider.MapProvider;
import com.soliscode.test.provider.MapProviders;
import com.soliscode.test.provider.StringProvider;


/// **Contract-based tests for `LinkedHashMap`**
///
/// This class provides the `LinkedHashMap`-specific implementation of the [SequencedMapContract],
/// ensuring that [LinkedHashMap] correctly adheres to the [java.util.LinkedHashMap] specification
/// within the SolisCode test framework.
///
/// ## Test Scope
/// This class tests all methods defined in the [java.util.SequencedMap] interface as implemented
/// by [LinkedHashMap], including:
/// - Insertion order preservation and sequenced access.
/// - Navigation operations: `firstEntry`, `lastEntry`, `pollFirstEntry`, `pollLastEntry`, etc.
/// - Reversed views: `reversed`.
/// - All standard [java.util.Map] operations inherited from [java.util.LinkedHashMap].
///
/// ## Configuration
/// The tests use [IntegerProvider] for keys and [StringProvider] for values.
///
/// @author evanbergstrom
/// @see LinkedHashMap
/// @see SequencedMapContract
/// @since 1.0.0
public class LinkedHashMapTest extends AbstractTest
        implements SequencedMapContract<LinkedHashMap<Integer,String>, Integer, String> {

    /// Default constructor.
    public LinkedHashMapTest() {
        super();
    }

    /// Returns the objectProvider for creating and populating `LinkedHashMap` instances.
    ///
    /// This implementation uses [MapProviders#provideLinkedHashMap] to provide a
    /// [MapProvider] that creates [LinkedHashMap] instances with [Integer] keys
    /// and [String] values, maintaining insertion order.
    ///
    /// @return a [MapProvider] for `LinkedHashMap`
    @Override
    public MapProvider<Integer, String, LinkedHashMap<Integer, String>> provider() {
        return MapProviders.provideLinkedHashMap(new IntegerProvider(), new StringProvider());
    }
}
