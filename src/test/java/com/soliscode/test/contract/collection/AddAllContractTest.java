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
public class AddAllContractTest extends AbstractCollectionContractTest<BreakableCollection<Integer>, Integer> {

    /// Default constructor
    public AddAllContractTest() {
        super();
    }

    protected static final class DynamicAddAllContract
            extends DynamicCollectionContract<BreakableCollection<Integer>, Integer>
            implements AddAllContract<BreakableCollection<Integer>, Integer> {

        public DynamicAddAllContract(
                final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                final ObjectProvider<Integer> elementProvider,
                final CollectionContractConfig config) {
            super(builder, elementProvider, config);
        }
    }

    @Override
    protected DynamicAddAllContract
        createTest(final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                   final CollectionContractConfig config) {

        return new DynamicAddAllContract(builder, new IntegerProvider(), config);
    }

    private BreakableCollection.Builder<Integer> builder() {
        return new BreakableCollection.Builder<>();
    }


    /// Test factory for tests of the addAll(Collection) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the addAll(Collection) method.
    @TestFactory
    @DisplayName("addAll(Collection) on an empty collection fails for each break of addAll")
    public Collection<DynamicTest> testsForAddAllWhenContainerIsEmpty() {
        return Arrays.asList(
                shouldFail(DynamicAddAllContract::addAllWhenContainerIsEmptyAddsAllArgumentElements,
                        "addAllWhenContainerIsEmptyAddsAllArgumentElements() fails with " +
                                "ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS break",
                        builder().addBreak(BreakableCollection.ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS)),

                shouldFail(DynamicAddAllContract::addAllWhenContainerIsEmptyAddsAllArgumentElements,
                        "addAllWhenContainerIsEmptyAddsAllArgumentElements() fails with ADD_" +
                                "ADD_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.ADD_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldFail(DynamicAddAllContract::addAllWhenContainerIsEmptyAddsAllArgumentElements,
                        "addAllWhenContainerIsEmptyAddsAllArgumentElements() fails with ADD_" +
                                "ADD_ALL_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(BreakableCollection.ADD_ALL_ALWAYS_RETURNS_FALSE)),

                shouldFail(DynamicAddAllContract::addAllWhenContainerIsEmptyAddsAllArgumentElements,
                        "addAllWhenContainerIsEmptyAddsAllArgumentElements() fails with ADD_" +
                                "ADD_ALL_SKIPS_FIRST_ELEMENT break",
                        builder().addBreak(BreakableCollection.ADD_ALL_SKIPS_FIRST_ELEMENT)),

                shouldFail(DynamicAddAllContract::addAllWhenContainerIsEmptyAddsAllArgumentElements,
                        "addAllWhenContainerIsEmptyAddsAllArgumentElements() fails with ADD_" +
                                "ADD_ALL_SKIPS_LAST_ELEMENT break",
                        builder().addBreak(BreakableCollection.ADD_ALL_SKIPS_LAST_ELEMENT)),

                shouldPass(DynamicAddAllContract::addAllWhenContainerIsEmptyAddsAllArgumentElements,
                        "addAllWhenContainerIsEmptyAddsAllArgumentElements() passes with no breaks",
                        builder().doesNotSupportMethod(CollectionMethods.ADD_ALL))
        );
    }

 }
