/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.provider;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/// Factories of [AutoCloseableProvider]s: providers of a given resource and its uses, and of `BufferedReader` and
/// `ByteArrayOutputStream`.
///
/// @since 0.1.3
public final class AutoCloseableProviders {

    private AutoCloseableProviders() { }

    /// A provider of the resources `open` makes, with the given uses, copied. It takes at least one use: a resource
    /// with no method but `close` is [#withoutUses(Supplier)]'s.
    ///
    /// @param open  makes a new, open resource each call
    /// @param first a use the resource's class documents
    /// @param more  the other uses
    /// @param <C>   the type of the resource
    /// @return the provider
    /// @throws NullPointerException if an argument or a use is null
    @SafeVarargs
    public static <C extends AutoCloseable> AutoCloseableProvider<C> of(final Supplier<? extends C> open,
                                                                       final ResourceUse<? super C> first,
                                                                       final ResourceUse<? super C>... more) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(more, "more");
        List<ResourceUse<? super C>> uses = new ArrayList<>(more.length + 1);
        uses.add(first);
        for (int i = 0; i < more.length; i++) {
            uses.add(Objects.requireNonNull(more[i], "use " + (i + 1)));
        }
        return provider(open, List.copyOf(uses));
    }

    /// A provider of the resources `open` makes, with no uses: the contract tests their `close` alone.
    ///
    /// @param open makes a new, open resource each call
    /// @param <C>  the type of the resource
    /// @return the provider
    /// @throws NullPointerException if `open` is null
    public static <C extends AutoCloseable> AutoCloseableProvider<C> withoutUses(final Supplier<? extends C> open) {
        return provider(open, List.of());
    }

    /// `BufferedReader`s over a `StringReader` of a few characters. `read()` and `ready()` throw `IOException` once
    /// closed, as [java.io.Reader#close()] says.
    ///
    /// @return the provider
    public static AutoCloseableProvider<BufferedReader> bufferedReader() {
        return of(() -> new BufferedReader(new StringReader("closeable")),
                ResourceUse.throwsOnceClosed("read()", IOException.class, BufferedReader::read),
                ResourceUse.throwsOnceClosed("ready()", IOException.class, BufferedReader::ready));
    }

    /// `ByteArrayOutputStream`s. `write(int)`, `size()` and `toByteArray()` work once closed: "Closing a
    /// `ByteArrayOutputStream` has no effect", its Javadoc says.
    ///
    /// @return the provider
    public static AutoCloseableProvider<ByteArrayOutputStream> byteArrayOutputStream() {
        return of(ByteArrayOutputStream::new,
                ResourceUse.worksOnceClosed("write(int)", s -> s.write(1)),
                ResourceUse.worksOnceClosed("size()", ByteArrayOutputStream::size),
                ResourceUse.worksOnceClosed("toByteArray()", ByteArrayOutputStream::toByteArray));
    }

    private static <C extends AutoCloseable> AutoCloseableProvider<C> provider(final Supplier<? extends C> open,
                                                                              final List<ResourceUse<? super C>> uses) {
        Objects.requireNonNull(open, "open");
        return new AutoCloseableProvider<>() {
            @Override
            public C open() {
                return open.get();
            }

            @Override
            public List<ResourceUse<? super C>> uses() {
                return uses;
            }
        };
    }
}
