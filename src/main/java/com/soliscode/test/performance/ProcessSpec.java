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

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

/// A process for a [ProcessTopology] to start: a jar run with `java -jar`, its JVM options and arguments, how it is
/// ready, and how long it has to be ready and to stop.
///
/// A sidecar, a process beside the ones under test (a load generator, a feed), is a spec with CPU limits:
/// [#activeProcessorCount(int)] and [#nice(int)]. Any spec may take them.
///
/// ## Usage Examples
/// ```java
/// ProcessSpec server = ProcessSpec.jar("server", serverJar)
///     .jvmOptions(context.jvmOptions())
///     .arguments("--port", "9000")
///     .readyOnLog(Pattern.compile("listening on 9000"));
/// ProcessSpec load = ProcessSpec.jar("load", loadJar)
///     .activeProcessorCount(2)
///     .nice(10);
/// ```
///
/// ## Thread Safety
/// Immutable.
///
/// @since 0.1.6
public final class ProcessSpec {

    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_-][A-Za-z0-9._-]*");
    private static final Duration DEFAULT_READY_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration DEFAULT_STOP_GRACE = Duration.ofSeconds(10);
    private static final int MAX_NICE = 19;

    private final String name;
    private final Path jar;
    private final List<String> jvmOptions;
    private final List<String> arguments;
    private final @Nullable Pattern readyLine;
    private final @Nullable ReadinessCheck readyCheck;
    private final Duration readyTimeout;
    private final Duration stopGrace;
    private final int processors;
    private final int niceness;

    private ProcessSpec(final Builder from) {
        this.name = from.name;
        this.jar = from.jar;
        this.jvmOptions = List.copyOf(from.jvmOptions);
        this.arguments = List.copyOf(from.arguments);
        this.readyLine = from.readyLine;
        this.readyCheck = from.readyCheck;
        this.readyTimeout = from.readyTimeout;
        this.stopGrace = from.stopGrace;
        this.processors = from.processors;
        this.niceness = from.niceness;
    }

    /// Returns a spec of a jar run with `java -jar`, ready at once, with 60 s to be ready and 10 s to stop.
    ///
    /// @param name the process's name: letters, digits, `.`, `_` and `-`, not starting with `.`, since it names
    ///     its log file
    /// @param jar the jar
    /// @return the spec
    /// @throws IllegalArgumentException if the name is not a safe file name
    public static ProcessSpec jar(final String name, final Path jar) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(jar, "jar");
        if (!NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("the process name '" + name + "' is not letters, digits, '.', '_' "
                + "and '-', not starting with '.'");
        }
        Builder builder = new Builder(name, jar);
        return new ProcessSpec(builder);
    }

    /// Returns the process's name.
    ///
    /// @return the name
    public String name() {
        return name;
    }

    /// Returns this spec with JVM options, before `-jar`, in place of any it had.
    ///
    /// @param options the options, such as a scenario context's `jvmOptions()`
    /// @return the new spec
    public ProcessSpec jvmOptions(final List<String> options) {
        Builder builder = builder();
        builder.jvmOptions = List.copyOf(options);
        return new ProcessSpec(builder);
    }

    /// Returns this spec with arguments, after the jar, in place of any it had.
    ///
    /// @param args the arguments
    /// @return the new spec
    public ProcessSpec arguments(final String... args) {
        Builder builder = builder();
        builder.arguments = List.of(args);
        return new ProcessSpec(builder);
    }

    /// Returns this spec ready once a complete line of its log matches the pattern (`find`).
    ///
    /// @param line the pattern
    /// @return the new spec, in place of any other way to be ready
    public ProcessSpec readyOnLog(final Pattern line) {
        Builder builder = builder();
        builder.readyLine = Objects.requireNonNull(line, "line");
        builder.readyCheck = null;
        return new ProcessSpec(builder);
    }

    /// Returns this spec ready once the health check answers `true`.
    ///
    /// @param check the check
    /// @return the new spec, in place of any other way to be ready
    public ProcessSpec readyWhen(final ReadinessCheck check) {
        Builder builder = builder();
        builder.readyCheck = Objects.requireNonNull(check, "check");
        builder.readyLine = null;
        return new ProcessSpec(builder);
    }

    /// Returns this spec with the time it has to be ready.
    ///
    /// @param timeout the time, above 0
    /// @return the new spec
    /// @throws IllegalArgumentException if it is not above 0
    public ProcessSpec readyTimeout(final Duration timeout) {
        Builder builder = builder();
        builder.readyTimeout = positive(timeout, "ready timeout");
        return new ProcessSpec(builder);
    }

    /// Returns this spec with the time it has to stop after SIGTERM, before SIGKILL.
    ///
    /// @param grace the time, above 0
    /// @return the new spec
    /// @throws IllegalArgumentException if it is not above 0
    public ProcessSpec stopGrace(final Duration grace) {
        Builder builder = builder();
        builder.stopGrace = positive(grace, "stop grace");
        return new ProcessSpec(builder);
    }

    /// Returns this spec with the JVM told it has `count` processors (`-XX:ActiveProcessorCount`).
    ///
    /// @param count the processors, at least 1
    /// @return the new spec
    /// @throws IllegalArgumentException if it is below 1
    public ProcessSpec activeProcessorCount(final int count) {
        if (count < 1) {
            throw new IllegalArgumentException("the active processor count " + count + " is below 1");
        }
        Builder builder = builder();
        builder.processors = count;
        return new ProcessSpec(builder);
    }

    /// Returns this spec started under `nice -n level`. A negative niceness needs privilege, without which
    /// `nice` runs the command at normal priority, so it is refused.
    ///
    /// @param level the niceness, 0 to 19
    /// @return the new spec
    /// @throws IllegalArgumentException if it is not from 0 to 19
    public ProcessSpec nice(final int level) {
        if (level < 0 || level > MAX_NICE) {
            throw new IllegalArgumentException("the niceness " + level + " is not from 0 to " + MAX_NICE);
        }
        Builder builder = builder();
        builder.niceness = level;
        return new ProcessSpec(builder);
    }

    @Override
    public String toString() {
        return name + " " + command(Path.of("java"));
    }

    /// Returns the command that starts the process with the `java` given.
    List<String> command(final Path java) {
        List<String> command = new ArrayList<>();
        if (niceness > 0) {
            command.addAll(List.of("nice", "-n", Integer.toString(niceness)));
        }
        command.add(java.toString());
        command.addAll(jvmOptions);
        if (processors > 0) {
            // after the options, so that an inherited -XX:ActiveProcessorCount does not override the sidecar's
            command.add("-XX:ActiveProcessorCount=" + processors);
        }
        command.add("-jar");
        command.add(jar.toString());
        command.addAll(arguments);
        return command;
    }

    /// Returns whether the command needs `nice`.
    boolean usesNice() {
        return niceness > 0;
    }

    @Nullable Pattern readyLine() {
        return readyLine;
    }

    @Nullable ReadinessCheck readyCheck() {
        return readyCheck;
    }

    Duration readyTimeout() {
        return readyTimeout;
    }

    Duration stopGrace() {
        return stopGrace;
    }

    private Builder builder() {
        Builder builder = new Builder(name, jar);
        builder.jvmOptions = jvmOptions;
        builder.arguments = arguments;
        builder.readyLine = readyLine;
        builder.readyCheck = readyCheck;
        builder.readyTimeout = readyTimeout;
        builder.stopGrace = stopGrace;
        builder.processors = processors;
        builder.niceness = niceness;
        return builder;
    }

    private static Duration positive(final Duration duration, final String what) {
        Objects.requireNonNull(duration, what);
        if (duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException("the " + what + " " + duration + " is not above 0");
        }
        return duration;
    }

    /// The fields of a spec being made.
    private static final class Builder {

        private final String name;
        private final Path jar;
        private List<String> jvmOptions = List.of();
        private List<String> arguments = List.of();
        private @Nullable Pattern readyLine;
        private @Nullable ReadinessCheck readyCheck;
        private Duration readyTimeout = DEFAULT_READY_TIMEOUT;
        private Duration stopGrace = DEFAULT_STOP_GRACE;
        private int processors;
        private int niceness;

        private Builder(final String nameArg, final Path jarArg) {
            this.name = nameArg;
            this.jar = jarArg;
        }
    }
}
