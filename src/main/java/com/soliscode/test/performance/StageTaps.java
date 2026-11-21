/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.performance;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLongArray;

import com.soliscode.performance.MetricKind;
import com.soliscode.performance.ScenarioMetric;

/// The latency of each stage of a pipeline, correlated by an id.
///
/// The stages are named when the taps are made, the first the origin. Each stage taps an id when the id reaches it
/// ([#tap(int, long)]); [#metrics(double)] then reports, for each stage after the origin, its latency from the
/// stage before it, over the ids tapped at the origin, at the stage before and at the stage, and fails if those are
/// fewer than a share of the ids tapped at the origin.
///
/// The times come from one clock: one JVM's `System.nanoTime`, or a clock the caller knows its processes share.
///
/// ## Usage Examples
/// ```java
/// StageTaps taps = StageTaps.of(100_000, "send", "match", "publish");
/// // on the sending thread
/// taps.tap(0, id);
/// // on the matching thread, and the publishing thread
/// taps.tap(1, id);
/// taps.tap(2, id);
/// // once every thread that taps has been joined
/// List<ScenarioMetric> metrics = taps.metrics(0.95);
/// ```
///
/// ## Thread Safety
/// [#tap(int, long)] and [#tap(int, long, long)] are thread safe and allocate nothing. [#metrics(double)] reads
/// what the taps wrote once they have stopped: the caller joins the tapping threads, or otherwise orders them
/// before it.
///
/// @since 0.1.6
public final class StageTaps {

    /// The time of an id not tapped at a stage.
    private static final long NOT_TAPPED = Long.MIN_VALUE;

    private static final Duration DEFAULT_HIGHEST = Duration.ofMinutes(1);
    private static final int SIGNIFICANT_DIGITS = 3;

    private final List<String> stages;
    private final AtomicLongArray[] times;
    private final int capacity;
    private final long highestNanos;

    private StageTaps(final int capacityArg, final Duration highest, final List<String> stagesArg) {
        if (capacityArg < 1) {
            throw new IllegalArgumentException("the capacity is " + capacityArg + ", below 1");
        }
        Objects.requireNonNull(highest, "highest");
        if (highest.compareTo(Duration.ofNanos(2)) < 0) {
            throw new IllegalArgumentException("the highest latency " + highest + " is below 2 ns");
        }
        Set<String> seen = new HashSet<>();
        for (final String stage : stagesArg) {
            Objects.requireNonNull(stage, "a stage's name");
            if (stage.isBlank() || !seen.add(stage)) {
                throw new IllegalArgumentException("the stage name '" + stage + "' is blank or given twice");
            }
        }
        this.capacity = capacityArg;
        this.highestNanos = highest.toNanos();
        this.stages = List.copyOf(stagesArg);
        this.times = new AtomicLongArray[stagesArg.size()];
        for (int stage = 0; stage < times.length; stage++) {
            long[] notTapped = new long[capacityArg];
            Arrays.fill(notTapped, NOT_TAPPED);
            times[stage] = new AtomicLongArray(notTapped);
        }
    }

    /// Returns taps for ids from 0 to `capacity − 1` at the stages named, the first the origin, with a highest
    /// latency of a minute.
    ///
    /// @param capacity how many ids, at least 1
    /// @param origin the first stage's name
    /// @param next the second stage's name
    /// @param rest the other stages' names, in order
    /// @return the taps
    /// @throws IllegalArgumentException if the capacity is below 1, or a name is blank or given twice
    public static StageTaps of(final int capacity, final String origin, final String next, final String... rest) {
        return of(capacity, DEFAULT_HIGHEST, origin, next, rest);
    }

    /// Returns taps for ids from 0 to `capacity − 1` at the stages named, the first the origin.
    ///
    /// @param capacity how many ids, at least 1
    /// @param highest the highest latency a stage's recorder takes, at least 2 ns
    /// @param origin the first stage's name
    /// @param next the second stage's name
    /// @param rest the other stages' names, in order
    /// @return the taps
    /// @throws IllegalArgumentException if the capacity is below 1, the highest below 2 ns, or a name is blank or
    ///     given twice
    public static StageTaps of(final int capacity, final Duration highest, final String origin, final String next,
        final String... rest) {
        List<String> names = new ArrayList<>(rest.length + 2);
        names.add(origin);
        names.add(next);
        names.addAll(Arrays.asList(rest));
        return new StageTaps(capacity, highest, names);
    }

    /// Returns the stages' names, in order, the origin first.
    ///
    /// @return the names
    public List<String> stages() {
        return stages;
    }

    /// Returns a stage's index, for [#tap(int, long)].
    ///
    /// @param name the stage's name
    /// @return its index, the origin's 0
    /// @throws IllegalArgumentException if there is no such stage
    public int stage(final String name) {
        int index = stages.indexOf(Objects.requireNonNull(name, "name"));
        if (index < 0) {
            throw new IllegalArgumentException("there is no stage " + name + " in " + stages);
        }
        return index;
    }

    /// Records that an id reached a stage now, by `System.nanoTime`. A second tap of the id at the stage replaces
    /// the first.
    ///
    /// @param stage the stage's index
    /// @param id the id, from 0 to the capacity less 1
    /// @throws IllegalArgumentException if the stage or the id is out of range
    public void tap(final int stage, final long id) {
        tap(stage, id, System.nanoTime());
    }

    /// Records that an id reached a stage at a time. A second tap of the id at the stage replaces the first.
    ///
    /// @param stage the stage's index
    /// @param id the id, from 0 to the capacity less 1
    /// @param nanos the time, in nanoseconds of the clock every tap uses; not `Long.MIN_VALUE`
    /// @throws IllegalArgumentException if the stage or the id is out of range, or the time is `Long.MIN_VALUE`
    public void tap(final int stage, final long id, final long nanos) {
        if (stage < 0 || stage >= times.length) {
            throw new IllegalArgumentException("the stage " + stage + " is not from 0 to " + (times.length - 1));
        }
        if (id < 0 || id >= capacity) {
            throw new IllegalArgumentException("the id " + id + " is not from 0 to " + (capacity - 1));
        }
        if (nanos == NOT_TAPPED) {
            throw new IllegalArgumentException("the time Long.MIN_VALUE marks an id not tapped");
        }
        times[stage].set((int) id, nanos);
    }

    /// Returns, for each stage after the origin, its latency from the stage before it as `<stage>.p50` to
    /// `<stage>.max` (`time`, `ns`), `<stage>.samples`, the ids tapped at the origin, the stage before and the
    /// stage (a `count` in `samples`), and `<stage>.reversed`, those of them whose time at the stage is before
    /// their time at the stage before it, which are not samples (a `count` in `samples`).
    ///
    /// @param minimumShare the fewest samples a stage may have, as a share of the ids tapped at the origin, 0 to 1
    /// @return the metrics
    /// @throws IllegalArgumentException if the share is not from 0 to 1
    /// @throws IllegalStateException if a stage has no samples or fewer than the share, or a latency is above the
    ///     highest
    public List<ScenarioMetric> metrics(final double minimumShare) {
        if (!(minimumShare >= 0.0 && minimumShare <= 1.0)) {
            throw new IllegalArgumentException("the minimum share " + minimumShare + " is not from 0 to 1");
        }
        long origins = originTaps();
        List<ScenarioMetric> metrics = new ArrayList<>();
        for (int stage = 1; stage < times.length; stage++) {
            metrics.addAll(stageMetrics(stage, origins, minimumShare));
        }
        return List.copyOf(metrics);
    }

    private long originTaps() {
        long origins = 0;
        for (int id = 0; id < capacity; id++) {
            if (times[0].get(id) != NOT_TAPPED) {
                origins++;
            }
        }
        return origins;
    }

    private List<ScenarioMetric> stageMetrics(final int stage, final long origins, final double minimumShare) {
        String name = stages.get(stage);
        LatencyRecorder latencies = new LatencyRecorder(highestNanos, SIGNIFICANT_DIGITS);
        long reversed = 0;
        for (int id = 0; id < capacity; id++) {
            long before = times[stage - 1].get(id);
            long at = times[stage].get(id);
            if (times[0].get(id) == NOT_TAPPED || before == NOT_TAPPED || at == NOT_TAPPED) {
                continue;
            }
            if (at < before) {
                reversed++;
            } else {
                record(latencies, name, id, at - before);
            }
        }
        long samples = latencies.count();
        if (samples == 0 || samples < minimumShare * origins) {
            throw new IllegalStateException("the stage " + name + " has " + samples + " samples of the " + origins
                + " ids tapped at the origin, fewer than the share of " + minimumShare);
        }
        List<ScenarioMetric> metrics = new ArrayList<>(latencies.metrics(name));
        metrics.add(new ScenarioMetric(name + ".samples", MetricKind.COUNT, "samples", samples));
        metrics.add(new ScenarioMetric(name + ".reversed", MetricKind.COUNT, "samples", reversed));
        return metrics;
    }

    /// Records a latency; a difference that overflowed is negative, and is above the highest as well.
    private static void record(final LatencyRecorder latencies, final String stage, final int id,
        final long latency) {
        if (!latencies.accepts(latency)) {
            throw new IllegalStateException("the id " + id + " took " + Long.toUnsignedString(latency) + " ns to "
                + "reach the stage " + stage + ", above the highest latency of " + latencies.highestTrackable()
                + " ns");
        }
        latencies.record(latency);
    }
}
