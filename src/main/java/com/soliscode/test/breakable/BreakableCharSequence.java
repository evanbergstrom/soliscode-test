/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.breakable;

import java.io.Serial;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

import org.jspecify.annotations.Nullable;

/// A char sequence that can be broken in well-defined ways, to show that
/// [com.soliscode.test.contract.charsequence.CharSequenceContract] can fail.
///
/// It wraps a delegate sequence, as [BreakableComparable] wraps a value. Unbroken, each method returns the delegate's
/// own result, `isEmpty`, `chars`, `codePoints` and `getChars` included, so that a break reaches only its own method;
/// its subsequences are the delegate's, without the breaks. It is [Comparable], as String, StringBuilder and
/// CharBuffer are, so that [CharSequence#compare] of two of them is its `compareTo`, which is
/// `CharSequence.compare` of the delegates. It is made with its constructor, and has no builder.
///
/// `equals` and `hashCode` are `Object`'s, as StringBuilder's are, so its natural ordering is not consistent with
/// `equals`: a [com.soliscode.test.contract.comparable.ComparableContract] run on it must override
/// `naturalOrderingIsConsistentWithEquals()` to return `false`.
///
/// Each bounds break of `subSequence` and `getChars` acts where its own rule is broken and every argument it does not
/// name is in bounds, and then skips the remaining checks; elsewhere every rule is checked as documented.
///
/// # Breaks
/// The breaks that are supported for this class are listed in the description of the method that they impact.
///
/// ## Thread Safety
/// Once it is safely published, its delegate never changes. Its breaks are a copy of the set it was made with, which
/// `addBreak` changes, unsynchronized. It may be shared between threads only while its breaks are not changed, and
/// only where its delegate is itself safe to share: it calls the delegate without a lock.
///
/// ## Serialization
/// It is serializable, as every breakable is, where its delegate is.
///
/// @since 0.1.4
/// @see CharSequence
public class BreakableCharSequence extends AbstractBreakable
        implements CharSequence, Comparable<BreakableCharSequence> {

    /// **Purpose:** `length()` counts code points, where [CharSequence#length()] counts `char`s.
    ///
    /// **Effect:** `length()` is [Character#codePointCount(CharSequence, int, int)] of the delegate: a surrogate pair
    /// counts as one.
    ///
    /// **Affected Methods:** [#length()]
    public static final Break LENGTH_COUNTS_CODE_POINTS =
            new Break("length counts code points, not chars");

    /// **Purpose:** `charAt` returns the wrong `char`.
    ///
    /// **Effect:** `charAt(i)`, for an index in bounds, is the delegate's `charAt(min(i + 1, length - 1))`. Out of
    /// bounds, it throws as documented.
    ///
    /// **Affected Methods:** [#charAt(int)]
    public static final Break CHAR_AT_IS_OFF_BY_ONE =
            new Break("charAt returns the char after the index");

    /// **Purpose:** `charAt` past the end does not throw, where [CharSequence#charAt(int)] throws
    /// [IndexOutOfBoundsException] for an index not less than `length()`.
    ///
    /// **Effect:** `charAt(i)` for `i >= length()` returns `'\0'`.
    ///
    /// **Affected Methods:** [#charAt(int)]
    public static final Break CHAR_AT_PAST_THE_END_RETURNS_ZERO =
            new Break("charAt past the end returns the zero char");

    /// **Purpose:** `charAt` of a negative index throws the wrong exception, where [CharSequence#charAt(int)] throws
    /// [IndexOutOfBoundsException].
    ///
    /// **Effect:** `charAt(i)` for `i < 0` throws [IllegalArgumentException].
    ///
    /// **Affected Methods:** [#charAt(int)]
    public static final Break CHAR_AT_NEGATIVE_INDEX_THROWS_ILLEGAL_ARGUMENT =
            new Break("charAt of a negative index throws IllegalArgumentException");

    /// **Purpose:** `isEmpty()` is wrong for the empty sequence.
    ///
    /// **Effect:** `isEmpty()` returns `false`.
    ///
    /// **Affected Methods:** [#isEmpty()]
    public static final Break IS_EMPTY_ALWAYS_RETURNS_FALSE =
            new Break("isEmpty always returns false");

    /// **Purpose:** `subSequence` reads its second argument as a length, where [CharSequence#subSequence(int, int)]
    /// takes an end index.
    ///
    /// **Effect:** the bounds are checked as documented; then `subSequence(start, end)` holds the `char`s from `start`
    /// to `min(start + end, length) - 1`. From `start` 0 it is right.
    ///
    /// **Affected Methods:** [#subSequence(int, int)]
    public static final Break SUB_SEQUENCE_TREATS_END_AS_A_LENGTH =
            new Break("subSequence treats its end as a length");

    /// **Purpose:** `subSequence` accepts a negative `start`, where [CharSequence#subSequence(int, int)] throws
    /// [IndexOutOfBoundsException].
    ///
    /// **Effect:** where `start < 0` and `end` is in `[0, length]`, `start` is taken as 0.
    ///
    /// **Affected Methods:** [#subSequence(int, int)]
    public static final Break SUB_SEQUENCE_ALLOWS_A_NEGATIVE_START =
            new Break("subSequence takes a negative start as zero");

    /// **Purpose:** `subSequence` accepts a negative `end`, where [CharSequence#subSequence(int, int)] throws
    /// [IndexOutOfBoundsException].
    ///
    /// **Effect:** where `end < 0` and `start` is in `[0, length]`, it returns an empty sequence.
    ///
    /// **Affected Methods:** [#subSequence(int, int)]
    public static final Break SUB_SEQUENCE_ALLOWS_A_NEGATIVE_END =
            new Break("subSequence of a negative end is empty");

    /// **Purpose:** `subSequence` accepts a `start` after `end`, where [CharSequence#subSequence(int, int)] throws
    /// [IndexOutOfBoundsException].
    ///
    /// **Effect:** where `start > end` and both are in `[0, length]`, it returns an empty sequence.
    ///
    /// **Affected Methods:** [#subSequence(int, int)]
    public static final Break SUB_SEQUENCE_ALLOWS_START_AFTER_END =
            new Break("subSequence of a start after the end is empty");

    /// **Purpose:** `subSequence` accepts an `end` past the length, where [CharSequence#subSequence(int, int)] throws
    /// [IndexOutOfBoundsException].
    ///
    /// **Effect:** where `end > length` and `start` is in `[0, length]`, `end` is taken as `length`.
    ///
    /// **Affected Methods:** [#subSequence(int, int)]
    public static final Break SUB_SEQUENCE_ALLOWS_END_PAST_THE_LENGTH =
            new Break("subSequence clamps an end past the length");

    /// **Purpose:** `toString()` is `Object`'s, as in a class that forgot to override it, where
    /// [CharSequence#toString()] holds the sequence's `char`s.
    ///
    /// **Effect:** `toString()` is the class name, `@` and the identity hash code in hexadecimal.
    ///
    /// **Affected Methods:** [#toString()]
    public static final Break TO_STRING_IS_OBJECTS =
            new Break("toString is Object's");

    /// **Purpose:** `chars()` sign-extends, where [CharSequence#chars()] zero-extends each `char`.
    ///
    /// **Effect:** each `char` of the delegate's `chars()` goes through `(short)`: U+FFFF is -1, and every `char` from
    /// U+8000 up, the surrogates included, is negative.
    ///
    /// **Affected Methods:** [#chars()]
    public static final Break CHARS_SIGN_EXTENDS =
            new Break("chars sign-extends each char");

    /// **Purpose:** `codePoints()` does not combine surrogate pairs, where [CharSequence#codePoints()] does.
    ///
    /// **Effect:** `codePoints()` is the delegate's `chars()`.
    ///
    /// **Affected Methods:** [#codePoints()]
    public static final Break CODE_POINTS_DOES_NOT_COMBINE_SURROGATE_PAIRS =
            new Break("codePoints does not combine surrogate pairs");

    /// **Purpose:** `codePoints()` drops unpaired surrogates, where [CharSequence#codePoints()] zero-extends them.
    ///
    /// **Effect:** `codePoints()` is the delegate's, without the code points from U+D800 to U+DFFF.
    ///
    /// **Affected Methods:** [#codePoints()]
    public static final Break CODE_POINTS_DROPS_UNPAIRED_SURROGATES =
            new Break("codePoints drops unpaired surrogates");

    /// **Purpose:** `compareTo` compares code points, so that [CharSequence#compare] of two of these sequences is not
    /// by `char`, as its Javadoc says it is.
    ///
    /// **Effect:** `compareTo` compares the delegates' code points lexicographically, the shorter first where one is a
    /// prefix of the other. U+E000 and U+FFFF then come before U+1F600, whose high surrogate is U+D83D.
    ///
    /// **Affected Methods:** [#compareTo(BreakableCharSequence)]
    public static final Break COMPARE_TO_COMPARES_CODE_POINTS =
            new Break("compareTo compares code points, not chars");

    /// **Purpose:** `compareTo` is shortlex, so that [CharSequence#compare] of two of these sequences is not
    /// lexicographic.
    ///
    /// **Effect:** `compareTo` compares the lengths first, and only sequences of one length by `char`. U+00E9 then
    /// comes before `"hello, world"`.
    ///
    /// **Affected Methods:** [#compareTo(BreakableCharSequence)]
    public static final Break COMPARE_TO_COMPARES_LENGTHS_FIRST =
            new Break("compareTo compares the lengths first");

    /// **Purpose:** `getChars` ignores `dstBegin`, where [CharSequence#getChars(int, int, char\[\], int)] copies to it.
    ///
    /// **Effect:** the bounds are checked as documented; then the `char`s are copied to `dst` from index 0.
    ///
    /// **Affected Methods:** [#getChars(int, int, char\[\], int)]
    public static final Break GET_CHARS_IGNORES_THE_DESTINATION_OFFSET =
            new Break("getChars copies to the start of the destination");

    /// **Purpose:** `getChars` copies one `char` too many, where [CharSequence#getChars(int, int, char\[\], int)]
    /// copies `srcEnd - srcBegin`.
    ///
    /// **Effect:** where `srcEnd < length` and `dst` has room after the range, the `char` at `srcEnd` is copied too.
    ///
    /// **Affected Methods:** [#getChars(int, int, char\[\], int)]
    public static final Break GET_CHARS_COPIES_ONE_CHAR_TOO_MANY =
            new Break("getChars copies one char too many");

    /// **Purpose:** `getChars` accepts a negative `srcBegin`, where [CharSequence#getChars(int, int, char\[\], int)]
    /// throws [IndexOutOfBoundsException].
    ///
    /// **Effect:** where `srcBegin < 0`, `srcEnd` is in `[0, length]`, and `srcEnd` `char`s fit in `dst` from
    /// `dstBegin`, `srcBegin` is taken as 0.
    ///
    /// **Affected Methods:** [#getChars(int, int, char\[\], int)]
    public static final Break GET_CHARS_ALLOWS_A_NEGATIVE_SOURCE_BEGIN =
            new Break("getChars takes a negative source begin as zero");

    /// **Purpose:** `getChars` accepts a negative `dstBegin`, where [CharSequence#getChars(int, int, char\[\], int)]
    /// throws [IndexOutOfBoundsException].
    ///
    /// **Effect:** where `dstBegin < 0`, the source range is in bounds, and it fits in `dst`, `dstBegin` is taken as 0.
    ///
    /// **Affected Methods:** [#getChars(int, int, char\[\], int)]
    public static final Break GET_CHARS_ALLOWS_A_NEGATIVE_DESTINATION_BEGIN =
            new Break("getChars takes a negative destination begin as zero");

    /// **Purpose:** `getChars` accepts a `srcBegin` after `srcEnd`, where
    /// [CharSequence#getChars(int, int, char\[\], int)] throws [IndexOutOfBoundsException].
    ///
    /// **Effect:** where `srcBegin > srcEnd`, both are in `[0, length]`, and `dstBegin` is in `[0, dst.length]`, it
    /// returns, copying nothing.
    ///
    /// **Affected Methods:** [#getChars(int, int, char\[\], int)]
    public static final Break GET_CHARS_ALLOWS_SOURCE_BEGIN_AFTER_END =
            new Break("getChars of a source begin after the end copies nothing");

    /// **Purpose:** `getChars` accepts a `srcEnd` past the length, where
    /// [CharSequence#getChars(int, int, char\[\], int)] throws [IndexOutOfBoundsException].
    ///
    /// **Effect:** where `srcEnd > length`, `srcBegin` is in `[0, length]`, and the `char`s from `srcBegin` to the end
    /// fit in `dst` from `dstBegin`, `srcEnd` is taken as `length`.
    ///
    /// **Affected Methods:** [#getChars(int, int, char\[\], int)]
    public static final Break GET_CHARS_ALLOWS_SOURCE_END_PAST_THE_LENGTH =
            new Break("getChars clamps a source end past the length");

    /// **Purpose:** `getChars` does not check that the range fits in `dst`, where
    /// [CharSequence#getChars(int, int, char\[\], int)] throws [IndexOutOfBoundsException] if
    /// `dstBegin + srcEnd - srcBegin > dst.length`.
    ///
    /// **Effect:** where the source range is in bounds, `dstBegin` is in `[0, dst.length]`, and the range does not fit,
    /// it copies what fits, and does not throw.
    ///
    /// **Affected Methods:** [#getChars(int, int, char\[\], int)]
    public static final Break GET_CHARS_DOES_NOT_CHECK_THE_DESTINATION_SIZE =
            new Break("getChars copies what fits in a destination too small");

    /// **Purpose:** `getChars` accepts a null `dst`, where [CharSequence#getChars(int, int, char\[\], int)] throws
    /// [NullPointerException].
    ///
    /// **Effect:** where `dst` is null, it returns, doing nothing.
    ///
    /// **Affected Methods:** [#getChars(int, int, char\[\], int)]
    public static final Break GET_CHARS_ACCEPTS_A_NULL_DESTINATION =
            new Break("getChars of a null destination does nothing");

    @Serial
    private static final long serialVersionUID = 1L;

    /// The breaks of `getChars`: with none of them set, it is the delegate's.
    private static final Set<Break> GET_CHARS_BREAKS = Set.of(GET_CHARS_IGNORES_THE_DESTINATION_OFFSET,
            GET_CHARS_COPIES_ONE_CHAR_TOO_MANY, GET_CHARS_ALLOWS_A_NEGATIVE_SOURCE_BEGIN,
            GET_CHARS_ALLOWS_A_NEGATIVE_DESTINATION_BEGIN, GET_CHARS_ALLOWS_SOURCE_BEGIN_AFTER_END,
            GET_CHARS_ALLOWS_SOURCE_END_PAST_THE_LENGTH, GET_CHARS_DOES_NOT_CHECK_THE_DESTINATION_SIZE,
            GET_CHARS_ACCEPTS_A_NULL_DESTINATION);

    /// The delegate.
    private final CharSequence delegate;

    /// Creates a `BreakableCharSequence` over a delegate, with the given breaks.
    ///
    /// @param delegateArg the delegate
    /// @param breaks      the set of breaks to apply, copied
    public BreakableCharSequence(final CharSequence delegateArg, final Set<Break> breaks) {
        super(breaks, Map.of(), false);
        this.delegate = Objects.requireNonNull(delegateArg);
    }

    /// Returns the delegate.
    ///
    /// @return the delegate
    public CharSequence delegate() {
        return delegate;
    }

    /// Implements [CharSequence#length()]: the delegate's.
    ///
    /// # Breaks
    /// - [#LENGTH_COUNTS_CODE_POINTS]
    ///
    /// @return the number of `char`s
    @Override
    public int length() {
        if (hasBreak(LENGTH_COUNTS_CODE_POINTS)) {
            return Character.codePointCount(delegate, 0, delegate.length());
        }
        return delegate.length();
    }

    /// Implements [CharSequence#charAt(int)]: the delegate's.
    ///
    /// # Breaks
    /// - [#CHAR_AT_IS_OFF_BY_ONE]
    /// - [#CHAR_AT_PAST_THE_END_RETURNS_ZERO]
    /// - [#CHAR_AT_NEGATIVE_INDEX_THROWS_ILLEGAL_ARGUMENT]
    ///
    /// @param index the index
    /// @return the `char` at it
    /// @throws IndexOutOfBoundsException if `index` is negative or not less than the length, unless broken
    @Override
    public char charAt(final int index) {
        int n = delegate.length();
        if (index >= n && hasBreak(CHAR_AT_PAST_THE_END_RETURNS_ZERO)) {
            return '\0';
        }
        if (index < 0 && hasBreak(CHAR_AT_NEGATIVE_INDEX_THROWS_ILLEGAL_ARGUMENT)) {
            throw new IllegalArgumentException("charAt(" + index + ")");
        }
        if (index >= 0 && index < n && hasBreak(CHAR_AT_IS_OFF_BY_ONE)) {
            return delegate.charAt(Math.min(index + 1, n - 1));
        }
        return delegate.charAt(index);
    }

    /// Implements [CharSequence#isEmpty()]: the delegate's.
    ///
    /// # Breaks
    /// - [#IS_EMPTY_ALWAYS_RETURNS_FALSE]
    ///
    /// @return whether the sequence is empty
    @Override
    public boolean isEmpty() {
        if (hasBreak(IS_EMPTY_ALWAYS_RETURNS_FALSE)) {
            return false;
        }
        return delegate.isEmpty();
    }

    /// Implements [CharSequence#subSequence(int, int)]: the delegate's subsequence, without the breaks.
    ///
    /// # Breaks
    /// - [#SUB_SEQUENCE_TREATS_END_AS_A_LENGTH]
    /// - [#SUB_SEQUENCE_ALLOWS_A_NEGATIVE_START]
    /// - [#SUB_SEQUENCE_ALLOWS_A_NEGATIVE_END]
    /// - [#SUB_SEQUENCE_ALLOWS_START_AFTER_END]
    /// - [#SUB_SEQUENCE_ALLOWS_END_PAST_THE_LENGTH]
    ///
    /// @param start the start index, inclusive
    /// @param end   the end index, exclusive
    /// @return the subsequence
    /// @throws IndexOutOfBoundsException if `start` or `end` is negative, `end` is greater than the length, or
    ///         `start` is greater than `end`, unless broken
    @Override
    public CharSequence subSequence(final int start, final int end) {
        int n = delegate.length();
        CharSequence allowed = allowedOutOfBounds(start, end, n);
        if (allowed != null) {
            return allowed;
        }
        if (hasBreak(SUB_SEQUENCE_TREATS_END_AS_A_LENGTH)) {
            Objects.checkFromToIndex(start, end, n);
            return delegate.subSequence(start, (int) Math.min((long) start + end, n));
        }
        return delegate.subSequence(start, end);
    }

    /// The subsequence a break that allows out-of-bounds indexes gives for them, or `null` if none applies.
    private @Nullable CharSequence allowedOutOfBounds(final int start, final int end, final int n) {
        if (start < 0 && inRange(end, n) && hasBreak(SUB_SEQUENCE_ALLOWS_A_NEGATIVE_START)) {
            return delegate.subSequence(0, end);
        }
        if (end < 0 && inRange(start, n) && hasBreak(SUB_SEQUENCE_ALLOWS_A_NEGATIVE_END)) {
            return delegate.subSequence(0, 0);
        }
        if (start > end && inRange(start, n) && inRange(end, n) && hasBreak(SUB_SEQUENCE_ALLOWS_START_AFTER_END)) {
            return delegate.subSequence(0, 0);
        }
        if (end > n && inRange(start, n) && hasBreak(SUB_SEQUENCE_ALLOWS_END_PAST_THE_LENGTH)) {
            return delegate.subSequence(start, n);
        }
        return null;
    }

    /// Returns the delegate's string form, which [CharSequence#toString()] says holds its `char`s.
    ///
    /// # Breaks
    /// - [#TO_STRING_IS_OBJECTS]
    ///
    /// @return the string form
    @Override
    public String toString() {
        if (hasBreak(TO_STRING_IS_OBJECTS)) {
            return getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(this));
        }
        return delegate.toString();
    }

    /// Implements [CharSequence#chars()]: the delegate's.
    ///
    /// # Breaks
    /// - [#CHARS_SIGN_EXTENDS]
    ///
    /// @return the stream of `char` values
    @Override
    public IntStream chars() {
        if (hasBreak(CHARS_SIGN_EXTENDS)) {
            return delegate.chars().map(c -> (short) c);
        }
        return delegate.chars();
    }

    /// Implements [CharSequence#codePoints()]: the delegate's.
    ///
    /// # Breaks
    /// - [#CODE_POINTS_DOES_NOT_COMBINE_SURROGATE_PAIRS]
    /// - [#CODE_POINTS_DROPS_UNPAIRED_SURROGATES]
    ///
    /// @return the stream of code points
    @Override
    public IntStream codePoints() {
        if (hasBreak(CODE_POINTS_DOES_NOT_COMBINE_SURROGATE_PAIRS)) {
            return delegate.chars();
        }
        if (hasBreak(CODE_POINTS_DROPS_UNPAIRED_SURROGATES)) {
            return delegate.codePoints().filter(c -> c < Character.MIN_SURROGATE || c > Character.MAX_SURROGATE);
        }
        return delegate.codePoints();
    }

    /// Implements [CharSequence#getChars(int, int, char\[\], int)]: the delegate's.
    ///
    /// # Breaks
    /// - [#GET_CHARS_IGNORES_THE_DESTINATION_OFFSET]
    /// - [#GET_CHARS_COPIES_ONE_CHAR_TOO_MANY]
    /// - [#GET_CHARS_ALLOWS_A_NEGATIVE_SOURCE_BEGIN]
    /// - [#GET_CHARS_ALLOWS_A_NEGATIVE_DESTINATION_BEGIN]
    /// - [#GET_CHARS_ALLOWS_SOURCE_BEGIN_AFTER_END]
    /// - [#GET_CHARS_ALLOWS_SOURCE_END_PAST_THE_LENGTH]
    /// - [#GET_CHARS_DOES_NOT_CHECK_THE_DESTINATION_SIZE]
    /// - [#GET_CHARS_ACCEPTS_A_NULL_DESTINATION]
    ///
    /// @param srcBegin the index of the first `char` to copy
    /// @param srcEnd   the index after the last `char` to copy
    /// @param dst      the array to copy into; `null` only to be refused
    /// @param dstBegin the index in `dst` to copy to
    /// @throws IndexOutOfBoundsException if an argument is out of bounds, unless broken
    /// @throws NullPointerException if `dst` is null, unless broken
    @Override
    public void getChars(final int srcBegin, final int srcEnd, final char @Nullable [] dst, final int dstBegin) {
        if (GET_CHARS_BREAKS.stream().noneMatch(this::hasBreak)) {
            delegate.getChars(srcBegin, srcEnd, dst, dstBegin);
            return;
        }
        if (dst == null) {
            if (hasBreak(GET_CHARS_ACCEPTS_A_NULL_DESTINATION)) {
                return;
            }
            throw new NullPointerException("dst");
        }
        int n = delegate.length();
        int size = dst.length;
        int begin = srcBegin;
        int end = srcEnd;
        int at = dstBegin;
        if (allowsNegativeSourceBegin(begin, end, at, n, size)) {
            begin = 0;
        } else if (allowsNegativeDestinationBegin(begin, end, at, n, size)) {
            at = 0;
        } else if (allowsSourceBeginAfterEnd(begin, end, at, n, size)) {
            return;
        } else if (allowsSourceEndPastTheLength(begin, end, at, n, size)) {
            end = n;
        } else if (doesNotCheckTheDestinationSize(begin, end, at, n, size)) {
            end = begin + size - at;
        }
        Objects.checkFromToIndex(begin, end, n);
        Objects.checkFromIndexSize(at, end - begin, size);
        if (hasBreak(GET_CHARS_IGNORES_THE_DESTINATION_OFFSET)) {
            at = 0;
        } else if (copiesOneCharTooMany(begin, end, at, n, size)) {
            end++;
        }
        delegate.getChars(begin, end, dst, at);
    }

    /// Compares the delegates lexicographically by `char`, as [CharSequence#compare] does.
    ///
    /// # Breaks
    /// - [#COMPARE_TO_COMPARES_CODE_POINTS]
    /// - [#COMPARE_TO_COMPARES_LENGTHS_FIRST]
    ///
    /// @param other the sequence to compare with; `null` only to be refused
    /// @return a negative number, zero or a positive number as this sequence is less than, equal to or greater than
    ///         the other
    /// @throws NullPointerException if `other` is null
    @Override
    public int compareTo(final @Nullable BreakableCharSequence other) {
        if (other == null) {
            throw new NullPointerException("compareTo(null)");
        }
        if (hasBreak(COMPARE_TO_COMPARES_CODE_POINTS)) {
            return Arrays.compare(delegate.codePoints().toArray(), other.delegate.codePoints().toArray());
        }
        if (hasBreak(COMPARE_TO_COMPARES_LENGTHS_FIRST) && delegate.length() != other.delegate.length()) {
            return Integer.compare(delegate.length(), other.delegate.length());
        }
        return CharSequence.compare(delegate, other.delegate);
    }

    /// Whether [#GET_CHARS_ALLOWS_A_NEGATIVE_SOURCE_BEGIN] applies to `getChars(begin, end, dst, at)`, with `n` the
    /// length and `size` the length of `dst`: only `begin` is out of bounds, and it is negative.
    private boolean allowsNegativeSourceBegin(final int begin, final int end, final int at, final int n,
            final int size) {
        return begin < 0 && inRange(end, n) && fits(at, end, size)
                && hasBreak(GET_CHARS_ALLOWS_A_NEGATIVE_SOURCE_BEGIN);
    }

    /// Whether [#GET_CHARS_ALLOWS_A_NEGATIVE_DESTINATION_BEGIN] applies: only `at` is out of bounds, and it is
    /// negative.
    private boolean allowsNegativeDestinationBegin(final int begin, final int end, final int at, final int n,
            final int size) {
        return at < 0 && validSource(begin, end, n) && end - begin <= size
                && hasBreak(GET_CHARS_ALLOWS_A_NEGATIVE_DESTINATION_BEGIN);
    }

    /// Whether [#GET_CHARS_ALLOWS_SOURCE_BEGIN_AFTER_END] applies: every index is in range, and `begin` is after
    /// `end`.
    private boolean allowsSourceBeginAfterEnd(final int begin, final int end, final int at, final int n,
            final int size) {
        return begin > end && inRange(begin, n) && inRange(end, n) && inRange(at, size)
                && hasBreak(GET_CHARS_ALLOWS_SOURCE_BEGIN_AFTER_END);
    }

    /// Whether [#GET_CHARS_ALLOWS_SOURCE_END_PAST_THE_LENGTH] applies: only `end` is out of bounds, past the
    /// length.
    private boolean allowsSourceEndPastTheLength(final int begin, final int end, final int at, final int n,
            final int size) {
        return end > n && inRange(begin, n) && fits(at, n - begin, size)
                && hasBreak(GET_CHARS_ALLOWS_SOURCE_END_PAST_THE_LENGTH);
    }

    /// Whether [#GET_CHARS_DOES_NOT_CHECK_THE_DESTINATION_SIZE] applies: the indexes are in range, and the `char`s
    /// do not fit in `dst` from `at`.
    private boolean doesNotCheckTheDestinationSize(final int begin, final int end, final int at, final int n,
            final int size) {
        return validSource(begin, end, n) && inRange(at, size) && !fits(at, end - begin, size)
                && hasBreak(GET_CHARS_DOES_NOT_CHECK_THE_DESTINATION_SIZE);
    }

    /// Whether [#GET_CHARS_COPIES_ONE_CHAR_TOO_MANY] applies: there is a `char` after `end`, and room for it.
    private boolean copiesOneCharTooMany(final int begin, final int end, final int at, final int n, final int size) {
        return end < n && fits(at, end + 1 - begin, size) && hasBreak(GET_CHARS_COPIES_ONE_CHAR_TOO_MANY);
    }

    /// Whether `[begin, end)` is a range of a sequence of length `n`.
    private static boolean validSource(final int begin, final int end, final int n) {
        return 0 <= begin && begin <= end && end <= n;
    }

    /// Whether `i` is in `[0, n]`.
    private static boolean inRange(final int i, final int n) {
        return 0 <= i && i <= n;
    }

    /// Whether `count` `char`s fit in an array of `size` from `at`, `at` not negative.
    private static boolean fits(final int at, final int count, final int size) {
        return at >= 0 && count >= 0 && at <= size - count;
    }
}
