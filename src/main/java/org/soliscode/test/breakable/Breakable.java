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
package org.soliscode.test.breakable;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/// Interface for classes that can be "broken" programmatically to test contract classes. Classes implementing
/// this interface can use the utility class [AbstractBreakable] to simplify the implementation.
///
/// @author Evan Bergstrom
/// @since 1.0
/// @see AbstractBreakable
public interface Breakable {

    /// Determines if this instance has a specified break. The default value for all breaks that have not been added is
    /// `false`.
    /// @param aBreak The break to determine if ti has been added to this instance.
    /// @return 'true' if the break has been added, 'false' if it has not.
    boolean hasBreak(final @NotNull Break aBreak);

    /// Returns all of the breaks that have been added to this object.
    /// @return The set of breaks that have ben added.
    @NotNull Set<Break> breaks();

    /// Adds a set of breaks to this object.
    /// @param breaks the breaks to add.
    void addBreaks(final @NotNull Collection<Break> breaks);
}
