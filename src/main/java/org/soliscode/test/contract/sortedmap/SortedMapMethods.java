package org.soliscode.test.contract.sortedmap;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.InterfaceMethod;

import java.util.SortedMap;

/// Enumeration of [SortedMap] methods that can be toggled in [SortedMapContract].
///
/// This enum allows users of the [SortedMapContract] to specify which methods of the [SortedMap]
/// interface are supported by the implementation being tested.
///
/// ## Usage Examples
///
/// Using [SortedMapMethods] to check for method support in a test implementation:
///
/// ```java
/// public interface FirstKeyContract<K, V, M extends SortedMap<K, V>> extends MapContractSupport<K, V, M> {
///
///     @Test
///     default void testFirstKeyOnEmptyMap() {
///         if (supportsMethod(SortedMapMethods.FIRST_KEY)) {
///             // Test tha firstKey() method works...
///         } else {
///             assertThrows(UnsupportedOperationException.class, map::firstKey);
///         }
///     }
/// ```
///
/// ## Thread Safety
///
/// This enum is thread-safe as it is immutable.
///
/// @see SortedMapContract#supportsMethod(InterfaceMethod)
/// @see SortedMap
/// @author evanbergstrom
/// @since 1.0
public enum SortedMapMethods implements InterfaceMethod {

    /// The method [SortedMap#comparator()].
    COMPARATOR("comparator()"),

    /// The method [SortedMap#firstKey()].
    FIRST_KEY("firstKey()"),

    /// The method [SortedMap#lastKey()].
    LAST_KEY("lastKey()"),

    /// The method [SortedMap#headMap(Object)].
    HEAD_MAP("headMap(Object)"),

    /// The method [SortedMap#tailMap(Object)].
    TAIL_MAP("tailMap(Object)"),

    /// The method [SortedMap#subMap(Object, Object)].
    SUB_MAP("subMap(Object, Object)");

    private final String name;

    SortedMapMethods(final @NonNull String name) {
        this.name = name;
    }

    /// {@inheritDoc}
    @Override
    public @NonNull String methodName() {
        return name;
    }
}
