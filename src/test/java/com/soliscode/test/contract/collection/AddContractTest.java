package com.soliscode.test.contract.collection;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import com.soliscode.test.breakable.BreakableCollection;
import com.soliscode.test.breakable.BreakableCollectionBuilder;
import com.soliscode.test.contract.AbstractCollectionContractTest;
import com.soliscode.test.contract.CollectionContractConfig;
import com.soliscode.test.contract.DynamicCollectionContract;
import com.soliscode.test.provider.IntegerProvider;
import com.soliscode.test.provider.ObjectProvider;

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
                final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                final ObjectProvider<Integer> elementProvider,
                final CollectionContractConfig config) {
            super(builder, elementProvider, config);
        }
    }

    @Override
    protected DynamicAddContract
        createTest(final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                   final CollectionContractConfig config) {

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
    /// @see DynamicAddContract#addSingleElementReturnsTrueAndUpdatesSize
    @TestFactory
    @DisplayName("add(E) contract tests fail for each break of add and pass when add is not supported")
    public Collection<DynamicTest> testsForAdd() {
        return Arrays.asList(
                shouldPass(DynamicAddContract::addSingleElementReturnsTrueAndUpdatesSize,
                        "add(E) passes with no breaks",
                        builder()),

                shouldFail(DynamicAddContract::addSingleElementReturnsTrueAndUpdatesSize,
                        "add(E) fails with ADD_DOES_NOT_ADD_ELEMENT break",
                        builder().addBreak(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT)),

                shouldFail(DynamicAddContract::addSingleElementReturnsTrueAndUpdatesSize,
                        "add(E) fails with ADD_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(BreakableCollection.ADD_ALWAYS_RETURNS_FALSE)),

                shouldFail(DynamicAddContract::addSingleElementReturnsTrueAndUpdatesSize,
                        "add(E) fails with ADD_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.ADD_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicAddContract::addSingleElementReturnsTrueAndUpdatesSize,
                        "add(E) passes when not supported",
                        builder().doesNotSupport(CollectionMethods.ADD)),

                shouldPass(DynamicAddContract::addSingleElementReturnsTrueAndUpdatesSize,
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
    /// @see DynamicAddContract#addWithNullValueHandlesCorrectly
    @TestFactory
    @DisplayName("add(E) contract tests with a null value fail for each break and follow the null policy")
    public Collection<DynamicTest> testsForAddWithNullValues() {
        return Arrays.asList(
                shouldPass(DynamicAddContract::addWithNullValueHandlesCorrectly,
                        "add(E) with null value passes with no breaks",
                        builder()),

                shouldFail(DynamicAddContract::addWithNullValueHandlesCorrectly,
                        "add(E) with null value fails with ADD_DOES_NOT_ADD_ELEMENT break",
                        builder().addBreak(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT)),

                shouldFail(DynamicAddContract::addWithNullValueHandlesCorrectly,
                        "add(E) with null value fails with ADD_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(BreakableCollection.ADD_ALWAYS_RETURNS_FALSE)),

                shouldFail(DynamicAddContract::addWithNullValueHandlesCorrectly,
                        "add(E) with null value fails with ADD_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.ADD_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicAddContract::addWithNullValueHandlesCorrectly,
                        "add(E) with null values passes when not supported",
                        builder().doesNotSupport(CollectionMethods.ADD)),

                shouldFail(DynamicAddContract::addWithNullValueHandlesCorrectly, CONTRACT_EXPECTS_NO_NULL_SUPPORT,
                        "add(E) with null value fails nulls are supported and contract expects no null support",
                        builder()),

                shouldPass(DynamicAddContract::addWithNullValueHandlesCorrectly, CONTRACT_EXPECTS_NO_NULL_SUPPORT,
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
    /// @see DynamicAddContract#addWithDuplicateValueHandlesCorrectly
    @TestFactory
    @DisplayName("add(E) contract tests with a duplicate value fail for each break of add")
    public Collection<DynamicTest> testsForAddWithDuplicateValues() {
        return Arrays.asList(
                shouldPass(DynamicAddContract::addWithDuplicateValueHandlesCorrectly,
                        "add(E) with duplicate values passes with no breaks",
                        builder()),

                shouldFail(DynamicAddContract::addWithDuplicateValueHandlesCorrectly,
                        "add(E) with duplicate values fails with ADD_DOES_NOT_ADD_ELEMENT break",
                        builder().addBreak(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT)),

                shouldFail(DynamicAddContract::addWithDuplicateValueHandlesCorrectly,
                        "add(E) with duplicate values fails with ADD_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(BreakableCollection.ADD_ALWAYS_RETURNS_FALSE)),

                shouldFail(DynamicAddContract::addWithDuplicateValueHandlesCorrectly,
                        "add(E) with duplicate values fails with ADD_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.ADD_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicAddContract::addWithDuplicateValueHandlesCorrectly,
                        "add(E) with duplicate values passes when not supported",
                        builder().doesNotSupport(CollectionMethods.ADD))
        );

    }

    /// Test factory for tests of the addWithIncompatibleTypeHandlesCorrectly()
    /// method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `addWithIncompatibleTypeHandlesCorrectly` method.
    /// @see AddContract#addWithIncompatibleTypeHandlesCorrectly
    @TestFactory
    @DisplayName("add(E) contract tests with an incompatible type fail for each break of add")
    public Collection<DynamicTest> testsForAddWithIncompatibleType() {
        return Arrays.asList(
                shouldFail(DynamicAddContract::addWithIncompatibleTypeHandlesCorrectly,
                        "add(E) with incompatible type fails with ADD_DOES_NOT_ADD_ELEMENT break",
                        builder().addBreak(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT)),

                shouldFail(DynamicAddContract::addWithIncompatibleTypeHandlesCorrectly,
                        "add(E) with incompatible type fails with ADD_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(BreakableCollection.ADD_ALWAYS_RETURNS_FALSE)),

                shouldFail(DynamicAddContract::addWithIncompatibleTypeHandlesCorrectly,
                        "add(E) with incompatible type fails with ADD_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.ADD_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldFail(DynamicAddContract::addWithIncompatibleTypeHandlesCorrectly,
                        "add(E) with incompatible type fails with ADD_THROWS_WRONG_INCOMPATIBLE_TYPE_EXCEPTION break",
                        builder().doesNotPermitIncompatibleTypes(Integer.class)
                                .addBreak(BreakableCollection.ADD_THROWS_WRONG_INCOMPATIBLE_TYPE_EXCEPTION)),

                shouldPass(DynamicAddContract::addWithIncompatibleTypeHandlesCorrectly,
                        "add(E) with duplicate values passes when not supported",
                        builder().doesNotSupport(CollectionMethods.ADD))
        );
    }

    /// Test factory for tests of the addWhenNotSupportedThrowsUnsupportedOperationException()
    /// method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `addWhenNotSupportedThrowsUnsupportedOperationException` method.
    /// @see AddContract#addWhenNotSupportedThrowsUnsupportedOperationException
    @TestFactory
    @DisplayName("add(E) contract tests when add is not supported fail for the wrong-exception break")
    public Collection<DynamicTest> testsForAddWhenUnsupported() {
        return Arrays.asList(
                shouldPass(DynamicAddContract::addWhenNotSupportedThrowsUnsupportedOperationException,
                        "throws UnsupportedOperationException when not supported",
                        builder().doesNotSupport(CollectionMethods.ADD)),

                shouldFail(DynamicAddContract::addWhenNotSupportedThrowsUnsupportedOperationException,
                        "add(E) throws wrong exception with ADD_THROWS_WRONG_UNSUPPORTED_EXCEPTION break",
                        builder().doesNotSupport(CollectionMethods.ADD)
                                .addBreak(BreakableCollection.ADD_THROWS_WRONG_UNSUPPORTED_EXCEPTION))
        );
    }
 }
