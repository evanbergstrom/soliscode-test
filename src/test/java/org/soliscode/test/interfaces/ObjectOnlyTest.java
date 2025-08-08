package org.soliscode.test.interfaces;

import org.jetbrains.annotations.NotNull;
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
public class ObjectOnlyTest extends AbstractTest implements ObjectContract<ObjectOnly> {

    @Override
    public @NotNull ObjectProvider<ObjectOnly> provider() {
        return new FunctionalProvider<>(ObjectOnly::new, ObjectOnly::new, ObjectOnly::new);
    }

    /// Test that `ObjectOnly` only implements the `Object` interface.
    @Test
    @DisplayName("ObjectOnly only implements the Object interface.")
    public void testObjectIsOnlyInterface() {
        ObjectOnly object = new ObjectOnly();
        Assertions.assertImplementsOnly(Object.class, object);
    }
}
