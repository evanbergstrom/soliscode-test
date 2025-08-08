package org.soliscode.test.contract.list;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.CollectionMethods;
import org.soliscode.test.contract.collection.AddContract;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// This interface tests if a list class has implemented the [sort][List#add(Object)] method correctly.
///
/// @param <E> The element type being tested.
/// @param <L> The list type being tested.
/// @author evanbergstrom
/// @since 1.0
/// @see List#add(Object)
public interface ListAddContract<E, L extends List<E>> extends AddContract<E, L> {

    /// Tests that the [add][List#add] method works.
    @DisplayName("Test that the add method appends to the end of the list")
    @Test
    default void testAdd() {
        if (supportsMethod(CollectionMethods.Add)) {
            List<E> list = provider().emptyInstance();
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            for (int i = 0; i < values.size(); i++) {
                E element = values.get(i);
                list.add(element);
                assertEquals(element, list.get(i));
            }
        } else {
            List<E> list = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, () -> list.add(elementProvider().createInstance()));
        }
    }

}
