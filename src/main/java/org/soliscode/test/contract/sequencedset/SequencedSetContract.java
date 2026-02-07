package org.soliscode.test.contract.sequencedset;

import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.contract.sequencedcollection.SequencedCollectionContract;
import org.soliscode.test.contract.set.SetContract;

import java.util.SequencedSet;

/// Test suite for classes that implement the {@link SequencedSet} interface.
///
/// This contract extends {@link SetContract} and {@link SequencedCollectionContract} and is intended
/// for testing implementations of the `SequencedSet` interface. `SequencedSet` is a `Set` that is
/// also a `SequencedCollection`. It provides additional methods for positional access and
/// manipulation of elements, while maintaining set uniqueness constraints.
///
/// When implementing this contract, the only method that needs to be implemented is
/// [provider()][org.soliscode.test.contract.support.CollectionContractSupport#provider()].
///
/// ## Usage Example
///
/// To create a test class for `LinkedHashSet`, the following implementation would be appropriate:
///
/// ```java
/// public class LinkedHashSetTest
///         extends AbstractTest<Integer>
///         implements SequencedSetContract<Integer, LinkedHashSet<Integer>>, WithIntegerElement<LinkedHashSet<Integer>> {
///
///     public LinkedHashSetTest() {
///         permitNulls(true);
///         permitIncompatibleTypes(true);
///         // Sets do not permit duplicates by definition.
///         permitDuplicates(false);
///     }
///
///     @Override
///     public CollectionProvider<Integer, LinkedHashSet<Integer>> provider() {
///         return CollectionProviders.provideLinkedHashSet(elementProvider());
///     }
/// }
/// ```
///
/// ## Thread Safety
///
/// Implementations of this contract are expected to be thread-safe for use by the JUnit test runner.
/// The tested `SequencedSet` instance itself should be handled according to its own thread safety guarantees.
///
/// @param <E> The element type being tested.
/// @param <S> The sequenced set type being tested.
/// @author evanbergstrom
/// @see SequencedSet
/// @since 1.0
public interface SequencedSetContract<E, S extends SequencedSet<E>>
        extends SetContract<E, S>, SequencedCollectionContract<E, S> {

    @Override
    boolean supportsMethod(InterfaceMethod method);

    @Override
    void doesNotSupportMethod(InterfaceMethod method);
}
