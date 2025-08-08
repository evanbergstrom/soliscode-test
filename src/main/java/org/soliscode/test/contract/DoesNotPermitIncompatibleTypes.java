package org.soliscode.test.contract;

/// A decorator interface that indicates that the collection class being tested does not permit incompatible types.
/// @author evanbergstrom
/// @since 1.0
public interface DoesNotPermitIncompatibleTypes extends CollectionContractConfig {

    /// {@inheritDoc}
    /// @return `false`
    @Override
    default boolean permitIncompatibleTypes() {
        return false;
    }
}
