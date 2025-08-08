package org.soliscode.test.contract;

/// A decorator interface that indicates that the collection class being tested does not permit `null` elements.
/// @author evanbergstrom
/// @since 1.0
public interface DoesNotPermitNulls extends CollectionContractConfig {

    /// {@inheritDoc}
    /// @return `false`
    @Override
    default boolean permitNulls() {
        return false;
    }
}
