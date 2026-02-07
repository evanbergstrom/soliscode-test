package org.soliscode.test.contract.map;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.InterfaceMethod;

/// Values used to identify collection class methods for use with the
/// [MapContract#supportsMethod(InterfaceMethod)] method.
///
/// @author evanbergstrom
/// @since 1.0
public enum MapMethods implements InterfaceMethod {

    // Core Map methods
    /// The method [size][java.util.Map#size()]
    SIZE("size()"),

    /// The method [isEmpty][java.util.Map#isEmpty()]
    IS_EMPTY("isEmpty()"),

    /// The method [containsKey][java.util.Map#containsKey(Object)]
    CONTAINS_KEY("containsKey(Object)"),

    /// The method [containsValue][java.util.Map#containsValue(Object)]
    CONTAINS_VALUE("containsValue(Object)"),

    /// The method [get][java.util.Map#get(Object)]
    GET("get(Object)"),

    /// The method [put][java.util.Map#put(Object, Object)]
    PUT("put(Object, Object)"),

    /// The method [remove][java.util.Map#remove(Object)]
    REMOVE("remove(Object)"),

    /// The method [putAll][java.util.Map#putAll(java.util.Map)]
    PUT_ALL("putAll(Map)"),

    /// The method [clear][java.util.Map#clear()]
    CLEAR("clear()"),

    /// The method [keySet][java.util.Map#keySet()]
    KEY_SET("keySet()"),

    /// The method [values][java.util.Map#values()]
    VALUES("values()"),

    /// The method [entrySet][java.util.Map#entrySet()]
    ENTRY_SET("entrySet()"),

    // Methods added in Java 8
    /// The method [getOrDefault][java.util.Map#getOrDefault(Object, Object)]
    GET_OR_DEFAULT("getOrDefault(Object, Object)"),

    /// The method [forEach][java.util.Map#forEach(java.util.function.BiConsumer)]
    FOR_EACH("forEach(BiConsumer)"),

    /// The method [replaceAll][java.util.Map#replaceAll(java.util.function.BiFunction)]
    REPLACE_ALL("replaceAll(BiFunction)"),

    /// The method [putIfAbsent][java.util.Map#putIfAbsent(Object, Object)]
    PUT_IF_ABSENT("putIfAbsent(Object, Object)"),

    /// The method [remove][java.util.Map#remove(Object, Object)] (two-argument version)
    REMOVE_TWO_ARG("remove(Object, Object)"),

    /// The method [replace][java.util.Map#replace(Object, Object, Object)] (three-argument version)
    REPLACE_THREE_ARG("clear()"),

    /// The method [replace][java.util.Map#replace(Object, Object)] (two-argument version)
    REPLACE_TWO_ARG("replace(Object, Object)"),

    /// The method [computeIfAbsent][java.util.Map#computeIfAbsent(Object, java.util.function.Function)]
    COMPUTE_IF_ABSENT("computeIfAbsent(Object, Function)"),

    /// The method [computeIfPresent][java.util.Map#computeIfPresent(Object, java.util.function.BiFunction)]
    COMPUTE_IF_PRESENT("computeIfPresent(Object, BiFunction)"),

    /// The method [compute][java.util.Map#compute(Object, java.util.function.BiFunction)]
    COMPUTE("compute(Object, BiFunction)"),

    /// The method [merge][java.util.Map#merge(Object, Object, java.util.function.BiFunction)]
    MERGE("merge(Object, Object, BiFunction)");

    private final String name;

    private MapMethods(final @NonNull String name) {
        this.name = name;
    }

    /// {@inheritDoc}
    @Override
    public @NonNull String methodName() {
        return name;
    }
}
