/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.guide;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.contract.DoesNotPermitDuplicates;
import com.soliscode.test.contract.set.SetContract;
import com.soliscode.test.contract.support.WithIntegerElement;
import com.soliscode.test.provider.CollectionProvider;
import com.soliscode.test.provider.CollectionProviders;

/// How to test a collection: [SetContract] run against a set that cannot be changed, here an unmodifiable
/// [LinkedHashSet] standing in for one of the reader's own.
class UniqueTagSetTest extends AbstractTest
        implements SetContract<Set<Integer>, Integer>, WithIntegerElement, DoesNotPermitDuplicates {

    /// Tells the contract that the set refuses every change.
    UniqueTagSetTest() {
        doesNotSupportModification();
    }

    @Override
    public CollectionProvider<Set<Integer>, Integer> provider() {
        return CollectionProviders.wrap(
                CollectionProviders.from(LinkedHashSet::new, LinkedHashSet::new, LinkedHashSet::new,
                        elementProvider()),
                Collections::unmodifiableSet);
    }
}
