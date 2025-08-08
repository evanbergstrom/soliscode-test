package org.soliscode.test.contract;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.breakable.Break;
import org.soliscode.test.breakable.BreakableCollection;
import org.soliscode.test.contract.collection.CollectionContract;
import org.soliscode.test.contract.support.WithIntegerElement;

import java.util.Arrays;
import java.util.Collection;

/// Tests for the CollectionContract class.
///
/// @author evanbergstrom
/// @since 1.0
@DisplayName("Tests for CollectionContract class")
public class CollectionContractTest extends ContractTest<Integer, BreakableCollection<Integer>> {

    /// Verifies that the tests all pass when testing a working Collection implementation.
    /// In this case, instances of `BreakableCollection` are used that have no breaks specified.
    @Nested
    class WorkingCollectionTest extends AbstractTest
            implements CollectionContract<Integer, BreakableCollection<Integer>>,
            BreakableCollection.WithProvider<Integer>, WithIntegerElement {
    }

    /// Dynamically created instance of `IterableContract` that will run on instances of `BreakableIterator` with a
    /// specified break. This contract will be expected to fail on certain tests depending on the specific break that
    /// is being used.
    @Disabled("Used only for dynamic test generation")
    protected static class DynamicBrokenCollectionContract extends DynamicContract<Integer, BreakableCollection<Integer>>
            implements CollectionContract<Integer, BreakableCollection<Integer>>, WithIntegerElement {

        protected DynamicBrokenCollectionContract(final @NotNull Break b) {
            super(b, BreakableCollection::collectionProvider);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected @NotNull DynamicBrokenCollectionContract createTest(final @NotNull Break b) {
        return new DynamicBrokenCollectionContract(b);
    }

    /// Test factory for tests of the add(Object) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the add(Object) method.
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForAdd() {
        return Arrays.asList(
                failingTest("testAdd() fails with ADD_DOES_NOT_ADD_ELEMENT break",
                        BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT,
                        DynamicBrokenCollectionContract::testAdd),

                failingTest("testAdd() fails with ADD_ALWAYS_RETURNS_FALSE break",
                        BreakableCollection.ADD_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenCollectionContract::testAdd),

                failingTest("testAdd() fails with ADD_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.ADD_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::testAdd),

                failingTest("testAddWithNullValue() fails with ADD_DOES_NOT_ADD_ELEMENT break",
                        BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT,
                        DynamicBrokenCollectionContract::testAddWithNullValue),

                failingTest("testAddWithNullValue() fails with ADD_ALWAYS_RETURNS_FALSE break",
                        BreakableCollection.ADD_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenCollectionContract::testAddWithNullValue),

                failingTest("testAddWithNullValue() fails with ADD_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.ADD_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::testAddWithNullValue)

        );
    }

    /// Test factory for tests of the addAll(Collection) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the addAll(Collection) method.
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForAddAll() {
        return Arrays.asList(
                failingTest("testAddAllToContainer() fails with ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS break",
                        BreakableCollection.ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS,
                        DynamicBrokenCollectionContract::testAddAllToContainer),

                failingTest("testAddAllToContainer() fails with ADD_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.ADD_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::testAddAllToContainer),

                failingTest("testAddAllToContainer() fails with ADD_ALL_ALWAYS_RETURNS_FALSE break",
                        BreakableCollection.ADD_ALL_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenCollectionContract::testAddAllToContainer),

                failingTest("testAddAllToContainer() fails with ADD_ALL_SKIPS_FIRST_ELEMENT break",
                        BreakableCollection.ADD_ALL_SKIPS_FIRST_ELEMENT,
                        DynamicBrokenCollectionContract::testAddAllToContainer),

                failingTest("testAddAllToContainer() fails with ADD_ALL_SKIPS_LAST_ELEMENT break",
                        BreakableCollection.ADD_ALL_SKIPS_LAST_ELEMENT,
                        DynamicBrokenCollectionContract::testAddAllToContainer)
        );
    }

    /// Test factory for tests of the clear() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the clear() method.
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForClear() {
        return Arrays.asList(
                failingTest("testClearOnCollectionWithElements() fails with CLEAR_DOES_NOT_REMOVE_ANY_ELEMENTS break",
                        BreakableCollection.CLEAR_DOES_NOT_REMOVE_ANY_ELEMENTS,
                        DynamicBrokenCollectionContract::testClearOnCollectionWithElements),

                failingTest("testClearOnCollectionWithElements() fails with CLEAR_SKIPS_FIRST_ELEMENT break",
                        BreakableCollection.CLEAR_SKIPS_FIRST_ELEMENT,
                        DynamicBrokenCollectionContract::testClearOnCollectionWithElements),

                failingTest("testClearOnCollectionWithElements() fails with CLEAR_SKIPS_LAST_ELEMENT break",
                        BreakableCollection.CLEAR_SKIPS_LAST_ELEMENT,
                        DynamicBrokenCollectionContract::testClearOnCollectionWithElements)
        );
    }

    /// Test factory for tests of the contains(Object) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the contains(Object) method.
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForContains() {
        return Arrays.asList(
                failingTest("testContainsOnCollectionWithElements() fails with CONTAINS_ALWAYS_RETURNS_TRUE break",
                        BreakableCollection.CONTAINS_ALWAYS_RETURNS_TRUE,
                        DynamicBrokenCollectionContract::testContainsOnCollectionWithElements),

                failingTest("testContainsOnCollectionWithElements() fails with CONTAINS_ALWAYS_RETURNS_FALSE break",
                        BreakableCollection.CONTAINS_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenCollectionContract::testContainsOnCollectionWithElements),

                failingTest("testContainsOnCollectionWithElements() fails with CONTAINS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.CONTAINS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::testContainsOnCollectionWithElements),

                failingTest("testContainsOnEmptyCollection() fails with CONTAINS_ALWAYS_RETURNS_TRUE break",
                        BreakableCollection.CONTAINS_ALWAYS_RETURNS_TRUE,
                        DynamicBrokenCollectionContract::testContainsOnEmptyCollection),

                failingTest("testContainsOnEmptyCollection() fails with CONTAINS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.CONTAINS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::testContainsOnEmptyCollection)
                );
    }

    /// Test factory for tests of the contains(Object) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the contains(Object) method.
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForContainsAll() {
        return Arrays.asList(
                failingTest("testContainsAllOnCollectionWithElements() fails with CONTAINS_ALWAYS_RETURNS_TRUE break",
                        BreakableCollection.CONTAINS_ALL_ALWAYS_RETURNS_TRUE,
                        DynamicBrokenCollectionContract::testContainsAllOnCollectionWithElements),

                failingTest("testContainsAllOnCollectionWithElements() fails with CONTAINS_ALWAYS_RETURNS_FALSE break",
                        BreakableCollection.CONTAINS_ALL_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenCollectionContract::testContainsAllOnCollectionWithElements),

                failingTest("testContainsAllOnCollectionWithElements() fails with CONTAINS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.CONTAINS_ALL_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::testContainsAllOnCollectionWithElements)
        );
    }

    /// Test factory for tests of the isEmpty() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the isEmpty() method.
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForIsEmpty() {
        return Arrays.asList(
                failingTest("testIsEmptyForEmptyCollection() fails with IS_EMPTY_ALWAYS_RETURNS_FALSE break",
                        BreakableCollection.IS_EMPTY_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenCollectionContract::testIsEmptyForEmptyCollection),

                failingTest("testIsEmptyForEmptyCollection() fails with IS_EMPTY_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.IS_EMPTY_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::testIsEmptyForEmptyCollection),

                failingTest("testIsEmptyForNonEmptyCollection() fails with IS_EMPTY_ALWAYS_RETURNS_TRUE break",
                        BreakableCollection.IS_EMPTY_ALWAYS_RETURNS_TRUE,
                        DynamicBrokenCollectionContract::testIsEmptyForNonEmptyCollection),

                failingTest("v() fails with IS_EMPTY_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.IS_EMPTY_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::testIsEmptyForNonEmptyCollection)
        );
    }


    /// Test factory for tests of the remove(Object) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the remove(Object) method.
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForRemove() {
        return Arrays.asList(
                failingTest("testRemoveOnEmptyContainer() fails with REMOVE_ALWAYS_RETURNS_TRUE break",
                        BreakableCollection.REMOVE_ALWAYS_RETURNS_TRUE,
                        DynamicBrokenCollectionContract::testRemoveOnEmptyContainer),

                failingTest("testRemoveOnEmptyContainer() fails with REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::testRemoveOnEmptyContainer),

                failingTest("testRemoveOnContainerWithElements() fails with REMOVE_DOES_NOT_REMOVE_ELEMENT break",
                        BreakableCollection.REMOVE_DOES_NOT_REMOVE_ELEMENT,
                        DynamicBrokenCollectionContract::testRemoveOnContainerWithElements),

                failingTest("testRemoveOnContainerWithElements() fails with REMOVE_ALWAYS_RETURNS_FALSE break",
                        BreakableCollection.REMOVE_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenCollectionContract::testRemoveOnContainerWithElements),

                failingTest("testRemoveOnContainerWithElements() fails with REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::testRemoveOnContainerWithElements),

                failingTest("testRemoveOnContainerWithNulls() fails with REMOVE_DOES_NOT_REMOVE_ELEMENT break",
                        BreakableCollection.REMOVE_DOES_NOT_REMOVE_ELEMENT,
                        DynamicBrokenCollectionContract::testRemoveOnContainerWithNulls),

                failingTest("testRemoveOnContainerWithNulls() fails with REMOVE_ALWAYS_RETURNS_FALSE break",
                        BreakableCollection.REMOVE_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenCollectionContract::testRemoveOnContainerWithNulls),

                failingTest("testRemoveOnContainerWithNulls() fails with REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::testRemoveOnContainerWithNulls)
                );
    }

    /// Test factory for tests of the removeAll(Collection) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the removeAll(Collection) method.
    @TestFactory
    @Disabled
    public Collection<DynamicTest> dynamicTestsForRemoveAll() {
        return Arrays.asList(
                failingTest("testRemoveAllOnEmptyContainer() fails with REMOVE_ALL_ALWAYS_RETURNS_TRUE break",
                        BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_TRUE,
                        DynamicBrokenCollectionContract::testRemoveAllOnEmptyContainer),

                failingTest("testRemoveAllOnEmptyContainer() fails with REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::testRemoveAllOnEmptyContainer),

                failingTest("testRemoveAllOnContainerWithElements() fails with REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS break",
                        BreakableCollection.REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS,
                        DynamicBrokenCollectionContract::testRemoveAllOnContainerWithElements),

                failingTest("testRemoveAllOnContainerWithElements() fails with REMOVE_ALL_SKIPS_FIRST_ELEMENT break",
                        BreakableCollection.REMOVE_ALL_SKIPS_FIRST_ELEMENT,
                        DynamicBrokenCollectionContract::testRemoveAllOnContainerWithElements),

                failingTest("testRemoveAllOnContainerWithElements() fails with REMOVE_ALL_SKIPS_LAST_ELEMENT break",
                        BreakableCollection.REMOVE_ALL_SKIPS_LAST_ELEMENT,
                        DynamicBrokenCollectionContract::testRemoveAllOnContainerWithElements),

                failingTest("testRemoveAllOnContainerWithElements() fails with REMOVE_ALL_ALWAYS_RETURNS_FALSE break",
                        BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenCollectionContract::testRemoveAllOnContainerWithElements),

                failingTest("testRemoveAllOnContainerWithElements() fails with REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::testRemoveAllOnContainerWithElements),

                failingTest("testRemoveAllOnNullElement() fails with REMOVE_ALL_ALWAYS_RETURNS_TRUE break",
                        BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_TRUE,
                        DynamicBrokenCollectionContract::testRemoveAllOnNullElement),

                failingTest("testRemoveAllOnNullElement() fails with REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::testRemoveAllOnNullElement),

                failingTest("testRemoveAllOnIncompatibleObject() fails with REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS break",
                        BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_TRUE,
                        DynamicBrokenCollectionContract::testRemoveAllOnIncompatibleObject),

                failingTest("testRemoveAllThrowsOnNullCollection() fails with REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS break",
                        BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_TRUE,
                        DynamicBrokenCollectionContract::testRemoveAllThrowsOnNullCollection)
        );
    }

    /// Test factory for tests of the removeIf() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the removeIf() method.
    @TestFactory
    @Disabled
    public Collection<DynamicTest> dynamicTestsForRemoveIf() {
        return Arrays.asList(
                failingTest("testRemoveIfOnEmptyContainer() fails with REMOVE_ALL_ALWAYS_RETURNS_TRUE break",
                        BreakableCollection.REMOVE_IF_ALWAYS_RETURNS_TRUE,
                        DynamicBrokenCollectionContract::testRemoveIfOnEmptyContainer),

                failingTest("testRemoveIfOnEmptyContainer() fails with REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::testRemoveIfOnEmptyContainer),

                failingTest("testRemoveAllOnContainerWithElements() fails with REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS break",
                        BreakableCollection.REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS,
                        DynamicBrokenCollectionContract::testRemoveAllOnContainerWithElements),

                failingTest("testRemoveAllOnContainerWithElements() fails with REMOVE_ALL_SKIPS_FIRST_ELEMENT break",
                        BreakableCollection.REMOVE_ALL_SKIPS_FIRST_ELEMENT,
                        DynamicBrokenCollectionContract::testRemoveAllOnContainerWithElements),

                failingTest("testRemoveAllOnContainerWithElements() fails with REMOVE_ALL_SKIPS_LAST_ELEMENT break",
                        BreakableCollection.REMOVE_ALL_SKIPS_LAST_ELEMENT,
                        DynamicBrokenCollectionContract::testRemoveAllOnContainerWithElements),

                failingTest("testRemoveAllOnContainerWithElements() fails with REMOVE_ALL_ALWAYS_RETURNS_FALSE break",
                        BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenCollectionContract::testRemoveAllOnContainerWithElements),

                failingTest("testRemoveAllOnContainerWithElements() fails with REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::testRemoveAllOnContainerWithElements),

                failingTest("testRemoveAllOnNullElement() fails with REMOVE_ALL_ALWAYS_RETURNS_TRUE break",
                        BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_TRUE,
                        DynamicBrokenCollectionContract::testRemoveAllOnNullElement),

                failingTest("testRemoveAllOnNullElement() fails with REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE,
                        DynamicBrokenCollectionContract::testRemoveAllOnNullElement),

                failingTest("testRemoveAllOnIncompatibleObject() fails with REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS break",
                        BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_TRUE,
                        DynamicBrokenCollectionContract::testRemoveAllOnIncompatibleObject),

                failingTest("testRemoveAllThrowsOnNullCollection() fails with REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS break",
                        BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_TRUE,
                        DynamicBrokenCollectionContract::testRemoveAllThrowsOnNullCollection)
            );
    }
}