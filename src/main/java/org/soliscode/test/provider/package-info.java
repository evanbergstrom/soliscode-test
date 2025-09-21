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

/// **Provider Framework for Test Instance Generation**
///
/// This package contains a comprehensive framework for generating instances of objects used in testing.
/// The provider pattern enables systematic creation of test data with predictable and controllable
/// characteristics, making tests more reliable and easier to understand.
///
/// ## Core Concepts
///
/// ### Object Providers
/// The foundation of the framework is the `ObjectProvider<T>` interface, which defines methods for
/// creating instances of type `T`. All providers implement this interface to ensure consistent
/// behavior across different types.
///
/// ### Specialized Providers
/// The framework includes specialized provider interfaces for specific categories:
///
/// - **`NumberProvider<T>`**: For numeric types with min/max value support
/// - **`IntegerNumberProvider<T>`**: For integer-based numeric types
/// - **`DoubleNumberProvider<T>`**: For floating-point numeric types
/// - **`CollectionProvider<E,I>`**: For collection types with element generation
/// - **`FunctionalProvider<T>`**: For functional interfaces and lambdas
///
/// ## Usage Patterns
///
/// ### Basic Instance Creation
/// ```java
/// // Create providers for different types
/// ObjectProvider<String> stringProvider = new StringProvider();
/// NumberProvider<Integer> intProvider = new IntegerProvider();
///
/// // Generate test instances
/// String defaultStr = stringProvider.defaultInstance();    // ""
/// String customStr = stringProvider.createInstance(42);    // "42"
/// Integer maxInt = intProvider.maxValue();                 // Integer.MAX_VALUE
/// ```
///
/// ### Collection Testing
/// ```java
/// CollectionProvider<String, List<String>> listProvider =
///     CollectionProviders.arrayListProvider(new StringProvider());
///
/// // Create collections with different characteristics
/// List<String> empty = listProvider.emptyInstance();           // []
/// List<String> single = listProvider.createSingleton("test");  // ["test"]
/// List<String> multiple = listProvider.createInstance(5);      // 5 unique strings
/// ```
///
/// ### Parameterized Testing Integration
/// ```java
/// @ParameterizedTest
/// @MethodSource("numberProviders")
/// void testNumericBoundaries(NumberProvider<? extends Number> provider) {
///     Number min = provider.minValue();
///     Number max = provider.maxValue();
///
///     assertTrue(min.doubleValue() <= max.doubleValue());
/// }
///
/// static Stream<NumberProvider<? extends Number>> numberProviders() {
///     return Stream.of(
///         new IntegerProvider(),
///         new LongProvider(),
///         new DoubleProvider()
///     );
/// }
/// ```
///
/// ### Controlled Test Data Generation
/// ```java
/// ObjectProvider<String> provider = new StringProvider();
///
/// // Generate predictable sequences for reproducible tests
/// List<String> equalInstances = provider.createEqualObjects(10);
/// List<String> uniqueInstances = provider.createUniqueInstances(10);
/// List<String> randomInstances = provider.createRandomInstances(10);
/// ```
///
/// ## Provider Types
///
/// ### Concrete Implementations
/// - **`StringProvider`**: Generates string instances from numeric seeds
/// - **`IntegerProvider`**: Creates Integer instances with full range support
/// - **`LongProvider`**: Creates Long instances with full range support
/// - **`DoubleProvider`**: Creates Double instances with floating-point support
/// - **`FloatProvider`**: Creates Float instances with floating-point support
/// - **`ShortProvider`**: Creates Short instances with range validation
///
/// ### Factory Classes
/// - **`Providers`**: Factory methods for common object providers
/// - **`CollectionProviders`**: Factory methods for collection providers
///
/// ## Design Principles
///
/// ### Predictability
/// Providers use seed-based generation to ensure reproducible test data. The same seed
/// will always produce the same instance, making tests deterministic.
///
/// ### Type Safety
/// Generic type parameters ensure compile-time type safety and prevent ClassCastException
/// at runtime.
///
/// ### Extensibility
/// The framework is designed for easy extension. Custom providers can be created by
/// implementing the appropriate provider interface.
///
/// ### Performance
/// Providers are designed for efficient instance creation with minimal overhead,
/// suitable for use in performance-critical test scenarios.
///
/// ## Implementation Notes
///
/// - All providers must handle null safety appropriately
/// - Seed-based methods should be deterministic and repeatable
/// - Boundary value methods should return mathematically correct limits
/// - Copy operations should preserve equality semantics
///
/// @author evanbergstrom
/// @since 1.0
/// @see ObjectProvider
/// @see NumberProvider
/// @see CollectionProvider
package org.soliscode.test.provider;
