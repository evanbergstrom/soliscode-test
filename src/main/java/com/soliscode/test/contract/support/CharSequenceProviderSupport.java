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
package com.soliscode.test.contract.support;

import com.soliscode.test.provider.CharSequenceProvider;

/// Interface for contract classes that test a [CharSequence], which a [CharSequenceProvider] makes of given contents.
///
/// @param <T> the class under test
/// @since 0.1.4
public interface CharSequenceProviderSupport<T extends CharSequence> {

    /// Returns the provider of the sequences under test and their contents.
    ///
    /// @return the char sequence provider
    CharSequenceProvider<T> charSequenceProvider();
}
