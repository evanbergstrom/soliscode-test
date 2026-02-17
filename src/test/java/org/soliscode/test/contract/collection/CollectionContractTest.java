package org.soliscode.test.contract.collection;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.breakable.Break;
import org.soliscode.test.breakable.BreakableCollection;
import org.soliscode.test.contract.ContractTest;
import org.soliscode.test.contract.DynamicContract;
import org.soliscode.test.contract.dynamic.DynamicBrokenCollectionContract;
import org.soliscode.test.contract.support.WithIntegerElement;

import java.util.Arrays;
import java.util.Collection;

/// Tests for the CollectionContract class.
///
/// @author evanbergstrom
/// @since 1.0
@DisplayName("Tests for CollectionContract class")
public class CollectionContractTest extends ContractTest<BreakableCollection<Integer>> {


    /// Unit tests for verifying the behavior and contract compliance of [BreakableCollection].
    ///
    /// This test class is a combination of the following components:
    /// - [AbstractTest]: Serves as a base class, providing utility support for contract testing.
    /// - [CollectionContract]: Implements the comprehensive test suite for validating the
    ///   [Collection] interface methods, ensuring full compliance with its specifications.
    /// - [BreakableCollection.WithProvider]: Supplies a consistent implementation of the
    ///   `provider()` method, tailored for creating unbroken [BreakableCollection] instances.
    /// - [WithIntegerElement]: Mixin interface providing an element provider specifically for
    ///   [Integer] instances to be used in testing.
    ///
    /// ### Responsibilities
    /// This test class ensures the following:
    /// - All methods of the `Collection` interface are tested for correct functionality as prescribed
    ///   by the [CollectionContract].
    /// - Instances of [BreakableCollection] are supplied in a consistent and reusable manner using
    ///   the [BreakableCollection.WithProvider] interface.
    /// - Test cases use [Integer] elements, provided by the implementation of [WithIntegerElement].
    ///
    /// ### Usage Context
    /// Use this class to validate implementations of [BreakableCollection] to ensure:
    /// - Adherence to the contract defined by [CollectionContract].
    /// - No breaks exist unless explicitly configured during the test setup.
    /// - Correct handling of integer-based elements.
    @Nested
    class WorkingCollectionTest extends AbstractTest
            implements CollectionContract<Integer, BreakableCollection<Integer>>,
            BreakableCollection.WithProvider<Integer>, WithIntegerElement {
    }


    @Override
    protected @NonNull DynamicContract<?,?> createTest(final @NonNull Break b, final @NonNull InterfaceMethod m) {
        return new DynamicBrokenCollectionContract(b, m);
    }

    /// Test factory for tests of the add_singleElement_returnsTrueAndUpdatesSize method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `add_singleElement_returnsTrueAndUpdatesSize` method.
    /// @see AddContract#add_singleElement_returnsTrueAndUpdatesSize
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForAdd() {
        return Arrays.asList(
                failsWithBreak(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT,
                        DynamicBrokenCollectionContract::add_singleElement_returnsTrueAndUpdatesSize,
                        "add(E) fails with ADD_DOES_NOT_ADD_ELEMENT break"
                ),
                failsWithBreak(BreakableCollection.ADD_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenCollectionContract::add_singleElement_returnsTrueAndUpdatesSize,
                        "add(E) fails with ADD_ALWAYS_RETURNS_FALSE break"
                ),
                failsWithBreak(BreakableCollection.ADD_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::add_singleElement_returnsTrueAndUpdatesSize,
                        "add(E) fails with ADD_ALWAYS_RETURNS_OPPOSITE_VALUE break"
                ),
                passesWhenUnsupported(CollectionMethods.ADD,
                        DynamicBrokenCollectionContract::add_singleElement_returnsTrueAndUpdatesSize,
                        "add(E) passes when not supported"
                )
        );
    }

    /// Test factory for tests of the add_withNullValue_handlesCorrectly() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `add_withNullValue_handlesCorrectly` method.
    /// @see AddContract#add_withNullValue_handlesCorrectly
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForAddWithNullValues() {
        return Arrays.asList(
                failsWithBreak(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT,
                        DynamicBrokenCollectionContract::add_withNullValue_handlesCorrectly,
                        "add(E) with null value fails with ADD_DOES_NOT_ADD_ELEMENT break"
                ),
                failsWithBreak(BreakableCollection.ADD_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenCollectionContract::add_withNullValue_handlesCorrectly,
                        "add(E) with null value fails with ADD_ALWAYS_RETURNS_FALSE break"
                ),

                failsWithBreak(BreakableCollection.ADD_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::add_withNullValue_handlesCorrectly,
                        "add(E) with null values fails with ADD_ALWAYS_RETURNS_OPPOSITE_VALUE break"
                ),
                passesWhenUnsupported(CollectionMethods.ADD,
                        DynamicBrokenCollectionContract::add_withNullValue_handlesCorrectly,
                        "add(E) with null value passes when not supported"
                )
        );
    }

    /// Test factory for tests of the add_withDuplicateValue_handlesCorrectly()
    /// method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `add_withDuplicateValue_handlesCorrectly` method.
    /// @see AddContract#add_withDuplicateValue_handlesCorrectly
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForAddWithDuplicateValues() {
        return Arrays.asList(
                failsWithBreak(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT,
                        DynamicBrokenCollectionContract::add_withDuplicateValue_handlesCorrectly,
                        "add(E) with duplicate values fails with ADD_DOES_NOT_ADD_ELEMENT break"
                ),

                failsWithBreak(BreakableCollection.ADD_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenCollectionContract::add_withDuplicateValue_handlesCorrectly,
                        "add(E) with duplicate values fails with ADD_ALWAYS_RETURNS_FALSE break"
                ),

                failsWithBreak(BreakableCollection.ADD_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::add_withDuplicateValue_handlesCorrectly,
                        "add(E) with duplicate values fails with ADD_ALWAYS_RETURNS_OPPOSITE_VALUE break"
                ),
                passesWhenUnsupported(CollectionMethods.ADD,
                        DynamicBrokenCollectionContract::add_withDuplicateValue_handlesCorrectly,
                        "add(E) with duplicate values fails when not supported"
                )
        );
    }

    /// Test factory for tests of the add_withIncompatibleType_throwsClassCastException()
    /// method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `add_withIncompatibleType_throwsClassCastException` method.
    /// @see AddContract#add_withIncompatibleType_throwsClassCastException
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForAddWithIncompatibleType() {
        return Arrays.asList(
                failsWithBreak(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT,
                        DynamicBrokenCollectionContract::add_withDuplicateValue_handlesCorrectly,
                        "add(E) with duplicate values fails with ADD_DOES_NOT_ADD_ELEMENT break"
                ),
//                failsWithBreak(BreakableCollection.ADD_THROWS_WRONG_INCOMPATIBLE_TYPE_EXCEPTION,
//                    DynamicBrokenCollectionContract::add_withIncompatibleType_throwsClassCastException,
//                    "add(E) with incompatible type fails with ADD_THROWS_WRONG_INCOMPATIBLE_TYPE_EXCEPTION break"
//                ),
                passesWhenUnsupported(CollectionMethods.ADD,
                        DynamicBrokenCollectionContract::add_withIncompatibleType_throwsClassCastException,
                        "add(E) with incompatible type fails when not supported"
                )
        );
    }

    /// Test factory for tests of the add_whenNotSupported_throwsUnsupportedOperationException()
    /// method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `add_whenNotSupported_throwsUnsupportedOperationException` method.
    /// @see AddContract#add_whenNotSupported_throwsUnsupportedOperationException
    public Collection<DynamicTest> dynamicTestsForAddWhenUnsupported() {
        return Arrays.asList(
                passesWhenUnsupported(CollectionMethods.ADD,
                        DynamicBrokenCollectionContract::add_whenNotSupported_throwsUnsupportedOperationException,
                        "add(E) throws UnsupportedOperationException when not supported"
                ),
                failsWithUnsupportedBreak(CollectionMethods.ADD,
                        BreakableCollection.ADD_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION,
                        DynamicBrokenCollectionContract::add_whenNotSupported_throwsUnsupportedOperationException,
                        "add(E) throws wrong exception with ADD_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION break"
                )
        );
    }


    /// Test factory for tests of the addAll(Collection) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the addAll(Collection) method.
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForAddAll() {
        return Arrays.asList(
                failsWithBreak(BreakableCollection.ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS,
                        DynamicBrokenCollectionContract::addAll_whenContainerIsEmpty_addsAllArgumentElements,
                        "addAll_whenContainerIsEmpty_addsAllArgumentElements() fails with ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS break"
                ),

                failsWithBreak(BreakableCollection.ADD_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::addAll_whenContainerIsEmpty_addsAllArgumentElements,
                        "addAll_whenContainerIsEmpty_addsAllArgumentElements() fails with ADD_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break"
                ),

                failsWithBreak(BreakableCollection.ADD_ALL_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenCollectionContract::addAll_whenContainerIsEmpty_addsAllArgumentElements,
                        "addAll_whenContainerIsEmpty_addsAllArgumentElements() fails with ADD_ALL_ALWAYS_RETURNS_FALSE break"
                ),

                failsWithBreak(BreakableCollection.ADD_ALL_SKIPS_FIRST_ELEMENT,
                        DynamicBrokenCollectionContract::addAll_whenContainerIsEmpty_addsAllArgumentElements,
                        "addAll_whenContainerIsEmpty_addsAllArgumentElements() fails with ADD_ALL_SKIPS_FIRST_ELEMENT break"
                ),

                failsWithBreak(BreakableCollection.ADD_ALL_SKIPS_LAST_ELEMENT,
                        DynamicBrokenCollectionContract::addAll_whenContainerIsEmpty_addsAllArgumentElements,
                        "addAll_whenContainerIsEmpty_addsAllArgumentElements() fails with ADD_ALL_SKIPS_LAST_ELEMENT break"
                ),

                passesWhenUnsupported(CollectionMethods.ADD_ALL,
                        DynamicBrokenCollectionContract::addAll_whenContainerIsEmpty_addsAllArgumentElements,
                        "addAll_whenContainerIsEmpty_addsAllArgumentElements() fails when not supported"
                )
        );
    }

    /// Test factory for tests of the clear() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the clear() method.
    /// @see ClearContract#clear_whenEmpty_isSuccessful
    /// @see ClearContract#clear_whenNotEmpty_removesAllElements
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForClear() {
        return Arrays.asList(
                passesWhenUnsupported(CollectionMethods.CLEAR, DynamicBrokenCollectionContract::clear_whenEmpty_isSuccessful, "clear() works for an empty collection fails when not supported"
                ),

                failsWithUnsupportedBreak(CollectionMethods.CLEAR, BreakableCollection.CLEAR_THROWS_WRONG_UNSUPPORTED_EXCEPTION, DynamicBrokenCollectionContract::clear_whenEmpty_isSuccessful, "clear() works for an empty collection fails with CLEAR_THROWS_WRONG_UNSUPPORTED_EXCEPTION break"
                ),

                failsWithBreak(BreakableCollection.CLEAR_DOES_NOT_REMOVE_ANY_ELEMENTS, DynamicBrokenCollectionContract::clear_whenNotEmpty_removesAllElements, "clear() works for a collection with elements fails with CLEAR_DOES_NOT_REMOVE_ANY_ELEMENTS break"
                ),

                failsWithBreak(BreakableCollection.CLEAR_SKIPS_FIRST_ELEMENT, DynamicBrokenCollectionContract::clear_whenNotEmpty_removesAllElements, "clear() works for a collection with elements fails with CLEAR_SKIPS_FIRST_ELEMENT break"
                ),

                failsWithBreak(BreakableCollection.CLEAR_SKIPS_LAST_ELEMENT, DynamicBrokenCollectionContract::clear_whenNotEmpty_removesAllElements, "clear() works for a collection with elements fails with CLEAR_SKIPS_LAST_ELEMENT break"
                ),

                passesWhenUnsupported(CollectionMethods.CLEAR, DynamicBrokenCollectionContract::clear_whenNotEmpty_removesAllElements, "clear() works for a collection with elements fails when not supported"
                ),

                failsWithUnsupportedBreak(CollectionMethods.CLEAR, BreakableCollection.CLEAR_THROWS_WRONG_UNSUPPORTED_EXCEPTION, DynamicBrokenCollectionContract::clear_whenNotEmpty_removesAllElements, "clear() works for a collection with elements fails with CLEAR_THROWS_WRONG_UNSUPPORTED_EXCEPTION break"
                )
                );
    }

    /// Test factory for tests of the contains(Object) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the contains(Object) method.
    /// @see ContainsContract#contains_whenNotEmpty_returnsExpectedResults
    /// @see ContainsContract#contains_whenEmpty_returnsFalse
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForContains() {
        return Arrays.asList(
                failsWithBreak(BreakableCollection.CONTAINS_ALWAYS_RETURNS_TRUE, DynamicBrokenCollectionContract::contains_whenNotEmpty_returnsExpectedResults, "contains(Object) returns expected results for a collection with elements fails with CONTAINS_ALWAYS_RETURNS_TRUE break"
                ),

                failsWithBreak(BreakableCollection.CONTAINS_ALWAYS_RETURNS_FALSE, DynamicBrokenCollectionContract::contains_whenNotEmpty_returnsExpectedResults, "contains(Object) returns expected results for a collection with elements fails with CONTAINS_ALWAYS_RETURNS_FALSE break"
                ),

                failsWithBreak(BreakableCollection.CONTAINS_RETURNS_OPPOSITE_VALUE, DynamicBrokenCollectionContract::contains_whenNotEmpty_returnsExpectedResults, "contains(Object) returns expected results for a collection with elements fails with CONTAINS_RETURNS_OPPOSITE_VALUE break"
                ),

                passesWhenUnsupported(CollectionMethods.CONTAINS, DynamicBrokenCollectionContract::contains_whenNotEmpty_returnsExpectedResults, "contains(Object) returns expected results for a collection with elements fails when not supported"
                ),

                failsWithBreak(BreakableCollection.CONTAINS_ALWAYS_RETURNS_TRUE, DynamicBrokenCollectionContract::contains_whenEmpty_returnsFalse, "contains(Object) returns false for an empty collection fails with CONTAINS_ALWAYS_RETURNS_TRUE break"
                ),

                failsWithBreak(BreakableCollection.CONTAINS_RETURNS_OPPOSITE_VALUE, DynamicBrokenCollectionContract::contains_whenEmpty_returnsFalse, "contains(Object) returns false for an empty collection fails with CONTAINS_RETURNS_OPPOSITE_VALUE break"
                ),

                passesWhenUnsupported(CollectionMethods.CONTAINS, DynamicBrokenCollectionContract::contains_whenEmpty_returnsFalse, "contains(Object) returns false for an empty collection fails when not supported"
                )
        );
    }

    /// Test factory for tests of the containsAll(Collection) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the containsAll(Collection) method.
    /// @see ContainsAllContract#containsAll_whenNotEmpty_returnsExpectedResults
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForContainsAll() {
        return Arrays.asList(
                failsWithBreak(BreakableCollection.CONTAINS_ALL_ALWAYS_RETURNS_TRUE, DynamicBrokenCollectionContract::containsAll_whenNotEmpty_returnsExpectedResults, "containsAll(Collection) works for a collection with elements fails with CONTAINS_ALWAYS_RETURNS_TRUE break"
                ),

                failsWithBreak(BreakableCollection.CONTAINS_ALL_ALWAYS_RETURNS_FALSE, DynamicBrokenCollectionContract::containsAll_whenNotEmpty_returnsExpectedResults, "containsAll(Collection) works for a collection with elements fails with CONTAINS_ALWAYS_RETURNS_FALSE break"
                ),

                failsWithBreak(BreakableCollection.CONTAINS_ALL_RETURNS_OPPOSITE_VALUE, DynamicBrokenCollectionContract::containsAll_whenNotEmpty_returnsExpectedResults, "containsAll(Collection) works for a collection with elements fails with CONTAINS_RETURNS_OPPOSITE_VALUE break"
                ),

                passesWhenUnsupported(CollectionMethods.CONTAINS_ALL, DynamicBrokenCollectionContract::containAll_whenNotSupported_throwsUnsupportedOperationException, "containsAll(Collection) works for a collection with elements fails when not supported"
                )
        );
    }

    /// Test factory for tests of the isEmpty() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the isEmpty() method.
    /// @see IsEmptyContract#isEmpty_whenEmpty_returnsTrue
    /// @see IsEmptyContract#isEmpty_whenNotEmpty_returnsFalse
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForIsEmpty() {
        return Arrays.asList(
                failsWithBreak(BreakableCollection.IS_EMPTY_ALWAYS_RETURNS_FALSE, DynamicBrokenCollectionContract::isEmpty_whenEmpty_returnsTrue, "isEmpty() returns true for an empty collection fails with IS_EMPTY_ALWAYS_RETURNS_FALSE break"
                ),

                failsWithBreak(BreakableCollection.IS_EMPTY_RETURNS_OPPOSITE_VALUE, DynamicBrokenCollectionContract::isEmpty_whenEmpty_returnsTrue, "isEmpty() returns true for an empty collection fails with IS_EMPTY_RETURNS_OPPOSITE_VALUE break"
                ),

                passesWhenUnsupported(CollectionMethods.IS_EMPTY, DynamicBrokenCollectionContract::isEmpty_whenEmpty_returnsTrue, "isEmpty() returns true for an empty collection fails when not supported"
                ),

                failsWithBreak(BreakableCollection.IS_EMPTY_ALWAYS_RETURNS_TRUE, DynamicBrokenCollectionContract::isEmpty_whenNotEmpty_returnsFalse, "isEmpty() returns false for a collection with elements fails with IS_EMPTY_ALWAYS_RETURNS_TRUE break"
                ),

                failsWithBreak(BreakableCollection.IS_EMPTY_RETURNS_OPPOSITE_VALUE, DynamicBrokenCollectionContract::isEmpty_whenNotEmpty_returnsFalse, "isEmpty() returns false for a collection with elements fails with IS_EMPTY_RETURNS_OPPOSITE_VALUE break"
                ),

                passesWhenUnsupported(CollectionMethods.IS_EMPTY, DynamicBrokenCollectionContract::isEmpty_whenNotEmpty_returnsFalse, "isEmpty() returns false for a collection with elements fails when not supported"
                )
        );
    }


    /// Test factory for tests of the remove(Object) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the remove(Object) method.
    /// @see RemoveContract#remove_whenEmpty_returnsFalse
    /// @see RemoveContract#remove_whenNotEmpty_returnsExpectedResults
    /// @see RemoveContract#remove_withNullValue_returnsExpectedResults
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForRemove() {
        return Arrays.asList(
                failsWithBreak(BreakableCollection.REMOVE_ALWAYS_RETURNS_TRUE, DynamicBrokenCollectionContract::remove_whenEmpty_returnsFalse, "remove(Object) returns false for an empty collection fails with REMOVE_ALWAYS_RETURNS_TRUE break"
                ),

                passesWhenUnsupported(CollectionMethods.REMOVE, DynamicBrokenCollectionContract::isEmpty_whenNotEmpty_returnsFalse, "remove(Object) returns false for an empty collection fails when not supported"
                ),

                failsWithBreak(BreakableCollection.REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE, DynamicBrokenCollectionContract::remove_whenEmpty_returnsFalse, "remove(Object) returns false for an empty collection fails with REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE break"
                ),

                failsWithBreak(BreakableCollection.REMOVE_DOES_NOT_REMOVE_ELEMENT, DynamicBrokenCollectionContract::remove_whenNotEmpty_returnsExpectedResults, "remove(Object) returns expected results for a collection with elements fails with REMOVE_DOES_NOT_REMOVE_ELEMENT break"
                ),

                failsWithBreak(BreakableCollection.REMOVE_ALWAYS_RETURNS_FALSE, DynamicBrokenCollectionContract::remove_whenNotEmpty_returnsExpectedResults, "remove(Object) returns expected results for a collection with elements fails with REMOVE_ALWAYS_RETURNS_FALSE break"
                ),

                failsWithBreak(BreakableCollection.REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE, DynamicBrokenCollectionContract::remove_whenNotEmpty_returnsExpectedResults, "remove(Object) returns expected results for a collection with elements fails with REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE break"
                ),

                passesWhenUnsupported(CollectionMethods.REMOVE, DynamicBrokenCollectionContract::remove_whenNotEmpty_returnsExpectedResults, "remove(Object) returns expected results for a collection with elements fails when not supported"
                ),

                failsWithBreak(BreakableCollection.REMOVE_DOES_NOT_REMOVE_ELEMENT, DynamicBrokenCollectionContract::remove_withNullValue_returnsExpectedResults, "remove(Object) handles null values correctly fails with REMOVE_DOES_NOT_REMOVE_ELEMENT break"
                ),

                failsWithBreak(BreakableCollection.REMOVE_ALWAYS_RETURNS_FALSE, DynamicBrokenCollectionContract::remove_withNullValue_returnsExpectedResults, "remove(Object) handles null values correctly fails with REMOVE_ALWAYS_RETURNS_FALSE break"
                ),

                failsWithBreak(BreakableCollection.REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE, DynamicBrokenCollectionContract::remove_withNullValue_returnsExpectedResults, "remove(Object) handles null values correctly fails with REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE break"
                ),

                passesWhenUnsupported(CollectionMethods.REMOVE, DynamicBrokenCollectionContract::remove_withNullValue_returnsExpectedResults, "remove(Object) handles null values correctly fails when not supported"
                )
                );
    }

    /// Test factory for tests of the removeAll(Collection) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the removeAll(Collection) method.
    /// @see RemoveAllContract#removeAll_whenEmpty_returnsFalse
    /// @see RemoveAllContract#removeAll_whenNotEmpty_removesArgumentElements
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForRemoveAll() {
        return Arrays.asList(
                failsWithBreak(BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_TRUE, DynamicBrokenCollectionContract::removeAll_whenEmpty_returnsFalse, "removeAll(Collection) returns false for an empty collection fails with REMOVE_ALL_ALWAYS_RETURNS_TRUE break"
                ),

                failsWithBreak(BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE, DynamicBrokenCollectionContract::removeAll_whenEmpty_returnsFalse, "removeAll(Collection) returns false for an empty collection fails with REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break"
                ),

                passesWhenUnsupported(CollectionMethods.REMOVE_ALL, DynamicBrokenCollectionContract::removeAll_whenEmpty_returnsFalse, "removeAll(Collection) returns false for an empty collection fails when not supported"
                ),

                failsWithBreak(BreakableCollection.REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS, DynamicBrokenCollectionContract::removeAll_whenNotEmpty_removesArgumentElements, "removeAll(Collection) removes argument elements from a non-empty collection fails with REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS break"
                ),

                failsWithBreak(BreakableCollection.REMOVE_ALL_SKIPS_FIRST_ELEMENT, DynamicBrokenCollectionContract::removeAll_whenNotEmpty_removesArgumentElements, "removeAll(Collection) removes argument elements from a non-empty collection fails with REMOVE_ALL_SKIPS_FIRST_ELEMENT break"
                ),

                failsWithBreak(BreakableCollection.REMOVE_ALL_SKIPS_LAST_ELEMENT, DynamicBrokenCollectionContract::removeAll_whenNotEmpty_removesArgumentElements, "removeAll(Collection) removes argument elements from a non-empty collection fails with REMOVE_ALL_SKIPS_LAST_ELEMENT break"
                ),

                failsWithBreak(BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_FALSE, DynamicBrokenCollectionContract::removeAll_whenNotEmpty_removesArgumentElements, "removeAll(Collection) removes argument elements from a non-empty collection fails with REMOVE_ALL_ALWAYS_RETURNS_FALSE break"
                ),

                failsWithBreak(BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE, DynamicBrokenCollectionContract::removeAll_whenNotEmpty_removesArgumentElements, "removeAll(Collection) removes argument elements from a non-empty collection fails with REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break"
                ),

                passesWhenUnsupported(CollectionMethods.REMOVE_ALL, DynamicBrokenCollectionContract::removeAll_whenNotEmpty_removesArgumentElements, "removeAll(Collection) removes argument elements from a non-empty collection fails when not supported"
                ),

                failsWithBreak(BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_TRUE, DynamicBrokenCollectionContract::removeAll_withNullElement_handlesCorrectly, "removeAll(Collection) handles null elements based on permission fails with REMOVE_ALL_ALWAYS_RETURNS_TRUE break"
                ),

                failsWithBreak(BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE, DynamicBrokenCollectionContract::removeAll_withNullElement_handlesCorrectly, "removeAll(Collection) handles null elements based on permission fails with REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break"
                ),

                passesWhenUnsupported(CollectionMethods.REMOVE_ALL, DynamicBrokenCollectionContract::removeAll_whenNotEmpty_removesArgumentElements, "removeAll(Collection) handles null elements based on permission fails when not supported"
                ),

                failsWithBreak(BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_TRUE, DynamicBrokenCollectionContract::removeAll_withIncompatibleType_returnsFalse, "removeAll(Collection) returns false for incompatible types fails with REMOVE_ALL_ALWAYS_RETURNS_TRUE break"
                ),

                passesWhenUnsupported(CollectionMethods.REMOVE_ALL, DynamicBrokenCollectionContract::removeAll_withNullElement_handlesCorrectly, "removeAll(Collection) handles null elements based on permission fails when not supported"
                )
        );
    }

    /// Test factory for tests of the removeIf() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the removeIf() method.
    /// @see RemoveIfContract#removeIf_whenEmpty_returnsFalse
    /// @see RemoveIfContract#removeIf_whenNotEmpty_returnsExpectedResults
    /// @see RemoveIfContract#removeIf_withNullFilter_throwsNullPointerException
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForRemoveIf() {
        return Arrays.asList(
                failsWithBreak(BreakableCollection.REMOVE_IF_ALWAYS_RETURNS_TRUE, DynamicBrokenCollectionContract::removeIf_whenEmpty_returnsFalse, "removeIf(Predicate) returns false for an empty collection fails with REMOVE_IF_ALWAYS_RETURNS_TRUE break"
                ),

                failsWithBreak(BreakableCollection.REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE, DynamicBrokenCollectionContract::removeIf_whenEmpty_returnsFalse, "removeIf(Predicate) returns false for an empty collection fails with REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE break"
                ),

                passesWhenUnsupported(CollectionMethods.REMOVE_IF, DynamicBrokenCollectionContract::removeIf_whenEmpty_returnsFalse, "removeIf(Predicate) returns false for an empty collection fails when not supported"
                ),

                failsWithBreak(BreakableCollection.REMOVE_IF_DOES_NOT_REMOVE_ANY_ELEMENTS, DynamicBrokenCollectionContract::removeIf_whenNotEmpty_returnsExpectedResults, "removeIf(Predicate) returns expected results for a collection with elements fails with REMOVE_IF_DOES_NOT_REMOVE_ANY_ELEMENTS break"
                ),

                failsWithBreak(BreakableCollection.REMOVE_IF_SKIPS_FIRST_ELEMENT, DynamicBrokenCollectionContract::removeIf_whenNotEmpty_returnsExpectedResults, "removeIf(Predicate) returns expected results for a collection with elements fails with REMOVE_IF_SKIPS_FIRST_ELEMENT break"
                ),

                failsWithBreak(BreakableCollection.REMOVE_IF_SKIPS_LAST_ELEMENT, DynamicBrokenCollectionContract::removeIf_whenNotEmpty_returnsExpectedResults, "removeIf(Predicate) returns expected results for a collection with elements fails with REMOVE_IF_SKIPS_LAST_ELEMENT break"
                ),

                failsWithBreak(BreakableCollection.REMOVE_IF_ALWAYS_RETURNS_FALSE, DynamicBrokenCollectionContract::removeIf_whenNotEmpty_returnsExpectedResults, "removeIf(Predicate) returns expected results for a collection with elements fails with REMOVE_IF_ALWAYS_RETURNS_FALSE break"
                ),

                failsWithBreak(BreakableCollection.REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE, DynamicBrokenCollectionContract::removeIf_whenNotEmpty_returnsExpectedResults, "removeIf(Predicate) returns expected results for a collection with elements fails with REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE break"
                ),

                passesWhenUnsupported(CollectionMethods.REMOVE_IF, DynamicBrokenCollectionContract::removeIf_whenNotEmpty_returnsExpectedResults, "removeIf(Predicate) returns expected results for a collection with elements fails when not supported"
                )
            );
    }

    /// Test factory for tests of the retainAll(Collection) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the retainAll(Collection) method.
    /// @see RetainAllContract#retainAll_whenEmpty_returnsFalse
    /// @see RetainAllContract#retainAll_whenNotEmpty_returnsExpectedResults
    /// @see RetainAllContract#retainAll_withNullCollection_throwsException
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForRetainAll() {
        return Arrays.asList(
                failsWithBreak(BreakableCollection.RETAIN_ALL_ALWAYS_RETURNS_TRUE, DynamicBrokenCollectionContract::retainAll_whenEmpty_returnsFalse, "retainAll(Collection) returns false for an empty collection fails with RETAIN_ALL_ALWAYS_RETURNS_TRUE break"
                ),

                failsWithBreak(BreakableCollection.RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE, DynamicBrokenCollectionContract::retainAll_whenEmpty_returnsFalse, "retainAll(Collection) returns false for an empty collection fails with RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break"
                ),

                passesWhenUnsupported(CollectionMethods.RETAIN_ALL, DynamicBrokenCollectionContract::retainAll_whenEmpty_returnsFalse, "retainAll(Collection) returns false for an empty collection fails when not supported"
                ),

                failsWithBreak(BreakableCollection.RETAIN_ALL_DOES_NOT_RETAIN_ANY_ELEMENTS, DynamicBrokenCollectionContract::retainAll_whenNotEmpty_returnsExpectedResults, "retainAll(Collection) works on a container with elements fails with RETAIN_ALL_DOES_NOT_RETAIN_ANY_ELEMENTS break"
                ),

                failsWithBreak(BreakableCollection.RETAIN_ALL_SKIPS_FIRST_ELEMENT, DynamicBrokenCollectionContract::retainAll_whenNotEmpty_returnsExpectedResults, "retainAll(Collection) works on a container with elements fails with RETAIN_ALL_SKIPS_FIRST_ELEMENT break"
                ),

                failsWithBreak(BreakableCollection.RETAIN_ALL_SKIPS_LAST_ELEMENT, DynamicBrokenCollectionContract::retainAll_whenNotEmpty_returnsExpectedResults, "retainAll(Collection) works on a container with elements fails with RETAIN_ALL_SKIPS_LAST_ELEMENT break"
                ),

                failsWithBreak(BreakableCollection.RETAIN_ALL_ALWAYS_RETURNS_FALSE, DynamicBrokenCollectionContract::retainAll_whenNotEmpty_returnsExpectedResults, "retainAll(Collection) works on a container with elements fails with RETAIN_ALL_ALWAYS_RETURNS_FALSE break"
                ),

                failsWithBreak(BreakableCollection.RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE, DynamicBrokenCollectionContract::retainAll_whenNotEmpty_returnsExpectedResults, "retainAll(Collection) works on a container with elements fails with RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break"
                ),

                passesWhenUnsupported(CollectionMethods.RETAIN_ALL, DynamicBrokenCollectionContract::retainAll_whenNotEmpty_returnsExpectedResults, "retainAll(Collection) works on a container with elements fails when not supported"
                )
        );
    }

    /// Test factory for tests of the size() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the size() method.
    /// @see SizeContract#size_whenEmpty_returnsZero
    /// @see SizeContract#size_whenNotEmpty_returnsCorrectSize
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForSize() {
        return Arrays.asList(
                failsWithBreak(BreakableCollection.SIZE_ALWAYS_RETURNS_CONSTANT_VALUE, DynamicBrokenCollectionContract::size_whenEmpty_returnsZero, "size() returns 0 for an empty collection fails with SIZE_ALWAYS_RETURNS_CONSTANT_VALUE break"
                ),

                failsWithBreak(BreakableCollection.SIZE_ALWAYS_RETURNS_ZERO, DynamicBrokenCollectionContract::size_whenNotEmpty_returnsCorrectSize, "size() returns correct number of elements for a collection with elements fails with SIZE_ALWAYS_RETURNS_ZERO break"
                ),

                failsWithBreak(BreakableCollection.SIZE_ALWAYS_RETURNS_CONSTANT_VALUE, DynamicBrokenCollectionContract::size_whenNotEmpty_returnsCorrectSize, "size() returns correct number of elements for a collection with elements fails with SIZE_ALWAYS_RETURNS_CONSTANT_VALUE break"
                )
        );
    }
}