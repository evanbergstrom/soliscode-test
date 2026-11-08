package com.soliscode.test.contract.list;

import com.soliscode.test.InterfaceMethod;

/// Values used to identify collection class methods for use with the
/// 'supportsMethod(InterfaceMethod)' method.
///
/// @author evanbergstrom
/// @since 1.0
/// @see com.soliscode.test.contract.collection.CollectionContract#supportsMethod(InterfaceMethod)
public enum ListMethods implements InterfaceMethod {

    /// The optional method [java.util.List#addAll(int, java.util.Collection)].
    ADD_ALL_AT_INDEX("addAll(int, Collection)"),

    /// The optional method [java.util.List#add(int, Object)].
    ADD_AT_INDEX("add(int, Object)"),

    /// The method [java.util.List#get(int)].
    GET("get(int)"),

    /// The method [java.util.List#indexOf(Object)].
    INDEX_OF("indexOf(Object)"),

    /// The method [java.util.List#lastIndexOf(Object)].
    LAST_INDEX_OF("lastIndexOf(Object)"),

    /// The optional method [java.util.List#remove(int)].
    REMOVE_AT_INDEX("remove(int)"),

    /// The optional method [java.util.List#replaceAll(java.util.function.UnaryOperator)].
    REPLACE_ALL("replaceAll(UnaryOperator)"),

    /// The optional method [java.util.List#set].
    SET("set(int, Object)"),

    /// The optional method [java.util.List#sort].
    SORT("sort(Comparator)"),

    /// The optional method [java.util.List#subList(int, int)].
    SUB_LIST("subList(int, int)");

    private final String name;

    ListMethods(final String nameArg) {
        this.name = nameArg;
    }

    /// {@inheritDoc}
    @Override
    public String methodName() {
        return name;
    }
}
