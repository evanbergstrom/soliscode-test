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
package com.soliscode.test.performance;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/// Reads a log a process is writing, a complete line at a time: each call reads from where the last stopped, at
/// most 1 MiB, and keeps a partial last line, as bytes, for the next; a line longer than 1 MiB fails it.
final class LogTail {

    /// The most one call reads, so that a process that writes faster than it is read cannot hold the caller.
    static final int MAX_READ = 1 << 20;

    /// The longest line kept, so that a process that writes without a line end cannot fill the caller's heap.
    static final int MAX_LINE = 1 << 20;

    /// The most of a log's end [#lastLines(Path, int)] reads.
    static final int MAX_TAIL = 64 * 1024;

    private static final int BUFFER_SIZE = 8192;

    private final Path log;
    private final ByteArrayOutputStream partial = new ByteArrayOutputStream();
    private long position;

    LogTail(final Path logArg) {
        this.log = logArg;
    }

    /// Returns the complete lines written since the last call, without their line ends.
    List<String> newLines() throws IOException {
        List<String> lines = new ArrayList<>();
        if (!Files.exists(log)) {
            return lines;
        }
        try (FileChannel channel = FileChannel.open(log, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            channel.position(position);
            long readHere = 0;
            int read = channel.read(buffer);
            while (read > 0) {
                position += read;
                readHere += read;
                split(buffer.array(), read, lines);
                if (readHere >= MAX_READ) {
                    break;
                }
                buffer.clear();
                read = channel.read(buffer);
            }
        }
        return lines;
    }

    private void split(final byte[] bytes, final int length, final List<String> lines) {
        int start = 0;
        for (int i = 0; i < length; i++) {
            if (bytes[i] == '\n') {
                keep(bytes, start, i - start);
                lines.add(line(partial.toByteArray()));
                partial.reset();
                start = i + 1;
            }
        }
        keep(bytes, start, length - start);
    }

    private void keep(final byte[] bytes, final int start, final int length) {
        if (partial.size() + length > MAX_LINE) {
            throw new IllegalStateException("the log " + log + " has a line longer than " + MAX_LINE + " bytes");
        }
        partial.write(bytes, start, length);
    }

    /// Returns the last lines of a log, from at most its last 64 KiB, for a failure's message.
    static List<String> lastLines(final Path log, final int count) {
        try (FileChannel channel = FileChannel.open(log, StandardOpenOption.READ)) {
            // one reading of the size, so that what the process writes meanwhile cannot widen the read
            long size = channel.size();
            long start = Math.max(0, size - MAX_TAIL);
            ByteBuffer buffer = ByteBuffer.allocate((int) (size - start));
            channel.position(start);
            int read = 0;
            while (buffer.hasRemaining() && read >= 0) {
                read = channel.read(buffer);
            }
            String[] lines = new String(buffer.array(), 0, buffer.position(), StandardCharsets.UTF_8).split("\n", -1);
            int first = start > 0 ? 1 : 0;
            int end = lines.length > 0 && lines[lines.length - 1].isEmpty() ? lines.length - 1 : lines.length;
            return Arrays.asList(lines).subList(Math.max(first, end - count), Math.max(first, end));
        } catch (final IOException e) {
            return List.of("(the log " + log + " cannot be read: " + e + ")");
        }
    }

    private static String line(final byte[] bytes) {
        int length = bytes.length > 0 && bytes[bytes.length - 1] == '\r' ? bytes.length - 1 : bytes.length;
        return new String(bytes, 0, length, StandardCharsets.UTF_8);
    }
}
