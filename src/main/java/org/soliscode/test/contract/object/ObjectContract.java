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

/// This interface tests if a class has implemented the `Object` interface as specified in [Object]. The following
/// table lists the methods that are tested by this contract interface and the methods that are not tested.
///
/// | Tested      | Not Tested           |
/// | ----------- | -------------------- |
/// | equals      | getClass             |
/// | hashCode    | clone                |
/// | toString    | notify               |
/// |             | notifyAll            |
/// |             | wait                 |
/// |             | finalize             |
///
/// This interface is a mix-in class that can be used by a test class to test the implementation if the `Object`
/// interface specification:
/// ```java
/// public class MyClassTest extends ObjectContract<MyClass> {
/// }
/// ```
/// @param <T> The type being tested.
/// @author evanbergstrom
/// @see Object
/// @see ObjectMethods
/// @see EqualsContract
/// @see HashCodeContract
/// @see ToStringContract
/// @since 1.0
public interface ObjectContract<T>
    extends EqualsContract<T>, HashCodeContract<T>, ToStringContract<T> {
}
