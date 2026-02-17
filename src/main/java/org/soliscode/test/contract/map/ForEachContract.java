package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the `forEach` method of a `Map`**
///
/// This interface defines tests for the [forEach(BiConsumer)][Map#forEach] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Map]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a map's `forEach` implementation
/// correctly performs the specified action for each entry in the map.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyMapForEachTest implements ForEachContract<String, String, MyMap<String, String>> {
///     @Override
///     public MapProvider<String, String, MyMap<String, String>> provider() {
///         return MyMap::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [Map] and [org.soliscode.test.provider.MapProvider] implementations being tested.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @see Map#forEach
/// @since 1.0.0
public interface ForEachContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that the [forEach][Map#forEach] method iterates over all entries in the map.
    ///
    /// @see Map#forEach
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("forEach(BiConsumer) iterates over all entries in the map")
    default void forEach_whenCalled_iteratesOverAllEntries() {
        Map<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (supportsMethod(MapMethods.FOR_EACH)) {
            Map<K, V> visited = new HashMap<>();
            AtomicInteger count = new AtomicInteger();
            map.forEach((k, v) -> {
                visited.put(k, v);
                count.incrementAndGet();
            });
            assertEquals(map.size(), count.get());
            assertEquals(map, visited);
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.forEach((k, v) -> { }));
        }
    }
}
