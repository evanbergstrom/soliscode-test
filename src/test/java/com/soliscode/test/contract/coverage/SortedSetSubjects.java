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

import java.util.Collections;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.contract.navigableset.NavigableSetContract;
import com.soliscode.test.contract.sortedset.SortedSetContract;
import com.soliscode.test.contract.support.WithIntegerElement;
import com.soliscode.test.provider.CollectionProvider;
import com.soliscode.test.provider.CollectionProviders;

/// The JDK subjects `ContractCoverageTest` runs `SortedSetContract` and `NavigableSetContract` against: `TreeSet` and
/// `ConcurrentSkipListSet`, in reverse and natural order, and unmodifiable views (#30, #31). Moved here from
/// `ContractCoverageTest` by #39, to keep that file within Checkstyle's length limit.
final class SortedSetSubjects {

    /// The comparator the sorted-set subjects use: reverse order, so that a set that ignores it shows.
    private static final Comparator<Integer> REVERSE = Comparator.reverseOrder();

    private SortedSetSubjects() { }

    /// A sorted set's contract: no nulls, duplicates or elements of other types.
    abstract static class SortedSetTest<S extends SortedSet<Integer>> extends AbstractTest
            implements SortedSetContract<S, Integer>, WithIntegerElement {
        @Override
        public boolean permitNulls() {
            return false;
        }

        @Override
        public boolean permitDuplicates() {
            return false;
        }

        @Override
        public boolean permitIncompatibleTypes() {
            return false;
        }
    }

    static final class JdkSortedSet extends SortedSetTest<TreeSet<Integer>> {
        @Override
        public CollectionProvider<TreeSet<Integer>, Integer> provider() {
            return CollectionProviders.from(() -> new TreeSet<>(REVERSE), TreeSet::new, c -> {
                TreeSet<Integer> set = new TreeSet<>(REVERSE);
                set.addAll(c);
                return set;
            }, PROVIDER);
        }
    }

    static final class JdkConcurrentSortedSet extends SortedSetTest<ConcurrentSkipListSet<Integer>> {
        @Override
        public CollectionProvider<ConcurrentSkipListSet<Integer>, Integer> provider() {
            return CollectionProviders.from(() -> new ConcurrentSkipListSet<>(REVERSE),
                    s -> new ConcurrentSkipListSet<>(s), c -> {
                        ConcurrentSkipListSet<Integer> set = new ConcurrentSkipListSet<>(REVERSE);
                        set.addAll(c);
                        return set;
                    }, PROVIDER);
        }
    }

    static final class JdkNaturalSortedSet extends SortedSetTest<TreeSet<Integer>> {
        @Override
        public CollectionProvider<TreeSet<Integer>, Integer> provider() {
            return CollectionProviders.from(TreeSet::new, TreeSet::new, TreeSet::new, PROVIDER);
        }
    }

    static final class JdkUnmodifiableSortedSet extends SortedSetTest<SortedSet<Integer>> {
        JdkUnmodifiableSortedSet() {
            doesNotSupportModification();
        }

        @Override
        public CollectionProvider<SortedSet<Integer>, Integer> provider() {
            return CollectionProviders.from(() -> Collections.unmodifiableSortedSet(new TreeSet<>(REVERSE)),
                    s -> Collections.unmodifiableSortedSet(new TreeSet<>(s)), c -> {
                        TreeSet<Integer> set = new TreeSet<>(REVERSE);
                        set.addAll(c);
                        return Collections.unmodifiableSortedSet(set);
                    }, PROVIDER);
        }
    }

    /// A navigable set's contract: no nulls, duplicates or elements of other types.
    abstract static class NavigableSetTest<S extends NavigableSet<Integer>> extends AbstractTest
            implements NavigableSetContract<S, Integer>, WithIntegerElement {
        @Override
        public boolean permitNulls() {
            return false;
        }

        @Override
        public boolean permitDuplicates() {
            return false;
        }

        @Override
        public boolean permitIncompatibleTypes() {
            return false;
        }
    }

    static final class JdkNavigableSet extends NavigableSetTest<TreeSet<Integer>> {
        @Override
        public CollectionProvider<TreeSet<Integer>, Integer> provider() {
            return CollectionProviders.from(() -> new TreeSet<>(REVERSE), TreeSet::new, c -> {
                TreeSet<Integer> set = new TreeSet<>(REVERSE);
                set.addAll(c);
                return set;
            }, PROVIDER);
        }
    }

    static final class JdkConcurrentNavigableSet extends NavigableSetTest<ConcurrentSkipListSet<Integer>> {
        @Override
        public CollectionProvider<ConcurrentSkipListSet<Integer>, Integer> provider() {
            return CollectionProviders.from(() -> new ConcurrentSkipListSet<>(REVERSE),
                    s -> new ConcurrentSkipListSet<>(s), c -> {
                        ConcurrentSkipListSet<Integer> set = new ConcurrentSkipListSet<>(REVERSE);
                        set.addAll(c);
                        return set;
                    }, PROVIDER);
        }
    }

    static final class JdkNaturalNavigableSet extends NavigableSetTest<TreeSet<Integer>> {
        @Override
        public CollectionProvider<TreeSet<Integer>, Integer> provider() {
            return CollectionProviders.from(TreeSet::new, TreeSet::new, TreeSet::new, PROVIDER);
        }
    }

    static final class JdkUnmodifiableNavigableSet extends NavigableSetTest<NavigableSet<Integer>> {
        JdkUnmodifiableNavigableSet() {
            doesNotSupportModification();
        }

        @Override
        public CollectionProvider<NavigableSet<Integer>, Integer> provider() {
            return CollectionProviders.from(() -> Collections.unmodifiableNavigableSet(new TreeSet<>(REVERSE)),
                    s -> Collections.unmodifiableNavigableSet(new TreeSet<>(s)), c -> {
                        TreeSet<Integer> set = new TreeSet<>(REVERSE);
                        set.addAll(c);
                        return Collections.unmodifiableNavigableSet(set);
                    }, PROVIDER);
        }
    }
}
