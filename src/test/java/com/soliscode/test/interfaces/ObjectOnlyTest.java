package com.soliscode.test.interfaces;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.assertions.Assertions;
import com.soliscode.test.contract.object.ObjectContract;
import com.soliscode.test.provider.FunctionalProvider;
import com.soliscode.test.provider.ObjectProvider;

/// Tests for the [ObjectOnly] class.
///
/// @author evanbergstrom
/// @since 1.0
/// @see ObjectOnly
@DisplayName("Tests for the ObjectOnly class")
public class ObjectOnlyTest extends AbstractTest implements ObjectContract<ObjectOnly> {

    /// Default constructor.
    public ObjectOnlyTest() {
        super();
    }

    @Override
    public ObjectProvider<ObjectOnly> provider() {
        return new FunctionalProvider<>(ObjectOnly::new, ObjectOnly::new, ObjectOnly::new);
    }

    /// Verifies that [ObjectOnly] only implements the [Object] interface.
    ///
    /// @see ObjectOnly
    @DisplayName("the constructor returns an object that implements only Object")
    @Test
    public void constructorWhenCalledReturnsObjectThatImplementsOnlyObject() {
        ObjectOnly object = new ObjectOnly();
        Assertions.assertImplementsOnly(Object.class, object);
    }
}
