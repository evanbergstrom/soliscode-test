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
package com.soliscode.test.contract.support;

import java.util.Arrays;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import com.soliscode.test.contract.charsequence.CharSequenceContract;
import com.soliscode.test.provider.CharSequenceProvider;
import com.soliscode.test.provider.CharSequenceProviders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `CharSequenceContractSupport`: its default, and its checks of a hand-written provider, which blame the
/// provider.
///
/// @see CharSequenceContractSupport
@DisplayName("Tests for CharSequenceContractSupport")
public class CharSequenceContractSupportTest {

    /// Default constructor.
    public CharSequenceContractSupportTest() {
        super();
    }

    /// A contract on a provider, with the support's defaults.
    private static <T extends CharSequence> CharSequenceContract<T> bare(final CharSequenceProvider<T> provider) {
        return () -> provider;
    }

    /// A provider of contents, which may be bad, whose sequences may be null.
    private static CharSequenceProvider<String> handWritten(final @Nullable List<String> contents,
                                                            final boolean givesNull) {
        return new CharSequenceProvider<>() {
            @Override
            public String create(final String chars) {
                return givesNull ? null : chars;
            }

            @Override
            public List<String> contents() {
                return contents;
            }
        };
    }

    @Test
    @DisplayName("naturalOrderIsByChar() is true by default")
    void naturalOrderIsByCharByDefault() {
        assertTrue(bare(CharSequenceProviders.strings()).naturalOrderIsByChar());
    }

    @Test
    @DisplayName("contents() and create() are the provider's")
    void contentsAndCreateAreTheProviders() {
        CharSequenceContract<StringBuilder> contract = bare(CharSequenceProviders.stringBuilders());
        assertEquals(CharSequenceProviders.standardContents(), contract.contents());
        assertEquals("ab", contract.create("ab").toString());
    }

    @Test
    @DisplayName("contents() blames a provider that gives no contents, an empty list, or a null content")
    void contentsBlamesABadProvider() {
        for (final List<String> bad : Arrays.asList(null, List.<String>of(), Arrays.asList("a", null))) {
            AssertionFailedError e = assertThrows(AssertionFailedError.class,
                    () -> bare(handWritten(bad, false)).contents());
            assertTrue(e.getMessage().startsWith("the char sequence provider is at fault"), e.getMessage());
        }
    }

    @Test
    @DisplayName("create() blames a provider that gives no sequence")
    void createBlamesAProviderThatGivesNone() {
        AssertionFailedError e = assertThrows(AssertionFailedError.class,
                () -> bare(handWritten(List.of("a"), true)).create("a"));
        assertEquals("the char sequence provider is at fault: it gave no sequence", e.getMessage());
    }
}
