package org.soliscode.test.breakable;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.CollectionMethods;
import org.soliscode.test.contract.sequenced.SequencedCollectionContract;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.provider.CollectionProvider;
import org.soliscode.test.provider.FunctionalCollectionProvider;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.assertions.collection.CollectionAssertions.*;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertEquals;

/// Tests for the [BreakableSequencedCollection] class. These tests determine if the breaks supported by this class
/// result in the behavior expected.
///
/// @author evanbergstrom
/// @since 1.0
/// @see BreakableSequencedCollection
public class BreakableSequencedCollectionTest extends AbstractTest
        implements SequencedCollectionContract<Integer, BreakableSequencedCollection<Integer>>, WithIntegerElement {

    @Override
    public @NonNull CollectionProvider<Integer, BreakableSequencedCollection<Integer>> provider() {
        return FunctionalCollectionProvider.from(
            BreakableSequencedCollection::new,
            BreakableSequencedCollection::new,
            elements -> new BreakableSequencedCollection<>(new java.util.ArrayList<>(elements)),
            elementProvider()
        );
    }

    // ========== addFirst Tests ==========

    /// Test that the `ADD_FIRST_DOES_NOT_ADD_ELEMENT` break causes the `addFirst` method to not add the element.
    /// @see BreakableSequencedCollection#addFirst(Object)
    @Test
    @DisplayName("Test the `addFirst` method with the ADD_FIRST_DOES_NOT_ADD_ELEMENT break")
    public void testAddFirstDoesNotAddElementBreak() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(2, 3)
                .addBreak(BreakableSequencedCollection.ADD_FIRST_DOES_NOT_ADD_ELEMENT)
                .build();

        collection.addFirst(1);
        assertEquals(java.util.Arrays.asList(2, 3), collection);
        assertDoesNotContain(1, collection);
    }

    /// Test that the `ADD_FIRST_ADDS_TO_END` break causes the `addFirst` method to add the element to the end.
    /// @see BreakableSequencedCollection#addFirst(Object)
    @Test
    @DisplayName("Test the `addFirst` method with the ADD_FIRST_ADDS_TO_END break")
    public void testAddFirstAddsToEndBreak() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(2, 3)
                .addBreak(BreakableSequencedCollection.ADD_FIRST_ADDS_TO_END)
                .build();

        collection.addFirst(1);
        assertEquals(java.util.Arrays.asList(2, 3, 1), collection);
        assertContains(1, collection);
    }

    ///  Test 'addFirst' fails when itg is not supported
    /// @see BreakableSequencedCollection#addFirst
    @Test
    @DisplayName("Test addFirst method fails when not supported")
    public void testAddFirstFailsWhenNotSupported() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2, 3)
                .doesNotSupport(CollectionMethods.AddFirst)
                .build();

        assertThrows(UnsupportedOperationException.class, () -> collection.addFirst(0));
    }

    // ========== addLast Tests ==========

    /// Test that the `ADD_LAST_DOES_NOT_ADD_ELEMENT` break causes the `addLast` method to not add the element.
    /// @see BreakableSequencedCollection#addLast(Object)
    @Test
    @DisplayName("Test the `addLast` method with the ADD_LAST_DOES_NOT_ADD_ELEMENT break")
    public void testAddLastDoesNotAddElementBreak() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2)
                .addBreak(BreakableSequencedCollection.ADD_LAST_DOES_NOT_ADD_ELEMENT)
                .build();

        collection.addLast(3);
        assertEquals(java.util.Arrays.asList(1, 2), collection);
        assertDoesNotContain(3, collection);
    }

    /// Test that the `ADD_LAST_ADDS_TO_FRONT` break causes the `addLast` method to add the element to the front.
    /// @see BreakableSequencedCollection#addLast(Object)
    @Test
    @DisplayName("Test the `addLast` method with the ADD_LAST_ADDS_TO_FRONT break")
    public void testAddLastAddsToFrontBreak() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2)
                .addBreak(BreakableSequencedCollection.ADD_LAST_ADDS_TO_FRONT)
                .build();

        collection.addLast(3);
        assertEquals(java.util.Arrays.asList(3, 1, 2), collection);
        assertContains(3, collection);
    }

    ///  Test 'addLast' fails when it is not supported
    /// @see BreakableSequencedCollection#addLast
    @Test
    @DisplayName("Test addLast method fails when not supported")
    public void testAddLastFailsWhenNotSupported() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2, 3)
                .doesNotSupport(CollectionMethods.AddLast)
                .build();

        assertThrows(UnsupportedOperationException.class, () -> collection.addLast(4));
    }


    // ========== getFirst Tests ==========

    /// Test that the `GET_FIRST_RETURNS_NULL` break causes the `getFirst` method to always return null.
    /// @see BreakableSequencedCollection#getFirst()
    @Test
    @DisplayName("Test the `getFirst` method with the GET_FIRST_RETURNS_NULL break")
    public void testGetFirstReturnsNullBreak() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2, 3)
                .addBreak(BreakableSequencedCollection.GET_FIRST_RETURNS_NULL)
                .build();

        assertNull(collection.getFirst());
    }

    /// Test that the `GET_FIRST_ALWAYS_THROWS` break causes the `getFirst` method to always throw NoSuchElementException.
    /// @see BreakableSequencedCollection#getFirst()
    @Test
    @DisplayName("Test the `getFirst` method with the GET_FIRST_ALWAYS_THROWS break")
    public void testGetFirstAlwaysThrowsBreak() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2, 3)
                .addBreak(BreakableSequencedCollection.GET_FIRST_ALWAYS_THROWS)
                .build();

        assertThrows(NoSuchElementException.class, collection::getFirst);
    }

    /// Test that the `GET_FIRST_SKIPS_FIRST_ELEMENT` break causes the `getFirst` method to return the second element.
    /// @see BreakableSequencedCollection#getFirst()
    @Test
    @DisplayName("Test the `getFirst` method with the GET_FIRST_SKIPS_FIRST_ELEMENT break")
    public void testGetFirstSkipsFirstElementBreak() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2, 3)
                .addBreak(BreakableSequencedCollection.GET_FIRST_SKIPS_FIRST_ELEMENT)
                .build();

        org.junit.jupiter.api.Assertions.assertEquals(2, collection.getFirst());
    }

    /// Test that the `GET_FIRST_SKIPS_FIRST_ELEMENT` break throws when there's only one element.
    /// @see BreakableSequencedCollection#getFirst()
    @Test
    @DisplayName("Test the `getFirst` method with the GET_FIRST_SKIPS_FIRST_ELEMENT break on single element collection")
    public void testGetFirstSkipsFirstElementBreakSingleElement() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1)
                .addBreak(BreakableSequencedCollection.GET_FIRST_SKIPS_FIRST_ELEMENT)
                .build();

        assertThrows(NoSuchElementException.class, collection::getFirst);
    }

    ///  Test 'getFirst' fails when it is not supported
    /// @see BreakableSequencedCollection#getFirst
    @Test
    @DisplayName("Test getFirst method fails when not supported")
    public void testGetFirstFailsWhenNotSupported() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2, 3)
                .doesNotSupport(CollectionMethods.GetFirst)
                .build();

        assertThrows(UnsupportedOperationException.class, collection::getFirst);
    }

    // ========== getLast Tests ==========

    /// Test that the `GET_LAST_RETURNS_NULL` break causes the `getLast` method to always return null.
    /// @see BreakableSequencedCollection#getLast()
    @Test
    @DisplayName("Test the `getLast` method with the GET_LAST_RETURNS_NULL break")
    public void testGetLastReturnsNullBreak() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2, 3)
                .addBreak(BreakableSequencedCollection.GET_LAST_RETURNS_NULL)
                .build();

        assertNull(collection.getLast());
    }

    /// Test that the `GET_LAST_ALWAYS_THROWS` break causes the `getLast` method to always throw NoSuchElementException.
    /// @see BreakableSequencedCollection#getLast()
    @Test
    @DisplayName("Test the `getLast` method with the GET_LAST_ALWAYS_THROWS break")
    public void testGetLastAlwaysThrowsBreak() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2, 3)
                .addBreak(BreakableSequencedCollection.GET_LAST_ALWAYS_THROWS)
                .build();

        assertThrows(NoSuchElementException.class, collection::getLast);
    }

    /// Test that the `GET_LAST_SKIPS_LAST_ELEMENT` break causes the `getLast` method to return the second to last element.
    /// @see BreakableSequencedCollection#getLast()
    @Test
    @DisplayName("Test the `getLast` method with the GET_LAST_SKIPS_LAST_ELEMENT break")
    public void testGetLastSkipsLastElementBreak() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2, 3)
                .addBreak(BreakableSequencedCollection.GET_LAST_SKIPS_LAST_ELEMENT)
                .build();

        org.junit.jupiter.api.Assertions.assertEquals(2, collection.getLast());
    }

    /// Test that the `GET_LAST_SKIPS_LAST_ELEMENT` break throws when there's only one element.
    /// @see BreakableSequencedCollection#getLast()
    @Test
    @DisplayName("Test the `getLast` method with the GET_LAST_SKIPS_LAST_ELEMENT break on single element collection")
    public void testGetLastSkipsLastElementBreakSingleElement() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1)
                .addBreak(BreakableSequencedCollection.GET_LAST_SKIPS_LAST_ELEMENT)
                .build();

        assertThrows(NoSuchElementException.class, collection::getLast);
    }

    ///  Test 'getLast' fails when it is not supported
    /// @see BreakableSequencedCollection#getLast
    @Test
    @DisplayName("Test getLast method fails when not supported")
    public void testGetLastFailsWhenNotSupported() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2, 3)
                .doesNotSupport(CollectionMethods.GetLast)
                .build();

        assertThrows(UnsupportedOperationException.class, collection::getLast);
    }

    // ========== removeFirst Tests ==========

    /// Test that the `REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT` break causes the `removeFirst` method to not remove the element.
    /// @see BreakableSequencedCollection#removeFirst()
    @Test
    @DisplayName("Test the `removeFirst` method with the REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT break")
    public void testRemoveFirstDoesNotRemoveElementBreak() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2, 3)
                .addBreak(BreakableSequencedCollection.REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT)
                .build();

        Integer removed = collection.removeFirst();
        org.junit.jupiter.api.Assertions.assertEquals(1, removed);
        assertEquals(java.util.Arrays.asList(1, 2, 3), collection);
        assertContains(1, collection);
    }

    /// Test that the `REMOVE_FIRST_RETURNS_NULL` break causes the `removeFirst` method to return null.
    /// @see BreakableSequencedCollection#removeFirst()
    @Test
    @DisplayName("Test the `removeFirst` method with the REMOVE_FIRST_RETURNS_NULL break")
    public void testRemoveFirstReturnsNullBreak() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2, 3)
                .addBreak(BreakableSequencedCollection.REMOVE_FIRST_RETURNS_NULL)
                .build();

        Integer removed = collection.removeFirst();
        assertNull(removed);
        assertEquals(java.util.Arrays.asList(1, 2, 3), collection);  // Element was NOT removed
        assertContains(1, collection);
    }

    /// Test that the `REMOVE_FIRST_ALWAYS_THROWS` break causes the `removeFirst` method to always throw NoSuchElementException.
    /// @see BreakableSequencedCollection#removeFirst()
    @Test
    @DisplayName("Test the `removeFirst` method with the REMOVE_FIRST_ALWAYS_THROWS break")
    public void testRemoveFirstAlwaysThrowsBreak() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2, 3)
                .addBreak(BreakableSequencedCollection.REMOVE_FIRST_ALWAYS_THROWS)
                .build();

        assertThrows(NoSuchElementException.class, collection::removeFirst);
        // Verify collection is unchanged when exception is thrown
        assertEquals(java.util.Arrays.asList(1, 2, 3), collection);
    }

    ///  Test 'removeFirst' fails when it is not supported
    /// @see BreakableSequencedCollection#removeFirst
    @Test
    @DisplayName("Test removeFirst method fails when not supported")
    public void testRemoveFirstFailsWhenNotSupported() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2, 3)
                .doesNotSupport(CollectionMethods.RemoveFirst)
                .build();

        assertThrows(UnsupportedOperationException.class, collection::removeFirst);
    }


    // ========== removeLast Tests ==========

    /// Test that the `REMOVE_LAST_DOES_NOT_REMOVE_ELEMENT` break causes the `removeLast` method to not remove the element.
    /// @see BreakableSequencedCollection#removeLast()
    @Test
    @DisplayName("Test the `removeLast` method with the REMOVE_LAST_DOES_NOT_REMOVE_ELEMENT break")
    public void testRemoveLastDoesNotRemoveElementBreak() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2, 3)
                .addBreak(BreakableSequencedCollection.REMOVE_LAST_DOES_NOT_REMOVE_ELEMENT)
                .build();

        Integer removed = collection.removeLast();
        org.junit.jupiter.api.Assertions.assertEquals(3, removed);
        assertEquals(java.util.Arrays.asList(1, 2, 3), collection);
        assertContains(3, collection);
    }

    /// Test that the `REMOVE_LAST_RETURNS_NULL` break causes the `removeLast` method to return null.
    /// @see BreakableSequencedCollection#removeLast()
    @Test
    @DisplayName("Test the `removeLast` method with the REMOVE_LAST_RETURNS_NULL break")
    public void testRemoveLastReturnsNullBreak() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2, 3)
                .addBreak(BreakableSequencedCollection.REMOVE_LAST_RETURNS_NULL)
                .build();

        Integer removed = collection.removeLast();
        assertNull(removed);
        assertEquals(java.util.Arrays.asList(1, 2, 3), collection);  // Element was NOT removed
        assertContains(3, collection);
    }

    /// Test that the `REMOVE_LAST_ALWAYS_THROWS` break causes the `removeLast` method to always throw NoSuchElementException.
    /// @see BreakableSequencedCollection#removeLast()
    @Test
    @DisplayName("Test the `removeLast` method with the REMOVE_LAST_ALWAYS_THROWS break")
    public void testRemoveLastAlwaysThrowsBreak() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2, 3)
                .addBreak(BreakableSequencedCollection.REMOVE_LAST_ALWAYS_THROWS)
                .build();

        assertThrows(NoSuchElementException.class, collection::removeLast);
        // Verify collection is unchanged when exception is thrown
        assertEquals(java.util.Arrays.asList(1, 2, 3), collection);
    }

    ///  Test 'removeLast' fails when it is not supported
    /// @see BreakableSequencedCollection#removeLast
    /// @see CollectionMethods#RemoveLast
    @Test
    @DisplayName("Test removeLast method fails when not supported")
    public void testRemoveLastFailsWhenNotSupported() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2, 3)
                .doesNotSupport(CollectionMethods.RemoveLast)
                .build();

        assertThrows(UnsupportedOperationException.class, collection::removeLast);
    }


    // ========== reversed Tests ==========

    /// Test that the `REVERSED_DOES_NOT_REVERSE_COLLECTION` break causes the `reversed` method to not reverse the collection.
    /// @see BreakableSequencedCollection#reversed()
    @Test
    @DisplayName("Test the `reversed` method with the REVERSED_DOES_NOT_REVERSE_COLLECTION break")
    public void testReversedDoesNotReverseCollectionBreak() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2, 3)
                .addBreak(BreakableSequencedCollection.REVERSED_DOES_NOT_REVERSE_COLLECTION)
                .build();

        SequencedCollection<Integer> reversed = collection.reversed();
        assertEquals(java.util.Arrays.asList(1, 2, 3), reversed);
        // Verify original collection is unchanged
        assertEquals(java.util.Arrays.asList(1, 2, 3), collection);
    }

    /// Test that the `REVERSED_MODIFIES_THE_COLLECTION` break causes the `reversed` method to modify the original collection.
    /// @see BreakableSequencedCollection#reversed()
    @Test
    @DisplayName("Test the `reversed` method with the REVERSED_MODIFIES_THE_COLLECTION break")
    public void testReversedModifiesTheCollectionBreak() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2, 3)
                .addBreak(BreakableSequencedCollection.REVERSED_MODIFIES_THE_COLLECTION)
                .build();

        SequencedCollection<Integer> reversed = collection.reversed();
        // The break causes the original collection to be modified and returned
        assertEquals(java.util.Arrays.asList(3, 2, 1), reversed);
        assertEquals(java.util.Arrays.asList(3, 2, 1), collection);
        // They should be the same instance (but we can't test this directly due to private field access)
    }

    /// Test normal `reversed` method behavior without breaks.
    /// @see BreakableSequencedCollection#reversed()
    @Test
    @DisplayName("Test the `reversed` method with no breaks")
    public void testReversedNormalBehavior() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2, 3)
                .build();

        SequencedCollection<Integer> reversed = collection.reversed();
        assertEquals(java.util.Arrays.asList(3, 2, 1), reversed);
        // Verify original collection is unchanged
        assertEquals(java.util.Arrays.asList(1, 2, 3), collection);
    }

    ///  Test 'reversed' fails when itg is not supported
    /// @see BreakableSequencedCollection#reversed()
    @Test
    @DisplayName("Test reversed method fails when not supported")
    public void testReverseFailsWhenNotSupported() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2, 3)
                .doesNotSupport(CollectionMethods.Reversed)
                .build();
        assertThrows(UnsupportedOperationException.class, collection::reversed);
    }

    // ========== Combined Breaks Tests ==========

    /// Test multiple breaks working together on add operations.
    @Test
    @DisplayName("Test combined breaks on add operations")
    public void testCombinedAddBreaks() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(2)
                .addBreak(BreakableSequencedCollection.ADD_FIRST_ADDS_TO_END)
                .addBreak(BreakableSequencedCollection.ADD_LAST_ADDS_TO_FRONT)
                .build();

        collection.addFirst(1);  // Should add to end
        collection.addLast(3);   // Should add to front
        assertEquals(java.util.Arrays.asList(3, 2, 1), collection);
    }

    /// Test multiple breaks working together on get operations.
    @Test
    @DisplayName("Test combined breaks on get operations")
    public void testCombinedGetBreaks() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2, 3, 4)
                .addBreak(BreakableSequencedCollection.GET_FIRST_SKIPS_FIRST_ELEMENT)
                .addBreak(BreakableSequencedCollection.GET_LAST_SKIPS_LAST_ELEMENT)
                .build();

        org.junit.jupiter.api.Assertions.assertEquals(2, collection.getFirst()); // Should return second element
        org.junit.jupiter.api.Assertions.assertEquals(3, collection.getLast());  // Should return second to last element
    }

    /// Test multiple breaks working together on remove operations.
    @Test
    @DisplayName("Test combined breaks on remove operations")
    public void testCombinedRemoveBreaks() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2, 3)
                .addBreak(BreakableSequencedCollection.REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT)
                .addBreak(BreakableSequencedCollection.REMOVE_LAST_RETURNS_NULL)
                .build();

        Integer first = collection.removeFirst();
        org.junit.jupiter.api.Assertions.assertEquals(1, first);
        assertEquals(java.util.Arrays.asList(1, 2, 3), collection); // First element not removed

        Integer last = collection.removeLast();
        assertNull(last);
        assertEquals(java.util.Arrays.asList(1, 2, 3), collection); // Last element NOT removed, only returns null
    }

    // ========== Edge Case Tests ==========

    /// Test operations on empty collections with breaks.
    @Test
    @DisplayName("Test breaks on empty collection")
    public void testBreaksOnEmptyCollection() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(Integer.class)
                .addBreak(BreakableSequencedCollection.GET_FIRST_RETURNS_NULL)
                .addBreak(BreakableSequencedCollection.GET_LAST_ALWAYS_THROWS)
                .build();

        assertNull(collection.getFirst());
        assertThrows(NoSuchElementException.class, collection::getLast);
    }

    /// Test that inherited Collection breaks still work.
    @Test
    @DisplayName("Test inherited Collection breaks")
    public void testInheritedCollectionBreaks() {
        BreakableSequencedCollection<Integer> collection = Breakables.buildSequencedCollection(1, 2, 3)
                .addBreak(BreakableCollection.SIZE_ALWAYS_RETURNS_ZERO)
                .addBreak(BreakableCollection.CONTAINS_ALWAYS_RETURNS_TRUE)
                .build();

        org.junit.jupiter.api.Assertions.assertEquals(0, collection.size()); // From BreakableCollection
        assertTrue(collection.contains(999)); // From BreakableCollection

        // SequencedCollection methods should still work normally
        org.junit.jupiter.api.Assertions.assertEquals(1, collection.getFirst());
        org.junit.jupiter.api.Assertions.assertEquals(3, collection.getLast());
    }

    /// Test builder functionality.
    @Test
    @DisplayName("Test builder pattern")
    public void testBuilder() {
        BreakableSequencedCollection<Integer> collection = new BreakableSequencedCollection.Builder<Integer>()
                .addElements(1, 2, 3)
                .addBreak(BreakableSequencedCollection.ADD_FIRST_DOES_NOT_ADD_ELEMENT)
                .addBreak(BreakableSequencedCollection.GET_FIRST_RETURNS_NULL)
                .build();

        assertNull(collection.getFirst());
        collection.addFirst(0);
        assertEquals(java.util.Arrays.asList(1, 2, 3), collection); // addFirst had no effect

        BreakableSequencedCollection<Integer> collection2 = new BreakableSequencedCollection.Builder<>(List.of(4, 5, 6))
                    .build();
        assertEquals(java.util.Arrays.asList(4, 5, 6), collection2); // addFirst had no effect

    }

    /// Test builder copy.
    @Test
    @DisplayName("Test builder copy")
    public void testBuilderCopy() {
        BreakableSequencedCollection.Builder<Integer> builder = new BreakableSequencedCollection.Builder<Integer>()
                .addElements(1, 2, 3)
                .addBreak(BreakableSequencedCollection.ADD_FIRST_DOES_NOT_ADD_ELEMENT)
                .addBreak(BreakableSequencedCollection.GET_FIRST_RETURNS_NULL);

        BreakableSequencedCollection.Builder<Integer> copy = builder.copy();

        BreakableSequencedCollection<Integer> collection = copy.build();

        assertNull(collection.getFirst());
        collection.addFirst(0);
        assertEquals(java.util.Arrays.asList(1, 2, 3), collection); // addFirst had no effect
    }

    /// Test copy constructor.
    @Test
    @DisplayName("Test copy constructor")
    public void testCopyConstructor() {
        BreakableSequencedCollection<Integer> original = Breakables.buildSequencedCollection(1, 2, 3)
                .addBreak(BreakableSequencedCollection.GET_FIRST_RETURNS_NULL)
                .build();

        BreakableSequencedCollection<Integer> copy = new BreakableSequencedCollection<>(original);

        // Copy should have the same elements but different behavior (no breaks copied)
        assertEquals(java.util.Arrays.asList(1, 2, 3), copy);
        org.junit.jupiter.api.Assertions.assertEquals(1, copy.getFirst()); // No break, should return actual first element
        assertNull(original.getFirst()); // Still broken
    }
}