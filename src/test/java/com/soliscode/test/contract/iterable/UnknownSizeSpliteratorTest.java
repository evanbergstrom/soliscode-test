package com.soliscode.test.contract.iterable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.contract.support.WithIntegerElement;
import com.soliscode.test.provider.CollectionProvider;
import com.soliscode.test.provider.FunctionalCollectionProvider;

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
    public CollectionProvider<UnknownSize<Integer>, Integer> provider() {
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
        public Iterator<E> iterator() {
            return elements.iterator();
        }
    }
}
