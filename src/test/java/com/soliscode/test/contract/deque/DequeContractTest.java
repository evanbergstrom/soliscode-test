package com.soliscode.test.contract.deque;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.breakable.BreakableCollectionBuilder;
import com.soliscode.test.breakable.BreakableDeque;
import com.soliscode.test.contract.AbstractCollectionContractTest;
import com.soliscode.test.contract.CollectionContractConfig;
import com.soliscode.test.contract.DynamicCollectionContract;
import com.soliscode.test.contract.support.WithIntegerElement;
import com.soliscode.test.provider.IntegerProvider;


/// Tests for the [DequeContract] interface.
@DisplayName("Tests for DequeContract class")
public class DequeContractTest extends AbstractCollectionContractTest<BreakableDeque<Integer>, Integer> {

    /// Default constructor.
    public DequeContractTest() {
        super();
    }

    @Nested
    class WorkingCollectionTest extends AbstractTest
            implements DequeContract<BreakableDeque<Integer>, Integer>,
                BreakableDeque.WithProvider<Integer>,
                WithIntegerElement {
    }

    protected static final class DynamicDequeContract
            extends DynamicCollectionContract<BreakableDeque<Integer>, Integer>
            implements DequeContract<BreakableDeque<Integer>, Integer> {

        public DynamicDequeContract(
                final BreakableCollectionBuilder<BreakableDeque<Integer>, Integer> builder,
                final CollectionContractConfig config) {
            super(builder, new IntegerProvider(), config);
        }
    }

    @Override
    protected DynamicCollectionContract<BreakableDeque<Integer>, Integer>
        createTest(final BreakableCollectionBuilder<BreakableDeque<Integer>, Integer> builder,
               final CollectionContractConfig config) {

        return new DynamicDequeContract(builder, config);
    }
}
