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
package com.soliscode.test.contract.coverage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.MethodStatus;
import com.soliscode.test.breakable.Break;
import com.soliscode.test.breakable.BreakableMapEntry;
import com.soliscode.test.contract.mapentry.MapEntryContract;
import com.soliscode.test.provider.MapEntryProvider;
import com.soliscode.test.provider.MapEntryProviders;
import com.soliscode.test.provider.MapEntrySource;
import com.soliscode.test.provider.ObjectProvider;
import com.soliscode.test.provider.Providers;

/// The subjects `ContractCoverageTest` runs `MapEntryContract` against: JDK entries, and breakable ones over a
/// `HashMap`'s entry, so that a break of the write-through shows (#39).
final class MapEntrySubjects {

    private MapEntrySubjects() { }

    /// `MapEntryContract` on an entry provider, with `Integer` keys and `String` values, which does not support the
    /// given methods.
    static class Subject extends AbstractTest
            implements MapEntryContract<Map.Entry<Integer, String>, Integer, String> {
        private final MapEntryProvider<Map.Entry<Integer, String>, Integer, String> entries;

        Subject(final MapEntryProvider<Map.Entry<Integer, String>, Integer, String> entriesArg,
                final List<InterfaceMethod> unsupported) {
            this.entries = entriesArg;
            unsupported.forEach(this::doesNotSupportMethod);
        }

        @Override
        public MapEntryProvider<Map.Entry<Integer, String>, Integer, String> mapEntryProvider() {
            return entries;
        }

        @Override
        public ObjectProvider<Integer> keyProvider() {
            return Providers.integerProvider();
        }

        @Override
        public ObjectProvider<String> valueProvider() {
            return Providers.stringProvider();
        }
    }

    /// `MapEntryContract` on a JDK entry.
    static final class JdkEntry extends Subject {
        JdkEntry(final MapEntryProvider<Map.Entry<Integer, String>, Integer, String> entries,
                 final InterfaceMethod... unsupported) {
            super(entries, List.of(unsupported));
        }
    }

    /// `MapEntryContract` on a `BreakableMapEntry` over a `HashMap`'s entry, backed by the map, with a break or none.
    static final class BrokenEntry extends Subject {
        BrokenEntry(final @Nullable Break b, final Optional<InterfaceMethod> unsupported) {
            super(breakable(b, unsupported), unsupported.stream().toList());
        }
    }

    private static MapEntryProvider<Map.Entry<Integer, String>, Integer, String> breakable(
            final @Nullable Break b, final Optional<InterfaceMethod> unsupported) {
        MapEntryProvider<Map.Entry<Integer, String>, Integer, String> maps = MapEntryProviders.fromMap(HashMap::new);
        Set<Break> breaks = b == null ? Set.of() : Set.of(b);
        Map<InterfaceMethod, MethodStatus> statuses = new HashMap<>();
        unsupported.ifPresent(m -> statuses.put(m, new MethodStatus(false)));
        return (key, value) -> {
            MapEntrySource<Map.Entry<Integer, String>, Integer, String> inner = maps.source(key, value);
            return new BrokenSource(inner, new BreakableMapEntry<>(inner.entry(), breaks, statuses));
        };
    }

    /// A breakable entry over a map's, read through the map's source.
    private record BrokenSource(MapEntrySource<Map.Entry<Integer, String>, Integer, String> inner,
                                Map.Entry<Integer, String> entry)
            implements MapEntrySource<Map.Entry<Integer, String>, Integer, String> {
        @Override
        public Optional<String> backingValue() {
            return inner.backingValue();
        }
    }
}
