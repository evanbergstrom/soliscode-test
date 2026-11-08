package com.soliscode.test.contract;

import java.util.function.Consumer;

import org.junit.jupiter.api.DynamicTest;

import com.soliscode.test.breakable.Breakable;
import com.soliscode.test.breakable.BreakableCollectionBuilder;

import static com.soliscode.test.assertions.Assertions.assertThrowsAny;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public abstract class AbstractCollectionContractTest<C extends Breakable & Iterable<E>, E> {

    protected static final CollectionContractConfig STANDARD_CONFIG = new CollectionContractConfig() {};

    protected static final CollectionContractConfig CONTRACT_EXPECTS_NO_NULL_SUPPORT = new CollectionContractConfig() {
        @Override
        public boolean permitNulls() {
            return false;
        }
    };

    protected static final CollectionContractConfig CONTRACT_EXPECTS_NO_DUPLICATE_SUPPORT =
            new CollectionContractConfig() {
        @Override
        public boolean permitDuplicates() {
            return false;
        }
    };

    protected static final CollectionContractConfig CONTRACT_EXPECTS_NO_INCOMPATIBLE_TYPE_SUPPORT =
            new CollectionContractConfig() {
        @Override
        public boolean permitIncompatibleTypes() {
            return false;
        }
    };

    protected <X> DynamicTest shouldPass(final Consumer<X> test,
                                         final String description,
                                         final BreakableCollectionBuilder<C, E> builder) {
        return shouldPass(test, STANDARD_CONFIG, description, builder);
    }

    protected <X> DynamicTest shouldPass(final Consumer<X> test,
                                         final CollectionContractConfig config,
                                         final String description,
                                         final BreakableCollectionBuilder<C, E> builder) {
        return dynamicTest(description, () -> assertDoesNotThrow(() -> test.accept((X) createTest(builder, config))));
    }

    protected <X> DynamicTest shouldFail(final Consumer<X> test,
                                         final String description,
                                         final BreakableCollectionBuilder<C, E> builder) {
        return shouldFail(test, STANDARD_CONFIG, description, builder);
    }

    protected <X> DynamicTest shouldFail(final Consumer<X> test,
                                         final CollectionContractConfig config,
                                         final String description,
                                         final BreakableCollectionBuilder<C, E> builder) {
        return dynamicTest(description, () -> assertThrowsAny(() -> test.accept((X) createTest(builder, config))));
    }

    protected abstract DynamicCollectionContract<C, E> createTest(BreakableCollectionBuilder<C, E> builder,
                                                                           CollectionContractConfig config);
}
