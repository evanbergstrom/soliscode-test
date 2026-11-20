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

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/// The main class of the child JVMs [ProcessTopologyTest] starts, from a jar of this class alone. Its arguments
/// are steps, run in order, after which it waits 60 s and exits:
/// - `print:<text>` writes a line to standard output, `partial:<text>` the text without a line end;
/// - `wait:<ms>` waits that long;
/// - `exit:<code>` exits with the code;
/// - `ignore-term` makes it ignore SIGTERM, by a shutdown hook that waits 60 s.
///
/// It waits with `LockSupport.parkNanos`, and writes through the standard output's file descriptor.
final class TopologyChildMain {

    private static final long LIFETIME_MILLIS = 60_000;

    private TopologyChildMain() {
    }

    /// Runs the steps.
    ///
    /// @param args the steps
    /// @throws IOException if standard output cannot be written
    public static void main(final String[] args) throws IOException {
        OutputStream out = new FileOutputStream(FileDescriptor.out);
        for (final String step : args) {
            String value = step.substring(step.indexOf(':') + 1);
            if (step.startsWith("print:")) {
                write(out, value + "\n");
            } else if (step.startsWith("partial:")) {
                write(out, value);
            } else if (step.startsWith("wait:")) {
                waitMillis(Long.parseLong(value));
            } else if (step.startsWith("exit:")) {
                System.exit(Integer.parseInt(value));
            } else if (step.equals("ignore-term")) {
                Runtime.getRuntime().addShutdownHook(new Thread(TopologyChildMain::waitLifetime));
            }
        }
        waitLifetime();
    }

    private static void write(final OutputStream out, final String text) throws IOException {
        out.write(text.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    private static void waitLifetime() {
        waitMillis(LIFETIME_MILLIS);
    }

    private static void waitMillis(final long millis) {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(millis);
        long remaining = deadline - System.nanoTime();
        while (remaining > 0) {
            LockSupport.parkNanos(remaining);
            remaining = deadline - System.nanoTime();
        }
    }
}
