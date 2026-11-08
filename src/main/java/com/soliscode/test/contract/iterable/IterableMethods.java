package com.soliscode.test.contract.iterable;

import com.soliscode.test.InterfaceMethod;

/// Values used to identify collection class methods for use with the
/// 'CollectionContract#supportsMethod(InterfaceMethod)' method.
///
/// @author evanbergstrom
/// @since 1.0
/// @see com.soliscode.test.contract.collection.CollectionContract#supportsMethod(InterfaceMethod)
public enum IterableMethods implements InterfaceMethod {

    /// The optional method [java.lang.Iterable#iterator()].
    ITERATOR("iterator()"),

    /// The optional method [java.lang.Iterable#forEach(java.util.function.Consumer)].
    FOR_EACH("forEach(Consumer)"),

    /// The method [java.util.Iterator#forEachRemaining(java.util.function.Consumer)].
    ITERATOR_FOR_EACH_REMAINING("forEachRemaining(Consumer)"),

    /// The method [java.util.Iterator#remove()].
    ITERATOR_REMOVE("remove()");

    private final String name;

    IterableMethods(final String nameArg) {
        this.name = nameArg;
    }

    /// {@inheritDoc}
    @Override
    public String methodName() {
        return name;
    }
}
