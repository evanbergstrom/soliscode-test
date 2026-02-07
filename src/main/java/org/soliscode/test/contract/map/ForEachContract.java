package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [Map#forEach(java.util.function.BiConsumer)] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface ForEachContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `forEach()` iterates over all entries in the map.
    @Test
    @DisplayName("Test forEach iterates over all entries in the map")
    default void testForEach() {
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
