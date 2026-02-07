package org.soliscode.test.contract.sortedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [SortedMap#comparator()] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface ComparatorContract<K, V, M extends SortedMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `comparator()` can be called.
    @Test
    @DisplayName("Test comparator can be called")
    default void testComparator() {
        SortedMap<K, V> map = provider().emptyInstance();
        if (supportsMethod(SortedMapMethods.COMPARATOR)) {
            map.comparator();
        } else {
            assertThrows(UnsupportedOperationException.class, map::comparator);
        }
    }
}
