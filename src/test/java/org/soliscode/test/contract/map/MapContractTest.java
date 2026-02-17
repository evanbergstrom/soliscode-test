package org.soliscode.test.contract.map;

import org.jspecify.annotations.NonNull;
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
import org.soliscode.test.contract.dynamic.DynamicBrokenMapContract;
import org.soliscode.test.contract.support.WithIntegerKeyAndStringValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/// Tests for the MapContract class.
///
/// @author evanbergstrom
/// @since 1.0
/// **Contract-based tests for `MapContract`**
///
/// This class provides a comprehensive suite of tests for the [MapContract] interface.
/// It verifies that the contract tests correctly identify both working [java.util.Map] implementations
/// and those with programmed [Break] conditions.
///
/// ## Test Scope
/// The tests in this class cover:
/// - Validation of working map implementations via [WorkingMapTest].
/// - Dynamic generation of tests for broken map behaviors using [DynamicBrokenMapContract].
/// - Verification of standard map operations: `size`, `isEmpty`, `containsKey`, `get`, `put`, `remove`, and `clear`.
///
/// ## Configuration
/// - Uses [Integer] keys and [String] values for testing.
/// - Employs [BreakableMap] as the implementation under test.
///
/// @author evanbergstrom
/// @see MapContract
/// @see BreakableMap
/// @since 1.0.0
@DisplayName("Tests for MapContract class")
public class MapContractTest extends ContractTest<BreakableMap<Integer, String>> {

    /// Tests for working map implementations.
    ///
    /// This nested class verifies that [MapContract] correctly passes when applied
    /// to a [BreakableMap] that has no active breaks and supports all operations.
    @Nested
    class WorkingMapTest extends AbstractTest
            implements MapContract<Integer, String, BreakableMap<Integer, String>>,
            BreakableMap.WithProvider<Integer, String>, WithIntegerKeyAndStringValue<BreakableMap<Integer, String>> {
    }

    /// {@inheritDoc}
    ///
    /// @param b the break to apply to the test object
    /// @param m the optional method to configure as unsupported
    /// @return a new [DynamicBrokenMapContract] instance
    @Override
    protected @NonNull DynamicContract<?, ?> createTest(final @NonNull Break b, final @NonNull InterfaceMethod m) {
        return new DynamicBrokenMapContract(b, m);
    }

    /// Test factory for verifying that `size()` contract tests correctly identify failures and unsupported operations.
    ///
    /// @return a collection of dynamic tests for `size()`
    /// @see SizeContract
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForSize() {
        return Arrays.asList(
                failsWithBreak(BreakableMap.SIZE_IS_OFF_BY_ONE, DynamicBrokenMapContract::size_whenEmpty_returnsZero, "size_whenEmpty_returnsZero() fails with SIZE_IS_OFF_BY_ONE break"
                ),

                passesWhenUnsupported(MapMethods.SIZE, DynamicBrokenMapContract::size_whenEmpty_returnsZero, "size_whenEmpty_returnsZero() fails when not supported"
                ),

                failsWithBreak(BreakableMap.SIZE_ALWAYS_RETURNS_ZERO, DynamicBrokenMapContract::size_whenNotEmpty_returnsCorrectSize, "size_whenNotEmpty_returnsCorrectSize() fails with SIZE_ALWAYS_RETURNS_ZERO break"
                ),

                failsWithBreak(BreakableMap.SIZE_IS_OFF_BY_ONE, DynamicBrokenMapContract::size_whenNotEmpty_returnsCorrectSize, "size_whenNotEmpty_returnsCorrectSize() fails with SIZE_IS_OFF_BY_ONE break"
                ),

                passesWhenUnsupported(MapMethods.SIZE, DynamicBrokenMapContract::size_whenNotEmpty_returnsCorrectSize, "size_whenNotEmpty_returnsCorrectSize() fails when not supported"
                )
        );
    }

    /// Test factory for verifying that `isEmpty()` contract tests correctly identify failures.
    ///
    /// @return a collection of dynamic tests for `isEmpty()`
    /// @see IsEmptyContract
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForIsEmpty() {
        return Arrays.asList(
                failsWithBreak(BreakableMap.IS_EMPTY_ALWAYS_RETURNS_FALSE, DynamicBrokenMapContract::isEmpty_whenEmpty_returnsTrue, "isEmpty_whenEmpty_returnsTrue() fails with IS_EMPTY_ALWAYS_RETURNS_FALSE break"
                ),

                failsWithBreak(BreakableMap.IS_EMPTY_RETURNS_OPPOSITE_VALUE, DynamicBrokenMapContract::isEmpty_whenEmpty_returnsTrue, "isEmpty_whenEmpty_returnsTrue() fails with IS_EMPTY_RETURNS_OPPOSITE_VALUE break"
                ),

                failsWithBreak(BreakableMap.IS_EMPTY_ALWAYS_RETURNS_TRUE, DynamicBrokenMapContract::isEmpty_whenNotEmpty_returnsFalse, "isEmpty_whenNotEmpty_returnsFalse() fails with IS_EMPTY_ALWAYS_RETURNS_TRUE break"
                ),

                failsWithBreak(BreakableMap.IS_EMPTY_RETURNS_OPPOSITE_VALUE, DynamicBrokenMapContract::isEmpty_whenNotEmpty_returnsFalse, "isEmpty_whenNotEmpty_returnsFalse() fails with IS_EMPTY_RETURNS_OPPOSITE_VALUE break"
                )
        );
    }

    /// Test factory for verifying that `containsKey()` contract tests correctly identify failures.
    ///
    /// @return a collection of dynamic tests for `containsKey()`
    /// @see ContainsKeyContract
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForContainsKey() {
        return Arrays.asList(
                failsWithBreak(BreakableMap.CONTAINS_KEY_ALWAYS_RETURNS_FALSE, DynamicBrokenMapContract::containsKey_whenNotEmpty_returnsTrue, "containsKey_whenNotEmpty_returnsTrue() fails with CONTAINS_KEY_ALWAYS_RETURNS_FALSE break"
                ),

                failsWithBreak(BreakableMap.CONTAINS_KEY_RETURNS_OPPOSITE_VALUE, DynamicBrokenMapContract::containsKey_whenNotEmpty_returnsTrue, "containsKey_whenNotEmpty_returnsTrue() fails with CONTAINS_KEY_RETURNS_OPPOSITE_VALUE break"
                ),

                failsWithBreak(BreakableMap.CONTAINS_KEY_ALWAYS_RETURNS_TRUE, DynamicBrokenMapContract::containsKey_whenEmpty_returnsFalse, "containsKey_whenEmpty_returnsFalse() fails with CONTAINS_KEY_ALWAYS_RETURNS_TRUE break"
                ),

                failsWithBreak(BreakableMap.CONTAINS_KEY_RETURNS_OPPOSITE_VALUE, DynamicBrokenMapContract::containsKey_whenEmpty_returnsFalse, "containsKey_whenEmpty_returnsFalse() fails with CONTAINS_KEY_RETURNS_OPPOSITE_VALUE break"
                )
        );
    }

    /// Test factory for verifying that `get()` contract tests correctly identify failures.
    ///
    /// @return a collection of dynamic tests for `get()`
    /// @see GetContract
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForGet() {
        return Arrays.asList(
                failsWithBreak(BreakableMap.GET_ALWAYS_RETURNS_NULL, DynamicBrokenMapContract::get_whenNotEmpty_returnsCorrectValue, "get_whenNotEmpty_returnsCorrectValue() fails with GET_ALWAYS_RETURNS_NULL break"
                ),

                failsWithBreak(BreakableMap.GET_FAILS_FOR_FIRST_KEY, DynamicBrokenMapContract::get_whenNotEmpty_returnsCorrectValue, "get_whenNotEmpty_returnsCorrectValue() fails with GET_FAILS_FOR_FIRST_KEY break"
                )
        );
    }

    /// Test factory for verifying that `put()` contract tests correctly identify failures.
    ///
    /// @return a collection of dynamic tests for `put()`
    /// @see PutContract
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForPut() {
        return Arrays.asList(
                failsWithBreak(BreakableMap.PUT_DOES_NOT_ADD_PAIR, DynamicBrokenMapContract::put_whenKeyNotPresent_addsEntry, "put_whenKeyNotPresent_addsEntry() fails with PUT_DOES_NOT_ADD_PAIR break"
                ),

                failsWithBreak(BreakableMap.PUT_ALWAYS_RETURNS_NULL, DynamicBrokenMapContract::put_whenKeyPresent_updatesEntry, "put_whenKeyPresent_updatesEntry() fails with PUT_ALWAYS_RETURNS_NULL break"
                )
        );
    }

    /// Test factory for verifying that `remove()` contract tests correctly identify failures.
    ///
    /// @return a collection of dynamic tests for `remove()`
    /// @see RemoveContract
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForRemove() {
        return Arrays.asList(
                failsWithBreak(BreakableMap.REMOVE_DOES_NOT_REMOVE_KEY, DynamicBrokenMapContract::remove_whenKeyPresent_removesEntryAndReturnsValue, "remove_whenKeyPresent_removesEntryAndReturnsValue() fails with REMOVE_DOES_NOT_REMOVE_KEY break"
                ),

                failsWithBreak(BreakableMap.REMOVE_ALWAYS_RETURNS_NULL, DynamicBrokenMapContract::remove_whenKeyPresent_removesEntryAndReturnsValue, "remove_whenKeyPresent_removesEntryAndReturnsValue() fails with REMOVE_ALWAYS_RETURNS_NULL break"
                )
        );
    }

    /// Test factory for verifying that `clear()` contract tests correctly identify failures.
    ///
    /// @return a collection of dynamic tests for `clear()`
    /// @see ClearContract
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForClear() {
        return Collections.singletonList(
                failsWithBreak(BreakableMap.CLEAR_ALL_DOES_NOT_REMOVE_ANY_PAIRS, DynamicBrokenMapContract::clear_whenCalled_removesAllEntries, "clear_whenCalled_removesAllEntries() fails with CLEAR_ALL_DOES_NOT_REMOVE_ANY_PAIRS break"
                )
        );
    }
}
