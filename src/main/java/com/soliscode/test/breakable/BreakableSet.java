package com.soliscode.test.breakable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.MethodStatus;
import com.soliscode.test.contract.collection.CollectionMethods;

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
/// - **AddAll Operation**: Goes through [BreakableCollection#addAll(Collection)], so the `ADD_ALL_*` breaks apply
/// - **Duplicate Prevention**: Automatically prevents duplicate additions
///
/// ### Breakable Behavior
///
/// When the `ADD_RETURNS_TRUE_FOR_DUPLICATES` break is active, the set violates its
/// fundamental contract by allowing the add() method to return true for duplicate elements,
/// while the set itself is left unchanged. The inherited [BreakableCollection] breaks apply as well.
///
/// ## Available Breaks
///
/// ### Uniqueness Violation Breaks
///
/// #### ADD_RETURNS_TRUE_FOR_DUPLICATES
/// **Purpose**: Forces the set's add() method to return true for duplicate elements
/// **Effect**: The `add()` method returns true even when elements already exist in the set; the set is not changed
/// **Use Case**: Testing code that must handle corrupted or non-compliant Set implementations
///
/// ```java
/// BreakableSet<String> set = new BreakableSet.Builder<String>()
///     .addBreak(BreakableSet.ADD_RETURNS_TRUE_FOR_DUPLICATES)
///     .build();
///
/// set.add("element");
/// boolean result = set.add("element"); // Returns true even for duplicate
/// assertTrue(result); // Break causes add to return true for duplicates
/// assertEquals(1, set.size()); // but the set still holds one copy
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
///     .addBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES)
///     .doesNotSupportMethod(CollectionMethods.REMOVE)
///     .build();
/// ```
///
/// ### Advanced Configuration
/// ```java
/// // Create a set with initial elements, a break and restrictions
/// BreakableSet<String> customSet = new BreakableSet.Builder<>(List.of("a", "b"))
///     .addBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES)
///     .doesNotPermitNulls()
///     .doesNotSupportMethod(CollectionMethods.CLEAR)
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
///         .addBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES)
///         .build();
///
///     corruptedSet.add("test");
///     assertTrue(corruptedSet.add("test")); // Reports a change for a duplicate
///     assertEquals(1, corruptedSet.size()); // but holds the element once
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
///         .doesNotSupportMethod(CollectionMethods.ADD)
///         .build();
///
///     assertThrows(UnsupportedOperationException.class, () -> restrictedSet.add(1));
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
///         .addBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES)
///         .build();
///
///     assertTrue(set.addAll(Arrays.asList("a", "b", "a")));
///     assertEquals(2, set.size()); // addAll keeps one copy; ADD_RETURNS_TRUE_FOR_DUPLICATES does not change it
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
/// - **Add Operations**: those of the backing set (O(1) average case for the default `HashSet`)
/// - **Contains Checks**: Performed before each add to maintain set semantics
/// - **Memory Overhead**: Minimal additional overhead beyond the backing collection
///
/// ### Backing Collection
/// The builder and the no-argument constructor back the set with a `HashSet`; the
/// [BreakableSet(Set)][#BreakableSet(Set)] constructor and `wrap` use the set they are given, which decides the
/// iteration order.
///
/// @param <E> the type of elements maintained by this set
/// @author evanbergstrom
/// @since 1.0.0
/// @see BreakableCollection
/// @see Set
/// @see java.util.HashSet
public class BreakableSet<E> extends BreakableCollection<E> implements Set<E> {

    /// Break that makes `add` report a change for an element the set already holds.
    /// When this break is active, `add` returns `true` for a duplicate element without adding it: the set still
    /// holds one copy, so the return value disagrees with the Set contract.
    ///
    /// **Purpose**: Forces `add()` method to return true for duplicate elements even when they already exist
    /// **Effect**: `add()` returns `true` for an element already present; the set is not changed
    /// **Affected Methods:** [add(Object)][#add(Object)]
    /// **Use Case**: Testing code that must handle corrupted or non-compliant Set implementations
    public static final Break ADD_RETURNS_TRUE_FOR_DUPLICATES = new Break("Add returns true for duplicates");

    /// The backing collection that stores the set elements.
    private final Set<E> set;

    /// Creates a new empty BreakableSet with default configuration.
    ///
    /// The set uses a `HashSet` as the backing collection and has no breaks active.
    /// This constructor is equivalent to using the Builder with default settings.
    ///
    /// ```java
    /// BreakableSet<String> set = new BreakableSet<>();
    /// set.add("element");
    /// ```
    public BreakableSet() {
        this(new HashSet<>(), new HashSet<>(), new HashMap<>(), DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS,
                DEFAULT_SAFETY, Object.class);
    }

    /// Creates a new BreakableSet as a copy of another BreakableSet.
    ///
    /// This constructor copies the source's elements into a new `HashSet` and takes the same breaks, method
    /// statuses and settings; later changes to either set are not seen by the other.
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
    public BreakableSet(final BreakableSet<E> other) {
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
    public BreakableSet(final Set<E> collection) {
        this(collection, new HashSet<>(), new HashMap<>(), DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS, DEFAULT_SAFETY,
                Object.class);
    }

    /// Creates a new BreakableSet with full configuration control.
    ///
    /// This constructor provides complete control over the set's configuration, including
    /// the backing collection, active breaks, and behavioral characteristics. It's primarily
    /// used internally by the Builder but can be used directly by a subclass.
    ///
    /// ```java
    /// Set<String> backing = new HashSet<>();
    /// Set<Break> breaks = new HashSet<>(Set.of(ADD_RETURNS_TRUE_FOR_DUPLICATES));
    /// int characteristics = Spliterator.DISTINCT;
    ///
    /// BreakableSet<String> set = new BreakableSet<>(backing, breaks, new HashMap<>(), characteristics, 0, false,
    ///         String.class);
    /// ```
    ///
    /// @param setArg the backing collection for element storage
    /// @param breaks the collection of breaks to activate
    /// @param methodStatuses the method status configuration.
    /// @param characteristics the spliterator characteristics
    /// @param permits         the flags that indicate what types of values are supported by the collection; duplicates
    ///                        are never permitted, whatever this says.
    /// @param isSafe          whether the collection is safe for concurrent access.
    /// @param compatibleType  the element type that the collection supports.
    /// @throws NullPointerException if set or breaks is null
    protected BreakableSet(final Set<E> setArg, final Set<Break> breaks,
                           final Map<InterfaceMethod, MethodStatus> methodStatuses,
                           final int characteristics, final int permits, final boolean isSafe,
                           final Class<?> compatibleType) {
        super(setArg, breaks, methodStatuses, characteristics, permits & ~PERMITS_DUPLICATES, isSafe,
                compatibleType);
        this.set = setArg;
    }

    /// **Static Factory Method for Wrapping Sets with Breaks**
    ///
    /// Creates a new BreakableSet that wraps an existing Set and applies the specified
    /// set of breaks. This factory method provides a way to create breakable
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
    /// - **Explicit Configuration**: Every setting is an argument; duplicates are never permitted, whatever
    ///   `permits` says
    ///
    /// ## Usage Examples
    ///
    /// The examples pass `0` for `permits` (no `null` elements; with `Object.class` every type is compatible) and
    /// no method statuses.
    ///
    /// ### Basic Set Wrapping with Uniqueness Violation
    /// ```java
    /// Set<String> existingSet = new HashSet<>(Arrays.asList("a", "b", "c"));
    /// Set<Break> breaks = Set.of(BreakableSet.ADD_RETURNS_TRUE_FOR_DUPLICATES);
    ///
    /// BreakableSet<String> wrapper = BreakableSet.wrap(existingSet, breaks, new HashMap<>(), 0, 0, false,
    ///         Object.class);
    ///
    /// // The wrapped set now violates uniqueness constraints
    /// assertTrue(wrapper.add("a")); // Returns true despite duplicate
    /// assertEquals(3, existingSet.size()); // But actual Set still maintains uniqueness in backing store
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
    /// BreakableSet<Integer> brokenWrapper = BreakableSet.wrap(numbers, testBreaks, new HashMap<>(), 0, 0, false,
    ///         Object.class);
    ///
    /// assertTrue(brokenWrapper.add(1));       // Returns true due to break
    /// assertFalse(brokenWrapper.contains(1)); // Returns false due to break
    /// assertEquals(0, brokenWrapper.size());  // Returns 0 due to break
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
    /// ### Break Activation
    /// All breaks in the provided set are immediately active and will affect subsequent
    /// operations on the wrapped Set according to their specific behaviors, particularly
    /// the ADD_RETURNS_TRUE_FOR_DUPLICATES break which can simulate Set contract violations.
    ///
    /// ## Comparison with Builder Pattern
    ///
    /// **Use wrap() when:**
    /// - You have an existing Set to make breakable
    /// - You want to preserve the exact Set instance and its characteristics
    /// - You need to test code with specific Set implementations (TreeSet, LinkedHashSet, etc.)
    ///
    /// **Use Builder when:**
    /// - You're building Sets from scratch with specific properties
    /// - You need method chaining for complex setups
    ///
    /// ### Example Comparison
    /// ```java
    /// // Using wrap() - preserves existing Set type and characteristics
    /// TreeSet<String> orderedSet = new TreeSet<>(Arrays.asList("c", "a", "b"));
    /// BreakableSet<String> wrapped = BreakableSet.wrap(
    ///     orderedSet, Set.of(ADD_RETURNS_TRUE_FOR_DUPLICATES), new HashMap<>(), 0, 0, false, Object.class
    /// );
    /// // Maintains TreeSet ordering and characteristics
    ///
    /// // Using Builder - creates new Set with default backing store
    /// BreakableSet<String> built = new BreakableSet.Builder<String>()
    ///     .addElements("c", "a", "b")
    ///     .addBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES)
    ///     .doesNotPermitNulls()
    ///     .build();
    /// // Uses HashSet backing store by default
    /// ```
    ///
    /// ## Design Considerations
    ///
    /// ### Thread Safety
    /// The thread safety of the wrapped BreakableSet depends entirely on the
    /// thread safety of the underlying Set and on `isSafe`.
    /// - **HashSet**: Not thread-safe
    /// - **Collections.synchronizedSet()**: Thread-safe if properly synchronized
    /// - **ConcurrentHashMap.newKeySet()**: Thread-safe for concurrent access
    ///
    /// ### Memory Efficiency
    /// This method doesn't copy the Set elements; it only creates a wrapper with break functionality.
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
    public static <E> BreakableSet<E> wrap(final Set<E> set, final Set<Break> breaks,
            final Map<InterfaceMethod, MethodStatus> methodStatuses, final int characteristics, final int permits,
            final boolean isSafe, final Class<?> compatibleType) {
        return new BreakableSet<>(set, breaks, methodStatuses, characteristics, permits, isSafe, compatibleType);
    }

    /// A set equals any [Set] with the same elements, whatever its class ([Set#equals(Object)]).
    @Override
    protected boolean valueEqualsOtherType(final Object other) {
        return other instanceof Set<?> that && set.equals(that);
    }

    /// Adds the specified element to this set if it is not already present.
    ///
    /// This method maintains proper Set semantics under normal operation by checking for element
    /// existence before adding. However, when the `ADD_RETURNS_TRUE_FOR_DUPLICATES` break is active,
    /// it returns `true` for an element that is already present, violating the Set contract.
    ///
    /// ### Normal Behavior
    /// - Returns `true` if the element was added (not already present)
    /// - Returns `false` if the element was already present
    /// - Maintains set uniqueness constraints
    ///
    /// ### With ADD_RETURNS_TRUE_FOR_DUPLICATES Break
    /// - Returns `true` for an element that is already present, without adding it
    /// - The set still holds one copy of the element
    /// - Any other call goes to [BreakableCollection#add(Object)], where the inherited `ADD_*` breaks apply
    ///
    /// ```java
    /// BreakableSet<String> normalSet = new BreakableSet<>();
    /// assertTrue(normalSet.add("test"));   // First add succeeds
    /// assertFalse(normalSet.add("test"));  // Duplicate add fails
    ///
    /// BreakableSet<String> brokenSet = new BreakableSet.Builder<String>()
    ///     .addBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES)
    ///     .build();
    /// assertTrue(brokenSet.add("test"));   // First add succeeds
    /// assertTrue(brokenSet.add("test"));   // Duplicate add also reports success
    /// assertEquals(1, brokenSet.size());   // but nothing was added
    /// ```
    ///
    /// @param e the element to be added to this set
    /// @return `true` if this set did not already contain the element (or the break is active)
    /// @throws UnsupportedOperationException if the add operation is not supported
    /// @throws ClassCastException if the class of the element prevents it from being added
    /// @throws NullPointerException if the element is null and this set does not permit nulls
    /// @throws IllegalArgumentException if some property of the element prevents it from being added
    @Override
    public boolean add(final E e) {
        // The collection's own add(E) applies the inherited ADD_* breaks; the set adds only this one.
        if (supportsMethod(CollectionMethods.ADD) && hasBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES) && set.contains(e)) {
            return true;
        }
        return super.add(e);
    }

    /// Adds all elements in the specified collection to this set.
    ///
    /// This method is [BreakableCollection#addAll(Collection)]: the `ADD_ALL_*` breaks apply, and
    /// `ADD_RETURNS_TRUE_FOR_DUPLICATES` does not (it changes only [#add(Object)]).
    ///
    /// ### Behavior
    /// - Returns `true` if any element was added to the set
    /// - Respects the `ADD_ALL_*` break configuration
    ///
    /// ```java
    /// BreakableSet<Integer> set = new BreakableSet<>();
    /// set.add(1);
    ///
    /// boolean changed = set.addAll(Arrays.asList(2, 3)); // true - added 2 and 3
    /// assertEquals(3, set.size());
    ///
    /// // With breaks active
    /// BreakableSet<Integer> brokenSet = new BreakableSet.Builder<Integer>()
    ///     .addBreak(ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS)
    ///     .build();
    /// brokenSet.addAll(Arrays.asList(1, 2)); // Nothing is added
    /// assertTrue(brokenSet.isEmpty());
    /// ```
    ///
    /// @param c collection containing elements to be added to this set
    /// @return `true` if this set changed as a result of the call
    /// @throws UnsupportedOperationException if the addAll operation is not supported
    /// @throws ClassCastException if any element prevents it from being added to this set
    /// @throws NullPointerException if the collection is null or contains null elements when nulls are not permitted
    /// @throws IllegalArgumentException if some property of an element prevents it from being added
    @Override
    public boolean addAll(final Collection<? extends E> c) {
        // The collection's own addAll(Collection) applies the inherited ADD_ALL_* breaks.
        return super.addAll(c);
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
    /// - **Configuration Flexibility**: Initial elements, breaks and restrictions
    /// - **Validation**: Ensures set-specific constraints are maintained
    /// - **Independent Building**: Each build() call creates a new independent instance, backed by a `HashSet`
    ///
    /// ## Builder Configuration
    ///
    /// ### Basic Set Creation
    /// ```java
    /// BreakableSet<String> set = new BreakableSet.Builder<String>()
    ///     .build();
    /// ```
    ///
    /// ### Set with Initial Elements
    /// ```java
    /// BreakableSet<Integer> set = new BreakableSet.Builder<>(List.of(1, 2, 3))
    ///     .build();
    /// ```
    ///
    /// ### Set with Breaks and Restrictions
    /// ```java
    /// BreakableSet<String> brokenSet = new BreakableSet.Builder<String>()
    ///     .addBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES)
    ///     .doesNotSupportMethod(CollectionMethods.REMOVE)
    ///     .doesNotPermitNulls()
    ///     .build();
    /// ```
    ///
    /// ### Complex Configuration
    /// ```java
    /// BreakableSet<Object> complexSet = new BreakableSet.Builder<Object>()
    ///     .addBreaks(List.of(ADD_RETURNS_TRUE_FOR_DUPLICATES, BreakableCollection.CONTAINS_ALWAYS_RETURNS_FALSE))
    ///     .doesNotSupportMethod(CollectionMethods.CLEAR)
    ///     .doesNotSupportMethod(CollectionMethods.REMOVE_ALL)
    ///     .setCharacteristics(Spliterator.ORDERED | Spliterator.DISTINCT)
    ///     .build();
    /// ```
    ///
    /// ## Set-Specific Constraints
    ///
    /// The BreakableSet.Builder enforces set-specific constraints:
    /// - **No Duplicate Permission**: `permitsDuplicates` is always set to false
    /// - **Set Semantics**: Maintains set behavior unless explicitly broken
    ///
    /// ## Inherited Configuration Methods
    ///
    /// From AbstractBuilder, the following methods are available:
    /// - `addBreak(Break)` and `addBreaks(Collection)` - Add behavioral breaks
    /// - `addElements(Iterable)` and `addElements(E...)` - Add initial elements
    /// - `doesNotSupportMethod(InterfaceMethod)` - Mark a method as unsupported
    /// - `doesNotPermitNulls()` - Reject `null` elements
    /// - `doesNotPermitIncompatibleTypes(Class)` - Reject elements of other types
    /// - `setCharacteristics(int)` - Add spliterator characteristics
    /// - `setSafe(boolean)` - Make the set safe for concurrent access
    ///
    /// ## Performance Considerations
    ///
    /// - **Break Activation**: Minimal runtime overhead when breaks are not used
    /// - **Builder Reuse**: Each build() creates a new instance, builder can be reused
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
        /// - No initial elements
        /// - No breaks active
        /// - Standard set semantics (no duplicates permitted)
        /// - Default null and type compatibility settings
        ///
        /// The built set is backed by a `HashSet`.
        ///
        /// ```java
        /// BreakableSet.Builder<String> builder = new BreakableSet.Builder<>();
        /// BreakableSet<String> set = builder.build();
        /// ```
        public Builder() {
            super();
            doesNotPermitDuplicates();
        }

        /// Creates a new builder with initial elements.
        ///
        /// The elements are copied into the builder; the built set is backed by a new `HashSet` that holds them, so the
        /// argument's type does not decide the set's iteration order. To back a set with a set of your own, use the
        /// [BreakableSet(Set)][BreakableSet#BreakableSet(Set)] constructor or `wrap`.
        ///
        /// ```java
        /// BreakableSet<String> set = new BreakableSet.Builder<>(List.of("a", "b", "c"))
        ///     .build();
        /// ```
        ///
        /// @param elements the initial elements of the built sets
        /// @throws NullPointerException if elements is null
        public Builder(final Collection<E> elements) {
            super(elements);
            doesNotPermitDuplicates();
        }

        /// Creates a new builder by copying configuration from another builder.
        ///
        /// This copy constructor creates a new builder that inherits all configuration
        /// from the source builder, including its elements, breaks, unsupported methods, and behavioral
        /// settings. The builders are independent: each holds its own copy of the elements and breaks.
        ///
        /// ### Use Cases
        /// - Creating variations of existing configurations
        /// - Template-based set creation
        /// - Configuration inheritance patterns
        ///
        /// ```java
        /// // Create a base configuration
        /// BreakableSet.Builder<String> baseBuilder = new BreakableSet.Builder<String>()
        ///     .doesNotPermitNulls()
        ///     .doesNotSupportMethod(CollectionMethods.CLEAR);
        ///
        /// // Create variations based on the template
        /// BreakableSet<String> normalSet = new BreakableSet.Builder<>(baseBuilder)
        ///     .build();
        ///
        /// BreakableSet<String> brokenSet = new BreakableSet.Builder<>(baseBuilder)
        ///     .addBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES)
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
        ///     .addBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES)  // returns BreakableSet.Builder<String>
        ///     .doesNotPermitNulls()                       // returns BreakableSet.Builder<String>
        ///     .build();
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
        ///     .doesNotPermitNulls()
        ///     .doesNotSupportMethod(CollectionMethods.REMOVE);
        ///
        /// // Create independent copies for different configurations
        /// BreakableSet.Builder<Integer> variation1 = template.copy()
        ///     .addBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES);
        ///
        /// BreakableSet.Builder<Integer> variation2 = template.copy()
        ///     .doesNotSupportMethod(CollectionMethods.ADD);
        /// ```
        ///
        /// @return a new builder with identical configuration
        @Override
        public BreakableSet.Builder<E> copy() {
            return new BreakableSet.Builder<>(this);
        }

        @Override
        public BreakableSet<E> copy(final BreakableSet<E> other) {
            return new BreakableSet<>(other);
        }

        /// Constructs a new BreakableSet instance with the current builder configuration and further elements.
        ///
        /// This method creates a new BreakableSet using all the settings configured in
        /// the builder, including breaks, method restrictions, behavioral settings, and
        /// the builder's elements, to which it adds `elements`. Each call creates a completely independent
        /// BreakableSet instance, backed by a new `HashSet`.
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
        ///     .addBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES)
        ///     .doesNotPermitNulls();
        ///
        /// // Create multiple independent sets with the same configuration
        /// BreakableSet<String> set1 = builder.build();
        /// BreakableSet<String> set2 = builder.build(List.of("x"));
        ///
        /// // Sets are independent - changes to one don't affect the other
        /// set1.add("test");
        /// assertEquals(Set.of("x"), set2.unbroken());
        /// ```
        ///
        /// @param elements further elements for the new set
        /// @return a new BreakableSet instance configured according to this builder's settings
        @Override
        public BreakableSet<E> build(final Iterable<E> elements) {
            HashSet<E> storage = new HashSet<>(elements());
            elements.forEach(storage::add);
            return new BreakableSet<>(storage, new HashSet<>(breaks()), new HashMap<>(methodStatuses()),
                    characteristics(), permits(), isSafe(), compatibleType());
        }
    }
}
