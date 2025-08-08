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

package org.soliscode.test.contract.numeric;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.IntegerSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test suite for classes that implement the {@link Integer} interface.
 *
 * @param <T> The number type being tested.
 * @author evanbergstrom
 * @since 1.0.0
 */
public interface IntegerContract<T extends Number> extends IntegerSupport<T> {

    /**
     * Tests that the implementation of the {@link Number#intValue()} method works for various values.
     */
    @Test
    @DisplayName("The intValue() function works for various values.")
    default void testIntValue() {
        int max = provider().maxIntegerValue();
        int min = provider().minIntegerValue();

        assertEquals(min, provider().createValue(min).intValue());
        assertEquals(-1, provider().createValue(-1).intValue());
        assertEquals(0, provider().createValue(0).intValue());
        assertEquals(1, provider().createValue(1).intValue());
        assertEquals(max, provider().createValue(max).intValue());
    }

    /**
     * Tests that the implementation of the {@link Number#longValue()} method works for various values.
     */
    @Test
    @DisplayName("The longValue() function works for various values.")
    default void testLongValue() {
        int max = provider().maxIntegerValue();
        int min = provider().minIntegerValue();

        assertEquals(min, provider().createValue(min).longValue());
        assertEquals(-1L, provider().createValue(-1).longValue());
        assertEquals(0L, provider().createValue(0).longValue());
        assertEquals(1L, provider().createValue(1).longValue());
        assertEquals(max, provider().createValue(max).longValue());
    }

    /**
     * Tests that the implementation of the {@link Number#floatValue()} method works for various values.
     */
    @Test
    @DisplayName("The floatValue() function works for various values.")
    default void testFloatValue() {
        int max = provider().maxIntegerValue();
        int min = provider().minIntegerValue();

        assertEquals((float) min, provider().createValue(min).floatValue());
        assertEquals(-1.0f, provider().createValue(-1).floatValue());
        assertEquals(0.0f, provider().createValue(0).floatValue());
        assertEquals(1.0f, provider().createValue(1).floatValue());
        assertEquals((float) max, provider().createValue(max).floatValue());
    }

    /**
     * Tests that the implementation of the {@link Number#doubleValue()} method works for various values.
     */
    @Test
    @DisplayName("The doubleValue() function works for various values.")
    default void testDoubleValue() {
        int max = provider().maxIntegerValue();
        int min = provider().minIntegerValue();

        assertEquals(min, provider().createValue(min).doubleValue());
        assertEquals(-1.0d, provider().createValue(-1).doubleValue());
        assertEquals(0.0d, provider().createValue(0).doubleValue());
        assertEquals(1.0d, provider().createValue(1).doubleValue());
        assertEquals(max, provider().createValue(max).doubleValue());
    }
}
