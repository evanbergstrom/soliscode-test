/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.object;

/// **Contract for the `Object` interface**
///
/// This interface provides a comprehensive contract for testing the fundamental methods of
/// [Object] as specified in its documentation. It combines tests for `equals()`, `hashCode()`,
/// and `toString()`.
///
/// The following table lists the methods that are tested by this contract interface and
/// those that are not:
///
/// | Tested        | Not Tested    |
/// | ------------- | ------------- |
/// | `equals`      | `getClass`    |
/// | `hashCode`    | `clone`       |
/// | `toString`    | `notify`      |
/// |               | `notifyAll`   |
/// |               | `wait`        |
/// |               | `finalize`    |
///
/// ## Purpose
/// The purpose of this contract is to ensure that a class correctly implements the basic
/// [Object] methods, which is critical for object equality, collection behavior, and
/// debugging.
///
/// ## Usage Examples
/// This interface is a mix-in class that can be used by a test class:
///
/// ```java
/// public class MyClassTest extends AbstractTest implements ObjectContract<MyClass> {
///     @Override
///     public ObjectProvider<MyClass> provider() {
///         return new FunctionalProvider<>(MyClass::new, MyClass::new, MyClass::new);
///     }
/// }
/// ```
///
/// [com.soliscode.test.AbstractTest] holds the method support the contract needs, and the provider makes a default
/// instance, a copy and an instance from a seed (`MyClass()`, `MyClass(MyClass)` and `MyClass(long)`).
/// `ObjectContractTest` compiles and runs this example.
///
/// ## How it reads `Object`'s documentation
/// The tests of `equals`, of `hashCode`'s first two rules and of a non-`null` `toString` follow [Object]'s
/// documentation as written. Some tests read it more strictly, each saying so in its own documentation, and each with
/// an opt-out: [HashCodeMethodContract#hashCodeSpreadsUnequalValues()],
/// [ToStringMethodContract#toStringDistinguishesUnequalValues()] and
/// [ToStringMethodContract#toStringDescribesValue()].
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the object and [com.soliscode.test.provider.ObjectProvider] implementations being tested.
///
/// @param <T> The type being tested.
/// @author evanbergstrom
/// @see Object
/// @see com.soliscode.test.methods.ObjectMethods
/// @see EqualsMethodContract
/// @see HashCodeMethodContract
/// @see ToStringMethodContract
/// @since 1.0.0
public interface ObjectContract<T>
    extends EqualsMethodContract<T>, HashCodeMethodContract<T>, ToStringMethodContract<T> {
}
