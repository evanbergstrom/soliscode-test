package org.soliscode.test.contract.iterable;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.collection.StreamContract;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.provider.CollectionProvider;
import org.soliscode.test.provider.FunctionalCollectionProvider;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Tests that the spliterator and stream contracts check the order of the elements only where the spliterator is
/// [ORDERED][Spliterator#ORDERED], and do check it there (soliscode-quant T-0019).
@DisplayName("Spliterator and stream contracts check order only for an ORDERED spliterator")
public class TraversalOrderContractTest {

    /// Default constructor.
    public TraversalOrderContractTest() {
        super();
    }

    @Test
    @DisplayName("SpliteratorMethodContract: an ORDERED spliterator whose trySplit returns the suffix fails")
    void spliteratorContract_orderedSplitReturningSuffix_fails() {
        var contract = new SpliteratorRun<>(SplitsOffSuffix::new);
        assertThrows(AssertionError.class, contract::trySplit_whenNotEmpty_returnsNonNullSpliterator);
    }

    @Test
    @DisplayName("SpliteratorMethodContract: a spliterator that is not ORDERED may give the elements in any order")
    void spliteratorContract_unorderedInAnotherOrder_passes() {
        var contract = new SpliteratorRun<>(UnorderedReversed::new);
        assertDoesNotThrow(contract::forEachRemaining_whenNotEmpty_traversesAllElements);
        assertDoesNotThrow(contract::tryAdvance_whenNotEmpty_traversesAllElements);
        assertDoesNotThrow(contract::trySplit_whenNotEmpty_returnsNonNullSpliterator);
    }

    @Test
    @DisplayName("StreamContract: a collection whose spliterator is not ORDERED may stream in another order")
    void streamContract_unorderedInAnotherOrder_passes() {
        var contract = new StreamRun();
        assertDoesNotThrow(contract::stream_whenNotEmpty_returnsStreamWithElements);
        assertDoesNotThrow(contract::parallelStream_whenNotEmpty_returnsStreamWithElements);
    }

    @Test
    @DisplayName("SpliteratorMethodContract.CHARACTERISTIC_VALUES cannot be changed")
    void characteristicValues_isUnmodifiable() {
        assertThrows(UnsupportedOperationException.class,
                () -> SpliteratorMethodContract.CHARACTERISTIC_VALUES.set(0, 0));
    }

    /// Makes an iterable from the elements of a collection.
    @FunctionalInterface
    private interface IterableFactory<I extends Iterable<Integer>> {
        I create(Collection<Integer> elements);
    }

    /// Runs the spliterator contract's tests, one at a time, against iterables from a factory.
    private static final class SpliteratorRun<I extends Iterable<Integer>> extends AbstractTest
            implements SpliteratorMethodContract<I, Integer>, WithIntegerElement {
        private final IterableFactory<I> factory;

        SpliteratorRun(final IterableFactory<I> factory) {
            this.factory = factory;
        }

        @Override
        public @NonNull CollectionProvider<I, Integer> provider() {
            return new FunctionalCollectionProvider<>(() -> factory.create(List.of()),
                    (i) -> factory.create(copy(i)), factory::create, elementProvider());
        }
    }

    /// Runs the stream contract's tests against [UnorderedReversed] collections.
    private static final class StreamRun extends AbstractTest
            implements StreamContract<UnorderedReversed, Integer>, WithIntegerElement {
        @Override
        public @NonNull CollectionProvider<UnorderedReversed, Integer> provider() {
            return new FunctionalCollectionProvider<>(() -> new UnorderedReversed(List.of()),
                    (c) -> new UnorderedReversed(c), UnorderedReversed::new, elementProvider());
        }
    }

    private static List<Integer> copy(final Iterable<Integer> iterable) {
        List<Integer> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }

    /// A collection whose iterator gives its elements in order, and whose spliterator, which is not ORDERED, gives them
    /// in reverse.
    private static final class UnorderedReversed extends AbstractCollection<Integer> {
        private final List<Integer> elements;

        UnorderedReversed(final Collection<Integer> c) {
            this.elements = new ArrayList<>(c);
        }

        @Override
        public @NonNull Iterator<Integer> iterator() {
            return elements.iterator();
        }

        @Override
        public int size() {
            return elements.size();
        }

        @Override
        public @NonNull Spliterator<Integer> spliterator() {
            return Spliterators.spliterator(elements.reversed(), 0);
        }
    }

    /// An iterable whose spliterator is ORDERED but whose `trySplit` returns the second half, not the first.
    private static final class SplitsOffSuffix implements Iterable<Integer> {
        private final List<Integer> elements;

        SplitsOffSuffix(final Collection<Integer> c) {
            this.elements = new ArrayList<>(c);
        }

        @Override
        public @NonNull Iterator<Integer> iterator() {
            return elements.iterator();
        }

        @Override
        public @NonNull Spliterator<Integer> spliterator() {
            return new SuffixSplitter(elements, 0, elements.size());
        }
    }

    /// The spliterator of [SplitsOffSuffix]: it covers `[low, high)` of the list.
    private static final class SuffixSplitter implements Spliterator<Integer> {
        private final List<Integer> list;
        private int low;
        private int high;

        SuffixSplitter(final List<Integer> list, final int low, final int high) {
            this.list = list;
            this.low = low;
            this.high = high;
        }

        @Override
        public boolean tryAdvance(final Consumer<? super Integer> action) {
            if (low >= high) {
                return false;
            }
            action.accept(list.get(low++));
            return true;
        }

        @Override
        public @Nullable Spliterator<Integer> trySplit() {
            int mid = (low + high) >>> 1;
            if (mid == low) {
                return null;
            }
            Spliterator<Integer> suffix = new SuffixSplitter(list, mid, high);
            high = mid;
            return suffix;
        }

        @Override
        public long estimateSize() {
            return high - low;
        }

        @Override
        public int characteristics() {
            return ORDERED | SIZED | SUBSIZED;
        }
    }
}
