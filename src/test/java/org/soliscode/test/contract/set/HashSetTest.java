package org.soliscode.test.contract.set;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.DoesNotPermitDuplicates;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.provider.CollectionProvider;
import org.soliscode.test.provider.CollectionProviders;

import java.util.HashSet;


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
        implements SetContract<Integer, HashSet<Integer>>, WithIntegerElement, DoesNotPermitDuplicates {

    /// Returns the provider for creating and populating `HashSet` instances.
    ///
    /// This implementation uses [CollectionProviders#from] to provide a
    /// [CollectionProvider] that creates [HashSet] instances with [Integer] elements.
    ///
    /// @return a [CollectionProvider] for `HashSet`
    @Override
    public @NonNull CollectionProvider<Integer, HashSet<Integer>> provider() {
        return CollectionProviders.from(HashSet::new, HashSet::new, HashSet::new, elementProvider());
    }
}
