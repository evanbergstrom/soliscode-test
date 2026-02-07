package org.soliscode.test.contract.set;

import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.contract.collection.CollectionContract;

import java.util.Set;

/// Test suite for classes that implement the {@link Set} interface.
///
/// This contract extends {@link CollectionContract} and is intended for testing implementations
/// of the `Set` interface. While the `Set` interface does not add_singleElement_returnsTrueAndUpdatesSize any new methods beyond those
/// defined in {@link java.util.Collection}, it imposes additional stipulations on the constructors
/// and the behavior of the `add_singleElement_returnsTrueAndUpdatesSize`, `equals`, and `hashCode` methods.
///
/// When implementing this contract, the only method that needs to be implemented is
/// [provider()][org.soliscode.test.contract.support.CollectionContractSupport#provider()].
///
/// ## Usage Example
///
/// To create a test class for `HashSet`, the following implementation would be appropriate:
///
/// ```java
/// public class HashSetTest
///         extends AbstractTest<Integer>
///         implements SetContract<Integer, HashSet<Integer>>, WithIntegerElement<HashSet<Integer>> {
///
///     public HashSetTest() {
///         permitNulls(true);
///         permitIncompatibleTypes(true);
///         // Sets do not permit duplicates by definition,
///         // so permitDuplicates(true) would violate Set contract.
///         permitDuplicates(false);
///     }
///
///     @Override
///     public CollectionProvider<Integer, HashSet<Integer>> provider() {
///         return CollectionProviders.provideHashSet(elementProvider());
///     }
/// }
/// ```
///
/// ## Thread Safety
///
/// Implementations of this contract are expected to be thread-safe for use by the JUnit test runner.
/// The tested `Set` instance itself should be handled according to its own thread safety guarantees.
///
/// @param <E> The element type being tested.
/// @param <S> The set type being tested.
/// @author evanbergstrom
/// @see Set
/// @since 1.0
public interface SetContract<E, S extends Set<E>> extends CollectionContract<E, S> {

    @Override
    boolean supportsMethod(InterfaceMethod method);

    @Override
    void doesNotSupportMethod(InterfaceMethod method);
}
