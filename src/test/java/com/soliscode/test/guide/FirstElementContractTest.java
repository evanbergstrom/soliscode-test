/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.guide;

import java.util.ArrayList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.breakable.BreakableBuilder;
import com.soliscode.test.breakable.BreakableList;
import com.soliscode.test.contract.support.WithArrayList;
import com.soliscode.test.contract.support.WithIntegerElement;
import com.soliscode.test.provider.CollectionProvider;

import static org.junit.jupiter.api.Assertions.assertThrows;

/// How to check that a contract can fail: [FirstElementContract] passes against an [ArrayList] and fails against
/// a [BreakableList] whose `get` is broken.
class FirstElementContractTest extends AbstractTest
        implements FirstElementContract<ArrayList<Integer>, Integer>, WithArrayList<Integer>, WithIntegerElement {

    /// The contract run against a list whose `get` always returns the last element: private, so that JUnit does not
    /// run it as a test class of its own.
    private static final class BrokenGet extends AbstractTest
            implements FirstElementContract<BreakableList<Integer>, Integer>, WithIntegerElement {

        @Override
        public CollectionProvider<BreakableList<Integer>, Integer> provider() {
            BreakableList.Builder<Integer> builder = new BreakableList.Builder<Integer>()
                    .addBreak(BreakableList.GET_ALWAYS_RETURNS_THE_LAST_ELEMENT);
            return BreakableBuilder.collectionProvider(builder, elementProvider());
        }
    }

    @Test
    @DisplayName("The contract fails when get returns the last element")
    void contractFailsWhenGetReturnsTheLastElement() {
        assertThrows(AssertionFailedError.class, new BrokenGet()::getZeroReturnsTheFirstElement);
    }
}
