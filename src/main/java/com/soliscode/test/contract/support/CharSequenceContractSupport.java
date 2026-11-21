/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.support;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.opentest4j.AssertionFailedError;

/// The base interface of the char sequence contracts: it gives them the contents, checked, the sequences under test
/// made of them, and what the class claims of its natural order.
///
/// @param <T> the class under test
/// @since 0.1.4
public interface CharSequenceContractSupport<T extends CharSequence> extends CharSequenceProviderSupport<T> {

    /// Whether the class under test, where it is [Comparable], orders its sequences by `char`, as String,
    /// StringBuilder and CharBuffer do. For two sequences of one `Comparable` class, [CharSequence#compare] is that
    /// class's `compareTo`, so the contract requires it by default, which [CharSequence]'s Javadoc does not. A class
    /// with another natural order overrides this to return `false`, and the `compare` test then checks only the
    /// comparisons against a `String`, which go through `charAt` and `length` (the user's decision on #43). It does
    /// not matter for a class that is not `Comparable`.
    ///
    /// @return `true` unless overridden
    default boolean naturalOrderIsByChar() {
        return true;
    }

    /// Returns the provider's contents, checked: at least one, and none null. A provider that breaks these rules
    /// fails the test with a message that says the provider, not the sequence, is at fault.
    ///
    /// @return the contents
    default List<String> contents() {
        @Nullable List<String> contents = charSequenceProvider().contents();
        if (contents == null || contents.isEmpty()) {
            throw new AssertionFailedError("the char sequence provider is at fault: it gave no contents");
        }
        for (int i = 0; i < contents.size(); i++) {
            if (contents.get(i) == null) {
                throw new AssertionFailedError("the char sequence provider is at fault: content " + i + " is null");
            }
        }
        return List.copyOf(contents);
    }

    /// Returns a new sequence of the class under test holding the given `char`s, checked: a provider that gives none
    /// fails the test with a message that says the provider, not the sequence, is at fault.
    ///
    /// @param chars the `char`s it holds: one of [#contents()]
    /// @return the sequence under test
    default T create(final String chars) {
        @Nullable T sequence = charSequenceProvider().create(chars);
        if (sequence == null) {
            throw new AssertionFailedError("the char sequence provider is at fault: it gave no sequence");
        }
        return sequence;
    }
}
