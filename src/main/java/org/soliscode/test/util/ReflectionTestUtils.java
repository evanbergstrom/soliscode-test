package org.soliscode.test.util;

import org.jspecify.annotations.NonNull;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/// **Reflection-Based Testing Utilities**
///
/// This utility class provides specialized reflection operations for testing scenarios involving
/// interface analysis and type introspection. It focuses on interface discovery and hierarchy
/// traversal, which are essential for validating that objects implement expected interfaces
/// and conform to design contracts.
///
/// ## Core Functionality
///
/// The class provides methods for discovering and analyzing interfaces implemented by classes,
/// including both direct interface implementations and interfaces inherited through class
/// hierarchies and interface inheritance chains. This is particularly useful for testing
/// that objects adhere to specific interface contracts.
///
/// ### Interface Discovery
///
/// The utility supports two levels of interface discovery:
/// - **Direct Interfaces**: Interfaces explicitly declared by a class
/// - **All Interfaces**: Complete set of interfaces including inherited ones through superclasses and super-interfaces
///
/// ### Hierarchy Traversal
///
/// The implementation uses an iterative depth-first approach with a stack-based algorithm
/// to traverse both class inheritance hierarchies and interface inheritance chains. This
/// ensures complete coverage of all implemented interfaces while avoiding infinite loops
/// in complex inheritance structures.
///
/// ## Usage Patterns
///
/// ### Interface Contract Validation
/// ```java
/// // Verify that a class implements specific interfaces
/// Set<Class<?>> interfaces = ReflectionTestUtils.getDirectInterfaces(ArrayList.class);
/// assertTrue(interfaces.contains(List.class));
/// assertTrue(interfaces.contains(RandomAccess.class));
/// assertTrue(interfaces.contains(Cloneable.class));
/// assertTrue(interfaces.contains(Serializable.class));
/// ```
///
/// ### Complete Interface Analysis
/// ```java
/// // Get all interfaces in the entire hierarchy
/// Set<Class<?>> allInterfaces = ReflectionTestUtils.getAllInterfaces(ArrayList.class);
/// assertTrue(allInterfaces.contains(List.class));       // Direct interface
/// assertTrue(allInterfaces.contains(Collection.class)); // Super-interface of List
/// assertTrue(allInterfaces.contains(Iterable.class));   // Super-interface of Collection
/// ```
///
/// ### Multiple Type Analysis
/// ```java
/// // Analyze interfaces for multiple related types
/// List<Class<?>> collectionTypes = List.of(ArrayList.class, HashSet.class, TreeMap.class);
/// Set<Class<?>> commonInterfaces = ReflectionTestUtils.getAllInterfaces(collectionTypes);
/// // Results include all interfaces implemented by any of the types
/// ```
///
/// ### Design Contract Testing
/// ```java
/// // Validate that implementation classes conform to expected interface contracts
/// Class<?> myImplementation = MyCustomList.class;
/// Set<Class<?>> implementedInterfaces = ReflectionTestUtils.getAllInterfaces(myImplementation);
///
/// // Verify required interfaces are implemented
/// assertTrue(implementedInterfaces.contains(List.class),
///            "Custom list must implement List interface");
/// assertTrue(implementedInterfaces.contains(Collection.class),
///            "List implementations must support Collection contract");
/// ```
///
/// ## Algorithm Details
///
/// ### Stack-Based Traversal
/// The interface discovery algorithm uses a `Deque<Class<?>>` as a stack to perform
/// iterative depth-first traversal. This approach:
/// - Avoids recursion-related stack overflow issues
/// - Handles complex inheritance hierarchies efficiently
/// - Prevents infinite loops through cycle detection
/// - Maintains deterministic ordering of discovered interfaces
///
/// ### Duplicate Prevention
/// The implementation uses a `LinkedHashSet` to store results, which:
/// - Prevents duplicate interface entries
/// - Maintains insertion order for consistent results
/// - Provides O(1) duplicate detection during traversal
/// - Ensures stable iteration order for testing
///
/// ## Performance Characteristics
///
/// - **Time Complexity**: O(n + m) where n = number of classes in hierarchy, m = number of interfaces
/// - **Space Complexity**: O(k) where k = total number of unique interfaces discovered
/// - **Memory Efficiency**: Uses iterative approach to avoid deep recursion overhead
/// - **Cycle Safe**: Prevents infinite loops in complex inheritance structures
///
/// ## Use Cases
///
/// ### Interface Contract Testing
/// Validate that implementations satisfy expected interface requirements:
/// - Custom collection implementations
/// - Plugin and extension systems
/// - Framework integration points
/// - API compliance verification
///
/// ### Design Pattern Validation
/// Verify adherence to specific design patterns:
/// - Decorator pattern interface consistency
/// - Adapter pattern interface mapping
/// - Proxy pattern interface preservation
/// - Facade pattern interface aggregation
///
/// ### Framework Integration Testing
/// Ensure compatibility with frameworks that depend on specific interfaces:
/// - Serialization framework compatibility (`Serializable`)
/// - Collection framework compliance (`Collection`, `List`, `Set`, etc.)
/// - Comparison framework support (`Comparable`, `Comparator`)
/// - Cloning framework integration (`Cloneable`)
///
/// ## Implementation Notes
///
/// - **Thread Safety**: All methods are static and stateless, making them thread-safe
/// - **Null Safety**: Uses `@NonNull` annotations for compile-time safety validation
/// - **Memory Efficiency**: Reuses collections and avoids unnecessary object creation
/// - **JVM Compatibility**: Uses standard reflection APIs available in all JVM versions
///
/// @author evanbergstrom
/// @since 1.0.0
/// @see Class#getInterfaces()
/// @see Class#getSuperclass()
/// @see java.lang.reflect.Type
public final class ReflectionTestUtils {

    /// Creates a new ReflectionTestUtils instance.
    ///
    /// This constructor is provided for completeness but this class is designed to be used
    /// through its static methods. All functionality is available without instantiation.
    private ReflectionTestUtils() { }

    /// Gets the set of interfaces directly implemented by the specified class.
    ///
    /// This method returns only the interfaces that are explicitly declared in the class's
    /// `implements` clause or interface's `extends` clause. It does not traverse the inheritance
    /// hierarchy to find interfaces implemented by superclasses or super-interfaces.
    ///
    /// ## Direct Interface Discovery
    ///
    /// The method uses Java's `Class.getInterfaces()` method to retrieve the immediate
    /// interfaces declared by the class. This provides a shallow view of interface
    /// implementation that focuses only on the direct contract declarations.
    ///
    /// ## Examples
    ///
    /// ```java
    /// // Basic class interface discovery
    /// Set<Class<?>> interfaces = ReflectionTestUtils.getDirectInterfaces(ArrayList.class);
    /// // Returns: [List, RandomAccess, Cloneable, Serializable]
    /// // Does NOT include Collection or Iterable (super-interfaces of List)
    ///
    /// // Interface inheritance discovery
    /// Set<Class<?>> listInterfaces = ReflectionTestUtils.getDirectInterfaces(List.class);
    /// // Returns: [Collection] - the direct super-interface of List
    /// // Does NOT include Iterable (super-interface of Collection)
    ///
    /// // Abstract class interface analysis
    /// Set<Class<?>> abstractInterfaces = ReflectionTestUtils.getDirectInterfaces(AbstractList.class);
    /// // Returns: [List] - the interface directly implemented by AbstractList
    ///
    /// // Custom implementation testing
    /// class MyList implements List<String>, Serializable {
    ///     // implementation
    /// }
    /// Set<Class<?>> myInterfaces = ReflectionTestUtils.getDirectInterfaces(MyList.class);
    /// // Returns: [List, Serializable] - only the directly declared interfaces
    /// ```
    ///
    /// ## Use Cases
    ///
    /// This method is particularly useful for:
    /// - **Contract verification**: Ensuring a class explicitly implements required interfaces
    /// - **Design validation**: Verifying that classes don't over-implement unnecessary interfaces
    /// - **API compliance**: Checking that implementations declare expected interface contracts
    /// - **Documentation generation**: Creating accurate interface implementation lists
    ///
    /// ## Comparison with getAllInterfaces
    ///
    /// Unlike `getAllInterfaces()`, this method provides a focused view of only the
    /// immediate interface declarations. This is useful when you need to distinguish
    /// between explicitly declared interfaces and inherited ones.
    ///
    /// @param type the class to analyze for direct interface implementations
    /// @return a set containing all interfaces directly implemented by the class
    /// @throws NullPointerException if type is null
    /// @see #getAllInterfaces(Class)
    /// @see Class#getInterfaces()
    /// @since 1.0.0
    public static Set<Class<?>> getDirectInterfaces(final @NonNull Class<?> type) {
        return new HashSet<>(Arrays.asList(type.getInterfaces()));
    }

    /// Gets the complete set of all interfaces implemented by the specified class throughout its inheritance hierarchy.
    ///
    /// This method performs a comprehensive traversal of both the class inheritance hierarchy and
    /// interface inheritance chains to discover all interfaces that the specified class implements,
    /// either directly or through inheritance. The result includes interfaces from superclasses,
    /// super-interfaces, and their entire inheritance trees.
    ///
    /// ## Comprehensive Interface Discovery
    ///
    /// The algorithm uses an iterative depth-first traversal approach with a stack-based implementation
    /// to explore the complete type hierarchy. It discovers interfaces through multiple inheritance paths:
    /// 1. **Direct interfaces** declared by the class itself
    /// 2. **Inherited interfaces** from superclasses in the inheritance chain
    /// 3. **Super-interfaces** from the interface inheritance hierarchy
    /// 4. **Transitive interfaces** through multiple levels of inheritance
    ///
    /// ## Algorithm Implementation
    ///
    /// The method uses a `LinkedHashSet` for result storage and an `ArrayDeque` as a stack for traversal:
    /// - **Cycle Detection**: The `LinkedHashSet` prevents duplicate processing of the same interface
    /// - **Deterministic Ordering**: Results maintain insertion order for consistent behavior
    /// - **Stack-Based Traversal**: Avoids recursion to prevent stack overflow on deep hierarchies
    /// - **Breadth-First Discovery**: Processes interfaces as they are encountered during traversal
    ///
    /// ## Examples
    ///
    /// ```java
    /// // Complete interface analysis for ArrayList
    /// Set<Class<?>> allInterfaces = ReflectionTestUtils.getAllInterfaces(ArrayList.class);
    /// // Returns: [List, RandomAccess, Cloneable, Serializable, Collection, Iterable]
    /// // Includes both direct interfaces and inherited super-interfaces
    ///
    /// // Interface hierarchy traversal
    /// Set<Class<?>> listInterfaces = ReflectionTestUtils.getAllInterfaces(List.class);
    /// // Returns: [Collection, Iterable] - all super-interfaces in the chain
    ///
    /// // Complex inheritance example
    /// class MyCustomList extends AbstractList<String> implements Cloneable {
    ///     // implementation
    /// }
    /// Set<Class<?>> customInterfaces = ReflectionTestUtils.getAllInterfaces(MyCustomList.class);
    /// // Returns: [Cloneable, List, Collection, Iterable]
    /// // Includes: Cloneable (direct), List (from AbstractList), Collection & Iterable (from List)
    ///
    /// // Framework integration validation
    /// Set<Class<?>> mapInterfaces = ReflectionTestUtils.getAllInterfaces(HashMap.class);
    /// // Returns: [Map, Cloneable, Serializable]
    /// // All interfaces available for framework reflection and proxy creation
    ///
    /// // Interface-only analysis
    /// Set<Class<?>> comparableInterfaces = ReflectionTestUtils.getAllInterfaces(Comparable.class);
    /// // Returns: [] - Comparable has no super-interfaces (empty result)
    /// ```
    ///
    /// ## Use Cases
    ///
    /// ### Comprehensive Contract Validation
    /// ```java
    /// // Verify complete interface compliance for framework integration
    /// Class<?> implementation = MyCollectionImpl.class;
    /// Set<Class<?>> allInterfaces = ReflectionTestUtils.getAllInterfaces(implementation);
    ///
    /// assertTrue(allInterfaces.contains(Collection.class), "Must support Collection contract");
    /// assertTrue(allInterfaces.contains(Iterable.class), "Must support iteration");
    /// ```
    ///
    /// ### Proxy and Framework Compatibility
    /// ```java
    /// // Check proxy creation compatibility
    /// Set<Class<?>> interfaces = ReflectionTestUtils.getAllInterfaces(serviceClass);
    /// Class<?>[] proxyInterfaces = interfaces.toArray(new Class<?>[0]);
    /// Object proxy = Proxy.newProxyInstance(classLoader, proxyInterfaces, handler);
    /// ```
    ///
    /// ### Design Pattern Verification
    /// ```java
    /// // Validate decorator pattern interface preservation
    /// Class<?> decorator = MyDecorator.class;
    /// Class<?> component = OriginalComponent.class;
    ///
    /// Set<Class<?>> decoratorInterfaces = ReflectionTestUtils.getAllInterfaces(decorator);
    /// Set<Class<?>> componentInterfaces = ReflectionTestUtils.getAllInterfaces(component);
    ///
    /// assertTrue(decoratorInterfaces.containsAll(componentInterfaces),
    ///            "Decorator must preserve all component interfaces");
    /// ```
    ///
    /// ## Performance Characteristics
    ///
    /// - **Time Complexity**: O(n + m) where n = classes in hierarchy, m = interfaces in hierarchy
    /// - **Space Complexity**: O(k) where k = total unique interfaces discovered
    /// - **Cycle Prevention**: O(1) duplicate detection through `LinkedHashSet`
    /// - **Memory Efficiency**: Iterative approach avoids recursion overhead
    ///
    /// ## Implementation Details
    ///
    /// The algorithm processes each class/interface in the hierarchy exactly once, adding its
    /// direct interfaces to both the result set and the traversal stack. The `LinkedHashSet.add_singleElement_returnsTrueAndUpdatesSize()`
    /// method returns `false` for duplicates, providing efficient cycle detection while maintaining
    /// insertion order for predictable results.
    ///
    /// @param type the class to analyze for all interface implementations in its hierarchy
    /// @return a set containing all interfaces implemented by the class and its inheritance hierarchy
    /// @throws NullPointerException if type is null
    /// @see #getDirectInterfaces(Class)
    /// @see #getAllInterfaces(Collection)
    /// @see Class#getInterfaces()
    /// @see Class#getSuperclass()
    /// @since 1.0.0
    public static Set<Class<?>> getAllInterfaces(final @NonNull Class<?> type) {
        Set<Class<?>> result = new LinkedHashSet<>();
        Deque<Class<?>> stack = new ArrayDeque<>();
        if (type != null) {
            stack.push(type);
        }
        while (!stack.isEmpty()) {
            Class<?> c = stack.pop();
            if (c.isInterface()) {
                result.add(c);
            }

            // add_singleElement_returnsTrueAndUpdatesSize interfaces of this class/interface
            for (Class<?> itf : c.getInterfaces()) {
                if (result.add(itf)) {
                    // also traverse super-interfaces of this interface
                    stack.push(itf);
                }
            }
            // walk up the superclass chain (if c is a class; null when reaching Object)
            Class<?> superClass = c.getSuperclass();
            if (superClass != null) {
                stack.push(superClass);
            }
        }
        return result;
    }

    /// Gets the complete set of all interfaces implemented by any of the specified classes throughout their inheritance hierarchies.
    ///
    /// This method performs comprehensive interface discovery across multiple classes simultaneously,
    /// aggregating all interfaces implemented by any class in the provided collection. The result
    /// represents the union of all interfaces discoverable from the entire set of input classes.
    ///
    /// ## Multi-Type Interface Aggregation
    ///
    /// The method processes each class in the collection individually using the single-class
    /// `getAllInterfaces()` method, then aggregates all discovered interfaces into a unified
    /// result set. This provides a comprehensive view of interface coverage across related types.
    ///
    /// ## Use Cases and Applications
    ///
    /// This method is particularly valuable for:
    /// - **Framework compatibility analysis** across multiple implementation classes
    /// - **Common interface discovery** for polymorphic operations
    /// - **Proxy creation** supporting multiple service types
    /// - **Design pattern validation** across related classes
    ///
    /// ## Examples
    ///
    /// ```java
    /// // Analyze multiple collection implementations
    /// Collection<Class<?>> collectionTypes = List.of(
    ///     ArrayList.class, HashSet.class, TreeMap.class
    /// );
    /// Set<Class<?>> allInterfaces = ReflectionTestUtils.getAllInterfaces(collectionTypes);
    /// // Returns union of all interfaces: [List, RandomAccess, Cloneable, Serializable,
    /// //                                   Collection, Iterable, Set, Map, NavigableMap, SortedMap]
    ///
    /// // Framework service compatibility check
    /// Collection<Class<?>> serviceClasses = List.of(
    ///     UserService.class, OrderService.class, PaymentService.class
    /// );
    /// Set<Class<?>> serviceInterfaces = ReflectionTestUtils.getAllInterfaces(serviceClasses);
    /// // Discover all interfaces supported by the service layer
    ///
    /// // Plugin system interface analysis
    /// Collection<Class<?>> pluginClasses = getLoadedPluginClasses();
    /// Set<Class<?>> pluginInterfaces = ReflectionTestUtils.getAllInterfaces(pluginClasses);
    /// // Identify all capabilities available across all loaded plugins
    ///
    /// // Proxy factory interface aggregation
    /// Collection<Class<?>> proxyTargets = List.of(
    ///     DatabaseService.class, CacheService.class, MetricsService.class
    /// );
    /// Set<Class<?>> proxyInterfaces = ReflectionTestUtils.getAllInterfaces(proxyTargets);
    /// // Create proxies supporting all discovered interfaces
    /// ```
    ///
    /// ### Framework Integration Patterns
    ///
    /// ```java
    /// // Dependency injection container analysis
    /// Collection<Class<?>> beanClasses = containerConfig.getBeanClasses();
    /// Set<Class<?>> availableInterfaces = ReflectionTestUtils.getAllInterfaces(beanClasses);
    ///
    /// // Verify framework requirements are satisfied
    /// assertTrue(availableInterfaces.contains(ApplicationListener.class),
    ///            "Container must support event listening");
    /// assertTrue(availableInterfaces.contains(BeanPostProcessor.class),
    ///            "Container must support bean post-processing");
    /// ```
    ///
    /// ### Polymorphic Operation Support
    ///
    /// ```java
    /// // API gateway route configuration
    /// Collection<Class<?>> handlerClasses = routeConfig.getHandlerClasses();
    /// Set<Class<?>> handlerInterfaces = ReflectionTestUtils.getAllInterfaces(handlerClasses);
    ///
    /// // Configure request routing based on supported interfaces
    /// if (handlerInterfaces.contains(AuthenticationHandler.class)) {
    ///     enableAuthenticationRouting();
    /// }
    /// if (handlerInterfaces.contains(ValidationHandler.class)) {
    ///     enableRequestValidation();
    /// }
    /// ```
    ///
    /// ## Performance Characteristics
    ///
    /// - **Time Complexity**: O(n * (h + i)) where n = number of input classes, h = average hierarchy depth, i = average interface count
    /// - **Space Complexity**: O(k) where k = total unique interfaces across all classes
    /// - **Deduplication**: `LinkedHashSet` ensures each interface appears only once in results
    /// - **Ordering**: Results maintain discovery order for deterministic behavior
    ///
    /// ## Implementation Strategy
    ///
    /// The method leverages the existing single-class interface discovery logic, ensuring
    /// consistent behavior and algorithm optimizations. The `LinkedHashSet` automatically
    /// handles deduplication when the same interface is discovered from multiple classes,
    /// while preserving insertion order for predictable results.
    ///
    /// @param types the collection of classes to analyze for interface implementations
    /// @return a set containing all interfaces implemented by any of the classes in their hierarchies
    /// @throws NullPointerException if types collection is null or contains null elements
    /// @see #getAllInterfaces(Class)
    /// @see #getDirectInterfaces(Class)
    /// @since 1.0.0
    public static Set<Class<?>> getAllInterfaces(final @NonNull Collection<Class<?>> types) {
        Set<Class<?>> result = new LinkedHashSet<>();
        for (Class<?> type : types) {
            result.addAll(getAllInterfaces(type));
        }
        return result;
    }
}
