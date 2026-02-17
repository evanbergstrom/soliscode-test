package org.soliscode.test.contract.sequencedmap;

import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.contract.map.MapContract;

import java.util.SequencedMap;

/// Contract for the [SequencedMap] interface.
///
/// ### Purpose
/// Verifies that a [SequencedMap] implementation adheres to the contract of the [SequencedMap] interface.
/// This contract covers sequencing-related methods like `putFirst`, `putLast`, `firstEntry`, `lastEntry`,
/// and sequenced view methods.
///
/// ### Usage Examples
///
/// #### Implementation
/// ```java
/// public class MySequencedMapTest implements SequencedMapContract<String, String, MySequencedMap<String, String>> {
///     @Override
///     public MapProvider<String, String, MySequencedMap<String, String>> provider() {
///         return new MySequencedMapProvider();
///     }
/// }
/// ```
///
/// ### Thread Safety
/// The tests in this contract are not guaranteed to be thread-safe. If the map implementation
/// is intended for concurrent use, additional thread-safety tests should be performed.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @see SequencedMap
/// @author evanbergstrom
/// @since 1.0.0
public interface SequencedMapContract<K, V, M extends SequencedMap<K, V>> extends MapContract<K, V, M>,
        PutFirstContract<K, V, M>,
        PutLastContract<K, V, M>,
        ReversedContract<K, V, M>,
        FirstEntryContract<K, V, M>,
        LastEntryContract<K, V, M>,
        PollFirstEntryContract<K, V, M>,
        PollLastEntryContract<K, V, M>,
        SequencedKeySetContract<K, V, M>,
        SequencedValuesContract<K, V, M>,
        SequencedEntrySetContract<K, V, M> {

    /// Checks if a given method is supported by the map implementation.
    ///
    /// @param method the method to check
    /// @return true if the method is supported, false otherwise
    /// @since 1.0.0
    @Override
    boolean supportsMethod(InterfaceMethod method);

    /// Marks a given method as not supported by the map implementation.
    ///
    /// @param method the method to mark as not supported
    /// @since 1.0.0
    @Override
    void doesNotSupportMethod(InterfaceMethod method);
}
