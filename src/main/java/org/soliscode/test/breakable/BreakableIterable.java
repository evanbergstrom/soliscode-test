/*
 * Copyright 2024 Evan Bergstrom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.soliscode.test.breakable;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.MethodStatus;
import org.soliscode.test.contract.iterable.IterableMethods;
import org.soliscode.test.contract.support.CollectionProviderSupport;
import org.soliscode.test.provider.CollectionProvider;
import org.soliscode.test.provider.CollectionProviders;
import org.soliscode.test.provider.ObjectProvider;
import org.soliscode.test.util.IterableTestUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

/// **Breakable Iterable Implementation for Testing**
///
/// This class provides an Iterable implementation that can be programmatically broken for comprehensive
/// testing scenarios. It serves as the foundation of the breakable framework, providing basic iteration
/// functionality that can be configured to violate standard Iterable contracts in controlled ways.
///
/// ## Core Functionality
///
/// As an Iterable implementation, this class provides fundamental iteration capabilities:
/// - **Basic Iteration**: iterator() method for element traversal
/// - **Enhanced Iteration**: forEach() method for functional-style processing
/// - **Parallel Processing**: spliterator() method for stream operations and parallel processing
/// - **Iterator Delegation**: Passes breaks through to Iterator and Spliterator instances
///
/// ### Iterable Semantics
///
/// Under normal operation (no breaks active), BreakableIterable maintains proper Iterable behavior:
/// - **Consistent Iteration**: Multiple iterator() calls return equivalent iteration sequences
/// - **forEach Compliance**: forEach() method processes all elements in iteration order
/// - **Spliterator Support**: spliterator() provides appropriate parallel processing capabilities
/// - **Element Preservation**: All elements are accessible through iteration
///
/// ### Breakable Behavior
///
/// The class introduces several Iterable-specific breaks that can simulate common iteration failures:
///
/// ## Available Breaks
///
/// ### forEach Operation Breaks
///
/// #### FOR_EACH_DOES_NOT_CALL_ACTION
/// **Purpose**: Forces `forEach()` method to accept the action but not call it on any elements
/// **Effect**: Method completes normally but no elements are processed
/// **Use Case**: Testing code that assumes forEach() actually processes elements
///
/// #### FOR_EACH_SKIPS_FIRST_ELEMENT
/// **Purpose**: Forces `forEach()` method to skip the first element during processing
/// **Effect**: First element is omitted from forEach() processing but remains accessible via iterator()
/// **Use Case**: Testing algorithms that depend on complete element processing
///
/// #### FOR_EACH_SKIPS_LAST_ELEMENT
/// **Purpose**: Forces `forEach()` method to skip the last element during processing
/// **Effect**: Last element is omitted from forEach() processing but remains accessible via iterator()
/// **Use Case**: Testing edge-case handling in functional processing
///
/// #### FOR_EACH_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT
/// **Purpose**: Forces `forEach()` method to throw RuntimeException instead of NullPointerException for null actions
/// **Effect**: Violates Iterable contract regarding null argument handling
/// **Use Case**: Testing exception handling robustness when contracts are violated
///
/// ### Inherited Breaks
///
/// This class also supports all breaks from:
/// - **BreakableIterator**: Breaks affecting iterator() method behavior
/// - **BreakableSpliterator**: Breaks affecting spliterator() method behavior
///
/// When iterator or spliterator breaks are added to a BreakableIterable, they are automatically
/// passed through to the Iterator and Spliterator instances created by this iterable.
///
/// ## Builder Pattern Usage
///
/// The class provides a comprehensive Builder for constructing BreakableIterable instances:
///
/// ### Basic Iterable Creation
/// ```java
/// BreakableIterable<String> iterable = new BreakableIterable.Builder<String>()
///     .addElements("first", "second", "third")
///     .build();
/// ```
///
/// ### Iterable with forEach Breaks
/// ```java
/// BreakableIterable<Integer> brokenIterable = new BreakableIterable.Builder<Integer>()
///     .addElements(1, 2, 3, 4, 5)
///     .addBreak(FOR_EACH_SKIPS_FIRST_ELEMENT)
///     .addBreak(FOR_EACH_SKIPS_LAST_ELEMENT)
///     .build();
/// ```
///
/// ### Iterable with Iterator and Spliterator Breaks
/// ```java
/// BreakableIterable<String> complexBroken = new BreakableIterable.Builder<String>()
///     .addElements("a", "b", "c")
///     .addBreak(FOR_EACH_DOES_NOT_CALL_ACTION)           // Iterable break
///     .addBreak(BreakableIterator.ITERATOR_IS_ALWAYS_EMPTY) // Iterator break
///     .addBreak(BreakableSpliterator.SPLITERATOR_IS_ALWAYS_EMPTY) // Spliterator break
///     .setCharacteristics(Spliterator.ORDERED | Spliterator.SIZED)
///     .build();
/// ```
///
/// ## Testing Applications
///
/// ### forEach Processing Testing
/// Test that code handles forEach() method failures appropriately:
///
/// ```java
/// @Test
/// void testForEachProcessingFailures() {
///     BreakableIterable<String> brokenIterable = new BreakableIterable.Builder<String>()
///         .addElements("first", "middle", "last")
///         .addBreak(FOR_EACH_DOES_NOT_CALL_ACTION)
///         .build();
///
///     AtomicInteger processedCount = new AtomicInteger(0);
///     brokenIterable.forEach(item -> processedCount.incrementAndGet());
///
///     assertEquals(0, processedCount.get()); // No elements processed due to break
///
///     // Test that algorithms handle failed forEach operations
///     testFunctionalProcessingWithBrokenForEach(brokenIterable);
/// }
/// ```
///
/// ### Partial forEach Processing Testing
/// Test that code handles incomplete forEach() processing:
///
/// ```java
/// @Test
/// void testPartialForEachProcessing() {
///     BreakableIterable<Integer> partiallyBroken = new BreakableIterable.Builder<Integer>()
///         .addElements(1, 2, 3, 4, 5)
///         .addBreak(FOR_EACH_SKIPS_FIRST_ELEMENT)
///         .addBreak(FOR_EACH_SKIPS_LAST_ELEMENT)
///         .build();
///
///     List<Integer> processed = new ArrayList<>();
///     partiallyBroken.forEach(processed::add_singleElement_returnsTrueAndUpdatesSize);
///
///     assertEquals(Arrays.asList(2, 3, 4), processed); // First and last skipped
///
///     // Verify full iteration still works
///     List<Integer> viaIterator = new ArrayList<>();
///     partiallyBroken.iterator().forEachRemaining(viaIterator::add_singleElement_returnsTrueAndUpdatesSize);
///     assertEquals(Arrays.asList(1, 2, 3, 4, 5), viaIterator);
/// }
/// ```
///
/// ### Exception Handling Testing
/// Test that code handles unexpected exceptions from forEach():
///
/// ```java
/// @Test
/// void testForEachExceptionHandling() {
///     BreakableIterable<String> exceptionThrowing = new BreakableIterable.Builder<String>()
///         .addElements("test")
///         .addBreak(FOR_EACH_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT)
///         .build();
///
///     // Test that wrong exception types are handled gracefully
///     assertThrows(RuntimeException.class, () ->
///         exceptionThrowing.forEach(null)); // Should throw RuntimeException, not NPE
///
///     // Test that exception handling code is robust
///     testExceptionRecoveryStrategies(exceptionThrowing);
/// }
/// ```
///
/// ### Iterator Integration Testing
/// Test that Iterator breaks work correctly when passed through:
///
/// ```java
/// @Test
/// void testIteratorBreakIntegration() {
///     BreakableIterable<String> withIteratorBreaks = new BreakableIterable.Builder<String>()
///         .addElements("a", "b", "c", "d")
///         .addBreak(BreakableIterator.ITERATOR_IS_ALWAYS_EMPTY)
///         .build();
///
///     Iterator<String> iterator = withIteratorBreaks.iterator();
///     assertFalse(iterator.hasNext()); // Iterator break makes it appear empty
///
///     // But forEach should still work (uses direct iterable access)
///     List<String> forEachResults = new ArrayList<>();
///     withIteratorBreaks.forEach(forEachResults::add_singleElement_returnsTrueAndUpdatesSize);
///     assertEquals(Arrays.asList("a", "b", "c", "d"), forEachResults);
/// }
/// ```
///
/// ### Spliterator Integration Testing
/// Test that Spliterator breaks work correctly when passed through:
///
/// ```java
/// @Test
/// void testSpliteratorBreakIntegration() {
///     BreakableIterable<Integer> withSpliteratorBreaks = new BreakableIterable.Builder<Integer>()
///         .addElements(1, 2, 3, 4, 5)
///         .addBreak(BreakableSpliterator.SPLITERATOR_IS_ALWAYS_EMPTY)
///         .setCharacteristics(Spliterator.ORDERED | Spliterator.SIZED)
///         .build();
///
///     Spliterator<Integer> spliterator = withSpliteratorBreaks.spliterator();
///     assertEquals(0, spliterator.estimateSize()); // Spliterator break makes it appear empty
///
///     // Test that stream operations handle empty spliterators
///     long streamCount = StreamSupport.stream(spliterator, false).count();
///     assertEquals(0, streamCount);
///
///     // But direct iteration should still work
///     assertEquals(5, IterableTestUtils.size(withSpliteratorBreaks));
/// }
/// ```
///
/// ### Combined Break Testing
/// Test that multiple break types work together correctly:
///
/// ```java
/// @Test
/// void testCombinedBreakScenarios() {
///     BreakableIterable<String> multiplyBroken = new BreakableIterable.Builder<String>()
///         .addElements("alpha", "beta", "gamma", "delta")
///         .addBreak(FOR_EACH_SKIPS_FIRST_ELEMENT)           // forEach break
///         .addBreak(BreakableIterator.HAS_NEXT_ALWAYS_FALSE) // Iterator break
///         .addBreak(BreakableSpliterator.ESTIMATE_SIZE_ALWAYS_RETURNS_ZERO) // Spliterator break
///         .build();
///
///     // Test forEach behavior (skips first)
///     List<String> forEachResults = new ArrayList<>();
///     multiplyBroken.forEach(forEachResults::add_singleElement_returnsTrueAndUpdatesSize);
///     assertEquals(Arrays.asList("beta", "gamma", "delta"), forEachResults);
///
///     // Test iterator behavior (appears to have no elements)
///     Iterator<String> iterator = multiplyBroken.iterator();
///     assertFalse(iterator.hasNext());
///
///     // Test spliterator behavior (reports size as 0)
///     Spliterator<String> spliterator = multiplyBroken.spliterator();
///     assertEquals(0, spliterator.estimateSize());
///
///     // Test that robust algorithms handle multiple failure modes
///     testMultipleIterationStrategies(multiplyBroken);
/// }
/// ```
///
/// ### Provider Integration Testing
/// Test that BreakableIterable works with the provider system:
///
/// ```java
/// @Test
/// void testProviderIntegration() {
///     ObjectProvider<String> stringProvider = () -> Stream.of("test1", "test2", "test3");
///     CollectionProvider<String, BreakableIterable<String>> provider =
///         BreakableIterable.iterableProvider(stringProvider);
///
///     Stream<BreakableIterable<String>> iterables = provider.collections();
///     List<BreakableIterable<String>> iterableList = iterables.collect(Collectors.toList());
///
///     // Test that provider creates working iterables
///     assertFalse(iterableList.isEmpty());
///     iterableList.forEach(iterable -> {
///         assertNotNull(iterable);
///         assertTrue(IterableTestUtils.size(iterable) >= 0);
///     });
/// }
/// ```
///
/// ## Design Considerations
///
/// ### Thread Safety
/// This class is not thread-safe. The underlying iterable's thread safety characteristics
/// determine the overall thread safety behavior. External synchronization is required for
/// concurrent access.
///
/// ### Performance
/// - **Normal Operations**: Performance depends on the backing iterable (default: ArrayList)
/// - **Broken Operations**: May have additional overhead for break condition checking
/// - **Memory Usage**: Minimal overhead beyond the backing iterable
/// - **Break Delegation**: Efficient pass-through of breaks to Iterator and Spliterator instances
///
/// ### Iteration Consistency
/// The class ensures that breaks are consistently applied across all iteration methods.
/// Iterator and Spliterator instances receive their respective breaks automatically.
///
/// ### Provider Support
/// The class includes static factory methods for creating collection providers that integrate
/// with the SolisCode Test framework's automated test generation system.
///
/// @author evanbergstrom
/// @param <E> The elements type for the `Iterable`.
/// @since 1.0
public class BreakableIterable<E> extends BreakableObject<Iterable<E>> implements Iterable<E> {

    /// The default capacity used for operations that need a constant size value.
    protected static final int DEFAULT_CAPACITY = 10;

    /// The underlying iterable that stores the actual elements.
    private final @NonNull Iterable<E> iterable;

    /// The characteristics flags for the spliterator created by this iterable.
    private final int characteristics;

    /// Default value for the characteristics field
    protected static final int DEFAULT_CHARACTERISTICS = 0;

    /// The method `forEach` does not call the action on any of the elements.
    /// @see BreakableIterable#forEach(Consumer)
    public static final Break FOR_EACH_DOES_NOT_CALL_ACTION =
            new Break("The method `forEach` does not call the action on any of the elements");

    /// The `forEach` method does not call the action on the first element.
    /// @see BreakableIterable#forEach(Consumer)
    public static final Break FOR_EACH_SKIPS_FIRST_ELEMENT =
            new Break("The `forEach` method does not call the action on the first element");

    /// The method `forEach` does not call the action on the last element.
    /// @see BreakableIterable#forEach(Consumer)
    public static final Break FOR_EACH_SKIPS_LAST_ELEMENT =
            new Break("The method `forEach` does not call the action on the last element.");

    /// The method `forEach` throws the wrong exception when passes a `null` action argument.
    /// @see BreakableIterable#forEach(Consumer)
    public static final Break FOR_EACH_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT =
            new Break("The method `forEach` throws the wrong exception when passes a `null` action argument.");

    /// Creates and empty breakable iterable with no breaks. The spliterator for this iterable will have no
    /// characteristics set.
    public BreakableIterable() {
        this(new ArrayList<>(), new HashSet<>(), new HashMap<>(), DEFAULT_CHARACTERISTICS, DEFAULT_SAFETY);
    }

    /// Creates a breakable iterable from an existing instance.
    /// @param other the breakable iterator to copy.
    public BreakableIterable(final @NonNull BreakableIterable<E> other) {
        this(IterableTestUtils.copy(other.iterable), new HashSet<>(other.breaks()),
                new HashMap<>(other.methodStatuses()), other.characteristics, other.isSafe());
    }

    /// Creates a breakable iterable from an iterable.
    /// @param collection the iterator to use for the elements.
    public BreakableIterable(final @NonNull Collection<E> collection) {
        this(new ArrayList<>(collection), new HashSet<>(), new HashMap<>(), DEFAULT_CHARACTERISTICS, DEFAULT_SAFETY);
    }

    /// Creates a breakable iterable from another iterable with a set of breaks and characteristics specified by the
    /// other arguments.
    /// @param i the iterable to use for the elements.
    /// @param breaks the set of breaks to include.
    /// @param methodStatuses the method status configuration.
    /// @param characteristics the characteristics of the iterable.
    /// @param isSafe whether the iterable is safe for concurrent access.
    public BreakableIterable(final @NonNull Iterable<E> i, final @NonNull Set<Break> breaks,
                             final @NonNull Map<InterfaceMethod, MethodStatus> methodStatuses,
                             final int characteristics, final boolean isSafe) {
        super(i, breaks, methodStatuses, isSafe);
        this.iterable = i;
        this.characteristics = characteristics;
    }

    @Override
    protected boolean valueEquals(final BreakableObject<?> other) {
        if (other instanceof BreakableIterable<?> that) {
            return iterable.equals(that.iterable);
        }
        return false;
    }

    /// Implements the [iterator][Iterable#iterator] method from the [Iterable] interface.
    ///
    /// #Breaks
    /// This method can be broken using any of the breaks found in the [BreakableIterator] class.
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Iterable<Integer> iterable = Breakable.iterableOf(1,2,3,4,5)
    ///         .withBreak(BreakableIterator.ITERATOR_IS_ALWAYS_EMPTY)
    ///         .build();
    /// ```
    /// @return An `Iterator` over the elements in this collection.
    @Override
    public @NonNull Iterator<E> iterator() {
        checkMethodSupport(IterableMethods.ITERATOR);

        final BreakableIterator<E> iterator = new BreakableIterator<>(iterable.iterator(), breaks(), methodStatuses(),
                characteristics, isSafe());
        unsupportedMethods().forEach(iterator::doesNotSupportMethod);
        return iterator;
    }

    /// Implements the [forEach][Iterable#forEach] method from the [Iterable] interface. This method can be broken
    /// using the following collection breaks:
    /// - [FOR_EACH_DOES_NOT_CALL_ACTION][BreakableIterable#FOR_EACH_DOES_NOT_CALL_ACTION]
    /// - [FOR_EACH_SKIPS_FIRST_ELEMENT][BreakableIterable#FOR_EACH_SKIPS_FIRST_ELEMENT]
    /// - [FOR_EACH_SKIPS_LAST_ELEMENT][BreakableIterable#FOR_EACH_SKIPS_LAST_ELEMENT]
    /// - [FOR_EACH_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT][BreakableIterable#FOR_EACH_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT]
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakable.iterableOf(1,2,3,4,5)
    ///         .withBreak(FOR_EACH_DOES_NOT_CALL_ACTION)
    ///         .build();
    /// ```
    /// @param action The action to be performed for each element
    /// @throws NullPointerException if the specified action is null
    @Override
    public void forEach(final Consumer<? super E> action) {
        checkMethodSupport(IterableMethods.FOR_EACH);

        if (action == null) {
            if (hasBreak(FOR_EACH_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT)) {
                throw new IllegalArgumentException();
            } else {
                throw new NullPointerException();
            }
        }
        if (!hasBreak(FOR_EACH_DOES_NOT_CALL_ACTION)) {
            if (hasBreak(FOR_EACH_SKIPS_FIRST_ELEMENT)) {
                Iterator<E> iterator = this.iterable.iterator();
                if (iterator.hasNext()) {
                    iterator.next();
                }
                while (iterator.hasNext()) {
                    action.accept(iterator.next());
                }
            } else if (hasBreak(FOR_EACH_SKIPS_LAST_ELEMENT)) {
                Iterator<E> iterator = this.iterable.iterator();
                while (iterator.hasNext()) {
                    E element = iterator.next();
                    if (iterator.hasNext()) {
                        action.accept(element);
                    }
                }
            } else {
                iterable.forEach(action);
            }
        }
    }

    /// Implements the [iterator][Iterable#spliterator] method from the [Iterable] interface. This method can be broken
    ///  using the following collection breaks:
    ///
    /// #Breaks
    /// This method can be broken using any of the breaks found in the [BreakableSpliterator] class.
    ///
    /// A iterable that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Iterable<Integer> iterable = Breakable.iterableOf()
    ///         .addElements(1,2,3,4,5)
    ///         .withBreak(SPLITERATOR_IS_ALWAYS_EMPTY)
    ///         .build();
    /// ```
    /// @return a `Spliterator` over the elements in this collection.Ø
    @Override
    public @NonNull Spliterator<E> spliterator() {
        return new BreakableSpliterator<>(iterable.spliterator(), breaks(), methodStatuses(), characteristics,
                isSafe());
    }

    /// returns the elements as an unbroken instance of `Iterable'
    /// @return an unbroken iterable.
    public @NonNull Iterable<E> unbroken() {
        return iterable;
    }

    /// Utility class for use by subclasses of `BreakableIterator` to make implementing a builder class easier.
    /// @param <B> builder type
    /// @param <C> the subclass of BreakableIterable
    /// @param <E> element type
    /// @author evanbergstrom
    // CHECKSTYLE:OFF: VisibilityModifier
    protected abstract static class AbstractBuilder<B extends AbstractBuilder<B, C, E>,
            C extends BreakableIterable<E>, E> extends AbstractBreakableBuilder<B, C> {

        /// The collection of elements that will be used to create the breakable iterable.
        private final @NonNull Collection<E> elements;

        /// The characteristics flags that will be applied to the spliterator.
        private int characteristics;

        /// Default constructor that creates an empty builder.
        /// @see AbstractBuilder(Collection)
        public AbstractBuilder() {
            this(new ArrayList<>());
        }

        /// Creates a builder with the specified initial elements.
        /// @param elements the initial elements for the builder
        /// @throws NullPointerException if elements is null
        public AbstractBuilder(final @NonNull Collection<E> elements) {
            super();
            this.elements = elements;
            this.characteristics = 0;
        }

        /// Copy constructor that creates a builder from another builder.
        /// @param other the builder to copy from
        /// @throws NullPointerException if other is null
        protected AbstractBuilder(final @NonNull AbstractBuilder<B, C, E> other) {
            super(other);
            this.elements = new ArrayList<>(other.elements);
            this.characteristics = other.characteristics;
        }

        /// Sets the characteristics for the spliterator using bitwise OR with existing characteristics.
        /// @param newCharacteristics the characteristics to add_singleElement_returnsTrueAndUpdatesSize
        /// @return this builder for method chaining
        /// @see Spliterator
        public final B setCharacteristics(final int newCharacteristics) {
            this.characteristics = this.characteristics | newCharacteristics;
            return self();
        }

        /// Adds all elements from the specified iterable to this builder.
        /// @param i the iterable containing elements to add_singleElement_returnsTrueAndUpdatesSize
        /// @return this builder for method chaining
        /// @throws NullPointerException if i is null
        public final B addElements(final @NonNull Iterable<E> i) {
            for (E e : i) {
                elements.add(e);
            }
            return self();
        }

        /// Adds the specified elements to this builder.
        /// @param e the elements to add_singleElement_returnsTrueAndUpdatesSize
        /// @return this builder for method chaining
        /// @throws NullPointerException if e is null
        @SafeVarargs
        public final @NonNull B addElements(final @NonNull E... e) {
            Collections.addAll(elements, e);
            return self();
        }

        /**
         * Returns the elements that have been added to the builder.
         * @return The elements for the collection.
         */
        public @NonNull Collection<E> elements() {
            return elements;
        }

        /**
         * Returns the characteristics that have been added to the builder.
         * @return The characteristics for the collection.
         */
        public int characteristics() {
            return characteristics;
        }
    }
    // CHECKSTYLE:ON: VisibilityModifier


    /// Builder class for `BreakableIterator`.
    /// @param <E> The type of the elements.
    /// @author evanbergstrom
    /// @since 1.0
    public static class Builder<E> extends AbstractBuilder<Builder<E>, BreakableIterable<E>, E> {

        /// Creates a builder for `BreakableIterator`.
        public Builder() {
            super();
        }

        /// Copies a builder for `BreakableIterator`.
        /// @param other the builder to copy.
        public Builder(final @NonNull Builder<E> other) {
            super(other);
        }

        /// {@inheritDoc}
        @Override
        public Builder<E> self() {
            return this;
        }

        /// {@inheritDoc}
        @Override
        public Builder<E> copy() {
            return new Builder<>(this);
        }

        /// Builds a new BreakableIterable instance with the current configuration.
        /// @return a new BreakableIterable instance
        @Override
        public BreakableIterable<E> build() {
            return new BreakableIterable<>(elements(), breaks(), methodStatuses(), characteristics(), isSafe());
        }
    }

    /// Creates a collection provider for instances of BreakableIterable, given an element provider.
    /// @param <E> the element type
    /// @param elementProvider the element provider to use
    /// @return a collection provider for breakable iterables
    /// @throws NullPointerException if elementProvider is null
    public static <E> @NonNull CollectionProvider<E, BreakableIterable<E>> iterableProvider(
            final @NonNull ObjectProvider<E> elementProvider) {
        return CollectionProviders.from(
                BreakableIterable::new,
                BreakableIterable::new,
                BreakableIterable::new,
                elementProvider
        );
    }

    /// Creates a collection provider for instances of BreakableIterable with specific breaks applied.
    /// @param <E> the element type
    /// @param elementProvider the element provider to use
    /// @param breaks the breaks to apply to each instance of BreakableIterable
    /// @return a collection provider for breakable iterables
    /// @throws NullPointerException if elementProvider or breaks is null
    public static <E> @NonNull CollectionProvider<E, BreakableIterable<E>> iterableProvider(
            final @NonNull ObjectProvider<E> elementProvider,
            final @NonNull Set<Break> breaks) {
        return iterableProvider(elementProvider, breaks, Collections.emptyMap());
    }

    /// Creates a collection provider for instances of BreakableIterable with specific breaks applied.
    /// @param <E> the element type
    /// @param elementProvider the element provider to use
    /// @param breaks the breaks to apply to each instance of BreakableIterable
    /// @param methodStatuses the method statuses to apply to each instance of BreakableIterable.
    /// @return a collection provider for breakable iterables
    /// @throws NullPointerException if elementProvider, breaks, or methodStatuses is null
    public static <E> @NonNull CollectionProvider<E, BreakableIterable<E>> iterableProvider(
            final @NonNull ObjectProvider<E> elementProvider,
            final @NonNull Set<Break> breaks,
            final @NonNull Map<InterfaceMethod, MethodStatus> methodStatuses) {
        return CollectionProviders.from(
                () -> new BreakableIterable<>(new ArrayList<>(), breaks, methodStatuses, DEFAULT_CHARACTERISTICS,
                        DEFAULT_SAFETY),
                (o) -> new BreakableIterable<>(IterableTestUtils.copy(o.iterable),
                        new HashSet<>(breaks), new HashMap<>(o.methodStatuses()), o.characteristics, o.isSafe()),
                (c) -> new BreakableIterable<>(c, breaks, methodStatuses, DEFAULT_CHARACTERISTICS, DEFAULT_SAFETY),
                elementProvider
        );
    }

    /// Mixin interface that adds an implementation of the `provider()` method that provides instances of
    /// `BreakableIterable` that do not have any breaks applied.
    /// @param <E> element type
    public interface WithProvider<E> extends CollectionProviderSupport<E, BreakableIterable<E>>  {
        /// Provides a collection provider for BreakableIterable instances.
        /// @return a collection provider for breakable iterables
        @Override
        default @NonNull CollectionProvider<E, BreakableIterable<E>> provider() {
            return BreakableIterable.iterableProvider(elementProvider());
        }
    }

    /// Retrieves the characteristics of this BreakableIterable instance. These characteristics
    /// represent the behavior and properties of the iterable and its associated spliterator.
    /// Characteristics are encoded as a bitmask, where each bit represents a specific property.
    ///
    /// @return an integer encoding the characteristics of the iterable. The value may include
    ///         properties such as orderedness, distinctiveness, immutability, or concurrency safety
    ///         as defined by the BreakableIterable implementation.
    protected int characteristics() {
        return characteristics;
    }
}
