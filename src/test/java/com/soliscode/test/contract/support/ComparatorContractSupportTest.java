/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import com.soliscode.test.contract.comparator.ComparatorContract;
import com.soliscode.test.contract.support.ComparatorContractSupport.LawSample;
import com.soliscode.test.contract.support.ComparatorContractSupport.Sample;
import com.soliscode.test.contract.support.ComparatorContractSupport.SamplePositions;
import com.soliscode.test.provider.ComparableProviders;
import com.soliscode.test.provider.ComparatorProvider;
import com.soliscode.test.provider.ComparatorProviders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `ComparatorContractSupport`: its defaults, its checks of a hand-written provider, its law samples and the
/// positions that give the `thenComparing` tests their tie-breaker.
///
/// @see ComparatorContractSupport
@DisplayName("Tests for ComparatorContractSupport")
public class ComparatorContractSupportTest {

    /// Default constructor.
    public ComparatorContractSupportTest() {
        super();
    }

    /// A contract on a provider, with the support's defaults.
    private static <T> ComparatorContract<T> bare(final ComparatorProvider<T> provider) {
        return () -> provider;
    }

    /// A provider of a comparator, which may be null, and tiers, which may be bad.
    private static ComparatorProvider<Integer> handWritten(final @Nullable Comparator<Integer> comparator,
                                                           final @Nullable List<List<Integer>> tiers) {
        return new ComparatorProvider<>() {
            @Override
            public Comparator<Integer> comparator() {
                return comparator;
            }

            @Override
            public List<List<Integer>> ascendingSamples() {
                return tiers;
            }
        };
    }

    private static void assertProviderAtFault(final ComparatorProvider<Integer> provider) {
        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> {
            bare(provider).comparator();
            bare(provider).sampleTiers();
        });
        assertTrue(error.getMessage().contains("the comparator provider is at fault"), error.getMessage());
    }

    @Test
    @DisplayName("By default, a comparator claims consistency with equals and refuses nulls")
    void defaults() {
        ComparatorContract<Integer> contract = bare(ComparatorProviders.naturalOrder(ComparableProviders.integers()));
        assertTrue(contract.comparatorIsConsistentWithEquals());
        assertFalse(contract.comparatorPermitsNulls());
    }

    @Test
    @DisplayName("A provider with no comparator, no tiers, a null or empty tier, or a null sample is at fault")
    void badProvidersAreBlamed() {
        Comparator<Integer> c = Comparator.naturalOrder();
        assertProviderAtFault(handWritten(null, List.of(List.of(1))));
        assertProviderAtFault(handWritten(c, null));
        assertProviderAtFault(handWritten(c, List.of()));
        assertProviderAtFault(handWritten(c, Arrays.asList(List.of(1), null)));
        assertProviderAtFault(handWritten(c, List.of(List.of(1), List.of())));
        assertProviderAtFault(handWritten(c, List.of(Arrays.asList(1, null))));
    }

    @Test
    @DisplayName("samples() numbers each sample with its tier and index")
    void samplesHaveTiersAndIndexes() {
        List<Sample<Integer>> samples = bare(ComparatorProviders.of(Comparator.<Integer>naturalOrder(),
                List.of(List.of(1), List.of(2, 2)))).samples();
        assertEquals(List.of(new Sample<>(1, 0, 0), new Sample<>(2, 1, 0), new Sample<>(2, 1, 1)), samples);
        assertEquals("2 (tier 1, sample 1)", samples.get(2).toString());
    }

    @Test
    @DisplayName("lawSamples() adds null last only when the comparator permits nulls")
    void lawSamplesAddNullWhenPermitted() {
        ComparatorProvider<Integer> provider = ComparatorProviders.ascending(Comparator.<Integer>naturalOrder(), 1, 2);
        assertEquals(List.of(1, 2), bare(provider).lawSamples().stream().map(LawSample::value).toList());
        ComparatorContract<Integer> permitting = new ComparatorContract<>() {
            @Override
            public ComparatorProvider<Integer> comparatorProvider() {
                return provider;
            }

            @Override
            public boolean comparatorPermitsNulls() {
                return true;
            }
        };
        List<LawSample<Integer>> samples = permitting.lawSamples();
        assertEquals(3, samples.size());
        assertNull(samples.getLast().value());
        assertEquals("null", samples.getLast().toString());
    }

    @Test
    @DisplayName("positions() finds samples by identity, first occurrence, and refuses others")
    void positionsFindSamplesByIdentity() {
        Integer a = Integer.valueOf(100_000);
        Integer b = Integer.valueOf(100_000);
        List<Sample<Integer>> samples = new ArrayList<>(List.of(new Sample<>(a, 0, 0), new Sample<>(b, 0, 1),
                new Sample<>(a, 0, 2)));
        SamplePositions<Integer> positions = bare(ComparatorProviders.ascending(
                Comparator.<Integer>naturalOrder(), 1)).positions(samples);
        assertEquals(0, positions.position(a));
        assertEquals(1, positions.position(b));
        assertThrows(AssertionFailedError.class, () -> positions.position(Integer.valueOf(100_000)));
        assertThrows(AssertionFailedError.class, () -> positions.position(null));
    }

    @Test
    @DisplayName("tieBreaker() orders by descending position")
    void tieBreakerDescends() {
        Integer a = Integer.valueOf(100_000);
        Integer b = Integer.valueOf(100_000);
        SamplePositions<Integer> positions = bare(ComparatorProviders.ascending(Comparator.<Integer>naturalOrder(), 1))
                .positions(List.of(new Sample<>(a, 0, 0), new Sample<>(b, 0, 1)));
        assertEquals(1, Integer.signum(positions.tieBreaker().compare(a, b)));
        assertEquals(-1, Integer.signum(positions.tieBreaker().compare(b, a)));
        assertEquals(0, positions.tieBreaker().compare(a, a));
    }
}
