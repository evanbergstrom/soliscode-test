package com.soliscode.test.breakable;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.contract.collection.CollectionContract;
import com.soliscode.test.contract.support.WithIntegerElement;
import com.soliscode.test.provider.CollectionProvider;
import com.soliscode.test.provider.CollectionProviders;
import com.soliscode.test.util.UsesCollections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link BreakableSequencedSet}.
 */
public class BreakableSequencedSetTest extends AbstractTest
        implements CollectionContract<BreakableSequencedSet<Integer>, Integer>, WithIntegerElement, UsesCollections {

    /// Default constructor.
    public BreakableSequencedSetTest() {
        super();
    }

    @Override
    public CollectionProvider<BreakableSequencedSet<Integer>, Integer> provider() {
        return CollectionProviders.from(
                BreakableSequencedSet::new,
                BreakableSequencedSet::new,
                c -> new BreakableSequencedSet.Builder<>(c).build(),
                elementProvider()
        );
    }

    @Override
    public boolean permitDuplicates() {
        return false;
    }

    @Test
    @DisplayName("Test SequencedSet basic operations")
    void testSequencedSetOperations() {
        BreakableSequencedSet<Integer> set = new BreakableSequencedSet<>();
        set.add(1);
        set.add(2);
        set.add(3);

        assertEquals(1, set.getFirst());
        assertEquals(3, set.getLast());

        set.addFirst(0);
        assertEquals(0, set.getFirst());
        assertEquals(4, set.size());

        set.addLast(4);
        assertEquals(4, set.getLast());
        assertEquals(5, set.size());

        assertEquals(0, set.removeFirst());
        assertEquals(4, set.removeLast());
        assertEquals(3, set.size());
    }

    @Test
    @DisplayName("Test SequencedSet breaks")
    void testSequencedSetBreaks() {
        BreakableSequencedSet<Integer> set = new BreakableSequencedSet.Builder<Integer>()
                .addBreak(BreakableSequencedCollection.GET_FIRST_ALWAYS_THROWS)
                .addBreak(BreakableSequencedCollection.ADD_LAST_DOES_NOT_ADD_ELEMENT)
                .build();

        set.add(1);
        assertThrows(NoSuchElementException.class, set::getFirst);

        set.addLast(2);
        assertFalse(set.contains(2));
    }

    @Test
    @DisplayName("Test reversed view")
    void testReversedView() {
        BreakableSequencedSet<Integer> set = new BreakableSequencedSet<>();
        set.add(1);
        set.add(2);
        set.add(3);

        var reversed = set.reversed();
        assertEquals(3, reversed.getFirst());
        assertEquals(1, reversed.getLast());

        // Reversed view should also be breakable
        var brokenReversed = new BreakableSequencedSet.Builder<Integer>()
                .addBreak(BreakableSequencedCollection.REVERSED_DOES_NOT_REVERSE_COLLECTION)
                .build();
        brokenReversed.add(1);
        brokenReversed.add(2);
        assertSame(brokenReversed, brokenReversed.reversed());
    }
}
