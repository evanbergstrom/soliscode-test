package org.soliscode.test.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/// **Mutable Collection Factory Interface for Testing**
///
/// This utility interface provides convenient factory methods for creating mutable collections
/// in testing scenarios. It offers an alternative to the immutable collections provided by
/// Java's `List.of()` methods, specifically designed for test environments where mutable
/// collections with null support are required.
///
/// ## Core Purpose
///
/// The primary purpose of this interface is to bridge the gap between the convenience of
/// factory methods (like `List.of()`) and the flexibility requirements of testing scenarios.
/// While `List.of()` creates immutable collections that don't support null values or
/// modifications, this interface provides similar convenience with full mutability.
///
/// ### Key Differences from List.of()
///
/// | Feature | List.of() | UsesCollections.listOf() |
/// |---------|-----------|---------------------------|
/// | **Mutability** | Immutable | Mutable |
/// | **Null Support** | Throws NPE | Allows nulls |
/// | **Modification** | UnsupportedOperationException | Full modification support |
/// | **Type** | Unmodifiable List | ArrayList |
/// | **Use Case** | Production immutable data | Testing with modifications |
///
/// ## Implementation Strategy
///
/// All methods are implemented as `default` interface methods that return new `ArrayList`
/// instances. This design allows:
/// - **Mix-in capability**: Classes can implement this interface to gain collection factory methods
/// - **Zero overhead**: No additional object instantiation beyond the collections themselves
/// - **Type safety**: Full generic type support with compile-time type checking
/// - **Flexibility**: Easy extension and customization in implementing classes
///
/// ## Usage Patterns
///
/// ### Test Class Implementation
/// ```java
/// public class MyCollectionTest implements UsesCollections {
///
///     @Test
///     void testListModification() {
///         List<String> names = listOf("Alice", "Bob");
///         names.add("Charlie");  // This works - list is mutable
///         assertEquals(3, names.size());
///     }
///
///     @Test
///     void testNullHandling() {
///         List<String> withNulls = listOf("first", null, "third");
///         assertNull(withNulls.get(1));  // Nulls are supported
///     }
/// }
/// ```
///
/// ### Static Import Alternative
/// ```java
/// // Can be used without implementing the interface
/// UsesCollections factory = new UsesCollections() {};
/// List<Integer> numbers = factory.listOf(1, 2, 3, 4, 5);
/// ```
///
/// ### Testing Scenarios
/// ```java
/// @Test
/// void testCollectionModifications() {
///     // Create test data that can be modified during the test
///     List<String> initialData = listOf("item1", "item2");
///
///     // Test adding elements
///     myService.addItems(initialData, "item3");
///     assertEquals(3, initialData.size());
///
///     // Test removing elements
///     myService.removeFirstItem(initialData);
///     assertEquals(2, initialData.size());
///     assertEquals("item2", initialData.get(0));
/// }
/// ```
///
/// ### Null Value Testing
/// ```java
/// @Test
/// void testNullValueHandling() {
///     // Create lists with null values for testing edge cases
///     List<String> withNulls = listOf("valid", null, "also valid");
///
///     // Test service behavior with null values
///     int nullCount = myService.countNulls(withNulls);
///     assertEquals(1, nullCount);
/// }
/// ```
///
/// ## Method Overloading Strategy
///
/// The interface provides overloaded `listOf()` methods for 0-5 elements, covering the most
/// common testing scenarios. This approach:
/// - **Avoids varargs**: Eliminates array creation overhead and type safety warnings
/// - **Provides clarity**: Each method signature is explicit about the number of elements
/// - **Maintains performance**: No array allocation for small collections
/// - **Enables optimization**: JIT compiler can optimize fixed-arity methods more effectively
///
/// ## Design Rationale
///
/// ### Why Mutable Collections for Testing?
/// 1. **State Modification Testing**: Many tests need to verify that methods correctly modify collection state
/// 2. **Setup Flexibility**: Test setup often requires building collections incrementally
/// 3. **Edge Case Testing**: Testing with null values and boundary conditions
/// 4. **Legacy Compatibility**: Working with APIs that expect mutable collections
///
/// ### Why ArrayList Implementation?
/// 1. **Predictable Performance**: Well-known O(1) append and O(1) random access characteristics
/// 2. **Universal Compatibility**: Implements all major collection interfaces
/// 3. **Debugging Friendly**: Clear string representation and familiar behavior
/// 4. **Memory Efficient**: Resizable array with reasonable default capacity
///
/// ## Testing Applications
///
/// ### Unit Test Data Setup
/// Create test data structures that can be modified during test execution:
/// - Input parameter preparation
/// - Expected result construction
/// - Test state manipulation
///
/// ### Integration Testing
/// Build complex data structures for integration test scenarios:
/// - Multi-step workflow testing
/// - Data transformation validation
/// - State consistency verification
///
/// ### Collection Implementation Testing
/// Test custom collection implementations with known data sets:
/// - Custom List implementations
/// - Collection wrapper classes
/// - Collection behavior validation
///
/// ## Performance Considerations
///
/// - **Memory**: Each method creates a new ArrayList with default initial capacity
/// - **Time Complexity**: O(n) construction time where n is the number of elements
/// - **Allocation**: One ArrayList allocation per method call
/// - **Optimization**: JIT compiler can inline these methods for better performance
///
/// @author evanbergstrom
/// @since 1.0.0
/// @see ArrayList
/// @see List#of()
/// @see java.util.Collections
public interface UsesCollections {

    /// Creates a mutable empty list. This is a convenience method for creating small test
    /// lists no elements. Unlike List.of(), this method returns a mutable ArrayList that allows null value.
    ///
    /// @param <E> the element type
    /// @return a mutable ArrayList containing no elements
    default <E> List<E> listOf()   {
        return new ArrayList<>();
    }

    /// Creates a mutable list containing exactly one element. This is a convenience method for creating small test
    /// lists with known elements. Unlike List.of(), this method returns a mutable ArrayList that allows null value.
    ///
    /// @param <E> the element type
    /// @param e1 the first element
    /// @return a mutable ArrayList containing the one element.
    default <E> List<E> listOf(final E e1)   {
        List<E> l = new ArrayList<>();
        l.add(e1);
        return l;
    }

    /// Creates a mutable list containing exactly two elements. This is a convenience method for creating small test
    /// lists with known elements. Unlike List.of(), this method returns a mutable ArrayList that allows null value.
    ///
    /// @param <E> the element type
    /// @param e1 the first element
    /// @param e2 the second element
    /// @return a mutable ArrayList containing the two elements.
   default <E> List<E> listOf(final E e1, final E e2) {
        List<E> l = new ArrayList<>();
        l.add(e1);
        l.add(e2);
        return l;
    }

    /// Creates a mutable list containing exactly three elements. This is a convenience method for creating small test
    /// lists with known elements. Unlike List.of(), this method returns a mutable ArrayList that allows null value.
    ///
    /// @param <E> the element type
    /// @param e1 the first element
    /// @param e2 the second element
    /// @param e3 the third element
    /// @return a mutable ArrayList containing the three elements
    default <E> List<E> listOf(final E e1, final E e2, final E e3)   {
        List<E> l = new ArrayList<>();
        l.add(e1);
        l.add(e2);
        l.add(e3);
        return l;
    }

    /// Creates a mutable list containing exactly four elements. This is a convenience method for creating small test
    /// lists with known elements. Unlike List.of(), this method returns a mutable ArrayList that allows null values.
    ///
    /// @param <E> the element type
    /// @param e1 the first element
    /// @param e2 the second element
    /// @param e3 the third element
    /// @param e4 the fourth element
    /// @return a mutable ArrayList containing the four elements
    default <E> List<E> listOf(final E e1, final E e2, final E e3, final E e4)   {
        List<E> l = new ArrayList<>();
        l.add(e1);
        l.add(e2);
        l.add(e3);
        l.add(e4);
        return l;
    }

    /// Creates a mutable list containing exactly five elements. This is a convenience method for creating small test
    /// lists with known elements. Unlike List.of(), this method returns a mutable ArrayList that allows null values.
    ///
    /// @param <E> the element type
    /// @param e1 the first element
    /// @param e2 the second element
    /// @param e3 the third element
    /// @param e4 the fourth element
    /// @param e5 the five element
    /// @return a mutable ArrayList containing the five elements
    default <E> List<E> listOf(final E e1, final E e2, final E e3, final E e4, final E e5)   {
        List<E> l = new ArrayList<>();
        l.add(e1);
        l.add(e2);
        l.add(e3);
        l.add(e4);
        l.add(e5);
        return l;
    }

    /// Creates a mutable empty set. This is a convenience method for creating small test
    /// sets no elements. Unlike Set.of(), this method returns a mutable HashSet that allows null value.
    ///
    /// @param <E> the element type
    /// @return a mutable HashSet containing no elements
    default <E> Set<E> setOf()   {
        return new HashSet<>();
    }

    /// Creates a mutable set containing exactly one element. This is a convenience method for creating small test
    /// sets with known elements. Unlike Set.of(), this method returns a mutable HashSet that allows null value.
    ///
    /// @param <E> the element type
    /// @param e1 the first element
    /// @return a mutable HashSet containing the one element.
    default <E> Set<E> setOf(final E e1)   {
        Set<E> l = new HashSet<>();
        l.add(e1);
        return l;
    }

    /// Creates a mutable set containing exactly two elements. This is a convenience method for creating small test
    /// sets with known elements. Unlike Set.of(), this method returns a mutable HashSet that allows null value.
    ///
    /// @param <E> the element type
    /// @param e1 the first element
    /// @param e2 the second element
    /// @return a mutable HashSet containing the two elements.
    default <E> Set<E> setOf(final E e1, final E e2) {
        Set<E> l = new HashSet<>();
        l.add(e1);
        l.add(e2);
        return l;
    }

    /// Creates a mutable set containing exactly three elements. This is a convenience method for creating small test
    /// sets with known elements. Unlike Set.of(), this method returns a mutable HashSet that allows null value.
    ///
    /// @param <E> the element type
    /// @param e1 the first element
    /// @param e2 the second element
    /// @param e3 the third element
    /// @return a mutable HashSet containing the three elements
    default <E> Set<E> setOf(final E e1, final E e2, final E e3)   {
        Set<E> l = new HashSet<>();
        l.add(e1);
        l.add(e2);
        l.add(e3);
        return l;
    }

    /// Creates a mutable set containing exactly four elements. This is a convenience method for creating small test
    /// sets with known elements. Unlike Set.of(), this method returns a mutable HashSet that allows null values.
    ///
    /// @param <E> the element type
    /// @param e1 the first element
    /// @param e2 the second element
    /// @param e3 the third element
    /// @param e4 the fourth element
    /// @return a mutable HashSet containing the four elements
    default <E> Set<E> setOf(final E e1, final E e2, final E e3, final E e4)   {
        Set<E> l = new HashSet<>();
        l.add(e1);
        l.add(e2);
        l.add(e3);
        l.add(e4);
        return l;
    }

    /// Creates a mutable set containing exactly five elements. This is a convenience method for creating small test
    /// sets with known elements. Unlike Set.of(), this method returns a mutable HashSet that allows null values.
    ///
    /// @param <E> the element type
    /// @param e1 the first element
    /// @param e2 the second element
    /// @param e3 the third element
    /// @param e4 the fourth element
    /// @param e5 the five element
    /// @return a mutable HashSet containing the five elements
    default <E> Set<E> setOf(final E e1, final E e2, final E e3, final E e4, final E e5)   {
        Set<E> l = new HashSet<>();
        l.add(e1);
        l.add(e2);
        l.add(e3);
        l.add(e4);
        l.add(e5);
        return l;
    }

}
