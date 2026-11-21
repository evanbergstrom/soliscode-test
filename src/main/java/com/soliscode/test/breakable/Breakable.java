/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.breakable;

import java.util.Collection;
import java.util.Set;

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
    boolean hasBreak(Break aBreak);

    /// Returns all the breaks that have been added to this object.
    /// @return The set of breaks that have been added.
    Set<Break> breaks();

    /// Adds a break to this object.
    /// @param aBreak the break to add.
    void addBreak(Break aBreak);

    /// Adds a set of breaks to this object.
    /// @param breaks the breaks to add.
    void addBreaks(Collection<Break> breaks);
}
