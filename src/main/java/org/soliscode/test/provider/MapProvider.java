package org.soliscode.test.provider;

import org.jspecify.annotations.NonNull;

import java.util.Map;

/// Provides instances of `Map` implementations for use in testing.
///
/// This interface extends `ObjectProvider` to add_singleElement_returnsTrueAndUpdatesSize map-specific functionality for creating
/// maps with controlled key and value content. It provides methods for creating maps with
/// various configurations such as empty, singleton, and seed-based generation.
///
/// ## Purpose
///
/// `MapProvider` enables systematic testing of map implementations by providing:
/// - Controlled key and value generation through associated providers
/// - Various map sizes and configurations
/// - Deterministic content based on seed values
/// - Support for unique key constraints
///
/// ## Usage Examples
///
/// ### Basic Map Creation
/// ```java
/// MapProvider<String, Integer, HashMap<String, Integer>> provider =
///     MapProviders.hashMapProvider(new StringProvider(), new IntegerProvider());
///
/// // Create maps with different characteristics
/// Map<String, Integer> empty = provider.emptyInstance();                    // {}
/// Map<String, Integer> single = provider.createSingleton("test", 1);         // {"test"=1}
/// Map<String, Integer> multiple = provider.createInstance(3);                // 3 entry map
/// ```
///
/// ### Provider Integration
/// ```java
/// ObjectProvider<String> keyProvider = new StringProvider();
/// ObjectProvider<Integer> valueProvider = new IntegerProvider();
/// MapProvider<String, Integer, TreeMap<String, Integer>> treeMapProvider =
///     MapProviders.treeMapProvider(keyProvider, valueProvider);
///
/// // Access the underlying providers
/// ObjectProvider<String> keys = treeMapProvider.keyProvider();
/// ObjectProvider<Integer> values = treeMapProvider.valueProvider();
/// ```
///
/// ## Implementation Requirements
///
/// Implementations must ensure:
/// - Consistent behavior for the same seed values
/// - Key uniqueness when generating entries
/// - Proper delegation to key and value providers for content generation
/// - Type safety and null safety throughout
///
/// @param <K> the type of keys maintained by the map
/// @param <V> the type of mapped values
/// @param <M> the specific map type being provided (e.g., `HashMap<K, V>`, `TreeMap<K, V>`)
/// @author evanbergstrom
/// @since 1.0.0
/// @see ObjectProvider
/// @see MapProviders
public interface MapProvider<K, V, M extends Map<K, V>> extends ObjectProvider<M> {

    /// Returns the object provider used to create keys for map instances.
    ///
    /// This provider is responsible for generating the individual keys that populate
    /// the maps created by this MapProvider.
    ///
    /// @return the ObjectProvider used for generating map keys
    /// @complexity constant time
    @NonNull ObjectProvider<K> keyProvider();

    /// Returns the object provider used to create values for map instances.
    ///
    /// This provider is responsible for generating the individual values that populate
    /// the maps created by this MapProvider.
    ///
    /// @return the ObjectProvider used for generating map values
    /// @complexity constant time
    @NonNull ObjectProvider<V> valueProvider();

    /// Creates an empty instance of the map type.
    ///
    /// This method provides a map with no entries, which is essential for testing
    /// edge cases and boundary conditions in map operations.
    ///
    /// @return an empty instance of the map type
    /// @complexity depends on the map implementation
    @NonNull M emptyInstance();

    /// Creates a map instance containing the same entries as the specified map.
    ///
    /// This method allows conversion from any map type to the target map type,
    /// preserving the entry order where applicable.
    ///
    /// @param m the source map containing entries to copy
    /// @return an instance of the map containing the entries from the source map
    /// @throws NullPointerException if the map parameter is null
    /// @complexity depends on the map implementation and source size
    @NonNull M createInstance(@NonNull Map<K, V> m);

    /// Creates a map instance based on a seed value for deterministic content generation.
    ///
    /// This method generates maps with predictable content based on the seed value.
    /// Maps created with the same seed should have identical content and ordering
    /// (where applicable), enabling reproducible test scenarios.
    ///
    /// @param seed the seed value used to determine map content and size
    /// @return an instance of the map with seed-based content
    /// @complexity depends on the map implementation and generated size
    @NonNull M createInstance(long seed);

    /// Creates a map instance containing a single entry from the key and value providers.
    ///
    /// This method creates a singleton map using the key and value providers' default
    /// instances, which is useful for testing single-entry scenarios and edge cases.
    ///
    /// @return an instance of the map containing one entry
    /// @complexity depends on the map implementation
    @NonNull M createSingleton();

    /// Creates a map instance containing the specified single entry.
    ///
    /// This method creates a singleton map with the exact key and value provided,
    /// allowing precise control over the singleton content for testing purposes.
    ///
    /// @param key the key to include in the singleton map
    /// @param value the value to include in the singleton map
    /// @return an instance of the map containing the specified entry
    /// @complexity depends on the map implementation
    @NonNull M createSingleton(K key, V value);

    /// Creates a map instance with the specified number of entries.
    ///
    /// This method generates a map with exactly the requested number of entries.
    /// The keys generated for the map are guaranteed to be unique.
    ///
    /// @param size the exact number of entries to include
    /// @return an instance of the map containing the specified number of entries
    /// @throws IllegalArgumentException if size is negative or exceeds provider limits
    /// @complexity depends on the map implementation and requested size
    @NonNull M createInstance(int size);

    /// Creates a map instance with unique keys using the specified size and seed.
    ///
    /// This method generates a map with the requested number of entries,
    /// using the seed value to determine the starting point for key and value generation.
    ///
    /// @param size the exact number of entries to include
    /// @param seed the starting seed value for entry generation
    /// @return an instance of the map containing entries starting from the seed
    /// @throws IllegalArgumentException if size is negative or exceeds provider limits
    /// @complexity depends on the map implementation and requested size
    @NonNull M createInstance(int size, long seed);
}
