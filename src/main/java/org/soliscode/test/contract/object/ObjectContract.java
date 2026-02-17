/*
 * Copyright 2024 Evan Bergstrom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.soliscode.test.contract.object;

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
/// public class MyClassTest extends ObjectContract<MyClass> {
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
/// @param <T> The type being tested.
/// @author evanbergstrom
/// @see Object
/// @see ObjectMethods
/// @see EqualsMethodContract
/// @see HashCodeMethodContract
/// @see ToStringMethodContract
/// @since 1.0.0
public interface ObjectContract<T>
    extends EqualsMethodContract<T>, HashCodeMethodContract<T>, ToStringMethodContract<T> {
}
