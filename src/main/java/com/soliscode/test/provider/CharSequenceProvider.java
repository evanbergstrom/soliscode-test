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
package com.soliscode.test.provider;

import java.util.List;

/// Makes what a [com.soliscode.test.contract.charsequence.CharSequenceContract] tests: sequences of the class under
/// test, each holding given `char`s, and the contents to make them of.
///
/// The contract checks each sequence against the content it was made of, so `create` must give a sequence holding
/// exactly those `char`s, in order. The tests of ranges are quadratic in a content's length: keep the contents short.
///
/// ## Usage Examples
/// ```java
/// class StringBuilderTest extends AbstractTest implements CharSequenceContract<StringBuilder> {
///     @Override
///     public CharSequenceProvider<StringBuilder> charSequenceProvider() {
///         return CharSequenceProviders.stringBuilders();
///     }
/// }
///
/// class AsciiNameTest extends AbstractTest implements CharSequenceContract<AsciiName> {
///     @Override
///     public CharSequenceProvider<AsciiName> charSequenceProvider() {
///         return CharSequenceProviders.of(AsciiName::new, List.of("", "a", "hello, world"));
///     }
/// }
/// ```
///
/// @param <T> the class under test
/// @since 0.1.4
/// @see CharSequenceProviders
public interface CharSequenceProvider<T extends CharSequence> {

    /// Returns a new sequence of the class under test, holding exactly the given `char`s.
    ///
    /// @param chars the `char`s it holds: one of [#contents()]
    /// @return the sequence
    T create(String chars);

    /// Returns the contents to make sequences of. There is at least one, and none is null. A contract does not change
    /// them.
    ///
    /// @return the contents
    List<String> contents();
}
