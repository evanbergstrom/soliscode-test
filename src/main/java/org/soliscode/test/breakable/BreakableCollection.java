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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.soliscode.test.OptionalMethod;
import org.soliscode.test.contract.CollectionMethods;
import org.soliscode.test.contract.support.CollectionProviderSupport;
import org.soliscode.test.provider.*;
import org.soliscode.test.util.CollectionTestOps;
import org.soliscode.test.util.IterableTestOps;

import java.util.*;
import java.util.function.Predicate;

/// A collection that can be broken in well-defined ways in order to test collection utilities or testing classes.
///
/// # Breaks
/// The breaks that are supported for this class are listed in the description of the method that they impact.
/// In addition it supports the breaks in the [BreakableIterable], [BreakableIterator], and [BreakableSpliterator]
/// classes. Any iterator or spliterator breaks that are added to this iterable will be passes along to iterator or
/// spliterator instances that are created.
///
/// Any breaks that are not listed in the supported classes can be added to an instance of `BreakableCollection`, but
/// will not have any impact on how it functions.
///
/// Builder methods are provided to make declaring a broken collection easier, for example:
/// ```java
///     BreakableCollection<Integer> broken = Breakables.buildCollection.of(1, 2)
///         .withBreak(CollectionBreaks,EMPTY_ALWAYS_RETURNS_TRUE)
///         .build();
/// ```
/// @author evanbergstrom
/// @param <E> The elements type for the `Collection`.
/// @see CollectionMethods
/// @since 1.0.0
public class BreakableCollection<E> extends BreakableIterable<E> implements Collection<E> {

    private static final int DEFAULT_CAPACITY = 10;

    private final @NotNull Collection<E> collection;
    private boolean permitsNulls;
    private boolean permitsDuplicates;
    private boolean permitsIncompatibleTypes;
    private final @NotNull Class<?> compatibleType;

    /// The [add][Collection#add] method does not add an element to the collection
    /// @see BreakableCollection#add(Object)
    public static final Break ADD_DOES_NOT_ADD_ELEMENT = new Break("ADD_DOES_NOT_ADD_ELEMENT");

    /// The [add][Collection#add] method always return a result of `true`, even if the element is not added.
    /// @see BreakableCollection#add(Object)
    public static final Break ADD_ALWAYS_RETURNS_TRUE = new Break("ADD_ALWAYS_RETURNS_TRUE");

    /// The [add][java.util.Collection#add] method always return a result of `false`, even if the element is added.
    /// @see BreakableCollection#add(Object)
    public static final Break ADD_ALWAYS_RETURNS_FALSE = new Break("ADD_ALWAYS_RETURNS_FALSE");

    /// The [add][java.util.Collection#add] method always returns the opposite of the appropriate result.
    /// @see BreakableCollection#add(Object)
    public static final Break ADD_ALWAYS_RETURNS_OPPOSITE_VALUE = new Break("ADD_ALWAYS_RETURNS_OPPOSITE_VALUE");

    /// The [addAll][java.util.Collection#addAll] method will not add any elements to the collection
    /// @see BreakableCollection#addAll(Collection)
    public static final Break ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS = new Break("ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS");

    /// The [addAll][Collection#addAll] method always return a result of `true`, even if the element is not
    /// added.
    /// @see BreakableCollection#addAll(Collection)
    public static final Break ADD_ALL_ALWAYS_RETURNS_TRUE = new Break("ADD_ALL_ALWAYS_RETURNS_TRUE");

    /// The [addAll][Collection#addAll(Collection)] method always return a result of `false`, even if the element is added.
    /// @see BreakableCollection#addAll(Collection)
    public static final Break ADD_ALL_ALWAYS_RETURNS_FALSE = new Break("ADD_ALL_ALWAYS_RETURNS_FALSE");

    /// The [addAll][Collection#addAll(Collection)] method always returns the opposite of the appropriate result.
    /// @see BreakableCollection#addAll(Collection)
    public static final Break ADD_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE = new Break("ADD_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE");

    /// The [addAll][Collection#addAll(Collection)] method skips the first element to be added.
    /// @see BreakableCollection#addAll(Collection)
    public static final Break ADD_ALL_SKIPS_FIRST_ELEMENT = new Break("ADD_ALL_SKIPS_FIRST_ELEMENT");

    /// The [addAll][Collection#addAll(Collection)] method skips the last element to be added.
    /// @see BreakableCollection#addAll(Collection)
    public static final Break ADD_ALL_SKIPS_LAST_ELEMENT = new Break("ADD_ALL_SKIPS_LAST_ELEMENT");

    /// The [clear][Collection#clear] method doers not remove any elements,
    /// @see BreakableCollection#clear()
    public static final Break CLEAR_DOES_NOT_REMOVE_ANY_ELEMENTS = new Break("CLEAR_DOES_NOT_REMOVE_ANY_ELEMENTS");

    /// The [clear][Collection#clear] method skips the first element,
    /// @see BreakableCollection#clear()
    public static final Break CLEAR_SKIPS_FIRST_ELEMENT = new Break("CLEAR_SKIPS_FIRST_ELEMENT");

    /// The [clear][Collection#clear] method skips the last element,
    /// @see BreakableCollection#clear()
    public static final Break CLEAR_SKIPS_LAST_ELEMENT = new Break("CLEAR_SKIPS_LAST_ELEMENT");

    /// The `contains` method will always return a true.
    /// @see BreakableCollection#contains(Object)
    public static final Break CONTAINS_ALWAYS_RETURNS_TRUE = new Break("CONTAINS_ALWAYS_RETURNS_TRUE");

    /// The `contains` method will always return a false.
    /// @see BreakableCollection#contains(Object)
    public static final Break CONTAINS_ALWAYS_RETURNS_FALSE = new Break("CONTAINS_ALWAYS_RETURNS_FALSE");

    /// The `contains` method will return the opposite value.
    /// @see BreakableCollection#contains(Object)
    public static final Break CONTAINS_RETURNS_OPPOSITE_VALUE = new Break("CONTAINS_RETURNS_OPPOSITE_VALUE");

    /// The `containsAll` method will always return a true.
    /// @see BreakableCollection#containsAll(Collection)
    public static final Break CONTAINS_ALL_ALWAYS_RETURNS_TRUE = new Break("CONTAINS_ALL_ALWAYS_RETURNS_TRUE");

    /// The `containsAll` method will always return a false.
    /// @see BreakableCollection#containsAll(Collection)
    public static final Break CONTAINS_ALL_ALWAYS_RETURNS_FALSE = new Break("CONTAINS_ALL_ALWAYS_RETURNS_FALSE");

    /// The `containsAll` method will return the opposite value.
    /// @see BreakableCollection#containsAll(Collection)
    public static final Break CONTAINS_ALL_RETURNS_OPPOSITE_VALUE = new Break("CONTAINS_ALL_RETURNS_OPPOSITE_VALUE");

    /// The `isEmpty` method will always return a true.
    /// @see BreakableCollection#isEmpty()
    public static final Break IS_EMPTY_ALWAYS_RETURNS_TRUE = new Break("IS_EMPTY_ALWAYS_RETURNS_TRUE");

    /// The `isEmpty` method will always return a false.
    /// @see BreakableCollection#isEmpty()
    public static final Break IS_EMPTY_ALWAYS_RETURNS_FALSE = new Break("IS_EMPTY_ALWAYS_RETURNS_FALSE");

    /// The `isEmpty` method will return the opposite value.
    /// @see BreakableCollection#isEmpty()
    public static final Break IS_EMPTY_RETURNS_OPPOSITE_VALUE = new Break("IS_EMPTY_RETURNS_OPPOSITE_VALUE");

    /// The `remove` method will not remove an element to the collection
    /// @see BreakableCollection#remove(Object)
    public static final Break REMOVE_DOES_NOT_REMOVE_ELEMENT = new Break("REMOVE_DOES_NOT_REMOVE_ELEMENT");

    /// The `remove` method always return a result of `true`, even if the element is not removed.
    /// @see BreakableCollection#remove(Object)
    public static final Break REMOVE_ALWAYS_RETURNS_TRUE = new Break("REMOVE_ALWAYS_RETURNS_TRUE");

    /// The `remove` method always return a result of `false`, even if the element is removed.
    /// @see BreakableCollection#remove(Object)
    public static final Break REMOVE_ALWAYS_RETURNS_FALSE = new Break("REMOVE_ALWAYS_RETURNS_FALSE");

    /// The `remove` method always returns the opposite of the appropriate result.
    /// @see BreakableCollection#remove(Object)
    public static final Break REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE = new Break("REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE");

    /// The [removeAll][Collection#removeAll] method will not remove any elements to the collection
    /// @see BreakableCollection#removeAll(Collection)
    public static final Break REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS = new Break("REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS");

    /// The [removeAll][Collection#removeAll] method always return a result of `true`, even if the element is not
    /// removed.
    /// @see BreakableCollection#removeAll(Collection)
    public static final Break REMOVE_ALL_ALWAYS_RETURNS_TRUE = new Break("REMOVE_ALL_ALWAYS_RETURNS_TRUE");

    /// The [removeAll][Collection#removeAll] method always return a result of `false`, even any of the elements
    /// are removed.
    /// @see BreakableCollection#removeAll(Collection)
    public static final Break REMOVE_ALL_ALWAYS_RETURNS_FALSE = new Break("REMOVE_ALL_ALWAYS_RETURNS_FALSE");

    /// The [removeAll][Collection#removeAll] method always returns the opposite of the appropriate result.
    /// @see BreakableCollection#removeAll(Collection)
    public static final Break REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE = new Break("REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE");

    /// The [removeAll][Collection#removeAll] method skips the first element to be removed.
    /// @see BreakableCollection#removeAll(Collection)
    public static final Break REMOVE_ALL_SKIPS_FIRST_ELEMENT = new Break("REMOVE_ALL_SKIPS_FIRST_ELEMENT");

    /// The [removeAll][Collection#removeAll] method skips the last element to be removed.
    /// @see BreakableCollection#removeAll(Collection)
    public static final Break REMOVE_ALL_SKIPS_LAST_ELEMENT = new Break("REMOVE_ALL_SKIPS_LAST_ELEMENT");

    /// The [removeIf][Collection#removeIf] method will not remove any elements to the collection
    /// @see BreakableCollection#removeIf(Predicate)
    public static final Break REMOVE_IF_DOES_NOT_REMOVE_ANY_ELEMENTS = new Break("REMOVE_IF_DOES_NOT_REMOVE_ANY_ELEMENTS");

    /// The [removeIf][Collection#removeIf] method always return a result of `true`, even if the element is not
    /// removed.
    /// @see BreakableCollection#removeIf(Predicate)
    public static final Break REMOVE_IF_ALWAYS_RETURNS_TRUE = new Break("REMOVE_IF_ALWAYS_RETURNS_TRUE");

    /// The [removeIf][Collection#removeIf] method always return a result of `false`, even any of the elements
    /// are removed.
    /// @see BreakableCollection#removeIf(Predicate)
    public static final Break REMOVE_IF_ALWAYS_RETURNS_FALSE = new Break("REMOVE_IF_ALWAYS_RETURNS_FALSE");

    /// The [removeIf][Collection#removeIf] method always returns the opposite of the appropriate result.
    /// @see BreakableCollection#removeIf(Predicate)
    public static final Break REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE = new Break("REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE");

    /// The [removeIf][Collection#removeIf] method skips the first element to be removed.
    /// @see BreakableCollection#removeIf(Predicate)
    public static final Break REMOVE_IF_SKIPS_FIRST_ELEMENT = new Break("REMOVE_IF_SKIPS_FIRST_ELEMENT");

    /// The [removeIf][Collection#removeIf] method skips the last element to be removed.
    /// @see BreakableCollection#removeIf(Predicate)
    public static final Break REMOVE_IF_SKIPS_LAST_ELEMENT = new Break("REMOVE_IF_SKIPS_LAST_ELEMENT");

    /// The [retainAll][java.util.Collection#retainAll] method will not retain any elements to the collection
    /// @see BreakableCollection#retainAll(Collection)
    public static final Break RETAIN_ALL_DOES_NOT_RETAIN_ANY_ELEMENTS = new Break("RETAIN_ALL_DOES_NOT_RETAIN_ANY_ELEMENTS");

    /// The [retainAll][java.util.Collection#retainAll] method always return a result of `true`, even if the collection
    /// is not modified.
    /// @see BreakableCollection#retainAll(Collection)
    public static final Break RETAIN_ALL_ALWAYS_RETURNS_TRUE = new Break("RETAIN_ALL_ALWAYS_RETURNS_TRUE");

    /// The [retainAll][java.util.Collection#retainAll] method always return a result of `false`, even the collection
    /// is modified
    /// @see BreakableCollection#retainAll(Collection)
    public static final Break RETAIN_ALL_ALWAYS_RETURNS_FALSE = new Break("RETAIN_ALL_ALWAYS_RETURNS_FALSE");

    /// The [retainAll][java.util.Collection#retainAll] method always returns the opposite of the appropriate result.
    /// @see BreakableCollection#retainAll(Collection)
    public static final Break RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE = new Break("RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE");

    /// The [retainAll][java.util.Collection#retainAll] method skips the first element to be retained.
    /// @see BreakableCollection#retainAll(Collection)
    public static final Break RETAIN_ALL_SKIPS_FIRST_ELEMENT = new Break("RETAIN_ALL_SKIPS_FIRST_ELEMENT");

    /// The [retainAll][java.util.Collection#retainAll] method skips the last element to be retained.
    /// @see BreakableCollection#retainAll(Collection)
    public static final Break RETAIN_ALL_SKIPS_LAST_ELEMENT = new Break("RETAIN_ALL_SKIPS_LAST_ELEMENT");

    /// The `size` method will always return zero.
    /// @see BreakableCollection#size()
    public static final Break SIZE_ALWAYS_RETURNS_ZERO = new Break("SIZE_ALWAYS_RETURNS_ZERO");

    /// The `size` method will always return a constant value.
    /// @see BreakableCollection#size()
    public static final Break SIZE_ALWAYS_RETURNS_CONSTANT_VALUE = new Break("SIZE_ALWAYS_RETURNS_CONSTANT_VALUE");

    /// The 'toArray' method will always return an empty array
    /// @see BreakableCollection#toArray()
    public static final Break TO_ARRAY_RETURNS_EMPTY_ARRAY = new Break("TO_ARRAY_RETURNS_EMPTY_ARRAY");

    /// The 'toArray' method will always return `null`
    /// @see BreakableCollection#toArray()
    public static final Break TO_ARRAY_RETURNS_NULL = new Break("TO_ARRAY_RETURNS_NULL");

    /// The 'toArray' method will always return an array missing the first element
    /// @see BreakableCollection#toArray()
    public static final Break TO_ARRAY_MISSING_FIRST_ELEMENT = new Break("TO_ARRAY_MISSING_FIRST_ELEMENT");

    /// The 'toArray' method will always return an array missing the last element
    /// @see BreakableCollection#toArray()
    public static final Break TO_ARRAY_MISSING_LAST_ELEMENT = new Break("TO_ARRAY_MISSING_LAST_ELEMENT");

    /// The 'toArray([])' method does not copy the elements into the array
    /// @see BreakableCollection#toArray(Object[])
    public static final Break TO_ARRAY_STORE_DOES_NOT_COPY_ELEMENTS = new Break("TO_ARRAY_STORE_DOES_NOT_COPY_ELEMENTS");

    /// The 'toArray' method will always return an empty array
    /// @see BreakableCollection#toArray(Object[])
    public static final Break TO_ARRAY_STORE_RETURNS_EMPTY_ARRAY = new Break("TO_ARRAY_STORE_RETURNS_EMPTY_ARRAY");

    /// The 'toArray' method will always return `null`
    /// @see BreakableCollection#toArray(Object[])
    public static final Break TO_ARRAY_STORE_RETURNS_NULL = new Break("TO_ARRAY_STORE_RETURNS_NULL");

    /// The 'toArray' method will always return an array missing the first element
    /// @see BreakableCollection#toArray(Object[])
    public static final Break TO_ARRAY_STORE_MISSING_FIRST_ELEMENT = new Break("TO_ARRAY_STORE_MISSING_FIRST_ELEMENT");

    /// The 'toArray' method will always return an array missing the last element
    /// @see BreakableCollection#toArray(Object[])
    public static final Break TO_ARRAY_STORE_MISSING_LAST_ELEMENT = new Break("TO_ARRAY_STORE_MISSING_LAST_ELEMENT");


    /// Creates an empty collection that has no breaks.
    public BreakableCollection() {
        this(new ArrayList<>(), new HashSet<>(), 0);
    }

    /// Creates a breakable collection from an existing instance.
    /// @param other the breakable collection to copy.
    public BreakableCollection(final @NotNull BreakableCollection<E> other) {
        this(new ArrayList<>(other.collection), new HashSet<>(), 0);
    }

    /// Creates a breakable iterable from an iterable.
    /// @param collection the iterator to use for the elements.
    public BreakableCollection(final @NotNull Collection<E> collection) {
        this(new ArrayList<>(collection), new HashSet<>(), 0);
    }

    /// Creates a `BreakableCollection` from en existing collection and specifying the breaks and collection
    /// characteristics. Rather than calling this constructor directly, consider using the builder
    /// [BreakableCollection.Builder].
    /// @param c               the initial elements for the breakable collection.
    /// @param breaks          the breaks for the collection.
    /// @param characteristics the characteristics for the collection.
    /// @throws NullPointerException if either the `c` or the `breaks` parameters are null.
    public BreakableCollection(@NotNull Collection<E> c, @NotNull Collection<Break> breaks, int characteristics) {
        super(c, breaks, characteristics);
        this.collection = Objects.requireNonNull(c);
        this.permitsNulls = true;
        this.permitsDuplicates = true;
        this.permitsIncompatibleTypes = true;
        this.compatibleType = CollectionTestOps.getGenericParameter(this, 0);
    }

    /// Indicates if the collection permits null values as elements.
    /// @return `true` if the collection permits `null` elements, `false` if it does not.
    public boolean permitsNulls() {
        return permitsNulls;
    }

    /// Sets how the collection handles `null` elements.
    /// @param permitsNulls specifies if the collection permits null values, `true` if null values are permitted
    ///                     and `false` if they are not.
    public void setPermitsNulls(boolean permitsNulls) {
        this.permitsNulls = permitsNulls;
    }

    /// Indicates if the collection permits duplicate elements.
    /// @return `true` if the collection permits duplicate elements, `false` if it does not.
    public boolean permitsDuplicates() {
        return permitsDuplicates;
    }

    /// Sets how the collection handles duplicate elements.
    /// @param permitsDuplicates specifies if the collection permits duplicate values, `true` if duplicate values are
    ///                          permitted and `false` if they are not.
    public void setPermitsDuplicates(boolean permitsDuplicates) {
        this.permitsDuplicates = permitsDuplicates;
    }

    /// Indicates if the collection permits elements with incompatible types as arguments.
    /// @return `true` if the collection permits elements with incompatible types, `false` if it does not.
    public boolean permitsIncompatibleTypes() {
        return permitsIncompatibleTypes;
    }

    /// Sets how the collection handles elements with incompatible types as arguments.
    /// @param permitsIncompatibleTypes specifies if the collection permits elements with incompatible types as
    ///                                 arguments, `true` if duplicate values are permitted and `false` if they are not.
    public void setPermitsIncompatibleTypes(boolean permitsIncompatibleTypes) {
        this.permitsIncompatibleTypes = permitsIncompatibleTypes;
    }

    /// Implements the [size][Collection#size] method from the [Collection] interface. This method can be broken using
    /// the following collection breaks:
    /// - [SIZE_ALWAYS_RETURNS_ZERO][BreakableCollection#SIZE_ALWAYS_RETURNS_ZERO]
    /// - [SIZE_ALWAYS_RETURNS_CONSTANT_VALUE][BreakableCollection#SIZE_ALWAYS_RETURNS_CONSTANT_VALUE]
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .withBreak(SIZE_ALWAYS_RETURNS_ZERO)
    ///         .build();
    /// ```
    /// @return The number of elements in the collection, or a different value if the collection has been broken.
    /// @see Collection#size()
    @Override
    public int size() {
        if (hasBreak(SIZE_ALWAYS_RETURNS_ZERO)) {
            return 0;
        } else if (hasBreak(SIZE_ALWAYS_RETURNS_CONSTANT_VALUE)) {
            return DEFAULT_CAPACITY;
        } else {
            return collection.size();
        }
    }

    /// Implements the [isEmpty][Collection#isEmpty] method from the [Collection] interface. This method can be broken
    /// using the following collection breaks:
    /// - [IS_EMPTY_ALWAYS_RETURNS_TRUE][BreakableCollection#IS_EMPTY_ALWAYS_RETURNS_TRUE]
    /// - [IS_EMPTY_ALWAYS_RETURNS_FALSE][BreakableCollection#IS_EMPTY_ALWAYS_RETURNS_FALSE]
    /// - [IS_EMPTY_RETURNS_OPPOSITE_VALUE][BreakableCollection#IS_EMPTY_RETURNS_OPPOSITE_VALUE]
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .withBreak(IS_EMPTY_RETURNS_OPPOSITE_VALUE)
    ///         .build();
    /// ```
    /// @return The number of elements in the collection, or a different value if the collection has been broken.
    /// @see Collection#isEmpty()
    @Override
    public boolean isEmpty() {
        if (hasBreak(IS_EMPTY_ALWAYS_RETURNS_TRUE)) {
            return true;
        } else if (hasBreak(IS_EMPTY_ALWAYS_RETURNS_FALSE)) {
            return false;
        } else if (hasBreak(IS_EMPTY_RETURNS_OPPOSITE_VALUE)) {
            return !collection.isEmpty();
        } else{
            return collection.isEmpty();
        }
    }

    /// Implements the [contains][Collection#contains] method from the [Collection] interface. This method can be broken
    ///  using the following collection breaks:
    /// - [CONTAINS_ALWAYS_RETURNS_TRUE][BreakableCollection#CONTAINS_ALWAYS_RETURNS_TRUE]
    /// - [CONTAINS_ALWAYS_RETURNS_FALSE][BreakableCollection#CONTAINS_ALWAYS_RETURNS_FALSE]
    /// - [CONTAINS_RETURNS_OPPOSITE_VALUE][BreakableCollection#CONTAINS_RETURNS_OPPOSITE_VALUE]
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .withBreak(CONTAINS_ALWAYS_RETURNS_FALSE)
    ///         .build();
    /// ```
    /// @return `true` if this collection contains the specified element or false if it does not, or some other value
    ///         if the collection is broken.
    /// @throws NullPointerException if the argument is `null` and collection does not support `null` values.
    /// @throws ClassCastException if the argument is an incompatible type and the collection does not support
    ///         incompatible types.
    /// @see Collection#contains(Object)
    @Override
    public boolean contains(final Object o) {
        checkArgument(o);
        if (hasBreak(CONTAINS_ALWAYS_RETURNS_TRUE)) {
            return true;
        } else if (hasBreak(CONTAINS_ALWAYS_RETURNS_FALSE)) {
            return false;
        } else if (hasBreak(CONTAINS_RETURNS_OPPOSITE_VALUE)) {
            return !collection.contains(o);
        } else{
            return collection.contains(o);
        }
    }

    /// Implements the [toArray][Collection#toArray] method from the [Collection] interface. This method can be broken
    ///  using the following collection breaks:
    /// - [TO_ARRAY_RETURNS_NULL][BreakableCollection#TO_ARRAY_RETURNS_NULL]
    /// - [TO_ARRAY_RETURNS_EMPTY_ARRAY][BreakableCollection#TO_ARRAY_RETURNS_EMPTY_ARRAY]
    /// - [TO_ARRAY_MISSING_FIRST_ELEMENT][BreakableCollection#TO_ARRAY_MISSING_FIRST_ELEMENT]
    /// - [TO_ARRAY_MISSING_LAST_ELEMENT][BreakableCollection#TO_ARRAY_MISSING_LAST_ELEMENT]
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .withBreak(TO_ARRAY_RETURNS_NULL)
    ///         .build();
    /// ```
    /// @return An array with the elements from the collection, or some other value if the collection is broken.
    /// @see Collection#toArray()
    @SuppressWarnings({"DataFlowIssue", "NullableProblems"})
    @Override
    public  Object [] toArray() {
        if (hasBreak(TO_ARRAY_RETURNS_NULL)) {
            return null;
        }
        Object[] array = collection.toArray();
        if (array.length == 0) {
            return array;
        } else if (hasBreak(TO_ARRAY_RETURNS_EMPTY_ARRAY)) {
            return Arrays.copyOf(array, 0);
        } else if (hasBreak(TO_ARRAY_MISSING_FIRST_ELEMENT)) {
            return Arrays.copyOfRange(array, 1, array.length);
        } else if (hasBreak(TO_ARRAY_MISSING_LAST_ELEMENT)) {
            return Arrays.copyOfRange(array, 0, array.length - 1);
        } else {
            return array;
        }
    }

    /// Implements the [toArray][Collection#toArray(Object\[\])] method from the [Collection] interface. This method
    /// can be broken using the following collection breaks:
    /// - [TO_ARRAY_STORE_RETURNS_NULL][BreakableCollection#TO_ARRAY_STORE_RETURNS_NULL]
    /// - [TO_ARRAY_STORE_DOES_NOT_COPY_ELEMENTS][BreakableCollection#TO_ARRAY_STORE_DOES_NOT_COPY_ELEMENTS]
    /// - [TO_ARRAY_STORE_RETURNS_EMPTY_ARRAY][BreakableCollection#TO_ARRAY_STORE_RETURNS_EMPTY_ARRAY]
    /// - [TO_ARRAY_STORE_MISSING_FIRST_ELEMENT][BreakableCollection#TO_ARRAY_STORE_MISSING_FIRST_ELEMENT]
    /// - [TO_ARRAY_STORE_MISSING_LAST_ELEMENT][BreakableCollection#TO_ARRAY_STORE_MISSING_LAST_ELEMENT]
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .withBreak(TO_ARRAY_STORE_RETURNS_NULL)
    ///         .build();
    /// ```
    /// @return An array with the elements from the collection, or some other value if the collection is broken.
    /// @see Collection#toArray(Object[])
    @SuppressWarnings("DataFlowIssue")
    @Override
    public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
        if (hasBreak(TO_ARRAY_STORE_RETURNS_NULL)) {
            return null;
        } else if (hasBreak(TO_ARRAY_STORE_DOES_NOT_COPY_ELEMENTS)) {
            return a;
        }

        T[] array = collection.toArray(a);
        if (hasBreak(TO_ARRAY_STORE_RETURNS_EMPTY_ARRAY)) {
            Arrays.fill(array, null);
        } else if (hasBreak(TO_ARRAY_STORE_MISSING_FIRST_ELEMENT)) {
            for (int i = 1; i < collection.size(); i++) {
                array[i-1] = array[i];
            }
            array[collection.size() - 1] = null;
        } else if (hasBreak(TO_ARRAY_STORE_MISSING_LAST_ELEMENT)) {
            array[collection.size() - 1] = null;
        }
        return array;
    }

    /// Implements the [add][Collection#add] method from the [Collection] interface. This method can be broken using
    /// the following collection breaks:
    /// - [ADD_DOES_NOT_ADD_ELEMENT][BreakableCollection#ADD_DOES_NOT_ADD_ELEMENT]
    /// - [ADD_ALWAYS_RETURNS_TRUE][BreakableCollection#ADD_ALWAYS_RETURNS_TRUE]
    /// - [ADD_ALWAYS_RETURNS_FALSE][BreakableCollection#ADD_ALWAYS_RETURNS_FALSE]
    /// - [ADD_ALWAYS_RETURNS_OPPOSITE_VALUE][BreakableCollection#ADD_ALWAYS_RETURNS_OPPOSITE_VALUE]
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .withBreak(ADD_DOES_NOT_ADD_ELEMENT)
    ///         .build();
    /// ```
    /// This is an optional method of the [Collection] interface. Support for this method can be removed using the
    /// optional method identifier [CollectionMethods#Add]. A collection that does not support the `add` method
    /// can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .doesNotSupportsMethod(OptionalCollectionMethod.Add)
    ///         .build();
    /// ```
    /// @return 'true' if the element is added, 'false' if it isn't, or possibly a different value if the collection has
    ///          been broken.
    /// @throws NullPointerException if the argument is `null` and collection does not support `null` values.
    /// @throws IllegalArgumentException if the argument is already contained in the collections, and it does not support
    ///         duplicate values.
    /// @throws ClassCastException if the argument is an incompatible type and the collection does not support
    ///         incompatible types.
    /// @throws UnsupportedOperationException if this collection does not support this method.
    @Override
    public boolean add(E e) {
        if (supportsMethod(CollectionMethods.Add)) {
            boolean result = false;
            if (checkNewElement(e)) {
                if (!hasBreak(ADD_DOES_NOT_ADD_ELEMENT)) {
                    result = collection.add(e);
                }
            }
            if (hasBreak(ADD_ALWAYS_RETURNS_TRUE)) {
                return true;
            } else if (hasBreak(ADD_ALWAYS_RETURNS_FALSE)) {
                return false;
            } else if (hasBreak(ADD_ALWAYS_RETURNS_OPPOSITE_VALUE)) {
                return !result;
            } else {
                return result;
            }
        } else {
            throw new UnsupportedOperationException("Unsupported method: add");
        }
    }

    /// Implements the [remove][Collection#remove] method from the [Collection] interface. This method can be broken using
    /// the following collection breaks:
    /// - [REMOVE_DOES_NOT_REMOVE_ELEMENT][BreakableCollection#REMOVE_DOES_NOT_REMOVE_ELEMENT]
    /// - [REMOVE_ALWAYS_RETURNS_TRUE][BreakableCollection#REMOVE_ALWAYS_RETURNS_TRUE]
    /// - [REMOVE_ALWAYS_RETURNS_FALSE][BreakableCollection#REMOVE_ALWAYS_RETURNS_FALSE]
    /// - [REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE][BreakableCollection#REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE]
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = BreakableCollection.of(1,2,3,4,5)
    ///         .withBreak(REMOVE_DOES_NOT_REMOVE_ELEMENT)
    ///         .build();
    /// ```
    /// This is an optional method of the [Collection] interface. Support for this method can be removed using the
    /// optional method identifier [CollectionMethods#Remove]. A collection that does not support the `remove`
    /// method can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = BreakableCollection.of(1,2,3,4,5)
    ///         .doesNotSupportsMethod(OptionalCollectionMethod.Remove)
    ///         .build();
    /// ```
    /// @return 'true' if the element is removed, 'false' if it isn't, or possibly a different value if the collection
    ///          has been broken.
    /// @throws NullPointerException if the argument is `null` and collection does not support `null` values.
    /// @throws ClassCastException if the argument is an incompatible type and the collection does not support
    ///         incompatible types.
    @Override
    public boolean remove(Object o) {
        if (supportsMethod(CollectionMethods.Add)) {
            checkArgument(o);
            boolean result = false;
            if (!hasBreak(REMOVE_DOES_NOT_REMOVE_ELEMENT)) {
                result = collection.remove(o);
            }
            if (hasBreak(REMOVE_ALWAYS_RETURNS_TRUE)) {
                return true;
            } else if (hasBreak(REMOVE_ALWAYS_RETURNS_FALSE)) {
                return false;
            } else if (hasBreak(REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE)) {
                return !result;
            } else {
                return result;
            }
        } else {
            throw new UnsupportedOperationException("Unsupported method: remove");
        }
    }

    /// Implements the [containsAll][Collection#containsAll] method from the [Collection] interface. This method can b
    ///  broken using the following collection breaks:
    /// - [CONTAINS_ALL_ALWAYS_RETURNS_TRUE][BreakableCollection#CONTAINS_ALL_ALWAYS_RETURNS_TRUE]
    /// - [CONTAINS_ALL_ALWAYS_RETURNS_FALSE][BreakableCollection#CONTAINS_ALL_ALWAYS_RETURNS_FALSE]
    /// - [CONTAINS_ALL_RETURNS_OPPOSITE_VALUE][BreakableCollection#CONTAINS_ALL_RETURNS_OPPOSITE_VALUE]
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .withBreak(CONTAINS_ALL_ALWAYS_RETURNS_FALSE)
    ///         .build();
    /// ```
    /// @return `true` if this collection contains the specified elements or false if it does not, or some other value
    ///         if the collection is broken.
    /// @throws NullPointerException if the argument contains a `null` and collection does not support `null` values.
    /// @throws ClassCastException if the argument is contains an incompatible type and the collection does not support
    ///         incompatible types.
    /// @see Collection#containsAll(Collection)
    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        if (supportsMethod(CollectionMethods.Add)) {
            c.forEach(this::checkArgument);
            if (hasBreak(CONTAINS_ALL_ALWAYS_RETURNS_TRUE)) {
                return true;
            } else if (hasBreak(CONTAINS_ALL_ALWAYS_RETURNS_FALSE)) {
                return false;
            } else if (hasBreak(CONTAINS_ALL_RETURNS_OPPOSITE_VALUE)) {
                return !collection.containsAll(c);
            } else {
                return collection.containsAll(c);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /// Implements the [addAll][Collection#addAll] method from the [Collection] interface. This method can be broken 
    /// using the following collection breaks:
    /// - [ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS][BreakableCollection#ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS]
    /// - [ADD_ALL_SKIPS_FIRST_ELEMENT][BreakableCollection#ADD_ALL_SKIPS_FIRST_ELEMENT]
    /// - [ADD_ALL_SKIPS_LAST_ELEMENT][BreakableCollection#ADD_ALL_SKIPS_LAST_ELEMENT]
    /// - [ADD_ALL_ALWAYS_RETURNS_TRUE][BreakableCollection#ADD_ALL_ALWAYS_RETURNS_TRUE]
    /// - [ADD_ALL_ALWAYS_RETURNS_FALSE][BreakableCollection#ADD_ALL_ALWAYS_RETURNS_FALSE]
    /// - [ADD_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE][BreakableCollection#ADD_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE]
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .withBreak(ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS)
    ///         .build();
    /// ```
    /// This is an optional method of the [Collection] interface. Support for this method can be removed using the
    /// optional method identifier [CollectionMethods#AddAll]. A collection that does not support the `addAll` method
    /// can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .doesNotSupportsMethod(OptionalCollectionMethod.AddAll)
    ///         .build();
    /// ```
    /// @return 'true' if all of the elements are added, 'false' otherwise, or possibly a different value if the
    ///          collection has been broken.
    /// @throws NullPointerException if the argument contains any `null` values and collection does not support `null`
    ///                              values.
    /// @throws ClassCastException if the argument contains any elements an incompatible type and the collection does
    ///                            not support incompatible types.
    /// @throws UnsupportedOperationException if this collection does not support this method.
    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        if (supportsMethod(CollectionMethods.AddAll)) {
            boolean result = false;
            if (c.stream().allMatch(this::checkNewElement)) {
                List<E> l = new ArrayList<>(c);
                if (hasBreak(ADD_ALL_SKIPS_FIRST_ELEMENT)) {
                    result = collection.addAll(l.subList(1, l.size()));
                } else if (hasBreak(ADD_ALL_SKIPS_LAST_ELEMENT)) {
                    result = collection.addAll(l.subList(0, l.size() - 1));
                } else if (!hasBreak(ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS)) {
                    result = collection.addAll(l);
                }
            }

            if (hasBreak(ADD_ALL_ALWAYS_RETURNS_TRUE)) {
                return true;
            } else if (hasBreak(ADD_ALL_ALWAYS_RETURNS_FALSE)) {
                return false;
            } else if (hasBreak(ADD_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE)) {
                return !result;
            } else {
                return result;
            }
        } else {
            throw new UnsupportedOperationException("Unsupported method: add");
        }
    }

    /// Implements the [removeAll][Collection#removeAll] method from the [Collection] interface. This method can be broken
    /// using the following collection breaks:
    /// - [REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS][BreakableCollection#REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS]
    /// - [REMOVE_ALL_SKIPS_FIRST_ELEMENT][BreakableCollection#REMOVE_ALL_SKIPS_FIRST_ELEMENT]
    /// - [REMOVE_ALL_SKIPS_LAST_ELEMENT][BreakableCollection#REMOVE_ALL_SKIPS_LAST_ELEMENT]
    /// - [REMOVE_ALL_ALWAYS_RETURNS_TRUE][BreakableCollection#REMOVE_ALL_ALWAYS_RETURNS_TRUE]
    /// - [REMOVE_ALL_ALWAYS_RETURNS_FALSE][BreakableCollection#REMOVE_ALL_ALWAYS_RETURNS_FALSE]
    /// - [REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE][BreakableCollection#REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE]
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .withBreak(REMOVE_ALL_DOES_NOT_ADD_ANY_ELEMENTS)
    ///         .build();
    /// ```
    /// This is an optional method of the [Collection] interface. Support for this method can be removed using the
    /// optional method identifier [CollectionMethods#RemoveAll]. A collection that does not support the `removeAll` method
    /// can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .doesNotSupportsMethod(OptionalCollectionMethod.RemoveAll)
    ///         .build();
    /// ```
    /// @return 'true' if all of the elements are removed, 'false' otherwise, or possibly a different value if the
    ///          collection has been broken.
    /// @throws NullPointerException if the argument contains any `null` values and collection does not support `null` values.
    /// @throws ClassCastException if the argument contains any elements an incompatible type and the collection does
    ///         not support incompatible types.
    /// @throws UnsupportedOperationException if this collection does not support this method.
    @Override @Contract(mutates="this")
    public boolean removeAll(@NotNull Collection<?> c) {
        if (supportsMethod(CollectionMethods.RemoveAll)) {
            c.forEach(this::checkArgument);
            boolean result = false;
            List<?> l = new ArrayList<>(c);
            if (hasBreak(REMOVE_ALL_SKIPS_FIRST_ELEMENT)) {
                result = collection.removeAll(l.subList(1, l.size()));
            } else if (hasBreak(REMOVE_ALL_SKIPS_LAST_ELEMENT)) {
                result = collection.removeAll(l.subList(0, l.size()-1));
            } else if (!hasBreak(REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS)) {
                result = collection.removeAll(l);
            }
            if (hasBreak(REMOVE_ALL_ALWAYS_RETURNS_TRUE)) {
                return true;
            } else if (hasBreak(REMOVE_ALL_ALWAYS_RETURNS_FALSE)) {
                return false;
            } else if (hasBreak(REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE)) {
                return !result;
            } else {
                return result;
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /// Implements the [removeIf][Collection#removeIf] method from the [Collection] interface. This method can be broken
    /// using the following collection breaks:
    /// - [REMOVE_IF_DOES_NOT_REMOVE_ANY_ELEMENTS][BreakableCollection#REMOVE_IF_DOES_NOT_REMOVE_ANY_ELEMENTS]
    /// - [REMOVE_IF_SKIPS_FIRST_ELEMENT][BreakableCollection#REMOVE_IF_SKIPS_FIRST_ELEMENT]
    /// - [REMOVE_IF_SKIPS_LAST_ELEMENT][BreakableCollection#REMOVE_IF_SKIPS_LAST_ELEMENT]
    /// - [REMOVE_IF_ALWAYS_RETURNS_TRUE][BreakableCollection#REMOVE_IF_ALWAYS_RETURNS_TRUE]
    /// - [REMOVE_IF_ALWAYS_RETURNS_FALSE][BreakableCollection#REMOVE_IF_ALWAYS_RETURNS_FALSE]
    /// - [REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE][BreakableCollection#REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE]
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .withBreak(REMOVE_IF_DOES_NOT_REMOVE_ANY_ELEMENTS)
    ///         .build();
    /// ```
    /// This is an optional method of the [Collection] interface. Support for this method can be removed using the
    /// optional method identifier [CollectionMethods#RemoveIf]. A collection that does not support the `removeIf` method
    /// can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .doesNotSupportsMethod(OptionalCollectionMethod.RemoveIf)
    ///         .build();
    /// ```
    /// @param filter a predicate which returns `true` for elements to be removed
    /// @return `true` if any elements were removed
    /// @throws NullPointerException if the specified filter is null
    /// @throws UnsupportedOperationException if the `removeIf` operation is not supported by this collection
    @Override @Contract(mutates="this")
    public boolean removeIf(Predicate<? super E> filter) {
        if (supportsMethod(CollectionMethods.RemoveAll)) {
            boolean changed = false;
            if (hasBreak(REMOVE_IF_SKIPS_FIRST_ELEMENT)) {
                Iterator<E> i = IterableTestOps.skipFirstIterator(collection);
                while (i.hasNext()) {
                    if (filter.test(i.next())) {
                        i.remove();
                    }
                }
            } else if (hasBreak(REMOVE_IF_SKIPS_LAST_ELEMENT)) {
                Iterator<E> i = IterableTestOps.skipLastIterator(collection);
                while(i.hasNext()) {
                    if (filter.test(i.next())) {
                        i.remove();
                    }
                }
            } else if (!hasBreak(REMOVE_IF_DOES_NOT_REMOVE_ANY_ELEMENTS)) {
                changed = Collection.super.removeIf(filter);
            }

            if (hasBreak(REMOVE_IF_ALWAYS_RETURNS_TRUE)) {
                return true;
            } else if (hasBreak(REMOVE_IF_ALWAYS_RETURNS_FALSE)) {
                return false;
            } else if (hasBreak(REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE)) {
                return !changed;
            } else {
                return changed;
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /// Implements the [retainAll][Collection#retainAll] method from the [Collection] interface. This method can be broken
    /// using the following collection breaks:
    /// - [RETAIN_ALL_DOES_NOT_RETAIN_ANY_ELEMENTS][BreakableCollection#RETAIN_ALL_DOES_NOT_RETAIN_ANY_ELEMENTS]
    /// - [RETAIN_ALL_SKIPS_FIRST_ELEMENT][BreakableCollection#RETAIN_ALL_SKIPS_FIRST_ELEMENT]
    /// - [RETAIN_ALL_SKIPS_LAST_ELEMENT][BreakableCollection#RETAIN_ALL_SKIPS_LAST_ELEMENT]
    /// - [RETAIN_ALL_ALWAYS_RETURNS_TRUE][BreakableCollection#RETAIN_ALL_ALWAYS_RETURNS_TRUE]
    /// - [RETAIN_ALL_ALWAYS_RETURNS_FALSE][BreakableCollection#RETAIN_ALL_ALWAYS_RETURNS_FALSE]
    /// - [RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE][BreakableCollection#RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE]
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .withBreak(RETAIN_ALL_DOES_NOT_ADD_ANY_ELEMENTS)
    ///         .build();
    /// ```
    /// This is an optional method of the [Collection] interface. Support for this method can be removed using the
    /// optional method identifier [CollectionMethods#RetainAll]. A collection that does not support the `retainAll`
    /// method can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .doesNotSupportsMethod(OptionalCollectionMethod.RetainAll)
    ///         .build();
    /// ```
    /// @return 'true' if the collection is modified, 'false' otherwise, or possibly a different value if the
    ///          collection has been broken.
    /// @throws NullPointerException if the argument contains any `null` values and collection does not support `null` values.
    /// @throws ClassCastException if the argument contains any elements an incompatible type and the collection does
    ///         not support incompatible types.
    /// @throws UnsupportedOperationException if this collection does not support this method.
    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        if (supportsMethod(CollectionMethods.RetainAll)) {
            c.forEach(this::checkArgument);
            boolean result = false;
            List<?> l = new ArrayList<>(c);
            if (hasBreak(RETAIN_ALL_SKIPS_FIRST_ELEMENT)) {
                result = collection.retainAll(l.subList(1, l.size()));
            } else if (hasBreak(RETAIN_ALL_SKIPS_LAST_ELEMENT)) {
                result = collection.retainAll(l.subList(0, l.size()-1));
            } else if (!hasBreak(RETAIN_ALL_DOES_NOT_RETAIN_ANY_ELEMENTS)) {
                result = collection.retainAll(l);
            }
            if (hasBreak(RETAIN_ALL_ALWAYS_RETURNS_TRUE)) {
                return true;
            } else if (hasBreak(RETAIN_ALL_ALWAYS_RETURNS_FALSE)) {
                return false;
            } else if (hasBreak(RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE)) {
                return !result;
            } else {
                return result;
            }
        } else {
            throw new UnsupportedOperationException("Unsupported method: addAll");
        }
    }

    /// Implements the [clear][Collection#clear] method from the [Collection] interface. This method can be broken using
    /// the following collection breaks:
    /// - [CLEAR_DOES_NOT_REMOVE_ANY_ELEMENTS][BreakableCollection#CLEAR_DOES_NOT_REMOVE_ANY_ELEMENTS]
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .withBreak(CLEAR_DOES_NOT_REMOVE_ANY_ELEMENTS)
    ///         .build();
    /// ```
    /// @see Collection#clear()
    @Override
    public void clear() {
        if (supportsMethod(CollectionMethods.Clear)) {
            if (hasBreak(CLEAR_SKIPS_FIRST_ELEMENT)) {
                Iterator<E> i = IterableTestOps.skipFirstIterator(collection);
                while(i.hasNext()) {
                    i.next();
                    i.remove();
                }
            } else if (hasBreak(CLEAR_SKIPS_LAST_ELEMENT)) {
                Iterator<E> i = IterableTestOps.skipLastIterator(collection);
                while(i.hasNext()) {
                    i.next();
                    i.remove();
                }
            } else if (!hasBreak(CLEAR_DOES_NOT_REMOVE_ANY_ELEMENTS)) {
                collection.clear();
            }
        } else {
            throw new UnsupportedOperationException("Unsupported method: addAll");
        }
    }

    /// returns the elements as an unbroken instance of `Collection'
    /// @return an unbroken collection.
    public @NotNull Collection<E> unbroken() {
        return collection;
    }

    /// Checks that the argument is valid for this collection. This will check that:
    ///  - The argument is not null or the collection permits nulls
    ///  - The argument is a compatible type or the collection permits incompatible types.
    /// @param arg the argument to check.
    /// @throws NullPointerException if the argument is 'null' and the collection does not permit nulls.
    /// @throws ClassCastException if the argument is not compatible and the collection does not permit incompatible types.
    protected void checkArgument(final Object arg) {
        if (!permitsNulls && arg == null) {
            throw new NullPointerException();
        }
        if (!permitsIncompatibleTypes && compatibleType.isAssignableFrom(arg.getClass())) {
            throw new ClassCastException("incompatible type: " + arg.getClass().getName());
        }
    }

    /// Checks that the element is valid to add to this collection. This will check that the element is not a duplicate,
    /// or the collection permits duplicate values.
    /// @param e the element to check.
    /// @throws NullPointerException if the argument is 'null' and the collection does not permit nulls.
    /// @throws ClassCastException if the argument is not compatible and the collection does not permit incompatible types.
    protected boolean checkNewElement(final E e) {
        checkArgument(e);
        return permitsDuplicates || !collection.contains(e);
    }

    /// Utility class for implementing builders for subclasses of BreakableCollection.
    /// @param <B> the builder type
    /// @param <C> the breakable collection type
    /// @param <E> the element type
    /// @author evanbergstrom
    /// @since 1.0
    public static abstract class AbstractBuilder<B extends AbstractBuilder<B, C, E>, C extends BreakableCollection<E>, E>
            extends BreakableIterable.AbstractBuilder<B, C, E>{

        protected boolean permitsNulls;
        protected boolean permitsDuplicates;
        protected boolean permitsIncompatibleTypes;
        protected final Collection<OptionalMethod> unsupportedMethods;

        /// Default constructor to be called by default constructors for subclasses.
        protected AbstractBuilder() {
            this(new ArrayList<>());
        }

        protected AbstractBuilder(final @NotNull Collection<E> elements) {
            super(elements);
            this.unsupportedMethods = new ArrayList<>();
            this.permitsNulls = true;
            this.permitsDuplicates = true;
            this.permitsIncompatibleTypes = true;
        }

            // Copy constructor to be called by copy constructors for subclasses.
        /// @param other the builder to copy.
        /// @throws NullPointerException if the argument is `null`.
        protected AbstractBuilder(final AbstractBuilder<B,C, E> other) {
            super(other);
            this.unsupportedMethods = new ArrayList<>(other.unsupportedMethods);
            this.permitsNulls = other.permitsNulls;
            this.permitsDuplicates = other.permitsDuplicates;
            this.permitsIncompatibleTypes = other.permitsIncompatibleTypes;
        }

        /// Sets the builder to construct a collection that does not permit nulls. By default, the collection will
        /// support `null`.
        /// @return th builder.
        public B doesNotPermitNulls() {
            permitsNulls = false;
            return self();
        }

        /// Sets the builder to construct a collection that does not permit duplicate elements. By default, the
        /// collection will support duplicate elements.
        /// @return th builder.
        public B doesNotPermitDuplicates() {
            permitsDuplicates = false;
            return self();
        }

        /// Sets the builder to construct a collection that does not permit incompatible types. By default, the
        /// collection will support incompatible types.
        /// @return th builder.
        public B doesNotPermitIncompatibleTypes() {
            permitsIncompatibleTypes = false;
            return self();
        }

        /// Sets the builder to construct a collection that does not support the method provided as the argument.
        /// @param method the method that the collection does not support.
        /// @return the builder.
        public final B doesNotSupportMethod(final OptionalMethod method) {
            unsupportedMethods.add(method);
            return self();
        }
    }

    /// The builder for BreakableCollection objects.
    /// @param <E> the element type.
    public static class Builder<E> extends AbstractBuilder<Builder<E>,BreakableCollection<E>, E> {

        /// Create a builder initialized with the default values.
        public Builder() {
            super();
        }

        /// Create a builder initialized with the values copied from another builder.
        /// @param other the builder to copy the values from.
        public Builder(final Builder<E> other) {
            super(other);
        }

        @Override
        public Builder<E> self() {
            return this;
        }

        @Override
        public Builder<E> copy() {
            return new Builder<>(this);
        }

        /// Build a BreakableCollection objects using the values from the builder.
        /// @return a new BreakableCollection object.
        public BreakableCollection<E> build() {
            BreakableCollection<E> broken = new BreakableCollection<>(elements, breaks, characteristics);
            broken.setPermitsNulls(permitsNulls);
            broken.setPermitsDuplicates(permitsDuplicates);
            broken.setPermitsIncompatibleTypes(permitsIncompatibleTypes);
            unsupportedMethods.forEach(broken::doesNotSupportMethod);
            return broken;
        }
    }

    /// Creates a collection provider for instances of BreakableCollection, given an element provider.
    /// @param <E> the element type.
    /// @param elementProvider the element provider to use.
    /// @return a collection provider for breakable collections.
    public static <E> @NotNull CollectionProvider<E, BreakableCollection<E>> collectionProvider(
            final @NotNull ObjectProvider<E> elementProvider) {
        return CollectionProviders.from(
                BreakableCollection::new,
                BreakableCollection::new,
                (c) -> new BreakableCollection<>(new ArrayList<>(c)),
                elementProvider
        );
    }

    /// Creates a collection provider for instances of `BreakableCollection`, given an element provider and a set of breaks.
    /// @param <E> the element type.
    /// @param elementProvider the element provider to use.
    /// @param breaks the breaks to apply to each instance of `BreakableCollection`.
    /// @return a collection provider for breakable collections.
    public static <E> @NotNull CollectionProvider<E, BreakableCollection<E>> collectionProvider(
            final @NotNull ObjectProvider<E> elementProvider,
            final @NotNull Set<Break> breaks) {
        return CollectionProviders.from(
                () -> new BreakableCollection<>(new ArrayList<>(), breaks, 0),
                (o) -> new BreakableCollection<>(new ArrayList<>(o.collection), breaks, 0),
                (c) -> new BreakableCollection<>(new ArrayList<>(c), breaks, 0),
                elementProvider
        );
    }

    /// Mixin interface that adds an implementation of the `provider()` method that provides instances of
    /// `BreakableCollection` that do not have any breaks applied.
    /// @param <E> element type
    public interface WithProvider<E> extends CollectionProviderSupport<E, BreakableCollection<E>> {
        @Override
        default @NotNull CollectionProvider<E, BreakableCollection<E>> provider() {
            return BreakableCollection.collectionProvider(elementProvider());
        }
    }
}
