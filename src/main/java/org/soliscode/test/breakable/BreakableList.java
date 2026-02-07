package org.soliscode.test.breakable;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.MethodStatus;
import org.soliscode.test.contract.collection.CollectionMethods;
import org.soliscode.test.contract.list.ListMethods;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;

/// **Breakable List Implementation for Testing**
///
/// This class provides a List implementation that can be programmatically broken for comprehensive
/// testing scenarios. It extends BreakableSequencedCollection to maintain all sequence operations
/// while adding List-specific indexed access, modification, and bulk operations that can be
/// individually configured to fail in various ways.
///
/// ## Core Functionality
///
/// As a List implementation, this class supports all standard collection and sequence operations
/// plus List-specific indexed operations that provide random access to elements. The class can be
/// configured to break these List-specific contracts through targeted breaks while maintaining
/// compatibility with the broader collection testing framework.
///
/// ### List Semantics
///
/// Under normal operation (no breaks active), BreakableList maintains proper List behavior:
/// - **Indexed Access**: get(index) and set(index, element) provide direct element access
/// - **Indexed Modification**: add_singleElement_returnsTrueAndUpdatesSize(index, element) and remove(index) modify at specific positions
/// - **Bulk Operations**: addAll(index, collection) and replaceAll(operator) operate on ranges
/// - **Search Operations**: indexOf() and lastIndexOf() find element positions
/// - **Ordering**: sort() arranges elements according to comparators
/// - **Views**: subList() and listIterator() provide List-specific views
///
/// ### Breakable Behavior
///
/// The class introduces List-specific breaks organized into several categories:
///
/// ## Available Breaks
///
/// ### Indexed Access Breaks
///
/// #### GET_ALWAYS_RETURNS_NULL
/// **Purpose**: Forces `get(index)` method to return null instead of the element at the specified index
/// **Effect**: Violates List contract that should return actual elements
/// **Use Case**: Testing code handling of unexpected null returns from indexed access
///
/// #### GET_ALWAYS_RETURNS_THE_FIRST_ELEMENT / GET_ALWAYS_RETURNS_THE_LAST_ELEMENT
/// **Purpose**: Forces `get(index)` method to return the first or last element regardless of index
/// **Effect**: Indexed access becomes unreliable, always returning boundary elements
/// **Use Case**: Testing code that depends on accurate indexed access
///
/// #### GET_RETURNS_THE_NEXT_ELEMENT / GET_RETURNS_THE_PREVIOUS_ELEMENT
/// **Purpose**: Forces `get(index)` method to return elements at adjacent indices (index±1)
/// **Effect**: Indexed access has consistent offset errors
/// **Use Case**: Testing code robustness when indices are systematically incorrect
///
/// #### GET_RETURNS_NULL_ON_BAD_INDEX / GET_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX
/// **Purpose**: Forces `get(index)` to handle invalid indices incorrectly
/// **Effect**: Wrong error handling for out-of-bounds access
/// **Use Case**: Testing code that expects proper IndexOutOfBoundsException behavior
///
/// ### Indexed Modification Breaks
///
/// #### SET_DOES_NOT_CHANGE_THE_ELEMENT
/// **Purpose**: Forces `set(index, element)` method to return old values without actually changing elements
/// **Effect**: Elements remain unchanged despite successful method completion
/// **Use Case**: Testing assumptions about set operation effectiveness
///
/// #### SET_ALWAYS_RETURNS_NULL
/// **Purpose**: Forces `set(index, element)` method to return null instead of the previous element
/// **Effect**: Violates List contract that should return replaced elements
/// **Use Case**: Testing code handling of unexpected null returns from modification methods
///
/// #### SET_CHANGES_THE_NEXT_ELEMENT / SET_CHANGES_THE_PREVIOUS_ELEMENT
/// **Purpose**: Forces `set(index, element)` method to modify adjacent elements instead of target
/// **Effect**: Indexed modification has systematic offset errors
/// **Use Case**: Testing code that assumes set operations target correct indices
///
/// #### SET_RETURNS_NULL_ON_BAD_INDEX / SET_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX
/// **Purpose**: Forces `set(index, element)` to handle invalid indices incorrectly
/// **Effect**: Wrong error handling for out-of-bounds modification
/// **Use Case**: Testing code that expects proper IndexOutOfBoundsException behavior
///
/// ### Indexed Addition Breaks
///
/// #### ADD_AT_INDEX_DOES_NOT_ADD_THE_ELEMENT
/// **Purpose**: Forces `add_singleElement_returnsTrueAndUpdatesSize(index, element)` method to complete without actually adding elements
/// **Effect**: Method completes normally but list remains unchanged
/// **Use Case**: Testing code that assumes successful add_singleElement_returnsTrueAndUpdatesSize operations modify the list
///
/// #### ADD_AT_INDEX_ADDS_AT_NEXT_POSITION / ADD_AT_INDEX_ADDS_AT_PREVIOUS_POSITION
/// **Purpose**: Forces `add_singleElement_returnsTrueAndUpdatesSize(index, element)` method to insert at adjacent positions
/// **Effect**: Indexed insertion has systematic offset errors
/// **Use Case**: Testing code that assumes add_singleElement_returnsTrueAndUpdatesSize operations target correct indices
///
/// #### ADD_AT_INDEX_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX
/// **Purpose**: Forces `add_singleElement_returnsTrueAndUpdatesSize(index, element)` to throw wrong exceptions for invalid indices
/// **Effect**: Wrong error handling for out-of-bounds insertion
/// **Use Case**: Testing code that expects proper IndexOutOfBoundsException behavior
///
/// ### Indexed Removal Breaks
///
/// #### REMOVE_AT_INDEX_DOES_NOT_REMOVE_THE_ELEMENT
/// **Purpose**: Forces `remove(index)` method to return elements without actually removing them
/// **Effect**: Elements remain in list despite successful method completion
/// **Use Case**: Testing assumptions about removal operation effectiveness
///
/// #### REMOVE_AT_INDEX_REMOVES_THE_NEXT_ELEMENT / REMOVE_AT_INDEX_REMOVES_THE_PREVIOUS_ELEMENT
/// **Purpose**: Forces `remove(index)` method to remove adjacent elements instead of target
/// **Effect**: Indexed removal has systematic offset errors
/// **Use Case**: Testing code that assumes remove operations target correct indices
///
/// #### REMOVE_AT_INDEX_ALWAYS_RETURNS_NULL
/// **Purpose**: Forces `remove(index)` method to return null instead of removed elements
/// **Effect**: Violates List contract that should return removed elements
/// **Use Case**: Testing code handling of unexpected null returns from removal methods
///
/// #### REMOVE_AT_INDEX_RETURNS_NULL_ON_BAD_INDEX / REMOVE_AT_INDEX_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX
/// **Purpose**: Forces `remove(index)` to handle invalid indices incorrectly
/// **Effect**: Wrong error handling for out-of-bounds removal
/// **Use Case**: Testing code that expects proper IndexOutOfBoundsException behavior
///
/// ### Bulk Operation Breaks
///
/// #### ADD_ALL_AT_INDEX_DOES_NOT_ADD_ANY_ELEMENTS
/// **Purpose**: Forces `addAll(index, collection)` method to complete without adding elements
/// **Effect**: Method completes normally but list remains unchanged
/// **Use Case**: Testing bulk operation failure handling
///
/// #### ADD_ALL_AT_INDEX_ADDS_TO_THE_END
/// **Purpose**: Forces `addAll(index, collection)` method to ignore index and add_singleElement_returnsTrueAndUpdatesSize at end
/// **Effect**: Indexed bulk insertion becomes append operation
/// **Use Case**: Testing code that depends on precise insertion positioning
///
/// #### ADD_ALL_AT_INDEX_ALWAYS_RETURNS_TRUE / ADD_ALL_AT_INDEX_ALWAYS_RETURNS_FALSE / ADD_ALL_AT_INDEX_ALWAYS_RETURNS_OPPOSITE_VALUE
/// **Purpose**: Forces `addAll(index, collection)` method to return incorrect success indicators
/// **Effect**: Return value doesn't match actual operation result
/// **Use Case**: Testing code that relies on addAll return values for control flow
///
/// #### Exception Handling Breaks for addAll
/// **Purpose**: Forces `addAll(index, collection)` to throw wrong exceptions for various error conditions
/// **Effect**: Improper exception handling for invalid indices, null arguments, or unsupported operations
/// **Use Case**: Testing exception handling robustness
///
/// ### Element Replacement Breaks
///
/// #### REPLACE_ALL_DOES_NOT_REPLACE_ELEMENTS
/// **Purpose**: Forces `replaceAll(operator)` method to complete without replacing any elements
/// **Effect**: Method completes normally but elements remain unchanged
/// **Use Case**: Testing assumptions about bulk replacement effectiveness
///
/// #### REPLACE_ALL_SKIPS_FIRST_ELEMENT / REPLACE_ALL_SKIPS_LAST_ELEMENT
/// **Purpose**: Forces `replaceAll(operator)` method to skip boundary elements
/// **Effect**: Partial replacement, leaving specific elements unchanged
/// **Use Case**: Testing handling of incomplete bulk operations
///
/// ### Sorting Breaks
///
/// #### SORT_DOES_NOT_SORT_THE_ELEMENTS
/// **Purpose**: Forces `sort(comparator)` method to complete without sorting elements
/// **Effect**: Method completes normally but element order remains unchanged
/// **Use Case**: Testing assumptions about sort operation effectiveness
///
/// #### SORT_REVERSES_THE_ORDER
/// **Purpose**: Forces `sort(comparator)` method to sort in reverse order
/// **Effect**: Sorting produces opposite of expected ordering
/// **Use Case**: Testing code that depends on specific sort ordering
///
/// #### SORT_THROWS_ON_NULL_ARGUMENT
/// **Purpose**: Forces `sort(comparator)` method to throw NullPointerException for null comparator
/// **Effect**: Rejects natural ordering that should normally be accepted
/// **Use Case**: Testing error handling when natural ordering should be allowed
///
/// ## Builder Pattern Usage
///
/// The class provides a comprehensive Builder for constructing BreakableList instances:
///
/// ### Basic List Creation
/// ```java
/// BreakableList<String> list = new BreakableList.Builder<String>()
///     .build();
/// ```
///
/// ### List with Breaks
/// ```java
/// BreakableList<Integer> brokenList = new BreakableList.Builder<Integer>()
///     .withBreak(GET_ALWAYS_RETURNS_NULL)
///     .withBreak(SET_DOES_NOT_CHANGE_THE_ELEMENT)
///     .build();
/// ```
///
/// ### List with Initial Elements
/// ```java
/// BreakableList<String> list = new BreakableList.Builder<String>()
///     .addElements(Arrays.asList("first", "second", "third"))
///     .withBreak(ADD_AT_INDEX_ADDS_AT_NEXT_POSITION)
///     .build();
/// ```
///
/// ## Inheritance Support
///
/// This class extends BreakableSequencedCollection and supports all collection, sequence, iterator, and spliterator breaks:
/// - Collection breaks affect standard collection operations (add_singleElement_returnsTrueAndUpdatesSize, remove, contains, size, etc.)
/// - SequencedCollection breaks affect sequence operations (addFirst, addLast, getFirst, getLast, etc.)
/// - Iterator breaks are passed through to created iterators
/// - Spliterator breaks affect spliterator behavior
/// - List-specific breaks can be combined with all inherited breaks
///
/// ## Testing Applications
///
/// ### Indexed Access Testing
/// ```java
/// @Test
/// void testGetIndexOffByOne() {
///     BreakableList<String> list = new BreakableList.Builder<String>()
///         .addElements(Arrays.asList("a", "b", "c"))
///         .withBreak(GET_RETURNS_THE_NEXT_ELEMENT)
///         .build();
///
///     assertEquals("b", list.get(0));  // Returns next element instead
///     assertEquals("c", list.get(1));  // Consistent offset error
/// }
/// ```
///
/// ### Indexed Modification Testing
/// ```java
/// @Test
/// void testSetDoesNotModify() {
///     BreakableList<Integer> list = new BreakableList.Builder<Integer>()
///         .addElements(Arrays.asList(1, 2, 3))
///         .withBreak(SET_DOES_NOT_CHANGE_THE_ELEMENT)
///         .build();
///
///     Integer old = list.set(1, 99);
///     assertEquals(2, old);            // Returns old value
///     assertEquals(2, list.get(1));    // But doesn't change element
/// }
/// ```
///
/// ### Bulk Operation Testing
/// ```java
/// @Test
/// void testAddAllWrongPosition() {
///     BreakableList<String> list = new BreakableList.Builder<String>()
///         .addElements(Arrays.asList("a", "b"))
///         .withBreak(ADD_ALL_AT_INDEX_ADDS_TO_THE_END)
///         .build();
///
///     list.addAll(0, Arrays.asList("x", "y"));
///
///     assertEquals("a", list.get(0));   // Original order preserved
///     assertEquals("b", list.get(1));   // Elements added at end instead
///     assertEquals("x", list.get(2));   // Not at requested index
///     assertEquals("y", list.get(3));
/// }
/// ```
///
/// ### Sorting Testing
/// ```java
/// @Test
/// void testSortReverseOrder() {
///     BreakableList<Integer> list = new BreakableList.Builder<Integer>()
///         .addElements(Arrays.asList(3, 1, 2))
///         .withBreak(SORT_REVERSES_THE_ORDER)
///         .build();
///
///     list.sort(Integer::compareTo);
///
///     assertEquals(3, list.get(0));     // Sorted in reverse
///     assertEquals(2, list.get(1));     // Not ascending order
///     assertEquals(1, list.get(2));
/// }
/// ```
///
/// ## Optional Method Support
///
/// This class supports optional method configuration using the InterfaceMethod system:
/// - List methods can be disabled to simulate unsupported operations
/// - UnsupportedOperationException is thrown for disabled methods
/// - Useful for testing code that handles optional list methods
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
/// - **Memory Usage**: Minimal overhead beyond the backing List and inherited collections
///
/// ### Index Handling
/// The class maintains proper index bounds checking and delegates to the underlying List
/// implementation for all index-based operations when not broken.
///
/// @param <E> the type of elements maintained by this list
/// @author evanbergstrom
/// @since 1.0.0
/// @see CollectionMethods
/// @see BreakableSequencedCollection
/// @see java.util.List
public class BreakableList<E> extends BreakableSequencedCollection<E> implements List<E> {

    private final @NonNull List<E> list;

    /// Creates an empty list that has no breaks.
    public BreakableList() {
        this(new ArrayList<>(), new HashSet<>(), new HashMap<>(), DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS,
                DEFAULT_SAFETY, Object.class);
    }

    /// Creates a breakable sequenced collection from an existing instance.
    /// @param other the breakable collection to copy.
    public BreakableList(final @NonNull BreakableList<E> other) {
        this(new ArrayList<>(other.list), new HashSet<>(other.breaks()), new HashMap<>(other.methodStatuses()),
                other.characteristics(), other.permits(), other.isSafe(), other.compatibleType());
    }

    /// Creates a breakable iterable from an iterable.
    /// @param collection the iterator to use for the elements.
    public BreakableList(final @NonNull List<E> collection) {
        this(collection, new HashSet<>(), new HashMap<>(), DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS, DEFAULT_SAFETY,
                Object.class);
    }

    /// Creates a `BreakableSequencedCollection` from en existing collection and specifying the breaks and collection
    /// characteristics. Rather than calling this constructor directly, consider using the builder
    /// [BreakableCollection.Builder].
    /// @param c               the initial elements for the breakable collection.
    /// @param breaks          the breaks for the collection.
    /// @param methodStatuses  the method status configuration.
    /// @param characteristics the characteristics for the collection.
    /// @param permits         the flags that indicate what types of values are supported by the collection.
    /// @throws NullPointerException if either the `c` or the `breaks` parameters are null.
    public BreakableList(final @NonNull List<E> c, final @NonNull Set<Break> breaks,
                         final @NonNull Map<InterfaceMethod, MethodStatus> methodStatuses,
                         final int characteristics, final int permits, final boolean isSafe,
                         final Class<?> compatibleType) {
        super(this.list = c, breaks, methodStatuses, characteristics, permits, isSafe, compatibleType);
    }

    /// The [addAll][List#addAll(int,Collection)] method always return a result of `true`, even if the
    /// element is not added.
    /// @see BreakableList#addAll(int,Collection)
    public static final Break ADD_ALL_AT_INDEX_ALWAYS_RETURNS_TRUE =
            new Break("addAll(int, Collection) always returns true");

    /// The [addAll][List#addAll(int,Collection)] method always return a result of `false`, even if the
    /// element is added.
    /// @see BreakableList#addAll(int,Collection)
    public static final Break ADD_ALL_AT_INDEX_ALWAYS_RETURNS_FALSE =
            new Break("addAll(int, Collection) always returns false");

    /// The [addAll][List#addAll(int,Collection)] method always returns the opposite of the appropriate result.
    /// @see BreakableList#addAll(int, Collection)
    public static final Break ADD_ALL_AT_INDEX_ALWAYS_RETURNS_OPPOSITE_VALUE =
            new Break("addAll(int, Collection) always returns opposite value");

    /// The [addAll][List#addAll(int,Collection)] method adds to the end of the collection.
    /// @see BreakableList#addAll(int,Collection)
    public static final Break ADD_ALL_AT_INDEX_ADDS_TO_THE_END =
            new Break("addAll(int, Collection) adds to the end");

    /// The [addAll][List#addAll(int,Collection)] method method throws the wrong exception when the index is out of
    /// bounds.
    /// @see BreakableList#addAll(int,Collection)
    public static final Break ADD_ALL_AT_INDEX_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX =
            new Break("addAll(int, Collection) throws wrong exception on bad index");

    /// The [addAll][List#addAll(int,Collection)] method method throws the wrong exception if it is not supported.
    /// @see BreakableList#addAll(int,Collection)
    public static final Break ADD_ALL_AT_INDEX_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED =
            new Break("addAll(int, Collection) throws wrong exception if not supported");

    /// The [addAll][List#addAll(int,Collection)] method method throws the wrong exception if the argument is null.
    /// @see BreakableList#addAll(int,Collection)
    public static final Break ADD_ALL_AT_INDEX_THROWS_WRONG_EXCEPTION_WHEN_ARGUMENT_IS_NULL =
            new Break("addAll(int, Collection) throws wrong exception when argument is null");

    /// The [addAll][List#addAll(int,Collection)] method will not add_singleElement_returnsTrueAndUpdatesSize any elements to the collection
    /// @see BreakableList#addAll(int,Collection)
    public static final Break ADD_ALL_AT_INDEX_DOES_NOT_ADD_ANY_ELEMENTS =
            new Break("addAll(int, Collection) does not add_singleElement_returnsTrueAndUpdatesSize elements");

    /// The [add_singleElement_returnsTrueAndUpdatesSize][List#add(int,Object)] method will not add_singleElement_returnsTrueAndUpdatesSize an element to the collection
    /// @see BreakableList#add(int,Object)
    public static final Break ADD_AT_INDEX_DOES_NOT_ADD_THE_ELEMENT =
            new Break("add_singleElement_returnsTrueAndUpdatesSize(int, Object) does not add_singleElement_returnsTrueAndUpdatesSize elements");

    /// The [add_singleElement_returnsTrueAndUpdatesSize][List#add(int,Object)] method adds the element at the next position.
    /// @see BreakableList#add(int,Object)
    public static final Break ADD_AT_INDEX_ADDS_AT_NEXT_POSITION =
            new Break("add_singleElement_returnsTrueAndUpdatesSize(int,Object) method will not add_singleElement_returnsTrueAndUpdatesSize an element to the collection");

    /// The [add_singleElement_returnsTrueAndUpdatesSize][List#add(int,Object)] method adds the element at the previous position.
    /// @see BreakableList#add(int,Object)
    public static final Break ADD_AT_INDEX_ADDS_AT_PREVIOUS_POSITION =
            new Break("add_singleElement_returnsTrueAndUpdatesSize(int,Object) method adds the element at the previous position");

    /// The [add_singleElement_returnsTrueAndUpdatesSize][List#add(int,Object)] method throws the wrong exception when the index is out of bounds.
    /// @see BreakableList#add(int,Object)
    public static final Break ADD_AT_INDEX_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX =
            new Break("add_singleElement_returnsTrueAndUpdatesSize(int,Object) method throws the wrong exception when the index is out of bounds");

    /// The [get][List#get(int)] method always returns `null`
    /// @see BreakableList#get(int)
    public static final Break GET_ALWAYS_RETURNS_NULL =
            new Break("get(int) method always returns null");

    /// The [get][List#get(int)] method always returns the first element.
    /// @see BreakableList#get(int)
    public static final Break GET_ALWAYS_RETURNS_THE_FIRST_ELEMENT =
            new Break("get(int) method always returns the first element");

    /// The [get][List#get(int)] method always returns the last element.
    /// @see BreakableList#get(int)
    public static final Break GET_ALWAYS_RETURNS_THE_LAST_ELEMENT =
            new Break("get(int) method always returns the last element");

    /// The [get][List#get(int)] method returns the next element (*i.e.* at index + 1).
    /// @see BreakableList#get(int)
    public static final Break GET_RETURNS_THE_NEXT_ELEMENT =
            new Break("get(int) method returns the next element (*i.e.* at index + 1)");

    /// The [get][List#get(int)] method returns the previous element.
    /// @see BreakableList#get(int)
    public static final Break GET_RETURNS_THE_PREVIOUS_ELEMENT =
            new Break("get(int) method returns the previous element");

    /// The [get][List#get(int)] method returns `null` on a bad index instead of throwing en exception.
    /// @see BreakableList#get(int)
    public static final Break GET_RETURNS_NULL_ON_BAD_INDEX =
            new Break("get(int) method returns null on a bad index instead of throwing en exception");

    /// The [get][List#get(int)] method throws the wrong exception on a bad index.
    /// @see BreakableList#get(int)
    public static final Break GET_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX =
            new Break("get(int) method throws the wrong exception on a bad index");

    /// The [remove][List#remove(int)] method does not remove the element.
    /// @see BreakableList#remove(int)
    public static final Break REMOVE_AT_INDEX_DOES_NOT_REMOVE_THE_ELEMENT =
            new Break("remove(int) method does not remove the element");

    /// The [remove][List#remove(int)] method removes the next element.
    /// @see BreakableList#remove(int)
    public static final Break REMOVE_AT_INDEX_REMOVES_THE_NEXT_ELEMENT =
            new Break("remove(int) method removes the next element");

    /// The [remove][List#remove(int)] method removes the previous element
    /// @see BreakableList#remove(int)
    public static final Break REMOVE_AT_INDEX_REMOVES_THE_PREVIOUS_ELEMENT =
            new Break("remove(int) method removes the previous element");

    /// The [remove][List#remove(int)] method returns `null` when the index is out of bounds.
    /// @see BreakableList#remove(int)
    public static final Break REMOVE_AT_INDEX_RETURNS_NULL_ON_BAD_INDEX =
            new Break("remove(int)] method returns null when the index is out of bound");

    /// The [remove][List#remove(int)] method throws the wrong exception when the index is out of bounds.
    /// @see BreakableList#remove(int)
    public static final Break REMOVE_AT_INDEX_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX =
            new Break("remove(int) method throws the wrong exception when the index is out of bounds");

    /// The [remove][List#remove(int)] method always returns `null`
    /// @see BreakableList#remove(int)
    public static final Break REMOVE_AT_INDEX_ALWAYS_RETURNS_NULL =
            new Break("remove(int) method always returns null");

    /// The [replaceAll][List#replaceAll(UnaryOperator)] method does not replace any elements
    /// @see BreakableList#replaceAll(UnaryOperator)
    public static final Break REPLACE_ALL_DOES_NOT_REPLACE_ELEMENTS =
            new Break("replaceAll(UnaryOperator) method does not replace any elements");

    /// The [replaceAll][List#replaceAll(UnaryOperator)] method skips the first element.
    /// @see BreakableList#replaceAll(UnaryOperator)
    public static final Break REPLACE_ALL_SKIPS_FIRST_ELEMENT =
            new Break("replaceAll(UnaryOperator) method skips the first element");

    /// The [replaceAll][List#replaceAll(UnaryOperator)] method skips the last element
    /// @see BreakableList#replaceAll(UnaryOperator)
    public static final Break REPLACE_ALL_SKIPS_LAST_ELEMENT =
            new Break("replaceAll(UnaryOperator) method skips the last element");

    /// The [set][List#set(int,Object)] method does not change the element.
    /// @see BreakableList#set(int,Object)
    public static final Break SET_DOES_NOT_CHANGE_THE_ELEMENT =
            new Break("set(int,Object) method does not change the element");

    /// The [set][List#set(int,Object)] method always returns `null`
    /// @see BreakableList#set(int,Object)
    public static final Break SET_ALWAYS_RETURNS_NULL =
            new Break("set(int,Object) method always returns null");

    /// The [set][List#set(int,Object)] method changes the next element (*i.e.* at index + 1).
    /// @see BreakableList#set(int,Object)
    public static final Break SET_CHANGES_THE_NEXT_ELEMENT =
            new Break("set(int, Object) changes the next element (i.e. at index + 1");

    /// The [set][List#set(int,Object)] method changes the previous element (*i.e.* at index - 1).
    /// @see BreakableList#set(int,Object)
    public static final Break SET_CHANGES_THE_PREVIOUS_ELEMENT =
            new Break("set(int,Object) method changes the previous element (*i.e.* at index - 1)");

    /// The [set][List#set(int,Object)] method returns `null` on a bad index instead of throwing en exception.
    /// @see BreakableList#set(int, Object)
    public static final Break SET_RETURNS_NULL_ON_BAD_INDEX =
            new Break("set(int,Object) method returns null on a bad index instead of throwing en exception");

    /// The [set][List#set(int,Object)] method throws the wrong exception on a bad index.
    /// @see BreakableList#set(int, Object)
    public static final Break SET_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX =
            new Break("set(int,Object) method throws the wrong exception on a bad index");

    /// The [sort][List#sort(Comparator)] method sorts the elements in the reverse order.
    /// @see BreakableList#sort(Comparator)
    public static final Break SORT_REVERSES_THE_ORDER =
            new Break("sort(Comparator) method sorts the elements in the reverse order");

    /// The [sort][List#sort(Comparator)] method throws a `NullPointerException` if the argument is `null`
    /// @see BreakableList#sort(Comparator)
    public static final Break SORT_THROWS_ON_NULL_ARGUMENT =
            new Break("sort(Comparator) method throws a NullPointerException if the argument is `null`");

    /// The [sort][List#sort(Comparator)] method does not sort the elements.
    /// @see BreakableList#sort(Comparator)
    public static final Break SORT_DOES_NOT_SORT_THE_ELEMENTS  =
            new Break("sort(Comparator) method does not sort the elements");

    @Override
    public List<E> reversed() {
        return list.reversed();
    }

    // Implements the [addAll][List#addAll] method from the [List] interface. This method can be broken using the
    // following collection breaks:
    ///
    /// | Break                    | Description                                     |
    /// | ------------------------ | ----------------------------------------------- |
    /// | ADD_ALL_AT_INDEX_DOES_NOT_ADD_ANY_ELEMENTS | The `addAll` method will not add_singleElement_returnsTrueAndUpdatesSize any elements to the collection. |
    /// | ADD_ALL_AT_INDEX_ADDS_TO_THE_END |  The `addAll` method adds to the end of the collection. |
    /// | ADD_ALL_AT_INDEX_ALWAYS_RETURNS_TRUE | The `addAll` method always return a result of `true`, even if the element is not added. |
    /// | ADD_ALL_AT_INDEX_ALWAYS_RETURNS_FALSE | The `addAll` method always returns `false` |
    /// | ADD_ALL_AT_INDEX_ALWAYS_RETURNS_OPPOSITE_VALUE | The `addAll` method always returns `true` |
    /// | ADD_ALL_AT_INDEX_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX | The `addAll` method throws the wrong exception when the index is out of bounds. |
    /// | ADD_ALL_AT_INDEX_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED | The `addAll` method throws the wrong exception if it is not supported. |
    /// | ADD_ALL_AT_INDEX_THROWS_WRONG_EXCEPTION_WHEN_ARGUMENT_IS_NULL | The `addAll` method throws the wrong exception when the argument is null. |
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildList(1,2,3,4,5)
    ///         .withBreak(ADD_ALL_AT_INDEX_ALWAYS_RETURNS_TRUE)
    ///         .build();
    /// ```
    /// @param index index at which to insert the first element from the specified collection.
    /// @param c collection containing elements to be added to this list
    /// @return 'true' if this list changed as a result of the call
    /// @throws UnsupportedOperationException if the 'addAll' operation is not supported by this list
    /// @throws ClassCastException if the class of an element of the specified collection prevents it from being added
    ///                            to this list
    /// @throws NullPointerException if the specified collection contains one or more null elements and this list does
    ///                              not permit null elements, or if the specified collection is null
    /// @throws IllegalArgumentException if some property of an element of the specified collection prevents it from
    ///                                  being added to this list
    /// @throws IndexOutOfBoundsException if the index is out of range ('index < 0 || index > size()')
    /// @see List#addAll(int, Collection)
    @SuppressWarnings("ConstantValue")
    @Override
    public boolean addAll(final int index, final @NonNull Collection<? extends E> c) {
        if (supportsMethod(CollectionMethods.ADD_ALL)) {
            if ((index < 0 || index >= list.size()) && hasBreak(ADD_ALL_AT_INDEX_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX)) {
                throw new RuntimeException();
            }

            if (c == null && hasBreak(ADD_ALL_AT_INDEX_THROWS_WRONG_EXCEPTION_WHEN_ARGUMENT_IS_NULL)) {
                throw new RuntimeException();
            }

            boolean result;
            if (hasBreak(ADD_ALL_AT_INDEX_ADDS_TO_THE_END)) {
                result = list.addAll(c);
            } else if (!hasBreak(ADD_ALL_AT_INDEX_DOES_NOT_ADD_ANY_ELEMENTS)) {
                result = list.addAll(index, c);
            } else {
                result = false;
            }

            if (hasBreak(ADD_ALL_AT_INDEX_ALWAYS_RETURNS_TRUE)) {
                return true;
            } else if (hasBreak(ADD_ALL_AT_INDEX_ALWAYS_RETURNS_FALSE)) {
                return false;
            } else if (hasBreak(ADD_ALL_AT_INDEX_ALWAYS_RETURNS_OPPOSITE_VALUE)) {
                return !result;
            } else {
                return result;
            }
        } else {
            if (hasBreak(ADD_ALL_AT_INDEX_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED)) {
                throw new RuntimeException();
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    /// Implements the [replaceAll][List#replaceAll] method from the [List] interface. This method can be broken using
    /// the following collection breaks:
    ///
    /// | Break                    | Description                                     |
    /// | ------------------------ | ----------------------------------------------- |
    /// | REPLACE_ALL_DOES_NOT_REPLACE_ELEMENTS | The `replaceAll` method does not replace any elements |
    /// | REPLACE_ALL_SKIPS_FIRST_ELEMENT | The `replaceAll` method skips the first element |
    /// | REPLACE_ALL_SKIPS_LAST_ELEMENT | The `replaceAll` method skips the last element |
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildList(1,2,3,4,5)
    ///         .withBreak(REPLACE_ALL_DOES_NOT_REPLACE_ELEMENTS)
    ///         .build();
    /// ```
    /// @throws UnsupportedOperationException if the `replaceAll` operation is not supported by this list
    /// @throws NullPointerException if the specified operator is null or if the operator result is a null value and
    ///         this list does not permit null elements.
    /// @see List#replaceAll(UnaryOperator)
    @Override
    public void replaceAll(final @NonNull UnaryOperator<E> operator) {
        if (supportsMethod(ListMethods.REPLACE_ALL)) {
            int start = 0;
            int end = list.size();
            if (hasBreak(REPLACE_ALL_SKIPS_FIRST_ELEMENT)) {
                start = 1;
            } else if (hasBreak(REPLACE_ALL_SKIPS_LAST_ELEMENT)) {
                end = size() - 1;
            } else if (hasBreak(REPLACE_ALL_DOES_NOT_REPLACE_ELEMENTS)) {
                return;
            }
            for (int i = start; i < end; i++) {
                E e = operator.apply(list.get(i));
                if (e == null && !permitsNulls()) {
                    throw new NullPointerException();
                }
                list.set(i, e);
            }
        } else {
            throw new UnsupportedOperationException("Unsupported method: addAll");
        }
    }

    // Implements the [sort][List#sort] method from the [List] interface. This method can be broken using the
    // following collection breaks:
    ///
    /// | Break                    | Description                                     |
    /// | ------------------------ | ----------------------------------------------- |
    /// | SORT_DOES_NOT_SORT_THE_ELEMENTS | The `sort` method does not sort the elements. |
    /// | SORT_REVERSES_THE_ORDER | The `sort` method sorts the elements in the reverse order. |
    /// | SORT_THROWS_ON_NULL_ARGUMENT | The `sort` method throws a `NullPointerException` if the argument is `null` |
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildList(1,2,3,4,5)
    ///         .withBreak(SORT_DOES_NOT_SORT_THE_ELEMENTS)
    ///         .build();
    /// ```
    /// @param c the Comparator` used to compare list elements. A `null` value indicates that the elements'
    ///          [natural ordering][Comparable] should be used.
    /// @throws ClassCastException if the list contains elements that are not mutually comparable using the specified
    ///                            comparator
    /// @throws UnsupportedOperationException if the `sort` operation is not supported by this list.
    /// @see List#sort(Comparator)
    @Override
    public void sort(final Comparator<? super E> c) {
        if (supportsMethod(ListMethods.REPLACE_ALL)) {
            if (hasBreak(SORT_REVERSES_THE_ORDER)) {
                list.sort(c.reversed());
            } else if (hasBreak(SORT_THROWS_ON_NULL_ARGUMENT)) {
                if (c == null) {
                    throw new NullPointerException();
                }
                list.sort(c);
            } else if (!hasBreak(SORT_DOES_NOT_SORT_THE_ELEMENTS)) {
                list.sort(c);
            }
        } else {
            throw new UnsupportedOperationException("Unsupported method: sort");
        }
    }

    // Implements the [get][List#get] method from the [List] interface. This method can be broken using the
    // following collection breaks:
    ///
    /// | Break                    | Description                                     |
    /// | ------------------------ | ----------------------------------------------- |
    /// | GET_ALWAYS_RETURNS_NULL | The `get` method always returns `null` |
    /// | GET_ALWAYS_RETURNS_THE_FIRST_ELEMENT | The `get` methods always returns the first element. |
    /// | GET_ALWAYS_RETURNS_THE_LAST_ELEMENT | The `get` methods always returns the last element. |
    /// | GET_RETURNS_THE_NEXT_ELEMENT | The `get` methods returns the next element (*i.e.* at index + 1). |
    /// | GET_RETURNS_THE_PREVIOUS_ELEMENT | The `get` methods returns the previous element (*i.e.* at index - 1). |
    /// | GET_RETURNS_NULL_ON_BAD_INDEX | The `get` methods returns `null` on a bad index instead of throwing en exception. |
    /// | GET_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX | The `get` methods throws the wrong exception on a bad index. |
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildList(1,2,3,4,5)
    ///         .withBreak(GET_ALWAYS_RETURNS_NULL)
    ///         .build();
    /// ```
    /// @param index index of the element to return.
    /// @return the element at the specified position in this list, or possibly a different element if the list is broken.
    /// @throws IndexOutOfBoundsException if the index is out of range `index < 0 || index >= size()}`
    @Override
    public E get(final int index) {
        if (supportsMethod(ListMethods.GET)) {
            if (hasBreak(GET_ALWAYS_RETURNS_NULL)) {
                return null;
            } else if (hasBreak(GET_ALWAYS_RETURNS_THE_FIRST_ELEMENT)) {
                return list.getFirst();
            } else if (hasBreak(GET_ALWAYS_RETURNS_THE_LAST_ELEMENT)) {
                return list.getLast();
            } else if (hasBreak(GET_RETURNS_THE_NEXT_ELEMENT)) {
                return list.get(index + 1);
            } else if (hasBreak(GET_RETURNS_THE_PREVIOUS_ELEMENT)) {
                return list.get(index - 1);
            } else {
                if (index < 0 || index >= list.size()) {
                    if (hasBreak(GET_RETURNS_NULL_ON_BAD_INDEX)) {
                        return null;
                    } else if (hasBreak(GET_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX)) {
                        throw new IllegalArgumentException();
                    }
                }
                return list.get(index);
            }
        } else {
            throw new UnsupportedOperationException("Unsupported method: sort");
        }
    }

    // Implements the [set][List#set] method from the [List] interface. This method can be broken using the
    // following collection breaks:
    ///
    /// | Break                    | Description                                     |
    /// | ------------------------ | ----------------------------------------------- |
    /// | SET_DOES_NOT_CHANGE_THE_ELEMENT | The `set` method does not change the element. |
    /// | SET_ALWAYS_RETURNS_NULL | The `set` method always returns `null` |
    /// | SET_CHANGES_THE_NEXT_ELEMENT | The `set` methods changes the next element (*i.e.* at index + 1). |
    /// | SET_CHANGES_THE_PREVIOUS_ELEMENT | The `set` methods changes the previous element (*i.e.* at index - 1). |
    /// | SET_RETURNS_NULL_ON_BAD_INDEX | The `set` methods returns `null` on a bad index instead of throwing en exception. |
    /// | SET_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX | The `get` methods throws the wrong exception on a bad index. |
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildList(1,2,3,4,5)
    ///         .withBreak(SET_DOES_NOT_CHANGE_THE_ELEMENT)
    ///         .build();
    /// ```
    /// @param index index of the element to return.
    /// @param element the element to add_singleElement_returnsTrueAndUpdatesSize at the index.
    /// @return the element at the specified position in this list, or possibly a different element if the list is broken.
    /// @throws IndexOutOfBoundsException if the index is out of range `index < 0 || index >= size()}`
    @Override
    public E set(final int index, final E element) {
        if (supportsMethod(ListMethods.SET)) {
            if (index < 0 || index >= list.size()) {
                if (hasBreak(SET_RETURNS_NULL_ON_BAD_INDEX)) {
                    return null;
                } else if (hasBreak(SET_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX)) {
                    throw new IllegalArgumentException();
                }
            }

            E result;
            if (hasBreak(SET_CHANGES_THE_NEXT_ELEMENT)) {
                result = list.set(index + 1, element);
            } else if (hasBreak(SET_CHANGES_THE_PREVIOUS_ELEMENT)) {
                result = list.set(index - 1, element);
            } else if (hasBreak(SET_DOES_NOT_CHANGE_THE_ELEMENT)) {
                result = list.get(index);
            } else {
                result = list.set(index, element);
            }

            if (hasBreak(SET_ALWAYS_RETURNS_NULL)) {
                return null;
            } else {
                return result;
            }
        } else {
            throw new UnsupportedOperationException("Unsupported method: set");
        }
    }

    /// Implements the [add_singleElement_returnsTrueAndUpdatesSize][List#add(int, Object)] method from the [List] interface. This method can be broken using the
    /// following collection breaks:
    ///
    /// | Break                    | Description                                     |
    /// | ------------------------ | ----------------------------------------------- |
    /// | ADD_AT_INDEX_DOES_NOT_ADD_THE_ELEMENT | The `add_singleElement_returnsTrueAndUpdatesSize` method will not add_singleElement_returnsTrueAndUpdatesSize an element to the collection |
    /// | ADD_AT_INDEX_ADDS_AT_NEXT_POSITION | The `add_singleElement_returnsTrueAndUpdatesSize` method adds the element at the next position. |
    /// | ADD_AT_INDEX_ADDS_AT_PREVIOUS_POSITION | The `add_singleElement_returnsTrueAndUpdatesSize` method adds the element at the previous position. |
    /// | ADD_AT_INDEX_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX | The `add_singleElement_returnsTrueAndUpdatesSize` method throws the wrong exception when the index is out of bounds. |
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildList(1,2,3,4,5)
    ///         .withBreak(ADD_AT_INDEX_DOES_NOT_ADD_THE_ELEMENT)
    ///         .build();
    /// ```
    /// @param index index at which the specified element is to be inserted
    /// @param element element to be inserted
    /// @throws UnsupportedOperationException if the `add_singleElement_returnsTrueAndUpdatesSize` operation is not supported by this list
    /// @throws ClassCastException if the class of the specified element prevents it from being added to this list
    /// @throws NullPointerException if the specified element is null and this list does not permit null elements
    /// @throws IllegalArgumentException if some property of the specified element prevents it from being added to this list
    /// @throws IndexOutOfBoundsException if the index is out of range (`index < 0 || index > size()`)
    @Override
    public void add(final int index, final E element) {
        if (supportsMethod(ListMethods.SET)) {
            if ((index < 0 || index >= list.size()) && hasBreak(ADD_AT_INDEX_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX)) {
                throw new IllegalArgumentException();
            } else if (hasBreak(ADD_AT_INDEX_ADDS_AT_NEXT_POSITION)) {
                list.add(index + 1, element);
            } else if (hasBreak(ADD_AT_INDEX_ADDS_AT_NEXT_POSITION)) {
                list.add(index - 1, element);
            } else if (!hasBreak(ADD_AT_INDEX_DOES_NOT_ADD_THE_ELEMENT)) {
                list.add(index, element);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /// Implements the [remove][List#remove(int)] method from the [List] interface. This method can be broken using the
    /// following collection breaks:
    ///
    /// | Break                    | Description                                     |
    /// | ------------------------ | ----------------------------------------------- |
    /// | REMOVE_AT_INDEX_DOES_NOT_REMOVE_THE_ELEMENT | The `remove` method does not remove the element. |
    /// | REMOVE_AT_INDEX_REMOVES_THE_NEXT_ELEMENT | The `remove` method removes the element from the next position. |
    /// | REMOVE_AT_INDEX_REMOVES_THE_PREVIOUS_ELEMENT | The `remove` method removes the element from the previous position. |
    /// | REMOVE_AT_INDEX_RETURNS_NULL_ON_BAD_INDEX | The `remove` method returns `null` when the index is out of bounds. |
    /// | REMOVE_AT_INDEX_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX | The `remove` method throws the wrong exception when the index is out of bounds. |
    /// | REMOVE_AT_INDEX_ALWAYS_RETURNS_NULL | The `remove` method always returns `null` |
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildList(1,2,3,4,5)
    ///         .withBreak(REMOVE_AT_INDEX_DOES_NOT_REMOVE_THE_ELEMENT)
    ///         .build();
    /// ```
    /// @param index the index of the element to be removed
    /// @return the element previously at the specified position
    /// @throws UnsupportedOperationException if the `remove` operation is not supported by this list
    /// @throws IndexOutOfBoundsException if the index is out of range (`@code index < 0 || index >= size()`)
    @Override
    public E remove(final int index) {
        if (supportsMethod(ListMethods.REMOVE_AT_INDEX)) {
            if (index < 0 || index >= list.size()) {
                if (hasBreak(REMOVE_AT_INDEX_RETURNS_NULL_ON_BAD_INDEX)) {
                    return null;
                } else if (hasBreak(REMOVE_AT_INDEX_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX)) {
                    throw new IllegalArgumentException();
                }
            }

            E result;
            if (hasBreak(REMOVE_AT_INDEX_REMOVES_THE_NEXT_ELEMENT)) {
                result = list.remove(index + 1);
            } else if (hasBreak(REMOVE_AT_INDEX_REMOVES_THE_PREVIOUS_ELEMENT)) {
                result = list.remove(index - 1);
            } else if (hasBreak(REMOVE_AT_INDEX_DOES_NOT_REMOVE_THE_ELEMENT)) {
                result = list.get(index);
            } else {
                result = list.remove(index);
            }

            if (hasBreak(REMOVE_AT_INDEX_ALWAYS_RETURNS_NULL)) {
                return null;
            } else {
                return result;
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public int indexOf(final Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(final Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public @NonNull ListIterator<E> listIterator() {
        return list.listIterator();
    }

    @Override
    public @NonNull ListIterator<E> listIterator(final int index) {
        return list.listIterator(index);
    }

    @Override
    public @NonNull List<E> subList(final int fromIndex, final int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    /// Builder for creating BreakableList instances.
    /// @param <E> the element type for the list
    public static class Builder<E>
            extends AbstractBuilder<BreakableList.Builder<E>, BreakableList<E>, E> {

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
        public Builder(final BreakableList.Builder<E> other) {
            super(other);
        }

        @Override
        public Builder<E> self() {
            return this;
        }

        @Override
        public BreakableList.Builder<E> copy() {
            return new BreakableList.Builder<>(this);
        }

        /// Build a BreakableList object using the values from the builder.
        /// @return a new BreakableList object.
        public BreakableList<E> build() {
            return new BreakableList<>(new ArrayList<>(elements()), new HashSet<>(breaks()),
                    new HashMap<>(methodStatuses()), characteristics(), permits(), isSafe(), compatibleType());
        }
    }
}
