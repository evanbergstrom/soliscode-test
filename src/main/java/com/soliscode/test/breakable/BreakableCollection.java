/*
 * Copyright 2025 Evan Bergstrom
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
package com.soliscode.test.breakable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.MethodStatus;
import com.soliscode.test.contract.collection.CollectionMethods;
import com.soliscode.test.contract.support.CollectionProviderSupport;
import com.soliscode.test.provider.CollectionProvider;
import com.soliscode.test.provider.ObjectProvider;
import com.soliscode.test.util.IterableTestUtils;

/// A [Collection] for testing, which can be broken on purpose through [Break] constants.
///
/// With no break set, it keeps the contract of [Collection] (by default it is backed by an [ArrayList]). Each
/// break constant below changes one method's behaviour as its description says, so that a contract or other
/// test code can be shown to fail against a non-compliant collection. The breaks fall into these groups:
/// - **Element modification**: `ADD_*` and `REMOVE_*` (elements not added or removed, wrong return values,
///   the wrong exception for a `null` or incompatible element).
/// - **Bulk operations**: `ADD_ALL_*`, `REMOVE_ALL_*`, `RETAIN_ALL_*` and `REMOVE_IF_*` (no elements, or the
///   first or last skipped, wrong return values, no exception for a `null` argument).
/// - **Queries**: `CONTAINS_*`, `CONTAINS_ALL_*`, `SIZE_*` and `IS_EMPTY_*` (wrong or inverted results).
/// - **Array conversion**: `TO_ARRAY_*` and `TO_ARRAY_STORE_*` (`null`, empty or incomplete arrays).
/// - **Clearing**: `CLEAR_*` (no elements, or the first or last left behind).
/// - **Thread safety and optional methods**: `*_IS_NOT_THREAD_SAFE` and `*_THROWS_WRONG_UNSUPPORTED_EXCEPTION`.
///
/// It extends [BreakableIterable], so the iterator and spliterator breaks apply too. The builder can also
/// remove support for an optional method (see [CollectionMethods]), and make the collection reject `null`
/// elements, duplicates or incompatible types.
///
/// ```java
/// BreakableCollection<String> collection = new BreakableCollection.Builder<String>()
///     .addBreak(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT)
///     .addBreak(BreakableCollection.ADD_ALWAYS_RETURNS_TRUE)
///     .build();
///
/// assertTrue(collection.add("test")); // returns true
/// assertTrue(collection.isEmpty());   // but the element was not added
/// ```
///
/// @param <E> the type of elements maintained by this collection
/// @author evanbergstrom
/// @since 1.0.0
/// @see CollectionMethods
/// @see BreakableIterable
/// @see java.util.Collection
public class BreakableCollection<E> extends BreakableIterable<E> implements Collection<E> {


    /// #### ADD_DOES_NOT_ADD_ELEMENT
    /// **Purpose**: Forces `add()` method to accept elements but not actually add them to the collection
    /// **Effect**: Method returns normally but collection remains unchanged
    /// **Affected Methods:** [add(Object)][#add(Object)]
    /// @see BreakableCollection#add(Object)
    public static final Break ADD_DOES_NOT_ADD_ELEMENT =
            new Break("ADD_DOES_NOT_ADD_ELEMENT");

    /// #### ADD_ALWAYS_RETURNS_TRUE
    /// **Purpose**: Forces `add()` method to always return a result of `true`, even if the element is not added.
    /// **Effect**: Method returns `true` regardless of whether the collection was modified.
    /// **Affected Methods:** [add(Object)][#add(Object)]
    /// @see BreakableCollection#add(Object)
    public static final Break ADD_ALWAYS_RETURNS_TRUE =
            new Break("ADD_ALWAYS_RETURNS_TRUE");

    /// #### ADD_ALWAYS_RETURNS_FALSE
    /// **Purpose**: Forces `add()` method to always return a result of `false`, even if the element is added.
    /// **Effect**: Method returns `false` regardless of whether the collection was modified.
    /// **Affected Methods:** [add(Object)][#add(Object)]
    /// @see BreakableCollection#add(Object)
    public static final Break ADD_ALWAYS_RETURNS_FALSE =
            new Break("ADD_ALWAYS_RETURNS_FALSE");

    /// #### ADD_ALWAYS_RETURNS_OPPOSITE_VALUE
    /// **Purpose**: Forces `add()` method to always return the opposite of the appropriate result.
    /// **Effect**: Method returns `true` if not modified, and `false` if modified.
    /// **Affected Methods:** [add(Object)][#add(Object)]
    /// @see BreakableCollection#add(Object)
    public static final Break ADD_ALWAYS_RETURNS_OPPOSITE_VALUE =
            new Break("ADD_ALWAYS_RETURNS_OPPOSITE_VALUE");

    /// #### ADD_THROWS_WRONG_NULL_EXCEPTION
    /// **Purpose**: Forces `add()` method to throw the wrong exception when
    /// it is provided a null value and the collection does not support null values..
    /// **Effect**: Throws `RuntimeException` instead of `NullPointerException`.
    /// **Affected Methods:** [add(Object)][#add(Object)]
    /// @see BreakableCollection#add(Object)
    public static final Break ADD_THROWS_WRONG_NULL_EXCEPTION =
            new Break("ADD_THROWS_WRONG_NULL_EXCEPTION");

    /// #### ADD_THROWS_WRONG_INCOMPATIBLE_TYPE_EXCEPTION
    /// **Purpose**: Forces `add()` method to throw the wrong exception when
    /// it is provided a value with an incompatible type and the collection does not support incompatible types.
    /// **Effect**: Throws `RuntimeException` instead of `ClassCastException`.
    /// **Affected Methods:** [add(Object)][#add(Object)]
    /// @see BreakableCollection#add(Object)
    public static final Break ADD_THROWS_WRONG_INCOMPATIBLE_TYPE_EXCEPTION =
            new Break("ADD_THROWS_WRONG_INCOMPATIBLE_TYPE_EXCEPTION");

    /// #### ADD_THROWS_WRONG_UNSUPPORTED_EXCEPTION
    /// **Purpose**: Forces `add()` method to throw the wrong exception when
    /// it is not supported.
    /// **Effect**: Throws `RuntimeException` instead of `UnsupportedOperationException`.
    /// **Affected Methods:** [add(Object)][#add(Object)]
    /// @see BreakableCollection#add(Object)
    public static final Break ADD_THROWS_WRONG_UNSUPPORTED_EXCEPTION =
            new Break("ADD_THROWS_WRONG_UNSUPPORTED_EXCEPTION");

    /// #### ADD_IS_NOT_THREAD_SAFE
    /// **Purpose**: Forces `add()` method to be not thread safe.
    /// **Effect**: Disables synchronization for the `add` operation.
    /// **Affected Methods:** [add(Object)][#add(Object)]
    /// @see BreakableCollection#add(Object)
    public static final Break ADD_IS_NOT_THREAD_SAFE =
            new Break("ADD_IS_NOT_THREAD_SAFE");

    /// #### ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS
    /// **Purpose**: Forces `addAll()` method to not add any elements to the collection.
    /// **Effect**: Method accepts the elements but the collection remains unchanged.
    /// **Affected Methods:** [addAll(Collection)][#addAll(Collection)]
    /// @see BreakableCollection#addAll(Collection)
    public static final Break ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS =
            new Break("ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS");

    /// #### ADD_ALL_ALWAYS_RETURNS_TRUE
    /// **Purpose**: Forces `addAll()` method to always return a result of `true`, even if the element is not added.
    /// **Effect**: Method returns `true` regardless of whether the collection was modified.
    /// **Affected Methods:** [addAll(Collection)][#addAll(Collection)]
    /// @see BreakableCollection#addAll(Collection)
    public static final Break ADD_ALL_ALWAYS_RETURNS_TRUE =
            new Break("ADD_ALL_ALWAYS_RETURNS_TRUE");

    /// #### ADD_ALL_ALWAYS_RETURNS_FALSE
    /// **Purpose**: Forces `addAll()` method to always return a result of `false`, even if the element is added.
    /// **Effect**: Method returns `false` regardless of whether the collection was modified.
    /// **Affected Methods:** [addAll(Collection)][#addAll(Collection)]
    /// @see BreakableCollection#addAll(Collection)
    public static final Break ADD_ALL_ALWAYS_RETURNS_FALSE =
            new Break("ADD_ALL_ALWAYS_RETURNS_FALSE");

    /// #### ADD_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE
    /// **Purpose**: Forces `addAll()` method to always return the opposite of the appropriate result.
    /// **Effect**: Method returns `true` if not modified, and `false` if modified.
    /// **Affected Methods:** [addAll(Collection)][#addAll(Collection)]
    /// @see BreakableCollection#addAll(Collection)
    public static final Break ADD_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE =
            new Break("ADD_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE");

    /// #### ADD_ALL_SKIPS_FIRST_ELEMENT
    /// **Purpose**: Forces `addAll()` method to skip the first element to be added.
    /// **Effect**: The first element in the input collection is not added.
    /// **Affected Methods:** [addAll(Collection)][#addAll(Collection)]
    /// @see BreakableCollection#addAll(Collection)
    public static final Break ADD_ALL_SKIPS_FIRST_ELEMENT =
            new Break("ADD_ALL_SKIPS_FIRST_ELEMENT");

    /// #### ADD_ALL_SKIPS_LAST_ELEMENT
    /// **Purpose**: Forces `addAll()` method to skip the last element to be added.
    /// **Effect**: The last element in the input collection is not added.
    /// **Affected Methods:** [addAll(Collection)][#addAll(Collection)]
    /// @see BreakableCollection#addAll(Collection)
    public static final Break ADD_ALL_SKIPS_LAST_ELEMENT =
            new Break("ADD_ALL_SKIPS_LAST_ELEMENT");

    /// #### ADD_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION
    /// **Purpose**: Forces `addAll()` method to throw the wrong exception when it is not supported.
    /// **Effect**: Throws `RuntimeException` instead of `UnsupportedOperationException`.
    /// **Affected Methods:** [addAll(Collection)][#addAll(Collection)]
    /// @see BreakableCollection#addAll(Collection)
    public static final Break ADD_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION =
            new Break("ADD_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION");

    /// #### ADD_ALL_IS_NOT_THREAD_SAFE
    /// **Purpose**: Forces `addAll()` method to be not thread safe.
    /// **Effect**: Disables synchronization for the `addAll` operation.
    /// **Affected Methods:** [addAll(Collection)][#addAll(Collection)]
    /// @see BreakableCollection#addAll(Collection)
    public static final Break ADD_ALL_IS_NOT_THREAD_SAFE =
            new Break("ADD_ALL_IS_NOT_THREAD_SAFE");

    /// #### CLEAR_DOES_NOT_REMOVE_ANY_ELEMENTS
    /// **Purpose**: Forces `clear()` method to not remove any elements.
    /// **Effect**: Method completes normally but the collection remains unchanged.
    /// **Affected Methods:** [clear()][#clear()]
    /// @see BreakableCollection#clear()
    public static final Break CLEAR_DOES_NOT_REMOVE_ANY_ELEMENTS =
            new Break("CLEAR_DOES_NOT_REMOVE_ANY_ELEMENTS");

    /// #### CLEAR_SKIPS_FIRST_ELEMENT
    /// **Purpose**: Forces `clear()` method to skip the first element.
    /// **Effect**: All elements except the first one are removed.
    /// **Affected Methods:** [clear()][#clear()]
    /// @see BreakableCollection#clear()
    public static final Break CLEAR_SKIPS_FIRST_ELEMENT =
            new Break("CLEAR_SKIPS_FIRST_ELEMENT");

    /// #### CLEAR_SKIPS_LAST_ELEMENT
    /// **Purpose**: Forces `clear()` method to skip the last element.
    /// **Effect**: All elements except the last one are removed.
    /// **Affected Methods:** [clear()][#clear()]
    /// @see BreakableCollection#clear()
    public static final Break CLEAR_SKIPS_LAST_ELEMENT =
            new Break("CLEAR_SKIPS_LAST_ELEMENT");

    /// #### CLEAR_THROWS_WRONG_UNSUPPORTED_EXCEPTION
    /// **Purpose**: Forces `clear()` method to throw the wrong exception when it is not supported.
    /// **Effect**: Throws `RuntimeException` instead of `UnsupportedOperationException`.
    /// **Affected Methods:** [clear()][#clear()]
    /// @see BreakableCollection#clear()
    public static final Break CLEAR_THROWS_WRONG_UNSUPPORTED_EXCEPTION =
            new Break("CLEAR_THROWS_WRONG_UNSUPPORTED_EXCEPTION");

    /// #### CLEAR_IS_NOT_THREAD_SAFE
    /// **Purpose**: Forces `clear()` method to be not thread safe.
    /// **Effect**: Disables synchronization for the `clear` operation.
    /// **Affected Methods:** [clear()][#clear()]
    /// @see BreakableCollection#clear()
    public static final Break CLEAR_IS_NOT_THREAD_SAFE =
            new Break("CLEAR_IS_NOT_THREAD_SAFE");

    /// #### CONTAINS_ALWAYS_RETURNS_TRUE
    /// **Purpose**: Forces `contains()` method to always return a `true`.
    /// **Effect**: Method returns `true` regardless of whether the element is present.
    /// **Affected Methods:** [contains(Object)][#contains(Object)]
    /// @see BreakableCollection#contains(Object)
    public static final Break CONTAINS_ALWAYS_RETURNS_TRUE =
            new Break("CONTAINS_ALWAYS_RETURNS_TRUE");

    /// #### CONTAINS_ALWAYS_RETURNS_FALSE
    /// **Purpose**: Forces `contains()` method to always return a `false`.
    /// **Effect**: Method returns `false` regardless of whether the element is present.
    /// **Affected Methods:** [contains(Object)][#contains(Object)]
    /// @see BreakableCollection#contains(Object)
    public static final Break CONTAINS_ALWAYS_RETURNS_FALSE =
            new Break("CONTAINS_ALWAYS_RETURNS_FALSE");

    /// #### CONTAINS_RETURNS_OPPOSITE_VALUE
    /// **Purpose**: Forces `contains()` method to return the opposite value.
    /// **Effect**: Method returns `true` if element is absent, and `false` if present.
    /// **Affected Methods:** [contains(Object)][#contains(Object)]
    /// @see BreakableCollection#contains(Object)
    public static final Break CONTAINS_RETURNS_OPPOSITE_VALUE =
            new Break("CONTAINS_RETURNS_OPPOSITE_VALUE");

    /// #### CONTAINS_IS_NOT_THREAD_SAFE
    /// **Purpose**: Forces `contains()` method to be not thread safe.
    /// **Effect**: Disables synchronization for the `contains` operation.
    /// **Affected Methods:** [contains(Object)][#contains(Object)]
    /// @see BreakableCollection#contains(Object)
    public static final Break CONTAINS_IS_NOT_THREAD_SAFE =
            new Break("CONTAINS_IS_NOT_THREAD_SAFE");

    /// #### CONTAINS_ALL_ALWAYS_RETURNS_TRUE
    /// **Purpose**: Forces `containsAll()` method to always return a `true`.
    /// **Effect**: Method returns `true` regardless of whether all elements are present.
    /// **Affected Methods:** [containsAll(Collection)][#containsAll(Collection)]
    /// @see BreakableCollection#containsAll(Collection)
    public static final Break CONTAINS_ALL_ALWAYS_RETURNS_TRUE =
            new Break("CONTAINS_ALL_ALWAYS_RETURNS_TRUE");

    /// #### CONTAINS_ALL_ALWAYS_RETURNS_FALSE
    /// **Purpose**: Forces `containsAll()` method to always return a `false`.
    /// **Effect**: Method returns `false` regardless of whether all elements are present.
    /// **Affected Methods:** [containsAll(Collection)][#containsAll(Collection)]
    /// @see BreakableCollection#containsAll(Collection)
    public static final Break CONTAINS_ALL_ALWAYS_RETURNS_FALSE =
            new Break("CONTAINS_ALL_ALWAYS_RETURNS_FALSE");

    /// #### CONTAINS_ALL_IS_NOT_THREAD_SAFE
    /// **Purpose**: Forces `containsAll()` method to be not thread safe.
    /// **Effect**: Disables synchronization for the `containsAll` operation.
    /// **Affected Methods:** [containsAll(Collection)][#containsAll(Collection)]
    /// @see BreakableCollection#containsAll(Collection)
    public static final Break CONTAINS_ALL_IS_NOT_THREAD_SAFE =
            new Break("CONTAINS_ALL_IS_NOT_THREAD_SAFE");

    /// #### CONTAINS_ALL_RETURNS_OPPOSITE_VALUE
    /// **Purpose**: Forces `containsAll()` method to return the opposite value.
    /// **Effect**: Method returns `true` if any element is absent, and `false` if all are present.
    /// **Affected Methods:** [containsAll(Collection)][#containsAll(Collection)]
    /// @see BreakableCollection#containsAll(Collection)
    public static final Break CONTAINS_ALL_RETURNS_OPPOSITE_VALUE =
            new Break("CONTAINS_ALL_RETURNS_OPPOSITE_VALUE");

    /// #### CONTAINS_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION
    /// **Purpose**: Forces `containsAll()` method to throw the wrong exception when it is not supported.
    /// **Effect**: Throws `RuntimeException` instead of `UnsupportedOperationException`.
    /// **Affected Methods:** [containsAll(Collection)][#containsAll(Collection)]
    /// @see BreakableCollection#containsAll(Collection)
    public static final Break CONTAINS_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION =
            new Break("CONTAINS_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION");

    /// #### IS_EMPTY_ALWAYS_RETURNS_TRUE
    /// **Purpose**: Forces `isEmpty()` method to always return a `true`.
    /// **Effect**: Method returns `true` regardless of whether the collection is empty.
    /// **Affected Methods:** [isEmpty()][#isEmpty()]
    /// @see BreakableCollection#isEmpty()
    public static final Break IS_EMPTY_ALWAYS_RETURNS_TRUE =
            new Break("IS_EMPTY_ALWAYS_RETURNS_TRUE");

    /// #### IS_EMPTY_ALWAYS_RETURNS_FALSE
    /// **Purpose**: Forces `isEmpty()` method to always return a `false`.
    /// **Effect**: Method returns `false` regardless of whether the collection is empty.
    /// **Affected Methods:** [isEmpty()][#isEmpty()]
    /// @see BreakableCollection#isEmpty()
    public static final Break IS_EMPTY_ALWAYS_RETURNS_FALSE =
            new Break("IS_EMPTY_ALWAYS_RETURNS_FALSE");

    /// #### IS_EMPTY_RETURNS_OPPOSITE_VALUE
    /// **Purpose**: Forces `isEmpty()` method to return the opposite value.
    /// **Effect**: Method returns `true` if not empty, and `false` if empty.
    /// **Affected Methods:** [isEmpty()][#isEmpty()]
    /// @see BreakableCollection#isEmpty()
    public static final Break IS_EMPTY_RETURNS_OPPOSITE_VALUE =
            new Break("IS_EMPTY_RETURNS_OPPOSITE_VALUE");


    /// #### IS_EMPTY_IS_NOT_THREAD_SAFE
    /// **Purpose**: Forces `isEmpty()` to throw the wrong exception when not supported
    /// **Effect**: Throws `RuntimeException` instead of `UnsupportedOperationException`.
    /// **Affected Methods:** [isEmpty()][#isEmpty()]
    /// @see BreakableCollection#isEmpty()
    public static final Break IS_EMPTY_THROWS_WRONG_UNSUPPORTED_EXCEPTION =
            new Break("IS_EMPTY_THROWS_WRONG_UNSUPPORTED_EXCEPTION");

    /// #### IS_EMPTY_IS_NOT_THREAD_SAFE
    /// **Purpose**: Forces `isEmpty()` method to be not thread safe.
    /// **Effect**: Disables synchronization for the `isEmpty` operation.
    /// **Affected Methods:** [isEmpty()][#isEmpty()]
    /// @see BreakableCollection#isEmpty()
    public static final Break IS_EMPTY_IS_NOT_THREAD_SAFE =
            new Break("IS_EMPTY_IS_NOT_THREAD_SAFE");

    /// #### REMOVE_DOES_NOT_REMOVE_ELEMENT
    /// **Purpose**: Forces `remove()` method to not remove an element from the collection.
    /// **Effect**: Method returns normally but the collection remains unchanged.
    /// **Affected Methods:** [remove(Object)][#remove(Object)]
    /// @see BreakableCollection#remove(Object)
    public static final Break REMOVE_DOES_NOT_REMOVE_ELEMENT =
            new Break("REMOVE_DOES_NOT_REMOVE_ELEMENT");

    /// #### REMOVE_ALWAYS_RETURNS_TRUE
    /// **Purpose**: Forces `remove()` method to always return a result of `true`, even if the element is not removed.
    /// **Effect**: Method returns `true` regardless of whether the collection was modified.
    /// **Affected Methods:** [remove(Object)][#remove(Object)]
    /// @see BreakableCollection#remove(Object)
    public static final Break REMOVE_ALWAYS_RETURNS_TRUE =
            new Break("REMOVE_ALWAYS_RETURNS_TRUE");

    /// #### REMOVE_ALWAYS_RETURNS_FALSE
    /// **Purpose**: Forces `remove()` method to always return a result of `false`, even if the element is removed.
    /// **Effect**: Method returns `false` regardless of whether the collection was modified.
    /// **Affected Methods:** [remove(Object)][#remove(Object)]
    /// @see BreakableCollection#remove(Object)
    public static final Break REMOVE_ALWAYS_RETURNS_FALSE =
            new Break("REMOVE_ALWAYS_RETURNS_FALSE");

    /// #### REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE
    /// **Purpose**: Forces `remove()` method to always return the opposite of the appropriate result.
    /// **Effect**: Method returns `true` if not modified, and `false` if modified.
    /// **Affected Methods:** [remove(Object)][#remove(Object)]
    /// @see BreakableCollection#remove(Object)
    public static final Break REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE =
            new Break("REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE");

    /// #### REMOVE_IS_NOT_THREAD_SAFE
    /// **Purpose**: Forces `remove()` method to be not thread safe.
    /// **Effect**: Disables synchronization for the `remove` operation.
    /// **Affected Methods:** [remove(Object)][#remove(Object)]
    /// @see BreakableCollection#remove(Object)
    public static final Break REMOVE_IS_NOT_THREAD_SAFE =
            new Break("REMOVE_IS_NOT_THREAD_SAFE");

    /// #### REMOVE_THROWS_WRONG_UNSUPPORTED_EXCEPTION
    /// **Purpose**: Forces `remove()` method to throw the wrong exception when it is not supported.
    /// **Effect**: Throws `RuntimeException` instead of `UnsupportedOperationException`.
    /// **Affected Methods:** [remove(Object)][#remove(Object)]
    /// @see BreakableCollection#remove(Object)
    public static final Break REMOVE_THROWS_WRONG_UNSUPPORTED_EXCEPTION =
            new Break("REMOVE_THROWS_WRONG_UNSUPPORTED_EXCEPTION");

    /// #### REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS
    /// **Purpose**: Forces `removeAll()` method to not remove any elements from the collection.
    /// **Effect**: Method accepts the elements but the collection remains unchanged.
    /// **Affected Methods:** [removeAll(Collection)][#removeAll(Collection)]
    /// @see BreakableCollection#removeAll(Collection)
    public static final Break REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS =
            new Break("REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS");

    /// #### REMOVE_ALL_ALWAYS_RETURNS_TRUE
    /// **Purpose**: Forces `removeAll()` method to always return a result of `true`, even if the element is not
    /// removed.
    /// **Effect**: Method returns `true` regardless of whether the collection was modified.
    /// **Affected Methods:** [removeAll(Collection)][#removeAll(Collection)]
    /// @see BreakableCollection#removeAll(Collection)
    public static final Break REMOVE_ALL_ALWAYS_RETURNS_TRUE =
            new Break("REMOVE_ALL_ALWAYS_RETURNS_TRUE");

    /// #### REMOVE_ALL_ALWAYS_RETURNS_FALSE
    /// **Purpose**: Forces `removeAll()` method to always return a result of `false`, even any of the elements are
    /// removed.
    /// **Effect**: Method returns `false` regardless of whether the collection was modified.
    /// **Affected Methods:** [removeAll(Collection)][#removeAll(Collection)]
    /// @see BreakableCollection#removeAll(Collection)
    public static final Break REMOVE_ALL_ALWAYS_RETURNS_FALSE =
            new Break("REMOVE_ALL_ALWAYS_RETURNS_FALSE");

    /// #### REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE
    /// **Purpose**: Forces `removeAll()` method to always return the opposite of the appropriate result.
    /// **Effect**: Method returns `true` if not modified, and `false` if modified.
    /// **Affected Methods:** [removeAll(Collection)][#removeAll(Collection)]
    /// @see BreakableCollection#removeAll(Collection)
    public static final Break REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE =
            new Break("REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE");

    /// #### REMOVE_ALL_SKIPS_FIRST_ELEMENT
    /// **Purpose**: Forces `removeAll()` method to skip the first element to be removed.
    /// **Effect**: The first element in the input collection is not removed from this collection.
    /// **Affected Methods:** [removeAll(Collection)][#removeAll(Collection)]
    /// @see BreakableCollection#removeAll(Collection)
    public static final Break REMOVE_ALL_SKIPS_FIRST_ELEMENT =
            new Break("REMOVE_ALL_SKIPS_FIRST_ELEMENT");

    /// #### REMOVE_ALL_SKIPS_LAST_ELEMENT
    /// **Purpose**: Forces `removeAll()` method to skip the last element to be removed.
    /// **Effect**: The last element in the input collection is not removed from this collection.
    /// **Affected Methods:** [removeAll(Collection)][#removeAll(Collection)]
    /// @see BreakableCollection#removeAll(Collection)
    public static final Break REMOVE_ALL_SKIPS_LAST_ELEMENT =
            new Break("REMOVE_ALL_SKIPS_LAST_ELEMENT");

    /// #### REMOVE_ALL_DOES_NOT_THROW_ON_NULL_COLLECTION
    /// **Purpose**: Forces `removeAll()` method to accept a `null` collection.
    /// **Effect**: Returns `false` for a `null` argument instead of throwing `NullPointerException`.
    /// **Affected Methods:** [removeAll(Collection)][#removeAll(Collection)]
    /// @see BreakableCollection#removeAll(Collection)
    public static final Break REMOVE_ALL_DOES_NOT_THROW_ON_NULL_COLLECTION =
            new Break("REMOVE_ALL_DOES_NOT_THROW_ON_NULL_COLLECTION");

    /// #### REMOVE_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION
    /// **Purpose**: Forces `removeAll()` method to throw the wrong exception when it is not supported.
    /// **Effect**: Throws `RuntimeException` instead of `UnsupportedOperationException`.
    /// **Affected Methods:** [removeAll(Collection)][#removeAll(Collection)]
    /// @see BreakableCollection#removeAll(Collection)
    public static final Break REMOVE_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION =
            new Break("REMOVE_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION");

    /// #### REMOVE_ALL_IS_NOT_THREAD_SAFE
    /// **Purpose**: Forces `removeAll()` method to be not thread safe.
    /// **Effect**: Disables synchronization for the `removeAll` operation.
    /// **Affected Methods:** [removeAll(Collection)][#removeAll(Collection)]
    /// @see BreakableCollection#removeAll(Collection)
    public static final Break REMOVE_ALL_IS_NOT_THREAD_SAFE =
            new Break("REMOVE_ALL_IS_NOT_THREAD_SAFE");

    /// #### REMOVE_IF_DOES_NOT_REMOVE_ANY_ELEMENTS
    /// **Purpose**: Forces `removeIf()` method to not remove any elements from the collection.
    /// **Effect**: Method accepts the predicate but the collection remains unchanged.
    /// **Affected Methods:** [removeIf(Predicate)][#removeIf(Predicate)]
    /// @see BreakableCollection#removeIf(Predicate)
    public static final Break REMOVE_IF_DOES_NOT_REMOVE_ANY_ELEMENTS =
            new Break("REMOVE_IF_DOES_NOT_REMOVE_ANY_ELEMENTS");

    /// #### REMOVE_IF_ALWAYS_RETURNS_TRUE
    /// **Purpose**: Forces `removeIf()` method to always return a result of `true`, even if no elements are removed.
    /// **Effect**: Method returns `true` regardless of whether the collection was modified.
    /// **Affected Methods:** [removeIf(Predicate)][#removeIf(Predicate)]
    /// @see BreakableCollection#removeIf(Predicate)
    public static final Break REMOVE_IF_ALWAYS_RETURNS_TRUE =
            new Break("REMOVE_IF_ALWAYS_RETURNS_TRUE");

    /// #### REMOVE_IF_ALWAYS_RETURNS_FALSE
    /// **Purpose**: Forces `removeIf()` method to always return a result of `false`, even if elements are removed.
    /// **Effect**: Method returns `false` regardless of whether the collection was modified.
    /// **Affected Methods:** [removeIf(Predicate)][#removeIf(Predicate)]
    /// @see BreakableCollection#removeIf(Predicate)
    public static final Break REMOVE_IF_ALWAYS_RETURNS_FALSE =
            new Break("REMOVE_IF_ALWAYS_RETURNS_FALSE");

    /// #### REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE
    /// **Purpose**: Forces `removeIf()` method to always return the opposite of the appropriate result.
    /// **Effect**: Method returns `true` if not modified, and `false` if modified.
    /// **Affected Methods:** [removeIf(Predicate)][#removeIf(Predicate)]
    /// @see BreakableCollection#removeIf(Predicate)
    public static final Break REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE =
            new Break("REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE");

    /// #### REMOVE_IF_SKIPS_FIRST_ELEMENT
    /// **Purpose**: Forces `removeIf()` method to skip the collection's first element.
    /// **Effect**: The first element is not tested, so it is not removed even if it matches the predicate.
    /// **Affected Methods:** [removeIf(Predicate)][#removeIf(Predicate)]
    /// @see BreakableCollection#removeIf(Predicate)
    public static final Break REMOVE_IF_SKIPS_FIRST_ELEMENT =
            new Break("REMOVE_IF_SKIPS_FIRST_ELEMENT");

    /// #### REMOVE_IF_SKIPS_LAST_ELEMENT
    /// **Purpose**: Forces `removeIf()` method to skip the collection's last element.
    /// **Effect**: The last element is not tested, so it is not removed even if it matches the predicate.
    /// **Affected Methods:** [removeIf(Predicate)][#removeIf(Predicate)]
    /// @see BreakableCollection#removeIf(Predicate)
    public static final Break REMOVE_IF_SKIPS_LAST_ELEMENT =
            new Break("REMOVE_IF_SKIPS_LAST_ELEMENT");

    /// #### REMOVE_IF_DOES_NOT_THROW_ON_NULL_FILTER
    /// **Purpose**: Forces `removeIf()` method to accept a `null` filter.
    /// **Effect**: Returns `false` for a `null` filter instead of throwing `NullPointerException`.
    /// **Affected Methods:** [removeIf(Predicate)][#removeIf(Predicate)]
    /// @see BreakableCollection#removeIf(Predicate)
    public static final Break REMOVE_IF_DOES_NOT_THROW_ON_NULL_FILTER =
            new Break("REMOVE_IF_DOES_NOT_THROW_ON_NULL_FILTER");

    /// #### REMOVE_IF_THROWS_WRONG_UNSUPPORTED_EXCEPTION
    /// **Purpose**: Forces `removeIf()` method to throw the wrong exception when it is not supported.
    /// **Effect**: Throws `RuntimeException` instead of `UnsupportedOperationException`.
    /// **Affected Methods:** [removeIf(Predicate)][#removeIf(Predicate)]
    /// @see BreakableCollection#removeIf(Predicate)
    public static final Break REMOVE_IF_THROWS_WRONG_UNSUPPORTED_EXCEPTION =
            new Break("REMOVE_IF_THROWS_WRONG_UNSUPPORTED_EXCEPTION");

    /// #### REMOVE_IF_IS_NOT_THREAD_SAFE
    /// **Purpose**: Forces `removeIf()` method to be not thread safe.
    /// **Effect**: Disables synchronization for the `removeIf` operation.
    /// **Affected Methods:** [removeIf(Predicate)][#removeIf(Predicate)]
    /// @see BreakableCollection#removeIf(Predicate)
    public static final Break REMOVE_IF_IS_NOT_THREAD_SAFE =
            new Break("REMOVE_IF_IS_NOT_THREAD_SAFE");

    /// #### RETAIN_ALL_DOES_NOT_RETAIN_ANY_ELEMENTS
    /// **Purpose**: Forces `retainAll()` method to do nothing.
    /// **Effect**: Method completes normally but the collection remains unchanged: no element is removed.
    /// **Affected Methods:** [retainAll(Collection)][#retainAll(Collection)]
    /// @see BreakableCollection#retainAll(Collection)
    public static final Break RETAIN_ALL_DOES_NOT_RETAIN_ANY_ELEMENTS =
            new Break("RETAIN_ALL_DOES_NOT_RETAIN_ANY_ELEMENTS");

    /// #### RETAIN_ALL_ALWAYS_RETURNS_TRUE
    /// **Purpose**: Forces `retainAll()` method to always return a result of `true`, even if the collection is not
    /// modified.
    /// **Effect**: Method returns `true` regardless of whether the collection was modified.
    /// **Affected Methods:** [retainAll(Collection)][#retainAll(Collection)]
    /// @see BreakableCollection#retainAll(Collection)
    public static final Break RETAIN_ALL_ALWAYS_RETURNS_TRUE =
            new Break("RETAIN_ALL_ALWAYS_RETURNS_TRUE");

    /// #### RETAIN_ALL_ALWAYS_RETURNS_FALSE
    /// **Purpose**: Forces `retainAll()` method to always return a result of `false`, even if the collection is
    /// modified.
    /// **Effect**: Method returns `false` regardless of whether the collection was modified.
    /// **Affected Methods:** [retainAll(Collection)][#retainAll(Collection)]
    /// @see BreakableCollection#retainAll(Collection)
    public static final Break RETAIN_ALL_ALWAYS_RETURNS_FALSE =
            new Break("RETAIN_ALL_ALWAYS_RETURNS_FALSE");

    /// #### RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE
    /// **Purpose**: Forces `retainAll()` method to always return the opposite of the appropriate result.
    /// **Effect**: Method returns `true` if not modified, and `false` if modified.
    /// **Affected Methods:** [retainAll(Collection)][#retainAll(Collection)]
    /// @see BreakableCollection#retainAll(Collection)
    public static final Break RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE =
            new Break("RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE");

    /// #### RETAIN_ALL_SKIPS_FIRST_ELEMENT
    /// **Purpose**: Forces `retainAll()` method to skip the first element to be retained.
    /// **Effect**: The first element that should have been retained is removed.
    /// **Affected Methods:** [retainAll(Collection)][#retainAll(Collection)]
    /// @see BreakableCollection#retainAll(Collection)
    public static final Break RETAIN_ALL_SKIPS_FIRST_ELEMENT =
            new Break("RETAIN_ALL_SKIPS_FIRST_ELEMENT");

    /// #### RETAIN_ALL_SKIPS_LAST_ELEMENT
    /// **Purpose**: Forces `retainAll()` method to skip the last element to be retained.
    /// **Effect**: The last element that should have been retained is removed.
    /// **Affected Methods:** [retainAll(Collection)][#retainAll(Collection)]
    /// @see BreakableCollection#retainAll(Collection)
    public static final Break RETAIN_ALL_SKIPS_LAST_ELEMENT =
            new Break("RETAIN_ALL_SKIPS_LAST_ELEMENT");

    /// #### RETAIN_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION
    /// **Purpose**: Forces `retainAll()` method to throw the wrong exception when it is not supported.
    /// **Effect**: Throws `RuntimeException` instead of `UnsupportedOperationException`.
    /// **Affected Methods:** [retainAll(Collection)][#retainAll(Collection)]
    /// @see BreakableCollection#retainAll(Collection)
    public static final Break RETAIN_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION =
            new Break("RETAIN_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION");

    /// #### RETAIN_ALL_IS_NOT_THREAD_SAFE
    /// **Purpose**: Forces `retainAll()` method to be not thread safe.
    /// **Effect**: Disables synchronization for the `retainAll` operation.
    /// **Affected Methods:** [retainAll(Collection)][#retainAll(Collection)]
    /// @see BreakableCollection#retainAll(Collection)
    public static final Break RETAIN_ALL_IS_NOT_THREAD_SAFE =
            new Break("RETAIN_ALL_IS_NOT_THREAD_SAFE");

    /// #### SIZE_ALWAYS_RETURNS_ZERO
    /// **Purpose**: Forces `size()` method to always return zero.
    /// **Effect**: Method returns `0` regardless of the actual number of elements.
    /// **Affected Methods:** [size()][#size()]
    /// @see BreakableCollection#size()
    public static final Break SIZE_ALWAYS_RETURNS_ZERO =
            new Break("SIZE_ALWAYS_RETURNS_ZERO");

    /// #### SIZE_IS_NOT_THREAD_SAFE
    /// **Purpose**: Forces `size()` method to be not thread safe.
    /// **Effect**: Disables synchronization for the `size` operation.
    /// **Affected Methods:** [size()][#size()]
    /// @see BreakableCollection#size()
    public static final Break SIZE_IS_NOT_THREAD_SAFE =
            new Break("SIZE_IS_NOT_THRAD_SAFE");

    /// #### SIZE_ALWAYS_RETURNS_CONSTANT_VALUE
    /// **Purpose**: Forces `size()` method to always return a constant value.
    /// **Effect**: Method returns `DEFAULT_CAPACITY` regardless of the actual number of elements.
    /// **Affected Methods:** [size()][#size()]
    /// @see BreakableCollection#size()
    public static final Break SIZE_ALWAYS_RETURNS_CONSTANT_VALUE =
            new Break("SIZE_ALWAYS_RETURNS_CONSTANT_VALUE");

    /// #### TO_ARRAY_RETURNS_EMPTY_ARRAY
    /// **Purpose**: Forces `toArray()` method to always return an empty array.
    /// **Effect**: Method returns a new `Object[0]` regardless of the collection content.
    /// **Affected Methods:** [toArray()][#toArray()]
    /// @see BreakableCollection#toArray()
    public static final Break TO_ARRAY_RETURNS_EMPTY_ARRAY =
            new Break("TO_ARRAY_RETURNS_EMPTY_ARRAY");

    /// #### TO_ARRAY_RETURNS_NULL
    /// **Purpose**: Forces `toArray()` method to always return `null`.
    /// **Effect**: Method returns `null` regardless of the collection content.
    /// **Affected Methods:** [toArray()][#toArray()]
    /// @see BreakableCollection#toArray()
    public static final Break TO_ARRAY_RETURNS_NULL =
            new Break("TO_ARRAY_RETURNS_NULL");

    /// #### TO_ARRAY_IS_NOT_THREAD_SAFE
    /// **Purpose**: Forces `toArray()` method to be not thread safe.
    /// **Effect**: Disables synchronization for the `toArray` operation.
    /// **Affected Methods:** [toArray()][#toArray()]
    /// @see BreakableCollection#toArray()
    public static final Break TO_ARRAY_IS_NOT_THREAD_SAFE =
            new Break("TO_ARRAY_IS_NOT_THREAD_SAFE");

    /// #### TO_ARRAY_MISSING_FIRST_ELEMENT
    /// **Purpose**: Forces `toArray()` method to return an array missing the first element.
    /// **Effect**: The returned array is smaller by one and does not contain the first element.
    /// **Affected Methods:** [toArray()][#toArray()]
    /// @see BreakableCollection#toArray()
    public static final Break TO_ARRAY_MISSING_FIRST_ELEMENT =
            new Break("TO_ARRAY_MISSING_FIRST_ELEMENT");

    /// #### TO_ARRAY_MISSING_LAST_ELEMENT
    /// **Purpose**: Forces `toArray()` method to return an array missing the last element.
    /// **Effect**: The returned array is smaller by one and does not contain the last element.
    /// **Affected Methods:** [toArray()][#toArray()]
    /// @see BreakableCollection#toArray()
    public static final Break TO_ARRAY_MISSING_LAST_ELEMENT =
            new Break("TO_ARRAY_MISSING_LAST_ELEMENT");

    /// #### TO_ARRAY_STORE_DOES_NOT_COPY_ELEMENTS
    /// **Purpose**: Forces `toArray(T[])` method to not copy elements into the array.
    /// **Effect**: The provided array is returned unchanged, whatever its size.
    /// **Affected Methods:** [toArray(Object\[\])][#toArray(Object\[\])]
    /// @see BreakableCollection#toArray(Object[])
    public static final Break TO_ARRAY_STORE_DOES_NOT_COPY_ELEMENTS =
            new Break("TO_ARRAY_STORE_DOES_NOT_COPY_ELEMENTS");

    /// #### TO_ARRAY_STORE_RETURNS_EMPTY_ARRAY
    /// **Purpose**: Forces `toArray(T[])` method to always return an empty array.
    /// **Effect**: Method returns an array with all elements set to `null`.
    /// **Affected Methods:** [toArray(Object\[\])][#toArray(Object\[\])]
    /// @see BreakableCollection#toArray(Object[])
    public static final Break TO_ARRAY_STORE_RETURNS_EMPTY_ARRAY =
            new Break("TO_ARRAY_STORE_RETURNS_EMPTY_ARRAY");

    /// #### TO_ARRAY_STORE_RETURNS_NULL
    /// **Purpose**: Forces `toArray(T[])` method to always return `null`.
    /// **Effect**: Method returns `null` regardless of the collection content.
    /// **Affected Methods:** [toArray(Object\[\])][#toArray(Object\[\])]
    /// @see BreakableCollection#toArray(Object[])
    public static final Break TO_ARRAY_STORE_RETURNS_NULL =
            new Break("TO_ARRAY_STORE_RETURNS_NULL");

    /// #### TO_ARRAY_STORE_MISSING_FIRST_ELEMENT
    /// **Purpose**: Forces `toArray(T[])` method to return an array missing the first element.
    /// **Effect**: The returned array does not contain the first element (elements are shifted).
    /// **Affected Methods:** [toArray(Object\[\])][#toArray(Object\[\])]
    /// @see BreakableCollection#toArray(Object[])
    public static final Break TO_ARRAY_STORE_MISSING_FIRST_ELEMENT =
            new Break("TO_ARRAY_STORE_MISSING_FIRST_ELEMENT");

    /// #### TO_ARRAY_STORE_MISSING_LAST_ELEMENT
    /// **Purpose**: Forces `toArray(T[])` method to return an array missing the last element.
    /// **Effect**: The returned array does not contain the last element.
    /// **Affected Methods:** [toArray(Object\[\])][#toArray(Object\[\])]
    /// @see BreakableCollection#toArray(Object[])
    public static final Break TO_ARRAY_STORE_MISSING_LAST_ELEMENT =
            new Break("TO_ARRAY_STORE_MISSING_LAST_ELEMENT");

    /// #### TO_ARRAY_STORE_IS_NOT_THREAD_SAFE
    /// **Purpose**: Forces `toArray(T[])` method to be not thread safe.
    /// **Effect**: Disables synchronization for the `toArray(T[])` operation.
    /// **Affected Methods:** [toArray(Object\[\])][#toArray(Object\[\])]
    /// @see BreakableCollection#toArray(Object[])
    public static final Break TO_ARRAY_STORE_IS_NOT_THREAD_SAFE =
            new Break("TO_ARRAY_STORE_IS_NOT_THREAD_SAFE");

    /// Flag indicating whether this collection permits null elements.
    protected static final int PERMITS_NULLS              = 0b001;

    /// Flag indicating whether this collection permits duplicate elements.
    protected static final int PERMITS_DUPLICATES         = 0b010;

    /// Flag indicating whether this collection permits elements of incompatible types.
    protected static final int PERMITS_INCOMPATIBLE_TYPES = 0b100;

    /// Default setting for the permits field
    protected static final int DEFAULT_PERMITS = PERMITS_NULLS | PERMITS_DUPLICATES | PERMITS_INCOMPATIBLE_TYPES;

    @Serial
    private static final long serialVersionUID = 1L;

    /// The default capacity used for constant size returns when SIZE_ALWAYS_RETURNS_CONSTANT_VALUE break is applied.
    private static final int DEFAULT_CAPACITY = 10;

    /// The underlying collection that stores the actual elements.
    private final transient Collection<E> collection;

    ///  Field of bit flags that store what is permitted by the collection.
    private final int permits;

    /// The compatible type for elements in this collection, derived from the generic parameter.
    private final Class<?> compatibleType;

    /// Creates an empty collection that has no breaks.
    public BreakableCollection() {
        this(new ArrayList<>(), new HashSet<>(), new HashMap<>(), DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS,
                DEFAULT_SAFETY, Object.class);
    }

    /// Creates a breakable collection from an existing instance.
    /// @param other the breakable collection to copy.
    public BreakableCollection(final BreakableCollection<E> other) {
        this(new ArrayList<>(other.collection), new HashSet<>(other.breaks()), new HashMap<>(other.methodStatuses()),
                other.characteristics(), other.permits(), other.isSafe(), other.compatibleType);
    }

    /// Creates a breakable iterable from an iterable.
    /// @param collectionArg the iterator to use for the elements.
    public BreakableCollection(final Collection<E> collectionArg) {
        this(new ArrayList<>(collectionArg), new HashSet<>(), new HashMap<>(), DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS,
                DEFAULT_SAFETY, Object.class);
    }

    /// Creates a `BreakableCollection` from en existing collection and specifying the breaks and collection
    /// characteristics. Rather than calling this constructor directly, consider using the builder
    /// [BreakableCollection.Builder].
    /// @param c               the initial elements for the breakable collection.
    /// @param breaks          the breaks for the collection.
    /// @param methodStatuses  the method status configuration.
    /// @param characteristics the characteristics for the collection.
    /// @param permitsArg         the flags that indicate what types of values are supported by the collection.
    /// @param isSafe          whether the resulting object is safe for concurrent access.
    /// @param elementType     the element type that the collection supports (see [#permitsIncompatibleTypes()].
    /// @throws NullPointerException if either the `c` or the `breaks` parameters are null.
    protected BreakableCollection(final Collection<E> c, final Set<Break> breaks,
                                  final Map<InterfaceMethod, MethodStatus> methodStatuses,
                                  final int characteristics, final int permitsArg, final boolean isSafe,
                                  final Class<?> elementType) {
        super(c, breaks, methodStatuses, characteristics, isSafe);
        this.collection = Objects.requireNonNull(c);
        this.permits = permitsArg;
        this.compatibleType = elementType;
    }

    /// Creates a BreakableCollection that wraps an existing collection, with the given breaks and configuration.
    ///
    /// The collection is not copied: it becomes the backing store, so changes made through either reference are
    /// visible through both, while the breaks affect only operations made through the returned collection. Use it
    /// to make an existing collection breakable; [BreakableCollection.Builder] builds one from scratch.
    ///
    /// ### Thread Safety
    /// The thread safety of the wrapped BreakableCollection depends entirely on the
    /// thread safety of the underlying collection. No additional synchronization is provided.
    ///
    /// @param <E> the type of elements in the collection
    /// @param collection the existing collection to wrap with breakable functionality
    /// @param breaks the set of breaks to apply to the wrapped collection
    /// @param methodStatuses the initial method statuses configuration
    /// @param characteristics the initial spliterator characteristics
    /// @param permits the initial permits configuration for the collection
    /// @param isSafe whether the wrapped collection should be thread-safe
    /// @param elementType the element type that the collection supports
    /// @return a new BreakableCollection that wraps the provided collection with the specified breaks
    /// @throws NullPointerException if collection or breaks is null
    /// @since 1.0.0
    /// @see BreakableCollection.Builder
    /// @see BreakableCollection#BreakableCollection(Collection, Set, Map, int, int, boolean, Class)
    public static <E> BreakableCollection<E> wrap(final Collection<E> collection,
                                                  final Set<Break> breaks,
                                                  final Map<InterfaceMethod, MethodStatus> methodStatuses,
                                                  final int characteristics,
                                                  final int permits,
                                                  final boolean isSafe,
                                                  final Class<?> elementType) {
        return new BreakableCollection<>(collection, breaks, methodStatuses, characteristics,
                permits, isSafe, elementType);
    }

    /// Indicates if the collection permits null values as elements.
    /// @return `true` if the collection permits `null` elements, `false` if it does not.
    public boolean permitsNulls() {
        return (permits & PERMITS_NULLS) != 0;
    }

    /// Indicates if the collection permits duplicate elements.
    /// @return `true` if the collection permits duplicate elements, `false` if it does not.
    public boolean permitsDuplicates() {
        return (permits & PERMITS_DUPLICATES) != 0;
    }

    /// Indicates if the collection permits elements with incompatible types as arguments.
    /// @return `true` if the collection permits elements with incompatible types, `false` if it does not.
    public boolean permitsIncompatibleTypes() {
        return (permits & PERMITS_INCOMPATIBLE_TYPES) != 0;
    }

    /// Implements the [size][Collection#size] method from the [Collection] interface. This method can be broken using
    /// the following collection breaks:
    /// - [SIZE_ALWAYS_RETURNS_ZERO][BreakableCollection#SIZE_ALWAYS_RETURNS_ZERO]
    /// - [SIZE_ALWAYS_RETURNS_CONSTANT_VALUE][BreakableCollection#SIZE_ALWAYS_RETURNS_CONSTANT_VALUE]
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .addBreak(SIZE_ALWAYS_RETURNS_ZERO)
    ///         .build();
    /// ```
    /// @return The number of elements in the collection, or a different value if the collection has been broken.
    /// @see Collection#size()
    @Override
    public int size() {
        checkMethodSupport(CollectionMethods.SIZE);
        if (hasBreak(SIZE_ALWAYS_RETURNS_ZERO)) {
            return 0;
        } else if (hasBreak(SIZE_ALWAYS_RETURNS_CONSTANT_VALUE)) {
            return DEFAULT_CAPACITY;
        } else {
            return getWithBreakableSafety(SIZE_IS_NOT_THREAD_SAFE, collection::size);
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
    ///         .addBreak(IS_EMPTY_RETURNS_OPPOSITE_VALUE)
    ///         .build();
    /// ```
    /// @return The number of elements in the collection, or a different value if the collection has been broken.
    /// @see Collection#isEmpty()
    @Override
    public boolean isEmpty() {
        checkOptionalMethodSupport(CollectionMethods.IS_EMPTY, IS_EMPTY_THROWS_WRONG_UNSUPPORTED_EXCEPTION);

        boolean result = getWithBreakableSafety(IS_EMPTY_IS_NOT_THREAD_SAFE, collection::isEmpty);
        if (hasBreak(IS_EMPTY_ALWAYS_RETURNS_TRUE)) {
            return true;
        }
        if (hasBreak(IS_EMPTY_ALWAYS_RETURNS_FALSE)) {
            return false;
        }
        if (hasBreak(IS_EMPTY_RETURNS_OPPOSITE_VALUE)) {
            return !result;
        }
        return result;
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
    ///         .addBreak(CONTAINS_ALWAYS_RETURNS_FALSE)
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
        boolean result = getWithBreakableSafety(CONTAINS_IS_NOT_THREAD_SAFE, () -> collection.contains(o));
        if (hasBreak(CONTAINS_ALWAYS_RETURNS_TRUE)) {
            return true;
        }
        if (hasBreak(CONTAINS_ALWAYS_RETURNS_FALSE)) {
            return false;
        }
        if (hasBreak(CONTAINS_RETURNS_OPPOSITE_VALUE)) {
            return !result;
        }
        return result;
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
    ///         .addBreak(TO_ARRAY_RETURNS_NULL)
    ///         .build();
    /// ```
    /// @return An array with the elements from the collection, or some other value if the collection is broken.
    /// @see Collection#toArray()
    @SuppressWarnings("DataFlowIssue")
    @Override
    public Object @Nullable [] toArray() {
        if (hasBreak(TO_ARRAY_RETURNS_NULL)) {
            return null;
        }
        Object[] array = getWithBreakableSafety(TO_ARRAY_IS_NOT_THREAD_SAFE, () -> collection.toArray());
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
    ///         .addBreak(TO_ARRAY_STORE_RETURNS_NULL)
    ///         .build();
    /// ```
    /// @return An array with the elements from the collection, or some other value if the collection is broken.
    /// @see Collection#toArray(Object[])
    @SuppressWarnings("DataFlowIssue")
    @Override
    public <T> T @Nullable [] toArray(final T[] a) {
        if (hasBreak(TO_ARRAY_STORE_RETURNS_NULL)) {
            return null;
        } else if (hasBreak(TO_ARRAY_STORE_DOES_NOT_COPY_ELEMENTS)) {
            return a;
        }

        T[] array = getWithBreakableSafety(TO_ARRAY_STORE_IS_NOT_THREAD_SAFE, () -> collection.toArray(a));
        if (hasBreak(TO_ARRAY_STORE_RETURNS_EMPTY_ARRAY)) {
            Arrays.fill(array, null);
        } else if (hasBreak(TO_ARRAY_STORE_MISSING_FIRST_ELEMENT)) {
            for (int i = 1; i < collection.size(); i++) {
                array[i - 1] = array[i];
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
    /// - [ADD_THROWS_WRONG_UNSUPPORTED_EXCEPTION][BreakableCollection#ADD_THROWS_WRONG_UNSUPPORTED_EXCEPTION]
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .addBreak(ADD_DOES_NOT_ADD_ELEMENT)
    ///         .build();
    /// ```
    /// This is an optional method of the [Collection] interface. Support for this method can be removed using the
    /// optional method identifier [CollectionMethods#ADD]. A collection that does not support the `add` method
    /// can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .doesNotSupportMethod(CollectionMethods.ADD)
    ///         .build();
    /// ```
    /// @return 'true' if the element is added, 'false' if it isn't, or possibly a different value if the collection has
    ///          been broken.
    /// @throws NullPointerException if the argument is `null` and collection does not support `null` values.
    /// @throws IllegalArgumentException if the argument is already contained in the collections, and it does not
    ///         support duplicate values.
    /// @throws ClassCastException if the argument is an incompatible type and the collection does not support
    ///         incompatible types.
    /// @throws UnsupportedOperationException if this collection does not support this method.
    /// @throws RuntimeException if this method is not supported and the ADD_THROWS_WRONG_UNSUPPORTED_EXCEPTION
    ///         break is active.
    @Override
    public boolean add(final E e) {
        checkOptionalMethodSupport(CollectionMethods.ADD, ADD_THROWS_WRONG_UNSUPPORTED_EXCEPTION);

        boolean result = false;
        if (checkNewElement(e, ADD_THROWS_WRONG_NULL_EXCEPTION, ADD_THROWS_WRONG_INCOMPATIBLE_TYPE_EXCEPTION)) {
            if (!hasBreak(ADD_DOES_NOT_ADD_ELEMENT)) {
                result = getWithBreakableSafety(ADD_IS_NOT_THREAD_SAFE, () -> collection.add(e));
            }
        }

        if (hasBreak(ADD_ALWAYS_RETURNS_TRUE)) {
            return true;
        }
        if (hasBreak(ADD_ALWAYS_RETURNS_FALSE)) {
            return false;
        }
        if (hasBreak(ADD_ALWAYS_RETURNS_OPPOSITE_VALUE)) {
            return !result;
        }
        return result;
    }

    /// Implements the [remove][Collection#remove] method from the [Collection] interface. This method can be broken
    /// using the following collection breaks:
    /// - [REMOVE_DOES_NOT_REMOVE_ELEMENT][BreakableCollection#REMOVE_DOES_NOT_REMOVE_ELEMENT]
    /// - [REMOVE_ALWAYS_RETURNS_TRUE][BreakableCollection#REMOVE_ALWAYS_RETURNS_TRUE]
    /// - [REMOVE_ALWAYS_RETURNS_FALSE][BreakableCollection#REMOVE_ALWAYS_RETURNS_FALSE]
    /// - [REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE][BreakableCollection#REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE]
    /// - [REMOVE_THROWS_WRONG_UNSUPPORTED_EXCEPTION][BreakableCollection#REMOVE_THROWS_WRONG_UNSUPPORTED_EXCEPTION]
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .addBreak(REMOVE_DOES_NOT_REMOVE_ELEMENT)
    ///         .build();
    /// ```
    /// This is an optional method of the [Collection] interface. Support for this method can be removed using the
    /// optional method identifier [CollectionMethods#REMOVE]. A collection that does not support the `remove`
    /// method can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .doesNotSupportMethod(CollectionMethods.REMOVE)
    ///         .build();
    /// ```
    /// @return 'true' if the element is removed, 'false' if it isn't, or possibly a different value if the collection
    ///          has been broken.
    /// @throws NullPointerException if the argument is `null` and collection does not support `null` values.
    /// @throws ClassCastException if the argument is an incompatible type and the collection does not support
    ///         incompatible types.
    /// @throws UnsupportedOperationException if the method is not supported.
    /// @throws IllegalStateException if the method is not supported and the REMOVE_THROWS_WRONG_UNSUPPORTED_EXCEPTION
    ///         break is active.
    @Override
    public boolean remove(final Object o) {
        checkOptionalMethodSupport(CollectionMethods.REMOVE, REMOVE_THROWS_WRONG_UNSUPPORTED_EXCEPTION);
        checkArgument(o);

        boolean result = false;
        if (!hasBreak(REMOVE_DOES_NOT_REMOVE_ELEMENT)) {
            result = getWithBreakableSafety(REMOVE_IS_NOT_THREAD_SAFE, () -> collection.remove(o));
        }
        if (hasBreak(REMOVE_ALWAYS_RETURNS_TRUE)) {
            return true;
        }
        if (hasBreak(REMOVE_ALWAYS_RETURNS_FALSE)) {
            return false;
        }
        if (hasBreak(REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE)) {
            return !result;
        }
        return result;
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
    ///         .addBreak(CONTAINS_ALL_ALWAYS_RETURNS_FALSE)
    ///         .build();
    /// ```
    /// @return `true` if this collection contains the specified elements or false if it does not, or some other value
    ///         if the collection is broken.
    /// @throws NullPointerException if the argument contains a `null` and collection does not support `null` values.
    /// @throws ClassCastException if the argument is contains an incompatible type and the collection does not support
    ///         incompatible types.
    /// @see Collection#containsAll(Collection)
    @Override
    public boolean containsAll(final Collection<?> c) {
        checkOptionalMethodSupport(CollectionMethods.CONTAINS_ALL, CONTAINS_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION);
        checkArgumentElements(c);

        boolean result = getWithBreakableSafety(CONTAINS_ALL_IS_NOT_THREAD_SAFE, () -> collection.containsAll(c));
        if (hasBreak(CONTAINS_ALL_ALWAYS_RETURNS_TRUE)) {
            return true;
        }
        if (hasBreak(CONTAINS_ALL_ALWAYS_RETURNS_FALSE)) {
            return false;
        }
        if (hasBreak(CONTAINS_ALL_RETURNS_OPPOSITE_VALUE)) {
            return !result;
        }
        return result;
    }

    /// Implements the [addAll][Collection#addAll] method from the [Collection] interface. This method can be broken
    /// using the following collection breaks:
    /// - [ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS][BreakableCollection#ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS]
    /// - [ADD_ALL_SKIPS_FIRST_ELEMENT][BreakableCollection#ADD_ALL_SKIPS_FIRST_ELEMENT]
    /// - [ADD_ALL_SKIPS_LAST_ELEMENT][BreakableCollection#ADD_ALL_SKIPS_LAST_ELEMENT]
    /// - [ADD_ALL_ALWAYS_RETURNS_TRUE][BreakableCollection#ADD_ALL_ALWAYS_RETURNS_TRUE]
    /// - [ADD_ALL_ALWAYS_RETURNS_FALSE][BreakableCollection#ADD_ALL_ALWAYS_RETURNS_FALSE]
    /// - [ADD_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE][BreakableCollection#ADD_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE]
    /// - [ADD_THROWS_WRONG_UNSUPPORTED_EXCEPTION][BreakableCollection#ADD_THROWS_WRONG_UNSUPPORTED_EXCEPTION]
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .addBreak(ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS)
    ///         .build();
    /// ```
    /// This is an optional method of the [Collection] interface. Support for this method can be removed using the
    /// optional method identifier [CollectionMethods#ADD_ALL]. A collection that does not support the `addAll` method
    /// can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .doesNotSupportMethod(CollectionMethods.ADD_ALL)
    ///         .build();
    /// ```
    /// @return 'true' if all of the elements are added, 'false' otherwise, or possibly a different value if the
    ///          collection has been broken.
    /// @throws NullPointerException if the argument contains any `null` values and collection does not support `null`
    ///                              values.
    /// @throws ClassCastException if the argument contains any elements an incompatible type and the collection does
    ///                            not support incompatible types.
    /// @throws UnsupportedOperationException if this collection does not support this method.
    /// @throws IllegalStateException if this method is not supported and the ADD_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION
    ///         break is active.
    @Override
    public boolean addAll(final Collection<? extends E> c) {
        checkOptionalMethodSupport(CollectionMethods.ADD_ALL, ADD_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION);

        boolean result = false;
        if (checkNewArgumentElements(c)) {
            if (hasBreak(ADD_ALL_SKIPS_FIRST_ELEMENT)) {
                List<E> l = new ArrayList<>(c);
                result = getWithBreakableSafety(ADD_ALL_IS_NOT_THREAD_SAFE, () ->
                        collection.addAll(l.subList(1, l.size()))
                );
            } else if (hasBreak(ADD_ALL_SKIPS_LAST_ELEMENT)) {
                List<E> l = new ArrayList<>(c);
                result = getWithBreakableSafety(ADD_ALL_IS_NOT_THREAD_SAFE, () ->
                        collection.addAll(l.subList(0, l.size() - 1))
                );
            } else if (!hasBreak(ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS)) {
                result = getWithBreakableSafety(ADD_ALL_IS_NOT_THREAD_SAFE, () ->
                        collection.addAll(c)
                );
            }
        }
        if (hasBreak(ADD_ALL_ALWAYS_RETURNS_TRUE)) {
            return true;
        }
        if (hasBreak(ADD_ALL_ALWAYS_RETURNS_FALSE)) {
            return false;
        }
        if (hasBreak(ADD_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE)) {
            return !result;
        }
        return result;
    }

    /// Implements the [removeAll][Collection#removeAll] method from the [Collection] interface. This method can be
    /// broken using the following collection breaks:
    /// - [REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS][BreakableCollection#REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS]
    /// - [REMOVE_ALL_SKIPS_FIRST_ELEMENT][BreakableCollection#REMOVE_ALL_SKIPS_FIRST_ELEMENT]
    /// - [REMOVE_ALL_SKIPS_LAST_ELEMENT][BreakableCollection#REMOVE_ALL_SKIPS_LAST_ELEMENT]
    /// - [REMOVE_ALL_ALWAYS_RETURNS_TRUE][BreakableCollection#REMOVE_ALL_ALWAYS_RETURNS_TRUE]
    /// - [REMOVE_ALL_ALWAYS_RETURNS_FALSE][BreakableCollection#REMOVE_ALL_ALWAYS_RETURNS_FALSE]
    /// - [REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE][BreakableCollection#REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE]
    /// - [REMOVE_ALL_DOES_NOT_THROW_ON_NULL_COLLECTION][#REMOVE_ALL_DOES_NOT_THROW_ON_NULL_COLLECTION]
    /// - [REMOVE_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION][#REMOVE_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION]
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .addBreak(REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS)
    ///         .build();
    /// ```
    /// This is an optional method of the [Collection] interface. Support for this method can be removed using the
    /// optional method identifier [CollectionMethods#REMOVE_ALL]. A collection that does not support the `removeAll`
    /// method can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .doesNotSupportMethod(CollectionMethods.REMOVE_ALL)
    ///         .build();
    /// ```
    /// @param c collection containing elements to be removed from this collection
    /// @return 'true' if all of the elements are removed, 'false' otherwise, or possibly a different value if the
    ///          collection has been broken.
    /// @throws NullPointerException if the argument contains any `null` values and collection does not support `null`
    ///         values.
    /// @throws ClassCastException if the argument contains any elements an incompatible type and the collection does
    ///         not support incompatible types.
    /// @throws UnsupportedOperationException if this collection does not support this method.
    /// @throws IllegalStateException if this method is not supported and the
    /// REMOVE_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION.
    ///
    /// @see Collection#removeAll(Collection)
    @Override
    public boolean removeAll(final Collection<?> c) {
        checkOptionalMethodSupport(CollectionMethods.REMOVE_ALL, REMOVE_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION);
        if (c == null && hasBreak(REMOVE_ALL_DOES_NOT_THROW_ON_NULL_COLLECTION)) {
            return false;
        }
        Objects.requireNonNull(c);
        checkArgumentElements(c);

        boolean result = false;
        List<?> l = new ArrayList<>(c);
        if (hasBreak(REMOVE_ALL_SKIPS_FIRST_ELEMENT)) {
            result = getWithBreakableSafety(REMOVE_ALL_IS_NOT_THREAD_SAFE, () ->
                    collection.removeAll(l.subList(1, l.size()))
            );
        } else if (hasBreak(REMOVE_ALL_SKIPS_LAST_ELEMENT)) {
            result = getWithBreakableSafety(REMOVE_ALL_IS_NOT_THREAD_SAFE, () ->
                    collection.removeAll(l.subList(0, l.size() - 1))
            );
        } else if (!hasBreak(REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS)) {
            result = getWithBreakableSafety(REMOVE_ALL_IS_NOT_THREAD_SAFE, () ->
                    collection.removeAll(l)
            );
        }

        if (hasBreak(REMOVE_ALL_ALWAYS_RETURNS_TRUE)) {
            return true;
        }
        if (hasBreak(REMOVE_ALL_ALWAYS_RETURNS_FALSE)) {
            return false;
        }
        if (hasBreak(REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE)) {
            return !result;
        }
        return result;
    }

    /// Implements the [removeIf][Collection#removeIf] method from the [Collection] interface. This method can be broken
    /// using the following collection breaks:
    /// - [REMOVE_IF_DOES_NOT_REMOVE_ANY_ELEMENTS][BreakableCollection#REMOVE_IF_DOES_NOT_REMOVE_ANY_ELEMENTS]
    /// - [REMOVE_IF_SKIPS_FIRST_ELEMENT][BreakableCollection#REMOVE_IF_SKIPS_FIRST_ELEMENT]
    /// - [REMOVE_IF_SKIPS_LAST_ELEMENT][BreakableCollection#REMOVE_IF_SKIPS_LAST_ELEMENT]
    /// - [REMOVE_IF_ALWAYS_RETURNS_TRUE][BreakableCollection#REMOVE_IF_ALWAYS_RETURNS_TRUE]
    /// - [REMOVE_IF_ALWAYS_RETURNS_FALSE][BreakableCollection#REMOVE_IF_ALWAYS_RETURNS_FALSE]
    /// - [REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE][BreakableCollection#REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE]
    /// - [REMOVE_IF_DOES_NOT_THROW_ON_NULL_FILTER][BreakableCollection#REMOVE_IF_DOES_NOT_THROW_ON_NULL_FILTER]
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .addBreak(REMOVE_IF_DOES_NOT_REMOVE_ANY_ELEMENTS)
    ///         .build();
    /// ```
    /// This is an optional method of the [Collection] interface. Support for this method can be removed using the
    /// optional method identifier [CollectionMethods#REMOVE_IF]. A collection that does not support the `removeIf`
    /// method can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .doesNotSupportMethod(CollectionMethods.REMOVE_IF)
    ///         .build();
    /// ```
    /// @param filter a predicate which returns `true` for elements to be removed
    /// @return `true` if any elements were removed
    /// @throws NullPointerException if the specified filter is null
    /// @throws UnsupportedOperationException if the `removeIf` method is not supported.
    ///
    /// @see Collection#removeIf(Predicate)
    @Override
    public boolean removeIf(final Predicate<? super E> filter) {
        checkOptionalMethodSupport(CollectionMethods.REMOVE_IF, REMOVE_IF_THROWS_WRONG_UNSUPPORTED_EXCEPTION);
        if (filter == null && hasBreak(REMOVE_IF_DOES_NOT_THROW_ON_NULL_FILTER)) {
            return false;
        }
        Objects.requireNonNull(filter);

        AtomicBoolean result = new AtomicBoolean(false);
        if (hasBreak(REMOVE_IF_SKIPS_FIRST_ELEMENT)) {
            runWithBreakableSafety(REMOVE_IF_IS_NOT_THREAD_SAFE, () -> {
                Iterator<E> i = IterableTestUtils.skipFirstIterator(collection);
                while (i.hasNext()) {
                    if (filter.test(i.next())) {
                        i.remove();
                        result.set(true);
                    }
                }
            });
        } else if (hasBreak(REMOVE_IF_SKIPS_LAST_ELEMENT)) {
            runWithBreakableSafety(REMOVE_IF_IS_NOT_THREAD_SAFE, () -> {
                Iterator<E> i = IterableTestUtils.skipLastIterator(collection);
                while (i.hasNext()) {
                    if (filter.test(i.next())) {
                        i.remove();
                        result.set(true);
                    }
                }
            });
        } else if (!hasBreak(REMOVE_IF_DOES_NOT_REMOVE_ANY_ELEMENTS)) {
            boolean changed = getWithBreakableSafety(REMOVE_IF_IS_NOT_THREAD_SAFE, () ->
                    collection.removeIf(filter)
            );
            result.set(changed);
        }

        if (hasBreak(REMOVE_IF_ALWAYS_RETURNS_TRUE)) {
            return true;
        }
        if (hasBreak(REMOVE_IF_ALWAYS_RETURNS_FALSE)) {
            return false;
        }
        if (hasBreak(REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE)) {
            return !result.get();
        }
        return result.get();
    }

    /// Implements the [retainAll][Collection#retainAll] method from the [Collection] interface. This method can be
    /// broken using the following collection breaks:
    /// - [RETAIN_ALL_DOES_NOT_RETAIN_ANY_ELEMENTS][BreakableCollection#RETAIN_ALL_DOES_NOT_RETAIN_ANY_ELEMENTS]
    /// - [RETAIN_ALL_SKIPS_FIRST_ELEMENT][BreakableCollection#RETAIN_ALL_SKIPS_FIRST_ELEMENT]
    /// - [RETAIN_ALL_SKIPS_LAST_ELEMENT][BreakableCollection#RETAIN_ALL_SKIPS_LAST_ELEMENT]
    /// - [RETAIN_ALL_ALWAYS_RETURNS_TRUE][BreakableCollection#RETAIN_ALL_ALWAYS_RETURNS_TRUE]
    /// - [RETAIN_ALL_ALWAYS_RETURNS_FALSE][BreakableCollection#RETAIN_ALL_ALWAYS_RETURNS_FALSE]
    /// - [RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE][BreakableCollection#RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE]
    /// - [RETAIN_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION][#RETAIN_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION]
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .addBreak(RETAIN_ALL_DOES_NOT_RETAIN_ANY_ELEMENTS)
    ///         .build();
    /// ```
    /// This is an optional method of the [Collection] interface. Support for this method can be removed using the
    /// optional method identifier [CollectionMethods#RETAIN_ALL]. A collection that does not support the `retainAll`
    /// method can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .doesNotSupportMethod(CollectionMethods.RETAIN_ALL)
    ///         .build();
    /// ```
    /// @return 'true' if the collection is modified, 'false' otherwise, or possibly a different value if the
    ///          collection has been broken.
    /// @throws NullPointerException if the argument contains any `null` values and collection does not support `null`
    ///     `   values.
    /// @throws ClassCastException if the argument contains any elements an incompatible type and the collection does
    ///         not support incompatible types.
    /// @throws UnsupportedOperationException if this collection does not support this method.
    /// @throws IllegalStateException if this metho is not supported and the
    ///         RETAIN_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION is active.
    ///
    /// @see Collection#retainAll(Collection)
    @Override
    public boolean retainAll(final Collection<?> c) {
        checkOptionalMethodSupport(CollectionMethods.RETAIN_ALL, RETAIN_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION);
        checkArgumentElements(c);

        boolean result = false;
        List<?> l = new ArrayList<>(c);
        if (hasBreak(RETAIN_ALL_SKIPS_FIRST_ELEMENT)) {
            result = getWithBreakableSafety(RETAIN_ALL_IS_NOT_THREAD_SAFE, () ->
                    collection.retainAll(l.subList(1, l.size()))
            );
        } else if (hasBreak(RETAIN_ALL_SKIPS_LAST_ELEMENT)) {
            result = getWithBreakableSafety(RETAIN_ALL_IS_NOT_THREAD_SAFE, () ->
                    collection.retainAll(l.subList(0, l.size() - 1))
            );
        } else if (!hasBreak(RETAIN_ALL_DOES_NOT_RETAIN_ANY_ELEMENTS)) {
            result = getWithBreakableSafety(RETAIN_ALL_IS_NOT_THREAD_SAFE, () ->
                    collection.retainAll(l)
            );
        }

        if (hasBreak(RETAIN_ALL_ALWAYS_RETURNS_TRUE)) {
            return true;
        }
        if (hasBreak(RETAIN_ALL_ALWAYS_RETURNS_FALSE)) {
            return false;
        }
        if (hasBreak(RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE)) {
            return !result;
        }
        return result;
    }

    /// Implements the [clear][Collection#clear] method from the [Collection] interface. This method can be broken using
    /// the following collection breaks:
    /// - [CLEAR_DOES_NOT_REMOVE_ANY_ELEMENTS][BreakableCollection#CLEAR_DOES_NOT_REMOVE_ANY_ELEMENTS]
    /// - [CLEAR_SKIPS_FIRST_ELEMENT][BreakableCollection#CLEAR_SKIPS_FIRST_ELEMENT]
    /// - [CLEAR_SKIPS_LAST_ELEMENT][BreakableCollection#CLEAR_SKIPS_LAST_ELEMENT]
    /// - [CLEAR_THROWS_WRONG_UNSUPPORTED_EXCEPTION][BreakableCollection#CLEAR_THROWS_WRONG_UNSUPPORTED_EXCEPTION].
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildCollection(1,2,3,4,5)
    ///         .addBreak(CLEAR_DOES_NOT_REMOVE_ANY_ELEMENTS)
    ///         .build();
    /// ```
    /// @throws UnsupportedOperationException if the `clear` is not supported.
    /// @throws IllegalStateException if this method is not supported and the CLEAR_THROWS_WRONG_UNSUPPORTED_EXCEPTION
    /// @see Collection#clear()
    @Override
    public void clear() {
        checkOptionalMethodSupport(CollectionMethods.CLEAR, CLEAR_THROWS_WRONG_UNSUPPORTED_EXCEPTION);
        if (hasBreak(CLEAR_SKIPS_FIRST_ELEMENT)) {
            runWithBreakableSafety(CLEAR_IS_NOT_THREAD_SAFE, () -> {
                Iterator<E> i = IterableTestUtils.skipFirstIterator(collection);
                while (i.hasNext()) {
                    i.next();
                    i.remove();
                }
            });
        } else if (hasBreak(CLEAR_SKIPS_LAST_ELEMENT)) {
            runWithBreakableSafety(CLEAR_IS_NOT_THREAD_SAFE, () -> {
                Iterator<E> i = IterableTestUtils.skipLastIterator(collection);
                while (i.hasNext()) {
                    i.next();
                    i.remove();
                }
            });
        } else if (!hasBreak(CLEAR_DOES_NOT_REMOVE_ANY_ELEMENTS)) {
            runWithBreakableSafety(CLEAR_IS_NOT_THREAD_SAFE, collection::clear);
        }
    }



    /// returns the elements as an unbroken instance of `Collection'
    /// @return an unbroken collection.
    @Override
    public Collection<E> unbroken() {
        return collection;
    }

    /// Serialization support for writing the collection state.
    ///
    /// This method ensures that the collection's fields, including the inherited
    /// behavioral modifications, are correctly serialized.
    ///
    /// @param out the [ObjectOutputStream] to write to
    /// @throws IOException if an I/O error occurs
    @Serial
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(collection);
    }

    /// Serialization support for reading the collection state.
    ///
    /// This method ensures that the collection's fields, including the inherited
    /// behavioral modifications, are correctly restored during deserialization.
    ///
    /// @param in the [ObjectInputStream] to read from
    /// @throws IOException if an I/O error occurs
    /// @throws ClassNotFoundException if the class of a serialized object could not be found
    @Serial
    @SuppressWarnings("unchecked")
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        Collection<E> c = (Collection<E>) in.readObject();
        try {
            var field = BreakableCollection.class.getDeclaredField("collection");
            field.setAccessible(true);
            field.set(this, c);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new IOException("Failed to restore collection field", e);
        }
    }

    /// Checks that the argument is valid for this collection. This will check that:
    ///  - The argument is not null or the collection permits nulls
    ///  - The argument is a compatible type or the collection permits incompatible types.
    /// @param arg the argument to check.
    /// @throws NullPointerException if the argument is 'null' and the collection does not permit nulls.
    /// @throws ClassCastException if the argument is not compatible and the collection does not permit incompatible
    ///                            types.
    protected void checkArgument(final Object arg) {
        if (!permitsNulls() && arg == null) {
            throw new NullPointerException();
        }
        if (!permitsIncompatibleTypes() && arg != null && !compatibleType.isAssignableFrom(arg.getClass())) {
            throw new ClassCastException("incompatible type: " + arg.getClass().getName());
        }
    }

    /// Validates the provided argument based on nullability and type compatibility rules.
    /// Throws appropriate exceptions or breaks the flow as defined by the parameters.
    ///
    /// This method ensures that the given `arg` complies with the constraints
    /// defined in this context. If null values or incompatible types are not permitted,
    /// it will handle violations using either a `Break` object or specific exceptions.
    ///
    /// @param arg              The object to be checked. It can be null or incompatible
    ///                         with the expected type based on the current settings.
    /// @param wrongNullBreak   The `Break` instance to handle null value violations.
    ///                         If `permitsNulls()` is `false` and `arg` is null,
    ///                         this `Break` is triggered (runtime exception is thrown).
    ///                         If no `Break` is defined, a [NullPointerException] is thrown instead.
    /// @param wrongTypeBreak   The `Break` instance to handle type compatibility violations.
    ///                         If `permitsIncompatibleTypes()` is `false` and `arg`
    ///                         is not assignable to the compatible type, this `Break` is triggered
    ///                         (runtime exception is thrown). If no `Break` is defined,
    ///                         a [ClassCastException] is thrown instead.
    ///
    /// @throws NullPointerException  If `arg` is null and `permitsNulls()` is `false`,
    ///                               and no `Break` is specified to handle the violation.
    /// @throws ClassCastException    If `arg` is incompatible with the expected type and
    ///                               `permitsIncompatibleTypes()` is `false`,
    ///                               and no `Break` is specified to handle the violation.
    protected void checkArgument(final Object arg, final Break wrongNullBreak, final Break wrongTypeBreak) {
        if (!permitsNulls() && arg == null) {
            if (hasBreak(wrongNullBreak)) {
                throw new RuntimeException();
            }
            throw new NullPointerException();
        }
        if (!permitsIncompatibleTypes() && arg != null && !compatibleType.isAssignableFrom(arg.getClass())) {
            if (hasBreak(wrongTypeBreak)) {
                throw new RuntimeException();
            }
            throw new ClassCastException("incompatible type: " + arg.getClass().getName());
        }
    }


    /// Checks that all elements in the specified collection are valid arguments for this collection.
    ///
    /// This method iterates through the provided collection and calls [BreakableCollection#checkArgument] for each
    /// element, ensuring they meet the collection's requirements regarding null values and type compatibility.
    ///
    /// @param c the collection of elements to check.
    /// @throws NullPointerException if the collection contains a `null` element and this collection does not permit
    ///                            nulls.
    /// @throws ClassCastException if the collection contains an element with an incompatible type and this collection
    ///                            does not permit incompatible types.
    protected void checkArgumentElements(final Collection<?> c) {
        c.forEach(this::checkArgument);
    }

    /**
     * Checks each element in the provided collection to ensure it meets the specified argument
     * validation conditions. If any element fails validation, the corresponding {@code Break}
     * action will be triggered.
     *
     * @param c                  The collection of elements to be validated.
     *                           Must not be null or contain unsupported elements.
     * @param wrongNullBreak     The {@link Break} action to invoke if a null element is found
     *                           in the collection.
     * @param wrongTypeBreak     The {@link Break} action to invoke if an element of an
     *                           unsupported or invalid type is found in the collection.
     */
    protected void checkArgumentElements(final Collection<?> c, final Break wrongNullBreak,
                                         final Break wrongTypeBreak) {
        c.forEach((e) -> checkArgument(e, wrongNullBreak, wrongTypeBreak));
    }

    /// Checks that the element is valid to add to this collection.
    /// This will check that the element is not a duplicate, or the collection permits duplicate values.
    /// @param e the element to check.
    /// @return 'true' if the element is valid, 'false' is it is not.
    /// @throws NullPointerException if the argument is 'null' and the collection does not permit nulls.
    /// @throws ClassCastException if the argument is not compatible and the collection does not permit incompatible
    ///                            types.
    protected boolean checkNewElement(final Object e) {
        checkArgument(e);
        return permitsDuplicates() || !collection.contains(e);
    }

    /// Checks if the specified element can be considered new within the current collection.
    /// This method evaluates the element against duplicate policies and collection content to
    /// determine whether it is already part of the collection or breaks specified constraints.
    ///
    /// @param e               The element to be checked. Must not be `null` and should
    ///                        be of an acceptable type, as enforced by the provided breaks.
    /// @param wrongNullBreak  The [Break] instance to trigger if `e` is `null`.
    ///                        Typically used to enforce non-null constraints.
    /// @param wrongTypeBreak  The [Break] instance to trigger if `e` is of an unsupported type.
    ///                        Ensures type-safety when adding elements.
    /// @return `true` if the element is considered new (i.e., either duplicates are allowed
    ///         or `e` is not already contained in the collection); `false` otherwise.
    protected boolean checkNewElement(final Object e, final Break wrongNullBreak,
                                      final Break wrongTypeBreak) {
        checkArgument(e, wrongNullBreak, wrongTypeBreak);
        return permitsDuplicates() || !collection.contains(e);
    }

    /// Checks that all elements in the specified collection are valid to be added to this collection.
    ///
    /// This method iterates through the provided collection and calls [BreakableCollection#checkNewElement] for each
    /// element. It ensures that all elements satisfy the collection's requirements, including null permissions,
    /// type compatibility, and duplicate permissions.
    ///
    /// @param c the collection of elements to check.
    /// @return `true` if all elements are valid to be added, `false` otherwise (e.g., if any element is a duplicate
    ///         and duplicates are not permitted).
    /// @throws NullPointerException if the collection contains a `null` element and this collection does not permit
    ///                            nulls.
    /// @throws ClassCastException if the collection contains an element with an incompatible type and this collection
    ///                            does not permit incompatible types.
    protected boolean checkNewArgumentElements(final Collection<?> c) {
        return c.stream().allMatch(this::checkNewElement);
    }

    /// Verifies if all elements in the provided collection meet the required conditions.
    /// Each element is validated using the `checkNewElement` method.
    ///
    /// @param c               The collection of elements that need to be checked. Must not be null.
    /// @param wrongNullBreak  A `Break` instance that will be triggered if any element in
    ///                        the collection is null.
    /// @param wrongTypeBreak  A `Break` instance that will be triggered if any element in
    ///                        the collection is of an incorrect type.
    /// @return `true` if all elements in the collection meet the required conditions,
    ///         otherwise `false`.
    protected boolean checkNewArgumentElements(final Collection<?> c, final Break wrongNullBreak,
                                               final Break wrongTypeBreak) {
        return c.stream().allMatch(e -> checkNewElement(e, wrongNullBreak, wrongTypeBreak));
    }

    /// Returns the bit flags that indicate what types of values that the collection supports.
    /// @return the bit flags that indicate what types of values that the collection supports.
    protected int permits() {
        return permits;
    }

    /// Retrieves the compatible type for a specific operation or validation within the system.
    ///
    /// This method is used to determine the class type that can be utilized
    /// or validated in the current context. It ensures type compatibility based
    /// on the operational requirements as defined in the implementation.
    ///
    /// @return the [Class] object representing the compatible type. This value is used
    ///         to evaluate and confirm type compatibility for the associated logic.
    protected Class<?> compatibleType() {
        return compatibleType;
    }

    /// Utility class for implementing builders for subclasses of BreakableCollection.
    /// @param <B> the builder type
    /// @param <C> the breakable collection type
    /// @param <E> the element type
    /// @author evanbergstrom
    /// @since 1.0
    public abstract static class AbstractBuilder<B extends AbstractBuilder<B, C, E>,
            C extends BreakableCollection<E>, E>
            extends BreakableIterable.AbstractBuilder<B, C, E> {

        ///  Field of bit flags that store what is permitted by the collection.
        private int permits;

        private Class<?> compatibleType = Object.class;

        /// Default constructor to be called by default constructors for subclasses.
        /// Initializes the builder with default values: permits nulls, duplicates, and incompatible types.
        protected AbstractBuilder() {
            this(new ArrayList<>());
        }

        /// Creates a builder with the specified initial elements.
        /// Initializes with default values: permits nulls, duplicates, and incompatible types.
        /// @param elements the initial elements for the builder
        /// @throws NullPointerException if elements is null
        protected AbstractBuilder(final Collection<E> elements) {
            super(Objects.requireNonNull(elements));
            this.permits = DEFAULT_PERMITS;
        }

        /// Copy constructor to be called by copy constructors for subclasses.
        /// @param other the builder to copy.
        /// @throws NullPointerException if the argument is `null`.
        protected AbstractBuilder(final AbstractBuilder<B, C, E> other) {
            super(other);
            this.permits = other.permits;
            this.compatibleType = other.compatibleType;
        }

        /// Sets the builder to construct a collection that does not permit nulls. By default, the collection will
        /// support `null`.
        /// @return th builder.
        public B doesNotPermitNulls() {
            permits = permits & ~PERMITS_NULLS;
            return self();
        }

        /// Sets the builder to construct a collection that does not permit duplicate elements. By default, the
        /// collection will support duplicate elements.
        /// @return th builder.
        public B doesNotPermitDuplicates() {
            permits = permits & ~PERMITS_DUPLICATES;
            return self();
        }

        /// Sets the builder to construct a collection that does not permit incompatible types. By default, the
        /// collection will support incompatible types.
        /// @param type the type that the collection supports.
        /// @return th builder.
        public B doesNotPermitIncompatibleTypes(final Class<?> type) {
            permits = permits & ~PERMITS_INCOMPATIBLE_TYPES;
            this.compatibleType = type;
            return self();
        }

        /// Sets the builder to construct a collection that does not support the method provided as the argument.
        /// @param method the method that the collection does not support.
        /// @return the builder.
        public final B doesNotSupportMethod(final InterfaceMethod method) {
            doesNotSupport(method);
            return self();
        }

        /// Returns the bit flags that indicate what types of values that the collection supports.
        /// @return the bit flags that indicate what types of values that the collection supports.
        protected int permits() {
            return permits;
        }


        /// Return the type supported by the collection.
        /// @return the type that is supported by the collection.
        public Class<?> compatibleType() {
            return compatibleType;
        }
    }

    /// The builder for BreakableCollection objects.
    /// @param <E> the element type.
    public static class Builder<E> extends AbstractBuilder<Builder<E>, BreakableCollection<E>, E> {

        /// Create a builder initialized with the default values.
        /// The builder will permit nulls, duplicates, and incompatible types by default.
        public Builder() {
            super();
        }

        /// Create a builder initialized with the values copied from another builder.
        /// @param other the builder to copy the values from
        /// @throws NullPointerException if the argument is `null`
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

        @Override
        public BreakableCollection<E> copy(final BreakableCollection<E> other) {
            return new BreakableCollection<>(other);
        }

        @Override
        public BreakableCollection<E> build(final Iterable<E> elements) {
            List<E>  list = new ArrayList<>(elements());
            elements.forEach(list::add);
            return new BreakableCollection<>(list, new HashSet<>(breaks()),
                    new HashMap<>(methodStatuses()), characteristics(), permits(), isSafe(), compatibleType());

        }
    }

    /// Creates a collection provider for instances of `BreakableCollection` built by a builder.
    ///
    /// @param <E> the element type
    /// @param builder the builder that configures each instance; it is copied, so later changes to it are not seen
    /// @param elementProvider the provider of elements
    /// @return a collection provider for breakable objects
    public static <E> CollectionProvider<BreakableCollection<E>, E> collectionProvider(
            final Builder<E> builder,
            final ObjectProvider<E> elementProvider) {
        return BreakableBuilder.collectionProvider(builder.copy(), elementProvider);
    }

    /// Mixin interface that adds an implementation of the `objectProvider()` method that provides instances of
    /// `BreakableCollection` that do not have any breaks applied.
    /// @param <E> element type
    public interface WithProvider<E> extends CollectionProviderSupport<BreakableCollection<E>, E> {
        @Override
        default CollectionProvider<BreakableCollection<E>, E> provider() {
            return BreakableCollection.collectionProvider(new Builder<>(), elementProvider());
        }
    }

    /// Mixin interface that adds an implementation of the `objectProvider()` method that provides instances of
    /// `BreakableCollection` that are thread safe.
    /// @param <E> element type
    public interface WithThreadSafeProvider<E> extends CollectionProviderSupport<BreakableCollection<E>, E> {
        @Override
        default CollectionProvider<BreakableCollection<E>, E> provider() {
            return BreakableCollection.collectionProvider(new Builder<E>().setSafe(true), elementProvider());
        }
    }

    /// Returns a random element from this collection, excluding a specific index.
    ///
    /// This method returns a random element from the collection using a deterministic
    /// random number generator, while attempting to avoid a specific index position.
    /// It's primarily used by break implementations that need to return arbitrary
    /// elements instead of the correct ones.
    ///
    /// **Behavior:**
    /// - Returns an element at a random (but deterministic) position
    /// - Attempts to exclude the specified index (when possible)
    /// - Uses the same random seed for consistent behavior across test runs
    /// - For List implementations, uses indexed access for efficiency
    /// - For other collections, converts to array first
    ///
    /// **Algorithm:**
    /// The method generates random indices until it finds one that doesn't match
    /// the exclusion index (or until it gives up). This ensures that when simulating
    /// broken behavior, the returned element is likely different from the expected one.
    ///
    /// **Usage:**
    /// This method is typically used internally by break implementations to simulate
    /// corrupted collection behavior where the wrong element is returned:
    /// ```java
    /// // Example internal usage in a break implementation
    /// if (hasBreak(PEEK_RETURNS_RANDOM_ELEMENT)) {
    ///     return randomElementExcludingIndex(0); // Return something other than first element
    /// }
    /// ```
    ///
    /// **Performance Considerations:**
    /// - List access is O(1) for ArrayList, O(n) for LinkedList
    /// - Non-list collections require O(n) array conversion
    /// - Random index generation adds minimal overhead
    ///
    /// @param exclude the index to attempt to exclude when selecting random element; -1 means no exclusion
    /// @return a random element from the collection, or null if collection is empty
    /// @see #randomInt()
    @SuppressWarnings("unchecked")
    protected @Nullable E randomElementExcludingIndex(final int exclude) {
        if (isEmpty()) {
            return null;
        }

        int i = 0;
        int size = size();
        if (size > 1 || exclude != 0) {
            do {
                i = Math.abs(randomInt() % size);
            } while (i == exclude);
        }

        if (collection instanceof List<E> list) {
            return list.get(i);
        } else {
            Object[] elements = collection.toArray();
            // unchecked cast
            return (E) elements[i];
        }
    }
}
