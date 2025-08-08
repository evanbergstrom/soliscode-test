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

package org.soliscode.test.contract.support;

import org.jetbrains.annotations.NotNull;
import org.soliscode.test.contract.object.ObjectContract;
import org.soliscode.test.provider.DoubleProvider;
import org.soliscode.test.provider.ObjectProvider;

/// Provides an implementation of the [#provider()] method that provides instances of [Double]. This is a mix-in class
/// that is meant ot be used alongside a Contract class.
///
/// ```java
/// ```
///
/// @author evanbergstrom
/// @see ObjectContract
/// @see DoubleProvider
/// @since 1.0
public interface WithDouble extends ContractSupport<Double> {

    /// Returns an instance of `ObjectProvider` that provides [Double] objects.
    /// @return an instance of [DoubleProvider].
    default @NotNull ObjectProvider<Double> provider() {
        return new DoubleProvider();
    }
}
