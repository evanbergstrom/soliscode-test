/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.performance;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/// A process a [ProcessTopology] started: its pid and log, and the signals a scenario sends it.
///
/// ## Thread Safety
/// Its methods may be called from any thread; [#freeze()], [#resume()] and [#stop()] are not meant to be called
/// at once from several.
///
/// @since 0.1.6
public final class TopologyProcess {

    /// The longest a `kill` command, or a process after SIGKILL, is waited for.
    private static final Duration COMMAND_LIMIT = Duration.ofSeconds(10);

    private final ProcessSpec spec;
    private final Process process;
    private final Path log;
    private volatile boolean frozen;

    TopologyProcess(final ProcessSpec specArg, final Process processArg, final Path logArg) {
        this.spec = specArg;
        this.process = processArg;
        this.log = logArg;
    }

    /// Returns the process's name, its spec's.
    ///
    /// @return the name
    public String name() {
        return spec.name();
    }

    /// Returns the process's pid.
    ///
    /// @return the pid
    public long pid() {
        return process.pid();
    }

    /// Returns the file its standard output and error are written to, kept after it stops.
    ///
    /// @return the log
    public Path log() {
        return log;
    }

    /// Returns whether it is running (a frozen process is).
    ///
    /// @return `true` if it has not exited
    public boolean isAlive() {
        return process.isAlive();
    }

    /// Stops the process where it is (`kill -STOP`), until [#resume()].
    ///
    /// @throws UnsupportedOperationException if this is not a POSIX system
    /// @throws IllegalStateException if `kill` fails
    /// @throws IOException if `kill` cannot be started
    /// @throws InterruptedException if interrupted while `kill` runs
    public void freeze() throws IOException, InterruptedException {
        signal("STOP");
        frozen = true;
    }

    /// Lets a frozen process go on (`kill -CONT`).
    ///
    /// @throws UnsupportedOperationException if this is not a POSIX system
    /// @throws IllegalStateException if `kill` fails
    /// @throws IOException if `kill` cannot be started
    /// @throws InterruptedException if interrupted while `kill` runs
    public void resume() throws IOException, InterruptedException {
        signal("CONT");
        frozen = false;
    }

    /// Stops the process: resumes it if it is frozen, so that SIGTERM reaches it, sends SIGTERM, and, if it has not
    /// exited after its spec's grace, SIGKILL.
    ///
    /// @return its exit code
    /// @throws IllegalStateException if it has not exited 10 s after SIGKILL, or resuming it fails
    /// @throws IOException if `kill` cannot be started to resume it
    /// @throws InterruptedException if interrupted while it is waited for
    public int stop() throws IOException, InterruptedException {
        if (!process.isAlive()) {
            return process.exitValue();
        }
        if (frozen) {
            resume();
        }
        process.destroy();
        if (!process.waitFor(spec.stopGrace().toNanos(), TimeUnit.NANOSECONDS)) {
            process.destroyForcibly();
            if (!process.waitFor(COMMAND_LIMIT.toNanos(), TimeUnit.NANOSECONDS)) {
                throw new IllegalStateException("the process " + name() + " (" + pid() + ") did not exit "
                    + COMMAND_LIMIT + " after SIGKILL");
            }
        }
        return process.exitValue();
    }

    @Override
    public String toString() {
        return name() + " (" + pid() + ")";
    }

    /// Kills it at once, for the topology's shutdown hook.
    void kill() {
        process.destroyForcibly();
    }

    Process process() {
        return process;
    }

    ProcessSpec spec() {
        return spec;
    }

    private void signal(final String signal) throws IOException, InterruptedException {
        requirePosix("kill -" + signal);
        Process kill = new ProcessBuilder(List.of("kill", "-" + signal, Long.toString(pid())))
            .redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        if (!kill.waitFor(COMMAND_LIMIT.toNanos(), TimeUnit.NANOSECONDS)) {
            kill.destroyForcibly();
            throw new IllegalStateException("kill -" + signal + " " + pid() + " did not finish in " + COMMAND_LIMIT);
        }
        if (kill.exitValue() != 0) {
            throw new IllegalStateException("kill -" + signal + " " + pid() + " failed with " + kill.exitValue());
        }
    }

    /// Refuses what needs `kill` or `nice` on a system that is not POSIX.
    static void requirePosix(final String what) {
        if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows")) {
            throw new UnsupportedOperationException(what + " needs a POSIX system, not "
                + System.getProperty("os.name"));
        }
    }
}
