/*
 * Copyright 2024 Evan Bergstrom
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


/// Factory utility class for creating instances of object providers for standard Java types.
///
/// This utility class provides convenient factory methods for obtaining commonly used
/// object providers without requiring direct instantiation. It follows the static factory
/// pattern to provide a clean, centralized API for objectProvider creation.
///
/// ## Design Pattern
///
/// This class implements the **Static Factory Pattern** with the following benefits:
/// - **Descriptive Names**: Method names clearly indicate the type being provided
/// - **Caching Potential**: Future versions could cache instances for performance
/// - **Interface Stability**: Clients depend on stable factory methods, not constructors
/// - **Extensibility**: New objectProvider types can be added without breaking existing code
///
/// ## Usage Examples
///
/// ### Basic Provider Creation
/// ```java
/// // Create providers using factory methods
/// IntegerProvider intProvider = Providers.integerProvider();
/// StringProvider stringProvider = Providers.stringProvider();
/// DoubleProvider doubleProvider = Providers.doubleProvider();
///
/// // Use providers to generate test data
/// Integer value = intProvider.createInstance(42);           // 42
/// String text = stringProvider.createInstance(100);        // "100"
/// Double decimal = doubleProvider.createInstance(123);     // 123.0
/// ```
///
/// ### Parameterized Test Integration
/// ```java
/// @ParameterizedTest
/// @MethodSource("numericProviders")
/// void testNumericProviders(NumberProvider<? extends Number> objectProvider) {
///     Number min = objectProvider.minValue();
///     Number max = objectProvider.maxValue();
///     assertNotNull(min);
///     assertNotNull(max);
/// }
///
/// static Stream<NumberProvider<? extends Number>> numericProviders() {
///     return Stream.of(
///         Providers.integerProvider(),
///         Providers.longProvider(),
///         Providers.doubleProvider(),
///         Providers.floatProvider(),
///         Providers.shortProvider()
///     );
/// }
/// ```
///
/// ### Collection Provider Integration
/// ```java
/// // Use with collection providers
/// CollectionProvider<String, List<String>> listProvider =
///     CollectionProviders.arrayListProvider(Providers.stringProvider());
///
/// CollectionProvider<Integer, Set<Integer>> setProvider =
///     CollectionProviders.hashSetProvider(Providers.integerProvider());
/// ```
///
/// ## Thread Safety
///
/// All factory methods in this class are thread-safe and can be called concurrently
/// from multiple threads. The returned objectProvider instances are also thread-safe for
/// their instance creation methods.
///
/// ## Performance Notes
///
/// Factory methods create new objectProvider instances on each call. For high-frequency
/// usage scenarios, consider caching the returned providers to avoid repeated
/// object allocation.
///
/// @author evanbergstrom
/// @since 1.0
/// @see ObjectProvider
/// @see NumberProvider
/// @see CollectionProviders
public final class Providers {

    /// Private constructor to prevent instantiation of this utility class.
    private Providers() { }

    /// Creates a new objectProvider for `Integer` instances.
    ///
    /// The returned objectProvider supports the full range of 32-bit signed integers
    /// and implements `IntegerNumberProvider` for integer-specific functionality.
    ///
    /// ## Examples
    ///
    /// ```java
    /// IntegerProvider objectProvider = Providers.integerProvider();
    ///
    /// Integer zero = objectProvider.defaultInstance();        // 0
    /// Integer custom = objectProvider.createInstance(42);     // 42
    /// Integer max = objectProvider.maxValue();                // 2147483647
    /// Integer min = objectProvider.minValue();                // -2147483648
    /// ```
    ///
    /// @return a new IntegerProvider instance
    /// @complexity constant time
    /// @see IntegerProvider
    /// @see IntegerNumberProvider
    public static IntegerProvider integerProvider() {
        return new IntegerProvider();
    }

    /// Creates a new objectProvider for `Long` instances.
    ///
    /// The returned objectProvider supports the full range of 64-bit signed integers
    /// and implements `IntegerNumberProvider` for integer-specific functionality.
    ///
    /// ## Examples
    ///
    /// ```java
    /// LongProvider objectProvider = Providers.longProvider();
    ///
    /// Long zero = objectProvider.defaultInstance();           // 0L
    /// Long custom = objectProvider.createInstance(42);        // 42L
    /// Long max = objectProvider.maxValue();                   // 9223372036854775807L
    /// Long min = objectProvider.minValue();                   // -9223372036854775808L
    /// ```
    ///
    /// @return a new LongProvider instance
    /// @complexity constant time
    /// @see LongProvider
    /// @see IntegerNumberProvider
    public static LongProvider longProvider() {
        return new LongProvider();
    }

    /// Creates a new objectProvider for `Short` instances.
    ///
    /// The returned objectProvider supports the full range of 16-bit signed integers
    /// and implements `IntegerNumberProvider` for integer-specific functionality.
    ///
    /// ## Examples
    ///
    /// ```java
    /// ShortProvider objectProvider = Providers.shortProvider();
    ///
    /// Short zero = objectProvider.defaultInstance();          // (short) 0
    /// Short custom = objectProvider.createInstance(42);       // (short) 42
    /// Short max = objectProvider.maxValue();                  // 32767
    /// Short min = objectProvider.minValue();                  // -32768
    /// ```
    ///
    /// @return a new ShortProvider instance
    /// @complexity constant time
    /// @see ShortProvider
    /// @see IntegerNumberProvider
    public static ShortProvider shortProvider() {
        return new ShortProvider();
    }

    /// Creates a new objectProvider for `Double` instances.
    ///
    /// The returned objectProvider supports the full range of double-precision floating-point
    /// numbers including special IEEE 754 values, and implements `DoubleNumberProvider`
    /// for floating-point specific functionality.
    ///
    /// ## Examples
    ///
    /// ```java
    /// DoubleProvider objectProvider = Providers.doubleProvider();
    ///
    /// Double zero = objectProvider.defaultInstance();         // 0.0
    /// Double custom = objectProvider.createInstance(42);      // 42.0
    /// Double max = objectProvider.maxValue();                 // 1.7976931348623157E308
    /// Double min = objectProvider.minValue();                 // 4.9E-324
    /// Double pi = objectProvider.createValue(Math.PI);        // 3.141592653589793
    /// ```
    ///
    /// @return a new DoubleProvider instance
    /// @complexity constant time
    /// @see DoubleProvider
    /// @see DoubleNumberProvider
    public static DoubleProvider doubleProvider() {
        return new DoubleProvider();
    }

    /// Creates a new objectProvider for `Float` instances.
    ///
    /// The returned objectProvider supports the full range of single-precision floating-point
    /// numbers including special IEEE 754 values, and implements `DoubleNumberProvider`
    /// for floating-point specific functionality.
    ///
    /// ## Examples
    ///
    /// ```java
    /// FloatProvider objectProvider = Providers.floatProvider();
    ///
    /// Float zero = objectProvider.defaultInstance();          // 0.0f
    /// Float custom = objectProvider.createInstance(42);       // 42.0f
    /// Float max = objectProvider.maxValue();                  // 3.4028235E38f
    /// Float min = objectProvider.minValue();                  // 1.4E-45f
    /// ```
    ///
    /// @return a new FloatProvider instance
    /// @complexity constant time
    /// @see FloatProvider
    /// @see DoubleNumberProvider
    public static FloatProvider floatProvider() {
        return new FloatProvider();
    }

    /// Creates a new objectProvider for `String` instances.
    ///
    /// The returned objectProvider generates string representations of numeric seed values,
    /// providing predictable and deterministic string content for testing scenarios.
    ///
    /// ## Examples
    ///
    /// ```java
    /// StringProvider objectProvider = Providers.stringProvider();
    ///
    /// String empty = objectProvider.defaultInstance();        // ""
    /// String number = objectProvider.createInstance(42);      // "42"
    /// String negative = objectProvider.createInstance(-100);  // "-100"
    /// String copy = objectProvider.copyInstance("test");      // "test" (same instance)
    /// ```
    ///
    /// @return a new StringProvider instance
    /// @complexity constant time
    /// @see StringProvider
    /// @see ObjectProvider
    public static StringProvider stringProvider() {
        return new StringProvider();
    }
}
