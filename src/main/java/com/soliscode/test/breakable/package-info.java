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
///     .addBreak(BreakableSet.ADD_RETURNS_TRUE_FOR_DUPLICATES)
///     .build();
///
/// brokenSet.add("duplicate");
/// boolean result = brokenSet.add("duplicate"); // Returns true despite duplicate
/// assertEquals(1, brokenSet.size());            // though the set still holds one copy
/// ```
///
/// ### Builder Pattern
///
/// The breakable collections, maps and iterables are configured through a fluent Builder:
/// ```java
/// BreakableList<Integer> complexBrokenList = new BreakableList.Builder<Integer>()
///     .addBreak(BreakableList.GET_ALWAYS_RETURNS_NULL)
///     .addBreak(BreakableCollection.ADD_ALL_SKIPS_FIRST_ELEMENT)
///     .doesNotSupportMethod(CollectionMethods.CLEAR)
///     .doesNotPermitNulls()
///     .build();
/// ```
///
/// ### Inheritance Hierarchy
///
/// The framework follows the standard Collection hierarchy with breakable implementations:
/// ```
/// BreakableObject
///   └── BreakableIterable
///       └── BreakableCollection
///           ├── BreakableSequencedCollection
///           │   └── BreakableList
///           ├── BreakableQueue
///           │   ├── BreakableDeque
///           │   │   └── BreakableBlockingDeque
///           │   └── BreakableBlockingQueue
///           │       └── BreakableTransferQueue
///           └── BreakableSet
///               └── BreakableSequencedSet
///                   └── BreakableSortedSet
///                       └── BreakableNavigableSet
///
/// BreakableMap
///   ├── BreakableSortedMap
///   │   └── BreakableNavigableMap
///   └── BreakableConcurrentMap
///       └── BreakableConcurrentNavigableMap
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
/// **Key Breaks**: Duplicates reported as added, membership query failures
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
/// ### Queue Implementations
///
/// #### BreakableQueue
/// **Purpose**: Queue implementation with FIFO ordering and queue-specific operation violations
/// **Key Breaks**: Offer/poll failures, peek inconsistencies, queue ordering violations
/// **Use Case**: Testing queue-based algorithms and producer-consumer patterns
///
/// #### BreakableDeque
/// **Purpose**: Double-ended queue implementation with bidirectional access violations
/// **Key Breaks**: Head/tail operation failures, bidirectional access inconsistencies
/// **Use Case**: Testing algorithms that require both stack and queue operations
///
/// #### BreakableBlockingQueue
/// **Purpose**: BlockingQueue implementation with blocking operation and timeout violations
/// **Key Breaks**: Blocking operation failures, timeout handling errors, capacity violations
/// **Use Case**: Testing concurrent producer-consumer systems and thread coordination
///
/// #### BreakableBlockingDeque
/// **Purpose**: BlockingDeque implementation combining deque and blocking operation violations
/// **Key Breaks**: Bidirectional blocking failures, timeout inconsistencies, capacity constraint violations
/// **Use Case**: Testing complex concurrent algorithms requiring bidirectional blocking operations
///
/// #### BreakableTransferQueue
/// **Purpose**: TransferQueue implementation with direct transfer operation violations
/// **Key Breaks**: Transfer operation failures, consumer waiting violations, capacity reporting errors
/// **Use Case**: Testing high-performance producer-consumer systems with direct handoff
///
/// ### Map Implementations
///
/// #### BreakableMap
/// **Purpose**: Map implementation with key-value relationship and access violations
/// **Key Breaks**: Get/put failures, containment check errors, view collection inconsistencies
/// **Use Case**: Testing algorithms that depend on reliable key-value mappings
///
/// #### BreakableSortedMap
/// **Purpose**: SortedMap implementation with key ordering and navigation violations
/// **Key Breaks**: Comparator failures, first/last key errors, submap creation failures
/// **Use Case**: Testing sorted map algorithms and range-based operations
///
/// #### BreakableNavigableMap
/// **Purpose**: NavigableMap implementation with advanced navigation method violations
/// **Key Breaks**: Ceiling/floor failures, navigation method inconsistencies, descendant view errors
/// **Use Case**: Testing sophisticated map navigation algorithms and search operations
///
/// #### BreakableConcurrentMap
/// **Purpose**: ConcurrentMap implementation with atomic operation and thread-safety violations
/// **Key Breaks**: Atomic operation failures, race condition simulation, consistency violations
/// **Use Case**: Testing concurrent algorithms and thread-safe map operations
///
/// #### BreakableConcurrentNavigableMap
/// **Purpose**: ConcurrentNavigableMap implementation combining concurrent and navigable operation violations
/// **Key Breaks**: Concurrent navigation failures, atomic navigation operations, thread-safe submap violations
/// **Use Case**: Testing complex concurrent navigation algorithms and thread-safe sorted map operations
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
/// **Key Breaks**: hasNext/next inconsistencies, removal failures, forEachRemaining failures
/// **Use Case**: Testing iterator-dependent code and traversal algorithms
///
/// #### BreakableListIterator
/// **Purpose**: ListIterator implementation, on BreakableIterator, with cursor and modification violations
/// **Key Breaks**: hasPrevious/previous failures, wrong indexes, set and add failures
/// **Use Case**: Testing list-iterator-dependent code, and `ListIteratorContract` (#37)
///
/// #### BreakableSpliterator
/// **Purpose**: Spliterator implementation with parallel processing violations
/// **Key Breaks**: Splitting failures, characteristic misrepresentation, estimation errors, traversal and null-action
/// faults
/// **Use Case**: Testing parallel stream operations and fork-join algorithms, and `SpliteratorContract` (#38)
///
/// #### BreakableSpliteratorOfInt, BreakableSpliteratorOfLong, BreakableSpliteratorOfDouble
/// **Purpose**: Primitive spliterators, on BreakableSpliterator, whose primitive methods take its breaks
/// **Key Breaks**: BreakableSpliterator's; none of their own
/// **Use Case**: The primitive spliterator contracts (#38)
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
/// - **Uniqueness Violations**: Sets report a duplicate element as added
/// - **Ordering Violations**: Sorted collections lose ordering guarantees
/// - **Immutability Violations**: Supposedly immutable views allow modifications
/// - **Exception Contract Violations**: Methods don't throw expected exceptions
///
/// ### Concurrency and Threading Breaks
/// - **Atomicity Violations**: Atomic operations complete non-atomically
/// - **Race Condition Simulation**: Methods simulate concurrent access issues
/// - **Blocking Failures**: Blocking operations return immediately or timeout incorrectly
/// - **Thread Safety Violations**: Thread-safe collections exhibit non-thread-safe behavior
///
/// ## Testing Patterns
///
/// ### Basic Error Handling Testing
/// ```java
/// @Test
/// void testCollectionErrorHandling() {
///     BreakableCollection<String> brokenCollection = new BreakableCollection.Builder<String>()
///         .addBreak(ADD_DOES_NOT_ADD_ELEMENT)
///         .addBreak(SIZE_ALWAYS_RETURNS_ZERO)
///         .build();
///
///     // Code under test that is given this collection sees elements dropped and a size of zero
///     assertFalse(brokenCollection.add("a"));
///     assertEquals(0, brokenCollection.size());
/// }
/// ```
///
/// ### Contract Violation Testing
/// ```java
/// @Test
/// void testSetUniquenessViolation() {
///     BreakableSet<Integer> corruptedSet = new BreakableSet.Builder<Integer>()
///         .addBreak(ADD_RETURNS_TRUE_FOR_DUPLICATES)
///         .build();
///
///     corruptedSet.add(1);
///     assertTrue(corruptedSet.add(1));      // add reports a change for a duplicate
///     assertEquals(1, corruptedSet.size()); // that it did not make
/// }
/// ```
///
/// ### Navigation Algorithm Testing
/// ```java
/// @Test
/// void testNavigationAlgorithmRobustness() {
///     BreakableNavigableSet<Double> brokenNavSet = new BreakableNavigableSet.Builder<Double>()
///         .addElements(1.0, 4.0, 7.0)
///         .addBreak(CEILING_ALWAYS_RETURNS_NULL)
///         .addBreak(FLOOR_RETURNS_CEILING_VALUE)
///         .build();
///
///     assertNull(brokenNavSet.ceiling(5.0));      // instead of 7.0
///     assertEquals(7.0, brokenNavSet.floor(5.0)); // the ceiling, instead of 4.0
/// }
/// ```
///
/// ### Bulk Operation Testing
/// ```java
/// @Test
/// void testBulkOperationResilience() {
///     BreakableList<String> problematicList = new BreakableList.Builder<String>()
///         .addBreak(ADD_ALL_SKIPS_FIRST_ELEMENT)
///         .addBreak(REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS)
///         .build();
///
///     problematicList.addAll(List.of("a", "b", "c"));     // "a" is skipped
///     problematicList.removeAll(List.of("b"));            // nothing is removed
///     assertEquals(List.of("b", "c"), problematicList);
/// }
/// ```
///
/// ### Concurrent Map Testing
/// ```java
/// @Test
/// void testConcurrentMapAtomicOperations() {
///     BreakableConcurrentNavigableMap<String, Integer> brokenConcurrentMap =
///         BreakableConcurrentNavigableMap.<String, Integer>builder()
///             .addBreak(PUT_IF_ABSENT_IGNORES_EXISTING)
///             .addBreak(FIRST_KEY_THROWS_EXCEPTION)
///             .build();
///
///     brokenConcurrentMap.put("a", 1);
///     assertEquals(1, brokenConcurrentMap.putIfAbsent("a", 2)); // replaces the existing value
///     assertEquals(2, brokenConcurrentMap.get("a"));
///     assertThrows(NoSuchElementException.class, brokenConcurrentMap::firstKey);
/// }
/// ```
///
/// ### Blocking Queue Testing
/// ```java
/// @Test
/// void testBlockingQueueTimeouts() throws InterruptedException {
///     BreakableBlockingQueue<String> brokenQueue = new BreakableBlockingQueue.Builder<String>()
///         .addBreak(TAKE_ALWAYS_RETURNS_NULL)
///         .addBreak(OFFER_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE)
///         .build();
///
///     assertFalse(brokenQueue.offer("task", 1, TimeUnit.SECONDS)); // reports a failure, but adds the element
///     assertEquals(1, brokenQueue.size());
///     assertNull(brokenQueue.take());                              // returns null at once
/// }
/// ```
///
/// ## Integration with Testing Framework
///
/// ### Contract Testing
/// The breakable framework integrates with the broader SolisCode Test framework. A contract runs against the
/// collections its provider supplies; it passes for an unbroken collection, and fails when a break it checks is
/// added to the builder:
/// ```java
/// public class CustomCollectionTest extends AbstractTest
///         implements CollectionContract<BreakableCollection<Integer>, Integer>, WithIntegerElement {
///
///     @Override
///     public CollectionProvider<BreakableCollection<Integer>, Integer> provider() {
///         return BreakableCollection.collectionProvider(new BreakableCollection.Builder<>(), elementProvider());
///     }
/// }
/// ```
///
/// ### Provider Integration
/// Breakable collections work with the provider system for automated test generation; each instance the
/// provider creates has the builder's breaks:
/// ```java
/// CollectionProvider<BreakableCollection<String>, String> provider = BreakableCollection.collectionProvider(
///     new BreakableCollection.Builder<String>().addBreak(ADD_ALWAYS_RETURNS_FALSE),
///     Providers.stringProvider());
///
/// BreakableCollection<String> collection = provider.createInstance(List.of("a", "b"));
/// assertFalse(collection.add("c")); // added, but reported as not added
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
/// - **Concurrency Support**: Full support for concurrent collections including ConcurrentMap, BlockingQueue, and their
///   variants
/// - **Navigation Support**: Complete coverage of NavigableSet and NavigableMap with all navigation methods
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
/// @see com.soliscode.test.contract Collection contract testing framework
/// @see com.soliscode.test.provider Provider system for automated test generation
@NullMarked
package com.soliscode.test.breakable;

import org.jspecify.annotations.NullMarked;
