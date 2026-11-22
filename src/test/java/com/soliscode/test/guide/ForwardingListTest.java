/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.guide;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.contract.list.ListContract;
import com.soliscode.test.contract.support.WithIntegerElement;
import com.soliscode.test.provider.CollectionProvider;
import com.soliscode.test.provider.CollectionProviders;

/// The first steps: [ListContract] run against [ForwardingList].
class ForwardingListTest extends AbstractTest
        implements ListContract<ForwardingList<Integer>, Integer>, WithIntegerElement {

    @Override
    public CollectionProvider<ForwardingList<Integer>, Integer> provider() {
        return CollectionProviders.from(ForwardingList::new, ForwardingList::new, ForwardingList::new,
                elementProvider());
    }
}
