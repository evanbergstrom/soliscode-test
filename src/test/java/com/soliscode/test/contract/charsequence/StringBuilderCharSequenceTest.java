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
package com.soliscode.test.contract.charsequence;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.provider.CharSequenceProvider;
import com.soliscode.test.provider.CharSequenceProviders;

/// `CharSequenceContract` run by JUnit on `StringBuilder`, as a user's test class runs it.
@DisplayName("CharSequenceContract on StringBuilder")
public class StringBuilderCharSequenceTest extends AbstractTest implements CharSequenceContract<StringBuilder> {

    /// Default constructor.
    public StringBuilderCharSequenceTest() {
        super();
    }

    @Override
    public CharSequenceProvider<StringBuilder> charSequenceProvider() {
        return CharSequenceProviders.stringBuilders();
    }
}
