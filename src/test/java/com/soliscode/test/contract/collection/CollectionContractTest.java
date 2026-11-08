package com.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.breakable.BreakableCollection;
import com.soliscode.test.breakable.BreakableCollectionBuilder;
import com.soliscode.test.contract.AbstractCollectionContractTest;
import com.soliscode.test.contract.CollectionContractConfig;
import com.soliscode.test.contract.DynamicCollectionContract;
import com.soliscode.test.contract.support.WithIntegerElement;
import com.soliscode.test.provider.IntegerProvider;

/// Tests for the CollectionContract class.
///
/// @author evanbergstrom
/// @since 1.0
@DisplayName("Tests for CollectionContract class")
public class CollectionContractTest extends AbstractCollectionContractTest<BreakableCollection<Integer>, Integer> {

    /// Default constructor
    public CollectionContractTest() {
        super();
    }

    /// Unit tests for verifying the behavior and contract compliance of [BreakableCollection].
    ///
    /// This test class is a combination of the following components:
    /// - [AbstractTest]: Serves as a base class, providing utility support for contract testing.
    /// - [CollectionContract]: Implements the comprehensive test suite for validating the
    ///   [Collection] interface methods, ensuring full compliance with its specifications.
    /// - [BreakableCollection.WithProvider]: Supplies a consistent implementation of the
    ///   `objectProvider()` method, tailored for creating unbroken [BreakableCollection] instances.
    /// - [WithIntegerElement]: Mixin interface providing an element objectProvider specifically for
    ///   [Integer] instances to be used in testing.
    ///
    /// ### Responsibilities
    /// This test class ensures the following:
    ///   - All methods of the `Collection` interface are tested for correct functionality as prescribed
    ///     by the [CollectionContract].
    ///   - Instances of [BreakableCollection] are supplied in a consistent and reusable manner using
    ///     the [BreakableCollection.WithProvider] interface.
    ///   - Test cases use [Integer] elements, provided by the implementation of [WithIntegerElement].
    ///
    /// ### Usage Context
    /// Use this class to validate implementations of [BreakableCollection] to ensure:
    ///   - Adherence to the contract defined by [CollectionContract].
    ///   - No breaks exist unless explicitly configured during the test setup.
    ///   - Correct handling of integer-based elements.
    @Nested
    class WorkingCollectionTest extends AbstractTest
            implements CollectionContract<BreakableCollection<Integer>, Integer>,
                BreakableCollection.WithProvider<Integer>,
                WithIntegerElement {
    }

    @Override
    protected DynamicCollectionContract<BreakableCollection<Integer>, Integer>
        createTest(final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                   final CollectionContractConfig config) {

        return new DynamicCollectionContract<>(builder, new IntegerProvider(), config);
    }

    private BreakableCollection.Builder<Integer> builder() {
        return new BreakableCollection.Builder<>();
    }
}
