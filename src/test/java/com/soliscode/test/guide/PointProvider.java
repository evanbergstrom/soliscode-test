/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.guide;

import com.soliscode.test.provider.ObjectProvider;

/// How to supply instances and elements: the provider of [Point]s, each seed its own point.
final class PointProvider implements ObjectProvider<Point> {

    @Override
    public Point defaultInstance() {
        return new Point(0, 0);
    }

    @Override
    public Point createInstance(final long seed) {
        return new Point((int) (seed % 1_000), (int) (seed / 1_000));
    }

    @Override
    public Point copyInstance(final Point other) {
        return new Point(other.x(), other.y());
    }

    @Override
    public long uniqueSizeLimit() {
        return 1_000_000;
    }
}
