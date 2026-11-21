/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.performance;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.soliscode.test.AbstractTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests of [LogTail].
@DisplayName("LogTail")
class LogTailTest extends AbstractTest {

    /// A line of 100 bytes with its line end.
    private static final String LINE = "x".repeat(99) + "\n";

    @TempDir
    private Path directory;

    @Test
    @DisplayName("newLines() reads at most 1 MiB a call, and the next call goes on from there")
    void newLinesReadsAtMostOneMebibyteACall() throws IOException {
        Path log = directory.resolve("big.log");
        Files.writeString(log, LINE.repeat(30_000));
        LogTail tail = new LogTail(log);

        int first = tail.newLines().size();
        int second = tail.newLines().size();
        int third = tail.newLines().size();

        assertTrue(first <= LogTail.MAX_READ / LINE.length() + 1, "first call read " + first + " lines");
        assertEquals(30_000, first + second + third + tail.newLines().size());
    }

    @Test
    @DisplayName("newLines() keeps a partial last line, and returns it once complete, without its line end")
    void newLinesKeepsAPartialLine() throws IOException {
        Path log = directory.resolve("partial.log");
        Files.writeString(log, "one\r\ntw");
        LogTail tail = new LogTail(log);

        assertEquals(List.of("one"), tail.newLines());
        Files.writeString(log, "one\r\ntwo\n");
        assertEquals(List.of("two"), tail.newLines());
        assertEquals(List.of(), new LogTail(directory.resolve("missing.log")).newLines());
    }

    @Test
    @DisplayName("newLines() fails on a line longer than 1 MiB, rather than keep it")
    void newLinesFailsOnALineTooLong() throws IOException {
        Path log = directory.resolve("endless.log");
        Files.writeString(log, "y".repeat(LogTail.MAX_LINE / 2));
        LogTail tail = new LogTail(log);
        assertEquals(List.of(), tail.newLines());
        Files.writeString(log, "y".repeat(LogTail.MAX_LINE + 1));

        assertThrows(IllegalStateException.class, tail::newLines);
    }

    @Test
    @DisplayName("lastLines() reads only a log's last 64 KiB, dropping the line cut there")
    void lastLinesReadsOnlyTheEnd() throws IOException {
        Path log = directory.resolve("big.log");
        Files.writeString(log, LINE.repeat(10_000) + "last\n");

        List<String> lines = LogTail.lastLines(log, 2);

        assertEquals(List.of(LINE.strip(), "last"), lines);
        // the last 64 KiB hold "last", 655 whole lines and the cut end of one more, which is dropped
        assertEquals((LogTail.MAX_TAIL - "last\n".length()) / LINE.length() + 1,
            LogTail.lastLines(log, 1_000_000).size());
    }

    @Test
    @DisplayName("lastLines() of a short log is its last lines, and of a missing one says it cannot be read")
    void lastLinesOfAShortOrMissingLog() throws IOException {
        Path log = directory.resolve("short.log");
        Files.writeString(log, "a\nb\nc");

        assertEquals(List.of("b", "c"), LogTail.lastLines(log, 2));
        assertTrue(LogTail.lastLines(directory.resolve("missing.log"), 2).get(0).startsWith("(the log "));
    }
}
