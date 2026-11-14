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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `CharSequenceProviders`: the standard contents, what each factory makes, and what `of` refuses.
///
/// @see CharSequenceProviders
@DisplayName("Tests for CharSequenceProviders")
public class CharSequenceProvidersTest {

    /// Default constructor.
    public CharSequenceProvidersTest() {
        super();
    }

    /// Checks that the provider makes, of each content, a sequence of the given class holding it.
    private static <T extends CharSequence> void assertMakes(final CharSequenceProvider<T> provider,
                                                             final Class<?> type) {
        assertEquals(CharSequenceProviders.standardContents(), provider.contents());
        for (final String s : provider.contents()) {
            T sequence = provider.create(s);
            assertTrue(type.isInstance(sequence), () -> sequence.getClass() + " is not a " + type);
            assertEquals(s, sequence.toString());
        }
    }

    @Test
    @DisplayName("The standard contents hold the empty sequence, U+FFFF, a surrogate pair and unpaired surrogates")
    void standardContentsHoldTheCases() {
        List<String> contents = CharSequenceProviders.standardContents();
        assertEquals(13, contents.size());
        assertTrue(contents.contains(""));
        assertTrue(contents.contains("\uFFFF"));
        assertTrue(contents.contains("\uE000")); // U+E000, a private-use char with no glyph
        assertTrue(contents.contains("\uD83D\uDE00")); // U+1F600 as a surrogate pair
        assertTrue(contents.stream().anyMatch(s -> s.length() > 1 && s.codePointCount(0, s.length()) == s.length()
                && s.chars().anyMatch(c -> Character.isSurrogate((char) c))), "a content with unpaired surrogates");
        assertThrows(UnsupportedOperationException.class, () -> contents.add("x"));
    }

    @Test
    @DisplayName("strings() makes a new String of each standard content")
    void stringsMakesNewStrings() {
        CharSequenceProvider<String> provider = CharSequenceProviders.strings();
        assertMakes(provider, String.class);
        String content = provider.contents().get(2);
        assertNotSame(content, provider.create(content));
    }

    @Test
    @DisplayName("stringBuilders() and charBuffers() make StringBuilders and CharBuffers of each standard content")
    void stringBuildersAndCharBuffers() {
        assertMakes(CharSequenceProviders.stringBuilders(), StringBuilder.class);
        assertMakes(CharSequenceProviders.charBuffers(), CharBuffer.class);
    }

    @Test
    @DisplayName("of(factory, contents) copies the contents")
    void ofCopiesTheContents() {
        List<String> contents = new ArrayList<>(List.of("a", "b"));
        CharSequenceProvider<StringBuilder> provider = CharSequenceProviders.of(StringBuilder::new, contents);
        contents.clear();
        assertEquals(List.of("a", "b"), provider.contents());
        assertEquals("b", provider.create("b").toString());
    }

    @Test
    @DisplayName("of() refuses a null factory, null or no contents, and a null content")
    void ofRefusesBadArguments() {
        assertThrows(NullPointerException.class, () -> CharSequenceProviders.of(null));
        assertThrows(NullPointerException.class, () -> CharSequenceProviders.of(StringBuilder::new, null));
        assertThrows(IllegalArgumentException.class, () -> CharSequenceProviders.of(StringBuilder::new, List.of()));
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> CharSequenceProviders.of(StringBuilder::new, Arrays.asList("a", null)));
        assertEquals("content 1", e.getMessage());
    }
}
