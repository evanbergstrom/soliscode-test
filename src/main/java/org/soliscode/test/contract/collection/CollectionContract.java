package org.soliscode.test.contract.collection;

import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.contract.CollectionContractConfig;
import org.soliscode.test.contract.iterable.IterableContract;

import java.util.Collection;

/// Test suite for classes that implement the {@link Collection} interface. When implementing  this class, the only
/// method that will need to be implemented is
/// [CollectionContractSupport.provider\(\)][org.soliscode.test.contract.support.CollectionContractSupport#provider()].
/// Also, the following methods will allow the tests to be configured based upon the desired behavior of the collection
/// class being tested:
///
/// - [permitNulls][CollectionContractConfig#permitNulls]: Specifies the collection permits null value elements.
/// - [permitDuplicates][CollectionContractConfig#permitDuplicates]: Specifies the collection permits duplicate values.
/// - [permitIncompatibleTypes][CollectionContractConfig#permitIncompatibleTypes]: Specifies if the collection allows
///       the search for incompatible values.
///
/// For example, most of the collections from the JDK permit null values, permit duplicate values, and permit the
/// search for incompatible types. To create a test class for ArrayList, the following constructor would be
/// appropriate:
/// ```java
///     public class ArrayListTest
///         extends AbstractTest<Integer>
///         implements TestCollection<ArrayList<Integer>, Integer>, WithIntegerElement<ArrayList<Integer>> {
///
///     public ArrayListTest() {
///         permitNulls(true);
///         permitIncompatibleTypes(true);
///         permitDuplicates(true);
///     }
/// }
/// ```
///
/// @param <E> The element type being tested.s
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see Collection
/// @since 1.0
public interface CollectionContract<E, C extends Collection<E>> extends IterableContract<E, C>,
        CollectionContractConfig,
        AddContract<E, C>,
        AddAllContract<E, C>,
        ClearContract<E, C>,
        ContainsContract<E, C>,
        ContainsAllContract<E, C>,
        IsEmptyContract<E, C>,
        RemoveContract<E, C>,
        RemoveAllContract<E, C>,
        RemoveIfContract<E, C>,
        RetainAllContract<E, C>,
        SizeContract<E, C>,
        StreamContract<E, C>,
        ToArrayContract<E, C> {

    @Override
    boolean supportsMethod(InterfaceMethod method);

    /// Specific if the test collection supports the methods that allow modification. It is a convenience function to
    /// set the support state for all the modification methods at once. These methods are:
    ///
    /// - [add_singleElement_returnsTrueAndUpdatesSize\(Object\)][Collection#add(Object)]
    /// - [addAll\(Collection\)][Collection#addAll(Collection)]
    /// - [clear\(\)][Collection#clear]
    /// - [remove\(Object\)][Collection#remove(Object)]
    /// - [removeAll\(Collection\)][Collection#removeAll(Collection)]
    /// - [removeIf\(Predicate\)][Collection#removeIf(java.util.function.Predicate)]
    /// - [retainAll\(Collection\)][Collection#retainAll(Collection)]
    /// - [size\(\)][Collection#size]
    /// - [Iterator.remove\(\)][java.util.Iterator#remove()]
    default void doesNotSupportModification() {
        IterableContract.super.doesNotSupportModification();
        doesNotSupportMethod(CollectionMethods.ADD);
        doesNotSupportMethod(CollectionMethods.ADD_ALL);
        doesNotSupportMethod(CollectionMethods.CLEAR);
        doesNotSupportMethod(CollectionMethods.REMOVE);
        doesNotSupportMethod(CollectionMethods.REMOVE_ALL);
        doesNotSupportMethod(CollectionMethods.REMOVE_IF);
        doesNotSupportMethod(CollectionMethods.RETAIN_ALL);
    }
}
