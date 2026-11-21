/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.charsequence;

/// **Contract for the [CharSequence] interface.**
///
/// A test class implements it with a [com.soliscode.test.provider.CharSequenceProvider] that makes sequences of the
/// class under test of given contents; JUnit runs its test methods as the class's own. Every expected value is
/// computed from the content a sequence was made of, and every index is the content's. Where the class is
/// [Comparable] with a natural order not by `char`, the class overrides
/// [com.soliscode.test.contract.support.CharSequenceContractSupport#naturalOrderIsByChar()] to return `false`.
///
/// It has a method contract for each of `CharSequence`'s nine methods, as of Java 25: [LengthContract],
/// [CharAtContract], [IsEmptyContract], [SubSequenceContract], [ToStringContract], [CharsContract],
/// [CodePointsContract], [CompareContract] (the static `compare`) and [GetCharsContract]. Each says how it reads the
/// Javadoc. `equals` and `hashCode` are not tested: `CharSequence` "does not refine" their contracts.
///
/// ## Usage Examples
/// ```java
/// class StringBuilderCharSequenceTest extends AbstractTest implements CharSequenceContract<StringBuilder> {
///     @Override
///     public CharSequenceProvider<StringBuilder> charSequenceProvider() {
///         return CharSequenceProviders.stringBuilders();
///     }
/// }
///
/// class NameTest extends AbstractTest implements CharSequenceContract<CaseInsensitiveName> {
///     @Override
///     public CharSequenceProvider<CaseInsensitiveName> charSequenceProvider() {
///         return CharSequenceProviders.of(CaseInsensitiveName::new);
///     }
///
///     @Override
///     public boolean naturalOrderIsByChar() {
///         return false;
///     }
/// }
/// ```
///
/// @param <T> the class under test
/// @since 0.1.4
/// @see CharSequence
public interface CharSequenceContract<T extends CharSequence> extends LengthContract<T>, CharAtContract<T>,
        IsEmptyContract<T>, SubSequenceContract<T>, ToStringContract<T>, CharsContract<T>, CodePointsContract<T>,
        CompareContract<T>, GetCharsContract<T> {
}
