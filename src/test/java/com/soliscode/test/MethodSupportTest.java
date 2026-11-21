/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.methods.CollectionMethods;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for [MethodSupport#restoreMethodStatuses(Map)] (#59).
public class MethodSupportTest {

    /// Default constructor.
    public MethodSupportTest() {
        super();
    }

    /// Verifies that the statuses restored replace the object's own.
    @Test
    @DisplayName("restoreMethodStatuses replaces the statuses")
    public void restoreMethodStatusesWhenCalledReplacesStatuses() {
        Support support = new Support();
        support.doesNotSupportMethod(CollectionMethods.REMOVE);
        Map<InterfaceMethod, MethodStatus> statuses = new HashMap<>();
        statuses.put(CollectionMethods.ADD, MethodStatus.UNSUPPORTED);
        statuses.put(CollectionMethods.CLEAR, MethodStatus.SUPPORTED);

        support.restoreMethodStatuses(statuses);

        assertEquals(statuses, support.methodStatuses(), "The statuses should be the ones restored");
        assertFalse(support.supportsMethod(CollectionMethods.ADD), "add should be unsupported");
        assertTrue(support.supportsMethod(CollectionMethods.REMOVE), "remove should be supported again");
    }

    /// Verifies that the statuses restored are a copy, which a later change to the argument does not reach.
    @Test
    @DisplayName("restoreMethodStatuses copies its argument")
    public void restoreMethodStatusesWhenArgumentChangedKeepsCopy() {
        Support support = new Support();
        Map<InterfaceMethod, MethodStatus> statuses = new HashMap<>();
        statuses.put(CollectionMethods.ADD, MethodStatus.UNSUPPORTED);

        support.restoreMethodStatuses(statuses);
        statuses.put(CollectionMethods.ADD, MethodStatus.SUPPORTED);
        statuses.put(CollectionMethods.REMOVE, MethodStatus.UNSUPPORTED);

        assertFalse(support.supportsMethod(CollectionMethods.ADD), "add should stay unsupported");
        assertTrue(support.supportsMethod(CollectionMethods.REMOVE), "remove should stay supported");
    }

    /// Verifies that a null map is refused.
    @Test
    @DisplayName("restoreMethodStatuses refuses a null map")
    public void restoreMethodStatusesWhenMapNullThrowsNullPointerException() {
        Support support = new Support();
        assertThrows(NullPointerException.class, () -> support.restoreMethodStatuses(null));
    }

    /// Verifies that a null key is refused, and the statuses are left as they were.
    @Test
    @DisplayName("restoreMethodStatuses refuses a null key")
    public void restoreMethodStatusesWhenKeyNullThrowsNullPointerException() {
        Support support = new Support();
        support.doesNotSupportMethod(CollectionMethods.ADD);
        Map<InterfaceMethod, MethodStatus> statuses = new HashMap<>();
        statuses.put(null, MethodStatus.UNSUPPORTED);

        assertThrows(NullPointerException.class, () -> support.restoreMethodStatuses(statuses));
        assertFalse(support.supportsMethod(CollectionMethods.ADD), "The statuses should be unchanged");
    }

    /// Verifies that a null value is refused, and the statuses are left as they were.
    @Test
    @DisplayName("restoreMethodStatuses refuses a null value")
    public void restoreMethodStatusesWhenValueNullThrowsNullPointerException() {
        Support support = new Support();
        support.doesNotSupportMethod(CollectionMethods.ADD);
        Map<InterfaceMethod, MethodStatus> statuses = new HashMap<>();
        statuses.put(CollectionMethods.REMOVE, null);

        assertThrows(NullPointerException.class, () -> support.restoreMethodStatuses(statuses));
        assertFalse(support.supportsMethod(CollectionMethods.ADD), "The statuses should be unchanged");
    }

    /// The smallest [MethodSupport].
    private static final class Support extends MethodSupport {

        /// Creates an object with no statuses.
        Support() {
            super();
        }
    }
}
