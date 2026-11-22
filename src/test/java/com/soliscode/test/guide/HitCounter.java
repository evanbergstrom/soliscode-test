/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.guide;

import java.util.concurrent.atomic.AtomicLong;

/// How to write a thread-safety test: a class of the reader's own that says it is thread-safe.
final class HitCounter {

    private final AtomicLong hits = new AtomicLong();

    /// Counts one hit.
    void hit() {
        hits.incrementAndGet();
    }

    /// Returns the hits counted so far.
    ///
    /// @return the hits
    long hits() {
        return hits.get();
    }
}
