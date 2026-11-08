package org.soliscode.test.contract.collection;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.soliscode.test.breakable.BreakableCollection;
import org.soliscode.test.breakable.BreakableCollectionBuilder;
import org.soliscode.test.contract.AbstractCollectionContractTest;
import org.soliscode.test.contract.CollectionContractConfig;
import org.soliscode.test.contract.DynamicCollectionContract;
import org.soliscode.test.provider.IntegerProvider;
import org.soliscode.test.provider.ObjectProvider;

import java.util.Arrays;
import java.util.Collection;

/// Tests for the CollectionContract class.
///
/// @author evanbergstrom
/// @since 1.0
@DisplayName("Tests for CollectionContract class")
public class AddContractTest extends AbstractCollectionContractTest<BreakableCollection<Integer>, Integer> {

    /// Default constructor
    public AddContractTest() {
        super();
    }

    protected static final class DynamicAddContract
            extends DynamicCollectionContract<BreakableCollection<Integer>, Integer>
            implements AddContract<BreakableCollection<Integer>, Integer> {

        public DynamicAddContract(
                final @NonNull BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                final @NonNull ObjectProvider<Integer> elementProvider,
                final @NonNull CollectionContractConfig config) {
            super(builder, elementProvider, config);
        }
    }

    @Override
    protected @NonNull DynamicAddContract
        createTest(final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                   final @NonNull CollectionContractConfig config) {

        return new DynamicAddContract(builder, new IntegerProvider(), config);
    }

    private BreakableCollection.Builder<Integer> builder() {
        return new BreakableCollection.Builder<>();
    }

    /// Test factory for validating the behavior of the `add(E)` method of a collection
    /// under various conditions, including failure scenarios caused by specific contract violations.
    ///
    /// This method generates a collection of dynamic tests that verify the following behaviors:
    ///
    ///   - The `add(E)` method correctly adds a single element, returns `true`,
    ///     and updates the size of the collection.
    ///   - Failure scenarios include:
    ///     - `ADD_DOES_NOT_ADD_ELEMENT`: The element is not added to the collection.
    ///     - `ADD_ALWAYS_RETURNS_FALSE`: The method always returns `false`,
    ///       regardless of whether the element is added or not.
    ///     - `ADD_ALWAYS_RETURNS_OPPOSITE_VALUE`: The method always returns the
    ///       opposite of the expected value.
    ///     - `ADD_THROWS_WRONG_UNSUPPORTED_EXCEPTION`: An incorrect exception is
    ///       thrown when the `add(E)` method is not supported.
    ///   - Validation of passing scenarios where the `add(E)` method is unsupported
    ///     and behaves as expected without any breaks.
    ///
    /// @return a collection of dynamic tests for the `add(E)` method, including both
    ///         passing and failing scenarios for various contract breaks.
    /// @see DynamicAddContract#add_singleElement_returnsTrueAndUpdatesSize
    @TestFactory
    public Collection<DynamicTest> testsForAdd() {
        return Arrays.asList(
                shouldPass(DynamicAddContract::add_singleElement_returnsTrueAndUpdatesSize,
                        "add(E) passes with no breaks",
                        builder()),

                shouldFail(DynamicAddContract::add_singleElement_returnsTrueAndUpdatesSize,
                        "add(E) fails with ADD_DOES_NOT_ADD_ELEMENT break",
                        builder().addBreak(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT)),

                shouldFail(DynamicAddContract::add_singleElement_returnsTrueAndUpdatesSize,
                        "add(E) fails with ADD_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(BreakableCollection.ADD_ALWAYS_RETURNS_FALSE)),

                shouldFail(DynamicAddContract::add_singleElement_returnsTrueAndUpdatesSize,
                        "add(E) fails with ADD_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.ADD_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicAddContract::add_singleElement_returnsTrueAndUpdatesSize,
                        "add(E) passes when not supported",
                        builder().doesNotSupport(CollectionMethods.ADD)),

                shouldPass(DynamicAddContract::add_singleElement_returnsTrueAndUpdatesSize,
                        "add(E) passes when not supported with ADD_THROWS_WRONG_UNSUPPORTED_EXCEPTION break, "
                                + "since it does not call add",
                        builder().doesNotSupport(CollectionMethods.ADD)
                                .addBreak(BreakableCollection.ADD_THROWS_WRONG_UNSUPPORTED_EXCEPTION))
        );
    }


    /// Generates a collection of dynamic tests to validate the behavior of the `add(E element)` method
    /// when `null` values are provided. The tests aim to ensure that the implementation adheres to the
    /// expected contract for handling `null` values in the collection, covering both expected and edge cases.
    ///
    /// This factory produces tests for the following scenarios:
    ///   - Tests that fail when the `add(E)` method does not add the `null` value, breaking the
    ///     `ADD_DOES_NOT_ADD_ELEMENT` contract.
    ///   - Tests that fail when the `add(E)` method always returns `false`, breaking the
    ///     `ADD_ALWAYS_RETURNS_FALSE` contract.
    ///   - Tests that fail when the `add(E)` method consistently returns the opposite of the expected
    ///     result, breaking the `ADD_ALWAYS_RETURNS_OPPOSITE_VALUE` contract.
    ///   - Tests that pass when the `add(E)` method handles `null` values appropriately or does not
    ///     support the operation as specified in the implementation.
    ///
    /// Each dynamic test description includes details of the specific break in the contract, aiding
    /// in identifying and fixing the issue in the collection's behavior.
    ///
    /// @return A collection of dynamic tests that evaluate contract compliance for `add(E)` with `null` values.
    /// @see DynamicAddContract#add_withNullValue_handlesCorrectly
    @TestFactory
    public Collection<DynamicTest> testsForAddWithNullValues() {
        return Arrays.asList(
                shouldPass(DynamicAddContract::add_withNullValue_handlesCorrectly,
                        "add(E) with null value passes with no breaks",
                        builder()),

                shouldFail(DynamicAddContract::add_withNullValue_handlesCorrectly,
                        "add(E) with null value fails with ADD_DOES_NOT_ADD_ELEMENT break",
                        builder().addBreak(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT)),

                shouldFail(DynamicAddContract::add_withNullValue_handlesCorrectly,
                        "add(E) with null value fails with ADD_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(BreakableCollection.ADD_ALWAYS_RETURNS_FALSE)),

                shouldFail(DynamicAddContract::add_withNullValue_handlesCorrectly,
                        "add(E) with null value fails with ADD_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.ADD_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicAddContract::add_withNullValue_handlesCorrectly,
                        "add(E) with null values passes when not supported",
                        builder().doesNotSupport(CollectionMethods.ADD)),

                shouldFail(DynamicAddContract::add_withNullValue_handlesCorrectly, CONTRACT_EXPECTS_NO_NULL_SUPPORT,
                        "add(E) with null value fails nulls are supported and contract expects no null support",
                        builder()),

                shouldPass(DynamicAddContract::add_withNullValue_handlesCorrectly, CONTRACT_EXPECTS_NO_NULL_SUPPORT,
                        "add(E) with null value fails nulls are not  supported and contract expects no null support",
                        builder().doesNotPermitNulls())

        );
    }


    /// Generates a collection of dynamic tests for verifying the behavior of the `add(E)` method
    /// when handling duplicate values in a collection. Each test assesses whether the implementation
    /// adheres to expected behaviors or fails due to specific contract-breaking scenarios.
    ///
    /// The test cases include:
    ///   - Scenarios where adding duplicate elements fails due to specific breaks:
    ///     - `ADD_DOES_NOT_ADD_ELEMENT` - The duplicate element is not added when it should be.
    ///       - `ADD_ALWAYS_RETURNS_FALSE` - The addition operation incorrectly always returns `false`.
    ///       - `ADD_ALWAYS_RETURNS_OPPOSITE_VALUE` - The addition operation returns the opposite of the expected value.
    ///       - A scenario where adding duplicate values is supported and does not encounter related issues.
    ///
    /// @return A collection of [DynamicTest] instances, each comprising a scenario to verify
    ///         the behavior of the `add(E)` method with duplicate values. These tests help assess
    ///         the contract compliance or breakages of the implementation.
    /// @see DynamicAddContract#add_withDuplicateValue_handlesCorrectly
    @TestFactory
    public Collection<DynamicTest> testsForAddWithDuplicateValues() {
        return Arrays.asList(
                shouldPass(DynamicAddContract::add_withDuplicateValue_handlesCorrectly,
                        "add(E) with duplicate values passes with no breaks",
                        builder()),

                shouldFail(DynamicAddContract::add_withDuplicateValue_handlesCorrectly,
                        "add(E) with duplicate values fails with ADD_DOES_NOT_ADD_ELEMENT break",
                        builder().addBreak(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT)),

                shouldFail(DynamicAddContract::add_withDuplicateValue_handlesCorrectly,
                        "add(E) with duplicate values fails with ADD_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(BreakableCollection.ADD_ALWAYS_RETURNS_FALSE)),

                shouldFail(DynamicAddContract::add_withDuplicateValue_handlesCorrectly,
                        "add(E) with duplicate values fails with ADD_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.ADD_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicAddContract::add_withDuplicateValue_handlesCorrectly,
                        "add(E) with duplicate values passes when not supported",
                        builder().doesNotSupport(CollectionMethods.ADD))
        );

    }

    /// Test factory for tests of the add_withIncompatibleType_throwsClassCastException()
    /// method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `add_withIncompatibleType_throwsClassCastException` method.
    /// @see AddContract#add_withIncompatibleType_handlesCorrectly
    @TestFactory
    public Collection<DynamicTest> testsForAddWithIncompatibleType() {
        return Arrays.asList(
                shouldFail(DynamicAddContract::add_withIncompatibleType_handlesCorrectly,
                        "add(E) with incompatible type fails with ADD_DOES_NOT_ADD_ELEMENT break",
                        builder().addBreak(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT)),

                shouldFail(DynamicAddContract::add_withIncompatibleType_handlesCorrectly,
                        "add(E) with incompatible type fails with ADD_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(BreakableCollection.ADD_ALWAYS_RETURNS_FALSE)),

                shouldFail(DynamicAddContract::add_withIncompatibleType_handlesCorrectly,
                        "add(E) with incompatible type fails with ADD_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.ADD_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldFail(DynamicAddContract::add_withIncompatibleType_handlesCorrectly,
                        "add(E) with incompatible type fails with ADD_THROWS_WRONG_INCOMPATIBLE_TYPE_EXCEPTION break",
                        builder().doesNotPermitIncompatibleTypes(Integer.class)
                                .addBreak(BreakableCollection.ADD_THROWS_WRONG_INCOMPATIBLE_TYPE_EXCEPTION)),

                shouldPass(DynamicAddContract::add_withIncompatibleType_handlesCorrectly,
                        "add(E) with duplicate values passes when not supported",
                        builder().doesNotSupport(CollectionMethods.ADD))
        );
    }

    /// Test factory for tests of the add_whenNotSupported_throwsUnsupportedOperationException()
    /// method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `add_whenNotSupported_throwsUnsupportedOperationException` method.
    /// @see AddContract#add_whenNotSupported_throwsUnsupportedOperationException
    @TestFactory
    public Collection<DynamicTest> testsForAddWhenUnsupported() {
        return Arrays.asList(
                shouldPass(DynamicAddContract::add_whenNotSupported_throwsUnsupportedOperationException,
                        "throws UnsupportedOperationException when not supported",
                        builder().doesNotSupport(CollectionMethods.ADD)),

                shouldFail(DynamicAddContract::add_whenNotSupported_throwsUnsupportedOperationException,
                        "add(E) throws wrong exception with ADD_THROWS_WRONG_UNSUPPORTED_EXCEPTION break",
                        builder().doesNotSupport(CollectionMethods.ADD)
                                .addBreak(BreakableCollection.ADD_THROWS_WRONG_UNSUPPORTED_EXCEPTION))
        );
    }
 }