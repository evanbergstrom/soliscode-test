package org.soliscode.test.contract.navigablemap;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.InterfaceMethod;

/// Values used to identify collection class methods for use with the
/// 'CollectionContract#supportsMethod(InterfaceMethod)' method.
///
/// @author evanbergstrom
/// @since 1.0
/// @see org.soliscode.test.contract.collection.CollectionContract#supportsMethod(InterfaceMethod)
public enum NavigableMapMethods implements InterfaceMethod {

    /// The method [lowerEntry][java.util.NavigableMap#lowerEntry(Object)]
    LOWER_ENTRY("lowerEntry(Object)"),

    /// The method [lowerKey][java.util.NavigableMap#lowerKey(Object)]
    LOWER_KEY("lowerKey(Object)"),

    /// The method [floorEntry][java.util.NavigableMap#floorEntry(Object)]
    FLOOR_ENTRY("floorEntry(Object)"),

    /// The method [floorKey][java.util.NavigableMap#floorKey(Object)]
    FLOOR_KEY("floorKey(Object)"),

    /// The method [ceilingEntry][java.util.NavigableMap#ceilingEntry(Object)]
    CEILING_ENTRY("ceilingEntry(Object)"),

    /// The method [ceilingKey][java.util.NavigableMap#ceilingKey(Object)]
    CEILING_KEY("ceilingKey(Object)"),

    /// The method [higherEntry][java.util.NavigableMap#higherEntry(Object)]
    HIGHER_ENTRY("higherEntry(Object)"),

    /// The method [higherKey][java.util.NavigableMap#higherKey(Object)]
    HIGHER_KEY("higherKey(Object)"),

    /// The method [firstEntry][java.util.NavigableMap#firstEntry()]
    FIRST_ENTRY("firstEntry()"),

    /// The method [lastEntry][java.util.NavigableMap#lastEntry()]
    LAST_ENTRY("lastEntry()"),

    /// The method [pollFirstEntry][java.util.NavigableMap#pollFirstEntry()]
    POLL_FIRST_ENTRY("pollFirstEntry()"),

    /// The method [pollLastEntry][java.util.NavigableMap#pollLastEntry()]
    POLL_LAST_ENTRY("pollLastEntry()"),

    /// The method [descendingMap][java.util.NavigableMap#descendingMap()]
    DESCENDING_MAP("descendingMap()"),

    /// The method [navigableKeySet][java.util.NavigableMap#navigableKeySet()]
    NAVIGABLE_KEY_SET("navigableKeySet()"),

    /// The method [descendingKeySet][java.util.NavigableMap#descendingKeySet()]
    DESCENDING_KEY_SET("descendingKeySet()"),

    /// The method [subMap][java.util.NavigableMap#subMap(Object, boolean, Object, boolean)] (four-argument version)
    SUB_MAP_FOUR_ARG("subMap(Object, boolean, Object, boolean)"),

    /// The method [headMap][java.util.NavigableMap#headMap(Object, boolean)] (two-argument version)
    HEAD_MAP_TWO_ARG("headMap(Object, boolean)"),

    /// The method [tailMap][java.util.NavigableMap#tailMap(Object, boolean)] (two-argument version)
    TAIL_MAP_TWO_ARG("tailMap(Object, boolean)");

    private final String name;

    NavigableMapMethods(final @NonNull String name) {
        this.name = name;
    }

    /// {@inheritDoc}
    @Override
    public @NonNull String methodName() {
        return name;
    }
}
