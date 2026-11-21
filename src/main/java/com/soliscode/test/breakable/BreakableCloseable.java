/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.breakable;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serial;
import java.util.Map;
import java.util.Set;

/// A closeable resource that can be broken in well-defined ways, to show that
/// [com.soliscode.test.contract.autocloseable.AutoCloseableContract] and
/// [com.soliscode.test.contract.closeable.CloseableContract] can fail.
///
/// Unlike the other breakables, it wraps no JDK implementation: it is a resource of its own, with a closed flag and a
/// count of reads, so that each break acts on that state and nothing else. Unbroken, it documents:
/// - [#read()] counts a read and returns the count, and throws [IOException] once closed;
/// - [#readCount()] and [#isClosed()] work once closed;
/// - [#close()] marks it closed, and a second call has no effect, as [Closeable] requires.
///
/// It is made with its constructor, and has no builder.
///
/// # Breaks
/// The breaks that are supported for this class are listed in the description of the method that they impact.
///
/// ## Thread Safety
/// Not thread safe: its flag and count are not synchronized, and its breaks are a copy of the set it was made with,
/// which `addBreak` changes, unsynchronized.
///
/// ## Serialization
/// It is serializable, as every breakable is, with its flag and count.
///
/// @since 0.1.3
/// @see Closeable
public class BreakableCloseable extends AbstractBreakable implements Closeable {

    /// **Purpose:** `close` fails, where [com.soliscode.test.contract.autocloseable.AutoCloseableContract] reads
    /// `close` on an open resource as returning normally.
    ///
    /// **Effect:** the first `close()` marks the resource closed, as [AutoCloseable#close()] advises, and then
    /// throws [IOException]. A second call has no effect.
    ///
    /// **Affected Methods:** [#close()]
    public static final Break CLOSE_THROWS =
            new Break("close throws IOException");

    /// **Purpose:** `close` does not close the resource.
    ///
    /// **Effect:** `close()` returns without marking the resource closed, so that [#read()] still works.
    ///
    /// **Affected Methods:** [#close()]
    public static final Break CLOSE_DOES_NOTHING =
            new Break("close does not close the resource");

    /// **Purpose:** `close` is not idempotent, where [java.io.Closeable#close()] says a second call has no effect.
    ///
    /// **Effect:** `close()` of a resource already closed throws [IOException]. The first call closes it.
    ///
    /// **Affected Methods:** [#close()]
    public static final Break CLOSE_TWICE_THROWS =
            new Break("a second close throws IOException");

    /// **Purpose:** a second `close` changes the resource, where [java.io.Closeable#close()] says it has no effect.
    ///
    /// **Effect:** `close()` of a resource already closed reopens it, so that [#read()] works again.
    ///
    /// **Affected Methods:** [#close()]
    public static final Break CLOSE_TWICE_REOPENS =
            new Break("a second close reopens the resource");

    /// **Purpose:** a use the class documents fails while the resource is open.
    ///
    /// **Effect:** `read()` throws [IOException] while the resource is open. Once closed it throws [IOException] as
    /// it should.
    ///
    /// **Affected Methods:** [#read()]
    public static final Break CLOSEABLE_READ_THROWS_WHILE_OPEN =
            new Break("read throws IOException while open");

    /// **Purpose:** a use the class says throws once closed does not check that it is closed.
    ///
    /// **Effect:** `read()` counts a read and returns the count once closed, as it does while open.
    ///
    /// **Affected Methods:** [#read()]
    public static final Break CLOSEABLE_READ_WORKS_ONCE_CLOSED =
            new Break("read works once closed");

    /// **Purpose:** a use the class says throws once closed throws the wrong exception.
    ///
    /// **Effect:** `read()` throws [IllegalStateException] once closed, in place of [IOException].
    ///
    /// **Affected Methods:** [#read()]
    public static final Break CLOSEABLE_READ_THROWS_WRONG_EXCEPTION_ONCE_CLOSED =
            new Break("read throws IllegalStateException once closed");

    /// **Purpose:** a use the class says works once closed fails once closed.
    ///
    /// **Effect:** `readCount()` throws [IllegalStateException] once closed.
    ///
    /// **Affected Methods:** [#readCount()]
    public static final Break CLOSEABLE_READ_COUNT_THROWS_ONCE_CLOSED =
            new Break("readCount throws IllegalStateException once closed");

    @Serial
    private static final long serialVersionUID = 1L;

    /// Whether it is closed.
    private boolean closed;

    /// The number of reads.
    private int reads;

    /// Creates an open `BreakableCloseable`, with the given breaks.
    ///
    /// @param breaks the set of breaks to apply, copied
    public BreakableCloseable(final Set<Break> breaks) {
        super(breaks, Map.of(), false);
    }

    /// Counts a read, and returns the count.
    ///
    /// # Breaks
    /// - [#CLOSEABLE_READ_THROWS_WHILE_OPEN]
    /// - [#CLOSEABLE_READ_WORKS_ONCE_CLOSED]
    /// - [#CLOSEABLE_READ_THROWS_WRONG_EXCEPTION_ONCE_CLOSED]
    ///
    /// @return the number of reads, this one included
    /// @throws IOException if it is closed, or, broken, while it is open
    public int read() throws IOException {
        if (closed && !hasBreak(CLOSEABLE_READ_WORKS_ONCE_CLOSED)) {
            if (hasBreak(CLOSEABLE_READ_THROWS_WRONG_EXCEPTION_ONCE_CLOSED)) {
                throw new IllegalStateException("closed");
            }
            throw new IOException("closed");
        }
        if (!closed && hasBreak(CLOSEABLE_READ_THROWS_WHILE_OPEN)) {
            throw new IOException("read while open");
        }
        return ++reads;
    }

    /// Returns the number of reads. It works once closed.
    ///
    /// # Breaks
    /// - [#CLOSEABLE_READ_COUNT_THROWS_ONCE_CLOSED]
    ///
    /// @return the number of reads
    /// @throws IllegalStateException if it is closed, only when broken
    public int readCount() {
        if (closed && hasBreak(CLOSEABLE_READ_COUNT_THROWS_ONCE_CLOSED)) {
            throw new IllegalStateException("closed");
        }
        return reads;
    }

    /// Returns whether it is closed.
    ///
    /// @return whether it is closed
    public boolean isClosed() {
        return closed;
    }

    /// Closes it. A second call has no effect.
    ///
    /// # Breaks
    /// - [#CLOSE_THROWS]
    /// - [#CLOSE_DOES_NOTHING]
    /// - [#CLOSE_TWICE_THROWS]
    /// - [#CLOSE_TWICE_REOPENS]
    ///
    /// @throws IOException only when broken
    @Override
    public void close() throws IOException {
        if (hasBreak(CLOSE_DOES_NOTHING)) {
            return;
        }
        if (closed) {
            if (hasBreak(CLOSE_TWICE_THROWS)) {
                throw new IOException("closed twice");
            }
            if (hasBreak(CLOSE_TWICE_REOPENS)) {
                closed = false;
            }
            return;
        }
        closed = true;
        if (hasBreak(CLOSE_THROWS)) {
            throw new IOException("close failed");
        }
    }

    /// Returns its state and read count.
    ///
    /// @return the string form
    @Override
    public String toString() {
        return "BreakableCloseable[" + (closed ? "closed" : "open") + ", reads=" + reads + "]";
    }
}
