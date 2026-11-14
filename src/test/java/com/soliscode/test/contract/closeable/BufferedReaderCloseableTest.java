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
package com.soliscode.test.contract.closeable;

import java.io.BufferedReader;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.provider.AutoCloseableProvider;
import com.soliscode.test.provider.AutoCloseableProviders;

/// `CloseableContract` run by JUnit on a `BufferedReader`, as a user's test class runs it: its five test methods,
/// the idempotence test once, as `CloseableCloseContract`'s override.
@DisplayName("CloseableContract on BufferedReader")
public class BufferedReaderCloseableTest extends AbstractTest implements CloseableContract<BufferedReader> {

    /// Default constructor.
    public BufferedReaderCloseableTest() {
        super();
    }

    @Override
    public AutoCloseableProvider<BufferedReader> autoCloseableProvider() {
        return AutoCloseableProviders.bufferedReader();
    }
}
