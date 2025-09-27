package org.soliscode.test.breakable;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.contract.CollectionMethods;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SequencedCollection;

/// **Breakable SequencedCollection Implementation for Testing**
///
/// This class provides a SequencedCollection implementation that can be programmatically broken
/// for comprehensive testing scenarios. It extends BreakableCollection to maintain all standard
/// collection semantics while adding sequence-specific operations (addFirst, addLast, getFirst,
/// getLast, removeFirst, removeLast) that can be individually configured to fail in various ways.
///
/// ## Core Functionality
///
/// As a SequencedCollection implementation, this class supports all standard collection operations
/// plus sequence-specific operations that provide ordered access to elements. The class can be
/// configured to break these sequence-specific contracts through targeted breaks while maintaining
/// compatibility with the broader collection testing framework.
///
/// ### SequencedCollection Semantics
///
/// Under normal operation (no breaks active), BreakableSequencedCollection maintains proper SequencedCollection behavior:
/// - **Ordered Access**: getFirst() and getLast() return the first and last elements respectively
/// - **Ordered Insertion**: addFirst() and addLast() insert elements at specific positions
/// - **Ordered Removal**: removeFirst() and removeLast() remove and return specific elements
/// - **Sequence Reversal**: reversed() returns a view with elements in reverse order
/// - **Exception Handling**: Proper NoSuchElementException throwing for empty collections
///
/// ### Breakable Behavior
///
/// The class introduces SequencedCollection-specific breaks organized into several categories:
///
/// ## Available Breaks
///
/// ### Positional Addition Breaks
///
/// #### ADD_FIRST_DOES_NOT_ADD_ELEMENT
/// **Purpose**: Forces `addFirst()` method to accept elements but not actually add them to the collection
/// **Effect**: Method completes normally but collection remains unchanged
/// **Use Case**: Testing code that assumes successful addFirst operations modify the collection
///
/// #### ADD_FIRST_ADDS_TO_END
/// **Purpose**: Forces `addFirst()` method to add elements to the end instead of the beginning
/// **Effect**: Sequence order is violated, elements appear at the wrong position
/// **Use Case**: Testing code robustness when positional guarantees are broken
///
/// #### ADD_LAST_DOES_NOT_ADD_ELEMENT
/// **Purpose**: Forces `addLast()` method to accept elements but not actually add them to the collection
/// **Effect**: Method completes normally but collection remains unchanged
/// **Use Case**: Testing code that assumes successful addLast operations modify the collection
///
/// #### ADD_LAST_ADDS_TO_FRONT
/// **Purpose**: Forces `addLast()` method to add elements to the front instead of the end
/// **Effect**: Sequence order is violated, elements appear at the wrong position
/// **Use Case**: Testing code robustness when positional guarantees are broken
///
/// ### Positional Access Breaks
///
/// #### GET_FIRST_RETURNS_NULL
/// **Purpose**: Forces `getFirst()` method to return null instead of the first element
/// **Effect**: Violates SequencedCollection contract that should return elements or throw exceptions
/// **Use Case**: Testing code handling of unexpected null returns from access methods
///
/// #### GET_FIRST_ALWAYS_THROWS
/// **Purpose**: Forces `getFirst()` method to always throw NoSuchElementException
/// **Effect**: Method throws exceptions even when elements exist in the collection
/// **Use Case**: Testing error handling when access methods fail unexpectedly
///
/// #### GET_FIRST_SKIPS_FIRST_ELEMENT
/// **Purpose**: Forces `getFirst()` method to skip the actual first element and return the second
/// **Effect**: First element becomes inaccessible via getFirst() method
/// **Use Case**: Testing code handling when positional access is unreliable
///
/// #### GET_LAST_RETURNS_NULL
/// **Purpose**: Forces `getLast()` method to return null instead of the last element
/// **Effect**: Violates SequencedCollection contract that should return elements or throw exceptions
/// **Use Case**: Testing code handling of unexpected null returns from access methods
///
/// #### GET_LAST_ALWAYS_THROWS
/// **Purpose**: Forces `getLast()` method to always throw NoSuchElementException
/// **Effect**: Method throws exceptions even when elements exist in the collection
/// **Use Case**: Testing error handling when access methods fail unexpectedly
///
/// #### GET_LAST_SKIPS_LAST_ELEMENT
/// **Purpose**: Forces `getLast()` method to skip the actual last element and return the second-to-last
/// **Effect**: Last element becomes inaccessible via getLast() method
/// **Use Case**: Testing code handling when positional access is unreliable
///
/// ### Positional Removal Breaks
///
/// #### REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT
/// **Purpose**: Forces `removeFirst()` method to return elements without actually removing them
/// **Effect**: Elements remain in collection despite successful method completion
/// **Use Case**: Testing assumptions about removal operation effectiveness
///
/// #### REMOVE_FIRST_RETURNS_NULL
/// **Purpose**: Forces `removeFirst()` method to return null instead of the removed element
/// **Effect**: Violates SequencedCollection contract for non-empty collections
/// **Use Case**: Testing code handling of unexpected null returns from removal methods
///
/// #### REMOVE_FIRST_ALWAYS_THROWS
/// **Purpose**: Forces `removeFirst()` method to always throw NoSuchElementException
/// **Effect**: Method throws exceptions even when elements exist to remove
/// **Use Case**: Testing error handling when removal methods fail unexpectedly
///
/// #### REMOVE_LAST_DOES_NOT_REMOVE_ELEMENT
/// **Purpose**: Forces `removeLast()` method to return elements without actually removing them
/// **Effect**: Elements remain in collection despite successful method completion
/// **Use Case**: Testing assumptions about removal operation effectiveness
///
/// #### REMOVE_LAST_RETURNS_NULL
/// **Purpose**: Forces `removeLast()` method to return null instead of the removed element
/// **Effect**: Violates SequencedCollection contract for non-empty collections
/// **Use Case**: Testing code handling of unexpected null returns from removal methods
///
/// #### REMOVE_LAST_ALWAYS_THROWS
/// **Purpose**: Forces `removeLast()` method to always throw NoSuchElementException
/// **Effect**: Method throws exceptions even when elements exist to remove
/// **Use Case**: Testing error handling when removal methods fail unexpectedly
///
/// ### Sequence View Breaks
///
/// #### REVERSED_DOES_NOT_REVERSE_COLLECTION
/// **Purpose**: Forces `reversed()` method to return the original collection instead of a reversed view
/// **Effect**: Sequence reversal operation has no effect, maintaining original order
/// **Use Case**: Testing code that depends on reversed views having different iteration order
///
/// #### REVERSED_MODIFIES_THE_COLLECTION
/// **Purpose**: Forces `reversed()` method to modify the original collection instead of returning a view
/// **Effect**: Violates SequencedCollection contract that reversed() should return views
/// **Use Case**: Testing code handling when view operations have unexpected side effects
///
/// ## Builder Pattern Usage
///
/// The class provides a comprehensive Builder for constructing BreakableSequencedCollection instances:
///
/// ### Basic SequencedCollection Creation
/// ```java
/// BreakableSequencedCollection<String> collection = new BreakableSequencedCollection.Builder<String>()
///     .build();
/// ```
///
/// ### SequencedCollection with Breaks
/// ```java
/// BreakableSequencedCollection<Integer> brokenCollection = new BreakableSequencedCollection.Builder<Integer>()
///     .withBreak(ADD_FIRST_ADDS_TO_END)
///     .withBreak(GET_FIRST_RETURNS_NULL)
///     .build();
/// ```
///
/// ### SequencedCollection with Initial Elements
/// ```java
/// BreakableSequencedCollection<String> collection = new BreakableSequencedCollection.Builder<String>()
///     .addElements(Arrays.asList("first", "second", "third"))
///     .withBreak(REMOVE_LAST_DOES_NOT_REMOVE_ELEMENT)
///     .build();
/// ```
///
/// ## Inheritance Support
///
/// This class extends BreakableCollection and supports all collection, iterator, and spliterator breaks:
/// - Collection breaks affect standard collection operations (add, remove, contains, size, etc.)
/// - Iterator breaks are passed through to created iterators
/// - Spliterator breaks affect spliterator behavior
/// - SequencedCollection-specific breaks can be combined with inherited breaks
///
/// ## Testing Applications
///
/// ### Positional Addition Testing
/// ```java
/// @Test
/// void testAddFirstBrokenBehavior() {
///     BreakableSequencedCollection<String> collection = new BreakableSequencedCollection.Builder<String>()
///         .withBreak(ADD_FIRST_ADDS_TO_END)
///         .build();
///     
///     collection.addFirst("first");
///     collection.addFirst("second");
///     
///     assertEquals("first", collection.getFirst());  // "second" went to end instead
///     assertEquals("second", collection.getLast());  // Unexpected behavior
/// }
/// ```
///
/// ### Access Method Testing
/// ```java
/// @Test
/// void testGetFirstNullReturn() {
///     BreakableSequencedCollection<Integer> collection = new BreakableSequencedCollection.Builder<Integer>()
///         .withBreak(GET_FIRST_RETURNS_NULL)
///         .build();
///     
///     collection.addFirst(1);
///     collection.addFirst(2);
///     
///     assertNull(collection.getFirst());      // Returns null despite elements
///     assertEquals(2, collection.size());     // But collection has elements
/// }
/// ```
///
/// ### Removal Method Testing
/// ```java
/// @Test
/// void testRemoveFirstDoesNotRemove() {
///     BreakableSequencedCollection<String> collection = new BreakableSequencedCollection.Builder<String>()
///         .withBreak(REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT)
///         .build();
///     
///     collection.addFirst("test");
///     String removed = collection.removeFirst();
///     
///     assertEquals("test", removed);           // Returns the element
///     assertEquals(1, collection.size());     // But doesn't remove it
///     assertTrue(collection.contains("test"));// Element still present
/// }
/// ```
///
/// ### Sequence Reversal Testing
/// ```java
/// @Test
/// void testReversedModifiesCollection() {
///     BreakableSequencedCollection<Integer> collection = new BreakableSequencedCollection.Builder<Integer>()
///         .withBreak(REVERSED_MODIFIES_THE_COLLECTION)
///         .build();
///     
///     collection.addLast(1);
///     collection.addLast(2);
///     collection.addLast(3);
///     
///     SequencedCollection<Integer> reversed = collection.reversed();
///     
///     // Original collection was modified instead of creating view
///     assertEquals(3, collection.getFirst()); // Original collection reversed
///     assertSame(collection, reversed);       // Same instance returned
/// }
/// ```
///
/// ## Optional Method Support
///
/// This class supports optional method configuration using the OptionalMethod system:
/// - SequencedCollection methods can be disabled to simulate unsupported operations
/// - UnsupportedOperationException is thrown for disabled methods
/// - Useful for testing code that handles optional sequenced collection methods
///
/// ## Type Safety Configuration
///
/// The class inherits configurable type safety from BreakableCollection:
/// - **Null Handling**: Can be configured to accept or reject null elements
/// - **Duplicate Handling**: Can be configured to accept or reject duplicate elements
/// - **Type Compatibility**: Can be configured to accept or reject incompatible types
///
/// ## Design Considerations
///
/// ### Thread Safety
/// This class is not thread-safe. The underlying List's thread safety characteristics
/// determine the overall thread safety behavior. External synchronization is required for
/// concurrent access.
///
/// ### Performance
/// - **Normal Operations**: Performance depends on the backing List (default: ArrayList)
/// - **Broken Operations**: May have additional overhead for break condition checking
/// - **Memory Usage**: Minimal overhead beyond the backing List and inherited collection
///
/// ### Sequence Ordering
/// The class maintains sequence ordering through an underlying List implementation,
/// ensuring that positional operations work correctly when not broken.
///
/// @param <E> the type of elements maintained by this sequenced collection
/// @author evanbergstrom
/// @since 1.0.0
/// @see CollectionMethods
/// @see BreakableCollection
/// @see java.util.SequencedCollection
public class BreakableSequencedCollection<E> extends BreakableCollection<E> implements SequencedCollection<E> {

    private final @NonNull List<E> sequenced;

    /// The [addFirst][SequencedCollection#addFirst] method does not add an element.
    /// @see BreakableSequencedCollection#addFirst(Object)
    public static final Break ADD_FIRST_DOES_NOT_ADD_ELEMENT = new Break("addFirst does not add element");

    /// The [addFirst][SequencedCollection#addFirst] method adds the element to the end of the collection.
    /// @see BreakableSequencedCollection#addFirst(Object)
    public static final Break ADD_FIRST_ADDS_TO_END = new Break("addFirst adds to the end");

    /// The [addLast][SequencedCollection#addLast] method does not add an element.
    /// @see BreakableSequencedCollection#addLast(Object)
    public static final Break ADD_LAST_DOES_NOT_ADD_ELEMENT = new Break("addLast does not add element");

    /// The [addLast][SequencedCollection#addLast] method adds the element to the front of the collection.
    /// @see BreakableSequencedCollection#addLast(Object)
    public static final Break ADD_LAST_ADDS_TO_FRONT = new Break("addLast adds to the beginning");

    /// The `getFirst` method returns a `null` value.
    /// @see BreakableSequencedCollection#getFirst()
    public static final Break GET_FIRST_RETURNS_NULL = new Break("getFirst always returns null");

    /// The `getFirst` method always throws a `NoSuchElementException`
    /// @see BreakableSequencedCollection#getFirst()
    public static final Break GET_FIRST_ALWAYS_THROWS = new Break("getFirst always throws");

    /// The `getFirst` method returns the second element.
    /// @see BreakableSequencedCollection#getFirst()
    public static final Break GET_FIRST_SKIPS_FIRST_ELEMENT = new Break("getFirst returns second element");

    /// The `getLast` method returns a `null` value.
    /// @see BreakableSequencedCollection#getLast()
    public static final Break GET_LAST_RETURNS_NULL = new Break("getLast always returns null");

    /// The `getLast` method always throws a `NoSuchElementException`
    /// @see BreakableSequencedCollection#getLast()
    public static final Break GET_LAST_ALWAYS_THROWS = new Break("getLast always throws");

    /// The `getLast` method returns the second-to-last element.
    /// @see BreakableSequencedCollection#getLast()
    public static final Break GET_LAST_SKIPS_LAST_ELEMENT = new Break("getLast returns second-to-last element");

    /// The [removeFirst][SequencedCollection#removeFirst()] method does not remove the first element.
    /// @see BreakableSequencedCollection#removeFirst()
    public static final Break REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT = new Break("removeFirst() does not remove element");

    /// The [removeFirst][SequencedCollection#removeFirst()] method returns `null`
    /// @see BreakableSequencedCollection#removeFirst()
    public static final Break REMOVE_FIRST_RETURNS_NULL = new Break("removeFirst() always returns null");

    /// The [removeFirst][SequencedCollection#removeFirst()] method always throws a `NoSuchElement` exception.
    /// @see BreakableSequencedCollection#removeFirst()
    public static final Break REMOVE_FIRST_ALWAYS_THROWS = new Break("removeFirst() always throws");

    /// The [removeLast][SequencedCollection#removeLast()] method does not remove the last element.
    /// @see BreakableSequencedCollection#removeLast()
    public static final Break REMOVE_LAST_DOES_NOT_REMOVE_ELEMENT = new Break("removeLast() does not remove element");

    /// The [removeLast][SequencedCollection#removeLast()] method returns `null`
    /// @see BreakableSequencedCollection#removeLast()
    public static final Break REMOVE_LAST_RETURNS_NULL = new Break("removeLast() always returns null");

    /// The [removeLast][SequencedCollection#removeLast()] method always throws a `NoSuchElement` exception.
    /// @see BreakableSequencedCollection#removeLast()
    public static final Break REMOVE_LAST_ALWAYS_THROWS = new Break("removeLast always throws");

    /// The [reversed][java.util.SequencedCollection#reversed] method does not reverse the collection.
    /// @see BreakableSequencedCollection#reversed()
    public static final Break REVERSED_DOES_NOT_REVERSE_COLLECTION = new Break("reversed() does not reverse elements");

    /// The [reversed][java.util.SequencedCollection#reversed] method modifies the collection.
    /// @see BreakableSequencedCollection#reversed()
    public static final Break REVERSED_MODIFIES_THE_COLLECTION = new Break("reversed() modifies the collection");

    /// Creates an empty sequenced collection that has no breaks.
    public BreakableSequencedCollection() {
        this(new ArrayList<>(), new HashSet<>(), 0);
    }

    /// Creates a breakable sequenced collection from an existing instance.
    /// @param other the breakable collection to copy.
    public BreakableSequencedCollection(final @NonNull BreakableSequencedCollection<E> other) {
        this(other.sequenced, new HashSet<>(), 0);
    }

    /// Creates a breakable iterable from an iterable.
    /// @param collection the iterator to use for the elements.
    public BreakableSequencedCollection(final @NonNull List<E> collection) {
        this(collection, new HashSet<>(), 0);
    }

    /// Creates a `BreakableSequencedCollection` from en existing collection and specifying the breaks and collection
    /// characteristics. Rather than calling this constructor directly, consider using the builder
    /// [BreakableCollection.Builder].
    /// @param c               the initial elements for the breakable collection.
    /// @param breaks          the breaks for the collection.
    /// @param characteristics the characteristics for the collection.
    /// @throws NullPointerException if either the `c` or the `breaks` parameters are null.
    public BreakableSequencedCollection(final @NonNull List<E> c, final @NonNull Collection<Break> breaks,
                                        final int characteristics) {
        super(c, breaks, characteristics);
        this.sequenced = Objects.requireNonNull(c);
    }

    /// Implements the [reversed][SequencedCollection#reversed] method from the [SequencedCollection] interface. This
    /// method can be broken using the following collection breaks:
    /// - REVERSED_DOES_NOT_REVERSE_COLLECTION
    /// - REVERSED_MODIFIES_THE_COLLECTION
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildSequencedCollection(1,2,3,4,5)
    ///         .withBreak(CollectionBreaks.REVERSED_DOES_NOT_REVERSE_COLLECTION)
    ///         .build();
    /// ```
    /// @return a collection with the elements in the reverse order.
    /// @see SequencedCollection#reversed()
    @Override
    public SequencedCollection<E> reversed() {
        if (supportsMethod(CollectionMethods.Reversed)) {
            if (hasBreak(REVERSED_DOES_NOT_REVERSE_COLLECTION)) {
                return sequenced;
            } else if (hasBreak(REVERSED_MODIFIES_THE_COLLECTION)) {
                Collections.reverse(sequenced);
                return sequenced;
            } else {
                return sequenced.reversed();
            }
        }  else {
            throw new UnsupportedOperationException();
        }
    }

    /// Implements the [addFirst][SequencedCollection#addFirst] method from the [SequencedCollection] interface. This
    /// method can be broken using the following collection breaks:
    /// - ADD_FIRST_DOES_NOT_ADD_ELEMENT
    /// - ADD_FIRST_ADDS_TO_END
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildSequencedCollection(1,2,3,4,5)
    ///         .withBreak(CollectionBreaks.ADD_FIRST_DOES_NOT_ADD_ELEMENT)
    ///         .build();
    /// ```
    /// @throws UnsupportedOperationException if this method is not supported
    /// @throws NullPointerException if the element is null and this collection does not permit null elements.
    /// @see SequencedCollection#addFirst
    @Override
    public void addFirst(final E e) {
        if (supportsMethod(CollectionMethods.AddFirst)) {
            checkNewElement(e);
            if (hasBreak(ADD_FIRST_ADDS_TO_END)) {
                sequenced.addLast(e);
            } else if (!hasBreak(ADD_FIRST_DOES_NOT_ADD_ELEMENT)) {
                sequenced.addFirst(e);
            }
        }  else {
            throw new UnsupportedOperationException();
        }
    }

    /// Implements the [addLast][SequencedCollection#addLast] method from the [SequencedCollection] interface. This
    /// method can be broken using the following collection breaks:
    /// - ADD_LAST_DOES_NOT_ADD_ELEMENT
    /// - ADD_LAST_ADDS_TO_FRONT
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildSequencedCollection(1,2,3,4,5)
    ///         .withBreak(CollectionBreaks.ADD_LAST_DOES_NOT_ADD_ELEMENT)
    ///         .build();
    /// ```
    /// @throws UnsupportedOperationException if this method is not supported
    /// @throws NullPointerException if the element is null and this collection does not permit null elements.
    /// @see SequencedCollection#addLast
    @Override
    public void addLast(final E e) {
        if (supportsMethod(CollectionMethods.AddLast)) {
            checkNewElement(e);
            if (hasBreak(ADD_LAST_ADDS_TO_FRONT)) {
                sequenced.addFirst(e);
            } else if (!hasBreak(ADD_LAST_DOES_NOT_ADD_ELEMENT)) {
                sequenced.addLast(e);
            }
        }  else {
            throw new UnsupportedOperationException();
        }
    }

    /// Implements the [getFirst][SequencedCollection#getFirst] method from the [SequencedCollection] interface. This
    /// method can be broken using the following collection breaks:
    /// - GET_FIRST_RETURNS_NULL
    /// - GET_FIRST_ALWAYS_THROWS
    /// - GET_FIRST_SKIPS_FIRST_ELEMENT
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildSequencedCollection(1,2,3,4,5)
    ///         .withBreak(CollectionBreaks.GET_FIRST_RETURNS_NULL)
    ///         .build();
    /// ```
    /// @throws UnsupportedOperationException if this method is not supported
    /// @throws NoSuchElementException if this collection is empty
    /// @see SequencedCollection#getFirst
    @Override
    public E getFirst() {
        if (supportsMethod(CollectionMethods.GetFirst)) {
            if (hasBreak(GET_FIRST_RETURNS_NULL)) {
                return null;
            } else if (hasBreak(GET_FIRST_ALWAYS_THROWS)) {
                throw new NoSuchElementException();
            } else if (hasBreak(GET_FIRST_SKIPS_FIRST_ELEMENT)) {
                Iterator<E> i = iterator();
                i.next();
                return i.next();
            } else {
                return sequenced.getFirst();
            }
        }  else {
            throw new UnsupportedOperationException();
        }
    }

    /// Implements the [getLast][SequencedCollection#getLast] method from the [SequencedCollection] interface. This
    /// method can be broken using the following collection breaks:
    /// - GET_LAST_RETURNS_NULL
    /// - GET_LAST_ALWAYS_THROWS
    /// - GET_LAST_SKIPS_FIRST_ELEMENT
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildSequencedCollection(1,2,3,4,5)
    ///         .withBreak(CollectionBreaks.GET_LAST_RETURNS_NULL)
    ///         .build();
    /// ```
    /// @throws UnsupportedOperationException if this method is not supported
    /// @see SequencedCollection#getLast
    @Override
    public E getLast() {
        if (supportsMethod(CollectionMethods.GetLast)) {
            if (hasBreak(GET_LAST_RETURNS_NULL)) {
                return null;
            } else if (hasBreak(GET_LAST_ALWAYS_THROWS)) {
                throw new NoSuchElementException();
            } else if (hasBreak(GET_LAST_SKIPS_LAST_ELEMENT)) {
                Iterator<E> i = reversed().iterator();
                i.next();
                return i.next();
            } else {
                return sequenced.getLast();
            }
        }  else {
            throw new UnsupportedOperationException();
        }
    }

    /// Implements the [removeFirst][SequencedCollection#removeFirst] method from the [SequencedCollection] interface.
    /// This method can be broken using the following collection breaks:
    /// - REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT
    /// - REMOVE_FIRST_RETURNS_NULL
    /// - REMOVE_FIRST_ALWAYS_THROWS
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildSequencedCollection(1,2,3,4,5)
    ///         .withBreak(CollectionBreaks.REMOVE_FIRST_RETURNS_NULL)
    ///         .build();
    /// ```
    /// @throws UnsupportedOperationException if this method is not supported
    /// @see SequencedCollection#removeFirst
    @Override
    public E removeFirst() {
        if (supportsMethod(CollectionMethods.RemoveFirst)) {
            if (hasBreak(REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT)) {
                return getFirst();
            } else if (hasBreak(REMOVE_FIRST_RETURNS_NULL)) {
                return null;
            } else if (hasBreak(REMOVE_FIRST_ALWAYS_THROWS)) {
                throw new NoSuchElementException();
            } else {
                return sequenced.removeFirst();
            }
        }  else {
            throw new UnsupportedOperationException();
        }
    }

    /// Implements the [removeLast][SequencedCollection#removeLast] method from the [SequencedCollection] interface.
    /// This method can be broken using the following collection breaks:
    /// - REMOVE_LAST_DOES_NOT_REMOVE_ELEMENT
    /// - REMOVE_LAST_RETURNS_NULL
    /// - REMOVE_LAST_ALWAYS_THROWS
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildSequencedCollection(1,2,3,4,5)
    ///         .withBreak(CollectionBreaks.REMOVE_LAST_RETURNS_NULL)
    ///         .build();
    /// ```
    /// @throws UnsupportedOperationException if this method is not supported
    /// @see SequencedCollection#removeLast
    @Override
    public E removeLast() {
        if (supportsMethod(CollectionMethods.RemoveLast)) {
            if (hasBreak(REMOVE_LAST_DOES_NOT_REMOVE_ELEMENT)) {
                return getLast();
            } else if (hasBreak(REMOVE_LAST_RETURNS_NULL)) {
                return null;
            } else if (hasBreak(REMOVE_LAST_ALWAYS_THROWS)) {
                throw new NoSuchElementException();
            } else {
                return sequenced.removeLast();
            }
        }  else {
            throw new UnsupportedOperationException();
        }
    }

    /// The builder for BreakableCollection objects.
    /// @param <E> the element type.
    public static class Builder<E>
            extends AbstractBuilder<BreakableSequencedCollection.Builder<E>, BreakableSequencedCollection<E>, E> {

        /// Create a builder initialized with the default values.
        public Builder() {
            super(new ArrayList<>());
        }

        /// Create a builder initialized with an element store.
        /// @param elements the element store to use.
        public Builder(final @NonNull Collection<E> elements) {
            super(new ArrayList<>(Objects.requireNonNull(elements)));
        }

        /// Create a builder initialized with the values copied from another builder.
        /// @param other the builder to copy the values from.
        public Builder(final BreakableSequencedCollection.Builder<E> other) {
            super(other);
        }

        @Override
        public Builder<E> self() {
            return this;
        }

        @Override
        public BreakableSequencedCollection.Builder<E> copy() {
            return new BreakableSequencedCollection.Builder<>(this);
        }

        /// Build a BreakableCollection objects using the values from the builder.
        /// @return a new BreakableCollection object.
        public BreakableSequencedCollection<E> build() {
            BreakableSequencedCollection<E> broken =
                    new BreakableSequencedCollection<>(new ArrayList<>(elements()), breaks(), characteristics());
            broken.setPermitsNulls(permitsNulls());
            broken.setPermitsDuplicates(permitsDuplicates());
            broken.setPermitsIncompatibleTypes(permitsIncompatibleTypes());
            unsupportedMethods().forEach(broken::doesNotSupportMethod);
            return broken;
        }
    }

}
