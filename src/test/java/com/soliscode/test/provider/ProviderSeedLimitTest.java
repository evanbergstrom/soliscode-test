/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Tests that the collection and map providers, and their wrappers, report in `uniqueSizeLimit()` the seeds they can
/// serve, which the providers they draw from bound (#70).
@DisplayName("Tests of the providers' seed limits")
public class ProviderSeedLimitTest {

    /// How many values the bounded providers have.
    private static final long BOUND = 2_000;

    /// The limit a provider asking for 10 unique instances from each seed up has, over one of [#BOUND] values.
    private static final long SEEDS = BOUND - 9;

    /// Default constructor.
    public ProviderSeedLimitTest() {
        super();
    }

    private static IntegerProvider integers(final long limit) {
        return new IntegerProvider() {
            @Override
            public long uniqueSizeLimit() {
                return limit;
            }
        };
    }

    private static CollectionProvider<List<Integer>, Integer> list(final long limit) {
        return CollectionProviders.from(ArrayList::new, ArrayList::new, ArrayList::new, integers(limit));
    }

    private static MapProvider<Integer, Integer, Map<Integer, Integer>> map(final long keys, final long values) {
        return MapProviders.from(HashMap::new, HashMap::new, HashMap::new, integers(keys), integers(values));
    }

    @Test
    @DisplayName("A collection provider serves the seeds its element provider can, less 9")
    void collectionProviderReportsTheSeedsItServes() {
        CollectionProvider<List<Integer>, Integer> provider = list(BOUND);
        assertEquals(SEEDS, provider.uniqueSizeLimit());
        assertDoesNotThrow(() -> provider.createInstance(SEEDS - 1));
        assertThrows(IllegalStateException.class, () -> provider.createInstance(SEEDS));
    }

    @Test
    @DisplayName("Over an unbounded element provider it serves every seed whose window fits; over a tiny one, none")
    void collectionProviderServesTheLastSeedAndClamps() {
        CollectionProvider<List<Integer>, Integer> unbounded = list(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE - 9, unbounded.uniqueSizeLimit());
        assertDoesNotThrow(() -> unbounded.createInstance(unbounded.uniqueSizeLimit() - 1),
                "the last seed it reports, whose window ends at the element provider's last");
        assertEquals(0, list(5).uniqueSizeLimit());
    }

    @Test
    @DisplayName("A map provider serves the seeds the smaller of its key and value providers can, less 9")
    void mapProviderReportsTheSeedsItServes() {
        assertEquals(SEEDS, map(BOUND, Long.MAX_VALUE).uniqueSizeLimit());
        assertEquals(SEEDS, map(Long.MAX_VALUE, BOUND).uniqueSizeLimit());
        MapProvider<Integer, Integer, Map<Integer, Integer>> unbounded = map(Long.MAX_VALUE, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE - 9, unbounded.uniqueSizeLimit());
        assertDoesNotThrow(() -> unbounded.createInstance(unbounded.uniqueSizeLimit() - 1));
        MapProvider<Integer, Integer, Map<Integer, Integer>> provider = map(BOUND, BOUND);
        assertDoesNotThrow(() -> provider.createInstance(SEEDS - 1));
        assertThrows(IllegalStateException.class, () -> provider.createInstance(SEEDS));
    }

    @Test
    @DisplayName("The wrappers report the limit of the provider they wrap")
    void wrappersReportTheWrappedLimit() {
        assertEquals(SEEDS, CollectionProviders.wrap(list(BOUND), Collections::unmodifiableList).uniqueSizeLimit());
        assertEquals(SEEDS, MapProviders.wrap(map(BOUND, BOUND), Collections::unmodifiableMap).uniqueSizeLimit());
    }
}
