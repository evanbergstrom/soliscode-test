package org.soliscode.test.contract.iterable;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.provider.CollectionProvider;
import org.soliscode.test.provider.FunctionalCollectionProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/// Runs the [SpliteratorMethodContract] against an iterable whose spliterator is the default one, which has unknown
/// size and no characteristics. Its `estimateSize()` stays `Long.MAX_VALUE` after `trySplit()`, which the contract
/// must accept (soliscode-quant T-0019).
@DisplayName("SpliteratorMethodContract on a spliterator of unknown size")
public class UnknownSizeSpliteratorTest extends AbstractTest
        implements SpliteratorMethodContract<UnknownSizeSpliteratorTest.UnknownSize<Integer>, Integer>,
        WithIntegerElement {

    /// Default constructor.
    public UnknownSizeSpliteratorTest() {
        super();
    }

    @Override
    public @NonNull CollectionProvider<UnknownSize<Integer>, Integer> provider() {
        return new FunctionalCollectionProvider<>(() -> new UnknownSize<>(List.of()),
                (u) -> new UnknownSize<>(u.elements), UnknownSize::new, elementProvider());
    }

    /// An iterable with the default [Iterable#spliterator()]: unknown size, no characteristics.
    /// @param <E> the element type
    public static final class UnknownSize<E> implements Iterable<E> {
        private final List<E> elements;

        UnknownSize(final Collection<E> c) {
            this.elements = new ArrayList<>(c);
        }

        @Override
        public @NonNull Iterator<E> iterator() {
            return elements.iterator();
        }
    }
}
