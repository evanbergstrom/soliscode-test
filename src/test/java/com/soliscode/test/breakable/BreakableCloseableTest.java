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
package com.soliscode.test.breakable;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.soliscode.test.breakable.BreakableCloseable.CLOSEABLE_READ_COUNT_THROWS_ONCE_CLOSED;
import static com.soliscode.test.breakable.BreakableCloseable.CLOSEABLE_READ_THROWS_WHILE_OPEN;
import static com.soliscode.test.breakable.BreakableCloseable.CLOSEABLE_READ_THROWS_WRONG_EXCEPTION_ONCE_CLOSED;
import static com.soliscode.test.breakable.BreakableCloseable.CLOSEABLE_READ_WORKS_ONCE_CLOSED;
import static com.soliscode.test.breakable.BreakableCloseable.CLOSE_DOES_NOTHING;
import static com.soliscode.test.breakable.BreakableCloseable.CLOSE_THROWS;
import static com.soliscode.test.breakable.BreakableCloseable.CLOSE_TWICE_REOPENS;
import static com.soliscode.test.breakable.BreakableCloseable.CLOSE_TWICE_THROWS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `BreakableCloseable`: what it does unbroken, and what each break changes, and nothing more.
///
/// @see BreakableCloseable
@DisplayName("Tests for BreakableCloseable")
public class BreakableCloseableTest {

    /// Default constructor.
    public BreakableCloseableTest() {
        super();
    }

    private static BreakableCloseable of(final Break... breaks) {
        return new BreakableCloseable(Set.of(breaks));
    }

    @Test
    @DisplayName("Unbroken, it reads while open, refuses reads once closed, and a second close has no effect")
    void unbrokenBehaves() throws IOException {
        BreakableCloseable resource = of();
        assertFalse(resource.isClosed());
        assertEquals(1, resource.read());
        assertEquals(2, resource.read());
        resource.close();
        assertTrue(resource.isClosed());
        assertThrows(IOException.class, resource::read);
        assertEquals(2, resource.readCount());
        resource.close();
        assertTrue(resource.isClosed());
        assertThrows(IOException.class, resource::read);
        assertEquals("BreakableCloseable[closed, reads=2]", resource.toString());
    }

    @Test
    @DisplayName("It copies its breaks: addBreak works on one made with Set.of(), and the caller's set is not shared")
    void copiesItsBreaks() {
        BreakableCloseable resource = of();
        resource.addBreak(CLOSE_THROWS);
        assertTrue(resource.hasBreak(CLOSE_THROWS));
        Set<Break> mine = new HashSet<>();
        BreakableCloseable copied = new BreakableCloseable(mine);
        mine.add(CLOSE_THROWS);
        assertFalse(copied.hasBreak(CLOSE_THROWS));
    }

    @Test
    @DisplayName("CLOSE_THROWS closes the resource and then throws, and a second close has no effect")
    void closeThrowsAfterClosing() throws IOException {
        BreakableCloseable resource = of(CLOSE_THROWS);
        assertThrows(IOException.class, resource::close);
        assertTrue(resource.isClosed());
        resource.close();
        assertThrows(IOException.class, resource::read);
    }

    @Test
    @DisplayName("CLOSE_DOES_NOTHING leaves the resource open")
    void closeDoesNothing() throws IOException {
        BreakableCloseable resource = of(CLOSE_DOES_NOTHING);
        resource.close();
        assertFalse(resource.isClosed());
        assertEquals(1, resource.read());
    }

    @Test
    @DisplayName("CLOSE_TWICE_THROWS closes on the first call and throws on the second")
    void closeTwiceThrows() throws IOException {
        BreakableCloseable resource = of(CLOSE_TWICE_THROWS);
        resource.close();
        assertTrue(resource.isClosed());
        assertThrows(IOException.class, resource::close);
        assertTrue(resource.isClosed());
    }

    @Test
    @DisplayName("CLOSE_TWICE_REOPENS closes on the first call and reopens on the second")
    void closeTwiceReopens() throws IOException {
        BreakableCloseable resource = of(CLOSE_TWICE_REOPENS);
        resource.close();
        assertThrows(IOException.class, resource::read);
        resource.close();
        assertFalse(resource.isClosed());
        assertEquals(1, resource.read());
    }

    @Test
    @DisplayName("CLOSEABLE_READ_THROWS_WHILE_OPEN makes read throw while open, and still IOException once closed")
    void readThrowsWhileOpen() throws IOException {
        BreakableCloseable resource = of(CLOSEABLE_READ_THROWS_WHILE_OPEN);
        assertThrows(IOException.class, resource::read);
        assertEquals(0, resource.readCount());
        resource.close();
        assertThrows(IOException.class, resource::read);
    }

    @Test
    @DisplayName("CLOSEABLE_READ_WORKS_ONCE_CLOSED lets read count once closed")
    void readWorksOnceClosed() throws IOException {
        BreakableCloseable resource = of(CLOSEABLE_READ_WORKS_ONCE_CLOSED);
        assertEquals(1, resource.read());
        resource.close();
        assertTrue(resource.isClosed());
        assertEquals(2, resource.read());
    }

    @Test
    @DisplayName("CLOSEABLE_READ_THROWS_WRONG_EXCEPTION_ONCE_CLOSED makes read throw IllegalStateException once closed")
    void readThrowsWrongExceptionOnceClosed() throws IOException {
        BreakableCloseable resource = of(CLOSEABLE_READ_THROWS_WRONG_EXCEPTION_ONCE_CLOSED);
        assertEquals(1, resource.read());
        resource.close();
        assertThrows(IllegalStateException.class, resource::read);
    }

    @Test
    @DisplayName("CLOSEABLE_READ_COUNT_THROWS_ONCE_CLOSED makes readCount throw once closed, and only then")
    void readCountThrowsOnceClosed() throws IOException {
        BreakableCloseable resource = of(CLOSEABLE_READ_COUNT_THROWS_ONCE_CLOSED);
        assertEquals(1, resource.read());
        assertEquals(1, resource.readCount());
        resource.close();
        assertThrows(IllegalStateException.class, resource::readCount);
        assertThrows(IOException.class, resource::read);
    }
}
