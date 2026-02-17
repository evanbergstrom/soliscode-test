package org.soliscode.test.breakable;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.MethodStatus;
import org.soliscode.test.contract.collection.CollectionMethods;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
/// can be configured to break this fundamental set contract through the `ADD_RETURNS_TRUE_FOR_DUPLICATES`
/// break. This allows testing of code that must handle corrupted or non-compliant Set instances.
///
/// ### Set Semantics
///
/// Under normal operation (no breaks active), BreakableSet maintains proper Set behavior:
/// - **Uniqueness**: Elements are added only if not already present
/// - **Add Operation**: Returns `true` only when a new element is added
/// - **AddAll Operation**: Processes each element individually through the add_singleElement_returnsTrueAndUpdatesSize method
/// - **Duplicate Prevention**: Automatically prevents duplicate additions
///
/// ### Breakable Behavior
///
/// When the `ADD_RETURNS_TRUE_FOR_DUPLICATES` break is active, the set violates its
/// fundamental contract by allowing the add_singleElement_returnsTrueAndUpdatesSize() method to return true for duplicate elements,
/// creating an inconsistent state that can be used to test error handling and validation logic.
///
/// ## Available Breaks
///
/// ### Uniqueness Violation Breaks
///
/// #### ADD_RETURNS_TRUE_FOR_DUPLICATES
/// **Purpose**: Forces the set's add_singleElement_returnsTrueAndUpdatesSize() method to return true for duplicate elements
/// **Effect**: The `add_singleElement_returnsTrueAndUpdatesSize()` method returns true even when elements already exist in the set
/// **Use Case**: Testing code that must handle corrupted or non-compliant Set implementations
///
/// ```java
/// BreakableSet<String> set = new BreakableSet.Builder<String>()
///     .withBreak(BreakableSet.ADD_RETURNS_TRUE_FOR_DUPLICATES)
///     .build();
///
/// set.add_singleElement_returnsTrueAndUpdatesSize("element");
/// boolean result = set.add_singleElement_returnsTrueAndUpdatesSize("element"); // Returns true even for duplicate
/// assertTrue(result); // Break causes add_singleElement_returnsTrueAndUpdatesSize to return true for duplicates
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
///     .withBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES)
///     .doesNotSupport(CollectionMethods.Remove)
///     .build();
/// ```
///
/// ### Advanced Configuration
/// ```java
/// // Create a set with custom backing collection and multiple breaks
/// Set<String> backingSet = new HashSet<>();
/// BreakableSet<String> customSet = new BreakableSet.Builder<>(backingSet)
///     .withBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES)
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
///         .withBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES)
///         .build();
///
///     corruptedSet.add_singleElement_returnsTrueAndUpdatesSize("test");
///     corruptedSet.add_singleElement_returnsTrueAndUpdatesSize("test"); // Allows duplicate
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
///         .withBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES)
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
/// - **Contains Checks**: Performed before each add_singleElement_returnsTrueAndUpdatesSize to maintain set semantics
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
    private final Set<E> set;

    /// Break that allows the set to violate uniqueness constraints by permitting duplicate elements.
    /// When this break is active, the set will accept duplicate elements, violating the fundamental
    /// Set contract that requires all elements to be unique.
    ///
    /// **Purpose**: Forces `add_singleElement_returnsTrueAndUpdatesSize()` method to return true for duplicate elements even when they already exist
    /// **Effect**: Set semantics are violated, allowing multiple instances of the same element
    /// **Use Case**: Testing code that must handle corrupted or non-compliant Set implementations
    public static final Break ADD_RETURNS_TRUE_FOR_DUPLICATES = new Break("Add returns true for duplicates");

    /// Creates a new empty BreakableSet with default configuration.
    ///
    /// The set uses an ArrayList as the backing collection and has no breaks active.
    /// This constructor is equivalent to using the Builder with default settings.
    ///
    /// ```java
    /// BreakableSet<String> set = new BreakableSet<>();
    /// set.add_singleElement_returnsTrueAndUpdatesSize("element");
    /// ```
    public BreakableSet() {
        this(new HashSet<>(), new HashSet<>(), new HashMap<>(), DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS,
                DEFAULT_SAFETY, Object.class);
    }

    /// Creates a new BreakableSet as a shallow copy of another BreakableSet.
    ///
    /// This constructor creates a new set that shares the same backing collection reference
    /// as the source with the same breaks active.
    ///
    /// ```java
    /// BreakableSet<Integer> original = new BreakableSet<>();
    /// original.addAll(Arrays.asList(1, 2, 3));
    ///
    /// BreakableSet<Integer> copy = new BreakableSet<>(original);
    /// // copy contains the same elements and breaks
    /// ```
    ///
    /// @param other the BreakableSet to copy from
    /// @throws NullPointerException if other is null
    public BreakableSet(final @NonNull BreakableSet<E> other) {
        this(new HashSet<>(other.set), new HashSet<>(other.breaks()), new HashMap<>(other.methodStatuses()),
                other.characteristics(), other.permits(), other.isSafe(), other.compatibleType());
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
    public BreakableSet(final @NonNull Set<E> collection) {
        this(collection, new HashSet<>(), new HashMap<>(), DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS, DEFAULT_SAFETY,
                Object.class);
    }

    /// Creates a new BreakableSet with full configuration control.
    ///
    /// This constructor provides complete control over the set's configuration, including
    /// the backing collection, active breaks, and behavioral characteristics. It's primarily
    /// used internally by the Builder but can be used directly for advanced scenarios.
    ///
    /// ```java
    /// Collection<String> backing = new HashSet<>();
    /// Collection<Break> breaks = Arrays.asList(ADD_RETURNS_TRUE_FOR_DUPLICATES);
    /// int characteristics = Spliterator.ORDERED;
    ///
    /// BreakableSet<String> set = new BreakableSet<>(backing, breaks, characteristics);
    /// ```
    ///
    /// @param set the backing collection for element storage
    /// @param breaks the collection of breaks to activate
    /// @param methodStatuses the method status configuration.
    /// @param characteristics the spliterator characteristics
    /// @param permits         the flags that indicate what types of values are supported by the collection.
    /// @param isSafe          whether the collection is safe for concurrent access.
    /// @throws NullPointerException if set or breaks is null
    protected BreakableSet(final @NonNull Set<E> set, final @NonNull Set<Break> breaks,
                           final @NonNull Map<InterfaceMethod, MethodStatus> methodStatuses,
                           final int characteristics, final int permits, final boolean isSafe,
                           final Class<?> compatibleType) {
        super(this.set = set, breaks, methodStatuses, characteristics, permits & ~PERMITS_DUPLICATES, isSafe,
                compatibleType);
    }

    /// **Static Factory Method for Wrapping Sets with Breaks**
    ///
    /// Creates a new BreakableSet that wraps an existing Set and applies the specified
    /// set of breaks. This factory method provides a convenient way to create breakable
    /// sets from existing Set instances without using the builder pattern, while maintaining
    /// all Set-specific semantics and constraints.
    ///
    /// ## Purpose and Usage
    ///
    /// This method is particularly useful when you have an existing Set that you want to
    /// make breakable for testing purposes, especially when testing code that must handle
    /// Set contract violations or unexpected Set behaviors.
    ///
    /// ### Key Characteristics
    /// - **Direct Set Wrapping**: The provided Set becomes the backing store
    /// - **Set Semantics**: Maintains Set uniqueness constraints unless broken
    /// - **Break Application**: All specified breaks are immediately active
    /// - **Default Configuration**: Uses default spliterator characteristics (0)
    /// - **Type Safety**: Preserves generic type information from the source Set
    ///
    /// ## Usage Examples
    ///
    /// ### Basic Set Wrapping with Uniqueness Violation
    /// ```java
    /// Set<String> existingSet = new HashSet<>(Arrays.asList("a", "b", "c"));
    /// Set<Break> breaks = Set.of(BreakableSet.ADD_RETURNS_TRUE_FOR_DUPLICATES);
    ///
    /// BreakableSet<String> wrapper = BreakableSet.wrap(existingSet, breaks);
    ///
    /// // The wrapped set now violates uniqueness constraints
    /// assertTrue(wrapper.add_singleElement_returnsTrueAndUpdatesSize("a")); // Returns true despite duplicate
    /// // But actual Set still maintains uniqueness in backing store
    /// ```
    ///
    /// ### Multiple Breaks for Comprehensive Testing
    /// ```java
    /// Set<Integer> numbers = new TreeSet<>(Arrays.asList(1, 2, 3, 4, 5));
    /// Set<Break> testBreaks = Set.of(
    ///     BreakableSet.ADD_RETURNS_TRUE_FOR_DUPLICATES,
    ///     BreakableCollection.CONTAINS_ALWAYS_RETURNS_FALSE,
    ///     BreakableCollection.SIZE_ALWAYS_RETURNS_ZERO
    /// );
    ///
    /// BreakableSet<Integer> brokenWrapper = BreakableSet.wrap(numbers, testBreaks);
    ///
    /// assertTrue(brokenWrapper.add_singleElement_returnsTrueAndUpdatesSize(1));       // Returns true due to break
    /// assertFalse(brokenWrapper.contains(1)); // Returns false due to break
    /// assertEquals(0, brokenWrapper.size());  // Returns 0 due to break
    /// ```
    ///
    /// ### Set Contract Violation Testing
    /// ```java
    /// @Test
    /// void testSetContractViolationHandling() {
    ///     // Start with a proper Set
    ///     Set<String> validSet = new HashSet<>(Arrays.asList("item1", "item2"));
    ///
    ///     // Wrap with breaks that violate Set contracts
    ///     Set<Break> contractBreaks = Set.of(ADD_RETURNS_TRUE_FOR_DUPLICATES);
    ///     BreakableSet<String> corruptedSet = BreakableSet.wrap(validSet, contractBreaks);
    ///
    ///     // Test that deduplication algorithms handle corrupted sets
    ///     DeduplicationAlgorithm algorithm = new DeduplicationAlgorithm();
    ///     Set<String> result = algorithm.deduplicate(corruptedSet);
    ///
    ///     // Verify algorithm produces truly unique results despite input corruption
    ///     assertEquals(result.size(), new HashSet<>(result).size());
    /// }
    /// ```
    ///
    /// ## Behavior and Characteristics
    ///
    /// ### Set Relationship
    /// - **Shared Reference**: The wrapped Set shares the same backing store as the original
    /// - **Modifications Visible**: Changes made through either reference are visible to both
    /// - **Break Isolation**: Breaks only affect operations through the BreakableSet wrapper
    /// - **Uniqueness Preservation**: Underlying Set still maintains uniqueness unless explicitly broken
    ///
    /// ### Default Settings
    /// The wrap method applies these default configurations:
    /// - **Permits Nulls**: `true` (allows null elements if underlying Set supports them)
    /// - **Permits Duplicates**: `false` (enforces Set semantics unless broken)
    /// - **Permits Incompatible Types**: `true` (allows type mixing)
    /// - **Spliterator Characteristics**: `0` (no special characteristics)
    ///
    /// ### Break Activation
    /// All breaks in the provided set are immediately active and will affect subsequent
    /// operations on the wrapped Set according to their specific behaviors, particularly
    /// the ADD_RETURNS_TRUE_FOR_DUPLICATES break which can simulate Set contract violations.
    ///
    /// ## Comparison with Builder Pattern
    ///
    /// ### When to Use wrap() vs Builder
    ///
    /// **Use wrap() when:**
    /// - You have an existing Set to make breakable
    /// - You need quick break application without configuration
    /// - You want to preserve the exact Set instance and its characteristics
    /// - You're writing simple Set contract violation tests
    /// - You need to test code with specific Set implementations (TreeSet, LinkedHashSet, etc.)
    ///
    /// **Use Builder when:**
    /// - You need fine-grained configuration control
    /// - You want to set custom characteristics or constraints
    /// - You're building Sets from scratch with specific properties
    /// - You need method chaining for complex setups
    ///
    /// ### Example Comparison
    /// ```java
    /// // Using wrap() - preserves existing Set type and characteristics
    /// TreeSet<String> orderedSet = new TreeSet<>(Arrays.asList("c", "a", "b"));
    /// BreakableSet<String> wrapped = BreakableSet.wrap(
    ///     orderedSet, Set.of(ADD_RETURNS_TRUE_FOR_DUPLICATES)
    /// );
    /// // Maintains TreeSet ordering and characteristics
    ///
    /// // Using Builder - creates new Set with default backing store
    /// BreakableSet<String> built = new BreakableSet.Builder<String>()
    ///     .addElements("c", "a", "b")
    ///     .withBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES)
    ///     .doesNotPermitNulls()
    ///     .build();
    /// // Uses HashSet backing store by default
    /// ```
    ///
    /// ## Set-Specific Considerations
    ///
    /// ### Uniqueness Contract
    /// The wrap method preserves the underlying Set's uniqueness guarantees unless
    /// specifically broken by the ADD_RETURNS_TRUE_FOR_DUPLICATES break. This allows
    /// testing of:
    /// - Code that assumes Set uniqueness
    /// - Algorithms that depend on Set contract compliance
    /// - Error handling when Sets appear to accept duplicates
    ///
    /// ### Set Implementation Preservation
    /// The wrapped BreakableSet preserves the characteristics of the original Set:
    /// - **HashSet**: Fast access, no ordering
    /// - **TreeSet**: Sorted order, comparator-based
    /// - **LinkedHashSet**: Insertion order preservation
    /// - **EnumSet**: Enum-specific optimizations
    ///
    /// ### Performance Characteristics
    /// The performance of wrapped operations depends on the underlying Set implementation:
    /// ```java
    /// // Fast O(1) operations with HashSet
    /// BreakableSet<String> fastSet = BreakableSet.wrap(new HashSet<>(), breaks);
    ///
    /// // Sorted O(log n) operations with TreeSet
    /// BreakableSet<String> sortedSet = BreakableSet.wrap(new TreeSet<>(), breaks);
    /// ```
    ///
    /// ## Design Considerations
    ///
    /// ### Thread Safety
    /// The thread safety of the wrapped BreakableSet depends entirely on the
    /// thread safety of the underlying Set. No additional synchronization is provided.
    /// - **HashSet**: Not thread-safe
    /// - **Collections.synchronizedSet()**: Thread-safe if properly synchronized
    /// - **ConcurrentHashMap.newKeySet()**: Thread-safe for concurrent access
    ///
    /// ### Memory Efficiency
    /// This method is memory-efficient as it doesn't copy the Set elements,
    /// only creates a wrapper with break functionality while preserving the
    /// original Set's memory characteristics.
    ///
    /// ### Type Preservation
    /// The method preserves the generic type information and Set implementation
    /// specifics, ensuring type safety and performance characteristics are
    /// maintained throughout the wrapping process.
    ///
    /// @param <E>             the type of elements in the Set
    /// @param set             the existing Set to wrap with breakable functionality
    /// @param breaks          the set of breaks to apply to the wrapped Set
    /// @param methodStatuses  the initial method statuses configuration
    /// @param characteristics the initial spliterator characteristics
    /// @param permits         the initial permits configuration for the Set
    /// @param isSafe          whether the wrapped Set should be thread-safe
    /// @param compatibleType  the element type that the Set supports
    /// @return a new BreakableSet that wraps the provided Set with the specified breaks
    /// @throws NullPointerException if set or breaks is null
    /// @since 1.0.0
    /// @see BreakableSet.Builder
    /// @see BreakableSet#BreakableSet(Set, Set, Map, int, int, boolean, Class)
    /// @see BreakableSet#ADD_RETURNS_TRUE_FOR_DUPLICATES
    public static <E> BreakableSet<E> wrap(final @NonNull Set<E> set, final @NonNull Set<Break> breaks,
            final Map<InterfaceMethod, MethodStatus> methodStatuses, final int characteristics, final int permits,
            final boolean isSafe, final Class<?> compatibleType) {
        return new BreakableSet<>(set, breaks, methodStatuses, characteristics, permits, isSafe, compatibleType);
    }

    /// Adds the specified element to this set if it is not already present, or if breaks allow duplicates.
    ///
    /// This method maintains proper Set semantics under normal operation by checking for element
    /// existence before adding. However, when the `ADD_RETURNS_TRUE_FOR_DUPLICATES` break is active,
    /// it allows duplicate elements to be added, violating the Set contract.
    ///
    /// ### Normal Behavior
    /// - Returns `true` if the element was added (not already present)
    /// - Returns `false` if the element was already present
    /// - Maintains set uniqueness constraints
    ///
    /// ### With ADD_RETURNS_TRUE_FOR_DUPLICATES Break
    /// - Always attempts to add_singleElement_returnsTrueAndUpdatesSize the element, even if already present
    /// - Returns the result of the backing collection's add_singleElement_returnsTrueAndUpdatesSize operation
    /// - May result in duplicate elements in the set
    ///
    /// ```java
    /// BreakableSet<String> normalSet = new BreakableSet<>();
    /// assertTrue(normalSet.add_singleElement_returnsTrueAndUpdatesSize("test"));   // First add_singleElement_returnsTrueAndUpdatesSize succeeds
    /// assertFalse(normalSet.add_singleElement_returnsTrueAndUpdatesSize("test"));  // Duplicate add_singleElement_returnsTrueAndUpdatesSize fails
    ///
    /// BreakableSet<String> brokenSet = new BreakableSet.Builder<String>()
    ///     .withBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES)
    ///     .build();
    /// assertTrue(brokenSet.add_singleElement_returnsTrueAndUpdatesSize("test"));   // First add_singleElement_returnsTrueAndUpdatesSize succeeds
    /// assertTrue(brokenSet.add_singleElement_returnsTrueAndUpdatesSize("test"));   // Duplicate add_singleElement_returnsTrueAndUpdatesSize also succeeds
    /// ```
    ///
    /// @param e the element to be added to this set
    /// @return `true` if this set did not already contain the element (or breaks allow duplicates)
    /// @throws UnsupportedOperationException if the add_singleElement_returnsTrueAndUpdatesSize operation is not supported
    /// @throws ClassCastException if the class of the element prevents it from being added
    /// @throws NullPointerException if the element is null and this set does not permit nulls
    /// @throws IllegalArgumentException if some property of the element prevents it from being added
    @Override
    public boolean add(final E e) {
        if (supportsMethod(CollectionMethods.ADD)) {
            if (set.contains(e) && hasBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES)) {
                return true;
            } else {
                return set.add(e);
            }
        } else {
            throw new UnsupportedOperationException("Unsupported method: add(E)");
        }
    }

    /// Adds all elements in the specified collection to this set if they're not already present.
    ///
    /// This method processes each element individually through the `add_singleElement_returnsTrueAndUpdatesSize` method, which means
    /// it respects the current break configuration. The operation maintains set semantics
    /// under normal conditions but may allow duplicates when the appropriate break is active.
    ///
    /// ### Behavior
    /// - Each element is processed individually through `add_singleElement_returnsTrueAndUpdatesSize(E e)`
    /// - Returns `true` if any element was added to the set
    /// - Returns `false` if no elements were added (all were already present)
    /// - Respects the `ADD_RETURNS_TRUE_FOR_DUPLICATES` break configuration
    ///
    /// ### Performance Note
    /// This implementation uses streaming to process elements, which may be less efficient
    /// than bulk operations but ensures consistent behavior with the `add_singleElement_returnsTrueAndUpdatesSize` method's logic.
    ///
    /// ```java
    /// BreakableSet<Integer> set = new BreakableSet<>();
    /// set.add_singleElement_returnsTrueAndUpdatesSize(1);
    ///
    /// Collection<Integer> newElements = Arrays.asList(2, 3, 1);
    /// boolean changed = set.addAll(newElements); // true - added 2 and 3, skipped duplicate 1
    /// assertEquals(3, set.size());
    ///
    /// // With breaks active
    /// BreakableSet<Integer> brokenSet = new BreakableSet.Builder<Integer>()
    ///     .withBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES)
    ///     .build();
    /// brokenSet.add_singleElement_returnsTrueAndUpdatesSize(1);
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
        if (supportsMethod(CollectionMethods.ADD_ALL)) {
            return c.stream().map(this::add).reduce(false, Boolean::logicalOr);
        } else {
            throw new UnsupportedOperationException("Unsupported method: addAll(c)");
        }
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
    ///     .withBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES)
    ///     .doesNotSupport(CollectionMethods.Remove)
    ///     .permitsNulls(false)
    ///     .build();
    /// ```
    ///
    /// ### Complex Configuration
    /// ```java
    /// BreakableSet<Object> complexSet = new BreakableSet.Builder<Object>()
    ///     .withBreaks(ADD_RETURNS_TRUE_FOR_DUPLICATES, COLLECTION_THROWS_ON_EMPTY)
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
            super();
            doesNotPermitDuplicates();
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
        public Builder(final @NonNull Collection<E> elements) {
            super(elements);
            doesNotPermitDuplicates();
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
        ///     .withBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES)
        ///     .build();
        /// ```
        ///
        /// @param other the builder to copy configuration from
        /// @throws NullPointerException if other is null
        public Builder(final BreakableSet.Builder<E> other) {
            super(other);
            doesNotPermitDuplicates();
        }

        /// Returns this builder instance for method chaining.
        ///
        /// This method is required by the AbstractBuilder pattern to enable fluent
        /// interface method chaining. It allows configuration methods to return the
        /// correct concrete builder type rather than the abstract base type.
        ///
        /// ```java
        /// BreakableSet<String> set = new BreakableSet.Builder<String>()
        ///     .withBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES)  // returns BreakableSet.Builder<String>
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
        ///     .withBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES);
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
        ///     .withBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES)
        ///     .permitsNulls(false);
        ///
        /// // Create multiple independent sets with the same configuration
        /// BreakableSet<String> set1 = builder.build();
        /// BreakableSet<String> set2 = builder.build();
        ///
        /// // Sets are independent - changes to one don't affect the other
        /// set1.add_singleElement_returnsTrueAndUpdatesSize("test");
        /// assertTrue(set2.isEmpty());
        /// ```
        ///
        /// @return a new BreakableSet instance configured according to this builder's settings
        public BreakableSet<E> build() {
            return new BreakableSet<>(new HashSet<>(elements()), new HashSet<>(breaks()),
                    new HashMap<>(methodStatuses()), characteristics(), permits(), isSafe(), compatibleType());
        }
    }
}
