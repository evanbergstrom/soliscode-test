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
import org.soliscode.test.OptionalMethodSupport;

import java.util.*;

/// Utility class for implementing the [Breakable] interface.
///
/// @author evanbergstrom
/// @since 1.0
public abstract class AbstractBreakable extends OptionalMethodSupport implements Breakable {

    private final @NotNull Set<Break> breaks;

    /// Create a breakable object with no breaks.
    public AbstractBreakable() {
        super();
        this.breaks = new HashSet<>();
    }

    /// Create a breakable object with the same breaks as another breakable object.
    /// @param other the breakable object to copy.
    public AbstractBreakable(final @NotNull AbstractBreakable other) {
        super(other);
        this.breaks = new HashSet<>(other.breaks);
    }

    /// Create a breakable object with a specified set of breaks.
    /// @param breaks the set of breaks for the object.
    public AbstractBreakable(final @NotNull Collection<Break> breaks) {
        super();
        this.breaks = new HashSet<>(Objects.requireNonNull(breaks));
    }

    @Override
    public boolean hasBreak(final @NotNull Break aBreak) {
        return breaks.contains(Objects.requireNonNull(aBreak));
    }

    @Override
    public @NotNull Set<Break> breaks() {
        return Collections.unmodifiableSet(breaks);
    }

    @Override
    public void addBreaks(final @NotNull Collection<Break> breaks) {
        this.breaks.addAll(breaks);
    }
}
