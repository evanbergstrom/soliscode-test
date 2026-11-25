# How to write a performance test

**Goal:** an allocation bound checked in an ordinary JUnit test, and a scenario for the SolisCode performance gate that measures your code's latency under a paced load.

## Before you start

- SolisCode Test 0.1.6 or later
- For a scenario: a project on the SolisCode parent with the performance gate turned on, as the parent's [Performance](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.17/docs/guide/03-gates/performance.md) page says. An allocation bound needs neither

## Steps

1. To bound what an operation allocates, call `AllocationAssertion.assertAllocatesAtMost(bound, n, operation)` in a test. It warms the operation with `n` runs, measures runs of `n` and of `10n`, and fails if the difference is more than `bound` bytes a run:

   ```java
   import java.time.Duration;

   import org.junit.jupiter.api.DisplayName;
   import org.junit.jupiter.api.Test;

   import com.soliscode.test.performance.AllocationAssertion;
   import com.soliscode.test.performance.LatencyRecorder;

   class RecorderAllocationTest {

       private static final double LESS_THAN_ONE_OBJECT = 1;

       @Test
       @DisplayName("Recording a latency allocates nothing")
       void recordingALatencyAllocatesNothing() throws Exception {
           LatencyRecorder recorder = LatencyRecorder.upTo(Duration.ofSeconds(1));
           AllocationAssertion.assertAllocatesAtMost(LESS_THAN_ONE_OBJECT, 10_000, () -> recorder.record(1_000));
       }
   }
   ```

   > **Note:** to assert that an operation allocates nothing, use a bound of 1 byte a run rather than 0. The smallest object is 16 bytes, so 1 catches an operation that allocates on every run, while the measurement's noise, a fraction of a byte a run, can fail a bound of 0. The bound is an average, on the calling thread only: an operation that allocates once in many runs, or allocates on another thread, can pass it, so a passing test is not proof that nothing is ever allocated. Where the JVM cannot measure allocation, the test is skipped, not passed.

2. To measure latency, write a `PerformanceScenario` (from `soliscode-performance-api`). Here it counts hits on the `HitCounter` of [How to write a thread-safety test](write-a-thread-safety-test.md). Give it its points with a `Sweep`, send the operation at a fixed rate with an `OpenLoopPacer`, and return the run's metrics:

   ```java
   import java.time.Duration;
   import java.util.List;
   import java.util.Map;

   import com.soliscode.performance.PerformanceScenario;
   import com.soliscode.performance.ScenarioContext;
   import com.soliscode.performance.ScenarioResult;
   import com.soliscode.test.performance.OpenLoopPacer;
   import com.soliscode.test.performance.PacedRun;
   import com.soliscode.test.performance.Sweep;

   public final class CounterScenario implements PerformanceScenario {

       private static final Duration WARMUP = Duration.ofSeconds(2);
       private static final Duration MEASURED = Duration.ofSeconds(10);

       @Override
       public String name() {
           return "counter";
       }

       @Override
       public List<Map<String, String>> points() {
           return Sweep.of("rate", 1_000, 10_000).points();
       }

       @Override
       public ScenarioResult run(final ScenarioContext context, final Map<String, String> point) throws Exception {
           HitCounter counter = new HitCounter();
           PacedRun run = OpenLoopPacer.at(Sweep.intParameter(point, "rate"))
                   .warmup(WARMUP)
                   .duration(MEASURED)
                   .run(sequence -> counter.hit());
           return new ScenarioResult(run.metrics(), List.of(), List.of());
       }

       @Override
       public Duration estimate(final Map<String, String> point) {
           return WARMUP.plus(MEASURED);
       }
   }
   ```

   The pacer times each operation from when it was due to be sent, not from when it started, so a slow operation makes the ones after it late, and their latency shows it. `run.metrics()` gives the latencies' percentiles and the rate achieved.

3. Register the scenario: list its class's full name, one line, in `src/test/resources/META-INF/services/com.soliscode.performance.PerformanceScenario`. Then run the gate, `mvn -B soliscode-performance:run`. It runs each point `repeats()` times (at least 5), each in a JVM of its own, and compares each metric with the baseline you record, as the parent's [Performance](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.17/docs/guide/03-gates/performance.md) page and its [build-and-test.md, "Performance"](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.17/docs/design/reference/build-and-test.md#performance) say.

   > **Note:** the other parts of `com.soliscode.test.performance` are for larger scenarios: `LatencyRecorder` on its own, for latencies you time yourself; `ProcessTopology`, to start processes from jars, each ready before the next; `StageTaps`, for the latency of each stage of a pipeline; and `HeapSoak`, for the slope of a process's live heap over a long run.

## Check that it worked

The allocation test passes:

```text
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

An operation that allocates fails it, with `the operation allocates … bytes per run, more than the bound of 1.0`. A scenario's metrics are in the gate's report, `target/soliscode-performance/<run id>/report.md`.

## Troubleshooting

| Symptom | Fix |
|---|---|
| An allocation test fails with a fraction of a byte a run, over a bound of 0 | Measurement noise: use a bound of 1 |
| An allocation test is skipped | The JVM cannot measure allocation on this thread (it is virtual, or `com.sun.management.ThreadMXBean` is missing): run it on a platform thread of a HotSpot JVM |
| `IllegalStateException: operation … took … ns from its scheduled send, above the highest latency of … ns` | An operation took longer than the pacer can record (by default the warmup and the duration together, and a minute): give `highestLatency` a larger value, or find why it stalled |
| `IllegalStateException: no latency was recorded, so there is none to report` | A `LatencyRecorder` used on its own was asked for its metrics before any `record` |

## See also

- [API reference](../reference/api.md#packages), for each class of `com.soliscode.test.performance`
- [Bounded thread-safety tests](../concepts/bounded-thread-safety-tests.md), on why tests here have time limits
