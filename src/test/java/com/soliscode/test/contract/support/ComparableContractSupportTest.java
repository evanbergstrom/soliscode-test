/*
 * Copyright 2026 Evan Bergstrom
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
package com.soliscode.test.contract.support;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import com.soliscode.test.contract.comparable.ComparableContract;
import com.soliscode.test.provider.ComparableProvider;
import com.soliscode.test.provider.ComparableProviders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `ComparableContractSupport`: its default claim of consistency with `equals`, and its checks of a
/// hand-written provider's tiers.
///
/// @see ComparableContractSupport
@DisplayName("Tests for ComparableContractSupport")
public class ComparableContractSupportTest {

    /// Default constructor.
    public ComparableContractSupportTest() {
        super();
    }

    /// A contract on a provider, with the support's defaults.
    private static <T extends Comparable<? super T>> ComparableContract<T> bare(final ComparableProvider<T> provider) {
        return () -> provider;
    }

    private static void assertProviderAtFault(final ComparableProvider<Integer> provider) {
        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> bare(provider).sampleTiers());
        assertTrue(error.getMessage().contains("the comparable provider is at fault"), error.getMessage());
    }

    @Test
    @DisplayName("By default, a class claims consistency with equals, so BigDecimal fails the consistency test")
    void consistencyIsClaimedByDefault() {
        ComparableContract<BigDecimal> contract = bare(ComparableProviders.bigDecimals());
        assertTrue(contract.naturalOrderingIsConsistentWithEquals());
        assertThrows(AssertionFailedError.class, contract::compareToIsConsistentWithEquals);
    }

    @Test
    @DisplayName("sampleTiers() returns a good provider's tiers")
    void sampleTiersReturnsGoodTiers() {
        assertEquals(List.of(List.of(1), List.of(2)), bare(() -> List.of(List.of(1), List.of(2))).sampleTiers());
    }

    @Test
    @DisplayName("sampleTiers() fails, blaming the provider, for no tiers")
    void sampleTiersRefusesNoTiers() {
        assertProviderAtFault(List::of);
        assertProviderAtFault(() -> null);
    }

    @Test
    @DisplayName("sampleTiers() fails, blaming the provider, for a null or empty tier")
    void sampleTiersRefusesBadTier() {
        assertProviderAtFault(() -> Arrays.asList(List.of(1), null));
        assertProviderAtFault(() -> List.of(List.of(1), List.of()));
    }

    @Test
    @DisplayName("sampleTiers() fails, blaming the provider, for a null sample")
    void sampleTiersRefusesNullSample() {
        assertProviderAtFault(() -> List.of(Arrays.asList(1, null)));
    }
}
