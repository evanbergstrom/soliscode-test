package org.soliscode.test.contract;

import org.jetbrains.annotations.NotNull;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.breakable.Break;
import org.soliscode.test.contract.support.CollectionContractSupport;
import org.soliscode.test.provider.CollectionProvider;
import org.soliscode.test.provider.ObjectProvider;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiFunction;

public abstract class DynamicContract<E,C extends Iterable<E>> extends AbstractTest
            implements CollectionContractSupport<E, C> {

    private final Set<Break> breaks;
    private final BiFunction<ObjectProvider<E>, Set<Break>, CollectionProvider<E, C>> providerCreator;

    DynamicContract(final Break b, final BiFunction<ObjectProvider<E>, Set<Break>, CollectionProvider<E, C>> providerCreator) {
        this.breaks = (b == null) ? Collections.emptySet() : Set.of(b);
        this.providerCreator = providerCreator;
    }

    @Override
    public @NotNull CollectionProvider<E, C> provider() {
        return providerCreator.apply(elementProvider(), breaks);
    }
}
