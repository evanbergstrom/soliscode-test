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
package com.soliscode.test.breakable;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.jspecify.annotations.Nullable;

/// An executor that can be broken in well-defined ways, to show that
/// [com.soliscode.test.contract.executorservice.ExecutorServiceContract] and
/// [com.soliscode.test.contract.executorservice.ThreadSafeExecutorServiceContract] can fail.
///
/// Like [BreakableFuture], it wraps no JDK implementation: it is an executor of its own, a fixed number of worker
/// threads over a queue, with a run state, all guarded by a lock, so that each break acts on that state and nothing
/// else, and so that its thread-unsafe breaks can step outside the lock. Unbroken, it keeps [ExecutorService]'s
/// contract, much as a fixed `ThreadPoolExecutor` does:
/// - its workers start as tasks arrive, up to the number it was made with, and take tasks from the queue in order;
/// - `shutdown` lets the queued tasks run and rejects new ones, and the executor terminates once the queue is empty
///   and every worker has ended;
/// - `shutdownNow` takes the queued tasks off the queue and returns them, and interrupts the workers;
/// - `submit` runs its task as a [FutureTask], and `invokeAll` and `invokeAny` are its own, over `submit` and an
///   [ExecutorCompletionService] on itself.
///
/// It is made with its constructor, and has no builder.
///
/// # Breaks
/// The breaks that are supported for this class are listed in the description of the method that they impact.
///
/// ## Thread Safety
/// Thread safe, unless [#SUBMIT_IS_NOT_THREAD_SAFE] or [#SHUTDOWN_IS_NOT_THREAD_SAFE] is set: its queue and state
/// are guarded by its lock. Its breaks are a copy of the set it was made with, which `addBreak` changes,
/// unsynchronized.
///
/// ## Serialization
/// It is not serializable, though every breakable is declared so: its worker threads cannot be. Serializing one
/// throws [NotSerializableException].
///
/// @since 0.1.5
/// @see ExecutorService
public class BreakableExecutorService extends AbstractBreakable implements ExecutorService {

    /// **Purpose:** `execute` drops the command.
    ///
    /// **Effect:** `execute` returns without queuing the command, which never runs.
    ///
    /// **Affected Methods:** [#execute(Runnable)], and both `invokeAny`s, which submit through it
    public static final Break EXECUTE_DOES_NOT_RUN_THE_TASK =
            new Break("execute does not run the task");

    /// **Purpose:** `execute` runs the command more than once.
    ///
    /// **Effect:** `execute` queues the command twice.
    ///
    /// **Affected Methods:** [#execute(Runnable)], and both `invokeAny`s, which submit through it
    public static final Break EXECUTE_RUNS_THE_TASK_TWICE =
            new Break("execute runs the task twice");

    /// **Purpose:** `execute` takes a null command, where its Javadoc says it throws `NullPointerException`.
    ///
    /// **Effect:** `execute(null)` returns, and nothing runs.
    ///
    /// **Affected Methods:** [#execute(Runnable)]
    public static final Break EXECUTE_ACCEPTS_NULL =
            new Break("execute accepts null");

    /// **Purpose:** `submit` loses a callable's result.
    ///
    /// **Effect:** the future `submit(Callable)` returns succeeds with `null` once the callable has run.
    ///
    /// **Affected Methods:** [#submit(Callable)], and both `invokeAll`s, which submit through it
    public static final Break SUBMIT_CALLABLE_RETURNS_NULL =
            new Break("submit callable returns null");

    /// **Purpose:** `submit` hides a callable's exception.
    ///
    /// **Effect:** the future `submit(Callable)` returns succeeds with `null` when the callable throws.
    ///
    /// **Affected Methods:** [#submit(Callable)], and both `invokeAll`s, which submit through it
    public static final Break SUBMIT_CALLABLE_SWALLOWS_THE_EXCEPTION =
            new Break("submit callable swallows the exception");

    /// **Purpose:** `submit` takes a null callable, where its Javadoc says it throws `NullPointerException`.
    ///
    /// **Effect:** `submit((Callable) null)` returns a future already succeeded with `null`.
    ///
    /// **Affected Methods:** [#submit(Callable)]
    public static final Break SUBMIT_CALLABLE_ACCEPTS_NULL =
            new Break("submit callable accepts null");

    /// **Purpose:** `submit` completes a runnable's future without running it.
    ///
    /// **Effect:** the future `submit(Runnable)` returns succeeds with `null`, and the runnable never runs.
    ///
    /// **Affected Methods:** [#submit(Runnable)]
    public static final Break SUBMIT_RUNNABLE_DOES_NOT_RUN_THE_TASK =
            new Break("submit runnable does not run the task");

    /// **Purpose:** `submit` takes a null runnable, where its Javadoc says it throws `NullPointerException`.
    ///
    /// **Effect:** `submit((Runnable) null)` returns a future already succeeded with `null`.
    ///
    /// **Affected Methods:** [#submit(Runnable)]
    public static final Break SUBMIT_RUNNABLE_ACCEPTS_NULL =
            new Break("submit runnable accepts null");

    /// **Purpose:** `submit` loses the result it was given for a runnable.
    ///
    /// **Effect:** the future `submit(Runnable, Object)` returns succeeds with `null` once the runnable has run.
    ///
    /// **Affected Methods:** [#submit(Runnable, Object)]
    public static final Break SUBMIT_RUNNABLE_WITH_RESULT_RETURNS_NULL =
            new Break("submit runnable with result returns null");

    /// **Purpose:** `submit` takes a null runnable with a result, where its Javadoc says it throws
    /// `NullPointerException`.
    ///
    /// **Effect:** `submit(null, result)` returns a future already succeeded with the result.
    ///
    /// **Affected Methods:** [#submit(Runnable, Object)]
    public static final Break SUBMIT_RUNNABLE_WITH_RESULT_ACCEPTS_NULL =
            new Break("submit runnable with result accepts null");

    /// **Purpose:** tasks submitted from several threads at once are lost.
    ///
    /// **Effect:** `execute` and every `submit` claim the task's place in the queue outside the lock (they read the
    /// next sequence number, yield, and write it back plus one), and then queue the task at that place under the
    /// lock, so that two at once can claim the same place, and one task overwrites the other, which never runs.
    ///
    /// **Affected Methods:** [#execute(Runnable)], [#submit(Callable)], [#submit(Runnable)],
    /// [#submit(Runnable, Object)], and `invokeAll` and `invokeAny`, which submit through them
    public static final Break SUBMIT_IS_NOT_THREAD_SAFE =
            new Break("submit is not thread safe");

    /// **Purpose:** `shutdown` throws away the tasks queued, where its Javadoc says "previously submitted tasks are
    /// executed".
    ///
    /// **Effect:** `shutdown` empties the queue; the tasks on it never run, and their futures never complete.
    ///
    /// **Affected Methods:** [#shutdown()]
    public static final Break SHUTDOWN_DROPS_QUEUED_TASKS =
            new Break("shutdown drops queued tasks");

    /// **Purpose:** `shutdown` leaves the executor taking tasks, where its Javadoc says "no new tasks will be
    /// accepted".
    ///
    /// **Effect:** after `shutdown`, but not `shutdownNow`, `execute` and every `submit` queue the task and return.
    ///
    /// **Affected Methods:** [#execute(Runnable)], [#submit(Callable)], [#submit(Runnable)],
    /// [#submit(Runnable, Object)], and `invokeAll` and `invokeAny`, which submit through them
    public static final Break SHUTDOWN_DOES_NOT_REJECT =
            new Break("shutdown does not reject");

    /// **Purpose:** `shutdown` waits for the tasks, where its Javadoc says it "does not wait".
    ///
    /// **Effect:** `shutdown` returns only once the executor has terminated.
    ///
    /// **Affected Methods:** [#shutdown()]
    public static final Break SHUTDOWN_WAITS_FOR_TASKS =
            new Break("shutdown waits for tasks");

    /// **Purpose:** a second `shutdown` has an effect, where its Javadoc says it has none.
    ///
    /// **Effect:** `shutdown` of an executor already shut down returns it to running: `isShutdown()` is `false`
    /// again, and it takes tasks again.
    ///
    /// **Affected Methods:** [#shutdown()]
    public static final Break SECOND_SHUTDOWN_RESTARTS =
            new Break("second shutdown restarts");

    /// **Purpose:** a task submitted while another thread shuts the executor down is accepted and never runs.
    ///
    /// **Effect:** `execute` and every `submit` check the run state outside the lock, yield, and then queue the task
    /// without checking again, and start no worker once shut down, so that a task can be queued after the workers
    /// have emptied the queue and ended.
    ///
    /// **Affected Methods:** [#execute(Runnable)], [#submit(Callable)], [#submit(Runnable)],
    /// [#submit(Runnable, Object)], and `invokeAll` and `invokeAny`, which submit through them
    public static final Break SHUTDOWN_IS_NOT_THREAD_SAFE =
            new Break("shutdown is not thread safe");

    /// **Purpose:** `shutdownNow` returns tasks that still run, where its Javadoc says it "halts the processing of
    /// waiting tasks" and returns them.
    ///
    /// **Effect:** `shutdownNow` returns the queued tasks, and leaves them on the queue, as `shutdown` does.
    ///
    /// **Affected Methods:** [#shutdownNow()]
    public static final Break SHUTDOWN_NOW_RUNS_THE_TASKS_IT_RETURNS =
            new Break("shutdown now runs the tasks it returns");

    /// **Purpose:** `shutdownNow` loses the waiting tasks, where its Javadoc says it "returns a list of the tasks
    /// that were awaiting execution".
    ///
    /// **Effect:** `shutdownNow` empties the queue and returns an empty list; the tasks it took off never run, and
    /// their futures are not cancelled.
    ///
    /// **Affected Methods:** [#shutdownNow()]
    public static final Break SHUTDOWN_NOW_DISCARDS_WAITING_TASKS =
            new Break("shutdown now discards waiting tasks");

    /// **Purpose:** `shutdownNow` leaves the executor taking tasks.
    ///
    /// **Effect:** after `shutdownNow`, `execute` and every `submit` return without throwing, and the task never
    /// runs.
    ///
    /// **Affected Methods:** [#execute(Runnable)], [#submit(Callable)], [#submit(Runnable)],
    /// [#submit(Runnable, Object)], and `invokeAll` and `invokeAny`, which submit through them
    public static final Break SHUTDOWN_NOW_DOES_NOT_REJECT =
            new Break("shutdown now does not reject");

    /// **Purpose:** `isShutdown` reports a running executor as shut down.
    ///
    /// **Effect:** `isShutdown()` returns `true` while the executor runs a task and has not been shut down.
    ///
    /// **Affected Methods:** [#isShutdown()]
    public static final Break IS_SHUTDOWN_IS_TRUE_WHILE_TASKS_RUN =
            new Break("is shutdown is true while tasks run");

    /// **Purpose:** `isShutdown` misses a `shutdown`.
    ///
    /// **Effect:** `isShutdown()` returns `false` after `shutdown`, terminated or not, unless `shutdownNow` was
    /// called.
    ///
    /// **Affected Methods:** [#isShutdown()]
    public static final Break IS_SHUTDOWN_IS_FALSE_AFTER_SHUTDOWN =
            new Break("is shutdown is false after shutdown");

    /// **Purpose:** `isShutdown` misses a `shutdownNow`.
    ///
    /// **Effect:** `isShutdown()` returns `false` after `shutdownNow`.
    ///
    /// **Affected Methods:** [#isShutdown()]
    public static final Break IS_SHUTDOWN_IS_FALSE_AFTER_SHUTDOWN_NOW =
            new Break("is shutdown is false after shutdown now");

    /// **Purpose:** `isTerminated` reports an idle executor as terminated, where its Javadoc says it "is never true
    /// unless either shutdown or shutdownNow was called first".
    ///
    /// **Effect:** `isTerminated()` returns `true` while the executor has not been shut down and runs no task.
    ///
    /// **Affected Methods:** [#isTerminated()]
    public static final Break IS_TERMINATED_IS_TRUE_BEFORE_SHUTDOWN =
            new Break("is terminated is true before shutdown");

    /// **Purpose:** `isTerminated` reports termination before the tasks have completed.
    ///
    /// **Effect:** `isTerminated()` returns `true` as soon as the executor has been shut down.
    ///
    /// **Affected Methods:** [#isTerminated()]
    public static final Break IS_TERMINATED_IS_TRUE_WHILE_TASKS_RUN =
            new Break("is terminated is true while tasks run");

    /// **Purpose:** `isTerminated` misses termination.
    ///
    /// **Effect:** `isTerminated()` returns `false` once the executor has terminated.
    ///
    /// **Affected Methods:** [#isTerminated()]
    public static final Break IS_TERMINATED_IS_FALSE_ONCE_TERMINATED =
            new Break("is terminated is false once terminated");

    /// **Purpose:** `awaitTermination` does not block, where its Javadoc says it "blocks until all tasks have
    /// completed execution after a shutdown request, or the timeout occurs".
    ///
    /// **Effect:** `awaitTermination` returns at once whether the executor has terminated.
    ///
    /// **Affected Methods:** [#awaitTermination(long, TimeUnit)]
    public static final Break AWAIT_TERMINATION_DOES_NOT_WAIT =
            new Break("await termination does not wait");

    /// **Purpose:** `awaitTermination` reports termination when it timed out.
    ///
    /// **Effect:** `awaitTermination` returns `true` once its timeout elapses, terminated or not.
    ///
    /// **Affected Methods:** [#awaitTermination(long, TimeUnit)]
    public static final Break AWAIT_TERMINATION_RETURNS_TRUE_ON_TIMEOUT =
            new Break("await termination returns true on timeout");

    /// **Purpose:** `awaitTermination` is not interruptible, where its Javadoc says it throws
    /// `InterruptedException`.
    ///
    /// **Effect:** `awaitTermination` clears the interrupt status and waits on, until termination or the timeout.
    ///
    /// **Affected Methods:** [#awaitTermination(long, TimeUnit)]
    public static final Break AWAIT_TERMINATION_IGNORES_INTERRUPT =
            new Break("await termination ignores interrupt");

    /// **Purpose:** `awaitTermination` with no time to wait misses a termination that has happened.
    ///
    /// **Effect:** `awaitTermination` with a timeout of zero or less returns `false`, terminated or not.
    ///
    /// **Affected Methods:** [#awaitTermination(long, TimeUnit)]
    public static final Break AWAIT_TERMINATION_TIMES_OUT_ONCE_TERMINATED =
            new Break("await termination times out once terminated");

    /// **Purpose:** `close` does not wait, where its Javadoc says it "waits until all tasks have completed execution
    /// and the executor has terminated".
    ///
    /// **Effect:** `close` calls `shutdown` and returns.
    ///
    /// **Affected Methods:** [#close()]
    public static final Break CLOSE_DOES_NOT_WAIT =
            new Break("close does not wait");

    /// **Purpose:** `close` loses an interrupt, where its Javadoc says "the interrupt status will be re-asserted
    /// before this method returns".
    ///
    /// **Effect:** `close` clears the interrupt status before it returns.
    ///
    /// **Affected Methods:** [#close()]
    public static final Break CLOSE_CLEARS_THE_INTERRUPT =
            new Break("close clears the interrupt");

    /// **Purpose:** a second `close` has an effect, where its Javadoc says "if already terminated, invoking this
    /// method has no effect".
    ///
    /// **Effect:** `close` of a terminated executor throws [IllegalStateException].
    ///
    /// **Affected Methods:** [#close()]
    public static final Break SECOND_CLOSE_THROWS =
            new Break("second close throws");

    /// **Purpose:** `invokeAll` returns before the tasks are done, where its Javadoc says "`Future.isDone` is `true`
    /// for each element of the returned list".
    ///
    /// **Effect:** `invokeAll(tasks)` submits the tasks and returns their futures at once.
    ///
    /// **Affected Methods:** [#invokeAll(Collection)]
    public static final Break INVOKE_ALL_DOES_NOT_WAIT =
            new Break("invoke all does not wait");

    /// **Purpose:** `invokeAll` loses the order, where its Javadoc says the futures are "in the same sequential
    /// order as produced by the iterator for the given task list".
    ///
    /// **Effect:** `invokeAll(tasks)` returns the futures in reverse order.
    ///
    /// **Affected Methods:** [#invokeAll(Collection)]
    public static final Break INVOKE_ALL_REVERSES_THE_ORDER =
            new Break("invoke all reverses the order");

    /// **Purpose:** `invokeAll` refuses an empty collection, where its Javadoc sets no such limit.
    ///
    /// **Effect:** `invokeAll` of an empty collection throws [IllegalArgumentException].
    ///
    /// **Affected Methods:** [#invokeAll(Collection)]
    public static final Break INVOKE_ALL_REJECTS_NO_TASKS =
            new Break("invoke all rejects no tasks");

    /// **Purpose:** `invokeAll` takes a null task, where its Javadoc says it throws `NullPointerException`.
    ///
    /// **Effect:** `invokeAll(tasks)` skips null tasks, and takes a null collection as an empty one.
    ///
    /// **Affected Methods:** [#invokeAll(Collection)]
    public static final Break INVOKE_ALL_ACCEPTS_NULL =
            new Break("invoke all accepts null");

    /// **Purpose:** `invokeAll` is not interruptible, where its Javadoc says it throws `InterruptedException`.
    ///
    /// **Effect:** `invokeAll(tasks)` clears the interrupt status and waits on for the tasks.
    ///
    /// **Affected Methods:** [#invokeAll(Collection)]
    public static final Break INVOKE_ALL_IGNORES_INTERRUPT =
            new Break("invoke all ignores interrupt");

    /// **Purpose:** the timed `invokeAll` returns before the tasks are done, though they would finish within the
    /// timeout.
    ///
    /// **Effect:** `invokeAll(tasks, timeout, unit)` cancels every task not yet done at once, and returns.
    ///
    /// **Affected Methods:** [#invokeAll(Collection, long, TimeUnit)]
    public static final Break TIMED_INVOKE_ALL_DOES_NOT_WAIT =
            new Break("timed invoke all does not wait");

    /// **Purpose:** the timed `invokeAll` leaves unfinished tasks running, where its Javadoc says "upon return,
    /// tasks that have not completed are cancelled".
    ///
    /// **Effect:** `invokeAll(tasks, timeout, unit)` returns at the timeout without cancelling the tasks not done.
    ///
    /// **Affected Methods:** [#invokeAll(Collection, long, TimeUnit)]
    public static final Break TIMED_INVOKE_ALL_DOES_NOT_CANCEL =
            new Break("timed invoke all does not cancel");

    /// **Purpose:** `invokeAny` gives up on the first failure, where its Javadoc says it returns "the result of one
    /// that has completed successfully".
    ///
    /// **Effect:** `invokeAny(tasks)` waits for every task, and throws [ExecutionException] if any failed.
    ///
    /// **Affected Methods:** [#invokeAny(Collection)]
    public static final Break INVOKE_ANY_FAILS_IF_ANY_TASK_FAILS =
            new Break("invoke any fails if any task fails");

    /// **Purpose:** `invokeAny` hides that no task succeeded, where its Javadoc says it throws
    /// `ExecutionException`.
    ///
    /// **Effect:** `invokeAny(tasks)` returns `null` when every task fails.
    ///
    /// **Affected Methods:** [#invokeAny(Collection)]
    public static final Break INVOKE_ANY_RETURNS_NULL_WHEN_ALL_FAIL =
            new Break("invoke any returns null when all fail");

    /// **Purpose:** `invokeAny` takes no tasks, where its Javadoc says it throws `IllegalArgumentException`.
    ///
    /// **Effect:** `invokeAny` of an empty collection returns `null`.
    ///
    /// **Affected Methods:** [#invokeAny(Collection)]
    public static final Break INVOKE_ANY_ACCEPTS_EMPTY =
            new Break("invoke any accepts empty");

    /// **Purpose:** `invokeAny` takes a null task, where its Javadoc says it throws `NullPointerException`.
    ///
    /// **Effect:** `invokeAny(tasks)` skips null tasks, and takes a null collection as an empty one, which then
    /// throws [IllegalArgumentException] as an empty one does.
    ///
    /// **Affected Methods:** [#invokeAny(Collection)]
    public static final Break INVOKE_ANY_ACCEPTS_NULL =
            new Break("invoke any accepts null");

    /// **Purpose:** `invokeAny` is not interruptible, where its Javadoc says it throws `InterruptedException`.
    ///
    /// **Effect:** `invokeAny(tasks)` clears the interrupt status and waits on for a task.
    ///
    /// **Affected Methods:** [#invokeAny(Collection)]
    public static final Break INVOKE_ANY_IGNORES_INTERRUPT =
            new Break("invoke any ignores interrupt");

    /// **Purpose:** the timed `invokeAny` gives up before its timeout.
    ///
    /// **Effect:** `invokeAny(tasks, timeout, unit)` throws [TimeoutException] at once unless a task has already
    /// succeeded.
    ///
    /// **Affected Methods:** [#invokeAny(Collection, long, TimeUnit)]
    public static final Break TIMED_INVOKE_ANY_DOES_NOT_WAIT =
            new Break("timed invoke any does not wait");

    /// **Purpose:** the timed `invokeAny` does not time out, where its Javadoc says it throws `TimeoutException`.
    ///
    /// **Effect:** `invokeAny(tasks, timeout, unit)` returns `null` once the timeout elapses with no task succeeded.
    ///
    /// **Affected Methods:** [#invokeAny(Collection, long, TimeUnit)]
    public static final Break TIMED_INVOKE_ANY_DOES_NOT_TIME_OUT =
            new Break("timed invoke any does not time out");

    @Serial
    private static final long serialVersionUID = 1L;

    /// The run states, in order.
    private enum RunState { RUNNING, SHUTDOWN, STOPPED, TERMINATED }

    /// Guards the queue, the state and the workers.
    private final ReentrantLock lock = new ReentrantLock();

    /// Signalled when a task is queued or the state changes.
    private final Condition work = lock.newCondition();

    /// Signalled once terminated.
    private final Condition terminated = lock.newCondition();

    /// The most worker threads it runs.
    private final int threads;

    /// The queue: the tasks not yet taken, by sequence number; the workers take the lowest.
    private final TreeMap<Long, Runnable> queue = new TreeMap<>();

    /// The worker threads alive.
    private final transient Set<Thread> workers = new HashSet<>();

    /// The worker threads running a task.
    private final transient Set<Thread> busy = new HashSet<>();

    /// The next sequence number; claimed outside the lock by [#SUBMIT_IS_NOT_THREAD_SAFE].
    private long sequence;

    /// The run state; read outside the lock by [#SHUTDOWN_IS_NOT_THREAD_SAFE] and [#close()].
    private volatile RunState state = RunState.RUNNING;

    /// Whether `shutdownNow` has been called, for [#IS_SHUTDOWN_IS_FALSE_AFTER_SHUTDOWN_NOW] and
    /// [#SHUTDOWN_NOW_DOES_NOT_REJECT].
    private boolean stoppedOnce;

    /// Creates a running `BreakableExecutorService` of at most `threads` workers, with the given breaks.
    ///
    /// @param threadsArg the most worker threads it runs
    /// @param breaks  the set of breaks to apply, copied
    /// @throws IllegalArgumentException if `threads` is not positive
    /// @throws NullPointerException     if `breaks` is null
    public BreakableExecutorService(final int threadsArg, final Set<Break> breaks) {
        super(new HashSet<>(Objects.requireNonNull(breaks, "breaks")), Map.of(), false);
        if (threadsArg <= 0) {
            throw new IllegalArgumentException("threads must be positive: " + threadsArg);
        }
        this.threads = threadsArg;
    }

    /// Runs the command on a worker, at some time in the future.
    ///
    /// # Breaks
    /// - [#EXECUTE_DOES_NOT_RUN_THE_TASK]
    /// - [#EXECUTE_RUNS_THE_TASK_TWICE]
    /// - [#EXECUTE_ACCEPTS_NULL]
    /// - [#SUBMIT_IS_NOT_THREAD_SAFE], [#SHUTDOWN_DOES_NOT_REJECT], [#SHUTDOWN_IS_NOT_THREAD_SAFE],
    ///   [#SHUTDOWN_NOW_DOES_NOT_REJECT]
    ///
    /// @param command the command
    /// @throws NullPointerException       if `command` is null
    /// @throws RejectedExecutionException if the executor has been shut down
    @Override
    public void execute(final Runnable command) {
        if (command == null && hasBreak(EXECUTE_ACCEPTS_NULL)) {
            return;
        }
        Objects.requireNonNull(command, "command");
        if (hasBreak(EXECUTE_DOES_NOT_RUN_THE_TASK)) {
            return;
        }
        enqueue(command);
        if (hasBreak(EXECUTE_RUNS_THE_TASK_TWICE)) {
            enqueue(command);
        }
    }

    /// Submits a callable, and returns a future of its result.
    ///
    /// # Breaks
    /// - [#SUBMIT_CALLABLE_RETURNS_NULL]
    /// - [#SUBMIT_CALLABLE_SWALLOWS_THE_EXCEPTION]
    /// - [#SUBMIT_CALLABLE_ACCEPTS_NULL]
    /// - [#SUBMIT_IS_NOT_THREAD_SAFE], [#SHUTDOWN_DOES_NOT_REJECT], [#SHUTDOWN_IS_NOT_THREAD_SAFE],
    ///   [#SHUTDOWN_NOW_DOES_NOT_REJECT]
    ///
    /// @param task the callable
    /// @param <T>  the type of its result
    /// @return the future
    /// @throws NullPointerException       if `task` is null
    /// @throws RejectedExecutionException if the executor has been shut down
    @Override
    // NullAway: SUBMIT_CALLABLE_RETURNS_NULL and SUBMIT_CALLABLE_SWALLOWS_THE_EXCEPTION return null for T, on purpose.
    @SuppressWarnings("NullAway")
    public <T> Future<T> submit(final Callable<T> task) {
        if (task == null && hasBreak(SUBMIT_CALLABLE_ACCEPTS_NULL)) {
            return nullFuture();
        }
        Objects.requireNonNull(task, "task");
        Callable<T> callable = task;
        if (hasBreak(SUBMIT_CALLABLE_RETURNS_NULL)) {
            callable = () -> {
                task.call();
                return null;
            };
        } else if (hasBreak(SUBMIT_CALLABLE_SWALLOWS_THE_EXCEPTION)) {
            callable = () -> {
                try {
                    return task.call();
                } catch (final Exception e) {
                    return null;
                }
            };
        }
        FutureTask<T> future = new FutureTask<>(callable);
        enqueue(future);
        return future;
    }

    /// Submits a runnable, and returns a future that succeeds with `null` once it has run.
    ///
    /// # Breaks
    /// - [#SUBMIT_RUNNABLE_DOES_NOT_RUN_THE_TASK]
    /// - [#SUBMIT_RUNNABLE_ACCEPTS_NULL]
    /// - [#SUBMIT_IS_NOT_THREAD_SAFE], [#SHUTDOWN_DOES_NOT_REJECT], [#SHUTDOWN_IS_NOT_THREAD_SAFE],
    ///   [#SHUTDOWN_NOW_DOES_NOT_REJECT]
    ///
    /// @param task the runnable
    /// @return the future
    /// @throws NullPointerException       if `task` is null
    /// @throws RejectedExecutionException if the executor has been shut down
    @Override
    public Future<?> submit(final Runnable task) {
        if (task == null && hasBreak(SUBMIT_RUNNABLE_ACCEPTS_NULL)) {
            return nullFuture();
        }
        Objects.requireNonNull(task, "task");
        Runnable runnable = hasBreak(SUBMIT_RUNNABLE_DOES_NOT_RUN_THE_TASK) ? () -> { } : task;
        FutureTask<@Nullable Object> future = new FutureTask<>(runnable, null);
        enqueue(future);
        return future;
    }

    /// Submits a runnable, and returns a future that succeeds with the given result once it has run.
    ///
    /// # Breaks
    /// - [#SUBMIT_RUNNABLE_WITH_RESULT_RETURNS_NULL]
    /// - [#SUBMIT_RUNNABLE_WITH_RESULT_ACCEPTS_NULL]
    /// - [#SUBMIT_IS_NOT_THREAD_SAFE], [#SHUTDOWN_DOES_NOT_REJECT], [#SHUTDOWN_IS_NOT_THREAD_SAFE],
    ///   [#SHUTDOWN_NOW_DOES_NOT_REJECT]
    ///
    /// @param task   the runnable
    /// @param result the result
    /// @param <T>    the type of the result
    /// @return the future
    /// @throws NullPointerException       if `task` is null
    /// @throws RejectedExecutionException if the executor has been shut down
    @Override
    // NullAway: SUBMIT_RUNNABLE_WITH_RESULT_RETURNS_NULL gives null for T, on purpose.
    @SuppressWarnings("NullAway")
    public <T> Future<T> submit(final Runnable task, final T result) {
        if (task == null && hasBreak(SUBMIT_RUNNABLE_WITH_RESULT_ACCEPTS_NULL)) {
            return CompletableFuture.completedFuture(result);
        }
        Objects.requireNonNull(task, "task");
        FutureTask<T> future = new FutureTask<>(task,
                hasBreak(SUBMIT_RUNNABLE_WITH_RESULT_RETURNS_NULL) ? null : result);
        enqueue(future);
        return future;
    }

    /// Shuts it down: the queued tasks still run, and new ones are rejected. It does not wait.
    ///
    /// # Breaks
    /// - [#SHUTDOWN_DROPS_QUEUED_TASKS]
    /// - [#SHUTDOWN_WAITS_FOR_TASKS]
    /// - [#SECOND_SHUTDOWN_RESTARTS]
    @Override
    public void shutdown() {
        lock.lock();
        try {
            if (state != RunState.RUNNING && hasBreak(SECOND_SHUTDOWN_RESTARTS)) {
                state = RunState.RUNNING;
                return;
            }
            if (state == RunState.RUNNING) {
                state = RunState.SHUTDOWN;
            }
            if (hasBreak(SHUTDOWN_DROPS_QUEUED_TASKS)) {
                queue.clear();
            }
            work.signalAll();
            tryTerminate();
        } finally {
            lock.unlock();
        }
        if (hasBreak(SHUTDOWN_WAITS_FOR_TASKS)) {
            awaitTerminated();
        }
    }

    /// Shuts it down at once: takes the queued tasks off the queue and returns them, rejects new ones, and
    /// interrupts the workers.
    ///
    /// # Breaks
    /// - [#SHUTDOWN_NOW_RUNS_THE_TASKS_IT_RETURNS]
    /// - [#SHUTDOWN_NOW_DISCARDS_WAITING_TASKS]
    ///
    /// @return the tasks that were queued, in order
    @Override
    public List<Runnable> shutdownNow() {
        lock.lock();
        try {
            List<Runnable> queued = new ArrayList<>(queue.values());
            if (hasBreak(SHUTDOWN_NOW_RUNS_THE_TASKS_IT_RETURNS)) {
                if (state == RunState.RUNNING) {
                    state = RunState.SHUTDOWN;
                }
            } else {
                queue.clear();
                stoppedOnce = true;
                if (state != RunState.TERMINATED) {
                    state = RunState.STOPPED;
                }
            }
            for (final Thread worker : busy) {
                worker.interrupt();
            }
            work.signalAll();
            tryTerminate();
            return hasBreak(SHUTDOWN_NOW_DISCARDS_WAITING_TASKS) ? new ArrayList<>() : queued;
        } finally {
            lock.unlock();
        }
    }

    /// Returns whether it has been shut down.
    ///
    /// # Breaks
    /// - [#IS_SHUTDOWN_IS_TRUE_WHILE_TASKS_RUN]
    /// - [#IS_SHUTDOWN_IS_FALSE_AFTER_SHUTDOWN]
    /// - [#IS_SHUTDOWN_IS_FALSE_AFTER_SHUTDOWN_NOW]
    ///
    /// @return `true` once `shutdown` or `shutdownNow` has been called
    @Override
    public boolean isShutdown() {
        lock.lock();
        try {
            if (state == RunState.RUNNING) {
                return hasBreak(IS_SHUTDOWN_IS_TRUE_WHILE_TASKS_RUN) && !busy.isEmpty();
            }
            return !(stoppedOnce ? hasBreak(IS_SHUTDOWN_IS_FALSE_AFTER_SHUTDOWN_NOW)
                    : hasBreak(IS_SHUTDOWN_IS_FALSE_AFTER_SHUTDOWN));
        } finally {
            lock.unlock();
        }
    }

    /// Returns whether it has terminated: shut down, with every task done and every worker ended.
    ///
    /// # Breaks
    /// - [#IS_TERMINATED_IS_TRUE_BEFORE_SHUTDOWN]
    /// - [#IS_TERMINATED_IS_TRUE_WHILE_TASKS_RUN]
    /// - [#IS_TERMINATED_IS_FALSE_ONCE_TERMINATED]
    ///
    /// @return `true` once terminated
    @Override
    public boolean isTerminated() {
        lock.lock();
        try {
            return switch (state) {
                case RUNNING -> hasBreak(IS_TERMINATED_IS_TRUE_BEFORE_SHUTDOWN) && busy.isEmpty() && queue.isEmpty();
                case SHUTDOWN, STOPPED -> hasBreak(IS_TERMINATED_IS_TRUE_WHILE_TASKS_RUN);
                case TERMINATED -> !hasBreak(IS_TERMINATED_IS_FALSE_ONCE_TERMINATED);
            };
        } finally {
            lock.unlock();
        }
    }

    /// Waits until it has terminated, the timeout elapses, or the thread is interrupted.
    ///
    /// # Breaks
    /// - [#AWAIT_TERMINATION_DOES_NOT_WAIT]
    /// - [#AWAIT_TERMINATION_RETURNS_TRUE_ON_TIMEOUT]
    /// - [#AWAIT_TERMINATION_IGNORES_INTERRUPT]
    /// - [#AWAIT_TERMINATION_TIMES_OUT_ONCE_TERMINATED]
    ///
    /// @param timeout the longest wait
    /// @param unit    its unit
    /// @return `true` if it terminated, `false` if the timeout elapsed first
    /// @throws InterruptedException if interrupted while waiting
    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        if (nanos <= 0 && hasBreak(AWAIT_TERMINATION_TIMES_OUT_ONCE_TERMINATED)) {
            return false;
        }
        boolean ignoreInterrupt = hasBreak(AWAIT_TERMINATION_IGNORES_INTERRUPT);
        if (ignoreInterrupt) {
            boolean ignored = Thread.interrupted();
        }
        lock.lock();
        try {
            if (hasBreak(AWAIT_TERMINATION_DOES_NOT_WAIT)) {
                return state == RunState.TERMINATED;
            }
            while (state != RunState.TERMINATED) {
                if (nanos <= 0) {
                    return hasBreak(AWAIT_TERMINATION_RETURNS_TRUE_ON_TIMEOUT);
                }
                nanos = ignoreInterrupt ? awaitUninterruptibly(nanos) : terminated.awaitNanos(nanos);
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    /// Shuts it down and waits until it has terminated, as [ExecutorService#close()] does: if interrupted while
    /// waiting, it calls `shutdownNow`, waits on, and sets the interrupt status again before it returns.
    ///
    /// # Breaks
    /// - [#CLOSE_DOES_NOT_WAIT]
    /// - [#CLOSE_CLEARS_THE_INTERRUPT]
    /// - [#SECOND_CLOSE_THROWS]
    @Override
    public void close() {
        if (hasBreak(SECOND_CLOSE_THROWS) && state == RunState.TERMINATED) {
            throw new IllegalStateException("already closed");
        }
        if (hasBreak(CLOSE_DOES_NOT_WAIT)) {
            shutdown();
            return;
        }
        ExecutorService.super.close();
        if (hasBreak(CLOSE_CLEARS_THE_INTERRUPT)) {
            boolean ignored = Thread.interrupted();
        }
    }

    /// Runs the tasks, and returns their futures, in order, once every one is done. If interrupted while waiting, it
    /// cancels the tasks not done and throws.
    ///
    /// # Breaks
    /// - [#INVOKE_ALL_DOES_NOT_WAIT]
    /// - [#INVOKE_ALL_REVERSES_THE_ORDER]
    /// - [#INVOKE_ALL_REJECTS_NO_TASKS]
    /// - [#INVOKE_ALL_ACCEPTS_NULL]
    /// - [#INVOKE_ALL_IGNORES_INTERRUPT]
    ///
    /// @param tasks the tasks
    /// @param <T>   the type of their results
    /// @return their futures, each done
    /// @throws InterruptedException       if interrupted while waiting
    /// @throws NullPointerException       if `tasks` or a task is null
    /// @throws RejectedExecutionException if the executor has been shut down
    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
        if (tasks == null && hasBreak(INVOKE_ALL_ACCEPTS_NULL)) {
            return new ArrayList<>();
        }
        if (hasBreak(INVOKE_ALL_REJECTS_NO_TASKS) && tasks != null && tasks.isEmpty()) {
            throw new IllegalArgumentException("no tasks");
        }
        List<Future<T>> futures = submitAll(tasks, hasBreak(INVOKE_ALL_ACCEPTS_NULL));
        if (hasBreak(INVOKE_ALL_REVERSES_THE_ORDER)) {
            Collections.reverse(futures);
        }
        if (hasBreak(INVOKE_ALL_DOES_NOT_WAIT)) {
            return futures;
        }
        boolean interrupted = false;
        try {
            for (final Future<T> future : futures) {
                while (!future.isDone()) {
                    try {
                        future.get();
                    } catch (final ExecutionException | CancellationException e) {
                        // The future holds its outcome.
                    } catch (final InterruptedException e) {
                        if (!hasBreak(INVOKE_ALL_IGNORES_INTERRUPT)) {
                            throw e;
                        }
                        interrupted = true;
                    }
                }
            }
            return futures;
        } catch (final InterruptedException e) {
            cancelAll(futures);
            throw e;
        } finally {
            if (interrupted) {
                boolean ignored = Thread.interrupted();
            }
        }
    }

    /// Runs the tasks, and returns their futures, in order, once every one is done or the timeout elapses; on return
    /// it cancels the tasks not done.
    ///
    /// # Breaks
    /// - [#TIMED_INVOKE_ALL_DOES_NOT_WAIT]
    /// - [#TIMED_INVOKE_ALL_DOES_NOT_CANCEL]
    ///
    /// @param tasks   the tasks
    /// @param timeout the longest wait
    /// @param unit    its unit
    /// @param <T>     the type of their results
    /// @return their futures, each done
    /// @throws InterruptedException       if interrupted while waiting
    /// @throws NullPointerException       if `tasks`, a task or `unit` is null
    /// @throws RejectedExecutionException if the executor has been shut down
    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks, final long timeout,
            final TimeUnit unit) throws InterruptedException {
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        List<Future<T>> futures = submitAll(tasks, false);
        if (hasBreak(TIMED_INVOKE_ALL_DOES_NOT_WAIT)) {
            cancelAll(futures);
            return futures;
        }
        try {
            for (final Future<T> future : futures) {
                if (!future.isDone()) {
                    try {
                        future.get(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
                    } catch (final ExecutionException | CancellationException e) {
                        // The future holds its outcome.
                    } catch (final TimeoutException e) {
                        break;
                    }
                }
            }
        } catch (final InterruptedException e) {
            cancelAll(futures);
            throw e;
        }
        if (!hasBreak(TIMED_INVOKE_ALL_DOES_NOT_CANCEL)) {
            cancelAll(futures);
        }
        return futures;
    }

    /// Runs the tasks, and returns the result of one that succeeded; on return it cancels the tasks not done.
    ///
    /// # Breaks
    /// - [#INVOKE_ANY_FAILS_IF_ANY_TASK_FAILS]
    /// - [#INVOKE_ANY_RETURNS_NULL_WHEN_ALL_FAIL]
    /// - [#INVOKE_ANY_ACCEPTS_EMPTY]
    /// - [#INVOKE_ANY_ACCEPTS_NULL]
    /// - [#INVOKE_ANY_IGNORES_INTERRUPT]
    ///
    /// @param tasks the tasks
    /// @param <T>   the type of their results
    /// @return the result of a task that succeeded
    /// @throws InterruptedException       if interrupted while waiting
    /// @throws ExecutionException         if no task succeeded
    /// @throws IllegalArgumentException   if `tasks` is empty
    /// @throws NullPointerException       if `tasks` or a task is null
    /// @throws RejectedExecutionException if the executor has been shut down
    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        try {
            return any(tasks, false, 0L);
        } catch (final TimeoutException e) {
            throw new AssertionError("an untimed invokeAny timed out", e);
        }
    }

    /// Runs the tasks, and returns the result of one that succeeded before the timeout elapsed; on return it cancels
    /// the tasks not done.
    ///
    /// # Breaks
    /// - [#TIMED_INVOKE_ANY_DOES_NOT_WAIT]
    /// - [#TIMED_INVOKE_ANY_DOES_NOT_TIME_OUT]
    ///
    /// @param tasks   the tasks
    /// @param timeout the longest wait
    /// @param unit    its unit
    /// @param <T>     the type of their results
    /// @return the result of a task that succeeded
    /// @throws InterruptedException       if interrupted while waiting
    /// @throws ExecutionException         if no task succeeded
    /// @throws TimeoutException           if the timeout elapsed before a task succeeded
    /// @throws IllegalArgumentException   if `tasks` is empty
    /// @throws NullPointerException       if `tasks`, a task or `unit` is null
    /// @throws RejectedExecutionException if the executor has been shut down
    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return any(tasks, true, unit.toNanos(timeout));
    }

    @Override
    public String toString() {
        lock.lock();
        try {
            return "BreakableExecutorService[" + state + ", " + workers.size() + " workers, "
                    + queue.size() + " queued]";
        } finally {
            lock.unlock();
        }
    }

    /// Queues a task, or rejects it once shut down, and starts a worker if there are fewer than [#threads].
    // ThreadPriorityCheck: the thread-unsafe breaks yield to let another submit or a shutdown in, on purpose.
    @SuppressWarnings("ThreadPriorityCheck")
    private void enqueue(final Runnable task) {
        boolean unsafeState = hasBreak(SHUTDOWN_IS_NOT_THREAD_SAFE);
        if (unsafeState) {
            checkAccepting(state);
            Thread.yield();
        }
        long place = -1L;
        if (hasBreak(SUBMIT_IS_NOT_THREAD_SAFE)) {
            place = sequence;
            Thread.yield();
            sequence = place + 1;
        }
        lock.lock();
        try {
            if (!unsafeState) {
                checkAccepting(state);
            }
            if (place < 0) {
                place = sequence++;
            }
            queue.put(place, task);
            if (workers.size() < threads && (state == RunState.RUNNING || (!unsafeState && !workers.isEmpty()))) {
                startWorker();
            }
            work.signal();
        } finally {
            lock.unlock();
        }
    }

    /// Throws [RejectedExecutionException] once shut down, unless a break says not to.
    private void checkAccepting(final RunState now) {
        if (now == RunState.RUNNING) {
            return;
        }
        if (stoppedOnce ? hasBreak(SHUTDOWN_NOW_DOES_NOT_REJECT) : hasBreak(SHUTDOWN_DOES_NOT_REJECT)) {
            return;
        }
        throw new RejectedExecutionException("the executor has been shut down");
    }

    /// Starts a worker thread. The lock is held.
    private void startWorker() {
        Thread worker = new Thread(this::work, "breakable-executor-worker");
        worker.setDaemon(true);
        workers.add(worker);
        worker.start();
    }

    /// A worker's loop: takes tasks in order until the executor is stopped, or shut down with the queue empty.
    private void work() {
        Thread self = Thread.currentThread();
        try {
            while (true) {
                Runnable task;
                lock.lock();
                try {
                    task = take();
                    if (task == null) {
                        return;
                    }
                    busy.add(self);
                    if (state != RunState.STOPPED) {
                        boolean ignored = Thread.interrupted();
                    }
                } finally {
                    lock.unlock();
                }
                try {
                    task.run();
                } catch (final RuntimeException | Error e) {
                    // An executed command's exception ends the command, not the worker.
                } finally {
                    lock.lock();
                    try {
                        busy.remove(self);
                    } finally {
                        lock.unlock();
                    }
                }
            }
        } finally {
            lock.lock();
            try {
                workers.remove(self);
                tryTerminate();
            } finally {
                lock.unlock();
            }
        }
    }

    /// Takes the next task, waiting while the queue is empty and the executor runs; returns `null` once the worker
    /// is to end. The lock is held.
    private @Nullable Runnable take() {
        while (true) {
            if (state == RunState.STOPPED || state == RunState.TERMINATED) {
                return null;
            }
            Map.Entry<Long, Runnable> first = queue.pollFirstEntry();
            if (first != null) {
                return first.getValue();
            }
            if (state != RunState.RUNNING) {
                return null;
            }
            work.awaitUninterruptibly();
        }
    }

    /// Terminates, if shut down with no worker left and nothing queued. The lock is held.
    private void tryTerminate() {
        if (((state == RunState.SHUTDOWN && queue.isEmpty()) || state == RunState.STOPPED) && workers.isEmpty()) {
            state = RunState.TERMINATED;
            terminated.signalAll();
        }
    }

    /// Waits, uninterruptibly, until terminated: for [#SHUTDOWN_WAITS_FOR_TASKS].
    private void awaitTerminated() {
        lock.lock();
        try {
            while (state != RunState.TERMINATED) {
                terminated.awaitUninterruptibly();
            }
        } finally {
            lock.unlock();
        }
    }

    /// Waits on [#terminated] for at most `nanos`, uninterruptibly, and returns the time left. The lock is held.
    private long awaitUninterruptibly(final long nanos) {
        long deadline = System.nanoTime() + nanos;
        while (true) {
            try {
                return terminated.awaitNanos(deadline - System.nanoTime());
            } catch (final InterruptedException e) {
                // AWAIT_TERMINATION_IGNORES_INTERRUPT: wait on.
            }
        }
    }

    private <T> List<Future<T>> submitAll(final Collection<? extends Callable<T>> tasks, final boolean skipNulls) {
        Objects.requireNonNull(tasks, "tasks");
        List<Callable<T>> all = new ArrayList<>(tasks.size());
        for (final Callable<T> task : tasks) {
            if (task == null) {
                if (skipNulls) {
                    continue;
                }
                throw new NullPointerException("a task is null");
            }
            all.add(task);
        }
        List<Future<T>> futures = new ArrayList<>(all.size());
        try {
            for (final Callable<T> task : all) {
                futures.add(submit(task));
            }
        } catch (final RuntimeException e) {
            cancelAll(futures);
            throw e;
        }
        return futures;
    }

    private static void cancelAll(final List<? extends Future<?>> futures) {
        for (final Future<?> future : futures) {
            future.cancel(true);
        }
    }

    /// `invokeAny`, timed or not, over an [ExecutorCompletionService] on this executor.
    // NullAway: the invokeAny breaks return null for T, on purpose.
    @SuppressWarnings("NullAway")
    private <T> T any(final Collection<? extends Callable<T>> tasks, final boolean timed, final long nanos)
            throws InterruptedException, ExecutionException, TimeoutException {
        boolean skipNulls = !timed && hasBreak(INVOKE_ANY_ACCEPTS_NULL);
        Collection<? extends Callable<T>> given = tasks == null && skipNulls ? List.of() : tasks;
        Objects.requireNonNull(given, "tasks");
        List<Callable<T>> all = new ArrayList<>(given.size());
        for (final Callable<T> task : given) {
            if (task == null) {
                if (skipNulls) {
                    continue;
                }
                throw new NullPointerException("a task is null");
            }
            all.add(task);
        }
        if (all.isEmpty()) {
            if (!timed && hasBreak(INVOKE_ANY_ACCEPTS_EMPTY)) {
                return null;
            }
            throw new IllegalArgumentException("no tasks");
        }
        ExecutorCompletionService<T> completion = new ExecutorCompletionService<>(this);
        List<Future<T>> futures = new ArrayList<>(all.size());
        long deadline = System.nanoTime() + nanos;
        try {
            for (final Callable<T> task : all) {
                futures.add(completion.submit(task));
            }
            boolean waitForAll = !timed && hasBreak(INVOKE_ANY_FAILS_IF_ANY_TASK_FAILS);
            @Nullable ExecutionException failure = null;
            @Nullable Future<T> succeeded = null;
            for (int done = 0; done < futures.size(); done++) {
                Future<T> next;
                if (timed) {
                    if (hasBreak(TIMED_INVOKE_ANY_DOES_NOT_WAIT)) {
                        next = completion.poll();
                    } else {
                        next = completion.poll(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
                    }
                    if (next == null) {
                        if (hasBreak(TIMED_INVOKE_ANY_DOES_NOT_TIME_OUT)) {
                            return null;
                        }
                        throw new TimeoutException("no task succeeded within the timeout");
                    }
                } else {
                    next = take(completion);
                }
                try {
                    T value = next.get();
                    if (!waitForAll) {
                        return value;
                    }
                    succeeded = next;
                } catch (final ExecutionException e) {
                    failure = e;
                } catch (final CancellationException e) {
                    failure = new ExecutionException(e);
                }
            }
            if (waitForAll && failure != null) {
                throw failure;
            }
            if (succeeded != null) {
                return succeeded.get();
            }
            if (!timed && hasBreak(INVOKE_ANY_RETURNS_NULL_WHEN_ALL_FAIL)) {
                return null;
            }
            throw failure != null ? failure : new ExecutionException("no task succeeded", null);
        } finally {
            cancelAll(futures);
        }
    }

    /// Takes the next completed future, ignoring interrupts with [#INVOKE_ANY_IGNORES_INTERRUPT].
    private <T> Future<T> take(final ExecutorCompletionService<T> completion) throws InterruptedException {
        if (!hasBreak(INVOKE_ANY_IGNORES_INTERRUPT)) {
            return completion.take();
        }
        boolean interrupted = Thread.interrupted();
        try {
            while (true) {
                try {
                    return completion.take();
                } catch (final InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                boolean ignored = Thread.interrupted();
            }
        }
    }

    /// A future already succeeded with `null`, for the breaks that accept a null task.
    // NullAway: the ACCEPTS_NULL breaks give a future of null for T, on purpose.
    @SuppressWarnings("NullAway")
    private static <T> Future<T> nullFuture() {
        return CompletableFuture.completedFuture(null);
    }

    /// Refuses serialization: the worker threads cannot be serialized.
    ///
    /// @param out the stream
    /// @throws IOException always, a [NotSerializableException]
    @Serial
    private void writeObject(final ObjectOutputStream out) throws IOException {
        throw new NotSerializableException(BreakableExecutorService.class.getName());
    }
}
