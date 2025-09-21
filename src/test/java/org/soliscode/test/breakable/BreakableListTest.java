package org.soliscode.test.breakable;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.*;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.CollectionMethods;
import org.soliscode.test.contract.list.ListContract;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.provider.CollectionProvider;
import org.soliscode.test.provider.FunctionalCollectionProvider;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.assertions.collection.CollectionAssertions.*;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertEquals;

/// Tests for the [BreakableList] class. These tests determine if the breaks supported by this class
/// result in the behavior expected.
///
/// @author evanbergstrom
/// @since 1.0
/// @see BreakableList
public class BreakableListTest extends AbstractTest
        implements ListContract<Integer, BreakableList<Integer>>, WithIntegerElement {

    @Override
    public @NonNull CollectionProvider<Integer, BreakableList<Integer>> provider() {
        return FunctionalCollectionProvider.from(
            BreakableList::new,
            BreakableList::new,
            elements -> new BreakableList<>(new ArrayList<>(elements)),
            elementProvider()
        );
    }


    // ========== addAll(int, Collection) Tests ==========

    /// Test that the `ADD_ALL_AT_INDEX_DOES_NOT_ADD_ANY_ELEMENTS` break causes the `addAll` method to not add elements.
    /// @see BreakableList#addAll(int, Collection)
    @Test
    @DisplayName("Test the `addAll(int, Collection)` method with the ADD_ALL_AT_INDEX_DOES_NOT_ADD_ANY_ELEMENTS break")
    public void testAddAllAtIndexDoesNotAddElementsBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.ADD_ALL_AT_INDEX_DOES_NOT_ADD_ANY_ELEMENTS)
                .build();

        boolean result = list.addAll(1, Arrays.asList(4, 5));
        assertFalse(result);
        assertEquals(Arrays.asList(1, 2, 3), list);
        assertDoesNotContain(4, list);
        assertDoesNotContain(5, list);
    }

    /// Test that the `ADD_ALL_AT_INDEX_ADDS_TO_THE_END` break causes the `addAll` method to add to the end.
    /// @see BreakableList#addAll(int, Collection)
    @Test
    @DisplayName("Test the `addAll(int, Collection)` method with the ADD_ALL_AT_INDEX_ADDS_TO_THE_END break")
    public void testAddAllAtIndexAddsToEndBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.ADD_ALL_AT_INDEX_ADDS_TO_THE_END)
                .build();

        boolean result = list.addAll(1, Arrays.asList(4, 5));
        assertTrue(result);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), list);
        assertContains(4, list);
        assertContains(5, list);
    }

    /// Test that the `ADD_ALL_AT_INDEX_ALWAYS_RETURNS_TRUE` break causes the `addAll` method to always return true.
    /// @see BreakableList#addAll(int, Collection)
    @Test
    @DisplayName("Test the `addAll(int, Collection)` method with the ADD_ALL_AT_INDEX_ALWAYS_RETURNS_TRUE break")
    public void testAddAllAtIndexAlwaysReturnsTrueBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.ADD_ALL_AT_INDEX_ALWAYS_RETURNS_TRUE)
                .addBreak(BreakableList.ADD_ALL_AT_INDEX_DOES_NOT_ADD_ANY_ELEMENTS)
                .build();

        boolean result = list.addAll(1, Arrays.asList(4, 5));
        assertTrue(result); // Returns true despite not adding elements
        assertEquals(Arrays.asList(1, 2, 3), list);
    }

    /// Test that the `ADD_ALL_AT_INDEX_ALWAYS_RETURNS_FALSE` break causes the `addAll` method to always return false.
    /// @see BreakableList#addAll(int, Collection)
    @Test
    @DisplayName("Test the `addAll(int, Collection)` method with the ADD_ALL_AT_INDEX_ALWAYS_RETURNS_FALSE break")
    public void testAddAllAtIndexAlwaysReturnsFalseBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.ADD_ALL_AT_INDEX_ALWAYS_RETURNS_FALSE)
                .build();

        boolean result = list.addAll(1, Arrays.asList(4, 5));
        assertFalse(result); // Returns false despite adding elements
        assertEquals(Arrays.asList(1, 4, 5, 2, 3), list);
    }

    /// Test that the `ADD_ALL_AT_INDEX_ALWAYS_RETURNS_OPPOSITE_VALUE` break causes the `addAll` method to return the opposite.
    /// @see BreakableList#addAll(int, Collection)
    @Test
    @DisplayName("Test the `addAll(int, Collection)` method with the ADD_ALL_AT_INDEX_ALWAYS_RETURNS_OPPOSITE_VALUE break")
    public void testAddAllAtIndexAlwaysReturnsOppositeValueBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.ADD_ALL_AT_INDEX_ALWAYS_RETURNS_OPPOSITE_VALUE)
                .build();

        // Normal operation would return true, so break should return false
        boolean result = list.addAll(1, Arrays.asList(4, 5));
        assertFalse(result);
        assertEquals(Arrays.asList(1, 4, 5, 2, 3), list);
    }

    /// Test that the `ADD_ALL_AT_INDEX_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX` break throws the wrong exception.
    /// @see BreakableList#addAll(int, Collection)
    @Test
    @DisplayName("Test the `addAll(int, Collection)` method with the ADD_ALL_AT_INDEX_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX break")
    public void testAddAllAtIndexThrowsWrongExceptionOnBadIndexBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.ADD_ALL_AT_INDEX_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX)
                .build();

        assertThrows(RuntimeException.class, () -> {
            list.addAll(10, Arrays.asList(4, 5)); // Bad index should throw RuntimeException instead of IndexOutOfBoundsException
        });
    }

    // ========== add(int, Object) Tests ==========

    /// Test that the `ADD_AT_INDEX_DOES_NOT_ADD_THE_ELEMENT` break causes the `add` method to not add the element.
    /// @see BreakableList#add(int, Object)
    @Test
    @DisplayName("Test the `add(int, Object)` method with the ADD_AT_INDEX_DOES_NOT_ADD_THE_ELEMENT break")
    public void testAddAtIndexDoesNotAddElementBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.ADD_AT_INDEX_DOES_NOT_ADD_THE_ELEMENT)
                .build();

        list.add(1, 4);
        assertEquals(Arrays.asList(1, 2, 3), list);
        assertDoesNotContain(4, list);
    }

    /// Test that the `ADD_AT_INDEX_ADDS_AT_NEXT_POSITION` break causes the `add` method to add at the next position.
    /// @see BreakableList#add(int, Object)
    @Test
    @DisplayName("Test the `add(int, Object)` method with the ADD_AT_INDEX_ADDS_AT_NEXT_POSITION break")
    public void testAddAtIndexAddsAtNextPositionBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.ADD_AT_INDEX_ADDS_AT_NEXT_POSITION)
                .build();

        list.add(1, 4); // Should add at position 2 instead of 1
        assertEquals(Arrays.asList(1, 2, 4, 3), list);
        assertContains(4, list);
    }

    /// Test that the `ADD_AT_INDEX_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX` break throws the wrong exception.
    /// @see BreakableList#add(int, Object)
    @Test
    @DisplayName("Test the `add(int, Object)` method with the ADD_AT_INDEX_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX break")
    public void testAddAtIndexThrowsWrongExceptionOnBadIndexBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.ADD_AT_INDEX_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX)
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            list.add(10, 4); // Bad index should throw IllegalArgumentException instead of IndexOutOfBoundsException
        });
    }

    // ========== get(int) Tests ==========

    /// Test that the `GET_ALWAYS_RETURNS_NULL` break causes the `get` method to always return null.
    /// @see BreakableList#get(int)
    @Test
    @DisplayName("Test the `get(int)` method with the GET_ALWAYS_RETURNS_NULL break")
    public void testGetAlwaysReturnsNullBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.GET_ALWAYS_RETURNS_NULL)
                .build();

        assertNull(list.get(0));
        assertNull(list.get(1));
        assertNull(list.get(2));
    }

    /// Test that the `GET_ALWAYS_RETURNS_THE_FIRST_ELEMENT` break causes the `get` method to always return the first element.
    /// @see BreakableList#get(int)
    @Test
    @DisplayName("Test the `get(int)` method with the GET_ALWAYS_RETURNS_THE_FIRST_ELEMENT break")
    public void testGetAlwaysReturnsFirstElementBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.GET_ALWAYS_RETURNS_THE_FIRST_ELEMENT)
                .build();

        org.junit.jupiter.api.Assertions.assertEquals(1, list.get(0));
        org.junit.jupiter.api.Assertions.assertEquals(1, list.get(1));
        org.junit.jupiter.api.Assertions.assertEquals(1, list.get(2));
    }

    /// Test that the `GET_ALWAYS_RETURNS_THE_LAST_ELEMENT` break causes the `get` method to always return the last element.
    /// @see BreakableList#get(int)
    @Test
    @DisplayName("Test the `get(int)` method with the GET_ALWAYS_RETURNS_THE_LAST_ELEMENT break")
    public void testGetAlwaysReturnsLastElementBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.GET_ALWAYS_RETURNS_THE_LAST_ELEMENT)
                .build();

        org.junit.jupiter.api.Assertions.assertEquals(3, list.get(0));
        org.junit.jupiter.api.Assertions.assertEquals(3, list.get(1));
        org.junit.jupiter.api.Assertions.assertEquals(3, list.get(2));
    }

    /// Test that the `GET_RETURNS_THE_NEXT_ELEMENT` break causes the `get` method to return the next element.
    /// @see BreakableList#get(int)
    @Test
    @DisplayName("Test the `get(int)` method with the GET_RETURNS_THE_NEXT_ELEMENT break")
    public void testGetReturnsNextElementBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3, 4)
                .addBreak(BreakableList.GET_RETURNS_THE_NEXT_ELEMENT)
                .build();

        org.junit.jupiter.api.Assertions.assertEquals(2, list.get(0)); // Returns element at index 1
        org.junit.jupiter.api.Assertions.assertEquals(3, list.get(1)); // Returns element at index 2
        org.junit.jupiter.api.Assertions.assertEquals(4, list.get(2)); // Returns element at index 3
    }

    /// Test that the `GET_RETURNS_THE_PREVIOUS_ELEMENT` break causes the `get` method to return the previous element.
    /// @see BreakableList#get(int)
    @Test
    @DisplayName("Test the `get(int)` method with the GET_RETURNS_THE_PREVIOUS_ELEMENT break")
    public void testGetReturnsPreviousElementBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3, 4)
                .addBreak(BreakableList.GET_RETURNS_THE_PREVIOUS_ELEMENT)
                .build();

        org.junit.jupiter.api.Assertions.assertEquals(1, list.get(1)); // Returns element at index 0
        org.junit.jupiter.api.Assertions.assertEquals(2, list.get(2)); // Returns element at index 1
        org.junit.jupiter.api.Assertions.assertEquals(3, list.get(3)); // Returns element at index 2
    }

    /// Test that the `GET_RETURNS_NULL_ON_BAD_INDEX` break causes the `get` method to return null on bad index.
    /// @see BreakableList#get(int)
    @Test
    @DisplayName("Test the `get(int)` method with the GET_RETURNS_NULL_ON_BAD_INDEX break")
    public void testGetReturnsNullOnBadIndexBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.GET_RETURNS_NULL_ON_BAD_INDEX)
                .build();

        assertNull(list.get(10)); // Should return null instead of throwing IndexOutOfBoundsException
        assertNull(list.get(-1)); // Should return null instead of throwing IndexOutOfBoundsException
    }

    /// Test that the `GET_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX` break causes the `get` method to throw wrong exception.
    /// @see BreakableList#get(int)
    @Test
    @DisplayName("Test the `get(int)` method with the GET_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX break")
    public void testGetThrowsWrongExceptionOnBadIndexBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.GET_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX)
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            list.get(10); // Should throw IllegalArgumentException instead of IndexOutOfBoundsException
        });
    }

    // ========== set(int, Object) Tests ==========

    /// Test that the `SET_DOES_NOT_CHANGE_THE_ELEMENT` break causes the `set` method to not change the element.
    /// @see BreakableList#set(int, Object)
    @Test
    @DisplayName("Test the `set(int, Object)` method with the SET_DOES_NOT_CHANGE_THE_ELEMENT break")
    public void testSetDoesNotChangeElementBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.SET_DOES_NOT_CHANGE_THE_ELEMENT)
                .build();

        Integer oldValue = list.set(1, 99);
        org.junit.jupiter.api.Assertions.assertEquals(2, oldValue); // Returns old value
        assertEquals(Arrays.asList(1, 2, 3), list); // List unchanged
    }

    /// Test that the `SET_ALWAYS_RETURNS_NULL` break causes the `set` method to always return null.
    /// @see BreakableList#set(int, Object)
    @Test
    @DisplayName("Test the `set(int, Object)` method with the SET_ALWAYS_RETURNS_NULL break")
    public void testSetAlwaysReturnsNullBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.SET_ALWAYS_RETURNS_NULL)
                .build();

        Integer oldValue = list.set(1, 99);
        assertNull(oldValue); // Returns null instead of old value
        assertEquals(Arrays.asList(1, 99, 3), list); // Element was changed
    }

    /// Test that the `SET_CHANGES_THE_NEXT_ELEMENT` break causes the `set` method to change the next element.
    /// @see BreakableList#set(int, Object)
    @Test
    @DisplayName("Test the `set(int, Object)` method with the SET_CHANGES_THE_NEXT_ELEMENT break")
    public void testSetChangesNextElementBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3, 4)
                .addBreak(BreakableList.SET_CHANGES_THE_NEXT_ELEMENT)
                .build();

        Integer oldValue = list.set(1, 99); // Should change element at index 2
        org.junit.jupiter.api.Assertions.assertEquals(3, oldValue); // Returns old value from index 2
        assertEquals(Arrays.asList(1, 2, 99, 4), list); // Element at index 2 changed
    }

    /// Test that the `SET_CHANGES_THE_PREVIOUS_ELEMENT` break causes the `set` method to change the previous element.
    /// @see BreakableList#set(int, Object)
    @Test
    @DisplayName("Test the `set(int, Object)` method with the SET_CHANGES_THE_PREVIOUS_ELEMENT break")
    public void testSetChangesPreviousElementBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3, 4)
                .addBreak(BreakableList.SET_CHANGES_THE_PREVIOUS_ELEMENT)
                .build();

        Integer oldValue = list.set(2, 99); // Should change element at index 1
        org.junit.jupiter.api.Assertions.assertEquals(2, oldValue); // Returns old value from index 1
        assertEquals(Arrays.asList(1, 99, 3, 4), list); // Element at index 1 changed
    }

    /// Test that the `SET_RETURNS_NULL_ON_BAD_INDEX` break causes the `set` method to return null on bad index.
    /// @see BreakableList#set(int, Object)
    @Test
    @DisplayName("Test the `set(int, Object)` method with the SET_RETURNS_NULL_ON_BAD_INDEX break")
    public void testSetReturnsNullOnBadIndexBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.SET_RETURNS_NULL_ON_BAD_INDEX)
                .build();

        Integer result = list.set(10, 99);
        assertNull(result); // Should return null instead of throwing IndexOutOfBoundsException
    }

    /// Test that the `SET_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX` break causes the `set` method to throw wrong exception.
    /// @see BreakableList#set(int, Object)
    @Test
    @DisplayName("Test the `set(int, Object)` method with the SET_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX break")
    public void testSetThrowsWrongExceptionOnBadIndexBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.SET_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX)
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            list.set(10, 99); // Should throw IllegalArgumentException instead of IndexOutOfBoundsException
        });
    }

    // ========== remove(int) Tests ==========

    /// Test that the `REMOVE_AT_INDEX_DOES_NOT_REMOVE_THE_ELEMENT` break causes the `remove` method to not remove.
    /// @see BreakableList#remove(int)
    @Test
    @DisplayName("Test the `remove(int)` method with the REMOVE_AT_INDEX_DOES_NOT_REMOVE_THE_ELEMENT break")
    public void testRemoveAtIndexDoesNotRemoveElementBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.REMOVE_AT_INDEX_DOES_NOT_REMOVE_THE_ELEMENT)
                .build();

        Integer removed = list.remove(1);
        org.junit.jupiter.api.Assertions.assertEquals(2, removed); // Returns element but doesn't remove
        assertEquals(Arrays.asList(1, 2, 3), list); // List unchanged
    }

    /// Test that the `REMOVE_AT_INDEX_REMOVES_THE_NEXT_ELEMENT` break causes the `remove` method to remove next element.
    /// @see BreakableList#remove(int)
    @Test
    @DisplayName("Test the `remove(int)` method with the REMOVE_AT_INDEX_REMOVES_THE_NEXT_ELEMENT break")
    public void testRemoveAtIndexRemovesNextElementBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3, 4)
                .addBreak(BreakableList.REMOVE_AT_INDEX_REMOVES_THE_NEXT_ELEMENT)
                .build();

        Integer removed = list.remove(1); // Should remove element at index 2
        org.junit.jupiter.api.Assertions.assertEquals(3, removed); // Returns removed element
        assertEquals(Arrays.asList(1, 2, 4), list); // Element at index 2 removed
    }

    /// Test that the `REMOVE_AT_INDEX_REMOVES_THE_PREVIOUS_ELEMENT` break causes the `remove` method to remove previous element.
    /// @see BreakableList#remove(int)
    @Test
    @DisplayName("Test the `remove(int)` method with the REMOVE_AT_INDEX_REMOVES_THE_PREVIOUS_ELEMENT break")
    public void testRemoveAtIndexRemovesPreviousElementBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3, 4)
                .addBreak(BreakableList.REMOVE_AT_INDEX_REMOVES_THE_PREVIOUS_ELEMENT)
                .build();

        Integer removed = list.remove(2); // Should remove element at index 1
        org.junit.jupiter.api.Assertions.assertEquals(2, removed); // Returns removed element
        assertEquals(Arrays.asList(1, 3, 4), list); // Element at index 1 removed
    }

    /// Test that the `REMOVE_AT_INDEX_ALWAYS_RETURNS_NULL` break causes the `remove` method to always return null.
    /// @see BreakableList#remove(int)
    @Test
    @DisplayName("Test the `remove(int)` method with the REMOVE_AT_INDEX_ALWAYS_RETURNS_NULL break")
    public void testRemoveAtIndexAlwaysReturnsNullBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.REMOVE_AT_INDEX_ALWAYS_RETURNS_NULL)
                .build();

        Integer removed = list.remove(1);
        assertNull(removed); // Returns null instead of removed element
        assertEquals(Arrays.asList(1, 3), list); // Element was removed
    }

    /// Test that the `REMOVE_AT_INDEX_RETURNS_NULL_ON_BAD_INDEX` break causes the `remove` method to return null on bad index.
    /// @see BreakableList#remove(int)
    @Test
    @DisplayName("Test the `remove(int)` method with the REMOVE_AT_INDEX_RETURNS_NULL_ON_BAD_INDEX break")
    public void testRemoveAtIndexReturnsNullOnBadIndexBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.REMOVE_AT_INDEX_RETURNS_NULL_ON_BAD_INDEX)
                .build();

        Integer result = list.remove(10);
        assertNull(result); // Should return null instead of throwing IndexOutOfBoundsException
    }

    /// Test that the `REMOVE_AT_INDEX_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX` break causes the `remove` method to throw wrong exception.
    /// @see BreakableList#remove(int)
    @Test
    @DisplayName("Test the `remove(int)` method with the REMOVE_AT_INDEX_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX break")
    public void testRemoveAtIndexThrowsWrongExceptionOnBadIndexBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.REMOVE_AT_INDEX_THROWS_WRONG_EXCEPTION_ON_BAD_INDEX)
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            list.remove(10); // Should throw IllegalArgumentException instead of IndexOutOfBoundsException
        });
    }

    // ========== replaceAll(UnaryOperator) Tests ==========

    /// Test that the `REPLACE_ALL_DOES_NOT_REPLACE_ELEMENTS` break causes the `replaceAll` method to not replace elements.
    /// @see BreakableList#replaceAll(java.util.function.UnaryOperator)
    @Test
    @DisplayName("Test the `replaceAll(UnaryOperator)` method with the REPLACE_ALL_DOES_NOT_REPLACE_ELEMENTS break")
    public void testReplaceAllDoesNotReplaceElementsBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.REPLACE_ALL_DOES_NOT_REPLACE_ELEMENTS)
                .build();

        list.replaceAll(x -> x * 10);
        assertEquals(Arrays.asList(1, 2, 3), list); // No elements replaced
    }

    /// Test that the `REPLACE_ALL_SKIPS_FIRST_ELEMENT` break causes the `replaceAll` method to skip the first element.
    /// @see BreakableList#replaceAll(java.util.function.UnaryOperator)
    @Test
    @DisplayName("Test the `replaceAll(UnaryOperator)` method with the REPLACE_ALL_SKIPS_FIRST_ELEMENT break")
    public void testReplaceAllSkipsFirstElementBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.REPLACE_ALL_SKIPS_FIRST_ELEMENT)
                .build();

        list.replaceAll(x -> x * 10);
        assertEquals(Arrays.asList(1, 20, 30), list); // First element not replaced
    }

    /// Test that the `REPLACE_ALL_SKIPS_LAST_ELEMENT` break causes the `replaceAll` method to skip the last element.
    /// @see BreakableList#replaceAll(java.util.function.UnaryOperator)
    @Test
    @DisplayName("Test the `replaceAll(UnaryOperator)` method with the REPLACE_ALL_SKIPS_LAST_ELEMENT break")
    public void testReplaceAllSkipsLastElementBreak() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.REPLACE_ALL_SKIPS_LAST_ELEMENT)
                .build();

        list.replaceAll(x -> x * 10);
        assertEquals(Arrays.asList(10, 20, 3), list); // Last element not replaced
    }

    // ========== sort(Comparator) Tests ==========

    /// Test that the `SORT_DOES_NOT_SORT_THE_ELEMENTS` break causes the `sort` method to not sort elements.
    /// @see BreakableList#sort(Comparator)
    @Test
    @DisplayName("Test the `sort(Comparator)` method with the SORT_DOES_NOT_SORT_THE_ELEMENTS break")
    public void testSortDoesNotSortElementsBreak() {
        BreakableList<Integer> list = Breakables.buildList(3, 1, 2)
                .addBreak(BreakableList.SORT_DOES_NOT_SORT_THE_ELEMENTS)
                .build();

        list.sort(Integer::compareTo);
        assertEquals(Arrays.asList(3, 1, 2), list); // No sorting occurred
    }

    /// Test that the `SORT_REVERSES_THE_ORDER` break causes the `sort` method to sort in reverse order.
    /// @see BreakableList#sort(Comparator)
    @Test
    @DisplayName("Test the `sort(Comparator)` method with the SORT_REVERSES_THE_ORDER break")
    public void testSortReversesOrderBreak() {
        BreakableList<Integer> list = Breakables.buildList(3, 1, 2)
                .addBreak(BreakableList.SORT_REVERSES_THE_ORDER)
                .build();

        list.sort(Integer::compareTo);
        assertEquals(Arrays.asList(3, 2, 1), list); // Sorted in reverse order
    }

    /// Test that the `SORT_THROWS_ON_NULL_ARGUMENT` break causes the `sort` method to throw on null argument.
    /// @see BreakableList#sort(Comparator)
    @Test
    @DisplayName("Test the `sort(Comparator)` method with the SORT_THROWS_ON_NULL_ARGUMENT break")
    public void testSortThrowsOnNullArgumentBreak() {
        BreakableList<Integer> list = Breakables.buildList(3, 1, 2)
                .addBreak(BreakableList.SORT_THROWS_ON_NULL_ARGUMENT)
                .build();

        assertThrows(NullPointerException.class, () -> {
            list.sort(null); // Should throw NPE even though null is normally allowed
        });
    }

    // ========== Combined Breaks Tests ==========

    /// Test multiple breaks working together on get/set operations.
    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    @Test
    @DisplayName("Test combined breaks on get/set operations")
    public void testCombinedGetSetBreaks() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3, 4)
                .addBreak(BreakableList.GET_RETURNS_THE_NEXT_ELEMENT)
                .addBreak(BreakableList.SET_ALWAYS_RETURNS_NULL)
                .build();

        org.junit.jupiter.api.Assertions.assertEquals(2, list.get(0)); // Get next element
        Integer result = list.set(1, 99);
        assertNull(result); // Set returns null
        assertEquals(Arrays.asList(1, 99, 3, 4), list);
    }

    /// Test multiple breaks working together on add/remove operations.
    @Test
    @DisplayName("Test combined breaks on add/remove operations")
    public void testCombinedAddRemoveBreaks() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3, 4)
                .addBreak(BreakableList.ADD_AT_INDEX_DOES_NOT_ADD_THE_ELEMENT)
                .addBreak(BreakableList.REMOVE_AT_INDEX_ALWAYS_RETURNS_NULL)
                .build();

        list.add(1, 99); // Should not add
        assertEquals(Arrays.asList(1, 2, 3, 4), list);

        Integer removed = list.remove(1); // Should remove but return null
        assertNull(removed);
        assertEquals(Arrays.asList(1, 3, 4), list);
    }

    /// Test multiple breaks working together on addAll operations.
    @Test
    @DisplayName("Test combined breaks on addAll operations")
    public void testCombinedAddAllBreaks() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.ADD_ALL_AT_INDEX_ADDS_TO_THE_END)
                .addBreak(BreakableList.ADD_ALL_AT_INDEX_ALWAYS_RETURNS_FALSE)
                .build();

        boolean result = list.addAll(1, Arrays.asList(4, 5)); // Should add to end but return false
        assertFalse(result);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), list);
    }

    // ========== Edge Case Tests ==========

    /// Test operations on empty lists with breaks.
    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    @Test
    @DisplayName("Test breaks on empty list")
    public void testBreaksOnEmptyList() {
        BreakableList<Integer> list = Breakables.<Integer>buildList()
                .addBreak(BreakableList.GET_RETURNS_NULL_ON_BAD_INDEX)
                .addBreak(BreakableList.SORT_DOES_NOT_SORT_THE_ELEMENTS)
                .build();

        assertNull(list.get(0)); // Should return null on empty list
        list.sort(Integer::compareTo); // Should do nothing on empty list
        assertEquals(Collections.emptyList(), list);
    }

    /// Test that inherited Collection and SequencedCollection breaks still work.
    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    @Test
    @DisplayName("Test inherited Collection and SequencedCollection breaks")
    public void testInheritedBreaks() {
        BreakableList<Integer> list = new BreakableList<>(new ArrayList<>(Arrays.asList(1, 2, 3)),
                Set.of(BreakableCollection.SIZE_ALWAYS_RETURNS_ZERO,
                       BreakableSequencedCollection.GET_FIRST_RETURNS_NULL), 0);

        org.junit.jupiter.api.Assertions.assertEquals(0, list.size()); // From BreakableCollection
        assertNull(list.getFirst()); // From BreakableSequencedCollection

        // List methods should still work normally
        org.junit.jupiter.api.Assertions.assertEquals(1, list.get(0));
    }

    /// Test builder functionality.
    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    @Test
    @DisplayName("Test builder pattern")
    public void testBuilder() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.GET_ALWAYS_RETURNS_NULL)
                .addBreak(BreakableList.SET_CHANGES_THE_NEXT_ELEMENT)
                .build();

        assertNull(list.get(0));
        Integer oldValue = list.set(1, 99);
        Assertions.assertEquals(3, oldValue);
        assertEquals(Arrays.asList(1, 2, 99), list); // Set had no effect
    }

    /// Test builder functionality.
    @Test
    @DisplayName("Test builder for empty list")
    public void testBuilderForEmptyList() {
        BreakableList<Integer> list = Breakables.buildList(Integer.class)
                .addBreak(BreakableList.GET_ALWAYS_RETURNS_NULL)
                .addBreak(BreakableList.SET_CHANGES_THE_NEXT_ELEMENT)
                .build();
        assertIsEmpty(list);
    }

    /// Test copy constructor.
    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    @Test
    @DisplayName("Test copy constructor")
    public void testCopyConstructor() {
        BreakableList<Integer> original = Breakables.buildList(1, 2, 3)
                .addBreak(BreakableList.GET_ALWAYS_RETURNS_NULL)
                .build();

        BreakableList<Integer> copy = new BreakableList<>(original);

        // Copy should have the same elements but different behavior (no breaks copied)
        assertEquals(Arrays.asList(1, 2, 3), copy);
        org.junit.jupiter.api.Assertions.assertEquals(1, copy.get(0)); // No break, should return actual element
        assertNull(original.get(0)); // Still broken
    }

    /// Test unsupported operations.
    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    @Test
    @DisplayName("Test unsupported operations")
    public void testUnsupportedOperations() {
        BreakableList<Integer> list = new BreakableList<>(new ArrayList<>(Arrays.asList(1, 2, 3)),
                Collections.emptySet(), 0);
        list.doesNotSupportMethod(CollectionMethods.Get);
        list.doesNotSupportMethod(CollectionMethods.Set);

        assertThrows(UnsupportedOperationException.class, () -> list.get(0));
        assertThrows(UnsupportedOperationException.class, () -> list.set(0, 99));
    }

    /// Test boundary conditions for index-based operations.
    @Test
    @DisplayName("Test boundary conditions")
    public void testBoundaryConditions() {
        BreakableList<Integer> list = Breakables.buildList(1, 2, 3).build();

        // Test normal boundary conditions
        org.junit.jupiter.api.Assertions.assertEquals(1, list.get(0));
        org.junit.jupiter.api.Assertions.assertEquals(3, list.get(2));

        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(3));
    }

    /// Test with null elements in the list.
    @Test
    @DisplayName("Test with null elements")
    public void testWithNullElements() {
        BreakableList<Integer> list = Breakables.buildList(1, null, 3)
                .addBreak(BreakableList.GET_ALWAYS_RETURNS_THE_FIRST_ELEMENT)
                .build();

        org.junit.jupiter.api.Assertions.assertEquals(1, list.get(0));
        org.junit.jupiter.api.Assertions.assertEquals(1, list.get(1)); // Should return first element, not null
        org.junit.jupiter.api.Assertions.assertEquals(1, list.get(2));
    }
}