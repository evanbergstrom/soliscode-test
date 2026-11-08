package org.soliscode.test.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Tests that the null-preventing wrappers' `equals` and `hashCode` agree (soliscode-quant T-0004).
@DisplayName("CollectionTestUtils.preventNulls: equals and hashCode agree")
public class CollectionTestUtilsEqualsTest {

    /// Default constructor.
    public CollectionTestUtilsEqualsTest() {
        super();
    }

    @Test
    @DisplayName("equal null-preventing collections have the same hash code")
    void preventNullsCollection_whenEqual_haveSameHashCode() {
        Collection<Integer> a = CollectionTestUtils.preventNulls((Collection<Integer>) new ArrayList<>(List.of(1, 2)));
        Collection<Integer> b = CollectionTestUtils.preventNulls((Collection<Integer>) new ArrayList<>(List.of(1, 2)));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("equal null-preventing lists have the same hash code")
    void preventNullsList_whenEqual_haveSameHashCode() {
        List<Integer> a = CollectionTestUtils.preventNulls(new ArrayList<>(List.of(1, 2)));
        List<Integer> b = CollectionTestUtils.preventNulls(new ArrayList<>(List.of(1, 2)));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
