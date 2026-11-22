/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.guide;

import java.util.ArrayList;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.contract.list.ListContract;
import com.soliscode.test.contract.support.WithArrayList;
import com.soliscode.test.contract.support.WithIntegerElement;

/// The example of the README, `README.md`: [ListContract] run against the JDK's [ArrayList].
class ArrayListReadmeTest extends AbstractTest
        implements ListContract<ArrayList<Integer>, Integer>, WithArrayList<Integer>, WithIntegerElement {
}
