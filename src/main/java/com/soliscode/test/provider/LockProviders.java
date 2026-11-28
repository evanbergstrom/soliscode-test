/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.provider;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;

/// Factories of [LockProvider]s: of the locks a supplier makes, of the JDK's reentrant locks, and of the write locks
/// of read-write locks, which are locks [com.soliscode.test.contract.lock.LockContract] can test.
///
/// @since 0.1.7
public final class LockProviders {

    private LockProviders() { }

    /// A provider of the locks `make` makes.
    ///
    /// @param make makes a new lock, held by no thread, each call
    /// @param <L>  the type of the lock
    /// @return the provider
    /// @throws NullPointerException if `make` is null
    public static <L extends Lock> LockProvider<L> of(final Supplier<? extends L> make) {
        Objects.requireNonNull(make, "make");
        return make::get;
    }

    /// A provider of new non-fair reentrant locks, made by [ReentrantLock#ReentrantLock()].
    ///
    /// @return the provider
    public static LockProvider<ReentrantLock> reentrantLocks() {
        return ReentrantLock::new;
    }

    /// A provider of new fair reentrant locks, made by [ReentrantLock#ReentrantLock(boolean)] with `true`.
    ///
    /// @return the provider
    public static LockProvider<ReentrantLock> fairReentrantLocks() {
        return () -> new ReentrantLock(true);
    }

    /// A provider of the write lock of each new read-write lock `make` makes.
    ///
    /// @param make makes a new read-write lock, held by no thread, each call
    /// @return the provider
    /// @throws NullPointerException if `make` is null
    public static LockProvider<Lock> writeLocksOf(final Supplier<? extends ReadWriteLock> make) {
        Objects.requireNonNull(make, "make");
        return () -> make.get().writeLock();
    }

    /// A provider of the write-lock view of new stamped locks, made by [StampedLock#asWriteLock()]. Its
    /// `newCondition()` throws [UnsupportedOperationException], so a contract run on it overrides
    /// [com.soliscode.test.contract.support.LockContractSupport#supportsConditions()] to return `false`.
    ///
    /// @return the provider
    public static LockProvider<Lock> stampedWriteLocks() {
        return () -> new StampedLock().asWriteLock();
    }
}
