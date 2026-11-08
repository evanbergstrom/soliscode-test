package org.soliscode.test.contract.deque;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.breakable.BreakableCollectionBuilder;
import org.soliscode.test.breakable.BreakableDeque;
import org.soliscode.test.contract.AbstractCollectionContractTest;
import org.soliscode.test.contract.CollectionContractConfig;
import org.soliscode.test.contract.DynamicCollectionContract;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.provider.IntegerProvider;


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
                final @NonNull BreakableCollectionBuilder<BreakableDeque<Integer>, Integer> builder,
                final @NonNull CollectionContractConfig config) {
            super(builder, new IntegerProvider(), config);
        }
    }

    @Override
    protected @NonNull DynamicCollectionContract<BreakableDeque<Integer>, Integer>
        createTest(final BreakableCollectionBuilder<BreakableDeque<Integer>, Integer> builder,
               final @NonNull CollectionContractConfig config) {

        return new DynamicDequeContract(builder, config);
    }
}
