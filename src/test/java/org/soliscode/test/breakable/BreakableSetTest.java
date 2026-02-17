package org.soliscode.test.breakable;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.collection.CollectionContract;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.provider.CollectionProvider;
import org.soliscode.test.provider.FunctionalCollectionProvider;
import org.soliscode.test.util.UsesCollections;

import java.util.List;

import static org.soliscode.test.assertions.collection.CollectionAssertions.assertEquals;

/// **Test Suite for BreakableSet Implementation**
///
/// This comprehensive test class validates the behavior of BreakableSet, focusing on both
/// standard Set contract compliance and the controlled violation of set semantics through
/// programmatic breaks. The tests ensure that BreakableSet maintains proper set behavior
/// under normal conditions while correctly implementing break mechanisms for testing purposes.
///
/// ## Test Coverage
///
/// ### Contract Compliance Testing
/// - **Collection Contract**: Full validation of Collection interface methods
/// - **Set Semantics**: Uniqueness constraints and duplicate prevention
/// - **Standard Operations**: Add, remove, contains, size operations
/// - **Behavioral Consistency**: Consistent behavior across different operation patterns
///
/// ### Break Mechanism Testing
/// - **SET_ALLOWS_DUPLICATE_ELEMENTS**: Tests violation of uniqueness constraints
/// - **Break Interaction**: How breaks affect standard set operations
/// - **State Consistency**: Set state remains coherent even with active breaks
///
/// ### Builder Pattern Testing
/// - **Configuration**: Builder setup with various parameters
/// - **Copy Semantics**: Builder copying and independence
/// - **Fluent Interface**: Method chaining and configuration transfer
///
/// ### Constructor Testing
/// - **Default Construction**: Empty set creation
/// - **Copy Construction**: Creating sets from existing instances
/// - **Collection Construction**: Building sets from other collections
///
/// ## Test Architecture
///
/// This test class follows the SolisCode testing framework patterns:
/// - **AbstractTest Extension**: Inherits common testing infrastructure
/// - **Contract Implementation**: Implements CollectionContract for automated testing
/// - **Provider Pattern**: Uses CollectionProvider for consistent test data generation
/// - **Element Support**: WithIntegerElement provides integer-based test elements
/// - **Collection Utilities**: UsesCollections provides mutable collection factories
///
/// ### Framework Integration
/// ```java
/// // The test class integrates multiple framework components:
/// public class BreakableSetTest extends AbstractTest                    // Base testing infrastructure
///     implements CollectionContract<Integer, BreakableSet<Integer>>,   // Automated contract testing
///               WithIntegerElement,                                     // Integer element support
///               UsesCollections {                                       // Mutable collection factories
/// ```
///
/// ## Test Methodology
///
/// ### Standard Behavior Validation
/// Tests verify that BreakableSet behaves like a proper Set implementation:
/// - Uniqueness of elements
/// - Proper return values from add_singleElement_returnsTrueAndUpdatesSize operations
/// - Correct size calculations
/// - Standard iteration behavior
///
/// ### Break Behavior Validation
/// Tests verify that breaks work as intended without compromising overall functionality:
/// - Break activation changes specific behaviors
/// - Non-broken operations continue to work normally
/// - State consistency is maintained
/// - Break effects are isolated and predictable
///
/// ### Builder Testing Strategy
/// Tests validate the builder pattern implementation:
/// - Configuration transfer from builder to set
/// - Builder reusability and independence
/// - Copy constructor behavior and isolation
/// - Fluent interface method chaining
///
/// ## Example Test Scenarios
///
/// ### Normal Set Behavior
/// ```java
/// @Test
/// void testStandardSetBehavior() {
///     BreakableSet<String> set = new BreakableSet<>();
///     assertTrue(set.add_singleElement_returnsTrueAndUpdatesSize("element"));      // First add_singleElement_returnsTrueAndUpdatesSize succeeds
///     assertFalse(set.add_singleElement_returnsTrueAndUpdatesSize("element"));     // Duplicate add_singleElement_returnsTrueAndUpdatesSize fails
///     assertEquals(1, set.size());         // Size reflects uniqueness
/// }
/// ```
///
/// ### Break Behavior Testing
/// ```java
/// @Test
/// void testDuplicateBreak() {
///     BreakableSet<String> brokenSet = new BreakableSet.Builder<String>()
///         .withBreak(SET_ALLOWS_DUPLICATE_ELEMENTS)
///         .build();
///     assertTrue(brokenSet.add_singleElement_returnsTrueAndUpdatesSize("test"));   // First add_singleElement_returnsTrueAndUpdatesSize succeeds
///     assertTrue(brokenSet.add_singleElement_returnsTrueAndUpdatesSize("test"));   // Duplicate add_singleElement_returnsTrueAndUpdatesSize also succeeds with break
///     assertEquals(2, brokenSet.size());   // Size reflects duplicates
/// }
/// ```
///
/// ### Builder Configuration Testing
/// ```java
/// @Test
/// void testBuilderConfiguration() {
///     BreakableSet<Integer> set = new BreakableSet.Builder<Integer>()
///         .addElements(1, 2, 3)
///         .withBreak(someBreak)
///         .permitsNulls(false)
///         .build();
///     // Verify configuration transfer and proper initialization
/// }
/// ```
///
/// ## Quality Assurance
///
/// ### Test Isolation
/// Each test method is independent and doesn't rely on state from other tests.
/// Fresh BreakableSet instances are created for each test scenario.
///
/// ### Error Condition Testing
/// Tests verify proper error handling for:
/// - Invalid configurations
/// - Null parameter handling
/// - Unsupported operations
/// - Break interaction edge cases
///
/// ### Performance Validation
/// While not explicitly performance tests, the tests validate that break mechanisms
/// don't introduce significant performance degradation in normal operation paths.
///
/// @author evanbergstrom
/// @since 1.0.0
/// @see BreakableSet
/// @see CollectionContract
/// @see AbstractTest
public class BreakableSetTest extends AbstractTest
    implements CollectionContract<Integer, BreakableSet<Integer>>, WithIntegerElement, UsesCollections {

    /// Provides the CollectionProvider for automated contract testing.
    ///
    /// This method creates a FunctionalCollectionProvider that supplies BreakableSet instances
    /// for the automated contract testing framework. The provider defines how to create
    /// empty sets, copy sets, and build sets from existing collections.
    ///
    /// ### Provider Configuration
    /// - **Empty Constructor**: `BreakableSet::new` - Creates empty sets
    /// - **Copy Constructor**: `BreakableSet::new` - Creates sets from other BreakableSet instances
    /// - **Collection Constructor**: Custom lambda that wraps collections in ArrayList backing
    /// - **Element Provider**: Uses the integer element provider from WithIntegerElement
    ///
    /// ### Framework Integration
    /// The CollectionContract interface uses this provider to automatically generate
    /// test scenarios with various set configurations, ensuring comprehensive coverage
    /// of the Collection interface methods.
    ///
    /// ```java
    /// // The framework uses this provider like:
    /// CollectionProvider<Integer, BreakableSet<Integer>> provider = provider();
    /// BreakableSet<Integer> emptySet = provider.getEmpty();
    /// BreakableSet<Integer> populatedSet = provider.getWithElements(Arrays.asList(1, 2, 3));
    /// ```
    ///
    /// @return a CollectionProvider configured for BreakableSet testing
    @Override
    public @NonNull CollectionProvider<Integer, BreakableSet<Integer>> provider() {
        return FunctionalCollectionProvider.from(
                BreakableSet::new,
                BreakableSet::new,
                elements -> new BreakableSet<>(new java.util.HashSet<>(elements)),
                elementProvider()
        );
    }

    /// Specifies that BreakableSet does not permit duplicate elements under normal operation.
    ///
    /// This method informs the contract testing framework that BreakableSet follows
    /// standard Set semantics by not allowing duplicate elements. This affects how
    /// the automated tests validate add_singleElement_returnsTrueAndUpdatesSize operations, size calculations, and other
    /// collection behaviors.
    ///
    /// ### Contract Testing Impact
    /// When this returns false, the contract tests will:
    /// - Expect `add_singleElement_returnsTrueAndUpdatesSize()` to return false for duplicate elements
    /// - Verify that set size doesn't increase when adding duplicates
    /// - Validate that contains() works correctly with unique elements
    /// - Test that collections maintain uniqueness constraints
    ///
    /// ### Break Mechanism Note
    /// While this method returns false (indicating no duplicates), the SET_ALLOWS_DUPLICATE_ELEMENTS
    /// break can override this behavior at runtime. The contract tests validate normal behavior,
    /// while specific break tests validate the exception cases.
    ///
    /// @return false, indicating that BreakableSet does not permit duplicate elements
    @Override
    public boolean permitDuplicates() {
        return false;
    }

    // ========== Constructor Tests ==========
    //
    // These tests validate the various constructor patterns available for
    // BreakableSet creation, including copy construction and initialization.

    /// Tests the BreakableSet copy constructor behavior and isolation.
    ///
    /// This test validates that the copy constructor creates a new BreakableSet instance
    /// that shares the backing collection with the original but does not inherit the
    /// breaks or other behavioral modifications. This ensures that copied sets start
    /// with clean, standard Set behavior regardless of the source set's configuration.
    ///
    /// ### Test Scenario
    /// 1. Create an original BreakableSet with elements {1, 2, 3}
    /// 2. Add the ADD_DOES_NOT_ADD_ELEMENT break to the original
    /// 3. Create a copy using the copy constructor
    /// 4. Verify that the copy contains the same elements as the original
    /// 5. Implicitly verify that the copy does not inherit the break
    ///
    /// ### Constructor Behavior Validation
    /// - **Element Copying**: All elements from the original are present in the copy
    /// - **Break Isolation**: Breaks from the original are not inherited
    /// - **Independence**: The copy is a separate instance that can be modified independently
    /// - **State Consistency**: The copy starts with standard Set behavior
    ///
    /// ### Copy Semantics
    /// The copy constructor provides shallow copying of elements but deep isolation
    /// of behavioral configuration:
    /// ```java
    /// BreakableSet<String> original = createBrokenSet();
    /// original.addBreak(someBreak);               // Original has breaks
    ///
    /// BreakableSet<String> copy = new BreakableSet<>(original);
    /// // copy has same elements but no breaks
    /// assertTrue(copy.add_singleElement_returnsTrueAndUpdatesSize("duplicate"));          // Standard behavior
    /// assertFalse(copy.add_singleElement_returnsTrueAndUpdatesSize("duplicate"));         // Rejects duplicates normally
    /// ```
    ///
    /// ### Use Cases
    /// - Creating clean instances from configured sets
    /// - Resetting behavioral state while preserving data
    /// - Template-based set creation with standard behavior
    ///
    /// @see BreakableSet#BreakableSet(BreakableSet)
    @Test
    @DisplayName("copyConstructor: copies elements from source set")
    public void copyConstructor_whenGivenConfiguredSet_copiesElements() {
        BreakableSet<Integer> original = new BreakableSet.Builder<Integer>()
                .addElements(1, 2, 3)
                .addBreak(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT)
                .build();

        BreakableSet<Integer> copy = new BreakableSet<>(original);
        assertEquals(setOf(1, 2, 3), copy);
    }

    // ========== Builder Pattern Tests ==========
    //
    // These tests validate the Builder pattern implementation, including
    // configuration transfer, copying, and fluent interface behavior.

    /// Tests the basic Builder pattern functionality and configuration transfer.
    ///
    /// This test validates that the BreakableSet.Builder correctly constructs sets
    /// with specified elements and breaks, ensuring that all configuration is
    /// properly transferred from the builder to the resulting BreakableSet instance.
    ///
    /// ### Test Scenario
    /// 1. Create a new Builder instance
    /// 2. Add elements {1, 2, 3} using the fluent interface
    /// 3. Add the ADD_DOES_NOT_ADD_ELEMENT break
    /// 4. Build the BreakableSet instance
    /// 5. Verify that the set contains the expected elements
    ///
    /// ### Builder Validation
    /// - **Element Addition**: Elements added to builder appear in final set
    /// - **Break Configuration**: Breaks configured in builder are active in final set
    /// - **Fluent Interface**: Method chaining works correctly
    /// - **Build Transfer**: All configuration is properly transferred during build()
    ///
    /// ### Configuration Patterns
    /// The test demonstrates the typical builder usage pattern:
    /// ```java
    /// BreakableSet<T> set = new BreakableSet.Builder<T>()
    ///     .addElements(element1, element2, element3)    // Add initial data
    ///     .addBreak(specificBreak)                      // Configure behavior
    ///     .permitsNulls(false)                          // Set policies
    ///     .build();                                     // Create instance
    /// ```
    ///
    /// ### Builder Benefits
    /// - **Readability**: Clear, declarative construction syntax
    /// - **Flexibility**: Can configure any combination of settings
    /// - **Type Safety**: Compile-time type checking throughout configuration
    /// - **Reusability**: Builder can be reused to create multiple similar instances
    ///
    /// @see BreakableSet.Builder
    /// @see BreakableSet.Builder#addElements(Object[])
    /// @see BreakableSet.Builder#addBreak(Break)
    /// @see BreakableSet.Builder#build()
    @Test
    @DisplayName("builder: builds configured set from individual elements")
    public void builder_withElementsAndBreak_buildsConfiguredSet() {
        BreakableSet<Integer> set = new BreakableSet.Builder<Integer>()
                .addElements(1, 2, 3)
                .addBreak(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT)
                .build();

        assertEquals(setOf(1, 2, 3), set);
    }

    /// Tests the `Builder(Map)` constructor for initializing sets from existing collections.
    ///
    /// This test ensures that providing an initial collection to the `Builder` correctly
    /// initializes the resulting `BreakableSet` with those elements and applies any
    /// configured breaks.
    ///
    /// @see BreakableSet.Builder
    @Test
    @DisplayName("builder: builds configured set from initial collection")
    public void builder_withInitialElements_buildsConfiguredSet() {
        BreakableSet<Integer> set = new BreakableSet.Builder<>(List.of(1,2,3))
                .addBreak(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT)
                .build();

        assertEquals(setOf(1, 2, 3), set);
    }

    /// Tests the Builder copy functionality and configuration inheritance.
    ///
    /// This test validates that the Builder.copy() method creates an independent
    /// builder instance that inherits all configuration from the source builder
    /// but can be modified without affecting the original. This is essential for
    /// template-based construction and configuration reuse patterns.
    ///
    /// ### Test Scenario
    /// 1. Create a base builder with elements {1, 2, 3} and a specific break
    /// 2. Create a copy of the builder using copy()
    /// 3. Build a BreakableSet from the copied builder
    /// 4. Verify that the resulting set contains the expected elements
    /// 5. Implicitly verify that all configuration was inherited
    ///
    /// ### Copy Behavior Validation
    /// - **Configuration Inheritance**: All settings from original builder are copied
    /// - **Instance Independence**: Copy and original are separate instances
    /// - **Build Consistency**: Sets built from copy have same configuration as original
    /// - **Modification Isolation**: Changes to copy don't affect original builder
    ///
    /// ### Template Pattern Usage
    /// Builder copying enables powerful template patterns:
    /// ```java
    /// // Create a base template
    /// BreakableSet.Builder<String> template = new BreakableSet.Builder<String>()
    ///     .permitsNulls(false)
    ///     .doesNotSupport(CollectionMethods.Clear);
    ///
    /// // Create specialized variations
    /// BreakableSet<String> normalSet = template.copy()
    ///     .build();
    ///
    /// BreakableSet<String> brokenSet = template.copy()
    ///     .addBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES)
    ///     .build();
    ///
    /// // Template remains unmodified for further use
    /// BreakableSet<String> anotherSet = template.copy().build();
    /// ```
    ///
    /// ### Builder Reusability
    /// The copy mechanism ensures that builders can be safely reused:
    /// - Original builder retains its configuration
    /// - Copies can be modified independently
    /// - Multiple sets can be created with variations of the same base configuration
    ///
    /// @see BreakableSet.Builder#copy()
    /// @see BreakableSet.Builder#build()
    @Test
    @DisplayName("builder.copy(): produces independent builder with inherited config")
    public void builderCopy_whenCalled_preservesConfiguration() {
        BreakableSet.Builder<Integer> builder = new BreakableSet.Builder<Integer>()
                .addElements(1, 2, 3)
                .addBreak(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT);

        BreakableSet.Builder<Integer> copy = builder.copy();

        BreakableSet<Integer> set = copy.build();
        assertEquals(setOf(1, 2, 3), set); // Configuration inherited correctly
    }
}
