package org.soliscode.test.contract.map;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.breakable.Break;
import org.soliscode.test.breakable.BreakableMap;
import org.soliscode.test.contract.ContractTest;
import org.soliscode.test.contract.DynamicContract;
import org.soliscode.test.contract.support.WithIntegerKeyAndStringValue;
import org.soliscode.test.provider.MapProvider;

import java.util.Arrays;
import java.util.Collection;

/// Tests for the MapContract class.
///
/// @author evanbergstrom
/// @since 1.0
@DisplayName("Tests for MapContract class")
public class MapContractTest extends ContractTest<BreakableMap<Integer, String>> {

    /// Verifies that the tests all pass when testing a working Map implementation.
    /// In this case, instances of `BreakableMap` are used that have no breaks specified.
    @Nested
    class WorkingMapTest extends AbstractTest
            implements MapContract<Integer, String, BreakableMap<Integer, String>>,
            BreakableMap.WithProvider<Integer, String>, WithIntegerKeyAndStringValue<BreakableMap<Integer, String>> {
    }

    /// Dynamically created instance of `MapContract` that will run on instances of `BreakableMap` with a
    /// specified break. This contract will be expected to fail on certain tests depending on the specific break that
    /// is being used.
    @Disabled("Used only for dynamic test generation")
    protected static class DynamicBrokenMapContract
            extends DynamicContract<BreakableMap<Integer, String>, MapProvider<Integer, String, BreakableMap<Integer, String>>>
            implements MapContract<Integer, String, BreakableMap<Integer, String>>, WithIntegerKeyAndStringValue<BreakableMap<Integer, String>> {

        protected DynamicBrokenMapContract(final @NonNull Break b, final @NonNull InterfaceMethod m) {
            super(b, m, (breaks, statuses, test) -> BreakableMap.mapProvider(
                    WithIntegerKeyAndStringValue.KEY_PROVIDER,
                    WithIntegerKeyAndStringValue.VALUE_PROVIDER,
                    breaks, statuses));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected @NonNull DynamicBrokenMapContract createTest(final @NonNull Break b, final @NonNull InterfaceMethod m) {
        return new DynamicBrokenMapContract(b, m);
    }

    @TestFactory
    public Collection<DynamicTest> dynamicTestsForSize() {
        return Arrays.asList(
                failingTestWithBreak("testSizeOnEmptyMap() fails with SIZE_IS_OFF_BY_ONE break",
                        BreakableMap.SIZE_IS_OFF_BY_ONE,
                        DynamicBrokenMapContract::testSizeOnEmptyMap),

                passingTestWithUnsupportedMethod("testSizeOnEmptyMap() fails when not supported",
                        MapMethods.SIZE,
                        DynamicBrokenMapContract::testSizeOnEmptyMap),

                failingTestWithBreak("testSizeOnMapWithElements() fails with SIZE_ALWAYS_RETURNS_ZERO break",
                        BreakableMap.SIZE_ALWAYS_RETURNS_ZERO,
                        DynamicBrokenMapContract::testSizeOnMapWithElements),

                failingTestWithBreak("testSizeOnMapWithElements() fails with SIZE_IS_OFF_BY_ONE break",
                        BreakableMap.SIZE_IS_OFF_BY_ONE,
                        DynamicBrokenMapContract::testSizeOnMapWithElements),

                passingTestWithUnsupportedMethod("testSizeOnMapWithElements() fails when not supported",
                        MapMethods.SIZE,
                        DynamicBrokenMapContract::testSizeOnMapWithElements)
        );
    }

    @TestFactory
    public Collection<DynamicTest> dynamicTestsForIsEmpty() {
        return Arrays.asList(
                failingTestWithBreak("testIsEmptyOnEmptyMap() fails with IS_EMPTY_ALWAYS_RETURNS_FALSE break",
                        BreakableMap.IS_EMPTY_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenMapContract::testIsEmptyOnEmptyMap),

                failingTestWithBreak("testIsEmptyOnEmptyMap() fails with IS_EMPTY_RETURNS_OPPOSITE_VALUE break",
                        BreakableMap.IS_EMPTY_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenMapContract::testIsEmptyOnEmptyMap),

                failingTestWithBreak("testIsEmptyOnMapWithElements() fails with IS_EMPTY_ALWAYS_RETURNS_TRUE break",
                        BreakableMap.IS_EMPTY_ALWAYS_RETURNS_TRUE,
                        DynamicBrokenMapContract::testIsEmptyOnMapWithElements),

                failingTestWithBreak("testIsEmptyOnMapWithElements() fails with IS_EMPTY_RETURNS_OPPOSITE_VALUE break",
                        BreakableMap.IS_EMPTY_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenMapContract::testIsEmptyOnMapWithElements)
        );
    }

    @TestFactory
    public Collection<DynamicTest> dynamicTestsForContainsKey() {
        return Arrays.asList(
                failingTestWithBreak("testContainsKeyOnMapWithElements() fails with CONTAINS_KEY_ALWAYS_RETURNS_FALSE break",
                        BreakableMap.CONTAINS_KEY_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenMapContract::testContainsKeyOnMapWithElements),

                failingTestWithBreak("testContainsKeyOnMapWithElements() fails with CONTAINS_KEY_RETURNS_OPPOSITE_VALUE break",
                        BreakableMap.CONTAINS_KEY_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenMapContract::testContainsKeyOnMapWithElements),

                failingTestWithBreak("testContainsKeyOnEmptyMap() fails with CONTAINS_KEY_ALWAYS_RETURNS_TRUE break",
                        BreakableMap.CONTAINS_KEY_ALWAYS_RETURNS_TRUE,
                        DynamicBrokenMapContract::testContainsKeyOnEmptyMap),

                failingTestWithBreak("testContainsKeyOnEmptyMap() fails with CONTAINS_KEY_RETURNS_OPPOSITE_VALUE break",
                        BreakableMap.CONTAINS_KEY_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenMapContract::testContainsKeyOnEmptyMap)
        );
    }

    @TestFactory
    public Collection<DynamicTest> dynamicTestsForGet() {
        return Arrays.asList(
                failingTestWithBreak("testGetOnMapWithElements() fails with GET_ALWAYS_RETURNS_NULL break",
                        BreakableMap.GET_ALWAYS_RETURNS_NULL,
                        DynamicBrokenMapContract::testGetOnMapWithElements),

                failingTestWithBreak("testGetOnMapWithElements() fails with GET_FAILS_FOR_FIRST_KEY break",
                        BreakableMap.GET_FAILS_FOR_FIRST_KEY,
                        DynamicBrokenMapContract::testGetOnMapWithElements)
        );
    }

    @TestFactory
    public Collection<DynamicTest> dynamicTestsForPut() {
        return Arrays.asList(
                failingTestWithBreak("testPutNewEntry() fails with PUT_DOES_NOT_ADD_PAIR break",
                        BreakableMap.PUT_DOES_NOT_ADD_PAIR,
                        DynamicBrokenMapContract::testPutNewEntry),

                failingTestWithBreak("testPutExistingEntry() fails with PUT_ALWAYS_RETURNS_NULL break",
                        BreakableMap.PUT_ALWAYS_RETURNS_NULL,
                        DynamicBrokenMapContract::testPutExistingEntry)
        );
    }

    @TestFactory
    public Collection<DynamicTest> dynamicTestsForRemove() {
        return Arrays.asList(
                failingTestWithBreak("testRemoveOnMapWithElements() fails with REMOVE_DOES_NOT_REMOVE_KEY break",
                        BreakableMap.REMOVE_DOES_NOT_REMOVE_KEY,
                        DynamicBrokenMapContract::testRemoveOnMapWithElements),

                failingTestWithBreak("testRemoveOnMapWithElements() fails with REMOVE_ALWAYS_RETURNS_NULL break",
                        BreakableMap.REMOVE_ALWAYS_RETURNS_NULL,
                        DynamicBrokenMapContract::testRemoveOnMapWithElements)
        );
    }

    @TestFactory
    public Collection<DynamicTest> dynamicTestsForClear() {
        return Arrays.asList(
                failingTestWithBreak("testClear() fails with CLEAR_ALL_DOES_NOT_REMOVE_ANY_PAIRS break",
                        BreakableMap.CLEAR_ALL_DOES_NOT_REMOVE_ANY_PAIRS,
                        DynamicBrokenMapContract::testClear)
        );
    }
}
