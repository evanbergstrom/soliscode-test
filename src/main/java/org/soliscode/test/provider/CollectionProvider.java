package org.soliscode.test.provider;

import org.jspecify.annotations.NonNull;

import java.util.Collection;

/// Provides instances of collections and other iterable types for use in testing.
///
/// This interface extends `ObjectProvider` to add collection-specific functionality for creating
/// iterables with controlled element content. It provides methods for creating collections with
/// various characteristics such as empty, singleton, unique elements, and seed-based generation.
///
/// ## Purpose
///
/// `CollectionProvider` enables systematic testing of collection implementations by providing:
/// - Controlled element generation through an associated element provider
/// - Various collection sizes and configurations
/// - Deterministic content based on seed values
/// - Support for unique element constraints
///
/// ## Usage Examples
///
/// ### Basic Collection Creation
/// ```java
/// CollectionProvider<String, List<String>> provider =
///     CollectionProviders.arrayListProvider(new StringProvider());
///
/// // Create collections with different characteristics
/// List<String> empty = provider.emptyInstance();                    // []
/// List<String> single = provider.createSingleton("test");           // ["test"]
/// List<String> multiple = provider.createInstance(3);               // 3 string elements
/// ```
///
/// ### Element Provider Integration
/// ```java
/// ObjectProvider<Integer> elementProvider = new IntegerProvider();
/// CollectionProvider<Integer, Set<Integer>> setProvider =
///     CollectionProviders.hashSetProvider(elementProvider);
///
/// // Access the underlying element provider
/// ObjectProvider<Integer> elements = setProvider.elementProvider();
/// Integer maxElement = elements.createInstance(Integer.MAX_VALUE);
/// ```
///
/// ### Unique Element Collections
/// ```java
/// CollectionProvider<String, List<String>> provider =
///     CollectionProviders.arrayListProvider(new StringProvider());
///
/// // Create collections with guaranteed unique elements
/// List<String> unique5 = provider.createInstanceWithUniqueElements(5);
/// List<String> uniqueSeeded = provider.createInstanceWithUniqueElements(10, 42);
/// ```
///
/// ## Implementation Requirements
///
/// Implementations must ensure:
/// - Consistent behavior for the same seed values
/// - Element uniqueness when using unique element methods
/// - Proper delegation to the element provider for content generation
/// - Type safety and null safety throughout
///
/// @param <E> the element type contained within the iterable
/// @param <I> the specific iterable type being provided (e.g., `List<E>`, `Set<E>`)
/// @author evanbergstrom
/// @since 1.0.0
/// @see ObjectProvider
/// @see CollectionProviders
public interface CollectionProvider<E, I extends Iterable<E>> extends ObjectProvider<I> {

    /// Returns the element provider used to create elements for collection instances.
    ///
    /// This provider is responsible for generating the individual elements that populate
    /// the collections created by this CollectionProvider. It determines the characteristics
    /// of the elements such as their values, distribution, and uniqueness properties.
    ///
    /// ## Examples
    ///
    /// ```java
    /// CollectionProvider<Integer, List<Integer>> listProvider =
    ///     CollectionProviders.arrayListProvider(new IntegerProvider());
    ///
    /// ObjectProvider<Integer> elementProvider = listProvider.elementProvider();
    /// Integer element = elementProvider.createInstance(42);  // 42
    /// ```
    ///
    /// @return the ObjectProvider used for generating collection elements
    /// @complexity constant time
    @NonNull ObjectProvider<E> elementProvider();

    /// Creates an empty instance of the iterable type.
    ///
    /// This method provides a collection with no elements, which is essential for testing
    /// edge cases and boundary conditions in collection operations.
    ///
    /// ## Examples
    ///
    /// ```java
    /// CollectionProvider<String, List<String>> provider =
    ///     CollectionProviders.arrayListProvider(new StringProvider());
    ///
    /// List<String> empty = provider.emptyInstance();
    /// assertTrue(empty.isEmpty());
    /// assertEquals(0, empty.size());
    /// ```
    ///
    /// @return an empty instance of the iterable type
    /// @complexity depends on the collection implementation
    @NonNull I emptyInstance();

    /// Creates an iterable instance containing the same elements as the specified collection.
    ///
    /// This method allows conversion from any collection type to the target iterable type,
    /// preserving the element order where applicable. It's useful for testing collection
    /// constructors and copy operations.
    ///
    /// ## Examples
    ///
    /// ```java
    /// CollectionProvider<Integer, Set<Integer>> setProvider =
    ///     CollectionProviders.hashSetProvider(new IntegerProvider());
    ///
    /// List<Integer> sourceList = Arrays.asList(1, 2, 3, 2);  // Duplicates included
    /// Set<Integer> resultSet = setProvider.createInstance(sourceList);  // {1, 2, 3}
    /// ```
    ///
    /// @param c the source collection containing elements to copy
    /// @return an instance of the iterable containing the elements from the source collection
    /// @throws NullPointerException if the collection parameter is null
    /// @complexity depends on the collection implementation and source size
    @NonNull I createInstance(@NonNull Collection<E> c);

    /// Creates an iterable instance based on a seed value for deterministic content generation.
    ///
    /// This method generates collections with predictable content based on the seed value.
    /// Collections created with the same seed should have identical content and ordering
    /// (where applicable), enabling reproducible test scenarios.
    ///
    /// ## Deterministic Behavior
    ///
    /// - Same seed values produce identical collections
    /// - Different seeds produce different collections (content, size, or both)
    /// - Elements are generated using the associated element provider
    ///
    /// ## Examples
    ///
    /// ```java
    /// CollectionProvider<String, List<String>> provider =
    ///     CollectionProviders.arrayListProvider(new StringProvider());
    ///
    /// List<String> list1 = provider.createInstance(42);
    /// List<String> list2 = provider.createInstance(42);
    /// assertEquals(list1, list2);  // Same seed produces identical content
    /// ```
    ///
    /// @param seed the seed value used to determine collection content and size
    /// @return an instance of the iterable with seed-based content
    /// @complexity depends on the collection implementation and generated size
    @NonNull I createInstance(long seed);

    /// Creates an iterable instance containing a single element from the element provider.
    ///
    /// This method creates a singleton collection using the element provider's default
    /// instance, which is useful for testing single-element scenarios and edge cases.
    ///
    /// ## Examples
    ///
    /// ```java
    /// CollectionProvider<String, List<String>> provider =
    ///     CollectionProviders.arrayListProvider(new StringProvider());
    ///
    /// List<String> singleton = provider.createSingleton();  // [""] (empty string default)
    /// assertEquals(1, singleton.size());
    /// ```
    ///
    /// @return an instance of the iterable containing one element
    /// @complexity depends on the collection implementation
    @NonNull I createSingleton();

    /// Creates an iterable instance containing the specified single element.
    ///
    /// This method creates a singleton collection with the exact element provided,
    /// allowing precise control over the singleton content for testing purposes.
    ///
    /// ## Examples
    ///
    /// ```java
    /// CollectionProvider<Integer, Set<Integer>> setProvider =
    ///     CollectionProviders.hashSetProvider(new IntegerProvider());
    ///
    /// Set<Integer> singleton = setProvider.createSingleton(42);  // {42}
    /// assertTrue(singleton.contains(42));
    /// assertEquals(1, singleton.size());
    /// ```
    ///
    /// @param e the element to include in the singleton collection
    /// @return an instance of the iterable containing the specified element
    /// @complexity depends on the collection implementation
    @NonNull I createSingleton(E e);

    /// Creates an iterable instance containing all elements from the specified array.
    ///
    /// This method converts an array of elements into the target iterable type,
    /// preserving element order where applicable. It's useful for testing with
    /// known, fixed sets of elements.
    ///
    /// ## Examples
    ///
    /// ```java
    /// CollectionProvider<String, List<String>> provider =
    ///     CollectionProviders.arrayListProvider(new StringProvider());
    ///
    /// String[] array = {"a", "b", "c"};
    /// List<String> list = provider.createInstance(array);  // ["a", "b", "c"]
    /// assertEquals(Arrays.asList(array), list);
    /// ```
    ///
    /// @param elements the array of elements to include in the collection
    /// @return an instance of the iterable containing all array elements
    /// @throws NullPointerException if the elements array is null
    /// @complexity depends on the collection implementation and array length
    @NonNull I createInstance(@NonNull E[] elements);

    /// Creates an iterable instance with a default number of unique elements.
    ///
    /// This method generates a collection where all elements are guaranteed to be unique
    /// according to their `equals()` method. The exact number of elements and their values
    /// are determined by the implementation.
    ///
    /// ## Uniqueness Guarantee
    ///
    /// For any two elements `e1` and `e2` in the result:
    /// - `e1.equals(e2)` returns `false` (unless they are the same object)
    /// - No duplicate values based on element provider generation
    ///
    /// ## Examples
    ///
    /// ```java
    /// CollectionProvider<Integer, Set<Integer>> setProvider =
    ///     CollectionProviders.hashSetProvider(new IntegerProvider());
    ///
    /// Set<Integer> uniqueSet = setProvider.createInstanceWithUniqueElements();
    /// assertEquals(uniqueSet.size(), new HashSet<>(uniqueSet).size());  // No duplicates
    /// ```
    ///
    /// @return an instance of the iterable containing unique elements
    /// @complexity depends on the collection implementation and generated size
    @NonNull I createInstanceWithUniqueElements();

    /// Creates an iterable instance with the specified number of unique elements.
    ///
    /// This method generates a collection with exactly the requested number of elements,
    /// all guaranteed to be unique. Elements are generated using the element provider
    /// with sequential seed values to ensure uniqueness.
    ///
    /// ## Examples
    ///
    /// ```java
    /// CollectionProvider<String, List<String>> provider =
    ///     CollectionProviders.arrayListProvider(new StringProvider());
    ///
    /// List<String> unique5 = provider.createInstanceWithUniqueElements(5);
    /// assertEquals(5, unique5.size());
    /// assertEquals(5, new HashSet<>(unique5).size());  // All unique
    /// ```
    ///
    /// @param size the exact number of unique elements to include
    /// @return an instance of the iterable containing the specified number of unique elements
    /// @throws IllegalArgumentException if size is negative or exceeds element provider limits
    /// @complexity depends on the collection implementation and requested size
    @NonNull I createInstanceWithUniqueElements(int size);

    /// Creates an iterable instance with unique elements using the specified size and seed.
    ///
    /// This method generates a collection with the requested number of unique elements,
    /// using the seed value to determine the starting point for element generation.
    /// This enables reproducible unique element sets for testing.
    ///
    /// ## Deterministic Uniqueness
    ///
    /// - Same size and seed produce identical unique element sets
    /// - Elements start generation from the seed value
    /// - Uniqueness is guaranteed within the generated set
    ///
    /// ## Examples
    ///
    /// ```java
    /// CollectionProvider<Integer, List<Integer>> provider =
    ///     CollectionProviders.arrayListProvider(new IntegerProvider());
    ///
    /// List<Integer> unique1 = provider.createInstanceWithUniqueElements(3, 10);  // [10, 11, 12]
    /// List<Integer> unique2 = provider.createInstanceWithUniqueElements(3, 10);  // [10, 11, 12]
    /// assertEquals(unique1, unique2);  // Same seed produces same result
    /// ```
    ///
    /// @param size the exact number of unique elements to include
    /// @param seed the starting seed value for element generation
    /// @return an instance of the iterable containing unique elements starting from the seed
    /// @throws IllegalArgumentException if size is negative or exceeds element provider limits
    /// @complexity depends on the collection implementation and requested size
    @NonNull I createInstanceWithUniqueElements(int size, int seed);
}
