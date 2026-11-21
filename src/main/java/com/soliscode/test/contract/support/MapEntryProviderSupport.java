/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.support;

import java.util.Map;

import com.soliscode.test.provider.MapEntryProvider;

/// Interface for contract classes that test a map entry, made by a [MapEntryProvider].
///
/// @param <T> the type of the entry under test
/// @param <K> the key type
/// @param <V> the value type
/// @since 0.1.3
public interface MapEntryProviderSupport<T extends Map.Entry<K, V>, K, V> {

    /// Returns the provider of the sources, and the entries under test in them.
    ///
    /// @return the map-entry provider
    MapEntryProvider<T, K, V> mapEntryProvider();
}
