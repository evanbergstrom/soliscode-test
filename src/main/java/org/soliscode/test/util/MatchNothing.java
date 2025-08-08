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

package org.soliscode.test.util;

import org.jetbrains.annotations.NotNull;

/**
 * An instance of this class will not compare equal to any other object.
 *
 * @author evanbergstrom
 * @since 1.0.0
 */
public class MatchNothing implements Comparable<MatchNothing> {

    /**
     * Always returns {@code false}.
     * @param obj The object to compare for equality (not used).
     * @return The value {@code false}.
     */
    @Override
    public boolean equals(final Object obj) {
        return false;
    }

    /**
     * Always returns {@code 1}.
     * @return The value {@code 1}.
     */
    @Override
    public int hashCode() {
        return 1;
    }

    /**
     * Always returns {@code 1}.
     * @param obj The object to compare (not used).
     * @return The value {@code 1}.
     */
    @SuppressWarnings("ComparatorMethodParameterNotUsed")
    @Override
    public int compareTo(@NotNull final MatchNothing obj) {
        return 1;
    }
}
