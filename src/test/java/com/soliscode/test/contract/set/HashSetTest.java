package com.soliscode.test.contract.set;

import java.util.HashSet;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.contract.DoesNotPermitDuplicates;
import com.soliscode.test.contract.support.WithIntegerElement;
import com.soliscode.test.provider.CollectionProvider;
import com.soliscode.test.provider.CollectionProviders;


/// **Contract-based tests for `HashSet`**
///
/// This class provides the `HashSet`-specific implementation of the [SetContract],
/// ensuring that [HashSet] correctly adheres to the [java.util.Set] specification
/// within the SolisCode test framework.
///
/// ## Test Scope
/// This class tests all methods defined in the [java.util.Set] interface as implemented
/// by [HashSet], including:
/// - Basic operations: `add`, `remove`, `contains`, `size`, `isEmpty`
/// - Bulk operations: `addAll`, `retainAll`, `removeAll`, `clear`
/// - Iterator behavior and element uniqueness
///
/// ## Configuration
/// - The tests use [Integer] elements.
/// - Duplicate elements are not permitted, consistent with [HashSet] semantics.
///
/// @author evanbergstrom
/// @see HashSet
/// @see SetContract
/// @since 1.0.0
public class HashSetTest extends AbstractTest
        implements SetContract<HashSet<Integer>, Integer>, WithIntegerElement, DoesNotPermitDuplicates {

    /// Default constructor.
    public HashSetTest() {
        super();
    }

    /// Returns the objectProvider for creating and populating `HashSet` instances.
    ///
    /// This implementation uses [CollectionProviders#from] to provide a
    /// [CollectionProvider] that creates [HashSet] instances with [Integer] elements.
    ///
    /// @return a [CollectionProvider] for `HashSet`
    @Override
    public CollectionProvider<HashSet<Integer>, Integer> provider() {
        return CollectionProviders.from(HashSet::new, HashSet::new, HashSet::new, elementProvider());
    }
}
