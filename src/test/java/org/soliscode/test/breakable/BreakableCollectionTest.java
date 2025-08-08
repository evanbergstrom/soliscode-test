package org.soliscode.test.breakable;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.collection.CollectionContract;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.interfaces.CollectionOnly;
import org.soliscode.test.provider.CollectionProvider;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.assertions.ArrayAssertions.assertLengthEquals;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContains;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertDoesNotContain;

/// Tests for the [BreakableCollection] class. These tests determine if the breaks supported by this class result in the
/// behavior expected.
///
/// @author evanbergstrom
/// @since 1.0
/// @see BreakableCollection
public class BreakableCollectionTest extends AbstractTest
        implements CollectionContract<Integer, BreakableCollection<Integer>>, WithIntegerElement {

    @Override
    public  @NotNull CollectionProvider<Integer, BreakableCollection<Integer>> provider() {
        return BreakableCollection.collectionProvider(elementProvider());
    }

    /// Test that the `SIZE_ALWAYS_RETURNS_ZERO` break causes the `size` method to return zero.
    /// @see BreakableCollection#size()
    @Test
    @DisplayName("Test the `size` method with the SIZE_ALWAYS_RETURNS_ZERO break")
    public void testSizeWithSizeAlwaysReturnsZeroBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.SIZE_ALWAYS_RETURNS_ZERO)
                .build();

        assertEquals(0, collection.size());
    }

    /// Test that the `SIZE_ALWAYS_RETURNS_CONSTANT_VALUE` break causes the `size` method to return a constant value.
    /// @see BreakableCollection#size()
    @Test
    @DisplayName("Test the `size` method with the SIZE_ALWAYS_RETURNS_CONSTANT_VALUE break")
    public void testSizeWithSizeAlwaysReturnsConstantValueBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.SIZE_ALWAYS_RETURNS_CONSTANT_VALUE)
                .build();
        assertNotEquals(3, collection.size());
    }

    /// Test that the `IS_EMPTY_ALWAYS_RETURNS_TRUE` break causes the `isEmpty` method to always return `true`.
    /// @see BreakableCollection#isEmpty()
    @Test
    @DisplayName("Test the `size` method with the IS_EMPTY_ALWAYS_RETURNS_TRUE break")
    public void testIsEmptyAlwaysReturnsTrueBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(Integer.class)
                .addBreak(BreakableCollection.IS_EMPTY_ALWAYS_RETURNS_TRUE)
                .build();
        assertTrue(collection.isEmpty());

        collection.add(1);
        assertTrue(collection.isEmpty());
    }

    /// Test that the `IS_EMPTY_ALWAYS_RETURNS_FALSE` break causes the `isEmpty` method to always return `false`.
    /// @see BreakableCollection#isEmpty()
    @Test
    @DisplayName("Test the `size` method with the IS_EMPTY_ALWAYS_RETURNS_FALSE break")
    public void testIsEmptyAlwaysReturnsFalseBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(Integer.class)
                .addBreak(BreakableCollection.IS_EMPTY_ALWAYS_RETURNS_FALSE)
                .build();
        assertFalse(collection.isEmpty());

        collection.add(1);
        assertFalse(collection.isEmpty());
    }

    /// Test that the `IS_EMPTY_RETURNS_OPPOSITE_VALUE` break causes the `isEmpty` method to always return the opposite
    /// value.
    /// @see BreakableCollection#isEmpty()
    @Test
    @DisplayName("Test the `size` method with the IS_EMPTY_RETURNS_OPPOSITE_VALUE break")
    public void testIsEmptyAlwaysReturnsOppositeValueBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(Integer.class)
                .addBreak(BreakableCollection.IS_EMPTY_RETURNS_OPPOSITE_VALUE)
                .build();
        assertFalse(collection.isEmpty());

        collection.add(1);
        assertTrue(collection.isEmpty());
    }

    /// Test that the `CONTAINS_ALWAYS_RETURNS_TRUE` break causes the `contains` method to always return `true`.
    /// @see BreakableCollection#contains(Object)
    @Test
    @DisplayName("Test the `contains(Object)` method with the CONTAINS_ALWAYS_RETURNS_TRUE break")
    public void testContainsAlwaysReturnsTrueBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.CONTAINS_ALWAYS_RETURNS_TRUE)
                .build();
        assertTrue(collection.contains(1));
        assertTrue(collection.contains(4));
    }

    /// Test that the `CONTAINS_ALWAYS_RETURNS_FALSE` break causes the `contains` method to always return `true`.
    /// @see BreakableCollection#contains(Object)
    @Test
    @DisplayName("Test the `contains(Object)` method with the CONTAINS_ALWAYS_RETURNS_FALSE break")
    public void testContainsAlwaysReturnsFalseBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.CONTAINS_ALWAYS_RETURNS_FALSE)
                .build();
        assertFalse(collection.contains(1));
        assertFalse(collection.contains(4));
    }

    /// Test that the `CONTAINS_RETURNS_OPPOSITE_VALUE` break causes the `contains` method to always return the opposite
    /// value.
    /// @see BreakableCollection#contains(Object)
    @Test
    @DisplayName("Test the `contains(Object)` method with the CONTAINS_RETURNS_OPPOSITE_VALUE break")
    public void testContainsAlwaysReturnsOppositeValueBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.CONTAINS_RETURNS_OPPOSITE_VALUE)
                .build();
        assertFalse(collection.contains(1));
        assertTrue(collection.contains(4));
    }

    /// Test that the `TO_ARRAY_RETURNS_NULL` break causes the `toArray()` method to always return `null`.`
    /// value.
    /// @see BreakableCollection#toArray()
    @Test
    @DisplayName("Test the `toArray()` method with the TO_ARRAY_RETURNS_NULL break")
    @SuppressWarnings("DataFlowIssue")
    public void testToArrayAlwaysReturnsNullBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.TO_ARRAY_RETURNS_NULL)
                .build();
        assertNull(collection.toArray());
    }

    /// Test that the `TO_ARRAY_RETURNS_EMPTY_ARRAY` break causes the `toArray()` method to always returns an empty
    /// array.
    /// @see BreakableCollection#toArray()
    @Test
    @DisplayName("Test the `toArray()` method with the TO_ARRAY_RETURNS_EMPTY_ARRAY break")
    public void testToArrayAlwaysReturnsEmptyArrayBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.TO_ARRAY_RETURNS_EMPTY_ARRAY)
                .build();
        Object[] array = collection.toArray();
        assertLengthEquals(0, array);
    }

    /// Test that the `TO_ARRAY_MISSING_FIRST_ELEMENT` break causes the `toArray()` method to return an array that is
    /// missing the first element.
    /// @see BreakableCollection#toArray()
    @Test
    @DisplayName("Test the `toArray()` method with the TO_ARRAY_MISSING_FIRST_ELEMENT break")
    public void testToArrayMissingFirstElementBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.TO_ARRAY_MISSING_FIRST_ELEMENT)
                .build();
        Object[] array = collection.toArray();
        assertArrayEquals(new Object[]{2, 3}, array);
    }

    /// Test that the `TO_ARRAY_MISSING_LAST_ELEMENT` break causes the `toArray()` method to return an array that is
    /// missing the first element.
    /// @see BreakableCollection#toArray()
    @Test
    @DisplayName("Test the `toArray()` method with the TO_ARRAY_MISSING_LAST_ELEMENT break")
    public void testToArrayMissingLastElementBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.TO_ARRAY_MISSING_LAST_ELEMENT)
                .build();
        Object[] array = collection.toArray();
        assertArrayEquals(new Object[]{1, 2}, array);
    }

    /// Test that the `TO_ARRAY_STORE_RETURNS_NULL` break causes the `toArray(Object[])` method to always return `null`.`
    /// value.
    /// @see BreakableCollection#toArray(Object[])
    @Test
    @DisplayName("Test the `toArray(Object[])` method with the TO_ARRAY_STORE_RETURNS_NULL break")
    @SuppressWarnings("DataFlowIssue")
    public void testToArrayStoreAlwaysReturnsNullBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.TO_ARRAY_STORE_RETURNS_NULL)
                .build();
        assertNull(collection.toArray(new Object[]{0, 0, 0}));
    }

    /// Test that the `TO_ARRAY_STORE_RETURNS_EMPTY_ARRAY` break causes the `toArray(Object[])` method to always returns an empty
    /// array.
    /// @see BreakableCollection#toArray(Object[])
    @Test
    @DisplayName("Test the `toArray(Object[])` method with the TO_ARRAY_STORE_RETURNS_EMPTY_ARRAY break")
    public void testToArrayStoreAlwaysReturnsEmptyArrayBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.TO_ARRAY_STORE_RETURNS_EMPTY_ARRAY)
                .build();
        assertArrayEquals(new Object[]{null, null, null}, collection.toArray(new Object[]{1, 2, 3}));
    }

    /// Test that the `TO_ARRAY_STORE_MISSING_FIRST_ELEMENT` break causes the `toArray()` method to return an array that is
    /// missing the first element.
    /// @see BreakableCollection#toArray(Object[])
    @Test
    @DisplayName("Test the `toArray(Object[])` method with the TO_ARRAY_STORE_MISSING_FIRST_ELEMENT break")
    public void testToArrayStoreMissingFirstElementBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.TO_ARRAY_STORE_MISSING_FIRST_ELEMENT)
                .build();
        Object[] array = new Object[]{0, 0, 0};
        collection.toArray(array);
        assertArrayEquals(new Object[]{2, 3, null}, array);
    }

    /// Test that the `TO_ARRAY_STORE_MISSING_LAST_ELEMENT` break causes the `toArray(Object[])` method to return an array that is
    /// missing the last element.
    /// @see BreakableCollection#toArray(Object[])
    @Test
    @DisplayName("Test the `toArray(Object[])` method with the TO_ARRAY_STORE_MISSING_LAST_ELEMENT break")
    public void testToArrayStoreMissingLastElementBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.TO_ARRAY_STORE_MISSING_LAST_ELEMENT)
                .build();
        Object[] array = new Object[]{0, 0, 0};
        collection.toArray(array);
        assertArrayEquals(new Object[]{1, 2, null}, array);
    }

    /// Test that the `TO_ARRAY_STORE_DOES_NOT_COPY_ELEMENTS` break causes the `toArray(Object[])` method to return an array that is
    /// missing the last element.
    /// @see BreakableCollection#toArray()
    @Test
    @DisplayName("Test the `toArray(Object[])` method with the TO_ARRAY_STORE_DOES_NOT_COPY_ELEMENTS break")
    public void testToArrayStoreDoesNotCopyElementsBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.TO_ARRAY_STORE_DOES_NOT_COPY_ELEMENTS)
                .build();
        Object[] array = new Object[]{0, 0, 0};
        collection.toArray(array);
        assertArrayEquals(new Object[]{0, 0, 0}, array);
    }

    /// Test that the `ADD_DOES_NOT_ADD_ELEMENT` break causes the `add(Object)` method to not add the element.
    /// @see BreakableCollection#add(Object)
    @Test
    @DisplayName("test the `add()` method with the ADD_DOES_NOT_ADD_ELEMENT break")
    public void testAddDoesNotAddElementBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(Integer.class)
                .addBreak(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT)
                .build();
        collection.add(1);
        assertDoesNotContain(1, collection);
    }

    /// Test that the `ADD_ALWAYS_RETURNS_TRUE` break causes the `add(Object)` method to always return true.
    /// @see BreakableCollection#add(Object)
    @Test
    @DisplayName("test the `add(Object)` method with the ADD_ALWAYS_RETURNS_TRUE break")
    public void testAddAlwaysReturnTrueBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.ADD_ALWAYS_RETURNS_TRUE)
                .doesNotPermitDuplicates()
                .build();
        assertTrue(collection.add(1));
    }

    /// Test that the `ADD_ALWAYS_RETURNS_FALSE` break causes the `add(Object)` method to always return false.
    /// @see BreakableCollection#add(Object)
    @Test
    @DisplayName("test the `add(Object)` method with the ADD_ALWAYS_RETURNS_FALSE break")
    public void testAddAlwaysReturnFalseBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.ADD_ALWAYS_RETURNS_FALSE)
                .build();
        assertFalse(collection.add(4));
    }

    /// Test that the `ADD_ALWAYS_RETURNS_OPPOSITE_VALUE` break causes the `add(Object)` method to always return the
    /// opposite value.
    /// @see BreakableCollection#add(Object)
    @Test
    @DisplayName("test the `add(Object)` method with the  ADD_ALWAYS_RETURNS_OPPOSITE_VALUE break")
    public void testAddAlwaysReturnsOppositeValueBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.ADD_ALWAYS_RETURNS_OPPOSITE_VALUE)
                .doesNotPermitDuplicates()
                .build();
        assertTrue(collection.add(1));
        assertFalse(collection.add(4));
    }

    /// Test that the `REMOVE_DOES_NOT_REMOVE_ELEMENT` break causes the `remove(Object)` method to not remove the
    /// element.
    /// @see BreakableCollection#remove(Object)
    @Test
    @DisplayName("test the `remove(Object)` method with the REMOVE_DOES_NOT_REMOVE_ELEMENT break")
    public void testRemoveDoesNotRemoveElementBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.REMOVE_DOES_NOT_REMOVE_ELEMENT)
                .build();
        collection.remove(1);
        assertContains(1, collection);
    }

    /// Test that the `REMOVE_ALWAYS_RETURNS_TRUE` break causes the `remove(Object)` method always returns `true`.
    /// @see BreakableCollection#remove(Object)
    @Test
    @DisplayName("test the `remove(Object)` method with the REMOVE_ALWAYS_RETURNS_TRUE break")
    public void testRemoveAlwaysReturnsTrueBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.REMOVE_ALWAYS_RETURNS_TRUE)
                .build();
        assertTrue(collection.remove(1));
        assertTrue(collection.remove(4));
    }

    /// Test that the `REMOVE_ALWAYS_RETURNS_FALSE` break causes the `remove(Object)` method always returns `false`.
    /// @see BreakableCollection#remove(Object)
    @Test
    @DisplayName("test the `remove(Object)` method with the REMOVE_ALWAYS_RETURNS_FALSE break")
    public void testRemoveAlwaysReturnsFalseBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.REMOVE_ALWAYS_RETURNS_FALSE)
                .build();
        assertFalse(collection.remove(1));
        assertFalse(collection.remove(4));
    }

    /// Test that the `REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE` break causes the `remove(Object)` method always returns `false`.
    /// @see BreakableCollection#remove(Object)
    @Test
    @DisplayName("test the `remove(Object)` method with the REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE break")
    public void testRemoveAlwaysReturnsOppositeValueBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE)
                .build();
        assertFalse(collection.remove(1));
        assertTrue(collection.remove(4));
    }

    /// Test that the `CONTAINS_ALL_ALWAYS_RETURNS_TRUE` break causes the `containsAll` method to always return `true`.
    /// @see BreakableCollection#containsAll(Collection)
    @Test
    @DisplayName("Test the `containsAll(Collection)` method with the CONTAINS_ALL_ALWAYS_RETURNS_TRUE break")
    public void testContainsAllAlwaysReturnsTrueBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.CONTAINS_ALL_ALWAYS_RETURNS_TRUE)
                .build();
        assertTrue(collection.containsAll(CollectionOnly.of(1)));
        assertTrue(collection.containsAll(CollectionOnly.of(4)));
    }

    /// Test that the `CONTAINS_ALL_ALWAYS_RETURNS_FALSE` break causes the `containsAll(Collection)` method to always return `true`.
    /// @see BreakableCollection#containsAll(Collection)
    @Test
    @DisplayName("Test the `contains(Object)` method with the CONTAINS_ALL_ALWAYS_RETURNS_FALSE break")
    public void testContainsAllAlwaysReturnsFalseBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.CONTAINS_ALL_ALWAYS_RETURNS_FALSE)
                .build();
        assertFalse(collection.containsAll(CollectionOnly.of(1)));
        assertFalse(collection.containsAll(CollectionOnly.of(4)));
    }

    /// Test that the `CONTAINS_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE` break causes the `containsAll(Collection)` method to always return the opposite
    /// value.
    /// @see BreakableCollection#containsAll(Collection)
    @Test
    @DisplayName("Test the `containsAll(Collection)` method with the CONTAINS_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break")
    public void testContainsAllReturnsOppositeValueBreak() {
        BreakableCollection<Integer> collection = Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.CONTAINS_ALL_RETURNS_OPPOSITE_VALUE)
                .build();
        assertFalse(collection.containsAll(CollectionOnly.of(1)));
        assertTrue(collection.containsAll(CollectionOnly.of(4)));
    }

}
