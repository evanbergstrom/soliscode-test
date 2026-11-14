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

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import com.soliscode.test.contract.autocloseable.AutoCloseableContract;
import com.soliscode.test.contract.closeable.CloseableContract;
import com.soliscode.test.provider.AutoCloseableProvider;
import com.soliscode.test.provider.AutoCloseableProviders;
import com.soliscode.test.provider.ResourceUse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `AutoCloseableContractSupport`: its default claim of idempotence, its checks of a hand-written
/// provider, and that a use that fails while open still closes the resource, a failing close kept as suppressed.
///
/// @see AutoCloseableContractSupport
@DisplayName("Tests for AutoCloseableContractSupport")
public class AutoCloseableContractSupportTest {

    /// Default constructor.
    public AutoCloseableContractSupportTest() {
        super();
    }

    /// The contract on a given provider.
    private record Contract<C extends AutoCloseable>(AutoCloseableProvider<C> autoCloseableProvider)
            implements AutoCloseableContract<C> {
    }

    /// A resource that counts its closes, and whose close may fail.
    private static final class Counted implements AutoCloseable {
        private final AtomicInteger closes;
        private final boolean failingClose;
        private boolean closed;

        Counted(final AtomicInteger closesArg, final boolean failingCloseArg) {
            this.closes = closesArg;
            this.failingClose = failingCloseArg;
        }

        boolean isClosed() {
            return closed;
        }

        @Override
        public void close() throws IOException {
            closed = true;
            closes.incrementAndGet();
            if (failingClose) {
                throw new IOException("close failed");
            }
        }
    }

    private static <C extends AutoCloseable> AutoCloseableProvider<C> provider(final C resource,
                                                                              final List<ResourceUse<? super C>> uses) {
        return new AutoCloseableProvider<>() {
            @Override
            public C open() {
                return resource;
            }

            @Override
            public List<ResourceUse<? super C>> uses() {
                return uses;
            }
        };
    }

    @Test
    @DisplayName("closeIsIdempotent() is true by default, for AutoCloseable and Closeable")
    void idempotentByDefault() {
        assertTrue(new Contract<>(AutoCloseableProviders.bufferedReader()).closeIsIdempotent());
        CloseableContract<StringReader> closeable = () -> AutoCloseableProviders.withoutUses(
                () -> new StringReader("x"));
        assertTrue(closeable.closeIsIdempotent());
    }

    @Test
    @DisplayName("A null resource, null uses or a null use is the provider's fault")
    void providerFaults() {
        Contract<StringReader> nullResource = new Contract<>(provider(null, List.of()));
        AssertionFailedError e = assertThrows(AssertionFailedError.class, nullResource::openResource);
        assertTrue(e.getMessage().contains("provider is at fault"), e.getMessage());

        Contract<StringReader> nullUses = new Contract<>(provider(new StringReader("x"), null));
        e = assertThrows(AssertionFailedError.class, nullUses::resourceUses);
        assertTrue(e.getMessage().contains("provider is at fault"), e.getMessage());

        Contract<StringReader> nullUse = new Contract<>(provider(new StringReader("x"), Arrays.asList(
                ResourceUse.worksOnceClosed("ready()", StringReader::ready), null)));
        e = assertThrows(AssertionFailedError.class, nullUse::resourceUses);
        assertTrue(e.getMessage().contains("use 1 is null"), e.getMessage());
    }

    @Test
    @DisplayName("A use that fails while open still closes the resource, once, and a failing close is suppressed")
    void failingUseStillCloses() {
        AtomicInteger closes = new AtomicInteger();
        ResourceUse<Counted> failing = ResourceUse.worksOnceClosed("fails", c -> {
            throw new IllegalStateException("use failed");
        });
        Contract<Counted> contract = new Contract<>(provider(new Counted(closes, true), List.of(failing)));
        AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> contract.useWhileOpen(failing));
        assertEquals(1, closes.get());
        assertInstanceOf(IllegalStateException.class, e.getCause());
        assertEquals(1, e.getSuppressed().length);
        assertInstanceOf(IOException.class, e.getSuppressed()[0].getCause());
    }

    @Test
    @DisplayName("A use that works but whose close fails fails with the close")
    void failingCloseAfterAWorkingUse() {
        AtomicInteger closes = new AtomicInteger();
        ResourceUse<Counted> working = ResourceUse.worksOnceClosed("works", c -> { });
        Contract<Counted> contract = new Contract<>(provider(new Counted(closes, true), List.of(working)));
        AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> contract.useWhileOpen(working));
        assertEquals(1, closes.get());
        assertInstanceOf(IOException.class, e.getCause());
    }

    @Test
    @DisplayName("Each test but the idempotence test closes each resource it opens exactly once")
    void closesOnce() {
        AtomicInteger closes = new AtomicInteger();
        AtomicInteger opens = new AtomicInteger();
        ResourceUse<Counted> use = ResourceUse.worksOnceClosed("works", c -> { });
        ResourceUse<Counted> refused = ResourceUse.throwsOnceClosed("refused", IllegalStateException.class, c -> {
            if (c.isClosed()) {
                throw new IllegalStateException("closed");
            }
        });
        AutoCloseableProvider<Counted> counting = new AutoCloseableProvider<>() {
            @Override
            public Counted open() {
                opens.incrementAndGet();
                return new Counted(closes, false);
            }

            @Override
            public List<ResourceUse<? super Counted>> uses() {
                return List.of(use, refused);
            }
        };
        Contract<Counted> contract = new Contract<>(counting);
        contract.closeOnAnOpenResourceCompletes();
        contract.usesWorkWhileOpen();
        contract.usesThatThrowOnceClosedThrow();
        contract.usesThatWorkOnceClosedWork();
        assertEquals(opens.get(), closes.get());
        assertEquals(5, opens.get());
    }
}
