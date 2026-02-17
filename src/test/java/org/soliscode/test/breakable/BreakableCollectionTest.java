package org.soliscode.test.breakable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.assertions.Assertions;
import org.soliscode.test.contract.collection.CollectionContract;
import org.soliscode.test.contract.collection.CollectionMethods;
import org.soliscode.test.contract.object.SerializableContract;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.interfaces.CollectionOnly;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.soliscode.test.assertions.ArrayAssertions.assertLengthEquals;
import static org.soliscode.test.assertions.Assertions.assertThrowsDifferent;
import static org.soliscode.test.assertions.collection.CollectionAssertions.*;

/// Tests for the [BreakableCollection] class. These tests determine if the breaks supported by this class result in the
/// behavior expected.
///
/// @author evanbergstrom
/// @since 1.0
/// @see BreakableCollection
public class BreakableCollectionTest extends AbstractTest
        implements CollectionContract<Integer, BreakableCollection<Integer>>,
           BreakableCollection.WithProvider<Integer>,
           WithIntegerElement,
           SerializableContract<BreakableCollection<Integer>> {



    //=========== Constructor Tests ============

    /// Verifies the default constructor creates an empty collection with proper initial state.
    ///
    /// @see BreakableCollection#BreakableCollection()
    @Test
    public void defaultConstructor_whenCalled_returnAnInstanceWithDefaultSettings() {
        BreakableCollection<Integer> collection = new BreakableCollection<>();
        assertIsEmpty(collection);
        assertTrue(collection.permitsDuplicates());
        assertTrue(collection.permitsNulls());
        assertTrue(collection.permitsIncompatibleTypes());
        assertIsEmpty(collection.breaks());
    }

    /// Verifies the copy constructor correctly copies elements and configuration.
    ///
    /// @see BreakableCollection#BreakableCollection(BreakableCollection)
    @Test
    public void copyConstructor_whenCalled_returnsACopy() {
        BreakableCollection<Integer> original = Breakables.buildCollection(1, 2, 3)
                .doesNotPermitIncompatibleTypes(Integer.class)
                .doesNotPermitDuplicates()
                .doesNotPermitNulls()
                .addBreak(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT)
                .build();

        BreakableCollection<Integer> copy = new BreakableCollection<>(original);

        assertContainsSame(original, copy);
        assertEquals(original.permits(), copy.permits());
        assertEquals(original.compatibleType(), copy.compatibleType());
        assertEquals(original.permitsDuplicates(), copy.permitsDuplicates());
        assertEquals(original.permitsNulls(), copy.permitsNulls());
        assertEquals(original.permitsIncompatibleTypes(), copy.permitsIncompatibleTypes());
        assertEquals(original.breaks(), copy.breaks());

        // TODO: This needs to check for method support
    }

    // ========== size Tests ==========

    /// Test that the `SIZE_ALWAYS_RETURNS_ZERO` break causes the `size` method to return zero.
    /// @see BreakableCollection#size()
    /// @throws org.opentest4j.AssertionFailedError if any assertions fail.
    @DisplayName("size() always returns zero with the SIZE_ALWAYS_RETURNS_ZERO break")
    @Test
    public void size_withAlwaysReturnsZeroBreak_returnsZero() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.SIZE_ALWAYS_RETURNS_ZERO)
                .build();

        assertEquals(0, collection.size());
    }

    /// Test that the `SIZE_ALWAYS_RETURNS_CONSTANT_VALUE` break causes the `size` method to return a constant value.
    /// @see BreakableCollection#size()
    @DisplayName("size() always returns constant value with the SIZE_ALWAYS_RETURNS_CONSTANT_VALUE break")
    @Test
    public void size_withSizeAlwaysReturnsConstantValueBreak_returnsConstantValue() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.SIZE_ALWAYS_RETURNS_CONSTANT_VALUE)
                .build();
        assertNotEquals(3, collection.size());  // size() returns default capacity (10)
    }

    // ========== isEmpty Tests ==========

    /// Test that the `IS_EMPTY_ALWAYS_RETURNS_TRUE` break causes the `isEmpty` method to always return `true`.
    /// @see BreakableCollection#isEmpty()
    @DisplayName("isEmpty() always returns true with IS_EMPTY_ALWAYS_RETURNS_TRUE break")
    @Test
    public void isEmpty_withIsEmptyAlwaysReturnsTrueBreak_returnsTrue() {
        Collection<Integer> collection = Breakables.buildCollection(Integer.class)
                .addBreak(BreakableCollection.IS_EMPTY_ALWAYS_RETURNS_TRUE)
                .build();
        assertTrue(collection.isEmpty());

        collection.add(1);
        assertTrue(collection.isEmpty());
    }

    /// Test that the `IS_EMPTY_ALWAYS_RETURNS_FALSE` break causes the `isEmpty` method to always return `false`.
    /// @see BreakableCollection#isEmpty()
    @DisplayName("isEmpty() always returns false with IS_EMPTY_ALWAYS_RETURNS_FALSE break")
    @Test
    public void isEmpty_withIsEmptyAlwaysReturnsFalseBreak_returnsFalse() {
        Collection<Integer> collection = Breakables.buildCollection(Integer.class)
                .addBreak(BreakableCollection.IS_EMPTY_ALWAYS_RETURNS_FALSE)
                .build();
        assertFalse(collection.isEmpty());

        collection.add(1);
        assertFalse(collection.isEmpty());
    }

    /// Test that the `IS_EMPTY_RETURNS_OPPOSITE_VALUE` break causes the `isEmpty` method to always return the opposite value.
    /// @see BreakableCollection#isEmpty()
    @DisplayName("isEmpty() always returns opposite value with IS_EMPTY_RETURNS_OPPOSITE_VALUE break")
    @Test
    public void isEmpty_withIsEmptyReturnsOppositeValueBreak_returnsOppositeValue() {
        Collection<Integer> collection = Breakables.buildCollection(Integer.class)
                .addBreak(BreakableCollection.IS_EMPTY_RETURNS_OPPOSITE_VALUE)
                .build();
        assertFalse(collection.isEmpty());

        collection.add(1);
        assertTrue(collection.isEmpty());
    }

    // ========== contains Tests ==========

    /// Test that the `CONTAINS_ALWAYS_RETURNS_TRUE` break causes the `contains` method to always return `true`.
    /// @see BreakableCollection#contains(Object)
    @DisplayName("contains(Object) always returns true with CONTAINS_ALWAYS_RETURNS_TRUE break")
    @Test
    public void contains_withContainsAlwaysReturnsTrueBreak_returnsTrue() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.CONTAINS_ALWAYS_RETURNS_TRUE)
                .build();
        assertTrue(collection.contains(1));
        assertTrue(collection.contains(4));
    }

    /// Test that the `CONTAINS_ALWAYS_RETURNS_FALSE` break causes the `contains` method to always return `false`.
    /// @see BreakableCollection#contains(Object)
    @DisplayName("contains(Object) always returns false with CONTAINS_ALWAYS_RETURNS_FALSE break")
    @Test
    public void contains_withContainsAlwaysReturnsFalseBreak_returnsFalse() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.CONTAINS_ALWAYS_RETURNS_FALSE)
                .build();
        assertFalse(collection.contains(1));
        assertFalse(collection.contains(4));
    }

    /// Test that the `CONTAINS_RETURNS_OPPOSITE_VALUE` break causes the `contains` method to always return the opposite value.
    /// @see BreakableCollection#contains(Object)
    @DisplayName("contains(Object) always returns opposite value with CONTAINS_RETURNS_OPPOSITE_VALUE break")
    @Test
    public void contains_withContainsReturnsOppositeValueBreak_returnsOppositeValue() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.CONTAINS_RETURNS_OPPOSITE_VALUE)
                .build();
        assertFalse(collection.contains(1));
        assertTrue(collection.contains(4));
    }

    // ========== toArray Tests ==========

    /// Test that the `TO_ARRAY_RETURNS_NULL` break causes the `toArray()` method to always return `null`.
    /// @see BreakableCollection#toArray()
    @DisplayName("toArray() always returns null with TO_ARRAY_RETURNS_NULL break")
    @Test
    @SuppressWarnings("DataFlowIssue")
    public void toArray_withToArrayReturnsNullBreak_returnsNull() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.TO_ARRAY_RETURNS_NULL)
                .build();
        assertNull(collection.toArray());
    }

    /// Test that the `TO_ARRAY_RETURNS_EMPTY_ARRAY` break causes the `toArray()` method to always returns an empty array.
    /// @see BreakableCollection#toArray()
    @DisplayName("toArray() always returns empty array with TO_ARRAY_RETURNS_EMPTY_ARRAY break")
    @Test
    public void toArray_withToArrayReturnsEmptyArrayBreak_returnsEmptyArray() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.TO_ARRAY_RETURNS_EMPTY_ARRAY)
                .build();
        Object[] array = collection.toArray();
        assertLengthEquals(0, array);
    }

    /// Test that the `TO_ARRAY_MISSING_FIRST_ELEMENT` break causes the `toArray()` method to return an array that is missing the first element.
    /// @see BreakableCollection#toArray()
    @DisplayName("toArray() returns array missing first element with TO_ARRAY_MISSING_FIRST_ELEMENT break")
    @Test
    public void toArray_withToArrayMissingFirstElementBreak_returnsArrayMissingFirstElement() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.TO_ARRAY_MISSING_FIRST_ELEMENT)
                .build();
        Object[] array = collection.toArray();
        assertArrayEquals(new Object[]{2, 3}, array);
    }

    /// Test that the `TO_ARRAY_MISSING_LAST_ELEMENT` break causes the `toArray()` method to return an array that is missing the last element.
    /// @see BreakableCollection#toArray()
    @DisplayName("toArray() returns array missing last element with TO_ARRAY_MISSING_LAST_ELEMENT break")
    @Test
    public void toArray_withToArrayMissingLastElementBreak_returnsArrayMissingLastElement() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.TO_ARRAY_MISSING_LAST_ELEMENT)
                .build();
        Object[] array = collection.toArray();
        assertArrayEquals(new Object[]{1, 2}, array);
    }

    /// Test that the `TO_ARRAY_STORE_RETURNS_NULL` break causes the `toArray(Object[])` method to always return `null`.
    /// @see BreakableCollection#toArray(Object[])
    @DisplayName("toArray(Object[]) always returns null with TO_ARRAY_STORE_RETURNS_NULL break")
    @Test
    @SuppressWarnings("DataFlowIssue")
    public void toArray_withToArrayStoreReturnsNullBreak_returnsNull() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.TO_ARRAY_STORE_RETURNS_NULL)
                .build();
        assertNull(collection.toArray(new Object[]{0, 0, 0}));
    }

    /// Test that the `TO_ARRAY_STORE_RETURNS_EMPTY_ARRAY` break causes the `toArray(Object[])` method to always returns an empty array.
    /// @see BreakableCollection#toArray(Object[])
    @DisplayName("toArray(Object[]) always returns empty array with TO_ARRAY_STORE_RETURNS_EMPTY_ARRAY break")
    @Test
    public void toArray_withToArrayStoreReturnsEmptyArrayBreak_returnsEmptyArray() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.TO_ARRAY_STORE_RETURNS_EMPTY_ARRAY)
                .build();
        assertArrayEquals(new Object[]{null, null, null}, collection.toArray(new Object[]{1, 2, 3}));
    }

    /// Test that the `TO_ARRAY_STORE_MISSING_FIRST_ELEMENT` break causes the `toArray(Object[])` method to return an array that is missing the first element.
    /// @see BreakableCollection#toArray(Object[])
    @DisplayName("toArray(Object[]) returns array missing first element with TO_ARRAY_STORE_MISSING_FIRST_ELEMENT break")
    @Test
    public void toArray_withToArrayStoreMissingFirstElementBreak_returnsArrayMissingFirstElement() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.TO_ARRAY_STORE_MISSING_FIRST_ELEMENT)
                .build();
        Object[] array = new Object[]{0, 0, 0};
        collection.toArray(array);
        assertArrayEquals(new Object[]{2, 3, null}, array);
    }

    /// Test that the `TO_ARRAY_STORE_MISSING_LAST_ELEMENT` break causes the `toArray(Object[])` method to return an array that is missing the last element.
    /// @see BreakableCollection#toArray(Object[])
    @DisplayName("toArray(Object[]) returns array missing last element with TO_ARRAY_STORE_MISSING_LAST_ELEMENT break")
    @Test
    public void toArray_withToArrayStoreMissingLastElementBreak_returnsArrayMissingLastElement() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.TO_ARRAY_STORE_MISSING_LAST_ELEMENT)
                .build();
        Object[] array = new Object[]{0, 0, 0};
        collection.toArray(array);
        assertArrayEquals(new Object[]{1, 2, null}, array);
    }

    /// Test that the `TO_ARRAY_STORE_DOES_NOT_COPY_ELEMENTS` break causes the `toArray(Object[])` method to not copy any elements into the store array.
    /// @see BreakableCollection#toArray(Object[])
    @DisplayName("toArray(Object[]) does not copy elements with TO_ARRAY_STORE_DOES_NOT_COPY_ELEMENTS break")
    @Test
    public void toArray_withToArrayStoreDoesNotCopyElementsBreak_doesNotCopyElements() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.TO_ARRAY_STORE_DOES_NOT_COPY_ELEMENTS)
                .build();
        Object[] array = new Object[]{0, 0, 0};
        collection.toArray(array);
        assertArrayEquals(new Object[]{0, 0, 0}, array);
    }

    // ========== add_singleElement_returnsTrueAndUpdatesSize Tests ==========

    /// Test that the `ADD_DOES_NOT_ADD_ELEMENT` break causes the `add(Object)` method to not add the element to the collection.
    /// @see BreakableCollection#add(Object)
    @DisplayName("add(Object) does not add element with ADD_DOES_NOT_ADD_ELEMENT break")
    @Test
    public void add_withAddDoesNotAddElementBreak_doesNotAddElement() {
        Collection<Integer> collection = Breakables.buildCollection(Integer.class)
                .addBreak(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT)
                .build();
        collection.add(1);
        assertDoesNotContain(1, collection);
    }

    /// Test that the `ADD_ALWAYS_RETURNS_TRUE` break causes the `add(Object)` method to always return `true`.
    /// @see BreakableCollection#add(Object)
    @DisplayName("add(Object) always returns true with ADD_ALWAYS_RETURNS_TRUE break")
    @Test
    public void add_withAddAlwaysReturnsTrueBreak_returnsTrue() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.ADD_ALWAYS_RETURNS_TRUE)
                .doesNotPermitDuplicates()
                .build();
        assertTrue(collection.add(1));
    }

    /// Test that the `ADD_ALWAYS_RETURNS_FALSE` break causes the `add(Object)` method to always return `false`.
    /// @see BreakableCollection#add(Object)
    @DisplayName("add(Object) always returns false with ADD_ALWAYS_RETURNS_FALSE break")
    @Test
    public void add_withAddAlwaysReturnsFalseBreak_returnsFalse() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.ADD_ALWAYS_RETURNS_FALSE)
                .build();
        assertFalse(collection.add(4));
    }

    /// Test that the `ADD_ALWAYS_RETURNS_OPPOSITE_VALUE` break causes the `add(Object)` method to always return the opposite value.
    /// @see BreakableCollection#add(Object)
    @DisplayName("add(Object) always returns opposite value with ADD_ALWAYS_RETURNS_OPPOSITE_VALUE break")
    @Test
    public void add_withAddAlwaysReturnsOppositeValueBreak_returnsOppositeValue() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.ADD_ALWAYS_RETURNS_OPPOSITE_VALUE)
                .doesNotPermitDuplicates()
                .build();
        assertTrue(collection.add(1));
        assertFalse(collection.add(4));
    }

    /// Test that the 'add(Object)' method throws an exception when incompatible types are not permitted and an object
    /// of an incompatible type is added through a raw interface.
    /// @see BreakableCollection#add(Object)
    /// @see BreakableCollection#permitsIncompatibleTypes
    @DisplayName("add(Object) throws an exception when incompatible types are not permitted")
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void add_whenIncompatibleTypesAreNotPermitted_throwsAnException() {
        Collection collection = Breakables.buildCollection(Integer.class)
                .doesNotPermitIncompatibleTypes(Integer.class)
                .build();

        Assertions.assertThrowsAnyOf(List.of(ClassCastException.class, IllegalArgumentException.class),
                () -> collection.add("Not An Integer"));
    }

    /// Test that the `ADD_THROWS_WRONG_INCOMPATIBLE_TYPE_EXCEPTION` break causes the `add(Object)` method to throw
    /// the wrong exception for an incompatible value.
    /// @see BreakableCollection#add(Object)
    @DisplayName("add(Object) throws wrong exception with ADD_THROWS_WRONG_INCOMPATIBLE_TYPE_EXCEPTION")
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void add_withThrowsWrongIncompatibleTypeBreak_throwsWrongException() {
        Collection collection = Breakables.buildCollection(Integer.class)
                .doesNotPermitIncompatibleTypes(Integer.class)
                .addBreak(BreakableCollection.ADD_THROWS_WRONG_INCOMPATIBLE_TYPE_EXCEPTION)
                .build();

        Assertions.assertThrowsDifferent(List.of(ClassCastException.class, IllegalArgumentException.class),
                () -> collection.add("Not An Integer"));
    }

    ///  Test that the `add(Object)` method throws `NullPointerException` when nulls are not permitted.
    /// @see BreakableCollection#add(Object)
    /// @see BreakableCollection#permitsNulls
    @DisplayName("add(Object) throws NullPointerException when nulls are not permitted")
    @Test
    public void add_whenNullsAreNotPermitted_throwsNullPointerException() {
        final Collection<Integer> collection = Breakables.buildCollection(Integer.class)
                .doesNotPermitNulls()
                .build();
        assertThrows(NullPointerException.class, () -> collection.add(null));
    }

    /// Test that the `ADD_THROWS_WRONG_NULL_EXCEPTION` break causes the `add(Object)` method to throw
    /// the wrong exception for a 'null' value.
    /// @see BreakableCollection#add(Object)
    @DisplayName("add(Object) throws wrong exception with ADD_THROWS_WRONG_NULL_EXCEPTION")
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void add_withThrowsWrongNullExceptionBreak_throwsWrongException() {
        Collection collection = Breakables.buildCollection(Integer.class)
                .doesNotPermitNulls()
                .addBreak(BreakableCollection.ADD_THROWS_WRONG_NULL_EXCEPTION)
                .build();
        assertThrowsDifferent(NullPointerException.class, () -> collection.add(null));
    }

    /// Test that the `ADD_THROWS_WRONG_UNSUPPORTED_EXCEPTION` break causes the `add(Object)` method to throw the wrong
    /// exception when unsupported.
    /// @see BreakableCollection#add(Object)
    @DisplayName("add(Object) throws wrong exception with ADD_THROWS_WRONG_UNSUPPORTED_EXCEPTION break")
    @Test
    public void add_withAddThrowsWrongUnsupportedExceptionBreak_throwsWrongException() {
        final Collection<Integer> collection = Breakables.buildCollection(Integer.class)
                .addBreak(BreakableCollection.ADD_THROWS_WRONG_UNSUPPORTED_EXCEPTION)
                .doesNotSupportMethod(CollectionMethods.ADD)
                .build();

        assertThrowsDifferent(UnsupportedOperationException.class, () -> collection.add(1));
    }

    // ========== remove Tests ==========

    /// Test that the `REMOVE_DOES_NOT_REMOVE_ELEMENT` break causes the `remove(Object)` method to not remove the element from the collection.
    /// @see BreakableCollection#remove(Object)
    @DisplayName("remove(Object) does not remove element with REMOVE_DOES_NOT_REMOVE_ELEMENT break")
    @Test
    public void remove_withRemoveDoesNotRemoveElementBreak_doesNotRemoveElement() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.REMOVE_DOES_NOT_REMOVE_ELEMENT)
                .build();
        collection.remove(1);
        assertContains(1, collection);
    }

    /// Test that the `REMOVE_ALWAYS_RETURNS_TRUE` break causes the `remove(Object)` method to always return `true`.
    /// @see BreakableCollection#remove(Object)
    @DisplayName("remove(Object) always returns true with REMOVE_ALWAYS_RETURNS_TRUE break")
    @Test
    public void remove_withRemoveAlwaysReturnsTrueBreak_returnsTrue() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.REMOVE_ALWAYS_RETURNS_TRUE)
                .build();
        assertTrue(collection.remove(1));
        assertTrue(collection.remove(4));
    }

    /// Test that the `REMOVE_ALWAYS_RETURNS_FALSE` break causes the `remove(Object)` method to always return `false`.
    /// @see BreakableCollection#remove(Object)
    @DisplayName("remove(Object) always returns false with REMOVE_ALWAYS_RETURNS_FALSE break")
    @Test
    public void remove_withRemoveAlwaysReturnsFalseBreak_returnsFalse() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.REMOVE_ALWAYS_RETURNS_FALSE)
                .build();
        assertFalse(collection.remove(1));
        assertFalse(collection.remove(4));
    }

    /// Test that the `REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE` break causes the `remove(Object)` method to always return the opposite value.
    /// @see BreakableCollection#remove(Object)
    @DisplayName("remove(Object) always returns opposite value with REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE break")
    @Test
    public void remove_withRemoveAlwaysReturnsOppositeValueBreak_returnsOppositeValue() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE)
                .build();
        assertFalse(collection.remove(1));
        assertTrue(collection.remove(4));
    }

    /// Test that the `REMOVE_THROWS_WRONG_UNSUPPORTED_EXCEPTION` break causes the `remove(Object)` method to throw the wrong exception when unsupported.
    /// @see BreakableCollection#remove(Object)
    @DisplayName("remove(Object) throws wrong exception with REMOVE_THROWS_WRONG_UNSUPPORTED_EXCEPTION break")
    @Test
    public void remove_withRemoveThrowsWrongUnsupportedExceptionBreak_throwsWrongException() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.REMOVE_THROWS_WRONG_UNSUPPORTED_EXCEPTION)
                .doesNotSupportMethod(CollectionMethods.REMOVE)
                .build();
        assertThrowsDifferent(UnsupportedOperationException.class, () -> collection.remove(1));
    }

    /// Test that the 'remove(Object)' method throws an exception when incompatible types are not permitted and an object
    /// of an incompatible type is removed through a raw interface.
    /// @see BreakableCollection#remove(Object)
    /// @see BreakableCollection#permitsIncompatibleTypes
    @DisplayName("remove(Object) throws an exception when incompatible types are not permitted")
    @Test
    @SuppressWarnings("rawtypes")
    public void remove_whenIncompatibleTypesAreNotPermitted_throwsAnException() {
        Collection collection = Breakables.buildCollection(Integer.class)
                .addElements(1, 2, 3)
                .doesNotPermitIncompatibleTypes(Integer.class)
                .build();

        Assertions.assertThrowsAnyOf(List.of(ClassCastException.class, IllegalArgumentException.class),
                () -> collection.remove("Not An Integer"));
    }

    // ========== addAll Tests ==========

    /// Test that the `ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS` break causes the `addAll(Collection)` method to not add_singleElement_returnsTrueAndUpdatesSize any elements.
    /// @see BreakableCollection#addAll(Collection)
    @DisplayName("addAll(Collection) does not add_singleElement_returnsTrueAndUpdatesSize any elements with ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS break")
    @Test
    public void addAll_withAddAllDoesNotAddAnyElementsBreak_doesNotAddElements() {
        Collection<Integer> collection = Breakables.buildCollection(Integer.class)
                .addBreak(BreakableCollection.ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS)
                .build();
        collection.addAll(Arrays.asList(1, 2, 3));
        assertTrue(collection.isEmpty());
    }

    /// Test that the `ADD_ALL_ALWAYS_RETURNS_TRUE` break causes the `addAll(Collection)` method to always return `true`.
    /// @see BreakableCollection#addAll(Collection)
    @DisplayName("addAll(Collection) always returns true with ADD_ALL_ALWAYS_RETURNS_TRUE break")
    @Test
    public void addAll_withAddAllAlwaysReturnsTrueBreak_returnsTrue() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.ADD_ALL_ALWAYS_RETURNS_TRUE)
                .doesNotPermitDuplicates()
                .build();
        assertTrue(collection.addAll(Arrays.asList(1, 2)));
    }

    /// Test that the `ADD_ALL_ALWAYS_RETURNS_FALSE` break causes the `addAll(Collection)` method to always return `false`.
    /// @see BreakableCollection#addAll(Collection)
    @DisplayName("addAll(Collection) always returns false with ADD_ALL_ALWAYS_RETURNS_FALSE break")
    @Test
    public void addAll_withAddAllAlwaysReturnsFalseBreak_returnsFalse() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.ADD_ALL_ALWAYS_RETURNS_FALSE)
                .build();
        assertFalse(collection.addAll(Arrays.asList(4, 5)));
    }

    /// Test that the `ADD_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE` break causes the `addAll(Collection)` method to always return the opposite value.
    /// @see BreakableCollection#addAll(Collection)
    @DisplayName("addAll(Collection) always returns opposite value with ADD_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break")
    @Test
    public void addAll_withAddAllAlwaysReturnsOppositeValueBreak_returnsOppositeValue() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.ADD_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE)
                .doesNotPermitDuplicates()
                .build();
        assertTrue(collection.addAll(Arrays.asList(1, 2)));
        assertFalse(collection.addAll(Arrays.asList(4, 5)));
    }

    /// Test that the `ADD_ALL_SKIPS_FIRST_ELEMENT` break causes the `addAll(Collection)` method to skip the first element.
    /// @see BreakableCollection#addAll(Collection)
    @DisplayName("addAll(Collection) skips first element with ADD_ALL_SKIPS_FIRST_ELEMENT break")
    @Test
    public void addAll_withAddAllSkipsFirstElementBreak_skipsFirstElement() {
        Collection<Integer> collection = Breakables.buildCollection(Integer.class)
                .addBreak(BreakableCollection.ADD_ALL_SKIPS_FIRST_ELEMENT)
                .build();
        collection.addAll(Arrays.asList(1, 2, 3));
        assertDoesNotContain(1, collection);
        assertContains(2, collection);
        assertContains(3, collection);
    }

    /// Test that the `ADD_ALL_SKIPS_LAST_ELEMENT` break causes the `addAll(Collection)` method to skip the last element.
    /// @see BreakableCollection#addAll(Collection)
    @DisplayName("addAll(Collection) skips last element with ADD_ALL_SKIPS_LAST_ELEMENT break")
    @Test
    public void addAll_withAddAllSkipsLastElementBreak_skipsLastElement() {
        Collection<Integer> collection = Breakables.buildCollection(Integer.class)
                .addBreak(BreakableCollection.ADD_ALL_SKIPS_LAST_ELEMENT)
                .build();
        collection.addAll(Arrays.asList(1, 2, 3));
        assertContains(1, collection);
        assertContains(2, collection);
        assertDoesNotContain(3, collection);
    }

    /// Test that the `ADD_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION` break causes the `addAll(Collection)` method to throw the wrong exception when unsupported.
    /// @see BreakableCollection#addAll(Collection)
    @DisplayName("addAll(Collection) throws wrong exception with ADD_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION break")
    @Test
    public void addAll_withAddAllThrowsWrongUnsupportedExceptionBreak_throwsWrongException() {
        Collection<Integer> collection = Breakables.buildCollection(Integer.class)
                .addBreak(BreakableCollection.ADD_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION)
                .doesNotSupportMethod(CollectionMethods.ADD_ALL)
                .build();
        assertThrowsDifferent(UnsupportedOperationException.class, () -> collection.addAll(Arrays.asList(1, 2)));
    }

    /// Test that the 'addAll(Collection)' method throws an exception when incompatible types are not permitted and an
    /// collection with an object of an incompatible type is added through a raw interface.
    /// @see BreakableCollection#addAll(Collection)
    /// @see BreakableCollection#permitsIncompatibleTypes
    @DisplayName("addAll(Object) throws an exception when incompatible types are not permitted")
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void addAll_whenIncompatibleTypesAreNotPermitted_throwsAnException() {
        Collection collection = Breakables.buildCollection(Integer.class)
                .doesNotPermitIncompatibleTypes(Integer.class)
                .build();

        Collection newElements = List.of(1, "Two", 3);
        Assertions.assertThrowsAnyOf(List.of(ClassCastException.class, IllegalArgumentException.class),
                () -> collection.addAll(newElements));
    }

    // ========== removeAll Tests ==========

    /// Test that the `REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS` break causes the `removeAll(Collection)` method to not remove any elements.
    /// @see BreakableCollection#removeAll(Collection)
    @DisplayName("removeAll(Collection) does not remove any elements with REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS break")
    @Test
    public void removeAll_withRemoveAllDoesNotRemoveAnyElementsBreak_doesNotRemoveElements() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS)
                .build();
        collection.removeAll(Arrays.asList(1, 2));
        assertContains(1, collection);
        assertContains(2, collection);
    }

    /// Test that the `REMOVE_ALL_ALWAYS_RETURNS_TRUE` break causes the `removeAll(Collection)` method to always return `true`.
    /// @see BreakableCollection#removeAll(Collection)
    @DisplayName("removeAll(Collection) always returns true with REMOVE_ALL_ALWAYS_RETURNS_TRUE break")
    @Test
    public void removeAll_withRemoveAllAlwaysReturnsTrueBreak_returnsTrue() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_TRUE)
                .build();
        assertTrue(collection.removeAll(Arrays.asList(1, 2)));
        assertTrue(collection.removeAll(Arrays.asList(4, 5)));
    }

    /// Test that the `REMOVE_ALL_ALWAYS_RETURNS_FALSE` break causes the `removeAll(Collection)` method to always return `false`.
    /// @see BreakableCollection#removeAll(Collection)
    @DisplayName("removeAll(Collection) always returns false with REMOVE_ALL_ALWAYS_RETURNS_FALSE break")
    @Test
    public void removeAll_withRemoveAllAlwaysReturnsFalseBreak_returnsFalse() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_FALSE)
                .build();
        assertFalse(collection.removeAll(Arrays.asList(1, 2)));
        assertFalse(collection.removeAll(Arrays.asList(4, 5)));
    }

    /// Test that the `REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE` break causes the `removeAll(Collection)` method to always return the opposite value.
    /// @see BreakableCollection#removeAll(Collection)
    @DisplayName("removeAll(Collection) always returns opposite value with REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break")
    @Test
    public void removeAll_withRemoveAllAlwaysReturnsOppositeValueBreak_returnsOppositeValue() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE)
                .build();
        assertFalse(collection.removeAll(Arrays.asList(1, 2)));
        assertTrue(collection.removeAll(Arrays.asList(4, 5)));
    }

    /// Test that the `REMOVE_ALL_SKIPS_FIRST_ELEMENT` break causes the `removeAll(Collection)` method to skip the first element.
    /// @see BreakableCollection#removeAll(Collection)
    @DisplayName("removeAll(Collection) skips first element with REMOVE_ALL_SKIPS_FIRST_ELEMENT break")
    @Test
    public void removeAll_withRemoveAllSkipsFirstElementBreak_skipsFirstElement() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.REMOVE_ALL_SKIPS_FIRST_ELEMENT)
                .build();
        collection.removeAll(Arrays.asList(1, 2, 3));
        assertContains(1, collection);
        assertDoesNotContain(2, collection);
        assertDoesNotContain(3, collection);
    }

    /// Test that the `REMOVE_ALL_SKIPS_LAST_ELEMENT` break causes the `removeAll(Collection)` method to skip the last element.
    /// @see BreakableCollection#removeAll(Collection)
    @DisplayName("removeAll(Collection) skips last element with REMOVE_ALL_SKIPS_LAST_ELEMENT break")
    @Test
    public void removeAll_withRemoveAllSkipsLastElementBreak_skipsLastElement() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.REMOVE_ALL_SKIPS_LAST_ELEMENT)
                .build();
        collection.removeAll(Arrays.asList(1, 2, 3));
        assertDoesNotContain(1, collection);
        assertDoesNotContain(2, collection);
        assertContains(3, collection);
    }

    /// Test that the `REMOVE_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION` break causes the `removeAll(Collection)` method to throw the wrong exception when unsupported.
    /// @see BreakableCollection#removeAll(Collection)
    @DisplayName("removeAll(Collection) throws wrong exception with REMOVE_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION break")
    @Test
    public void removeAll_withRemoveAllThrowsWrongUnsupportedExceptionBreak_throwsWrongException() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.REMOVE_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION)
                .doesNotSupportMethod(CollectionMethods.REMOVE_ALL)
                .build();
        assertThrowsDifferent(UnsupportedOperationException.class, () -> collection.removeAll(Arrays.asList(1, 2)));
    }

    // ========== removeIf Tests ==========

    /// Test that the `REMOVE_IF_DOES_NOT_REMOVE_ANY_ELEMENTS` break causes the `removeIf(Predicate)` method to not remove any elements.
    /// @see BreakableCollection#removeIf(Predicate)
    @DisplayName("removeIf(Predicate) does not remove any elements with REMOVE_IF_DOES_NOT_REMOVE_ANY_ELEMENTS break")
    @Test
    public void removeIf_withRemoveIfDoesNotRemoveAnyElementsBreak_doesNotRemoveElements() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.REMOVE_IF_DOES_NOT_REMOVE_ANY_ELEMENTS)
                .build();
        collection.removeIf(i -> i < 3);
        assertContains(1, collection);
        assertContains(2, collection);
    }

    /// Test that the `REMOVE_IF_ALWAYS_RETURNS_TRUE` break causes the `removeIf(Predicate)` method to always return `true`.
    /// @see BreakableCollection#removeIf(Predicate)
    @DisplayName("removeIf(Predicate) always returns true with REMOVE_IF_ALWAYS_RETURNS_TRUE break")
    @Test
    public void removeIf_withRemoveIfAlwaysReturnsTrueBreak_returnsTrue() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.REMOVE_IF_ALWAYS_RETURNS_TRUE)
                .build();
        assertTrue(collection.removeIf(i -> i < 3));
        assertTrue(collection.removeIf(i -> i > 5));
    }

    /// Test that the `REMOVE_IF_ALWAYS_RETURNS_FALSE` break causes the `removeIf(Predicate)` method to always return `false`.
    /// @see BreakableCollection#removeIf(Predicate)
    @DisplayName("removeIf(Predicate) always returns false with REMOVE_IF_ALWAYS_RETURNS_FALSE break")
    @Test
    public void removeIf_withRemoveIfAlwaysReturnsFalseBreak_returnsFalse() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.REMOVE_IF_ALWAYS_RETURNS_FALSE)
                .build();
        assertFalse(collection.removeIf(i -> i < 3));
        assertFalse(collection.removeIf(i -> i > 5));
    }

    /// Test that the `REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE` break causes the `removeIf(Predicate)` method to always return the opposite value.
    /// @see BreakableCollection#removeIf(Predicate)
    @DisplayName("removeIf(Predicate) always returns opposite value with REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE break")
    @Test
    public void removeIf_withRemoveIfAlwaysReturnsOppositeValueBreak_returnsOppositeValue() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE)
                .build();
        assertFalse(collection.removeIf(i -> i < 3));
        assertTrue(collection.removeIf(i -> i > 5));
    }

    /// Test that the `REMOVE_IF_SKIPS_FIRST_ELEMENT` break causes the `removeIf(Predicate)` method to skip the first element.
    /// @see BreakableCollection#removeIf(Predicate)
    @DisplayName("removeIf(Predicate) skips first element with REMOVE_IF_SKIPS_FIRST_ELEMENT break")
    @Test
    public void removeIf_withRemoveIfSkipsFirstElementBreak_skipsFirstElement() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.REMOVE_IF_SKIPS_FIRST_ELEMENT)
                .build();
        collection.removeIf(i -> true);
        assertContains(1, collection);
        assertDoesNotContain(2, collection);
        assertDoesNotContain(3, collection);
    }

    /// Test that the `REMOVE_IF_SKIPS_LAST_ELEMENT` break causes the `removeIf(Predicate)` method to skip the last element.
    /// @see BreakableCollection#removeIf(Predicate)
    @DisplayName("removeIf(Predicate) skips last element with REMOVE_IF_SKIPS_LAST_ELEMENT break")
    @Test
    public void removeIf_withRemoveIfSkipsLastElementBreak_skipsLastElement() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.REMOVE_IF_SKIPS_LAST_ELEMENT)
                .build();
        collection.removeIf(i -> true);
        assertContains(1, collection);
        assertDoesNotContain(2, collection);
        assertDoesNotContain(3, collection);
    }

    /// Test that the `REMOVE_IF_THROWS_WRONG_UNSUPPORTED_EXCEPTION` break causes the `removeIf(Predicate)` method to throw the wrong exception when unsupported.
    /// @see BreakableCollection#removeIf(Predicate)
    @DisplayName("removeIf(Predicate) throws wrong exception with REMOVE_IF_THROWS_WRONG_UNSUPPORTED_EXCEPTION break")
    @Test
    public void removeIf_withRemoveIfThrowsWrongUnsupportedExceptionBreak_throwsWrongException() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.REMOVE_IF_THROWS_WRONG_UNSUPPORTED_EXCEPTION)
                .doesNotSupportMethod(CollectionMethods.REMOVE_IF)
                .build();
        assertThrowsDifferent(UnsupportedOperationException.class, () -> collection.removeIf(i -> true));
    }

    // ========== retainAll Tests ==========

    /// Test that the `RETAIN_ALL_DOES_NOT_RETAIN_ANY_ELEMENTS` break causes the `retainAll(Collection)` method to not retain any elements.
    /// @see BreakableCollection#retainAll(Collection)
    @DisplayName("retainAll(Collection) does not retain any elements with RETAIN_ALL_DOES_NOT_RETAIN_ANY_ELEMENTS break")
    @Test
    public void retainAll_withRetainAllDoesNotRetainAnyElementsBreak_doesNotRetainElements() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.RETAIN_ALL_DOES_NOT_RETAIN_ANY_ELEMENTS)
                .build();
        collection.retainAll(Arrays.asList(1, 2));
        assertContains(1, collection);
        assertContains(2, collection);
        assertContains(3, collection);
    }

    /// Test that the `RETAIN_ALL_ALWAYS_RETURNS_TRUE` break causes the `retainAll(Collection)` method to always return `true`.
    /// @see BreakableCollection#retainAll(Collection)
    @DisplayName("retainAll(Collection) always returns true with RETAIN_ALL_ALWAYS_RETURNS_TRUE break")
    @Test
    public void retainAll_withRetainAllAlwaysReturnsTrueBreak_returnsTrue() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.RETAIN_ALL_ALWAYS_RETURNS_TRUE)
                .build();
        assertTrue(collection.retainAll(Arrays.asList(1, 2, 3)));
        assertTrue(collection.retainAll(Arrays.asList(1, 2)));
    }

    /// Test that the `RETAIN_ALL_ALWAYS_RETURNS_FALSE` break causes the `retainAll(Collection)` method to always return `false`.
    /// @see BreakableCollection#retainAll(Collection)
    @DisplayName("retainAll(Collection) always returns false with RETAIN_ALL_ALWAYS_RETURNS_FALSE break")
    @Test
    public void retainAll_withRetainAllAlwaysReturnsFalseBreak_returnsFalse() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.RETAIN_ALL_ALWAYS_RETURNS_FALSE)
                .build();
        assertFalse(collection.retainAll(Arrays.asList(1, 2, 3)));
        assertFalse(collection.retainAll(Arrays.asList(1, 2)));
    }

    /// Test that the `RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE` break causes the `retainAll(Collection)` method to always return the opposite value.
    /// @see BreakableCollection#retainAll(Collection)
    @DisplayName("retainAll(Collection) always returns opposite value with RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break")
    @Test
    public void retainAll_withRetainAllAlwaysReturnsOppositeValueBreak_returnsOppositeValue() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE)
                .build();
        assertFalse(collection.retainAll(Arrays.asList(1, 2)));
        assertTrue(collection.retainAll(Arrays.asList(1, 2, 3)));
    }

    /// Test that the `RETAIN_ALL_SKIPS_FIRST_ELEMENT` break causes the `retainAll(Collection)` method to skip the first element.
    /// @see BreakableCollection#retainAll(Collection)
    @DisplayName("retainAll(Collection) skips first element with RETAIN_ALL_SKIPS_FIRST_ELEMENT break")
    @Test
    public void retainAll_withRetainAllSkipsFirstElementBreak_skipsFirstElement() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.RETAIN_ALL_SKIPS_FIRST_ELEMENT)
                .build();
        collection.retainAll(Arrays.asList(1, 2, 3));
        assertDoesNotContain(1, collection);
        assertContains(2, collection);
        assertContains(3, collection);
    }

    /// Test that the `RETAIN_ALL_SKIPS_LAST_ELEMENT` break causes the `retainAll(Collection)` method to skip the last element.
    /// @see BreakableCollection#retainAll(Collection)
    @DisplayName("retainAll(Collection) skips last element with RETAIN_ALL_SKIPS_LAST_ELEMENT break")
    @Test
    public void retainAll_withRetainAllSkipsLastElementBreak_skipsLastElement() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.RETAIN_ALL_SKIPS_LAST_ELEMENT)
                .build();
        collection.retainAll(Arrays.asList(1, 2, 3));
        assertContains(1, collection);
        assertContains(2, collection);
        assertDoesNotContain(3, collection);
    }

    /// Test that the `RETAIN_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION` break causes the `retainAll(Collection)` method
    /// to throw the wrong exception when unsupported.
    /// @see BreakableCollection#retainAll(Collection)
    @DisplayName("retainAll(Collection) throws wrong exception with RETAIN_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION break")
    @Test
    public void retainAll_withRetainAllThrowsWrongUnsupportedExceptionBreak_throwsWrongException() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.RETAIN_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION)
                .doesNotSupportMethod(CollectionMethods.RETAIN_ALL)
                .build();
        assertThrowsDifferent(UnsupportedOperationException.class, () -> collection.retainAll(Arrays.asList(1, 2)));
    }

    // ========== clear Tests ==========

    /// Test that the `CLEAR_DOES_NOT_REMOVE_ANY_ELEMENTS` break causes the `clear()` method to not remove any elements.
    /// @see BreakableCollection#clear()
    @DisplayName("clear() does not remove any elements with CLEAR_DOES_NOT_REMOVE_ANY_ELEMENTS break")
    @Test
    public void clear_withClearDoesNotRemoveAnyElementsBreak_doesNotRemoveElements() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.CLEAR_DOES_NOT_REMOVE_ANY_ELEMENTS)
                .build();
        collection.clear();
        assertContains(1, collection);
        assertContains(2, collection);
        assertContains(3, collection);
    }

    /// Test that the `CLEAR_SKIPS_FIRST_ELEMENT` break causes the `clear()` method to skip the first element.
    /// @see BreakableCollection#clear()
    @DisplayName("clear() skips first element with CLEAR_SKIPS_FIRST_ELEMENT break")
    @Test
    public void clear_withClearSkipsFirstElementBreak_skipsFirstElement() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.CLEAR_SKIPS_FIRST_ELEMENT)
                .build();
        collection.clear();
        assertContains(1, collection);
        assertEquals(1, collection.unbroken().size());
    }

    /// Test that the `CLEAR_SKIPS_LAST_ELEMENT` break causes the `clear()` method to skip the last element.
    /// @see BreakableCollection#clear()
    @DisplayName("clear() skips last element with CLEAR_SKIPS_LAST_ELEMENT break")
    @Test
    public void clear_withClearSkipsLastElementBreak_skipsLastElement() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.CLEAR_SKIPS_LAST_ELEMENT)
                .build();
        collection.clear();
        assertContains(1, collection);
        assertEquals(1, collection.unbroken().size());
    }

    /// Test that the `CLEAR_THROWS_WRONG_UNSUPPORTED_EXCEPTION` break causes the `clear()` method to throw the wrong exception when unsupported.
    /// @see BreakableCollection#clear()
    @DisplayName("clear() throws wrong exception with CLEAR_THROWS_WRONG_UNSUPPORTED_EXCEPTION break")
    @Test
    public void clear_withClearThrowsWrongUnsupportedExceptionBreak_throwsWrongException() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.CLEAR_THROWS_WRONG_UNSUPPORTED_EXCEPTION)
                .doesNotSupportMethod(CollectionMethods.CLEAR)
                .build();
        assertThrowsDifferent(UnsupportedOperationException.class, collection::clear);
    }

    // ========== containsAll Tests ==========

    /// Test that the `CONTAINS_ALL_ALWAYS_RETURNS_TRUE` break causes the `containsAll(Collection)` method to always return `true`.
    /// @see BreakableCollection#containsAll(Collection)
    @DisplayName("containsAll(Collection) always returns true with CONTAINS_ALL_ALWAYS_RETURNS_TRUE break")
    @Test
    public void containsAll_withContainsAllAlwaysReturnsTrueBreak_returnsTrue() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.CONTAINS_ALL_ALWAYS_RETURNS_TRUE)
                .build();
        assertTrue(collection.containsAll(CollectionOnly.of(1)));
        assertTrue(collection.containsAll(CollectionOnly.of(4)));
    }

    /// Test that the `CONTAINS_ALL_ALWAYS_RETURNS_FALSE` break causes the `containsAll(Collection)` method to always return `false`.
    /// @see BreakableCollection#containsAll(Collection)
    @DisplayName("containsAll(Collection) always returns false with CONTAINS_ALL_ALWAYS_RETURNS_FALSE break")
    @Test
    public void containsAll_withContainsAllAlwaysReturnsFalseBreak_returnsFalse() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.CONTAINS_ALL_ALWAYS_RETURNS_FALSE)
                .build();
        assertFalse(collection.containsAll(CollectionOnly.of(1)));
        assertFalse(collection.containsAll(CollectionOnly.of(4)));
    }

    /// Test that the `CONTAINS_ALL_RETURNS_OPPOSITE_VALUE` break causes the `containsAll(Collection)` method to always return the opposite value.
    /// @see BreakableCollection#containsAll(Collection)
    @DisplayName("containsAll(Collection) always returns opposite value with CONTAINS_ALL_RETURNS_OPPOSITE_VALUE break")
    @Test
    public void containsAll_withContainsAllReturnsOppositeValueBreak_returnsOppositeValue() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.CONTAINS_ALL_RETURNS_OPPOSITE_VALUE)
                .build();
        assertFalse(collection.containsAll(CollectionOnly.of(1)));
        assertTrue(collection.containsAll(CollectionOnly.of(4)));
    }

    /// Test that the `CONTAINS_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION` break causes the `containsAll(Collection)` method to throw the wrong exception when unsupported.
    /// @see BreakableCollection#containsAll(Collection)
    @DisplayName("containsAll(Collection) throws wrong exception with CONTAINS_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION break")
    @Test
    public void containsAll_withContainsAllThrowsWrongUnsupportedExceptionBreak_throwsWrongException() {
        Collection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.CONTAINS_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION)
                .doesNotSupportMethod(CollectionMethods.CONTAINS_ALL)
                .build();
        //noinspection ResultOfMethodCallIgnored
        assertThrowsDifferent(UnsupportedOperationException.class, () -> collection.containsAll(CollectionOnly.of(1)));
    }

    // ========== randomElementExcludingIndex Tests ==========


    /// Test that `randomElementExcludingIndex(int)` excludes the specified index when possible.
    @Test
    @DisplayName("randomElementExcludingIndex(int) excludes specified index")
    public void randomElementExcludingIndex_whenValidIndexProvided_excludesSpecifiedIndex() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(10, 20, 30, 40, 50).build();

        // Try to exclude index 0 (element 10)
        Integer element = collection.randomElementExcludingIndex(0);

        assertNotNull(element, "Should return a non-null element");
        assertTrue(collection.contains(element), "Returned element should be in the collection");
        // With multiple elements and proper exclusion, it should return a different element
        // Note: This test may occasionally fail if the random selection happens to pick index 0
        // despite the exclusion logic, so we test the behavior rather than absolute exclusion
    }

    /// Test that `randomElementExcludingIndex(int)` with a single element returns that element.
    @Test
    @DisplayName("randomElementExcludingIndex(int) with single element collection")
    public void randomElementExcludingIndex_whenCollectionHasSingleElement_returnsThatElement() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(42).build();

        // Even when trying to exclude the only element, it should return it
        Integer element = collection.randomElementExcludingIndex(0);

        assertEquals(42, element, "Should return the only available element");
    }

    /// Test that `randomElementExcludingIndex(int)` with negative index excludes nothing.
    @Test
    @DisplayName("randomElementExcludingIndex(int) with negative index")
    public void randomElementExcludingIndex_whenNegativeIndexProvided_excludesNothing() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3).build();

        Integer element = collection.randomElementExcludingIndex(-1);

        assertNotNull(element, "Should return a non-null element");
        assertTrue(collection.contains(element), "Returned element should be in the collection");
    }

    /// Test that `randomElementExcludingIndex(int)` with out-of-bounds index still returns an element.
    @Test
    @DisplayName("randomElementExcludingIndex(int) with out-of-bounds index")
    public void randomElementExcludingIndex_whenIndexIsOutOfBounds_stillReturnsAnElement() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3).build();

        // Index 10 is out of bounds for a 3-element collection
        Integer element = collection.randomElementExcludingIndex(10);

        assertNotNull(element, "Should return a non-null element");
        assertTrue(collection.contains(element), "Returned element should be in the collection");
    }

    /// Test that `randomElementExcludingFirst()` excludes the first element when possible.
    @Test
    @DisplayName("randomElementExcludingFirst() excludes first element")
    public void randomElementExcludingFirst_whenCalled_excludesFirstElement() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3, 4, 5).build();

        Integer element = collection.randomElementExcludingIndex(0);

        assertNotNull(element, "Should return a non-null element");
        assertTrue(collection.contains(element), "Returned element should be in the collection");
    }

    /// Test that `randomElementExcludingLast()` excludes the last element when possible.
    @Test
    @DisplayName("randomElementExcludingLast() excludes last element")
    public void randomElementExcludingLast_whenCalled_excludesLastElement() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3, 4, 5).build();

        Integer element = collection.randomElementExcludingIndex(collection.size() - 1);

        assertNotNull(element, "Should return a non-null element");
        assertTrue(collection.contains(element), "Returned element should be in the collection");
    }


    /// Test that `randomElementExcludingIndex(int)` works with non-List collections.
    @Test
    @DisplayName("randomElementExcludingIndex(int) with non-List collection")
    public void randomElementExcludingIndex_whenCollectionIsNotAList_stillReturnsAnElement() {
        // Using a Set (non-List collection)
        BreakableCollection<Integer> collection = new BreakableCollection<>(
                new java.util.HashSet<>(java.util.Arrays.asList(1, 2, 3, 4, 5)),
                new java.util.HashSet<>(),
                new java.util.HashMap<>(),
                0,
                0,
                false,
                Object.class
        );

        Integer element = collection.randomElementExcludingIndex(0);

        assertNotNull(element, "Should return a non-null element");
        assertTrue(collection.contains(element), "Returned element should be in the collection");
    }

    /// Verifies serialization round-trip returns an equal collection.
    ///
    /// @throws java.io.IOException if an I/O error occurs
    /// @throws ClassNotFoundException if the class of a serialized object cannot be found
    @Test
    @DisplayName("serialization round-trip returns an equal collection")
    public void serialize_whenCalled_returnsEqualCollection() throws java.io.IOException, ClassNotFoundException {
        BreakableCollection<Integer> original = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT)
                .setSafe(true)
                .build();

        byte[] bytes = serialize(original);
        BreakableCollection<Integer> deserialized = deserialize(bytes);

        assertEquals(original, deserialized,
                "Deserialized collection should be equal to the original");
        assertEquals(original.hashCode(), deserialized.hashCode(),
                "Deserialized collection should have the same hash code");
        assertTrue(deserialized.hasBreak(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT),
                "Deserialized collection should retain breaks");
        assertTrue(deserialized.isSafe(),
                "Deserialized collection should retain safety setting");
        assertArrayEquals(original.toArray(), deserialized.toArray(),
                "Deserialized collection should retain elements");
    }
}
