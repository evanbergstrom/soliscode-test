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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

/// Processes started from jars, in order, each ready before the next starts, and stopped together.
///
/// Each process's standard output and error go to `<logDirectory>/<name>.log`, emptied when it starts and kept
/// after it stops. A process is ready when a complete line of its log matches its spec's pattern, when its health
/// check answers `true` while it is alive, or at once; readiness is polled on the caller's thread every 10 ms. One
/// that exits first, or is not ready within its spec's timeout, fails the start, and every process started, that
/// one included, is stopped.
///
/// From [Builder#start()] to [#close()] a JVM shutdown hook kills any process still running, so that a test that fails
/// does not leave them behind. Freezing, resuming and `nice` need a POSIX system (`kill`, `nice`).
///
/// ## Usage Examples
/// ```java
/// try (ProcessTopology topology = ProcessTopology.builder(context.scratchDirectory())
///         .process(ProcessSpec.jar("server", serverJar).readyOnLog(Pattern.compile("started")))
///         .process(ProcessSpec.jar("load", loadJar).activeProcessorCount(2).nice(10))
///         .start()) {
///     topology.process("server").freeze();
///     // ... measure ...
///     topology.process("server").resume();
/// }
/// ```
///
/// ## Thread Safety
/// [Builder#start()] and [#close()] run on the calling thread; [#process(String)] may be called from any.
///
/// @since 0.1.6
public final class ProcessTopology implements AutoCloseable {

    /// How often readiness is polled.
    private static final long POLL_NANOS = TimeUnit.MILLISECONDS.toNanos(10);

    /// How many of a log's last lines a failure quotes.
    private static final int LOG_LINES_QUOTED = 20;

    private final Map<String, TopologyProcess> processes;
    private final Thread shutdownHook;
    private final ShutdownHooks hooks;

    private ProcessTopology(final Map<String, TopologyProcess> processesArg, final Thread hook,
        final ShutdownHooks hooksArg) {
        this.processes = processesArg;
        this.shutdownHook = hook;
        this.hooks = hooksArg;
    }

    /// Returns a builder of a topology whose logs go to a directory.
    ///
    /// @param logDirectory the directory, made if it does not exist
    /// @return the builder
    public static Builder builder(final Path logDirectory) {
        return new Builder(Objects.requireNonNull(logDirectory, "logDirectory"));
    }

    /// Returns a process by name.
    ///
    /// @param name the process's name
    /// @return the process
    /// @throws IllegalArgumentException if there is no process of that name
    public TopologyProcess process(final String name) {
        TopologyProcess process = processes.get(Objects.requireNonNull(name, "name"));
        if (process == null) {
            throw new IllegalArgumentException("there is no process " + name + " in " + processes.keySet());
        }
        return process;
    }

    /// Returns the processes, in the order they were started.
    ///
    /// @return the processes
    public List<TopologyProcess> processes() {
        return List.copyOf(processes.values());
    }

    /// Stops the processes in reverse order ([TopologyProcess#stop()]), each whatever the others' stops throw, and
    /// removes the shutdown hook. If a stop fails, every process is killed (SIGKILL) before the hook goes, and the
    /// first failure is thrown, with the others suppressed in it.
    ///
    /// @throws IllegalStateException if a process did not exit after SIGKILL, or could not be resumed
    /// @throws IOException if `kill` cannot be started to resume a frozen process
    /// @throws InterruptedException if interrupted while a process is waited for
    @Override
    public void close() throws IOException, InterruptedException {
        try {
            stopAll(new ArrayList<>(processes.values()));
        } finally {
            hooks.remove(shutdownHook);
        }
    }

    /// Returns the shutdown hook, for the tests.
    Thread shutdownHook() {
        return shutdownHook;
    }

    /// Stops each process, in reverse order, whatever the others' stops throw; if any stop fails, kills every
    /// process (SIGKILL), so that none outlives the hook's removal, and throws the first failure, with the others
    /// suppressed in it.
    private static void stopAll(final List<TopologyProcess> started) throws IOException, InterruptedException {
        @Nullable Exception first = null;
        for (int i = started.size() - 1; i >= 0; i--) {
            try {
                started.get(i).stop();
            } catch (final IOException | InterruptedException | RuntimeException e) {
                if (first == null) {
                    first = e;
                } else {
                    first.addSuppressed(e);
                }
            }
        }
        if (first != null) {
            started.forEach(TopologyProcess::kill);
        }
        if (first instanceof IOException e) {
            throw e;
        }
        if (first instanceof InterruptedException e) {
            throw e;
        }
        if (first instanceof RuntimeException e) {
            throw e;
        }
    }

    /// Where the topology registers its shutdown hook: the JVM's, or, in the tests, one that records it.
    interface ShutdownHooks {

        /// The JVM's shutdown hooks.
        ShutdownHooks RUNTIME = new ShutdownHooks() {
            @Override
            public void add(final Thread hook) {
                Runtime.getRuntime().addShutdownHook(hook);
            }

            @Override
            public void remove(final Thread hook) {
                try {
                    Runtime.getRuntime().removeShutdownHook(hook);
                } catch (final IllegalStateException ignored) {
                    // the JVM is shutting down, and the hook is running or has run
                }
            }
        };

        /// Registers a hook.
        ///
        /// @param hook the hook
        void add(Thread hook);

        /// Removes a hook, if the JVM is not shutting down.
        ///
        /// @param hook the hook
        void remove(Thread hook);
    }

    /// A topology being described: its processes, in the order they start.
    ///
    /// @since 0.1.6
    public static final class Builder {

        private final Path logDirectory;
        private final List<ProcessSpec> specs = new ArrayList<>();
        private final Set<String> names = new HashSet<>();
        private ShutdownHooks hooks = ShutdownHooks.RUNTIME;

        private Builder(final Path logDirectoryArg) {
            this.logDirectory = logDirectoryArg;
        }

        /// Registers the shutdown hook with others than the JVM's, for the tests.
        Builder hooks(final ShutdownHooks other) {
            this.hooks = Objects.requireNonNull(other, "other");
            return this;
        }

        /// Adds a process, started after those added before it.
        ///
        /// @param spec the process
        /// @return this builder
        /// @throws IllegalArgumentException if a process of that name, ignoring case, was added already
        public Builder process(final ProcessSpec spec) {
            Objects.requireNonNull(spec, "spec");
            // ignoring case, since on a case-insensitive file system two such names would share a log
            if (!names.add(spec.name().toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("the process name " + spec.name() + " is given twice, ignoring "
                    + "case");
            }
            if (spec.usesNice()) {
                TopologyProcess.requirePosix("nice");
            }
            specs.add(spec);
            return this;
        }

        /// Starts the processes in order, each ready before the next.
        ///
        /// Whatever fails the start, every process started, the failing one included, has been stopped (killed, on
        /// an interrupt) and the shutdown hook removed when it is thrown; a failure to stop them is suppressed in it.
        ///
        /// @return the topology
        /// @throws IllegalStateException if a process exits before it is ready, or is not ready in time
        /// @throws IOException if the log directory cannot be made, or a process cannot be started
        /// @throws InterruptedException if interrupted while a process is waited for
        public ProcessTopology start() throws IOException, InterruptedException {
            Files.createDirectories(logDirectory);
            Path java = Path.of(System.getProperty("java.home"), "bin", "java");
            // read by the shutdown hook's thread, written by this one
            List<TopologyProcess> started = new CopyOnWriteArrayList<>();
            Thread hook = new Thread(() -> started.forEach(TopologyProcess::kill), "ProcessTopology-shutdown");
            hooks.add(hook);
            try {
                for (final ProcessSpec spec : specs) {
                    started.add(launch(spec, java));
                    awaitReady(started.get(started.size() - 1));
                }
            } catch (final Throwable e) {
                cleanUp(started, hook, e);
                throw e;
            }
            Map<String, TopologyProcess> byName = new LinkedHashMap<>();
            started.forEach(process -> byName.put(process.name(), process));
            return new ProcessTopology(byName, hook, hooks);
        }

        /// Stops what a failed start started (kills it, on an interrupt) and removes the hook, adding any failure
        /// to do so to the start's.
        private void cleanUp(final List<TopologyProcess> started, final Thread hook, final Throwable failure) {
            try {
                if (failure instanceof InterruptedException) {
                    started.forEach(TopologyProcess::kill);
                } else {
                    stopAll(started);
                }
            } catch (final IOException | InterruptedException | RuntimeException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                failure.addSuppressed(e);
            } finally {
                hooks.remove(hook);
            }
        }

        private TopologyProcess launch(final ProcessSpec spec, final Path java) throws IOException {
            Path log = logDirectory.resolve(spec.name() + ".log");
            Process process = new ProcessBuilder(spec.command(java))
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.to(log.toFile()))
                .start();
            return new TopologyProcess(spec, process, log);
        }

        private static void awaitReady(final TopologyProcess process) throws IOException, InterruptedException {
            ProcessSpec spec = process.spec();
            long deadline = System.nanoTime() + spec.readyTimeout().toNanos();
            LogTail tail = new LogTail(process.log());
            while (true) {
                boolean ready = isReady(spec, tail, deadline);
                if (!process.isAlive()) {
                    throw failure(process, "exited with " + process.process().exitValue() + " before it was ready");
                }
                if (System.nanoTime() - deadline >= 0) {
                    throw failure(process, "was not ready in " + spec.readyTimeout());
                }
                if (ready) {
                    return;
                }
                process.process().waitFor(POLL_NANOS, TimeUnit.NANOSECONDS);
            }
        }

        /// Returns whether the process is ready by its spec; a check's answer after the deadline is not taken.
        private static boolean isReady(final ProcessSpec spec, final LogTail tail, final long deadline)
            throws IOException, InterruptedException {
            Pattern line = spec.readyLine();
            if (line != null) {
                return tail.newLines().stream().anyMatch(text -> line.matcher(text).find());
            }
            @Nullable ReadinessCheck check = spec.readyCheck();
            if (check == null) {
                return true;
            }
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) {
                return false;
            }
            try {
                return check.isReady(Duration.ofNanos(remaining));
            } catch (final InterruptedException e) {
                throw e;
            } catch (final Exception notReadyYet) {
                return false;
            }
        }

        private static IllegalStateException failure(final TopologyProcess process, final String what) {
            return new IllegalStateException("the process " + process + " " + what + "; the last lines of "
                + process.log() + ":\n" + String.join("\n", LogTail.lastLines(process.log(), LOG_LINES_QUOTED)));
        }
    }
}
