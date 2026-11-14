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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Tests for `AutoCloseableProviders` and `ResourceUse`: that each factory makes new resources with the uses it says,
/// and that the uses do what they say on the JDK's own classes.
///
/// @see AutoCloseableProviders
/// @see ResourceUse
@DisplayName("Tests for AutoCloseableProviders and ResourceUse")
public class AutoCloseableProvidersTest {

    /// Default constructor.
    public AutoCloseableProvidersTest() {
        super();
    }

    @Test
    @DisplayName("of() makes a new resource each call, with its uses in order")
    void ofMakesNewResourcesWithItsUses() {
        ResourceUse<StringReader> first = ResourceUse.worksOnceClosed("first", r -> { });
        ResourceUse<StringReader> second = ResourceUse.throwsOnceClosed("second", IOException.class,
                StringReader::read);
        AutoCloseableProvider<StringReader> provider = AutoCloseableProviders.of(() -> new StringReader("x"), first,
                second);
        assertNotSame(provider.open(), provider.open());
        assertEquals(List.of(first, second), provider.uses());
        assertThrows(UnsupportedOperationException.class, () -> provider.uses().clear());
    }

    @Test
    @DisplayName("of() refuses a null supplier or use, and withoutUses() has none")
    void ofRefusesNulls() {
        ResourceUse<StringReader> use = ResourceUse.worksOnceClosed("use", r -> { });
        assertThrows(NullPointerException.class, () -> AutoCloseableProviders.<StringReader>of(null, use));
        assertThrows(NullPointerException.class,
                () -> AutoCloseableProviders.of(() -> new StringReader("x"), null));
        assertThrows(NullPointerException.class,
                () -> AutoCloseableProviders.of(() -> new StringReader("x"), use, (ResourceUse<StringReader>) null));
        assertEquals(List.of(), AutoCloseableProviders.withoutUses(() -> new StringReader("x")).uses());
        assertThrows(NullPointerException.class, () -> AutoCloseableProviders.withoutUses(null));
    }

    @Test
    @DisplayName("ResourceUse's factories say what the use does once closed, and refuse nulls")
    void resourceUseFactories() {
        ResourceUse<StringReader> throwing = ResourceUse.throwsOnceClosed("read()", IOException.class,
                StringReader::read);
        assertSame(IOException.class, throwing.thrownOnceClosed());
        assertEquals("read() (throws IOException once closed)", throwing.toString());
        ResourceUse<StringReader> working = ResourceUse.worksOnceClosed("ready()", StringReader::ready);
        assertNull(working.thrownOnceClosed());
        assertEquals("ready() (works once closed)", working.toString());
        assertThrows(NullPointerException.class, () -> ResourceUse.worksOnceClosed(null, r -> { }));
        assertThrows(NullPointerException.class, () -> ResourceUse.worksOnceClosed("x", null));
        assertThrows(NullPointerException.class,
                () -> ResourceUse.throwsOnceClosed("x", null, (StringReader r) -> { }));
    }

    @Test
    @DisplayName("bufferedReader()'s uses work while open and throw IOException once closed")
    void bufferedReaderUses() throws Throwable {
        AutoCloseableProvider<BufferedReader> provider = AutoCloseableProviders.bufferedReader();
        assertEquals(2, provider.uses().size());
        for (final ResourceUse<? super BufferedReader> use : provider.uses()) {
            assertSame(IOException.class, use.thrownOnceClosed());
            try (BufferedReader open = provider.open()) {
                use.action().accept(open);
            }
            BufferedReader closed = provider.open();
            closed.close();
            assertThrows(IOException.class, () -> use.action().accept(closed), use::name);
        }
    }

    @Test
    @DisplayName("byteArrayOutputStream()'s uses work while open and once closed")
    void byteArrayOutputStreamUses() throws Throwable {
        AutoCloseableProvider<ByteArrayOutputStream> provider = AutoCloseableProviders.byteArrayOutputStream();
        assertEquals(3, provider.uses().size());
        for (final ResourceUse<? super ByteArrayOutputStream> use : provider.uses()) {
            assertNull(use.thrownOnceClosed());
            ByteArrayOutputStream stream = provider.open();
            use.action().accept(stream);
            stream.close();
            use.action().accept(stream);
        }
    }
}
