package org.soliscode.test.contract;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.breakable.Breakable;
import org.soliscode.test.breakable.BreakableBuilder;
import org.soliscode.test.breakable.BreakableCollectionBuilder;
import org.soliscode.test.contract.support.CollectionProviderSupport;
import org.soliscode.test.provider.CollectionProvider;
import org.soliscode.test.provider.ObjectProvider;

public class DynamicCollectionContract<C extends Breakable & Iterable<E>, E>
        extends DynamicContract<C> implements CollectionProviderSupport<C, E>, CollectionContractConfig {

    private final BreakableCollectionBuilder<C, E> builder;
    private final ObjectProvider<E> elementProvider;
    private final CollectionContractConfig config;

    public DynamicCollectionContract(final @NonNull BreakableCollectionBuilder<C, E> builder,
                                     final @NonNull ObjectProvider<E> elementProvider,
                                     final @NonNull CollectionContractConfig config) {
        super(builder);
        this.builder = builder;
        this.elementProvider = elementProvider;
        this.config = config;
    }

    /// Returns the objectProvider for instances of the class being tested.
    ///
    /// @return the objectProvider instance.
    @Override
    public @NonNull CollectionProvider<C, E> provider() {
        return BreakableBuilder.collectionProvider(builder, elementProvider);
    }

    @Override
    public @NonNull ObjectProvider<E> elementProvider() {
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
