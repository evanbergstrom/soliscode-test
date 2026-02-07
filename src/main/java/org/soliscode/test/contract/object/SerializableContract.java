package org.soliscode.test.contract.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.ContractSupport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/// **Contract for the `Serializable` interface**
///
/// This interface defines tests for classes that implement [Serializable]. It verifies that
/// an object can be serialized and deserialized, and that the resulting object is equal to
/// the original.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a class's serialization implementation correctly:
/// - Serializes the object state to a byte stream.
/// - Deserializes the byte stream back into an object instance.
/// - Maintains object equality and hash code consistency after round-trip serialization.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MySerializableTest implements SerializableContract<MyClass> {
///     @Override
///     public ObjectProvider<MyClass> provider() {
///         return MyClass::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the object and [org.soliscode.test.provider.ObjectProvider] implementations being tested.
///
/// @param <T> The type being tested, which must implement [Serializable].
/// @author evanbergstrom
/// @see Serializable
/// @since 1.0.0
public interface SerializableContract<T extends Serializable> extends ContractSupport<T> {

    /// Tests that an object can be successfully serialized and deserialized, and that the
    /// resulting object is equal to the original.
    ///
    /// This test verifies that:
    /// 1. The object can be written to an [ObjectOutputStream].
    /// 2. The object can be read back from an [ObjectInputStream].
    /// 3. The deserialized object is equal to the original object using [Object#equals].
    /// 4. The deserialized object has the same hash code as the original object.
    ///
    /// @see Serializable
    /// @throws org.opentest4j.AssertionFailedError if the deserialized object is not equal to the original or
    ///         if any exceptions are thrown during serialization.
    @DisplayName("serialization round-trip returns an equal object")
    @Test
    @SuppressWarnings("unchecked")
    default void serialize_whenCalled_returnsEqualObject() {
        if (supportsMethod(ObjectMethods.SERIALIZATION)) {
            T original = provider().createInstance();
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                    oos.writeObject(original);
                }

                byte[] bytes = baos.toByteArray();
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                T deserialized;
                try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                    deserialized = (T) ois.readObject();
                }

                assertEquals(original, deserialized, "Deserialized object should be equal to the original");
                assertEquals(original.hashCode(), deserialized.hashCode(),
                        "Deserialized object should have the same hash code as the original");
            } catch (IOException | ClassNotFoundException e) {
                fail("Serialization round-trip failed with exception: " + e.getMessage(), e);
            }
        }
    }
}
