package org.soliscode.test.contract;

/// Interface for collection contract classes that allows tests to check some characteristics of the class being tested.
///
/// @author evanbergstrom
/// @since 1.0
public interface CollectionContractConfig {
    /**
     * Determines if the collection being tested allows nulls.
     * @return {@code true} if the collection allows nulls, {@code false} otherwise.
     */
    default boolean permitNulls() {
        return true;
    }

    /**
     * Determines if methods of this collection throw an exception when called with an object whose type is
     * incompatible with the element type.
     * @return {@code true} if the collection throws for incompatible types, {@code false} otherwise.
     */
    default boolean permitIncompatibleTypes() {
        return true;
    }

    /**
     * Determines if the collection permits duplicate values.
     * @return {@code true} if the collection permits duplicates, {@code false} otherwise.
     */
    default boolean permitDuplicates() {
        return true;
    }
}
