/*
 * Copyright 2026 Evan Bergstrom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.soliscode.test.provider;

import java.nio.CharBuffer;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/// Factory methods for [CharSequenceProvider]s: of a factory over the standard contents or given ones, and of
/// `String`, `StringBuilder` and `CharBuffer`.
///
/// @since 0.1.4
/// @see CharSequenceProvider
public final class CharSequenceProviders {

    /// The standard contents, as [#standardContents()] lists them.
    private static final List<String> STANDARD_CONTENTS = List.of(
            "", "a", "hello, world", "é", "\u0000",
            "\uE000", "\uFFFF", // U+E000 and U+FFFF: chars with no glyph
            "\uD83D\uDE00", "a\uD83D\uDE00b", // U+1F600 as a surrogate pair, alone and between "a" and "b"
            "\uD83Dx", "x\uDE00", "ab\uD83D", "\uDE00\uD83D"); // unpaired surrogates: no character to write

    private CharSequenceProviders() { }

    /// Returns the standard contents, in this order. Each is there for a fault:
    /// - `""`: the empty sequence;
    /// - `"a"`: one `char`, the smallest range with an out-of-bounds index on each side;
    /// - `"hello, world"`: several `char`s, for the sub-ranges;
    /// - `"é"` (U+00E9): one `char`, shorter than `"hello, world"` but greater at index 0, so that a shortlex
    ///   `compareTo` fails;
    /// - U+0000: the least `char`, which a sequence that pads with or ends at `'\0'` mistakes;
    /// - U+E000, a private-use `char` above the surrogates: it compares one way by `char` and the other by code point
    ///   against a supplementary character;
    /// - U+FFFF: sign-extended, it would be -1;
    /// - U+1F600, a surrogate pair, alone and between `"a"` and `"b"`;
    /// - an unpaired high surrogate before `"x"`, an unpaired low surrogate after `"x"`, and a high surrogate at the
    ///   end of `"ab"`;
    /// - a low surrogate before a high one: two unpaired surrogates.
    ///
    /// @return the standard contents, unmodifiable
    public static List<String> standardContents() {
        return STANDARD_CONTENTS;
    }

    /// A provider of the given factory over the standard contents.
    ///
    /// @param factory makes a new sequence of the class under test holding the given `char`s
    /// @param <T> the class under test
    /// @return the provider
    /// @throws NullPointerException if `factory` is null
    public static <T extends CharSequence> CharSequenceProvider<T> of(final Function<String, T> factory) {
        return of(factory, STANDARD_CONTENTS);
    }

    /// A provider of the given factory over the given contents, copied. The tests of ranges are quadratic in a
    /// content's length: keep the contents short.
    ///
    /// @param factory  makes a new sequence of the class under test holding the given `char`s
    /// @param contents the contents: at least one
    /// @param <T> the class under test
    /// @return the provider
    /// @throws NullPointerException if `factory`, `contents` or a content is null
    /// @throws IllegalArgumentException if there are no contents
    public static <T extends CharSequence> CharSequenceProvider<T> of(final Function<String, T> factory,
                                                                      final List<String> contents) {
        Objects.requireNonNull(factory, "factory");
        Objects.requireNonNull(contents, "contents");
        for (int i = 0; i < contents.size(); i++) {
            Objects.requireNonNull(contents.get(i), "content " + i);
        }
        if (contents.isEmpty()) {
            throw new IllegalArgumentException("contents: there must be at least one");
        }
        List<String> copy = List.copyOf(contents);
        return new CharSequenceProvider<>() {
            @Override
            public T create(final String chars) {
                return factory.apply(chars);
            }

            @Override
            public List<String> contents() {
                return copy;
            }
        };
    }

    /// `String`s over the standard contents, each a new object (`new String(s)`).
    ///
    /// @return the provider
    public static CharSequenceProvider<String> strings() {
        return of(String::new);
    }

    /// `StringBuilder`s over the standard contents.
    ///
    /// @return the provider
    public static CharSequenceProvider<StringBuilder> stringBuilders() {
        return of(StringBuilder::new);
    }

    /// Heap `CharBuffer`s over the standard contents, each wrapping a new array (`CharBuffer.wrap(s.toCharArray())`).
    ///
    /// @return the provider
    public static CharSequenceProvider<CharBuffer> charBuffers() {
        return of(s -> CharBuffer.wrap(s.toCharArray()));
    }
}
