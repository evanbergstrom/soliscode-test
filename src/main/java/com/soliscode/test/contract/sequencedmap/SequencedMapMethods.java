package com.soliscode.test.contract.sequencedmap;

import com.soliscode.test.InterfaceMethod;

/// Enum representing the methods of the [java.util.SequencedMap] interface.
/// These values are used to identify which methods are supported by a map implementation
/// during contract testing.
///
/// @see SequencedMapContract#supportsMethod(com.soliscode.test.InterfaceMethod)
/// @author evanbergstrom
/// @since 1.0.0
public enum SequencedMapMethods implements InterfaceMethod {

    /// The method `firstEntry()` of [java.util.SequencedMap].
    FIRST_ENTRY("firstEntry()"),

    /// The method `lastEntry()` of [java.util.SequencedMap].
    LAST_ENTRY("lastEntry()"),

    /// The method `pollFirstEntry()` of [java.util.SequencedMap].
    POLL_FIRST_ENTRY("pollFirstEntry()"),

    /// The method `pollLastEntry()` of [java.util.SequencedMap].
    POLL_LAST_ENTRY("pollLastEntry()"),

    /// The method `putFirst(K, V)` of [java.util.SequencedMap].
    PUT_FIRST("putFirst(Object, Object)"),

    /// The method `putLast(K, V)` of [java.util.SequencedMap].
    PUT_LAST("putLast(Object, Object)"),

    /// The method `reversed()` of [java.util.SequencedMap].
    REVERSED("reversed()"),

    /// The method `sequencedKeySet()` of [java.util.SequencedMap].
    SEQUENCED_KEY_SET("sequencedKeySet()"),

    /// The method `sequencedValues()` of [java.util.SequencedMap].
    SEQUENCED_VALUES("sequencedValues()"),

    /// The method `sequencedEntrySet()` of [java.util.SequencedMap].
    SEQUENCED_ENTRY_SET("sequencedEntrySet()");

    private final String name;

    SequencedMapMethods(final String nameArg) {
        this.name = nameArg;
    }

    /// {@inheritDoc}
    @Override
    public String methodName() {
        return name;
    }
}
