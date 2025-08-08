package org.soliscode.test.contract;

/// A decorator interface that indicates that the collection class being tested does not permit duplicate elements.
/// @author evanbergstrom
/// @since 1.0
public interface DoesNotPermitDuplicates extends CollectionContractConfig {

    /// {@inheritDoc}
    /// @return `false`
    @Override
    default boolean permitDuplicates() {
        return false;
    }
}
