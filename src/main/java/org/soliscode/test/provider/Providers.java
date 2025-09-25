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

package org.soliscode.test.provider;

import org.jspecify.annotations.NonNull;

/// Factory utility class for creating instances of object providers for standard Java types.
///
/// This utility class provides convenient factory methods for obtaining commonly used
/// object providers without requiring direct instantiation. It follows the static factory
/// pattern to provide a clean, centralized API for provider creation.
///
/// ## Design Pattern
///
/// This class implements the **Static Factory Pattern** with the following benefits:
/// - **Descriptive Names**: Method names clearly indicate the type being provided
/// - **Caching Potential**: Future versions could cache instances for performance
/// - **Interface Stability**: Clients depend on stable factory methods, not constructors
/// - **Extensibility**: New provider types can be added without breaking existing code
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
/// void testNumericProviders(NumberProvider<? extends Number> provider) {
///     Number min = provider.minValue();
///     Number max = provider.maxValue();
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
/// from multiple threads. The returned provider instances are also thread-safe for
/// their instance creation methods.
///
/// ## Performance Notes
///
/// Factory methods create new provider instances on each call. For high-frequency
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

    /// Creates a new provider for `Integer` instances.
    ///
    /// The returned provider supports the full range of 32-bit signed integers
    /// and implements `IntegerNumberProvider` for integer-specific functionality.
    ///
    /// ## Examples
    ///
    /// ```java
    /// IntegerProvider provider = Providers.integerProvider();
    ///
    /// Integer zero = provider.defaultInstance();        // 0
    /// Integer custom = provider.createInstance(42);     // 42
    /// Integer max = provider.maxValue();                // 2147483647
    /// Integer min = provider.minValue();                // -2147483648
    /// ```
    ///
    /// @return a new IntegerProvider instance
    /// @complexity constant time
    /// @see IntegerProvider
    /// @see IntegerNumberProvider
    public static @NonNull IntegerProvider integerProvider() {
        return new IntegerProvider();
    }

    /// Creates a new provider for `Long` instances.
    ///
    /// The returned provider supports the full range of 64-bit signed integers
    /// and implements `IntegerNumberProvider` for integer-specific functionality.
    ///
    /// ## Examples
    ///
    /// ```java
    /// LongProvider provider = Providers.longProvider();
    ///
    /// Long zero = provider.defaultInstance();           // 0L
    /// Long custom = provider.createInstance(42);        // 42L
    /// Long max = provider.maxValue();                   // 9223372036854775807L
    /// Long min = provider.minValue();                   // -9223372036854775808L
    /// ```
    ///
    /// @return a new LongProvider instance
    /// @complexity constant time
    /// @see LongProvider
    /// @see IntegerNumberProvider
    public static @NonNull LongProvider longProvider() {
        return new LongProvider();
    }

    /// Creates a new provider for `Short` instances.
    ///
    /// The returned provider supports the full range of 16-bit signed integers
    /// and implements `IntegerNumberProvider` for integer-specific functionality.
    ///
    /// ## Examples
    ///
    /// ```java
    /// ShortProvider provider = Providers.shortProvider();
    ///
    /// Short zero = provider.defaultInstance();          // (short) 0
    /// Short custom = provider.createInstance(42);       // (short) 42
    /// Short max = provider.maxValue();                  // 32767
    /// Short min = provider.minValue();                  // -32768
    /// ```
    ///
    /// @return a new ShortProvider instance
    /// @complexity constant time
    /// @see ShortProvider
    /// @see IntegerNumberProvider
    public static @NonNull ShortProvider shortProvider() {
        return new ShortProvider();
    }

    /// Creates a new provider for `Double` instances.
    ///
    /// The returned provider supports the full range of double-precision floating-point
    /// numbers including special IEEE 754 values, and implements `DoubleNumberProvider`
    /// for floating-point specific functionality.
    ///
    /// ## Examples
    ///
    /// ```java
    /// DoubleProvider provider = Providers.doubleProvider();
    ///
    /// Double zero = provider.defaultInstance();         // 0.0
    /// Double custom = provider.createInstance(42);      // 42.0
    /// Double max = provider.maxValue();                 // 1.7976931348623157E308
    /// Double min = provider.minValue();                 // 4.9E-324
    /// Double pi = provider.createValue(Math.PI);        // 3.141592653589793
    /// ```
    ///
    /// @return a new DoubleProvider instance
    /// @complexity constant time
    /// @see DoubleProvider
    /// @see DoubleNumberProvider
    public static @NonNull DoubleProvider doubleProvider() {
        return new DoubleProvider();
    }

    /// Creates a new provider for `Float` instances.
    ///
    /// The returned provider supports the full range of single-precision floating-point
    /// numbers including special IEEE 754 values, and implements `DoubleNumberProvider`
    /// for floating-point specific functionality.
    ///
    /// ## Examples
    ///
    /// ```java
    /// FloatProvider provider = Providers.floatProvider();
    ///
    /// Float zero = provider.defaultInstance();          // 0.0f
    /// Float custom = provider.createInstance(42);       // 42.0f
    /// Float max = provider.maxValue();                  // 3.4028235E38f
    /// Float min = provider.minValue();                  // 1.4E-45f
    /// ```
    ///
    /// @return a new FloatProvider instance
    /// @complexity constant time
    /// @see FloatProvider
    /// @see DoubleNumberProvider
    public static @NonNull FloatProvider floatProvider() {
        return new FloatProvider();
    }

    /// Creates a new provider for `String` instances.
    ///
    /// The returned provider generates string representations of numeric seed values,
    /// providing predictable and deterministic string content for testing scenarios.
    ///
    /// ## Examples
    ///
    /// ```java
    /// StringProvider provider = Providers.stringProvider();
    ///
    /// String empty = provider.defaultInstance();        // ""
    /// String number = provider.createInstance(42);      // "42"
    /// String negative = provider.createInstance(-100);  // "-100"
    /// String copy = provider.copyInstance("test");      // "test" (same instance)
    /// ```
    ///
    /// @return a new StringProvider instance
    /// @complexity constant time
    /// @see StringProvider
    /// @see ObjectProvider
    public static @NonNull StringProvider stringProvider() {
        return new StringProvider();
    }
}
