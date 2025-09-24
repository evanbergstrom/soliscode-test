package org.soliscode.test.breakable;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.contract.CollectionMethods;

import java.util.*;

/// **Breakable Set Implementation for Testing**
///
/// This class extends `BreakableCollection` to provide a Set implementation that can be
/// programmatically broken for comprehensive testing scenarios. It maintains set semantics
/// (no duplicate elements) while allowing controlled introduction of various failure modes
/// and behavioral anomalies for testing purposes.
///
/// ## Core Functionality
///
/// As a Set implementation, this class enforces unique element constraints by default but
/// can be configured to break this fundamental set contract through the `SET_ALLOWS_DUPLICATE_ELEMENTS`
/// break. This allows testing of code that must handle corrupted or non-compliant Set instances.
///
/// ### Set Semantics
///
/// Under normal operation (no breaks active), BreakableSet maintains proper Set behavior:
/// - **Uniqueness**: Elements are added only if not already present
/// - **Add Operation**: Returns `true` only when a new element is added
/// - **AddAll Operation**: Processes each element individually through the add method
/// - **Duplicate Prevention**: Automatically prevents duplicate additions
///
/// ### Breakable Behavior
///
/// When the `SET_ALLOWS_DUPLICATE_ELEMENTS` break is active, the set violates its
/// fundamental contract by allowing duplicate elements to be added, creating an inconsistent
/// state that can be used to test error handling and validation logic.
///
/// ## Available Breaks
///
/// ### SET_ALLOWS_DUPLICATE_ELEMENTS
/// **Purpose**: Allows the set to accept duplicate elements, violating set semantics
/// **Effect**: The `add()` method will add elements even if they already exist
/// **Use Case**: Testing code that must handle corrupted or non-compliant Set implementations
///
/// ```java
/// BreakableSet<String> set = new BreakableSet.Builder<String>()
///     .withBreak(BreakableSet.SET_ALLOWS_DUPLICATE_ELEMENTS)
///     .build();
///
/// set.add("element");
/// set.add("element"); // This succeeds when break is active
/// assertEquals(2, set.size()); // Set now contains duplicates
/// ```
///
/// ## Builder Pattern
///
/// The class provides a comprehensive Builder for constructing BreakableSet instances with
/// specific configurations:
///
/// ### Basic Usage
/// ```java
/// // Create a standard breakable set
/// BreakableSet<Integer> set = new BreakableSet.Builder<Integer>()
///     .build();
///
/// // Create a set with specific breaks
/// BreakableSet<String> brokenSet = new BreakableSet.Builder<String>()
///     .withBreak(SET_ALLOWS_DUPLICATE_ELEMENTS)
///     .doesNotSupport(CollectionMethods.Remove)
///     .build();
/// ```
///
/// ### Advanced Configuration
/// ```java
/// // Create a set with custom backing collection and multiple breaks
/// Set<String> backingSet = new HashSet<>();
/// BreakableSet<String> customSet = new BreakableSet.Builder<>(backingSet)
///     .withBreak(SET_ALLOWS_DUPLICATE_ELEMENTS)
///     .permitsNulls(false)
///     .doesNotSupport(CollectionMethods.Clear)
///     .build();
/// ```
///
/// ## Testing Applications
///
/// ### Set Contract Validation
/// Test that code properly validates set contracts and handles violations:
///
/// ```java
/// @Test
/// void testSetValidation() {
///     BreakableSet<String> corruptedSet = new BreakableSet.Builder<String>()
///         .withBreak(SET_ALLOWS_DUPLICATE_ELEMENTS)
///         .build();
///
///     corruptedSet.add("test");
///     corruptedSet.add("test"); // Allows duplicate
///
///     // Test that validation logic catches the violation
///     assertThrows(IllegalStateException.class,
///         () -> validateSetIntegrity(corruptedSet));
/// }
/// ```
///
/// ### Error Handling Testing
/// Verify robust error handling when set operations fail:
///
/// ```java
/// @Test
/// void testErrorHandling() {
///     BreakableSet<Integer> restrictedSet = new BreakableSet.Builder<Integer>()
///         .doesNotSupport(CollectionMethods.Add)
///         .build();
///
///     // Test that service handles unsupported operations gracefully
///     assertThrows(UnsupportedOperationException.class,
///         () -> dataService.populateSet(restrictedSet, Arrays.asList(1, 2, 3)));
/// }
/// ```
///
/// ### Collection Framework Integration
/// Test integration with Java Collection Framework utilities:
///
/// ```java
/// @Test
/// void testCollectionIntegration() {
///     BreakableSet<String> set = new BreakableSet.Builder<String>()
///         .withBreak(SET_ALLOWS_DUPLICATE_ELEMENTS)
///         .build();
///
///     set.addAll(Arrays.asList("a", "b", "a")); // Adds duplicates
///
///     // Test that collection utilities handle non-standard behavior
///     testCollectionUtility(set);
/// }
/// ```
///
/// ## Design Considerations
///
/// ### Thread Safety
/// This class is not thread-safe. The underlying collection's thread safety characteristics
/// determine the overall thread safety behavior.
///
/// ### Performance
/// - **Add Operations**: O(1) average case for HashSet backing, O(n) for ArrayList
/// - **Contains Checks**: Performed before each add to maintain set semantics
/// - **Memory Overhead**: Minimal additional overhead beyond the backing collection
///
/// ### Backing Collection
/// The implementation uses a configurable backing collection (default: ArrayList) which
/// affects performance characteristics and iteration order.
///
/// @param <E> the type of elements maintained by this set
/// @author evanbergstrom
/// @since 1.0.0
/// @see BreakableCollection
/// @see Set
/// @see java.util.HashSet
public class BreakableSet<E> extends BreakableCollection<E> implements Set<E> {

    /// The backing collection that stores the set elements.
    private final Collection<E> set;

    /// Break that allows the set to violate uniqueness constraints by permitting duplicate elements.
    /// When this break is active, the set will accept duplicate elements, violating the fundamental
    /// Set contract that requires all elements to be unique.
    public static final Break SET_ALLOWS_DUPLICATE_ELEMENTS = new Break("Set allows addition of duplicate elements");

    /// Creates a new empty BreakableSet with default configuration.
    ///
    /// The set uses an ArrayList as the backing collection and has no breaks active.
    /// This constructor is equivalent to using the Builder with default settings.
    ///
    /// ```java
    /// BreakableSet<String> set = new BreakableSet<>();
    /// set.add("element");
    /// ```
    public BreakableSet() {
        this(new ArrayList<>(), new ArrayList<>(), 0);
    }

    /// Creates a new BreakableSet as a shallow copy of another BreakableSet.
    ///
    /// This constructor creates a new set that shares the same backing collection reference
    /// as the source set but starts with no breaks active. This is useful for creating
    /// variations of existing sets with different break configurations.
    ///
    /// ```java
    /// BreakableSet<Integer> original = new BreakableSet<>();
    /// original.addAll(Arrays.asList(1, 2, 3));
    ///
    /// BreakableSet<Integer> copy = new BreakableSet<>(original);
    /// // copy contains the same elements but no breaks
    /// ```
    ///
    /// @param other the BreakableSet to copy from
    /// @throws NullPointerException if other is null
    public BreakableSet(final @NonNull BreakableSet<E> other) {
        this(other, new ArrayList<>(), 0);
    }

    /// Creates a new BreakableSet using the specified collection as the backing store.
    ///
    /// This constructor allows you to specify the underlying collection implementation,
    /// which affects performance characteristics and iteration order. The set starts
    /// with no breaks active and enforces standard set semantics.
    ///
    /// ```java
    /// // Use HashSet for O(1) average case performance
    /// Set<String> backing = new HashSet<>();
    /// BreakableSet<String> set = new BreakableSet<>(backing);
    ///
    /// // Use LinkedHashSet for predictable iteration order
    /// Set<Integer> ordered = new LinkedHashSet<>();
    /// BreakableSet<Integer> orderedSet = new BreakableSet<>(ordered);
    /// ```
    ///
    /// @param collection the backing collection to use for element storage
    /// @throws NullPointerException if collection is null
    public BreakableSet(final @NonNull Collection<E> collection) {
        this(collection, new ArrayList<>(), 0);
    }

    /// Creates a new BreakableSet with full configuration control.
    ///
    /// This constructor provides complete control over the set's configuration, including
    /// the backing collection, active breaks, and behavioral characteristics. It's primarily
    /// used internally by the Builder but can be used directly for advanced scenarios.
    ///
    /// ```java
    /// Collection<String> backing = new HashSet<>();
    /// Collection<Break> breaks = Arrays.asList(SET_ALLOWS_DUPLICATE_ELEMENTS);
    /// int characteristics = Spliterator.ORDERED;
    ///
    /// BreakableSet<String> set = new BreakableSet<>(backing, breaks, characteristics);
    /// ```
    ///
    /// @param set the backing collection for element storage
    /// @param breaks the collection of breaks to activate
    /// @param characteristics the spliterator characteristics
    /// @throws NullPointerException if set or breaks is null
    public BreakableSet(final @NonNull Collection<E> set, final @NonNull Collection<Break> breaks,
                        final int characteristics) {
        super(set, breaks, characteristics);
        this.set = Objects.requireNonNull(set);
        setPermitsDuplicates(false);
    }

    /// Adds the specified element to this set if it is not already present, or if breaks allow duplicates.
    ///
    /// This method maintains proper Set semantics under normal operation by checking for element
    /// existence before adding. However, when the `SET_ALLOWS_DUPLICATE_ELEMENTS` break is active,
    /// it allows duplicate elements to be added, violating the Set contract.
    ///
    /// ### Normal Behavior
    /// - Returns `true` if the element was added (not already present)
    /// - Returns `false` if the element was already present
    /// - Maintains set uniqueness constraints
    ///
    /// ### With SET_ALLOWS_DUPLICATE_ELEMENTS Break
    /// - Always attempts to add the element, even if already present
    /// - Returns the result of the backing collection's add operation
    /// - May result in duplicate elements in the set
    ///
    /// ```java
    /// BreakableSet<String> normalSet = new BreakableSet<>();
    /// assertTrue(normalSet.add("test"));   // First add succeeds
    /// assertFalse(normalSet.add("test"));  // Duplicate add fails
    ///
    /// BreakableSet<String> brokenSet = new BreakableSet.Builder<String>()
    ///     .withBreak(SET_ALLOWS_DUPLICATE_ELEMENTS)
    ///     .build();
    /// assertTrue(brokenSet.add("test"));   // First add succeeds
    /// assertTrue(brokenSet.add("test"));   // Duplicate add also succeeds
    /// ```
    ///
    /// @param e the element to be added to this set
    /// @return `true` if this set did not already contain the element (or breaks allow duplicates)
    /// @throws UnsupportedOperationException if the add operation is not supported
    /// @throws ClassCastException if the class of the element prevents it from being added
    /// @throws NullPointerException if the element is null and this set does not permit nulls
    /// @throws IllegalArgumentException if some property of the element prevents it from being added
    @Override
    public boolean add(final E e) {
        if (supportsMethod(CollectionMethods.Add)) {
            if (!set.contains(e) || hasBreak(SET_ALLOWS_DUPLICATE_ELEMENTS)) {
                return set.add(e);
            } else {
                return false;
            }
        } else {
            throw new UnsupportedOperationException("Unsupported method: add(e)");
        }
    }

    /// Adds all elements in the specified collection to this set if they're not already present.
    ///
    /// This method processes each element individually through the `add` method, which means
    /// it respects the current break configuration. The operation maintains set semantics
    /// under normal conditions but may allow duplicates when the appropriate break is active.
    ///
    /// ### Behavior
    /// - Each element is processed individually through `add(E e)`
    /// - Returns `true` if any element was added to the set
    /// - Returns `false` if no elements were added (all were already present)
    /// - Respects the `SET_ALLOWS_DUPLICATE_ELEMENTS` break configuration
    ///
    /// ### Performance Note
    /// This implementation uses streaming to process elements, which may be less efficient
    /// than bulk operations but ensures consistent behavior with the `add` method's logic.
    ///
    /// ```java
    /// BreakableSet<Integer> set = new BreakableSet<>();
    /// set.add(1);
    ///
    /// Collection<Integer> newElements = Arrays.asList(2, 3, 1);
    /// boolean changed = set.addAll(newElements); // true - added 2 and 3, skipped duplicate 1
    /// assertEquals(3, set.size());
    ///
    /// // With breaks active
    /// BreakableSet<Integer> brokenSet = new BreakableSet.Builder<Integer>()
    ///     .withBreak(SET_ALLOWS_DUPLICATE_ELEMENTS)
    ///     .build();
    /// brokenSet.add(1);
    /// brokenSet.addAll(Arrays.asList(1, 2)); // Both elements added, including duplicate
    /// ```
    ///
    /// @param c collection containing elements to be added to this set
    /// @return `true` if this set changed as a result of the call
    /// @throws UnsupportedOperationException if the addAll operation is not supported
    /// @throws ClassCastException if any element prevents it from being added to this set
    /// @throws NullPointerException if the collection is null or contains null elements when nulls are not permitted
    /// @throws IllegalArgumentException if some property of an element prevents it from being added
    @Override
    public boolean addAll(@NonNull final Collection<? extends E> c) {
        if (supportsMethod(CollectionMethods.AddAll)) {
            return c.stream().map(this::add).reduce(false, Boolean::logicalOr);
        } else {
            throw new UnsupportedOperationException("Unsupported method: addAll(c)");
        }
    }

    /// Configures whether this set permits duplicate elements.
    ///
    /// For BreakableSet, this method always throws an exception when called with `true`
    /// because sets fundamentally cannot permit duplicates as part of their contract.
    /// The duplicate behavior is controlled through the `SET_ALLOWS_DUPLICATE_ELEMENTS`
    /// break mechanism instead.
    ///
    /// ### Design Rationale
    /// This method exists to override the parent class behavior and enforce that sets
    /// cannot be configured to permit duplicates through the normal configuration API.
    /// Duplicate handling is a behavioral break, not a configuration option.
    ///
    /// ```java
    /// BreakableSet<String> set = new BreakableSet<>();
    /// set.setPermitsDuplicates(false); // OK - explicitly disabling (already disabled)
    /// set.setPermitsDuplicates(true);  // Throws IllegalArgumentException
    ///
    /// // To allow duplicates, use the break mechanism instead:
    /// BreakableSet<String> brokenSet = new BreakableSet.Builder<String>()
    ///     .withBreak(SET_ALLOWS_DUPLICATE_ELEMENTS)
    ///     .build();
    /// ```
    ///
    /// @param permitsDuplicates whether to permit duplicate elements (must be false)
    /// @throws IllegalArgumentException if permitsDuplicates is true
    @Override
    public void setPermitsDuplicates(final boolean permitsDuplicates) {
        if (permitsDuplicates) {
            throw new IllegalArgumentException("BreakableSet cannot permit duplicates");
        }
        super.setPermitsDuplicates(permitsDuplicates);
    }

    /// **Builder for BreakableSet Construction**
    ///
    /// This builder class provides a fluent interface for constructing BreakableSet instances
    /// with specific configurations, breaks, and behavioral characteristics. The builder
    /// extends AbstractBuilder to inherit common configuration methods while providing
    /// set-specific functionality and constraints.
    ///
    /// ## Key Features
    ///
    /// - **Fluent Interface**: Method chaining for readable configuration
    /// - **Type Safety**: Generic type preservation throughout the building process
    /// - **Configuration Flexibility**: Support for custom backing collections and breaks
    /// - **Validation**: Ensures set-specific constraints are maintained
    /// - **Immutable Building**: Each build() call creates a new independent instance
    ///
    /// ## Builder Configuration
    ///
    /// ### Basic Set Creation
    /// ```java
    /// BreakableSet<String> set = new BreakableSet.Builder<String>()
    ///     .build();
    /// ```
    ///
    /// ### Set with Custom Backing Collection
    /// ```java
    /// Set<Integer> backing = new LinkedHashSet<>();
    /// BreakableSet<Integer> orderedSet = new BreakableSet.Builder<>(backing)
    ///     .build();
    /// ```
    ///
    /// ### Set with Breaks and Restrictions
    /// ```java
    /// BreakableSet<String> brokenSet = new BreakableSet.Builder<String>()
    ///     .withBreak(SET_ALLOWS_DUPLICATE_ELEMENTS)
    ///     .doesNotSupport(CollectionMethods.Remove)
    ///     .permitsNulls(false)
    ///     .build();
    /// ```
    ///
    /// ### Complex Configuration
    /// ```java
    /// BreakableSet<Object> complexSet = new BreakableSet.Builder<Object>()
    ///     .withBreaks(SET_ALLOWS_DUPLICATE_ELEMENTS, COLLECTION_THROWS_ON_EMPTY)
    ///     .doesNotSupport(CollectionMethods.Clear, CollectionMethods.RemoveAll)
    ///     .permitsIncompatibleTypes(true)
    ///     .withCharacteristics(Spliterator.ORDERED | Spliterator.DISTINCT)
    ///     .build();
    /// ```
    ///
    /// ## Set-Specific Constraints
    ///
    /// The BreakableSet.Builder enforces set-specific constraints:
    /// - **No Duplicate Permission**: `permitsDuplicates` is always set to false
    /// - **Set Semantics**: Maintains set behavior unless explicitly broken
    /// - **Backing Collection**: Can be customized but affects performance characteristics
    ///
    /// ## Inherited Configuration Methods
    ///
    /// From AbstractBuilder, the following methods are available:
    /// - `withBreak(Break...)` - Add behavioral breaks
    /// - `doesNotSupport(CollectionMethods...)` - Mark methods as unsupported
    /// - `permitsNulls(boolean)` - Configure null value handling
    /// - `permitsIncompatibleTypes(boolean)` - Configure type compatibility
    /// - `withCharacteristics(int)` - Set spliterator characteristics
    ///
    /// ## Performance Considerations
    ///
    /// - **Backing Collection Choice**: LinkedHashSet for order, HashSet for performance
    /// - **Break Activation**: Minimal runtime overhead when breaks are not used
    /// - **Builder Reuse**: Each build() creates a new instance, builder can be reused
    /// - **Memory Efficiency**: No unnecessary object creation during configuration
    ///
    /// @param <E> the type of elements maintained by the built set
    /// @see AbstractBuilder
    /// @see BreakableSet
    /// @since 1.0.0
    public static class Builder<E>
            extends AbstractBuilder<BreakableSet.Builder<E>, BreakableSet<E>, E> {

        /// The backing collection that will store the set elements.
        /// This reference is maintained to ensure proper builder-to-set configuration transfer.
        private final Collection<E> set;

        /// Creates a new builder with default configuration.
        ///
        /// This constructor initializes the builder with:
        /// - ArrayList as the backing collection (for simplicity, though not optimal for sets)
        /// - No breaks active
        /// - Standard set semantics (no duplicates permitted)
        /// - Default null and type compatibility settings
        ///
        /// **Note**: The default backing collection is ArrayList for historical reasons.
        /// For better set performance, consider using the constructor that accepts a Set.
        ///
        /// ```java
        /// BreakableSet.Builder<String> builder = new BreakableSet.Builder<>();
        /// BreakableSet<String> set = builder.build();
        /// ```
        public Builder() {
            // super(this.list = new HashSet<>()); <-- This will work once Flexible Constructors are available
            super(new ArrayList<>());
            this.set = (ArrayList<E>) elements;
            this.permitsDuplicates = false;
        }

        /// Creates a new builder with a custom backing set implementation.
        ///
        /// This constructor allows you to specify the underlying Set implementation,
        /// which affects performance characteristics, iteration order, and memory usage
        /// of the resulting BreakableSet. The builder starts with no breaks active
        /// and standard set configuration.
        ///
        /// ### Recommended Backing Collections
        /// - **HashSet**: Best general-purpose performance with O(1) average case operations
        /// - **LinkedHashSet**: Maintains insertion order with slightly higher memory overhead
        /// - **TreeSet**: Sorted iteration but O(log n) operations
        /// - **EnumSet**: Optimal for enum types
        ///
        /// ```java
        /// // High-performance set with no ordering guarantees
        /// Set<String> backing = new HashSet<>();
        /// BreakableSet<String> fastSet = new BreakableSet.Builder<>(backing)
        ///     .build();
        ///
        /// // Set with predictable iteration order
        /// Set<Integer> ordered = new LinkedHashSet<>();
        /// BreakableSet<Integer> orderedSet = new BreakableSet.Builder<>(ordered)
        ///     .build();
        /// ```
        ///
        /// @param elements the backing set implementation to use for element storage
        /// @throws NullPointerException if elements is null
        public Builder(final @NonNull Set<E> elements) {
            // super(this.list = Objects.requireNonNull(elements));  <-- This will work once Flexible Constructors
            // are available
            super(Objects.requireNonNull(elements));
            this.set = elements;
            this.permitsDuplicates = false;
        }

        /// Creates a new builder by copying configuration from another builder.
        ///
        /// This copy constructor creates a new builder that inherits all configuration
        /// from the source builder, including breaks, unsupported methods, and behavioral
        /// settings. The backing collection reference is shared, but the builder instances
        /// are independent.
        ///
        /// ### Use Cases
        /// - Creating variations of existing configurations
        /// - Template-based set creation
        /// - Configuration inheritance patterns
        ///
        /// ```java
        /// // Create a base configuration
        /// BreakableSet.Builder<String> baseBuilder = new BreakableSet.Builder<String>()
        ///     .permitsNulls(false)
        ///     .doesNotSupport(CollectionMethods.Clear);
        ///
        /// // Create variations based on the template
        /// BreakableSet<String> normalSet = new BreakableSet.Builder<>(baseBuilder)
        ///     .build();
        ///
        /// BreakableSet<String> brokenSet = new BreakableSet.Builder<>(baseBuilder)
        ///     .withBreak(SET_ALLOWS_DUPLICATE_ELEMENTS)
        ///     .build();
        /// ```
        ///
        /// @param other the builder to copy configuration from
        /// @throws NullPointerException if other is null
        public Builder(final BreakableSet.Builder<E> other) {
            super(other);
            this.set = other.set;
            this.permitsDuplicates = false;
        }

        /// Returns this builder instance for method chaining.
        ///
        /// This method is required by the AbstractBuilder pattern to enable fluent
        /// interface method chaining. It allows configuration methods to return the
        /// correct concrete builder type rather than the abstract base type.
        ///
        /// ```java
        /// BreakableSet<String> set = new BreakableSet.Builder<String>()
        ///     .withBreak(SET_ALLOWS_DUPLICATE_ELEMENTS)  // returns BreakableSet.Builder<String>
        ///     .permitsNulls(false)                       // returns BreakableSet.Builder<String>
        ///     .build();                                  // can call set-specific methods
        /// ```
        ///
        /// @return this builder instance
        @Override
        public BreakableSet.Builder<E> self() {
            return this;
        }

        /// Creates a copy of this builder with identical configuration.
        ///
        /// This method creates a new builder instance that has the same configuration
        /// as the current builder, including all breaks, restrictions, and settings.
        /// The copy is independent and can be modified without affecting the original.
        ///
        /// ### Use Cases
        /// - Creating configuration templates
        /// - Preserving builder state before modifications
        /// - Parallel configuration branches
        ///
        /// ```java
        /// BreakableSet.Builder<Integer> template = new BreakableSet.Builder<Integer>()
        ///     .permitsNulls(false)
        ///     .doesNotSupport(CollectionMethods.Remove);
        ///
        /// // Create independent copies for different configurations
        /// BreakableSet.Builder<Integer> variation1 = template.copy()
        ///     .withBreak(SET_ALLOWS_DUPLICATE_ELEMENTS);
        ///
        /// BreakableSet.Builder<Integer> variation2 = template.copy()
        ///     .doesNotSupport(CollectionMethods.Add);
        /// ```
        ///
        /// @return a new builder with identical configuration
        @Override
        public BreakableSet.Builder<E> copy() {
            return new BreakableSet.Builder<>(this);
        }

        /// Constructs a new BreakableSet instance with the current builder configuration.
        ///
        /// This method creates a new BreakableSet using all the settings configured in
        /// the builder, including breaks, method restrictions, behavioral settings, and
        /// the backing collection. Each call to build() creates a completely independent
        /// BreakableSet instance.
        ///
        /// ### Configuration Transfer
        /// The build process transfers the following settings:
        /// - **Breaks**: All active breaks are applied to the new set
        /// - **Method Support**: Unsupported methods are marked in the new set
        /// - **Behavioral Settings**: Null handling, type compatibility, etc.
        /// - **Characteristics**: Spliterator characteristics for stream operations
        ///
        /// ### Builder Reusability
        /// The builder can be reused after calling build() to create multiple sets
        /// with the same configuration or to create variations.
        ///
        /// ```java
        /// BreakableSet.Builder<String> builder = new BreakableSet.Builder<String>()
        ///     .withBreak(SET_ALLOWS_DUPLICATE_ELEMENTS)
        ///     .permitsNulls(false);
        ///
        /// // Create multiple independent sets with the same configuration
        /// BreakableSet<String> set1 = builder.build();
        /// BreakableSet<String> set2 = builder.build();
        ///
        /// // Sets are independent - changes to one don't affect the other
        /// set1.add("test");
        /// assertTrue(set2.isEmpty());
        /// ```
        ///
        /// @return a new BreakableSet instance configured according to this builder's settings
        public BreakableSet<E> build() {
            BreakableSet<E> broken = new BreakableSet<>(set, breaks, characteristics);
            broken.setPermitsNulls(permitsNulls);
            broken.setPermitsDuplicates(permitsDuplicates);
            broken.setPermitsIncompatibleTypes(permitsIncompatibleTypes);
            unsupportedMethods.forEach(broken::doesNotSupportMethod);
            return broken;
        }
    }
}
