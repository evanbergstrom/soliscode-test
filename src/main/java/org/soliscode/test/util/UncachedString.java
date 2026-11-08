package org.soliscode.test.util;

import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.stream.IntStream;

/// **String Wrapper with Controlled Hash Code Behavior**
///
/// This utility class provides a wrapper around `String` that implements the standard
/// `CharSequence`, `Comparable`, and `Serializable` interfaces while maintaining controlled
/// hash code computation behavior. It serves as a testing utility for scenarios where
/// predictable object identity and hash distribution patterns are required.
///
/// ## Core Purpose
///
/// The primary purpose of `UncachedString` is to provide a string-like object that can be
/// used in testing scenarios where the standard `String` class's optimizations (such as
/// string interning and hash code caching) might interfere with test behavior or make
/// tests non-deterministic.
///
/// ### Key Characteristics
///
/// - **String Delegation**: All string operations delegate to the wrapped `String` instance
/// - **Interface Compliance**: Fully implements `CharSequence`, `Comparable<UncachedString>`, and `Serializable`
/// - **Controlled Behavior**: Provides predictable object behavior without string optimizations
/// - **Test-Friendly**: Designed specifically for testing scenarios requiring controlled string behavior
///
/// ## Interface Implementations
///
/// ### CharSequence Implementation
/// Provides complete compatibility with string processing APIs that accept `CharSequence`:
/// - Character access via `charAt(int)`
/// - Length queries via `length()`
/// - Subsequence extraction via `subSequence(int, int)`
/// - Stream-based character processing via `chars()` and `codePoints()`
///
/// ### Comparable Implementation
/// Enables natural ordering based on the underlying string content:
/// - Lexicographical comparison using `String.compareTo()`
/// - Consistent with equals for proper sorting behavior
/// - Type-safe comparison with other `UncachedString` instances
///
/// ### Serializable Implementation
/// Supports object serialization for testing scenarios involving:
/// - Persistence testing
/// - Network communication testing
/// - Object caching validation
/// - Distributed system testing
///
/// ## Usage Patterns
///
/// ### Hash Code Testing
/// ```java
/// // Test hash distribution without string interning effects
/// UncachedString str1 = new UncachedString("test");
/// UncachedString str2 = new UncachedString("test");
///
/// // These are different objects (unlike String literals)
/// assertNotSame(str1, str2);
/// assertEquals(str1, str2);  // But still equal in value
/// assertEquals(str1.hashCode(), str2.hashCode());  // With consistent hash codes
/// ```
///
/// ### Collection Testing
/// ```java
/// // Test collection behavior with controlled string objects
/// Set<UncachedString> stringSet = new HashSet<>();
/// stringSet.add_singleElement_returnsTrueAndUpdatesSize(new UncachedString("alpha"));
/// stringSet.add_singleElement_returnsTrueAndUpdatesSize(new UncachedString("beta"));
/// stringSet.add_singleElement_returnsTrueAndUpdatesSize(new UncachedString("alpha"));  // Duplicate value
///
/// assertEquals(2, stringSet.size());  // Duplicates properly handled
/// ```
///
/// ### Sorting and Comparison Testing
/// ```java
/// // Test natural ordering without string interning complications
/// List<UncachedString> strings = Arrays.asList(
///     new UncachedString("zebra"),
///     new UncachedString("alpha"),
///     new UncachedString("beta")
/// );
///
/// Collections.sort(strings);
/// assertEquals("alpha", strings.get(0).toString());
/// assertEquals("beta", strings.get(1).toString());
/// assertEquals("zebra", strings.get(2).toString());
/// ```
///
/// ### CharSequence API Testing
/// ```java
/// // Test string processing APIs that accept CharSequence
/// UncachedString text = new UncachedString("Hello, World!");
///
/// // Works with standard string processing methods
/// assertTrue(text.toString().contains("World"));
/// assertEquals(13, text.length());
/// assertEquals('H', text.charAt(0));
/// assertEquals("Hello", text.subSequence(0, 5).toString());
/// ```
///
/// ### Stream Processing Testing
/// ```java
/// // Test character stream processing
/// UncachedString text = new UncachedString("ABC123");
///
/// long letterCount = text.chars()
///     .filter(Character::isLetter)
///     .count();
/// assertEquals(3, letterCount);
///
/// long digitCount = text.chars()
///     .filter(Character::isDigit)
///     .count();
/// assertEquals(3, digitCount);
/// ```
///
/// ## Testing Applications
///
/// ### Object Identity Testing
/// Validate that algorithms correctly handle object identity vs. value equality:
/// - Hash table implementation testing
/// - Object caching validation
/// - Reference equality vs. value equality testing
///
/// ### Performance Testing
/// Benchmark algorithms with controlled object creation patterns:
/// - Memory allocation testing
/// - Garbage collection impact analysis
/// - Hash distribution validation
///
/// ### Serialization Testing
/// Test object persistence and transmission scenarios:
/// - Custom serialization logic validation
/// - Network protocol testing
/// - Caching system testing
///
/// ### Collection Framework Testing
/// Validate collection implementations with predictable object behavior:
/// - Hash-based collection testing
/// - Sorted collection testing
/// - Custom collection implementation validation
///
/// ## Implementation Notes
///
/// ### Thread Safety
/// This class is **thread-safe** because:
/// - The wrapped `String` is immutable
/// - The `value` field is `final` and cannot be changed after construction
/// - All methods are read-only operations on immutable data
///
/// ### Memory Considerations
/// - Each instance maintains a reference to a `String` object
/// - No additional memory overhead beyond the object header and string reference
/// - Eligible for garbage collection when no longer referenced
///
/// ### Performance Characteristics
/// - **Time Complexity**: All operations are O(1) or delegate to `String` with equivalent complexity
/// - **Space Complexity**: O(1) additional space beyond the wrapped string
/// - **Hash Code**: Computed on every call (not cached like `String`)
///
/// @author evanbergstrom
/// @since 1.0.0
/// @see String
/// @see CharSequence
/// @see Comparable
/// @see java.io.Serializable
public class UncachedString implements java.io.Serializable, Comparable<UncachedString>, CharSequence {

    /// The wrapped string value that provides the underlying string content.
    ///
    /// This field is declared `final` to ensure immutability and thread safety.
    /// All string operations delegate to this underlying `String` instance.
    private final String value;

    /// Creates a new UncachedString wrapper around the specified string value.
    ///
    /// This constructor creates an immutable wrapper around the provided string,
    /// enabling controlled string behavior for testing scenarios. The wrapper
    /// maintains a reference to the original string and delegates all operations
    /// to it while providing predictable object identity behavior.
    ///
    /// ## Usage Examples
    ///
    /// ```java
    /// // Basic construction
    /// UncachedString simple = new UncachedString("hello");
    /// assertEquals("hello", simple.toString());
    ///
    /// // Construction with special characters
    /// UncachedString unicode = new UncachedString("Hello, 世界! 🌍");
    /// assertEquals(11, unicode.length());  // Includes Unicode characters
    ///
    /// // Empty string handling
    /// UncachedString empty = new UncachedString("");
    /// assertTrue(empty.isEmpty());
    /// assertEquals(0, empty.length());
    ///
    /// // Multiline string support
    /// UncachedString multiline = new UncachedString("Line 1\nLine 2\nLine 3");
    /// assertEquals(3, multiline.toString().split("\n").length);
    /// ```
    ///
    /// ## Object Identity Behavior
    ///
    /// Unlike `String` literals which may be interned, each `UncachedString`
    /// instance is a distinct object even when wrapping identical string values:
    ///
    /// ```java
    /// UncachedString str1 = new UncachedString("test");
    /// UncachedString str2 = new UncachedString("test");
    ///
    /// assertNotSame(str1, str2);           // Different object references
    /// assertEquals(str1, str2);            // But logically equal
    /// assertEquals(str1.hashCode(), str2.hashCode());  // With consistent hash codes
    /// ```
    ///
    /// @param value the string value to wrap; must not be null
    /// @throws NullPointerException if value is null (enforced by @NonNull annotation)
    /// @see String
    /// @since 1.0.0
    public UncachedString(final @NonNull String value) {
        this.value = value;
    }

    /// Returns the length of the wrapped string.
    ///
    /// This method delegates to the underlying `String.length()` method to provide
    /// the character count of the wrapped string content. The length includes all
    /// characters including whitespace, special characters, and Unicode characters.
    ///
    /// ## Examples
    ///
    /// ```java
    /// UncachedString simple = new UncachedString("hello");
    /// assertEquals(5, simple.length());
    ///
    /// UncachedString empty = new UncachedString("");
    /// assertEquals(0, empty.length());
    ///
    /// UncachedString unicode = new UncachedString("café");
    /// assertEquals(4, unicode.length());  // é counts as one character
    ///
    /// UncachedString emoji = new UncachedString("Hello 👋");
    /// assertEquals(7, emoji.length());  // Emoji counts as one character
    /// ```
    ///
    /// @return the number of characters in this string
    /// @see String#length()
    /// @since 1.0.0
    @Override
    public int length() {
        return value.length();
    }

    /// Returns the character at the specified index.
    ///
    /// This method delegates to the underlying `String.charAt()` method to retrieve
    /// the character at the specified position. The index is zero-based, with the
    /// first character at index 0.
    ///
    /// ## Examples
    ///
    /// ```java
    /// UncachedString text = new UncachedString("Hello");
    /// assertEquals('H', text.charAt(0));  // First character
    /// assertEquals('e', text.charAt(1));  // Second character
    /// assertEquals('o', text.charAt(4));  // Last character
    ///
    /// UncachedString unicode = new UncachedString("café");
    /// assertEquals('c', unicode.charAt(0));
    /// assertEquals('é', unicode.charAt(3));  // Unicode character
    /// ```
    ///
    /// @param index the index of the character to return (0-based)
    /// @return the character at the specified index
    /// @throws IndexOutOfBoundsException if index is negative or >= length()
    /// @see String#charAt(int)
    /// @since 1.0.0
    @Override
    public char charAt(final int index) {
        return value.charAt(index);
    }

    /// Tests whether the wrapped string is empty.
    ///
    /// This method delegates to the underlying `String.isEmpty()` method to check
    /// if the string has zero length. This is equivalent to checking if
    /// `length() == 0`, but may be more efficient.
    ///
    /// ## Examples
    ///
    /// ```java
    /// UncachedString empty = new UncachedString("");
    /// assertTrue(empty.isEmpty());
    ///
    /// UncachedString notEmpty = new UncachedString("hello");
    /// assertFalse(notEmpty.isEmpty());
    ///
    /// UncachedString whitespace = new UncachedString(" ");
    /// assertFalse(whitespace.isEmpty());  // Whitespace is not empty
    /// ```
    ///
    /// @return true if the string has zero length, false otherwise
    /// @see String#isEmpty()
    /// @since 1.0.0
    @Override
    public boolean isEmpty() {
        return value.isEmpty();
    }

    /// Returns a subsequence of this string as a CharSequence.
    ///
    /// This method delegates to the underlying `String.subSequence()` method to
    /// extract a portion of the string content. The subsequence includes characters
    /// from the start index (inclusive) to the end index (exclusive).
    ///
    /// ## Examples
    ///
    /// ```java
    /// UncachedString text = new UncachedString("Hello, World!");
    ///
    /// CharSequence hello = text.subSequence(0, 5);
    /// assertEquals("Hello", hello.toString());
    ///
    /// CharSequence world = text.subSequence(7, 12);
    /// assertEquals("World", world.toString());
    ///
    /// CharSequence empty = text.subSequence(5, 5);
    /// assertEquals("", empty.toString());  // Empty subsequence
    /// ```
    ///
    /// @param start the start index (inclusive)
    /// @param end the end index (exclusive)
    /// @return a CharSequence containing the specified subsequence
    /// @throws IndexOutOfBoundsException if start < 0, end > length(), or start > end
    /// @see String#subSequence(int, int)
    /// @since 1.0.0
    @Override
    public @NonNull CharSequence subSequence(final int start, final int end) {
        return value.subSequence(start, end);
    }

    /// Returns a stream of int values representing the characters in this string.
    ///
    /// This method delegates to the underlying `String.chars()` method to provide
    /// a stream of the character codes (as int values) that make up this string.
    /// This is useful for functional-style character processing.
    ///
    /// ## Examples
    ///
    /// ```java
    /// UncachedString text = new UncachedString("ABC123");
    ///
    /// // Count letters
    /// long letterCount = text.chars()
    ///     .filter(Character::isLetter)
    ///     .count();
    /// assertEquals(3, letterCount);
    ///
    /// // Count digits
    /// long digitCount = text.chars()
    ///     .filter(Character::isDigit)
    ///     .count();
    /// assertEquals(3, digitCount);
    ///
    /// // Convert to uppercase
    /// String uppercase = text.chars()
    ///     .map(Character::toUpperCase)
    ///     .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
    ///     .toString();
    /// assertEquals("ABC123", uppercase);
    /// ```
    ///
    /// @return an IntStream of the character codes in this string
    /// @see String#chars()
    /// @see Character
    /// @since 1.0.0
    @Override
    public @NonNull IntStream chars() {
        return value.chars();
    }

    /// Returns a stream of Unicode code points from this string.
    ///
    /// This method delegates to the underlying `String.codePoints()` method to
    /// provide a stream of Unicode code point values. This correctly handles
    /// multi-byte Unicode characters and surrogate pairs.
    ///
    /// ## Examples
    ///
    /// ```java
    /// UncachedString text = new UncachedString("Hello 🌍");
    ///
    /// // Count code points (properly handles emoji)
    /// long codePointCount = text.codePoints().count();
    /// assertEquals(7, codePointCount);  // 6 regular chars + 1 emoji
    ///
    /// // Filter for emoji code points
    /// long emojiCount = text.codePoints()
    ///     .filter(cp -> cp > 0x1F000)  // Simple emoji range check
    ///     .count();
    /// assertEquals(1, emojiCount);
    ///
    /// // Process Unicode categories
    /// boolean hasLetters = text.codePoints()
    ///     .anyMatch(Character::isLetter);
    /// assertTrue(hasLetters);
    /// ```
    ///
    /// @return an IntStream of Unicode code points in this string
    /// @see String#codePoints()
    /// @see Character#isValidCodePoint(int)
    /// @since 1.0.0
    @Override
    public @NonNull IntStream codePoints() {
        return value.codePoints();
    }

    /// Compares this UncachedString lexicographically with another UncachedString.
    ///
    /// This method delegates to the underlying `String.compareTo()` method to provide
    /// lexicographical comparison of the wrapped string values. The comparison is
    /// case-sensitive and based on the Unicode values of the characters.
    ///
    /// ## Comparison Behavior
    ///
    /// - Returns negative integer if this string is lexicographically less than the other
    /// - Returns zero if the strings are lexicographically equal
    /// - Returns positive integer if this string is lexicographically greater than the other
    ///
    /// ## Examples
    ///
    /// ```java
    /// UncachedString alpha = new UncachedString("alpha");
    /// UncachedString beta = new UncachedString("beta");
    /// UncachedString alpha2 = new UncachedString("alpha");
    ///
    /// assertTrue(alpha.compareTo(beta) < 0);   // "alpha" < "beta"
    /// assertTrue(beta.compareTo(alpha) > 0);   // "beta" > "alpha"
    /// assertEquals(0, alpha.compareTo(alpha2)); // "alpha" == "alpha"
    ///
    /// // Sorting example
    /// List<UncachedString> strings = Arrays.asList(
    ///     new UncachedString("zebra"),
    ///     new UncachedString("alpha"),
    ///     new UncachedString("beta")
    /// );
    /// Collections.sort(strings);
    /// assertEquals("alpha", strings.get(0).toString());
    /// assertEquals("beta", strings.get(1).toString());
    /// assertEquals("zebra", strings.get(2).toString());
    /// ```
    ///
    /// @param o the UncachedString to compare against
    /// @return negative, zero, or positive integer as this string is less than, equal to, or greater than the specified string
    /// @see String#compareTo(String)
    /// @see Comparable
    /// @since 1.0.0
    @Override
    public int compareTo(final @NonNull UncachedString o) {
        return value.compareTo(o.value);
    }

    /// Tests this UncachedString for equality with another object.
    ///
    /// This method returns true if and only if the argument is not null, is an
    /// UncachedString object, and represents the same string value as this object.
    /// The comparison is performed using the wrapped string's equality semantics.
    ///
    /// ## Equality Contract
    ///
    /// This implementation satisfies the general contract of `Object.equals()`:
    /// - **Reflexive**: `x.equals(x)` returns true
    /// - **Symmetric**: `x.equals(y)` returns true if and only if `y.equals(x)` returns true
    /// - **Transitive**: If `x.equals(y)` and `y.equals(z)` both return true, then `x.equals(z)` returns true
    /// - **Consistent**: Multiple invocations return the same result (assuming no modification)
    /// - **Null handling**: `x.equals(null)` returns false
    ///
    /// ## Examples
    ///
    /// ```java
    /// UncachedString str1 = new UncachedString("hello");
    /// UncachedString str2 = new UncachedString("hello");
    /// UncachedString str3 = new UncachedString("world");
    /// String regularString = "hello";
    ///
    /// assertTrue(str1.equals(str2));     // Same content
    /// assertFalse(str1.equals(str3));    // Different content
    /// assertFalse(str1.equals(regularString)); // Different types
    /// assertFalse(str1.equals(null));    // Null safety
    ///
    /// // Object identity vs value equality
    /// assertNotSame(str1, str2);         // Different objects
    /// assertEquals(str1, str2);          // But equal values
    /// ```
    ///
    /// @param o the object to compare with this UncachedString
    /// @return true if the objects are equal, false otherwise
    /// @see Object#equals(Object)
    /// @see #hashCode()
    /// @since 1.0.0
    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UncachedString that = (UncachedString) o;
        return Objects.equals(value, that.value);
    }

    /// Returns a hash code value for this UncachedString.
    ///
    /// This method delegates to the underlying string's hash code computation,
    /// ensuring that two UncachedString objects with equal string values will
    /// have the same hash code. This satisfies the general contract of hashCode
    /// in relation to equals.
    ///
    /// ## Hash Code Contract
    ///
    /// - If two objects are equal according to `equals()`, they must have the same hash code
    /// - Hash codes should be consistent across multiple invocations (for immutable objects)
    /// - Hash codes should be reasonably distributed for good hash table performance
    ///
    /// ## Examples
    ///
    /// ```java
    /// UncachedString str1 = new UncachedString("test");
    /// UncachedString str2 = new UncachedString("test");
    /// UncachedString str3 = new UncachedString("different");
    ///
    /// assertEquals(str1.hashCode(), str2.hashCode()); // Equal objects have equal hash codes
    /// assertNotEquals(str1.hashCode(), str3.hashCode()); // Usually different for different values
    ///
    /// // Hash table usage
    /// Set<UncachedString> stringSet = new HashSet<>();
    /// stringSet.add_singleElement_returnsTrueAndUpdatesSize(str1);
    /// stringSet.add_singleElement_returnsTrueAndUpdatesSize(str2); // Won't be added again due to equality
    /// assertEquals(1, stringSet.size());
    ///
    /// // Map usage
    /// Map<UncachedString, String> stringMap = new HashMap<>();
    /// stringMap.put(str1, "value1");
    /// stringMap.put(str2, "value2"); // Overwrites previous value
    /// assertEquals("value2", stringMap.get(str1));
    /// ```
    ///
    /// @return a hash code value for this UncachedString
    /// @see Object#hashCode()
    /// @see #equals(Object)
    /// @see String#hashCode()
    /// @since 1.0.0
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    /// Returns the string representation of this UncachedString.
    ///
    /// This method returns the wrapped string value directly, providing seamless
    /// integration with APIs that expect string representations. The returned
    /// string is identical to the original string provided to the constructor.
    ///
    /// ## String Conversion
    ///
    /// The toString method enables transparent usage of UncachedString in contexts
    /// where string representation is needed, such as:
    /// - String concatenation operations
    /// - Logging and debugging output
    /// - API calls requiring string parameters
    /// - String formatting operations
    ///
    /// ## Examples
    ///
    /// ```java
    /// UncachedString text = new UncachedString("Hello, World!");
    /// assertEquals("Hello, World!", text.toString());
    ///
    /// // String concatenation
    /// String greeting = "Message: " + text.toString();
    /// assertEquals("Message: Hello, World!", greeting);
    ///
    /// // Logging integration
    /// System.out.println("Processing: " + text); // Automatically calls toString()
    ///
    /// // String formatting
    /// String formatted = String.format("Text: '%s'", text);
    /// assertEquals("Text: 'Hello, World!'", formatted);
    ///
    /// // Collection toString behavior
    /// List<UncachedString> strings = List.of(
    ///     new UncachedString("first"),
    ///     new UncachedString("second")
    /// );
    /// System.out.println(strings); // Uses toString() for each element
    /// ```
    ///
    /// @return the string value wrapped by this UncachedString
    /// @see Object#toString()
    /// @see String
    /// @since 1.0.0
    @Override
    public @NonNull String toString() {
        return value;
    }

    /**
     * Creates an instance of UncachedString with the value provided by the argument.
     * @param value The value of the uncached string instance.
     * @return An UncachedString instance with the supplied value.
     */
    public static UncachedString valueOf(final String value) {
        return new UncachedString(value);
    }
}
