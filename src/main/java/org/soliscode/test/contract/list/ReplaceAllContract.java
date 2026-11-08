package org.soliscode.test.contract.list;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.List;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [replaceAll(UnaryOperator)][List#replaceAll(UnaryOperator)] method of the [List] interface.
///
/// @param <L> The type of the list
/// @param <E> The element type for the list
/// @author evanbergstrom
/// @since 1.0
/// @see List#replaceAll(UnaryOperator)
/// @see ListContract
public interface ReplaceAllContract<L extends List<E>, E> extends CollectionContractSupport<L, E> {

    /// Tests that [replaceAll(UnaryOperator)][List#replaceAll(UnaryOperator)] replaces every element, the first and
    /// last included, with the operator's result.
    @Test
    @DisplayName("replaceAll(UnaryOperator) replaces every element")
    default void replaceAll_whenCalled_replacesEveryElement() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE * 2);
        List<E> initial = values.subList(0, DEFAULT_SIZE);
        List<E> replacements = values.subList(DEFAULT_SIZE, DEFAULT_SIZE * 2);
        UnaryOperator<E> operator = (e) -> replacements.get(initial.indexOf(e));
        L list = provider().createInstance(initial);
        if (supportsMethod(ListMethods.REPLACE_ALL)) {
            list.replaceAll(operator);
            assertIterableEquals(replacements, list);
        } else {
            assertThrows(UnsupportedOperationException.class, () -> list.replaceAll(operator));
        }
    }

    /// Tests that [replaceAll(UnaryOperator)][List#replaceAll(UnaryOperator)] throws [NullPointerException] for a
    /// null operator.
    @Test
    @DisplayName("replaceAll(UnaryOperator) throws NullPointerException for a null operator")
    default void replaceAll_withNullOperator_throwsNullPointerException() {
        if (supportsMethod(ListMethods.REPLACE_ALL)) {
            L list = provider().createInstanceWithUniqueElements();
            assertThrows(NullPointerException.class, () -> list.replaceAll(null));
        }
    }
}
