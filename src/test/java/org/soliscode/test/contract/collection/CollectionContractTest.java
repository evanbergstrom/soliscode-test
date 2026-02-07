package org.soliscode.test.contract.collection;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Disabled;
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
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.provider.CollectionProvider;

import java.util.Arrays;
import java.util.Collection;

/// Tests for the CollectionContract class.
///
/// @author evanbergstrom
/// @since 1.0
@DisplayName("Tests for CollectionContract class")
public class CollectionContractTest extends ContractTest<BreakableCollection<Integer>> {

    /// Verifies that the tests all pass when testing a working Collection implementation.
    /// In this case, instances of `BreakableCollection` are used that have no breaks specified.
    @Nested
    class WorkingCollectionTest extends AbstractTest
            implements CollectionContract<Integer, BreakableCollection<Integer>>,
            BreakableCollection.WithProvider<Integer>, WithIntegerElement {
    }

    /// Dynamically created instance of `CollectionContract` that will run on instances of `BreakableCollection` with a
    /// specified break. This contract will be expected to fail on certain tests depending on the specific break that
    /// is being used.
    @Disabled("Used only for dynamic test generation")
    protected static final class DynamicBrokenCollectionContract
            extends DynamicContract<BreakableCollection<Integer>, CollectionProvider<Integer, BreakableCollection<Integer>>>
            implements CollectionContract<Integer, BreakableCollection<Integer>>, WithIntegerElement {

        protected DynamicBrokenCollectionContract(final @NonNull Break b, final @NonNull InterfaceMethod m) {
            super(b, m, (breaks, statuses, test) ->
                    BreakableCollection.collectionProvider(
                            new BreakableCollection.Builder<Integer>().addBreaks(breaks).setMethodStatuses(statuses),
                            WithIntegerElement.PROVIDER
                    )
            );
        }

        @Override
        public boolean supportsMethod(final @NonNull InterfaceMethod method) {
            return super.supportsMethod(method);
        }

        @Override
        public void doesNotSupportMethod(final @NonNull InterfaceMethod method) {
            super.doesNotSupportMethod(method);
        }
    }

    @SuppressWarnings({"unchecked"})
    @Override
    protected @NonNull DynamicBrokenCollectionContract createTest(final @NonNull Break b,
                                                                  final @NonNull InterfaceMethod m) {
        return new DynamicBrokenCollectionContract(b, m);
    }

    /// Test factory for tests of the add_singleElement_returnsTrueAndUpdatesSize method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `add_singleElement_returnsTrueAndUpdatesSize` method.
    /// @see AddContract#add_singleElement_returnsTrueAndUpdatesSize
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForAdd() {
        return Arrays.asList(
                passingTestWithUnsupportedMethod("add(E) adds a single element and updates size fails with UnsupportedOperationException when not supported",
                        CollectionMethods.ADD,
                        DynamicBrokenCollectionContract::add_singleElement_returnsTrueAndUpdatesSize),

                failingTestWithBreak("add(E) adds a single element and updates size fails with ADD_DOES_NOT_ADD_ELEMENT break",
                        BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT,
                        DynamicBrokenCollectionContract::add_singleElement_returnsTrueAndUpdatesSize),

                failingTestWithBreak("add(E) adds a single element and updates size fails with ADD_ALWAYS_RETURNS_FALSE break",
                        BreakableCollection.ADD_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenCollectionContract::add_singleElement_returnsTrueAndUpdatesSize),

                failingTestWithBreak("add(E) adds a single element and updates size fails with ADD_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.ADD_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::add_singleElement_returnsTrueAndUpdatesSize),

                failingTestWithUnsupportedMethodBreak("add(E) adds a single element and updates size fails with ADD_THROWS_WRONG_UNSUPPORTED_EXCEPTION break",
                        CollectionMethods.ADD, BreakableCollection.ADD_THROWS_WRONG_UNSUPPORTED_EXCEPTION,
                        DynamicBrokenCollectionContract::add_singleElement_returnsTrueAndUpdatesSize)
        );
    }

    /// Test factory for tests of the add_withNullValue_handlesCorrectly() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `add_withNullValue_handlesCorrectly` method.
    /// @see AddContract#add_withNullValue_handlesCorrectly
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForAddWithNullValues() {
        return Arrays.asList(
                passingTestWithUnsupportedMethod("add(E) handles null values based on permission fails with UnsupportedOperationException when not supported",
                        CollectionMethods.ADD,
                        DynamicBrokenCollectionContract::add_withNullValue_handlesCorrectly),

                failingTestWithBreak("add(E) handles null values based on permission fails with ADD_DOES_NOT_ADD_ELEMENT break",
                        BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT,
                        DynamicBrokenCollectionContract::add_withNullValue_handlesCorrectly),

                failingTestWithBreak("add(E) handles null values based on permission fails with ADD_ALWAYS_RETURNS_FALSE break",
                        BreakableCollection.ADD_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenCollectionContract::add_withNullValue_handlesCorrectly),

                failingTestWithBreak("add(E) handles null values based on permission fails with ADD_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.ADD_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::add_withNullValue_handlesCorrectly)
        );
    }

    /// Test factory for tests of the add_withDuplicateValue_handlesCorrectly() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `add_withDuplicateValue_handlesCorrectly` method.
    /// @see AddContract#add_withDuplicateValue_handlesCorrectly
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForAddWithDuplicateValues() {
        return Arrays.asList(
                failingTestWithBreak("add(E) handles duplicate values based on permission fails with ADD_DOES_NOT_ADD_ELEMENT break",
                        BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT,
                        DynamicBrokenCollectionContract::add_withDuplicateValue_handlesCorrectly),

                failingTestWithBreak("add(E) handles duplicate values based on permission fails with ADD_ALWAYS_RETURNS_FALSE break",
                        BreakableCollection.ADD_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenCollectionContract::add_withDuplicateValue_handlesCorrectly),

                failingTestWithBreak("add(E) handles duplicate values based on permission fails with ADD_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.ADD_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::add_withDuplicateValue_handlesCorrectly),

                passingTestWithUnsupportedMethod("add(E) handles duplicate values based on permission fails when not supported",
                        CollectionMethods.ADD,
                        DynamicBrokenCollectionContract::add_withDuplicateValue_handlesCorrectly)
        );
    }

    /// Test factory for tests of the addAll(Collection) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the addAll(Collection) method.
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForAddAll() {
        return Arrays.asList(
                failingTestWithBreak("addAll_whenContainerIsEmpty_addsAllArgumentElements() fails with ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS break",
                        BreakableCollection.ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS,
                        DynamicBrokenCollectionContract::addAll_whenContainerIsEmpty_addsAllArgumentElements),

                failingTestWithBreak("addAll_whenContainerIsEmpty_addsAllArgumentElements() fails with ADD_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.ADD_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::addAll_whenContainerIsEmpty_addsAllArgumentElements),

                failingTestWithBreak("addAll_whenContainerIsEmpty_addsAllArgumentElements() fails with ADD_ALL_ALWAYS_RETURNS_FALSE break",
                        BreakableCollection.ADD_ALL_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenCollectionContract::addAll_whenContainerIsEmpty_addsAllArgumentElements),

                failingTestWithBreak("addAll_whenContainerIsEmpty_addsAllArgumentElements() fails with ADD_ALL_SKIPS_FIRST_ELEMENT break",
                        BreakableCollection.ADD_ALL_SKIPS_FIRST_ELEMENT,
                        DynamicBrokenCollectionContract::addAll_whenContainerIsEmpty_addsAllArgumentElements),

                failingTestWithBreak("addAll_whenContainerIsEmpty_addsAllArgumentElements() fails with ADD_ALL_SKIPS_LAST_ELEMENT break",
                        BreakableCollection.ADD_ALL_SKIPS_LAST_ELEMENT,
                        DynamicBrokenCollectionContract::addAll_whenContainerIsEmpty_addsAllArgumentElements),

                passingTestWithUnsupportedMethod("addAll_whenContainerIsEmpty_addsAllArgumentElements() fails when not supported",
                        CollectionMethods.ADD_ALL,
                        DynamicBrokenCollectionContract::addAll_whenContainerIsEmpty_addsAllArgumentElements),

                failingTestWithUnsupportedMethodBreak("addAll_whenContainerIsEmpty_addsAllArgumentElements() fails with ADD_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION break",
                        CollectionMethods.ADD_ALL, BreakableCollection.ADD_ALL_THROWS_WRONG_UNSUPPORTED_EXCEPTION,
                        DynamicBrokenCollectionContract::addAll_whenContainerIsEmpty_addsAllArgumentElements)
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
                passingTestWithUnsupportedMethod("clear() works for an empty collection fails when not supported",
                        CollectionMethods.CLEAR,
                        DynamicBrokenCollectionContract::clear_whenEmpty_isSuccessful),

                failingTestWithUnsupportedMethodBreak("clear() works for an empty collection fails with CLEAR_THROWS_WRONG_UNSUPPORTED_EXCEPTION break",
                        CollectionMethods.CLEAR, BreakableCollection.CLEAR_THROWS_WRONG_UNSUPPORTED_EXCEPTION,
                        DynamicBrokenCollectionContract::clear_whenEmpty_isSuccessful),

                failingTestWithBreak("clear() works for a collection with elements fails with CLEAR_DOES_NOT_REMOVE_ANY_ELEMENTS break",
                        BreakableCollection.CLEAR_DOES_NOT_REMOVE_ANY_ELEMENTS,
                        DynamicBrokenCollectionContract::clear_whenNotEmpty_removesAllElements),

                failingTestWithBreak("clear() works for a collection with elements fails with CLEAR_SKIPS_FIRST_ELEMENT break",
                        BreakableCollection.CLEAR_SKIPS_FIRST_ELEMENT,
                        DynamicBrokenCollectionContract::clear_whenNotEmpty_removesAllElements),

                failingTestWithBreak("clear() works for a collection with elements fails with CLEAR_SKIPS_LAST_ELEMENT break",
                        BreakableCollection.CLEAR_SKIPS_LAST_ELEMENT,
                        DynamicBrokenCollectionContract::clear_whenNotEmpty_removesAllElements),

                passingTestWithUnsupportedMethod("clear() works for a collection with elements fails when not supported",
                        CollectionMethods.CLEAR,
                        DynamicBrokenCollectionContract::clear_whenNotEmpty_removesAllElements),

                failingTestWithUnsupportedMethodBreak("clear() works for a collection with elements fails with CLEAR_THROWS_WRONG_UNSUPPORTED_EXCEPTION break",
                        CollectionMethods.CLEAR, BreakableCollection.CLEAR_THROWS_WRONG_UNSUPPORTED_EXCEPTION,
                        DynamicBrokenCollectionContract::clear_whenNotEmpty_removesAllElements)
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
                failingTestWithBreak("contains(Object) returns expected results for a collection with elements fails with CONTAINS_ALWAYS_RETURNS_TRUE break",
                        BreakableCollection.CONTAINS_ALWAYS_RETURNS_TRUE,
                        DynamicBrokenCollectionContract::contains_whenNotEmpty_returnsExpectedResults),

                failingTestWithBreak("contains(Object) returns expected results for a collection with elements fails with CONTAINS_ALWAYS_RETURNS_FALSE break",
                        BreakableCollection.CONTAINS_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenCollectionContract::contains_whenNotEmpty_returnsExpectedResults),

                failingTestWithBreak("contains(Object) returns expected results for a collection with elements fails with CONTAINS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.CONTAINS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::contains_whenNotEmpty_returnsExpectedResults),

                passingTestWithUnsupportedMethod("contains(Object) returns expected results for a collection with elements fails when not supported",
                        CollectionMethods.CONTAINS,
                        DynamicBrokenCollectionContract::contains_whenNotEmpty_returnsExpectedResults),

                failingTestWithBreak("contains(Object) returns false for an empty collection fails with CONTAINS_ALWAYS_RETURNS_TRUE break",
                        BreakableCollection.CONTAINS_ALWAYS_RETURNS_TRUE,
                        DynamicBrokenCollectionContract::contains_whenEmpty_returnsFalse),

                failingTestWithBreak("contains(Object) returns false for an empty collection fails with CONTAINS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.CONTAINS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::contains_whenEmpty_returnsFalse),

                passingTestWithUnsupportedMethod("contains(Object) returns false for an empty collection fails when not supported",
                        CollectionMethods.CONTAINS,
                        DynamicBrokenCollectionContract::contains_whenEmpty_returnsFalse)
        );
    }

    /// Test factory for tests of the containsAll(Collection) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the containsAll(Collection) method.
    /// @see ContainsAllContract#containsAll_whenNotEmpty_returnsExpectedResults
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForContainsAll() {
        return Arrays.asList(
                failingTestWithBreak("containsAll(Collection) works for a collection with elements fails with CONTAINS_ALWAYS_RETURNS_TRUE break",
                        BreakableCollection.CONTAINS_ALL_ALWAYS_RETURNS_TRUE,
                        DynamicBrokenCollectionContract::containsAll_whenNotEmpty_returnsExpectedResults),

                failingTestWithBreak("containsAll(Collection) works for a collection with elements fails with CONTAINS_ALWAYS_RETURNS_FALSE break",
                        BreakableCollection.CONTAINS_ALL_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenCollectionContract::containsAll_whenNotEmpty_returnsExpectedResults),

                failingTestWithBreak("containsAll(Collection) works for a collection with elements fails with CONTAINS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.CONTAINS_ALL_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::containsAll_whenNotEmpty_returnsExpectedResults),

                passingTestWithUnsupportedMethod("containsAll(Collection) works for a collection with elements fails when not supported",
                        CollectionMethods.CONTAINS_ALL,
                        DynamicBrokenCollectionContract::containsAll_whenNotEmpty_returnsExpectedResults)
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
                failingTestWithBreak("isEmpty() returns true for an empty collection fails with IS_EMPTY_ALWAYS_RETURNS_FALSE break",
                        BreakableCollection.IS_EMPTY_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenCollectionContract::isEmpty_whenEmpty_returnsTrue),

                failingTestWithBreak("isEmpty() returns true for an empty collection fails with IS_EMPTY_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.IS_EMPTY_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::isEmpty_whenEmpty_returnsTrue),

                passingTestWithUnsupportedMethod("isEmpty() returns true for an empty collection fails when not supported",
                        CollectionMethods.IS_EMPTY,
                        DynamicBrokenCollectionContract::isEmpty_whenEmpty_returnsTrue),

                failingTestWithBreak("isEmpty() returns false for a collection with elements fails with IS_EMPTY_ALWAYS_RETURNS_TRUE break",
                        BreakableCollection.IS_EMPTY_ALWAYS_RETURNS_TRUE,
                        DynamicBrokenCollectionContract::isEmpty_whenNotEmpty_returnsFalse),

                failingTestWithBreak("isEmpty() returns false for a collection with elements fails with IS_EMPTY_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.IS_EMPTY_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::isEmpty_whenNotEmpty_returnsFalse),

                passingTestWithUnsupportedMethod("isEmpty() returns false for a collection with elements fails when not supported",
                        CollectionMethods.IS_EMPTY,
                        DynamicBrokenCollectionContract::isEmpty_whenNotEmpty_returnsFalse)
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
                failingTestWithBreak("remove(Object) returns false for an empty collection fails with REMOVE_ALWAYS_RETURNS_TRUE break",
                        BreakableCollection.REMOVE_ALWAYS_RETURNS_TRUE,
                        DynamicBrokenCollectionContract::remove_whenEmpty_returnsFalse),

                passingTestWithUnsupportedMethod("remove(Object) returns false for an empty collection fails when not supported",
                        CollectionMethods.REMOVE,
                        DynamicBrokenCollectionContract::isEmpty_whenNotEmpty_returnsFalse),

                failingTestWithBreak("remove(Object) returns false for an empty collection fails with REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::remove_whenEmpty_returnsFalse),

                failingTestWithBreak("remove(Object) returns expected results for a collection with elements fails with REMOVE_DOES_NOT_REMOVE_ELEMENT break",
                        BreakableCollection.REMOVE_DOES_NOT_REMOVE_ELEMENT,
                        DynamicBrokenCollectionContract::remove_whenNotEmpty_returnsExpectedResults),

                failingTestWithBreak("remove(Object) returns expected results for a collection with elements fails with REMOVE_ALWAYS_RETURNS_FALSE break",
                        BreakableCollection.REMOVE_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenCollectionContract::remove_whenNotEmpty_returnsExpectedResults),

                failingTestWithBreak("remove(Object) returns expected results for a collection with elements fails with REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::remove_whenNotEmpty_returnsExpectedResults),

                passingTestWithUnsupportedMethod("remove(Object) returns expected results for a collection with elements fails when not supported",
                        CollectionMethods.REMOVE,
                        DynamicBrokenCollectionContract::remove_whenNotEmpty_returnsExpectedResults),

                failingTestWithBreak("remove(Object) handles null values correctly fails with REMOVE_DOES_NOT_REMOVE_ELEMENT break",
                        BreakableCollection.REMOVE_DOES_NOT_REMOVE_ELEMENT,
                        DynamicBrokenCollectionContract::remove_withNullValue_returnsExpectedResults),

                failingTestWithBreak("remove(Object) handles null values correctly fails with REMOVE_ALWAYS_RETURNS_FALSE break",
                        BreakableCollection.REMOVE_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenCollectionContract::remove_withNullValue_returnsExpectedResults),

                failingTestWithBreak("remove(Object) handles null values correctly fails with REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::remove_withNullValue_returnsExpectedResults),

                passingTestWithUnsupportedMethod("remove(Object) handles null values correctly fails when not supported",
                        CollectionMethods.REMOVE,
                        DynamicBrokenCollectionContract::remove_withNullValue_returnsExpectedResults)
                );
    }

    /// Test factory for tests of the removeAll(Collection) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the removeAll(Collection) method.
    /// @see RemoveAllContract#removeAll_whenEmpty_returnsFalse
    /// @see RemoveAllContract#removeAll_whenNotEmpty_removesArgumentElements
    @TestFactory
    @Disabled
    public Collection<DynamicTest> dynamicTestsForRemoveAll() {
        return Arrays.asList(
                failingTestWithBreak("removeAll(Collection) returns false for an empty collection fails with REMOVE_ALL_ALWAYS_RETURNS_TRUE break",
                        BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_TRUE,
                        DynamicBrokenCollectionContract::removeAll_whenEmpty_returnsFalse),

                failingTestWithBreak("removeAll(Collection) returns false for an empty collection fails with REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::removeAll_whenEmpty_returnsFalse),

                passingTestWithUnsupportedMethod("removeAll(Collection) returns false for an empty collection fails when not supported",
                        CollectionMethods.REMOVE_ALL,
                        DynamicBrokenCollectionContract::removeAll_whenEmpty_returnsFalse),

                failingTestWithBreak("removeAll(Collection) removes argument elements from a non-empty collection fails with REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS break",
                        BreakableCollection.REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS,
                        DynamicBrokenCollectionContract::removeAll_whenNotEmpty_removesArgumentElements),

                failingTestWithBreak("removeAll(Collection) removes argument elements from a non-empty collection fails with REMOVE_ALL_SKIPS_FIRST_ELEMENT break",
                        BreakableCollection.REMOVE_ALL_SKIPS_FIRST_ELEMENT,
                        DynamicBrokenCollectionContract::removeAll_whenNotEmpty_removesArgumentElements),

                failingTestWithBreak("removeAll(Collection) removes argument elements from a non-empty collection fails with REMOVE_ALL_SKIPS_LAST_ELEMENT break",
                        BreakableCollection.REMOVE_ALL_SKIPS_LAST_ELEMENT,
                        DynamicBrokenCollectionContract::removeAll_whenNotEmpty_removesArgumentElements),

                failingTestWithBreak("removeAll(Collection) removes argument elements from a non-empty collection fails with REMOVE_ALL_ALWAYS_RETURNS_FALSE break",
                        BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenCollectionContract::removeAll_whenNotEmpty_removesArgumentElements),

                failingTestWithBreak("removeAll(Collection) removes argument elements from a non-empty collection fails with REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::removeAll_whenNotEmpty_removesArgumentElements),

                passingTestWithUnsupportedMethod("removeAll(Collection) removes argument elements from a non-empty collection fails when not supported",
                        CollectionMethods.REMOVE_ALL,
                        DynamicBrokenCollectionContract::removeAll_whenNotEmpty_removesArgumentElements),

                failingTestWithBreak("removeAll(Collection) handles null elements based on permission fails with REMOVE_ALL_ALWAYS_RETURNS_TRUE break",
                        BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_TRUE,
                        DynamicBrokenCollectionContract::removeAll_withNullElement_handlesCorrectly),

                failingTestWithBreak("removeAll(Collection) handles null elements based on permission fails with REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::removeAll_withNullElement_handlesCorrectly),

                passingTestWithUnsupportedMethod("removeAll(Collection) handles null elements based on permission fails when not supported",
                        CollectionMethods.REMOVE_ALL,
                        DynamicBrokenCollectionContract::removeAll_whenNotEmpty_removesArgumentElements),

                failingTestWithBreak("removeAll(Collection) returns false for incompatible types fails with REMOVE_ALL_ALWAYS_RETURNS_TRUE break",
                        BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_TRUE,
                        DynamicBrokenCollectionContract::removeAll_withIncompatibleType_returnsFalse),

                failingTestWithBreak("removeAll(Collection) throws exception when argument collection is null fails with REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::removeAll_withNullCollection_throwsException),

                passingTestWithUnsupportedMethod("removeAll(Collection) handles null elements based on permission fails when not supported",
                       CollectionMethods.REMOVE_ALL,
                       DynamicBrokenCollectionContract::removeAll_withNullElement_handlesCorrectly)
        );
    }

    /// Test factory for tests of the removeIf() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the removeIf() method.
    /// @see RemoveIfContract#removeIf_whenEmpty_returnsFalse
    /// @see RemoveIfContract#removeIf_whenNotEmpty_returnsExpectedResults
    /// @see RemoveIfContract#removeIf_withNullFilter_throwsNullPointerException
    @TestFactory
    @Disabled
    public Collection<DynamicTest> dynamicTestsForRemoveIf() {
        return Arrays.asList(
                failingTestWithBreak("removeIf(Predicate) returns false for an empty collection fails with REMOVE_IF_ALWAYS_RETURNS_TRUE break",
                        BreakableCollection.REMOVE_IF_ALWAYS_RETURNS_TRUE,
                        DynamicBrokenCollectionContract::removeIf_whenEmpty_returnsFalse),

                failingTestWithBreak("removeIf(Predicate) returns false for an empty collection fails with REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::removeIf_whenEmpty_returnsFalse),

                passingTestWithUnsupportedMethod("removeIf(Predicate) returns false for an empty collection fails when not supported",
                        CollectionMethods.REMOVE_IF,
                        DynamicBrokenCollectionContract::removeIf_whenEmpty_returnsFalse),

                failingTestWithBreak("removeIf(Predicate) returns expected results for a collection with elements fails with REMOVE_IF_DOES_NOT_REMOVE_ANY_ELEMENTS break",
                        BreakableCollection.REMOVE_IF_DOES_NOT_REMOVE_ANY_ELEMENTS,
                        DynamicBrokenCollectionContract::removeIf_whenNotEmpty_returnsExpectedResults),

                failingTestWithBreak("removeIf(Predicate) returns expected results for a collection with elements fails with REMOVE_IF_SKIPS_FIRST_ELEMENT break",
                        BreakableCollection.REMOVE_IF_SKIPS_FIRST_ELEMENT,
                        DynamicBrokenCollectionContract::removeIf_whenNotEmpty_returnsExpectedResults),

                failingTestWithBreak("removeIf(Predicate) returns expected results for a collection with elements fails with REMOVE_IF_SKIPS_LAST_ELEMENT break",
                        BreakableCollection.REMOVE_IF_SKIPS_LAST_ELEMENT,
                        DynamicBrokenCollectionContract::removeIf_whenNotEmpty_returnsExpectedResults),

                failingTestWithBreak("removeIf(Predicate) returns expected results for a collection with elements fails with REMOVE_IF_ALWAYS_RETURNS_FALSE break",
                        BreakableCollection.REMOVE_IF_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenCollectionContract::removeIf_whenNotEmpty_returnsExpectedResults),

                failingTestWithBreak("removeIf(Predicate) returns expected results for a collection with elements fails with REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::removeIf_whenNotEmpty_returnsExpectedResults),

                passingTestWithUnsupportedMethod("removeIf(Predicate) returns expected results for a collection with elements fails when not supported",
                        CollectionMethods.REMOVE_IF,
                        DynamicBrokenCollectionContract::removeIf_whenNotEmpty_returnsExpectedResults),

                failingTestWithBreak("removeIf(Predicate) throws NullPointerException when filter is null fails with REMOVE_IF_ALWAYS_RETURNS_TRUE break",
                        BreakableCollection.REMOVE_IF_ALWAYS_RETURNS_TRUE,
                        DynamicBrokenCollectionContract::removeIf_withNullFilter_throwsNullPointerException),

                failingTestWithBreak("removeIf(Predicate) throws NullPointerException when filter is null fails with REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::removeIf_withNullFilter_throwsNullPointerException)
            );
    }

    /// Test factory for tests of the retainAll(Collection) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the retainAll(Collection) method.
    /// @see RetainAllContract#retainAll_whenEmpty_returnsFalse
    /// @see RetainAllContract#retainAll_whenNotEmpty_returnsExpectedResults
    /// @see RetainAllContract#retainAll_withNullCollection_throwsException
    @TestFactory
    @Disabled
    public Collection<DynamicTest> dynamicTestsForRetainAll() {
        return Arrays.asList(
                failingTestWithBreak("retainAll(Collection) returns false for an empty collection fails with RETAIN_ALL_ALWAYS_RETURNS_TRUE break",
                        BreakableCollection.RETAIN_ALL_ALWAYS_RETURNS_TRUE,
                        DynamicBrokenCollectionContract::retainAll_whenEmpty_returnsFalse),

                failingTestWithBreak("retainAll(Collection) returns false for an empty collection fails with RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::retainAll_whenEmpty_returnsFalse),

                passingTestWithUnsupportedMethod("retainAll(Collection) returns false for an empty collection fails when not supported",
                        CollectionMethods.RETAIN_ALL,
                        DynamicBrokenCollectionContract::retainAll_whenEmpty_returnsFalse),

                failingTestWithBreak("retainAll(Collection) works on a container with elements fails with RETAIN_ALL_DOES_NOT_RETAIN_ANY_ELEMENTS break",
                        BreakableCollection.RETAIN_ALL_DOES_NOT_RETAIN_ANY_ELEMENTS,
                        DynamicBrokenCollectionContract::retainAll_whenNotEmpty_returnsExpectedResults),

                failingTestWithBreak("retainAll(Collection) works on a container with elements fails with RETAIN_ALL_SKIPS_FIRST_ELEMENT break",
                        BreakableCollection.RETAIN_ALL_SKIPS_FIRST_ELEMENT,
                        DynamicBrokenCollectionContract::retainAll_whenNotEmpty_returnsExpectedResults),

                failingTestWithBreak("retainAll(Collection) works on a container with elements fails with RETAIN_ALL_SKIPS_LAST_ELEMENT break",
                        BreakableCollection.RETAIN_ALL_SKIPS_LAST_ELEMENT,
                        DynamicBrokenCollectionContract::retainAll_whenNotEmpty_returnsExpectedResults),

                failingTestWithBreak("retainAll(Collection) works on a container with elements fails with RETAIN_ALL_ALWAYS_RETURNS_FALSE break",
                        BreakableCollection.RETAIN_ALL_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenCollectionContract::retainAll_whenNotEmpty_returnsExpectedResults),

                failingTestWithBreak("retainAll(Collection) works on a container with elements fails with RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::retainAll_whenNotEmpty_returnsExpectedResults),

                passingTestWithUnsupportedMethod("retainAll(Collection) works on a container with elements fails when not supported",
                        CollectionMethods.RETAIN_ALL,
                        DynamicBrokenCollectionContract::retainAll_whenNotEmpty_returnsExpectedResults),

                failingTestWithBreak("retainAll(Collection) throws exception when argument collection is null fails with RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::retainAll_withNullCollection_throwsException)
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
                failingTestWithBreak("size() returns 0 for an empty collection fails with SIZE_ALWAYS_RETURNS_CONSTANT_VALUE break",
                        BreakableCollection.SIZE_ALWAYS_RETURNS_CONSTANT_VALUE,
                        DynamicBrokenCollectionContract::size_whenEmpty_returnsZero),

                failingTestWithBreak("size() returns correct number of elements for a collection with elements fails with SIZE_ALWAYS_RETURNS_ZERO break",
                        BreakableCollection.SIZE_ALWAYS_RETURNS_ZERO,
                        DynamicBrokenCollectionContract::size_whenNotEmpty_returnsCorrectSize),

                failingTestWithBreak("size() returns correct number of elements for a collection with elements fails with SIZE_ALWAYS_RETURNS_CONSTANT_VALUE break",
                        BreakableCollection.SIZE_ALWAYS_RETURNS_CONSTANT_VALUE,
                        DynamicBrokenCollectionContract::size_whenNotEmpty_returnsCorrectSize)
        );
    }
}