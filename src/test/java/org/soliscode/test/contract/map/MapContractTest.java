package org.soliscode.test.contract.map;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.breakable.Break;
import org.soliscode.test.breakable.BreakableMap;
import org.soliscode.test.breakable.BreakableMapBuilder;
import org.soliscode.test.contract.AbstractContractTest;
import org.soliscode.test.contract.DynamicMapContract;
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
/// - Dynamic generation of tests for broken map behaviors using [DynamicMapTestContract].
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
public class MapContractTest extends AbstractContractTest<BreakableMap<Integer, String>,
        BreakableMapBuilder<BreakableMap<Integer, String>, Integer, String>> {

    /// Default constructor.
    public MapContractTest() {
        super();
    }

    /// Tests for working map implementations.
    ///
    /// This nested class verifies that [MapContract] correctly passes when applied
    /// to a [BreakableMap] that has no active breaks and supports all operations.
    @Nested
    class WorkingMapTest extends AbstractTest
            implements MapContract<BreakableMap<Integer, String>, Integer, String>,
            BreakableMap.WithProvider<Integer, String>, WithIntegerKeyAndStringValue<BreakableMap<Integer, String>> {
    }

    /// A map contract for maps built by a builder.
    protected static final class DynamicMapTestContract
            extends DynamicMapContract<BreakableMap<Integer, String>, Integer, String>
            implements MapContract<BreakableMap<Integer, String>, Integer, String> {

        /// Creates the contract.
        ///
        /// @param builder the builder of the maps under test
        public DynamicMapTestContract(
                final @NonNull BreakableMapBuilder<BreakableMap<Integer, String>, Integer, String> builder) {
            super(builder, WithIntegerKeyAndStringValue.KEY_PROVIDER, WithIntegerKeyAndStringValue.VALUE_PROVIDER);
        }
    }

    @Override
    protected @NonNull DynamicMapTestContract createTest(
            final @NonNull BreakableMapBuilder<BreakableMap<Integer, String>, Integer, String> builder) {
        return new DynamicMapTestContract(builder);
    }

    private static BreakableMap.Builder<Integer, String> builder() {
        return new BreakableMap.Builder<>();
    }

    /// Test factory for verifying that `size()` contract tests correctly identify failures and unsupported operations.
    ///
    /// @return a collection of dynamic tests for `size()`
    /// @see SizeContract
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForSize() {
        return Arrays.asList(
                shouldFail(DynamicMapTestContract::size_whenEmpty_returnsZero,
                        "size_whenEmpty_returnsZero() fails with SIZE_IS_OFF_BY_ONE break",
                        builder().addBreak(BreakableMap.SIZE_IS_OFF_BY_ONE)),

                shouldPass(DynamicMapTestContract::size_whenEmpty_returnsZero,
                        "size_whenEmpty_returnsZero() passes when not supported",
                        builder().doesNotSupport(MapMethods.SIZE)),

                shouldFail(DynamicMapTestContract::size_whenNotEmpty_returnsCorrectSize,
                        "size_whenNotEmpty_returnsCorrectSize() fails with SIZE_ALWAYS_RETURNS_ZERO break",
                        builder().addBreak(BreakableMap.SIZE_ALWAYS_RETURNS_ZERO)),

                shouldFail(DynamicMapTestContract::size_whenNotEmpty_returnsCorrectSize,
                        "size_whenNotEmpty_returnsCorrectSize() fails with SIZE_IS_OFF_BY_ONE break",
                        builder().addBreak(BreakableMap.SIZE_IS_OFF_BY_ONE)),

                shouldPass(DynamicMapTestContract::size_whenNotEmpty_returnsCorrectSize,
                        "size_whenNotEmpty_returnsCorrectSize() passes when not supported",
                        builder().doesNotSupport(MapMethods.SIZE))
        );
    }

    /// Test factory for verifying that `isEmpty()` contract tests correctly identify failures.
    ///
    /// @return a collection of dynamic tests for `isEmpty()`
    /// @see IsEmptyContract
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForIsEmpty() {
        return Arrays.asList(
                shouldFail(DynamicMapTestContract::isEmpty_whenEmpty_returnsTrue,
                        "isEmpty_whenEmpty_returnsTrue() fails with IS_EMPTY_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(BreakableMap.IS_EMPTY_ALWAYS_RETURNS_FALSE)),

                shouldFail(DynamicMapTestContract::isEmpty_whenEmpty_returnsTrue,
                        "isEmpty_whenEmpty_returnsTrue() fails with IS_EMPTY_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableMap.IS_EMPTY_RETURNS_OPPOSITE_VALUE)),

                shouldFail(DynamicMapTestContract::isEmpty_whenNotEmpty_returnsFalse,
                        "isEmpty_whenNotEmpty_returnsFalse() fails with IS_EMPTY_ALWAYS_RETURNS_TRUE break",
                        builder().addBreak(BreakableMap.IS_EMPTY_ALWAYS_RETURNS_TRUE)),

                shouldFail(DynamicMapTestContract::isEmpty_whenNotEmpty_returnsFalse,
                        "isEmpty_whenNotEmpty_returnsFalse() fails with IS_EMPTY_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableMap.IS_EMPTY_RETURNS_OPPOSITE_VALUE))
        );
    }

    /// Test factory for verifying that `containsKey()` contract tests correctly identify failures.
    ///
    /// @return a collection of dynamic tests for `containsKey()`
    /// @see ContainsKeyContract
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForContainsKey() {
        return Arrays.asList(
                shouldFail(DynamicMapTestContract::containsKey_whenNotEmpty_returnsTrue,
                        "containsKey_whenNotEmpty_returnsTrue() fails with CONTAINS_KEY_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(BreakableMap.CONTAINS_KEY_ALWAYS_RETURNS_FALSE)),

                shouldFail(DynamicMapTestContract::containsKey_whenNotEmpty_returnsTrue,
                        "containsKey_whenNotEmpty_returnsTrue() fails with CONTAINS_KEY_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableMap.CONTAINS_KEY_RETURNS_OPPOSITE_VALUE)),

                shouldFail(DynamicMapTestContract::containsKey_whenEmpty_returnsFalse,
                        "containsKey_whenEmpty_returnsFalse() fails with CONTAINS_KEY_ALWAYS_RETURNS_TRUE break",
                        builder().addBreak(BreakableMap.CONTAINS_KEY_ALWAYS_RETURNS_TRUE)),

                shouldFail(DynamicMapTestContract::containsKey_whenEmpty_returnsFalse,
                        "containsKey_whenEmpty_returnsFalse() fails with CONTAINS_KEY_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableMap.CONTAINS_KEY_RETURNS_OPPOSITE_VALUE))
        );
    }

    /// Test factory for verifying that `get()` contract tests correctly identify failures.
    ///
    /// @return a collection of dynamic tests for `get()`
    /// @see GetContract
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForGet() {
        return Arrays.asList(
                shouldFail(DynamicMapTestContract::get_whenNotEmpty_returnsCorrectValue,
                        "get_whenNotEmpty_returnsCorrectValue() fails with GET_ALWAYS_RETURNS_NULL break",
                        builder().addBreak(BreakableMap.GET_ALWAYS_RETURNS_NULL)),

                shouldFail(DynamicMapTestContract::get_whenNotEmpty_returnsCorrectValue,
                        "get_whenNotEmpty_returnsCorrectValue() fails with GET_FAILS_FOR_FIRST_KEY break",
                        builder().addBreak(BreakableMap.GET_FAILS_FOR_FIRST_KEY))
        );
    }

    /// Test factory for verifying that `put()` contract tests correctly identify failures.
    ///
    /// @return a collection of dynamic tests for `put()`
    /// @see PutContract
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForPut() {
        return Arrays.asList(
                shouldFail(DynamicMapTestContract::put_whenKeyNotPresent_addsEntry,
                        "put_whenKeyNotPresent_addsEntry() fails with PUT_DOES_NOT_ADD_PAIR break",
                        builder().addBreak(BreakableMap.PUT_DOES_NOT_ADD_PAIR)),

                shouldFail(DynamicMapTestContract::put_whenKeyPresent_updatesEntry,
                        "put_whenKeyPresent_updatesEntry() fails with PUT_ALWAYS_RETURNS_NULL break",
                        builder().addBreak(BreakableMap.PUT_ALWAYS_RETURNS_NULL))
        );
    }

    /// Test factory for verifying that `remove()` contract tests correctly identify failures.
    ///
    /// @return a collection of dynamic tests for `remove()`
    /// @see RemoveContract
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForRemove() {
        return Arrays.asList(
                shouldFail(DynamicMapTestContract::remove_whenKeyPresent_removesEntryAndReturnsValue,
                        "remove_whenKeyPresent_removesEntryAndReturnsValue() fails with REMOVE_DOES_NOT_REMOVE_KEY break",
                        builder().addBreak(BreakableMap.REMOVE_DOES_NOT_REMOVE_KEY)),

                shouldFail(DynamicMapTestContract::remove_whenKeyPresent_removesEntryAndReturnsValue,
                        "remove_whenKeyPresent_removesEntryAndReturnsValue() fails with REMOVE_ALWAYS_RETURNS_NULL break",
                        builder().addBreak(BreakableMap.REMOVE_ALWAYS_RETURNS_NULL))
        );
    }

    /// Test factory for verifying that `clear()` contract tests correctly identify failures.
    ///
    /// @return a collection of dynamic tests for `clear()`
    /// @see ClearContract
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForClear() {
        return Collections.singletonList(
                shouldFail(DynamicMapTestContract::clear_whenCalled_removesAllEntries,
                        "clear_whenCalled_removesAllEntries() fails with CLEAR_ALL_DOES_NOT_REMOVE_ANY_PAIRS break",
                        builder().addBreak(BreakableMap.CLEAR_ALL_DOES_NOT_REMOVE_ANY_PAIRS))
        );
    }
}
