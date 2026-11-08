package com.soliscode.test.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/// Tests that the null-preventing wrappers' `equals` and `hashCode` agree (soliscode-quant T-0004, T-0005).
@DisplayName("CollectionTestUtils.preventNulls: equals and hashCode agree")
public class CollectionTestUtilsEqualsTest {

    /// Default constructor.
    public CollectionTestUtilsEqualsTest() {
        super();
    }

    @Test
    @DisplayName("a null-preventing collection equals only itself, as an unmodifiable collection does")
    void preventNullsCollectionEqualsOnlyItself() {
        Collection<Integer> a = CollectionTestUtils.preventNulls((Collection<Integer>) new ArrayList<>(List.of(1, 2)));
        Collection<Integer> b = CollectionTestUtils.preventNulls((Collection<Integer>) new ArrayList<>(List.of(1, 2)));

        assertEquals(a, a);
        assertEquals(a.hashCode(), a.hashCode());
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equal null-preventing lists have the same hash code")
    void preventNullsListWhenEqualHaveSameHashCode() {
        List<Integer> a = CollectionTestUtils.preventNulls(new ArrayList<>(List.of(1, 2)));
        List<Integer> b = CollectionTestUtils.preventNulls(new ArrayList<>(List.of(1, 2)));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
