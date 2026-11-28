/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.provider;

import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;

/// Factories of [ReadWriteLockProvider]s: of the read-write locks a supplier makes, of the JDK's reentrant
/// read-write locks, and of the read-write view of its stamped locks.
///
/// @since 0.1.7
public final class ReadWriteLockProviders {

    private ReadWriteLockProviders() { }

    /// A provider of the read-write locks `make` makes.
    ///
    /// @param make makes a new read-write lock, held by no thread, each call
    /// @param <L>  the type of the lock
    /// @return the provider
    /// @throws NullPointerException if `make` is null
    public static <L extends ReadWriteLock> ReadWriteLockProvider<L> of(final Supplier<? extends L> make) {
        Objects.requireNonNull(make, "make");
        return make::get;
    }

    /// A provider of new non-fair reentrant read-write locks, made by
    /// [ReentrantReadWriteLock#ReentrantReadWriteLock()].
    ///
    /// @return the provider
    public static ReadWriteLockProvider<ReentrantReadWriteLock> reentrantReadWriteLocks() {
        return ReentrantReadWriteLock::new;
    }

    /// A provider of new fair reentrant read-write locks, made by
    /// [ReentrantReadWriteLock#ReentrantReadWriteLock(boolean)] with `true`.
    ///
    /// @return the provider
    public static ReadWriteLockProvider<ReentrantReadWriteLock> fairReentrantReadWriteLocks() {
        return () -> new ReentrantReadWriteLock(true);
    }

    /// A provider of the read-write view of new stamped locks, made by [StampedLock#asReadWriteLock()].
    ///
    /// @return the provider
    public static ReadWriteLockProvider<ReadWriteLock> stampedReadWriteLocks() {
        return () -> new StampedLock().asReadWriteLock();
    }
}
