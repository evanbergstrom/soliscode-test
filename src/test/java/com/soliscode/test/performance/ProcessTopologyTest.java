/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.performance;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.annotations.Slow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/// Tests of [ProcessTopology], [ProcessSpec] and [TopologyProcess], on child JVMs of [TopologyChildMain].
@DisplayName("ProcessTopology")
@Slow
class ProcessTopologyTest extends AbstractTest {

    /// The exit code of a JVM that SIGTERM ended.
    private static final int SIGTERM_EXIT = 143;

    /// The exit code of a process that SIGKILL ended.
    private static final int SIGKILL_EXIT = 137;

    private static final Pattern READY = Pattern.compile("^ready$");

    @TempDir
    private static Path jarDirectory;

    private static Path jar;

    @TempDir
    private Path logs;

    @BeforeAll
    static void writeTheChildsJar() throws IOException {
        assumeFalse(System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows"),
            "the topology's signals need a POSIX system");
        jar = jarDirectory.resolve("child.jar");
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, TopologyChildMain.class.getName());
        String entry = TopologyChildMain.class.getName().replace('.', '/') + ".class";
        try (OutputStream file = Files.newOutputStream(jar);
             JarOutputStream out = new JarOutputStream(file, manifest);
             InputStream in = TopologyChildMain.class.getClassLoader().getResourceAsStream(entry)) {
            out.putNextEntry(new JarEntry(entry));
            in.transferTo(out);
            out.closeEntry();
        }
    }

    @Test
    @DisplayName("start() starts the processes, each ready on its log line, and close() stops them")
    void startsInOrderAndStops() throws Exception {
        ProcessTopology topology = ProcessTopology.builder(logs)
            .process(child("server", "wait:100", "print:ready").readyOnLog(READY))
            .process(child("load", "print:ready").readyOnLog(READY))
            .start();
        TopologyProcess server = topology.process("server");
        TopologyProcess load = topology.process("load");

        assertTrue(server.isAlive() && load.isAlive());
        assertEquals(List.of(server, load), topology.processes());
        assertEquals(logs.resolve("server.log"), server.log());
        assertTrue(server.pid() > 0);
        assertTrue(server.toString().startsWith("server ("));
        Thread hook = topology.shutdownHook();
        assertTrue(Runtime.getRuntime().removeShutdownHook(hook), "the hook is registered while it runs");
        Runtime.getRuntime().addShutdownHook(hook);
        topology.close();

        assertFalse(Runtime.getRuntime().removeShutdownHook(hook), "close() removes the hook");

        assertFalse(server.isAlive() || load.isAlive());
        assertEquals(SIGTERM_EXIT, server.stop());
        assertEquals("ready\n", Files.readString(server.log()));
        assertThrows(IllegalArgumentException.class, () -> topology.process("feed"));
    }

    @Test
    @DisplayName("a line written in two parts is read as one line, and an old log is emptied first")
    void aLineInTwoPartsIsOneLineAndAnOldLogIsEmptied() throws Exception {
        Files.createDirectories(logs);
        Files.writeString(logs.resolve("server.log"), "ready\n");
        ProcessSpec spec = child("server", "partial:rea", "wait:300", "print:dy").readyOnLog(READY);

        try (ProcessTopology topology = ProcessTopology.builder(logs).process(spec).start()) {
            assertEquals("ready\n", Files.readString(topology.process("server").log()));
        }
    }

    @Test
    @DisplayName("a partial last line is not matched until it is complete")
    void aPartialLineIsNotMatched() {
        ProcessTopology.Builder builder = ProcessTopology.builder(logs)
            .process(child("server", "partial:ready", "wait:300", "print:x").readyOnLog(READY)
                .readyTimeout(Duration.ofSeconds(5)));

        IllegalStateException failure = assertThrows(IllegalStateException.class, builder::start);

        assertTrue(failure.getMessage().contains("was not ready in PT5S"), failure.getMessage());
        assertTrue(failure.getMessage().endsWith("readyx"), failure.getMessage());
    }

    @Test
    @DisplayName("close() stops every process when one's stop throws, kills the one cut short, removes the hook")
    void closeStopsEveryProcessWhenOneThrows() throws Exception {
        RecordingHooks hooks = new RecordingHooks();
        ProcessTopology topology = ProcessTopology.builder(logs).hooks(hooks)
            .process(child("server", "print:ready").readyOnLog(READY))
            .process(child("load", "ignore-term", "print:ready").readyOnLog(READY))
            .start();
        TopologyProcess server = topology.process("server");
        TopologyProcess load = topology.process("load");
        assertEquals(1, hooks.registered.size());

        Thread.currentThread().interrupt();
        assertThrows(InterruptedException.class, topology::close);

        assertEquals(SIGTERM_EXIT, server.process().onExit().get(10, TimeUnit.SECONDS).exitValue());
        assertEquals(SIGKILL_EXIT, load.process().onExit().get(10, TimeUnit.SECONDS).exitValue());
        assertTrue(hooks.registered.isEmpty());
    }

    @Test
    @DisplayName("a process that exits before it is ready fails the start, quoting its log, and the others stop")
    void aProcessThatExitsFailsTheStart() throws Exception {
        ProcessTopology.Builder builder = ProcessTopology.builder(logs)
            .process(child("server", "print:ready").readyOnLog(READY))
            .process(child("feed", "print:no config", "exit:3").readyOnLog(READY));

        RecordingHooks hooks = new RecordingHooks();
        builder.hooks(hooks);

        IllegalStateException failure = assertThrows(IllegalStateException.class, builder::start);

        assertTrue(hooks.added && hooks.registered.isEmpty(), "the hook was added, then removed");
        assertTrue(failure.getMessage().startsWith("the process feed ("), failure.getMessage());
        assertTrue(failure.getMessage().contains(") exited with 3 before it was ready; the last lines of "),
            failure.getMessage());
        assertTrue(failure.getMessage().endsWith("feed.log:\nno config"), failure.getMessage());
        assertProcessesEnded();
    }

    @Test
    @DisplayName("a process not ready in its timeout fails the start, and the next is never started")
    void aProcessNotReadyInTimeFailsTheStart() {
        ProcessTopology.Builder builder = ProcessTopology.builder(logs)
            .process(child("server", "print:starting").readyOnLog(READY).readyTimeout(Duration.ofMillis(500)))
            .process(child("load"));

        IllegalStateException failure = assertThrows(IllegalStateException.class, builder::start);

        assertTrue(failure.getMessage().contains("was not ready in PT0.5S"), failure.getMessage());
        assertFalse(Files.exists(logs.resolve("load.log")));
        assertProcessesEnded();
    }

    @Test
    @DisplayName("a health check that throws is not ready yet, and one that answers true is ready")
    void aHealthCheckThatThrowsIsNotReadyYet() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        ReadinessCheck check = remaining -> {
            if (calls.incrementAndGet() < 3) {
                throw new IOException("connection refused");
            }
            return true;
        };

        try (ProcessTopology topology = ProcessTopology.builder(logs)
                .process(child("server").readyWhen(check)).start()) {
            assertEquals(3, calls.get());
            assertTrue(topology.process("server").isAlive());
        }
    }

    @Test
    @DisplayName("a health check that answers true after the timeout is not ready in time")
    void aLateTrueIsNotReadyInTime() {
        ReadinessCheck late = remaining -> {
            waitFor(remaining.plusMillis(50));
            return true;
        };
        ProcessTopology.Builder builder = ProcessTopology.builder(logs)
            .process(child("server").readyWhen(late).readyTimeout(Duration.ofMillis(300)));

        IllegalStateException failure = assertThrows(IllegalStateException.class, builder::start);

        assertTrue(failure.getMessage().contains("was not ready in PT0.3S"), failure.getMessage());
        assertProcessesEnded();
    }

    @Test
    @DisplayName("a health check's true is not taken if the process exited while it ran")
    void aTrueFromAProcessThatExitedIsNotTaken() {
        ReadinessCheck afterExit = remaining -> {
            ProcessHandle child = ProcessHandle.current().children()
                .filter(p -> p.info().arguments().map(a -> List.of(a).contains(jar.toString())).orElse(false))
                .findFirst().orElseThrow();
            child.onExit().get(remaining.toNanos(), TimeUnit.NANOSECONDS);
            return true;
        };
        ProcessTopology.Builder builder = ProcessTopology.builder(logs)
            .process(child("server", "wait:200", "exit:0").readyWhen(afterExit));

        IllegalStateException failure = assertThrows(IllegalStateException.class, builder::start);

        assertTrue(failure.getMessage().contains("exited with 0 before it was ready"), failure.getMessage());
    }

    @Test
    @DisplayName("close() whose stop is cut short kills the process, so it does not outlive the hook")
    void closeCutShortKillsTheProcess() throws Exception {
        ProcessTopology topology = ProcessTopology.builder(logs).hooks(new RecordingHooks())
            .process(child("stubborn", "ignore-term", "print:ready").readyOnLog(READY)).start();
        TopologyProcess stubborn = topology.process("stubborn");

        Thread.currentThread().interrupt();
        assertThrows(InterruptedException.class, topology::close);

        assertEquals(SIGKILL_EXIT, stubborn.process().onExit().get(10, TimeUnit.SECONDS).exitValue());
    }

    @Test
    @DisplayName("an interrupted health check ends the start with its InterruptedException, the process killed")
    void anInterruptedHealthCheckEndsTheStart() {
        RecordingHooks hooks = new RecordingHooks();
        ProcessTopology.Builder builder = ProcessTopology.builder(logs).hooks(hooks)
            .process(child("server").readyWhen(remaining -> {
                throw new InterruptedException("cancelled");
            }));

        assertThrows(InterruptedException.class, builder::start);

        assertTrue(hooks.added && hooks.registered.isEmpty(), "the hook was added, then removed");
        assertProcessesEnded();
    }

    @Test
    @DisplayName("stop() sends SIGKILL to a process still running after its grace")
    void stopKillsAProcessThatIgnoresSigterm() throws Exception {
        try (ProcessTopology topology = ProcessTopology.builder(logs)
                .process(child("stubborn", "ignore-term", "print:ready").readyOnLog(READY)
                    .stopGrace(Duration.ofMillis(300)))
                .start()) {
            assertEquals(SIGKILL_EXIT, topology.process("stubborn").stop());
        }
    }

    @Test
    @DisplayName("freeze() stops a process where it is, resume() lets it go on, and stop() resumes it first")
    void freezeResumeAndStopAFrozenProcess() throws Exception {
        try (ProcessTopology topology = ProcessTopology.builder(logs)
                .process(child("server", "print:ready").readyOnLog(READY)).start()) {
            TopologyProcess server = topology.process("server");

            server.freeze();
            assertTrue(state(server.pid()).startsWith("T"), state(server.pid()));
            server.resume();
            assertFalse(state(server.pid()).startsWith("T"), state(server.pid()));
            server.freeze();

            assertEquals(SIGTERM_EXIT, server.stop());
        }
    }

    @Test
    @DisplayName("activeProcessorCount() and nice() limit a sidecar's CPU, in its command")
    void aSidecarsLimitsAreInItsCommand() throws Exception {
        ProcessSpec sidecar = ProcessSpec.jar("load", jar).jvmOptions(List.of("-Xmx64m")).arguments("print:ready")
            .activeProcessorCount(2).nice(10);

        assertEquals(List.of("nice", "-n", "10", "java", "-Xmx64m", "-XX:ActiveProcessorCount=2", "-jar",
            jar.toString(), "print:ready"), sidecar.command(Path.of("java")));
        try (ProcessTopology topology = ProcessTopology.builder(logs)
                .process(sidecar.readyOnLog(READY)).start()) {
            TopologyProcess load = topology.process("load");
            assertEquals("10", run("ps", "-o", "nice=", "-p", Long.toString(load.pid())));
            assertTrue(load.toString().startsWith("load ("));
        }
    }

    @Test
    @DisplayName("a spec refuses an unsafe name and settings out of range, and a builder a name given twice")
    void refusesNamesAndSettingsOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> ProcessSpec.jar("../server", jar));
        assertThrows(IllegalArgumentException.class, () -> ProcessSpec.jar(".hidden", jar));
        assertThrows(IllegalArgumentException.class, () -> ProcessSpec.jar("", jar));
        ProcessSpec spec = ProcessSpec.jar("server", jar);
        assertThrows(IllegalArgumentException.class, () -> spec.readyTimeout(Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> spec.stopGrace(Duration.ofSeconds(-1)));
        assertThrows(IllegalArgumentException.class, () -> spec.activeProcessorCount(0));
        assertThrows(IllegalArgumentException.class, () -> spec.nice(-1));
        assertThrows(IllegalArgumentException.class, () -> spec.nice(20));
        assertThrows(IllegalArgumentException.class,
            () -> ProcessTopology.builder(logs).process(spec).process(ProcessSpec.jar("server", jar)));
        assertThrows(IllegalArgumentException.class,
            () -> ProcessTopology.builder(logs).process(spec).process(ProcessSpec.jar("Server", jar)));
        assertEquals("server", spec.name());
        assertTrue(spec.toString().startsWith("server [java, -jar, "), spec.toString());
    }

    /// Shutdown hooks that are recorded, and not registered with the JVM.
    private static final class RecordingHooks implements ProcessTopology.ShutdownHooks {

        private final Set<Thread> registered = ConcurrentHashMap.newKeySet();
        private boolean added;

        @Override
        public void add(final Thread hook) {
            registered.add(hook);
            added = true;
        }

        @Override
        public void remove(final Thread hook) {
            registered.remove(hook);
        }
    }

    private static ProcessSpec child(final String name, final String... steps) {
        return ProcessSpec.jar(name, jar).jvmOptions(List.of("-Xmx32m", "-XX:TieredStopAtLevel=1"))
            .arguments(steps);
    }

    /// Asserts that no child of this JVM is still running a jar of this test: the ones a failed start stopped.
    private static void assertProcessesEnded() {
        ProcessHandle.current().children().forEach(ProcessTopologyTest::awaitExit);
        assertTrue(ProcessHandle.current().children()
            .noneMatch(p -> p.isAlive() && p.info().arguments().map(a -> List.of(a).contains(jar.toString()))
                .orElse(false)));
    }

    /// Waits up to 10 s for a process to exit, as a killed one does at once.
    private static void awaitExit(final ProcessHandle process) {
        try {
            process.onExit().get(10, TimeUnit.SECONDS);
        } catch (final ExecutionException | TimeoutException e) {
            throw new AssertionError("the process " + process.pid() + " did not exit in 10 s", e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("interrupted while waiting for " + process.pid(), e);
        }
    }

    private static String state(final long pid) throws IOException, InterruptedException {
        return run("ps", "-o", "stat=", "-p", Long.toString(pid));
    }

    private static String run(final String... command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).strip();
        assertTrue(process.waitFor(10, TimeUnit.SECONDS));
        return output;
    }

    private static void waitFor(final Duration duration) {
        long deadline = System.nanoTime() + duration.toNanos();
        while (System.nanoTime() - deadline < 0) {
            LockSupport.parkNanos(deadline - System.nanoTime());
        }
    }
}
