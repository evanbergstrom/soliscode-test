/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.guide;

import java.util.Collection;
import java.util.Collections;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.contract.collection.ThreadSafeCollectionContract;
import com.soliscode.test.contract.support.WithIntegerElement;
import com.soliscode.test.methods.ObjectMethods;
import com.soliscode.test.provider.CollectionProvider;
import com.soliscode.test.provider.CollectionProviders;

/// How to write a thread-safety test: [ThreadSafeCollectionContract] run against a synchronized
/// [ForwardingList].
class SynchronizedListThreadSafetyTest extends AbstractTest
        implements ThreadSafeCollectionContract<Collection<Integer>, Integer>, WithIntegerElement {

    /// A synchronized collection inherits `equals` and `hashCode` from `Object`, which the contract's value
    /// equality would fail.
    SynchronizedListThreadSafetyTest() {
        doesNotSupportMethod(ObjectMethods.EQUALS);
        doesNotSupportMethod(ObjectMethods.HASH_CODE);
    }

    @Override
    public CollectionProvider<Collection<Integer>, Integer> provider() {
        return CollectionProviders.wrap(
                CollectionProviders.from(ForwardingList::new, ForwardingList::new, ForwardingList::new,
                        elementProvider()),
                Collections::synchronizedCollection);
    }
}
