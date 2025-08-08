package org.soliscode.test.breakable;

import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.soliscode.test.contract.CollectionMethods;

import java.util.*;
import java.util.function.UnaryOperator;

/// A list that can be broken in well-defined ways in order to test collection utilities or testing
/// classes.
///
/// # Breaks
/// In addition to the breaks for [BreakableSequencedCollection], the breaks found in [?] are also supported.
///
/// Builder methods are provided to make declaring a broken list easier, for example:
/// ```java
///     BreakableList<Integer> broken = Breakables.buildList(1, 2)
///         .withBreak(List.ADD_ALL_AT_INDEX_DOES_NOT_ADD_ANY_ELEMENTS)
///         .build();
/// ```
/// @author evanbergstrom
/// @param <E> The elements type for the `List`.
/// @see CollectionMethods
/// @since 1.0.0
public class BreakableList<E> extends BreakableSequencedCollection<E> implements List<E> {

    private final @NotNull List<E> list;

    /// Creates an empty list that has no breaks.
    public BreakableList() {
        this(new ArrayList<>(), new HashSet<>(), 0);
    }

    /// Creates a breakable sequenced collection from an existing instance.
    /// @param other the breakable collection to copy.
    public BreakableList(final @NotNull BreakableList<E> other) {
        this(other.list, new HashSet<>(), 0);
    }

    /// Creates a breakable iterable from an iterable.
    /// @param collection the iterator to use for the elements.
    public BreakableList(final @NotNull List<E> collection) {
        this(collection, new HashSet<>(), 0);
    }

    /// Creates a `BreakableSequencedCollection` from en existing collection and specifying the breaks and collection
    /// characteristics. Rather than calling this constructor directly, consider using the builder
    /// [BreakableCollection.Builder].
    /// @param c               the initial elements for the breakable collection.
    /// @param breaks          the breaks for the collection.
    /// @param characteristics the characteristics for the collection.
    /// @throws NullPointerException if either the `c` or the `breaks` parameters are null.
    public BreakableList(@NotNull List<E> c, @NotNull Collection<Break> breaks, int characteristics) {
        super(c, breaks, characteristics);
        this.list = Objects.requireNonNull(c);
    }

    /// The [addAll][List#addAll(int, Collection)] method always return a result of `true`, even if the
    /// element is not added.
    /// @see BreakableList#addAll(int, Collection)
    public static final Break ADD_ALL_AT_INDEX_ALWAYS_RETURNS_TRUE = new Break("addAll(int, Collection) always returns true");

    /// The [addAll][List#addAll(int, Collection)] method always return a result of `false`, even if the
    /// element is added.
    /// @see BreakableList#addAll(int, Collection)
    public static final Break ADD_ALL_AT_INDEX_ALWAYS_RETURNS_FALSE = new Break("addAll(int, Collection) always returns false");

    /// The [addAll][List#addAll(int, Collection)] method always returns the opposite of the appropriate result.
    /// @see BreakableList#addAll(int, Collection)
    public static final Break ADD_ALL_AT_INDEX_ALWAYS_RETURNS_OPPOSITE_VALUE = new Break("addAll(int, Collection) always returns opposite value");

    /// The [addAll][List#addAll(int, Collection)] method adds to the end of the collection.
    /// @see BreakableList#addAll(int, Collection)
    public static final Break ADD_ALL_AT_INDEX_ADDS_TO_THE_END = new Break("addAll(int, Collection) adds to the end");

    /// The [addAll][List#addAll(int, Collection)] method method throws the wrong exception when the index is out of bounds.
    /// @see BreakableList#addAll(int, Collection)
    public static final Break ADD_ALL_AT_INDEX_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX = new Break("addAll(int, Collection) throws wrong exception on bad index");

    /// The [addAll][List#addAll(int, Collection)] method method throws the wrong exception if it is not supported.
    /// @see BreakableList#addAll(int, Collection)
    public static final Break ADD_ALL_AT_INDEX_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED = new Break("addAll(int, Collection) throws wrong exception if not supported");

    /// The [addAll][List#addAll(int, Collection)] method method throws the wrong exception if the argument is null.
    /// @see BreakableList#addAll(int, Collection)
    public static final Break ADD_ALL_AT_INDEX_THROWS_WRONG_EXCEPTION_WHEN_ARGUMENT_IS_NULL = new Break("addAll(int, Collection) throws wrong exception when argument is null");

    /// The [addAll][List#addAll(int, Collection)] method will not add any elements to the collection
    /// @see BreakableList#addAll(int, Collection)
    public static final Break ADD_ALL_AT_INDEX_DOES_NOT_ADD_ANY_ELEMENTS = new Break("addAll(int, Collection) does not add elements");

    /// The [add][List#add(int, Object)] method will not add an element to the collection
    /// @see BreakableList#add(int, Object)
    public static final Break ADD_AT_INDEX_DOES_NOT_ADD_THE_ELEMENT = new Break("");

    /// The [add][List#add(int, Object)] method adds the element at the next position.
    /// @see BreakableList#add(int, Object)
    public static final Break ADD_AT_INDEX_ADDS_AT_NEXT_POSITION = new Break("");

    /// The [add][List#add(int, Object)] method adds the element at the previous position.
    /// @see BreakableList#add(int, Object)
    public static final Break ADD_AT_INDEX_ADDS_AT_PREVIOUS_POSITION = new Break("");

    /// The [add][List#add(int, Object)] method throws the wrong exception when the index is out of bounds.
    /// @see BreakableList#add(int, Object)
    public static final Break ADD_AT_INDEX_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX = new Break("");

    /// The [get][List#get(int)] method always returns `null`
    /// @see BreakableList#get(int)
    public static final Break GET_ALWAYS_RETURNS_NULL = new Break("");

    /// The [get][List#get(int)] method always returns the first element.
    /// @see BreakableList#get(int)
    public static final Break GET_ALWAYS_RETURNS_THE_FIRST_ELEMENT = new Break("");

    /// The [get][List#get(int)] method always returns the last element.
    /// @see BreakableList#get(int)
    public static final Break GET_ALWAYS_RETURNS_THE_LAST_ELEMENT = new Break("");

    /// The [get][List#get(int)] method returns the next element (*i.e.* at index + 1).
    /// @see BreakableList#get(int)
    public static final Break GET_RETURNS_THE_NEXT_ELEMENT = new Break("");
    
    /// The [get][List#get(int)] method returns the previous element (*i.e.* at index - 1).
    /// @see BreakableList#get(int)
    public static final Break GET_RETURNS_THE_PREVIOUS_ELEMENT = new Break("");

    /// The [get][List#get(int)] method returns `null` on a bad index instead of throwing en exception.
    /// @see BreakableList#get(int)
    public static final Break GET_RETURNS_NULL_ON_BAD_INDEX = new Break("");

    /// The [get][List#get(int)] method throws the wrong exception on a bad index.
    /// @see BreakableList#get(int)
    public static final Break GET_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX = new Break("");

    /// The [remove][List#remove(int)] method does not remove the element.
    /// @see BreakableList#remove(int)
    public static final Break REMOVE_AT_INDEX_DOES_NOT_REMOVE_THE_ELEMENT = new Break("");

    /// The [remove][List#remove(int)] method removes the next element.
    /// @see BreakableList#remove(int)
    public static final Break REMOVE_AT_INDEX_REMOVES_THE_NEXT_ELEMENT = new Break("");

    /// The [remove][List#remove(int)] method removes the previous element
    /// @see BreakableList#remove(int)
    public static final Break REMOVE_AT_INDEX_REMOVES_THE_PREVIOUS_ELEMENT = new Break("");

    /// The [remove][List#remove(int)] method returns `null` when the index is out of bounds.
    /// @see BreakableList#remove(int)
    public static final Break REMOVE_AT_INDEX_RETURNS_NULL_ON_BAD_INDEX = new Break("");

    /// The [remove][List#remove(int)] method throws the wrong exception when the index is out of bounds.
    /// @see BreakableList#remove(int)
    public static final Break REMOVE_AT_INDEX_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX = new Break("");

    /// The [remove][List#remove(int)] method always returns `null`
    /// @see BreakableList#remove(int)
    public static final Break REMOVE_AT_INDEX_ALWAYS_RETURNS_NULL = new Break("");

    /// The [replaceAll][List#replaceAll()] method does not replace any elements
    /// @see BreakableList#replaceAll(UnaryOperator)
    public static final Break REPLACE_ALL_DOES_NOT_REPLACE_ELEMENTS = new Break("");

    /// The [replaceAll][List#replaceAll()] method skips the first element.
    /// @see BreakableList#replaceAll(UnaryOperator)
    public static final Break REPLACE_ALL_SKIPS_FIRST_ELEMENT = new Break("");

    /// The [replaceAll][List#replaceAll()] method skips the last element
    /// @see BreakableList#replaceAll(UnaryOperator)
    public static final Break REPLACE_ALL_SKIPS_LAST_ELEMENT = new Break("");

    /// The [set][List#set(int)] method does not change the element.
    /// @see BreakableList#set(int, Object)
    public static final Break SET_DOES_NOT_CHANGE_THE_ELEMENT = new Break("");

    /// The [set][List#set(int)] method always returns `null`
    /// @see BreakableList#set(int, Object)
    public static final Break SET_ALWAYS_RETURNS_NULL = new Break("");

    /// The [set][List#set(int)] method changes the next element (*i.e.* at index + 1).
    /// @see BreakableList#set(int, Object)
    public static final Break SET_CHANGES_THE_NEXT_ELEMENT = new Break("");

    /// The [set][List#set(int)] method changes the previous element (*i.e.* at index - 1).
    /// @see BreakableList#set(int, Object)
    public static final Break SET_CHANGES_THE_PREVIOUS_ELEMENT = new Break("");

    /// The [set][List#set(int)] method returns `null` on a bad index instead of throwing en exception.
    /// @see BreakableList#set(int, Object)
    public static final Break SET_RETURNS_NULL_ON_BAD_INDEX = new Break("");

    /// The [set][List#set(int)] method throws the wrong exception on a bad index.
    /// @see BreakableList#set(int, Object)
    public static final Break SET_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX = new Break("");

    /// The [sort][List#sort()] method sorts the elements in the reverse order.
    /// @see BreakableList#sort(Comparator)
    public static final Break SORT_REVERSES_THE_ORDER = new Break("");
    
    /// The [sort][List#sort()] method throws a `NullPointerException` if the argument is `null`.
    /// @see BreakableList#sort(Comparator)
    public static final Break SORT_THROWS_ON_NULL_ARGUMENT = new Break("");

    /// The [sort][List#sort()] method does not sort the elements.
    /// @see BreakableList#sort(Comparator)
    public static final Break SORT_DOES_NOT_SORT_THE_ELEMENTS  = new Break("");

    @Override
    public List<E> reversed() {
        return list.reversed();
    }

    // Implements the [addAll][List#addAll] method from the [List] interface. This method can be broken using the
    // following collection breaks:
    ///
    /// | Break                    | Description                                     |
    /// | ------------------------ | ----------------------------------------------- |
    /// | ADD_ALL_AT_INDEX_DOES_NOT_ADD_ANY_ELEMENTS | The `addAll` method will not add any elements to the collection. |
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
    public boolean addAll(int index, @NotNull Collection<? extends E> c) {
        if (supportsMethod(CollectionMethods.AddAll)) {
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
        } else{
            if (hasBreak(ADD_ALL_AT_INDEX_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED)) {
                throw new RuntimeException();
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    // Implements the [replaceAll][List#replaceAll] method from the [List] interface. This method can be broken using the
    // following collection breaks:
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
    public void replaceAll(final UnaryOperator<E> operator) {
        if (supportsMethod(CollectionMethods.ReplaceAll)) {
            int start = 0;
            int end = list.size();
            if (hasBreak(REPLACE_ALL_SKIPS_FIRST_ELEMENT)) {
                start = 1;
            } else if (hasBreak(REPLACE_ALL_SKIPS_LAST_ELEMENT)) {
                end = size() -1;
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
        } else{
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
    public void sort(Comparator<? super E> c) {
        if (supportsMethod(CollectionMethods.ReplaceAll)) {
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
        } else{
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
    public E get(int index) {
        if (supportsMethod(CollectionMethods.Get)) {
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
        } else{
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
    /// @param element the element to add at the index.
    /// @return the element at the specified position in this list, or possibly a different element if the list is broken.
    /// @throws IndexOutOfBoundsException if the index is out of range `index < 0 || index >= size()}`
    @Override
    @Contract(mutates="this") @Flow(sourceIsContainer = true)
    public E set(int index, E element) {
        if (supportsMethod(CollectionMethods.Set)) {
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
        } else{
            throw new UnsupportedOperationException("Unsupported method: set");
        }
    }

    /// Implements the [add][List#add(int, Object)] method from the [List] interface. This method can be broken using the
    /// following collection breaks:
    ///
    /// | Break                    | Description                                     |
    /// | ------------------------ | ----------------------------------------------- |
    /// | ADD_AT_INDEX_DOES_NOT_ADD_THE_ELEMENT | The `add` method will not add an element to the collection |
    /// | ADD_AT_INDEX_ADDS_AT_NEXT_POSITION | The `add` method adds the element at the next position. |
    /// | ADD_AT_INDEX_ADDS_AT_PREVIOUS_POSITION | The `add` method adds the element at the previous position. |
    /// | ADD_AT_INDEX_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX | The `add` method throws the wrong exception when the index is out of bounds. |
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildList(1,2,3,4,5)
    ///         .withBreak(ADD_AT_INDEX_DOES_NOT_ADD_THE_ELEMENT)
    ///         .build();
    /// ```
    /// @param index index at which the specified element is to be inserted
    /// @param element element to be inserted
    /// @throws UnsupportedOperationException if the `add` operation is not supported by this list
    /// @throws ClassCastException if the class of the specified element prevents it from being added to this list
    /// @throws NullPointerException if the specified element is null and this list does not permit null elements
    /// @throws IllegalArgumentException if some property of the specified element prevents it from being added to this list
    /// @throws IndexOutOfBoundsException if the index is out of range (`index < 0 || index > size()`)
    @Override
    @Contract(mutates="this")
    public void add(int index, E element) {
        if (supportsMethod(CollectionMethods.Set)) {
            if ((index < 0 || index >= list.size()) && hasBreak(ADD_AT_INDEX_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX)) {
                throw new IllegalArgumentException();
            }
            else if (hasBreak(ADD_AT_INDEX_ADDS_AT_NEXT_POSITION)) {
                list.add(index + 1, element);
            } else if (hasBreak(ADD_AT_INDEX_ADDS_AT_NEXT_POSITION)) {
                list.add(index - 1, element);
            } else if (!hasBreak(ADD_AT_INDEX_DOES_NOT_ADD_THE_ELEMENT)) {
                list.add(index, element);
            }
        } else{
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
    public E remove(int index) {
        if (supportsMethod(CollectionMethods.RemoveAtIndex)) {
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
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public @NotNull ListIterator<E> listIterator() {
        return list.listIterator();
    }

    @Override
    public @NotNull ListIterator<E> listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public @NotNull List<E> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }
}
