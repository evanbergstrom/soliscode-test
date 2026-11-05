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
public class AddAllContractTest extends AbstractCollectionContractTest<BreakableCollection<Integer>, Integer> {

    /// Default constructor
    public AddAllContractTest() {
        super();
    }

    protected static final class DynamicAddAllContract
            extends DynamicCollectionContract<BreakableCollection<Integer>, Integer>
            implements AddAllContract<BreakableCollection<Integer>, Integer> {

        public DynamicAddAllContract(
                final @NonNull BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                final @NonNull ObjectProvider<Integer> elementProvider,
                final @NonNull CollectionContractConfig config) {
            super(builder, elementProvider, config);
        }
    }

    @Override
    protected @NonNull DynamicAddAllContract
        createTest(final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                   final @NonNull CollectionContractConfig config) {

        return new DynamicAddAllContract(builder, new IntegerProvider(), config);
    }

    private BreakableCollection.Builder<Integer> builder() {
        return new BreakableCollection.Builder<>();
    }


    /// Test factory for tests of the addAll(Collection) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the addAll(Collection) method.
    @TestFactory
    public Collection<DynamicTest> testsForAddAllWhenContainerIsEmpty() {
        return Arrays.asList(
                shouldFail(DynamicAddAllContract::addAll_whenContainerIsEmpty_addsAllArgumentElements,
                        "addAll_whenContainerIsEmpty_addsAllArgumentElements() fails with " +
                                "ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS break",
                        builder().addBreak(BreakableCollection.ADD_ALL_DOES_NOT_ADD_ANY_ELEMENTS)),

                shouldFail(DynamicAddAllContract::addAll_whenContainerIsEmpty_addsAllArgumentElements,
                        "addAll_whenContainerIsEmpty_addsAllArgumentElements() fails with ADD_" +
                                "ADD_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.ADD_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldFail(DynamicAddAllContract::addAll_whenContainerIsEmpty_addsAllArgumentElements,
                        "addAll_whenContainerIsEmpty_addsAllArgumentElements() fails with ADD_" +
                                "ADD_ALL_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(BreakableCollection.ADD_ALL_ALWAYS_RETURNS_FALSE)),

                shouldFail(DynamicAddAllContract::addAll_whenContainerIsEmpty_addsAllArgumentElements,
                        "addAll_whenContainerIsEmpty_addsAllArgumentElements() fails with ADD_" +
                                "ADD_ALL_SKIPS_FIRST_ELEMENT break",
                        builder().addBreak(BreakableCollection.ADD_ALL_SKIPS_FIRST_ELEMENT)),

                shouldFail(DynamicAddAllContract::addAll_whenContainerIsEmpty_addsAllArgumentElements,
                        "addAll_whenContainerIsEmpty_addsAllArgumentElements() fails with ADD_" +
                                "ADD_ALL_SKIPS_LAST_ELEMENT break",
                        builder().addBreak(BreakableCollection.ADD_ALL_SKIPS_LAST_ELEMENT)),

                shouldPass(DynamicAddAllContract::addAll_whenContainerIsEmpty_addsAllArgumentElements,
                        "addAll_whenContainerIsEmpty_addsAllArgumentElements() passes with no breaks",
                        builder().doesNotSupportMethod(CollectionMethods.ADD_ALL))
        );
    }

 }