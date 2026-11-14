/*
 * Copyright 2026 Evan Bergstrom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.soliscode.test.contract.comparator;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.provider.ComparatorProvider;
import com.soliscode.test.provider.ComparatorProviders;

/// `ComparatorContract` run by JUnit on `String.CASE_INSENSITIVE_ORDER`, which is not consistent with `equals`, as a
/// user's test class runs it.
@DisplayName("ComparatorContract on String.CASE_INSENSITIVE_ORDER")
public class CaseInsensitiveOrderComparatorTest extends AbstractTest implements ComparatorContract<String> {

    /// Default constructor.
    public CaseInsensitiveOrderComparatorTest() {
        super();
    }

    @Override
    public ComparatorProvider<String> comparatorProvider() {
        return ComparatorProviders.caseInsensitiveOrder();
    }

    @Override
    public boolean comparatorIsConsistentWithEquals() {
        return false;
    }
}
