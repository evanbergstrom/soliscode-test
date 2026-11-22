/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.guide;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.contract.list.ListContract;
import com.soliscode.test.provider.CollectionProvider;
import com.soliscode.test.provider.CollectionProviders;
import com.soliscode.test.provider.ObjectProvider;

/// How to supply instances and elements: [ListContract] run against a [ForwardingList] of [Point]s, from
/// [PointProvider].
class PointListTest extends AbstractTest implements ListContract<ForwardingList<Point>, Point> {

    @Override
    public ObjectProvider<Point> elementProvider() {
        return new PointProvider();
    }

    @Override
    public CollectionProvider<ForwardingList<Point>, Point> provider() {
        return CollectionProviders.from(ForwardingList::new, ForwardingList::new, ForwardingList::new,
                elementProvider());
    }
}
