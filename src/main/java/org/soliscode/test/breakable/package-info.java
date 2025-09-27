/// **Breakable Collection Framework for Robust Testing**
///
/// This package provides a comprehensive framework of "breakable" collection implementations that can be
/// programmatically configured to violate standard Java Collection Framework contracts in controlled ways.
/// These implementations enable comprehensive testing of code that must handle corrupted, non-compliant,
/// or edge-case collection behaviors.
///
/// ## Overview
///
/// The breakable framework is designed around the principle of **controlled contract violation**. Standard
/// Java collections maintain strict behavioral contracts (e.g., Set uniqueness, List ordering, Map key-value
/// relationships). Real-world applications must often handle collections that don't behave as expected due to:
/// - **Implementation bugs** in third-party libraries
/// - **Corrupted data structures** from serialization or persistence issues
/// - **Custom implementations** that don't fully comply with interface contracts
/// - **Concurrent modification** leading to inconsistent states
/// - **Resource exhaustion** causing degraded performance or failures
///
/// ## Core Concepts
///
/// ### Break Mechanism
///
/// The framework uses a **Break** system where specific behavioral anomalies can be activated:
/// ```java
/// // Create a collection that violates the Set uniqueness contract
/// BreakableSet<String> brokenSet = new BreakableSet.Builder<String>()
///     .withBreak(BreakableSet.ADD_RETURNS_TRUE_FOR_DUPLICATES)
///     .build();
///
/// brokenSet.add("duplicate");
/// boolean result = brokenSet.add("duplicate"); // Returns true despite duplicate
/// ```
///
/// ### Builder Pattern
///
/// All breakable implementations use a fluent Builder pattern for configuration:
/// ```java
/// BreakableList<Integer> complexBrokenList = new BreakableList.Builder<Integer>()
///     .withBreak(GET_ALWAYS_RETURNS_NULL)
///     .withBreak(ADD_ALL_SKIPS_FIRST_ELEMENT)
///     .doesNotSupport(CollectionMethods.Clear)
///     .permitsNulls(false)
///     .build();
/// ```
///
/// ### Inheritance Hierarchy
///
/// The framework follows the standard Collection hierarchy with breakable implementations:
/// ```
/// BreakableIterable
///   └── BreakableCollection
///       ├── BreakableList
///       ├── BreakableSequencedCollection
///       └── BreakableSet
///           └── BreakableSortedSet
///               └── BreakableNavigableSet
/// ```
///
/// ## Available Implementations
///
/// ### Core Collection Types
///
/// #### BreakableCollection
/// **Purpose**: Base collection implementation with fundamental collection contract violations
/// **Key Breaks**: Element addition/removal failures, size inconsistencies, bulk operation anomalies
/// **Use Case**: Testing basic collection handling and error recovery
///
/// #### BreakableList
/// **Purpose**: List implementation with indexed access and modification violations
/// **Key Breaks**: Indexed access failures, element replacement anomalies, sorting corruptions
/// **Use Case**: Testing algorithms that depend on positional access and list ordering
///
/// #### BreakableSet
/// **Purpose**: Set implementation that can violate uniqueness constraints
/// **Key Breaks**: Duplicate acceptance, membership query failures
/// **Use Case**: Testing code that assumes Set uniqueness guarantees
///
/// #### BreakableSortedSet
/// **Purpose**: SortedSet implementation with ordering and navigation violations
/// **Key Breaks**: Navigation method failures, comparator inconsistencies, subset operation errors
/// **Use Case**: Testing sorted collection algorithms and range-based operations
///
/// #### BreakableNavigableSet
/// **Purpose**: NavigableSet implementation with advanced navigation method violations
/// **Key Breaks**: Ceiling/floor failures, poll operation inconsistencies, descendant view corruptions
/// **Use Case**: Testing sophisticated navigation algorithms and search operations
///
/// #### BreakableSequencedCollection
/// **Purpose**: SequencedCollection implementation with positional insertion/removal violations
/// **Key Breaks**: First/last access failures, positional addition anomalies, sequence reversal errors
/// **Use Case**: Testing sequence-dependent algorithms and FIFO/LIFO operations
///
/// ### Supporting Infrastructure
///
/// #### BreakableIterable
/// **Purpose**: Base iterable implementation with iteration and spliterator violations
/// **Key Breaks**: Iterator failures, spliterator anomalies, forEach operation errors
/// **Use Case**: Testing iteration-based algorithms and stream processing
///
/// #### BreakableIterator
/// **Purpose**: Iterator implementation with traversal and modification violations
/// **Key Breaks**: hasNext/next inconsistencies, removal failures, concurrent modification simulation
/// **Use Case**: Testing iterator-dependent code and traversal algorithms
///
/// #### BreakableSpliterator
/// **Purpose**: Spliterator implementation with parallel processing violations
/// **Key Breaks**: Splitting failures, characteristic misrepresentation, estimation errors
/// **Use Case**: Testing parallel stream operations and fork-join algorithms
///
/// ## Break Categories
///
/// ### Element Access Breaks
/// - **Null Returns**: Methods return null instead of elements or proper exceptions
/// - **Wrong Elements**: Methods return incorrect elements (first instead of last, etc.)
/// - **Access Failures**: Methods throw exceptions even when operations should succeed
/// - **Skipped Elements**: Methods skip intended elements and return adjacent ones
///
/// ### Modification Breaks
/// - **Silent Failures**: Modification methods complete without actually modifying the collection
/// - **Inconsistent Returns**: Methods return success indicators that don't match actual results
/// - **Partial Operations**: Bulk operations complete only partially
/// - **Wrong Targets**: Modifications affect different elements than intended
///
/// ### State Reporting Breaks
/// - **Size Misreporting**: size() and isEmpty() return incorrect values
/// - **Membership Errors**: contains() and containsAll() return wrong results
/// - **Iterator Inconsistencies**: Iterators don't reflect actual collection state
/// - **View Corruption**: Subset views don't accurately represent ranges
///
/// ### Contract Violation Breaks
/// - **Uniqueness Violations**: Sets accept duplicate elements
/// - **Ordering Violations**: Sorted collections lose ordering guarantees
/// - **Immutability Violations**: Supposedly immutable views allow modifications
/// - **Exception Contract Violations**: Methods don't throw expected exceptions
///
/// ## Testing Patterns
///
/// ### Basic Error Handling Testing
/// ```java
/// @Test
/// void testCollectionErrorHandling() {
///     BreakableCollection<String> brokenCollection = new BreakableCollection.Builder<String>()
///         .withBreak(ADD_DOES_NOT_ADD_ELEMENT)
///         .withBreak(SIZE_ALWAYS_RETURNS_ZERO)
///         .build();
///
///     // Test that algorithms handle inconsistent collection behavior
///     MyAlgorithm algorithm = new MyAlgorithm();
///     assertDoesNotThrow(() -> algorithm.process(brokenCollection));
/// }
/// ```
///
/// ### Contract Violation Testing
/// ```java
/// @Test
/// void testSetUniquenessViolation() {
///     BreakableSet<Integer> corruptedSet = new BreakableSet.Builder<Integer>()
///         .withBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES)
///         .build();
///
///     // Test that deduplication logic handles corrupted sets
///     DeduplicationUtility utility = new DeduplicationUtility();
///     Set<Integer> result = utility.deduplicate(corruptedSet);
///     assertEquals(result.size(), new HashSet<>(result).size());
/// }
/// ```
///
/// ### Navigation Algorithm Testing
/// ```java
/// @Test
/// void testNavigationAlgorithmRobustness() {
///     BreakableNavigableSet<Double> brokenNavSet = new BreakableNavigableSet.Builder<Double>()
///         .withBreak(CEILING_ALWAYS_RETURNS_NULL)
///         .withBreak(FLOOR_RETURNS_CEILING_VALUE)
///         .build();
///
///     // Test that search algorithms gracefully handle navigation failures
///     BinarySearchUtil searchUtil = new BinarySearchUtil();
///     SearchResult result = searchUtil.findClosest(brokenNavSet, 5.0);
///     assertTrue(result.hasValidResult() || result.hasGracefulFallback());
/// }
/// ```
///
/// ### Bulk Operation Testing
/// ```java
/// @Test
/// void testBulkOperationResilience() {
///     BreakableList<String> problematicList = new BreakableList.Builder<String>()
///         .withBreak(ADD_ALL_SKIPS_FIRST_ELEMENT)
///         .withBreak(REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS)
///         .build();
///
///     // Test that bulk processing handles partial operation completions
///     BulkProcessor processor = new BulkProcessor();
///     ProcessingResult result = processor.processBatch(problematicList, operations);
///     assertTrue(result.hasPartialCompletionHandling());
/// }
/// ```
///
/// ## Integration with Testing Framework
///
/// ### Contract Testing
/// The breakable framework integrates seamlessly with the broader SolisCode Test framework:
/// ```java
/// public class CustomCollectionTest extends AbstractTest
///         implements CollectionContract<String, BreakableCollection<String>> {
///
///     @Override
///     public BreakableCollection<String> createCollection() {
///         return new BreakableCollection.Builder<String>()
///             .withBreak(selectedBreak)
///             .build();
///     }
/// }
/// ```
///
/// ### Provider Integration
/// Breakable collections work with the provider system for automated test generation:
/// ```java
/// public class BreakableCollectionProvider implements CollectionProvider<String> {
///     @Override
///     public Stream<BreakableCollection<String>> collections() {
///         return Stream.of(
///             new BreakableCollection.Builder<String>().build(),
///             new BreakableCollection.Builder<String>()
///                 .withBreak(ADD_ALWAYS_RETURNS_FALSE).build(),
///             // Additional broken configurations...
///         );
///     }
/// }
/// ```
///
/// ## Design Principles
///
/// ### Controlled Degradation
/// Breaks are designed to simulate realistic failure modes rather than arbitrary chaos:
/// - **Predictable Behavior**: Each break has well-defined, consistent effects
/// - **Isolated Failures**: Breaks can be combined but each affects specific functionality
/// - **Realistic Scenarios**: Breaks model actual problems seen in production systems
///
/// ### Comprehensive Coverage
/// The framework covers all major collection interfaces and their contracts:
/// - **Interface Completeness**: Every major collection interface has a breakable implementation
/// - **Method Coverage**: All significant methods can be individually broken
/// - **Contract Dimensions**: Breaks cover functionality, performance, and exception behavior
///
/// ### Testing Integration
/// Designed to work seamlessly with existing testing infrastructure:
/// - **JUnit Integration**: Works naturally with standard JUnit test patterns
/// - **Framework Compatibility**: Integrates with the broader SolisCode Test framework
/// - **Mock Replacement**: Can replace mock objects for more realistic failure simulation
///
/// ## Best Practices
///
/// ### Break Selection
/// - **Target Specific Scenarios**: Choose breaks that match realistic failure modes
/// - **Test One Concept**: Focus each test on a specific type of contract violation
/// - **Combine Judiciously**: Use multiple breaks only when testing interaction effects
///
/// ### Test Organization
/// - **Separate Break Categories**: Group tests by the type of break being tested
/// - **Document Expectations**: Clearly specify how code should handle each break
/// - **Verify Graceful Degradation**: Ensure systems fail safely rather than catastrophically
///
/// ### Error Handling Verification
/// - **Exception Safety**: Verify that broken collections don't cause resource leaks
/// - **State Consistency**: Ensure that applications maintain consistent state despite collection failures
/// - **Fallback Mechanisms**: Test that backup strategies activate when primary collection operations fail
///
/// @author evanbergstrom
/// @since 1.0.0
/// @see org.soliscode.test.contract Collection contract testing framework
/// @see org.soliscode.test.provider Provider system for automated test generation
package org.soliscode.test.breakable;
