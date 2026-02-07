package org.soliscode.test.contract.sequencedmap;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.contract.collection.CollectionContract;

/// Values used to identify collection class methods for use with the
/// [CollectionContract#supportsMethod(InterfaceMethod)] method.
///
/// @author evanbergstrom
/// @since 1.0
public enum SequencedMapMethods implements InterfaceMethod {

    /// The method [firstEntry][java.util.SequencedMap#firstEntry()]
    FIRST_ENTRY("firstEntry()"),

    /// The method [lastEntry][java.util.SequencedMap#lastEntry()]
    LAST_ENTRY("lastEntry()"),

    /// The method [pollFirstEntry][java.util.SequencedMap#pollFirstEntry()]
    POLL_FIRST_ENTRY("pollFirstEntry()"),

    /// The method [pollLastEntry][java.util.SequencedMap#pollLastEntry()]
    POLL_LAST_ENTRY("pollLastEntry()"),

    /// The method [putFirst][java.util.SequencedMap#putFirst(Object, Object)]
    PUT_FIRST("putFirst(Object, Object)"),

    /// The method [putLast][java.util.SequencedMap#putLast(Object, Object)]
    PUT_LAST("putLast(Object, Object)"),

    /// The method [reversed][java.util.SequencedMap#reversed()]
    REVERSED("reversed()"),

    /// The method [sequencedKeySet][java.util.SequencedMap#sequencedKeySet()]
    SEQUENCED_KEY_SET("sequencedKeySet()"),

    /// The method [sequencedValues][java.util.SequencedMap#sequencedValues()]
    SEQUENCED_VALUES("sequencedValues()"),

    /// The method [sequencedEntrySet][java.util.SequencedMap#sequencedEntrySet()]
    SEQUENCED_ENTRY_SET("sequencedEntrySet()");

    private final String name;

    SequencedMapMethods(final @NonNull String name) {
        this.name = name;
    }

    /// {@inheritDoc}
    @Override
    public @NonNull String methodName() {
        return name;
    }
}
