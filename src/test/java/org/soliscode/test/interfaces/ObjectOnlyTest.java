package org.soliscode.test.interfaces;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.assertions.Assertions;
import org.soliscode.test.contract.object.ObjectContract;
import org.soliscode.test.provider.FunctionalProvider;
import org.soliscode.test.provider.ObjectProvider;

/// Tests for the [ObjectOnly] class.
///
/// @author evanbergstrom
/// @since 1.0
/// @see ObjectOnly
@DisplayName("Tests for the ObjectOnly class")
public class ObjectOnlyTest extends AbstractTest implements ObjectContract<ObjectOnly> {

    @Override
    public @NonNull ObjectProvider<ObjectOnly> provider() {
        return new FunctionalProvider<>(ObjectOnly::new, ObjectOnly::new, ObjectOnly::new);
    }

    /// Verifies that [ObjectOnly] only implements the [Object] interface.
    ///
    /// @see ObjectOnly
    @DisplayName("constructor_whenCalled_returnsObjectThatImplementsOnlyObject")
    @Test
    public void constructor_whenCalled_returnsObjectThatImplementsOnlyObject() {
        ObjectOnly object = new ObjectOnly();
        Assertions.assertImplementsOnly(Object.class, object);
    }
}
