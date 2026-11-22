/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.guide;

import java.util.LinkedHashMap;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.contract.map.MapContract;
import com.soliscode.test.provider.MapProvider;
import com.soliscode.test.provider.MapProviders;
import com.soliscode.test.provider.Providers;

/// How to test a collection: [MapContract] run against a map, here a [LinkedHashMap] standing in for one of the
/// reader's own.
class InventoryMapTest extends AbstractTest
        implements MapContract<LinkedHashMap<String, Integer>, String, Integer> {

    @Override
    public MapProvider<String, Integer, LinkedHashMap<String, Integer>> provider() {
        return MapProviders.from(LinkedHashMap::new, LinkedHashMap::new, LinkedHashMap::new,
                Providers.stringProvider(), Providers.integerProvider());
    }
}
