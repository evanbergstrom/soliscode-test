/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.performance;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

/// Reads a process's heap occupancy just after a full collection with one `jcmd` attach: `jcmd <pid> -f` running
/// `GC.run` and then `GC.heap_info`, its output in a file and its time limited, so that no thread reads it.
final class JcmdHeapReader implements HeapSoak.HeapReader {

    /// A heap line's occupancy: `used 29671K`, `used 60M`.
    private static final Pattern USED = Pattern.compile("\\bused (\\d+)([KMG])\\b");

    private static final int KILO_SHIFT = 10;
    private static final int MEGA_SHIFT = 20;
    private static final int GIGA_SHIFT = 30;

    private final Duration limit;

    JcmdHeapReader(final Duration limitArg) {
        this.limit = limitArg;
    }

    @Override
    public long liveBytes(final long pid) throws IOException, InterruptedException {
        Path commands = Files.createTempFile("heap-soak-", ".jcmd");
        @Nullable Path output = null;
        try {
            output = Files.createTempFile("heap-soak-", ".out");
            Files.writeString(commands, "GC.run\nGC.heap_info\n", StandardCharsets.UTF_8);
            Path jcmd = Path.of(System.getProperty("java.home"), "bin", "jcmd");
            Process process = new ProcessBuilder(List.of(jcmd.toString(), Long.toString(pid), "-f",
                commands.toString())).redirectErrorStream(true).redirectOutput(output.toFile()).start();
            awaitOrKill(process, pid);
            String text = Files.readString(output, StandardCharsets.UTF_8);
            if (process.exitValue() != 0) {
                throw new IllegalStateException("jcmd " + pid + " failed with " + process.exitValue() + ":\n" + text);
            }
            return usedBytes(text);
        } finally {
            Files.deleteIfExists(commands);
            if (output != null) {
                Files.deleteIfExists(output);
            }
        }
    }

    /// Waits for `jcmd` within the limit; kills it if the limit passes or the wait is interrupted, so that it is
    /// never left attached to the target with commands pending.
    private void awaitOrKill(final Process process, final long pid) throws InterruptedException {
        boolean finished = false;
        try {
            finished = process.waitFor(limit.toNanos(), TimeUnit.NANOSECONDS);
        } finally {
            if (!finished) {
                process.destroyForcibly();
            }
        }
        if (!finished) {
            throw new IllegalStateException("jcmd " + pid + " did not finish in " + limit);
        }
    }

    /// Returns the sum of the `used` of the heap lines of `GC.heap_info`'s output: the lines not indented that
    /// hold `used <n>K`, `M` or `G` (G1's `garbage-first heap`, ZGC's `ZHeap`, Parallel's `PSYoungGen` and
    /// `ParOldGen`, Serial's `DefNew` and `Tenured`).
    ///
    /// @throws IllegalStateException if no line holds one
    static long usedBytes(final String output) {
        long bytes = 0;
        boolean found = false;
        for (final String line : output.lines().toList()) {
            if (line.isEmpty() || Character.isWhitespace(line.charAt(0))) {
                continue;
            }
            Matcher used = USED.matcher(line);
            if (used.find()) {
                bytes = Math.addExact(bytes, Long.parseLong(used.group(1)) << shift(used.group(2)));
                found = true;
            }
        }
        if (!found) {
            throw new IllegalStateException("no heap line with its 'used' in jcmd's output:\n" + output);
        }
        return bytes;
    }

    private static int shift(final String unit) {
        return switch (unit.toUpperCase(Locale.ROOT)) {
            case "K" -> KILO_SHIFT;
            case "M" -> MEGA_SHIFT;
            default -> GIGA_SHIFT;
        };
    }
}
