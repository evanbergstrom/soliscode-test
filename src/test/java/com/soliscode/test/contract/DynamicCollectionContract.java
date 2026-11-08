package com.soliscode.test.contract;

import com.soliscode.test.breakable.Breakable;
import com.soliscode.test.breakable.BreakableBuilder;
import com.soliscode.test.breakable.BreakableCollectionBuilder;
import com.soliscode.test.contract.support.CollectionProviderSupport;
import com.soliscode.test.provider.CollectionProvider;
import com.soliscode.test.provider.ObjectProvider;

public class DynamicCollectionContract<C extends Breakable & Iterable<E>, E>
        extends DynamicContract<C> implements CollectionProviderSupport<C, E>, CollectionContractConfig {

    private final BreakableCollectionBuilder<C, E> builder;
    private final ObjectProvider<E> elementProvider;
    private final CollectionContractConfig config;

    public DynamicCollectionContract(final BreakableCollectionBuilder<C, E> builderArg,
                                     final ObjectProvider<E> elementProviderArg,
                                     final CollectionContractConfig configArg) {
        super(builderArg);
        this.builder = builderArg;
        this.elementProvider = elementProviderArg;
        this.config = configArg;
    }

    /// Returns the objectProvider for instances of the class being tested.
    ///
    /// @return the objectProvider instance.
    @Override
    public CollectionProvider<C, E> provider() {
        return BreakableBuilder.collectionProvider(builder, elementProvider);
    }

    @Override
    public ObjectProvider<E> elementProvider() {
        return elementProvider;
    }

    @Override
    public boolean permitNulls() {
        return config.permitNulls();
    }

    @Override
    public boolean permitDuplicates() {
        return config.permitDuplicates();
    }

    @Override
    public boolean permitIncompatibleTypes() {
        return config.permitIncompatibleTypes();
    }
}
